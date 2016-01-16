package epfl.lcav.model;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FFTCompute {
	private FloatFFT_1D fft = new FloatFFT_1D(2048);

	public float[] apply(float[] params) {
		float[] output = new float[params.length * 2];
		System.arraycopy(params, 0, output, 0, params.length);
		fft.realForwardFull(output);

		return output;
	}

	
	
	
	public void setFft(int chunkSize) {
		this.fft =  new FloatFFT_1D(chunkSize);
	}




	public float[] computeAmplitude(float [] DFTRealAndImaginaryData) {
		float[] amplitudes = new float[DFTRealAndImaginaryData.length/2] ; 
		
		for (int i = 0; i < amplitudes.length; i++) {
			amplitudes[i] = (float) Math.sqrt(Math.pow(DFTRealAndImaginaryData[2*i], 2)+Math.pow(DFTRealAndImaginaryData[2*i+1], 2)) ; 
		}
		return amplitudes ; 
	}
	
	public float[] flipData (float [] data) {
		float [] dataFlipped = new float [data.length] ; 
		System.arraycopy(data, 0,dataFlipped, dataFlipped.length/2, data.length/2) ; 
		System.arraycopy(data, data.length/2,dataFlipped, 0, data.length/2) ; 

		
		return dataFlipped ; 
	}
}
