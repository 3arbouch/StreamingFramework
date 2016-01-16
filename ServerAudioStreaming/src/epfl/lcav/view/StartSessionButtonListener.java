package epfl.lcav.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.SocketException;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import epfl.lcav.model.AVReceive2;
import epfl.lcav.model.AnnouncingThread;
import epfl.lcav.model.Device;
import epfl.lcav.model.DeviceAcceptor;
import epfl.lcav.model.FetchingIPAddressCloudThread;
import epfl.lcav.shared.ServerAttributes;
/**
 * Listener for the Start/End session button 
 * Responsible for calling/killing the threads that handle a session 
 * @author wissem.allouchi@epfl.ch	
 *
 */
public class StartSessionButtonListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// first we verify the parameters of the session (the parameters that are stored in the SharedAttributes class )
		// i.e. the port numbers, the session ipAddress, the storage directory ...
		JButton source = (JButton) e.getSource();
		String text=source.getText();
		switch (text) {
		case ServerAttributes.TAG_START_SESSION:
			if (ServerAttributes.getInstance().validateAttirbutes()) {// if everything is fine then 
				source.setText(ServerAttributes.TAG_END_SESSION);
				System.err.println("Starting a new session");
				//no more modification on the configuration panel
				ManagerConfigPanel.disableTextFields();
				
				//Launch the Announcing procedure 
				
				try {
					AnnouncingThread announce = new AnnouncingThread() ;
					Thread announceThread = new Thread(announce) ; 
					announceThread.start() ; 
				} catch (SocketException e1) {
				        // TODO fix 	
				} 
				
				// Write the IP address in the Cloud 
				
				FetchingIPAddressCloudThread fetching = new FetchingIPAddressCloudThread() ; 
				Thread fetchingThread = new Thread(fetching) ; 
				fetchingThread.start() ; 
				
				// Launch the TCP server that allow to accept new devices
				DeviceAcceptor server = DeviceAcceptor.getInstance();
				server.start();
				// Launch the Thread that will receive RTP audio data from the connected
				// devices
				Thread audioReceiverThread = new Thread(new AVReceive2());
				audioReceiverThread.start();
			} else {// notify the client about the incorrect attributes
				JOptionPane.showMessageDialog(null, ServerAttributes.getInstance().getErroMessage());
			}
			break;
			
		case ServerAttributes.TAG_END_SESSION:
			System.out.println("SESSION ENDED");
			ImageIcon ii=new ImageIcon("ajax-loader.gif");
			source.setIcon(ii);
			source.setText("ending session");
			
			// close AvReceive thread and the device acceptor
			
			// close the DeviceAcceptor thread so that no more devices
			// can join the session
			DeviceAcceptor.getInstance().close();

			for (Iterator<Device> it = DeviceAcceptor.getInstance()
					.getDevicesList().getList().iterator(); it
					.hasNext();) {
				Device device = (Device) it.next();
				device.deviceDeconnect();
				it.remove();
				//remove the device from the GUI list
				MainView.list.removeElement(device);
				// Destroy the frame corresponding to this device 
				System.err.println("\tDestroy the frame corresponding to device (BUTTON LISTENER)"+device);
				DeviceFrame frame = MainView.deviceFrameHashMap.get(device.getIdentifier());
				if (frame!=null) 
					frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
				MainView.deviceFrameHashMap.remove(device.getIdentifier());
				System.err.println("\tsuccessful deconection");
			}
			Iterator<Device> it = DeviceAcceptor.getInstance()
					.getDevicesList().getList().iterator(); 
			
			while (it.hasNext()) {
				// wait for all devices to disconnect
			}
			System.err.println("all devices are closed ");
			DeviceAcceptor.killDeviceAcceptor();
			// order the AVReceive2 thread to terminate
			synchronized (AVReceive2.closeSync) {
				AVReceive2.closedwindow = true;
				AVReceive2.closeSync.notifyAll();
			}
			
			source.setIcon(null);
			source.setText(ServerAttributes.TAG_START_SESSION);
			break;

		default:
			break;
		}
		
		
	}

}
