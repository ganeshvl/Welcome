package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferringPhysicians {

	public static final String FIELD_JOBID = "JobID";
    public static final String FIELD_REFERRINGID = "ReferringID";
    
    public final long jobid;
    public final long refid;
    
    @JsonCreator
    public ReferringPhysicians(@JsonProperty(FIELD_REFERRINGID) long rid,
    						@JsonProperty(FIELD_JOBID) long jid
    					)
    {
    	this.jobid = jid;
    	this.refid = rid;
    }
}
