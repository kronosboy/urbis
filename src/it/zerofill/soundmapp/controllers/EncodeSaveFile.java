package it.zerofill.soundmapp.controllers;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import android.util.Base64;

public class EncodeSaveFile {
	
	
	public static void save(String text, String outPath){
		try{
			FileOutputStream fos = new FileOutputStream(outPath);
			
			byte[] data = Base64.decode(text, Base64.DEFAULT);

			fos.write(data);
			fos.close();	
		}catch(Exception e){}
	}
	
	public static String getEncodedString(String inPath){
		String res = "";
		try{
			RandomAccessFile file = new RandomAccessFile(inPath, "r");
			byte[] data = new byte[(int)file.length()];
			file.read(data);
			res = Base64.encodeToString(data, Base64.DEFAULT);
			file.close();
		}catch(Exception e){}
		return res;
	}
	
	

}
