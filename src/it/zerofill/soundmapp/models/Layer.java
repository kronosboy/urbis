package it.zerofill.soundmapp.models;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Layer implements Serializable{
	
	private static final long serialVersionUID = 498416006397497773L;
	private String id;
	private String name;
	private List<AttributeType> attributes;
	
	public Layer(String name, List<AttributeType> attributes) {
		this.name = name;
		this.attributes = attributes;
	}
	
	public Layer(String id) {
		this.id = id;
	}
	
	public Layer() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AttributeType> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeType> attributes) {
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public HashMap<String, AttributeType> getAttributesMap(){
		HashMap<String, AttributeType> res = new HashMap<String, AttributeType>();
		for(AttributeType attr : attributes){
			res.put(attr.getId(),attr);
		}
		return res;
	}
	
	public String getKeyColumn(){
		String res = "";
		for(AttributeType attr : attributes){
			if(attr.getType().equalsIgnoreCase("key"))
				return attr.getId();
		}
		return res;
	}
	
	public String getNameColumn(){
		String res = "";
		for(AttributeType attr : attributes){
			if(attr.getType().equalsIgnoreCase("name"))
				return attr.getId();
		}
		return res;
	}
	
	public String getCreatorolumn(){
		String res = "";
		for(AttributeType attr : attributes){
			if(attr.getType().equalsIgnoreCase("creator"))
				return attr.getId();
		}
		return res;
	}
	
	@SuppressLint("UseSparseArrays")
	public HashMap<Integer, List<AttributeType>> getAttributesMapByPage(){
		HashMap<Integer, List<AttributeType>> res = new HashMap<Integer, List<AttributeType>>();
		for(AttributeType attr : attributes){
			if(!res.containsKey(attr.getPage())){
				List<AttributeType> lista = new ArrayList<AttributeType>();
				lista.add(attr);
				res.put(attr.getPage(),lista);
			}else{
				List<AttributeType> lista = res.get(attr.getPage());
				lista.add(attr);
				
				
				Collections.sort(lista, new Comparator<AttributeType>() {
				    public int compare(AttributeType object1, AttributeType object2) {
				    	if(object1.getOrdinal() > object2.getOrdinal())
				    		return 1;
				    	else return -1;
				    }
				});
				
			}
		}
		
		return res;
	}
	

}
