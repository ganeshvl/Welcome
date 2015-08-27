package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

public class Blob {
	private String content_type;
	private String name;
	
	public Blob(String content_type, String name) {
		super();
		this.content_type = content_type;
		this.name = name;
	}
	public String getContent_type() {
		return content_type;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
