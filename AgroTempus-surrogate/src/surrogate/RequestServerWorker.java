package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RequestServerWorker implements Runnable {
	private static final int SOCKET_TIMEOUT = 5000; //ms
	private static final long SLEEP_TIME_REGIONAL_REQUEST = 1000; //ms
	private static final long SLEEP_TIME_FORECAST_REQUEST = 1000; //ms
	private static final long SLEEP_TIME_COMPUTATION_RESULTS_REQUEST = 1000; //ms
	private static final long TIMEOUT_FORECAST_REQUEST = 20000; //ms
	private static final long TIMEOUT_REGIONAL_REQUEST = 30000; //ms
	private static final long TIMEOUT_COMPUTATION_RESULTS_REQUEST = 20000; //ms
	protected Socket clientSocket;
	BufferedWriter out;
	public volatile StorageManager storageManager;
	private boolean running;

	public RequestServerWorker(Socket clientSocket, StorageManager storageManager) {
		running = true;
		try {
			clientSocket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (SocketException e) {			
			try {
				clientSocket.close();
			} catch (IOException e1) {
				System.out.println("Error closing socket during initialisation. @Request worker.");
				e1.printStackTrace();
			}
			running = false;
			System.out.println("Error setting socket timeout. @Request worker.");
			e.printStackTrace();
			
		}
		this.clientSocket = clientSocket;
		this.storageManager = storageManager;
		out = null;
	}
	
	public void run() {
		if(running){
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
				} catch (SocketTimeoutException se) {
					System.out.println("Connection with mobile app timed out. @Offload worker.");
				} catch (Exception e) {
					System.out.println("Error handling request. @Request worker.");
					e.printStackTrace();
				}
		        if(success){
		        	System.out.println("Successfully handled request. @Request worker.");
		        } else {
		        	System.out.println("Failed service request from mobile. Closing thread. @Request worker.");
		        }
			}
			try {
				clientSocket.close();
				System.out.println("Closing connection to mobile device. @Request worker.");
			} catch (IOException e) {
				System.out.println("Error closing client socket. @Request worker.");
				e.printStackTrace();
			}
		}
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
		System.out.println("Unknown message from mobile. Closing thread. @Request worker.");
	}
	
	@SuppressWarnings("unchecked")
	private void sendFailureMessage() {
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
    		JSONObject response = new JSONObject();
			response.put("response", "failed");
			out.write(response.toJSONString() + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. @Request worker.");
			e.printStackTrace();
		}
		System.out.println("Sent failure message to mobile device. Closing thread. @Request worker.");
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
				success = sendComputationResults(in, out);
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
		} catch (SocketTimeoutException se) {
			System.out.println("Connection with mobile app timed out. @Offload worker.");
			return false;
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
			long timeoutTime = System.currentTimeMillis() + TIMEOUT_FORECAST_REQUEST;
			while(!request.ready){
				if(timeoutTime < System.currentTimeMillis()){
					sendFailureMessage();
					return false;
				} else {
					Thread.sleep(SLEEP_TIME_FORECAST_REQUEST);
				}
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
			long timeoutTime = System.currentTimeMillis() + TIMEOUT_REGIONAL_REQUEST;
			while(!request.ready){
				if(timeoutTime < System.currentTimeMillis()){
					sendFailureMessage();
					return false;
				} else {
					Thread.sleep(SLEEP_TIME_REGIONAL_REQUEST);
				}
			}
			list = request.response;
		} catch (Exception e) {
			System.out.println("Error getting weather data from Storage manager. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return sendRequestData(list, in, out);
	}

	private boolean sendComputationResults(BufferedReader in, BufferedWriter out) {
		String inputLine;
		JSONObject request;
    	JSONParser parser = new JSONParser();
		try {
			if ((inputLine = in.readLine()) != null){
				request = (JSONObject)parser.parse(inputLine);
				if(request.containsKey("ticket")){
					String ticket = (String)request.get("ticket");
					ComputationResultRequest compRequest = new ComputationResultRequest(this, ticket);
					storageManager.computationResultRequestQueue.add(compRequest);
					long timeoutTime = System.currentTimeMillis() + TIMEOUT_COMPUTATION_RESULTS_REQUEST;
					while(!compRequest.ready){
						if(timeoutTime < System.currentTimeMillis()){
							sendFailureMessage();
							return false;
						} else {
							Thread.sleep(SLEEP_TIME_COMPUTATION_RESULTS_REQUEST );
						}
					}					
					String sendStr = compRequest.response.toJSONString() + "\n";
					//System.out.println(sendStr);
					out.write(sendStr);
					out.flush();
					System.out.println("Sending response to requestor for ticket: " + ticket + ". @Request worker.");
					return receiveMobileConfirmation(in, out, ticket);
			    }
			}
		} catch (SocketTimeoutException se) {
			System.out.println("Connection with mobile app timed out. @Offload worker.");
		} catch (Exception e) {
			System.out.println("Error handling computation result request. @Request worker.");
			e.printStackTrace();
		}
		return false;
	}

	private boolean receiveMobileConfirmation(BufferedReader in, BufferedWriter out, String ticket) {
		String inputLine;
		JSONObject confirmation;
    	JSONParser parser = new JSONParser();
		try {
			if ((inputLine = in.readLine()) != null){
				confirmation = (JSONObject)parser.parse(inputLine);
				if(confirmation.containsKey("response")){
					String status = (String)confirmation.get("response");
					if(status.equals("ok")){
						storageManager.successfullyReceivedTickets.add(ticket);
						String continueGettingTickets = (String)confirmation.get("moretickets");
						if(continueGettingTickets.equals("yes")){
							return sendComputationResults(in, out);
						}
						return true;
					}
			    } 
			}
		} catch (SocketTimeoutException se) {
			System.out.println("Connection with mobile app timed out. @Offload worker.");
		} catch (Exception e) {
			System.out.println("Error parsing regional request. @Request worker.");
			e.printStackTrace();
		}
		return false;
	}

	//TODO: update to be able to handle very long strings
	private boolean sendRequestData(ArrayList<JSONObject> list, BufferedReader in, BufferedWriter out) {
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
		JSONObject mobileResponse;
    	JSONParser parser = new JSONParser();
		try {
			if((inputLine = in.readLine()) != null){
				mobileResponse = (JSONObject)parser.parse(inputLine);
				if(mobileResponse.containsKey("response") && mobileResponse.get("response").equals("ok")){
					System.out.println("Succesfully sent data to requestor. @Request worker.");
					return true;
			    }
				else {
					handleUnknownMessage();
				}
			}
		} catch (SocketTimeoutException se) {
			System.out.println("Connection with mobile app timed out. @Offload worker.");
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
