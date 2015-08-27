package com.entradahealth.entrada.android.app.personal.activities.add_account;

public class EUser {

	private String name;
	private String password;
	private String environment;
	private String currentDictator;
	private boolean isCurrent;
	
	public EUser(String name, String password, String environment,
			String currentDictator, boolean isCurrent) {
		super();
		this.name = name;
		this.password = password;
		this.environment = environment;
		this.currentDictator = currentDictator;
		this.isCurrent = isCurrent;
	}
	public EUser() {
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getCurrentDictator() {
		return currentDictator;
	}
	public void setCurrentDictator(String currentDictator) {
		this.currentDictator = currentDictator;
	}
	public boolean isCurrent() {
		return isCurrent;
	}
	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
	
	
}
