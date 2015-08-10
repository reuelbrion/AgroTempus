package surrogate;

import org.json.simple.JSONObject;

public class ComputationRequest {
	boolean ready;
	JSONObject request;
	String ticket;
	
	public ComputationRequest(JSONObject request, String ticket){
		this.request = request;
		this.ticket = ticket;
		ready = false;
	}
}
