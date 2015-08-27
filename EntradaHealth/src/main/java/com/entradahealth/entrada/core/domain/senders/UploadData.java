package com.entradahealth.entrada.core.domain.senders;

import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Job;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 1/3/13
 */
public class UploadData {
    public final ImmutableList<Job> jobs;
    public final ImmutableList<Dictation> dictations;

    private static final ObjectMapper mapper = new ObjectMapper();
    static
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public UploadData(List<Job> jobsToSync, List<Dictation> dictationsToSync) {
        jobs = ImmutableList.copyOf(jobsToSync);
        if (dictationsToSync != null)
            dictations = ImmutableList.copyOf(dictationsToSync);
        else
            dictations = ImmutableList.of();
    }

    @JsonProperty("Jobs")
    public List<Job> getJobs() {
        return this.jobs;
    }


    @JsonProperty("Dictations")
    public List<Dictation> getDictations() {
        return this.dictations;
    }

    // TODO: This is an ugly hack that lets UploadData JSONify its own jobs and dictations. Remove once we have atomic sync.
    /*public String toJsonString(Object t) {
        try {
            return mapper.writeValueAsString(t);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }*/

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
