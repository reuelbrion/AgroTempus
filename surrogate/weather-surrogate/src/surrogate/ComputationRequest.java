package surrogate;

import org.json.simple.JSONObject;

public class ComputationRequest {
	boolean ready;
	Runnable requestor;
	JSONObject request;
	JSONObject response;
	String ticket;
	
	public ComputationRequest(Runnable requestor, JSONObject request, String ticket){
		this.requestor = requestor;
		this.request = request;
		this.ticket = ticket;
		ready = false;
	}
}
