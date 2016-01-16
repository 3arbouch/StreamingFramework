package epfl.lcav.services;

import epfl.lcav.audio.AudioLiveReaderSavingAndPlayer;

/**
 * This service is responsible of receiving RTP audio Packet from the server: save and play the audio received
 * @author MohamedBenArbia
 *
 */
public class ReceiveLiveAudioStoreAndPlaySercive {

	private static AudioLiveReaderSavingAndPlayer mSoundReaderAndSaving;
    private static Thread readerAndSavingThread ; 
	public static void launchService(int samplingRate, int bitsPerSample,
			long channels) {
		mSoundReaderAndSaving = new AudioLiveReaderSavingAndPlayer(9090, 9091,
				samplingRate, bitsPerSample, channels);
		readerAndSavingThread = new Thread(mSoundReaderAndSaving);
		readerAndSavingThread.start();
	}

	public static void stopService() {
		if (mSoundReaderAndSaving != null && readerAndSavingThread!=null) {
			mSoundReaderAndSaving.stopSession();
			mSoundReaderAndSaving = null ; 
			
		
		}
	}

}
