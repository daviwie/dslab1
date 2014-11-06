package node.listener;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

import node.container.NodeAttr;

public class ControllerListener implements Runnable {
	
	private ServerSocket serverSocket;
	private ExecutorService pool;
	private NodeAttr node;

	public ControllerListener(ServerSocket serverSocket, ExecutorService pool, NodeAttr node) {
		this.serverSocket = serverSocket;
		this.pool = pool;
		this.node = node;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
