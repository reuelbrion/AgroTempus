package surrogate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RequestServer implements Runnable {

final static int SERVER_PORT = 11113;

public volatile boolean running;
public volatile StorageManager storageManager;

RequestServer(StorageManager storageManager){
	this.storageManager = storageManager;
}
	
	@Override
	public void run() {
		running = true;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server socket successfully opened. @Request server.");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SERVER_PORT + ".");
            running = false;
        }

        while(running){
        	Socket acceptSocket = null;
            try {
                acceptSocket = serverSocket.accept();
                System.out.println("connection from mobile accepted. @Request server.");
                Thread newThread = new Thread(new RequestServerWorker(acceptSocket, storageManager));
                newThread.start();
            } catch (IOException e) {
                System.err.println("Accept failed. @Request server.");
            }
        }  
	}
}
