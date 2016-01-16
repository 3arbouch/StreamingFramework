/* This file is based on 
 * http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * Please see the site for license information.
 */
package epfl.lcav.audio;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import jlibrtp.*;

/**
 * This thread is responsible of launching an RTP Session who receives the RTP
 * Packets from the server.
 * 
 * @author MohamedBenArbia
 * 
 */

public class AudioLiveReaderSavingAndPlayer implements RTPAppIntf, Runnable {

	AudioTrack mAudioTrack;

	private final static int DURATION = 120;

	private DatagramSocket rtpSocket = null;
	private DatagramSocket rtcpSocket = null;
	private int mSamplingRate;
	private int mBitsPerSample;
	private long mChannels;
	RTPSession rtpSession = null;

	private boolean initialized = false;
	DataOutputStream outFile;
	private boolean firstEntry = true;

	/**
	 * Access the received data from RTP Session through the frame. Access the
	 * participant details. Used to Write received audio file in SD Card. USed
	 * to play the received audio file.
	 */
	@Override
	public void receiveData(DataFrame frame, Participant p) {

		byte data[][] = frame.getData();

		
		if (firstEntry) {
			mAudioTrack.play();
			firstEntry = false;
		}

		mAudioTrack.write(data[0], 0, data[0].length);

		if (!initialized && p.getCNAME() != null) {
			try {
				initialize(p.getCNAME());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			initialized = true;
		}

		if (initialized) {

			try {
				outFile.write(data[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void initialize(String extension) throws FileNotFoundException {
		String outputURL = Environment.getExternalStorageDirectory().getPath()
				+ "/" + extension + ".wav";
		Log.d("FILE STORAGE", outputURL) ; 

		File dest = new File(outputURL);
		BufferedOutputStream bout = new BufferedOutputStream(
				new FileOutputStream(dest));
		outFile = new DataOutputStream(bout);
		int myDataSize = mSamplingRate * DURATION * 2;
		long mySubChunk1Size = mBitsPerSample;
		int myBitsPerSample = mBitsPerSample;
		int myFormat = 1;
		long myChannels = mChannels;
		long mySampleRate = mSamplingRate;
		long myByteRate = mySampleRate * myChannels * myBitsPerSample / 8;
		int myBlockAlign = (int) (myChannels * myBitsPerSample / 8);
		long myChunk2Size = myDataSize * myChannels * myBitsPerSample / 8;
		long myChunkSize = 36 + myChunk2Size;

		try {
			outFile.writeBytes("RIFF");
			// 04 - how big is the rest of this file?
			outFile.write(intToByteArray((int) myChunkSize), 0, 4);
			outFile.writeBytes("WAVE"); // 08 - WAVE
			outFile.writeBytes("fmt "); // 12 - fmt
			// 16 - size of this chunk
			outFile.write(intToByteArray((int) mySubChunk1Size), 0, 4);
			outFile.write(shortToByteArray((short) myFormat), 0, 2); // 20 -
																		// what
																		// is
																		// the
																		// audio
																		// format?
																		// 1 for
																		// PCM =
																		// Pulse
																		// Code
																		// Modulation
			outFile.write(shortToByteArray((short) myChannels), 0, 2); // 22 -
																		// mono
																		// or
																		// stereo?
																		// 1 or
																		// 2?
																		// (or 5
																		// or
																		// ???)
			outFile.write(intToByteArray((int) mySampleRate), 0, 4); // 24 -
																		// samples
																		// per
																		// second
																		// (numbers
																		// per
																		// second)
			outFile.write(intToByteArray((int) myByteRate), 0, 4); // 28 - bytes
																	// per
																	// second
			outFile.write(shortToByteArray((short) myBlockAlign), 0, 2); // 32 -
																			// #
																			// of
																			// bytes
																			// in
																			// one
																			// sample,
																			// for
																			// all
																			// channels
			outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2); // 34
																			// -
																			// how
																			// many
																			// bits
																			// in
																			// a
																			// sample(number)?
																			// usually
																			// 16
																			// or
																			// 24
			outFile.writeBytes("data"); // 36 - data
			outFile.write(intToByteArray(myDataSize), 0, 4);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		} // 00 - RIFF
	}

	private static byte[] intToByteArray(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);
		return b;
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short data) {
		/*
		 * NB have also tried: return new byte[]{(byte)(data &
		 * 0xff),(byte)((data >> 8) & 0xff)};
		 */

		return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
	}

	@Override
	public void userEvent(int type, Participant[] participant) {
		// Do nothing
	}

	@Override
	public int frameSize(int payloadType) {
		return 1;
	}

	public AudioLiveReaderSavingAndPlayer(int rtpPort, int rtcpPort,
			int samplingRate, int bitsPerSample, long channels) {
		this.mSamplingRate = samplingRate;
		this.mBitsPerSample = bitsPerSample;
		this.mChannels = channels;
		try {
			this.rtpSocket = new DatagramSocket(rtpPort);
			this.rtcpSocket = new DatagramSocket(rtcpPort);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}

		int bufferSize = AudioTrack.getMinBufferSize(mSamplingRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSamplingRate,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize,
				AudioTrack.MODE_STREAM);

	}

	public int getmSamplingRate() {
		return mSamplingRate;
	}

	public int getmBitsPerSample() {
		return mBitsPerSample;
	}

	/**
	 * @param args
	 */
	@Override
	public void run() {

		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.naivePktReception(true);
		rtpSession.RTPSessionRegister(this, null, null);
		System.out.println("Done");
	}

	public void stopSession() {

		this.rtpSession.endSession();
	}

}
