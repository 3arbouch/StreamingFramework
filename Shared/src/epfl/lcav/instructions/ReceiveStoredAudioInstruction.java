package epfl.lcav.instructions;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;

public class ReceiveStoredAudioInstruction implements Instruction{

	/**
	 * The related Instruction message related to this instruction
	 */
	private InstructionMessages mInstructionMessage = InstructionMessages.RECEIVESTOREDAUDIO;
	/**
	 * Sampling rate with which the device will record the audio from the MIC.
	 */
	private int mSamplingRate;

	/**
	 * Number of bits per sample
	 */
	private int mBitsPerSample;
	/**
	 * Number of Audio Channels
	 */
	private int mChannels;
	
	/**
	 * Port through which the client will receive the stream of stored audio 
	 */
	private int mPort ; 
	
	/**
	 * Duration of the audio file to send (from the server)/ read by the client 
	 */
	private int mDuration ; 

	

	public  ReceiveStoredAudioInstruction(int port,int samplingRate, int bitsPerSample,
			int channels, int duration) {
		this.mSamplingRate = samplingRate;
		this.mBitsPerSample = bitsPerSample;
		this.mChannels = channels;
		this.mPort = port ; 
		this.mDuration = duration ; 
		
	}

	@Override
	public String getFormattedJSONInstruction() {
		String formattedInstruction = null;
		JSONObject json = new JSONObject();

		try {
			json.put(SharedAttributes.INSTRUCTION_MESSAGE_KEY,
					this.mInstructionMessage.toString());
			json.put(SharedAttributes.SAMPLING_RATE_MESSAGE_KEY,
					this.mSamplingRate);
			json.put(SharedAttributes.BITS_PER_SAMPLE_MESSAGE_KEY,
					this.mBitsPerSample);
			json.put(SharedAttributes.CHANNEL_MESSAGE_KEY, this.mChannels);
			json.put(SharedAttributes.PORT_KEY, this.mPort) ; 
			json.put(SharedAttributes.DURATION_KEY, this.mDuration) ; 

			formattedInstruction = json.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return formattedInstruction;
	}

	/**
	 * Translate a string message into an Instruction object .
	 * Used by the client to get the corresponding Instruction from the message received from the server. 
	 * @param receivedMessage String that contains details of the instruction (Encapsulated into a JSON Object)
	 * @return Corresponding instruction .
	 */

	public static Instruction getInstructionFromString(String receivedMessage) {

		ReceiveStoredAudioInstruction receiveStoredAudioInstruction = null;

		JSONObject json = null;
		try {
			json = new JSONObject(receivedMessage);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			int samplingRate = json
					.getInt(SharedAttributes.SAMPLING_RATE_MESSAGE_KEY);
			int bitsPerSample = json
					.getInt(SharedAttributes.BITS_PER_SAMPLE_MESSAGE_KEY);
			int channels = json.getInt(SharedAttributes.CHANNEL_MESSAGE_KEY);
			int port = json.getInt(SharedAttributes.PORT_KEY) ; 
			int duration = json.getInt(SharedAttributes.DURATION_KEY) ;
			receiveStoredAudioInstruction = new ReceiveStoredAudioInstruction(port,
					samplingRate, bitsPerSample, channels,duration);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return receiveStoredAudioInstruction;
	}

	public int getmSamplingRate() {
		return mSamplingRate;
	}

	public int getmBitsPerSample() {
		return mBitsPerSample;
	}

	public int getmChannels() {
		return mChannels;
	}


	 public int getmPort() {
		return mPort;
	}
	 
	 public int getDuration(){
		  return mDuration ; 
	 }

}
