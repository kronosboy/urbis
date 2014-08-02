package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Configuration implements Serializable{
	
	private static final long serialVersionUID = -8705528204321885904L;
	private String name;
	private HashMap<String, String> urls;
	private List<String> appSettings;
	private List<String> nameSpace;
	
	private List<AttributeType> regConfig;
	private List<Layer> layers;
	
	public Configuration() {
	}
	
	public Configuration(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> getUrls() {
		return urls;
	}

	public void setUrls(HashMap<String, String> urls) {
		this.urls = urls;
	}

	public List<String> getAppSettings() {
		return appSettings;
	}

	public void setAppSettings(List<String> appSettings) {
		this.appSettings = appSettings;
	}

	public List<AttributeType> getRegConfig() {
		return regConfig;
	}

	public void setRegConfig(List<AttributeType> regConfig) {
		this.regConfig = regConfig;
	}

	public List<Layer> getLayers() {
		return layers;
	}
	
	public Layer getLayerByName(String layerName) {
		for(Layer layer : layers){
			if(layer.getName().equals(layerName)){
				return layer;
			}
		}
		return null;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public List<String> getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(List<String> nameSpace) {
		this.nameSpace = nameSpace;
	}

}
