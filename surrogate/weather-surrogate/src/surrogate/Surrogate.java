package surrogate;

public class Surrogate {
	public final static String SERVICE_TYPE_RETRIEVE_FORECASTS = "retrieve_forecasts";
	public final static String SERVICE_TYPE_RETRIEVE_REGIONAL_DATA = "retrieve_regional_data";
	public final static String SERVICE_TYPE_STORE_WEATHER_DATA = "store_weather_data";
	public final static String SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS =  "retrieve_computation_results";
	public final static String SERVICE_TYPE_OFFLOAD_REGRESSION =  "offload_regression";
	public final static String SERVICE_TYPE_OFFLOAD_PREDICTION =  "offload_prediction";

	public volatile boolean userExit;
	private boolean running;
	private SurrogateUI surrogateUI;
	private StorageManager storageManager;
	private OffloadComputationManager offloadComputationManager;
	private StorageServer storageServer;
	private RequestServer requestServer;
	private OffloadServer offloadServer;
		
	public Surrogate(){
		running = true;
		userExit = false;
	}
	
	public static void main(String[] args) {	
		new Surrogate().run();
	}

	private void run() {
		initComponents();
		while(running){
			//TODO: check if managers are alive etc...
			if(userExit){
				closeSurrogate();
			}
		}
	}
	
	private void initComponents() {
		surrogateUI = new SurrogateUI(this);
		Thread surrogateUIThread = new Thread(surrogateUI);
		surrogateUIThread.start();
		//TODO: timeout
		while(!surrogateUI.initiated);	
		System.out.println("Surrogate Initializing.");
		storageManager = new StorageManager();
		Thread storageManagerThread = new Thread(storageManager);
		storageManagerThread.start();
		offloadComputationManager = new OffloadComputationManager(storageManager);
		Thread offloadComputationManagerThread = new Thread(offloadComputationManager);
		offloadComputationManagerThread.start();
		storageServer = new StorageServer(storageManager);
		Thread storageServerThread = new Thread(storageServer);
		storageServerThread.start();
		requestServer = new RequestServer(storageManager);
		Thread requestServerThread = new Thread(requestServer);
		requestServerThread.start();
		offloadServer = new OffloadServer(storageManager, offloadComputationManager);
		Thread offloadServerThread = new Thread(offloadServer);
		offloadServerThread.start();
	}

	private void closeSurrogate(){
		System.out.println("Surrogate closing down.");
		storageManager.running = false;
		offloadComputationManager.running = false;
		storageServer.running = false;
		requestServer.running = false;
		offloadServer.running = false;
	}
}