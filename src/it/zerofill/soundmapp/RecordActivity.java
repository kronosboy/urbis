package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.FeatureParser;
import it.zerofill.soundmapp.controllers.SurveyXmlBuilder;
import it.zerofill.soundmapp.models.AttributeType;
import it.zerofill.soundmapp.models.ObjFeature;
import it.zerofill.soundmapp.models.ObjRecord;
import it.zerofill.soundmapp.models.ObjValue;
import it.zerofill.soundmapp.views.AudioAnalyzer;
import it.zerofill.soundmapp.views.AudioAnalyzerListener;
import it.zerofill.soundmapp.views.BarLevelDrawable;
import it.zerofill.soundmapp.views.GPSTracker;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import simplesound.pcm.PcmAudioHelper;
import simplesound.pcm.WavAudioFormat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
//import it.zerofill.soundmapp.controllers.AssetsPropertyReader;

public class RecordActivity extends Activity implements AudioAnalyzerListener{

	private AudioAnalyzer audioAnalyzer = null;
//	private Context mainContext = null;
	private boolean isRecording = false;
	private boolean isPlaying = false;
	private boolean isDrawing = false;
	private TextView dbText = null;
	private BarLevelDrawable mBarLevel;
	
	private ImageView logoBannerImg;

	private final int WIDTH = 720;
	private int deviceWidth;
	
//	private AssetsPropertyReader assetsPropertyReader;
//	private Properties properties;
	
	private double mOffsetdB = 10;  // Offset for bar, i.e. 0 lit LEDs at 10 dB.
	  // The Google ASR input requirements state that audio input sensitivity
	  // should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
	  // 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
	private double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
	  // For displaying error in calibration.
//	private double mDifferenceFromNominal = 0.0;
	private double mRmsSmoothed;  // Temporally filtered version of RMS.
	private double mAlpha = 0.9;  // Coefficient of IIR smoothing filter for RMS.
	
	private Button recButton;
	private Button playButton;
	private Chronometer chronometer;
	private TextView audioLenghtLabel;
	private TextView audioLevelLabel;
	private TextView audioDecibleListText;
	
	private List<Double> dbList;
	private List<double[]> locationList;
	
	private int base_di_campionamento = 2;
	
//	private String audioPath;
//	private String surveyId;
	private String audioFileName;
	private String audioFileNameTemp;
	private boolean isNewSurvey = false;
	private boolean isLocal = false;
	private String layerName;
	private String xmlFilePath;
	private AttributeType audioAttribute;
	
	DecimalFormat df = new DecimalFormat("##.00");
	
//	private GPSTracker gps;
	private Context mainContext;
	private MediaPlayer mPlayer = null;
	
