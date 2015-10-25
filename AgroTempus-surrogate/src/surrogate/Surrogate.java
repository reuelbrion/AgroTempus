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
	private static final long SLEEP_TIME_RUNNING = 7000;//ms

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
	}
	
	public static void main(String[] args) {	
		new Surrogate().run();
	}

	private void run() {
		initComponents();
		while(running){
			checkComponentsAlive();
			if(userExit){
				closeSurrogate();
			}
			if(userExitUI){
				closeUI();
				userExitUI = false;
			}
			checkSTDIN();
			try {
				Thread.sleep(SLEEP_TIME_RUNNING);
			} catch (InterruptedException e) {
				System.out.println("Couldn't sleep.");
				e.printStackTrace();
			}
		}
		System.out.println("Surrogate closed.");
	}

	private void checkComponentsAlive() {
		if(!surrogateUIThread.isAlive()){
			System.out.println("UI inactive. Restarting.");
			surrogateUIRestart();
		}
		if(!storageManagerThread.isAlive()){
			System.out.println("Storage Manager inactive. Restarting.");
			startStorageManager();
		}
		if(!offloadComputationManagerThread.isAlive()){
			System.out.println("Offload Computation Manager inactive. Restarting.");
			startOffloadComputationManager();
		}
		if(!storageServerThread.isAlive()){
			System.out.println("Storage Server inactive. Restarting.");
			startStorageServer();
		}
		if(!requestServerThread.isAlive()){
			System.out.println("Request Server inactive. Restarting.");
			startRequestServer();
		}
		if(!offloadServerThread.isAlive()){
			System.out.println("Offload Server inactive. Restarting.");
			startOffloadServer();
		}
	}

	private void startSetupManager() {
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
	}

	private void startOffloadComputationManager() {
		offloadComputationManager = new OffloadComputationManager(storageManager);
		offloadComputationManagerThread = new Thread(offloadComputationManager);
		offloadComputationManagerThread.start();
	}

	private void startStorageManager() {
		storageManager = new StorageManager();
		storageManagerThread = new Thread(storageManager);
		storageManagerThread.start();
	}

	private void startStorageServer() {
		storageServer = new StorageServer(storageManager);
		storageServerThread = new Thread(storageServer);
		storageServerThread.start();
	}

	private void startRequestServer() {
		requestServer = new RequestServer(storageManager);
		requestServerThread = new Thread(requestServer);
		requestServerThread.start();
	}
	
	private void startOffloadServer() {
		offloadServer = new OffloadServer(storageManager, offloadComputationManager, name);
		offloadServerThread = new Thread(offloadServer);
		offloadServerThread.start();
	}

	private void surrogateUIRestart() {
		surrogateUI = null;
		startNewUI();
	}

	private void checkSTDIN() {
		try {
			boolean reading = true;
			while(stdinReader.ready() && reading){
				char c = (char)stdinReader.read();
				if(c == '\n' || c == '\r'){
					String input = sb.toString();
					if(input.equals("ui")){
						startNewUI();
					} else {
						sb.setLength(0);
						System.out.println("Unknown command on stdin.");
					}
					reading = false;
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
		startSetupManager();
		name = setupManager.getSurrogateName();
		surrogateUI.setName(name);
		System.out.println("Initializing components.");
		startStorageManager();
		startOffloadComputationManager();
		startStorageServer();
		startRequestServer();
		startOffloadServer();
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