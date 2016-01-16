package epfl.lcav.model;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Observable;

import epfl.lcav.attributes.SharedAttributes;
import epfl.lcav.view.DeviceFrame;

public class AudioDisplayerThread implements Runnable {

	private PipedInputStream pipeInput;
	private Samples samplesAmplitudes;
	private SamplesView sampleViewAmplitudes;
	/**
	 * down-sampling rate
	 */
	final int D=3;
	private  int  chunkSize;
	private  int  chunkToDispSize;

	private Samples samplesDFT;
	private SamplesView sampleViewDFT;



	private short[] previousData1; 
	private short[] previousData2;
	private short[] previousData3;
	
	private FFTCompute ffTCompute;

	public AudioDisplayerThread(PipedOutputStream output,
			DeviceFrame relatedDeviceFrame, int availableArea) {
		try {
			
			this.chunkToDispSize=D*(availableArea-120);
			this.chunkSize=chunkToDispSize/4;
			
			previousData1 = new short[chunkSize];
			previousData2 = new short[chunkSize];
			previousData3 = new short[chunkSize];
			
			pipeInput = new PipedInputStream(output,
					10 * SharedAttributes.BUFFER_SIZE);
			samplesAmplitudes = new Samples(chunkToDispSize, 0);
			sampleViewAmplitudes = new SamplesView(samplesAmplitudes, availableArea-100,D);
			
			samplesDFT = new Samples(chunkToDispSize, 0);
			sampleViewDFT = new SamplesView(samplesDFT,availableArea-100,D);
			ffTCompute = new FFTCompute();
			ffTCompute.setFft(chunkToDispSize);
			relatedDeviceFrame.getmRealTimeAmplitudeAudioDataDisplayer().add(
					sampleViewAmplitudes);
			relatedDeviceFrame.getmRealTimeFourrierAudioDataDisplayer().add(
					sampleViewDFT);



		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		byte[] buffer = new byte[2*chunkSize];
		short[] bufferShort = new short[chunkSize];
		short[] dataToDisplay = new short[chunkToDispSize];
		int bytesRead = 0;

		try {
			
			while ((bytesRead = pipeInput.read(buffer, 0, buffer.length)) != -1) {
				

				System.arraycopy(previousData1, 0, dataToDisplay, 0,
						previousData1.length);
				System.arraycopy(previousData2, 0, dataToDisplay,
						previousData2.length, previousData2.length);
				System.arraycopy(previousData3, 0, dataToDisplay,
						2 * previousData3.length, previousData3.length);

				for (int i = 0; i < bufferShort.length; i++) {
					byte[] temp = { buffer[2 * i], buffer[2 * i + 1] };
					bufferShort[i] = toShort(temp);
				}
				System.arraycopy(previousData2, 0, previousData1, 0,
						previousData1.length);
				System.arraycopy(previousData3, 0, previousData2, 0,
						previousData2.length);
				System.arraycopy(bufferShort, 0, previousData3, 0,
						previousData3.length);
				System.arraycopy(bufferShort, 0, dataToDisplay,
						3 * bufferShort.length, previousData3.length);
				
				float values[] = samplesAmplitudes
						.updateSampleFromRealTimeAudioData(dataToDisplay);
				sampleViewAmplitudes.repaint();

				samplesDFT.updateSampleFromRealTimeAudioData(ffTCompute.flipData(ffTCompute
						.computeAmplitude(ffTCompute.apply(values))));

				sampleViewDFT.repaint();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static short toShort(byte[] bytes) {

		return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	class Samples extends Observable {

		public float values[];
		public int origin;

		public Samples(int length, int origin) {
			this.origin = origin;
			values = new float[length];
			zero();
		}


		public void zero() {
			for (int i = 0; i < this.values.length; ++i)
				this.values[i] = 0.0f;
		}

		public float[] updateSampleFromRealTimeAudioData(short[] data) {

			for (int i = 0; i < data.length; i++) {

				this.values[i] = (float) ((float) data[i] / Math.pow(2, 15));
			}

			return this.values;

		}
		
		
		public float[] updateSampleFromRealTimeAudioData(float[] data) {

			for (int i = 0; i < data.length; i++) {

				this.values[i] =  (float) (data[i] /128); 
			}

			return this.values;

		}


		public void notifyObservers() {
			setChanged();
			super.notifyObservers();
		}
	}

	class SamplesView extends Canvas {

		/**
		 * serialization number 
		 */
		private static final long serialVersionUID = 1L;

		private int sampleStart, sampleBase, sampleWidth, sampleRadius;
		private float sampleScale, sampleValue;

		/**
		 * Down-sampling rate 
		 */
		int dsp;
		/**
		 * represents the number of samples that have been escaped from the display  
		 */
		int escaped=0;

		
		int areaWidth;
		final int areaHeight=280;
		
		public Samples samples;

		public SamplesView(Samples s, int panelSize, int d ) {
			this.dsp=d;
			samples = s;
			this.areaWidth= panelSize;
			System.out.println("area in which we will display the signals "+ panelSize);
			setSampleValue(1.0f);
			setSize(this.areaWidth, areaHeight);
			updateDrawingSizes();
			setBackground(new Color(224,255,255));
		}

		public void updateViewFromSamples(Samples s) {
			this.samples = s;
			updateDrawingSizes();
		}

		public void setSampleValue(float v) {
			int height = size().height;
			sampleValue = (v != 0.0f) ? v : 1.0f;
			sampleScale = -0.2f * height / sampleValue;
		}



		public void paint(Graphics g) {
			g.setColor(new Color(139,0,0)) ; 
			updateDrawingSizes();
			drawSamples(g);
		}

		public Dimension minimumSize() {
			return new Dimension(this.areaWidth, this.areaHeight);
		}

		public Dimension preferredSize() {
			return minimumSize();
		}


		
		
		private void drawOneSample(Graphics g, int i) {
			if (i%dsp != 0) {
				escaped++;	
			}
			else {
			int x = sampleStart + i - escaped;
			int y = sampleBase;

			int w = sampleWidth;
			int h = (int) (samples.values[i] * sampleScale);
          
			g.drawLine(x - w / 2, y, x + w / 2, y);
			g.drawLine(x, y, x, y + h);}

		}

		private void drawSamples(Graphics g) {
			for (int i = 0; i < samples.values.length; ++i) {
				drawOneSample(g, i);
			}
			escaped=0;
		}

		
		private void updateDrawingSizes() {

			int height = this.areaHeight;

			sampleWidth = 0;
			sampleStart = 10;
			sampleBase = (int) (0.5f * height);
			sampleScale = -0.25f * height / sampleValue;
			sampleRadius = (int) (0.4f * sampleWidth);
			int maxRadius = (int) (0.5f * height);
			if (sampleRadius > maxRadius)
				sampleRadius = maxRadius;
		}
	}

}
