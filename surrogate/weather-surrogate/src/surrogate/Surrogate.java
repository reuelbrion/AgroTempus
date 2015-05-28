package surrogate;

public class Surrogate {
	public final static String SERVICE_TYPE_RETRIEVE_FORECASTS = "retrieve_forecasts";
	public final static String SERVICE_TYPE_RETRIEVE_REGIONAL_DATA = "retrieve_regional_data";
	public final static String SERVICE_TYPE_STORE_WEATHER_DATA = "store_weather_data";
	public final static String SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS =  "retrieve_computation_results";
	public final static String SERVICE_TYPE_OFFLOAD_REGRESSION =  "offload_regression";
	public final static String SERVICE_TYPE_OFFLOAD_PREDICTION =  "offload_prediction";	

	public static void main(String[] args) {
		StorageManager storageManager = new StorageManager();
		Thread storageManagerThread = new Thread(storageManager);
		storageManagerThread.start();
		Thread storageServer = new Thread(new StorageServer(storageManager));
		storageServer.start();
		Thread requestServer = new Thread(new RequestServer(storageManager));
		requestServer.start();
		Thread offloadServer = new Thread(new OffloadServer(storageManager));
		offloadServer.start();
	}
}