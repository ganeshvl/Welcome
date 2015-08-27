package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author edr
 * @since 28 Aug 2012
 */
public  class Job
{
    private final static Joiner FLAGS_JOINER = Joiner.on(", ");


    public static final String FIELD_JOB_ID = "JobID";
    public static final String FIELD_JOB_NUMBER = "JobNumber";
    public static final String FIELD_STAT = "Stat";
    public static final String FIELD_LOCAL_FLAGS = "LocalFlags";

    @JsonProperty(FIELD_JOB_ID)
    public final long id;

    @JsonProperty(FIELD_JOB_NUMBER)
    public final String number;

    @JsonProperty(Encounter.FIELD_ENCOUNTER_ID)
    public final long encounterId;

    @JsonProperty(JobType.SQL_FIELD_ID)
    public final long jobTypeId;

    @JsonProperty(FIELD_STAT)
    public final boolean stat;

    @JsonIgnore
    public int localFlags;

    @JsonIgnore
    public final boolean dirty;

    // this should only be used for JSON deserialization, so don't be dumb here.
    @JsonCreator
    public Job(@JsonProperty(FIELD_JOB_ID) long id,
               @JsonProperty(FIELD_JOB_NUMBER) String number,
               @JsonProperty(Encounter.FIELD_ENCOUNTER_ID) long encounterId,
               @JsonProperty(JobType.SQL_FIELD_ID) long jobTypeId,
               @JsonProperty(FIELD_STAT) boolean stat)
    {
        this(id, number, encounterId, jobTypeId, stat, 0);
    }

    public Job(long id, String number, long encounterId, long jobTypeId,
               boolean stat, int localFlags)
    {
        this.id = id;
        this.number = number;
        this.encounterId = encounterId;
        this.jobTypeId = jobTypeId;
        this.stat = stat;

        this.localFlags = localFlags;
        this.dirty = false;
    }

    private Job(long id, String number, long encounterId, long jobTypeId,
                boolean stat, int localFlags, boolean dirty)
    {
        this.id = id;
        this.number = number;
        this.encounterId = encounterId;
        this.jobTypeId = jobTypeId;
        this.stat = stat;

        this.localFlags = localFlags;
        this.dirty = dirty;
    }

    public boolean isDirty() { return dirty; }

    public boolean isFlagSet(Flags flag)
    {
        return (flag.value & localFlags) == flag.value;
    }
    public Job setFlag(Flags flag)
    {
        int newFlags = localFlags | flag.value;
        return new Job(id, number, encounterId,  jobTypeId,  stat, newFlags, true);
    }
    public Job clearFlag(Flags flag)
    {
        int newFlags = localFlags & (~flag.value);
        return new Job(id, number, encounterId,  jobTypeId,  stat, newFlags, true);
    }

    public Job setFlagToValue(Flags flag, boolean value)
    {
        if (value)
            return setFlag(flag);
        else
            return clearFlag(flag);
    }

    public Job setJobType(JobType jt)
    {
        return new Job(id, number, encounterId, jt.id, stat, localFlags, true);
    }

    public Job setJobTypeId(long jobTypeId)
    {
        return new Job(id, number, encounterId, jobTypeId, stat, localFlags, true);
    }

    public long getJobTypeId()
    {
        return this.jobTypeId;
    }

    public Job setStat(boolean newStat)
    {
        return new Job(id, number, encounterId,  jobTypeId, newStat, localFlags, true);
    }

    @Override
    public boolean equals(Object o)
    {
        return Objects.equal(this, o);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }

    @Override
    public String toString()
    {
        return String.format("job_id %s, dirty %b, encounter_id %d, jobtype_id %d, stat %s, " +
                                     "local_create %b, local_delete %b, local_modify %b, local_clear %b, hold %b",
                             id, dirty, encounterId, jobTypeId, stat,
                             isFlagSet(Flags.LOCALLY_CREATED),
                             isFlagSet(Flags.LOCALLY_DELETED),
                             isFlagSet(Flags.LOCALLY_MODIFIED),
                             isFlagSet(Flags.CLEAR_LOCAL_CHANGES_ON_SYNC),
                             isFlagSet(Flags.HOLD));
    }

    @JsonIgnore
    public String getFlagsString()
    {
        if (this.localFlags != Job.Flags.NONE.value)
        {
            List<String> flags = Lists.newArrayList();

            if (this.isFlagSet(Job.Flags.LOCALLY_CREATED)) flags.add("new");
            if (this.isFlagSet(Job.Flags.LOCALLY_MODIFIED)) flags.add("modified");
            if (this.isFlagSet(Job.Flags.LOCALLY_DELETED)) flags.add("deleted");
            if (this.isFlagSet(Flags.CLEAR_LOCAL_CHANGES_ON_SYNC)) flags.add("clear changes on sync");
            if (this.isFlagSet(Job.Flags.HOLD)) flags.add("held");
            if (this.isFlagSet(Flags.UPLOAD_IN_PROGRESS)) flags.add("uploading");
            if (this.isFlagSet(Flags.UPLOAD_COMPLETED)) flags.add("uploaded");
            if (this.isFlagSet(Flags.UPLOAD_PENDING)) flags.add("upload pending");
            if (this.isFlagSet(Flags.FAILED)) flags.add("upload failed");

            return FLAGS_JOINER.join(flags);
        }

        return "";
    }

    @JsonIgnore
    public boolean isLocallyChanged()
    {
        return isFlagSet(Flags.LOCALLY_CREATED) && isFlagSet(Flags.LOCALLY_DELETED) && isFlagSet(Flags.LOCALLY_MODIFIED);
    }

    @JsonIgnore
    public boolean isComplete()
    {
        return isFlagSet(Flags.UPLOAD_COMPLETED);
    }
    @JsonIgnore
    public boolean isPending()
    {
        return isFlagSet(Flags.UPLOAD_IN_PROGRESS) ||
               isFlagSet(Flags.UPLOAD_PENDING);
    }
    @JsonIgnore
    public boolean isFailed()
    {
        return isFlagSet(Flags.FAILED);
    }

    public static List<Job> sortJobs(Iterable<Job> input) { return sortJobs(input, STANDARD_JOB_COMPARATOR); }
    public static List<Job> sortJobs(Iterable<Job> input, Comparator<Job> comparator)
    {
        List<Job> jobs = Lists.newArrayList(input);

        Collections.sort(jobs, comparator);

        return jobs;
    }

    public static final StandardJobComparator STANDARD_JOB_COMPARATOR = new StandardJobComparator();
    public static class StandardJobComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job first, Job second)
        {
            return first.number.compareTo(second.number);
        }
    }

	public enum Flags {
		NONE(0), LOCALLY_CREATED(1), LOCALLY_DELETED(1 << 1), LOCALLY_MODIFIED(
				1 << 2), CLEAR_LOCAL_CHANGES_ON_SYNC(1 << 3), HOLD(1 << 4), UPLOAD_IN_PROGRESS(
				1 << 5), UPLOAD_COMPLETED(1 << 6), UPLOAD_PENDING(1 << 7), SERVER_HOLD(
				1 << 8), FAILED(1 << 9), IS_FIRST(1 << 10);

        public final int value;

        Flags(int value)
        {
            this.value = value;
        }
    }
}
