package controller.handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.CloudController;
import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.UserConcurrentHashMap;

public class ClientListener implements Runnable {
	private ServerSocket serverSocket;
	private final ExecutorService clients;
	private boolean isStopped = false;
	private UserConcurrentHashMap userMap;
	private NodeConcurrentHashMap nodeMap;
	
	public ClientListener(ServerSocket serverSocket, UserConcurrentHashMap userMap, NodeConcurrentHashMap nodeMap, ExecutorService pool) {
		this.serverSocket = serverSocket;
		this.userMap = userMap;
		this.nodeMap = nodeMap;
		clients =  Executors.newFixedThreadPool(4);
	}
	
	public void run() {		
		while (!isStopped()) {
			try {
				ClientHandler client = new ClientHandler(serverSocket.accept(), userMap, nodeMap);
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
