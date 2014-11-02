package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

	private CloudController controller;
	private Socket socket;
	// Prepare the input reader for the socket
	private BufferedReader reader;
	// Prepare writer for responding to client requests
	private PrintWriter writer;
	// Client request
	private String request;

	public ClientHandler(CloudController controller, Socket socket) {
		this.controller = controller;
		this.setSocket(socket);
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("ERROR in ClientHandler: " + e.getMessage());
		}
	}

	@Override
	public void run() {
		try {
			// Read client requests
			while ((request = reader.readLine()) != null) {
				String parts[] = request.split("_");

				if (request.startsWith("login")) {
					synchronized (controller.getUserMap()) {
						String username = parts[1];
						String password = parts[2];

						if (controller.getUserMap().containsKey(username)) {
							if (controller.getUserMap().get(username).equals(password)) {
								if (!controller.getUserMap().get(username).isOnline()) {
									controller.getUserMap().get(username).setOnline(true);
									writer.println("Successfully logged in.");
								} else
									writer.println("You are already logged in!");
							} else
								writer.println("Wrong username or password.");
						} else
							writer.println("Wrong username or password.");
					}
				}

				if (request.startsWith("logout")) {
					synchronized (controller.getUserMap()) {
						String username = parts[1];

						if (controller.getUserMap().get(username).isOnline()) {
							controller.getUserMap().get(username).setOnline(false);
							writer.println("Successfully logged out.");
						} else
							writer.println("You are not logged in.");
					}

				}

				if (request.startsWith("credits")) {
					synchronized (controller.getUserMap()) {
						String username = parts[1];

						writer.println(controller.getUserMap().get(username).getCredits());
					}
				}

				if (request.startsWith("buy")) {
					synchronized (controller.getUserMap()) {
						String username = parts[1];
						String credits = parts[2];

						Integer oldCredits = controller.getUserMap().get(username).getCredits();
						controller.getUserMap().get(username).setCredits(oldCredits + Integer.parseInt(credits));
						writer.println(controller.getUserMap().get(username).getCredits());
					}
				}

				if (request.startsWith("list")) {
					// TODO
				}

				if (request.startsWith("compute")) {
					synchronized (controller.getUsageStats()) {
						Iterator<Entry<String, Integer>> it = controller.getUsageStats().entrySet().iterator();
						ConcurrentHashMap.Entry<String, Integer> lowest = null;
						
						while (it.hasNext()) {
							if (lowest == null)
								lowest = (ConcurrentHashMap.Entry<String, Integer>) it.next();
							else {
								ConcurrentHashMap.Entry<String, Integer> pairs = (ConcurrentHashMap.Entry<String, Integer>) it.next();
								if (pairs.getValue() < lowest.getValue()) {
									lowest = pairs;
								}
							}
						}
						
						synchronized(controller.getNodes()) {
							// TODO Connect to lowest
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

}
