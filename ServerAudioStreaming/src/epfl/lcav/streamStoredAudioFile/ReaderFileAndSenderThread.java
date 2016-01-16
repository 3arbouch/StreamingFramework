package epfl.lcav.streamStoredAudioFile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import com.sun.media.sound.WaveFileReader;

public class ReaderFileAndSenderThread implements Runnable {

	private Socket mSocket;
	private OutputStream mOutputStream;
	private AudioInputStream audioInputStream;

	public ReaderFileAndSenderThread(Socket socket, String path)
			throws IOException {
		this.mSocket = socket;

		WaveFileReader wavFileReader = new WaveFileReader();
		try {
			mOutputStream = socket.getOutputStream();

			AudioFileFormat audioFileFormat = wavFileReader
					.getAudioFileFormat(new URL(path));
			this.audioInputStream = wavFileReader.getAudioInputStream(new URL(
					path));

			System.out.println("Format: " + audioFileFormat.getFormat()
					+ " **************** FRAME LENTGH********* "
					+ audioInputStream.getFrameLength());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Override
	public void run() {
		DataOutputStream data = new DataOutputStream(mOutputStream);

		try {
			int totalNumberOfBytes = 0;
			int nBytesRead = 0;
			byte[] buffer = new byte[128];
			byte[] buffer2 = new byte[128];

			data.write(buffer2, 0, buffer2.length);
			data.flush();

			while ((nBytesRead = audioInputStream
					.read(buffer, 0, buffer.length)) != -1) {

				totalNumberOfBytes = totalNumberOfBytes + nBytesRead;

				data.write(buffer, 0, nBytesRead);
				data.flush();

			}

			data.write(buffer2, 0, buffer2.length);
			data.flush();
			
			System.out.println("The total number of  readead bytes is : "
					+ totalNumberOfBytes);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
