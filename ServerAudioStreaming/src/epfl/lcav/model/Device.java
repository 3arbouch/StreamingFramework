package epfl.lcav.model;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.controller.DevSrvMessages;
import epfl.lcav.controller.DeviceBuffer;
import epfl.lcav.exceptions.InitProtocolException;
import epfl.lcav.model.AVReceive2.ParticipantResourcesManager;
import epfl.lcav.shared.ServerAttributes;

import epfl.lcav.streamStoredAudioFile.ReaderFileAndSenderThread;

import epfl.lcav.view.DeviceFrame;
import epfl.lcav.view.MainView;

/**
 * Class that handles a device (which represent the client) and stores the
 * necessary informations about the device. Each device has a thread that is
 * responsible for the exchange of control messages.
 * 
 * @author wissem.allouchi@epfl.ch
 * 
 */

public class Device implements Runnable {

	private boolean haveUI = false;

	/**
	 * The instructionSocket that links the thread to the device
	 */
	private Socket instructionSocket;
	/**
	 * Input stream of the device
	 */
	private BufferedReader in;
	/**
	 * output Stream of the device
	 */
	private PrintWriter out;
	/**
	 * the thread that reads/answers the commands initiated by the device
	 */
	private Thread t;

	/**
	 * Identifier of the device
	 */
	private String identifier;

	/**
	 * Socket responsible of sending the selected stored audio file 
	 */
	private Socket storedAudioSenderSocket ; 
	
	/**
	 * 0 means connected 1 means disconnected
	 */
	private int status = 0;
	

	/**
	 * constructs a device object then starts the thread responsible for the
	 * communication between the device and the server
	 * 
	 * @param s
	 *            the instructionSocket that is used for the communication
	 */
	public Device(Socket s) {
		this.instructionSocket = s;
		try {
			this.in = new BufferedReader(new InputStreamReader(
					s.getInputStream()));

			this.out = new PrintWriter(s.getOutputStream());
			this.identifier = "D00"+DeviceAcceptor.deviceCount  ; 
		} catch (IOException e) {
			e.printStackTrace();
		}
		//added a comment in device 
		// start the worker thread that will handle the communication between the device and the server
		this.t = new Thread(this);
		this.t.start();
	}
	
	/**
	 *  Build a socket responsible of sending stored audio to the client .
	 */

