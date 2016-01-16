package epfl.lcav.instructions;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;

public class StopReceivingLiveAudioInstruction implements Instruction {

	private InstructionMessages mInstructionMessage = InstructionMessages.STOPRECEIVINGLIVEAUDIO;


	@Override
	public String getFormattedJSONInstruction() {
		
		String formattedInstruction = null;
		JSONObject json = new JSONObject();

		try {
			json.put(SharedAttributes.INSTRUCTION_MESSAGE_KEY,
					this.mInstructionMessage.toString());
		

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
		return new StopReceivingLiveAudioInstruction() ; 
	}

}
