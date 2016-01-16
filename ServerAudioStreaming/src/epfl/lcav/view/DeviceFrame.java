package epfl.lcav.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.json.JSONException;
import epfl.lcav.instructions.ReceiveLiveAudioInstruction;
import epfl.lcav.instructions.RecordAndStreamInstruction;
import epfl.lcav.instructions.StopReceivingLiveAudioInstruction;
import epfl.lcav.instructions.StopRecordAudioInstruction;
import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.controller.DeviceBuffer;
import epfl.lcav.model.Device;

/**
 * This class represents the frame that will be shown when a device is
 * connected. When the device button is clicked, this frame will show the
 * details about this device and the possible actions that can be performed
 * 
 * @author MohamedBenArbia
 * 
 */
public class DeviceFrame extends JFrame {

	// TODO 
	// the new file button : make a radio btn and label to show on which directory/file the audio is being stored
	// make the borders more visible 
	// try to fix the real time and Fourier representations of the audio signal 
	private static final long serialVersionUID = 1L;

	/**
	 * The device related to this frame
	 */
	Device mDevice;

	/**
	 * The principle panel that will contain all the other panels
	 */
	private Container principlePanel;

	/**
	 * Panel that displays the real time amplitude received audio data
	 */
	private JPanel mRealTimeAmplitudeAudioDataDisplayer;

	/**
	 * Panel that displays the fft of the received audio data
	 */
	private JPanel mRealTimeFourrierAudioDataDisplayer;

	/**
	 * Panel that holds the two panels: real time amplitude and fourrier audio
	 * data displayer
	 */

	private JPanel mRealTimeAudioDataDisplayerHolder;



	/**
	 * Selected File To Send
	 */
	protected File mSelectedFile = null;
	
	protected JList<Device> dlist;
	//protected JLabel mlistTextView;

	protected boolean forward;

	protected Device selectedDevice;

	protected int selectedSamplingRate;

	protected int selectedBitsPerSample;

	protected int selectedChannel;

	
	
