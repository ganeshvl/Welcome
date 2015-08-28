package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DictatorInfo
{
    public static final String JSON_DEFAULT_JOBTYPE_ID = "DefaultJobTypeID";
    public static final String JSON_DEFAULT_QUEUE_ID = "DefaultQueueID";
    
    @JsonProperty(JSON_DEFAULT_JOBTYPE_ID)
    public final int defaultJobTypeID;

    @JsonProperty(JSON_DEFAULT_QUEUE_ID)
    public final int defaultQueueID;
    
    @JsonCreator
    public DictatorInfo(@JsonProperty(JSON_DEFAULT_JOBTYPE_ID) int defaultJobTypeID, @JsonProperty(JSON_DEFAULT_QUEUE_ID) int defaultQueueID)
    {
        this.defaultJobTypeID = defaultJobTypeID;
        this.defaultQueueID = defaultQueueID;
    }
}
