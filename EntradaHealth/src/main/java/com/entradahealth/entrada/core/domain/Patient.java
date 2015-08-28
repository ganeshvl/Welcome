package com.entradahealth.entrada.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

/**
 * Maps to the Patient table within the device. Unlike the iOS
 * application that uses sqlite, with h2 we can securely make the
 * MRN the primary index (though this is subject to change in the
 * future).
 *
 * @author edr
 * @since 28 Aug 2012
 */
public final class Patient implements Comparable<Patient>, Serializable
{
    // database mappings
    public static final String FIELD_ID = "PatientID";
    public static final String FIELD_MRN = "MRN";
    public static final String FIELD_FIRST_NAME = "FirstName";
    public static final String FIELD_MIDDLE_INITIAL = "MI";
    public static final String FIELD_LAST_NAME = "LastName";
    public static final String FIELD_DOB = "DOB";
    public static final String FIELD_GENDER = "Gender";
    public static final String FIELD_PCPID= "PrimaryCareProviderID";
    public static final String FIELD_ADDRESS1 = "Address1";
    public static final String FIELD_ADDRESS2 = "Address2";
    public static final String FIELD_CITY = "City";
    public static final String FIELD_STATE = "State";
    public static final String FIELD_ZIP = "Zip";
    public static final String FIELD_PHONE = "Phone1";

    public final long id;
    public final String medicalRecordNumber;
    public final String firstName;
    public final String middleInitial;
    public final String lastName;
    public final String dateOfBirth;
    public final Gender gender;
    public final String pcpid;
    public final String address1;
    public final String address2;
    public final String city;
    public final String state;
    public final String zip;
    public final String phone;

    @JsonCreator
    public Patient(@JsonProperty(FIELD_ID) long id,
                   @JsonProperty(FIELD_MRN) String medicalRecordNumber,
                   @JsonProperty(FIELD_FIRST_NAME) String firstName,
                   @JsonProperty(FIELD_MIDDLE_INITIAL) String middleInitial,
                   @JsonProperty(FIELD_LAST_NAME) String lastName,
                   @JsonProperty(FIELD_DOB) String dateOfBirth,
                   @JsonProperty(FIELD_GENDER) String gender,
                   @JsonProperty(FIELD_PCPID) String pcpid,
                   @JsonProperty(FIELD_ADDRESS1) String address1,
                   @JsonProperty(FIELD_ADDRESS2) String address2,
                   @JsonProperty(FIELD_CITY) String city,
                   @JsonProperty(FIELD_STATE) String state,
                   @JsonProperty(FIELD_ZIP) String zip,
                   @JsonProperty(FIELD_PHONE) String phone)
    {
        // TODO: make firstName and lastName nullable-safe.

    	
        this.id = id;
        this.medicalRecordNumber = medicalRecordNumber;
        this.firstName = firstName != null ? firstName : "";
        this.middleInitial = middleInitial != null ? middleInitial : null;
        this.lastName = lastName != null ? lastName : "";
        this.dateOfBirth = dateOfBirth;
        this.pcpid = pcpid;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        
    	/*
    	this.id = id;
        this.medicalRecordNumber = BundleKeys.LOCAL_MRN;
        this.firstName = BundleKeys.LOCAL_FIRSTNAME != null ? BundleKeys.LOCAL_FIRSTNAME : "";
        this.middleInitial = middleInitial != null ? middleInitial : null;
        this.lastName = BundleKeys.LOCAL_LASTNAME != null ? BundleKeys.LOCAL_LASTNAME : "";
        this.dateOfBirth = dateOfBirth;
        */

        if (gender.length() == 0)
        {
            this.gender = Gender.UNKNOWN;
        }
        else
        {
            String g = gender.trim().substring(0, 1).toUpperCase();
            if ("M".equals(g))
            {
                this.gender = Gender.MALE;
            }
            else if ("F".equals(g))
            {
                this.gender = Gender.FEMALE;
            }
            else
            {
                this.gender = Gender.UNKNOWN;
            }
        }
    }

    
    public String getFirstName(){
    	return firstName;
    }
    
    public String getLastName(){
    	return lastName;
    }
    
    @JsonIgnore
    public String getFullName()
    {
        if (Strings.isNullOrEmpty(middleInitial) || middleInitial.charAt(0) == '?')
        {
            //return String.format("%s %s", firstName, lastName);
            return String.format("%s %s", lastName+",", firstName);
        }
        else
        {
            //return String.format("%s %s. %s", firstName, middleInitial, lastName);
            return String.format("%s %s. %s", lastName+",", firstName, middleInitial);
        }
    }
    
    @JsonIgnore
    public String getName()
    {
        return String.format("%s %s", lastName+",", firstName);
        
    }
    
    public long getPatientID(){
		return id;
    	
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Patient patient = (Patient) o;

        if (id != patient.id)
            return false;
        if (!medicalRecordNumber.equals(patient.medicalRecordNumber))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + medicalRecordNumber.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("%s %s (MRN %s, id %d)", firstName,  lastName, medicalRecordNumber, id);
    }

    @Override
    public int compareTo(Patient patient)
    {
        if (Strings.isNullOrEmpty(lastName) && Strings.isNullOrEmpty(patient.lastName)) return 0;
        if (Strings.isNullOrEmpty(lastName) && !Strings.isNullOrEmpty(patient.lastName)) return 1;
        if (!Strings.isNullOrEmpty(lastName) && Strings.isNullOrEmpty(patient.lastName)) return -1;
        return lastName.compareTo(patient.lastName);
    }
    
    
}
