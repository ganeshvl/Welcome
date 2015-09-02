package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A not-enum that maps job type IDs to fluff text. Job.jobType maps
 * to JobType.id.
 *
 * @author edr
 * @since 28 Aug 2012
 */
public final class JobType implements Comparable<JobType> {
	public static final String JSON_FIELD_ID = "JobTypeID";
	public static final String JSON_FIELD_DISABLE = "DisableGenericJobs";//

    public static final String SQL_FIELD_ID = "JobTypeID";
	public static final String SQL_FIELD_DISABLE = "DisableGenericJobs";//
    public static final String SQL_FIELD_NAME = "Name";

    public final long id;
    public final String name;
    public final String disable;

    @JsonCreator
    public JobType(@JsonProperty(JSON_FIELD_ID) long id,
                   @JsonProperty(SQL_FIELD_NAME) String name,
			@JsonProperty(SQL_FIELD_DISABLE) String disable) {
        this.id = id;
        this.name = name;
		this.disable = disable;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JobType jobType = (JobType) o;

        if (id != jobType.id)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int compareTo(JobType jobType)
    {
        return this.name.compareToIgnoreCase(jobType.name);
    }
}
