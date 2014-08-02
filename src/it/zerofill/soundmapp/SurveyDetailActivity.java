package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AttributesExtractor;
import it.zerofill.soundmapp.controllers.AuthenticationException;
import it.zerofill.soundmapp.controllers.EncodeSaveFile;
import it.zerofill.soundmapp.controllers.FeatureParser;
import it.zerofill.soundmapp.controllers.GetXmlFromGeoserver;
import it.zerofill.soundmapp.controllers.LocalFileHandler;
import it.zerofill.soundmapp.controllers.SurveyXmlBuilder;
import it.zerofill.soundmapp.models.AttributeType;
import it.zerofill.soundmapp.models.Configuration;
import it.zerofill.soundmapp.models.FeatureElementAttribute;
import it.zerofill.soundmapp.models.Layer;
import it.zerofill.soundmapp.models.ObjFeature;
import it.zerofill.soundmapp.models.ObjRecord;
import it.zerofill.soundmapp.models.ObjValue;
import it.zerofill.soundmapp.views.GPSTracker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class SurveyDetailActivity extends Activity{
	
	private final String TYPE_COMBO = "combo";
	private final String TYPE_STRING = "String";
	private final String TYPE_IMAGE = "img64";
	private final String TYPE_AUDIO = "AudioRec";
	private final String TYPE_DOUBLE = "Double";
	private final String TYPE_INT = "Integer";
	private final String TYPE_DATE = "Date";
	private final String TYPE_POINT = "point";
	private final String TYPE_LINE = "line";

	private int base_di_campionamento = 2;
	public String sessionId;
	
	private Context mainContext;
	private Resources resource = null;
	private String imgDir = "";
	private String soundDir = "";
	private String localDir = "";
	private String sentReplyDir = "";
	private String imgName = "";
	private String defaultSurveyName = "";
	private String defaultSurveyKey = "";
	private MediaPlayer mPlayer = null;
	
	private String pass;
	
	private Button sendButton;
	private View getDescribeFeatureStatusBar;
	private View sendSurveyStatusLayoutBar;
	private View mainLayoutTab;
	
	private GetDescribeFeatureTask getDescribeTask;
	private SaveFeatureTask saveFeatureTask;
	private SendDataTask sendDataTask;
	
	private String loggedUser;
	private String loggedUserFilter;
	private String creatorField = "creator";
	private String currentFileName;
	
	private int snap_img_button_id;
	private int snap_img_button_pressed_id;
	private int snap_img_button_disabled_id;
	
	private int del_img_button_id;
	private int del_img_button_pressed_id;
	private int del_img_button_disabled_id;
	
	private int wrench_img_button_id;
	private int wrench_img_button_pressed_id;
	private int wrench_img_button_disabled_id;
	
	private int calendar_img_button_id;
	private int calendar_img_button_pressed_id;
	private int calendar_img_button_disabled_id;
	
	private int location_img_button_id;
	private int location_img_button_pressed_id;
	private int location_img_button_disabled_id;
	
//	private int play_img_button_id;
//	private int play_img_button_pressed_id;
//	private int play_img_button_disabled_id;
//	
//	private int rec_img_button_id;
//	private int rec_img_button_pressed_id;
//	private int rec_img_button_disabled_id;
	
	
	
	private TabHost tabs;
	private LinearLayout mainlayoutTab;
	
	private DatePickerDialog.OnDateSetListener dateSetListener;
	private int year;
	private int month;
	private int day;
	
	private FeatureParser featureParser;
	private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	private Configuration config;
	private Layer layer;
	private HashMap<String, AttributeType> attributesMap;
	
	private String layerName;
	private boolean isNewSurvey = false;
	private boolean isLocal = false;
	private String surveyName = "";
	private String surveyId = "";
	private HashMap<String, FeatureElementAttribute> describeFeatureMap;
	private List<ObjFeature> records;
	private String localFileHandler;
	
	private HashMap<Integer, ImageView> mappaImmagini;
	private HashMap<Integer, LinearLayout> mappaImmaginiView;
	private HashMap<Integer, String> mappaNextImgToShow;
	
	private HashMap<Integer, Boolean> mappaImgToShow;
	private HashMap<Integer, Boolean> mappaImgIsTaken;
	private int imgShowed;
	
	private HashMap<String, Integer> mappaImmaginiComponent;
	private HashMap<Integer, Boolean> imgVisibileMap;
	private HashMap<Integer, String> mappaPathImmagini;
	private HashMap<Integer, String> mappaPathAudio;
	private HashMap<Integer, String> mappaPathAudioDecibel;
	private HashMap<Integer, AttributeType> mappaAudioAttribute;
	private HashMap<Integer, String> mappaPathAudioLocation;
	private HashMap<Integer, EditText> mappaDateComponent;
	private HashMap<Integer, Button> pointsViewDataComponent;
	private HashMap<Integer, Chronometer> chronoPointsComponent;
	private HashMap<Integer, Boolean> chronoPointsIsRecording;
	private HashMap<Integer, String> mappaDateComponentId;
	private HashMap<Integer, List<double[]>> mappaCoordinates;
	private HashMap<Integer, ImageView> mappaDeleteAudioButton;
	private HashMap<Integer, ImageView> mappaDeleteImgButton;
	private HashMap<Integer, ImageView> mappaAudioImgButton;
	private int selectedDateComponentId;
	private int selectedAudioCompId = 0;
	private boolean isDateChanged = false;
	
	private final int WIDTH = 720;
	private final int HEIGHT = 1280;
	private int deviceWidth;
	private int deviceHeight;
	private LinearLayout.LayoutParams layout_button_params;
	private ImageView logoBannerImg;
	
	private HashMap<String, String> mappaValoriAttributi;
	private HashMap<String, Boolean> mandatoryFieldMap;
	private HashMap<String, Boolean> errorFieldMap;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        init();
        
        showProgress(true);
        getDescribeTask = new GetDescribeFeatureTask();
        getDescribeTask.execute((Void) null);
        
        buttonsHandler();
    }

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig){
	  super.onConfigurationChanged(newConfig);
	  setContentView(R.layout.activity_survey_detail);
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
    
	private void init(){
    	mainContext = this;
    	DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		deviceWidth = metrics.widthPixels;
		deviceHeight = metrics.heightPixels;
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		resource = new Resources(getAssets(), metrics, null);
		
		assetsPropertyReader = new AssetsPropertyReader(mainContext);
        properties = assetsPropertyReader.getProperties("settings.properties");
        featureParser = new FeatureParser(mainContext);
        
        mappaImmagini = new HashMap<Integer, ImageView>();
        mappaImmaginiView = new HashMap<Integer, LinearLayout>();
        mappaNextImgToShow = new HashMap<Integer, String>();
        
        mappaImgToShow = new HashMap<Integer, Boolean>();
        mappaImgIsTaken = new HashMap<Integer, Boolean>();
        imgShowed = 0;
        
        mappaImmaginiComponent = new HashMap<String, Integer>();
        imgVisibileMap = new HashMap<Integer, Boolean>();
    	mappaPathImmagini = new HashMap<Integer, String>();
    	mappaPathAudio = new HashMap<Integer, String>();
    	mappaPathAudioDecibel = new HashMap<Integer, String>();
    	mappaAudioAttribute = new HashMap<Integer, AttributeType>();
    	mappaPathAudioLocation = new HashMap<Integer, String>();
    	mappaValoriAttributi = new HashMap<String, String>();
    	mappaDateComponent = new HashMap<Integer, EditText>();
    	chronoPointsIsRecording = new HashMap<Integer, Boolean>();
    	chronoPointsComponent = new HashMap<Integer, Chronometer>();
    	pointsViewDataComponent = new HashMap<Integer, Button>();
    //	gpsComponent = new HashMap<Integer, GPSTracker>();
    	mappaDateComponentId = new HashMap<Integer, String>();
    	mandatoryFieldMap = new HashMap<String, Boolean>();
    	errorFieldMap = new HashMap<String, Boolean>();
    	mappaCoordinates = new HashMap<Integer, List<double[]>>();
    	mappaDeleteAudioButton = new HashMap<Integer, ImageView>();
    	mappaDeleteImgButton = new HashMap<Integer, ImageView>();
    	mappaAudioImgButton = new HashMap<Integer, ImageView>();
    	
        spinnerCompMap = new HashMap<Integer, String>();
        freeTextCompMap = new HashMap<Integer, String>();
        pointsCompMap = new HashMap<Integer, String>();
//        LineCompMap = new HashMap<Integer, String>();
        imageCompMap = new HashMap<Integer, String>();
        audioCompMap = new HashMap<Integer, String>();
        
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_DIM = 150;
		float FIXED_WIDTH_PERC = ((float) FIXED_DIM /WIDTH) * 100;
		float FIXED_HEIGHT_PERC = ((float) FIXED_DIM /HEIGHT) * 100;
		int FIXED_WIDTH_VALUE = (int)((FIXED_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_HEIGHT_VALUE = (int)((FIXED_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        layout_button_params = new LinearLayout.LayoutParams(FIXED_WIDTH_VALUE,FIXED_HEIGHT_VALUE);
        
        logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
        
        getDescribeFeatureStatusBar = findViewById(R.id.getDescribeFeatureLayout);
        sendSurveyStatusLayoutBar = findViewById(R.id.sendSurveyStatusLayout);
        mainLayoutTab = findViewById(R.id.mainLayoutTab);
        sendButton = (Button)findViewById(R.id.sendButton);
		mainlayoutTab = (LinearLayout)findViewById(R.id.mainlayoutTab);

        snap_img_button_id = resource.getIdentifier("camera", "drawable","it.zerofill.soundmapp");
		snap_img_button_pressed_id = resource.getIdentifier("camera_pressed", "drawable","it.zerofill.soundmapp");
		snap_img_button_disabled_id = resource.getIdentifier("camera_disabled", "drawable","it.zerofill.soundmapp");
		
		del_img_button_id = resource.getIdentifier("delete", "drawable","it.zerofill.soundmapp");
		del_img_button_pressed_id = resource.getIdentifier("delete_pressed", "drawable","it.zerofill.soundmapp");
		del_img_button_disabled_id = resource.getIdentifier("delete_disabled", "drawable","it.zerofill.soundmapp");
		
		wrench_img_button_id = resource.getIdentifier("wrench", "drawable","it.zerofill.soundmapp");
		wrench_img_button_pressed_id = resource.getIdentifier("wrench_pressed", "drawable","it.zerofill.soundmapp");
		wrench_img_button_disabled_id = resource.getIdentifier("wrench_disabled", "drawable","it.zerofill.soundmapp");
		
		calendar_img_button_id = resource.getIdentifier("calendar_btn", "drawable","it.zerofill.soundmapp");
		calendar_img_button_pressed_id = resource.getIdentifier("calendar_btn_pressed", "drawable","it.zerofill.soundmapp");
		calendar_img_button_disabled_id = resource.getIdentifier("calendar_btn_disabled", "drawable","it.zerofill.soundmapp");
		
		location_img_button_id = resource.getIdentifier("location_off_btn", "drawable","it.zerofill.soundmapp");
		location_img_button_pressed_id = resource.getIdentifier("location_on_btn", "drawable","it.zerofill.soundmapp");
		location_img_button_disabled_id = resource.getIdentifier("location_off_btn_disabled", "drawable","it.zerofill.soundmapp");
		
//		play_img_button_id = resource.getIdentifier("play", "drawable","it.zerofill.soundmapp");
//		play_img_button_pressed_id = resource.getIdentifier("play_pressed", "drawable","it.zerofill.soundmapp");
//		play_img_button_disabled_id = resource.getIdentifier("play_disabled", "drawable","it.zerofill.soundmapp");
//		
//		rec_img_button_id = resource.getIdentifier("rec", "drawable","it.zerofill.soundmapp");
//		rec_img_button_pressed_id = resource.getIdentifier("rec_pressed", "drawable","it.zerofill.soundmapp");
//		rec_img_button_disabled_id = resource.getIdentifier("rec_disabled", "drawable","it.zerofill.soundmapp");
		
		final int FIXED_TITLE_DIM = 200;
		float FIXED_TITLE_WIDTH_PERC = ((float) FIXED_TITLE_DIM /WIDTH) * 100;
		int FIXED_TITLE_WIDTH_VALUE = (int)((FIXED_TITLE_WIDTH_PERC * deviceWidth) / 100);
		titleViewParams = new LinearLayout.LayoutParams(FIXED_TITLE_WIDTH_VALUE ,LayoutParams.WRAP_CONTENT); 
		
    	getConfigAttributes();
    }
	private LinearLayout.LayoutParams titleViewParams;
    
	@SuppressLint("SimpleDateFormat")
    @SuppressWarnings("static-access")
	private void getConfigAttributes(){
    	try{
    		Bundle extras = getIntent().getExtras();
        	if (extras != null){
        		loggedUser = extras.getString("loggedUser");
        		loggedUserFilter = extras.getString("loggedUserFilter");
        		pass = extras.getString("pass");
        		sessionId = extras.getString("sessionId");
        		config = (Configuration)extras.getSerializable("config");
        		isNewSurvey = extras.getBoolean("isNewSurvey");
        		isLocal = extras.getBoolean("isLocal");
        		
        		
        		if(!isNewSurvey){
        			surveyName = extras.getString("surveyName");
        			surveyId = extras.getString("surveyId"); 
        		}
        		
        		// aggiunto la gestione di un file di controllo all'interno del quale viene salvato
        		// l'id della segnalazione
        		// se Android richiama la creazione della view a seguito del ritorno dalla fotocamera
        		// utilizzo questo file di controllo per leggere tutti i valori della segnalazioni precedentemente creata
        		String homepath = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
        		File checksumFile = new File(homepath,"checksum");
        		if(checksumFile.exists()){
        			isNewSurvey = false;
        		}
        		
        		
        		layerName = extras.getString("layerName");
        	}
        	layer = config.getLayerByName(layerName);
        	if(layer!=null)
        		attributesMap = layer.getAttributesMap();
        	else
        		finish();
        	
        	// Find for this layer creator field.
        	creatorField = layer.getCreatorolumn();
        	if("".equals(creatorField))
        		creatorField = "creator";
        	
        	maxPageNumber = attributeExstractor.getMaxPageNumber(layer);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Calendar currentDate = new GregorianCalendar();
		year = currentDate.get(Calendar.YEAR);
		month = currentDate.get(Calendar.MONTH);
		day = currentDate.get(Calendar.DAY_OF_MONTH);
    	String currentDateandTime = sdf.format(currentDate.getTime());
    	defaultSurveyName = getString(R.string.defaultSurveyName)+"_"+currentDateandTime;
    	defaultSurveyKey = layerName.substring(layerName.indexOf(":")+1,layerName.length()) + "." + loggedUser + "." + currentDateandTime;
    	
    	if(isNewSurvey)
    		surveyId = defaultSurveyKey;
    	
    	dateSetListener = new DatePickerDialog.OnDateSetListener() {
	        public void onDateSet(DatePicker view, int myear, int monthOfYear,int dayOfMonth) {
	            year = myear;
	            month = monthOfYear;
	            day = dayOfMonth;
	            updateDateDisplay();
	        }
	    };
	    
    	
    	imgDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("picturesDirectory") + "/" + surveyId ;
		soundDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("soundsDirectory") + "/" + surveyId;
		localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory") + "/" + surveyId;
		sentReplyDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory") + "/"+ properties.getProperty("sentReplyDirectory");
		localFileHandler = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
//		imgDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("picturesDirectory") + "/" + surveyId ;
//		soundDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("soundsDirectory") + "/" + surveyId;
//		localDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory") + "/" + surveyId;
//		sentReplyDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory") + "/"+ properties.getProperty("sentReplyDirectory");
//		localFileHandler = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
		
//		fileEncr = new FileEncryptor();
//		userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");
		
		File newLocaldir = new File(localDir);
		if(isNewSurvey) //&& !newLocaldir.exists()
			newLocaldir.mkdirs();
		File newdir = new File(imgDir); 
		if(!newdir.exists())
			newdir.mkdirs();
		File newdirSound = new File(soundDir);
		if(!newdirSound.exists())
			newdirSound.mkdirs();
		
		File sentRepl = new File(sentReplyDir);
		if(!sentRepl.exists())
			sentRepl.mkdirs();
		
		File nomedia = new File(imgDir, ".nomedia");
		File nomediaSound = new File(soundDir, ".nomedia");
		
		try{
			nomedia.createNewFile();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			nomediaSound.createNewFile();
		}catch(Exception e){
			e.printStackTrace();
		}
		
    }
    
	
    private void buttonsHandler(){
    	
    	if(!isLocal && !isNewSurvey)
    		sendButton.setEnabled(false);
    	
    	sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try{
					stopPlaying();
					closeKeyboard();
				}catch(Exception e){
					e.printStackTrace();
				}
				String campiNulli = "";
				for(String attrId : mandatoryFieldMap.keySet()){
					if(!mandatoryFieldMap.get(attrId)){
						campiNulli += layer.getAttributesMap().get(attrId).getName()+"\n";
					}
				}
				
				if(!"".equals(campiNulli)){
					// showAlert
					showGenericAlert(getString(R.string.warningLabel), getString(R.string.mandatoryFieldsMissingMessage) + "\n" + campiNulli,false);
				}else{
					// send
					boolean haveNetworkConnection = haveNetworkConnection();
					if(haveNetworkConnection){
						// send
						showSendingProgress(true);
						sendDataTask = new SendDataTask();
						sendDataTask.execute((Void) null);	
					}else{
						showGenericAlert(getString(R.string.warningLabel), getString(R.string.noConnMessage),false);
					}
					
				}
			}
    	});

    }
    
    public WaitTask waitTask;
    
    @Override
	public void onResume(){
		super.onResume();
		
		showProgress(true);
		waitTask = new WaitTask();
		waitTask.execute((Void)null);
	}
    
    
    public void onResumeProcess(){
    	if(isSnappedPicture){
    		
    		String path = mappaPathImmagini.get(currentImageIDComponentSelected);
    		File imgFile = new File(path);

    		// after came back from camera and image is taken
    		// if image exists (and has real dimension)
    		if(imgFile.exists() && imgFile.length()>0){
    			compressImage();
    			saveImageToXml();
    			setImage();
    			
    			mappaDeleteImgButton.get(currentImageIDComponentSelected).setEnabled(true);
    			mappaDeleteImgButton.get(currentImageIDComponentSelected).setImageResource(del_img_button_disabled_id);
    			if(!isSingleImage){
    				mappaImgIsTaken.put(currentImageIDComponentSelected, true);
    				
    				int imgComponentShowed = 0;
    				int imgComponentIsTaken = 0;
    				for(int k : mappaImgToShow.keySet()){
    					if(mappaImgToShow.get(k))
    						imgComponentShowed++;
    					if(mappaImgIsTaken.get(k))
    						imgComponentIsTaken++;
    				}
    				if(imgComponentShowed<=imgComponentIsTaken)
    					showNextImgView();
    			}
    			
    			String attrId = imageCompMap.get(currentImageIDComponentSelected);
    			// check if it is in mandatory map and set its value to true
            	if(mandatoryFieldMap.containsKey(attrId)){
            		mandatoryFieldMap.put(attrId,true);
            		errorFieldMap.put(attrId,false);
            	}
            // if image exist but has not real dimension (file is created but pic was not taken)
    		}else if(imgFile.exists()){
    			mappaPathImmagini.remove(currentImageIDComponentSelected);
    			imgFile.delete();
    		}
			
        	isSnappedPicture = false;
        	isSingleImage = false;
		}
		
		if(isRecordedAudio && selectedAudioCompId!=0){
			
			String attrId = audioCompMap.get(selectedAudioCompId);
			String audioFileName = soundDir+"/"+attrId + "_" + surveyId + ".mp4";
			File audioFile = new File(audioFileName);
			if(audioFile.exists()){
				mappaPathAudio.put(selectedAudioCompId,audioFileName);
				mappaDeleteAudioButton.get(selectedAudioCompId).setEnabled(true);
				mappaDeleteAudioButton.get(selectedAudioCompId).setImageResource(del_img_button_disabled_id);
				int audioimg_full_id = resource.getIdentifier("audio_full_ico", "drawable","it.zerofill.soundmapp");
				mappaAudioImgButton.get(selectedAudioCompId).setImageResource(audioimg_full_id);
			}
			
			isRecordedAudio = false;
		}
    }
    
    private void compressImage(){
    	//ShrinkBitmap();
    	String path = mappaPathImmagini.get(currentImageIDComponentSelected);
    	try{
    		File imgFile = new File(path);
    		if(imgFile.exists() && imgFile.length()>0){
    			Bitmap myBitmap = decodeFile(imgFile,false,false);
    			FileOutputStream fileOutputStream = new FileOutputStream(path);
    			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
    			myBitmap.compress(CompressFormat.JPEG, 100, bos);
        		bos.flush();
        		bos.close();
    		}
    	}catch(Exception e){}
    	
    	//Bitmap bitmap = Bitmap.createScaledBitmap(capturedImage, width, height, true);
    	
//    	byte[] data = null;
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        data = baos.toByteArray();
    	
//    	Bitmap bmp = BitmapFactory.decodeFile(miFoto);
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		bmp.compress(CompressFormat.JPEG, 70, bos);
//		InputStream in = new ByteArrayInputStream(bos.toByteArray());
//		ContentBody foto = new InputStreamBody(in, "image/jpeg", "filename");
    }
    
    
    public Bitmap ShrinkBitmap(){
    	String file = mappaPathImmagini.get(currentImageIDComponentSelected);
    	
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) deviceHeight);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) deviceWidth);

        if(heightRatio > 1 || widthRatio > 1){
            if(heightRatio > widthRatio){
                bmpFactoryOptions.inSampleSize = heightRatio;
            }
            else{
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }
    
    private void deleteImage(int imgId){
    	String imgPath = mappaPathImmagini.get(imgId);
    	File imgFile = new  File(imgPath);
		try{
			imgFile.delete();
			
			// delete image from xml
			saveFeatureTask = new SaveFeatureTask();
    		saveFeatureTask.execute(currentFileName,currentImageAttrIDComponentSelected,"img",null);
    		
    		if(!isSingleImage)
    			mappaImgIsTaken.put(imgId,false);
    		
    		mappaDeleteImgButton.get(imgId).setEnabled(false);
			setImage();
			
			String attrId = imageCompMap.get(imgId);
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attrId)){
        		mandatoryFieldMap.put(attrId,false);
        		errorFieldMap.put(attrId,true);
        	}
        	
        //	isSingleImage = false;
        	
		}catch(Exception e){}
	}
    
    
    private void deleteAudio(int audioId){
    	String audioPath = mappaPathAudio.get(audioId);
    	File audioFile = new  File(audioPath);
		try{
			
			String attrId = audioCompMap.get(selectedAudioCompId);
			// delete audio from xml
			saveFeatureTask = new SaveFeatureTask();
			// delete audio component
			saveFeatureTask.execute(currentFileName,attrId,"audioDelete",null);
			
			audioFile.delete();
		//	mappaPathAudio.remove(audioId);
			
			mappaDeleteAudioButton.get(selectedAudioCompId).setEnabled(false);
			int audioimg_id = resource.getIdentifier("audio_ico", "drawable","it.zerofill.soundmapp");
			mappaAudioImgButton.get(selectedAudioCompId).setImageResource(audioimg_id);
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attrId)){
        		mandatoryFieldMap.put(attrId,false);
        		errorFieldMap.put(attrId,true);
        	}
			
		}catch(Exception e){}
	}
    
    
    private String currentImageAttrIDComponentSelected;
    private int currentImageIDComponentSelected;
    private void saveImageToXml(){
    	String path = mappaPathImmagini.get(currentImageIDComponentSelected);
    	//String selectedItem = EncodeSaveFile.getEncodedString(path);
    	if(path!=null && !"".equals(path)){
    		saveFeatureTask = new SaveFeatureTask();
    		saveFeatureTask.execute(currentFileName,currentImageAttrIDComponentSelected,"img",path);
    	}
    }
    
	private void setImage(){
		if(mappaPathImmagini!=null){
			for(int key : mappaPathImmagini.keySet()){
				String path = mappaPathImmagini.get(key);
				ImageView img = mappaImmagini.get(key);
				File imgFile = new  File(path);
				if(imgFile.exists() && imgFile.length()>0){
					try{
					Bitmap myBitmap = decodeFile(imgFile,false,true);
					img.setImageBitmap(myBitmap);
					
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					int noImg_id = resource.getIdentifier("noimage", "drawable","it.zerofill.soundmapp");
					img.setImageResource(noImg_id);
				}
			}
		}
	}
	
	private void showNextImgView(){
		for(int key : mappaImgToShow.keySet()){
			// if element in the map is false
			// it means it is not showed
			// so get the linearlayout and show it
			// then insert in the map that this element now is showed
			// and increment imageShowed
			if(!mappaImgToShow.get(key)){
				LinearLayout llToShow = mappaImmaginiView.get(key);
				llToShow.setVisibility(View.VISIBLE);
				mappaImgToShow.put(key, true);
				imgShowed++;
				break;
			}
		}
	}
	
	private void hideNextImgView(int id){
		if(imgShowed>1 ){ //&& imgShowed-countImg>=0
			// if there are more then one image components showed
			// hide this element
			// set to false in the map of element
			// decrease the counter
			LinearLayout llToShow = mappaImmaginiView.get(id);
			llToShow.setVisibility(View.GONE);
			mappaImgToShow.put(id, false);
			imgShowed--;
			
			LinearLayout parent = (LinearLayout)llToShow.getParent();
			int childNumber = parent.getChildCount();
			parent.removeView(llToShow);
			parent.addView(llToShow, childNumber-1);
		//	parent.addView(llToShow, parent.getChildCount()-1);
		//	parent.addView(llToShow, mainImgComponentLayout.getChildCount()-1);
			
		}
		
		int imgComponentShowed = 0;
		int imgComponentIsTaken = 0;
		for(int k : mappaImgToShow.keySet()){
			if(mappaImgToShow.get(k))
				imgComponentShowed++;
			if(mappaImgIsTaken.get(k))
				imgComponentIsTaken++;
		}
		if(imgComponentShowed<=imgComponentIsTaken)
			showNextImgView();
	}
	
	
	
//	@SuppressLint("SimpleDateFormat")
	private void snapPicture(int imgid){
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//    	String currentDateandTime = sdf.format(new Date());
//		imgName = currentDateandTime + ".jpg";
		
		imgName = currentImageAttrIDComponentSelected + "_" + surveyId + ".jpg";
		
		String file = imgDir+"/"+imgName;
		mappaPathImmagini.put(imgid, file);
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        stopPlaying();
        Uri outputFileUri = Uri.fromFile(newfile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, 0);
	}
  	
  	
    
  	
    private int maxPageNumber = 0;
    private void createTabs(){
    	
    	tabs = (TabHost) findViewById(R.id.tabhost);
		tabs.setup();
		TabHost.TabSpec spec = tabs.newTabSpec("maintab");
		//spec.setContent(R.id.mainlayoutTab);
		spec.setContent(mainlayoutTab.getId());
		spec.setIndicator("Main");
		tabs.addTab(spec);
		
		tabs.getTabWidget().getChildAt(0).setVisibility(View.GONE);
		tabs.setCurrentTab(1);
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		      @Override
		      public void onTabChanged(String tabId) {
		    	  try{
		    		  stopPlaying();
		    		  closeKeyboard();
		    	  }catch(Exception e){
		    		  e.printStackTrace();
		    	  }
		      }
		    });
		
		for(int i=1; i<maxPageNumber+1; i++){
			TabHost.TabSpec specTmp = tabs.newTabSpec("tag"+i);
			specTmp.setContent(new TabHost.TabContentFactory() {
				public View createTabContent(String tag) {
					return createView();
				}
			});
			specTmp.setIndicator("Pag. "+i);
			tabs.addTab(specTmp);
		}
		
		// Force to select all tabs to create all views (and collect data about mandatory fields)
		for(int j=1; j<=maxPageNumber; j++){
			tabs.setCurrentTab(j);
		}
		tabs.setCurrentTab(1);
		
		if(isNewSurvey){
			currentFileName = defaultSurveyKey;
			String column = config.getLayerByName(layerName).getKeyColumn();
			saveFeatureTask = new SaveFeatureTask();
			saveFeatureTask.execute(defaultSurveyKey,column);
		}
    }
  	
    private int SpinnerID = 0;
    private int FreeTextID = 100;
    private int ImageComponentID = 200;
    private int AudioComponentID = 300;
    private int DateComponentID = 400;
    private int PointsComponentID = 500;
    private int LineComponentID = 600;
    private int SingleImageComponentID = 700;
    private HashMap<Integer, String> spinnerCompMap;
    private HashMap<Integer, String> freeTextCompMap;
    private HashMap<Integer, String> imageCompMap;
    private HashMap<Integer, String> audioCompMap;
    private HashMap<Integer, String> pointsCompMap;
