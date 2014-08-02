package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.List;

public class FeatureElement implements Serializable{
	
	private static final long serialVersionUID = -9064904069164791439L;
	private String name;
	private List<FeatureElementAttribute> element;
	
	public FeatureElement() {}
	
	public FeatureElement(String name) {
		this.name = name;
	}
	
	public FeatureElement(String name, List<FeatureElementAttribute> element) {
		this.name = name;
		this.element = element;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<FeatureElementAttribute> getElement() {
		return element;
	}
	public void setElement(List<FeatureElementAttribute> element) {
		this.element = element;
	}
}
