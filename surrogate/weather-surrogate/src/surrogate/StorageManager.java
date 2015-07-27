package surrogate;

import java.io.BufferedReader;
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
	public final ConcurrentLinkedQueue<ComputationResultRequest> computationResultRequestQueue = new ConcurrentLinkedQueue<ComputationResultRequest>();
	public final ConcurrentLinkedQueue<String> successfullyReceivedTickets = new ConcurrentLinkedQueue<String>();
	private final ArrayList<JSONObject> storedWeatherObjects = new ArrayList<JSONObject>();	
	private final ArrayList<JSONObject> storedForecastObjects = new ArrayList<JSONObject>();
	private final ArrayList<JSONObject> storedComputationResultObjects = new ArrayList<JSONObject>();	
	
	StorageManager(){
		running = true;
		loadDummyData();
		System.out.println("Storage Manager successfully started. @Storage manager.");
	}
	
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
    	
    	//load dummy forecasts
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
    	//end load dummy forecasts
    	
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
			if(!computationResultRequestQueue.isEmpty()){
				ComputationResultRequest request = computationResultRequestQueue.poll();
				if(request != null){
					handleComputationResultRequest(request);
				}
			}
			if(!successfullyReceivedTickets.isEmpty()){
				String ticket = successfullyReceivedTickets.poll();
				if(ticket != null){
					removeResultByTicket(ticket);
				}
			}
			
			try {
				Thread.sleep(SLEEP_TIME_PER_CYCLE);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep. @Storage Manager.");
				e.printStackTrace();
			}
		}
		System.out.println("Storage manager closing down. @Storage manager.");
	}
	
	private void removeResultByTicket(String ticket) {
		for(JSONObject obj : storedComputationResultObjects){
			String oldTicket = (String)obj.get("ticket");
			if(ticket.equals(oldTicket)){
				storedComputationResultObjects.remove(obj);
				System.out.println("Removed computation results from storage. Ticket: " +ticket + " @Storage manager.");
				return;
			}
		}
		System.out.println("Could not remove computation results from storage. Ticket not found: " +ticket + " @Storage manager.");
	}

	private void computationStorage() {
		JSONObject obj = computationResultStorageQueue.poll();
		if(obj != null){
			storedComputationResultObjects.add(obj);
			String ticket = "unknown";
			try{
				ticket = (String) obj.get("ticket");
			} catch (Exception e) {
				System.out.println("Result object has no ticket. @Storage Manager.");
				e.printStackTrace();
			}
			System.out.println("Saved JSON computation result object to storage. @Storage manager. Ticket: " + ticket);
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

	@SuppressWarnings("unchecked")
	private void handleComputationResultRequest(ComputationResultRequest request) {
		String requestedTicket = request.ticket;
		System.out.println("Creating response for computation result request, number: " + requestedTicket +  ".  @Storage Manager");
		boolean foundTicket = false;
		for(JSONObject obj : storedComputationResultObjects){
			String ticket = (String)obj.get("ticket");
			if(ticket.equals(requestedTicket)){
				request.response = obj;
				foundTicket = true;
			}
		}
		if(!foundTicket){
			JSONObject response = new JSONObject();
			response.put("response", "unknown");
			request.response = response;
			System.out.println("Received request for unknown ticket number: " + requestedTicket +  ". @Storage Manager");
		}
		request.ready = true;
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