//    private HashMap<Integer, String> LineCompMap;
    private HashMap<String, ObjValue> recordsMap;
    
    private LinearLayout main;
  //  private LinearLayout mainImgComponentLayout;
    private String currentSurveyName;
    private View createView(){
        
  		int tabIndex = tabs.getCurrentTab();
  		
		LinearLayout.LayoutParams scrollParam = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		ScrollView scrollMainTmp = new ScrollView(mainContext);
		scrollMainTmp.setPadding(10, 0, 10, 0);
		scrollMainTmp.setLayoutParams(scrollParam);
		
		main = new LinearLayout(mainContext);
		LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		main.setLayoutParams(par);
		main.setOrientation(LinearLayout.VERTICAL);
		
		//recordsMap = getMapValueByColumnId();
		
		// First dynamic tab
		if(tabIndex==1){
			
			// New/Edit Survey Title
			LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT); 
			TextView titleView = new TextView(mainContext);
			titleView.setLayoutParams(textViewParams);
			titleView.setGravity(Gravity.CENTER);
			titleView.setTextAppearance(mainContext, R.style.headerText);
			titleView.setText(getString(R.string.prompt_survey_name));
			
			// New/Detail Survey name
			LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT); 
			EditText segnName = new EditText(mainContext);
			segnName.setLayoutParams(editTextViewParams);
			segnName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
			segnName.setMaxLines(1);
			segnName.setSingleLine(true);
			if(isNewSurvey){
				segnName.setText(defaultSurveyName);
				surveyName = defaultSurveyName;
				surveyId = defaultSurveyKey;
			}else{
				segnName.setText(surveyName);
				/*
				  
				// get surveyname from records where column = config.getLayerByName(layerName).getNameColumn();
				if(recordsMap.containsKey(config.getLayerByName(layerName).getNameColumn())){
					String sn = recordsMap.get(config.getLayerByName(layerName).getNameColumn()).getValue();
					segnName.setText(sn);
				}else
					segnName.setText(surveyName);
					
				*/
			}
			
			currentSurveyName = segnName.getText().toString();
			boolean columnNameExist = false;
			if(!"".equals(config.getLayerByName(layerName).getNameColumn())){
				columnNameExist = describeFeatureMap.containsKey(config.getLayerByName(layerName).getNameColumn());
			}
			
			// if in DB doesn't exist column with name of survey disable this field, because it will be the key to be used
			if(!isLocal || !columnNameExist)
				segnName.setEnabled(false);
			
			if(isNewSurvey || isLocal)
				segnName.setOnFocusChangeListener(new View.OnFocusChangeListener() {          
			        public void onFocusChange(View v, boolean hasFocus) {
			            if(!hasFocus){
			            	String text = ((EditText)v).getText().toString();
			            	String attrId = config.getLayerByName(layerName).getNameColumn();
			            
			            	if(!"".equals(attrId)){
				            	// DO NOT USE anymore
				            	/*
				            	if("".equals(attrId))
				            		attrId = config.getLayerByName(layerName).getKeyColumn();
				            	*/
				            	currentSurveyName = text.trim();
				            	((EditText)v).setText(text.trim());
				            	LocalFileHandler.updateName(localFileHandler, surveyId, text.trim(), loggedUser, layerName);
				            	//LocalFileHandler.updateName(localFileHandler, previousSurveyName, text.trim(), loggedUser);
			            	
			            	
			            		currentFileName = surveyId;
			            		saveFeatureTask = new SaveFeatureTask();
				        		saveFeatureTask.execute(currentFileName,attrId,text);	
			            	}
			            }
			        }
			    });
			
			segnName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			
			
			// ******************************************************************************
			// insert in the map the value for the Name of Survey, if is not specified in the config try to get the key
			String columnForNameAttr = config.getLayerByName(layerName).getNameColumn();
			if(columnForNameAttr==null || "".equals(columnForNameAttr))
				columnForNameAttr = config.getLayerByName(layerName).getKeyColumn();
			if("".equals(columnForNameAttr)){
				showGenericAlert(getString(R.string.warningLabel), getString(R.string.noSurveyNameFoundMessage),true);
			}else
				mappaValoriAttributi.put(columnForNameAttr, surveyName);
			// ******************************************************************************
			
			// Line
			LinearLayout lineraLineTmp = new LinearLayout(mainContext);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			lineraLineTmp.setLayoutParams(params);
			lineraLineTmp.setPadding(0, 50, 0, 50);
			lineraLineTmp.setOrientation(LinearLayout.VERTICAL);
			
			LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,4);
			View line = new View(mainContext);
			line.setLayoutParams(lineParams);
			line.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			lineraLineTmp.addView(line);
			
			main.addView(titleView);
			main.addView(segnName);
			main.addView(lineraLineTmp);
		}
			
		List<AttributeType> attributes = layer.getAttributesMapByPage().get(tabIndex);
		for(AttributeType attribute : attributes){
			
			ObjValue valore = null;
			if(!isNewSurvey){
				if(recordsMap.containsKey((attribute.getId())))
					valore = recordsMap.get(attribute.getId());
			}
			
			if(attribute.getType().equalsIgnoreCase(TYPE_COMBO) && describeFeatureMap.containsKey(attribute.getId())){
				addSpinner(main, attribute, valore);
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_STRING) && describeFeatureMap.containsKey(attribute.getId())){
				addFreeText(main, attribute, valore, "string");
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_IMAGE) && describeFeatureMap.containsKey(attribute.getId()) && attribute.getTypeConfig()!=null && attribute.getTypeConfig().size()>0){
			//	imgVisibileMap.put(ImageComponentID, true);
				
				LinearLayout mainImgComponentLayout = new LinearLayout(mainContext);
				mainImgComponentLayout.setLayoutParams(par);
				mainImgComponentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
				mainImgComponentLayout.setOrientation(LinearLayout.VERTICAL);
				
				TextView titleVieww = new TextView(mainContext);
				titleVieww.setLayoutParams(titleViewParams);
				if(mandatoryFieldMap.containsKey(attribute.getId()))
					titleVieww.setText("* " + attribute.getName()+ " ");
				else
					titleVieww.setText(attribute.getName()+ " ");
				
				mainImgComponentLayout.addView(titleVieww);	
				
				main.addView(mainImgComponentLayout);
				addImage(mainImgComponentLayout, attribute, valore, false);
			//	imgShowed ++;
				if(attribute.getTypeConfig()!=null){
					for(String idNextImg : attribute.getTypeConfig()){
						if(describeFeatureMap.containsKey(idNextImg)){
							mappaNextImgToShow.put(ImageComponentID-1, idNextImg);
							imgVisibileMap.put(ImageComponentID, false);
							AttributeType at = new AttributeType(idNextImg);
							if(recordsMap!=null && recordsMap.containsKey(at.getId()))
								valore = recordsMap.get(at.getId());
							else
								valore = null;
							addImage(mainImgComponentLayout, at, valore, true);
						}
					}
					if(!isNewSurvey && isLocal){
	    				int imgComponentShowed = 0;
	    				int imgComponentIsTaken = 0;
	    				for(int k : mappaImgToShow.keySet()){
	    					if(mappaImgToShow.get(k))
	    						imgComponentShowed++;
	    					if(mappaImgIsTaken.get(k))
	    						imgComponentIsTaken++;
	    				}
	    				if(imgComponentShowed<=imgComponentIsTaken){
	    					orderImgComponentByVisibility(mainImgComponentLayout);
	    					showNextImgView();
	    				}
					}
				}
			}
			
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_IMAGE) && describeFeatureMap.containsKey(attribute.getId())){
				addSingleImage(main, attribute, valore);
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_AUDIO) && describeFeatureMap.containsKey(attribute.getId())){
				addAudio(main, attribute);
			}
			else if(attribute.getType().equalsIgnoreCase(TYPE_DOUBLE) && describeFeatureMap.containsKey(attribute.getId())){
				addFreeText(main, attribute, valore, "double");
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_INT) && describeFeatureMap.containsKey(attribute.getId())){
				addFreeText(main, attribute, valore, "integer");
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_DATE) && describeFeatureMap.containsKey(attribute.getId())){
				addDate(main, attribute, valore);
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_POINT) && describeFeatureMap.containsKey(attribute.getId())){
				addPoint(main, attribute, valore);
			}
			
			else if(attribute.getType().equalsIgnoreCase(TYPE_LINE) && describeFeatureMap.containsKey(attribute.getId())){
				addLine(main, attribute, valore);
			}
			
			
			// else if(attr!=null && attr.getType().equals(" OTHERS TYPE ")){}
		}
			
		// Delay it to allow display preview 
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!isNewSurvey)
					setImage();
			}
		}, 500);
