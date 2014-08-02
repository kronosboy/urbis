package it.zerofill.soundmapp;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
//import android.os.Environment;
import android.widget.ImageView;

public class ShowFullImageActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);
		
		String path = "";
		Bundle extras = getIntent().getExtras();
    	if (extras != null){
    		path = extras.getString("imgPath"); 
    	}
		
		ImageView img = (ImageView)findViewById(R.id.imageFullView);
		File imgFile = new  File(path);
		if(imgFile.exists()){
			Bitmap myBitmap = decodeFile(imgFile);//BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			img.setImageBitmap(myBitmap);
		}else{
			finish();
		}
	}
	
	@Override
	public void onBackPressed(){
    	finish();
    }
	
	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=100;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (Exception e) {}
	    return null;
	}
}
