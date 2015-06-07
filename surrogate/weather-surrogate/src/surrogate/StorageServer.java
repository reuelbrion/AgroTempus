package surrogate;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class StorageServer implements Runnable {
	final static int SERVER_PORT = 11112;
	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	
	StorageServer(StorageManager storageManager){
		this.storageManager = storageManager;
	}
	
	public void run() {
		running = true;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server socket successfully opened. @Storage server.");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SERVER_PORT + ". @Storage server.");
            running = false;
        }

        while(running){
        	Socket acceptSocket = null;
            try {
                acceptSocket = serverSocket.accept();
                System.out.println("connection from mobile accepted. @Storage server.");
                Thread newThread = new Thread(new StorageServerWorker(acceptSocket, storageManager));
                newThread.start();
            } catch (IOException e) {
                System.err.println("Accept failed. @Storage server");
            }
            
        }   
	}
}