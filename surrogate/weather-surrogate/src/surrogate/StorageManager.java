package surrogate;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

public class StorageManager implements Runnable {
	private static final long SLEEP_TIME_PER_CYCLE = 3000;
	
	public volatile boolean running;
	public final ConcurrentLinkedQueue<JSONObject> weatherStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	public final ConcurrentLinkedQueue<RegionalRequest> regionalRequestQueue = new ConcurrentLinkedQueue<RegionalRequest>();
	private final ArrayList<JSONObject> storedWeatherObjects = new ArrayList<JSONObject>();	
	
	public void run() {
		running = true;
		while(running){
			if(!weatherStorageQueue.isEmpty()){
				JSONObject obj = weatherStorageQueue.poll();
				if(obj != null){
					storedWeatherObjects.add(obj);
					System.out.println("Saved JSON object to storage. @Storage manager.\n" + obj.toString());
				}
			}
			if(!regionalRequestQueue.isEmpty()){
				RegionalRequest request = regionalRequestQueue.poll();
				if(request != null){
					handleRegionalRequest(request);
				}
			}
			try {
				Thread.sleep(SLEEP_TIME_PER_CYCLE);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep. @Storage Manager.");
				e.printStackTrace();
			}
		}
	}

	private void handleRegionalRequest(RegionalRequest request) {
		System.out.println("Creating response for regional data request. @Storage Manager");
		ArrayList<JSONObject> response = new ArrayList<JSONObject>();
		for(JSONObject obj : storedWeatherObjects){
			long time = (long)obj.get("time");
			if(time >= request.start && time < request.end){
				response.add(obj);
			}
		}
		request.response = response;
		request.ready = true;
	}
}
