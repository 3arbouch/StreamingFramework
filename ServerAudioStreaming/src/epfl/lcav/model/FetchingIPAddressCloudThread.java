package epfl.lcav.model;

import java.io.PrintWriter;
import java.util.Locale;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxStreamWriter;
import com.dropbox.core.DbxWriteMode;
import com.dropbox.core.NoThrowOutputStream;

import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.shared.ServerAttributes;

/**
 * This class is responsible of writing the IP address of the server in the
 * cloud. The clients will fetch the IP address of the server from the Cloud (A
 * shared File).
 * 
 * @author MohamedBenArbia
 * 
 */
public class FetchingIPAddressCloudThread extends Thread {

	public void run() {

		DbxRequestConfig config = new DbxRequestConfig(
				"LCAVAudioStreaminAPP/1.0", Locale.getDefault().toString());

		DbxClient client = new DbxClient(config, SharedAttributes.ACCESS_TOKEN);

		String ipAddress = ServerAttributes.getInstance()
				.getRTPSessionAddress();
 
		Writer writer = new Writer(ipAddress) ; 
		try {
			DbxEntry.File uploadedFile = client.uploadFile(
					"/AudioStreamingAppShared/ServerIPAddress.txt",
					DbxWriteMode.force(), ipAddress.getBytes().length,writer ) ;
		} catch (DbxException e) {
			
			e.printStackTrace();
		} catch (RuntimeException e) {
			
			e.printStackTrace();
		} 

	}

}

class Writer extends DbxStreamWriter<RuntimeException> {

	private String ipAddress;

	public Writer(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public void write(NoThrowOutputStream out) throws RuntimeException {
		PrintWriter pw = new PrintWriter(out);
		pw.write(ipAddress);
		pw.flush();

	}

}
