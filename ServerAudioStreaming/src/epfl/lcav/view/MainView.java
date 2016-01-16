package epfl.lcav.view;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import epfl.lcav.model.AVReceive2;
import epfl.lcav.model.Device;
import epfl.lcav.model.DeviceAcceptor;
import epfl.lcav.view.matlabLinker.MatlabFrame;
/**
 * 
 * @author wissem
 *
 */
public class MainView extends JFrame {

	/**
	 * 
	 */
	private final static Rectangle RECTANGLE_REF = new Rectangle(0, 0, 500, 800);
	private static final long serialVersionUID = 1L;
	public static DefaultListModel<Device> list;
	public static HashMap<String, DeviceFrame> deviceFrameHashMap;

	public MainView() {
		super("Device Manager");
		this.setBounds(RECTANGLE_REF);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// ------------------set up the principle container panel---------------------------//

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());

		// ------------------creating the device list panel---------------------------//

		deviceFrameHashMap = new HashMap<>();
		// graphical component
		JList<Device> dlist = new JList<Device>();
		list = new DefaultListModel<Device>();
		// the model of the JList is a list of devices
		dlist.setModel(list);
		// set the factory that render an element of the list
		dlist.setCellRenderer(new DeviceCellRenderer());
		dlist.setVisibleRowCount(-1);
		JScrollPane listpanel = new JScrollPane(dlist);

		// ------------------creating the configuration panel------------------------//

		JPanel confPanel = new ManagerConfigPanel();

		// ------------------Creating the start/end session button-----------------//
		JButton startSessionButton = new JButton("Start a session");
		startSessionButton.addActionListener(new StartSessionButtonListener());
		
		// ------------------Creating the Matlab Algorithm pannel-----------------//
		MatlabFrame matlabPannel = new MatlabFrame( null);
		// ------------------Adding components to the principal container---------//
		containerPanel.add(startSessionButton, BorderLayout.PAGE_START);
		containerPanel.add(listpanel, BorderLayout.CENTER);
		containerPanel.add(matlabPannel, BorderLayout.LINE_START);
		containerPanel.add(confPanel, BorderLayout.PAGE_END);

		this.setContentPane(containerPanel);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				JFrame frame = (JFrame) e.getSource();

				int result = JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to exit the application?",
						"Exit Application", JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {

					// close the DeviceAcceptor thread so that no more devices
					// can join the session
					DeviceAcceptor.getInstance().close();

					for (Iterator<Device> it = DeviceAcceptor.getInstance()
							.getDevicesList().getList().iterator(); it
							.hasNext();) {
						Device device = (Device) it.next();
						System.out.println("CALL TO DEVICE DECONNECT FROM THE MAIN VIEW CLASS");
						device.deviceDeconnect();
						it.remove();
					}
					Iterator<Device> it = DeviceAcceptor.getInstance()
							.getDevicesList().getList().iterator(); 
					
					while (it.hasNext()) {
						// wait for all devices to disconnect
						System.out.println("waiting  for devices to disconnect ..");
					}
					System.err.println("all devices are closed ");
					// order the AVReceive2 thread to terminate
					synchronized (AVReceive2.closeSync) {
						AVReceive2.closedwindow = true;
						AVReceive2.closeSync.notifyAll();
					}
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}

			}
		});

	}

}
