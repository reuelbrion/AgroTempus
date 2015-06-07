package surrogate;

import org.json.simple.JSONObject;

public class ComputationRequest {
	boolean ready;
	Runnable requestor;
	JSONObject request;
	JSONObject response;
	
	public ComputationRequest(Runnable requestor, JSONObject request){
		this.requestor = requestor;
		this.request = request;
		ready = false;
	}
}
