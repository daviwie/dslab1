package controller.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import controller.container.NodeData;
import controller.persistence.NodeConcurrentHashMap;
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
	 *            user input split into an array of strings
	 *            login_username_password
	 * @return A message to the client that is output to the client's shell
	 *         stating whether or not log in was successful
	 */
	private String login(String[] input) {
		if (input.length != 3)
			return "Error: Invalid input!";

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
		if (input.length != 2)
			return "Error: Invalid input!";

		return userMap.logout(input[1]);
	}

	/**
	 * Provides the client with the current amount of credits available for
	 * calculations.
	 * 
	 * @param input
	 *            user input split into an array of strings credits_username
	 * @return The client's current credits
	 */
	private String credits(String[] input) {
		if (input.length != 2)
			return "Error: Invalid input!";

		return "You have " + userMap.get(input[1]).getCredits() + " credits.";
	}

	/**
	 * Adds a specific amount of credits to a user's account.
	 * 
	 * @param input
	 *            user input split into an array of strings buy_username_credits
	 * @return The client's current credits
	 */
	private String buy(String[] input) {
		if (input.length != 3)
			return "Error: Invalid input!";

		userMap.get(input[1]).buyCredits(Long.parseLong(input[3]));
		return "You now have " + userMap.get(input[1]).getCredits() + " credits.";
	}

	/**
	 * Lists all operations supported by the system
	 * 
	 * @param input
	 *            user input split into an array of strings
	 * @return all operations supported by the system
	 */
	private String list(String[] input) {
		if (input.length != 1)
			return "Error: Invalid input!";

		return nodeMap.getOperations();
	}

	/**
	 * Computes a formula as provided by the user. Steps: Check to make sure
	 * input length is actually valid. Check to see if input contains any divide
	 * by zero statements. Calculate the potential on credits that will be used
	 * provided that no node fails. Set the cursor on the first part of $TERM
	 * and then constructs a miniature term to be sent to the node that is made
	 * up of two operands and one operator. This mini term is sent to the node,
	 * calculated and returned. If $TERM has more than one operator, then the
	 * for loop continues collecting (semi-recursion) and sending the new cursor
	 * (replaced by the result of the mini term) and another operator and
	 * operand to the best available node.
	 * 
	 * @param input
	 *            user input split into an array of strings
	 *            compute_username_$TERM
	 * @return the result of the calculation
	 */
	private String compute(String[] input) {
		if (input.length != 3)
			return "Error: Invalid input!";

		if (input[2].contains("/ 0"))
			return "Division by zero not supported!";

		String[] termParts = input[2].split(" ");

		if ((termParts.length / 2) * 50 > userMap.get(input[1]).getCredits())
			return "Error: Not enough credits!";

		Integer usedCredits = (termParts.length / 2) * 50;

		try {
			// Set the cursor on the first part of the main term
			Integer cursor = Integer.parseInt(termParts[0]);

			for (int i = 1; i < termParts.length; i++) {
				// Find the best appropriate node
				NodeData node = nodeMap.getBestNode(termParts[i]);
				// If there is no node that supports the operation, error
				if (node == null)
					return "Error: " + termParts[i] + " is unsupported!";

				// Set our tempResult
				String tempResult = null;

				try {
					tempResult = sendToNode(node, "compute_" + cursor + "_" + termParts[i] + "_" + termParts[i + 1]);
				} catch (IOException e) {
					// TODO Handle node failure - rollback?
				}

				if (tempResult != null) {
					/*
					 * Replace cursor with the tempResult, provided it is not
					 * null so semi-recursion can continue
					 */
					cursor = Integer.parseInt(tempResult);
					// Increase node usage after successful calculation
					nodeMap.get(node.getKey()).incUsage(50 * tempResult.length());
				} else {
					/*
					 * Calculation failed, node set to offline and credits
					 * decremented by 50 for failed operation
					 */
					nodeMap.get(node.getKey()).setAlive(false);
					usedCredits -= 50;
				}

				// Jump to the next operation
				i++;
			}

			// TODO Roll back all changes if a node failed

			// Decrement the user calculating by the number of credits paid
			userMap.get(input[1]).decCredits(usedCredits);

			return cursor.toString();
		} catch (NumberFormatException e) {
			return "Error: Input can only contain valid integers!";
		}
	}

	private String sendToNode(NodeData node, String term) throws UnknownHostException, IOException {
		// Open a socket on a node
		Socket nodeSocket = new Socket(node.getIpAddr(), node.getTcpPort());
		PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

		// Write to node
		out.println(term);
		// Get result from node
		String result = in.readLine();

		// Close resources
		out.close();
		in.close();
		nodeSocket.close();

		return result;
	}

}
