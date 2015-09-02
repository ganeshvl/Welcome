package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

import java.util.Date;

public class ChatMessage {
	
	public static final int STATUS_UNSENT = 0;
	public static final int STATUS_SENT = 1;
	public static final int STATUS_DELIVERED = 2;
	public static final int STATUS_READ = 3;
	
	public static final int MSGTYPE_TEXT = 0;
	public static final int MSGTYPE_ALERT = 1;
	public static final int MSGTYPE_IMAGE = 2;
	public static final int MSGTYPE_AUDIO = 3;
	
	private int id;
	/**
	 * The content of the message
	 */
	private String message;
	/**
	 * boolean to determine, who is sender of this message
	 */
	private boolean isMine;
	/**
	 * boolean to determine, whether the message is a status message or not.
	 * it reflects the changes/updates about the sender is writing, have entered text etc
	 */
	private boolean isStatusMessage;
	private Date messageTime;
	
	private String fromContact;
	//private Contact selectedContact;
	private String selectedContact;
	private int messageStatus;
	
	private boolean isGroup = false;
	private int messageType;
	private int audioDuration = 0;
	private int audioPosition = 0;
	private String imagePath ;
	private String attachmentId;
	private boolean read;
	
	public ChatMessage() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Constructor to make a Message object
	 */
	public ChatMessage(String message, boolean isMine) {
		super();
		this.message = message;
		this.isMine = isMine;
		this.isStatusMessage = false;
	}
	/**
	 * Constructor to make a status Message object
	 * consider the parameters are swaped from default Message constructor,
	 *  not a good approach but have to go with it.
	 */
	public ChatMessage(boolean status, String message) {
		super();
		this.message = message;
		this.isMine = false;
		this.isStatusMessage = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isMine() {
		return isMine;
	}
	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}
	public boolean isStatusMessage() {
		return isStatusMessage;
	}
	public void setStatusMessage(boolean isStatusMessage) {
		this.isStatusMessage = isStatusMessage;
	}
	public Date getMessageTime() {
		return messageTime;
	}
	public void setMessageTime(Date messageTime) {
		this.messageTime = messageTime;
	}
	public String getFromContact() {
		return fromContact;
	}
	public void setFromContact(String fromContact) {
		this.fromContact = fromContact;
	}
	public int getMessageStatus() {
		return messageStatus;
	}
	public void setMessageStatus(int messageStatus) {
		this.messageStatus = messageStatus;
	}
	public boolean isGroup() {
		return isGroup;
	}
	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public int getAudioDuration() {
		return audioDuration;
	}
	public void setAudioDuration(int audioDuration) {
		this.audioDuration = audioDuration;
	}
	public int getAudioPosition() {
		return audioPosition;
	}
	public void setAudioPosition(int audioPosition) {
		this.audioPosition = audioPosition;
	}
	public void setImagePath(String imagePath){
		this.imagePath = imagePath;
	}
	public String getImagePath(){
		return imagePath;
	}
	/*public void setSelectedContact(Contact selectedContact){
		this.selectedContact = selectedContact;
	}
	public Contact getSelectedContact(){
		return selectedContact;
	}*/
	public void setSelectedContact(String selContact){
		this.selectedContact = selContact;
	}
	public String getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	
}
