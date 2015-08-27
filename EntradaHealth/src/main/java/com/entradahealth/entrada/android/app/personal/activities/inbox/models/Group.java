package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.util.List;

public class Group {

	private int id;
	private String name;
	private List<Contact> contacts;
		
	public Group(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Group(int id, String name, List<Contact> contacts) {
		super();
		this.id = id;
		this.name = name;
		this.contacts = contacts;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Contact> getContacts() {
		return contacts;
	}
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	public String getAll(){
		return id+"/"+name;
	}
	
	@Override
	public String toString() {
		return "Group [id = "+id+" name=" + name + ", Contacts=" + contacts + "]";
	}

}
