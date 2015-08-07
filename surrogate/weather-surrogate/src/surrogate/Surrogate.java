package surrogate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Surrogate {
	public final static String SERVICE_TYPE_RETRIEVE_FORECASTS = "retrieve_forecasts";
	public final static String SERVICE_TYPE_RETRIEVE_REGIONAL_DATA = "retrieve_regional_data";
	public final static String SERVICE_TYPE_STORE_WEATHER_DATA = "store_weather_data";
	public final static String SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS = "retrieve_computation_results";
	public final static String SERVICE_TYPE_OFFLOAD_REGRESSION =  "offload_regression";
	public final static String SERVICE_TYPE_OFFLOAD_PREDICTION =  "offload_prediction";
	private static final long SLEEP_TIME_UI_LOADING = 250; //ms
	private static final long SLEEP_TIME_SETUP_MANAGER_LOADING = 100;//ms

	private final BufferedReader stdinReader;
	private final StringBuilder sb;
	
	public volatile boolean userExit;
	public volatile boolean userExitUI;
	private boolean running;
	private SurrogateUI surrogateUI;
	private StorageManager storageManager;
	private OffloadComputationManager offloadComputationManager;
	private StorageServer storageServer;
	private RequestServer requestServer;
	private OffloadServer offloadServer;
	private SetupManager setupManager;
	private Thread surrogateUIThread;
	private Thread storageManagerThread;
	private Thread offloadComputationManagerThread;
	private Thread storageServerThread;
	private Thread requestServerThread;
	private Thread offloadServerThread;
	private Thread setupManagerThread;
	public String name;
	
	public Surrogate(){
		running = true;
		userExit = false;
		stdinReader = new BufferedReader(new InputStreamReader(System.in));
		sb = new StringBuilder();
		//TODO: get name from setup data
		name = "Amsterdam-NL";
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
			if(userExitUI){
				closeUI();
				userExitUI = false;
			}
			checkSTDIN();
		}
		System.out.println("Surrogate closed.");
	}

	private void checkSTDIN() {
		try {
			if(stdinReader.ready()){
				char c = (char)stdinReader.read();
				if(c == '\n' || c == '\r'){
					String input = sb.toString();
					if(input.equals("ui")){
						startNewUI();
					} else {
						sb.setLength(0);
						System.out.println("Unknown command on stdin.");
					}
				} else {
					sb.append(c);
				}
			}
		} catch (IOException e) {
			System.out.println("Problem reading from STDIN.");
			e.printStackTrace();
		}
	}

	private void startNewUI() {
		if(surrogateUI == null){
			surrogateUI = new SurrogateUI(this);
			surrogateUIThread = new Thread(surrogateUI);
			surrogateUIThread.start();
		} else {
			System.out.println("Got \"ui\" command, but User Interface is already open.");
		}
	}

	private void closeUI() {
		//if running is false we are already closing down
		if(running){
			if(surrogateUI != null){
				surrogateUI.running = false;
			}
			try {
				surrogateUIThread.join();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Surrogate User Interface closed. Surrogate will keep running.\nEnter \"ui\" to restart User Interface.");
			surrogateUI = null;
		}
	}

	private void initComponents() {
		System.out.println("Surrogate Initializing.");
		surrogateUI = new SurrogateUI(this);
		surrogateUIThread = new Thread(surrogateUI);
		surrogateUIThread.start();
		while(!surrogateUI.initiated){
			try {
				Thread.sleep(SLEEP_TIME_UI_LOADING);
			} catch (InterruptedException e) {
				System.out.println("Can't sleep.");
				e.printStackTrace();
			}
		}
		System.out.println("Checking setup data.");
		setupManager = new SetupManager();
		setupManagerThread = new Thread(setupManager);
		setupManagerThread.start();
		while(!setupManager.ready){
			try {
				Thread.sleep(SLEEP_TIME_SETUP_MANAGER_LOADING);
			} catch (InterruptedException e) {
				System.out.println("Can't sleep.");
				e.printStackTrace();
			}
		}
		System.out.println("Setup data loaded.");

		System.out.println("Initializing components.");
		storageManager = new StorageManager();
		storageManagerThread = new Thread(storageManager);
		storageManagerThread.start();
		offloadComputationManager = new OffloadComputationManager(storageManager);
		offloadComputationManagerThread = new Thread(offloadComputationManager);
		offloadComputationManagerThread.start();
		storageServer = new StorageServer(storageManager);
		storageServerThread = new Thread(storageServer);
		storageServerThread.start();
		requestServer = new RequestServer(storageManager);
		requestServerThread = new Thread(requestServer);
		requestServerThread.start();
		offloadServer = new OffloadServer(storageManager, offloadComputationManager, name);
		offloadServerThread = new Thread(offloadServer);
		offloadServerThread.start();
	}

	private void closeSurrogate(){
		running = false;
		System.out.println("Surrogate closing. Stopping components.");
		if(storageManager != null){
			storageManager.running = false;
		}
		if(offloadComputationManager != null){
			offloadComputationManager.running = false;
		}
		if(storageServer != null){
			storageServer.running = false;
		}
		if(requestServer != null){
			requestServer.running = false;
		}
		if(offloadServer != null){
			offloadServer.running = false;
		}
		try {
			storageManagerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			offloadComputationManagerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			storageServerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			requestServerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			offloadServerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(surrogateUI != null){
			surrogateUI.running = false;
		}
		try {
			surrogateUIThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}