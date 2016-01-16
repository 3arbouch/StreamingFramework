package epfl.lcav.shared;


import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import epfl.lcav.exceptions.ExistingInstanceException;
import epfl.lcav.serveraudiostreaming.MainClass;

/**
 * This calss is used to put into it the shared attributes between all the
 * classes. It implements the singleton design pattern: the first instance of
 * this class must be instantiated by a call to the newInstance(int tcpConPort,
 * int tcpFilePort, ...) then getInstance() returns the uniquely created
 * instance of this class.
 * 
 * @author Wissem
 * 
 */
public class ServerAttributes {

	private ServerAttributes(int tcpConPort, int tcpFilePort,
			int rtpRecPort, int rtpTransPort, String rtpSessionAddr,
			String recDir) {
		this.TCPConnectionPort = tcpConPort;
		this.TCPFileTransmitPort = tcpFilePort;
		this.RTPAudioReceivePort = rtpRecPort;
		this.RTPAudioTransmitPort = rtpTransPort;
		this.RTPSessionAddress = rtpSessionAddr;
		this.RecordingDirectory = recDir;
	}

	/**
	 * The unique instance of this class.
	 */
	private static ServerAttributes instance = null;

	/**
	 * Used to create the unique instance of this class If an instance already
	 * exits then an error is thrown and one have to use getInstance() instead
	 * of this method.
	 * 
	 * @return the unique instance of this class
	 * @throws ExistingInstanceException
	 *             Thrown whenever one calls this method but an instance already
	 *             exits.
	 */
	public static ServerAttributes newInstance(int tcpConPort,
			int tcpFilePort, int rtpRecPort, int rtpTransPort,
			String rtpSessionAddr, String recDir)
			throws ExistingInstanceException {
		if (ServerAttributes.instance != null)
			throw new ExistingInstanceException();
		// if there no instance : create a new one and return it.
		instance = new ServerAttributes(tcpConPort, tcpFilePort,
				rtpRecPort, rtpTransPort, rtpSessionAddr, recDir);
		return instance;
	}

	/**
	 * Returns the unique instance of this class. The instance must be created
	 * using the newInstance() method If the instance does not exist then a
	 * NullPointerException is thrown
	 * 
	 * @return The unique instance of this class
	 * @throws NullPointerException
	 *             Thrown when it is called before a call to new instance has happened
	 */
	public static ServerAttributes getInstance()
			throws NullPointerException {
		if (ServerAttributes.instance != null)
			return ServerAttributes.instance;
		throw new NullPointerException(
				" method ServerSharedAttributes newIntance(int tcpConPort, int tcpFilePort, int rtpRecPort,"
						+ " int rtpTransPort, String rtpSessionAddr, String recDir) "
						+ "must be used to get the first instance of the ServerSharedAttributes class ");
	}

	
	private String erroMessage="" ;
	
	
	/**
	 * 
	 */
	private int TCPConnectionPort = 0;
	/**
	 * 
	 */
	private int RTPAudioReceivePort = 0;
	/**
	 * 
	 */
	private int RTPAudioTransmitPort = 0;
	/**
	 * 
	 */
	private int TCPFileTransmitPort = 0;
	/**
	 * 
	 */
	private String RTPSessionAddress = "";
	/**
	 * 
	 */
	private String RecordingDirectory = "";

	public int getTCPConnectionPort() {
		return TCPConnectionPort;
	}

	public String setTCPConnectionPort(int tCPConnectionPort) {
		String message;
		if ((message=portNumCheck(tCPConnectionPort))==null) 
			TCPConnectionPort = tCPConnectionPort;
		return message;	
	}

	public int getRTPAudioReceivePort() {
		return RTPAudioReceivePort;
	}

	public String setRTPAudioReceivePort(int rTPAudioReceivePort) {
		String message;
		if ((message=portNumCheck(rTPAudioReceivePort))==null) 
			RTPAudioReceivePort = rTPAudioReceivePort;
		return message;	
		
	}

	public int getRTPAudioTransmitPort() {
		return RTPAudioTransmitPort;
	}

	public String setRTPAudioTransmitPort(int rTPAudioTransmitPort) {
		String message;
		if ((message=portNumCheck(rTPAudioTransmitPort))==null) 
			RTPAudioTransmitPort = rTPAudioTransmitPort;
		return message;	
	}

	public int getTCPFileTransmitPort() {
		return TCPFileTransmitPort;
	}

	public String setTCPFileTransmitPort(int tCPFileTransmitPort) {
		String message;
		if ((message=portNumCheck(tCPFileTransmitPort))==null) 
			TCPFileTransmitPort = tCPFileTransmitPort;
		return message;	
	}

	public String getRTPSessionAddress() {
		return RTPSessionAddress;
	}

	public String setRTPSessionAddress(String rTPSessionAddress) {
		String message;
		if ((message=sessionIPAddrCheck(this.RTPSessionAddress))==null)
			RTPSessionAddress=rTPSessionAddress;
		return message;
	}