	private int TIMEOUT_TIME = 90;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);
		
		init();
		buttonsHandler();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
    	//check selected menu item
    	if(item.getItemId() == R.id.action_about){
    		String version = "1.0";
    		try{
    		PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
    		version = pInfo.versionName;
    		}catch(NameNotFoundException e){}
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
	        alertDialog.setTitle(getString(R.string.action_about));
	        alertDialog.setMessage("Author:\t\t\tAntonio Mele\nVersion:\t\t"+version);
	        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	            	dialog.cancel();
	            }
	        });
	        alertDialog.show();
    	    
    		return true;
    	}
    	return false;
    }
	
	@Override
	public void onBackPressed(){
    	finish();
    }
	
	private void buttonsHandler(){
		recButton = (Button)findViewById(R.id.recButton);

		recButton.setEnabled(isNewSurvey || isLocal);
		
		recButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isRecording){
					File fileAudio = new File(audioFileName); //+".mp4"
					if(fileAudio.exists()){
						showRecordNewFileAlert();
					}else{
						recordButton_handler();
					}
				}else{
					playButton.setEnabled(true);
					recButton.setText(getString(R.string.startLabel));
					stopTime();
					audioAnalyzer.stop();
					convertFile();
					saveAudioComponentsToXml();
					isRecording = !isRecording;
				}
			//	isRecording = !isRecording;
			}
		});
		
		playButton = (Button)findViewById(R.id.playButton);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File fileAudio = new File(audioFileName); //+".mp4"
				if(fileAudio.exists()){
					if(!isPlaying){
						recButton.setEnabled(false);
						playButton.setText(getString(R.string.stopLabel));
						startPlaying();
					}else{
						recButton.setEnabled(isNewSurvey || isLocal);
						playButton.setText(getString(R.string.playLabel));
						stopPlaying();
					}
					
					isPlaying = !isPlaying;
				}
			}
		});
	}
	
	private void recordButton_handler(){
		isRecording = true;
		playButton.setEnabled(false);
		recButton.setText(getString(R.string.stopLabel));
		startTime();
		audioAnalyzer.start(audioFileNameTemp+".pcm");
	}
	
	private void convertFile(){
		try{
			File fileSrc = new File(audioFileNameTemp+".pcm");
			WavAudioFormat af = WavAudioFormat.mono16Bit(8000);
			File fileDest = new File(audioFileName);
			PcmAudioHelper.convertRawToWav(af, fileSrc, fileDest);
			fileSrc.delete();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private long getAudioDuration(){
		long res = 0;
		mPlayer = new MediaPlayer();
		try{
			mPlayer.setDataSource(audioFileName);
			mPlayer.prepare();
			res = mPlayer.getDuration();
		}catch(IOException e){}
		finally{
			mPlayer = null;
		}
		return res;
	}
	
	private void startPlaying(){
		mPlayer = new MediaPlayer();
		try{
			mPlayer.setDataSource(audioFileName);
			mPlayer.prepare();
			mPlayer.start();
		}catch(IOException e){}
	}
	
	private void stopPlaying(){
		mPlayer.release();
		mPlayer = null;
	}
	
	private void timeOut(){
		showAlert();
		playButton.setEnabled(true);
		recButton.setText(getString(R.string.startLabel));
		stopTime();
		audioAnalyzer.stop();
		convertFile();
	}
	
	public void showAlert(){
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
	        alertDialog.setTitle(getString(R.string.warningLabel));
	        alertDialog.setMessage(getString(R.string.exceededTimeOutLabel));
	        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	            	dialog.cancel();
	            }
	        });
	        alertDialog.show();
	    }
	
	
	public void showRecordNewFileAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.warningLabel));
        alertDialog.setMessage(getString(R.string.recordNewFileAudioWarning));
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	recordButton_handler();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	isRecording = false;
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }
	
	
	@Override
	public void processAudioFrame(short[] audioFrame) {
		if(!isDrawing){
			
			isDrawing = true;
			double rms = 0;
			for(int i=0; i < audioFrame.length; i++){
				rms += audioFrame[i]*audioFrame[i];
			}
			rms = Math.sqrt(rms/audioFrame.length);
			mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
			final double rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed);
			mBarLevel.post(new Runnable() {
				@Override
		        public void run() {
		          // The bar has an input range of [0.0 ; 1.0] and 10 segments.
		          // Each LED corresponds to 6 dB.
		          mBarLevel.setLevel((mOffsetdB + rmsdB) / 60);
		          dbText.setText((df.format(20 + rmsdB)).replace(",", "."));
		          isDrawing = false;
		        }
		      });
			}
		}
		
		private void init(){
		//	mainContext = this;
		//	Resources resource;
		//	DisplayMetrics metrics = new DisplayMetrics();
		//	getWindowManager().getDefaultDisplay().getMetrics(metrics);
			//deviceWidth = metrics.widthPixels;
			//deviceHeight = metrics.heightPixels;
		//	getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//	resource = new Resources(getAssets(), metrics, null);
			
			mainContext = this;
			
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			deviceWidth = metrics.widthPixels;
			
			dbText = (TextView)findViewById(R.id.audioDbLabel);
			audioLenghtLabel = (TextView)findViewById(R.id.audioLenghtLabel);
			audioLevelLabel = (TextView)findViewById(R.id.audioLevelLabel);
			audioDecibleListText = (TextView)findViewById(R.id.audioDecibleListText);
			mBarLevel = (BarLevelDrawable)findViewById(R.id.bar_level_drawable_view);
			chronometer = (Chronometer)findViewById(R.id.chronometer);
			
			logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
	    	
	    	// Fixed dimension for a button (on default screen it will be 150x150)
			final int FIXED_BANNER_W_DIM = 720; 
			float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
			int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
	        // This will resize button on screen resolutions
	        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
	        logoBannerImg.setLayoutParams(bannerViewParams);
			
//			assetsPropertyReader = new AssetsPropertyReader(mainContext);
//	        properties = assetsPropertyReader.getProperties("settings.properties");
	        
	        String decibel = "";
	        String location = "";
	        String surveyId = "";
			try{
	    		Bundle extras = getIntent().getExtras();
	        	if (extras != null){
//	        		config = (Configuration)extras.getSerializable("config");
//	        		isNewSurvey = extras.getBoolean("isNewSurvey");
//	        		isLocal = extras.getBoolean("isLocal");
//	        		if(!isNewSurvey){
//	        			surveyName = extras.getString("surveyName"); 
//	            		//custom = (CustomObj)extras.getSerializable("custom");	
//	        		}
	        		
	        	//	audioPath = extras.getString("audioPath");
//	        		surveyId = extras.getString("surveyId");
	        		audioFileName = extras.getString("audioFileName");
	        		audioFileNameTemp = audioFileName.substring(0, audioFileName.lastIndexOf("."));
	        		decibel = extras.getString("decibel");
	        		location = extras.getString("location");
	        		isNewSurvey = extras.getBoolean("isNewSurvey");
	        		isLocal = extras.getBoolean("isLocal");
	        		
	        		layerName = extras.getString("layerName");
	        		xmlFilePath = extras.getString("xmlFilePath");
	        		audioAttribute = (AttributeType)extras.getSerializable("audioAttribute");
	        		
	        		surveyId = extras.getString("surveyId");
	        	}
	        	
	        	long duration = getAudioDuration();
	        	if(duration>0){
	        		//String s = new SimpleDateFormat("mm:ss:SSS")).format(new Date(duration));
	        		int seconds = (int) ((duration / 1000) % 60) ;
	        		int minutes = (int) (seconds / 60);
	        		DecimalFormat formatter = new DecimalFormat("00");
	        		audioLenghtLabel.setText(formatter.format(minutes)+":"+formatter.format(seconds));

	        		if (decibel==null || "".equals(decibel)){
	        		// read decibel from xml file
	    	  			String columnNameAudioDb = ""; // attrib id
	    	  			HashMap<String, String> map = getMapAudioAssociatedComponent(audioAttribute);
	    	  			if(map.size()>0){
	    					if(map.containsKey(AUDIO_COMPONENT_DECIBEL)){
	    						columnNameAudioDb = map.get(AUDIO_COMPONENT_DECIBEL);		
	    	  				}
	    					
	    	  			}
	    	  			List<String> colonne = new ArrayList<String>();
	    	  			if(!"".equals(columnNameAudioDb))colonne.add(columnNameAudioDb);
	    	  			
	        			List<String> layerList = new ArrayList<String>();
	        			layerList.add(layerName);
	        			FeatureParser featureParser = new FeatureParser(mainContext);
	        			List<ObjFeature> records = featureParser.parseGetFeatureFile(layerList,true,surveyId,colonne);
	        			
	        			if(records!=null && records.size()>0){
		        			HashMap<String, ObjValue> m = getMapValueByColumnId(records);
		        			String decString = "";
		        			if(m.containsKey(columnNameAudioDb)){
		        				decString = m.get(map.get(AUDIO_COMPONENT_DECIBEL)).getValue();
		        				decibel = decString;
		        			}
		        			dbList = new ArrayList<Double>();
		        			StringTokenizer tokenizer = new StringTokenizer(decString); // all point separated by space
		        			while(tokenizer.hasMoreTokens()){
		        				StringTokenizer tokenizerPoint = new StringTokenizer(tokenizer.nextToken(),";"); // couple of points
		        				String dec = tokenizerPoint.nextToken().trim();
		        				double decDouble = Double.parseDouble(dec);
		        				dbList.add(decDouble);
		        			}
	        			}
	        		}else{
	        			dbList = new ArrayList<Double>();
	        			StringTokenizer tokenizer = new StringTokenizer(decibel); // all point separated by space
	        			while(tokenizer.hasMoreTokens()){
	        				StringTokenizer tokenizerPoint = new StringTokenizer(tokenizer.nextToken(),";"); // couple of points
	        				String dec = tokenizerPoint.nextToken().trim();
	        				double decDouble = Double.parseDouble(dec);
	        				dbList.add(decDouble);
	        			}
	        		}
	        	}
	        	
	        	//Toast.makeText(getApplicationContext(), ""+decibel, Toast.LENGTH_LONG).show();
	        	if(!"".equals(decibel)){
	        		// show decibel
	        		audioDecibleListText.setText(decibel);
	        		audioLevelLabel.setText(calcolaLeq());
	        		//audioLevelLabel.setText("Leq: "+calcolaLeq());
	        	}
	        	if(!"".equals(location)){
	        		// show location
	        		
	        	}
	        	
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
			
			
			chronometer.setOnChronometerTickListener(new OnChronometerTickListener() {
				@Override
				public void onChronometerTick(Chronometer ch) {
					audioLenghtLabel.setText(chronometer.getText());
					
					long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
					long elapsedSecs = elapsedMillis / 1000;
					
					if(elapsedSecs==0 || (elapsedSecs>0 && elapsedSecs % base_di_campionamento == 0)){
						try{
							double[] location = getLocation();
							locationList.add(location);
							
							double val = Double.parseDouble(dbText.getText().toString().replace(",", "."));
							dbList.add(val);
						}catch(Exception e){}
						
						audioLevelLabel.setText(calcolaLeq());
						//audioLevelLabel.setText("Leq: "+calcolaLeq());
					}
					
					
					if(elapsedSecs > TIMEOUT_TIME){
						timeOut();
					} 
					
					
				}
			});
			
			audioAnalyzer = new AudioAnalyzer(this);
//			gps = new GPSTracker(mainContext);
		}
		
		private HashMap<String, ObjValue> getMapValueByColumnId(List<ObjFeature> records){
	    	HashMap<String, ObjValue> res = new HashMap<String, ObjValue>();
	    	if(records==null) return null;
	    	for(ObjFeature objf : records){
				for(ObjRecord objr : objf.getRecords()){
					for(ObjValue objv : objr.getValues()){
						String col = objv.getColumn_name();
						res.put(col, objv);
					}
				}
	    	}
	    	return res;
	    }
		
		private void startTime(){
			dbList = new ArrayList<Double>();
			locationList = new ArrayList<double[]>();
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
		}
		private void stopTime(){
			chronometer.stop();
			audioLevelLabel.setText(calcolaLeq());
			//audioLevelLabel.setText("Leq: "+calcolaLeq());
			
			// ********************************************************
			//
			String text = "Decible list size: "+dbList.size()+"\n";
			String decibelList = "";
			for(double d : dbList){
				text += d +"; ";
				decibelList += d + "; ";
			}
			audioDecibleListText.setText(decibelList);
			
			text = text.substring(0, text.length()-2) + "\nLocation list size: "+locationList.size()+"\n";
			if(locationList.size()==1){
				double[] tmp = locationList.get(0);
				locationList.add(tmp);
			}
			else if(locationList.size()>2){
				double[] tmp0 = locationList.get(0);
				double[] tmp1 = locationList.get(1);
				if(tmp0[0]==tmp1[0] && tmp0[1]==tmp1[1])
					locationList.remove(0);
			}
			for(double[] d : locationList){
				text += df.format(d[0]) + " - " + df.format(d[1]) + "\n"; 
			}
		//	Toast.makeText(getApplicationContext(), ""+text, Toast.LENGTH_LONG).show();
			// ********************************************************
			
		}
		
		private String calcolaLeq(){
			double TEMP = 0;
			for(double v : dbList){
				TEMP += Math.pow(10d, v/10);
			}
			double Leq = 10 * Math.log10(TEMP/dbList.size());
			if(Double.compare(Leq,Double.NaN)!=0)
				return (df.format(Leq)).replace(",", ".");
			else
				return "";
		}
		
		private double[] getLocation(){
			GPSTracker gps = new GPSTracker(mainContext);
			gps.getLocation();
			double[] res = new double[2];
			res[0] = 0d;
			res[1] = 0d;
			// check if GPS enabled     
            if(gps.canGetLocation()){
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                res[0] = latitude;
    			res[1] = longitude;
              //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                
            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
            return res;
		}
		
		
		
		
		private final String AUDIO_COMPONENT_DECIBEL = "decibel";
		private final String AUDIO_COMPONENT_LOCATION = "location";
		private final String AUDIO_COMPONENT_FILE = "audio";
		private HashMap<String, String> getMapAudioAssociatedComponent(AttributeType attr){
			HashMap<String, String> res = new HashMap<String, String>();
			for(String item : attr.getTypeConfig()){
				StringTokenizer tokenizer = new StringTokenizer(item, ":");
				String nomeColonnaElemento = tokenizer.nextToken();
				String tipoElemento = tokenizer.nextToken();
				res.put(tipoElemento, nomeColonnaElemento);
			}
			return res;
		}
		
		private void saveAudioComponentsToXml(){
			saveAudioFeatureTask = new SaveAudioFeatureTask();
			saveAudioFeatureTask.execute((Void) null);
		}
		
		private SaveAudioFeatureTask saveAudioFeatureTask;
		public class SaveAudioFeatureTask extends AsyncTask<Void, Void, Boolean> {
	  		
	  		@Override
			protected Boolean doInBackground(Void... args) {
	  			
	  			String columnNameAudioFile = ""; // attrib id
	  			String columnNameAudioDb = ""; // attrib id
	  			String columnNameAudioPoints = ""; // attrib id
	  			
	  			HashMap<String, String> map = getMapAudioAssociatedComponent(audioAttribute);
	  			if(map.size()>0){
	  				if(map.containsKey(AUDIO_COMPONENT_FILE)){
	  					columnNameAudioFile = map.get(AUDIO_COMPONENT_FILE);
	  				} 
					if(map.containsKey(AUDIO_COMPONENT_DECIBEL)){
						columnNameAudioDb = map.get(AUDIO_COMPONENT_DECIBEL);		
	  				}
					if(map.containsKey(AUDIO_COMPONENT_LOCATION)){
						columnNameAudioPoints = map.get(AUDIO_COMPONENT_LOCATION);
					}
	  			}
	  			
	  			String namespace = layerName.substring(0, layerName.indexOf(":"));
	  			String layer = layerName.substring(layerName.indexOf(":")+1,layerName.length());
	  			
	  			//String audioPath = ""; // path audio
	  			
	  			// insert Audio File
	  			SurveyXmlBuilder.insertUpdate(xmlFilePath, namespace, layer, columnNameAudioFile, true, false,"", audioFileName);
	  			// insert Decibel
	  			SurveyXmlBuilder.insertUpdate(xmlFilePath, namespace, layer, columnNameAudioDb, false, false,"", audioDecibleListText.getText().toString());
	  			// insert Points
	  			String coord = "";
	  			for(double[] d : locationList){
	  				// 2014-07-08 inverted longitude and latitude (the new value will be [longitude, latitude])
//	  				coord += d[0]+","+d[1] + " ";
	  				coord += d[1]+","+d[0] + " ";
				}
	  			if(coord.length()>1)
	  				coord = coord.substring(0, coord.length()-1);
	  			SurveyXmlBuilder.insertUpdate(xmlFilePath, namespace, layer, columnNameAudioPoints, false, true,"line", coord);
				
				return true;
			}
			
			@Override
			protected void onPostExecute(final Boolean success) {
				saveAudioFeatureTask = null;
				
				if (success) {
					
				} else {
					
				}
			}
	  	}
		
		
		
		
		
		
		
		
		
		
		
		

}
