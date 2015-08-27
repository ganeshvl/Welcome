package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.util.Date;

public class Conversation {

	private int id;
	private String recepientName;
	private String lastMessage;
	private Date lastSeenDate;
	private String patientName;
	private int patientResource;
	private int unreadCount;
	
	public Conversation(int id, String recepientName, String lastMessage,
			Date lastSeenDate, String patientName, int patientResource, int unreadCount) {
		super();
		this.id = id;
		this.recepientName = recepientName;
		this.lastMessage = lastMessage;
		this.lastSeenDate = lastSeenDate;
		this.patientName = patientName;
		this.patientResource = patientResource;
		this.unreadCount = unreadCount;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRecepientName() {
		return recepientName;
	}
	public void setRecepientName(String recepientName) {
		this.recepientName = recepientName;
	}
	public String getLastMessage() {
		return lastMessage;
	}
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	public Date getLastSeenDate() {
		return lastSeenDate;
	}
	public void setLastSeenDate(Date lastSeenDate) {
		this.lastSeenDate = lastSeenDate;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public int getPatientResource() {
		return patientResource;
	}
	public void setPatientResource(int patientResource) {
		this.patientResource = patientResource;
	}
	public int getUnreadCount() {
		return unreadCount;
	}
	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

}
