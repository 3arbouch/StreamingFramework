package epfl.lcav.services;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.preference.PreferenceManager;

import epfl.lcav.activities.WaitingForInstructionsActivity;
import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.audio.SoundRecorderAndStream;


/**
 * This class is responsible of providing the service of recording audio from
 * the microphone and stream it to the server
 * 
 * @author MohamedBenArbia
 * 
 */
public class RecordAudioAndStreamService {

	private static SoundRecorderAndStream mSoundRecorderAndStream;

	/**
	 * This method responsible of streaming the audio recorded from the MIC
	 * 
	 * @param samplingRate
	 *            Sampling rate of the recorded audio
	 * @param bitsPerSample
	 *            Number of bits per sample
	 * @param channel
	 *            Number of audio channels
	 * @throws UnknownHostException
	 */
	public static void launchService(int samplingRate, int bitsPerSample,
			int channel, int payloadType, String identifier)
			throws UnknownHostException {
		// A little trick for channel
		switch (channel) {
		case 1:
			channel = AudioFormat.CHANNEL_IN_MONO;
			break;

		case 2:
			channel = AudioFormat.CHANNEL_IN_STEREO;
		default:
			break;
		}

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(WaitingForInstructionsActivity
						.getWaitingForInstructionContext());

		InetAddress serverIpAddress = InetAddress.getByName(sharedPref
				.getString(SharedAttributes.KEY_SERVER_ADDRESS, ""));
		int rtpPort = sharedPref.getInt(
				SharedAttributes.KEY_UDP_PORT_NUMBER_AUDIO, 0);
		if (!serverIpAddress.equals("") && rtpPort != 0) {
			mSoundRecorderAndStream = new SoundRecorderAndStream(false,
					serverIpAddress, rtpPort, samplingRate, bitsPerSample,
					channel, payloadType, identifier);
			Thread recordandStreamThread = new Thread(mSoundRecorderAndStream);
			recordandStreamThread.start();
		} else {
			throw new IllegalArgumentException();
		}

	}

	public static void stopService() {
		if (mSoundRecorderAndStream != null) {
			mSoundRecorderAndStream.setRecordAndStream(false);
			mSoundRecorderAndStream = null ; 
		}
	}

}
