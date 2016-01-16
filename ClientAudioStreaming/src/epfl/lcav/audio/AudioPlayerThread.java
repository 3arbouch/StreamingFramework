package epfl.lcav.audio;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Thread responsible of playing audio received from the server
 * 
 * @author MohamedBenArbia
 * 
 */
public class AudioPlayerThread implements Runnable {

	private static final String TAG = "AUDIO PLAYER THREAD";

	/**
	 * Number of frames that the client will read each time
	 */
	private static final int NUMBER_OF_READING_FRAMES = 5;

	/**
	 * Pipe through which the data will be read
	 */
	private PipedInputStream input;
	/**
	 * Audio track that will push data to speakers
	 */
	AudioTrack mAudioTrack;

	private int mBitsPerSample;

	public AudioPlayerThread(PipedOutputStream output, int size,
			int samplingRate, int bitsPerSample, int channels)
			throws IOException {

		input = new PipedInputStream(output, size);
		this.mBitsPerSample = bitsPerSample;

		if (bitsPerSample == 16) {
			bitsPerSample = AudioFormat.ENCODING_PCM_16BIT;
		} else if (bitsPerSample == 8) {
			bitsPerSample = AudioFormat.ENCODING_PCM_8BIT;
		}

		if (channels == 1) {
			channels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		} else if (channels == 2) {
			channels = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		}

		int bufferSize = AudioTrack.getMinBufferSize(samplingRate, channels,
				bitsPerSample);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate,
				channels, bitsPerSample, bufferSize, AudioTrack.MODE_STREAM);

	}

	@Override
	public void run() {
		Log.d(TAG, "RUN METHOD");
		mAudioTrack.play();

		// byte[] buffer = new byte[10];
		byte[] buffer = new byte[32];

		int bytesRead = 0;

		int totalbytes = 0;

		try {
			while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {

				

					mAudioTrack.write(buffer, 0, bytesRead);
					mAudioTrack.flush() ; 
					totalbytes = totalbytes + bytesRead;
				

			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Log.d(TAG, "TOTAL NUMBER OF BYTES READ FROM THE PIPE :  "
					+ totalbytes);

			mAudioTrack.stop();
			input.close();
		} catch (IOException e) {
			// mAudioTrack.stop() ;

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
