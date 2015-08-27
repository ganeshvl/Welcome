package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpressNotesTags implements Comparable<ExpressNotesTags> {
	
	public static final String JSON_FIELD_ID = "JobTypeID";
	public static final String JSON_FIELD_NAME = "Name";//

	public static final String SQL_FIELD_JID = "ID";
    public static final String SQL_FIELD_ID = "JobTypeID";
	public static final String SQL_FIELD_NAME = "Name";//
    public static final String SQL_FIELD_REQUIRED = "Required";

    public final long id;
    public final String name;
    public final String required;

    @JsonCreator
    public ExpressNotesTags(@JsonProperty(JSON_FIELD_ID) long id,
                   @JsonProperty(SQL_FIELD_NAME) String name,
			@JsonProperty(SQL_FIELD_REQUIRED) String required) {
        this.id = id;
        this.name = name;
		this.required = required;
    }
    	
	@Override
	public int compareTo(ExpressNotesTags another) {
		// TODO Auto-generated method stub
		return 0;
	}

}
