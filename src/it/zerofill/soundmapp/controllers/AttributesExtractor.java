package it.zerofill.soundmapp.controllers;

import android.annotation.SuppressLint;
import it.zerofill.soundmapp.models.AttributeType;
import it.zerofill.soundmapp.models.Configuration;
import it.zerofill.soundmapp.models.Layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AttributesExtractor {
	
	private static final String CONFIGURATION = "configuration";
	private static final String URLS = "urls";
	private static final String REGISTRATIONCONFIG = "registrationConfig";
	private static final String LAYERS = "layers";
	private static final String NAMESPACE = "namespace";
	
	private static final String GEOSLAYER = "geoserverLayer";
	private static final String FEATCONFIG = "featuresConfig";
	
	private static final String NAME = "Name";
	private static final String ORDIANL = "Ordinal";
	private static final String TYPE = "type";
	private static final String NULLABLE = "nullable";
	private static final String TYPE_CONF = "typeConfig";
	private static final String VALUES = "values";
	
	private static final String PAGE = "Page";
	
//	private static final String TYPE_COMBO = "combo";
	private static final String TYPE_STRING = "String";
	private static final String TYPE_INT = "Integer";
	private static final String TYPE_DOUBLE = "Double";
//	private static final String TYPE_DATE = "Date";
//	private static final String TYPE_IMAGE = "Image";
	
	private static HashMap<String, String> firstAttributeForQueryMap;
	
	private static AttributesExtractor instance;
	private AttributesExtractor(){}
	public static AttributesExtractor getInstance(){
		if(instance==null)
			instance = new AttributesExtractor();
		return instance;
	}
	
	public static HashMap<String, String> getFirstAttributeForQueryMap(){
		return firstAttributeForQueryMap;
	}
	
	
	@SuppressWarnings("rawtypes")
	public static List<String> getConfigurationsName(String jsonString){
		List<String> res = new ArrayList<String>();
		
		try{
			JSONObject json = new JSONObject(jsonString);
			JSONArray jsonMainNode = json.optJSONArray("Android");
			int jsonLen = jsonMainNode.length();
			
			// main for: cycling to get configurations
			for(int i=0; i < jsonLen; i++){
				JSONObject jsonConfig = jsonMainNode.getJSONObject(i);
				
				// cycling over elements of each configuration
				Iterator element = jsonConfig.keys();
		       	while(element.hasNext()){
		       		String key = (String)element.next();
		       		// Keys of a configuration:
		       		// * configuration 		-> config name
		       		// * urls				-> urls of the server
		       		// * namespace			-> namespace to use
		       		// * appSettings		-> general settings		NOT USED
		       		// * treeStruct			-> treeStruct			NOT USED
		       		// * registrationConfig	-> registration element
		       		// * layers				-> layers
		       		if(key.equals(CONFIGURATION)){
		       			res.add(jsonConfig.optString(key));
		       		}
		       	}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	
	
	
	@SuppressWarnings("rawtypes")
	public static HashMap<String, Configuration> getAttrbiutes(String jsonString){
		HashMap<String, Configuration> res = new HashMap<String, Configuration>();
		firstAttributeForQueryMap = new HashMap<String, String>();
		try{
			JSONObject json = new JSONObject(jsonString);
			JSONArray jsonMainNode = json.optJSONArray("Android");
			int jsonLen = jsonMainNode.length();
			
			Configuration config = new Configuration();
			// main for: cycling to get configurations
			for(int i=0; i < jsonLen; i++){
				JSONObject jsonConfig = jsonMainNode.getJSONObject(i);
				
				config = new Configuration();
				
				// cycling over elements of each configuration
				Iterator element = jsonConfig.keys();
		       	while(element.hasNext()){
		       		String key = (String)element.next();
		       		// Keys of a configuration:
		       		// * configuration 		-> config name
		       		// * urls				-> urls of the server
		       		// * namespace			-> namespace to use
		       		// * appSettings		-> general settings		NOT USED
		       		// * treeStruct			-> treeStruct			NOT USED
		       		// * registrationConfig	-> registration element
		       		// * layers				-> layers
		       		
		       		
		       		if(key.equals(CONFIGURATION)){
		       			config.setName(jsonConfig.optString(key));
		       		}

		       		
		       		if(key.equals(URLS)){
		       			HashMap<String, String> urls = new HashMap<String, String>();
		       			JSONObject jsonElement = jsonConfig.getJSONObject(key);
		       			Iterator it = jsonElement.keys();
		       			while(it.hasNext()){
		       				String k = (String)it.next(); 
		       				if(k.equals("wfs"))
		       					urls.put("wfs", jsonElement.optString(k));
		       				if(k.equals("auth"))
		       					urls.put("auth", jsonElement.optString(k));
		       				if(k.equals("background"))
		       					urls.put("background", jsonElement.optString(k));
		       				if(k.equals("banner"))
		       					urls.put("banner", jsonElement.optString(k));
		       			}
		       			config.setUrls(urls);
		       		}
		       		
		       		
		       		if(key.equals(NAMESPACE)){
		       			List<String> namespace = new ArrayList<String>();
       					JSONArray values = jsonConfig.optJSONArray(NAMESPACE);
       					for(int x=0; x<values.length(); x++){
       						String val = values.optString(x);
       						namespace.add(val);
       					}
		       			config.setNameSpace(namespace);
		       		}

		       		
		       		
		       		if(key.equals(REGISTRATIONCONFIG)){
		       			List<AttributeType> regConfig = new ArrayList<AttributeType>();
		       			
		       			JSONObject jsonElements = jsonConfig.getJSONObject(key); // List of items in registrationConfig
		       			Iterator it = jsonElements.keys();
		       			while(it.hasNext()){
		       				String k = (String)it.next(); 
		       				AttributeType attrTemp = new AttributeType(k);
		       				
		       				JSONObject jsonElement = jsonElements.getJSONObject(k); // generic item (age, sex...)
		       				String name = jsonElement.optString(NAME);
		       				attrTemp.setName(name);
		       				int ordinal = jsonElement.optInt(ORDIANL);
		       				attrTemp.setOrdinal(ordinal);
		       				String type = jsonElement.optString(TYPE);
		       				attrTemp.setType(type);
		       				if(jsonElement.has(TYPE_CONF)){
		       					JSONObject jsonValues = jsonElement.getJSONObject(TYPE_CONF);
		       					JSONArray values = jsonValues.optJSONArray(VALUES);
		       					List<String> typeConfig = new ArrayList<String>();
		       					for(int x=0; x<values.length(); x++){
		       						String val = values.optString(x);
		       						typeConfig.add(val);
		       					}
		       					attrTemp.setTypeConfig(typeConfig);
		       				}
		       				regConfig.add(attrTemp);
		       			}
		       			config.setRegConfig(regConfig);
		       		}

		       		
		       		
		       		if(key.equals(LAYERS)){
		       			List<Layer> layers = new ArrayList<Layer>();
		       			
		       			JSONArray layerArray = jsonConfig.optJSONArray(key);
		       			
		       			// cycling over layers
		       			for(int x=0; x<layerArray.length(); x++){
		       				Layer layerTmp = new Layer();
		       				
		       				JSONObject layer = layerArray.optJSONObject(x); // Object LAYERNAME...
		       				
		       				// cycling over elements of each layer
		    				Iterator layerElementKey = layer.keys(); // keys are LAYERNAME...
		    		       	while(layerElementKey.hasNext()){
		    		       		String layerName = (String)layerElementKey.next();
		    		       		
		    		       		layerTmp.setId(layerName);
		    		       		
		    		       		JSONObject layerObj = layer.optJSONObject(layerName); // ex. Object LAYERNAME
		    		       		Iterator layerElement = layerObj.keys(); // keys are LAYERNAME...
		    		       		while(layerElement.hasNext()){
		    		       			String layerKey = (String)layerElement.next(); 
		    		       			// Keys of a layer:
			    		       		// * geoserverLayer 	-> name of layer
			    		       		// * treeDir			-> treeDir 			NOT USED
			    		       		// * featuresConfig		-> list of element
			    		       		
			    		       		if(layerKey.equals(GEOSLAYER)){
			    		       			layerTmp.setName(layerObj.optString(GEOSLAYER));
			    		       		}

			    		       		
			    		       		if(layerKey.equals(FEATCONFIG)){
			    		       			List<AttributeType> attributes = new ArrayList<AttributeType>();
			    		       			
			    		       			JSONObject jsonElements = layerObj.getJSONObject(layerKey); // List of items in featuresConfig
			    		       			Iterator it = jsonElements.keys();
			    		       			while(it.hasNext()){
			    		       				String k = (String)it.next(); 
			    		       				AttributeType attrTemp = new AttributeType(k); // ex. k=picture, k=vegatazione
			    		       				
			    		       				JSONObject jsonElement = jsonElements.getJSONObject(k); // generic item (picture, vegatazione...)
			    		       				
			    		       				String name = jsonElement.optString(NAME);
			    		       				attrTemp.setName(name);
			    		       				int page = jsonElement.optInt(PAGE);
			    		       				attrTemp.setPage(page);
			    		       				int ordinal = jsonElement.optInt(ORDIANL);
			    		       				attrTemp.setOrdinal(ordinal);
			    		       				
			    		       				if(jsonElement.has(NULLABLE)){
			    		       					String nullable = jsonElement.optString(NULLABLE);
				    		       				attrTemp.setNullable(nullable);
			    		       				}else{
			    		       					attrTemp.setNullable("true");
			    		       				}
			    		       				
			    		       				String type = jsonElement.optString(TYPE);
			    		       				attrTemp.setType(type);
			    		       				
			    		       				// type:
			    		       				// img64
			    		       				// combo
			    		       				// Integer
			    		       				// Double
			    		       				// String
			    		       				// Date
			    		       				if(!firstAttributeForQueryMap.containsKey(layerName) && (type.equals(TYPE_STRING) || type.equals(TYPE_INT) || type.equals(TYPE_DOUBLE))){
			    		       					firstAttributeForQueryMap.put(layerTmp.getName(), k);
			    		       				}
			    		       				
			    		       				if(jsonElement.has(TYPE_CONF)){
			    		       					JSONObject jsonValues = jsonElement.getJSONObject(TYPE_CONF);
			    		       					JSONArray values = jsonValues.optJSONArray(VALUES);
			    		       					List<String> typeConfig = new ArrayList<String>();
			    		       					for(int y=0; y<values.length(); y++){
			    		       						String val = values.optString(y);
			    		       						typeConfig.add(val);
			    		       					}
			    		       					attrTemp.setTypeConfig(typeConfig);
			    		       				}
			    		       				attributes.add(attrTemp);
			    		       			}
			    		       			layerTmp.setAttributes(attributes);
			    		       		}
		    		       		}
		    		       		layers.add(layerTmp);
		    		       	}
       					}
		       			config.setLayers(layers);
		       		}
		       	}
			}
			res.put(config.getName(), config);
		}catch(JSONException e){
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	public static List<AttributeType> getSortedAttributes(List<AttributeType> toSort){
		//maxPageNumber = 1;
		List<AttributeType> sorted = new ArrayList<AttributeType>();
		for(AttributeType toSortAttr : toSort){
			if(sorted.size()==0)
				sorted.add(toSortAttr);
			else{
				int index = 0;
				boolean isInserted = false;
				for(AttributeType attr : sorted){
//					if(attr.getPage()>maxPageNumber)
//						maxPageNumber = attr.getPage();
					if(toSortAttr.getPage()<=attr.getPage() && toSortAttr.getOrdinal()<attr.getOrdinal()){
						sorted.add(index, toSortAttr);
						isInserted = true;
						break;
					}else index++;
				}
				if(!isInserted)sorted.add(index, toSortAttr);
			}
		}
		
		return sorted;
	}
	
	@SuppressLint("UseSparseArrays")
	public static HashMap<Integer, List<AttributeType>> getLayersMapOrderedByPage(List<Layer> layers){
		HashMap<Integer, List<AttributeType>> result = new HashMap<Integer, List<AttributeType>>();
		
		for(Layer layer:layers){
			for(AttributeType attr:layer.getAttributes()){
				if(!result.containsKey(attr.getPage())){
					List<AttributeType> attributeTmp = new ArrayList<AttributeType>();
					attributeTmp.add(attr);
					result.put(attr.getPage(), attributeTmp);
				}else{
					result.get(attr.getPage()).add(attr);
				}
			}
		}
		
		for(int key:result.keySet()){
			List<AttributeType> sorted = getSortedAttributes(result.get(key));
			result.get(key).removeAll(result.get(key));
			result.get(key).addAll(sorted);
		}
		
		return result;
	}
	
	public static int getMaxPageNumber(Layer layer){
		int maxPageNumber = 1;
		for(AttributeType attr:layer.getAttributes()){
			if(attr.getPage()>maxPageNumber)
				maxPageNumber = attr.getPage();
		}
		return maxPageNumber;
	}

}