//		if(!isNewSurvey)
//			setImage();
		
		scrollMainTmp.addView(main);
		return scrollMainTmp;
  	}
    
    private void orderImgComponentByVisibility(LinearLayout layout){
    	int childNumber = layout.getChildCount();
    	List<LinearLayout> removedView = new ArrayList<LinearLayout>();
    	// first child is title
    	for(int i=1; i<childNumber; i++){
    		LinearLayout l = (LinearLayout)layout.getChildAt(i);
    		if(l.getVisibility()==View.GONE){
    			removedView.add(l);
    		}
    	}
    	for(LinearLayout layoutToAdd : removedView){
    		layout.removeView(layoutToAdd);
    		int index = layout.getChildCount();
    		layout.addView(layoutToAdd, index);
    	}
    	
    }
    
    
    private void addSpinner(LinearLayout main, AttributeType attr, ObjValue objv){
    	// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
    			
    	int elemPos = 1;
    	boolean isFound = false;
    	for(String elemento : attr.getTypeConfig()){
    		if(objv!=null && elemento.trim().equalsIgnoreCase(objv.getValue().trim())){
    			isFound = true;
    			
    			// check if it is in mandatory map and set its value to true
            	if(mandatoryFieldMap.containsKey(attr.getId())){
            		mandatoryFieldMap.put(attr.getId(),true);
            	}
    			
    			break;
    		}else{
    			elemPos++;
    		}
    	}
    	if(!isFound) elemPos = 0;
    	
    	LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		
	//	LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/16;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>16)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_SPINNER_DIM = 350;
		float FIXED_SPINNER_WIDTH_PERC = ((float) FIXED_SPINNER_DIM /WIDTH) * 100;
		int FIXED_SPINNER_WIDTH_VALUE = (int)((FIXED_SPINNER_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams spinnerViewParams = new LinearLayout.LayoutParams(FIXED_SPINNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
//		LinearLayout.LayoutParams spinnerViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
		Spinner spinnerView = new Spinner(mainContext);
		spinnerView.setId(SpinnerID);
		spinnerCompMap.put(SpinnerID, attr.getId());
		
		spinnerView.setLayoutParams(spinnerViewParams);
		List<String> elements = attr.getTypeConfig();
		elements.add(0, "");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainContext,android.R.layout.simple_spinner_item, elements);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setAdapter(dataAdapter);
		spinnerView.setSelection(elemPos);
		if(!isLocal) spinnerView.setEnabled(false);
		
		
		spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedItem = parent.getItemAtPosition(pos).toString();
                String attrId = spinnerCompMap.get(parent.getId());
                mappaValoriAttributi.put(attrId,selectedItem);
                if(!"".equals(selectedItem.trim())){
                	
                	// check if it is in mandatory map and set its value to true
                	if(mandatoryFieldMap.containsKey(attrId)){
                		mandatoryFieldMap.put(attrId,true);
                		errorFieldMap.put(attrId,false);
                	}
                	
                	saveFeatureTask = new SaveFeatureTask();
                	saveFeatureTask.execute(currentFileName,attrId,selectedItem);
                }else{
                	//delete from XML file
                	saveFeatureTask = new SaveFeatureTask();
                	saveFeatureTask.execute(currentFileName,attrId,null);
                	
                	// check if it is in mandatory map and set its value to false
                	if(mandatoryFieldMap.containsKey(attrId)){
                		// show error cannot be null
                		mandatoryFieldMap.put(attrId,false);
                		errorFieldMap.put(attrId,true);
                	}
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
		dataAdapter.notifyDataSetChanged();
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(spinnerView);
		
		main.addView(lineraTitleTmp);
		SpinnerID++;
    }
    
    
    
    private void addFreeText(LinearLayout main, AttributeType attr, ObjValue objv, String type){
    	// if it is mandatory insert it in the map
    	if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
    	
    	LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		
		//LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/16;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>16)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
		//LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
		EditText editTextView = new EditText(mainContext);
		editTextView.setLayoutParams(editTextViewParams);
		
		editTextView.setId(FreeTextID);
		freeTextCompMap.put(FreeTextID, attr.getId());
		//editTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);
		editTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
		editTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {          
	        public void onFocusChange(View v, boolean hasFocus) {
	            if(!hasFocus){
	            	String text = ((EditText)v).getText().toString();
	            	String attrId = freeTextCompMap.get(v.getId());
		        	mappaValoriAttributi.put(attrId,text);
		        	if(!"".equals(text.trim())){
		        		AttributeType attribute = attributesMap.get(attrId);
		        		double lowBoundary = 0;
		        		double highBoundary = 0;
		        		boolean hasError = false;
		        		boolean hasBoundary = false;
		        		if(attribute.getTypeConfig()!=null && attribute.getTypeConfig().size()>0){
		        			String low = attribute.getTypeConfig().get(0);
		        			String high = attribute.getTypeConfig().get(attribute.getTypeConfig().size()-1);
		        			try{
		        				lowBoundary = Double.parseDouble(low);
		        				highBoundary = Double.parseDouble(high);
		        				hasBoundary = true;
		        			}catch(Exception e){
		        				e.printStackTrace();
		        			}
		        		}
		        		if(attribute.getType().equalsIgnoreCase("Integer")){
		        			int number = 0;
		        			try{
		        				number = Integer.parseInt(text);
		        			}catch(Exception e){
		        				hasError = true;
		        				((EditText)v).setError(getString(R.string.NaNError));
		        			}
		        			if(hasBoundary && (number<lowBoundary || number>highBoundary)){
		        				hasError = true;
		        				String errorMessage = getString(R.string.invalidBoundariesError, lowBoundary, highBoundary);
		        				((EditText)v).setError(errorMessage);
		        			}
		        		}
		        		if(attribute.getType().equalsIgnoreCase("Double")){
		        			double number = 0;
		        			try{
		        				number = Double.parseDouble(text);
		        			}catch(Exception e){
		        				hasError = true;
		        				((EditText)v).setError(getString(R.string.NaNError));
		        			}
		        			if(hasBoundary && (number<lowBoundary || number>highBoundary)){
		        				hasError = true;
		        				String errorMessage = getString(R.string.invalidBoundariesError, lowBoundary, highBoundary);
		        				((EditText)v).setError(errorMessage);
		        			}
		        		}
		        		
		        		if(!hasError){
		        			// check if it is in mandatory map and set its value to true
		        			errorFieldMap.put(attrId,false);
		        			if(mandatoryFieldMap.containsKey(attrId)){
		                		// show error cannot be null
		                		mandatoryFieldMap.put(attrId,true);
		                	}
		        			
		        			saveFeatureTask = new SaveFeatureTask();
			        		saveFeatureTask.execute(currentFileName,attrId,text);	
		        		}else{
		        			//v.requestFocus();
		        			errorFieldMap.put(attrId,true);
		        		}
		        	}else{
		        		//delete from XML file
		        		saveFeatureTask = new SaveFeatureTask();
		        		saveFeatureTask.execute(currentFileName,attrId,null);
		        		// check if it is in mandatory map and set its value to false
	                	if(mandatoryFieldMap.containsKey(attrId)){
	                		// show error cannot be null
	                		mandatoryFieldMap.put(attrId,false);
	                	}
		        	}
	            }
	        }
	    });
		
		editTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		editTextView.setMinLines(3);
		editTextView.setMaxLines(3);
		if(objv!=null && objv.getValue()!=null){
			editTextView.setText(objv.getValue());
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        	}
		}
		if(!isLocal)
			editTextView.setEnabled(false);
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(editTextView);
		
		main.addView(lineraTitleTmp);
		FreeTextID++;
		
    }
    
    
    private void addDate(LinearLayout main, AttributeType attr, ObjValue objv){
    	// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
    	
    	LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		
		
		//LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/16;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>16)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams dateTextViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
	//	LinearLayout.LayoutParams dateTextViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
		
		EditText dateTextView = new EditText(mainContext);
		dateTextView.setLayoutParams(dateTextViewParams);
		dateTextView.setEnabled(false);
		
		dateTextView.setId(DateComponentID);
		mappaDateComponent.put(DateComponentID, dateTextView);
		mappaDateComponentId.put(DateComponentID, attr.getId());
		
		if(objv!=null && objv.getValue()!=null){
			
            // check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        		errorFieldMap.put(attr.getId(),false);
        	}
        	
			String dateString = objv.getValue();
			int y = year; int m = month; int d = day;
			try{
				y = Integer.parseInt(dateString.substring(0, 4));
				m = Integer.parseInt(dateString.substring(5, 7));
				d =	Integer.parseInt(dateString.substring(8, 10));
			}catch(Exception e){
				y = year;
				m = month;
				d = day;
				e.printStackTrace();
			}
			updateDateView(dateTextView, y, m, d);
		}
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 65;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
		        
//		LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	//	LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(65,65);
        

        ImageView dateButtonOpenerImg = new ImageView(mainContext);
        dateButtonOpenerImg.setImageResource(calendar_img_button_id);
        dateButtonOpenerImg.setId(DateComponentID);
        dateButtonOpenerImg.setLayoutParams(buttonViewParams); //layout_button_params
        dateButtonOpenerImg.setOnTouchListener(new View.OnTouchListener() {
        	@SuppressWarnings("deprecation")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)v).setImageResource(calendar_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)v).setImageResource(calendar_img_button_id);
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	                selectedDateComponentId = v.getId();
	                isDateChanged = true;
	                
	                String attrId = mappaDateComponentId.get(v.getId());
	                // check if it is in mandatory map and set its value to true
	            	if(mandatoryFieldMap.containsKey(attrId)){
	            		mandatoryFieldMap.put(attrId,true);
	            	}
	                
	                showDialog(999);
				}
				return true;
			}
		});
        
        
        
        ImageView dateResetButtonImg = new ImageView(mainContext);
        dateResetButtonImg.setImageResource(del_img_button_id);
        dateResetButtonImg.setId(DateComponentID);
        dateResetButtonImg.setLayoutParams(buttonViewParams); //layout_button_params
        dateResetButtonImg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)v).setImageResource(del_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)v).setImageResource(del_img_button_id);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	                selectedDateComponentId = v.getId();
	                String attrId = mappaDateComponentId.get(v.getId());
	                
	                TextView tv = mappaDateComponent.get(selectedDateComponentId);
	                tv.setText("");
	                
	                //delete from XML file
	            	saveFeatureTask = new SaveFeatureTask();
	            	saveFeatureTask.execute(currentFileName,attrId,null);
	                
	                // check if it is in mandatory map and set its value to true
	            	if(mandatoryFieldMap.containsKey(attrId)){
	            		mandatoryFieldMap.put(attrId,false);
	            		errorFieldMap.put(attrId,true);
	            	}
					
				}
				return true;
			}
		});
        
        
        /*
		Button dateButtonOpener = new Button(mainContext);
		dateButtonOpener.setId(DateComponentID);
		dateButtonOpener.setLayoutParams(buttonViewParams);
		dateButtonOpener.setBackgroundResource(R.drawable.calendar_btn);
		dateButtonOpener.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View view) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                selectedDateComponentId = view.getId();
                isDateChanged = true;
                
                String attrId = mappaDateComponentId.get(view.getId());
                // check if it is in mandatory map and set its value to true
            	if(mandatoryFieldMap.containsKey(attrId)){
            		mandatoryFieldMap.put(attrId,true);
            	}
                
                showDialog(999);
			}
    	});
		
		
		Button dateResetButton = new Button(mainContext);
		dateResetButton.setId(DateComponentID);
		dateResetButton.setLayoutParams(buttonViewParams);
		dateResetButton.setBackgroundResource(R.drawable.delete_btn);
		dateResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                selectedDateComponentId = view.getId();
                String attrId = mappaDateComponentId.get(view.getId());
                
                TextView tv = mappaDateComponent.get(selectedDateComponentId);
                tv.setText("");
                
                //delete from XML file
            	saveFeatureTask = new SaveFeatureTask();
            	saveFeatureTask.execute(currentFileName,attrId,null);
                
                // check if it is in mandatory map and set its value to true
            	if(mandatoryFieldMap.containsKey(attrId)){
            		mandatoryFieldMap.put(attrId,false);
            		errorFieldMap.put(attrId,true);
            	}
                
               
			}
    	});
		*/
        
        
		if(!isLocal){
//			dateButtonOpener.setEnabled(false);
//			dateResetButton.setEnabled(false);
			
			dateButtonOpenerImg.setEnabled(false);
			dateButtonOpenerImg.setImageResource(calendar_img_button_disabled_id);
			dateResetButtonImg.setEnabled(false);
			dateResetButtonImg.setImageResource(del_img_button_disabled_id);
		}
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(dateTextView);
//		lineraTitleTmp.addView(dateButtonOpener);
//		lineraTitleTmp.addView(dateResetButton);
		lineraTitleTmp.addView(dateButtonOpenerImg);
		lineraTitleTmp.addView(dateResetButtonImg);
		
		main.addView(lineraTitleTmp);
		DateComponentID++;
    }
    
    
    
    
    
    
    
    private void addLine(LinearLayout main, AttributeType attr, ObjValue objv){
    	// if it is mandatory insert it in the map
    	if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
    	
    	LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		
	//	LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/16;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>16)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
	//	LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
		Button viewPositionButton = new Button(mainContext);
		viewPositionButton.setLayoutParams(textViewParams);
		viewPositionButton.setId(LineComponentID);
		viewPositionButton.setText(getString(R.string.viewDataLabel));
		viewPositionButton.setEnabled(false);
		viewPositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mappaCoordinates.containsKey(view.getId())){
					String message = "";
					List<double[]> list = mappaCoordinates.get(view.getId());
					for(double[] coord : list){
						message += coord[0]+"; "+coord[1] + "\n";
					}
					showDataDialog(message);	
				}
			}
    	});
		
