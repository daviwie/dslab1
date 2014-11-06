package node.container;

import java.util.ArrayList;

public class NodeAttr {
	private final String operations;
	private final String fileDir;
	private final Integer number;
	private final Integer tcpPort;
	private final String controllerHost;
	private final Integer controllerUdp;
	private final long nodeALive;
	private ArrayList<String> history;

	public NodeAttr(Integer number, String operations, String fileDir, Integer tcpPort, String controllerHost, Integer controllerUdp,
			long nodeAlive) {
		this.operations = operations;
		this.fileDir = fileDir;
		this.number = number;
		this.tcpPort = tcpPort;
		this.controllerHost = controllerHost;
		this.controllerUdp = controllerUdp;
		this.nodeALive = nodeAlive;
		history = new ArrayList<String>();
	}
	
	public Integer getNumber() {
		return number;
	}

	public String getOperations() {
		return operations;
	}

	public String getFileDir() {
		return fileDir;
	}
	
	public Integer getTcpPort() {
		return tcpPort;
	}

	public String getControllerHost() {
		return controllerHost;
	}

	public Integer getControllerUdp() {
		return controllerUdp;
	}

	public long getNodeALive() {
		return nodeALive;
	}
	
	public synchronized ArrayList<String> getHistory() {
		return history;
	}
}