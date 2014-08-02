package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AttributesExtractor;
import it.zerofill.soundmapp.controllers.FileEncryptor;
import it.zerofill.soundmapp.models.Configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("unused")
public class LoginActivity extends Activity {
	
	public String username;
	public String pass;
	public String emai;
	public String sessionid;
	
	public String GEOSERVER_USR = "admin";
	public String GEOSERVER_PASS = "geoserver";
	
	private View loginStatusBar;
	private TextView loginMessageView;
	private View mainScrollView;
	private EditText usernameTextView;
	private EditText passTextView;
	private Button loginButton;
	private Button signupButton;
	private Spinner configsCombo;
	private CheckBox rememberMeCB;
	private LinearLayout mLayout;
	private View getPropertiesFileStatusBar;
	private TextView getPropertiesFileMessageView;
	private Context mainContext;
	private ImageView logoBannerImg;
	
	private final int WIDTH = 720;
	private int deviceWidth;
	
	private String loggedUser;
	private List<String> configList;
	private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	
	private boolean isFromHome;
	private String configName;
	private boolean fileExist = false;
	private Configuration configuration;
	private String json;
	private String userDataFilePath;
	private FileEncryptor fileEncr;
	public String localDir;
	
	private UserLoginTask authTask = null;
	private GetPropertiesFileTask getPropFileTask = null;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
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
    
    private void init(){
    	mainContext = this;
    	
    	DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		deviceWidth = metrics.widthPixels;
    	
    	configsCombo = (Spinner)findViewById(R.id.configsCombo);
    	loginStatusBar = findViewById(R.id.loginStatusLayout);
    	mainScrollView = findViewById(R.id.mainScroll);
    	usernameTextView = (EditText)findViewById(R.id.usernameText);
    	passTextView = (EditText)findViewById(R.id.passwordText);
    	loginButton = (Button)findViewById(R.id.loginButton);
    	signupButton = (Button)findViewById(R.id.signupButton);
    	loginMessageView = (TextView)findViewById(R.id.loginStatusMessage);
    	mLayout = (LinearLayout)findViewById(R.id.mLayout);
    	rememberMeCB = (CheckBox)findViewById(R.id.rememberMeCB);
    	logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
        
        
        
    	getPropertiesFileStatusBar = findViewById(R.id.propertiesFileStatusLayout);
    	getPropertiesFileMessageView = (TextView)findViewById(R.id.propertiesFileStatusMessage);
    	
    	assetsPropertyReader = new AssetsPropertyReader(mainContext);
        properties = assetsPropertyReader.getProperties("settings.properties");
        
        //localDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory");
        localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
    	
        loginButton.setEnabled(true);
		signupButton.setEnabled(true);
		
		fileEncr = new FileEncryptor();
		userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");
		
    	getPropertiesFile();
    }
    
    @Override
	public void onBackPressed(){
    	finish();
    }
    
