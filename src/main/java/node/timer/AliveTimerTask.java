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
	private Integer port;

	public AliveTimerTask(DatagramSocket datagramSocket, String controllerHost, Integer port, String operations) {
		// Get InetAddress direct rather than via datagramSocket
		this.port = port;
		this.datagramSocket = datagramSocket;
		message = "alive " + port + " " + operations;
	}

	@Override
	public void run() {
		try {
			inetAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddr, port);
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
