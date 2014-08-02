package it.zerofill.soundmapp.controllers;

import it.zerofill.soundmapp.models.FeatureElementAttribute;
import it.zerofill.soundmapp.models.ObjFeature;
import it.zerofill.soundmapp.models.ObjRecord;
import it.zerofill.soundmapp.models.ObjValue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

public class FeatureParser {
	
	private AssetsPropertyReader assetsPropertyReader;
	private Properties properties;
	
	private String LOCAL_PATH;
	private String BASE_PATH;
	private String GET_CAPABILITIES_FILE_NAME = "GetCapabilities.xml";
	private List<FeatureElementAttribute> FeatureElementAttributeList;
	
	private HashMap<String, List<ObjFeature>> recordListByName;
	
	public HashMap<String, List<ObjFeature>> getRecordListByName(){
		return recordListByName;
	}
	
	public FeatureParser(Context context){
		assetsPropertyReader = new AssetsPropertyReader(context);
    	properties = assetsPropertyReader.getProperties("settings.properties");
    	
    	String homeDir =  properties.getProperty("homeDirectory");
    	String geoserverDir = properties.getProperty("geoserverDirectory");
    	String localDir = properties.getProperty("localDirectory");
    	BASE_PATH = context.getExternalFilesDir(null).getAbsolutePath().toString() + "/"+ homeDir+ "/"+geoserverDir;
    	LOCAL_PATH = context.getExternalFilesDir(null).getAbsolutePath().toString() + "/"+ homeDir+ "/"+localDir;
//    	BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/"+ homeDir+ "/"+geoserverDir;
//    	LOCAL_PATH = Environment.getExternalStorageDirectory().toString() + "/"+ homeDir+ "/"+localDir;
    	
		recordListByName = new HashMap<String, List<ObjFeature>>();
	}
	
	
	 /*
     * This method parses layers
     */
    public List<String> parseGetCapabilitesFile(List<String> selectedNameSpaces){
  	  List<String> result = new ArrayList<String>();
  		try{
  		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  		    DocumentBuilder builder = factory.newDocumentBuilder();
  		    Document document = builder.parse(new File(BASE_PATH+"/"+GET_CAPABILITIES_FILE_NAME));
  		    NodeList nodeList = document.getDocumentElement().getChildNodes();

  		    for (int i = 0; i < nodeList.getLength()-1; i++) {
  		      Node node = nodeList.item(i);
  		      if("FeatureTypeList".equals(node.getNodeName())){
  		    	  NodeList childNodes = node.getChildNodes();
  		    	  for (int j = 0; j < childNodes.getLength(); j++) {
  		    		  Node nodeChild = childNodes.item(j);
  		    		  if("FeatureType".equals(nodeChild.getNodeName())){
  				    	  NodeList childFeatureTypeNodes = nodeChild.getChildNodes();
  				    	  for (int y = 0; y < childFeatureTypeNodes.getLength(); y++) {
  				    		  Node nodeChildFeatureType = childFeatureTypeNodes.item(y);
  				    		  if("Name".equals(nodeChildFeatureType.getNodeName())){
  				    			  String content = nodeChildFeatureType.getTextContent().trim();
  				    			  String namespace = content.substring(0, content.indexOf(":"));
  				    			  if(selectedNameSpaces.contains(namespace))
  				    				  result.add(nodeChildFeatureType.getTextContent().trim());
  				    		  }
  				    	  }
  				      }
  		    	  }
  		      }
  		    }
  		}catch(Exception e){
  			e.printStackTrace();
  		}
  		return result;
  	  }
    
    
    
    
    
    /*
     * This method parses the xml files with FEATURE descriptions
     */

