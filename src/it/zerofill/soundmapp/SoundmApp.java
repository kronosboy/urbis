package it.zerofill.soundmapp;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import android.app.Application;


//working
// http://192.168.1.42:5984/acra-soundmapp/_design/acra-storage/_update/report
// soundmapp

//http://androidreports.iriscouch.com/acra-urbis/_design/acra-storage/_update/report

@ReportsCrashes(
		formKey = "", 
		formUri = "http://androidreports.iriscouch.com/acra-urbis/_design/acra-storage/_update/report",
		httpMethod = Method.PUT,
	    reportType = Type.JSON,
	    formUriBasicAuthLogin = "urbis",
	    formUriBasicAuthPassword = "urbis"
		)
public class SoundmApp extends Application {

	 @Override
     public void onCreate() {
         super.onCreate();

         // The following line triggers the initialization of ACRA
         ACRA.init(this);
     }
}
