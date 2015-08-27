package com.entradahealth.entrada.android.app.personal.activities.schedule.model;

/**
 * Resource Names bean
 */
public class Resource {

	private String resourceId;
	private String resourceName;
	private boolean isChecked = false;
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}


}
