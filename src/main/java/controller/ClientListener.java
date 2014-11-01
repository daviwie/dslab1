package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientListener implements Runnable {
	private ServerSocket serverSocket;
	private final ExecutorService clients;
	private boolean isStopped = false;
	private CloudController controller;
	
	public ClientListener(CloudController controller, ServerSocket serverSocket) {
		this.controller = controller;
		this.serverSocket = serverSocket;
		clients =  Executors.newFixedThreadPool(4);
	}
	
	public void run() {		
		while (!isStopped()) {
			try {			
				// TODO Spawn client handler thread
				ClientHandler client = new ClientHandler(controller, serverSocket.accept());
				clients.execute(client);
				
				if(isStopped()) {
					close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		
		if(isStopped()) {
			close();
		}
	}
	
	public void close() {
		// TODO Shut down the listener and its thread pool
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		clients.shutdownNow();
	}

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}
	
}
