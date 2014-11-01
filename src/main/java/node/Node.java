package node;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
	
	private ConcurrentHashMap<String, String> historyMap;

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
	public Node(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		
		shell = new Shell(componentName, userRequestStream, userResponseStream);

		logDir = config.getString("log.dir");
		tcpPort = config.getInt("tcp.port");
		controllerHost = config.getString("controller.host");
		controllerUdp = config.getInt("controller.udp.port");
		nodeAlive = config.getInt("node.alive");
		operators = config.getString("node.operators");
		
		operatorA = new char[operators.length()];
		for(int i = 0; i < operators.length(); i++) {
			operatorA[i] = operators.charAt(i);
		}
		
		historyMap = new ConcurrentHashMap<String, String>();
	}

	@Override
	public void run() {
		new Thread(shell).start();
	}

	@Override
	@Command
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Command
	public String history(int numberOfRequests) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Node} component,
	 *            which also represents the name of the configuration
	 */
	public static void main(String[] args) {
		Node node = new Node(args[0], new Config(args[0]), System.in,
				System.out);
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
