package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

import cli.Command;
import cli.Shell;
import util.Config;

/**
 * Provides a thread for a client to access the Cloud Controller and perform calculations.
 *
 */
public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	private Shell shell;

	private String controllerHost;
	private Integer controllerTcpPort;

	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	private boolean isLoggedIn = false;
	private String currentlyLoggedIn = null;

	/**
	 * @param componentName the name of the component - represented in the prompt
	 * @param config the configuration to use
	 * @param userRequestStream the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public Client(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		this.controllerHost = this.config.getString("controller.host");
		this.controllerTcpPort = this.config.getInt("controller.tcp.port");

		try {
			/*
			 * From user/program perspective, we are outputting data to the server and the server is inputting data back to the
			 * user/program.
			 */
			socket = new Socket(controllerHost, controllerTcpPort);
			/*
			 * Writes the actual user output to the socket.
			 */
			out = new PrintWriter(socket.getOutputStream(), true);
			/*
			 * Readers the socket/server's output.
			 */
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.out.println("CLIENT ERROR: " + e.getMessage());
		}
	}

	@Override
	public void run() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		new Thread(shell).start();
	}

	/**
	 * Attempts to log in a user into the system. If the user is already logged in, an appropriate error message is returned. However, if a
	 * user is not logged in, the input is passed to the system in the form of "username_password".
	 * 
	 * @param username User to be logged into the system
	 * @param password User password to be checked against username
	 * @return The system's response
	 * @throws IOException
	 */
	@Override
	@Command
	public String login(String username, String password) throws IOException {
		if (isLoggedIn) {
			return "You are already logged in!";
		} else {
			out.println("login_" + username + "_" + password);
			String response = in.readLine();
			if (response.equals("Successfully logged in.")) {
				setLoggedIn(true);
				setCurrentlyLoggedIn(username);

				return response;
			} else
				return response;
		}
	}

	/**
	 * Logs a user out of the system. If the user is not logged in, an appropriate error message is returned. Else the user is logged out.
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	@Command
	public String logout() throws IOException {
		if (!isLoggedIn) {
			return "You are not logged in!";
		} else {
			out.println("logout_" + getCurrentlyLoggedIn());
			setCurrentlyLoggedIn(null);
			setLoggedIn(false);

			return in.readLine();
		}
	}

	/**
	 * Displays the credits for the currently logged in user.
	 * 
	 * @return the credits for the currently logged in user
	 * @throws IOException
	 */
	@Override
	@Command
	public String credits() throws IOException {
		if (isLoggedIn) {
			out.println("credits_" + getCurrentlyLoggedIn());

			return in.readLine();
		} else
			return "You must first log in!";
	}

	/**
	 * Buys credits for the currently logged in user.
	 * 
	 * @param credits
	 * @return An appropriate feedback message
	 * @throws IOException
	 */
	@Override
	@Command
	public String buy(long credits) throws IOException {
		if (isLoggedIn) {
			out.println("buy_" + getCurrentlyLoggedIn() + "_" + credits);

			return in.readLine();
		} else
			return "You must first log in!";
	}

	/**
	 * Displays a list of all arithmetic operations that can be performed
	 * 
	 * @return all available operations
	 * @throws IOException
	 */
	@Override
	@Command
	public String list() throws IOException {
		if (isLoggedIn) {
			out.println("list");

			return in.readLine();
		} else
			return "You must first log in!";
	}

	/**
	 * Sends a term to the system to be calculated. The result is returned. The method also checks to make sure that no division by zero is
	 * possible. If a term contains any division by zero, an error message is returned.
	 * 
	 * @param term
	 * @return the result of a term
	 * @throws IOException
	 */
	@Override
	@Command
	public String compute(String term) throws IOException {
		if (isLoggedIn) {
			out.println("compute_" + getCurrentlyLoggedIn() + "_" + term);
			return in.readLine();
		} else
			return "You must first log in!";
	}

	/**
	 * Closes the socket connection to the system and closes all open resources.
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	@Command
	public String exit() throws IOException {
		logout();
		socket.close();
		shell.close();
		out.close();
		in.close();
		userRequestStream.close();
		userResponseStream.close();
		return "Exiting now";
	}

	/**
	 * @return true if logged in, else false
	 */
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * Sets whether a client is logged into a current session or not
	 * 
	 * @param isLoggedIn
	 */
	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	/**
	 * @return the username of the currently logged in client
	 */
	public String getCurrentlyLoggedIn() {
		return currentlyLoggedIn;
	}

	/**
	 * Sets the username of the currently logged in client
	 * 
	 * @param currentlyLoggedIn
	 */
	public void setCurrentlyLoggedIn(String currentlyLoggedIn) {
		this.currentlyLoggedIn = currentlyLoggedIn;
	}

	/**
	 * @param args the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in, System.out);
		new Thread(client).start();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
