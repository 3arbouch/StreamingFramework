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

import java.io.IOException;

import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

import java.util.Vector;
import java.util.Enumeration;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.datasink.*;
import epfl.lcav.view.MainView;

/**
 * This DataSourceHandler class reads from a DataSource and display information
 * of each frame of data received.
 */
public class DataSourceAnalyzer implements DataSink, BufferTransferHandler {

	DataSource source;
	PullBufferStream pullStrms[] = null;
	PushBufferStream pushStrms[] = null;
	String identifyer;
	private PipedOutputStream outputPipe;
	private AudioDisplayerThread audioDisplayerThread;
	boolean firstEntry = true;
	private int count = 0;

	public DataSourceAnalyzer(String part) {
		this.identifyer = part;
		outputPipe = new PipedOutputStream();
		audioDisplayerThread = new AudioDisplayerThread(outputPipe,
				MainView.deviceFrameHashMap.get(part),
				MainView.deviceFrameHashMap.get(part).availableDispAreaSize);

	}

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
	 * Does nothing since we will analyze the data in real time
	 * 
	 */
	public void open() {

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

		// Start the processing loop if we are dealing with a
		// PullBufferDataSource.
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].pause();
		}
	}

	public void close() { // clean-up
		stop();
		if (loops != null) {
			for (int i = 0; i < loops.length; i++)
				loops[i].kill();
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

	void printDataInfo(Buffer buffer) {

		byte[] data1 = ((byte[]) buffer.getData());

		try {

			this.outputPipe
					.write(data1, buffer.getOffset(), buffer.getLength());
			this.outputPipe.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (count == 5) {

			Thread t = new Thread(audioDisplayerThread);
			t.start();
			firstEntry = false;
		}

		count++;

	}

	public static short toShort(byte[] bytes) {

		return ByteBuffer.wrap(bytes).getShort();
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

		DataSourceAnalyzer handler;
		PullBufferStream stream;
		boolean paused = true;
		boolean killed = false;

		public Loop(DataSourceAnalyzer handler, PullBufferStream stream) {
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

}
