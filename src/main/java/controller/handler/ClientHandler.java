package controller.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.NodeData;
import controller.persistence.UserConcurrentHashMap;
import controller.persistence.UserData;

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
	private UserData user;

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

				switch (parts[0]) {

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

	/**
	 * Attempts to log user into the system. If unsuccessful, an appropriate
	 * error message is returned.
	 * 
	 * @param input
	 *            user input split into an array of strings login_username_password
	 * @return A message to the client that is output to the client's shell
	 *         stating whether or not log in was successful
	 */
	private String login(String[] input) {
		return userMap.login(input[1], input[2]);
	}

	/**
	 * Attempts to log user out of the system. If unsuccessful, an appropriate
	 * error message is returned.
	 * 
	 * @param input
	 *            user input split into an array of strings logout_username
	 * @return A message to the client that is output to the client's shell
	 *         stating whether or not log out was successful
	 */
	private String logout(String[] input) {
		return userMap.logout(input[1]);
	}

	/**
	 * Provides the client with the current amount of credits available for calculations. 
	 * 
	 * @param input user input split into an array of strings credits_username
	 * @return The client's current credits
	 */
	private String credits(String[] input) {
		return "You have " + userMap.get(input[1]).getCredits() + " credits.";
	}

	/**
	 * Adds a specific amount of credits to a user's account. 
	 * 
	 * @param input user input split into an array of strings buy_username_credits
	 * @return The client's current credits
	 */
	private String buy(String[] input) {
		userMap.get(input[1]).buyCredits(Long.parseLong(input[3]));
		return "You now have " + userMap.get(input[1]).getCredits() + " credits.";
	}

	/**
	 * Lists all operations supported by the system
	 * 
	 * @param input user input split into an array of strings
	 * @return all operations supported by the system
	 */
	private String list(String[] input) {
		return nodeMap.getOperations();
	}

	/**
	 * Computes a formula as provided by the user.
	 * 
	 * @param input user input split into an array of strings compute_username_$TERM
	 * @return the result of the calculation
	 */
	private String compute(String[] input) {
		return null;
	}

	private String sendToNode(NodeData node, String term) {
		return null;
	}

}
