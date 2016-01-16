package epfl.lcav.exceptions;
/**
 * 
 * @author wissem
 *
 */
public class ExistingInstanceException extends Exception{

	/**
	 * Signals that an instance of this class that follows the singleton pattern already exist.
	 */
	private static final long serialVersionUID = 828426568764421258L;
	
	public ExistingInstanceException() {
		super("An instance of this class already exits. Use getInstance() instead !");
	}

}
