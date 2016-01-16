package epfl.lcav.audio;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import epfl.lcav.attributes.SharedAttributes;

import android.util.Log;

/**
 * Thread responsible of reading the audio data received from the server.
 * Construct a pipe (buffer) that stores the received data and pass it to the
 * palyer thread that plays the audio.
 * 
 * @author MohamedBenArbia
 * 
 */
public class AudioDataReceivingThread implements Runnable {
	
	private static final String TAG = "AUDIO RECEIVING THREAD";

	/**
	 * The client will start playing audio after START_TIME_IN_SECONDS of received audio data
	 * This value is calculated from the length of the received audio file
	 */
	private static  int START_TIME_IN_SECONDS = 0;
	/**
	 * Number of frames that the client will read each time
	 */
	private static final int NUMBER_OF_READING_FRAMES = 10;

	/**
	 * Pipe responsible of pushing the data to the reader Thread
	 */
	private PipedOutputStream output = new PipedOutputStream();

	/**
	 * The thread responsible of reading the audio puhsed by the
	 * pipeOutputStream
	 */
	private AudioPlayerThread pl;

	/**
	 * Sampling rate of the received audio data
	 */
	private int mSamplingRate;
	/**
	 * Bits per samples
	 */
	private int mBitsPerSample;

	
	/**
	 * Number of channel
	 */
	private int mCahnnels;

	
	/**
	 * Socket trough which the data is received
	 */
	private Socket mSocekt;
	
	/**
	 * Duration if the audio file to play 
	 */
	private int  mDuration ; 

	/**
	 * Construct the thread responsible of reading the data received from the server 
	 * @param socket Socket through which the data will be read
	 * @param samplingRate Sampling rate of the received audio data
	 * @param bitsPerSample Bits per sample of the received audio data
	 * @throws IOException
	 */
	public AudioDataReceivingThread(Socket socket, int samplingRate,
			int bitsPerSample, int channels, int duration) throws IOException {
		this.mSocekt = socket;
		this.mSamplingRate = samplingRate;
		this.mBitsPerSample = bitsPerSample ; 
		this.mCahnnels = channels ; 
		START_TIME_IN_SECONDS = duration/4 ; 
		pl = new AudioPlayerThread(output, SharedAttributes.BUFFER_SIZE, samplingRate, bitsPerSample, mCahnnels)  ;

	}

	@Override
	public void run() {
		Log.d(TAG, "Reader Thread Luanched");

		try {

			DataInputStream data = new DataInputStream(mSocekt.getInputStream());
			int bytesRead = 0;
			//byte[] buffer = new byte[20];
			byte[] buffer = new byte[64];
			int round = 0;
			int total = 0 ; 
			
			while ((bytesRead = data.read(buffer, 0, buffer.length)) != -1) {

				round++;
				total = total + bytesRead ; 
				if (round == Math.floor(mSamplingRate * START_TIME_IN_SECONDS
						/ (64))) {
					Log.d(TAG, "THREAD LAUNCHED AFTER AROUND"+ START_TIME_IN_SECONDS +"S OF DATA");
					Thread t = new Thread(pl);
					t.start();
				}

				output.write(buffer, 0, bytesRead);
				output.flush();

			}
			
			Log.d(TAG, "Total number of received bytes: "+total) ;


		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				output.close();
				mSocekt.close() ; 
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}

	}
}
