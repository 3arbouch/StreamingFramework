package epfl.lcav.communication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import epfl.lcav.activities.MainActivity;
import epfl.lcav.attributes.SharedAttributes;

/**
 * This class is responsible of finding the IP Address of the server in the LAN.
 * The client send a specific message to the broadcast address and wait for a
 * reply from the server. Once the IP Address of the server identified, store it
 * in Shared Preferences. Created by MohamedBenArbia on 02/02/15.
 */
public class FindServerInLANAsyncTask extends AsyncTask<String, Void, Integer> {

	/**
	 * Socket to use to broadcast the search request
	 */
	DatagramSocket socket;

	// This context is used to show result of the connections
	private Context context;
	
	// TextView used to show important messages to the user 
	private TextView text ; 

	// progress dialog used to display the loading while finding the IP Address
	// of the
	// server for the first time

	private ProgressDialog progressDialog;

	public FindServerInLANAsyncTask(Context context, TextView text) {
		this.context = context;
		this.text = text ; 

	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(this.context);
		progressDialog.setTitle("Loading...Finding the Server");
		progressDialog.show();
	}

	@Override
	protected Integer doInBackground(String... params) {
		// This value indicates the result of the connection process
		int codeState = SharedAttributes.CONNECTION_REFUSED_CODE;

		// Get Broadcast IP Address and port Number
		String broadcastAddress = null;
		int portNumber = -1;

		broadcastAddress = params[0];
		portNumber = Integer.parseInt(params[1]);

		if (broadcastAddress != null && portNumber != -1) {
			try {
				// Send Search message to the broadcast Address

				socket = new DatagramSocket(7575);
				socket.setBroadcast(true);
				// If the socket do not receive a response within 5 seconds. The
				// user has to put the address of the server Manually
				socket.setSoTimeout(5000);
				// Construct the packet to send
				byte[] data = new byte[1024];
				JSONObject json = new JSONObject();
				json.put(SharedAttributes.KEY_SEARCH_MESSAGE,
						SharedAttributes.VALUE_SEARCH_MESSAGE);
				data = json.toString().getBytes();
				DatagramPacket packet, receivedPacket = null;

				// Send the packet to the broadcast address
				InetAddress ipAddress = InetAddress.getByName(broadcastAddress);
				packet = new DatagramPacket(data, data.length, ipAddress,
						portNumber);
				socket.send(packet);
				Log.d("SEVER ADDRESS", "Sending packet to ... "
						+ broadcastAddress);

				// Wait for Announce message from the server
				byte[] buf = new byte[1024];
				receivedPacket = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(receivedPacket);

					// Read The Announce Message from the server
					if (receivedPacket != null) {
						String receivedMessage = new String(
								receivedPacket.getData(), 0,
								receivedPacket.getLength());
						Log.d("RECEIVED MESSAGE", receivedMessage);
						if (receivedMessage != null) {
							JSONObject jsonResponse = new JSONObject(
									receivedMessage);
							String receivedFoundMessage = jsonResponse
									.getString(SharedAttributes.KEY_FIND_MESSAGE);

							// Check if the received message corresponds to the
							// expected one
							if (receivedFoundMessage != null) {
								// TODO check if it's a VALID IP ADRESS
								// Save the server IP ADDRESS in the SHARED
								// PREFERENCE
								SharedPreferences sharedPref = PreferenceManager
										.getDefaultSharedPreferences(this.context);
								SharedPreferences.Editor editor = sharedPref
										.edit();
								editor.putString(
										SharedAttributes.KEY_SERVER_ADDRESS,
										receivedFoundMessage);

								editor.commit();
								codeState = SharedAttributes.CONNECTION_SUCCEFULL_CODE;

							}

						}
					}

				} catch (InterruptedIOException e) {
					// Timeout
					e.printStackTrace();
					codeState = SharedAttributes.CONNECTION_TIMEOUT_CODE ; 
				}
			} catch (IOException e) {
				e.printStackTrace();
				codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			} catch (JSONException e) {
				e.printStackTrace();
				codeState = SharedAttributes.CONNECTION_REFUSED_CODE;

			}
		}
		return codeState;
	}

	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.dismiss();
		if (result == SharedAttributes.CONNECTION_SUCCEFULL_CODE) {
			Toast.makeText(context, "Server Identified",
					SharedAttributes.TOAST_DURATION).show();

			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(this.context);
			String serverAddress = sharedPref.getString(
					SharedAttributes.KEY_SERVER_ADDRESS, "");

			if (!serverAddress.equals("")) {
				// Lunch the Handshake procedure between the client and the
				// server
				new HandShakeAsyncTask(this.context, this.text).execute(serverAddress,
						String.valueOf(SharedAttributes.PORT));
			}

		} else if( result == SharedAttributes.CONNECTION_REFUSED_CODE) {
			Toast.makeText(
					context,
					"Failed to identify the server! Please click on reconnect to retry",
					SharedAttributes.TOAST_DURATION).show();
			socket.close();
			
			new FetchServerIPAddressAsyncTask(this.context, this.text).execute();

		}
		else if (result== SharedAttributes.CONNECTION_TIMEOUT_CODE) {
			Toast.makeText(context, "The Server is not in the same LAN... Fetching server IP address",
					SharedAttributes.TOAST_DURATION).show();
		
			new FetchServerIPAddressAsyncTask(this.context, this.text).execute();
			
		}

	}
}
