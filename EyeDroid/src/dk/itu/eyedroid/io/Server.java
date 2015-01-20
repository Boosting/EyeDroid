package dk.itu.eyedroid.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
/**
 * Network server abstraction.
 */
public abstract class Server {
	
	protected int mServerPort;					// Server port
	protected int mClientPort = 0;				// Client port (For connectionless clients)
	protected InetAddress mClientIPAddress;		// Client IP Address (For connectionless clients)
	
	/**
	 * Default constructor
	 * @param port Server port
	 */
	public Server(int port){
		mServerPort = port;
	}
	
	/**
	 * Start server
	 * @throws SocketException
	 */
	public abstract void start() throws SocketException;
	
	/**
	 * Read from socket
	 * @param block Block until a message is received
	 * @throws IOException 
	 */
	public abstract int[] read(boolean block) throws IOException;
	
	/**
	 * Send message
	 *  @param Message to client
	 *  @param x X-coordinate
	 *  @param y Y-coordinate
	 */
	public abstract void send(int message, int x, int y) throws IOException;
	
	/**
	 * Stop server
	 */
	public abstract void stop();
}
