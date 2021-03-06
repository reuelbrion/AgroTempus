package surrogate;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StorageServerWorker implements Runnable {
	private static final int SOCKET_TIMEOUT = 7000; //ms
	
	Socket clientSocket;
	BufferedWriter out;
	public volatile StorageManager storageManager;
	private boolean running;

	public StorageServerWorker(Socket clientSocket, StorageManager storageManager) {
		running = true;
		try {
			clientSocket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (SocketException e) {			
			try {
				clientSocket.close();
			} catch (IOException e1) {
				System.out.println("Error closing socket during initialisation. @Storage worker.");
				e1.printStackTrace();
			}
			running = false;
			System.out.println("Error setting socket timeout. @Storage worker.");
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
				System.out.println("Error opening BufferedReader. @Storage worker.");
				e.printStackTrace();
			}
			if(in != null){
				boolean success = false;
				boolean timedOut = false;
				String inputLine;
		    	JSONObject request = null;
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
					System.out.println("Connection with mobile app timed out. @Storage worker worker.");
					timedOut = true;
				} catch (Exception e) {
					System.out.println("Error handling request. @Storage worker.");
					e.printStackTrace();
				}
		        if(success){
		        	handleSuccess();
		        }
		        else if (!timedOut){
		        	handleFailed();
		        }  
			}
			try {
				clientSocket.close();
				System.out.println("Closing connection to mobile device. @Storage worker.");
			} catch (IOException e) {
				System.out.println("Error closing client socket. @Storage worker.");
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
			System.out.println("Error opening BufferedWriter. @Storage worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. @Storage worker.");
	}

	@SuppressWarnings("unchecked")
	private void handleSuccess() {
		
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
    		JSONObject response = new JSONObject();
			response.put("response", "ok");
			out.write(response.toJSONString() + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. @Storage worker.");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void handleFailed() {
    	BufferedWriter out = null;
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
    		JSONObject response = new JSONObject();
			response.put("response", "failed");
			out.write(response.toJSONString() + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. @Storage worker.");
			e.printStackTrace();
		}
		System.out.println("Failed service request from mobile. Closing thread. @Storage worker.");
	}

	@SuppressWarnings("unchecked")
	private boolean handleServiceRequest(BufferedReader in, String serviceType) {
    	System.out.println("Reading service request. @Storage worker.");
    	boolean success = false;
    	try {
				if(serviceType.equals(Surrogate.SERVICE_TYPE_STORE_WEATHER_DATA)){
					if(out == null){
		    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    		}
					JSONObject response = new JSONObject();
					response.put("response", "ok");
					out.write(response.toJSONString() + "\n");
					out.flush();
					success = storeWeatherData(in, Surrogate.SERVICE_TYPE_STORE_WEATHER_DATA);
			    }
				else{	
					 handleUnknownMessage();
					 return false;
				}
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. @Storage worker.");
			e.printStackTrace();
		}
		return success;
	}

	private boolean storeWeatherData(BufferedReader in, String serviceType) {
		System.out.println("Receiving weather data. @Storage worker.");
		String inputLine;
		try {
			if ((inputLine = in.readLine()) != null) {
				ArrayList<JSONObject> parsedJSON = parseStorageJSON(inputLine);
				if(parsedJSON != null){
					return sendToStorageManager(parsedJSON, serviceType);
				}
			}
		} catch (SocketTimeoutException se) {
			System.out.println("Connection with mobile app timed out. @Storage worker.");
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. @Storage worker.");
			e.printStackTrace();
		}
		return false;
	}

	private boolean sendToStorageManager(ArrayList<JSONObject> parsedJSON, String serviceType) {
		boolean success = true;
		if(serviceType == Surrogate.SERVICE_TYPE_STORE_WEATHER_DATA){
			System.out.println("Sending data to Storage manager. @Storage worker.");
			
			ArrayList<JSONObject> restoreList = new ArrayList<JSONObject>();
			for(JSONObject obj : parsedJSON){
				try{
					storageManager.weatherStorageQueue.add(obj);
					restoreList.add(obj);
				} catch(Exception e) {
					//undo saves on exception
					System.out.println("Error sending data to Storage manager, data was not stored. @Storage worker.");
					e.printStackTrace();
					success = false;
					for(JSONObject obj2 : restoreList){
						storageManager.weatherStorageQueue.remove(obj2);
					}
				}
			}	
		}
		else{
			System.out.println("Unknown service type. Nothing sent to Storage manager. @Storage worker.");
			success = false;
		}
		return success;
	}

	private ArrayList<JSONObject> parseStorageJSON(String receivedString) {
		JSONParser parser = new JSONParser();
		ArrayList<JSONObject> output = new ArrayList<JSONObject>();
		if(receivedString.charAt(0) == '['){
			receivedString = receivedString.substring(1);
		}
		if(receivedString.charAt(receivedString.length() - 1) == ']'){
			receivedString = receivedString.substring(0, receivedString.length() - 1);
		}
		String[] JSONStrings = receivedString.replace("},{", "}{").split("(?<=})");
		if(JSONStrings.length < 1){
			System.out.println("Error with JSON parsing, received empty string. @Storage worker. String:\n" + receivedString);
			return null;
		}
		for(String JSONString : JSONStrings){
			try {
				output.add((JSONObject)parser.parse(JSONString));
			} catch (ParseException e) {
				System.out.println("Error with JSON parsing, nothing was stored. @Storage worker. String:\n" + receivedString);
				e.printStackTrace();
				return null;
			}
		}
		return output;
	}
}

