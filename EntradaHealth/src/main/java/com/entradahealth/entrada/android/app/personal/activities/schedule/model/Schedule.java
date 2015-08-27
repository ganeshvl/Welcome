package com.entradahealth.entrada.android.app.personal.activities.schedule.model;

/**
 * Schedule bean
 */
public class Schedule {

	private int scheduleID;
	private int AppointmentStatus;
	private int PatientID;
	private String JobId;
	private String ReasonName;
	private String  ResourceID;
	private String AppointmentDate;
	
	private String MRN;
	
	public String getMRN() {
		return MRN;
	}
	public void setMRN(String mRN) {
		MRN = mRN;
	}
	public int getScheduleID() {
		return scheduleID;
	}
	public void setScheduleID(int scheduleID) {
		this.scheduleID = scheduleID;
	}
	public int getAppointmentStatus() {
		return AppointmentStatus;
	}
	public void setAppointmentStatus(int appointmentStatus) {
		AppointmentStatus = appointmentStatus;
	}
	public int getPatientID() {
		return PatientID;
	}
	public void setPatientID(int patientID) {
		PatientID = patientID;
	}
	public String getJobId() {
		return JobId;
	}
	public void setJobId(String jobId) {
		JobId = jobId;
	}
	public String getReasonName() {
		return ReasonName;
	}
	public void setReasonName(String reasonName) {
		ReasonName = reasonName;
	}
	public String getResourceID() {
		return ResourceID;
	}
	public void setResourceID(String resourceID) {
		ResourceID = resourceID;
	}
	public String getAppointmentDate() {
		return AppointmentDate;
	}
	public void setAppointmentDate(String appointmentDate) {
		AppointmentDate = appointmentDate;
	}
}