	public String getRecordingDirectory() {
		return RecordingDirectory;
	}

	/**
	 * 
	 * @param recordingDirectory
	 * @return null if successful update, the error message if not
	 */
	public String setRecordingDirectory(String recordingDirectory) {
		
		String message=null;
		File f = new File(recordingDirectory);
		
		if (f.exists() && f.isDirectory() && f.canWrite() && f.canRead() ) { //this directory is fine 
			System.out.println("Every thing will be stored at: "+recordingDirectory);
			RecordingDirectory = recordingDirectory;
			// update the file.properties file 
			FileInputStream input;
			try {

				String filename = "file.properties";
				input= new FileInputStream(new File(filename));
				Properties props = new Properties();
				props.load(input);
				input.close();
				
		
				FileOutputStream out = new FileOutputStream(new File(filename));
				props.setProperty("Recording_default_directory", RecordingDirectory);
				props.store(out, null);
				out.close();
				System.out.println("the storage directory is now "+ RecordingDirectory);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 

		} else {
			message=recordingDirectory + "no such directory or does not have required permission"+"\n";
		}

		
		return message;
	}
	
	public String getErroMessage() {
		return erroMessage;
	}

	/**
	 * Used to check if all the attributes are correctly fetched
	 * @return true if the attributes are correct and false if not.
	 */
	public boolean validateAttirbutes () {
		
		erroMessage="";
		String message1;
		boolean res=true;
		if ((message1=portNumCheck(this.TCPConnectionPort) )!= null) {//1
			erroMessage="TCP port " +message1+"\n";
			res=false;
		}
		if ((message1=portNumCheck(this.TCPFileTransmitPort) )!= null) {//2
			erroMessage=erroMessage+"TCP file transmission port "+message1+"\n";
			res=false;
		}
		
		if ((message1=portNumCheck(this.RTPAudioReceivePort)) != null) {//3
			erroMessage=erroMessage+"RTP recieve audio port "+message1+"\n";
			res=false;
		}
		
		if ( (message1=portNumCheck(this.RTPAudioTransmitPort) )!= null){//4
			erroMessage=erroMessage+"RTP transmit audio port "+message1+"\n";
			res=false;
		}
		
		if ((message1=sessionIPAddrCheck(this.RTPSessionAddress))!=null) {//5
			erroMessage=erroMessage+"RTP receive session address  "+message1+"\n";
			res=false;
		}
		
		File f = new File(this.RecordingDirectory);
		
		if (f.exists() && f.isDirectory() && f.canWrite() && f.canRead() ) {
			System.out.println("Every thing will be stored at: "+ this.RecordingDirectory);
		} else {
			erroMessage=erroMessage+"Recording directory "+"no such directory or does not have required permission"+"\n";
			res=false;
		}
		

		
		return res;
	}
	
	
	/**
	 * checks the user's input
	 * 
	 * @return the error message, or null if there is no errors
	 */

	private String portNumCheck(int p) {
		String message = null;
		if (p > 65535 || p < MIN_PORT_NUM)
			message = "Port number must be in the range "+MIN_PORT_NUM+" .. 65535";
		return message;
	}
	
	private String sessionIPAddrCheck(String ip) {
		String message = null;
		if (!validIP(ip)) 
			message="Wrong ip address "+ip;
		return message;
	}
	
	
	private boolean validIP(String ip) {
		if (ip == null || ip.isEmpty()) return false;
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) return false;

	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        return matcher.matches();
	    } catch (PatternSyntaxException ex) {
	        return false;
	    }
	}
	

	public static final int MIN_PORT_NUM=1030;
	public static final Color RED_COL = new Color(241, 76, 56);
	public static final Color GREEN_COL = new Color(165, 194, 92);

	public static String KEY_SERVER_ADDRESS = "pref_key_ip_adress_server";
	public static final String KEY_UDP_PORT_NUMBER_AUDIO = "udp_port_number_audio";
	public static final String KEY_UDP_PORT_NUMBER_SENSORS = "udp_port_number_sensors";
	public static final String KEY_SAMPLING_RATE_AUDIO = "sampling_rate_audio";
	public static final String KEY_BITS_PER_SAMPLE = "bits_per_sample";
	public static final String KEY_CHANNEL = "channel";

	public final static int WRONG_IP_ADRESS_CODE = 1;
	public final static int CONNECTION_REFUSED_CODE = 2;
	public final static int CONNECTION_SUCCEFULL_CODE = 0;
	public final static int TOAST_DURATION = 1000;

	public static int TCPAudioFileTransmitPort=9090;


	
	
	public static final String TAG_START_SESSION="Start a session";
	public static final String TAG_END_SESSION="End session";
	public static final String TAG_TCP_CON_PORT="TCP connection port";
	public static final String TAG_TCP_FILE_TRANS_PORT="TCP file transmit port";
	public static final String TAG_RTP_REC_PORT="RTP audio receive port";
	public static final String TAG_RTP_TRANS_PORT="RTP audio transmit port";





}
