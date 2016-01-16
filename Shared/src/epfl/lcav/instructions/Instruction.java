package epfl.lcav.instructions;
/**
 * This interface represents a general instruction which can consist on : RECORDAUDIO , RECEIVEAUDIO ...
 * An instruction is used by the server to order the client performing certain services.
 * Each Instruction received from the server by the client results on launching a service.
 * Used by the client to form an instruction Object from the received message(String) .
 * @author MohamedBenArbia
 *
 */
public interface Instruction {

	
	/**
	 * Form the corresponding JSON Object for this instruction  and format it.
	 * The formatted JSON object (String) will be used by the server to send the instruction to the client.
	 * @return The formatted JSON which contains the description about the instruction.
	 */
	
	public String getFormattedJSONInstruction() ; 
	
	
	
	
	
}
