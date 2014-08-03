package it.zerofill.soundmapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import it.zerofill.soundmapp.SurveyDetailActivity.WaitTask;
import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AttributesExtractor;
import it.zerofill.soundmapp.controllers.AuthenticationException;
import it.zerofill.soundmapp.controllers.FeatureParser;
import it.zerofill.soundmapp.controllers.FileEncryptor;
import it.zerofill.soundmapp.controllers.GetXmlFromGeoserver;
import it.zerofill.soundmapp.models.Configuration;
import it.zerofill.soundmapp.models.CustomObj;
import it.zerofill.soundmapp.models.Layer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

@SuppressWarnings("unused")
public class HomeActivity extends Activity{
	
	private Button newSurveyButton;
	private Button surveyListButton;
	private Button logoutButton;
	private View loginStatusBar;
	private View mainScrollView;
	private Spinner layersCombo;
	private LinearLayout mLayout;
	
	private String loggedUserFilter;
	private String loggedUser;
	private String userDataFilePath;
	private FileEncryptor fileEncr;
	private Context mainContext;
	private String localDir;
	
	public String sessionId;
	
	private ImageView logoBannerImg;

	private final int WIDTH = 720;
	private int deviceWidth;
	
	private FeatureParser featureParser;
	//private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	private Configuration config;
	
	//private GetWFSFilesTask getWfsTask;
	public GetCapabilitiesFromGeoserverTask getCapabilitiesTask;

//	private HashMap<String, List<ObjFeature>> recordsMap;
//	private List<ObjFeature> listaValori;
	
	private List<String> selectedNameSpaces;
	private HashMap<Integer, String> nameSpaceMapByPosition;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        init();
        
        buttonsHandler();
        
     //   showProgress(true);
     //   getWfsTask = new GetWFSFilesTask();
     //   getWfsTask.execute((Void) null);
        
        showProgress(true);
        getCapabilitiesTask = new GetCapabilitiesFromGeoserverTask();
        getCapabilitiesTask.execute((Void) null);
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
   	}
	    
    private void init(){
    	mainContext = this;
    	
    	DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		deviceWidth = metrics.widthPixels;
		
		getWindow().setBackgroundDrawableResource(R.drawable.background);
    	
    	assetsPropertyReader = new AssetsPropertyReader(mainContext);
    	properties = assetsPropertyReader.getProperties("settings.properties");
    	newSurveyButton = (Button)findViewById(R.id.newSurveyButton);
    	surveyListButton = (Button)findViewById(R.id.surveyListButton);
    	logoutButton = (Button)findViewById(R.id.logoutButton);
    	loginStatusBar = findViewById(R.id.loginStatusLayout);
    	mainScrollView = findViewById(R.id.mainScroll);
    	layersCombo = (Spinner)findViewById(R.id.layersCombo);
    	
    	mLayout = (LinearLayout)findViewById(R.id.mLayout);
    	
    	surveyListButton.setEnabled(true);
    	newSurveyButton.setEnabled(true);
    	
    	featureParser = new FeatureParser(mainContext);
    	
    	//localDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory");
    	localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
    	userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");
    	fileEncr = new FileEncryptor();
    	
    	logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
    	
    	getConfigAttributes();
    }
    
    public void disableButtons(){
    	newSurveyButton.setEnabled(false);
    	surveyListButton.setEnabled(false);
    	
    }
    
    private void buttonsHandler(){
    	
    	newSurveyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				String nameSapce_Layer = nameSpaceMapByPosition.get(layersCombo.getSelectedItemPosition());
				
				Intent i = new Intent(mainContext, SurveyDetailActivity.class);
				i.putExtra("config", config);
				i.putExtra("isNewSurvey", true);
				i.putExtra("isLocal", true);
				i.putExtra("layerName", nameSapce_Layer);
				i.putExtra("loggedUser", loggedUser);
				i.putExtra("loggedUserFilter", loggedUserFilter);
				i.putExtra("pass", pass);
				startActivity(i);
			}
    	});
    	
    	surveyListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(mainContext, SurveyListActivity.class);
				String nameSapce_Layer = nameSpaceMapByPosition.get(layersCombo.getSelectedItemPosition());
				intent.putExtra("config", config);
				intent.putExtra("layerName", nameSapce_Layer);
				intent.putExtra("loggedUser", loggedUser);
				intent.putExtra("pass", pass);
				intent.putExtra("loggedUserFilter", loggedUserFilter);
				startActivity(intent);
			}
    	});
    	
    	logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doLogout();
			}
    	});
    	
    	
    	
    }
    
    private String pass;
    @SuppressLint("UseSparseArrays")
	private void getConfigAttributes(){
    	try{
    		Bundle extras = getIntent().getExtras();
        	if (extras != null){
        		config = (Configuration)extras.getSerializable("config");
//        		loggedUser = extras.getString("loggedUser");
        		loggedUserFilter = extras.getString("loggedUser");
//        		pass = extras.getString("pass");
        		loggedUser = extras.getString("GEOSERVER_USR");
        		pass = extras.getString("GEOSERVER_PASS");
        		sessionId = extras.getString("sessionId");
        	}
        	
        	// populate combobox with layers
        	List<String> lcombo = new ArrayList<String>();
        	if(config!=null && config.getLayers()!=null){
        		List<Layer> layers = config.getLayers();
        		for(Layer layer : layers){
            		lcombo.add(layer.getName());
            	}	
        	}
        	
			selectedNameSpaces = new ArrayList<String>();
			nameSpaceMapByPosition = new HashMap<Integer, String>();
			if(config!=null && config.getNameSpace()!=null && config.getNameSpace().size()>0 && (!config.getNameSpace().get(0).equals(""))){
				// TODO in this version selected namespace can be only one! So we are going to take the first
				// element of the list. Handling more namespaces may be implemented in the future.
				selectedNameSpaces.addAll(config.getNameSpace());
			}else{
				showAlert(getString(R.string.noNamespaceFoundMessage));
				surveyListButton.setEnabled(false);
				newSurveyButton.setEnabled(false);
			}
    	}catch(Exception e){}
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
    
    public void showAlert(String messageError){
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
    
    @Override
    public void onBackPressed(){
    	if(!"".equals(fileEncr.getUser(userDataFilePath)))
    		finish();
    	else
    		doLogout();
    	//this.finishAffinity();
    }
    
    private void doLogout(){
    	AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setMessage(getString(R.string.logoutMessage));
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	//TODO invalidate sessione and go back to login view
            	fileEncr.emptyFile(userDataFilePath);
            	finish();
            }
        });
        alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
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
    
    
	
	
	public class GetCapabilitiesFromGeoserverTask extends AsyncTask<Void, Void, Boolean> {

		private String message = "";
		private List<String> layerList;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			String geoserverUrl = "";
			if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
				geoserverUrl = config.getUrls().get("wfs");
			}else{
				message = getString(R.string.noWfsFoundMessage);
				return false;
			}
			
			boolean haveConnection = haveNetworkConnection();
			
