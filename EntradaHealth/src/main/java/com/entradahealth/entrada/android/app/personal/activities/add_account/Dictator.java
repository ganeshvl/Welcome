package com.entradahealth.entrada.android.app.personal.activities.add_account;

public class Dictator {

	private long DictatorID;
	private String DictatorName;
	private long ClinicID;
	private long DefaultJobTypeID;
	private long DefaultQueueID;
	private String FirstName;
	private String MI;
	private String LastName;
	private String Suffix;
	private String ClinicCode;
	private String ClinicName;
	private boolean current;
	
	public long getDictatorID() {
		return DictatorID;
	}
	public void setDictatorID(long dictatorID) {
		DictatorID = dictatorID;
	}
	public String getDictatorName() {
		return DictatorName;
	}
	public void setDictatorName(String dictatorName) {
		DictatorName = dictatorName;
	}
	public long getClinicID() {
		return ClinicID;
	}
	public void setClinicID(long clinicID) {
		ClinicID = clinicID;
	}
	public long getDefaultJobTypeID() {
		return DefaultJobTypeID;
	}
	public void setDefaultJobTypeID(long defaultJobTypeID) {
		DefaultJobTypeID = defaultJobTypeID;
	}
	public long getDefaultQueueID() {
		return DefaultQueueID;
	}
	public void setDefaultQueueID(long defaultQueueID) {
		DefaultQueueID = defaultQueueID;
	}
	public String getFirstName() {
		return FirstName;
	}
	public void setFirstName(String firstName) {
		FirstName = firstName;
	}
	public String getMI() {
		return MI;
	}
	public void setMI(String mI) {
		MI = mI;
	}
	public String getLastName() {
		return LastName;
	}
	public void setLastName(String lastName) {
		LastName = lastName;
	}
	public String getSuffix() {
		return Suffix;
	}
	public void setSuffix(String suffix) {
		Suffix = suffix;
	}
	public String getClinicCode() {
		return ClinicCode;
	}
	public void setClinicCode(String clinicCode) {
		ClinicCode = clinicCode;
	}
	public String getClinicName() {
		return ClinicName;
	}
	public void setClinicName(String clinicName) {
		ClinicName = clinicName;
	}
	public boolean isCurrent() {
		return current;
	}
	public void setCurrent(boolean current) {
		this.current = current;
	}
	
}