	public void buildStoredAudioSenderSocket() {
		try {
			 this.storedAudioSenderSocket = new Socket(
					instructionSocket.getInetAddress(),
					ServerAttributes.TCPAudioFileTransmitPort);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void luanchReaderFileAndSenderThread(String filePath) {
		try {
			ReaderFileAndSenderThread readerFileAndSender = new ReaderFileAndSenderThread(storedAudioSenderSocket, filePath) ;
			Thread t = new Thread(readerFileAndSender)  ; 
			t.start() ; 
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	/**
	 * the thread waits until a new command from the user arrives, then the
	 * server answers with the appropriate command
	 */
	public void run() {
		try {

			if (this.deviceConnect()) {
				MainView.list.addElement(this);
				// the server waits for the commands/requests of the client(i.e
				// device) that have to be executed
				// then answers with the appropriate command/instruction
				String inputline;
				while ((inputline = this.in.readLine()) != null) {
					System.err.println(this + "the client said: " + inputline);
					 DevSrvMessages msg = this.deviceReceiveMsg(inputline);
					 this.deviceHandleReceivedMsg(msg);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InitProtocolException e) {
			System.err.println("Connection refused to  client" + this
					+ " : wrong init msg");
			System.err.println("Aborting connection");
			e.printStackTrace();
		} finally {
			// if an I/O exception occurs (i.e the device is disconnected) we
			// close correctly the socket
			if (this.status==0) {
				System.out.println("CALL TO DEVICE DECONNECT FROM FINALLY IN DEVICE CLASS");
				this.deviceDeconnect();
				System.err.println("\tRemoving device " + this + " from the list\n\t\t"
						+ DeviceBuffer.getInstance());
				DeviceBuffer.getInstance().removeDevice(this);
				//remove the device from the GUI list
				MainView.list.removeElement(this);
				// Destroy the frame corresponding to this device 
				System.err.println("\tDestroy the frame corresponding to device "+this);
				DeviceFrame frame = MainView.deviceFrameHashMap.get(this.getIdentifier());
				if (frame!=null) 
					frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
				MainView.deviceFrameHashMap.remove(this.getIdentifier());
				System.err.println("\tsuccessful deconection");
			}

		}

	}

	// ************************************************************************************//
	// UTILITY METHODS //
	// ************************************************************************************//

	/**
	 * This method is used whenever an order shall be transmitted to the device
	 * 
	 * @param message
	 *            is a string containing a JSONObject
	 */
	public synchronized void writeOrder(String message) {
		// make sure the msg ends by a \n
		if (message.charAt(message.length() - 1) != '\n')
			message += '\n';
		this.out.write(message);
		this.out.flush();

	}

	/**
	 * Implement the connection protocol of a device
	 * 
	 * @return true if the connection is successfully established
	 */
	public boolean deviceConnect() throws IOException, JSONException,
			InitProtocolException {
		boolean connectionEstablished = false;

		String inputline = in.readLine();
		System.err.println("The client " + this + " said: " + inputline);
		JSONObject json = new JSONObject(inputline);
		if (json.get("Register").equals("Hello")) {
			// the connection should be accepted
			// prepare the Ack response
			// the port numbers for the received streams
			System.err.println("the client " + this + "Accepted");
			json = new JSONObject();
			json.put("Ack", "Accepted");
			json.put("PortNumberAudio", String.valueOf(ServerAttributes.getInstance().getRTPAudioReceivePort()));
			json.put("PortNumberSensors", String.valueOf(7072));
			// send the ack msg
			String outputline = json.toString() + "\n";
			this.writeOrder(outputline);
			System.err.println(this + " Ack msg sent");
			connectionEstablished = true;
		} else
			throw new InitProtocolException();

		return connectionEstablished;
	}

	/**
	 * Implement the disconnection protocol of a device
	 */
	public void deviceDeconnect() {
		try {
			if (status==0) {
				this.status=1;// update the status of the device.
				System.err.println("Deconnecting "+this);
				signalRelatedDevicesToStop() ; 
				
				System.err.println("\t"+this + " Closing the output stream of the socket");
				this.out.close();
				System.err.println("\t"+this + " Closing the socket");
				this.instructionSocket.close();
				System.err.println("\t"+this + " --> Socket closed");


				// close the medias that were responsible for handling the streams of this device 
				// i.e closing the data sink, the send stream and the player
				System.err
						.println("\tClosing processes responsible for handling the stream comming from "
								+ this);
				for (int i = 0; i < AVReceive2.mParticipantResourcesManagerVector
						.size(); i++) {
					ParticipantResourcesManager amLinker = (ParticipantResourcesManager) AVReceive2.mParticipantResourcesManagerVector
							.elementAt(i);
					if (amLinker != null
							&& this.identifier.equals(amLinker.participant
									.getCNAME())) {

						if (amLinker.datasink != null) {
							System.err.println("\tClosing datasink.");
							amLinker.datasink.close();

						}  
						if (amLinker.player!=null) {
							System.err.println("\tClosing player.");
							amLinker.player.close();
						}
						if (amLinker.sendStream!=null) {

							System.err.println("\tClosing sendStream.");
							amLinker.sendStream.close();
						}
						System.err
								.println("\tRemoving the medialinker corresponding to this stream");
						AVReceive2.mParticipantResourcesManagerVector
								.remove(amLinker); // remove this participant from
													// the list

						break;
					}
				}

			} else {
				System.out.println(this +" is already deconnected");
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inform the related devices: devices who are receiving the streams to stop
	 * recording
	 * 
	 * @throws JSONException
	 */

	private void signalRelatedDevicesToStop() throws JSONException {
		DeviceFrame frame = MainView.deviceFrameHashMap.get(this
				.getIdentifier());

		Device relatedDevice;
		if (frame != null
				&& (relatedDevice = frame.getSelectedDevice()) != null) {
			System.out.println("Order device " + relatedDevice
					+ " to stop receiving audio.");
			if (relatedDevice.getOut() != null) {
				// Close the receiving session of the related device
				JSONObject json = new JSONObject();
				json.put("Instruction", "STOPRECEIVINGAUDIO");
				relatedDevice.getOut().write(json.toString() + "\n");
				relatedDevice.getOut().flush();

			}
		}
	}

	/**
	 * DONE
	 */
	public DevSrvMessages deviceReceiveMsg(String inputline) {
		DevSrvMessages msg = DevSrvMessages.UNKNOWNMESSAGE;
		try {
			JSONObject jsonReceived;
			jsonReceived = new JSONObject(inputline);
			String deviceMsg = (String) jsonReceived.get("Instruction");
			msg = DevSrvMessages.valueOf(deviceMsg);
		} catch (JSONException e) {

			System.err.println("Malformed message!\n\t message will be ignored");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("Unknown message!\n\t message will be ignored");
			e.printStackTrace();
		}

		return msg;
	}

	/**
	 * 
	 */
	public void deviceHandleReceivedMsg(DevSrvMessages msg) {
		switch (msg) {
		case UNKNOWNMESSAGE:
			// Do nothing, just ignore the msg
			// subject to improvement (make use of state attribute)
			break;

		default:
			break;
		}
	}

	// ************************************************************************************//
	// ACCESS METHODS //
	// ************************************************************************************//
	/**
	 * Provides the instructionSocket through which the communication between
	 * the server and the device
	 * 
	 * @return The TCP instructionSocket that link this device
	 */
	public Socket getSocket() {
		return instructionSocket;
	}

	public String toString() {
		String res = this.identifier;
		return res;
	}

	/**
	 * Returns the input stream of this device
	 * 
	 * @return The input stream of this device
	 */
	public BufferedReader getIn() {
		return in;
	}

	/**
	 * Returns the output stream of this device
	 * 
	 * @return The output stream of this device
	 */
	public PrintWriter getOut() {
		return out;
	}

	/**
	 * Says if a frame was already created for this device
	 * 
	 * @return True if this device has a frame
	 */
	public boolean isHaveUI() {
		return haveUI;
	}

	public void setHaveUI(boolean haveUI) {
		this.haveUI = haveUI;
	}

	/**
	 * Return a string that identifies this device
	 * 
	 * @return a String identifier of this device
	 */
	public String getIdentifier() {
		return identifier;
	}

}
