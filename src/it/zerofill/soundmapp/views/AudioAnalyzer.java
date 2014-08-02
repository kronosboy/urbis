package it.zerofill.soundmapp.views;

import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import it.zerofill.soundmapp.views.AudioAnalyzerListener;

public class AudioAnalyzer implements Runnable{

	private int mSampleRate = 8000; // 16000
	//private int mAudioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
	private int mAudioSource = MediaRecorder.AudioSource.MIC;
	
	private final int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
	private final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	private final AudioAnalyzerListener mListener;
	private Thread mThread;
	private boolean mRunning;
	
	private FileOutputStream os = null;
	private short sData[];
	private int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	private int BytesPerElement = 2; // 2 bytes in 16bit format
	
	private AudioRecord recorder;
	private int mTotalSamples = 0;
	
	//private DataOutputStream dos;


	public AudioAnalyzer(AudioAnalyzerListener listener) {
		mListener = listener;
	}
	  
	public void setSampleRate(int sampleRate) {
		mSampleRate = sampleRate;
	}
	  
	public void setAudioSource(int audioSource) {
		mAudioSource = audioSource;
	}

	public void start(String audioPath) {
		if (false == mRunning) {
			try{
			    sData = new short[BufferElements2Rec];
			    os = new FileOutputStream(audioPath);
			}catch(Exception e){}
			
			mRunning = true;
			mThread = new Thread(this);
			mThread.start();
	    }
	}

	public void stop() {
		try {
			if (mRunning) {
				mRunning = false;
				mThread.join();
			}
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }  
	}

	@Override
	public void run() {
		// Buffer for 20 milliseconds of data, e.g. 160 samples at 8kHz.
	    short[] buffer20ms = new short[mSampleRate / 50];
	    // Buffer size of AudioRecord buffer, which will be at least 1 second.
	   // int buffer1000msSize = bufferSize(mSampleRate, mChannelConfig,mAudioFormat);
	    try {
	    	//recorder = new AudioRecord(mAudioSource,mSampleRate,mChannelConfig,mAudioFormat,buffer1000msSize);
	    	recorder = new AudioRecord(mAudioSource,mSampleRate,mChannelConfig,mAudioFormat,BufferElements2Rec * BytesPerElement);
	    	
	    	//int recBufSize = AudioRecord.getMinBufferSize(mSampleRate,  mChannelConfig, mAudioFormat); 
	    	recorder.startRecording();
	    	while (mRunning) {      
	    		int numSamples = recorder.read(buffer20ms, 0, buffer20ms.length);

	    		recorder.read(sData, 0, BufferElements2Rec);
		        try {
		            byte bData[] = short2byte(sData);
		            os.write(bData, 0, BufferElements2Rec * BytesPerElement);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }

	    		mTotalSamples += numSamples;
	    		mListener.processAudioFrame(buffer20ms);
	    	}
	    	
	    	try {
		        os.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	    	
	    	recorder.stop();
	    } catch(Exception e) {
	      e.printStackTrace();
	    }   
	}

	public int totalSamples() {
		return mTotalSamples;
	}

	public void setTotalSamples(int totalSamples) {
		mTotalSamples = totalSamples;
	}
	  
	  
//	private int bufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
//		int buffSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
//	    if (buffSize < sampleRateInHz) {
//	    	buffSize = sampleRateInHz;
//	    }
//	    return buffSize;
//	  }
	
	 //convert short to byte
	private byte[] short2byte(short[] sData) {
	    int shortArrsize = sData.length;
	    byte[] bytes = new byte[shortArrsize * 2];
	    for (int i = 0; i < shortArrsize; i++) {
	        bytes[i * 2] = (byte) (sData[i] & 0x00FF);
	        bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
	        sData[i] = 0;
	    }
	    return bytes;

	}
	
}
