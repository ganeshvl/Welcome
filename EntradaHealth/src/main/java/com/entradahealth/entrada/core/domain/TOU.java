package com.entradahealth.entrada.core.domain;

public class TOU {
	
	String UserId;
	String TOUVersionNumber;
	boolean TOUAccepted;
	
	public TOU(String UserId, String TOUVersionNumber, boolean TOUAccepted) {
		// TODO Auto-generated constructor stub
		this.UserId = UserId;
		this.TOUVersionNumber = TOUVersionNumber;
		this.TOUAccepted = TOUAccepted;
	}
	
	public void setUserId(String UserID){
		this.UserId = UserID;
	}
	public String getUserId(){
		return UserId;
	}
	public void setTOUVersion(String versionNo){
		this.TOUVersionNumber = versionNo;
	}
	public String getTOUVersion(){
		return TOUVersionNumber;
	}
	public void setTOUAccepted(boolean accepted){
		this.TOUAccepted = accepted;
	}
	public boolean getTOUAccepted(){
		return TOUAccepted;
	}
	
}
