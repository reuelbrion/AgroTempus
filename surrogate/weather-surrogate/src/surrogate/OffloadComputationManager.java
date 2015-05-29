package surrogate;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OffloadComputationManager implements Runnable {
	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	public final ConcurrentLinkedQueue<ComputationRequest> computationRequestQueue = new ConcurrentLinkedQueue<ComputationRequest>();
	
	OffloadComputationManager(StorageManager storageManager){
		this.storageManager = storageManager;
		running = true;
	}

	public void run() {
		while(running){
			
		}

	}

}
