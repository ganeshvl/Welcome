package com.entradahealth.entrada.core.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.annotation.CheckForNull;
import java.util.Map;

/**
 * Represents a dictation (of one or more audio chunks).
 *
 * @author edr
 * @since 28 Aug 2012
 */
public final class Dictation
{
    // TODO: differentiation between jobtype and dictationtype
    public static final String FIELD_DICTATIONID = "DictationID";
    public static final String FIELD_JOBNUMBER = "JobID";
    public static final String FIELD_DICTATIONTYPEID = "DictationTypeID";
    public static final String FIELD_DICTATORID = "DictatorID";
    public static final String FIELD_QUEUEID = "QueueID";
    public static final String FIELD_STATUS = "Status";
    public static final String FIELD_DURATION = "Duration";
    public static final String FIELD_MACHINENAME = "MachineName";
    public static final String FIELD_FILENAME = "Filename";
    public static final String FIELD_CLIENTVERSION = "ClientVersion";


    @JsonProperty(FIELD_DICTATIONID)    public final long dictationId;
    @JsonProperty(FIELD_DICTATORID)     public final long dictatorId;
    @JsonProperty(FIELD_JOBNUMBER)      public final long jobId;
    @JsonProperty(FIELD_DICTATIONTYPEID)      public final long dictationTypeId;
    @JsonProperty(FIELD_STATUS)         public final Dictation.Status status; // status = type?
    @JsonProperty(FIELD_DURATION)       public final long duration; // seconds
    @JsonProperty(FIELD_MACHINENAME)    public final String machineName;
    @JsonIgnore                         public final String fileName;
    @JsonProperty(FIELD_QUEUEID)        public final Integer queueId;
    @JsonProperty(FIELD_CLIENTVERSION)  public final String clientVersion;

    @JsonCreator
    public Dictation(
                     @JsonProperty(FIELD_DICTATIONID)   long dictationId,
                     @JsonProperty(FIELD_JOBNUMBER)     long jobId,
                     @JsonProperty(FIELD_DICTATORID)    long dictatorId,
                     @JsonProperty(FIELD_DICTATIONTYPEID)     long dictationTypeId,
                     @JsonProperty(FIELD_STATUS)        int status,
                     @JsonProperty(FIELD_DURATION)      long duration,
                     @JsonProperty(FIELD_MACHINENAME)   String machineName,
                     @JsonProperty(FIELD_FILENAME)      String fileName,
                     @JsonProperty(FIELD_QUEUEID)       Integer queueId,
                     @JsonProperty(FIELD_CLIENTVERSION) String clientVersion)
    {
        this.dictationId = dictationId;
        this.jobId = jobId;
        this.dictatorId = dictatorId;
        this.dictationTypeId = dictationTypeId;
        this.status = Status.getByValue(status);
        this.duration = duration;
        this.machineName = machineName;
        this.fileName = fileName;
        this.queueId = queueId;
        this.clientVersion = clientVersion;
    }

    public Dictation(long dictationId,
                     long jobId,
                     long dictatorId,
                     long dictationTypeId,
                     Status status,
                     long duration,
                     String machineName,
                     String fileName,
                     Integer queueId,
                     String clientVersion)
    {
        this.dictationId = dictationId;
        this.jobId = jobId;
        this.dictatorId = dictatorId;
        this.dictationTypeId = dictationTypeId;
        this.status = status;
        this.duration = duration;
        this.machineName = machineName;
        this.fileName = fileName;
        this.queueId = queueId;
        this.clientVersion = clientVersion;
    }

    public Dictation(long dictationId,
                     long jobId,
                     long dictatorId,
                     long dictationTypeId,
                     long duration,
                     String machineName,
                     String fileName,
                     Integer queueId)
    {
        this.dictationId = dictationId;
        this.jobId = jobId;
        this.dictatorId = dictatorId;
        this.dictationTypeId = dictationTypeId;
        this.status = Status.NONE;
        this.duration = duration;
        this.machineName = machineName;
        this.fileName = fileName;
        this.queueId = queueId;
        this.clientVersion = Dictation.class.getPackage().getImplementationVersion();
    }

    public Dictation setStatus(Status newStatus) {
        return new Dictation(
                this.dictationId,
                this.jobId,
                this.dictatorId,
                this.dictationTypeId,
                newStatus,
                this.duration,
                this.machineName,
                this.fileName,
                this.queueId,
                this.clientVersion);
    }

    public Dictation setDuration(long newDuration) {
        return new Dictation(
                this.dictationId,
                this.jobId,
                this.dictatorId,
                this.dictationTypeId,
                this.status,
                newDuration,
                this.machineName,
                this.fileName,
                this.queueId,
                this.clientVersion);
    }

    @JsonProperty(FIELD_STATUS)
    protected int getStatusForSerialization()
    {
        return status.value;
    }

    @Override
    public boolean equals(Object o)
    {
        return Objects.equal(this, o);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.jobId, this.dictationTypeId, this.dictatorId);
    }



    public static enum Status
    {
        NONE(0),
        /**
         *  This is a job you can dictate
         */
        AVAILABLE(100),
        /**
         * Held on the server (should not be manipulable locally)
         */
        SERVER_HOLD(200),
        /**
         * It has been dictated, which means the background uploader will upload soon
         */
        DICTATED(250),
        /**
         * Done uploading
         */
        COMPLETED(300),
        /*
         *
         */
        ON_SERVER(375),
        /*
         *
         */
        SENT(400),
        /**
         * ummm, deleted. On the server we don't delete the row, just change the status
         */
        DELETED(500);

        public final int value;

        private Status(int value)
        {
            this.value = value;
        }


        private static final Map<Integer, Status> _lookupTable;
        static
        {
            Status[] values = Status.values();
            _lookupTable = Maps.newHashMapWithExpectedSize(values.length);

            for (Status s : values)
            {
                _lookupTable.put(s.value, s);
            }
        }

        /**
         * The enum values in Dictation.Status are consistent with the other Entrada
         * apps, and ints will be passed around through the JSON. This performs a
         * reverse lookup to get the Status enum out of it.
         * @param value an integer corresponding to a Status.value
         * @return the corresponding Status object, or null if not found
         */
        @CheckForNull
        public static Status getByValue(int value)
        {
            return _lookupTable.get(value);
        }
    }
}
