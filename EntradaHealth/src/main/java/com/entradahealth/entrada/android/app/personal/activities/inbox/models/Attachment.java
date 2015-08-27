package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.io.Serializable;

public class Attachment implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String type;
	private String id;
	
	public Attachment(String type, String id) {
		super();
		this.type = type;
		this.id = id;
	}
	public Attachment() {
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
