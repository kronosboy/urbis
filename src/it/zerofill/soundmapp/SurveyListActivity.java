package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AuthenticationException;
import it.zerofill.soundmapp.controllers.FeatureParser;
import it.zerofill.soundmapp.controllers.GetXmlFromGeoserver;
import it.zerofill.soundmapp.controllers.LocalFileHandler;
import it.zerofill.soundmapp.models.Configuration;
import it.zerofill.soundmapp.models.FeatureElementAttribute;
import it.zerofill.soundmapp.models.ObjFeature;
import it.zerofill.soundmapp.models.ObjRecord;
import it.zerofill.soundmapp.models.ObjValue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class SurveyListActivity extends Activity{
	
	
	private LinearLayout formVariableFieldsLayout;
//	private Button backButton;
	private View loginStatusBar;
	private View mainScrollView;
	
	private Context mainContext;
	public String sessionId;
//	private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private FeatureParser featureParser;
//	private HashMap<String, List<ObjFeature>> recordsFromGeoserverMap;
	private List<ObjFeature> listaValori;
	private HashMap<String, String> recordsMapFromLocal;
	
	private Configuration config;
	private String layerName;
	
	private final int WIDTH = 720;
	private final int HEIGHT = 1280;
	private int deviceWidth;
	private int deviceHeight;
	
	private int goImgButtonId_pressed;
	private int goImgButtonId;
	private int delImgButtonId_pressed;
	private int delImgButtonId;
	private ImageView logoBannerImg;
	
	private String pass;
	
	private String firstAttributeForQuery;
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	private String localFilePath;
	private String loggedUser;
	private String loggedUserFilter;
	private String creatorField = "creator";
	private String selectedSurveyId;
	private String selectedSurveyName;
	
	private String localDir;
	private String imgDir;
	private String soundDir;
	private String localFileHandler;
	
//	private String userDataFilePath;
//	private FileEncryptor fileEncr;
	
	private GetSurveyNameTask getSurveyNameTask;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);
        
        init();
        
        // Moved to onResume so it can be called when back from SurveyDetail (once it has been sent this view must be refreshed)
        
//        showProgress(true);
//        getSurveyNameTask = new GetSurveyNameTask();
//        getSurveyNameTask.execute((Void) null);
        
        buttonHandler();
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
    
    private void reloadView(){
    	listaValori = new ArrayList<ObjFeature>();
    	showProgress(true);
        getSurveyNameTask = new GetSurveyNameTask();
        getSurveyNameTask.execute((Void) null);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	try{
   			String homepath = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
   			File checksumFile = new File(homepath,"checksum");
   			if(checksumFile.exists())
 			   checksumFile.delete();
   		}catch(Exception e){
   			e.printStackTrace();
   		}
    	
    	if(!hasError){
    		reloadView();
    	}
    }
    
    @Override
    public void onBackPressed(){
    	finish();
    }
    
    private boolean hasError;
    private void init(){
    	mainContext = this;
    	formVariableFieldsLayout = (LinearLayout)findViewById(R.id.formVariableFieldsLayout);
    	loginStatusBar = findViewById(R.id.loginStatusLayout);
    	mainScrollView = findViewById(R.id.mainScroll);
    	
    	assetsPropertyReader = new AssetsPropertyReader(mainContext);
        properties = assetsPropertyReader.getProperties("settings.properties");
        
    	featureParser = new FeatureParser(mainContext);
    	
    	Bundle extras = getIntent().getExtras();
    	if (extras != null){
    		config = (Configuration)extras.getSerializable("config");
    		layerName = extras.getString("layerName");
    		loggedUser = extras.getString("loggedUser");
    		pass = extras.getString("pass");
    		sessionId = extras.getString("sessionId");
    		loggedUserFilter = extras.getString("loggedUserFilter");
    	}
    	
    	
    	// Find for this layer creator field. Must BE!
    	if(config.getLayerByName(layerName)!=null){
    		creatorField = config.getLayerByName(layerName).getCreatorolumn();
//        	if("".equals(creatorField))
//        		creatorField = "creator";
    	}else{
    		hasError = true;
    		showGenericAlert(getString(R.string.warningLabel), getString(R.string.noConnOrPropFileMessage), true);
    		return;
    	}
    	
    	// get column with NAME
    	//firstAttributeForQuery = attributeExstractor.getFirstAttributeForQueryMap().get(layerName);
    	firstAttributeForQuery = config.getLayerByName(layerName).getNameColumn();
    	
    	localFilePath = getExternalFilesDir(null).getAbsolutePath().toString() + "/"+ properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
    	localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/"+ properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory");
    	imgDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("picturesDirectory");
		soundDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("soundsDirectory");
		localFileHandler = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
		
