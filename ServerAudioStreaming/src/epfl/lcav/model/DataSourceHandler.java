package epfl.lcav.model;

/*
 * @(#)DataSourceReader.java	1.2 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.datasink.*;

/**
 * This DataSourceHandler class reads from a DataSource and writes the received
 * data to a file.
 * WAVE file are created according to Microsoft RIFF http://netghost.narod.ru/gff/graphics/summary/micriff.htm
 * and http://www.lightlink.com/tjweber/StripWav/WAVE.html
 */
public class DataSourceHandler implements DataSink, BufferTransferHandler {
	
	/**
	 * duration in seconds of the WAVE file 
	 */
	private static final int DURATION = 120;
	/**
	 * sampling rate of the received data 
	 */
	private int samplingRate;
	/**
	 * number of bits per sample
	 */
	private int bitsPerSample;
	/**
	 * number of channels 
	 */
	private long channels;
	/**
	 * data source of the received stream
	 */
	DataSource source;
	
	PullBufferStream pullStrms[] = null;
	PushBufferStream pushStrms[] = null;

	// Data sink listeners.
	private Vector listeners = new Vector(1);

	// Stored all the streams that are not yet finished (i.e. EOM
	// has not been received.
	SourceStream unfinishedStrms[] = null;

	// Loop threads to pull data from a PullBufferDataSource.
	// There is one thread per each PullSourceStream.
	Loop loops[] = null;

	Buffer readBuffer;
	/**
	 * absolute path to the file where the data will be saved
	 */
	private String outputURL;
	/**
	 * destination audio file 
	 */
	private File dest;
	/**
	 * output stream to write into the file 
	 */
	private BufferedOutputStream bout;
	/**
	 * utility attribute to help writing the wav file
	 */
	private DataOutputStream outFile;
	/**
	 * creates a data source handles with null input data source. Use setSource() to configure the input data source
	 * @param out absolute path to the destination file 
	 * @param samplingRate sampling rate of the received signal
	 * @param bitsPerSample number of bits per sample
	 * @param channels number of channels
	 */
	public DataSourceHandler(String out, int samplingRate, int bitsPerSample,
			long channels) {
		System.out.println("RECEIVED ARGUMENTS: " + "SAMPLING RATE: "
				+ samplingRate + " BITS PER SAMPLE " + bitsPerSample
				+ " CHANNELS: " + channels);
		this.samplingRate = samplingRate;
		this.bitsPerSample = bitsPerSample;
		this.channels = channels;
		this.outputURL = out;
	}

	/**
	 * Sets the media source this <code>MediaHandler</code> should use to obtain
	 * content.
	 */
	public void setSource(DataSource source) throws IncompatibleSourceException {

		// Different types of DataSources need to handled differently.
		if (source instanceof PushBufferDataSource) {

			pushStrms = ((PushBufferDataSource) source).getStreams();
			unfinishedStrms = new SourceStream[pushStrms.length];

			// Set the transfer handler to receive pushed data from
			// the push DataSource.
			for (int i = 0; i < pushStrms.length; i++) {
				pushStrms[i].setTransferHandler(this);
				unfinishedStrms[i] = pushStrms[i];
			}

		} else if (source instanceof PullBufferDataSource) {

			pullStrms = ((PullBufferDataSource) source).getStreams();
			unfinishedStrms = new SourceStream[pullStrms.length];

			// For pull data sources, we'll start a thread per
			// stream to pull data from the source.
			loops = new Loop[pullStrms.length];
			for (int i = 0; i < pullStrms.length; i++) {
				loops[i] = new Loop(this, pullStrms[i]);
				unfinishedStrms[i] = pullStrms[i];
			}

		} else {

			// This handler only handles push or pull buffer datasource.
			throw new IncompatibleSourceException();

		}

		this.source = source;
		readBuffer = new Buffer();
	}

	/**
	 * For completeness, DataSink's require this method. But we don't need it.
	 */
	public void setOutputLocator(MediaLocator ml) {
	}

	public MediaLocator getOutputLocator() {
		return null;
	}

	public String getContentType() {
		return source.getContentType();
	}

