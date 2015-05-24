package surrogate;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;;

public class StorageServerWorker implements Runnable {
	
	protected Socket clientSocket;
	BufferedWriter out;
	public volatile StorageManager storageManager;

	public StorageServerWorker(Socket clientSocket, StorageManager storageManager) {
		this.clientSocket = clientSocket;
		this.storageManager = storageManager;
		out = null;
	}
	
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error opening BufferedReader. Storage worker.");
			e.printStackTrace();
		}
		
		if(in != null){
			boolean success = false;
			String inputLine;
			try {
				if ((inputLine = in.readLine()) != null) {
					if(inputLine.equals("request-service")){
						success = handleServiceRequest(in);
				    }
					else {
						handleUnknownMessage();
					}
				}
			} catch (IOException e) {
				System.out.println("Error reading buffer. Storage worker.");
				e.printStackTrace();
			}
			
	        if(success){
	        	handleSuccess();
	        }
	        else{
	        	handleFailed();
	        }
	        
	        try {
				clientSocket.close();
				System.out.println("Closing connection to mobile device. Storage worker.");
			} catch (IOException e) {
				System.out.println("Error closing client socket. Storage worker.");
				e.printStackTrace();
			}
		}
	}

	private void handleSuccess() {
		
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
			out.write("ok\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. Storage worker.");
			e.printStackTrace();
		}
	}

	private void handleUnknownMessage() {
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
			out.write("unknown\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. Storage worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. Storage worker.");
	}
	
	private void handleFailed() {
    	BufferedWriter out = null;
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
			out.write("failed\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. Storage worker.");
			e.printStackTrace();
		}
		System.out.println("Failed service request from mobile. Closing thread. Storage worker.");
	}

	private boolean handleServiceRequest(BufferedReader in) {
    	System.out.println("Reading service request. Storage worker.");
    	String inputLine;
    	boolean success = false;
    	int serviceType = -1;
    	try {
			if ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("store_weather_data")){
					serviceType = Surrogate.SERVICE_STORE_WEATHER_DATA;
					if(out == null){
		    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    		}
					out.write("ok\n");
					out.flush();
					success = storeWeatherData(in, serviceType);
			    }
				else{	
					 handleUnknownMessage();
					 return false;
				}
			}
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. Storage worker.");
			e.printStackTrace();
		}
		return success;
	}

	private boolean storeWeatherData(BufferedReader in, int serviceType) {
		System.out.println("Receiving weather data. Storage worker.");
		boolean receiving = true;
		boolean success = false;
		String inputLine;
		String receivedString = "";
		while(receiving){
			try {
				if ((inputLine = in.readLine()) != null) {
					if (inputLine.equals("END")){
						receiving = false;
					}
					else{
						receivedString += inputLine;
					}
				}
			} catch (IOException e) {
				System.out.println("Error with BufferedWriter. Storage worker.");
				e.printStackTrace();
				receiving = false;
			}
		}
		ArrayList<JSONObject> parsedJSON = parseJSON(receivedString);
		if(parsedJSON != null){
			success = sendToStorageManager(parsedJSON, serviceType);
		}
		return success;
	}

	private boolean sendToStorageManager(ArrayList<JSONObject> parsedJSON, int serviceType) {
		boolean success = true;
		if(serviceType == Surrogate.SERVICE_STORE_WEATHER_DATA){
			System.out.println("Sending data to Storage manager. Storage worker.");
			
			ArrayList<JSONObject> backupList = new ArrayList<JSONObject>();
			for(JSONObject obj : parsedJSON){
				try{
					storageManager.weatherStorageQueue.add(obj);
					backupList.add(obj);
				} catch(Exception e) {
					//undo saves on exception
					System.out.println("Error sending data to Storage manager, data was not stored. Storage worker.");
					e.printStackTrace();
					success = false;
					for(JSONObject obj2 : backupList){
						storageManager.weatherStorageQueue.remove(obj2);
					}
				}
			}	
		}
		else{
			System.out.println("Unknown service type. Nothing sent to Storage manager. Storage worker.");
			success = false;
		}
		return success;
	}

	private ArrayList<JSONObject> parseJSON(String receivedString) {
		JSONParser parser = new JSONParser();
		ArrayList<JSONObject> output = new ArrayList<JSONObject>();
		String[] JSONStrings = receivedString.split("(?<=})");
		int i = 0;	
		if(JSONStrings.length < 1){
			System.out.println("Error with JSON parsing, received empty string. Storage worker. String:\n" + receivedString);
			return null;
		}
		for(String JSONString : JSONStrings){
			try {
				if(!JSONString.isEmpty()){
					output.add((JSONObject)parser.parse(JSONString));
					i++;
				}
			} catch (ParseException e) {
				System.out.println("Error with JSON parsing, nothing was stored. Storage worker. String:\n" + receivedString);
				e.printStackTrace();
				return null;
			}
		}
		return output;
	}
}

