package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AttributesExtractor;
import it.zerofill.soundmapp.controllers.FileEncryptor;
import it.zerofill.soundmapp.models.AttributeType;
import it.zerofill.soundmapp.models.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

public class RegistrationActivity extends Activity{
	
	private LinearLayout formVariableFieldsLayout;
	private Button signupButton;
	
	private Context mainContext;
	
	private EditText userTextView;
	private EditText emailTextView;
	private EditText passwordTextView;
	private EditText passwordRepeatTextView;
	private View signupStatusBar;
	private View mainScrollView;
	
	private String username;
	private String email;
	private String pass;
	private String repPass;
	
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	private String localDir;
	private FileEncryptor fileEncr;
	private String userDataFilePath;
	
	private ImageView logoBannerImg;
	private final int WIDTH = 720;
	private int deviceWidth;
	
	private RegistrationTask registrationTask;
	
	private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private Configuration config;
	private List<AttributeType> sortedAttributes;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        init();
        getConfigAttributes();
     //   createFieldLayout();
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
    
    @Override
	public void onBackPressed(){
    	finish();
    }
    
    private void init(){
    	mainContext = this;
    	
    	DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		deviceWidth = metrics.widthPixels;
    	
		userTextView = (EditText)findViewById(R.id.userText);
    	emailTextView = (EditText)findViewById(R.id.emailText);
    	passwordTextView = (EditText)findViewById(R.id.passwordText);
    	passwordRepeatTextView = (EditText)findViewById(R.id.passwordRepeatText);
    	
    	signupStatusBar = findViewById(R.id.signupStatusLayout); 
    	mainScrollView = findViewById(R.id.mainScroll);
    	
    	formVariableFieldsLayout = (LinearLayout)findViewById(R.id.formVariableFieldsLayout);
    	signupButton = (Button)findViewById(R.id.signupButton);
    	
    	assetsPropertyReader = new AssetsPropertyReader(mainContext);
        properties = assetsPropertyReader.getProperties("settings.properties");
    	//localDir = Environment.getExternalStorageDirectory().toString() + "/" + properties.getProperty("homeDirectory");
    	localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
    	fileEncr = new FileEncryptor();
		userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");
		
		logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
		
    }
    
    @SuppressWarnings("static-access")
	private void getConfigAttributes(){
    	try{
    		Bundle extras = getIntent().getExtras();
        	if (extras != null){
        		config = (Configuration)extras.getSerializable("config");
        	}
        	sortedAttributes = attributeExstractor.getSortedAttributes(config.getRegConfig());
    	}catch(Exception e){}
    }
    
