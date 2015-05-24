package surrogate;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class StorageServer implements Runnable {
	final static int SERVER_PORT = 11112;
	
	public volatile boolean running;
	StorageManager storageManager;
	
	StorageServer(StorageManager storageManager){
		this.storageManager = storageManager;
	}
	
	public void run() {
		running = true;
		ArrayList<Thread> threadList = new ArrayList<Thread>();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server socket successfully opened. Storage server.");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SERVER_PORT + ".");
            running = false;
        }

        while(running){
        	Socket acceptSocket = null;
            try {
                acceptSocket = serverSocket.accept();
                System.out.println("connection from mobile accepted. Storage server.");
                Thread newThread = new Thread(new StorageServerWorker(acceptSocket, storageManager));
                threadList.add(newThread);
                newThread.start();
            } catch (IOException e) {
                System.err.println("Accept failed.");
            }
            
        }   
	}
}