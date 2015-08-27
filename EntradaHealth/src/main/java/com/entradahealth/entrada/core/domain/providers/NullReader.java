package com.entradahealth.entrada.core.domain.providers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.core.domain.*;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;

import javax.annotation.CheckForNull;

/**
 * A completely empty provider that can never return data objects.
 * All methods return null (for methods that return a single object)
 * or an empty list.
 *
 * @author edr
 * @since 30 Aug 2012
 */
public class NullReader implements DomainObjectReader
{
    @Override
    public ImmutableList<Dictation> getDictations()
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Dictation> getDictationsByJob(Long jobId)
    {
        return ImmutableList.of();
    }

    @Override
    @CheckForNull
    public Encounter getEncounter(long id)
    {
        return null;
    }

    @Override
    public ImmutableList<Encounter> getEncounters()
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Encounter> getEncountersByPatient(final long patientId)
    {
        return ImmutableList.of();
    }

    @Override
    @CheckForNull
    public Job getJob(long jobId)
    {
        return null;
    }

    @Override
    public ImmutableList<Job> getJobs()
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> getJobsByEncounter(long encounterId)
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> getJobsByJobType(long jobTypeId)
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> getJobsByStat(boolean stat)
    {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> getJobsByAllFlags(int flags) {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> getJobsByAnyFlags(int flags) {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<Job> searchJobs(String searchText)
    {
        return ImmutableList.of();
    }

    @Override
    @CheckForNull
    public JobType getJobType(long id)
    {
        return null;
    }

    @Override
    public ImmutableList<JobType> getJobTypes()
    {
        return ImmutableList.of();
    }

    @Override
    @CheckForNull
    public Patient getPatient(long id)
    {
        return null;
    }

    @Override
    @CheckForNull
    public Patient getPatientByMRN(String medicalRecordNumber)
    {
        return null;
    }

    @Override
    public ImmutableSet<Patient> searchPatients(String searchText)
    {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableList<Patient> getPatients()
    {
        return ImmutableList.of();
    }

    @Override
    @CheckForNull
    public Queue getQueue(long id)
    {
        return null;
    }

    @Override
    public ImmutableList<Queue> getQueues()
    {
        return ImmutableList.of();
    }

	@Override
	public List<Job> getDeletedJobs(int flags) {
		// TODO Auto-generated method stub
		return ImmutableList.of();
	}

	@Override
	public ImmutableList<Job> getCompletedJobs(int flags) {
		// TODO Auto-generated method stub
		return ImmutableList.of();
	}

	@Override
	public List<Job> getHeldJobs(int flags) {
		// TODO Auto-generated method stub
		return ImmutableList.of();
	}

	@Override
	public List<Job> getLocalJobs(int flags) {
		// TODO Auto-generated method stub
		return ImmutableList.of();
	}

	@Override
	public void writeExpressNotesTags(Iterable<ExpressNotesTags> tags)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeExpressNotesTags(ExpressNotesTags tag)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImmutableList<ExpressNotesTags> getExpressNotesTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExpressNotesTags getExpressNotesTags(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getReferringPhysicians(long jobId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImmutableList<Physicians> getPhysicians(long refId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Patient getPatientDemographicInfo(long patientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EUser> getEUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Dictator> getDictatorsForUser(String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EUser getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserExists(EUser user) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Dictator getCurrentDictatorForUser(String user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Schedule> getRaawScheduleDetailsList(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Schedule> searchSchedules(String searchText) {
		// TODO Auto-generated method stub
		return null;
	}
}
