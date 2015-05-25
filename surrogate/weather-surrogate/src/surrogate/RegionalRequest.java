package surrogate;

import java.util.ArrayList;
import org.json.simple.JSONObject;

public class RegionalRequest {
	long start;
	long end;
	Runnable requestor;
	boolean ready;
	ArrayList<JSONObject> response;
	
	public RegionalRequest(long start, long end, Runnable requestor){
		this.start = start;
		this.end = end;
		this.requestor = requestor;
		ready = false;
	}
}