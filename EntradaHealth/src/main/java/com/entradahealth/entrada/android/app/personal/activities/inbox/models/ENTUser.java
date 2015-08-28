package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

public class ENTUser {

	private String id;
	private String full_name; // firstname + lastname
	private String login;
	private String password;
	private boolean favorite;
	private String MI;
	private String firstName;
	private String lastName;
	private String registrationCode;
	private String phoneNumber;
	private String emailAddress;
	
	public ENTUser() {
	}

	public ENTUser(String MI, String firstName, String lastName,
			String phoneNumber, String emailAddress) {
		this.MI = MI;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}



	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return full_name;
	}
	public void setName(String name) {
		this.full_name = name;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isFavorite() {
		return favorite;
	}
	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
	public String getMI() {
		return MI;
	}
	public void setMI(String mI) {
		MI = mI;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getRegistrationCode() {
		return registrationCode;
	}
	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
}
