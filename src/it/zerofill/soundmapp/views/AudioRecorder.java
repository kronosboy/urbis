package it.zerofill.soundmapp.views;

import java.io.IOException;

import android.media.MediaRecorder;


// TO DELETE
public class AudioRecorder implements Runnable{

	private boolean mRunning;
	private Thread mThread;
	private MediaRecorder mRecorder = null;
	
	// DELETE THIS CLASS
	@Deprecated
	public void start(String audioPath){
		if (false == mRunning) {
			
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mRecorder.setOutputFile(audioPath);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			
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
	
	private boolean isRecording;
	@Override
	public void run() {
		
		if(!isRecording){
			try{
				mRecorder.prepare();
			}catch(IOException e){
			}
			mRecorder.start();
			isRecording = true;
		}
		
		
		if(!mRunning){
			try{
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			}catch(Exception e){}
			
		}
	}

}
