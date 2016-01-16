package epfl.lcav.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONException;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import epfl.lcav.activities.WaitingForInstructionsActivity;
import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.clientaudiostreaming.R;
import epfl.lcav.instructions.ReceiveLiveAudioInstruction;
import epfl.lcav.messagesProcessing.MessageHandler;
import epfl.lcav.services.ReceiveLiveAudioStoreAndPlaySercive;
import epfl.lcav.services.ReceiveStoredAudioAndPlayService;
import epfl.lcav.services.RecordAudioAndStreamService;


/**
 * This class is responsible of listening actively to the inputStream of the
 * socket in order to read the server messages
 * 
 * @author MohamedBenArbia
 * 
 */
public class ServerMessageReader implements Runnable {

	private WaitingForInstructionsActivity activity;
	private String message;
	private Boolean finish;
	private static final String WAITING_FOR_INSTRUCTIONS_KEY = "Instruction";
	private static final String WAITING_FOR_INSTRUCTIONS_VALUE = "Wait";


	public ServerMessageReader(Activity activitiy) {
		this.activity = (WaitingForInstructionsActivity) activitiy;
	}

	/**
	 * Show messages on UI when errors occurs, or when instructions comes from
	 * the Server message reader thread Finish the activity if the errors occurs
	 * 
	 * @param message
	 */
	private void showMessagesOnUI(String message, boolean finish) {
		this.message = message;
		this.finish = finish;
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, ServerMessageReader.this.message,
						SharedAttributes.TOAST_DURATION).show();

				if (ServerMessageReader.this.finish) {
					activity.finish();

				}
			}
		});

	}

	@Override
	public void run() {
		// The list view to show the instruction received from the server on the
		// screen of the client
		ListView listView = (ListView) activity.findViewById(R.id.list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);

		try {
			BufferedReader reader = TCPConnectionHandler.getInstance()
					.getBufferedReader();
			
			String receivedMessage;
			// Read the messages from the server
			while ((receivedMessage = reader.readLine()) != null) {
		
				// Handle the instruction and execute the corresponding service
				this.activity.runOnUiThread(new UIThread(receivedMessage, adapter));
				MessageHandler.getInstance().handleMessages(
						receivedMessage);

			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			showMessagesOnUI("Receiving Server Message Failed", true);
		} catch (IOException e) {
			showMessagesOnUI("Receiving Server Message Failed", true);
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				TCPConnectionHandler.isConnected = false;
				TCPConnectionHandler.getInstance().closeConnection();
				RecordAudioAndStreamService.stopService(); 
				ReceiveLiveAudioStoreAndPlaySercive.stopService();  
				showMessagesOnUI("Connection closed !! Try to reconnect", true);

			} catch (IOException e) {
				showMessagesOnUI("ERROR Ocuured when closing the connection ",
						true);

				e.printStackTrace();
			}
		}

	}

}

class UIThread implements Runnable {
	private String message;
	private ArrayAdapter<String> adapter ; 
	public UIThread(String message, ArrayAdapter<String> adapter) {
		this.message = message ; 
		this.adapter = adapter ;
	}

	@Override
	public void run() {
	 this.adapter.add(this.message);

	}

}
