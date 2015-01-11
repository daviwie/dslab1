package controller.persistence;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import controller.container.NodeData;

/**
 * Extends a ConcurrentHashMap and provides additional functionality necessary to the management of a Node within the CloudController. 
 *
 */
@SuppressWarnings("serial")
public class NodeConcurrentHashMap extends ConcurrentHashMap<String, NodeData> {
	private final String supported = "+-*/";

	/**
	 * Updates a specific node. If the node is not already within the ConcurrentHashMap then the node is added and its status is set to be
	 * online (under the assumption that this method is called whenever a UDP datagram is received from a node.
	 * 
	 * @param ipAddr The node's IP address - the first part of its unique identifier
	 * @param tcpPort The node's TCP port sent with the UDP datagram - the second part of its unique identifier
	 * @param operations The operations that a node supports, these are sent with the UDP datagram
	 */
	public void update(String ipAddr, Integer tcpPort, String operations) {
		String id = ipAddr + ":" + tcpPort.intValue();
		NodeData node = get(id);

		if (node == null)
			put(id, new NodeData(ipAddr, tcpPort, operations));
		else {
			node.setOperations(parseOperations("", operations));
			node.setAlive(true);
		}
	}

	/**
	 * Adds new operations to a node's already supported operations. In the case of a new node the first parameter should be left blank.
	 * 
	 * @param original A node's already supported operations
	 * @param newOperations Potentially new operations
	 * @return The completed series of operations that a node can support
	 */
	private String parseOperations(String original, String newOperations) {
		for (int i = 0; i < newOperations.length(); i++) {
			char c = newOperations.charAt(i);
			if (supported.indexOf(c) >= 0 && original.indexOf(c) < 0) {
				original += newOperations.charAt(i);
			}
		}

		return original;
	}

	/**
	 * @return Returns all supported operations from nodes that are currently online.
	 */
	public String getOperations() {
		String result = "";

		for (String key : keySet()) {
			NodeData temp = get(key);

			if (temp.isAlive()) {
				result = parseOperations(result, temp.getOperations());
			}
		}

		return result;
	}

	/**
	 * Searches through the ConcurrentHashMap and looks for the node that fits the best usage conditions, in this case the node with the
	 * smallest usage/smallest load that also supports the operation.
	 * 
	 * @param operation The operation that needs to be calculated
	 * @return All of the necessary information for a Node in order to facilitate a connection
	 */
	public NodeData getBestNode(String operation) {
		NodeData result = null;

		for (String key : keySet()) {
			NodeData temp = get(key);

			if (temp.isAlive() && temp.getOperations().contains(operation)) {
				if (result == null) {
					result = temp;
				} else if (temp.getUsage() < result.getUsage()) {
					result = temp;
				}
			}
		}

		return result;
	}

	/**
	 * Checks all nodes in the ConcurrentHashMap as to whether or not they have timed out. If so, the node is set to be offline until it
	 * sends another isAlive UDP datagram. If a Node's lastAlive + timeoutPeriod is less than the current time in milliseconds, the Node has
	 * timed out.
	 * 
	 * @param timeoutPeriod The maximum amount of time (in milliseconds) since a Node's lastAlive attribute
	 */
	public void updateNodeAlive(long timeoutPeriod) {
		/*
		 * Empty java.util.Date constructor initializes with the current date/time in milliseconds
		 */
		long currentTime = new Date().getTime();

		for (String key : keySet()) {
			NodeData temp = get(key);

			if (temp.getLastAlive() + timeoutPeriod <= currentTime) {
				temp.setAlive(false);
			}
		}
	}

	/**
	 * Constructs a string representation of this data structure.
	 * 
	 * @return A list of nodes with an unsorted numbered list, IP address, port, online/offline status and usage stats
	 */
	public String listNodes() {
		int counter = 1;
		String output = "";

		for (String key : keySet()) {
			NodeData node = get(key);
			output += counter + ". IP: " + node.getIpAddr() + "Port: " + node.getTcpPort() + " " + node.stringAlive() + " Usage: " + node.getUsage() + "\n";
			counter++;
		}

		return output;
	}

}
