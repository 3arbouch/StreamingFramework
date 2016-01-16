package epfl.lcav.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.activities.WaitingForInstructionsActivity;
import epfl.lcav.attributes.SharedAttributes;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is responsible of doing the HandShake process with server: Sending
 * a "hello" message to the server and wait for an Ackm and some parameters for
 * further connections
 * 
 * @author MohamedBenArbia
 * 
 */
public class HandShakeAsyncTask extends AsyncTask<String, Void, Integer> {

	// These are the messages key and values exchanged between the client and
	// the server

	/*************************** HELLO MESSAGE: FROM CLIENT ******************************/
	private final static String HELLO_MESSAGE = "Hello";
	private final static String KEY_HELLO_MESSAGE = "Register";

	/*************************** ACK MESSAGE: FROM SERVER ******************************/
	private final static String ACK_MESSAGE_KEY = "Ack";
	private final static String ACK_MESSAGE_VALUE_ACCEPTED = "Accepted";

	/*************************** PARAMS MESSAGES: FROM SERVER ******************************/
	private final static String PORT_NUMBER_AUDIO_MESSAGE_KEY = "PortNumberAudio";
	private final static String PORT_NUMBER_SENSORS_MESSAGE_KEY = "PortNumberSensors";

	// This context is used to show result of the connections
	private Context context;
	
	private TextView text ; 

	// progress dialog used to display the loading while connecting to the
	// server for the first time

	private ProgressDialog progressDialog;

	public HandShakeAsyncTask(Context context, TextView text) {
		this.context = context;
		this.text = text ; 

	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(this.context);
		progressDialog.setTitle("Loading...Connecting to the Server");
		progressDialog.show();
	}

	@Override
	protected Integer doInBackground(String... params) {
		// This value indicates the result of the connection process
		int codeState = SharedAttributes.CONNECTION_REFUSED_CODE;

		// Get Server IP Address
		InetAddress serverIPAddress = null;
		int portNumber = -1;
		try {
			serverIPAddress = InetAddress.getByName(params[0]);
			portNumber = Integer.parseInt(params[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			codeState = SharedAttributes.WRONG_IP_ADRESS_CODE;
		}

		if (serverIPAddress != null && portNumber != -1) {
			try {
				// Send register message to the server
				PrintWriter out = TCPConnectionHandler.getIntance(
						serverIPAddress, portNumber).getPrintWriter();
				JSONObject json = new JSONObject();
				json.put(KEY_HELLO_MESSAGE, HELLO_MESSAGE);
				out.write(json.toString() + "\n");
				out.flush();

				// Wait for ack message from the server
				BufferedReader reader = TCPConnectionHandler.getIntance(
						serverIPAddress, portNumber).getBufferedReader();
				String receivedAckMessage;
				String receivedParamMessageAudio;
				String receivedParamMessageSensors;
				String receivedMessage;
				// codeState = CONNECTION_SUCCEFULL_CODE;

				// Read The ACK message and Port number Message from the server
				if ((receivedMessage = reader.readLine()) != null) {
					Log.d("RECEIVED MESSAGE", receivedMessage);
					if (receivedMessage != null) {
						JSONObject jsonResponse = new JSONObject(
								receivedMessage);
						receivedAckMessage = jsonResponse
								.getString(ACK_MESSAGE_KEY);
						receivedParamMessageAudio = jsonResponse
								.getString(PORT_NUMBER_AUDIO_MESSAGE_KEY);
						receivedParamMessageSensors = jsonResponse
								.getString(PORT_NUMBER_SENSORS_MESSAGE_KEY);
						Log.d("RECEIVED MESSAGE", receivedAckMessage + " "
								+ receivedParamMessageAudio);
						// Check if the server has accepted our connection and
						// if the port number sent is a valid one
						if (receivedAckMessage != null
								&& receivedParamMessageAudio != null
								&& receivedAckMessage
										.equals(ACK_MESSAGE_VALUE_ACCEPTED)
								&& Integer.parseInt(receivedParamMessageAudio) > 1030
								&& Integer
										.parseInt(receivedParamMessageSensors) > 1030) {
							// Save the portNumbers for UDP connections in
							// shared
							// preferences
							SharedPreferences sharedPref = PreferenceManager
									.getDefaultSharedPreferences(this.context);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putInt(
									SharedAttributes.KEY_UDP_PORT_NUMBER_AUDIO,
									Integer.parseInt(receivedParamMessageAudio));
							editor.putInt(
									SharedAttributes.KEY_UDP_PORT_NUMBER_SENSORS,
									Integer.parseInt(receivedParamMessageSensors));

							editor.commit();
							codeState = SharedAttributes.CONNECTION_SUCCEFULL_CODE;
							Log.d("Connection",
									"Connection has been established successfully");
						}

					}
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
			Toast.makeText(context, "SUCCEFULL CONNECTION",
					SharedAttributes.TOAST_DURATION).show();
			TCPConnectionHandler.isConnected = true;
			Intent intent = new Intent(this.context,
					WaitingForInstructionsActivity.class);
			this.context.startActivity(intent);
		} else {
			Toast.makeText(context, "CONNECTION REFUSED",
					SharedAttributes.TOAST_DURATION).show();
			
			this.text.setText("The server is offline!! Please check if the server is running") ; 
			this.text.setError("The server is offline!! Please check if the server is running ") ;
			TCPConnectionHandler.isConnected = false;
			try {
				TCPConnectionHandler connectionHandler = TCPConnectionHandler
						.getInstance();
				if (connectionHandler != null) {
					connectionHandler.closeConnection();
				}
			} catch (IOException e) {
				Toast.makeText(context,
						"Error occured when closing the connection !!",
						SharedAttributes.TOAST_DURATION).show();

				e.printStackTrace();
			} catch (NullPointerException e) {

				e.printStackTrace();
			}
		}

	}

}
