package acp.background;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.ACPLogic;

/**
 * Listen for connections who wish to communicate with ACP Logic. Once the connection 
 * is accepted, a ClientWorker thread will be spawned for further processing.
 * 
 * @author Loke Yan Hao
 */
public class Server implements Runnable{
	
	final static Logger logger = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ServerSocket server;
	public static final int BACKLOG = 20;
	
	/**
	 * Basic constructor to create an instance of Server object.
	 * 
	 * @param port	the port it is listening to.
	 */
	public Server(int port){
		this.port = port; 
	}
	
	/**
	 * Terminate the server.
	 */
	public void terminate(){
		// TODO: Need to find a way to stop and clean up the server.
	}
	
	/**
	 * Listen to client connections (eg. plugins) on the port. Once a connection is accepted,
	 * a instance of ClientWorker object will be created. Further processing will be done on 
	 * the ClientWorker object.
	 */
	private void listenConnection(){
		// Listen to port
		try{
			server = new ServerSocket(port, BACKLOG, InetAddress.getLoopbackAddress());
		} catch (IOException e) {
			// TODO: Think of ways to popup error box
			e.printStackTrace();
		}
		
		// Waiting for clients		
		while(true){
			ClientWorker w;
			try{
				Socket client = server.accept();
				//client.setSoTimeout(10000);
				w = new ClientWorker(client);
				
				Thread t = new Thread(w);
				t.start();
			} catch(IOException e){
				logger.error("IOException occur.");
			}
		}
	}

	/**
	 * Start listening for network connections.
	 */
	public void run() {
		listenConnection();
	}
}
