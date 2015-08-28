package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

public final class Physicians {

	public static final String FIELD_REFERRINGID = "ReferringID";
	public static final String FIELD_PHYSICIANID = "PhysicianID";
	public static final String FIELD_CLINICID = "ClinicID";
    public static final String FIELD_FIRST_NAME = "FirstName";
    public static final String FIELD_MIDDLE_INITIAL = "MI";
    public static final String FIELD_LAST_NAME = "LastName";
    public static final String FIELD_DOB = "DOB";
    public static final String FIELD_GENDER = "Gender";
    public static final String FIELD_ADDRESS1 = "Address1";
    public static final String FIELD_ADDRESS2 = "Address2";
    public static final String FIELD_CITY = "City";
    public static final String FIELD_STATE = "State";
    public static final String FIELD_ZIP = "Zip";
    public static final String FIELD_PHONE = "Phone1";


    public final String phyid;
    public final long refid;
    public final long clinicid;
    public final String firstName;
    public final String middleInitial;
    public final String lastName;
    public final String dateOfBirth;
    public final Gender gender;
    public final String address1;
    public final String address2;
    public final String city;
    public final String state;
    public final String zip;
    public final String phone;
    

    @JsonCreator
    public Physicians(@JsonProperty(FIELD_REFERRINGID) long rid,
    					@JsonProperty(FIELD_PHYSICIANID) String pid,
    					@JsonProperty(FIELD_CLINICID) long clncid,
			            @JsonProperty(FIELD_FIRST_NAME) String firstName,
			            @JsonProperty(FIELD_MIDDLE_INITIAL) String middleInitial,
			            @JsonProperty(FIELD_LAST_NAME) String lastName,
			            @JsonProperty(FIELD_DOB) String dateOfBirth,
			            @JsonProperty(FIELD_GENDER) String gender,
			            @JsonProperty(FIELD_ADDRESS1) String address1,
			            @JsonProperty(FIELD_ADDRESS2) String address2,
			            @JsonProperty(FIELD_CITY) String city,
			            @JsonProperty(FIELD_STATE) String state,
			            @JsonProperty(FIELD_ZIP) String zip,
			            @JsonProperty(FIELD_PHONE) String phone)
    {
    	this.phyid = pid;
    	this.refid = rid;
    	this.clinicid = clncid;
        this.firstName = firstName != null ? firstName : "";
        this.middleInitial = middleInitial != null ? middleInitial : null;
        this.lastName = lastName != null ? lastName : "";
        this.dateOfBirth = dateOfBirth;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        
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
           
}

