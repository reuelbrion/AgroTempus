package surrogate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class StorageServer implements Runnable {
	private static final int SERVER_PORT = 11112;
	private static final long SLEEP_TIME_ACCEPT_SOCKET = 1000;
	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	
	StorageServer(StorageManager storageManager){
		this.storageManager = storageManager;
	}
	
	public void run() {
		running = true;
        ServerSocketChannel serverSocket = null;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(SERVER_PORT));
            serverSocket.configureBlocking(false);
            System.out.println("Server socket successfully opened. @Storage server.");
        } catch (Exception e) {
            System.out.println("Could not listen on port: " + SERVER_PORT + ". @Storage server.");
            running = false;
        }

        while(running){
        	SocketChannel acceptSocket = null;
            try {
                acceptSocket = serverSocket.accept();
                if (acceptSocket == null) {
                    try {
						Thread.sleep(SLEEP_TIME_ACCEPT_SOCKET);
					} catch (InterruptedException e) {
						System.out.println("Couldn't sleep. @Storage server.");
						e.printStackTrace();
					}
                } else {
                    System.out.println("Connection from mobile accepted. @Storage server.");
                    Thread newThread = new Thread(new StorageServerWorker(acceptSocket.socket(), storageManager));
                    newThread.start();
                }
            } catch (IOException e) {
                System.out.println("Accept failed. @Storage server.");
            }
        }    
        System.out.println("Storage server closing down. @Storage server.");
	}
}