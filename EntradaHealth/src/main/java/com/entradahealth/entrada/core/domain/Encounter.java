package com.entradahealth.entrada.core.domain;

import android.util.Log;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The top-level domain object for a single "event" in the Entrada
 * system.
 *
 * @author edr
 * @since 28 Aug 2012
 */
public class Encounter
{
    public static final String FIELD_ENCOUNTER_ID = "EncounterID";

    public static final String JSON_FIELD_APPOINTMENT_DATE = "AppointmentDate";
    public static final String SQL_FIELD_APPOINTMENT_DATE = "ApptDate";

    public static final String FIELD_PATIENT_ID = "PatientID";
    public static final String FIELD_ATTENDING = "Attending";


    @JsonIgnore public final long id;
    @JsonIgnore public final DateTime appointmentDate;
    @JsonIgnore public final long patientId;
    @JsonIgnore public final String attending;

    @JsonCreator
    public Encounter(@JsonProperty(FIELD_ENCOUNTER_ID) Long id,
                     @JsonProperty(JSON_FIELD_APPOINTMENT_DATE) String apptDate,
                     @JsonProperty(FIELD_PATIENT_ID) Long patientId,
                     @JsonProperty(FIELD_ATTENDING) String attending)
    {
        if (id == null)
        {
            Log.e("Entrada-EncounterDeserialize", "null id - wtf?");
        }
        else if (patientId == null)
        {
            Log.e("Entrada-EncounterDeserialize", "null patientId for id " + id);
        }

        try
        {
        	
        	this.appointmentDate = DateTime.parse(apptDate,
                                            ISODateTimeFormat.dateOptionalTimeParser());
        }
        catch (NullPointerException e)
        {
            throw e;
        }

        this.id = id;

        this.patientId = patientId;
        this.attending = attending; // optional, but null == "none given" in our world
    }

    public Encounter(long id, DateTime date, long patientId, String attending)
    {
        this.id = id;
        this.appointmentDate = date;
        this.patientId = patientId;
        this.attending = attending;
    }

    @JsonProperty(FIELD_ENCOUNTER_ID)
    public long getId()
    {
        return id;
    }

    @JsonIgnore
    public DateTime getAppointmentDate()
    {
        return appointmentDate;
    }
    @JsonProperty(JSON_FIELD_APPOINTMENT_DATE)
    protected String getAppointmentDateString()
    {
        return appointmentDate.toString();
    }

    @JsonProperty(FIELD_PATIENT_ID)
    public long getPatientId()
    {
        return patientId;
    }

    @JsonProperty(FIELD_ATTENDING)
    public String getAttending()
    {
        return attending;
    }


    @JsonIgnore
    public String getDateTimeText()
    {
        //return appointmentDate.toString("EEE MMM dd @ HH:mm aa");
    	return appointmentDate.toString("MM-dd-yyyy h:mm aa");
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Encounter encounter = (Encounter) o;

        if (id != encounter.id)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }
}
