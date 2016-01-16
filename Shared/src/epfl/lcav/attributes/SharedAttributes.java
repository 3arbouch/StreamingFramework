package epfl.lcav.attributes;

import java.util.HashMap;

/**
 * This calss is used to put into it the shared attributes between all the
 * classes. The shared attributes are generally constatns.
 * 
 * @author MohamedBenArbia
 * 
 */
public class SharedAttributes {




    /*************************** Broadcast message for finfing the server: FROM CLIENT ******************************/

    public final static String KEY_SEARCH_MESSAGE = "Search";
    public final static String VALUE_SEARCH_MESSAGE = "Hey server! Is that you ?";

    /*************************** Find server, reply to the search request: FROM SERVER ******************************/
    public final static String KEY_FIND_MESSAGE = "Announce";



    /********************************** PARAMS MESSAGE FROM SERVER **********************************/
	
	public final static String INSTRUCTION_MESSAGE_KEY = "Instruction";
	public final static String SAMPLING_RATE_MESSAGE_KEY = "SamplingRate";
	public final static String BITS_PER_SAMPLE_MESSAGE_KEY = "BitsPerSample";
	public final static String CHANNEL_MESSAGE_KEY = "Channel";
	public final static String PAYLOAD_TYPE_KEY = "PayloadType";
	public final static String IDENTIFIER_KEY = "Identifier";
	public final static String PORT_KEY = "Port";
	
	
	public static final int MAX_SAMPLING_RATE = 44100 ; 
	public static final int MAX_BYTES_PER_SAMPLE = 2 ; 
	public static final int MAX_DURATION_IN_SECONDS = 60 ; 
	
	

	
	/**
	 * The  size of the buffer of the client in which it stores data 
	 */
	
	
	public static final int BUFFER_SIZE = MAX_SAMPLING_RATE*MAX_BYTES_PER_SAMPLE*MAX_DURATION_IN_SECONDS ; 
	public static final int PORT = 8080;
	
	public static final int BROADCAST_PORT = 8585;

	public static String KEY_SERVER_ADDRESS = "pref_key_ip_adress_server";
	public static final String KEY_UDP_PORT_NUMBER_AUDIO = "udp_port_number_audio";
	public static final String KEY_UDP_PORT_NUMBER_SENSORS = "udp_port_number_sensors";
	public static final String KEY_SAMPLING_RATE_AUDIO = "sampling_rate_audio";
	public static final String KEY_BITS_PER_SAMPLE = "bits_per_sample";
	public static final String DROPBOX_APP_KEY = "app_key";
	public static final String DROPBOX_APP_SECRET = "app_secret";
	public static final String DROPBOX_ACCESS_TOKEN = "generated_access_token";

	
	public static final String ACCESS_TOKEN = "X_ErvEEjYHoAAAAAAAAIkznE_wjczZCjXQK0_-tSMfTzG5ZDsJMayGNG3qeVJQEF" ; 

	public static final String KEY_CHANNEL = "channel";
	public static final String DURATION_KEY = "duration";

	
	public final static int WRONG_IP_ADRESS_CODE = 1;
	public final static int CONNECTION_REFUSED_CODE = 2;
	public final static int CONNECTION_SUCCEFULL_CODE = 0;
	public final static int CONNECTION_TIMEOUT_CODE = 3;

	public final static int TOAST_DURATION = 1000;

	private static HashMap<Integer, Integer> samplingRatePayloadType = null;

	public static HashMap<Integer, Integer> getHashMap() {

		if (samplingRatePayloadType == null) {
			samplingRatePayloadType = new HashMap<Integer, Integer>();
			samplingRatePayloadType.put(8000, 96);
			samplingRatePayloadType.put(11025, 97) ; 
			samplingRatePayloadType.put(16000, 98) ; 
			samplingRatePayloadType.put(22050, 99) ;
			samplingRatePayloadType.put(44100, 100) ; 

		}
		
		return samplingRatePayloadType ; 
	}

}
