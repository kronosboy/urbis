package it.zerofill.soundmapp.models;

import java.io.Serializable;

public class ObjValue implements Serializable{
	private static final long serialVersionUID = 8741775162388819324L;
	private String column_name;
	private String value;
	private int ordinal;
	
	
	public ObjValue() {}
	
	public ObjValue(String column_name) {
		this.column_name = column_name;
	}
	
	public ObjValue(String column_name, String value) {
		this.column_name = column_name;
		this.value = value;
	}
	
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
