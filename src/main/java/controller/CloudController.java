package controller;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import node.Node;
import cli.Command;
import cli.Shell;

public class CloudController implements ICloudControllerCli, Runnable {

	private String componentName;
	private Config controllerConfig;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	/*
	 * Everything past this point in variables was not part of the original
	 * class
	 */
	private Shell shell;
	// Read out config values
	private final Integer tcpPort;
	private final Integer udpPort;
	private final Integer nodeTimeout;
	private final Integer nodeCheckPeriod;
	private final Config userConfig;

	// Hashmap of all users stored in the config file along with their passwords
	private ConcurrentHashMap<String, User> userMap;

	// Hashmaps of nodes
	private ConcurrentHashMap<String, Node> liveNodes;
	private ConcurrentHashMap<String, Node> deadNodes;
	private ConcurrentHashMap<String, Integer> usageStats;

	// Server utilities
	ServerSocket serverSocket;
	DatagramSocket datagramSocket;

	// Thread pools
	private final ExecutorService listenerPool;

	private boolean isStopped = false;

	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public CloudController(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.controllerConfig = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		/*
		 * Set the shell for the controller
		 */
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);

		/*
		 * Initialize the user map
		 */
		userMap = new ConcurrentHashMap<String, User>();

		/*
		 * Initialize the node lists
		 */
		liveNodes = new ConcurrentHashMap<String, Node>();
		deadNodes = new ConcurrentHashMap<String, Node>();

		/*
		 * Read out all of the config information for the controller and
		 * initialize the variables
		 */
		tcpPort = controllerConfig.getInt("tcp.port");
		udpPort = controllerConfig.getInt("udp.port");
		nodeTimeout = controllerConfig.getInt("node.timeout");
		nodeCheckPeriod = controllerConfig.getInt("node.checkPeriod");

		/*
		 * Read user list from user.properties, build userMap and assign user
		 * attributes (password, credits)
		 */
		userConfig = new Config("user");
		Set<String> userKeys = userConfig.listKeys();

		for (String key : userKeys) {
			String[] parts = key.split("\\.");
			int test = parts.length;
			String userName = parts[0];
			String attr = parts[1];
			if (!userMap.containsKey(userName)) {
				User user = new User();
				user.setName(userName);
				switch (attr) {
				case "credits":
					user.setCredits(userConfig.getInt(userName + "." + attr));
				case "password":
					user.setPassword(userConfig.getString(userName + "." + attr));
				default:
					break;
				}

				userMap.put(userName, user);
			} else {
				switch (attr) {
				case "credits":
					userMap.get(userName).setCredits(userConfig.getInt(userName + "." + attr));
				case "password":
					userMap.get(userName).setPassword(userConfig.getString(userName + "." + attr));
				default:
					break;
				}
			}
		}

		/*
		 * Instantiate thread pools
		 */
		listenerPool = Executors.newFixedThreadPool(4);

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
	}

	@Override
	public void run() {
		while (!isStopped()) {
			// TODO Spawn thread for Shell
			listenerPool.execute(shell);
			// TODO Spawn thread for UDP
			// TODO Spawn thread for TCP
			ClientListener cL = new ClientListener(serverSocket);
			listenerPool.execute(cL);

			if (isStopped()) {
				try {
					exit();
				} catch (IOException e) {

				}
			}
		}
	}

	@Override
	@Command
	public synchronized String nodes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public synchronized String users() throws IOException {
		// http://stackoverflow.com/questions/1066589/java-iterate-through-hashmap
		Iterator<Entry<String, User>> it = userMap.entrySet().iterator();
		String output = "";

		while (it.hasNext()) {
			int counter = 1;
			ConcurrentHashMap.Entry<String, User> pairs = (ConcurrentHashMap.Entry<String, User>) it.next();
			output += counter + ". " + pairs.getKey() + " " + pairs.getValue().isOnline() + " Credits: " + pairs.getValue().getCredits() + "\n";

			counter++;

			it.remove();
		}

		return output;
	}

	@Override
	@Command
	public synchronized String exit() throws IOException {
		String output = "Shutting down " + componentName + " now, please wait...\n";
		
		userRequestStream.close();
		userResponseStream.close();

		// TODO Take care of shutting down the timer(s)
		// TODO Shut down listeners

		isStopped = true;
		/*
		 * Shut down the sockets used for receiving connections
		 */
		serverSocket.close();
		datagramSocket.close();

		/*
		 * Disable new tasks from being submitted to either pool
		 */
		listenerPool.shutdown();

		try {
			// Wait a while for existing tasks to terminate
			if (!listenerPool.awaitTermination(60, TimeUnit.SECONDS)) {
				listenerPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!listenerPool.awaitTermination(60, TimeUnit.SECONDS))
					output += "Client Pool did not terminate!\n";
			}
		} catch (InterruptedException e) {
			// (Re-)cancel if current thread also interrupted
			listenerPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		return output;
	}

	/**
	 * Calculates the usage stats for a particular node
	 * 
	 * @param node
	 */
	private void calcUsageStats(Node node, int digits) {
		usageStats.put(node.getComponentName(), usageStats.get(node) + (50 * digits));
	}

	private boolean isStopped() {
		return isStopped;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link CloudController}
	 *            component
	 */
	public static void main(String[] args) {
		CloudController cloudController = new CloudController(args[0], new Config("controller"), System.in, System.out);
		new Thread(cloudController).start();
	}

}
