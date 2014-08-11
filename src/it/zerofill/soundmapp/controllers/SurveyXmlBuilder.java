package it.zerofill.soundmapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class SurveyXmlBuilder {
	
	private static String HEADER = "<?xml version=\"1.0\"?> \n"+
			"<wfs:Transaction service=\"WFS\" version=\"1.0.0\" \n"+
			"\txmlns:wfs=\"http://www.opengis.net/wfs\" \n"+
			"\txmlns:NAMESPACE=\"NAMESPACE\" \n"+
			"\txmlns:gml=\"http://www.opengis.net/gml\" \n"+
			"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"+
			"\txsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd \n" +
			"http://www.opengeospatial.net/NAMESPACE SERVERADDRESS/wfs/DescribeFeatureType?typename=NAMESPACE:LAYERNAME\"> \n"+
			"\t\t<wfs:Insert> \n"+
			"\t\t\t<NAMESPACE:LAYERNAME> \n";
	
	private static String FOOTER = "\t\t\t</NAMESPACE:LAYERNAME> \n"+
			"\t\t</wfs:Insert> \n"+
			"</wfs:Transaction> \n";

	public static void createFile(String filePath, String namespace, String layerName, String serverAddress, String columnName, String columnForNameAttr, String value, String creatorField, String user){
		String header = ((HEADER.replace("NAMESPACE", namespace)).replace("SERVERADDRESS", serverAddress).replace("LAYERNAME", layerName));
		String footer = (FOOTER.replace("LAYERNAME", layerName)).replace("NAMESPACE", namespace);
		// DO NOT crate key, key will be created by GeoServer
		String body = ""; // "\t\t\t<"+namespace+":"+columnName+">"+value+"</"+namespace+":"+columnName+">";
		body += "\n\t\t\t<"+namespace+":"+columnForNameAttr+">"+value+"</"+namespace+":"+columnForNameAttr+">";
		body += "\n\t\t\t<"+namespace+":"+creatorField+">"+user+"</"+namespace+":"+creatorField+">";
		
		String fileString = header + body + footer;
		try{
			File file = new File(filePath);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	        writer.write(fileString.toString());
	        writer.close();	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void createFile(String filePath, String namespace, String layerName, String serverAddress, String columnName, String value){
		String header = ((HEADER.replace("NAMESPACE", namespace)).replace("SERVERADDRESS", serverAddress).replace("LAYERNAME", layerName));
		String footer = (FOOTER.replace("LAYERNAME", layerName)).replace("NAMESPACE", namespace);
		String body = ""; 
		String fileString = header + body + footer;
		try{
			File file = new File(filePath);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	        writer.write(fileString.toString());
	        writer.close();	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void insertUpdate(String filePath, String namespace, String layerName, String columnName, boolean isPath, boolean isPoints, String typePoint, String value){
		try{
			String element = "<"+namespace+":"+columnName+">";
			String key = namespace+":"+columnName;
			String footer = namespace+":"+layerName;
			
			File fileTemp = new File(filePath+"2");
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileTemp));
			
			FileInputStream f = new FileInputStream(filePath);
			DataInputStream i = new DataInputStream(f);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(i));
			String line = "";
			boolean found = false;
			
			while ((line = buffer.readLine()) != null){
				if(line.contains(element)){ // if there is the key, replace its value
					while(!line.contains("</"+key)){ // this to handle VALUE on multiple lines, ex. images or audio coded
						line = buffer.readLine();
					}
					if(value!=null){
						if(isPath)
							writer.write("\t\t\t<"+key+">"+EncodeSaveFile.getEncodedString(value)+"</"+key+"> \n");
						else if(isPoints){
							if("point".equals(typePoint)){
								writer.write("\t\t\t<"+key+">\n"); // open tag
								writer.write("\t\t\t\t<gml:Point srsName=\"EPSG:4326\"> \n"); // open inner tag
								writer.write("\t\t\t\t\t<gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"+value+"</gml:coordinates>\n"); // value
								writer.write("\t\t\t\t</gml:Point> \n"); // close inner tag
								writer.write("\t\t\t</"+key+"> \n"); // close tag
							}
							if("line".equals(typePoint)){
								writer.write("\t\t\t<"+key+">\n"); // open tag
								writer.write("\t\t\t\t<gml:LineString srsName=\"EPSG:4326\"> \n"); // open inner tag
								//writer.write("\t\t\t\t\t<gml:posList dimension=\"2\">"+value+"</gml:posList>\n"); // 45.67 88.56 55.56 89.44
								writer.write("\t\t\t\t\t<gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"+value+"</gml:coordinates>\n"); // value
								writer.write("\t\t\t\t</gml:LineString> \n"); // close inner tag
								writer.write("\t\t\t</"+key+"> \n"); // close tag
							}
							
							
						}else
							writer.write("\t\t\t<"+key+">"+value+"</"+key+"> \n");
					}
					found = true;
				}else if(line.contains("/"+footer) && !found){ // if there is no key and i reached the footer, insert the key before footer
					if(isPath)
						writer.write("\t\t\t<"+key+">"+EncodeSaveFile.getEncodedString(value)+"</"+key+"> \n"+line);
					else if(isPoints){
						if("point".equals(typePoint)){
							writer.write("\t\t\t<"+key+">\n"); // open tag
							writer.write("\t\t\t\t<gml:Point srsName=\"EPSG:4326\"> \n"); // open inner tag
							writer.write("\t\t\t\t\t<gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"+value+"</gml:coordinates>\n"); // value
							writer.write("\t\t\t\t</gml:Point> \n"); // close inner tag
							writer.write("\t\t\t</"+key+"> \n"+line); // close tag
						}
						if("line".equals(typePoint)){
							writer.write("\t\t\t<"+key+">\n"); // open tag
							writer.write("\t\t\t\t<gml:LineString srsName=\"EPSG:4326\"> \n"); // open inner tag
							//writer.write("\t\t\t\t\t<gml:posList dimension=\"2\">"+value+"</gml:posList>\n"); // 45.67 88.56 55.56 89.44
							writer.write("\t\t\t\t\t<gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"+value+"</gml:coordinates>\n"); // value
							writer.write("\t\t\t\t</gml:LineString> \n"); // close inner tag
							writer.write("\t\t\t</"+key+"> \n"+line); // close tag
						}
					}
					else if(value!=null)
						writer.write("\t\t\t<"+key+">"+value+"</"+key+"> \n"+line);
					else
						writer.write("\n"+line);
				}else
					writer.write(line + "\n");
			}
			buffer.close();
			writer.close();
			
			File fileToDel =new File(filePath);
			fileToDel.delete();
			File oldfile =new File(filePath+"2");
			File newfile =new File(filePath);
			boolean res = oldfile.renameTo(newfile);
	        if(!res)
	        	System.out.println("cant rename");
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	

	
}
