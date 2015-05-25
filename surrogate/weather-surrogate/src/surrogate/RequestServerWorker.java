package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.simple.JSONObject;

public class RequestServerWorker implements Runnable {
	private static final long SLEEP_TIME_REGIONAL_REQUEST = 3000;
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
				System.out.println("Error reading buffer. @Request worker.");
				e.printStackTrace();
			}
	        if(!success){
	        	handleFailed();
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
	
	private void handleFailed() {
    		System.out.println("Failed service request from mobile. Closing thread. @Request worker.");
	}
	
	private void handleUnknownMessage() {
    	try {
    		if(out == null){
    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    		}
			out.write("unknown\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. @Request worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. @Request worker.");
	}

	private boolean handleServiceRequest(BufferedReader in) {
    	System.out.println("Reading service request. Request worker.");
    	String inputLine;
    	boolean success = false;
    	try {
			if ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("retrieve_regional_data")){
					if(out == null){
		    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    		}
					out.write("ok\n");
					out.flush();
					success = getRegionalData(in, out);
			    }
				else if(inputLine.equals("retrieve_computation_results")){
					if(out == null){
		    			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		    		}
					out.write("ok\n");
					out.flush();
					success = getComputationResults(in, out);
			    }
				else{	
					 handleUnknownMessage();
				}
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
		long start, end = 0;
		try {
			if ((inputLine = in.readLine()) != null){
				start = Long.parseLong(inputLine);
			} else {
				System.out.println("Error parsing regional request. @Request worker.");
				return false;
			}
			if ((inputLine = in.readLine()) != null){
				end = Long.parseLong(inputLine);
			} else {
				System.out.println("Error parsing regional request. @Request worker.");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error parsing regional request. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return getRequestDataFromStorageManager(start, end, in, out);
	}

	private boolean getRequestDataFromStorageManager(long start, long end,
			BufferedReader in, BufferedWriter out) {
		String storedData = "";
		try{
			RegionalRequest request = new RegionalRequest(start, end, this);
			storageManager.regionalRequestQueue.add(request);
			while(!request.ready){
				Thread.sleep(SLEEP_TIME_REGIONAL_REQUEST);
			}
			for(JSONObject obj : request.response){
				storedData += obj.toJSONString();
			}
			storedData += "\n";
		} catch (Exception e) {
			System.out.println("Error getting data from Storage manager. @Request worker.");
			e.printStackTrace();
			return false;
		}
		return sendRequestData(storedData, in, out);
	}

	private boolean getComputationResults(BufferedReader in, BufferedWriter out) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean sendRequestData(String sendStr, BufferedReader in,
			BufferedWriter out) {
		//TODO: handle timeout in response
		try {
			out.write(sendStr);
			out.flush();
			System.out.println("Sent data to requestor. @Request worker.");
		} catch (IOException e) {
			System.out.println("Error writing to BufferedWriter. @Request worker.");
			e.printStackTrace();
			return false;
		}
		String inputLine;
		try {
			if((inputLine = in.readLine()) != null){
				if(inputLine.equals("ok")){
					System.out.println("Succesfully sent data to requestor. @Request worker.");
					return true;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading from BufferedWriter. @Request worker.");
			e.printStackTrace();
		}
		return false;
	}

}
