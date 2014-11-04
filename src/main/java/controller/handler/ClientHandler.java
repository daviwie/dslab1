package controller.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import controller.CloudController;
import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.NodeData;
import controller.persistence.UserConcurrentHashMap;

public class ClientHandler implements Runnable {

	private Socket socket;
	// Prepare the input reader for the socket
	private BufferedReader reader;
	// Prepare writer for responding to client requests
	private PrintWriter writer;
	// Client request
	private String request;
	private UserConcurrentHashMap userMap;
	private NodeConcurrentHashMap nodeMap;

	public ClientHandler(Socket socket, UserConcurrentHashMap userMap, NodeConcurrentHashMap nodeMap) {
		this.socket = socket;
		this.userMap = userMap;
		this.nodeMap = nodeMap;
	}

	@Override
	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			// Read client requests
			while ((request = reader.readLine()) != null) {
				String parts[] = request.split("_");
				
				switch(parts[0]) {
				
				case "login":
					writer.println(login(parts));
				case "logout":
					writer.println(logout(parts));
				case "credits":
					writer.println(credits(parts));
				case "buy":
					writer.println(buy(parts));
				case "list":
					writer.println(list(parts));
				case "compute":
					writer.println(compute(parts));
				default:
					writer.println("Unknown command, please try again with a known command");
				}
				
			}
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}
	
	private String login(String[] input) {
		return null;
		
	}
	
	private String logout(String[] input) {
		return null;
	}
	
	private String credits(String[] input) {
		return null;
	}
	
	private String buy(String[] inputs) {
		return null;
	}
	
	private String list(String[] input) {
		return null;
	}
	
	private String compute(String[] input) {
		return null;
	}
	
	private String sendToNode(NodeData node, String term) {
		return null;
	}

}
