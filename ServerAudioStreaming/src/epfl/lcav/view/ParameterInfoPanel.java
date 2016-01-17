package epfl.lcav.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.json.JSONException;

import com.sun.media.sound.WaveFileReader;

import epfl.lcav.instructions.ReceiveStoredAudioInstruction;
import epfl.lcav.model.WAVReader;
import epfl.lcav.shared.ServerAttributes;

public class ParameterInfoPanel extends JPanel{
	/**
	 * device frame: the container frame of all panels 
	 */
	private DeviceFrame actualDeviceFrame;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7787184841185828521L;

	/**
	 * The possible bits per sample values
	 */
	private static final String[] BITS_PER_SAMPLE = new String[] { "16" };

	/**
	 * The possible channel values
	 */
	private static final String[] CHANNELS = new String[] { "1" };
	/**
	 * The First Panel that will handle the configuration of the streaming part
	 * of the phone
	 */
	protected PhoneStreamingConfigurationPannel devStreamingConfPanel;
	/**
	 * The Second Panel that will handle the configuration of sending to the
	 * phone a file to play ;
	 */
	protected PhonePlayingConfigurationPannel devPlaySoundConfPanel;
	
	protected InformationPanel infoMonitorPanel;

	public ParameterInfoPanel(DeviceFrame df) {
		this.actualDeviceFrame=df;
		//setup devStreaming panel  
		TitledBorder title;
		title = BorderFactory.createTitledBorder("Audio Streaming Configuration");
		devStreamingConfPanel = new PhoneStreamingConfigurationPannel();
		devStreamingConfPanel.setBorder(title);
		//setup devPlaysound panel
		title = BorderFactory.createTitledBorder("Play sound Configuration");
		devPlaySoundConfPanel = new PhonePlayingConfigurationPannel();
		devPlaySoundConfPanel.setBorder(title);
		//setup information panel 
		infoMonitorPanel = new InformationPanel();
		title = BorderFactory.createTitledBorder("Details");
		infoMonitorPanel.setBorder(title);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(devStreamingConfPanel);
		this.add(devPlaySoundConfPanel);
		this.add(infoMonitorPanel);
	}
	
	public void setinfoMonitor(int expcount, boolean state) {
		System.out.println("SETTING THE INFO MONITOR PANEL");
		Color c = (state) ? Color.GREEN : Color.RED;
		this.infoMonitorPanel.expCount = expcount;
		this.infoMonitorPanel.update(c);
	}
	
	
	
	/**
	 * The First Panel that will handle the configuration of the streaming part
	 * of the phone
	 * @author wissem
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected class PhoneStreamingConfigurationPannel extends JPanel implements ActionListener {
		
		private static final String TAG_SAMPLING_RATE = "Sampling Rate";
		private static final String TAG_BITS_PER_SAMPLE = "Bits Per Sample";
		private static final String TAG_CHANNEL = "Number Of Channels(MONO OR STEREO)";
		private static final String TAG_DLIST = "Send to";
		/**
		 * Text to show on the StartButton
		 */
		private static final String TAG_START_RECORDING_BUTTON = "RECORD AND STREAM";
		private static final String TAG_STOP_RECORDING_BUTTON = "STOP RECORDING";
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The possible sampling rates values
		 */
		private final String[] SAMPLING_RATES = new String[] { "16000",
				"22050", "44100" };
		
		/**
		 * The ComboBox containing all the possible sampling rates
		 */
		private JComboBox mSamplingRates;

		/**
		 * The ComboBox containing all the possible bitsPerSample values
		 */
		private JComboBox mBitsPerSample;

		/**
		 * The ComboBox containing all the possible channel values
		 */
		private JComboBox mChannel;

		/**
		 * Button responsible of sending the order to the client in order to start
		 * recording
		 */
		private JButton mStartRecording;
		/**
		 * check box to allow the user to choose between saving the incoming 
		 * stream in a new sound file (i.e. that corresponds to a new experiment)
		 * or to append it to the previous file (continue on the same experiment) 
		 */
		protected JCheckBox savingOption = new JCheckBox("New File?", true);
		
