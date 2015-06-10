package surrogate;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OffloadComputationManager implements Runnable {	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	public final ConcurrentLinkedQueue<ComputationRequest> computationRequestQueue = new ConcurrentLinkedQueue<ComputationRequest>();
	
	OffloadComputationManager(StorageManager storageManager){
		this.storageManager = storageManager;
		running = true;
		System.out.println("Computation manager successfully started. @Computation manager.");
	}

	public void run() {
		while(running){
			if(!computationRequestQueue.isEmpty()){
				System.out.println("Creating new computation thread. @Computation manager.");
                Thread newThread = new Thread(new OffloadComputationWorker(computationRequestQueue.poll(), storageManager));
                newThread.start();
			}
		}
	}
}
