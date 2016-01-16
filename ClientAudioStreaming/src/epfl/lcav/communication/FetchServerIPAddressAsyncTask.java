package epfl.lcav.communication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import epfl.lcav.activities.MainActivity;
import epfl.lcav.attributes.SharedAttributes;

/**
 * This class is responsible of fetching the server IP address from the Shared
 * Folder in dropbox
 * 
 * @author MohamedBenArbia
 * 
 */
public class FetchServerIPAddressAsyncTask extends
		AsyncTask<String, Void, Integer> {

	private static final String APP_KEY = "fgykqz9zllz3k7v";
	private static final String APP_SECRET = "k0r3kahn98tlddi";

	
	// This context is used to show result of the connections
	private Context context;

	// TextView used to show important messages to the user
	private TextView text;

	// progress dialog used to display the loading while fetching the IP address
	// of the Server

	private ProgressDialog progressDialog;

	public FetchServerIPAddressAsyncTask(Context context, TextView text) {
		this.context = context;
		this.text = text;

	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(this.context);
		progressDialog
				.setTitle("Loading...Fetching the IP address of the server!");
		progressDialog.show();
	}

	@Override
	protected Integer doInBackground(String... params) {
		// This value indicates the result of the connection process
		int codeState = SharedAttributes.CONNECTION_SUCCEFULL_CODE;

		try {
			String serverIPAddress = readSahredFileFromDropbox();

			if (!serverIPAddress.equals("")) {
				// Save the server IP address in sharedPref
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(this.context);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString(SharedAttributes.KEY_SERVER_ADDRESS,
						serverIPAddress);
				editor.commit();
			} else {
				codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			}

		} catch (IOException e) {
			codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			e.printStackTrace();
		} catch (DropboxException e) {
			codeState = SharedAttributes.CONNECTION_REFUSED_CODE;
			e.printStackTrace();
		}

		return codeState;
	}
/*
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

		AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
		loadAuth(session);
		return session;
	}

	private void loadAuth(AndroidAuthSession session) {
		SharedPreferences prefs = this.context.getSharedPreferences(
				ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key == null || secret == null || key.length() == 0
				|| secret.length() == 0)
			return;

		if (key.equals("oauth2:")) {
			// If the key is set to "oauth2:", then we can assume the token is
			// for OAuth 2.
			session.setOAuth2AccessToken(secret);
		} else {
			// Still support using old OAuth 1 tokens.
			session.setAccessTokenPair(new AccessTokenPair(key, secret));
		}
	}*/

	/**
	 * Download the shared File from Dropbox and return the ipaddress written by
	 * the server
	 * 
	 * @throws DbxException
	 * @throws IOException
	 * @throws DropboxException
	 */
	private String readSahredFileFromDropbox() throws IOException,
			DropboxException {
		String ipAddress = "";

		DropboxAPI<AndroidAuthSession> mApi;

	    AndroidAuthSession session = new AndroidAuthSession(new AppKeyPair(APP_KEY, APP_SECRET), SharedAttributes.ACCESS_TOKEN) ; 
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this.context);

		String accessToken = sharedPref.getString(
				SharedAttributes.DROPBOX_ACCESS_TOKEN, "");

		InputStream downloader = mApi.getFileStream(
				"/AudioStreamingAppShared/ServerIPAddress.txt", null);
		try {
			DataInputStream reader = new DataInputStream(downloader);
			ipAddress = reader.readLine();
		} finally {
			downloader.close();
		}

		return ipAddress;
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
				new HandShakeAsyncTask(this.context, this.text).execute(
						serverAddress, String.valueOf(SharedAttributes.PORT));
			}

		} else if (result == SharedAttributes.CONNECTION_REFUSED_CODE) {
			this.text
					.setText("Cannot retreive server IP Address... The server is probably offline OR an error occured! \n Please set the IP address of the server manually in settings and then click on reconnect!");
			this.text
					.setError("Cannot retreive server IP Address... The server is probably offline OR an error occured! \n Please set the IP address of the server manually in settings and then click on reconnect!");
		}

	}
}
