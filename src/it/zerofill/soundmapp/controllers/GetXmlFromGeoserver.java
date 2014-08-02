package it.zerofill.soundmapp.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.util.Base64;

public class GetXmlFromGeoserver {
	
	//private static String BASE_URL = "http://kronosboy.no-ip.org:8080/geoserver/wfs?service=wfs&version=2.0.0&request=";
	private static String BASE_URL = "/wfs?service=wfs&version=2.0.0&request=";
	
	private static final String GET_CAPABILITIES = "GetCapabilities";
	private static final String GET_DESCRIBEFEATURETYPE = "DescribeFeatureType&typeName=";
	private static final String GET_FEATURE = "GetFeature&typeName=";
	private static final String PROPERTY_NAME = "&propertyName=";
	private static final String CQL_FILTER = "&CQL_FILTER=";
	private static final String FEATUREID = "&featureID=";
	//private static String MAX_FEATURE = "&count=50";
	
	//private static String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/SoundmApp/geoserver";
	private static String GET_CAPABILITIES_FILE_NAME = "GetCapabilities.xml"; 
	
	
	public static void getCapabilites(String geoserverUrl, String user, String pass, String sessionId, Context context) throws AuthenticationException{
		try{
			
			//String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/SoundmApp/geoserver";
			String BASE_PATH = context.getExternalFilesDir(null).getAbsolutePath().toString() + "/SoundmApp/geoserver";
			
    		BufferedReader reader=null;
    		String urlPath = geoserverUrl+BASE_URL+GET_CAPABILITIES;
    		
    		urlPath += "&configuration=svrtoc";
    		if(sessionId!=null && sessionId.length()>0)
    			urlPath += "&session="+sessionId;
    		
    		URL url = new URL(urlPath);
    		HttpURLConnection con = (HttpURLConnection) url.openConnection();
    		con.setDoInput(true);
    		con.setDoOutput(true);
    		
    		String authString = user + ":" + pass;
			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
			String authStringEnc = new String(authEncBytes);
    		con.setRequestProperty("Authorization", "Basic " + authStringEnc);
    		
    		con.setRequestMethod("GET");
    		OutputStream outputStream = con.getOutputStream();
    		outputStream.close();
    		int responseCode = con.getResponseCode();
            if(responseCode!=200){
            	throw new AuthenticationException(responseCode);
            }
    		
    		reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
            	sb.append(line + "\n");
            }
            
	    	File newdir = new File(BASE_PATH); 
	    	if(newdir.exists()==false) newdir.mkdirs();
            File file = new File(BASE_PATH,GET_CAPABILITIES_FILE_NAME);
            PrintStream out = new PrintStream(file);
	        out.print(sb);
	        out.flush();
	        out.close();
    	}catch(MalformedURLException mue){
    		mue.printStackTrace();
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
		}
	}
	
	
	public static void getDescribeFeatureType(String geoserverUrl, String layerName, String user, String pass, String sessionId, Context context) throws AuthenticationException{
		try{ 
			//String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/SoundmApp/geoserver";
			String BASE_PATH = context.getExternalFilesDir(null).getAbsolutePath().toString() + "/SoundmApp/geoserver";
			
			BufferedReader reader=null;
			
			String urlPath = geoserverUrl+BASE_URL+GET_DESCRIBEFEATURETYPE+layerName;
			
			urlPath += "&configuration=svrtoc";
    		if(sessionId!=null && sessionId.length()>0)
    			urlPath += "&session="+sessionId;
			
			URL url = new URL(urlPath);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
    		con.setDoOutput(true);
			
			String authString = user + ":" + pass;
			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
			String authStringEnc = new String(authEncBytes);
    		con.setRequestProperty("Authorization", "Basic " + authStringEnc);
    		
    		con.setRequestMethod("GET");
    		OutputStream outputStream = con.getOutputStream();
    		outputStream.close();
    		int responseCode = con.getResponseCode();
            if(responseCode!=200){
            	throw new AuthenticationException(responseCode);
            }
            
			reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
	        StringBuilder sb = new StringBuilder();
	        String line = null;
	        while((line = reader.readLine()) != null){
	        	sb.append(line + "\n");
	        }
	        
	        File file = new File(BASE_PATH,"DescribeFeatureType_"+layerName.replace(":", "_")+".xml");
	        PrintStream out = new PrintStream(file);
	        out.print(sb);
	        out.flush();
	        out.close();
		}catch(MalformedURLException mue){
    		mue.printStackTrace();
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
		}
	}
	
	public static void getFeature(String geoserverUrl, List<String> layerList, String propertyName, boolean isKey, String creator, String user, String loggedUser, String pass, String sessionId, Context context) throws AuthenticationException{
		try{
			//String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/SoundmApp/geoserver";
			String BASE_PATH = context.getExternalFilesDir(null).getAbsolutePath().toString() + "/SoundmApp/geoserver";
			
    		for(String layer : layerList){
    			BufferedReader reader=null;
    			String u = geoserverUrl+BASE_URL+GET_FEATURE+layer; // MAX_FEATURE
    			if(propertyName!=null && !"".equals(propertyName)){
    				if(isKey){
    					u += FEATUREID + propertyName;
    				}else{
    					u += PROPERTY_NAME + propertyName;
    				}
    			}
    			if(!"".equals(creator)  && user!=null && !"".equals(user)){
    				u += CQL_FILTER + creator + "=" +"'"+user+"'";
    			}
    			
    			u += "&configuration=svrtoc";
        		if(sessionId!=null && sessionId.length()>0)
        			u += "&session="+sessionId;
    			
    			URL url = new URL(u); 
    			HttpURLConnection con = (HttpURLConnection) url.openConnection();
    			con.setDoInput(true);
        		con.setDoOutput(true);
    			
    			String authString = loggedUser + ":" + pass;
    			byte[] authEncBytes = Base64.encode(authString.getBytes(),Base64.DEFAULT);
    			String authStringEnc = new String(authEncBytes);
        		con.setRequestProperty("Authorization", "Basic " + authStringEnc);
    			
        		con.setRequestMethod("GET");
        		OutputStream outputStream = con.getOutputStream();
        		outputStream.close();
        		int responseCode = con.getResponseCode();
                if(responseCode!=200){
                	throw new AuthenticationException(responseCode);
                }
                
    			reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
    	        StringBuilder sb = new StringBuilder();
    	        String line = null;
    	        while((line = reader.readLine()) != null){
    	        	sb.append(line + "\n");
    	        }
    	        
    	        File file = new File(BASE_PATH,"Feature_"+layer.replace(":", "_")+".xml");
    	        PrintStream out = new PrintStream(file);
    	        out.print(sb);
    	        out.flush();
    	        out.close();
    		}
		}catch(MalformedURLException mue){
    		mue.printStackTrace();
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
		}
	}
	
	

}
