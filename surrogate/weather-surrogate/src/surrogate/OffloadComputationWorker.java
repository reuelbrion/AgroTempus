package surrogate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OffloadComputationWorker implements Runnable {
	private ComputationRequest computationRequest;
	
	public OffloadComputationWorker(ComputationRequest computationRequest) {
		this.computationRequest = computationRequest;
	}

	public void run() {
		String type = (String)computationRequest.request.get("type");
		if(type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_REGRESSION)){
			performRegression();
		} else if (type.equals(Surrogate.SERVICE_TYPE_OFFLOAD_PREDICTION)){
			//TODO
		} else {
			System.out.println("Unknown service type. @Computation worker.");            
		}
	}

	private void performRegression() {
		//TODO
		System.out.println("regressioning!");
	}
}
