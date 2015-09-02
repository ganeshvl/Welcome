package com.entradahealth.entrada.core.domain.providers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.ExpressNotesTags;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Physicians;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.ReferringPhysicians;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * An interface for reading domain objects on the device. Also
 * handles syncing tasks (including flushing the database) where
 * appropriate.
 *
 * No objects should be cached outside of the Provider class; if
 * an Encounter is selected in one activity, its id should be
 * passed to the next activity rather than the Encounter object.
 * This is less than ideal, but will preserve coherency and avoid
 * weird states if caught mid-sync or after a pause/resume.
 *
 * @author edr
 * @since 29 Aug 2012
 */
public interface DomainObjectReader {
	// TODO: Convert DomainObjectReader API to ImmutableSets.
	// I just (19 Nov 2012) added equals and hashCode implementations
	// to every domain object. As such, we should remplace all these
	// ImmutableLists with ImmutableSets, as that improves the code
	// contract.

	ImmutableList<Dictation> getDictations();

	ImmutableList<Dictation> getDictationsByJob(final Long jobId);

	@CheckForNull
	Encounter getEncounter(final long id);

	List<Encounter> getEncounters();

	List<Encounter> getEncountersByPatient(final long patientId);

	@CheckForNull
	Job getJob(final long jobId);

	List<Job> getJobs();

	List<Job> getJobsByEncounter(final long encounterId);

	List<Job> getJobsByJobType(final long jobTypeId);

	List<Job> getJobsByStat(final boolean stat);

	List<Job> getJobsByAllFlags(final int flags);

	List<Job> getJobsByAnyFlags(final int flags);

	List<Job> getDeletedJobs(int flags);

	/**
	 * Searches the domain space for jobs that match the patient MRN or patient
	 * name. Result sorted via the default sort (Job.StandardComparator).
	 */
	List<Job> searchJobs(final String searchText);

	@CheckForNull
	JobType getJobType(final long id);

	ImmutableList<JobType> getJobTypes();
	
	ImmutableList<ExpressNotesTags> getExpressNotesTags();

	@CheckForNull
	Patient getPatient(final long id);

	@CheckForNull
	Patient getPatientByMRN(final String medicalRecordNumber);

	ImmutableList<Patient> getPatients();

	/**
	 * Searches the domain space for patients whose MRNs, first names, or last
	 * names match any of the words in the search text. This is an unordered
	 * set. An empty search string returns no results.
	 * 
	 * @param searchText
	 *            The text (including spaces) to search for.
	 * @return A set of patients who match the search text.
	 */
	ImmutableSet<Patient> searchPatients(final String searchText);

	@CheckForNull
	Queue getQueue(final long id);

	ImmutableList<Queue> getQueues();

	List<Job> getCompletedJobs(int flags);
	List<Job> getHeldJobs(int flags);
	List<Job> getLocalJobs(int flags);

	void writeExpressNotesTags(Iterable<ExpressNotesTags> tags)
			throws DomainObjectWriteException;

	void writeExpressNotesTags(ExpressNotesTags tag)
			throws DomainObjectWriteException;

	ExpressNotesTags getExpressNotesTags(long id);

	long getReferringPhysicians(long jobId);

	ImmutableList<Physicians> getPhysicians(long refId);

	Patient getPatientDemographicInfo(long patientId);

	List<EUser> getEUsers();

	List<Dictator> getDictatorsForUser(String user);

	EUser getCurrentUser();

	boolean isUserExists(EUser user);

	Dictator getCurrentDictatorForUser(String user);

	ArrayList<Schedule> getRaawScheduleDetailsList(String query);

	ArrayList<Schedule> searchSchedules(String searchText);

	JobType getDefaultGenericJobType();

	List<JobType> getDefaultGenericJobTypes();

	boolean isExistsInDefaultGenericJobTypes(Long jobTypeId);
	
}