//		GPSTracker gps = new GPSTracker(mainContext);
		Chronometer chronometerPosition = new Chronometer(mainContext);
		chronometerPosition.setId(LineComponentID);
		chronometerPosition.setOnChronometerTickListener(new OnChronometerTickListener() {
			@Override
			public void onChronometerTick(Chronometer ch) {
				//GPSTracker gps = gpsComponent.get(ch.getId());
				
				long elapsedMillis = SystemClock.elapsedRealtime() - ch.getBase();
				long elapsedSecs = elapsedMillis / 1000;
				
				if(elapsedSecs==0 || (elapsedSecs>1 && elapsedSecs % base_di_campionamento == 0)){
					try{
						GPSTracker gps = new GPSTracker(mainContext);
						double[] location = getLocation(gps);
						
						if(mappaCoordinates.containsKey(ch.getId())){
							mappaCoordinates.get(ch.getId()).add(location);
						}else{
							List<double[]> l = new ArrayList<double[]>();
							l.add(location);
							mappaCoordinates.put(ch.getId(), l);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 65;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
		        
		LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		chronometerPosition.setLayoutParams(buttonParams);
		chronometerPosition.setVisibility(View.GONE);
		
		
		
		
		
		ImageView takePositionButtonImg = new ImageView(mainContext);
		takePositionButtonImg.setLayoutParams(buttonViewParams);
		takePositionButtonImg.setId(LineComponentID);
		takePositionButtonImg.setImageResource(location_img_button_id);
		takePositionButtonImg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)v).setImageResource(location_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)v).setImageResource(location_img_button_id);
					boolean isRecordingPosition = chronoPointsIsRecording.get(v.getId());
					Chronometer chronometer = chronoPointsComponent.get(v.getId());
					String attrId = pointsCompMap.get(v.getId());
					
					if(!isRecordingPosition){
						((Button)v).setBackgroundResource(R.drawable.location_on_btn);
						// disable view data button while is recording GSP location
						pointsViewDataComponent.get(v.getId()).setEnabled(false);
						chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.start();
					}else{
						((Button)v).setBackgroundResource(R.drawable.location_off_btn);
						chronometer.stop();
						
						if(mappaCoordinates.containsKey(v.getId())){
							String value = "";
							List<double[]> list = mappaCoordinates.get(v.getId());
							if(list.size()==1){
								double[] tmp = list.get(0);
								list.add(tmp);
							}
							else if(list.size()>2){
								double[] tmp0 = list.get(0);
								double[] tmp1 = list.get(1);
								if(tmp0[0]==tmp1[0] && tmp0[1]==tmp1[1])
									list.remove(0);
							}
									
							for(double[] coord : list){
								// 2014-07-08 inverted longitude and latitude (the new value will be [longitude, latitude])
//								value += coord[0]+","+coord[1] + " ";
								value += coord[1]+","+coord[0] + " ";
							}
							//value = value.substring(0, value.length()-1);

							if(!value.equals("")){
								// if there are points enable button to show data
								pointsViewDataComponent.get(v.getId()).setEnabled(true);
								
								// check if it is in mandatory map and set its value to true
				            	if(mandatoryFieldMap.containsKey(attrId)){
				            		mandatoryFieldMap.put(attrId,true);
				            	}
				            	
								// write data in xml
								saveFeatureTask = new SaveFeatureTask();
			                	//saveFeatureTask.execute(currentFileName,attrId,value);
			                	saveFeatureTask.execute(currentFileName,attrId,TYPE_LINE,value);
							}
						}
					}
					chronoPointsIsRecording.put(v.getId(), !isRecordingPosition);
					
				}
				return true;
			}
		});
		
		
		/*
		Button takePositionButton = new Button(mainContext);
		takePositionButton.setLayoutParams(buttonViewParams);
		takePositionButton.setId(LineComponentID);
		takePositionButton.setBackgroundResource(R.drawable.location_off_btn);
		takePositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean isRecordingPosition = chronoPointsIsRecording.get(view.getId());
				Chronometer chronometer = chronoPointsComponent.get(view.getId());
				String attrId = pointsCompMap.get(view.getId());
				
				if(!isRecordingPosition){
					((Button)view).setBackgroundResource(R.drawable.location_on_btn);
					// disable view data button while is recording GSP location
					pointsViewDataComponent.get(view.getId()).setEnabled(false);
					chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();
				}else{
					((Button)view).setBackgroundResource(R.drawable.location_off_btn);
					chronometer.stop();
					
					if(mappaCoordinates.containsKey(view.getId())){
						String value = "";
						List<double[]> list = mappaCoordinates.get(view.getId());
						if(list.size()==1){
							double[] tmp = list.get(0);
							list.add(tmp);
						}
						else if(list.size()>2){
							double[] tmp0 = list.get(0);
							double[] tmp1 = list.get(1);
							if(tmp0[0]==tmp1[0] && tmp0[1]==tmp1[1])
								list.remove(0);
						}
								
						for(double[] coord : list){
							// 2014-07-08 inverted longitude and latitude (the new value will be [longitude, latitude])
//							value += coord[0]+","+coord[1] + " ";
							value += coord[1]+","+coord[0] + " ";
						}
						//value = value.substring(0, value.length()-1);

						if(!value.equals("")){
							// if there are points enable button to show data
							pointsViewDataComponent.get(view.getId()).setEnabled(true);
							
							// check if it is in mandatory map and set its value to true
			            	if(mandatoryFieldMap.containsKey(attrId)){
			            		mandatoryFieldMap.put(attrId,true);
			            	}
			            	
							// write data in xml
							saveFeatureTask = new SaveFeatureTask();
		                	//saveFeatureTask.execute(currentFileName,attrId,value);
		                	saveFeatureTask.execute(currentFileName,attrId,TYPE_LINE,value);
						}
					}
				}
				chronoPointsIsRecording.put(view.getId(), !isRecordingPosition);
			}
    	});
    	*/
		
		// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
		
		if(objv!=null && objv.getValue()!=null){
			viewPositionButton.setEnabled(true);
			// parse input field with location to double[2]
			// ex. 40.73158 -73.999559 40.732188 -73.999079
			List<double[]> coordList = new ArrayList<double[]>();
			
			if(objv.getValue().contains(",")){ // from local file
				StringTokenizer tokenizer = new StringTokenizer(objv.getValue()); // all point separated by space
				while(tokenizer.hasMoreTokens()){
					StringTokenizer tokenizerPoint = new StringTokenizer(tokenizer.nextToken(),","); // couple of points
					String latitudeStr = tokenizerPoint.nextToken();
					String longitudeStr = tokenizerPoint.nextToken();
					
					double latitude = Double.parseDouble(latitudeStr);
					double longitude = Double.parseDouble(longitudeStr);
					
					double[] location = new double[2];
					location[0] = latitude;
					location[1] = longitude;
					coordList.add(location);
				}
			}else{ // from geoserver
				StringTokenizer tokenizer = new StringTokenizer(objv.getValue()); // all point separated by space
				while(tokenizer.hasMoreTokens()){
					String latitudeStr = tokenizer.nextToken();
					String longitudeStr = tokenizer.nextToken();
					
					double latitude = Double.parseDouble(latitudeStr);
					double longitude = Double.parseDouble(longitudeStr);
					double[] location = new double[2];
					location[0] = latitude;
					location[1] = longitude;
					coordList.add(location);
				}
			}
			
			
			mappaCoordinates.put(LineComponentID, coordList);
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        	}
		}
		
		
		ImageView pointsResetButtonImg = new ImageView(mainContext);
		pointsResetButtonImg.setId(LineComponentID);
		pointsResetButtonImg.setLayoutParams(buttonViewParams);
		pointsResetButtonImg.setImageResource(del_img_button_id);
		pointsResetButtonImg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)view).setImageResource(del_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)view).setImageResource(del_img_button_id);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	                int selectedPComponentId = view.getId();
	                String attrId = pointsCompMap.get(view.getId());
	                
	                if(mappaCoordinates.containsKey(selectedPComponentId)){
	                	mappaCoordinates.remove(selectedPComponentId);
	                	
	                	// disable view data button
	                	pointsViewDataComponent.get(selectedPComponentId).setEnabled(false);
	                	
	                	//delete from XML file
	                	saveFeatureTask = new SaveFeatureTask();
	                	saveFeatureTask.execute(currentFileName,attrId,null);
	                    
	                    // check if it is in mandatory map and set its value to true
	                	if(mandatoryFieldMap.containsKey(attrId)){
	                		mandatoryFieldMap.put(attrId,false);
	                	}
	                }
					
				}
				return true;
			}
		});
		
		/*
		Button pointsResetButton = new Button(mainContext);
		pointsResetButton.setId(LineComponentID);
		pointsResetButton.setLayoutParams(buttonViewParams);
		pointsResetButton.setBackgroundResource(R.drawable.delete_btn);
		pointsResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                int selectedPComponentId = view.getId();
                String attrId = pointsCompMap.get(view.getId());
                
                if(mappaCoordinates.containsKey(selectedPComponentId)){
                	mappaCoordinates.remove(selectedPComponentId);
                	
                	// disable view data button
                	pointsViewDataComponent.get(selectedPComponentId).setEnabled(false);
                	
                	//delete from XML file
                	saveFeatureTask = new SaveFeatureTask();
                	saveFeatureTask.execute(currentFileName,attrId,null);
                    
                    // check if it is in mandatory map and set its value to true
                	if(mandatoryFieldMap.containsKey(attrId)){
                		mandatoryFieldMap.put(attrId,false);
                	}
                }
			}
    	});
		*/
		
		pointsCompMap.put(LineComponentID, attr.getId());
		pointsViewDataComponent.put(LineComponentID, viewPositionButton);
		chronoPointsComponent.put(LineComponentID, chronometerPosition);
