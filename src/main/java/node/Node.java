package node;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

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
	
	private boolean add;
	private boolean sub;
	private boolean div;
	private boolean mult;

	private ArrayList<Operation> historyList;

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

		shell = new Shell(componentName, userRequestStream, userResponseStream);

		logDir = config.getString("log.dir");
		tcpPort = config.getInt("tcp.port");
		controllerHost = config.getString("controller.host");
		controllerUdp = config.getInt("controller.udp.port");
		nodeAlive = config.getInt("node.alive");
		operators = config.getString("node.operators");
		
		if(operators.indexOf('+')>0)
			add = true;
		if(operators.indexOf('-')>0)
			sub = true;
		if(operators.indexOf('/')>0)
			div = true;
		if(operators.indexOf('*')>0)
			mult = true;

		operatorA = new char[operators.length()];
		for (int i = 0; i < operators.length(); i++) {
			operatorA[i] = operators.charAt(i);
		}

		historyList = new ArrayList<Operation>();
	}

	@Override
	public void run() {
		new Thread(shell).start();
	}
	
	public String calculate(Integer termA, String operator, Integer termB) {
		Integer result = null;
		
		if(operator.equals("+") && supportsAdd()) {
			result = termA + termB;
		}
		
		if(operator.equals("-") && supportsSub()) {
			result = termA - termB;
		}
		
		if(operator.equals("/") && supportsDiv()) {
			// TODO Handle rounding as per Angabe
			result = termA / termB;
		}
		
		if(operator.equals("*") && supportsMult()) {
			result = termA * termB;
		}
		
		return result.toString();
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
		
		for(int i = historyList.size()-1; i >= (historyList.size() - numberOfRequests);i--){
			output += historyList.get(i).toString();
		}
		
		return output;
	}

	public boolean supportsAdd() {
		return add;
	}

	public boolean supportsSub() {
		return sub;
	}

	public boolean supportsDiv() {
		return div;
	}

	public boolean supportsMult() {
		return mult;
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
