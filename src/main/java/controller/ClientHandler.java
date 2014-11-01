package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private Socket socket;
	// Prepare the input reader for the socket
	private BufferedReader reader; 
	// Prepare writer for responding to client requests
	private PrintWriter writer;
	// Client request
	private String request;

	public ClientHandler(Socket socket) {
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
		// TODO Handle client input
		try {
			// Read client requests
			while ((request = reader.readLine()) != null) {
				
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
