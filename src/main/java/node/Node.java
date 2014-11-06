package node;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import node.container.NodeAttr;
import node.listener.ControllerListener;
import cli.Command;
import cli.Shell;

public class Node implements INodeCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;

	/*
	 * Everything past this point in variables was not part of the original
	 * class
	 */
	private Shell shell;

	private String logDir;
	private Integer tcpPort;
	private String controllerHost;
	private Integer controllerUdp;
	private Integer nodeAlive;

	private String operators;
	private char[] operatorA;

	private ArrayList<Operation> historyList;
	private ExecutorService pool;
	private NodeAttr node;
	private ServerSocket serverSocket;

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
	public Node(String componentName, Config config, InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		logDir = config.getString("log.dir");
		tcpPort = config.getInt("tcp.port");
		controllerHost = config.getString("controller.host");
		controllerUdp = config.getInt("controller.udp.port");
		nodeAlive = config.getInt("node.alive");
		operators = config.getString("node.operators");

		node = new NodeAttr(Integer.parseInt(componentName.substring(componentName.length() - 1)), operators, logDir, tcpPort, controllerHost,
				controllerUdp, nodeAlive);

		operatorA = new char[operators.length()];
		for (int i = 0; i < operators.length(); i++) {
			operatorA[i] = operators.charAt(i);
		}

		historyList = new ArrayList<Operation>();

		pool = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		ControllerListener cL = new ControllerListener(serverSocket, pool, node);
		pool.execute(cL);
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		new Thread(shell).start();
	}

	@Override
	@Command
	public String exit() throws IOException {
		shell.close();
		userResponseStream.close();
		userRequestStream.close();
		return "Closing " + componentName + " down...";
	}

	@Override
	@Command
	public String history(int numberOfRequests) throws IOException {
		String output = "";

		for (int i = historyList.size() - 1; i >= (historyList.size() - numberOfRequests); i--) {
			output += historyList.get(i).toString();
		}

		return output;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Node} component,
	 *            which also represents the name of the configuration
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

	public String getComponentName() {
		return componentName;
	}

}
