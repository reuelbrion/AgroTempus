package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class RequestServerWorker implements Runnable {
	protected Socket clientSocket;

	public RequestServerWorker(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error opening BufferedReader. Request worker.");
			e.printStackTrace();
		}
		
		if(in != null){
			String inputLine;
			try {
				if ((inputLine = in.readLine()) != null) {
					if(inputLine.equals("request-service")){
						handleServiceRequest(in);
				    }
					else {
						handleUnknownMessage();
					}
				}
			} catch (IOException e) {
				System.out.println("Error reading buffer. Request worker.");
				e.printStackTrace();
			}
		}
		

	}

	private void handleUnknownMessage() {
		BufferedWriter out = null;
    	try {
			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.write("unknown\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("Error opening BufferedWriter. Request worker.");
			e.printStackTrace();
		}
    	
    	try {
			out.close();
		} catch (IOException e) {
			System.out.println("Error closing BufferedWriter. Request worker.");
			e.printStackTrace();
		}
		System.out.println("Unknow message from mobile. Closing thread. Request worker.");
	}

	private void handleServiceRequest(BufferedReader in) {
    	System.out.println("Reading service request. Request worker.");
    	String inputLine;
    	BufferedWriter out = null;
    	try {
			if ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("retrieve_regional_data")){
					getRegionalData();
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
					out.write("ok\n");
					out.flush();
			    }
				else if(inputLine.equals("retrieve_computation_results")){
					getComputationResults();
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
					out.write("ok\n");
					out.flush();
			    }
				else{	
					 handleUnknownMessage();
				}
			}
		} catch (IOException e) {
			System.out.println("Error with BufferedWriter. Request worker.");
			e.printStackTrace();
		}

		try {
			out.close();
		} catch (IOException e) {
			System.out.println("Error closing BufferedWriter. Request worker.");
			e.printStackTrace();
		}
	}

}
