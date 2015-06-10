package surrogate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.simple.JSONObject;

public class OffloadComputationWorker implements Runnable {
	final static long SLEEP_TIME_DATA_REQUEST = 1000;
	
	private ComputationRequest computationRequest;
	public volatile StorageManager storageManager;
	
	public OffloadComputationWorker(ComputationRequest computationRequest, StorageManager storageManager) {
		this.computationRequest = computationRequest;
		this.storageManager = storageManager;
	}

	public void run() {
		String type = (String)computationRequest.request.get("type");
		if(type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION)){
			doRegression();
		} else if (type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION)){
			//TODO
		} else {
			System.out.println("Unknown service type. @Computation worker.");            
		}
	}

	private void doRegression() {
		RegionalRequest regionalRequest = new RegionalRequest((long)computationRequest.request.get("startdate"), System.currentTimeMillis(), storageManager);
		storageManager.regionalRequestQueue.add(regionalRequest);
		while(!regionalRequest.ready){
			//TODO: timeout
			try {
				Thread.sleep(SLEEP_TIME_DATA_REQUEST);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep. @Computation worker.");
				e.printStackTrace();
			}
		}
		performRegression(regionalRequest.response);
	}

	private void performRegression(ArrayList<JSONObject> dataList) {
		try{
			String regressionVariable = (String)computationRequest.request.get("variable");
			Long lon;
			Double dub = null;
			Date date;
			XYSeriesCollection regressionData = new XYSeriesCollection();
			XYSeries seriesData = new XYSeries("input data");
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
					seriesData.addOrUpdate(lon.doubleValue(), dub.doubleValue());
				}
			}
			regressionData.addSeries(seriesData);
			double[] functionItems = Regression.getOLSRegression(regressionData, 0);
			LineFunction2D lineFunction = new LineFunction2D(functionItems[0], functionItems[1]);
			lon = (long)computationRequest.request.get("startdate");
			Long now = System.currentTimeMillis();
			XYDataset regressionLineDataset = DatasetUtilities.sampleFunction2D(lineFunction, lon.doubleValue(), now.doubleValue(), dataList.size(),"Regression line");
			//regressionData.addSeries(regressionLineDataset);
			JFreeChart chart = ChartFactory.createScatterPlot("title", "x", "y", regressionData);
			
			/*XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yy")); */
			
			ChartPanel chartPanel = new ChartPanel(chart);
			JFrame plotFrame = new JFrame();
			plotFrame.setContentPane(chartPanel);
			plotFrame.setSize(640,430);
			plotFrame.setVisible(true);
			plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (Exception e) {
			System.out.println("Error with regression computation request. @Computation worker.");
			e.printStackTrace();
		}
	}
}
