package epfl.lcav.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import epfl.lcav.shared.ServerAttributes;
/**
 * Creates the panel that allows the user to manually configure the application before starting a session 
 * User can manage ports number (to receive data), the IP address of the session(should be the ip address of the server),
 * the directory where to store the data...   
 * @author wissem
 *
 */
public class ManagerConfigPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5105685577931963496L;
	private JFileChooser dirChooser = new JFileChooser();


	private final String TAG_CHOOSE_DIR = "StorageDirectory";

	private JPanel confPan;
	protected static JTextField jTCPConPort,jTCPFilePort,jrtpRecPort,jrtpTransPort,jrtpSession;
	

	public ManagerConfigPanel() {
		super(new GridLayout(1, 1));
		// ---------------- First tab : storage tab -----------------//
		JTabbedPane tabbedPane = new JTabbedPane();

		JComponent panel1 = new JPanel();
		JButton b = new JButton(TAG_CHOOSE_DIR);
		b.addActionListener(this);
		JLabel l = new JLabel("Files will be saved at:\n"+ServerAttributes.getInstance().getRecordingDirectory());
		panel1.setLayout(new GridLayout(2, 1));
		panel1.add(b);
		panel1.add(l);
		tabbedPane.addTab("Storage config", null, panel1,
				"Edit the storage path");

		// ---------------- second tab : configuration tab -----------------//
		confPan = new JPanel();
		confPan.setLayout(new GridLayout(5, 1));
		jTCPConPort=new JTextField(""+ServerAttributes.getInstance().getTCPConnectionPort());
		confPan.add(makeMyPanel("TCP connection port",jTCPConPort,new PortNumberVerifier("TCP connection port")));
		jTCPFilePort=new JTextField(""+ServerAttributes.getInstance().getTCPFileTransmitPort());
		confPan.add(makeMyPanel("TCP file transmit port", jTCPFilePort, new PortNumberVerifier("TCP file transmit port")));
		jrtpRecPort=new JTextField(""+ServerAttributes.getInstance().getRTPAudioReceivePort());
		confPan.add(makeMyPanel("RTP audio receive port",jrtpRecPort,new PortNumberVerifier("RTP audio receive port")));
		jrtpTransPort=new JTextField(""+ServerAttributes.getInstance().getRTPAudioTransmitPort());
		confPan.add(makeMyPanel("RTP audio transmit port",jrtpTransPort,new PortNumberVerifier("RTP audio transmit port")));
		jrtpSession = new JTextField(ServerAttributes.getInstance().getRTPSessionAddress());
		confPan.add(makeMyPanel("RTP audio receive session address",jrtpSession,new Ip4AddressVerifier()));

		tabbedPane.addTab("Session configuration", null, confPan, "Configuring Session ports and addresses");

		// Add the tabbed pane to this panel.
		add(tabbedPane);

		// The following line enables to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	private JComponent makeMyPanel(String text, JTextField t,InputVerifier verifier) {
		JLabel l = new JLabel(text);
		//JTextField t = new JTextField();
		//t.setText(defaultText);
		t.setInputVerifier(verifier);
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(l, BorderLayout.LINE_START);
		p1.add(t, BorderLayout.LINE_END);
		return p1;
	}
	protected static void disableTextFields() {
		jTCPConPort.setEnabled(false);
		jTCPFilePort.setEnabled(false);
		jrtpRecPort.setEnabled(false);;
		jrtpTransPort.setEnabled(false);
		jrtpSession.setEnabled(false);
	}
	protected static void enableTextFields() {
		jTCPConPort.setEnabled(true);
		jTCPFilePort.setEnabled(true);
		jrtpRecPort.setEnabled(true);;
		jrtpTransPort.setEnabled(true);
		jrtpSession.setEnabled(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();
		String bText = b.getText();
		switch (bText) {
			
			
		case TAG_CHOOSE_DIR:
			JPanel p = (JPanel) b.getParent();
			JLabel l = (JLabel) p.getComponents()[1];
			dirChooser
					.setDialogTitle("Select the directory where to save the wav files");
			dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			dirChooser.setAcceptAllFileFilterUsed(false);
			if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String dirPath = dirChooser.getSelectedFile().getAbsolutePath();
				String message;
				if ((message=ServerAttributes.getInstance().setRecordingDirectory(dirPath))!=null) {
					JOptionPane.showMessageDialog(null, message);
					
				} else {
					l.setText("Files will be saved at: \n"+ServerAttributes.getInstance().getRecordingDirectory());
					this.repaint();
				}
			} else {
				String message="No selection !";
				JOptionPane.showMessageDialog(null, message);
			}
			break;
		default:
			break;
		}
	}
}
