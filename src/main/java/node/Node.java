package node;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import node.container.NodeAttr;
import node.listener.ControllerListener;
import node.timer.AliveTimerTask;
import cli.Command;
import cli.Shell;

/**
 * Represents a Node object within the network. All actual calculations are done in ControllerHandler and listens for these requests from
 * the CloudController in a ControllerListener thread.
 *
 */
public class Node implements INodeCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	/*
	 * Everything past this point in variables was not part of the original class
	 */
	private Shell shell;

	private String logDir;
	private Integer tcpPort;
	private String controllerHost;
	private Integer controllerUdp;
	private long nodeAlive;

	private String operators;
	private char[] operatorA;

	private ExecutorService pool;
	private NodeAttr node;
	private ServerSocket serverSocket;

	private Timer aliveTimer;
	private AliveTimerTask aliveTimerTask;

	/**
	 * @param componentName the name of the component - represented in the prompt
	 * @param config the configuration to use
	 * @param userRequestStream the input stream to read user input from
	 * @param userResponseStream the output stream to write the console output to
	 */
	public Node(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		logDir = this.config.getString("log.dir");
		tcpPort = this.config.getInt("tcp.port");
		controllerHost = this.config.getString("controller.host");
		controllerUdp = this.config.getInt("controller.udp.port");
		nodeAlive = this.config.getInt("node.alive");
		operators = this.config.getString("node.operators");

		try {
			serverSocket = new ServerSocket(tcpPort);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		node = new NodeAttr(Integer.parseInt(componentName.substring(componentName.length() - 1)), operators, logDir, tcpPort, controllerHost, controllerUdp, nodeAlive);

		operatorA = new char[operators.length()];
		for (int i = 0; i < operators.length(); i++) {
			operatorA[i] = operators.charAt(i);
		}

		pool = Executors.newCachedThreadPool();

		DatagramSocket datagramSocket = null;
		try {
			datagramSocket = new DatagramSocket(null);
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		}

		aliveTimer = new Timer();
		aliveTimerTask = new AliveTimerTask(datagramSocket, controllerHost, controllerUdp, tcpPort, operators);
	}

	@Override
	public void run() {
		ControllerListener cL = new ControllerListener(serverSocket, pool, node);
		pool.execute(cL);

		aliveTimer.scheduleAtFixedRate(aliveTimerTask, 0, nodeAlive);

		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		new Thread(shell).start();
	}

	@Override
	@Command
	public String exit() throws IOException {
		String output = "";

		aliveTimer.cancel();

		shell.close();
		serverSocket.close();
		userResponseStream.close();
		userRequestStream.close();

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

		return output + "Closing " + componentName + " down...";
	}

	@Override
	@Command
	public String history(int numberOfRequests) throws IOException {
		String output = "";

		if (node.getHistory().size() == 0)
			return "No operations have been done on this node.";
		else if (node.getHistory().size() < numberOfRequests) {
			output += "There have  only been " + node.getHistory().size() + " so far: \n";
			numberOfRequests = node.getHistory().size();
		}

		for (int i = node.getHistory().size() - 1; i >= (node.getHistory().size() - numberOfRequests); i--) {
			output += (i + 1) + ". " + node.getHistory().get(i) + "\n";
		}

		return output;
	}

	/**
	 * @param args the first argument is the name of the {@link Node} component, which also represents the name of the configuration
	 */
	public static void main(String[] args) {
		Node node = new Node(args[0], new Config(args[0]), System.in, System.out);
		new Thread(node).start();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String resources() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
