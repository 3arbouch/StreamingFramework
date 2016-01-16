package epfl.lcav.instructions;

import org.json.JSONException;
import org.json.JSONObject;

import epfl.lcav.attributes.SharedAttributes;


/**
 * Is an Utility Class that detect which is the corresponding the instruction from the received message from the server.
 * Used by the client to detect the type of the instruction contained in the received message.
 * @author MohamedBenArbia
 *
 */
public class InstructionDetector {
	
	
	public static InstructionMessages  detectInstruction(String receivedMessage) {
		InstructionMessages type = null ; 
		try {
			JSONObject json = new JSONObject(receivedMessage)  ;
			InstructionMessages instructionMessage = InstructionMessages
					.valueOf(json.getString(SharedAttributes.INSTRUCTION_MESSAGE_KEY));
			type = instructionMessage ; 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		
		
		
		
		return type ; 
	}

}
