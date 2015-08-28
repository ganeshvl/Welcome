package com.entradahealth.entrada.android.app.personal.activities.schedule.model;

/**
 * Section List Item info modelclass
 */
public class SectionListItemInfo {


	private String id;
	private String appointmentDate; //2014-03-03T09:25:00
	private String appointmentSimpleDate;
	private String appointmentTime;
	private String appointmentStatus;
	private String jobId;
	private String patientID;
	private String resourceName;
	private String reasonName;
	private String resourceID;
	private String scheduleId;
	private String mrn;

	public SectionListItemInfo() {

	}

	public SectionListItemInfo(String appointmentDate,String appointmentSimpleDate,String appointmentTime, String appointmentStatus, String jobId, String patientID,
			String resourceName, String reasonName, String resourceID,
			String scheduleId, String mrn) {
		this.appointmentDate = appointmentDate;
		this.appointmentSimpleDate = appointmentSimpleDate;
		this.appointmentTime = appointmentTime;
		this.appointmentStatus = appointmentStatus;
		this.jobId = jobId;
		this.patientID = patientID;
		this.resourceName = resourceName;
		this.reasonName = reasonName;
		this.resourceID = resourceID;
		this.scheduleId = scheduleId;
		this.mrn = mrn;
	}

	public String getId() {
		return id;
	}

	public String getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(String appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public String getAppointmentStatus() {
		return appointmentStatus;
	}

	public void setAppointmentStatus(String appointmentStatus) {
		this.appointmentStatus = appointmentStatus;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getPatientID() {
		return patientID;
	}

	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getReasonName() {
		return reasonName;
	}

	public void setReasonName(String reasonName) {
		this.reasonName = reasonName;
	}

	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	
	public String getAppointmentSimpleDate() {
		return appointmentSimpleDate;
	}

	public void setAppointmentSimpleDate(String appointmentSimpleDate) {
		this.appointmentSimpleDate = appointmentSimpleDate;
	}

	public String getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}


}