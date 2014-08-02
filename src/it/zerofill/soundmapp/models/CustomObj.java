package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class CustomObj implements Serializable{

	private static final long serialVersionUID = 6466128517824399281L;
	private HashMap<String, String> nameSpaceMap;

	public CustomObj(){
	}
	
	public HashMap<String, String> getNameSpaceMap() {
		return nameSpaceMap;
	}

	public void setNameSpaceMap(HashMap<String, String> nameSpaceMap) {
		this.nameSpaceMap = nameSpaceMap;
	}
	
	
	
	
	
	private HashMap<String, List<ObjFeature>> recordsMap;
	private Configuration configuration;
	private List<ObjFeature> listaValori;
	public HashMap<String, List<ObjFeature>> getRecordsMap() {
		return recordsMap;
	}

	public void setRecordsMap(HashMap<String, List<ObjFeature>> recordsMap) {
		this.recordsMap = recordsMap;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public List<ObjFeature> getListaValori() {
		return listaValori;
	}

	public void setListaValori(List<ObjFeature> listaValori) {
		this.listaValori = listaValori;
	}
}