//		localFilePath = Environment.getExternalStorageDirectory().toString() + "/"+ properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
//		localDir = Environment.getExternalStorageDirectory().toString() + "/"+ properties.getProperty("homeDirectory") + "/"+ properties.getProperty("localDirectory");
//		imgDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("picturesDirectory");
//		soundDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("soundsDirectory");
//		localFileHandler = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory") + "/"+  properties.getProperty("localFileHandler");
		
//		fileEncr = new FileEncryptor();
//		userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");

    	Resources resource;
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	deviceWidth = metrics.widthPixels;
		deviceHeight = metrics.heightPixels;
    	resource = new Resources(getAssets(), metrics, null);
    	
    	goImgButtonId_pressed = resource.getIdentifier("play_pressed", "drawable","it.zerofill.soundmapp");
    	goImgButtonId = resource.getIdentifier("play", "drawable","it.zerofill.soundmapp");
    	delImgButtonId_pressed = resource.getIdentifier("delete_pressed", "drawable","it.zerofill.soundmapp");
    	delImgButtonId = resource.getIdentifier("delete", "drawable","it.zerofill.soundmapp");
    	
    	final int FIXED_TITLE_DIM = 350;
		float FIXED_TITLE_WIDTH_PERC = ((float) FIXED_TITLE_DIM /WIDTH) * 100;
		int FIXED_TITLE_WIDTH_VALUE = (int)((FIXED_TITLE_WIDTH_PERC * deviceWidth) / 100);
		titleViewParams = new LinearLayout.LayoutParams(FIXED_TITLE_WIDTH_VALUE ,LayoutParams.WRAP_CONTENT);
		
		logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
    }
    private LinearLayout.LayoutParams titleViewParams;
    
    
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
	private void toBeClosed(){
		if(finish)
			finish();
	}
    
    private void buttonHandler(){
//    	backButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				finish();
//			}
//    	});
    }
    
    
  
    private HashMap<Integer, String> mappaId;
    private HashMap<Integer, String> mappaSurveyIDname;
    private HashMap<Integer, Boolean> mappaIslocal;
    private int viewId;
    @SuppressLint("UseSparseArrays")
	private void insertFieldIntoLayout(){
    	mappaId = new HashMap<Integer, String>();
    	mappaSurveyIDname = new HashMap<Integer, String>();
    	mappaIslocal = new HashMap<Integer, Boolean>();
    	viewId = 0;
    	
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_DIM = 100;
		float FIXED_WIDTH_PERC = ((float) FIXED_DIM /WIDTH) * 100;
		float FIXED_HEIGHT_PERC = ((float) FIXED_DIM /HEIGHT) * 100;
		int FIXED_WIDTH_VALUE = (int)((FIXED_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_HEIGHT_VALUE = (int)((FIXED_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams layout_button_params = new LinearLayout.LayoutParams(FIXED_WIDTH_VALUE,FIXED_HEIGHT_VALUE);
    	
        if(listaValori.size()>0 && listaValori.get(0).getRecords().size()>0){
        	createView(layout_button_params,listaValori.get(0).getRecords(),false);
        }else{
        	formVariableFieldsLayout.removeAllViews();
        }
    }
    
    
    private String getNomeSurvey(ObjRecord item, String colonnaName){
		//String nomeCol = d.getId(); //soundmapp.anto.survey_soundmapp_20140503_091030
		List<ObjValue> vals = item.getValues();
		if(vals.size()>0){
			for(ObjValue v:vals){
				String pp = v.getColumn_name();
				String ppp = v.getValue();
				if(colonnaName.equals(pp))
					return ppp;
			}
		}
    	return null;
    }
    
    
    
    public void createView(LinearLayout.LayoutParams layout_button_params, List<ObjRecord> records, boolean isLocal){
    	formVariableFieldsLayout.removeAllViews();
    	for(ObjRecord record : records){
    		if(!record.getId().equals("")){
    			
    			if(recordsMapFromLocal.containsKey(record.getId())){
    				isLocal = true;
    			}else
    				isLocal = false;
    			
    		//	LinearLayout.LayoutParams titleViewParams = new LinearLayout.LayoutParams(300 ,LayoutParams.WRAP_CONTENT);
        		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT ,LayoutParams.WRAP_CONTENT);
        		
        		LinearLayout llTemp = new LinearLayout(mainContext);
        		llTemp.setLayoutParams(llParams);
        		llTemp.setOrientation(LinearLayout.HORIZONTAL);
        		llTemp.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        		llTemp.setPadding(0, 10, 0, 10);
        		
        		TextView tvTemp = new TextView(mainContext);
        		tvTemp.setLayoutParams(titleViewParams);
        		
        		String nomeSegn = getNomeSurvey(record, config.getLayerByName(layerName).getNameColumn());
        		if(nomeSegn==null){
	        		if(record.getId().indexOf(".")>0)
	        			nomeSegn = record.getId().substring(record.getId().indexOf(".")+1, record.getId().length()); // first remove name of layer
	        		if(nomeSegn.indexOf(".")>0)
	        			nomeSegn = nomeSegn.substring(nomeSegn.indexOf(".")+1, nomeSegn.length()); // then remove user name
        		}
        		
        		mappaSurveyIDname.put(viewId, nomeSegn);
        		
        		tvTemp.setText(nomeSegn);
        		tvTemp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        		tvTemp.setGravity(Gravity.CENTER_HORIZONTAL);
        		
        		ImageView goImgTemp = new ImageView(mainContext);
        		goImgTemp.setImageResource(goImgButtonId);
        		goImgTemp.setLayoutParams(layout_button_params);
        		goImgTemp.setId(viewId);
        		mappaId.put(viewId, record.getId());
        		mappaIslocal.put(viewId, isLocal);
        		goImgTemp.setOnTouchListener(new View.OnTouchListener() {
        			@Override
        			public boolean onTouch(View v, MotionEvent event) {
        				
        				ImageView imv = (ImageView)v;
        				if(event.getAction()==0){ //==0 means ACTION_DOWN
        					imv.setImageResource(goImgButtonId_pressed);
        				}else if(event.getAction()==1){ //==1 means ACTION_UP
        					imv.setImageResource(goImgButtonId);
        					
        					boolean isLocal = false;
        					String surveyId = "";
        					String surveyName = "";
        					if(mappaId.containsKey(imv.getId())){
        						surveyId = mappaId.get(imv.getId());
        						isLocal = mappaIslocal.get(imv.getId()); 
        						surveyName = mappaSurveyIDname.get(imv.getId());
        					}
        					
        					Intent i = new Intent(mainContext, SurveyDetailActivity.class);
        					
        					i.putExtra("config", config);
        					i.putExtra("isNewSurvey", false);
        					i.putExtra("isLocal", isLocal);
        					i.putExtra("layerName", layerName);
        					i.putExtra("surveyName", surveyName);
        					i.putExtra("surveyId", surveyId);
        					i.putExtra("pass", pass);
        					i.putExtra("loggedUser", loggedUser);
        					
        					startActivity(i);
        					
        				}
        				return true;
        			}
        		});
        		
        		ImageView delImgTemp = new ImageView(mainContext);
        		if(isLocal){
            		delImgTemp.setImageResource(delImgButtonId);
            		delImgTemp.setLayoutParams(layout_button_params);
            		delImgTemp.setId(viewId);
            		mappaId.put(viewId, record.getId());
            		delImgTemp.setOnTouchListener(new View.OnTouchListener() {
            			@Override
            			public boolean onTouch(View v, MotionEvent event) {
            				
            				ImageView imv = (ImageView)v;
            				if(event.getAction()==0){ //==0 means ACTION_DOWN
            					imv.setImageResource(delImgButtonId_pressed);
            				}else if(event.getAction()==1){ //==1 means ACTION_UP
            					imv.setImageResource(delImgButtonId);
            					if(mappaId.containsKey(imv.getId())){
            						selectedSurveyId = mappaId.get(imv.getId());
            						//selectedSurveyName = mappaSurveyIDname.get(selectedSurveyId);
            						selectedSurveyName = mappaSurveyIDname.get(imv.getId());
            						showAlert(mappaId.get(imv.getId()));
            					}
            				}
            				return true;
            			}
            		});
        		}
        		
        		// Fixed dimension for a button (on default screen it will be 150x150)
        		final int FIXED_SPACE_W_DIM = 50;
        		final int FIXED_SPACE_H_DIM = 10;
        		float FIXED_SPACE_WIDTH_PERC = ((float) FIXED_SPACE_W_DIM /WIDTH) * 100;
        		float FIXED_SPACE_HEIGHT_PERC = ((float) FIXED_SPACE_H_DIM /HEIGHT) * 100;
        		int FIXED_SPACE_WIDTH_VALUE = (int)((FIXED_SPACE_WIDTH_PERC * deviceWidth) / 100);
                int FIXED_SPACE_HEIGHT_VALUE = (int)((FIXED_SPACE_HEIGHT_PERC * deviceHeight) / 100);
                // This will resize button on screen resolutions
                LinearLayout.LayoutParams layout_space_params = new LinearLayout.LayoutParams(FIXED_SPACE_WIDTH_VALUE,FIXED_SPACE_HEIGHT_VALUE);
        		
        		llTemp.addView(tvTemp);
        		LinearLayout space = new LinearLayout(mainContext);
        		//space.setLayoutParams(new LinearLayout.LayoutParams(50 ,10));
        		space.setLayoutParams(layout_space_params);
        		llTemp.addView(space);
        		llTemp.addView(goImgTemp);
        		LinearLayout space2 = new LinearLayout(mainContext);
        		space2.setLayoutParams(layout_space_params);
        		//space2.setLayoutParams(new LinearLayout.LayoutParams(50 ,10));
        		
        		if(isLocal) llTemp.addView(space2);
        		if(isLocal) llTemp.addView(delImgTemp);
        		
        		viewId++;
        		formVariableFieldsLayout.addView(llTemp);
    		}
    	}
    }
    
    
    private void deleteSurvey(){
    	try{
    		// delete from file the entry for selected survey
        	//LocalFileHandler.removeSurvey(selectedSurveyId, selectedSurveyId, selectedSurveyName, loggedUser);
    		LocalFileHandler.removeSurvey(localFileHandler, selectedSurveyId, selectedSurveyName, loggedUser, layerName);
        	
        	// delete xml file
        	String pathLocal = localDir + "/" +selectedSurveyId;
        	File localD = new File(pathLocal);
        	if(localD.exists())
        		deleteFiles(localD);
        	// delete pictures
        	String pathImg = imgDir + "/" +selectedSurveyId;
        	File imgD = new File(pathImg);
        	if(imgD.exists())
        		deleteFiles(imgD);
        	// delete sounds
        	String pathSound = soundDir + "/" +selectedSurveyId;
        	File soundD = new File(pathSound);
        	if(soundD.exists())
        		deleteFiles(soundD);
        	
        	reloadView();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    private void deleteFiles(File file){
    	try{
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
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void showAlert(String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.warningLabel));
        alertDialog.setMessage(getString(R.string.deleteLabel)+" "+message+"?");
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	deleteSurvey();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        
        alertDialog.show();
    }
    
    public void showErrorAlert(String messageError){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.errorLabel));
        alertDialog.setMessage(messageError);
        // On pressing Settings button
        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
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

			loginStatusBar.setVisibility(View.VISIBLE);
			loginStatusBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							loginStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mainScrollView.setVisibility(View.VISIBLE);
			mainScrollView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainScrollView.setVisibility(
									show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			loginStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mainScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
    public class GetSurveyNameTask extends AsyncTask<Void, Void, Boolean> {
    	private String message = "";
    	
    	@Override
		protected Boolean doInBackground(Void... arg0) {

    		String geoserverUrl = "";
			if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
				geoserverUrl = config.getUrls().get("wfs");
			}else{
				message = getString(R.string.noWfsFoundMessage);
				return false;
			}
			
			boolean haveConnection = haveNetworkConnection();
			
			List<String> layers = new ArrayList<String>();
			layers.add(layerName);
			if(haveConnection){
				try{
					GetXmlFromGeoserver.getDescribeFeatureType(geoserverUrl, layerName, loggedUser, pass, sessionId, mainContext);
					HashMap<String, FeatureElementAttribute> describeFeatureMap = featureParser.parseDescribeFeature(layerName);
					if(!describeFeatureMap.containsKey(creatorField))
						creatorField = "";
						
					if(!"".equals(creatorField)){
						// Alert, CREATOR not found in table or mismatch between config and table
					}
					
					GetXmlFromGeoserver.getFeature(geoserverUrl, layers, firstAttributeForQuery, false, creatorField, loggedUserFilter, loggedUser, pass, sessionId, mainContext); // fileEncr.getPass(userDataFilePath, loggedUser)
				}catch(AuthenticationException e){
					int errorCode = e.getErrorCode();
					if(errorCode==401)
						message = getString(R.string.unauthorizedUser);
	    			else
	    				message = getString(R.string.genericAuthenticationError);
	    			return false;
				}
			}
			
			// Parsing the file (got from geoserver) if can't parse and list is empty show alert
			listaValori = featureParser.parseGetFeatureFile(layers, false, null);
//			if(listaValori.size()==0){ //, config.getLayerByName(layerName).getNameColumn()
//				message = getString(R.string.noConnOrPropFileMessage); 
//				return false;
//			}
			
			recordsMapFromLocal = LocalFileHandler.getFilePathByUser(localFilePath,loggedUser, layerName);
			
			List<ObjFeature> recordsFromLocal = new ArrayList<ObjFeature>();
			ObjFeature oFeatureTemp = new ObjFeature(layerName);
			List<ObjRecord> oRecordList = new ArrayList<ObjRecord>();
			if(recordsMapFromLocal!=null && recordsMapFromLocal.size()>0){
				for(String key : recordsMapFromLocal.keySet()){
					ObjRecord oRecordTemp = new ObjRecord();
					oRecordTemp.setId(key);
					List<ObjValue> oValueList = new ArrayList<ObjValue>();
					ObjValue oValueTemp = new ObjValue();
					oValueTemp.setColumn_name(config.getLayerByName(layerName).getNameColumn());
					oValueTemp.setValue(recordsMapFromLocal.get(key));
					oValueList.add(oValueTemp);
					oRecordTemp.setValues(oValueList);
					oRecordList.add(oRecordTemp);
				}
			}
			oFeatureTemp.setRecords(oRecordList);
			recordsFromLocal.add(oFeatureTemp);
			listaValori.get(0).getRecords().addAll(oRecordList);
			
			Collections.sort(listaValori.get(0).getRecords(), new Comparator<ObjRecord>() {
			    public int compare(ObjRecord object1, ObjRecord object2) {
			    	return object2.getId().compareToIgnoreCase(object1.getId());
			    }
			});
			// 
			// recordsFromFileMap = process file
			
			// Argument of intent LIST SURVEY is featureParser.getRecordListByName, so get from mappa.keySet() name of survey to show and then give to selected survey intent mappa.get(key=name of survey)
			//recordsMap = featureParser.getRecordListByName();
			/*
			HashMap<String, List<ObjFeature>> rmap = featureParser.getRecordListByName();
			setMap(rmap,listaValori);
			*/
			
			
			return true;
		}
		
		@Override
		protected void onCancelled() {
			getSurveyNameTask = null;
			showProgress(false);
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			getSurveyNameTask = null;
			showProgress(false);

			if (success) {
				insertFieldIntoLayout();
			} else {
				//showAlert(message);
				showGenericAlert(getString(R.string.warningLabel), message, true);
			}
		}
    }
    
    
    

}
