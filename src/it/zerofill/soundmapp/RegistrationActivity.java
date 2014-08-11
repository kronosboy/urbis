package it.zerofill.soundmapp;

import it.zerofill.soundmapp.controllers.AssetsPropertyReader;
import it.zerofill.soundmapp.controllers.AttributesExtractor;
import it.zerofill.soundmapp.controllers.AuthenticationException;
import it.zerofill.soundmapp.controllers.FeatureParser;
import it.zerofill.soundmapp.controllers.FileEncryptor;
import it.zerofill.soundmapp.controllers.GetXmlFromGeoserver;
import it.zerofill.soundmapp.controllers.SurveyXmlBuilder;
import it.zerofill.soundmapp.models.AttributeType;
import it.zerofill.soundmapp.models.Configuration;
import it.zerofill.soundmapp.models.FeatureElementAttribute;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class RegistrationActivity extends Activity{
	
	private LinearLayout formVariableFieldsLayout;
	private Button signupButton;
	
	private Context mainContext;
	
	private final String TYPE_COMBO = "combo";
	private final String TYPE_STRING = "String";
	private final String TYPE_DOUBLE = "Double";
	private final String TYPE_INT = "Integer";
	private final String TYPE_DATE = "Date";
	
	private EditText userTextView;
	private EditText emailTextView;
	private EditText passwordTextView;
	private EditText passwordRepeatTextView;
	private View signupStatusBar;
	private View mainScrollView;
	private View getDescribeFeatureStatusBar;
	
	private String username;
	private String email;
	private String pass;
	private String repPass;
	private Resources resource = null;
	
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	private String localDir;
	private String anagDir;
	private FileEncryptor fileEncr;
	private String userDataFilePath;
	private String anagLayer;
	private HashMap<String, FeatureElementAttribute> describeFeatureMap;
	private FeatureParser featureParser;
	private HashMap<String, Boolean> mandatoryFieldMap;
	private LinearLayout.LayoutParams titleViewParams;
	
	private ImageView logoBannerImg;
	private final int WIDTH = 720;
	private final int HEIGHT = 1280;
	private int deviceWidth;
	private int deviceHeight;
	
	private int SpinnerID = 0;
    private int FreeTextID = 100;
    private int DateComponentID = 200;
    private HashMap<Integer, String> spinnerCompMap;
    private HashMap<Integer, String> freeTextCompMap;
   // private HashMap<String, ObjValue> recordsMap;
    
    private HashMap<String, String> mappaValoriAttributi;
    private HashMap<String, AttributeType> attributesMap;
    private HashMap<String, Boolean> errorFieldMap;
    private HashMap<Integer, EditText> mappaDateComponent;
    private HashMap<Integer, String> mappaDateComponentId;
    
    private int calendar_img_button_id;
	private int calendar_img_button_pressed_id;
	
	private int del_img_button_id;
	private int del_img_button_pressed_id;
	
	private int selectedDateComponentId;
	private boolean isDateChanged = false;
	private DatePickerDialog.OnDateSetListener dateSetListener;
	private int year;
	private int month;
	private int day;
	
	private RegistrationTask registrationTask;
	
	private AttributesExtractor attributeExstractor = AttributesExtractor.getInstance();
	private Configuration config;
	private List<AttributeType> sortedAttributes;
	private GetDescribeFeatureTask getDescribeTask;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        init();
        getConfigAttributes();
        
        showGetDescribeFeatureProgress(true);
        getDescribeTask = new GetDescribeFeatureTask();
        getDescribeTask.execute((Void) null);
        
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
		deviceHeight = metrics.heightPixels;
		
		resource = new Resources(getAssets(), metrics, null);
		getWindow().setBackgroundDrawableResource(R.drawable.background);
    	
		userTextView = (EditText)findViewById(R.id.userText);
    	emailTextView = (EditText)findViewById(R.id.emailText);
    	passwordTextView = (EditText)findViewById(R.id.passwordText);
    	passwordRepeatTextView = (EditText)findViewById(R.id.passwordRepeatText);
    	
    	signupStatusBar = findViewById(R.id.signupStatusLayout); 
    	mainScrollView = findViewById(R.id.mainScroll);
    	getDescribeFeatureStatusBar = findViewById(R.id.getDescribeFeatureLayout);
    	
    	formVariableFieldsLayout = (LinearLayout)findViewById(R.id.formVariableFieldsLayout);
    	signupButton = (Button)findViewById(R.id.signupButton);
    	
    	featureParser = new FeatureParser(mainContext);
    	assetsPropertyReader = new AssetsPropertyReader(mainContext);
        properties = assetsPropertyReader.getProperties("settings.properties");
    	localDir = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory");
    	anagDir = localDir + "/" + properties.getProperty("anagDirectory");
    	fileEncr = new FileEncryptor();
		userDataFilePath = localDir+"/"+properties.getProperty("userDataFile");
		
		String BASE_PATH = getExternalFilesDir(null).getAbsolutePath().toString() + "/" + properties.getProperty("homeDirectory") + "/" + properties.getProperty("geoserverDirectory");
		File newBaseDir = new File(BASE_PATH);
		if(!newBaseDir.exists())
			newBaseDir.mkdirs();
		
		File newAnagdir = new File(anagDir); 
		if(!newAnagdir.exists())
			newAnagdir.mkdirs();
		
		logoBannerImg = (ImageView)findViewById(R.id.logoBannerImg);
    	// Fixed dimension for a button (on default screen it will be 150x150)
		final int FIXED_BANNER_W_DIM = 720; 
		float FIXED_BANNER_WIDTH_PERC = ((float) FIXED_BANNER_W_DIM /WIDTH) * 100;
		int FIXED_BANNER_WIDTH_VALUE = (int)((FIXED_BANNER_WIDTH_PERC * deviceWidth) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams bannerViewParams = new LinearLayout.LayoutParams(FIXED_BANNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
        logoBannerImg.setLayoutParams(bannerViewParams);
        
        final int FIXED_TITLE_DIM = 200;
		float FIXED_TITLE_WIDTH_PERC = ((float) FIXED_TITLE_DIM /WIDTH) * 100;
		int FIXED_TITLE_WIDTH_VALUE = (int)((FIXED_TITLE_WIDTH_PERC * deviceWidth) / 100);
		titleViewParams = new LinearLayout.LayoutParams(FIXED_TITLE_WIDTH_VALUE ,LayoutParams.WRAP_CONTENT); 
		
		spinnerCompMap = new HashMap<Integer, String>();
        freeTextCompMap = new HashMap<Integer, String>();
        mappaValoriAttributi = new HashMap<String, String>();
        errorFieldMap = new HashMap<String, Boolean>();
        mappaDateComponent = new HashMap<Integer, EditText>();
        mappaDateComponentId = new HashMap<Integer, String>();
        mandatoryFieldMap = new HashMap<String, Boolean>();
        
        calendar_img_button_id = resource.getIdentifier("calendar_btn", "drawable","it.zerofill.soundmapp");
		calendar_img_button_pressed_id = resource.getIdentifier("calendar_btn_pressed", "drawable","it.zerofill.soundmapp");
		//calendar_img_button_disabled_id = resource.getIdentifier("calendar_btn_disabled", "drawable","it.zerofill.soundmapp");
		del_img_button_id = resource.getIdentifier("delete", "drawable","it.zerofill.soundmapp");
		del_img_button_pressed_id = resource.getIdentifier("delete_pressed", "drawable","it.zerofill.soundmapp");
		//del_img_button_disabled_id = resource.getIdentifier("delete_disabled", "drawable","it.zerofill.soundmapp");
		
		dateSetListener = new DatePickerDialog.OnDateSetListener() {
	        public void onDateSet(DatePicker view, int myear, int monthOfYear,int dayOfMonth) {
	            year = myear;
	            month = monthOfYear;
	            day = dayOfMonth;
	            updateDateDisplay();
	        }
	    };
    }
    
    private String userAttrId;
    
    @SuppressWarnings("static-access")
	private void getConfigAttributes(){
    	try{
    		Bundle extras = getIntent().getExtras();
        	if (extras != null){
        		config = (Configuration)extras.getSerializable("config");
        		anagLayer = config.getAnagLayer();
        		attributesMap = config.getRegConfigMap();
        		if(attributesMap!=null && attributesMap.size()>0){
        			for(String key : attributesMap.keySet()){
        				if("user".equals(attributesMap.get(key).getType())){
        					userAttrId = attributesMap.get(key).getId();
        				}
        			}
        		}
        	}
        	sortedAttributes = attributeExstractor.getSortedAttributes(config.getRegConfig());
    	}catch(Exception e){}
    }
    
    public boolean isRegistering = false;
    private void buttonHandler(){
    	signupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				closeKeyboard();
				
				if(!haveNetworkConnection()){
					showAlert(getString(R.string.warningLabel), getString(R.string.noConnMessage));
				}else{
					if(hasAnagLayer && userAttrId!=null){
						isRegistering = true;
						saveFeatureTask = new SaveFeatureTask();
		        		saveFeatureTask.execute(userAttrId,userTextView.getText().toString().trim(),"update");	
					}else
						validateAndRegister();
				}
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
    
    private boolean isViewCompleted = false;
    
	private void createFieldLayout(){
    	if(sortedAttributes==null || anagLayer==null) return;
    	
    	for(AttributeType attr : sortedAttributes){
    		if(describeFeatureMap.containsKey(attr.getId())){
    			describeFeatureMap.get(attr.getId()).setNullable(attr.isNullable());
    			
    			if(attr.getType().equalsIgnoreCase(TYPE_COMBO)){
    				addSpinner(formVariableFieldsLayout, attr);
    			}
    			
    			else if(attr.getType().equalsIgnoreCase(TYPE_STRING)){
    				addFreeText(formVariableFieldsLayout, attr, "string");
    			}
    			
    			else if(attr.getType().equalsIgnoreCase(TYPE_DOUBLE)){
    				addFreeText(formVariableFieldsLayout, attr, "double");
    			}
    			
    			else if(attr.getType().equalsIgnoreCase(TYPE_INT)){
    				addFreeText(formVariableFieldsLayout, attr, "integer");
    			}
    			
    			else if(attr.getType().equalsIgnoreCase(TYPE_DATE)){
    				addDate(formVariableFieldsLayout, attr);
    			}
    			
    			Handler handler = new Handler();
    			handler.postDelayed(new Runnable() {
    				@Override
    				public void run() {
    					isViewCompleted = true;
    				}
    			}, 1000);
    			
    		}
    	}
    }
    
	
	
	
	
	
	
	private void addSpinner(LinearLayout main, AttributeType attr){
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
		
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/12;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>12)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_SPINNER_DIM = 350;
		float FIXED_SPINNER_WIDTH_PERC = ((float) FIXED_SPINNER_DIM /WIDTH) * 100;
		int FIXED_SPINNER_WIDTH_VALUE = (int)((FIXED_SPINNER_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams spinnerViewParams = new LinearLayout.LayoutParams(FIXED_SPINNER_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
		Spinner spinnerView = new Spinner(mainContext);
		spinnerView.setId(SpinnerID);
		spinnerCompMap.put(SpinnerID, attr.getId());
		
		spinnerView.setLayoutParams(spinnerViewParams);
		List<String> elements = attr.getTypeConfig();
		elements.add(0, "");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mainContext,android.R.layout.simple_spinner_item, elements);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setAdapter(dataAdapter);
		
		spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(!isViewCompleted) return;
                
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
                	saveFeatureTask.execute(attrId,selectedItem,"update");
                }else{
                	//delete from XML file
                	saveFeatureTask = new SaveFeatureTask();
                	saveFeatureTask.execute(attrId,null,"update");
                	
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
	
	
	
	
	
	private void addFreeText(LinearLayout main, AttributeType attr,  String type){
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
		
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/12;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>12)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams editTextViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
		EditText editTextView = new EditText(mainContext);
		editTextView.setLayoutParams(editTextViewParams);
		
		editTextView.setId(FreeTextID);
		freeTextCompMap.put(FreeTextID, attr.getId());
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
			        		saveFeatureTask.execute(attrId,text,"update");	
		        		}else{
		        			//v.requestFocus();
		        			errorFieldMap.put(attrId,true);
		        		}
		        	}else{
		        		//delete from XML file
		        		saveFeatureTask = new SaveFeatureTask();
		        		saveFeatureTask.execute(attrId,null,"update");
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
		
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(editTextView);
		
		main.addView(lineraTitleTmp);
		FreeTextID++;
		
    }
	
	private void addDate(LinearLayout main, AttributeType attr){
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
		
		TextView titleVieww = new TextView(mainContext);
		titleVieww.setLayoutParams(titleViewParams);
		if(mandatoryFieldMap.containsKey(attr.getId()))
			titleVieww.setText("* " + attr.getName()+ " ");
		else
			titleVieww.setText(attr.getName()+ " ");
		
		int maxLine = 0;
		double ratio = attr.getName().length()/12;
		maxLine = ((int)ratio)+2;
		if(attr.getName().length()>12)
			titleVieww.setMinLines(maxLine);
		
		final int FIXED_FREETEXT_DIM = 350;
		float FIXED_FREETEXT_WIDTH_PERC = ((float) FIXED_FREETEXT_DIM /WIDTH) * 100;
		int FIXED_FREETEXT_WIDTH_VALUE = (int)((FIXED_FREETEXT_WIDTH_PERC * deviceWidth) / 100);
		LinearLayout.LayoutParams dateTextViewParams = new LinearLayout.LayoutParams(FIXED_FREETEXT_WIDTH_VALUE,LayoutParams.WRAP_CONTENT);
		
		EditText dateTextView = new EditText(mainContext);
		dateTextView.setLayoutParams(dateTextViewParams);
		dateTextView.setEnabled(false);
		
		dateTextView.setId(DateComponentID);
		mappaDateComponent.put(DateComponentID, dateTextView);
		mappaDateComponentId.put(DateComponentID, attr.getId());
		
		final int FIXED_BUT_DIM = 65;
		float FIXED_BUT_WIDTH_PERC = ((float) FIXED_BUT_DIM /WIDTH) * 100;
		float FIXED_BUT_HEIGHT_PERC = ((float) FIXED_BUT_DIM /HEIGHT) * 100;
		int FIXED_BUT_WIDTH_VALUE = (int)((FIXED_BUT_WIDTH_PERC * deviceWidth) / 100);
        int FIXED_BUT_HEIGHT_VALUE = (int)((FIXED_BUT_HEIGHT_PERC * deviceHeight) / 100);
        // This will resize button on screen resolutions
        LinearLayout.LayoutParams buttonViewParams = new LinearLayout.LayoutParams(FIXED_BUT_WIDTH_VALUE,FIXED_BUT_HEIGHT_VALUE);
		        
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
	            	saveFeatureTask.execute(attrId,null,"update");
	                
	                // check if it is in mandatory map and set its value to true
	            	if(mandatoryFieldMap.containsKey(attrId)){
	            		mandatoryFieldMap.put(attrId,false);
	            		errorFieldMap.put(attrId,true);
	            	}
					
				}
				return true;
			}
		});
        
		lineraTitleTmp.addView(titleVieww);
		lineraTitleTmp.addView(dateTextView);
		lineraTitleTmp.addView(dateButtonOpenerImg);
		lineraTitleTmp.addView(dateResetButtonImg);
		
		main.addView(lineraTitleTmp);
		DateComponentID++;
    }
	
	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 999:
            return new DatePickerDialog(this, dateSetListener, year, month, day);
        }
        return null;
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
        		saveFeatureTask.execute(attrId,dateTextToSend,"update");
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
	
	/**
	 * Shows the progress UI and hides the main scroll view.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showGetDescribeFeatureProgress(final boolean show) {
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
			getDescribeFeatureStatusBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
	
	public boolean toBeClosed = false;
	public void showAlert(String title, String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainContext);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            	if(toBeClosed)closeActiviy();
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
	            	@SuppressWarnings("unused")
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
			//showProgress(false);
			if (success) {
				// once user has been created on optima, register it on geoserver anagraphic table
				sendDataTask = new SendDataTask();
				sendDataTask.execute((Void) null);
			}else{
				if(returnedMessage!=null && returnedMessage.length()>0)
					showAlert(getString(R.string.warningLabel), returnedMessage);
				else
					showAlert(getString(R.string.warningLabel), getString(R.string.registrationErrorMessage));
			}
		}
		 
	 }
	 
	 
	 
	 private void registrationComplete_handle(){
		 	// store login/pass to login in previous view on optima and get sessionid
			
			String filename = localDir+"/"+properties.getProperty("userDataFile");
			fileEncr.createEncryptedFile(filename.substring(0, filename.length()-4));
			fileEncr.addUSer(userDataFilePath, username, pass);
			
			toBeClosed = true;
			showAlert(getString(R.string.registrationLabel), getString(R.string.registrationSuccessMessage));
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
	 

	private boolean hasAnagLayer = false;
	public class GetDescribeFeatureTask extends AsyncTask<Void, Void, Boolean> {
		//private String message = "";
		 
		@Override
		protected Boolean doInBackground(Void... params) {
			
			String geoserverUrl = "";
			if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
				geoserverUrl = config.getUrls().get("wfs");
			}else{
				//message = getString(R.string.noWfsFoundMessage);
				return false;
			}
			
			if(anagLayer==null){
				//message = getString(R.string. noAnagLayer); // anagraphic layer not defined in config file
				return false;
			}
				
			
			try{
				GetXmlFromGeoserver.getDescribeFeatureType(geoserverUrl, anagLayer, "admin", "geoserver", null, mainContext);
			}catch(AuthenticationException e){
//					int errorCode = e.getErrorCode();
//					if(errorCode==401)
//						message = getString(R.string.unauthorizedUser);
//	    			else
//	    				message = getString(R.string.genericAuthenticationError);
    			return false;
			}
			describeFeatureMap = featureParser.parseDescribeFeature(anagLayer);
			if(describeFeatureMap==null || describeFeatureMap.size()==0){
				//message = getString(R.string.noConnOrGeoserverFileMessage);
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			showGetDescribeFeatureProgress(false);
			getDescribeTask = null;
			
			if(success){
				hasAnagLayer = true;
				
				saveFeatureTask = new SaveFeatureTask();
				saveFeatureTask.execute("","","insert");
				
				createFieldLayout();
			}
		}
		 
	 }
	 
	 
	 
	 
	 
	 
	 private SaveFeatureTask saveFeatureTask;
	 public class SaveFeatureTask extends AsyncTask<String, Void, Boolean> {
	  		
	  		@Override
			protected Boolean doInBackground(String... args) {
				String columnName = args[0];
				String value = args[1];
				value = value.trim();
				String type = args[2];
				
				String filePath = anagDir + "/" + "anag.xml";
				String namespace = anagLayer.substring(0, anagLayer.indexOf(":"));
				String layer = anagLayer.substring(anagLayer.indexOf(":")+1,anagLayer.length());
				String geoserverAddress = config.getUrls().get("wfs");
	  			
				if("img".equals(type))
					SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, true, false,"", value);
				else if("update".equals(type))
					SurveyXmlBuilder.insertUpdate(filePath, namespace, layer, columnName, false, false,"", value);
				else
					SurveyXmlBuilder.createFile(filePath, namespace, layer, geoserverAddress, columnName, value);
				
				return true;
			}
			
			@Override
			protected void onPostExecute(final Boolean success) {
				saveFeatureTask = null;
				
				if (success && isRegistering) {
					validateAndRegister();
				} else {
					
				}
			}
	  	}
	 
	 private SendDataTask sendDataTask;
	 public class SendDataTask extends AsyncTask<Void, Integer, Boolean> {

		 @Override
			protected Boolean doInBackground(Void... args) {
			 
			 if(sortedAttributes==null || anagLayer==null)
				 return true;
			 
	  			try{
	  				String geoserverUrl = "";
	  				if(config!=null && config.getUrls()!=null && config.getUrls().containsKey("wfs")){
	  					geoserverUrl = config.getUrls().get("wfs")+"/wfs?service=wfs";
	  				}else{
	  					return false;
	  				}
	  				
		  			HttpURLConnection httpCon = null;
					URL url = new URL(geoserverUrl);
					httpCon = (HttpURLConnection) url.openConnection();
					
					String filePath = anagDir + "/" + "anag.xml";
					httpCon.setDoOutput(true);
					httpCon.setDoInput(true);
					httpCon.setRequestMethod("POST");
					
					String authString = "admin" + ":" + "geoserver";
	    			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
	    			String authStringEnc = new String(authEncBytes);
	    			httpCon.setRequestProperty("Authorization", "Basic " + authStringEnc);
					
					httpCon.setRequestProperty("Content-type", "application/xml");
					
					File file = new File(filePath);
		            FileInputStream fileInputStream = new FileInputStream(file);
		            byte[] bytes = new byte[(int) file.length()];
		            fileInputStream.read(bytes);
		            fileInputStream.close();
					OutputStream outputStream = httpCon.getOutputStream();
					int bufferLength = 1024;
		            for (int j = 0; j < bytes.length; j += bufferLength) {
		                if (bytes.length - j >= bufferLength) {
		                    outputStream.write(bytes, j, bufferLength);
		                } else {
		                    outputStream.write(bytes, j, bytes.length - j);
		                }
		            }
		            outputStream.close();
		            
					int responseCode = httpCon.getResponseCode(); // codice di risposta http
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
				    try{
				    	File fileTemp = new File(filePath, "anag_REPLY.xml");
				    	fileTemp.createNewFile();
						BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemp));
						writer.write(sb.toString());
						writer.close();	
				    }catch(Exception e){}
				    
					
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
			protected void onPostExecute(final Boolean success) {
				sendDataTask = null;
				showProgress(false);
				if (success) {
					registrationComplete_handle();
				} else {
					showAlert(getString(R.string.warningLabel), getString(R.string.registrationAnagErrorMessage));
				}
			}
			
			@Override
			protected void onCancelled() {
				sendDataTask = null;
			}
	  	}
    
    

}
