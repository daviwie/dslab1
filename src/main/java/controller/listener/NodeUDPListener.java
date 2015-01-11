package controller.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import controller.persistence.NodeConcurrentHashMap;

/**
 * Listens on UDP for any incoming datagrams from a Node.
 *
 */
public class NodeUDPListener implements Runnable {
	private DatagramSocket datagramSocket;
	private NodeConcurrentHashMap nodeMap;

	public NodeUDPListener(DatagramSocket datagramSocket, NodeConcurrentHashMap nodeMap) {
		this.datagramSocket = datagramSocket;
		this.nodeMap = nodeMap;
	}

	@Override
	public void run() {
		byte[] buffer;
		DatagramPacket packet;

		try {
			while (true) {
				buffer = new byte[1024];

				packet = new DatagramPacket(buffer, buffer.length);

				datagramSocket.receive(packet);

				String nodeIp = packet.getAddress().getHostAddress();
				String[] parts = new String(packet.getData()).split("\\s+");

				if (parts[0].equals("alive") && parts.length == 3) {
					Integer tcpPort = Integer.parseInt(parts[1]);
					nodeMap.update(nodeIp, tcpPort, parts[2]);
				}
			}
		} catch (IOException e) {

		}

	}
}
