package epfl.lcav.audio;

/* This file is based on 
 * http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * Please see the site for license information.
 */

import java.lang.String;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import jlibrtp.*;

/**
 * @author Arne Kepp
 */
public class SoundRecorderAndStream implements RTPAppIntf , Runnable{

	private static final String DEBUG_TAG = "SOUNDRECORDERANDSTREAM";
	private static final int SAMPLE_INTERVAL = 20; // milliseconds
  
	private InetAddress serverIpAddress;
	private int rtpPort;
	private int samplingRate;
	private int bitsPerSample;
	private int channel;
	private int payloadType;
	private int bufferSize;
	private String identifier ; 
    public static  int timeStamp ; 
	private boolean recordAndStream = true;

	public RTPSession rtpSession = null;
	static int pktCount = 0;
	static int dataCount = 0;

	boolean local;



	public SoundRecorderAndStream(boolean isLocal, InetAddress serverIpAddress,
			int rtpPort, int samplingRate, int bitsPerSample, int channel, int payloadType, String identifier) {

		this.serverIpAddress = serverIpAddress;
		this.rtpPort = rtpPort;
		this.samplingRate = samplingRate;
		this.bitsPerSample = bitsPerSample;
		this.channel = channel;
		this.payloadType = payloadType ; 
		this.identifier = identifier ; 
		this.bufferSize = (SAMPLE_INTERVAL * this.samplingRate * (this.bitsPerSample / 8)) / 1000;
		timeStamp = (SAMPLE_INTERVAL*this.samplingRate)/1000 ; 
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;

		try {
			rtpSocket = new DatagramSocket(this.rtpPort);
			rtcpSocket = new DatagramSocket(this.rtpPort + 1);
		} catch (Exception e) {
			Log.d(DEBUG_TAG, "RTPSession failed to obtain port");
		}

		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.RTPSessionRegister(this, null, null);
		this.local = isLocal;

		
		Participant p = new Participant(this.serverIpAddress.getHostAddress(),
				this.rtpPort, this.rtpPort + 1);
		this.rtpSession.addParticipant(p);
		
	}

	/**
	 * @param args
	 */

	@Override
	public void receiveData(DataFrame dummy1, Participant dummy2) {
		// We don't expect any data.
	}

	@Override
	public void userEvent(int type, Participant[] participant) {
		// Do nothing
	}

	@Override
	public int frameSize(int payloadType) {
		return 1;
	}

	@Override
	public void run() {
		if (RTPSession.rtpDebugLevel > 1) {
			System.out.println("-> Run()");
		}

		if (this.bitsPerSample == 16) {
			this.bitsPerSample = AudioFormat.ENCODING_PCM_16BIT;
		}
		AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				this.samplingRate, this.channel, this.bitsPerSample,
				AudioRecord.getMinBufferSize(this.samplingRate, this.channel,
						this.bitsPerSample) * 10);
		Log.d("Debug stoping thread", "Recorder State" +recorder.getState()) ; 
		
		recorder.startRecording();
		int nBytesRead = 0;
		byte[] abData = new byte[this.bufferSize];
		Log.d("Debug stoping thread", "start recording session");
		while (nBytesRead != -1 && this.recordAndStream) {
			nBytesRead = recorder.read(abData, 0, abData.length);

			if (nBytesRead >= 0) {
                rtpSession.CNAME(this.identifier);
				//rtpSession.SSRC(Long.valueOf(this.identifier));
				rtpSession.payloadType(this.payloadType);
				rtpSession.sendData(abData);
				pktCount++;
			}

		}

		try {
			Thread.sleep(200);
		} catch (Exception e) {
		}
		Log.d("Debug stoping thread", "end session");
	
		this.rtpSession.endSession();
		recorder.stop();

		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		if (RTPSession.rtpDebugLevel > 1) {
			System.out.println("<- Run()");
		}
	}

	public void setRecordAndStream(boolean recordAndStream) {
		this.recordAndStream = recordAndStream;
	}

}
