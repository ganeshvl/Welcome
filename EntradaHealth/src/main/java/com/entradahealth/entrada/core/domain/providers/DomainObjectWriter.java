package com.entradahealth.entrada.core.domain.providers;

import java.sql.ResultSet;
import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.domain.*;

/**
 * An interface for writing objects to whatever backing store is
 * currently most considered awesome.
 *
 * @author edr
 * @since 5 Sep 2012
 */
public interface DomainObjectWriter {
	void writeDictation(Dictation dictation) throws DomainObjectWriteException;

	void writeDictations(Iterable<Dictation> dicts)
			throws DomainObjectWriteException;

	void writeEncounter(Encounter encounter) throws DomainObjectWriteException;

	void writeEncounters(Iterable<Encounter> encs)
			throws DomainObjectWriteException;

	Encounter createNewEncounter(Patient patient)
			throws DomainObjectWriteException;

	Encounter createNewEncounter(String patient)
			throws DomainObjectWriteException;

	void writeJob(Job job) throws DomainObjectWriteException;

	void writeJobs(Iterable<Job> jobs) throws DomainObjectWriteException;

	void writeJobType(JobType jt) throws DomainObjectWriteException;

	void writeJobTypes(Iterable<JobType> jts) throws DomainObjectWriteException;

	Job updateJob(Job newJob) throws DomainObjectWriteException;

	Job createNewJob(Encounter encounter) throws DomainObjectWriteException;

	Job createNewJob(String job) throws DomainObjectWriteException;

	void writePatient(Patient patient) throws DomainObjectWriteException;

	void writePatients(Iterable<Patient> patients)
			throws DomainObjectWriteException;

	void writeQueue(Queue queue) throws DomainObjectWriteException;

	void writeQueues(Iterable<Queue> queues) throws DomainObjectWriteException;

	void writeSyncData(SyncData data) throws DomainObjectWriteException;

	/**
	 * Clears all encounters, jobs, jobtypes, patients, and queues from the
	 * device.
	 * 
	 * @throws DomainObjectWriteException
	 */
	// TODO: figure out how we want to handle clearing dictations
	// because Dictations might need to persist, but also reference Jobs.
	void clearReadonlyItems() throws DomainObjectWriteException;

	void clearJobs() throws DomainObjectWriteException;

	void updatePasscode(int code) throws DomainObjectWriteException;
	int getPasscode() throws DomainObjectWriteException;
	void createGroupQueues(List<String> list_queues)
			throws DomainObjectWriteException;

	List<String> getGroupQueues(int grpId) throws DomainObjectWriteException;

	ResultSet getGroupName() throws DomainObjectWriteException;

	void updateGroupName(String grpName, String grpId) throws DomainObjectWriteException;

	void insertGroupName(String grpName) throws DomainObjectWriteException;

	void insertFavGroupName(String grpName) throws DomainObjectWriteException;

	void updateFavGroupName(String grpId, String grpName)
			throws DomainObjectWriteException;

	int getQueueCount(int qId) throws DomainObjectWriteException;

	void updateGroupQueues(List<String> list_queues);
	
	void deleteFavGroups(int grpId);
	
	void writePhysicians(Iterable<Physicians> physician) throws DomainObjectWriteException;
	
	void writeReferringPhysicians(Iterable<ReferringPhysicians> referringPhysician) throws DomainObjectWriteException;
	
	void writePhysician(Physicians physician) throws DomainObjectWriteException;
	
	void writeReferringPhysician(ReferringPhysicians referrringPhysician) throws DomainObjectWriteException;

	void addUser(EUser user) throws DomainObjectWriteException;

	void addDictator(Dictator dictator, String user) throws DomainObjectWriteException;

	void updateDictator(Dictator dictator, String user) throws DomainObjectWriteException;

	void updateUser(EUser user) throws DomainObjectWriteException;
	
	void addResources(Resource resource) throws DomainObjectWriteException;
	
	List<Resource> getResources() throws DomainObjectWriteException;
	void deleteResources() throws DomainObjectWriteException;
	void scheduleInsertUpdate(Schedule schedule);

}
