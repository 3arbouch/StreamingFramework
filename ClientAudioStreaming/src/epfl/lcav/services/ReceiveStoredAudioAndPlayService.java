package epfl.lcav.services;

import epfl.lcav.audio.AudioStoredReaderAndPlayer;

public class ReceiveStoredAudioAndPlayService {

	private static AudioStoredReaderAndPlayer mAudioStoredReaderAndPlayer;
    private static Thread readerAndPlayerThread ; 
	public static void launchService(int port , int samplingRate, int bitsPerSample,
			long channels, int duration) {
		mAudioStoredReaderAndPlayer = new AudioStoredReaderAndPlayer(port,
				samplingRate, bitsPerSample, channels, duration);
		readerAndPlayerThread = new Thread(mAudioStoredReaderAndPlayer);
		readerAndPlayerThread.start();
	}

	
	
}