	/**
	 * opens the file to which the data will be saved and adds the WAVE header on the beginning of the file.
	 * @throws FileNotFoundException
	 */
	public void open() throws FileNotFoundException {
		this.dest = new File(outputURL);
		this.bout = new BufferedOutputStream(new FileOutputStream(this.dest));
		outFile = new DataOutputStream(bout);
		int myDataSize = DURATION * this.samplingRate
				* (this.bitsPerSample / 8);
		long mySubChunk1Size = this.bitsPerSample;
		int myBitsPerSample = this.bitsPerSample;
		int myFormat = 1;
		long myChannels = this.channels;
		long mySampleRate = this.samplingRate;
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
			// 20 - what is the audio format? 1 for PCM =Pulse Code Modulation
			outFile.write(shortToByteArray((short) myFormat), 0, 2); 
			// 22 -  mono or stereo? 1 or 2? (or 5 or ???)
			outFile.write(shortToByteArray((short) myChannels), 0, 2); 
			// 24 - samples per second (numbers per second)
			outFile.write(intToByteArray((int) mySampleRate), 0, 4);
			// 28 - bytes per second
			outFile.write(intToByteArray((int) myByteRate), 0, 4); 
			// 32 -  # of bytes in one sample, for all channels
			outFile.write(shortToByteArray((short) myBlockAlign), 0, 2);
			// 34 - how many bits in a sample(number)? usually 16 or 24
			outFile.write(shortToByteArray((short) myBitsPerSample), 0, 2); 
			outFile.writeBytes("data"); // 36 - data
			outFile.write(intToByteArray((int) myDataSize), 0, 4);

		} catch (IOException e) {
			e.printStackTrace();
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

	/**
	 *  convert a short to a byte array
	 * @param data
	 * @return the corresponding byte array
	 */
	public static byte[] shortToByteArray(short data) {
		/*
		 * NB have also tried: return new byte[]{(byte)(data &
		 * 0xff),(byte)((data >> 8) & 0xff)};
		 */

		return new byte[] { (byte) (data & 0xff), (byte) ((data >>> 8) & 0xff) };
	}


	public void start() {
		try {
			source.start();
		} catch (IOException e) {
			System.err.println(e);
		}

		// Start the processing loop if we are dealing with a
		// PullBufferDataSource.
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].restart();
		}
	}

	public void stop() {
		try {
			source.stop();
		} catch (IOException e) {
			System.err.println(e);
		}

		// Stop the processing loop if we are dealing with a
		// PullBufferDataSource.
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].pause();
		}
	}

	public void close() { // clean-up
		// first stop all processes
		stop();
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].kill();
		}
		// then close all streams
		try {
			this.bout.close();
			this.outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void addDataSinkListener(DataSinkListener dsl) {
		if (dsl != null)
			if (!listeners.contains(dsl))
				listeners.addElement(dsl);
	}

	public void removeDataSinkListener(DataSinkListener dsl) {
		if (dsl != null)
			listeners.removeElement(dsl);
	}

	protected void sendEvent(DataSinkEvent event) {
		if (!listeners.isEmpty()) {
			synchronized (listeners) {
				Enumeration list = listeners.elements();
				while (list.hasMoreElements()) {
					DataSinkListener listener = (DataSinkListener) list
							.nextElement();
					listener.dataSinkUpdate(event);
				}
			}
		}
	}

	/**
	 * This will get called when there's data pushed from the
	 * PushBufferDataSource.
	 */
	public void transferData(PushBufferStream stream) {

		try {
			stream.read(readBuffer);
		} catch (IOException e) {
			System.err.println(e);
			sendEvent(new DataSinkErrorEvent(this, e.getMessage()));
			return;
		}

		printDataInfo(readBuffer);

		// Check to see if we are done with all the streams.
		if (readBuffer.isEOM() && checkDone(stream)) {
			sendEvent(new EndOfStreamEvent(this));
		}
	}

	/**
	 * This is called from the Loop thread to pull data from the
	 * PullBufferStream.
	 */
	public boolean readPullData(PullBufferStream stream) {
		try {
			stream.read(readBuffer);
		} catch (IOException e) {
			System.err.println(e);
			return true;
		}

		printDataInfo(readBuffer);

		if (readBuffer.isEOM()) {
			// Check to see if we are done with all the streams.
			if (checkDone(stream)) {
				System.err.println("All done!");
				close();
			}
			return true;
		}
		return false;
	}

	/**
	 * Check to see if all the streams are processed.
	 */
	public boolean checkDone(SourceStream strm) {
		boolean done = true;

		for (int i = 0; i < unfinishedStrms.length; i++) {
			if (strm == unfinishedStrms[i])
				unfinishedStrms[i] = null;
			else if (unfinishedStrms[i] != null) {
				// There's at least one stream that's not done.
				done = false;
			}
		}
		return done;
	}

	/**
	 * reads from the buffer and writes into the file 
	 * @param buffer input buffer that contains the received data
	 */
	void printDataInfo(Buffer buffer) {

		byte[] data1 = ((byte[]) buffer.getData());

		try {

			this.outFile.write(data1, buffer.getOffset(), buffer.getLength());
			this.outFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (buffer.isEOM())
			System.err.println("  Got EOM!");
	}

	public Object[] getControls() {
		return new Object[0];
	}

	public Object getControl(String name) {
		return null;
	}

	/**
	 * A thread class to implement a processing loop. This loop reads data from
	 * a PullBufferDataSource.
	 */
	class Loop extends Thread {

		DataSourceHandler handler;
		PullBufferStream stream;
		boolean paused = true;
		boolean killed = false;

		public Loop(DataSourceHandler handler, PullBufferStream stream) {
			this.handler = handler;
			this.stream = stream;
			start();
		}

		public synchronized void restart() {
			paused = false;
			notify();
		}

		/**
		 * This is the correct way to pause a thread; unlike suspend.
		 */
		public synchronized void pause() {
			paused = true;
		}

		/**
		 * This is the correct way to kill a thread; unlike stop.
		 */
		public synchronized void kill() {
			killed = true;
			notify();
		}

		/**
		 * This is the processing loop to pull data from a PullBufferDataSource.
		 */
		public void run() {
			while (!killed) {
				try {
					while (paused && !killed) {
						wait();
					}
				} catch (InterruptedException e) {
				}

				if (!killed) {
					boolean done = handler.readPullData(stream);
					if (done)
						pause();
				}
			}
		}
	}

	public String toString() {
		return "Datasink writing to file: " + this.dest;
	}

}
