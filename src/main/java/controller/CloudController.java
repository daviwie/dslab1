package controller;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import controller.container.UserData;
import controller.listener.ClientListener;
import controller.listener.NodeUDPListener;
import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.UserConcurrentHashMap;
import controller.timer.AliveTimerTask;
import cli.Command;
import cli.Shell;

/**
 * Handles all communication between Clients and Nodes. Acts as a loud balancer of sorts.
 *
 */
public class CloudController implements ICloudControllerCli, Runnable {

	private String componentName;
	private Config controllerConfig;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	// TODO Change ports back to dslab456 in client, controller and node1 props.

	/*
	 * Everything past this point in variables was not part of the original class
	 */
	private Shell shell;
	// Read out config values
	private final Integer tcpPort;
	private final Integer udpPort;
	private final Integer nodeTimeout;
	private final Integer nodeCheckPeriod;
	private final Config userConfig;

	// Hashmap of all users stored in the config file along with their passwords
	private UserConcurrentHashMap userMap;

	// Hashmaps of nodes
	private NodeConcurrentHashMap nodeMap;

	// Thread pool
	private final ExecutorService pool;

	// Server utilities
	ServerSocket serverSocket;
	DatagramSocket datagramSocket;

	// Timer utilities
	private Timer aliveTimer;
	private AliveTimerTask aliveTimerTask;

	/**
	 * @param componentName the name of the component - represented in the prompt
	 * @param config the configuration to use
	 * @param userRequestStream the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public CloudController(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.controllerConfig = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		/*
		 * Initialize the user map
		 */
		userMap = new UserConcurrentHashMap();

		/*
		 * Initialize the node lists
		 */
		nodeMap = new NodeConcurrentHashMap();

		/*
		 * Read out all of the config information for the controller and initialize the variables
		 */
		tcpPort = controllerConfig.getInt("tcp.port");
		udpPort = controllerConfig.getInt("udp.port");
		nodeTimeout = controllerConfig.getInt("node.timeout");
		nodeCheckPeriod = controllerConfig.getInt("node.checkPeriod");

		/*
		 * Read user list from user.properties, build userMap and assign user attributes (password, credits)
		 */
		userConfig = new Config("user");
		Set<String> userKeys = userConfig.listKeys();

		for (String key : userKeys) {
			String[] parts = key.split("\\.");
			String userName = parts[0];
			String attr = parts[1];
			if (!userMap.containsKey(userName)) {
				UserData user = new UserData();
				user.setUserName(userName);
				if (attr.equals("credits"))
					user.setCredits(userConfig.getInt(userName + "." + attr));
				if (attr.equals("password"))
					user.setPassword(userConfig.getString(userName + "." + attr));
				userMap.put(userName, user);
			} else {
				if (attr.equals("credits"))
					userMap.get(userName).setCredits(userConfig.getInt(userName + "." + attr));
				if (attr.equals("password"))
					userMap.get(userName).setPassword(userConfig.getString(userName + "." + attr));
			}
		}

		/*
		 * Instantiate thread pool
		 */
		pool = Executors.newCachedThreadPool();

		/*
		 * Instantiate ServerSocket and DatagramSocket
		 */
		try {
			serverSocket = new ServerSocket(tcpPort);
			datagramSocket = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			System.out.println("ERROR: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		}

		aliveTimer = new Timer();
		aliveTimerTask = new AliveTimerTask(nodeMap, nodeTimeout);
	}

	@Override
	public void run() {
		/*
		 * Finish setup for CloudController. These instantiations are only performed once we're getting underway and need to begin spawning
		 * threads.
		 */

		// Spawn ClientListener
		ClientListener cL = new ClientListener(serverSocket, userMap, nodeMap, pool);
		pool.execute(cL);

		// Spawn NodeUDPListener
		NodeUDPListener nL = new NodeUDPListener(datagramSocket, nodeMap);
		pool.execute(nL);

		// Start timer
		aliveTimer.scheduleAtFixedRate(aliveTimerTask, 0, nodeCheckPeriod);

		/*
		 * Set the shell for the controller last so that way the program can go ahead and immediately start listening for connections. Other
		 * implementation with shell at the start of run seemed to cause strange behavior.
		 */
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		pool.execute(shell);
	}

	@Override
	@Command
	public String nodes() throws IOException {
		return nodeMap.listNodes();
	}

	@Override
	@Command
	public String users() throws IOException {
		return userMap.listUsers();
	}

	/**
	 * Shuts down everything in reverse order that it was declared
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	@Command
	public synchronized String exit() throws IOException {
		String output = "Shutting down " + componentName + " now, please wait...\n";

		/*
		 * Shut down the timer and the task
		 */
		aliveTimer.cancel();
		aliveTimerTask.cancel();

		/*
		 * Shut down the sockets used for receiving connections
		 */
		serverSocket.close();
		datagramSocket.close();

		userRequestStream.close();
		userResponseStream.close();

		/*
		 * Disable new tasks from being submitted to either pool
		 */
		pool.shutdown();

		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					output += "FATAL: Client Pool did not terminate!\n";
			}
		} catch (InterruptedException e) {
			// (Re-)cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		return output;
	}

	/**
	 * @param args the first argument is the name of the {@link CloudController} component
	 */
	public static void main(String[] args) {
		CloudController cloudController = new CloudController(args[0], new Config("controller"), System.in, System.out);
		new Thread(cloudController).start();
	}

}
