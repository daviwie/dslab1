package controller.timer;

import java.util.TimerTask;

import controller.persistence.NodeConcurrentHashMap;

/**
 * Checks to see if a node is alive at regular intervals. 
 *
 */
public class AliveTimerTask extends TimerTask {
	private NodeConcurrentHashMap nodeMap;
	private long aliveTimeout;

	/**
	 * Generates a new TimerTask of type AliveTimerTask
	 * 
	 * @param nodeMap All of the nodes in the system that need to regularly be checked if they have timed out
	 * @param aliveTimeout The interval in milliseconds when nodes should be checked for alive
	 */
	public AliveTimerTask(NodeConcurrentHashMap nodeMap, long aliveTimeout) {
		this.nodeMap = nodeMap;
		this.aliveTimeout = aliveTimeout;
	}

	@Override
	public void run() {
		nodeMap.updateNodeAlive(aliveTimeout);
	}

}
