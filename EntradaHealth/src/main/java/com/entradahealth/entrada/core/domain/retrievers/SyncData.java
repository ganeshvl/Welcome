package com.entradahealth.entrada.core.domain.retrievers;

import com.entradahealth.entrada.core.domain.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Stupid boilerplatey class for encapsulating the return results
 * from a RemoteService.
 *
 * @author edr
 * @since 10 Sep 2012
 */
public class SyncData
{
    @JsonProperty("Encounters")
    public final List<Encounter> encounters;
    @JsonProperty("Jobs")
    public final List<Job> jobs;
    @JsonProperty("JobTypes")
    public final ImmutableList<JobType> jobTypes;
    @JsonProperty("Queues")
    public final ImmutableList<Queue> queues;
    @JsonProperty("ExpressNotesTags")
    public final ImmutableList<ExpressNotesTags> expressNotesTags;
    @JsonProperty("Patients")
    public final ImmutableList<Patient> patients;
    @JsonProperty("Dictations")
    public final ImmutableList<Dictation> dictations;
    @JsonProperty("Physicians")
    public final ImmutableList<Physicians> physicians;
    @JsonProperty("ReferringPhysicians")
    public final ImmutableList<ReferringPhysicians> referringPhysicians;
    @JsonProperty("Dictator")
    public final DictatorInfo dictatorInfo;
    @JsonProperty("SystemSettings")
    public final SystemSettingsInfo systemSettings;

    @JsonCreator
    @SuppressWarnings("unchecked") // because I am That Guy
    public SyncData(
            @JsonProperty("Encounters") List<Encounter> encounters,
            @JsonProperty("Jobs") List<Job> jobs,
            @JsonProperty("Dictations") List<Dictation> dictations,
            @JsonProperty("JobTypes") List<JobType> jobTypes,
            @JsonProperty("ExpressNotesTags") List<ExpressNotesTags> expressNotesTags,
            @JsonProperty("Queues") List<Queue> queues,
            @JsonProperty("Patients") List<Patient> patients,
            @JsonProperty("Physicians") List<Physicians> physicians,
            @JsonProperty("ReferringPhysicians") List<ReferringPhysicians> referringPhysicians,
            @JsonProperty("Dictator") DictatorInfo dictatorInfo,
            @JsonProperty("SystemSettings") SystemSettingsInfo systemSettings)
    {
        // note for clarity: you would think that blindly copyOf'ing the list
        // would be bad - ImmutableList implements List, so we could be creating
        // a double copy. Guava, however, has smart in it and will shortcut the
        // method if the passed-in type implements ImmutableCollection.
        //
        // the_more_you_know.jpg

        this.encounters = encounters;//ImmutableList.copyOf(encounters);
        this.jobs = jobs;//ImmutableList.copyOf(jobs);
        this.dictations = ImmutableList.copyOf(dictations);
        this.jobTypes = ImmutableList.copyOf(jobTypes);
        this.expressNotesTags = ImmutableList.copyOf(expressNotesTags);
        this.queues = ImmutableList.copyOf(queues);
        this.patients = ImmutableList.copyOf(patients);
        this.physicians = ImmutableList.copyOf(physicians);
        this.referringPhysicians= ImmutableList.copyOf(referringPhysicians);
        this.dictatorInfo = dictatorInfo;
        this.systemSettings = systemSettings;
    }
}
