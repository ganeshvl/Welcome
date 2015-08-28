package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ENTMessage implements Serializable{
	
	public static final int TEXT = 0;
	public static final int ALERT = 1;
	public static final int IMAGE = 2;
	public static final int AUDIO = 3;
	
	private static final long serialVersionUID = 1L;
	
	private String _id;
	private String message;
	private String recipient_id;
	private String chat_dialog_id;
	private Date created_at;
	private long date_sent;
	private String sender_id;
	private Date updated_at;
	private int read;
	private String customString;
	private String attachmentID;
	private String[] recipient_ids;
	private boolean outgoing;
	private boolean delivered;
	private long patient_id;
	private String attachmentName;
	private int contentType;
	private List<Attachment> attachments;
	private String passPhrase;
	private int type;
	
	public String getId() {
		return _id;
	}
	public void setId(String id) {
		this._id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getRecepient() {
		return recipient_id;
	}
	public void setRecepient(String recepient) {
		this.recipient_id = recepient;
	}
	public String getChatDialogId() {
		return chat_dialog_id;
	}
	public void setChatDialogId(String chatDialogId) {
		this.chat_dialog_id = chatDialogId;
	}
	public Date getCreatedDate() {
		return created_at;
	}
	public void setCreatedDate(Date created_at) {
		this.created_at = created_at;
	}
	public long getSentDate() {
		return date_sent;
	}
	public void setSentDate(long date_sent) {
		this.date_sent = date_sent;
	}
	public String getSender() {
		return sender_id;
	}
	public void setSender(String sender_id) {
		this.sender_id = sender_id;
	}
	public Date getUpdatedDate() {
		return updated_at;
	}
	public void setUpdatedDate(Date updated_at) {
		this.updated_at = updated_at;
	}
	public boolean isRead() {
		return (read == 1) ? true: false;
	}
	public void setAsRead(int read) {
		this.read = read;
	}
	public String getCustomString() {
		return customString;
	}
	public void setCustomString(String customString) {
		this.customString = customString;
	}
	public String getAttachmentID() {
		return attachmentID;
	}
	public void setAttachmentID(String attachmentID) {
		this.attachmentID = attachmentID;
	}
	public String[] getRecipients() {
		return recipient_ids;
	}
	public void setRecipients(String[] recipient_ids) {
		this.recipient_ids = recipient_ids;
	}
	public boolean isOutgoing() {
		return outgoing;
	}
	public void setOutgoing(boolean outgoing) {
		this.outgoing = outgoing;
	}
	public boolean isDelivered() {
		return delivered;
	}
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
	public long getPatientID() {
		return patient_id;
	}
	public void setPatientID(long patientID) {
		this.patient_id = patientID;
	}
	public String getAttachmentName() {
		return attachmentName;
	}
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}
	public int getContentType() {
		return contentType;
	}
	public void setContentType(int contentType) {
		this.contentType = contentType;
	}
	public List<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	public int getTypeFromContent(String contentType){
		if(contentType.equals("audio")){
			return AUDIO;
		} else if(contentType.equals("image")){
			return IMAGE;
		}
		return TEXT;
	}
	public String getPassPhrase() {
		return passPhrase;
	}
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