    private void buttonHandler(){
    	signupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				closeKeyboard();
				validateAndRegister();
			}
    	});
    }
    
    private void validateAndRegister(){
    	
    	// Reset errors
    	userTextView.setError(null);
		emailTextView.setError(null);
		passwordTextView.setError(null);
		passwordRepeatTextView.setError(null);
		
		username = userTextView.getText().toString().trim();
		email = emailTextView.getText().toString().trim();
		pass = passwordTextView.getText().toString();
		repPass = passwordRepeatTextView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		
		if (TextUtils.isEmpty(username)) {
			userTextView.setError(getString(R.string.error_field_required));
			focusView = userTextView;
			cancel = true;
		}
		
		else if (TextUtils.isEmpty(email)) {
			emailTextView.setError(getString(R.string.error_field_required));
			focusView = emailTextView;
			cancel = true;
		}
		
		else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			emailTextView.setError(getString(R.string.error_invalid_email));
			focusView = emailTextView;
			cancel = true;
		}
		
		if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(repPass)) {
			if(TextUtils.isEmpty(pass)){
				passwordTextView.setError(getString(R.string.error_field_required));
				focusView = passwordTextView;
			}
			if(TextUtils.isEmpty(repPass)){
				passwordRepeatTextView.setError(getString(R.string.error_field_required));
				focusView = passwordRepeatTextView;
			}
			cancel = true;
		}else if(!pass.equals(repPass)){
			passwordTextView.setError(getString(R.string.error_field_passnotmatch));
			focusView = passwordTextView;
			cancel = true;
		} 
		
		
		if (cancel) {
			// There was an error; don't attempt register and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			//loginMessageView.setText(R.string.loginProgressText);
			showProgress(true);
			registrationTask = new RegistrationTask();
			registrationTask.execute((Void) null);
		}
		
    }
    
    
    
    @SuppressWarnings("unused")
	private void createFieldLayout(){
    	
    	if(sortedAttributes==null) return;
    	
    	for(AttributeType attr : sortedAttributes){
    		
    		LinearLayout layoutTemp = new LinearLayout(mainContext);

    		// layout attributes
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    		layoutTemp.setLayoutParams(params);
    		layoutTemp.setOrientation(LinearLayout.HORIZONTAL);
    		
    		// resize width according to screen's size device
    		LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(200 ,LayoutParams.WRAP_CONTENT); 
    		LinearLayout.LayoutParams spinnerViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
    		LinearLayout.LayoutParams freeTextViewParams = new LinearLayout.LayoutParams(350,LayoutParams.WRAP_CONTENT);
    		
    		TextView tvTemp = new TextView(mainContext);
    		tvTemp.setLayoutParams(textViewParams);
    		tvTemp.setText(attr.getName());
    		
    		int maxLine = 0;
    		double ratio = attr.getName().length()/16;
    		maxLine = ((int)ratio)+2;
    		if(attr.getName().length()>16)
    			tvTemp.setMinLines(maxLine);
    		
    		layoutTemp.addView(tvTemp);
    		
    		if(attr.getType().equals("combo")){
        		Spinner spTemp = new Spinner(mainContext);
        		spTemp.setLayoutParams(spinnerViewParams);
        		//spTemp.setId();
        		// Populate Dropdown list with items retrieved from server
        		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainContext,android.R.layout.simple_spinner_item, attr.getTypeConfig());
        		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		spTemp.setAdapter(dataAdapter);
        		
        		layoutTemp.addView(spTemp);
    		}
    		
    		if(attr.getType().equals("Integer") || attr.getType().equals("Double") || attr.getType().equals("String")){
    			EditText etTemp = new EditText(mainContext);
    			etTemp.setLayoutParams(freeTextViewParams);
    			//etTemp.setId();
    			etTemp.setMaxLines(1);
        		etTemp.setSingleLine(true);   			
    			
    			layoutTemp.addView(etTemp);
    		}
    		
    		if(attr.getType().equals("Date")){
    			
    		}
    		
    		if(attr.getType().equals("Image")){
    			
    		}
    		
    		formVariableFieldsLayout.addView(layoutTemp);
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

			signupStatusBar.setVisibility(View.VISIBLE);
			signupStatusBar.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							signupStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
			signupStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
			mainScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void closeActiviy(){
		this.finish();
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
	
	public void showAlert(String title, String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            	closeActiviy();
            }
        });
        alertDialog.show();
    }
    
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
	
	 public class RegistrationTask extends AsyncTask<Void, Void, Boolean> {

		 private String sessionid;
		private String returnedMessage;
		 
		@Override
		protected Boolean doInBackground(Void... arg0) {
			InputStream inputStream = null;
			returnedMessage = "";
			try{
				
				String optimaBaseUrl = "";
    			if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("auth")){
    				optimaBaseUrl = config.getUrls().get("auth");
  				}else{
  					returnedMessage = getString(R.string.serverNotAvailableError);
  					return false;
  				}
				
				/***********************************************
				 *  ADMIN LOGIN TO OPTIMA TO GET SESSIONID
				 ***********************************************/
				
				//String optimaGetFakeUserUrl = "http://ts.piemonte.optima.sistemaits.com/optima-wsi/auth/login/?uname=admin&password=admin&configuration=svrtoc";
				
				String optimaGetFakeUserUrl = optimaBaseUrl +"/auth/login/?uname=admin&password=admin&configuration=svrtoc"; 
				
				HttpClient httpc = new DefaultHttpClient();
				// Create a local instance of cookie store
		        CookieStore cookieStore = new BasicCookieStore();
		        // Create local HTTP context
		        HttpContext localContext = new BasicHttpContext();
		        // Bind custom cookie store to the local context
		        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		        HttpGet httpget = new HttpGet(optimaGetFakeUserUrl);
		        // Pass local context as a parameter
		        HttpResponse response = httpc.execute(httpget, localContext);
		        HttpEntity entity = response.getEntity();
		        
		        if (entity != null) {
		        	sessionid = EntityUtils.toString(entity);
		        }

				
				/**********************************************
				 * REGISTER NEW USER ON OPTIMA
				 **********************************************/
    			/*
    			{
    			uname: "tommaso",
    			realName: "Tommaso Doninelli",
    			email: tommaso.doninelli@sistemaits.com,
    			password: "pippo123",
    			memberOf: [{
    				roleID: "SVR-operatori",
    				description: ""
    			}]
				}
    			*/
    			JSONObject userJson = new JSONObject();
    			userJson.put("uname", username);
    			userJson.put("realName", username);
    			userJson.put("email", email);
    			userJson.put("password", pass);
    			JSONObject roleJson = new JSONObject();
    			roleJson.put("roleID", "urbis");
    			roleJson.put("description", "urbis");
    			
    			JSONObject roleMandatoryJson = new JSONObject();
    			roleMandatoryJson.put("roleID", "ts_user");
    			roleMandatoryJson.put("description", "*DO NOT DELETE THIS ROLE* All user that can login in TrafficSupervisor MUST have this role");
    			
    			JSONArray arrayJson = new JSONArray();
    			arrayJson.put(roleJson);
    			arrayJson.put(roleMandatoryJson);
    			userJson.put("memberOf", arrayJson);
    			
    			// create user
    			// METHOD: POST /optima-wsi/authmanager/users/{uname}
    			// BODY: a valid USER
    			// RETURNS: the added USER, or an error
    			// NOTE: the user must be member of existing groups only
    			
    			//String optimaCreateUserUrl = "http://ts.piemonte.optima.sistemaits.com/optima-wsi/authmanager/users/"+username+"?configuration=svrtoc";
    			
    			String optimaCreateUserUrl = optimaBaseUrl + "/authmanager/users/"+username+"?configuration=svrtoc";
    			
    			if(sessionid!=null && sessionid.length()>0)
    				optimaCreateUserUrl += "&session="+sessionid;
    			else 
    				return false;
    			
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(optimaCreateUserUrl);
			    StringEntity se = new StringEntity(userJson.toString());
			    httpPost.setEntity(se);
			    httpPost.setHeader("Accept", "application/json");
			    httpPost.setHeader("Content-type", "application/json");

			    // execute POST
	            HttpResponse httpResponse = httpclient.execute(httpPost);
	            int responseCode = httpResponse.getStatusLine().getStatusCode();
	            // get response
	            inputStream = httpResponse.getEntity().getContent();
	            if(inputStream != null){
	            	String msg = convertInputStreamToString(inputStream);
	                if(responseCode!=200)
	                	return false;
	            }
	            else
	            	return false;
	            	//returnedMessage = "error";
    			
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			registrationTask = null;
			showProgress(false);
			if (success) {
				// store login/pass to login in previous view on optima and get sessionid
				
				String filename = localDir+"/"+properties.getProperty("userDataFile");
				fileEncr.createEncryptedFile(filename.substring(0, filename.length()-4));
				fileEncr.addUSer(userDataFilePath, username, pass);
				
				showAlert(getString(R.string.registrationLabel), getString(R.string.registrationSuccessMessage));
			}else{
				if(returnedMessage!=null && returnedMessage.length()>0)
					showAlert(getString(R.string.warningLabel), returnedMessage);
				else
					showAlert(getString(R.string.warningLabel), getString(R.string.registrationErrorMessage));
			}
		}
		 
	 }
    
    

}
