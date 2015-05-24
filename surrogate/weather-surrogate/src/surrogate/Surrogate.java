package surrogate;

public class Surrogate {
	public final static int SERVICE_STORE_WEATHER_DATA = 1;
	public final static int SERVICE_RETRIEVE_REGIONAL_DATA = 2;
	public final static int SERVICE_RETRIEVE_COMPUTATION_RESULTS = 3;

	public static void main(String[] args) {
		StorageManager storageManager = new StorageManager();
		Thread storageManagerThread = new Thread(storageManager);
		storageManagerThread.start();
		Thread storageServer = new Thread(new StorageServer(storageManager));
		storageServer.start();
	}

}