package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RequestServerWorker implements Runnable {
	private static final long SLEEP_TIME_REGIONAL_REQUEST = 1000;
	private static final long SLEEP_TIME_FORECAST_REQUEST = 1000;
	protected Socket clientSocket;
	BufferedWriter out;
	public volatile StorageManager storageManager;

	public RequestServerWorker(Socket clientSocket, StorageManager storageManager) {
		this.clientSocket = clientSocket;
		this.storageManager = storageManager;
		out = null;
	}
	
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error opening BufferedReader. @Request worker.");
			e.printStackTrace();
		}
		
		if(in != null){
			boolean success = false;
			String inputLine;
			JSONObject request;
	    	JSONParser parser = new JSONParser();
			try {
				if ((inputLine = in.readLine()) != null) {
					request = (JSONObject)parser.parse(inputLine);
					if(request.containsKey("type")){
						success = handleServiceRequest(in, (String)request.get("type"));
				    }
					else {
						handleUnknownMessage();
					}
				}
			} catch (Exception e) {
				System.out.println("Error handling request. @Request worker.");
				e.printStackTrace();
			}
	        if(!success){
	        	handleFailed();
	        }
		}
		try {
			if(out != null){
				out.close();
			}
			clientSocket.close();
			System.out.println("Closing connection to mobile device. @Request worker.");
		} catch (IOException e) {
			System.out.println("Error closing client socket. @Request worker.");
			e.printStackTrace();
		}
	}
	
	private void handleFailed() {
    		System.out.println("Failed service request from mobile. Closing thread. @Request worker.");
	}
	
	@SuppressWarnings("unchecked")
	private void handleUnknownMessage() {
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
    		JSONObject response = new JSONObject();
			response.put("response", "unknown");
			out.write(response.toJSONString() + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. @Request worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. @Request worker.");
	}

	@SuppressWarnings("unchecked")
	private boolean handleServiceRequest(BufferedReader in, String serviceType) {
    	System.out.println("Reading service request. @Request worker.");
    	boolean success = false;
    	try {
			if(serviceType.equals(Surrogate.SERVICE_TYPE_RETRIEVE_REGIONAL_DATA)){
				if(out == null){
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			    }
				JSONObject response = new JSONObject();
				response.put("response", "ok");
				out.write(response.toJSONString());
				out.flush();
				success = getRegionalData(in, out);
			} else if(serviceType.equals(Surrogate.SERVICE_TYPE_RETRIEVE_FORECASTS)){
				if(out == null){
		    		out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    	}
				JSONObject response = new JSONObject();
				response.put("response", "ok");
				out.write(response.toJSONString());
				out.flush();
				success = getForecastsFromStorageManager(in, out);
			} else if(serviceType.equals(Surrogate.SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS)){
				if(out == null){
		    		out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    	}
				JSONObject response = new JSONObject();
				response.put("response", "ok");
				out.write(response.toJSONString());
				out.flush();
				success = getComputationResults(in, out);
			} else {	
					 handleUnknownMessage();
				}
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. @Request worker.");
			e.printStackTrace();
		}
    	return success;
	}

	private boolean getRegionalData(BufferedReader in, BufferedWriter out) {
		System.out.println("Receiving regional request. @Request worker.");
		String inputLine;
		long start = 0l, end = 0l;
		JSONObject request;
    	JSONParser parser = new JSONParser();
		try {
			if ((inputLine = in.readLine()) != null){
				request = (JSONObject)parser.parse(inputLine);
				if(request.containsKey("start_time")){
					start = (long)request.get("start_time");
			    } else {
			    	System.out.println("Error parsing regional request (no start time). @Request worker.");
					handleUnknownMessage();
			    	return false;
			    }
				if(request.containsKey("end_time")){
					end = (long)request.get("end_time");
			    } else {
			    	System.out.println("Error parsing regional request (no end time). @Request worker.");
			    	handleUnknownMessage();
			    	return false;
			    }
			}
		} catch (Exception e) {
			System.out.println("Error parsing regional request. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return getRequestDataFromStorageManager(start, end, in, out);
	}

	private boolean getForecastsFromStorageManager(BufferedReader in, BufferedWriter out) {
		ArrayList<JSONObject> list = null;
		try{
			ForecastRequest request = new ForecastRequest(this);
			storageManager.forecastRequestQueue.add(request);
			//TODO: time out!
			while(!request.ready){
				Thread.sleep(SLEEP_TIME_FORECAST_REQUEST);
			}
			list = request.response;
		} catch (Exception e) {
			System.out.println("Error getting forecast data from Storage manager. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return sendRequestData(list, in, out);
	}

	private boolean getRequestDataFromStorageManager(long start, long end,
			BufferedReader in, BufferedWriter out) {
		ArrayList<JSONObject> list = null;
		try{
			RegionalRequest request = new RegionalRequest(start, end, this);
			storageManager.regionalRequestQueue.add(request);
			//TODO: timeout!
			while(!request.ready){
				Thread.sleep(SLEEP_TIME_REGIONAL_REQUEST);
			}
			list = request.response;
		} catch (Exception e) {
			System.out.println("Error getting weather data from Storage manager. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return sendRequestData(list, in, out);
	}

	private boolean getComputationResults(BufferedReader in, BufferedWriter out) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean sendRequestData(ArrayList<JSONObject> list, BufferedReader in,
			BufferedWriter out) {
		//TODO: handle timeout in response
			String sendStr = createSendString(list);
		try {
			out.write(sendStr);
			out.flush();
			System.out.println("Sending data to requestor. @Request worker.");
		} catch (IOException e) {
			System.out.println("Error writing to BufferedWriter. @Request worker.");
			e.printStackTrace();
			return false;
		}
		String inputLine;
		JSONObject mobResponse;
    	JSONParser parser = new JSONParser();
		try {
			if((inputLine = in.readLine()) != null){
				mobResponse = (JSONObject)parser.parse(inputLine);
				if(mobResponse.containsKey("response") && mobResponse.get("response").equals("ok")){
					System.out.println("Succesfully sent data to requestor. @Request worker. Data sent:");
					System.out.println(sendStr);
					return true;
			    }
				else {
					handleUnknownMessage();
				}
			}
		} catch (Exception e) {
			System.out.println("Error reading confirmation from mobile. @Request worker.");
			e.printStackTrace();
		}
		return false;
	}

	private String createSendString(ArrayList<JSONObject> list) {
		String output = "[";
		int i = 0;
		for(JSONObject obj : list){
			if(i > 0){
				output += ",";
			}
			output += obj.toJSONString();
			i++;
		}
		output += "]";
		return output;
	}

}
