package surrogate;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

public class StorageManager implements Runnable {
	private static final long SLEEP_TIME_PER_CYCLE = 500;
	
	public volatile boolean running;
	public final ConcurrentLinkedQueue<JSONObject> weatherStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	public final ConcurrentLinkedQueue<RegionalRequest> regionalRequestQueue = new ConcurrentLinkedQueue<RegionalRequest>();
	public final ConcurrentLinkedQueue<ForecastRequest> forecastRequestQueue = new ConcurrentLinkedQueue<ForecastRequest>();
	public final ConcurrentLinkedQueue<JSONObject> forecastStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	private final ArrayList<JSONObject> storedWeatherObjects = new ArrayList<JSONObject>();	
	private final ArrayList<JSONObject> storedForecastObjects = new ArrayList<JSONObject>();	
	
	StorageManager(){
		loadDummyData();
	}
	
	@SuppressWarnings("unchecked")
	private void loadDummyData() {
		JSONObject obj = new JSONObject();
		obj.put("location", "Amsterdam - NL");
		obj.put("lat", "52.379");
		obj.put("long", "4.899");
		obj.put("temp", "5");
		obj.put("humidity", "60");
		obj.put("pressure", "550");
		obj.put("windspeed", "30");
		obj.put("winddegree", "135");
		obj.put("time", System.currentTimeMillis());
		obj.put("description", "Sunny day");
		storedForecastObjects.add(obj);
		
		obj = new JSONObject();
		obj.put("location", "Amsterdam - NL");
		obj.put("lat", "52.379");
		obj.put("long", "4.899");
		obj.put("temp", "6");
		obj.put("humidity", "66");
		obj.put("pressure", "510");
		obj.put("windspeed", "11");
		obj.put("winddegree", "222");
		obj.put("time", System.currentTimeMillis()-1230500l);
		obj.put("description", "Rain");
		storedForecastObjects.add(obj);
	}

	public void run() {
		running = true;
		while(running){
			if(!weatherStorageQueue.isEmpty()){
				weatherStorage();
			}
			if(!forecastStorageQueue.isEmpty()){
				forecastStorage();
			}
			if(!forecastRequestQueue.isEmpty()){
				ForecastRequest request = forecastRequestQueue.poll();
				if(request != null){
					handleForecastRequest(request);
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
	
	private void forecastStorage() {
		JSONObject obj = forecastStorageQueue.poll();
		if(obj != null){
			storedForecastObjects.add(obj);
			System.out.println("Saved JSON forecast object to storage. @Storage manager.\n" + obj.toString());
		}
	}

	private void weatherStorage() {
		JSONObject obj = weatherStorageQueue.poll();
		if(obj != null){
			storedWeatherObjects.add(obj);
			System.out.println("Saved JSON weather data object to storage. @Storage manager.\n" + obj.toString());
		}
	}
	
	private void handleForecastRequest(ForecastRequest request) {
		System.out.println("Creating response for forecast request. @Storage Manager");
		ArrayList<JSONObject> response = new ArrayList<JSONObject>();
		long currentTime = System.currentTimeMillis();
		for(JSONObject obj : storedForecastObjects){
			long time = (long)obj.get("time");
			if(currentTime >= time){
				response.add(obj);
			}
		}
		request.response = response;
		request.ready = true;
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
