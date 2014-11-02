package controller;

public class NodeData {
	private String name;
	private String ipAddr;
	private Integer tcpPort;
	private boolean alive;
	private Integer usage;

	public NodeData(String name, String ipAddr, Integer tcpPort) {
		this.name = name;
		this.ipAddr = ipAddr;
		this.tcpPort = tcpPort;
		this.alive = false;
		this.usage = 0;
	}

	public NodeData(String name, String ipAddr, Integer tcpPort, boolean alive) {
		this.name = name;
		this.ipAddr = ipAddr;
		this.tcpPort = tcpPort;
		this.alive = alive;
		this.usage = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public Integer getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(Integer tcpPort) {
		this.tcpPort = tcpPort;
	}

	public boolean isAlive() {
		return alive;
	}

	public String stringAlive() {
		if (alive)
			return "online";
		else
			return "offine";
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public Integer getUsage() {
		return usage;
	}

	public void setUsage(Integer usage) {
		this.usage = usage;
	}

	@Override
	public String toString() {
		String response;

		response = "IP: " + getIpAddr() + " Port: " + getTcpPort() + " "
				+ stringAlive() + " Usage: " + getUsage();

		return response;
	}

}
