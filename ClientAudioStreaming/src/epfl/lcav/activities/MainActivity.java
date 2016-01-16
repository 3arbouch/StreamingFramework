package epfl.lcav.activities;

import java.net.InetAddress;
import java.net.UnknownHostException;

import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.clientaudiostreaming.R;

import epfl.lcav.communication.FindServerInLANAsyncTask;
import epfl.lcav.communication.TCPConnectionHandler;
import epfl.lcav.communication.HandShakeAsyncTask;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import android.widget.TextView;

/**
 * MainActivity represents the main GUI to be displayed. The GUI includes only
 * one button which is the setting button. This activity calls the
 * HandShakeAsyncTask class in order to perform the connection to the server
 * 
 * @author MohamedBenArbia
 * 
 */

public class MainActivity extends Activity {


	/**
	 * Text view to display information about the network interface of the
	 * device
	 */
	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.text = (TextView) findViewById(R.id.DeviceWifiIpAdress);
		saveDropboxParameters() ; 
		String broadcastAddress = "";
		try {
			broadcastAddress = getBroadcastAddress();
		} catch (UnknownHostException e) {
			this.text
					.setText("CANNOT obtain the broadcast Address! Please check the WIFI connection!");
		}


		if (!broadcastAddress.equals("")) {

			new FindServerInLANAsyncTask(this, this.text).execute(broadcastAddress,
					String.valueOf(SharedAttributes.BROADCAST_PORT));
		}


	}
	
	/**
	 * Method used to save the dropbox parameters if they are not already saved
	 */
	private void saveDropboxParameters() {
		// Save the Dropbox prameters in shared pref
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(this);
				
				
			
				String appKey = sharedPref.getString(
						SharedAttributes.DROPBOX_APP_KEY, "");
				String appSecret = sharedPref.getString(
						SharedAttributes.DROPBOX_APP_SECRET, "");
				String accessToken = sharedPref.getString(
						SharedAttributes.DROPBOX_ACCESS_TOKEN, "");
				
				if(appKey.equals("") && appSecret.equals("") && accessToken.equals("")){
					SharedPreferences.Editor editor = sharedPref
							.edit();
					editor.putString(
							SharedAttributes.DROPBOX_APP_KEY,
							"fgykqz9zllz3k7v");
					editor.putString(
							SharedAttributes.DROPBOX_APP_SECRET,
							"k0r3kahn98tlddi");
					editor.putString(
							SharedAttributes.DROPBOX_ACCESS_TOKEN,
							"X_ErvEEjYHoAAAAAAAAIkznE_wjczZCjXQK0_-tSMfTzG5ZDsJMayGNG3qeVJQEF");
					editor.commit() ; 
				
				}
				
			
				
		
	}


	/**
	 * Get the broadcast IP Address of the network
	 * 
	 * @return the broadcast IP address
	 * @throws UnknownHostException
	 */
	private String getBroadcastAddress() throws UnknownHostException {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

		// Extract theIP address of the device and the broadcast address
		String broadcastAddress = InetAddress.getByAddress(quads).toString();
		broadcastAddress = broadcastAddress.substring(1,
				broadcastAddress.length());
		String ipAddress = Formatter.formatIpAddress(dhcp.ipAddress);

		this.text.setText("IP address of the device for the WIFI interface: "
				+ ipAddress + " Broadcast Address: " + broadcastAddress);

		return broadcastAddress;

	}

	/**
	 * Method called when the Setting button is clicked. This event will display
	 * the setting Activity .
	 * 
	 * @param v
	 */
	public void settingButtonClicked(View v) {
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);

	}

	/**
	 * Method called when the reconnect button is clicked. This event will try
	 * reconnect the client to the server !
	 * 
	 * 
	 * @param v
	 */
	public void reconnectButtonClicked(View v) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String serverAddress = sharedPref.getString(
				SharedAttributes.KEY_SERVER_ADDRESS, "");
		if (!serverAddress.equals("") && !TCPConnectionHandler.isConnected) {
			Log.d("SERVER ADDRESS", "Connecting to the server address"
					+ serverAddress);
			new HandShakeAsyncTask(this, this.text).execute(serverAddress,
					String.valueOf(SharedAttributes.PORT));
		} else {
			Intent intent = new Intent(this,
					WaitingForInstructionsActivity.class);
			startActivity(intent);
		}

	}

}
