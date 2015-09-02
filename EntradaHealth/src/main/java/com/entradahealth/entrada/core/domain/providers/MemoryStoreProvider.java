package com.entradahealth.entrada.core.domain.providers;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;

import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.core.domain.*;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.googlecode.androidannotations.annotations.EProvider;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This provider provides (gasp) an in-memory store of domain objects and can be
 * used for test, etc. It will not sanity-check the data passed-in, but you can
 * call ReaderSanityCheck.check() for that.
 * 
 * This is just a bunch of dumb maps and loops for now; it's not intended to be
 * production code. It quite happily does big ol' copies all over the place and
 * boxes all your ints.
 * 
 * 12 Nov 2012: This now serializes down to JSON
 * 
 * @author edr
 * @since 30 Aug 2012
 */
public class MemoryStoreProvider implements DomainObjectReader,
		DomainObjectWriter {
	private final Multimap<Long, Dictation> _dictations;
	private final HashMap<Long, Encounter> _encounters;
	private final HashMap<Long, Job> _jobs;
	private final HashMap<Long, JobType> _jobTypes;
	private final HashMap<Long, ExpressNotesTags> _tags;
	private final HashMap<Long, Patient> _patients;
	private final HashMap<Long, Queue> _queues;

	public MemoryStoreProvider(List<Dictation> dictations,
			List<Encounter> encounters, List<Job> jobs, List<JobType> jobTypes,
			List<ExpressNotesTags> tags, List<Patient> patients, List<Queue> queues) {
		_queues = Maps.newHashMapWithExpectedSize(queues.size());
		for (Queue q : queues)
			_queues.put(q.id, q);

		_patients = Maps.newHashMapWithExpectedSize(patients.size());
		for (Patient p : patients)
			_patients.put(p.id, p);

		_jobTypes = Maps.newHashMapWithExpectedSize(jobTypes.size());
		for (JobType jt : jobTypes)
			_jobTypes.put(jt.id, jt);
		
		_tags = Maps.newHashMapWithExpectedSize(tags.size());
		for (ExpressNotesTags tag : tags)
			_tags.put(tag.id, tag);

		_jobs = Maps.newHashMapWithExpectedSize(jobs.size());
		for (Job j : jobs)
			_jobs.put(j.id, j);

		_encounters = Maps.newHashMapWithExpectedSize(encounters.size());
		for (Encounter e : encounters)
			_encounters.put(e.id, e);

		_dictations = ArrayListMultimap.create();
		for (Dictation d : dictations)
			_dictations.put(d.jobId, d);
	}

	@Override
	public ImmutableList<Dictation> getDictations() {
		return ImmutableList.copyOf(_dictations.values());
	}

	@Override
	public ImmutableList<Dictation> getDictationsByJob(Long jobId) {
		return ImmutableList.copyOf(_dictations.get(jobId));
	}

	@Override
	@CheckForNull
	public Encounter getEncounter(long id) {
		return _encounters.get(id);
	}

	@Override
	public ImmutableList<Encounter> getEncounters() {
		return ImmutableList.copyOf(_encounters.values());
	}

	@Override
	public ImmutableList<Encounter> getEncountersByPatient(final long patientId) {
		return ImmutableList.copyOf(Iterables.filter(_encounters.values(),
				new Predicate<Encounter>() { // boy howdy I sure don't miss
												// lambdas, not one bit
					@Override
					public boolean apply(@Nullable Encounter e) {
						return e != null && e.patientId == patientId;
					}
				}));
	}

	@Override
	@CheckForNull
	public Job getJob(long jobId) {
		return _jobs.get(jobId);
	}

	@Override
	public ImmutableList<Job> getJobs() {
		return ImmutableList.copyOf(_jobs.values());
	}

	@Override
	public ImmutableList<Job> getJobsByEncounter(final long encounterId) {
		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && j.encounterId == encounterId;
					}
				}));
	}

	@Override
	public ImmutableList<Job> getJobsByJobType(final long jobTypeId) {
		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && j.jobTypeId == jobTypeId;
					}
				}));
	}

	@Override
	public ImmutableList<Job> getJobsByStat(final boolean stat) {
		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && j.stat == stat;
					}
				}));
	}

	@Override
	public ImmutableList<Job> getJobsByAllFlags(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && (j.localFlags | flags) == flags;
					}
				}));
	}

	@Override
	public ImmutableList<Job> getJobsByAnyFlags(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && (j.localFlags | flags) != 0;
					}
				}));
	}

	@Override
	public List<Job> searchJobs(String searchText) {
		final Iterable<String> tokens = Providers.SEARCH_TEXT_SPLITTER
				.split(searchText);
		final ImmutableSet<Patient> patients = searchPatients(searchText);

		return ImmutableList.copyOf(Iterables.filter(_jobs.values(),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job job) {
						if (job == null)
							return false;

						Encounter enc = getEncounter(job.encounterId);
						Patient patient = getPatient(enc.getPatientId());

						return patients.contains(patient);
					}
				}));

	}

	@Override
	@CheckForNull
	public JobType getJobType(final long id) {
		return _jobTypes.get(id);
	}

	@Override
	public ImmutableList<JobType> getJobTypes() {
		return ImmutableList.copyOf(_jobTypes.values());
	}

	@Override
	@CheckForNull
	public Patient getPatient(final long id) {
		return _patients.get(id);
	}

	@Override
	@CheckForNull
	public Patient getPatientByMRN(final String medicalRecordNumber) {
		for (Patient p : _patients.values()) {
			if (p.medicalRecordNumber.equals(medicalRecordNumber))
				return p;
		}
		return null;
	}

	@Override
	public ImmutableList<Patient> getPatients() {
		return ImmutableList.copyOf(_patients.values());
	}

	@Override
	public ImmutableSet<Patient> searchPatients(final String searchText) {
		if (Strings.isNullOrEmpty(searchText))
			return ImmutableSet.of();
		final Iterable<String> tokens = Providers.SEARCH_TEXT_SPLITTER
				.split(searchText.toLowerCase());
		final ImmutableSet.Builder<Patient> builder = ImmutableSet.builder();

		for (Patient p : _patients.values()) {
			final String mrn = (p.medicalRecordNumber != null) ? p.medicalRecordNumber
					.toLowerCase() : null;
			final String first = (p.firstName != null) ? p.firstName
					.toLowerCase() : null;
			final String last = (p.lastName != null) ? p.lastName.toLowerCase()
					: null;

			for (String t : tokens) {
				if ((mrn != null && mrn.contains(t))
						|| (first != null && first.contains(t))
						|| (last != null && last.contains(t))) {
					builder.add(p);
					break;
				}
			}
		}

		return builder.build();
	}

	@Override
	@CheckForNull
	public Queue getQueue(final long id) {
		return _queues.get(id);
	}

	@Override
	public ImmutableList<Queue> getQueues() {
		return ImmutableList.copyOf(_queues.values());
	}

	@Override
	public void writeDictation(Dictation dictation)
			throws DomainObjectWriteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeDictations(Iterable<Dictation> dicts)
			throws DomainObjectWriteException {
		for (Dictation d : dicts) {
			this.writeDictation(d);
		}
	}

	@Override
	public void writeEncounter(Encounter encounter)
			throws DomainObjectWriteException {
		_encounters.put(encounter.id, encounter);
	}

	@Override
	public void writeEncounters(Iterable<Encounter> encs)
			throws DomainObjectWriteException {
		for (Encounter e : encs)
			writeEncounter(e);
	}

	@Override
	public Encounter createNewEncounter(Patient patient)
			throws DomainObjectWriteException {
		long eID = Providers.makeTempEncounterId();
		long pID = patient.id;
		DateTime date = DateTime.now();
		String attending = "TEMPORARY";

		Encounter enc = new Encounter(eID, date, pID, attending);
		writeEncounter(enc);

		return enc;
	}

	@Override
	public void writeJob(Job job) throws DomainObjectWriteException {
		_jobs.put(job.id, job);
	}

	@Override
	public void writeJobs(Iterable<Job> jobs) throws DomainObjectWriteException {
		for (Job j : jobs)
			writeJob(j);
	}

	@Override
	public void writePatient(Patient patient) throws DomainObjectWriteException {
		_patients.put(patient.id, patient);
	}

	@Override
	public void writePatients(Iterable<Patient> patients)
			throws DomainObjectWriteException {
		for (Patient p : patients)
			writePatient(p);
	}

	@Override
	public void writeQueue(Queue queue) throws DomainObjectWriteException {
		_queues.put(queue.id, queue);
	}

	@Override
	public void writeQueues(Iterable<Queue> queues)
			throws DomainObjectWriteException {
		for (Queue q : queues)
			writeQueue(q);
	}

	@Override
	public void writeSyncData(SyncData data) throws DomainObjectWriteException {
		clearReadonlyItems();

		writeQueues(data.queues);
		writeJobTypes(data.jobTypes);
		writePatients(data.patients);
		writeEncounters(data.encounters);
		writeJobs(data.jobs);

		syncFixups();
	}

	/**
	 * Runs after all data is initially written to the database to perform
	 * cross-checking and sanity checks.
	 */
	private void syncFixups() {
		for (Dictation d : getDictations()) {
			if (d.status == Dictation.Status.SERVER_HOLD) {
				try {
					writeJob(getJob(d.jobId).setFlag(Job.Flags.SERVER_HOLD));
				} catch (Exception ex) {
					throw new RuntimeException("Failure in fixups", ex);
				}
			}
		}
	}

	@Override
	public void clearReadonlyItems() throws DomainObjectWriteException {
		_encounters.clear();
		_jobs.clear();
		_patients.clear();
		_queues.clear();
	}

	@Override
	public void clearJobs() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		_jobs.clear();
		_patients.clear();
	}

	@Override
	public void writeJobType(JobType jt) throws DomainObjectWriteException {
		_jobTypes.put(jt.id, jt);
	}

	@Override
	public void writeJobTypes(Iterable<JobType> jts)
			throws DomainObjectWriteException {
		for (JobType jt : jts)
			writeJobType(jt);
	}

	@Override
	public Job updateJob(Job newJob) throws DomainObjectWriteException {
		_jobs.put(newJob.id, newJob);
		return newJob;
	}

	@Override
	public Job createNewJob(Encounter encounter)
			throws DomainObjectWriteException {
		String jNumber = Providers.makeTempJobNumber();
		long eID = encounter.id;
		// TODO: replace with default job type ID
		long jobTypeId = getJobTypes().get(1).id;
		Integer queueId = null;
		boolean stat = false;

		Job j = new Job(Long.valueOf(jNumber), jNumber, eID, jobTypeId, stat);
		j.setFlag(Job.Flags.LOCALLY_CREATED);
		writeJob(j);
		return j;
	}

	@JsonProperty("dictations")
	protected Collection<Dictation> getDictationsForSerialization() {
		return _dictations.values();
	}

	@JsonProperty("encounters")
	protected Collection<Encounter> getEncountersForSerialization() {
		return _encounters.values();
	}

	@JsonProperty("jobs")
	protected Collection<Job> getJobsForSerialization() {
		return _jobs.values();
	}

	@JsonProperty("job_types")
	protected Collection<JobType> getJobTypesForSerialization() {
		return _jobTypes.values();
	}

	@JsonProperty("patients")
	protected Collection<Patient> getPatientsForSerialization() {
		return _patients.values();
	}

	@JsonProperty("queues")
	protected Collection<Queue> getQueuesForSerialization() {
		return _queues.values();
	}

	@Override
	public Encounter createNewEncounter(String patient)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		long eID = 0, pID = 0;

		try {
			JSONObject jpatient = new JSONObject(patient);
			for (int i = 0; i < jpatient.length(); i++) {

				eID = jpatient.getLong("EncounterID");
				pID = jpatient.getLong("PatientID");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DateTime date = DateTime.now();
		String attending = "TEMPORARY";

		Encounter enc = new Encounter(eID, date, pID, attending);
		writeEncounter(enc);

		return enc;
	}

	@Override
	public Job createNewJob(String j) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		boolean stat = false;
		long JobID = 0, EncounterID = 0, JobTypeID = 0;
		String JobNumber = null;

		try {
			JSONObject jjob = new JSONObject(j);
			for (int i = 0; i < jjob.length(); i++) {
				JobID = jjob.getLong("JobID");
				JobNumber = jjob.getString("JobNumber");
				EncounterID = jjob.getLong("EncounterID");
				JobTypeID = jjob.getLong("JobTypeID");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Integer queueId = null;

		Job job = new Job(JobID, JobNumber, EncounterID, JobTypeID, stat)
				.setFlag(Job.Flags.LOCALLY_CREATED);
		writeJob(job);

		return job;
	}

	@Override
	public List<Job> getDeletedJobs(int flags) {
		// TODO Auto-generated method stub
		return ImmutableList.of();
	}

	@Override
	public List<Job> getCompletedJobs(int flags) {
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
	public void updatePasscode(int code) throws DomainObjectWriteException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPasscode() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void createGroupQueues(List<String> list_queues)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getGroupQueues(int grpId) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet getGroupName() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateGroupName(String grpName, String grpId)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertGroupName(String grpName)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertFavGroupName(String grpName)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFavGroupName(String grpId, String grpName)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getQueueCount(int qId) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void updateGroupQueues(List<String> list_queues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFavGroups(int grpId) {
		// TODO Auto-generated method stub
		
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
		return ImmutableList.copyOf(_tags.values());
	}

	@Override
	public ExpressNotesTags getExpressNotesTags(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writePhysicians(Iterable<Physicians> physician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeReferringPhysicians(
			Iterable<ReferringPhysicians> referringPhysician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePhysician(Physicians physician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeReferringPhysician(ReferringPhysicians referrringPhysician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
	public void addUser(EUser user) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDictator(Dictator dictator, String user)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
	public void updateDictator(Dictator dictator, String user)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUser(EUser user) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
	public void addResources(Resource resource)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Resource> getResources() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteResources() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}
	
	public void deleteDictator(Dictator dictator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDictatorName(long dictatorId, String dictatorName)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void scheduleInsertUpdate(Schedule schedule) {
		// TODO Auto-generated method stub
		
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

	@Override
	public JobType getDefaultGenericJobType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JobType> getDefaultGenericJobTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExistsInDefaultGenericJobTypes(Long jobTypeId) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
