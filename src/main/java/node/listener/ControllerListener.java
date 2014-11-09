package node.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

import node.container.NodeAttr;
import node.handler.ControllerHandler;

public class ControllerListener implements Runnable {

	private ServerSocket serverSocket;
	private ExecutorService pool;
	private NodeAttr node;
	private boolean isStopped = false;

	public ControllerListener(ServerSocket serverSocket, ExecutorService pool, NodeAttr node) {
		this.serverSocket = serverSocket;
		this.pool = pool;
		this.node = node;
	}

	private boolean isStopped() {
		return isStopped;
	}

	@Override
	public void run() {
		while (!isStopped()) {
			try {
				ControllerHandler controller = new ControllerHandler(serverSocket.accept(), node);
				pool.execute(controller);
			} catch (IOException e) {
				isStopped = true;
				System.out.println("ERROR: Failure to connect to ControllerHandler socket.");
			}

			if (isStopped()) {
				close();
			}
		}

		if (isStopped()) {
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
