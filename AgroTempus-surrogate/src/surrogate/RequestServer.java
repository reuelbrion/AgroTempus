package surrogate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class RequestServer implements Runnable {

private static final int SERVER_PORT = 11113;
private static final long SLEEP_TIME_ACCEPT_SOCKET = 1000;

public volatile boolean running;
public volatile StorageManager storageManager;

RequestServer(StorageManager storageManager){
	this.storageManager = storageManager;
}
	public void run() {
		running = true;
        ServerSocketChannel serverSocket = null;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(SERVER_PORT));
            serverSocket.configureBlocking(false);
            System.out.println("Server socket successfully opened. @Request server.");
        } catch (Exception e) {
            System.out.println("Could not listen on port: " + SERVER_PORT + ". @Request server.");
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
						System.out.println("Couldn't sleep. @Request server.");
						e.printStackTrace();
					}
                } else {
                    System.out.println("Connection from mobile accepted. @Request server.");
                    Thread newThread = new Thread(new RequestServerWorker(acceptSocket.socket(), storageManager));
                    newThread.start();
                }
            } catch (IOException e) {
                System.out.println("Accept failed. @Request server.");
            }
        }  
        System.out.println("Request server closing down. @Request server.");
	}
}
