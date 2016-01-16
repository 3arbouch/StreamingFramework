package epfl.lcav.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class is responsible of providing the sockets linked to the server for
 * any connection needed between the client and the server. This class
 * implements the Singelton pattern in order to have always one single instance
 * that handles the connections between the client and the server
 * 
 * @author MohamedBenArbia
 * 
 */

public class TCPConnectionHandler {
	/**
	 * Check the connection between the client and the server. Change each time
	 * the connection of the connection change
	 */

	public static boolean isConnected = false;

	private static TCPConnectionHandler instance = null;

	private Socket TCPSocket = null;
	private PrintWriter printWriter = null;

	private BufferedReader bufferedReader = null;

	protected TCPConnectionHandler(InetAddress ipAddress, int portNumber)
			throws IOException {

		this.TCPSocket = new Socket(ipAddress, portNumber);
	}

	/**
	 * This method is used to provide a single instance of the connection
	 * HandlerClass which ensure the creation of single socket between the
	 * client and the server The socket will be used for all the connection
	 * between the client and the server
	 * 
	 * @param ipAddress
	 *            IP Adress of the server
	 * @param portNumber
	 *            port number of the service
	 * @return
	 * @throws IOException
	 */

	public static TCPConnectionHandler getIntance(InetAddress ipAddress,
			int portNumber) throws IOException {
		if (instance == null) {
			instance = new TCPConnectionHandler(ipAddress, portNumber);
		}
		return instance;
	}

	public static TCPConnectionHandler getInstance() {
		if (instance == null) {
			throw new NullPointerException(
					" method ConnectionHandler getIntance(InetAddress ipAddress,int portNumber) must be used to get an instance of the Connection handler class ");
		}
		return instance;
	}

	/**
	 * This method is used to provide the dataOutputStream linked to the socket
	 * between the client and the server. Get instance method should be called
	 * before calling this method !
	 * 
	 * @return DataOutputStream of the socket
	 * @throws IOException
	 */

	public PrintWriter getPrintWriter() throws IOException {
		if (this.printWriter == null && instance != null) {
			this.printWriter = new PrintWriter(this.TCPSocket.getOutputStream());
		}
		return this.printWriter;
	}

	/**
	 * This method is used to provide the BufferedReader linked to the socket
	 * between the client and the server in order to read instructions from the
	 * server. Get instance method should be called before calling this method !
	 * 
	 * @return DataOutputStream of the socket
	 * @throws IOException
	 */

	public BufferedReader getBufferedReader() throws IOException {
		if (bufferedReader == null) {
			bufferedReader = new BufferedReader(new InputStreamReader(
					this.TCPSocket.getInputStream()));
		}
		return bufferedReader;
	}

	/**
	 * Close the input and output stream of the socket
	 * 
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		this.TCPSocket.close();
		this.printWriter.close();
		this.bufferedReader.close();
		instance = null;
	}
	/**
	 * Check if the client is connected to the server
	 * 
	 * @return
	 */

}
