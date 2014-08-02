package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.List;

public class AttributeType implements Serializable{

	private static final long serialVersionUID = 7140428055896576820L;
	private String id;
	private String name;
	private int ordinal;
	private int page;
	private String type;
	private boolean isNullable;
	private List<String> typeConfig;
	
	public AttributeType(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getTypeConfig() {
		return typeConfig;
	}

	public void setTypeConfig(List<String> typeConfig) {
		this.typeConfig = typeConfig;
	}
	
	public void setNullable(String nullable){
		if("true".equals(nullable))
			isNullable = true;
		else
			isNullable = false;
	}
	
	public boolean isNullable(){
		return isNullable;
	}
	
}
