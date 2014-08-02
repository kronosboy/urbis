package it.zerofill.soundmapp.models;

import java.io.Serializable;

public class FeatureElementAttribute implements Serializable{

	private static final long serialVersionUID = -2190935494291790498L;
	private String name;
	private String type;
	private boolean isNullable;
	
	public FeatureElementAttribute() {}
	
	public FeatureElementAttribute(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public FeatureElementAttribute(String name, String type, boolean isNullable) {
		this.name = name;
		this.type = type;
		this.isNullable = isNullable;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	
}
