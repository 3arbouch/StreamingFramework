package epfl.lcav.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import epfl.lcav.shared.ServerAttributes;
/**
 * Utility class that performs checks that IP address is valid
 * @author wissem
 *
 */
public class Ip4AddressVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
    	String ip = ((JTextField) input).getText();
    	ip=ip.trim();
    	if (ip == null || ip.isEmpty()) {
    		JOptionPane.showMessageDialog(null, "Wrong ip address "+ip);
    		((JTextField)input).setForeground(ServerAttributes.RED_COL);
    		return false;
    	}
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) {
	    	JOptionPane.showMessageDialog(null, "Wrong ip address "+ip);
    		((JTextField)input).setForeground(ServerAttributes.RED_COL);
	    	return false;
	    }
	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        if (!matcher.matches()) {
		    	JOptionPane.showMessageDialog(null, "Wrong ip address "+ip);
        		((JTextField)input).setForeground(ServerAttributes.RED_COL);
		    	return false;
	        }
	        ServerAttributes.getInstance().setRTPSessionAddress(ip);
	        ((JTextField)input).setForeground(ServerAttributes.GREEN_COL);
	        return true ;
	    } catch (PatternSyntaxException ex) {
	    	JOptionPane.showMessageDialog(null, "Wrong ip address "+ip);
    		((JTextField)input).setForeground(ServerAttributes.RED_COL);
	        return false;
	    }
    }
}