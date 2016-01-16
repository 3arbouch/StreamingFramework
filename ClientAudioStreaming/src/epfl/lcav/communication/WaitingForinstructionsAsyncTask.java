package epfl.lcav.communication;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;



import android.app.Activity;

import android.os.AsyncTask;
import android.widget.Toast;

public class WaitingForinstructionsAsyncTask extends
		AsyncTask<Void, Void, Integer> {

	private Activity callingActivity;
	private static final String WAITING_FOR_INSTRUCTIONS_KEY = "Instruction";
	private static final String WAITING_FOR_INSTRUCTIONS_VALUE = "Wait";

	public WaitingForinstructionsAsyncTask(Activity callingActivity) {

		this.callingActivity = callingActivity;

	}

	@Override
	protected Integer doInBackground(Void... params) {
		int codeState = SharedAttributes.CONNECTION_SUCCEFULL_CODE;

		try {
			PrintWriter printWriter = TCPConnectionHandler.getInstance()
					.getPrintWriter();
			JSONObject json = new JSONObject();
			json.put(WAITING_FOR_INSTRUCTIONS_KEY,
					WAITING_FOR_INSTRUCTIONS_VALUE);
			printWriter.write(json.toString() + "\n");
			printWriter.flush();
			Thread serverMessageReaderThread = new Thread(
					new ServerMessageReader(this.callingActivity));
			serverMessageReaderThread.start();
		} catch (IOException e) {
			codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			e.printStackTrace();
		} catch (JSONException e) {
			codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			e.printStackTrace();
		}
		return codeState;
	}

	@Override
	protected void onPostExecute(Integer result) {

		if (result == 0) {
			Toast.makeText(this.callingActivity,
					"Waiting For Instructions From the server",
					SharedAttributes.TOAST_DURATION).show();

		} else {
			Toast.makeText(this.callingActivity, "CONNECTION Failed",
					SharedAttributes.TOAST_DURATION).show();
			TCPConnectionHandler.isConnected = false;
			try {
				TCPConnectionHandler connectionHandler = TCPConnectionHandler
						.getInstance();
				if (connectionHandler != null) {
					connectionHandler.closeConnection();
				}
			} catch (IOException e) {
				Toast.makeText(this.callingActivity,
						"Error occured when closing the connection !!",
						SharedAttributes.TOAST_DURATION).show();

				e.printStackTrace();
			} catch (NullPointerException e) {

				e.printStackTrace();
			}
			this.callingActivity.finish();
		}

	}

}
