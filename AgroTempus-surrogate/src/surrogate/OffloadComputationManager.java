package surrogate;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OffloadComputationManager implements Runnable {
	final static long SLEEP_TIME_IDLE_CYCLE = 10000; //ms
	
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
				ComputationRequest request = computationRequestQueue.poll();
				if(request != null){
					System.out.println("Creating new computation thread for ticket " + request.ticket + " @Computation manager.");
	                Thread newThread = new Thread(new OffloadComputationWorker(request, storageManager, computationRequestQueue));
	                newThread.start();
				}
			} else {
				try {
					Thread.sleep(SLEEP_TIME_IDLE_CYCLE);
				} catch (InterruptedException e) {
					System.out.println("Couldn't sleep. @Computation manager.");
					e.printStackTrace();
				}
			}
		}
		System.out.println("Computation manager closing down. @Computation manager.");
	}
}