    public HashMap<String, FeatureElementAttribute> parseDescribeFeature(String layerName){
    	HashMap<String, FeatureElementAttribute> mappa = new HashMap<String, FeatureElementAttribute>();
    	try{
			String namespace = layerName.substring(0, layerName.indexOf(":"));
			String name = layerName.substring(layerName.indexOf(":")+1, layerName.length());
			String path = BASE_PATH + "/" +"DescribeFeatureType_"+namespace+"_"+name+".xml"; 
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document document = builder.parse(new File(path));
		    NodeList nodeList = document.getDocumentElement().getChildNodes();

		    FeatureElementAttributeList = new ArrayList<FeatureElementAttribute>();
		    getElement(nodeList,name);
		    
		    for(FeatureElementAttribute item : FeatureElementAttributeList){
		    	mappa.put(item.getName(),item);
		    }
		    
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return mappa;
    }
    
    
    /*
     * This method retrieves definition of FEATURE (column name and its type)
     */
    private void getElement(NodeList nodelist, String feature){
    	for(int j = 0; j < nodelist.getLength(); j++){
    		  Node child = nodelist.item(j);
    		  if("xsd:element".equals(child.getNodeName())){
    			  NamedNodeMap map = child.getAttributes();
    			  String nome = "";
    			  String tipo  = "";
    			  boolean isNullable = false;
    			  for(int x=0; x<map.getLength(); x++){
    				  Node nodoElemento = map.item(x);
    				  String nomeElemento = nodoElemento.getNodeName();
    				  String valoreElemento = nodoElemento.getTextContent().trim();
    				  
    				  if(nomeElemento.equals("name")){
    					  nome = valoreElemento;
    				  }
    				  if(nomeElemento.equals("type")){
    					  tipo = valoreElemento.substring(valoreElemento.indexOf(":")+1, valoreElemento.length());
    				  }
    				  if(nomeElemento.equals("nillable")){
    					  String nullable = valoreElemento;
    					  if("true".equals(nullable))
    						  isNullable = true;
    				  }
    				  
    				  
    				 // String element = ""+map.item(x);
//    				  if(element.substring(0, element.indexOf("=")).equals("name")){
//    					  nome = element.substring(element.indexOf("=")+1, element.length()).replace("\"", "");
//    				  }
//    				  if(element.substring(0, element.indexOf("=")).equals("type")){
//    					  tipo = element.substring(element.indexOf("=")+1, element.length());
//    					  tipo = tipo.substring(tipo.indexOf(":")+1, tipo.length()).replace("\"", "");
//    				  }
    			  }
    			  if(!nome.equals("") && !nome.equals(feature)){ // discards attribute with name of feature
    				  FeatureElementAttribute attr = new FeatureElementAttribute(nome,tipo);
    				  attr.setNullable(isNullable);
    				  FeatureElementAttributeList.add(attr);
    			  }
    		  }else{
    			  NodeList childNodes = nodelist.item(j).getChildNodes();
    			  getElement(childNodes,feature);
    		  }
    	  }
    }
    
    
    
    
    
    /*
     * This method parses the xml files with FEATUREs, there will be one file for every layer
     */
    public List<ObjFeature> parseGetFeatureFile(List<String> layerList, boolean isLocal, String surveyId){ //, String colonnaName
    	recordListByName = new HashMap<String, List<ObjFeature>>();
		List<ObjFeature> result = new ArrayList<ObjFeature>();
		try{
			for(String layer : layerList){
				ObjFeature elemento = new ObjFeature();
				elemento.setFeatureName(layer);
				String namespace = layer.substring(0, layer.indexOf(":"));
				String name = layer.substring(layer.indexOf(":")+1, layer.length());
				String path = "";
				if(isLocal)
					//path = LOCAL_PATH + "/" +surveyId+".xml";
					path = LOCAL_PATH + "/" + surveyId + "/" +surveyId+".xml";
				else
					path = BASE_PATH+"/"+"Feature_"+namespace+"_"+name+".xml";
			    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document document = builder.parse(new File(path));
			    NodeList nodeList = document.getDocumentElement().getChildNodes(); // ROOT

			    List<ObjRecord> val =  getData(nodeList, layer, isLocal); //, colonnaName
			    elemento.setRecords(val);
			    result.add(elemento);
			    
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
    
    @Deprecated
    public List<ObjFeature> parseLocalFeatureFile(String layerName){
    	recordListByName = new HashMap<String, List<ObjFeature>>();
		List<ObjFeature> result = new ArrayList<ObjFeature>();
		try{
			ObjFeature elemento = new ObjFeature();
			elemento.setFeatureName(layerName);
			
			String namespace = layerName.substring(0, layerName.indexOf(":"));
			String name = layerName.substring(layerName.indexOf(":")+1, layerName.length());
			String path = BASE_PATH+"/"+ LOCAL_PATH + "/" +"Feature_local_"+namespace+"_"+name+".xml"; 
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document document = builder.parse(new File(path));
		    NodeList nodeList = document.getDocumentElement().getChildNodes(); // ROOT

		    List<ObjRecord> val =  getLocalData(nodeList, layerName);
		    elemento.setRecords(val);
		    result.add(elemento);
			    
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
    
    
    /*
     * This method retrieves data of FEATUREs (COLUMN, VALUE)
     */
    private List<ObjRecord> getData(NodeList nodelist, String feature, boolean isLocal){ // String colonnaName
		List<ObjRecord> listaValori = new ArrayList<ObjRecord>();
		for(int j = 0; j < nodelist.getLength(); j++){
			  Node child = nodelist.item(j);
			  
			  String START_TAG = "";
			  if(isLocal)
				  START_TAG = "wfs:Insert";
			  else
				  START_TAG = "wfs:member";
			  
			  if(START_TAG.equals(child.getNodeName())){
				  
			//	  String nome_segnalazione = "";
				  String ID = "";
				  
				  NodeList nodeChildList = child.getChildNodes();
				  for (int i = 0; i < nodeChildList.getLength(); i++) {
					  String nomeColonna = "";
					  String valoreColonna = "";
				      Node node = nodeChildList.item(i);
				      
				      NamedNodeMap attributes = node.getAttributes();
				      if(!isLocal){
				    	  Node nodeid = attributes.getNamedItem("gml:id");
				    	  ID = nodeid.getNodeValue();
				      }else{
				    	  ID = ""; //prendere il nome del campo ID da configurationFile
				      }
			    	  
			    	  ObjRecord tmpObj = new ObjRecord(ID);
			    	  List<ObjValue> valori = new ArrayList<ObjValue>();
				      
				      NodeList elementList = node.getChildNodes();
				      for (int x = 0; x < elementList.getLength(); x++) {
				    	  Node element = elementList.item(x);
				    	  
				    	  nomeColonna = element.getNodeName();
				    	  valoreColonna = element.getTextContent().trim();
				    	  
				    	  nomeColonna = nomeColonna.substring(nomeColonna.indexOf(":")+1, nomeColonna.length());
				    	  
				    	  if(nomeColonna.equals("NOME_COLONNA_FROM_CONFIG")){
				    		  ID = valoreColonna;
				    		  tmpObj.setId(ID);
				    	  }
				    	  /*
				    	  if(nomeColonna.equals(colonnaName)){
				    		  nome_segnalazione = valoreColonna;
				    	  }*/
				    	  
				    	  ObjValue tmp = new ObjValue(nomeColonna,valoreColonna);
				    	  valori.add(tmp);
				      }
				      
				      tmpObj.setValues(valori);
				      listaValori.add(tmpObj);
				      /*
				      if(recordListByName.containsKey(ID)){//if(recordListByName.containsKey(nome_segnalazione)){
				    	  
				      }else{
				    	  List<ObjFeature> l = new ArrayList<ObjFeature>();
				    	  ObjFeature f = new ObjFeature(feature);
				    	  ObjRecord r = new ObjRecord(ID);
				    	  ObjValue v = new ObjValue(nomeColonna,valoreColonna);
				    	  List<ObjValue> vv = new ArrayList<ObjValue>(); vv.add(v);
				    	  r.setValues(vv);
				    	  List<ObjRecord> rr = new ArrayList<ObjRecord>(); //rr.add(r);
				    	  
				    	  List<ObjRecord> tempValList = new ArrayList<ObjRecord>();
				    	  List<ObjValue> tempList = new ArrayList<ObjValue>();
				    	  for(ObjRecord ox : listaValori){
				    		  for(ObjValue ov : ox.getValues()){
				    			 // if(ov.getColumn_name().equals(colonnaName) && ov.getValue().equals(nome_segnalazione)){
				    				  //tempList.addAll(c)
				    				  tempList.addAll(ox.getValues());
				    				  ObjRecord obk = new ObjRecord(ID);
				    				  obk.setValues(tempList);
				    				  tempValList.add(obk);
				    			 // }
				    		  }
				    	  }
				    	  
				    	  rr.addAll(tempValList);
				    	  
				    	  f.setRecords(rr);
				    	  l.add(f);
				    	  recordListByName.put(ID, l);//recordListByName.put(nome_segnalazione, l);
				      }*/
				  }
			  }// else if not 'wfs:member' do nothing
		  }
		return listaValori;
	}
    
    
    
    
    
    
    public List<ObjFeature> parseGetFeatureFile(List<String> layerList, boolean isLocal, String surveyId, List<String> colonnaName){ //, String colonnaName
    	recordListByName = new HashMap<String, List<ObjFeature>>();
		List<ObjFeature> result = new ArrayList<ObjFeature>();
		try{
			for(String layer : layerList){
				ObjFeature elemento = new ObjFeature();
				elemento.setFeatureName(layer);
				String namespace = layer.substring(0, layer.indexOf(":"));
				String name = layer.substring(layer.indexOf(":")+1, layer.length());
				String path = "";
				if(isLocal)
					//path = LOCAL_PATH + "/" +surveyId+".xml";
					path = LOCAL_PATH + "/" + surveyId + "/" +surveyId+".xml";
				else
					path = BASE_PATH+"/"+"Feature_"+namespace+"_"+name+".xml";
			    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document document = builder.parse(new File(path));
			    NodeList nodeList = document.getDocumentElement().getChildNodes(); // ROOT

			    List<ObjRecord> val =  getData(nodeList, layer, isLocal,colonnaName); //, colonnaName
			    elemento.setRecords(val);
			    result.add(elemento);
			    
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
    /*
     * This method retrieves data of FEATUREs (COLUMN, VALUE)
     */
    private List<ObjRecord> getData(NodeList nodelist, String feature, boolean isLocal, List<String> colonnaName){ // String colonnaName
		List<ObjRecord> listaValori = new ArrayList<ObjRecord>();
		HashMap<String, String> mappa = new HashMap<String, String>();
		for(String s : colonnaName){
			mappa.put(s, s);
		}
		
		for(int j = 0; j < nodelist.getLength(); j++){
			  Node child = nodelist.item(j);
			  
			  String START_TAG = "";
			  if(isLocal)
				  START_TAG = "wfs:Insert";
			  else
				  START_TAG = "wfs:member";
			  
			  if(START_TAG.equals(child.getNodeName())){
				  
			//	  String nome_segnalazione = "";
				  String ID = "";
				  
				  NodeList nodeChildList = child.getChildNodes();
				  for (int i = 0; i < nodeChildList.getLength(); i++) {
					  String nomeColonna = "";
					  String valoreColonna = "";
				      Node node = nodeChildList.item(i);
				      
				      NamedNodeMap attributes = node.getAttributes();
				      if(!isLocal){
				    	  Node nodeid = attributes.getNamedItem("gml:id");
				    	  ID = nodeid.getNodeValue();
				      }else{
				    	  ID = ""; //prendere il nome del campo ID da configurationFile
				      }
			    	  
			    	  ObjRecord tmpObj = new ObjRecord(ID);
			    	  List<ObjValue> valori = new ArrayList<ObjValue>();
				      
				      NodeList elementList = node.getChildNodes();
				      for (int x = 0; x < elementList.getLength(); x++) {
				    	  Node element = elementList.item(x);
				    	  
				    	  nomeColonna = element.getNodeName();
				    	  valoreColonna = element.getTextContent().trim();
				    	  
				    	  nomeColonna = nomeColonna.substring(nomeColonna.indexOf(":")+1, nomeColonna.length());
				    	  
				    	  if(nomeColonna.equals("NOME_COLONNA_FROM_CONFIG")){
				    		  ID = valoreColonna;
				    		  tmpObj.setId(ID);
				    	  }
				    	  
				    	  if(mappa.containsKey(nomeColonna)){
					    	  ObjValue tmp = new ObjValue(nomeColonna,valoreColonna);
					    	  valori.add(tmp);
				    	  }
				      }
				      
				      tmpObj.setValues(valori);
				      listaValori.add(tmpObj);
				      /*
				      if(recordListByName.containsKey(ID)){//if(recordListByName.containsKey(nome_segnalazione)){
				    	  
				      }else{
				    	  List<ObjFeature> l = new ArrayList<ObjFeature>();
				    	  ObjFeature f = new ObjFeature(feature);
				    	  ObjRecord r = new ObjRecord(ID);
				    	  ObjValue v = new ObjValue(nomeColonna,valoreColonna);
				    	  List<ObjValue> vv = new ArrayList<ObjValue>(); vv.add(v);
				    	  r.setValues(vv);
				    	  List<ObjRecord> rr = new ArrayList<ObjRecord>(); //rr.add(r);
				    	  
				    	  List<ObjRecord> tempValList = new ArrayList<ObjRecord>();
				    	  List<ObjValue> tempList = new ArrayList<ObjValue>();
				    	  for(ObjRecord ox : listaValori){
				    		  for(ObjValue ov : ox.getValues()){
				    			 // if(ov.getColumn_name().equals(colonnaName) && ov.getValue().equals(nome_segnalazione)){
				    				  //tempList.addAll(c)
				    				  tempList.addAll(ox.getValues());
				    				  ObjRecord obk = new ObjRecord(ID);
				    				  obk.setValues(tempList);
				    				  tempValList.add(obk);
				    			 // }
				    		  }
				    	  }
				    	  
				    	  rr.addAll(tempValList);
				    	  
				    	  f.setRecords(rr);
				    	  l.add(f);
				    	  recordListByName.put(ID, l);//recordListByName.put(nome_segnalazione, l);
				      }*/
				  }
			  }// else if not 'wfs:member' do nothing
		  }
		return listaValori;
	}
    
    
    
    
    /*
     * This method retrieves data of FEATUREs (COLUMN, VALUE)
     */
    @Deprecated
    private List<ObjRecord> getLocalData(NodeList nodelist, String feature){
		List<ObjRecord> listaValori = new ArrayList<ObjRecord>();
		for(int j = 0; j < nodelist.getLength(); j++){
			  Node child = nodelist.item(j);
			  if("wfs:Insert".equals(child.getNodeName())){
				  
				  String nome_segnalazione = "";
				  String ID = "";
				  
				  NodeList nodeChildList = child.getChildNodes();
				  for (int i = 0; i < nodeChildList.getLength(); i++) {
					  String nomeColonna = "";
					  String valoreColonna = "";
				      Node node = nodeChildList.item(i);
				      
				      NamedNodeMap attributes = node.getAttributes();
			    	  Node nodeid = attributes.getNamedItem("gml:id");
			    	  ID = nodeid.getNodeValue();
			    	  
			    	  ObjRecord tmpObj = new ObjRecord(ID);
			    	  List<ObjValue> valori = new ArrayList<ObjValue>();
				      
				      NodeList elementList = node.getChildNodes();
				      for (int x = 0; x < elementList.getLength(); x++) {
				    	  Node element = elementList.item(x);
				    	  
				    	  nomeColonna = element.getNodeName();
				    	  valoreColonna = element.getTextContent().trim();
				    	  
				    	  nomeColonna = nomeColonna.substring(nomeColonna.indexOf(":")+1, nomeColonna.length());
				    	  
				    	  if(nomeColonna.equals("survey_name")){
				    		  nome_segnalazione = valoreColonna;
				    	  }
				    	  
				    	  ObjValue tmp = new ObjValue(nomeColonna,valoreColonna);
				    	  valori.add(tmp);
				      }
				      
				      tmpObj.setValues(valori);
				      listaValori.add(tmpObj);
				      
				      if(recordListByName.containsKey(ID)){//if(recordListByName.containsKey(nome_segnalazione)){
				    	  
				      }else{
				    	  List<ObjFeature> l = new ArrayList<ObjFeature>();
				    	  ObjFeature f = new ObjFeature(feature);
				    	  ObjRecord r = new ObjRecord(ID);
				    	  ObjValue v = new ObjValue(nomeColonna,valoreColonna);
				    	  List<ObjValue> vv = new ArrayList<ObjValue>(); vv.add(v);
				    	  r.setValues(vv);
				    	  List<ObjRecord> rr = new ArrayList<ObjRecord>(); //rr.add(r);
				    	  
				    	  List<ObjRecord> tempValList = new ArrayList<ObjRecord>();
				    	  List<ObjValue> tempList = new ArrayList<ObjValue>();
				    	  for(ObjRecord ox : listaValori){
				    		  for(ObjValue ov : ox.getValues()){
				    			  if(ov.getColumn_name().equals("survey_name") && ov.getValue().equals(nome_segnalazione)){
				    				  //tempList.addAll(c)
				    				  tempList.addAll(ox.getValues());
				    				  ObjRecord obk = new ObjRecord(ID);
				    				  obk.setValues(tempList);
				    				  tempValList.add(obk);
				    			  }
				    		  }
				    	  }
				    	  
				    	  rr.addAll(tempValList);
				    	  
				    	  f.setRecords(rr);
				    	  l.add(f);
				    	  recordListByName.put(ID, l);//recordListByName.put(nome_segnalazione, l);
				      }
				  }
			  }// else if not 'wfs:member' do nothing
		  }
		return listaValori;
	}

}
