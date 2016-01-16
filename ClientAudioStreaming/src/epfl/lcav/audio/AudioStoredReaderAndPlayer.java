package epfl.lcav.audio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class AudioStoredReaderAndPlayer implements Runnable {

	private int mPort ; 
	private int mSamplingRate ; 
	private int mBitsPerSample ; 
	private int mChannels ; 
	private int mDuration ; 
	
	public AudioStoredReaderAndPlayer(int port , int samplingRate,
			int bitsPerSample, long channels, int duration ) {
		
		this.mPort = port ; 
		this.mSamplingRate = samplingRate ; 
		this.mBitsPerSample = bitsPerSample ; 
		this.mChannels = (int)channels  ; 
		this.mDuration = duration ; 
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		Log.d("ReceivingData Thread","HEEEREEE") ;
		int numberOfReceivedStreams = 0  ; 
		ServerSocket serverSocket = null;
		try {
		 serverSocket = new ServerSocket(this.mPort) ;
			
			while (numberOfReceivedStreams<1) {
			
		         Socket connectedSocekt  = serverSocket.accept() ; 
		         AudioDataReceivingThread audioDataReceivingThread = new AudioDataReceivingThread(connectedSocekt, mSamplingRate, mBitsPerSample, mChannels, mDuration) ; 
		         Thread t = new Thread(audioDataReceivingThread) ; 
		         t.start() ; 
		         numberOfReceivedStreams ++ ; 
		         
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		
			try {
				serverSocket.close() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		
	}


}
