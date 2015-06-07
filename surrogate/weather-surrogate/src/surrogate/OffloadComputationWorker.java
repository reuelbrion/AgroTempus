package surrogate;

public class OffloadComputationWorker implements Runnable {
	private ComputationRequest computationRequest;
	
	public OffloadComputationWorker(ComputationRequest computationRequest) {
		this.computationRequest = computationRequest;
	}

	public void run() {
		if(computationRequest != null){
			
		}
	}

}
