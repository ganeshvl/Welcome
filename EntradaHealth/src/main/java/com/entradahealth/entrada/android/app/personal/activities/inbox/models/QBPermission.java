package com.entradahealth.entrada.android.app.personal.activities.inbox.models;

public class QBPermission{
	
	private String QBUserLogin;
	private int PermissionCode;
	
	public QBPermission(String qBUserLogin, int permissionCode) {
		QBUserLogin = qBUserLogin;
		PermissionCode = permissionCode;
	}
	
	public String getQBUserLogin() {
		return QBUserLogin;
	}
	public void setQBUserLogin(String qBUserLogin) {
		QBUserLogin = qBUserLogin;
	}
	public int getPermissionCode() {
		return PermissionCode;
	}
	public void setPermissionCode(int permissionCode) {
		PermissionCode = permissionCode;
	}
	
	
}