//			if(pass == null || "".equals(pass)){
//				pass = fileEncr.getPass(userDataFilePath, loggedUser);
//			}
			
			if(haveConnection){
				try{
					GetXmlFromGeoserver.getCapabilites(geoserverUrl, loggedUser, pass, sessionId, mainContext);
				}catch(AuthenticationException e){
					int errorCode = e.getErrorCode();
					if(errorCode==401)
						message = getString(R.string.unauthorizedUser);
	    			else
	    				message = getString(R.string.genericAuthenticationError);
	    			return false;
				}
			}
				
			//layerList = featureParser.parseGetCapabilitesFile(selectedNameSpaces);
			layerList = new ArrayList<String>();
			List<String> layerListTmp = new ArrayList<String>();
			layerListTmp = featureParser.parseGetCapabilitesFile(selectedNameSpaces);
			
			HashSet<String> layerFromConfig = new HashSet<String>();
			for(Layer layer : config.getLayers()){
				layerFromConfig.add(layer.getName());
			}
			for(String layerTmp : layerListTmp){
				if(layerFromConfig.contains(layerTmp))
					layerList.add(layerTmp);
			}
			
			if(layerList.size()==0){
				message = getString(R.string.noConnOrGeoserverFileMessage);
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onCancelled() {
			getCapabilitiesTask = null;
			showProgress(false);
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			getCapabilitiesTask = null;
			showProgress(false);
			
			if (success) {
				int index = 0;
				List<String> layerComboList = new ArrayList<String>(); 
				for(String layer : layerList){
					String layerName = layer.substring(layer.indexOf(":")+1, layer.length());
					nameSpaceMapByPosition.put(index,layer);
					layerComboList.add(layerName);
					index++;
				}
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainContext,android.R.layout.simple_spinner_item, layerComboList);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				layersCombo.setAdapter(dataAdapter);
				
			} else {
				showAlert(message);
				disableButtons();
			}
		}
		
	}
	
    
}