    private void buttonsHandler(){
    	loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				loginButton.requestFocus();
				closeKeyboard();
				attemptLogin();
			}
    	});
    	
    	signupButton.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onClick(View view) {
				closeKeyboard();
				isFromSignUp = true;
				Intent i = new Intent(mainContext, RegistrationActivity.class);
				configName = configsCombo.getSelectedItem().toString();
				configuration = attributeExstractor.getAttrbiutes(json.toString()).get(configName);
				i.putExtra("config", configuration);
				startActivity(i);
			}
    	});
    }
    
    private boolean isFromSignUp = false;
    
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
    
    public void getPropertiesFile() {
    	if(getPropFileTask!=null) return;
    	
    	// check if connection is available, download and parse properties file
    	boolean isCon = haveNetworkConnection();
    	//TEST. remove
    	//isCon = false;
    	if(isCon){
    		getPropertiesFileMessageView.setText(R.string.propertiesFileProgressText);
    		showGetPropFileProgress(true);
    		getPropFileTask = new GetPropertiesFileTask();
    		getPropFileTask.execute((Void) null);
    	}else{
    		// Alert show ( no Internet connection, turn it on)
			// if no connection, check if propeties file exist and read it
        	try{
        		readPropertiesFile();
        		
        		if(!"".equals(fileEncr.getUser(userDataFilePath))){
        		//	String u = fileEncr.getUser(userDataFilePath);
        		//	boolean isValidUser = fileEncr.isValid(userDataFilePath, u);
        		//	if(isValidUser){
        				//getSessionId();
        				//openHomeActivity();

        			loggedUser = fileEncr.getUser(userDataFilePath);
        			pass = fileEncr.getPass(userDataFilePath, loggedUser);
        			usernameTextView.setText(loggedUser);
        			passTextView.setText(pass);
        			attemptLogin();
        			
        		//	}
        		}
        	}catch(Exception exc){
        		exc.printStackTrace();
        	}
    	
        	// if no connection and properties file doesn't exist show error
        	if(!haveNetworkConnection() && !fileExist){
        		showAlert();
        		loginButton.setEnabled(false);
        		signupButton.setEnabled(false);
        	}
    	}
    }
    
    public void showAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.errorLabel));
        alertDialog.setMessage(getString(R.string.noConnOrPropFileMessage));
        // On pressing Settings button
        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        alertDialog.show();
    }
    
    public void showGenericAlert(String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(getString(R.string.warningLabel));
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        
        alertDialog.show();
    }
    
    
    /**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (authTask != null) {
			return;
		}

		// Reset errors
		usernameTextView.setError(null);
		passTextView.setError(null);
		
		// Clear focus, not working properly 
		usernameTextView.clearFocus();
		passTextView.clearFocus();
		
		// Store values at the time of the login attempt.
		username = usernameTextView.getText().toString();
		pass = passTextView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(pass)) {
			passTextView.setError(getString(R.string.error_field_required));
			focusView = usernameTextView;
			cancel = true;
		} 
		// do strict control on password?
		/*
		else if (pass.length() < 4) { 
			passTextView.setError(getString(R.string.error_invalid_password));
			focusView = passTextView;
			cancel = true;
		}*/

		// Check for a valid email address.
		if (TextUtils.isEmpty(username)) {
			usernameTextView.setError(getString(R.string.error_field_required));
			focusView = usernameTextView;
			cancel = true;
		} 
		
		/*
		else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			emailTextView.setError(getString(R.string.error_invalid_email));
			focusView = emailTextView;
			cancel = true;
		}*/

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			loginMessageView.setText(R.string.loginProgressText);
			showProgress(true);
			authTask = new UserLoginTask();
			authTask.execute((Void) null);
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
    
    
    
	
	/**
	 * Shows the progress UI and hides the main scroll view.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showGetPropFileProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			getPropertiesFileStatusBar.setVisibility(View.VISIBLE);
			getPropertiesFileStatusBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							getPropertiesFileStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
			getPropertiesFileStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mainScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		// if come back from home view and user is saved quit application
		if(isFromHome && !"".equals(fileEncr.getUser(userDataFilePath))){
			finish();
		}else if(isFromHome){
			usernameTextView.setText("");
			passTextView.setText("");
			rememberMeCB.setChecked(false);
		}
		
		if(isFromSignUp && !"".equals(fileEncr.getUser(userDataFilePath))){
			loggedUser = fileEncr.getUser(userDataFilePath);
			pass = fileEncr.getPass(userDataFilePath, loggedUser);
			fileEncr.emptyFile(userDataFilePath);
		//	email = fileEncr.getEmail(userDataFilePath, loggedUser);
			usernameTextView.setText(loggedUser);
			passTextView.setText(pass);
			isFromSignUp = false;
		}
	}
    
	
	@SuppressWarnings("static-access")
	private void openHomeActivity(){
		loggedUser = username;
		if(loggedUser==null || "".equals(loggedUser)){
			loggedUser = fileEncr.getUser(userDataFilePath);
			pass = fileEncr.getPass(userDataFilePath, loggedUser);
		}
		Intent i = new Intent(mainContext, HomeActivity.class);
		configName = configsCombo.getSelectedItem().toString();
		configuration = attributeExstractor.getAttrbiutes(json.toString()).get(configName);
		i.putExtra("config", configuration);
		i.putExtra("loggedUser", loggedUser);
		
		i.putExtra("GEOSERVER_USR", GEOSERVER_USR);
		i.putExtra("GEOSERVER_PASS", GEOSERVER_PASS);
		i.putExtra("SESSIONID", sessionid);
		
//		if(!rememberMeCB.isChecked()){
//			i.putExtra("pass", pass);
//		}
		isFromHome = true;
		startActivity(i);
	}
    
	
	 @SuppressWarnings("static-access")
		private void readPropertiesFile() throws Exception{
			String filePath = localDir + "/" + properties.getProperty("propertiesFileName") + ".enc";
			String filePathDecrypted = localDir + "/" + properties.getProperty("propertiesFileName") + ".enc.dec";
			fileEncr.decrypt(filePath);
			
			FileInputStream f = new FileInputStream(filePathDecrypted);
			DataInputStream i = new DataInputStream(f);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(i,"UTF-8"));
			String line = "";
			json = "";
			while ((line = buffer.readLine()) != null){
				json += line + "\n";
			}
			buffer.close();
			File fileDecr = new File(filePathDecrypted);
			fileDecr.delete();
			
			configList = attributeExstractor.getConfigurationsName(json.toString());
			
			if(configList.size()>0 && !configList.get(0).equals("")){
				
			}else{
				showAlert();
				loginButton.setEnabled(false);
        		signupButton.setEnabled(false);
			}
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainContext,android.R.layout.simple_spinner_item, configList);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			configsCombo.setAdapter(dataAdapter);
			
			fileExist = true;
	    }
    
	
	
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
	
	
	 
	 
	
	
	
    
    // Class to handle Login
        
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		
    	private String errorMessage;
    	
    	@SuppressWarnings("static-access")
		@Override
		protected Boolean doInBackground(Void... params) {
			// attempt authentication against a network service.
			try {
				/**********************************************
				 * USING GEOSERVER TO CHECK LOGIN
				 **********************************************/
				
				String geoserverUrl = "";
				configName = configsCombo.getSelectedItem().toString();
				configuration = attributeExstractor.getAttrbiutes(json.toString()).get(configName);
  				if(configuration!=null && configuration.getUrls()!=null && configuration.getUrls().containsKey("wfs")){
  					geoserverUrl = configuration.getUrls().get("wfs")+"/wfs";
  				}else{
  					errorMessage = getString(R.string.serverNotAvailableError);
  					return false;
  				}
  				
				HttpURLConnection httpCon = null;
				URL url = new URL(geoserverUrl);
				httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setDoInput(true);
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("GET");
				
				//String authString = username + ":" + pass; //fileEncr.getPass(userDataFilePath, loggedUser);
				String authString = GEOSERVER_USR + ":" + GEOSERVER_PASS; //fileEncr.getPass(userDataFilePath, loggedUser);
    			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
    			String authStringEnc = new String(authEncBytes);
    			httpCon.setRequestProperty("Authorization", "Basic " + authStringEnc);
    			
    			OutputStream outputStream = httpCon.getOutputStream();
    			outputStream.close();
    			int responseCode = httpCon.getResponseCode();
    			
    			
    			/**********************************************
				 * USING OPTIMA TO CHECK LOGIN
				 **********************************************/
    			
    			// base = http://ts.piemonte.optima.sistemaits.com/optima-wsi
    			
    			//String optimaUrl = "http://ts.piemonte.optima.sistemaits.com/optima-wsi/auth/login/?uname="+username+"&password="+pass+"&configuration=svrtoc";

    			String optimaUrl = "";
    			if(configuration!=null && configuration.getUrls()!=null && configuration.getUrls().containsKey("auth")){
    				optimaUrl = configuration.getUrls().get("auth")+"/auth/login/?uname="+username+"&password="+pass+"&configuration=svrtoc";
  				}else{
  					errorMessage = getString(R.string.serverNotAvailableError);
  					return false;
  				}
    			
    			
				HttpClient httpclient = new DefaultHttpClient();
				// Create a local instance of cookie store
		        CookieStore cookieS = new BasicCookieStore();
		        // Create local HTTP context
		        HttpContext localCntx = new BasicHttpContext();
		        // Bind custom cookie store to the local context
		        localCntx.setAttribute(ClientContext.COOKIE_STORE, cookieS);
		        HttpGet httpget2 = new HttpGet(optimaUrl);
		        // Pass local context as a parameter
		        HttpResponse response2 = httpclient.execute(httpget2, localCntx);
		        HttpEntity entity2 = response2.getEntity();
		        
		        int responseFromOptima = response2.getStatusLine().getStatusCode();
		        
		        if (entity2 != null) {
		        	sessionid = EntityUtils.toString(entity2);
		        }
    		
    			if(responseCode==401){
    				errorMessage = getString(R.string.unauthorizedGeoserverUser);
    				return false;
    			}
    			if(responseFromOptima==401){
    				errorMessage = getString(R.string.unauthorizedUser);
    				return false;
    			}
    			
    			if(responseCode==200 && responseFromOptima==200){
    				return true;
    			}
    			errorMessage = getString(R.string.genericAuthenticationError);
    			return false;
				
				//Thread.sleep(1);
			}catch (Exception e) {
				errorMessage = getString(R.string.serverNotAvailableError);
				e.printStackTrace();
				return false;
			}

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			authTask = null;
			showProgress(false);

			if (success) {
				
				
				File userDataFile = new File(userDataFilePath);
				if(!userDataFile.exists()){
					String filename = localDir+"/"+properties.getProperty("userDataFile");
					fileEncr.createEncryptedFile(filename.substring(0, filename.length()-4));
				}
				
				// if remember me is checked save user and password
				if(rememberMeCB.isChecked()){
					fileEncr.addUSer(userDataFilePath, username, pass);
				}
				
				// goto home
				openHomeActivity();
				
			} else {
				showGenericAlert(errorMessage);
				passTextView.setError(getString(R.string.error_incorrect_password));
				passTextView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			authTask = null;
			showProgress(false);
			errorMessage = getString(R.string.serverNotAvailableError);
			showGenericAlert(errorMessage);
		}
	}
    
    
    
    
    
    
    
    // Class to get properties's file from server
    public class GetPropertiesFileTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean res = true;
	    	File newdir = new File(localDir); 
	    	if(newdir.exists()==false) newdir.mkdirs();
			String serverURL = properties.getProperty("propertiesFileURL");
			BufferedReader reader=null;
			try{
				URL url = new URL(serverURL);
				URLConnection conn = url.openConnection();
               // conn.setDoOutput(true);
               // String data ="&" + URLEncoder.encode("data", "UTF-8") + "=";
               // OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
               // wr.write(data);
               // wr.flush();
				
				
				//http://mirror.nohup.it/apache//commons/io/binaries/
//				String defaultEncoding = "UTF-8";
//				InputStream inputStream = conn.getInputStream();
//				try {
//				    BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
//				    ByteOrderMark bom = bOMInputStream.getBOM();
//				    String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
//				    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);
//				    //use reader
//				} finally {
//				    inputStream.close();
//				}
				
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                // Read Server Response
                sb.append("{ \"Android\": \n"); // Add header and footer
                while((line = reader.readLine()) != null){
                	//Append server response in string
                	sb.append(line + "\n");
                }
                sb.append("}");
                File file = new File(newdir,properties.getProperty("propertiesFileName"));
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(sb.toString());
                writer.close();
                
                fileEncr.createEncryptedFile(file.getAbsolutePath());
                
			}catch(Exception ec){
				// show alert error
				res = false;
				ec.printStackTrace();
			}
			return res;
		}
		
		
		@Override
		protected void onPostExecute(final Boolean success) {
			authTask = null;
			showGetPropFileProgress(false);
			if (success) {
				try{
					readPropertiesFile();
					// For future implementation
					// getImageFromConfig();
					// setImage();
					
					if(!"".equals(fileEncr.getUser(userDataFilePath)))
			    		openHomeActivity();
					
				}catch(Exception e){
					e.printStackTrace();
				}
				
			} else {
				showAlert();
				loginButton.setEnabled(false);
        		signupButton.setEnabled(false);
			}
		}

		@Override
		protected void onCancelled() {
			authTask = null;
			showGetPropFileProgress(false);
			loginButton.setEnabled(false);
    		signupButton.setEnabled(false);
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// ******************************************************************************
		// For future implementation
		// not working yet!
		// ******************************************************************************
		
		private void saveImageFromUrl(String url, String confName, String backgroundDir, String suffix) throws Exception{
			URL ubackground = new URL(url);
			URLConnection connbackground = ubackground.openConnection();
			
			InputStream inputStream = null;
            HttpURLConnection httpConn = (HttpURLConnection)connbackground;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
             inputStream = httpConn.getInputStream();
            }
            File backgroundFile = new File(backgroundDir,confName+suffix);
            FileOutputStream fos = new FileOutputStream(backgroundFile);  
         //   int totalSize = httpConn.getContentLength();
         //   int downloadedSize = 0;   
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) >0 ) {                 
                fos.write(buffer, 0, bufferLength);                  
               // downloadedSize += bufferLength;                 
            }   
            fos.close();
		}
		
		@SuppressWarnings("static-access")
		private void getImageFromConfig() throws Exception{
            //String backgroundDir = Environment.getExternalStorageDirectory().toString() + "/SoundmApp/background";
            String backgroundDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/SoundmApp/background";
	    	File bdir = new File(backgroundDir); 
	    	if(bdir.exists()==false) bdir.mkdirs();
            // Try to retrieve background image and banner from server
            for(String confName : configList){
            	Configuration conf = attributeExstractor.getAttrbiutes(json.toString()).get(confName);
            	 for(String key : conf.getUrls().keySet()){
                 	if(key.equals("background")){
                 		String urlbackground = conf.getUrls().get(key);
                 		saveImageFromUrl(urlbackground, confName,backgroundDir,"_background.png");
                 	}
                 	if(key.equals("banner")){
                 		String urlbanner = conf.getUrls().get(key);
                 		saveImageFromUrl(urlbanner, confName,backgroundDir,"_banner.png");
                 	}
                 }
            }
		}
		
		// setBackground is AVAILABLE only after API 16!
		private void setImage(){
			String backgroundPathName = "";
			String bannerPathName = ""; 
			Drawable background = Drawable.createFromPath(backgroundPathName);
			Drawable banner = Drawable.createFromPath(bannerPathName);
		//	mLayout.setBackground(background);
			/*
			if( background_background.png EXISTs)
				mLayout.setBackground(background);
			if (banner_background.png EXISTs)
				mLayout.setBackground(background);
			
			*/
		}
		
		// ******************************************************************************
		// ******************************************************************************
		
		
		
		
		

		
	}
    
    
    
    
    
}
