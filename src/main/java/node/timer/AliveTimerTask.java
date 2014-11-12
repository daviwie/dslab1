package node.timer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;

public class AliveTimerTask extends TimerTask {
	private String message;
	private DatagramSocket datagramSocket;
	private InetAddress inetAddr;
	private Integer udpPort;
	private Integer tcpPort;

	public AliveTimerTask(DatagramSocket datagramSocket, String controllerHost, Integer udpPort, Integer tcpPort, String operations) {
		this.datagramSocket = datagramSocket;
		try {
			inetAddr = InetAddress.getByName(controllerHost);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		}
		this.udpPort = udpPort;
		this.tcpPort = tcpPort;
		message = "alive " + this.tcpPort + " " + operations;
	}

	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddr, udpPort);
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
