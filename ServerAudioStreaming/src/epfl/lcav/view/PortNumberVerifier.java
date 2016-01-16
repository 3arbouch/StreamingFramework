package epfl.lcav.view;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import epfl.lcav.shared.ServerAttributes;
/**
 * Utility class that performs a sanity check on the user's input
 * @author wissem
 *
 */
public class PortNumberVerifier extends InputVerifier {
	String message;
	public PortNumberVerifier(String m) {
		this.message=m;
	}
	
	@Override
	public boolean verify(JComponent input) {
		String text = ((JTextField) input).getText();
		text = text.trim();
		int p;
		try {
			p = Integer.valueOf(text);
			if (p > 65535 || p < ServerAttributes.MIN_PORT_NUM) {
				JOptionPane.showMessageDialog(null,
						message+": Port number must be in the range"
								+ ServerAttributes.MIN_PORT_NUM
								+ " .. 65535");
				((JTextField) input)
						.setForeground(ServerAttributes.RED_COL);
				return false;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null,
					message+": Port number must be in the range"
							+ ServerAttributes.MIN_PORT_NUM
							+ " .. 65535");
			((JTextField) input).setForeground(ServerAttributes.RED_COL);
			return false;
		}
		switch (message) {
		case ServerAttributes.TAG_TCP_CON_PORT:
			ServerAttributes.getInstance().setTCPConnectionPort(p);
			break;
		case ServerAttributes.TAG_TCP_FILE_TRANS_PORT:
			ServerAttributes.getInstance().setTCPFileTransmitPort(p);
			break;
		case ServerAttributes.TAG_RTP_REC_PORT:
			ServerAttributes.getInstance().setRTPAudioReceivePort(p);
			break;
		case ServerAttributes.TAG_RTP_TRANS_PORT:
			ServerAttributes.getInstance().setRTPAudioTransmitPort(p);
			break;
		default:
			break;
		}
		((JTextField) input).setForeground(ServerAttributes.GREEN_COL);
		return true;
	}
}