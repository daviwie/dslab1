package controller.persistence;

import java.util.Date;

public class NodeData {
	// Combo of IP and port are used as keys in NodeConcurrentHashMap
	private final String ipAddr;
	private final Integer tcpPort;
	// Initially zero
	private Integer usage;
	// Always true when first created after receiving UDP datagram
	private boolean online;
	private String operations;
	// Time in milliseconds
	// TODO Switch to java.util.Date?
	private long lastAlive;

	/**
	 * Creates a NodeData object to be stored inside a NodeConcurrentHashMap
	 * 
	 * @param ipAddr The IP address where a node can be found
	 * @param tcpPort The TCP port used to connect 
	 * @param operations The operations supported by this node
	 */
	public NodeData(String ipAddr, Integer tcpPort, String operations) {
		this.ipAddr = ipAddr;
		this.tcpPort = tcpPort;
		this.operations = operations;
		this.usage = 0;
		this.online = true;
		this.lastAlive = new Date().getTime();
	}

	/**
	 * @return Get the IP Address of a node
	 */
	public String getIpAddr() {
		return ipAddr;
	}

	/**
	 * @return Get the TCP port of a node. This is sent as a parameter of its
	 *         isAlive UDP datagram
	 */
	public int getTcpPort() {
		return tcpPort;
	}

	/**
	 * @return A node's current usage/load
	 */
	public synchronized Integer getUsage() {
		return usage;
	}

	/**
	 * Increments the current usage by the difference of the new usage
	 * 
	 * @param usage
	 */
	public synchronized void incUsage(int usage) {
		this.usage += usage;
	}

	/**
	 * Overwrites the usage with the parameter
	 * 
	 * @param usage
	 *            the calculated node usage
	 */
	public synchronized void setUsage(Integer usage) {
		this.usage = usage;
	}

	/**
	 * @return true if online, else false
	 */
	public synchronized boolean isOnline() {
		return online;
	}

	/**
	 * Updates the node to be online or offline. If the parameter is true then
	 * the lastAlive is updated with the current Date in milliseconds.
	 * 
	 * @param online
	 *            if online true, else false
	 */
	public synchronized void setOnline(boolean online) {
		/*
		 * Empty java.util.Date constructor initializes with the current
		 * date/time in milliseconds
		 */
		if (online)
			this.lastAlive = new Date().getTime();

		this.online = online;
	}

	/**
	 * Used primarily in the toString method, but set to public to allow other
	 * access if necessary.
	 * 
	 * @return A string representation of a node's alive/dead state
	 */
	public synchronized String stringAlive() {
		// TODO Set to private?
		if (isOnline())
			return "online";
		else
			return "offline";
	}

	/**
	 * @return All operations supported by this node
	 */
	public synchronized String getOperations() {
		return operations;
	}

	/**
	 * @param operations
	 *            Sets a node's supported operations
	 */
	public synchronized void setOperations(String operations) {
		this.operations = operations;
	}

	/**
	 * @return Date in milliseconds
	 */
	public synchronized long getLastAlive() {
		return lastAlive;
	}

	/**
	 * @return Milliseconds represented as a java.util.Date object
	 */
	public synchronized Date getLastAliveDate() {
		return new Date(lastAlive);
	}

	/**
	 * Returns a string representation of a node with IP address, TCP port, its
	 * online/offline status and its current usage/load (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String response;

		response = "IP: " + getIpAddr() + " Port: " + getTcpPort() + " " + stringAlive() + " Usage: " + getUsage();

		return response;
	}

}
