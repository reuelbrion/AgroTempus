package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OffloadServerWorker implements Runnable {
	
	Socket clientSocket;
	BufferedWriter out;
	public volatile StorageManager storageManager;
	public volatile OffloadComputationManager offloadComputationManager;

	public OffloadServerWorker(Socket clientSocket, StorageManager storageManager, OffloadComputationManager offloadComputationManager) {
		this.clientSocket = clientSocket;
		this.storageManager = storageManager;
		this.offloadComputationManager = offloadComputationManager;
		out = null;
	}
	
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error opening BufferedReader. @Offload worker.");
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
				System.out.println("Error with mobile request. @Offload worker.");
				e.printStackTrace();
			}
			
	        if(success){
	        	//?
	        }
	        else{
	        	//?
	        }  
		}
		try {
			clientSocket.close();
			System.out.println("Closing connection to mobile device. @Offload worker.");
		} catch (IOException e) {
			System.out.println("Error closing client socket. @Offload worker.");
			e.printStackTrace();
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
			System.out.println("Error opening BufferedWriter. @Offload worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. @Offload worker.");
	}

	@SuppressWarnings("unchecked")
	private boolean handleServiceRequest(BufferedReader in, String serviceType) {
		System.out.println("Reading service request. @Offload worker.");
    	boolean success = false;
    	try {
			if(serviceType.equals(Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION)){
				if(out == null){
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    	}
				JSONObject response = new JSONObject();
				response.put("response", "ok");
				out.write(response.toJSONString() + "\n");
				out.flush();
				success = submitRegression(in, out);
			} else if(serviceType.equals(Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION)){
				if(out == null){
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    	}
				JSONObject response = new JSONObject();
				response.put("response", "ok");
				out.write(response.toJSONString() + "\n");
				out.flush();
				success = submitPrediction(in, out);
			} else {	
				 handleUnknownMessage();
			}
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. @Offload worker.");
			e.printStackTrace();
		}
    	return success;
	}

	private boolean submitPrediction(BufferedReader in, BufferedWriter out2) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean submitRegression(BufferedReader in, BufferedWriter out2) {
		JSONParser parser = new JSONParser();
		JSONObject request;
		String inputLine;
		System.out.println("Adding regression request. @Offload worker.");
		try {
			if ((inputLine = in.readLine()) != null) {
				request = (JSONObject)parser.parse(inputLine);
				System.out.println(inputLine);
				if(validateRegressionRequest(request)){
					
					//TODO:ticket number for storage.
					
					offloadComputationManager.computationRequestQueue.add(new ComputationRequest(this, request));
					return true;
				}
			} else {
				handleUnknownMessage();
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error in adding regression request. @Offload worker.");
			e.printStackTrace();
		}
		return false;
	}

	private boolean validateRegressionRequest(JSONObject JSONRequest) {
		if(JSONRequest.containsKey("type") && JSONRequest.containsKey("variable") && JSONRequest.containsKey("regressiontype") 
		&& JSONRequest.containsKey("startdate") && JSONRequest.containsKey("days")){
			if(JSONRequest.get("type") instanceof String && JSONRequest.get("variable") instanceof String && JSONRequest.get("type") instanceof String
			&& JSONRequest.get("startdate") instanceof Long && (JSONRequest.get("days") instanceof Long || JSONRequest.get("days") instanceof Integer)){
				return true;
			}
		}
		return false;
	}

}
