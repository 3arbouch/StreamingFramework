package epfl.lcav.instructions;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;

/**
 * Represents a Record and Stream instruction. When receiving this instruction,
 * the client will record audio with the given parameters and stream it to the
 * server.
 * 
 * @author MohamedBenArbia
 * 
 */

public class RecordAndStreamInstruction implements Instruction {

	/**
	 * The related Instruction message related to this instruction
	 */
	private InstructionMessages mInstructionMessage = InstructionMessages.RECORDAUDIO;
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
	 * Payload identifier number for the RTP Packet
	 */
	private int mPayloadType;

	/**
	 * Identifier of the device. Will be used as a CNAME of the device
	 * (Participant of RTP Session)
	 */
	private String mIdentifier;

	public RecordAndStreamInstruction(int samplingRate, int bitsPerSample,
			int channels, int payloadType, String identifier) {
		this.mSamplingRate = samplingRate;
		this.mBitsPerSample = bitsPerSample;
		this.mChannels = channels;
		this.mPayloadType = SharedAttributes.getHashMap().get(samplingRate);
		this.mIdentifier = identifier;
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
			json.put(SharedAttributes.PAYLOAD_TYPE_KEY, this.mPayloadType);
			json.put(SharedAttributes.IDENTIFIER_KEY, this.mIdentifier);

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

		RecordAndStreamInstruction recordAndStreamInstruction = null;

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
			int payloadType = json.getInt(SharedAttributes.PAYLOAD_TYPE_KEY);
			String identifier = json.getString(SharedAttributes.IDENTIFIER_KEY);

			recordAndStreamInstruction = new RecordAndStreamInstruction(
					samplingRate, bitsPerSample, channels, payloadType,
					identifier);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return recordAndStreamInstruction;
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

	public int getmPayloadType() {
		return mPayloadType;
	}

	public String getmIdentifier() {
		return mIdentifier;
	}

}
