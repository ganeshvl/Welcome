package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.io.Serializable;
import java.util.Date;

public class ENTConversation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5130668754426441612L;
	private String _id;
	private Date created_at;
	private String last_message;
	private long last_message_date_sent;
	private String last_message_user_id;
	private String name;
	private String[] occupants_ids;
	private String photo;
	private int type;
	private String user_id;
	private String xmpp_room_jid;
	private int unread_messages_count;
	private String customString;
	private long patient_id = 0;
	private String passPhrase;
	private boolean patientAccess;
	
	public String getId() {
		return _id;
	}
	public void setId(String _id) {
		this._id = _id;
	}
	public Date getCreatedAt() {
		return created_at;
	}
	public void setCreatedAt(Date created_at) {
		this.created_at = created_at;
	}
	public String getLastMessage() {
		return last_message;
	}
	public void setLastMessage(String last_message) {
		this.last_message = last_message;
	}
	public long getLastMessageDateSent() {
		return last_message_date_sent;
	}
	public void setLastMessageDateSent(long last_message_date_sent) {
		this.last_message_date_sent = last_message_date_sent;
	}
	public String getLastMessageUserId() {
		return last_message_user_id;
	}
	public void setLastMessageUserIid(String last_message_user_id) {
		this.last_message_user_id = last_message_user_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getOccupantsIds() {
		return occupants_ids;
	}
	public void setOccupantsIds(String[] occupants_ids) {
		this.occupants_ids = occupants_ids;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getUserId() {
		return user_id;
	}
	public void setUserId(String user_id) {
		this.user_id = user_id;
	}
	public String getXmpp_room_jid() {
		return xmpp_room_jid;
	}
	public void setXmpp_room_jid(String xmpp_room_jid) {
		this.xmpp_room_jid = xmpp_room_jid;
	}
	public int getUnreadMessagesCount() {
		return unread_messages_count;
	}
	public void setUnreadMessagesCount(int unread_messages_count) {
		this.unread_messages_count = unread_messages_count;
	}
	public String getCustomString() {
		return customString;
	}
	public void setCustomString(String customString) {
		this.customString = customString;
	}
	public long getPatientID() {
		return patient_id;
	}
	public void setPatientID(long patientID) {
		this.patient_id = patientID;
	}
	public String getPassPhrase() {
		return passPhrase;
	}
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}	
	public boolean getPatientAccess(){
		return patientAccess;
	}
	public void setPatientAccess(boolean patientAccess){
		this.patientAccess = patientAccess;
	}
}
