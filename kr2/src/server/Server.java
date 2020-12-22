package server;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Represents a simple Echo Server implementation.
 */
public class Server extends Thread {

	private int port;
    private ServerSocket serverSocket;
    private boolean running;
    
    /**
     * Constructs a (Echo-) Server object which listens to connection attempts 
     * at the given port.
     * 
     * @param port a port number which the Server is listening to in order to 
     * 		establish a socket connection to a client. The port number should 
     * 		reside in the range of dynamic ports, i.e 49152 - 65535.
     */
    public Server(int port){
        this.port = port;
    }

    /**
     * Initializes and starts the server. 
     * Loops until the the server should be closed.
     */
    @Override
    public void run() {
        
    	running = initializeServer();
        
        if(serverSocket != null) {
	        while(isRunning()){
	            try {
	                Socket client = serverSocket.accept();                
	                ClientConnection connection = 
	                		new ClientConnection(client);
	                new Thread(connection).start();

					System.out.println("Connected to "
	                		+ client.getInetAddress().getHostName() 
	                		+  " on port " + client.getPort());
	            } catch (IOException e) {
					System.out.println("Error! Unable to establish connection. \n");
	            }
	        }
        }
		System.out.println("Server stopped.");
    }
    
    private boolean isRunning() {
        return this.running;
    }

    /**
     * Stops the server insofar that it won't listen at the given port any more.
     */
    public void stopServer(){
        running = false;
        try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Error! Unable to close socket on port: " + port);
		}
    }

    private boolean initializeServer() {
		System.out.println("Initialize server ...");
    	try {
            serverSocket = new ServerSocket(port);
			System.out.println("Server listening on port: "
            		+ serverSocket.getLocalPort());    
            return true;
        
        } catch (IOException e) {
			System.out.println("Error! Cannot open server socket:");
            if(e instanceof BindException){
				System.out.println("Port " + port + " is already bound!");
            }
            return false;
        }
    }
    
    /**
     * Main entry point for the echo server application. 
     * @param args contains the port number at args[0].
     */
    public static void main(String[] args) {
    	try {
			if(args.length != 1) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port>!");
			} else {
				int port = Integer.parseInt(args[0]);
				new Server(port).start();
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Invalid argument <port>! Not a number!");
			System.out.println("Usage: Server <port>!");
			System.exit(1);
		}
    }
}
