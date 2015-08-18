package surrogate;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.simple.JSONObject;

public class OffloadComputationWorker implements Runnable {
	final static long SLEEP_TIME_DATA_REQUEST = 1000; //ms
	final static long TIME_OUT_DATA_REQUEST = 30000; //ms
	private static final int REGRESSION_IMAGE_WIDTH = 400;	//pixels
	private static final int REGRESSION_IMAGE_HEIGHT = 400; //pixels
	private static final int NUMBER_OF_DAYS_PREDICTION = 7; //days
	
	private ComputationRequest computationRequest;
	public volatile StorageManager storageManager;
	ConcurrentLinkedQueue<ComputationRequest> computationRequestQueue;
	
	public OffloadComputationWorker(ComputationRequest computationRequest, StorageManager storageManager, ConcurrentLinkedQueue<ComputationRequest> computationRequestQueue) {
		this.computationRequest = computationRequest;
		this.storageManager = storageManager;
		this.computationRequestQueue = computationRequestQueue;
	}

	public void run() {
		String type = (String)computationRequest.request.get("type");
		if (type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION)){
			getPredictionData(); 
		} else if (type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION)){
			getRegressionData();
		} else {
			System.out.println("Unknown service type. @Computation worker.");            
		}
	}

	private void getPredictionData() {
		RegionalRequest regionalRequest = new RegionalRequest(0, System.currentTimeMillis(), this);
		storageManager.regionalRequestQueue.add(regionalRequest);
		long timeoutTime = System.currentTimeMillis() + TIME_OUT_DATA_REQUEST;
		while(!regionalRequest.ready){
			if(timeoutTime < System.currentTimeMillis()){
				timeoutGettingData();
				return;
			} else {
				try {
					Thread.sleep(SLEEP_TIME_DATA_REQUEST);
				} catch (InterruptedException e) {
					System.out.println("Couldn't sleep. @Computation worker.");
					e.printStackTrace();
				}
			}
		}
		performPrediction(regionalRequest.response);
	}
	
	private void timeoutGettingData() {
		computationRequestQueue.add(computationRequest);
		System.out.println("Timed out getting data. @Computation worker.");
	}

	private void getRegressionData() {
		RegionalRequest regionalRequest = new RegionalRequest((long)computationRequest.request.get("startdate"), System.currentTimeMillis(), storageManager);
		storageManager.regionalRequestQueue.add(regionalRequest);
		long timeoutTime = System.currentTimeMillis() + TIME_OUT_DATA_REQUEST;
		while(!regionalRequest.ready){
			if(timeoutTime < System.currentTimeMillis()){
				timeoutGettingData();
				return;
			} else {
				try {
					Thread.sleep(SLEEP_TIME_DATA_REQUEST);
				} catch (InterruptedException e) {
					System.out.println("Couldn't sleep. @Computation worker.");
					e.printStackTrace();
				}
			}
		}
		performRegression(regionalRequest.response);
	}

	private void performRegression(ArrayList<JSONObject> dataList) {
		try{
			String regressionVariable = (String)computationRequest.request.get("variable");
			long extrapolateDays = (long)computationRequest.request.get("extrapolatedays");
			double minMaxDomainValues[] = getMinMaxDomainValues(dataList); 
			double minMaxRangeValues[] = getMinMaxRangeValues(dataList, regressionVariable);
			if(minMaxDomainValues == null || minMaxRangeValues == null){
				throw new Exception("Not enough values in data set. @Computation worker.");
			} else {
				XYSeriesCollection regressionData = new XYSeriesCollection();
				XYSeries seriesData = createSeriesDataRegression(dataList, regressionVariable);
				regressionData.addSeries(seriesData);
				String title = "Regression resutls - " + LocalDateTime.now().toString(); 
				JFreeChart chart = ChartFactory.createScatterPlot(title, "x", "y", regressionData);
				XYPlot plot = chart.getXYPlot();
				if(extrapolateDays > 0){
					//add ms to domain axis
					minMaxDomainValues[1] += (86400000d * extrapolateDays); 
				}
				plot.getDomainAxis().setRange(minMaxDomainValues[0], minMaxDomainValues[1]);
				plot.getRangeAxis().setRange(minMaxRangeValues[0], minMaxRangeValues[1]);
				double regressionParameters[] = Regression.getOLSRegression(plot.getDataset(), 0);
				LineFunction2D lineFunction = new LineFunction2D(regressionParameters[0], regressionParameters[1]);
				XYDataset lineDataset = DatasetUtilities.sampleFunction2D(lineFunction, minMaxDomainValues[0], minMaxDomainValues[1], 10000, "Fitted regression line");
				plot.setDataset(1, lineDataset);
				XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
				lineRenderer.setSeriesPaint(0, Color.YELLOW);
				plot.setRenderer(1, lineRenderer);
				storeRegressionResults(chart);
				/*
				 * this nethod call can be used to visualize charts for testing
				 *
					chartToScreen(chart);
				*/
			}
			
		} catch (Exception e) {
			System.out.println("Error with regression computation request. @Computation worker.");
			e.printStackTrace();
			storeRegressionResults(null);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void performPrediction(ArrayList<JSONObject> dataList) {
		try {
			String regressionVariable = (String)computationRequest.request.get("variable");
			XYSeriesCollection regressionData = new XYSeriesCollection();
			XYSeries seriesData = createSeriesDataRegression(dataList, regressionVariable);
			regressionData.addSeries(seriesData);
			JFreeChart chart = ChartFactory.createScatterPlot("", "", "", regressionData);
			XYPlot plot = chart.getXYPlot();
			double regressionParameters[] = Regression.getOLSRegression(plot.getDataset(), 0);
			LineFunction2D lineFunction = new LineFunction2D(regressionParameters[0], regressionParameters[1]);
			JSONObject results = new JSONObject();
			double addDays = 0d;
			for(Integer i = 0; i < NUMBER_OF_DAYS_PREDICTION; i++){
				results.put(i.toString(), lineFunction.getValue(System.currentTimeMillis() + addDays));
				//add a day
				addDays += 86400000d;
			}
			storePredictionResults(results);
		} catch (Exception e){
			System.out.println("Error with prediction computation request. @Computation worker.");
			e.printStackTrace();
			storePredictionResults(null);
		}
	}

	@SuppressWarnings("unchecked")
	private void storeRegressionResults(JFreeChart chart) {
		if(chart == null){
			JSONObject results = new JSONObject();
			results.put("response", "failed");
			results.put("ticket", computationRequest.ticket);
			results.put("graphimage", "");
			results.put("createtime", System.currentTimeMillis());
			results.put("type", Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION);
			storageManager.computationResultStorageQueue.add(results);
			System.out.println("Failed computing ticket: " + computationRequest.ticket + " Sending to storage. @Computation worker.");
		} else {
			byte[] imageByteArray = null;
			try {
				imageByteArray = ChartUtilities.encodeAsPNG(chart.createBufferedImage(REGRESSION_IMAGE_WIDTH, REGRESSION_IMAGE_HEIGHT));
			} catch (IOException e) {
				System.out.println("Error encoding png. @Computation worker.");
				e.printStackTrace();
				return;
			}
			JSONObject results = new JSONObject();
			results.put("response", "success");
			results.put("ticket", computationRequest.ticket);
			results.put("graphimage", Base64.encodeBase64String(imageByteArray));
			results.put("createtime", System.currentTimeMillis());
			results.put("type", Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION);
			storageManager.computationResultStorageQueue.add(results);
			System.out.println("Successfully computed ticket: " + computationRequest.ticket + " Sending to storage. @Computation worker.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void storePredictionResults(JSONObject resultObj) {
		if(resultObj == null){
			JSONObject results = new JSONObject();
			results.put("response", "failed");
			results.put("ticket", computationRequest.ticket);
			results.put("predictions", "");
			results.put("createtime", System.currentTimeMillis());
			results.put("type", Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION);
			storageManager.computationResultStorageQueue.add(results);
			System.out.println("Failed computing ticket: " + computationRequest.ticket + " Sending to storage. @Computation worker.");
		} else {
			JSONObject results = new JSONObject();
			results.put("response", "success");
			results.put("ticket", computationRequest.ticket);
			results.put("predictions", resultObj);
			results.put("createtime", System.currentTimeMillis());
			results.put("type", Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION);
			storageManager.computationResultStorageQueue.add(results);
			System.out.println("Successfully computed ticket: " + computationRequest.ticket + " Sending to storage. @Computation worker.");
		}
	}

	@SuppressWarnings("unused")
	private void chartToScreen(JFreeChart chart){
		ChartPanel chartPanel = new ChartPanel(chart);
		JFrame plotFrame = new JFrame();
		plotFrame.setContentPane(chartPanel);
		plotFrame.setSize(640,430);
		plotFrame.setVisible(true);
		plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private double[] getMinMaxRangeValues(ArrayList<JSONObject> dataList, String regressionVariable) throws Exception {
		double[] output = new double[2];
		Double minValue = Double.MAX_VALUE;
		Double maxValue = Double.MIN_VALUE;
		for(JSONObject JSONObj : dataList){
			Object obj = JSONObj.get(regressionVariable);
			if(obj != null){
				if(obj instanceof Double){
					Double compare = (Double) obj;
					if(compare < minValue){
						minValue = compare;
					}
					if(compare > maxValue){
						maxValue = compare;
					}
				}
			}
		}
		if(minValue == Double.MAX_VALUE || maxValue == Double.MIN_VALUE || minValue == maxValue){
			return null;
		} else {
			output[0] = minValue.doubleValue();
			output[1] = maxValue.doubleValue();
		}
		return output;
	}

	private double[] getMinMaxDomainValues(ArrayList<JSONObject> dataList) throws Exception {
		double[] output = new double[2];
		Long minValue = Long.MAX_VALUE;
		Long maxValue = Long.MIN_VALUE;
		for(JSONObject JSONObj : dataList){
			Object obj = JSONObj.get("time");
			if(obj != null){
				if(obj instanceof Long){
					long lon = (Long)obj;
					if(lon < minValue){
						minValue = lon;
					}
					if(lon > maxValue){
						maxValue = lon;
					}
				}
			}
		}
		if(minValue == Long.MAX_VALUE || maxValue == Long.MIN_VALUE || minValue == maxValue){
			return null;
		} else {
			output[0] = minValue.doubleValue();
			output[1] = maxValue.doubleValue();
		}
		return output;
	}

	private XYSeries createSeriesDataRegression(ArrayList<JSONObject> dataList, String regressionVariable) {
		XYSeries output = new XYSeries("input data");
		Long lon;
		Double dub = null;
		for(JSONObject JSONObj : dataList){
			Object obj = JSONObj.get(regressionVariable);
			if(obj != null){
				if(obj instanceof Double){
					dub = (double)obj;
				}
				else if(obj instanceof Long){
					lon = (Long)obj;
					dub = lon.doubleValue();
				}
				lon = (Long)JSONObj.get("time");
				//TODO: at this moment, only 1 piece of data per date is used, should be for example the mean
				output.addOrUpdate(lon.doubleValue(), dub.doubleValue());
			}
		}
		return output;
	}
}
