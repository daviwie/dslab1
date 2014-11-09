package controller.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

import controller.handler.ClientHandler;
import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.UserConcurrentHashMap;

public class ClientListener implements Runnable {
	private ServerSocket serverSocket;
	private final ExecutorService pool;
	private boolean isStopped = false;
	private UserConcurrentHashMap userMap;
	private NodeConcurrentHashMap nodeMap;
	
	public ClientListener(ServerSocket serverSocket, UserConcurrentHashMap userMap, NodeConcurrentHashMap nodeMap, ExecutorService pool) {
		this.serverSocket = serverSocket;
		this.userMap = userMap;
		this.nodeMap = nodeMap;
		this.pool =  pool;
	}
	
	private boolean isStopped() {
		return isStopped;
	}

	public void run() {		
		while (!isStopped()) {
			try {
				ClientHandler client = new ClientHandler(serverSocket.accept(), userMap, nodeMap);
				pool.execute(client);
				
				if(isStopped()) {
					close();
				}
			} catch (IOException e) {
				isStopped = true;
				System.out.println(e.getMessage());
			}
		}
		
		if(isStopped()) {
			close();
		}
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// Not handled
		}
		pool.shutdownNow();
	}
	
}
