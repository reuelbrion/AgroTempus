package surrogate;

import java.util.ArrayList;

import org.json.simple.JSONObject;

public class ForecastRequest {
	Runnable requestor;
	boolean ready;
	ArrayList<JSONObject> response;
	
	public ForecastRequest(Runnable requestor){
		this.requestor = requestor;
		ready = false;
	}
}
