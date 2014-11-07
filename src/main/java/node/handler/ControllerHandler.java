package node.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import node.container.NodeAttr;
import node.logger.Logger;

public class ControllerHandler implements Runnable {
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private NodeAttr node;
	private String request;
	private final Logger logger;

	public ControllerHandler(Socket socket, NodeAttr node) {
		this.socket = socket;
		this.node = node;
		logger = new Logger();
	}

	@Override
	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			// Read controller requests
			while ((request = reader.readLine()) != null) {
				String parts[] = request.split("_");

				if (parts.length != 4)
					writer.println("ERROR: Missing arguments for compute!");
				else if (parts[0].equals("compute"))
					writer.println(compute(parts));
				else
					writer.println("ERROR: Invalid command!");
			}
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	private String compute(String[] input) {
		try {
			Integer operand1 = Integer.parseInt(input[1]);
			Integer operand2 = Integer.parseInt(input[3]);

			if (!node.getOperators().contains(input[2]))
				return "Error: Operation not supported!";

			String result = "";

			switch (input[2]) {
			case "+":
				result = String.valueOf(operand1 + operand2);
			case "-":
				result = String.valueOf(operand1 - operand2);
			case "*":
				result = String.valueOf(operand1 * operand2);
			case "/":
				if(operand2 == 0)
					return "Error: No division by zero!";
				result = String.valueOf(Math.round((float) operand1 / operand2));
			default:
				result = "Error: Unsupported operation!";
			}

			logger.log(node.getFileDir(), node.getNumber(), operand1 + " " + input[2] + " " + operand2 + " ", result);
			node.getHistory().add(operand1 + " " + input[2] + " " + operand2 + " = " + result);
			
			return result;
		} catch (NumberFormatException e) {
			return "Error: Input can only contain valid integers!";
		}
	}
}
