package node.timer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

public class AliveTimerTask extends TimerTask {
	private String message;
	private DatagramSocket datagramSocket;
	private InetAddress inetAddr;
	private Integer port;

	public AliveTimerTask(DatagramSocket datagramSocket, String controllerHost, Integer port, String operations) {
		this.port = port;
		this.datagramSocket = datagramSocket;
		message = "alive " + port + " " + operations;
	}

	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddr, port);
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
