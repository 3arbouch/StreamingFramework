package epfl.lcav.serveraudiostreaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import epfl.lcav.exceptions.ExistingInstanceException;
import epfl.lcav.shared.ServerAttributes;
import epfl.lcav.view.MainView;

public class MainClass {
	public static void main(String[] args) {

		// learn the configuration parameters from the file.properties file
		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "/Users/wissem/Desktop/arbouchSF/AudioStreamingApplication/ServerAudioStreaming/file.properties";

			input= new FileInputStream(new File(filename));
			// load a properties file from class path
						
			prop.load(input);
			int tcpConPort = Integer.valueOf(prop
					.getProperty("TCP_connection_port"));
			int tcpFilePort = Integer.valueOf(prop
					.getProperty("TCP_transmit_file_port"));
			int rtpRecPort = Integer.valueOf(prop
					.getProperty("RTP_audio_receiver_port"));
			int rtpTransPort = Integer.valueOf(prop
					.getProperty("RTP_audio_transmitter_port"));
			
			
			String rtpSessionAddr = findWifiIpAddress();//prop.getProperty("RTP_audio_session_ip_add");
			if (rtpSessionAddr == null) // this means that no IP address is available 
				return;
			
			String recDir = prop.getProperty("Recording_default_directory");

			ServerAttributes.newInstance(tcpConPort, tcpFilePort,
					rtpRecPort, rtpTransPort, rtpSessionAddr, recDir);
		

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ExistingInstanceException e) {
			System.err
					.println("Should not be here! Something is really wrong! closing the App");
			System.exit(-1);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// starting the GUI
		new MainView();
		
	}
	
	public static String  findWifiIpAddress () {
	    String ip=null;
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                // we check for one available IPv4 address 
	                if (addr.getClass().equals( Inet4Address.class)) {
	                	ip = addr.getHostAddress();
		                System.out.println(iface.getDisplayName() + " " + ip );
		                return ip; 
	                }
	                
	            }
	        }
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
	    return ip;
	}
}
