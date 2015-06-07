package surrogate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class OffloadServer implements Runnable {
final static int SERVER_PORT = 11114;
	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	public volatile OffloadComputationManager offloadComputationManager;
	
	OffloadServer(StorageManager storageManager, OffloadComputationManager offloadComputationManager){
		this.storageManager = storageManager;
		this.offloadComputationManager = offloadComputationManager;
		running = true;
	}
	
	public void run() {
		ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server socket successfully opened. @Offload server.");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SERVER_PORT + ". @Offload server.");
            running = false;
        }

        while(running){
        	Socket acceptSocket = null;
            try {
                acceptSocket = serverSocket.accept();
                System.out.println("connection from mobile accepted. @Offload server.");
                Thread newThread = new Thread(new OffloadServerWorker(acceptSocket, storageManager, offloadComputationManager));
                newThread.start();
            } catch (IOException e) {
                System.err.println("Accept failed. @Offload server");
            }
            
        }   
	}
}