//		gpsComponent.put(LineComponentID, gps);
		chronoPointsIsRecording.put(LineComponentID, false);
		
		if(!isLocal){
//			takePositionButton.setEnabled(false);
//			pointsResetButton.setEnabled(false);
			
			takePositionButtonImg.setEnabled(false);
			takePositionButtonImg.setImageResource(location_img_button_disabled_id);
			pointsResetButtonImg.setEnabled(false);
			pointsResetButtonImg.setImageResource(del_img_button_disabled_id);
			
			
		}
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(viewPositionButton);
//		lineraTitleTmp.addView(takePositionButton);
//		lineraTitleTmp.addView(pointsResetButton);
		lineraTitleTmp.addView(takePositionButtonImg);
		lineraTitleTmp.addView(pointsResetButtonImg);
		lineraTitleTmp.addView(chronometerPosition);
		main.addView(lineraTitleTmp);
		LineComponentID++;
    }
    
    
    
    
    
    
    private void addPoint(LinearLayout main, AttributeType attr, ObjValue objv){
    	// if it is mandatory insert it in the map
    	if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
    	
    	LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		
	//	LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/16;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>16)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);

		Button viewPositionButton = new Button(mainContext);
		viewPositionButton.setLayoutParams(textViewParams);
		viewPositionButton.setId(PointsComponentID);
		viewPositionButton.setText(getString(R.string.viewDataLabel));
		viewPositionButton.setEnabled(false);
		viewPositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mappaCoordinates.containsKey(view.getId())){
					String message = "";
					List<double[]> list = mappaCoordinates.get(view.getId());
					for(double[] coord : list){
						message += coord[0]+"; "+coord[1] + "\n";
					}
					showDataDialog(message);	
				}
			}
    	});
		
	//	GPSTracker gps = new GPSTracker(mainContext);
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 65;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
		
		
        ImageView takePositionButtonImg = new ImageView(mainContext);
        takePositionButtonImg.setLayoutParams(buttonViewParams);
        takePositionButtonImg.setId(PointsComponentID);
        takePositionButtonImg.setImageResource(location_img_button_id);
        takePositionButtonImg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)view).setImageResource(location_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)view).setImageResource(location_img_button_id);
					
					//GPSTracker gps = gpsComponent.get(view.getId());
					try{
						GPSTracker gps = new GPSTracker(mainContext);
						double[] location = getLocation(gps);
						List<double[]> l = new ArrayList<double[]>();
						l.add(location);
						mappaCoordinates.put(view.getId(), l);
						pointsViewDataComponent.get(view.getId()).setEnabled(true);
						
						String attrId = pointsCompMap.get(view.getId());
						// write data in xml
						saveFeatureTask = new SaveFeatureTask();
//		            	saveFeatureTask.execute(currentFileName,attrId,TYPE_POINT,location[0] + "," + location[1]);
						// 2014-07-08 inverted longitude and latitude (the new value will be [longitude, latitude])
		            	saveFeatureTask.execute(currentFileName,attrId,TYPE_POINT,location[1] + "," + location[0]);
		            	
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				return true;
			}
		});
        
        /*
        Button takePositionButton = new Button(mainContext);
		takePositionButton.setLayoutParams(buttonViewParams);
		takePositionButton.setId(PointsComponentID);
		takePositionButton.setBackgroundResource(R.drawable.location_off_btn);
		takePositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				//GPSTracker gps = gpsComponent.get(view.getId());
				try{
					GPSTracker gps = new GPSTracker(mainContext);
					double[] location = getLocation(gps);
					List<double[]> l = new ArrayList<double[]>();
					l.add(location);
					mappaCoordinates.put(view.getId(), l);
					pointsViewDataComponent.get(view.getId()).setEnabled(true);
					
					String attrId = pointsCompMap.get(view.getId());
					// write data in xml
					saveFeatureTask = new SaveFeatureTask();
//	            	saveFeatureTask.execute(currentFileName,attrId,TYPE_POINT,location[0] + "," + location[1]);
					// 2014-07-08 inverted longitude and latitude (the new value will be [longitude, latitude])
	            	saveFeatureTask.execute(currentFileName,attrId,TYPE_POINT,location[1] + "," + location[0]);
	            	
				}catch(Exception e){
					e.printStackTrace();
				}
			}
    	});
    	*/
		
		// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
		
		if(objv!=null && objv.getValue()!=null){
			viewPositionButton.setEnabled(true);
			// parse input field with location to double[2]
			// ex. 40.73158 -73.999559 40.732188 -73.999079
			List<double[]> coordList = new ArrayList<double[]>();
			StringTokenizer tokenizer = null;
			if(objv.getValue().contains(","))
				tokenizer = new StringTokenizer(objv.getValue(),",");
			else
				tokenizer = new StringTokenizer(objv.getValue());
			while(tokenizer.hasMoreTokens()){
				String latitudeStr = tokenizer.nextToken();
				String longitudeStr = tokenizer.nextToken();
				
				double latitude = Double.parseDouble(latitudeStr);
				double longitude = Double.parseDouble(longitudeStr);
				
				double[] location = new double[2];
				location[0] = latitude;
				location[1] = longitude;
				coordList.add(location);
			}
			mappaCoordinates.put(PointsComponentID, coordList);
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        	}
		}
		
		
		ImageView pointsResetButtonImg = new ImageView(mainContext);
		pointsResetButtonImg.setId(PointsComponentID);
		pointsResetButtonImg.setLayoutParams(buttonViewParams);
		pointsResetButtonImg.setImageResource(del_img_button_id);
		pointsResetButtonImg.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					((ImageView)view).setImageResource(del_img_button_pressed_id);
					try{
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					((ImageView)view).setImageResource(del_img_button_id);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	                int selectedPComponentId = view.getId();
	                String attrId = pointsCompMap.get(view.getId());
	                
	                if(mappaCoordinates.containsKey(selectedPComponentId)){
	                	mappaCoordinates.remove(selectedPComponentId);
	                	
	                	// disable view data button
	                	pointsViewDataComponent.get(selectedPComponentId).setEnabled(false);
	                	
	                	//delete from XML file
	                	saveFeatureTask = new SaveFeatureTask();
	                	saveFeatureTask.execute(currentFileName,attrId,null);
	                    
	                    // check if it is in mandatory map and set its value to true
	                	if(mandatoryFieldMap.containsKey(attrId)){
	                		mandatoryFieldMap.put(attrId,false);
	                	}
	                }
					
				}
				return true;
			}
		});
		
		/*
		Button pointsResetButton = new Button(mainContext);
		pointsResetButton.setId(PointsComponentID);
		pointsResetButton.setLayoutParams(buttonViewParams);
		pointsResetButton.setBackgroundResource(R.drawable.delete_btn);
		pointsResetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                int selectedPComponentId = view.getId();
                String attrId = pointsCompMap.get(view.getId());
                
                if(mappaCoordinates.containsKey(selectedPComponentId)){
                	mappaCoordinates.remove(selectedPComponentId);
                	
                	// disable view data button
                	pointsViewDataComponent.get(selectedPComponentId).setEnabled(false);
                	
                	//delete from XML file
                	saveFeatureTask = new SaveFeatureTask();
                	saveFeatureTask.execute(currentFileName,attrId,null);
                    
                    // check if it is in mandatory map and set its value to true
                	if(mandatoryFieldMap.containsKey(attrId)){
                		mandatoryFieldMap.put(attrId,false);
                	}
                }
			}
    	});
    	*/
		
		pointsCompMap.put(PointsComponentID, attr.getId());
		pointsViewDataComponent.put(PointsComponentID, viewPositionButton);
	//	gpsComponent.put(PointsComponentID, gps);
		
		if(!isLocal){
//			takePositionButton.setEnabled(false);
//			pointsResetButton.setEnabled(false);
			
			takePositionButtonImg.setEnabled(false);
			takePositionButtonImg.setImageResource(location_img_button_disabled_id);
			pointsResetButtonImg.setEnabled(false);
			pointsResetButtonImg.setImageResource(del_img_button_disabled_id);
		}
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(viewPositionButton);
//		lineraTitleTmp.addView(takePositionButton);
//		lineraTitleTmp.addView(pointsResetButton);
		lineraTitleTmp.addView(takePositionButtonImg);
		lineraTitleTmp.addView(pointsResetButtonImg);
	
		main.addView(lineraTitleTmp);
		PointsComponentID++;
    }
    
    private double[] getLocation(GPSTracker gps){
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
    
    
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 999:
            return new DatePickerDialog(this, dateSetListener, year, month, day);
        }
        return null;
    }

	private boolean isSnappedPicture;
	private boolean isRecordedAudio;
	
	private void addImage(LinearLayout main, AttributeType attr, ObjValue objv, boolean visibilityGone){
		
		// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
				
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		LinearLayout lineraOuterTmp = new LinearLayout(mainContext);
		lineraOuterTmp.setLayoutParams(paramss);
		lineraOuterTmp.setOrientation(LinearLayout.VERTICAL);
		lineraOuterTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraOuterTmp.setId(ImageComponentID);
		lineraOuterTmp.setPadding(5, 25, 5, 25);
		
		mappaImmaginiView.put(ImageComponentID, lineraOuterTmp);
		mappaImmaginiComponent.put(attr.getId(), ImageComponentID);
		
	//	LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT);
		/*
		if(!visibilityGone){
			TextView titleVieww = new TextView(mainContext);
			titleVieww.setLayoutParams(titleViewParams);
			if(mandatoryFieldMap.containsKey(attr.getId()))
				titleVieww.setText("* " + attr.getName()+ " ");
			else
				titleVieww.setText(attr.getName()+ " ");
			
			lineraOuterTmp.addView(titleVieww);	
		}
		*/
		 
		
		LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		lineraTitleTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraTitleTmp.setId(ImageComponentID);
		lineraTitleTmp.setPadding(0, 5, 0, 0);
		
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 160;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
        
//		LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(160,160);
		ImageView imageView = new ImageView(mainContext);
		imageView.setLayoutParams(imageViewParams);
		imageView.setId(ImageComponentID);
		
		mappaImmagini.put(ImageComponentID, imageView);
		imageCompMap.put(ImageComponentID, attr.getId());
		
		int noImg_id = resource.getIdentifier("noimage", "drawable","it.zerofill.soundmapp");
		imageView.setImageResource(noImg_id);
		
		mappaImgIsTaken.put(ImageComponentID,false);
		
		imageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
				}else if(event.getAction()==1){ //==1 means ACTION_UP && existImage
					int imgid =v.getId();
					if(mappaPathImmagini.containsKey(imgid) && !"".equals(mappaPathImmagini.get(imgid))){
						stopPlaying();
						String path = mappaPathImmagini.get(imgid);
						Intent i = new Intent(mainContext, ShowFullImageActivity.class);
						i.putExtra("imgPath", path);
						startActivity(i);
					}
				}
				return true;
			}
		});
		
		ImageView snap_img_button = new ImageView(mainContext);
		snap_img_button.setLayoutParams(layout_button_params);
		snap_img_button.setImageResource(snap_img_button_id);
		snap_img_button.setId(ImageComponentID);
		snap_img_button.setPadding(10, 0, 10, 0);
		
		ImageView del_img_button = new ImageView(mainContext);
		del_img_button.setLayoutParams(layout_button_params);
		del_img_button.setImageResource(del_img_button_id);
		del_img_button.setId(ImageComponentID);
		del_img_button.setPadding(10, 0, 10, 0);
		
		if(isNewSurvey || isLocal){
			snap_img_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(snap_img_button_pressed_id);
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						((ImageView)v).setImageResource(snap_img_button_id);
						int imgid =v.getId();
						
						String path = mappaPathImmagini.get(imgid);
						File imgFile = null;
						if(path!=null)
							imgFile = new  File(path);
						
						if(path!=null && imgFile.exists() && imgFile.length()>0){

							showGenericAlert(getString(R.string.warningLabel),getString(R.string.delImgBeforeSnapMessage),false);
							
						}else{
							try{
								//int imgid =((View)v.getParent()).getId();
								isSnappedPicture = true;
								isSingleImage = false;
								currentImageAttrIDComponentSelected = imageCompMap.get(imgid);
								currentImageIDComponentSelected = imgid;
								snapPicture(imgid);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
					}
					return true;
				}
			});
			
			del_img_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(del_img_button_pressed_id);
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						((ImageView)v).setImageResource(del_img_button_id);
						try{
							int imgid =v.getId();
							isSingleImage = false;
							String path = mappaPathImmagini.get(imgid);
							File imgFile = null;
							if(path!=null)
								imgFile = new  File(path);
							
							if(path!=null && imgFile.exists() && imgFile.length()>0){
							
							//if(mappaPathImmagini.containsKey(imgid)){
								currentImageAttrIDComponentSelected = imageCompMap.get(imgid);
								currentImageIDComponentSelected = imgid;
								showDeleteImageAlert(imgid);
							}
							
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return true;
				}
			});	
		}else{
			snap_img_button.setImageResource(snap_img_button_disabled_id);
			del_img_button.setImageResource(del_img_button_disabled_id);
		}
		
		mappaDeleteImgButton.put(ImageComponentID, del_img_button);
		del_img_button.setEnabled(false);
		del_img_button.setImageResource(del_img_button_disabled_id);
		
		boolean hasImage = false;
		if(objv!=null && objv.getValue()!=null && objv.getValue().length()>0){
			del_img_button.setEnabled(true);
			del_img_button.setImageResource(del_img_button_id);
			mappaImgIsTaken.put(ImageComponentID,true);
			//String imgFileName = imgDir+"/"+surveyName+".jpg";
			String imgFileName = imgDir+"/"+attr.getId() + "_" + surveyId + ".jpg";
			mappaPathImmagini.put(ImageComponentID, imgFileName);
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        	}
        	
        	hasImage = true;
			
			SaveFileTask task = new SaveFileTask();
			task.execute(objv.getValue(), imgFileName, ImageComponentID+""+ "image");
		}
		
		lineraTitleTmp.addView(imageView);
		
		LinearLayout linearButtons = new LinearLayout(mainContext);
		LinearLayout.LayoutParams linearButtonsParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		linearButtons.setLayoutParams(linearButtonsParam);
		linearButtons.setOrientation(LinearLayout.HORIZONTAL);
		linearButtons.setPadding(20, 0, 20, 0);
	//	linearButtons.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

		linearButtons.addView(snap_img_button);
		linearButtons.addView(del_img_button);
		lineraTitleTmp.addView(linearButtons);
		
		lineraOuterTmp.addView(lineraTitleTmp);
		main.addView(lineraOuterTmp);
		
		if(visibilityGone && !hasImage){
			lineraOuterTmp.setVisibility(View.GONE);
			mappaImgToShow.put(ImageComponentID, false);
		}else{
			mappaImgToShow.put(ImageComponentID, true);
			imgShowed++;
		}
		
		
		
		
	//	main.addView(lineraTitleTmp);
		ImageComponentID++;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private void addSingleImage(LinearLayout main, AttributeType attr, ObjValue objv){
		
		// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
				
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		LinearLayout lineraOuterTmp = new LinearLayout(mainContext);
		lineraOuterTmp.setLayoutParams(paramss);
		lineraOuterTmp.setOrientation(LinearLayout.VERTICAL);
		lineraOuterTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraOuterTmp.setId(SingleImageComponentID);
		lineraOuterTmp.setPadding(5, 25, 5, 25);
		
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		lineraOuterTmp.addView(titleVieww);	
		
		LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		lineraTitleTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraTitleTmp.setId(SingleImageComponentID);
		lineraTitleTmp.setPadding(0, 5, 0, 0);
		
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 160;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
        
//		LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(160,160);
		ImageView imageView = new ImageView(mainContext);
		imageView.setLayoutParams(imageViewParams);
		imageView.setId(SingleImageComponentID);
		
		mappaImmagini.put(SingleImageComponentID, imageView);
		imageCompMap.put(SingleImageComponentID, attr.getId());
		
		int noImg_id = resource.getIdentifier("noimage", "drawable","it.zerofill.soundmapp");
		imageView.setImageResource(noImg_id);
		
		imageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
				}else if(event.getAction()==1){ //==1 means ACTION_UP && existImage
					int imgid =v.getId();
					if(mappaPathImmagini.containsKey(imgid) && !"".equals(mappaPathImmagini.get(imgid))){
						stopPlaying();
						String path = mappaPathImmagini.get(imgid);
						Intent i = new Intent(mainContext, ShowFullImageActivity.class);
						i.putExtra("imgPath", path);
						startActivity(i);
					}
				}
				return true;
			}
		});
		
		ImageView snap_img_button = new ImageView(mainContext);
		snap_img_button.setLayoutParams(layout_button_params);
		snap_img_button.setImageResource(snap_img_button_id);
		snap_img_button.setId(SingleImageComponentID);
		snap_img_button.setPadding(10, 0, 10, 0);
		
		ImageView del_img_button = new ImageView(mainContext);
		del_img_button.setLayoutParams(layout_button_params);
		del_img_button.setImageResource(del_img_button_id);
		del_img_button.setId(SingleImageComponentID);
		del_img_button.setPadding(10, 0, 10, 0);
		
		if(isNewSurvey || isLocal){
			snap_img_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(snap_img_button_pressed_id);
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						((ImageView)v).setImageResource(snap_img_button_id);
						int imgid =v.getId();
						
						String path = mappaPathImmagini.get(imgid);
						File imgFile = null;
						if(path!=null)
							imgFile = new  File(path);
						
						if(path!=null && imgFile.exists() && imgFile.length()>0){

							showGenericAlert(getString(R.string.warningLabel),getString(R.string.delImgBeforeSnapMessage),false);
							
						}else{
							try{
								//int imgid =((View)v.getParent()).getId();
								isSnappedPicture = true;
								isSingleImage = true;
								
								currentImageAttrIDComponentSelected = imageCompMap.get(imgid);
								currentImageIDComponentSelected = imgid;
								snapPicture(imgid);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
					}
					return true;
				}
			});
			
			del_img_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(del_img_button_pressed_id);
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						((ImageView)v).setImageResource(del_img_button_id);
						try{
							int imgid =v.getId();
							
							String path = mappaPathImmagini.get(imgid);
							File imgFile = null;
							if(path!=null)
								imgFile = new  File(path);
							
							if(path!=null && imgFile.exists() && imgFile.length()>0){
							
								isSingleImage = true;
								currentImageAttrIDComponentSelected = imageCompMap.get(imgid);
								currentImageIDComponentSelected = imgid;
								showDeleteImageAlert(imgid);
							}
							
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return true;
				}
			});	
		}else{
			snap_img_button.setImageResource(snap_img_button_disabled_id);
			del_img_button.setImageResource(del_img_button_disabled_id);
		}
		
		
		mappaDeleteImgButton.put(SingleImageComponentID, del_img_button);
		del_img_button.setEnabled(false);
		del_img_button.setImageResource(del_img_button_disabled_id);
		
		if(objv!=null && objv.getValue()!=null && objv.getValue().length()>0){
			del_img_button.setEnabled(true);
			del_img_button.setImageResource(del_img_button_id);
			String imgFileName = imgDir+"/"+attr.getId() + "_" + surveyId + ".jpg";
			mappaPathImmagini.put(SingleImageComponentID, imgFileName);
			
			// check if it is in mandatory map and set its value to true
        	if(mandatoryFieldMap.containsKey(attr.getId())){
        		mandatoryFieldMap.put(attr.getId(),true);
        	}
			
			SaveFileTask task = new SaveFileTask();
			task.execute(objv.getValue(), imgFileName, SingleImageComponentID+""+ "image");
		}
		
		lineraTitleTmp.addView(imageView);
		
		LinearLayout linearButtons = new LinearLayout(mainContext);
		LinearLayout.LayoutParams linearButtonsParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		linearButtons.setLayoutParams(linearButtonsParam);
		linearButtons.setOrientation(LinearLayout.HORIZONTAL);
		linearButtons.setPadding(20, 0, 20, 0);
	//	linearButtons.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

		linearButtons.addView(snap_img_button);
		linearButtons.addView(del_img_button);
		lineraTitleTmp.addView(linearButtons);
		
		lineraOuterTmp.addView(lineraTitleTmp);
		main.addView(lineraOuterTmp);
		
	//	main.addView(lineraTitleTmp);
		SingleImageComponentID++;
	}
	
	private boolean isSingleImage;
	
	
	
	
	
	
	public void closeKeyboard(){
		try{
			InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
	
		    View v=this.getCurrentFocus();
		    if(v==null)
		        return;
	
		    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}catch(Exception e){
			 e.printStackTrace();
		}
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
	
	
	private void addAudio(LinearLayout main, AttributeType attr){
		// if it is mandatory insert it in the map
		if(describeFeatureMap.containsKey(attr.getId())){
			if(!describeFeatureMap.get(attr.getId()).isNullable()){
				mandatoryFieldMap.put(attr.getId(), false);
			}
		}
		
		LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		LinearLayout lineraOuterTmp = new LinearLayout(mainContext);
		lineraOuterTmp.setLayoutParams(paramss);
		lineraOuterTmp.setOrientation(LinearLayout.VERTICAL);
		lineraOuterTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraOuterTmp.setId(ImageComponentID);
		lineraOuterTmp.setPadding(5, 25, 5, 25);
		
//		LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		lineraOuterTmp.addView(titleVieww);
		
		LinearLayout lineraTitleTmp = new LinearLayout(mainContext);
		lineraTitleTmp.setLayoutParams(paramss);
		lineraTitleTmp.setOrientation(LinearLayout.HORIZONTAL);
		lineraTitleTmp.setGravity(Gravity.CENTER_HORIZONTAL);
		lineraTitleTmp.setId(AudioComponentID);
		
		audioCompMap.put(AudioComponentID,attr.getId());
		lineraTitleTmp.setPadding(0, 5, 0, 0);
		
		
		// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BUT_DIM = 160;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
        
	//	LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(160,160);
		ImageView audioImageView = new ImageView(mainContext);
		audioImageView.setLayoutParams(imageViewParams);
		int audioimg_id = resource.getIdentifier("audio_ico", "drawable","it.zerofill.soundmapp");
		audioImageView.setImageResource(audioimg_id);
		audioImageView.setId(AudioComponentID);
		mappaAudioImgButton.put(AudioComponentID, audioImageView);
		
		String audioFileName = soundDir+"/"+attr.getId() + "_" + surveyId + ".mp4";
		mappaPathAudio.put(AudioComponentID,audioFileName);
		mappaAudioAttribute.put(AudioComponentID, attr);
		HashMap<String, String> map = getMapAudioAssociatedComponent(attr);
		boolean hasData = false;
		if(recordsMap!=null && map.size()>0){
			// check if config file has key for AUDIO_FILE, check if the column exist and has value
			if(map.containsKey(AUDIO_COMPONENT_FILE) && recordsMap.containsKey(map.get(AUDIO_COMPONENT_FILE)) && recordsMap.get(map.get(AUDIO_COMPONENT_FILE)).getValue().length()>0){
				
				int audioimg_full_id = resource.getIdentifier("audio_full_ico", "drawable","it.zerofill.soundmapp");
				audioImageView.setImageResource(audioimg_full_id);
				hasData = true;
				
				// check if it is in mandatory map and set its value to true
	        	if(mandatoryFieldMap.containsKey(attr.getId())){
	        		mandatoryFieldMap.put(attr.getId(),true);
	        	}
				
				SaveFileTask task = new SaveFileTask();
				// it needs only first 2 parameters to save the file, the other 2 are used to create thumbnail for image
				task.execute(recordsMap.get(map.get(AUDIO_COMPONENT_FILE)).getValue(), audioFileName, "", "audio");
			}
			
			if(map.containsKey(AUDIO_COMPONENT_DECIBEL) && recordsMap.containsKey(map.get(AUDIO_COMPONENT_DECIBEL))  && recordsMap.get(map.get(AUDIO_COMPONENT_DECIBEL)).getValue().length()>0){
				String decibel = recordsMap.get(map.get(AUDIO_COMPONENT_DECIBEL)).getValue();
				mappaPathAudioDecibel.put(AudioComponentID, decibel);
			}
			
			if(map.containsKey(AUDIO_COMPONENT_LOCATION) && recordsMap.containsKey(map.get(AUDIO_COMPONENT_LOCATION))  && recordsMap.get(map.get(AUDIO_COMPONENT_LOCATION)).getValue().length()>0){
				String location = recordsMap.get(map.get(AUDIO_COMPONENT_LOCATION)).getValue();
				mappaPathAudioLocation.put(AudioComponentID, location);
			}
			
			
		}
		
		audioImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==0){ //==0 means ACTION_DOWN
					
				}else if(event.getAction()==1){ //==1 means ACTION_UP
					String audioFileName = mappaPathAudio.get(v.getId());
					if(audioFileName!=null){
						File fileAudio = new File(audioFileName);
						if(fileAudio.exists()){
							if(!isAudioPlaying){
								startPlaying(audioFileName);
							}else{
								stopPlaying();
							}
							//isAudioPlaying = !isAudioPlaying;
						}
					}
				}
				return true;
			}
		});
		
				
		ImageView wrench_img_button = new ImageView(mainContext);
		wrench_img_button.setLayoutParams(layout_button_params);
		wrench_img_button.setImageResource(wrench_img_button_id);
		wrench_img_button.setId(AudioComponentID);
		wrench_img_button.setPadding(10, 0, 10, 0);
		
		ImageView del_audio_button = new ImageView(mainContext);
		del_audio_button.setLayoutParams(layout_button_params);
		del_audio_button.setImageResource(del_img_button_id);
		del_audio_button.setId(AudioComponentID);
		del_audio_button.setPadding(10, 0, 10, 0);
		
		if(isLocal || isNewSurvey){
			wrench_img_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(wrench_img_button_pressed_id);
						try{
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						isRecordedAudio = true;
						selectedAudioCompId = v.getId();
						
						((ImageView)v).setImageResource(wrench_img_button_id);
						
						int audioId =v.getId();
						
						// TO VERIFY non devo passare il path ma il nome del file o idsegnalazione. Vedere come e' fatto per le immagini
						// nome file audio tipo audio_survey.. guarda le immagini
						String audioFileName = mappaPathAudio.get(audioId);
						String decibel = "";
						if(mappaPathAudioDecibel.containsKey(audioId))
							decibel = mappaPathAudioDecibel.get(audioId);
						String location = "";
						if(mappaPathAudioLocation.containsKey(audioId))
							location = mappaPathAudioLocation.get(audioId);
	//						if(isNewSurvey)
	//							audioFileName = soundDir+"/"+attr.getId() + "_" + surveyId + ".mp4";
						
						Intent i = new Intent(mainContext, RecordActivity.class);
						i.putExtra("audioFileName", audioFileName);
						i.putExtra("decibel", decibel);
						i.putExtra("location", location);
						i.putExtra("surveyId", surveyId);
						i.putExtra("isNewSurvey",isNewSurvey);
						i.putExtra("isLocal",isLocal);
						
						i.putExtra("layerName",layerName);
						String filePath = localDir + "/" + currentFileName + ".xml";
						i.putExtra("xmlFilePath",filePath);
						i.putExtra("audioAttribute",mappaAudioAttribute.get(audioId));
						stopPlaying();
						startActivity(i);
					}
					return true;
				}
			});
		}else{
			wrench_img_button.setImageResource(wrench_img_button_disabled_id);
		}
		
		if(isLocal || isNewSurvey){
			del_audio_button.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==0){ //==0 means ACTION_DOWN
						((ImageView)v).setImageResource(del_img_button_pressed_id);
					}else if(event.getAction()==1){ //==1 means ACTION_UP
						((ImageView)v).setImageResource(del_img_button_id);
						try{

							selectedAudioCompId = v.getId();
							showDeleteAudioAlert(selectedAudioCompId);
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return true;
				}
			});	
		}else{
			del_audio_button.setImageResource(del_img_button_disabled_id);
		}
		
		del_audio_button.setEnabled(hasData);
		if(hasData)
			del_audio_button.setImageResource(del_img_button_id);
		else
			del_audio_button.setImageResource(del_img_button_disabled_id);
			
		mappaDeleteAudioButton.put(AudioComponentID,del_audio_button);
			
		
		
		lineraTitleTmp.addView(audioImageView);
		
		LinearLayout linearButtons = new LinearLayout(mainContext);
		LinearLayout.LayoutParams linearButtonsParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		linearButtons.setLayoutParams(linearButtonsParam);
		linearButtons.setOrientation(LinearLayout.HORIZONTAL);
		linearButtons.setPadding(20, 0, 20, 0);
		
		linearButtons.addView(wrench_img_button);
		linearButtons.addView(del_audio_button);
		
		lineraTitleTmp.addView(linearButtons);
		
		lineraOuterTmp.addView(lineraTitleTmp);
		main.addView(lineraOuterTmp);
		//main.addView(lineraTitleTmp);
		AudioComponentID++;
	}
    
	
	
	private boolean isAudioPlaying;
	private void startPlaying(String audioFileName){
		mPlayer = new MediaPlayer();
		try{
			mPlayer.setDataSource(audioFileName);
			mPlayer.prepare();
			mPlayer.start();
			isAudioPlaying = true;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void stopPlaying(){
		try{
			if(isAudioPlaying)
				isAudioPlaying = false;
			mPlayer.release();
			mPlayer = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
   private void extinOnCancel(){
	   finish();
   }
   
   private void toBeClosed(){
		if(finish){
			stopPlaying();
			deleteSurvey();
			finish();
		}
	}
   
   @Override
   public void onBackPressed(){
	   try{
		   stopPlaying();
		   closeKeyboard();
		}catch(Exception e){
			e.printStackTrace();
		}
	   finish();
   }
   
   private boolean finish;
	public void showGenericAlert(String title, String message, boolean flag){
       AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
       alertDialog.setTitle(title);
       alertDialog.setMessage(message);
       finish = flag;
       alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog,int which) {
           	dialog.cancel();
           	toBeClosed();
           }
       });
       
       alertDialog.show();
   }
	
	public void showDataDialog(String message){
	   AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
	   alertDialog.setTitle(getString(R.string.locationLabel));
	   alertDialog.setMessage(message);
	   alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,int which) {
	       	dialog.cancel();
	       }
	   });
	   alertDialog.show();
	}
    
    // Utility methods
    private int tempImgToDeleteParam;
    public void showDeleteImageAlert(int imgid){
    	tempImgToDeleteParam = imgid;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.warningLabel));
        alertDialog.setMessage(getString(R.string.deleteImageWarning));
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	deleteImage(tempImgToDeleteParam);
            	if(!isSingleImage)
            		hideNextImgView(tempImgToDeleteParam);
            	
            	isSingleImage = false;
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }
    
    private int tempAudioToDeleteParam;
    public void showDeleteAudioAlert(int imgid){
    	tempAudioToDeleteParam = imgid;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.warningLabel));
        alertDialog.setMessage(getString(R.string.deleteAudioWarning));
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	deleteAudio(tempAudioToDeleteParam);
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }
    
    private HashMap<String, ObjValue> getMapValueByColumnId(){
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
    
    
    /* 
     * DO NOT DELETE, for future review
     * 
    private List<ObjValue> orderRecords(int index){
    	List<AttributeType> attr = layer.getAttributes();
    	
    	List<ObjValue> res = new ArrayList<ObjValue>();
    	for(ObjFeature objf : records){
			for(ObjRecord objr : objf.getRecords()){
				for(ObjValue objv : objr.getValues()){
					String col = objv.getColumn_name();
					for(AttributeType a : attr){
						if(a.getId().equals(col) && a.getPage()==index){
							objv.setOrdinal(a.getOrdinal());
							res.add(objv);
						}
					}
				}
			}
    	}
    	
    	Collections.sort(res, new Comparator<ObjValue>() {
		    public int compare(ObjValue object1, ObjValue object2) {
		    	if(object1.getOrdinal() > object2.getOrdinal())
		    		return 1;
		    	else return -1;
		    }
		});
    	return res;
    }
    */
    
    //decodes image and scales it to reduce memory consumption
  	private Bitmap decodeFile(File f, boolean rotate, boolean smalSize){
  	    try {
  	        //Decode image size
  	        BitmapFactory.Options o = new BitmapFactory.Options();
  	        o.inJustDecodeBounds = true;
  	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

  	        //The new size we want to scale to
  	        //final int REQUIRED_SIZE=80;
  	        int REQUIRED_SIZE=80;
  	        if(smalSize)
  	        	REQUIRED_SIZE=80;
  	        else
  	        	REQUIRED_SIZE=550;

  	        //Find the correct scale value. It should be the power of 2.
  	        int scale=1;
  	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
  	            scale*=2;

  	        //Decode with inSampleSize
  	        BitmapFactory.Options o2 = new BitmapFactory.Options();
  	        o2.inSampleSize=scale;
	  	      
  	        // image to show
  	        Bitmap temp = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
  	        // Rotate image
  	        
  	        Matrix mtx = new Matrix();
	  	    if(rotate)
	  	    	mtx.postRotate(90);
	  	    else
	  	    	mtx.postRotate(0);
	  	    Bitmap rotatedBitmap = Bitmap.createBitmap(temp, 0,0,temp.getWidth(), temp.getHeight(), mtx, true);
  	        
  	        return rotatedBitmap;
  	    } catch (Exception e) {}
  	    return null;
  	}
  	
  	
  	private boolean haveNetworkConnection() {
    	try{
	    	ConnectivityManager cm = (ConnectivityManager)mainContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    	boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	    	return isConnected;
    	}catch(Exception e){
    		return false;
    	}
    }
  	
  	DecimalFormat df = new DecimalFormat("00");
  	private void updateDateDisplay() {
        if(mappaDateComponent.containsKey(selectedDateComponentId)){
        	updateDateView(mappaDateComponent.get(selectedDateComponentId),year,month,day);
        	if(isDateChanged){
        		String attrId = mappaDateComponentId.get(selectedDateComponentId);
        		isDateChanged = false;
        		saveFeatureTask = new SaveFeatureTask();
        		String dateTextToSend = year+"-"+df.format(month)+"-"+df.format(day)+"T00:00:00.000Z";
        		saveFeatureTask.execute(currentFileName,attrId,dateTextToSend);
        	}
        }
    }
  	
  	private void updateDateView(EditText et, int y, int m, int d){
  		et.setText(new StringBuilder()
        .append(df.format(d)).append("-").append(df.format(m+1)).append("-")
        .append(y).append(""));
  	}
  	
  	
 	/**
	 * Shows the progress UI and hides the main scroll view.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			getDescribeFeatureStatusBar.setVisibility(View.VISIBLE);
			getDescribeFeatureStatusBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							getDescribeFeatureStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mainLayoutTab.setVisibility(View.VISIBLE);
			mainLayoutTab.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayoutTab.setVisibility(
									show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			getDescribeFeatureStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mainLayoutTab.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
	/**
	 * Shows the progress UI and hides the main scroll view.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showSendingProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			sendSurveyStatusLayoutBar.setVisibility(View.VISIBLE);
			sendSurveyStatusLayoutBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							sendSurveyStatusLayoutBar.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mainLayoutTab.setVisibility(View.VISIBLE);
			mainLayoutTab.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayoutTab.setVisibility(
									show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			sendSurveyStatusLayoutBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mainLayoutTab.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
  	
  	
  	
  	
  	public class GetDescribeFeatureTask extends AsyncTask<Void, Void, Boolean> {
    	private String message = "";
    	
    	@Override
		protected Boolean doInBackground(Void... arg0) {
    		
    		// aggiunto la gestione di un file di controllo all'interno del quale viene salvato
    		// l'id della segnalazione
    		// se Android richiama la creazione della view a seguito del ritorno dalla fotocamera
    		// utilizzo questo file di controllo per leggere tutti i valori della segnalazioni precedentemente creata
    		String homepath = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
    		File checksumFile = new File(homepath,"checksum");
    		try{
    			if(!checksumFile.exists()){
        			checksumFile.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(checksumFile));
                    writer.write(surveyId);
                    writer.close();
        		}else{
        			BufferedReader reader = new BufferedReader(new FileReader(checksumFile));
        			surveyId = reader.readLine();
        			/*
        			String line;
        			while((line = reader.readLine()) != null) {
        				surveyId = line;
        			}
        			*/
        			reader.close();
        		}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		

    		String geoserverUrl = "";
			if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
				geoserverUrl = config.getUrls().get("wfs");
			}else{
				message = getString(R.string.noWfsFoundMessage);
				return false;
			}
			
			boolean haveConnection = haveNetworkConnection();
			
			// layerName must be namespace:layername
			if(haveConnection){
				try{
					GetXmlFromGeoserver.getDescribeFeatureType(geoserverUrl, layerName, loggedUser, pass, sessionId, mainContext); //fileEncr.getPass(userDataFilePath, loggedUser)
				}catch(AuthenticationException e){
					int errorCode = e.getErrorCode();
					if(errorCode==401)
						message = getString(R.string.unauthorizedUser);
	    			else
	    				message = getString(R.string.genericAuthenticationError);
	    			return false;
				}
			}
			describeFeatureMap = featureParser.parseDescribeFeature(layerName);
			if(describeFeatureMap==null || describeFeatureMap.size()==0){
				message = getString(R.string.noConnOrGeoserverFileMessage);
				return false;
			}
			
			
			
			// override is nullable from geoserver with value in config file
			for(String key : describeFeatureMap.keySet()){
				if(attributesMap.containsKey(key)){
					describeFeatureMap.get(key).setNullable(attributesMap.get(key).isNullable());
				}
			}
			
			
			
			List<String> layerList = new ArrayList<String>();
			layerList.add(layerName);
			if(!isNewSurvey && !isLocal){
				//getFeature from geooserver
				// featureId and cql_filter both specified but are mutually exclusive
				// so here it doesn't need to use cql_filter cos it is already filtered in the list view. -> USER = NULL
				if(haveConnection){
					try{
						GetXmlFromGeoserver.getFeature(geoserverUrl, layerList, surveyId, true, creatorField, null, loggedUser, pass, sessionId, mainContext); //fileEncr.getPass(userDataFilePath, loggedUser)
					}catch(AuthenticationException e){
						int errorCode = e.getErrorCode();
						if(errorCode==401)
							message = getString(R.string.unauthorizedUser);
		    			else
		    				message = getString(R.string.genericAuthenticationError);
		    			return false;
					}
				}
				records = featureParser.parseGetFeatureFile(layerList,false,null);
				if(records.size()==0){
					message = getString(R.string.noConnOrPropFileMessage); 
					return false;
				}
			}
			else if(!isNewSurvey && isLocal){
				//getFeature from local
				// find the path of file and set this too currentFileName = surveyName;
				// then READ the file!
				records = featureParser.parseGetFeatureFile(layerList,true,surveyId);
				if(records.size()==0){
					message = getString(R.string.noConnOrPropFileMessage); 
					return false;
				}
			}else if(isNewSurvey){
				//currentFileName = surveyName;
				//donothing, no record to show
			}
			
			recordsMap = getMapValueByColumnId();
			if(!isNewSurvey && isLocal){
				String surveyNameAttrId = config.getLayerByName(layerName).getNameColumn();
				surveyName = recordsMap.get(surveyNameAttrId).getValue();
			}
			currentFileName = surveyId;
			return true;
		}
		
		@Override
		protected void onCancelled() {
			getDescribeTask = null;
			showProgress(false);
			extinOnCancel();
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			getDescribeTask = null;
			showProgress(false);

			if (success) {
				createTabs();
			} else {
				showGenericAlert(getString(R.string.warningLabel),message,true);
			}
		}
    }
  	
  	private void deleteFiles(File file){
    //	try{
    		if(file.isDirectory()){
        		//directory is empty, then delete it
        		if(file.list().length==0){
        		   file.delete();
        		}else{
        		   //list all the directory contents
            	   String files[] = file.list();
            	   for (String temp : files) {
            	      //construct the file structure
            	      File fileDelete = new File(file, temp);
            	      //recursive delete
            	      deleteFiles(fileDelete);
            	   }
            	   //check the directory again, if empty then delete it
            	   if(file.list().length==0){
               	     file.delete();
            	   }
        		}
        	}else{
        		//if file, then delete it
        		file.delete();
        	}
   // 	}catch(Exception e){
   // 		e.printStackTrace();
    //	}
    }
  	
  	
  	public class SaveFileTask extends AsyncTask<String, Void, Boolean> {

  		private int id;
  		private boolean isImage;
  		
		@Override
		protected Boolean doInBackground(String... args) {
			
			EncodeSaveFile.save(args[0], args[1]);
			if(args.length>3 && args[3].equals("image")){
				isImage = true;
				id = Integer.parseInt(args[2]);
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			//saveFileTask = null;
			
			if (success) {
				if(isImage){
					String path = mappaPathImmagini.get(id);
					ImageView item = mappaImmagini.get(id);
					File imgFile = new File(path);
					Bitmap myBitmap = decodeFile(imgFile,false,true);
					item.setImageBitmap(myBitmap);	
				}
				//setImage();
				
			} else {
				
			}
		}
  		
  	}
  	
  	
  	
  	
  	public class SaveFeatureTask extends AsyncTask<String, Void, Boolean> {
  		
  		@Override
		protected Boolean doInBackground(String... args) {
			String fileName = args[0];
			String columnName = args[1];
			
			String filePath = localDir + "/" + fileName + ".xml";
			String namespace = layerName.substring(0, layerName.indexOf(":"));
			String layer = layerName.substring(layerName.indexOf(":")+1,layerName.length());
			String geoserverAddress = config.getUrls().get("wfs");
  			
  			if(args.length>2){
  				String value = args[2];
  				if(value!=null)
  					value = value.trim();
  				
  				if(args.length==4){
  					if("img".equals(args[2]))
  						SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, true, false,"", args[3]);
  					else if(TYPE_POINT.equals(args[2]))
  						SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, true,TYPE_POINT, args[3]);
  					else if(TYPE_LINE.equals(args[2]))
  						SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, true,TYPE_LINE, args[3]);
  					else if("audioDelete".equals(args[2])){
  						
  						AttributeType attr = mappaAudioAttribute.get(selectedAudioCompId);
  						String attrId = audioCompMap.get(selectedAudioCompId);
  						HashMap<String, String> map = getMapAudioAssociatedComponent(attr);
  						// delete audio component
  						columnName = attrId;
						SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, false,"", null);
  						if(map.containsKey(AUDIO_COMPONENT_LOCATION)){
  							String locationId = map.get(AUDIO_COMPONENT_LOCATION);
  							columnName = locationId;
  							// delete audio location component
  							SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, false,"", null);
  						}
  						if(map.containsKey(AUDIO_COMPONENT_DECIBEL)){
  							String decibelId = map.get(AUDIO_COMPONENT_DECIBEL);
  							columnName = decibelId;
  							// delete audio decibel component
  							SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, false,"", null);
  						}
  					}
  						
  				}else
  					SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, false,"", value);
  			}else{
  				//String value = loggedUser + "." + surveyId;
  				String value = defaultSurveyName;
  				String columnForNameAttr = config.getLayerByName(layerName).getNameColumn();
  				SurveyXmlBuilder.createFile(filePath, namespace, layer, geoserverAddress, columnName, columnForNameAttr, value, creatorField, loggedUserFilter);
  				//LocalFileHandler.addSurvey(localFileHandler, surveyId, loggedUser);
  				//LocalFileHandler.addSurvey(localFileHandler, surveyName, loggedUser);
  				//LocalFileHandler.addSurvey(localFileHandler, defaultSurveyName, loggedUser);
  				LocalFileHandler.addSurvey(localFileHandler, surveyId, defaultSurveyName, loggedUser, layerName);
  			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			saveFeatureTask = null;
			
			if (success) {
				
			} else {
				
			}
		}
  	}
  	
  	
  	
  	private void deleteSurvey(){
  		try{
			// delete file from map
			LocalFileHandler.removeSurvey(localFileHandler, surveyId,currentSurveyName, loggedUser, layerName);
			
			// delete directories
			
//			String filePath = localDir + "/" + currentFileName + ".xml";
//			File file = new File(filePath);
//			file.delete();
			String pathLocal = localDir; // + "/" +currentFileName;
        	File localD = new File(pathLocal);
        	if(localD.exists())
        		deleteFiles(localD);
        	// delete pictures
        	String pathImg = imgDir; // + "/" +currentFileName;
        	File imgD = new File(pathImg);
        	if(imgD.exists())
        		deleteFiles(imgD);
        	// delete sounds
        	String pathSound = soundDir; // + "/" +currentFileName;
        	File soundD = new File(pathSound);
        	if(soundD.exists())
        		deleteFiles(soundD);
			
		}catch(Exception e){
			showGenericAlert("alert","can't delete local files\n"+e.getMessage(),false);
			e.printStackTrace();
		}
  	}
  	
  	
  	
  	public class WaitTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {

			try{
				Thread.sleep(100);
			}catch(Exception e){}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			showProgress(false);
			waitTask = null;
			onResumeProcess();
		}
  		
  	}
  	
  	
  	
  	ProgressDialog dialog;
  	public class SendDataTask extends AsyncTask<Void, Integer, Boolean> {
  		
  		
  		@Override
  	    protected void onPreExecute() {
  	        dialog = new ProgressDialog(mainContext);
  	        dialog.setMessage(getString(R.string.sendSurveyProgressText));
  	        dialog.setIndeterminate(false);
  	        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
  	        dialog.setProgress(0);
  	        dialog.show();
  	    }
  		
  		@Override
		protected Boolean doInBackground(Void... args) {
  			
  			try{
  				String geoserverUrl = "";
  				if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
  					geoserverUrl = config.getUrls().get("wfs")+"/wfs?service=wfs";
  				}else{
  					//message = getString(R.string.noWfsFoundMessage);
  					return false;
  				}
  				
  				geoserverUrl += "&configuration=svrtoc";
  				if(sessionId!=null && sessionId.length()>0)
  					geoserverUrl += "&session="+sessionId;
  				
	  			//String BASE_URL = "http://kronosboy.no-ip.org:8080/geoserver/wfs";
	  			HttpURLConnection httpCon = null;
				URL url = new URL(geoserverUrl);
				httpCon = (HttpURLConnection) url.openConnection();
	//			StringBuffer myWFSRequest = new StringBuffer();
				
				String filePath = localDir + "/" + currentFileName + ".xml";
				/*
				FileInputStream f = new FileInputStream(filePath);
				DataInputStream i = new DataInputStream(f);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(i));
				String line = "";
				String fileString = "";
				while ((line = buffer.readLine()) != null){
					fileString += line + "\n";
				}
				buffer.close();
				*/
				
	//			myWFSRequest.append(fileString);
				httpCon.setDoOutput(true);
				httpCon.setDoInput(true);
				httpCon.setRequestMethod("POST");
				
				String authString = loggedUser + ":" + pass; //fileEncr.getPass(userDataFilePath, loggedUser);
    			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
    			String authStringEnc = new String(authEncBytes);
    			httpCon.setRequestProperty("Authorization", "Basic " + authStringEnc);
				
				httpCon.setRequestProperty("Content-type", "application/xml");
	//			OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
	//			out.write(myWFSRequest.toString());
	//			out.close();
				
				// NEW 
				File file = new File(filePath);
	            FileInputStream fileInputStream = new FileInputStream(file);
	            byte[] bytes = new byte[(int) file.length()];
	            fileInputStream.read(bytes);
	            fileInputStream.close();
				OutputStream outputStream = httpCon.getOutputStream();
				int bufferLength = 1024;
	            for (int j = 0; j < bytes.length; j += bufferLength) {
	                int progress = (int)((j / (float) bytes.length) * 100);
	                publishProgress(progress);
	                if (bytes.length - j >= bufferLength) {
	                    outputStream.write(bytes, j, bufferLength);
	                } else {
	                    outputStream.write(bytes, j, bytes.length - j);
	                }
	            }
	            outputStream.close();
				
	            publishProgress(99);
	            
				int responseCode = httpCon.getResponseCode(); // codice di risposta http
				// more strict controls, show reply to user
				if(responseCode!=200)
					return false;

				InputStream in = httpCon.getInputStream();
			    StringBuffer sb = new StringBuffer();
			    try {
			        int chr;
			        while ((chr = in.read()) != -1) {
			            sb.append((char) chr);
			        }
			    } finally {
			        in.close();
			    }
			    // process xml file for error
			    // sb.toString(); // XML aswer from geoserver
			    File fileTemp = new File(sentReplyDir + "/" + currentFileName + "_REPLY.xml");
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemp));
				writer.write(sb.toString());
				writer.close();
			
				publishProgress(100);
				
				if(sb.toString().contains("ServiceException") || sb.toString().contains("wfs:FAILED") ){
			    	return false;
			    }
				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
  		
  		@Override
  	    protected void onProgressUpdate(Integer... progress) {
  	        dialog.setProgress(progress[0]);
  	    }
		
		@Override
		protected void onPostExecute(final Boolean success) {
			sendDataTask = null;
			showSendingProgress(false);
			dialog.dismiss();
			
			if (success) {
				showGenericAlert("",getString(R.string.fileSuccessfullySentMessage),true);
			} else {
				showGenericAlert(getString(R.string.warningLabel),getString(R.string.fileErrorSentMessage),false);				
			}
		}
		
		@Override
		protected void onCancelled() {
			sendDataTask = null;
			dialog.dismiss();
			showSendingProgress(false);
		}
  	}
  	
  	
    

}
