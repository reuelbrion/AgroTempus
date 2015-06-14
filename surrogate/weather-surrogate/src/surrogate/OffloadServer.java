package surrogate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class OffloadServer implements Runnable {
	private static final int SERVER_PORT = 11114;
	private static final long SLEEP_TIME_ACCEPT_SOCKET = 1000;
	
	public volatile boolean running;
	public volatile StorageManager storageManager;
	public volatile OffloadComputationManager offloadComputationManager;
	
	OffloadServer(StorageManager storageManager, OffloadComputationManager offloadComputationManager){
		this.storageManager = storageManager;
		this.offloadComputationManager = offloadComputationManager;
		running = true;
	}
	
	public void run() {
		ServerSocketChannel serverSocket = null;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(SERVER_PORT));
            serverSocket.configureBlocking(false);
            System.out.println("Server socket successfully opened. @Offload server.");
        } catch (Exception e) {
            System.out.println("Could not listen on port: " + SERVER_PORT + ". @Offload server.");
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
						System.out.println("Couldn't sleep. @Offload server.");
						e.printStackTrace();
					}
                } else {
                	System.out.println("Connection from mobile accepted. @Offload server.");
                    Thread newThread = new Thread(new OffloadServerWorker(acceptSocket.socket(), storageManager, offloadComputationManager));
                    newThread.start();
                }
                
            } catch (IOException e) {
                System.out.println("Accept failed. @Offload server");
            }
        }   
        System.out.println("Offload server closing down. @Offload server.");
	}
}
