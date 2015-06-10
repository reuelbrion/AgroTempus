package surrogate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StorageManager implements Runnable {
	private static final long SLEEP_TIME_PER_CYCLE = 500;
	
	public volatile boolean running;
	public final ConcurrentLinkedQueue<JSONObject> weatherStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	public final ConcurrentLinkedQueue<JSONObject> forecastStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	public final ConcurrentLinkedQueue<JSONObject> computationResultStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	public final ConcurrentLinkedQueue<RegionalRequest> regionalRequestQueue = new ConcurrentLinkedQueue<RegionalRequest>();
	public final ConcurrentLinkedQueue<ForecastRequest> forecastRequestQueue = new ConcurrentLinkedQueue<ForecastRequest>();
	private final ArrayList<JSONObject> storedWeatherObjects = new ArrayList<JSONObject>();	
	private final ArrayList<JSONObject> storedForecastObjects = new ArrayList<JSONObject>();
	private final ArrayList<JSONObject> storedComputationResultObjects = new ArrayList<JSONObject>();	
	
	StorageManager(){
		running = true;
		loadDummyData();
		System.out.println("Storage Manager successfully started. @Storage manager.");
	}
	
	@SuppressWarnings("unchecked")
	private void loadDummyData() {
		//load dummy regional weather data
    	JSONParser parser = new JSONParser();
    	try {
    	    InputStream fis = new FileInputStream("data/DUMMY_REGIONAL_DATA.json");
    	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
    	    BufferedReader br = new BufferedReader(isr);
    	    try {
				JSONArray array = (JSONArray) parser.parse(br);
				for(Object json : array){
					storedWeatherObjects.add((JSONObject) json);
				}
			} catch (ParseException e) {
				System.out.println("Error loading dummy data. @Storage Manager.");
				e.printStackTrace();
			}
    	} catch (FileNotFoundException e) {
    		System.out.println("Error loading dummy data. @Storage Manager.");
			e.printStackTrace();  
		} catch (IOException e) {
			System.out.println("Error loading dummy data. @Storage Manager.");
			e.printStackTrace();
		}
    	//end load dummy regional weather data
    	
    	//load foracasts
    	try {
    	    InputStream fis = new FileInputStream("data/DUMMY_FORECASTS.json");
    	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
    	    BufferedReader br = new BufferedReader(isr);
    	    try {
    	    	JSONArray array = (JSONArray) parser.parse(br);
    	    	for(Object json : array){
    	    		storedForecastObjects.add((JSONObject) json);
    	    	}
    		} catch (ParseException e) {
    			System.out.println("Error loading dummy data. @Storage Manager.");
    			e.printStackTrace();    					
    		}
    	    	} catch (FileNotFoundException e) {
    	    		System.out.println("Error loading dummy data. @Storage Manager.");
        			e.printStackTrace();  
    			} catch (IOException e) {
    				System.out.println("Error loading dummy data. @Storage Manager.");
        			e.printStackTrace();  
    			}
    	//end load foracasts
    	
    	System.out.println("Dummy data successfully loaded. @Storage Manager.");
	}

	public void run() {
		while(running){
			if(!weatherStorageQueue.isEmpty()){
				weatherStorage();
			}
			if(!forecastStorageQueue.isEmpty()){
				forecastStorage();
			}
			if(!computationResultStorageQueue.isEmpty()){
				computationStorage();
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
	
	private void computationStorage() {
		JSONObject obj = computationResultStorageQueue.poll();
		if(obj != null){
			storedComputationResultObjects.add(obj);
			System.out.println("Saved JSON computation result object to storage. @Storage manager.\n" + obj.toString());
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
