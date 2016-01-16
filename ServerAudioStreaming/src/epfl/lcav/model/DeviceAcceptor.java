package epfl.lcav.model;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import epfl.lcav.controller.DeviceBuffer;
import epfl.lcav.shared.ServerAttributes;

/**
 * Implements the server that waits for new device to connect then
 * sends/receives control messages. Follows the singleton pattern: use
 * getInstance() to create the server.
 * 
 * @author wissem.allouchi@epfl.ch
 */
public class DeviceAcceptor extends Thread {
	
	/**
	 * counts the number of devices that was connected to the server
	 */
	static int deviceCount = 0 ; 
	/**
	 * stores the list of connected devices
	 */
	private DeviceBuffer devicesList;
	/**
	 * the only existing instance of the server
	 */
	private static DeviceAcceptor INSTANCE = null;
	/**
	 * port number
	 */
	private final int PORT = ServerAttributes.getInstance().getTCPConnectionPort();
	/**
	 *  server socket
	 */
	private ServerSocket ss;
	
	private DeviceAcceptor() {
		this.devicesList = DeviceBuffer.getInstance();
	}

	/**
	 * this method returns the unique instance of the server if it exists, if
	 * not it creates an instance and return it
	 * 
	 * @return the unique instance of the server
	 */
	public static DeviceAcceptor getInstance() {

		if (DeviceAcceptor.INSTANCE == null) {
			DeviceAcceptor.INSTANCE = new DeviceAcceptor();
		}
		return DeviceAcceptor.INSTANCE;

	}

	public DeviceBuffer getDevicesList() {
		return devicesList;
	}

	
	
	public void run() {
            
		try {
			this.ss = new ServerSocket(PORT);
			while (true) {
				Socket s = this.ss.accept();
				System.err.println("A new device tries to connect");
				Device d = new Device(s);
				
				deviceCount++ ; 
				this.devicesList.addDevice(d);	
				System.err.println("Device " + d +" connected");
				System.err.println("List of devices\n" + this.devicesList);
			}

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				ss.close();
				System.err.println("Server socket closed");
			} catch (IOException e) {
				System.err.println("Server closed"); 
				e.printStackTrace();
			}
		}

	}
	
	public static void killDeviceAcceptor() {
		INSTANCE=null;
	}
	
	public  void close() {
		try {
			if (this.ss!=null)
				this.ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
