package it.zerofill.soundmapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryptor{
    
	private final String EncryptionAlgorithm = "DES/ECB/PKCS5Padding";
	
    public FileEncryptor() {
    }
    
    public void createEncryptedFile(String path){
    	try{
    		File file = new File(path);
    		if(!file.exists())
    			file.createNewFile();
    		this.encrypt(path);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void addUSer(String path, String user, String pass){
    	try{
    		File file = new File(path);
    		
    		this.decrypt(path);
    		
    		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()+".dec"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()+"Tmp.dec"));

			String line;
			boolean isFound = false;
			while((line = reader.readLine()) != null) {
				if("".equals(line.trim())){
					StringTokenizer tokenizer = new StringTokenizer(line, ";");
					String u = tokenizer.nextToken();
				    if(u.equals(user)){
				    	writer.write(user+";"+pass);
				    	writer.newLine();
				    	isFound = true;
				    }else{
				    	writer.write(line);
				    	writer.newLine();
				    }	
				}
			}
			if(!isFound){
				writer.write(user+";"+pass);
		    	writer.newLine();
			}

			writer.close();
			reader.close();
			
			File decFile = new File(file.getAbsolutePath()+".dec");
			File tempFile = new File(file.getAbsolutePath()+"Tmp.dec");
			File f = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-4));
			if(!f.exists())
				f.createNewFile();
			tempFile.renameTo(f);
			this.encrypt(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-4));
			decFile.delete();
			tempFile.delete();
			
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public String getPass(String path, String user){
    	String pass="";
    	File file = new File(path);
    	
    	try{
    		this.decrypt(path);
    		
    		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()+".dec"));

			String line;
			while((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				String u = tokenizer.nextToken();
			    if(u.equals(user)){
			    	pass = tokenizer.nextToken();
			    	break;
			    }
			}
			reader.close();
			
			File decFile = new File(file.getAbsolutePath()+".dec");
			decFile.delete();
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return pass;
    }
    
    public String getUser(String path){
    	String user="";
    	File file = new File(path);
    	
    	try{
    		this.decrypt(path);
    		
    		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()+".dec"));

			String line;
			while((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line, ";");
				user = tokenizer.nextToken();
			    break;
			}
			reader.close();
			
			File decFile = new File(file.getAbsolutePath()+".dec");
			decFile.delete();
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return user;
    }
    
    
    public void emptyFile(String path){
    	try{
    		File file = new File(path);
        	if(file.exists()){
        		file.delete();
        		file = new File(path.substring(0, path.length()-4));
        		file.createNewFile();
        		encrypt(path.substring(0, path.length()-4));
        	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    
    public void encrypt(String path) throws Exception{
    	 File file = new File(path);
         FileInputStream fis =new FileInputStream(file);
         File nfile=new File(file.getAbsolutePath()+".enc");
         FileOutputStream fos =new FileOutputStream(nfile);
         //generating key
         byte k[] = "HignDlPs".getBytes();   
         SecretKeySpec key = new SecretKeySpec(k,EncryptionAlgorithm.split("/")[0]);  
         //creating and initializing cipher and cipher streams
         Cipher encrypt =  Cipher.getInstance(EncryptionAlgorithm);  
         encrypt.init(Cipher.ENCRYPT_MODE, key);  
         CipherOutputStream cout=new CipherOutputStream(fos, encrypt);
         
         byte[] buf = new byte[1024];
         int read;
         while((read=fis.read(buf))!=-1)
             cout.write(buf,0,read);  //writing encrypted data

         fis.close();
         cout.flush();
         cout.close();
         file.delete();
     }
     
     public void decrypt(String path) throws Exception{
    	 File file = new File(path);
   	     FileInputStream fis =new FileInputStream(file);
         File nfile=new File(file.getAbsolutePath()+".dec");
         FileOutputStream fos =new FileOutputStream(nfile);               
         //generating same key
         byte k[] = "HignDlPs".getBytes();   
         SecretKeySpec key = new SecretKeySpec(k,EncryptionAlgorithm.split("/")[0]);  
         //creating and initializing cipher and cipher streams
         Cipher decrypt =  Cipher.getInstance(EncryptionAlgorithm);  
         decrypt.init(Cipher.DECRYPT_MODE, key);  
         CipherInputStream cin=new CipherInputStream(fis, decrypt);
              
         byte[] buf = new byte[1024];
         int read=0;
         while((read=cin.read(buf))!=-1)  //reading encrypted data
             fos.write(buf,0,read);  //writing decrypted data

         cin.close();
         fos.flush();
         fos.close();
      }

}
