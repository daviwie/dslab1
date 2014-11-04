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

import controller.handler.ClientListener;
import controller.persistence.NodeConcurrentHashMap;
import controller.persistence.NodeData;
import controller.persistence.UserConcurrentHashMap;
import controller.persistence.UserData;
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
	private UserConcurrentHashMap userMap;

	// Hashmaps of nodes
	private NodeConcurrentHashMap nodeMap;

	// Server utilities
	ServerSocket serverSocket;
	DatagramSocket datagramSocket;

	// Thread pools
	private final ExecutorService pool;

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
		 * Initialize the user map
		 */
		userMap = new UserConcurrentHashMap();

		/*
		 * Initialize the node lists
		 */
		nodeMap = new NodeConcurrentHashMap();

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
				UserData user = new UserData();
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
	}

	@Override
	public void run() {
		/*
		 * Set the shell for the controller
		 */
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);

		while (!isStopped()) {
			// TODO Spawn thread for Shell
			pool.execute(shell);
			// TODO Spawn thread for UDP
			// TODO Spawn thread for TCP
			ClientListener cL = new ClientListener(serverSocket, userMap, nodeMap, pool);
			pool.execute(cL);

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
	public String nodes() throws IOException {
		// TODO
		return null;
	}

	@Override
	@Command
	public String users() throws IOException {
		return null;
	}

	@Override
	@Command
	public synchronized String exit() throws IOException {
		String output = "Shutting down " + componentName + " now, please wait...\n";

		userRequestStream.close();
		userResponseStream.close();

		// TODO Take care of shutting down the timer(s)

		isStopped = true;
		/*
		 * Shut down the sockets used for receiving connections
		 */
		serverSocket.close();
		datagramSocket.close();

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
					output += "Client Pool did not terminate!\n";
			}
		} catch (InterruptedException e) {
			// (Re-)cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		return output;
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
