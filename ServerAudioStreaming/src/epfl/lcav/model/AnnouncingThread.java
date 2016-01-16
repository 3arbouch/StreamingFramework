package epfl.lcav.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.shared.ServerAttributes;

/**
 * This class is responsible of announcing the ip address of the server to all
 * the clients. The server will respond to a specific request providing its IP
 * address as a response
 * 
 * @author MohamedBenArbia
 * 
 */
public class AnnouncingThread extends Thread {

	/**
	 * Socket responsible of sending replies to queries of all clients.
	 */
	DatagramSocket socket;

	public AnnouncingThread() throws SocketException {

		this.socket = new DatagramSocket(SharedAttributes.BROADCAST_PORT);

		this.socket.setBroadcast(true);
	}

	public void run() {
		System.out.println("Running Announcing Thread: ");
		while (true) {
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				this.socket.receive(packet);
				if (packet != null) {
					String receivedMessage = new String(packet.getData(), 0,
							packet.getLength());
					System.out.println("Message Received: " + receivedMessage);

					JSONObject receivedJSON = new JSONObject(receivedMessage);

					if (receivedJSON.getString(
							SharedAttributes.KEY_SEARCH_MESSAGE).equals(
							SharedAttributes.VALUE_SEARCH_MESSAGE)) {
						// Form the response and send it
						String ipAddress = ServerAttributes.getInstance()
								.getRTPSessionAddress();
						JSONObject responseJSON = new JSONObject();
						responseJSON.put(SharedAttributes.KEY_FIND_MESSAGE,
								ipAddress);
						byte[] response = responseJSON.toString().getBytes();
						InetAddress address = packet.getAddress();
						int port = packet.getPort();
						packet = new DatagramPacket(response, response.length,
								address, port);
						socket.send(packet);
						System.out.println("Send response to client :"
								+ responseJSON.toString());
					}
				}

			} catch (IOException e) {
				return;
			} catch (JSONException e) {
				return;
			}
		}
	}

}