	private ParameterInfoPanel paramInfoPan;
	
	
	/**
	 * size of the area reserved for display of the audio signal 
	 */
	public int availableDispAreaSize;
	
	
	public DeviceFrame(Device device) {
		super(device.getIdentifier());
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    this.setBounds(0,0,screenSize.width, screenSize.height);
		//this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		

		this.mDevice = device;

		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				DeviceFrame frame = (DeviceFrame) e.getSource();
				frame.getmDevice().deviceDeconnect();
				DeviceBuffer.getInstance().removeDevice(frame.getmDevice());
				// remove the device from the GUI list
				MainView.list.removeElement(frame.getmDevice());
				// Destroy the frame corresponding to this device
				System.err
						.println("\tDestroy the frame corresponding to device "
								+ frame.getmDevice());

				MainView.deviceFrameHashMap.remove(frame);
				System.err.println("\tsuccessful deconection");
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		});

		dlist = new JList<Device>();
		dlist.setModel(MainView.list);
		dlist.setCellRenderer(new DeviceCellRenderForDeviceFrame(this.mDevice));
		dlist.setVisibleRowCount(2);
		

		mRealTimeAmplitudeAudioDataDisplayer = new JPanel(true);
		mRealTimeFourrierAudioDataDisplayer = new JPanel(true);
		mRealTimeAudioDataDisplayerHolder = new JPanel(true);
		mRealTimeAudioDataDisplayerHolder.setLayout(new GridLayout(2, 1));
		mRealTimeAmplitudeAudioDataDisplayer.setBackground(new Color(0, 191,255));
		mRealTimeFourrierAudioDataDisplayer.setBackground(new Color(0, 191, 255));


	}

	public void showFrame() {
		
		paramInfoPan = new ParameterInfoPanel(this);

		principlePanel = this.getContentPane();
		//principlePanel.setOpaque(true);
		principlePanel.setBackground(Color.green);
		
		if (this.mSelectedFile == null) {
			paramInfoPan.devPlaySoundConfPanel.mSelectedFileTextView = new JLabel("No file is selected!");
			paramInfoPan.devPlaySoundConfPanel.mSendFileButton.setEnabled(false);
		} else {
			paramInfoPan.devPlaySoundConfPanel.mSelectedFileTextView = new JLabel("Selected File: "
					+ this.mSelectedFile.getAbsolutePath());
		}

		//this.setContentPane(principlePanel);
		
		
		
		 // Setting up the Real Data displayer panels
		 
		
		
		mRealTimeAudioDataDisplayerHolder
		.add(mRealTimeAmplitudeAudioDataDisplayer);
		mRealTimeAudioDataDisplayerHolder
		.add(mRealTimeFourrierAudioDataDisplayer);

		principlePanel.setLayout(new BoxLayout(principlePanel, BoxLayout.X_AXIS));
		
		this.setVisible(true);
		Dimension d1= new Dimension((int)principlePanel.getSize().getWidth()/6, (int)principlePanel.getSize().getHeight()); 
		System.out.println(d1.width);
		principlePanel.setMaximumSize(principlePanel.getSize());
		paramInfoPan.setMaximumSize(d1);
		mRealTimeAudioDataDisplayerHolder.setMaximumSize(new Dimension((int)(principlePanel.getSize().getWidth()-d1.width),(int)d1.height));
		this.availableDispAreaSize=(int)(principlePanel.getSize().getWidth()-d1.width);
		principlePanel.add(paramInfoPan);
		principlePanel.add(mRealTimeAudioDataDisplayerHolder);

		principlePanel.repaint();
		this.repaint();

		
		System.out.println("param width ="+paramInfoPan.getWidth()+" holder width = "+mRealTimeAudioDataDisplayerHolder.getWidth());

		System.out.println(mRealTimeAudioDataDisplayerHolder.getWidth());
		
	}

	public Device getSelectedDevice() {
		return selectedDevice;
	}



	public boolean isForward() {
		return forward;
	}

	public int getSelectedSamplingRate() {
		return selectedSamplingRate;
	}

	public int getSelectedChannel() {
		return selectedChannel;
	}

	public int getSelectedBitsPerSample() {
		return selectedBitsPerSample;
	}
	
	public boolean isSavingOptionActivated() {
		return paramInfoPan.devStreamingConfPanel.savingOption.isSelected();
	}

	void startRecordingAndStreaming() {

		int payload = SharedAttributes.getHashMap().get(
				selectedSamplingRate);
		RecordAndStreamInstruction recordAndStreamInstruction = new RecordAndStreamInstruction(
				selectedSamplingRate, selectedBitsPerSample, selectedChannel,
				payload, this.mDevice.getIdentifier());

		this.mDevice.writeOrder(recordAndStreamInstruction
				.getFormattedJSONInstruction());

	}

    void startReceivingAudio() {

		ReceiveLiveAudioInstruction receiveLiveAudioInstruction = new ReceiveLiveAudioInstruction(
				selectedSamplingRate, selectedBitsPerSample, selectedChannel);

		this.selectedDevice.writeOrder(receiveLiveAudioInstruction
				.getFormattedJSONInstruction());

	}

	void stopRecordingAudio() throws JSONException {
		StopRecordAudioInstruction stopRecordAudioInstruction = new StopRecordAudioInstruction();

		this.mDevice.writeOrder(stopRecordAudioInstruction
				.getFormattedJSONInstruction());


	}

	void stopReceivingAudio() throws JSONException {
		
		

		StopReceivingLiveAudioInstruction stopReceivingLiveAudioInstruction = new StopReceivingLiveAudioInstruction();

		this.selectedDevice.writeOrder(stopReceivingLiveAudioInstruction
				.getFormattedJSONInstruction());

		
	}
	
	/**
	 * 
	 * @return the device that this frame is being monitoring 
	 */
	public Device getmDevice() {
		return mDevice;
	}

	

	public JPanel getmRealTimeAmplitudeAudioDataDisplayer() {
		return mRealTimeAmplitudeAudioDataDisplayer;
	}

	public JPanel getmRealTimeAudioDataDisplayerHolder() {
		return mRealTimeAudioDataDisplayerHolder;
	}

	public ParameterInfoPanel getParamInfoPan() {
		return paramInfoPan;
	}

	public JPanel getmRealTimeFourrierAudioDataDisplayer() {
		return mRealTimeFourrierAudioDataDisplayer;
	}

}
