package epfl.lcav.messagesProcessing;

import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.instructions.InstructionDetector;
import epfl.lcav.instructions.InstructionMessages;
import epfl.lcav.instructions.ReceiveLiveAudioInstruction;
import epfl.lcav.instructions.ReceiveStoredAudioInstruction;
import epfl.lcav.instructions.RecordAndStreamInstruction;
import epfl.lcav.services.ReceiveLiveAudioStoreAndPlaySercive;
import epfl.lcav.services.ReceiveStoredAudioAndPlayService;
import epfl.lcav.services.RecordAudioAndStreamService;
import epfl.lcav.services.RecordSensorsDataAndStream;


/**
 * This class is responsible of handling the messages from server and to launch
 * the corresponding service This class implements the Singelton pattern since
 * only one instance of this class is needed
 * 
 * @author MohamedBenArbia
 * 
 */
public class MessageHandler {

	/********************************** PARAMS MESSAGE FROM SERVER **********************************/
	
	private final static String INSTRUCTION_MESSAGE_KEY = "Instruction";
	private final static String SAMPLING_RATE_MESSAGE_KEY = "SamplingRate";
	private final static String BITS_PER_SAMPLE_MESSAGE_KEY = "BitsPerSample";
	private final static String CHANNEL_MESSAGE_KEY = "Channel";
	private final static String PAYLOAD_TYPE_KEY = "PayloadType";
	private final static String IDENTIFIER_KEY = "Identifier";

	private static MessageHandler instance = null;

	protected MessageHandler() {

	}

	public static MessageHandler getInstance() {
		if (instance == null) {
			instance = new MessageHandler();
		}
		return instance;
	}

	/**
	 * Is used to obtain the message value (String) from the message received
	 * from the server: If the received message is a valid one , it return its
	 * value otherwise it return an empty String. This method is used by the
	 * reading thread in order to show the valid received message on the screen
	 * of the client
	 * 
	 * @param message
	 * @return
	 */
	public String getMessageValue(InstructionMessages message) {
		String messageValue = "";

		switch (message) {
		case RECORDAUDIO:
			messageValue = InstructionMessages.RECORDAUDIO.toString()
					+ ": recording and streaming audio to the server ...";
			break;

		case STOPRECORDAUDIO:
			messageValue = InstructionMessages.STOPRECORDAUDIO.toString()
					+ ": stop recording!!";
			break;
		case RECORDSENSORSDATA:
			messageValue = InstructionMessages.RECORDSENSORSDATA.toString()
					+ ": recording and streaming sensors data to the server ...";
			break;
		default:
			break;
		}

		return messageValue;
	}

	/**
	 * This method is responsible of handling messages received from the server:
	 * for each message received, this method calls the corresponding service to
	 * be launched and save the parameters if available.
	 * 
	 * @param message
	 * @throws UnknownHostException
	 * @return message to display on the screen that explains the instruction
	 *         received from the server
	 * @throws JSONException
	 *             is thrown when an error occurs during the construction of
	 *             JSON Object
	 */

	public void handleMessages(String receivedMessage) throws JSONException, UnknownHostException {
		
		InstructionMessages instructionMessage = 	InstructionDetector.detectInstruction(receivedMessage) ; 

			JSONObject json = new JSONObject(receivedMessage);

			// Get the instruction message
			
			switch (instructionMessage) {
			case RECORDAUDIO:{
				// In case the instruction is RECORDAUDIO, parameters of the
				// recorded audio must be sent
				// from the server
				// Get the paramaters of the recording process from the instruction object 
				RecordAndStreamInstruction recordAndStreamInstruction = 
					(RecordAndStreamInstruction)RecordAndStreamInstruction.getInstructionFromString(receivedMessage) ; 
				
				int samplingRate = recordAndStreamInstruction.getmSamplingRate(); 
				int bitsPerSample = recordAndStreamInstruction.getmBitsPerSample() ; 
				int channel = recordAndStreamInstruction.getmChannels() ; 
				int payloadType = recordAndStreamInstruction.getmPayloadType() ; 
				String identifier = recordAndStreamInstruction.getmIdentifier() ; 
				
				
				//Launch the service 
				RecordAudioAndStreamService.launchService(samplingRate,
						bitsPerSample, channel,payloadType, identifier);
				break;
			}
			case STOPRECORDAUDIO:{
				
				RecordAudioAndStreamService.stopService();
				break;
			}

			case RECEIVELIVEAUDIO:{
				ReceiveLiveAudioInstruction receiveLiveAudioInstruction = (ReceiveLiveAudioInstruction)ReceiveLiveAudioInstruction.getInstructionFromString(receivedMessage) ; 
				int samplingRate = receiveLiveAudioInstruction.getmSamplingRate() ; 
				int bitsPerSample = receiveLiveAudioInstruction.getmBitsPerSample() ; 
				long channels = receiveLiveAudioInstruction.getmChannels() ; 
				ReceiveLiveAudioStoreAndPlaySercive.launchService(samplingRate, bitsPerSample, channels) ; 
				break ; 
			}
			
			case RECEIVESTOREDAUDIO: {
			
				ReceiveStoredAudioInstruction receiveStoredAudioInstruction = (ReceiveStoredAudioInstruction)ReceiveStoredAudioInstruction.getInstructionFromString(receivedMessage) ; 
				int samplingRate = receiveStoredAudioInstruction.getmSamplingRate() ; 
				int bitsPerSample = receiveStoredAudioInstruction.getmBitsPerSample() ; 
				long channels = receiveStoredAudioInstruction.getmChannels() ; 
				int port = receiveStoredAudioInstruction.getmPort() ;
				int duration = receiveStoredAudioInstruction.getDuration() ; 
				ReceiveStoredAudioAndPlayService.launchService(port , samplingRate, bitsPerSample, channels, duration) ; 
			}
			
			
			case STOPRECEIVINGLIVEAUDIO:{
				ReceiveLiveAudioStoreAndPlaySercive.stopService() ; 
				break ; 
			}
			
			case STOPALL :{
				RecordAudioAndStreamService.stopService();
				ReceiveLiveAudioStoreAndPlaySercive.stopService() ; 
			}
				
			case RECORDSENSORSDATA:
			
				new RecordSensorsDataAndStream().launchService();
				break;
			default:
				break;
			}

		


	}
}
