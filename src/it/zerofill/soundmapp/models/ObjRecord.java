package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.List;

public class ObjRecord implements Serializable{

	private static final long serialVersionUID = 2707274680795461371L;
	private String id;
	private List<ObjValue> values;
	
	public ObjRecord() {}
	
	public ObjRecord(String id) {
		this.id = id;
	}
	public ObjRecord(String id, List<ObjValue> values) {
		this.id = id;
		this.values = values;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<ObjValue> getValues() {
		return values;
	}
	public void setValues(List<ObjValue> values) {
		this.values = values;
	}
	
	
}
