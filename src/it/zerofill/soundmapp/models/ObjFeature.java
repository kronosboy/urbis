package it.zerofill.soundmapp.models;

import java.io.Serializable;
import java.util.List;

public class ObjFeature implements Serializable{
	
	private static final long serialVersionUID = 3760687017934519407L;
	private String featureName;
	List<ObjRecord> records;
	
	public ObjFeature() {}
	
	public ObjFeature(String featureName) {
		this.featureName = featureName;
	}
	
	public ObjFeature(String featureName, List<ObjRecord> records) {
		this.featureName = featureName;
		this.records = records;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public List<ObjRecord> getRecords() {
		return records;
	}

	public void setRecords(List<ObjRecord> records) {
		this.records = records;
	}
	

}
