package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import client.Client;
import client.ClientSocketListener;
import client.TextMessage;

public class Application implements ClientSocketListener {

	private static final String PROMPT = "EchoClient> ";
	private BufferedReader stdin;
	private Client client = null;
	private boolean stop = false;
	
	private String serverAddress;
	private int serverPort;
	
	public void run() {
		while(!stop) {
			stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.print(PROMPT);
			
			try {
				String cmdLine = stdin.readLine();
				this.handleCommand(cmdLine);
			} catch (IOException e) {
				stop = true;
				printError("CLI does not respond - Application terminated ");
			}
		}
	}
	
	private void handleCommand(String cmdLine) {
		String[] tokens = cmdLine.split("\\s+");

		if(tokens[0].equals("quit")) {	
			stop = true;
			disconnect();
			System.out.println(PROMPT + "Application exit!");
		
		} else if (tokens[0].equals("connect")){
			if(tokens.length == 3) {
				try{
					serverAddress = tokens[1];
					serverPort = Integer.parseInt(tokens[2]);
					connect(serverAddress, serverPort);
				} catch(NumberFormatException nfe) {
					printError("No valid address. Port must be a number!");
					System.out.println("Unable to parse argument <port>");
				} catch (UnknownHostException e) {
					printError("Unknown Host!");
					System.out.println("Unknown Host!");
				} catch (IOException e) {
					printError("Could not establish connection!");
					System.out.println("Could not establish connection!");
				}
			} else {
				printError("Invalid number of parameters!");
			}
			
		} else  if (tokens[0].equals("send")) {
			if(tokens.length >= 2) {
				if(client != null && client.isRunning()){
					StringBuilder msg = new StringBuilder();
					for(int i = 1; i < tokens.length; i++) {
						msg.append(tokens[i]);
						if (i != tokens.length -1 ) {
							msg.append(" ");
						}
					}	
					sendMessage(msg.toString());
				} else {
					printError("Not connected!");
				}
			} else {
				printError("No message passed!");
			}
			
		} else if(tokens[0].equals("disconnect")) {
			disconnect();
			
		} else if(tokens[0].equals("help")) {
			printHelp();
		} else {
			printError("Unknown command");
			printHelp();
		}
	}
	
	private void sendMessage(String msg){
		try {
			client.sendMessage(new TextMessage(msg));
		} catch (IOException e) {
			printError("Unable to send message!");
			disconnect();
		}
	}

	private void connect(String address, int port) 
			throws UnknownHostException, IOException {
		client = new Client(address, port);
		client.addListener(this);
		client.start();
	}
	
	private void disconnect() {
		if(client != null) {
			client.closeConnection();
			client = null;
		}
	}
	
	private void printHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(PROMPT).append("ECHO CLIENT HELP (Usage):\n");
		sb.append(PROMPT);
		sb.append("::::::::::::::::::::::::::::::::");
		sb.append("::::::::::::::::::::::::::::::::\n");
		sb.append(PROMPT).append("connect <host> <port>");
		sb.append("\t establishes a connection to a server\n");
		sb.append(PROMPT).append("send <text message>");
		sb.append("\t\t sends a text message to the server \n");
		sb.append(PROMPT).append("disconnect");
		sb.append("\t\t\t disconnects from the server \n");
		
		sb.append(PROMPT).append("quit ");
		sb.append("\t\t\t exits the program");
		System.out.println(sb.toString());
	}

	@Override
	public void handleNewMessage(TextMessage msg) {
		if(!stop) {
			System.out.println(msg.getMsg());
			System.out.print(PROMPT);
		}
	}

	@Override
	public void handleStatus(SocketStatus status) {
		if(status == SocketStatus.CONNECTED) {

		} else if (status == SocketStatus.DISCONNECTED) {
			System.out.print(PROMPT);
			System.out.println("Connection terminated: " 
					+ serverAddress + " / " + serverPort);
			
		} else if (status == SocketStatus.CONNECTION_LOST) {
			System.out.println("Connection lost: " 
					+ serverAddress + " / " + serverPort);
			System.out.print(PROMPT);
		}
		
	}

	private void printError(String error){
		System.out.println(PROMPT + "Error! " +  error);
	}
	
    /**
     * Main entry point for the echo server application. 
     * @param args contains the port number at args[0].
     */
    public static void main(String[] args) {
    	Application app = new Application();
		app.run();
    }

}
