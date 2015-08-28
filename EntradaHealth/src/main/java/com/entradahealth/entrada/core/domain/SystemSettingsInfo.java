package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SystemSettingsInfo
{
    public static final String JSON_GENERIC_PATIENT_ID = "GenericPatientID";
    public static final String JSON_PATIENT_MRN = "MRN";
    public static final String JSON_PATIENT_LNAME = "LastName";
    public static final String JSON_PATIENT_FNAME = "FirstName";
    public static final String JSON_EXPRESS_QUEUES = "ExpressQueuesEnabled";
    
	public static final String JSON_IMAGE_CAPTURE = "ImageCaptureEnabled";
	public static final String JSON_PATIENT_CLINICALS = "PatientClinicalsEnabled";
    
    @JsonProperty(JSON_GENERIC_PATIENT_ID)
    public final int genericPatientID;

    @JsonProperty(JSON_PATIENT_MRN)
    public final int mrn;
    
    @JsonProperty(JSON_PATIENT_LNAME)
    public final String lastname;
    
    @JsonProperty(JSON_PATIENT_FNAME)
    public final String firstname;
    
    @JsonProperty(JSON_EXPRESS_QUEUES)
    public final boolean expressQueues;
    
	@JsonProperty(JSON_IMAGE_CAPTURE)
	public final boolean captureEnabled;
	
	@JsonProperty(JSON_PATIENT_CLINICALS)
	public final boolean clinicalsEnabled;
    
    @JsonCreator
    public SystemSettingsInfo(@JsonProperty(JSON_GENERIC_PATIENT_ID) int genericPatientID,
    						@JsonProperty(JSON_PATIENT_MRN) int mrn,
    						@JsonProperty(JSON_PATIENT_LNAME) String lastname,
    						@JsonProperty(JSON_PATIENT_FNAME) String firstname,
			@JsonProperty(JSON_EXPRESS_QUEUES) boolean expressQueues,
			@JsonProperty(JSON_IMAGE_CAPTURE) boolean captureEnabled,
			@JsonProperty(JSON_PATIENT_CLINICALS) boolean clinicalsEnabled) {
        this.genericPatientID = genericPatientID;
        this.mrn = mrn;
        this.lastname = lastname;
        this.firstname = firstname;
        this.expressQueues = expressQueues;
		this.captureEnabled = captureEnabled;
		this.clinicalsEnabled = clinicalsEnabled;
     
    }
}
