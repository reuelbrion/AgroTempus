package surrogate;

import org.json.simple.JSONObject;

public class ComputationResultRequest {
	Runnable requestor;
	boolean ready;
	String ticket;
	JSONObject response;
	
	public ComputationResultRequest(Runnable requestor, String ticket){
		this.requestor = requestor;
		this.ticket = ticket;
		ready = false;
	}
}