		protected PhoneStreamingConfigurationPannel() {
			mSamplingRates = new JComboBox(SAMPLING_RATES);
			mBitsPerSample = new JComboBox(BITS_PER_SAMPLE);
			mChannel = new JComboBox(CHANNELS);
			mStartRecording = new JButton(TAG_START_RECORDING_BUTTON);
			mStartRecording.addActionListener(this);
			
			// configuration of the host panel i.e. phone streaming conf panel 
			this.setName("Streaming Configuration Pannel");
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			//add the sampling rate 
			JPanel p = new JPanel();
			TitledBorder title;
			title = BorderFactory.createTitledBorder(TAG_SAMPLING_RATE);
			p.setBorder(title);
			p.add(mSamplingRates);
			this.add(p);
			//add bits per sample 
			p=new JPanel();
			title = BorderFactory.createTitledBorder(TAG_BITS_PER_SAMPLE);
			p.setBorder(title);
			p.add(mBitsPerSample);
			this.add(p);
			//add number of channels 
			p=new JPanel();
			title = BorderFactory.createTitledBorder(TAG_CHANNEL);
			p.setBorder(title);
			p.add(mChannel);
			this.add(p);
			//add the device list
			p=new JPanel();
			p.add(new JScrollPane(actualDeviceFrame.dlist));
			title = BorderFactory.createTitledBorder(TAG_DLIST);
			p.setBorder(title);
			this.add(p);
			
			
			//add the start/stop btn
			this.add(mStartRecording);
			//add the saving option
			this.add(savingOption);
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JButton selectedbutton = (JButton) e.getSource();
			String textOnSelectedButton = selectedbutton.getText();
			switch (textOnSelectedButton) {
				case TAG_START_RECORDING_BUTTON: {
	
					selectedbutton.setText(TAG_STOP_RECORDING_BUTTON);
					mSamplingRates.setEnabled(false);
					mBitsPerSample.setEnabled(false);
					mChannel.setEnabled(false);
					actualDeviceFrame.dlist.setEnabled(false);
					if (actualDeviceFrame.dlist.getSelectedValue() != null
							&& !actualDeviceFrame.dlist.getSelectedValue().equals(actualDeviceFrame.mDevice)) {
						actualDeviceFrame.forward = true;
						actualDeviceFrame.selectedDevice = actualDeviceFrame.dlist.getSelectedValue();
					}
	
					actualDeviceFrame.selectedSamplingRate = Integer.valueOf(mSamplingRates
							.getSelectedItem().toString());
					actualDeviceFrame.selectedBitsPerSample = Integer.valueOf(mBitsPerSample
							.getSelectedItem().toString());
					actualDeviceFrame.selectedChannel = Integer.valueOf(mChannel.getSelectedItem()
							.toString());
					// order the device to start recording and streaming audio
					actualDeviceFrame.startRecordingAndStreaming();
	
					
					// Reading Instruction: Testing phase
					
	
					if (actualDeviceFrame.forward) {
						// order the device to start receiving
						actualDeviceFrame.startReceivingAudio();
						
					}
					break;
				}
				case TAG_STOP_RECORDING_BUTTON: {

					try {
						selectedbutton.setText(TAG_START_RECORDING_BUTTON);

						mSamplingRates.setEnabled(true);
						mBitsPerSample.setEnabled(true);
						mChannel.setEnabled(true);
						actualDeviceFrame.dlist.setEnabled(true);
						// order the device to stop recording
						actualDeviceFrame.stopRecordingAudio();
						// order the device to stop receiving
						if (actualDeviceFrame.dlist.getSelectedValue() != null
								&& !actualDeviceFrame.dlist.getSelectedValue().equals(actualDeviceFrame.mDevice)) {
							actualDeviceFrame.stopReceivingAudio();
							actualDeviceFrame.forward=false;
						}
						setinfoMonitor(infoMonitorPanel.expCount, false);

					} catch (JSONException e1) {
						e1.printStackTrace();
					}

					break;
				}
			}
			
		}
	}//phoneStreamingConfigurationPanel
	
	/**
	 * The Second Panel that will handle the configuration of sending to the
	 * phone a file to play 
	 * @author wissem
	 * 
	 */
	 protected class PhonePlayingConfigurationPannel extends JPanel implements ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final String TAG_CHOOSE_FILE = "Select a File to send ";
		private static final String TAG_SEND_FILE_BUTTON = "STREAM TO DEVICE";
		protected static final String TAG_SELECTED_FILE = "No file is selected!";
		/**
		 * File Chooser responsible of displaying a dialog panel in order to choose 
		 * a sound file that will be sent to the device
		 */
		private JFileChooser fileChooser;
		/**
		 * Button responsible for displaying the choosing Dialog to select a sound file that will be sent to the device  
		 */
		protected JButton chooseFileButton;
		/**
		 * Button responsible of streaming the chosen file to the device
		 */
		protected JButton mSendFileButton;
		
		/**
		 * Label to show a msg after choosing the sound file to send 
		 */
		protected JLabel mSelectedFileTextView=new JLabel(TAG_SELECTED_FILE);
		
		protected PhonePlayingConfigurationPannel() {
			fileChooser = new JFileChooser();
			chooseFileButton = new JButton(TAG_CHOOSE_FILE);
			mSendFileButton = new JButton(TAG_SEND_FILE_BUTTON);
			chooseFileButton.addActionListener(this);
			mSendFileButton.addActionListener(this);
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			//configuration of the layout of the host panel i.e phone play conf panel
			
			this.setName("Playing Configuration Pannel");
			this.add(chooseFileButton);
			JPanel p = new JPanel();
			TitledBorder title = BorderFactory.createTitledBorder("File to stream");
			p.setBorder(title);
			p.add(mSelectedFileTextView);
			this.add(p);
			this.add(mSendFileButton);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JButton selectedbutton = (JButton) e.getSource();
			String textOnSelectedButton = selectedbutton.getText();
			
			switch (textOnSelectedButton) {
				case TAG_CHOOSE_FILE: {
					
					fileChooser.showDialog(this, "Select");
					if ((actualDeviceFrame.mSelectedFile = fileChooser.getSelectedFile()) != null) {
						System.out.println("the selected file to be sent is "+actualDeviceFrame.mSelectedFile.getName());
						mSelectedFileTextView.setText("Selected File: "
								+ actualDeviceFrame.mSelectedFile.getName());
						mSendFileButton.setEnabled(true);
						mSelectedFileTextView.setBackground(Color.RED);
						System.out.println("Trying to set this fucccking backgroung to red color");
						this.repaint();
					}
					break;
				}
				case TAG_SEND_FILE_BUTTON: {
					if (actualDeviceFrame.mSelectedFile != null) {

						javax.sound.sampled.AudioFormat fmt = null;
						// read the wav file informations
						try {
							fmt = ((new WAVReader()).getAudioFileFormat(actualDeviceFrame.mSelectedFile))
									.getFormat();
						} catch (UnsupportedAudioFileException | IOException e1) {
							e1.printStackTrace();
						}

						int fs = (int) fmt.getSampleRate();
						int bps = fmt.getSampleSizeInBits() / 8;
						int channels = fmt.getChannels();

						WaveFileReader wavFileReader = new WaveFileReader();

						int duration = 0;
						try {
							AudioFileFormat audioFileFormat = wavFileReader
									.getAudioFileFormat(new URL("file:"
											+ actualDeviceFrame.mSelectedFile.getAbsolutePath()));
							duration = audioFileFormat.getFrameLength() / fs;
						} catch (UnsupportedAudioFileException | IOException e2) {
							e2.printStackTrace();
						}

						// Form the instruction and Send it to the client
						ReceiveStoredAudioInstruction receiveStoredAudioInstruction = new ReceiveStoredAudioInstruction(
								ServerAttributes.TCPAudioFileTransmitPort, fs,
								bps, channels, duration);
						actualDeviceFrame.mDevice.writeOrder(receiveStoredAudioInstruction
								.getFormattedJSONInstruction());
						// Wait until the concerned device prepare the receiving session
						try {
							Thread.sleep(300);
						} catch (InterruptedException e1) {

							e1.printStackTrace();
						}

						actualDeviceFrame.mDevice.buildStoredAudioSenderSocket();
						actualDeviceFrame.mDevice.luanchReaderFileAndSenderThread("file:"
								+ actualDeviceFrame.mSelectedFile.getAbsolutePath());
					}
					break;
				}
			}	
		}
	}//phonePlayingConfigurationPanel
	 
	 
	 private class InformationPanel extends JPanel{

			/**
			 * 
			 */
			private static final long serialVersionUID = 313291406639074019L;
			
			JLabel expText;
			JLabel deviceState;
			int expCount=0;
			

			public InformationPanel ( ) {
				//add the experience number TODO improve this information
				this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
				JPanel p= new JPanel();
				TitledBorder title = BorderFactory.createTitledBorder("Experience N°");
				p.setBorder(title);
				expText= new JLabel(""+expCount);
				p.add(expText);
				this.add(p);
				// add a status information to tell if the device is streaming or not 
				p=new JPanel();
				title = BorderFactory.createTitledBorder("Audio Straming status");
				p.setBorder(title);
				deviceState= new JLabel();
				deviceState.setBackground(Color.RED);
				deviceState.setOpaque(true);
				p.add(deviceState);
				this.add(p);
			}
			
			public void update(Color c) {
				this.expText.setText(""+expCount);
				this.deviceState.setBackground(c);
				this.repaint();
			}

		} //Information Panel class 
	
	
}//ParamInfoPanel Class

