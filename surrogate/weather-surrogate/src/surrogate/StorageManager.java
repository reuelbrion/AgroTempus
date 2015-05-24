package surrogate;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.simple.JSONObject;

public class StorageManager implements Runnable {
	private static final long SLEEP_TIME_PER_CYCLE = 10000;
	
	public volatile boolean running;
	public final ConcurrentLinkedQueue<JSONObject> weatherStorageQueue = new ConcurrentLinkedQueue<JSONObject>();
	final private ArrayList<JSONObject> storedWeatherObjects = new ArrayList<JSONObject>();	
	
	public void run() {
		running = true;
		while(running){
			if(!weatherStorageQueue.isEmpty()){
				JSONObject obj = weatherStorageQueue.poll();
				if(obj != null){
					storedWeatherObjects.add(obj);
					System.out.println("Saved JSON object to storage. Storage manager.\n" + obj.toString());
				}
			}
			try {
				Thread.sleep(SLEEP_TIME_PER_CYCLE);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep. Storage Manager.");
				e.printStackTrace();
			}
		}
	}
}
