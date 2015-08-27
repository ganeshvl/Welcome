package com.entradahealth.entrada.core.domain.providers;

import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;

/**
 * Helper class to sanity-check the contents of a provider. Primarily
 * for test classes, I think, but maybe it gets used after a service
 * sync.
 *
 * This would be an extension method if this was C#. Sadface.
 *
 * @author edr
 * @since 30 Aug 2012
 */
public class ReaderSanityCheck
{
    private ReaderSanityCheck() { }

    public static boolean check(DomainObjectReader provider)
    {
        // TODO: find a good way of expressing sanity failures.
        // TODO: logs? exceptions? need to investigate Android-y bits more.
        for (Encounter e : provider.getEncounters())
        {
            if (provider.getPatient(e.patientId) == null)
                return false; // TODO: explain failure
        }

        for (Job j : provider.getJobs())
        {
            if (provider.getEncounter(j.encounterId) == null)
                return false; // TODO: explain failure
            if (provider.getJobType(j.jobTypeId) == null)
                return false; // TODO: explain failure
        }

        for (Dictation d : provider.getDictations())
        {
            if (provider.getJob(d.jobId) == null)
                return false; // TODO: explain failure
        }

        return true;
    }
}
