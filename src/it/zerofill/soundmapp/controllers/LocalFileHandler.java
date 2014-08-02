package it.zerofill.soundmapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.StringTokenizer;

public class LocalFileHandler {
	
	public static void addSurvey(String fileName, String surveyId, String surveyName, String user, String layerName){
		try{
			File file =new File(fileName);
			boolean isExist = true;
			if(!file.exists()){
				isExist = false;
				file.createNewFile();
			}
			
			//String data = user + ";"+surveyName+ ";"+surveyName+"\n";
			String data = user + ";" + layerName + ";"+surveyId+ ";"+surveyName;
			FileWriter fileWritter = new FileWriter(fileName,true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        if(isExist) bufferWritter.newLine();
	        bufferWritter.write(data);
	        bufferWritter.close();
	        // String newLineChar = System.getProperty("line.separator");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void removeSurvey(String fileName, String surveyId, String name, String user, String layerName){
		try{
			File inputFile = new File(fileName);
			File tempFile = new File(fileName+"2");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String lineToRemove = user.trim()+";"+ layerName +";"+surveyId.trim() +";"+name.trim();
			String line;

			while((line = reader.readLine()) != null) {
			    if(!line.trim().equalsIgnoreCase(lineToRemove)){
			    	writer.write(line);
			    	writer.newLine();
			    }
			}

			writer.close();
			reader.close();
			
			tempFile.renameTo(inputFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void updateName(String fileName, String surveyId, String name, String user, String layerName){
		try{
			File inputFile = new File(fileName);
			File tempFile = new File(fileName+"2");

			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String lineToUpdate = user+";"+layerName+";"+surveyId;
			String newValue = user+";"+layerName+";"+surveyId+";"+name;
			String line;

			while((line = reader.readLine()) != null) {
			    if(line.contains(lineToUpdate)){
			    	writer.write(newValue);
			    	writer.newLine();
			    }else{
			    	writer.write(line.trim());
			    	writer.newLine();
			    }
			}

			writer.close();
			reader.close();
			
			tempFile.renameTo(inputFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	public static HashMap<String, String> getFilePathByKey(String fileName, String layerName){
		HashMap<String, String> res = new HashMap<String, String>();
		try{
			File inputFile = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line,";");
       			String user = tokenizer.nextToken();
       			String layer = tokenizer.nextToken();
       			String surveyName = tokenizer.nextToken();
       			String name = tokenizer.nextToken();
       			String key = user + "_" + surveyName;
       			res.put(key, name);
			}
			reader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}
	
	public static HashMap<String, String> getFilePathByUser(String fileName, String user, String layerName){
		HashMap<String, String> res = new HashMap<String, String>();
		try{
			File inputFile = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while((line = reader.readLine()) != null) {
				if(!"".equals(line)){
					StringTokenizer tokenizer = new StringTokenizer(line,";");
	       			String cuser = tokenizer.nextToken();
	       			String layern = tokenizer.nextToken();
	       			String surveyId = tokenizer.nextToken();
	       			String name = tokenizer.nextToken();
	       			if(cuser.equals(user) && layern.equals(layerName)){
	       				String key = surveyId;
	           			res.put(key, name);       				
	       			}	
				}
			}
			reader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}

}
