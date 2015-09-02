package com.entradahealth.entrada.core.domain.providers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import android.util.Log;

import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.db.H2Utils;
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
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider.DatabaseProviderException;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class MainUserDatabaseProvider implements DomainObjectProvider{

	private Connection _conn;

	public MainUserDatabaseProvider() throws DomainObjectWriteException{
		this(true);
	}
	
	public MainUserDatabaseProvider(boolean isUpdateRequired) throws DomainObjectWriteException {
		File dbFile = new File(User.getUserRoot(), "main_user");
//		if(dbFile.exists()){
//			return;
//		}
		_conn = H2Utils.openEncryptedDatabase(dbFile, "entrada");

		if(true) {
			GenericSchemaManager schema = new GenericSchemaManager(_conn);
			try {
				schema.updateSchema();
			} catch (Exception e) {
				throw new DomainObjectWriteException(e);
			}
		}
	}

	public Connection getRawConnection() {
		return _conn;
	}

	private boolean _isClosed = false;

	public void close() {
		// H2Utils.close(_conn);
		_isClosed = true;
	}

	public boolean isClosed() {
		return _isClosed;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (!_isClosed)
				close();
		} catch (Exception e) {
			super.finalize();
			throw e;
		}
	}

	@Override
	public ImmutableList<Dictation> getDictations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<Dictation> getDictationsByJob(Long jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public Encounter getEncounter(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Encounter> getEncounters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Encounter> getEncountersByPatient(long patientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public Job getJob(long jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobsByEncounter(long encounterId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobsByJobType(long jobTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobsByStat(boolean stat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobsByAllFlags(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getJobsByAnyFlags(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getDeletedJobs(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> searchJobs(String searchText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public JobType getJobType(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<JobType> getJobTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<ExpressNotesTags> getExpressNotesTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public Patient getPatient(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public Patient getPatientByMRN(String medicalRecordNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<Patient> getPatients() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableSet<Patient> searchPatients(String searchText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@CheckForNull
	public Queue getQueue(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<Queue> getQueues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getCompletedJobs(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getHeldJobs(int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Job> getLocalJobs(int flags) {
		// TODO Auto-generated method stub
		return null;
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
	public void writeDictation(Dictation dictation)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDictations(Iterable<Dictation> dicts)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEncounter(Encounter encounter)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeEncounters(Iterable<Encounter> encs)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Encounter createNewEncounter(Patient patient)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Encounter createNewEncounter(String patient)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeJob(Job job) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeJobs(Iterable<Job> jobs) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeJobType(JobType jt) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeJobTypes(Iterable<JobType> jts)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Job updateJob(Job newJob) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job createNewJob(Encounter encounter)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job createNewJob(String job) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writePatient(Patient patient) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePatients(Iterable<Patient> patients)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeQueue(Queue queue) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeQueues(Iterable<Queue> queues)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeSyncData(SyncData data) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearReadonlyItems() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearJobs() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
	public List<String> getGroupQueues(int grpId)
			throws DomainObjectWriteException {
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

	public static final String SQL_INSERT_EUSER = "INSERT INTO EUser(Name, Password, Environment, CurrentDictator, IsCurrent, QBUserName) VALUES (?, ?, ?, ?, ?, ?);";

	@Override
	public void addUser(EUser user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_INSERT_EUSER);
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getEnvironment());
			stmt.setString(4, user.getCurrentDictator());
			stmt.setBoolean(5, user.isCurrent());
			stmt.setString(6, user.getQbUserName());
			
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 H2Utils.close(stmt);
		}
	}

	public static final String SQL_UPDATE_EUSER = "UPDATE EUser SET IsCurrent = ? WHERE Name = ?";
	
	@Override
	public void updateUser(EUser user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_UPDATE_EUSER);
			stmt.setBoolean(1, user.isCurrent());
			stmt.setString(2, user.getName());
			Log.e("","User--"+user.getName()+"--"+user.isCurrent());
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 H2Utils.close(stmt);
		}
	}

	
	private static final String SQL_GET_EUSERS = "SELECT * FROM EUser;";
	
	@Override
	public List<EUser> getEUsers() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_EUSERS);
			rs = stmt.executeQuery();
			List<EUser> users = createUsers(rs);
			return users;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private static final String SQL_GET_CURRENT_EUSER = "SELECT * FROM EUser WHERE IsCurrent = ?;";

	@Override
	public EUser getCurrentUser(){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_CURRENT_EUSER);
			stmt.setBoolean(1, true);
			rs = stmt.executeQuery();
			List<EUser> users = createUsers(rs);
			if(users.size()>0)
				return users.get(0);
			else 
				return null;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	
	public static final String SQL_INSERT_DICTATOR = "INSERT INTO Dictator(DictID, Name, Clinic, Username, IsCurrent) VALUES (?, ?, ?, ?, ?);";

	@Override
	public void addDictator(Dictator dictator, String user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_INSERT_DICTATOR);
			stmt.setLong(1, dictator.getDictatorID());
			stmt.setString(2, dictator.getDictatorName());
			stmt.setString(3, dictator.getClinicName());			
			stmt.setString(4, user);
			stmt.setBoolean(5, dictator.isCurrent());
			
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	private static final String SQL_DEL_DICTATOR_BY_ID = "DELETE FROM DICTATOR WHERE DictID = ?;";
	
	@Override
	public void deleteDictator(Dictator dictator){
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DEL_DICTATOR_BY_ID);
			stmt.setLong(1, dictator.getDictatorID());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteUserAndDictators(EUser user){
		deleteUser(user);
		deleteUserDictators(user);
	}
	
	private static final String SQL_DEL_USER_BY_NAME = "DELETE FROM EUSER WHERE Name = ?";

	public void deleteUser(EUser user){
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DEL_USER_BY_NAME);
			stmt.setString(1, user.getName());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static final String SQL_DEL_USER_DICTATORS = "DELETE FROM DICTATOR WHERE Username = ?";

	public void deleteUserDictators(EUser user){
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DEL_USER_DICTATORS);
			stmt.setString(1, user.getName());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static final String SQL_UPDATE_DICTATOR_NAME = "UPDATE DICTATOR SET Name = ? WHERE DictID = ?";
	
	@Override
	public void updateDictatorName(long dictatorId, String dictatorName) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_UPDATE_DICTATOR_NAME);
			stmt.setString(1, dictatorName);
			stmt.setLong(2, dictatorId);
			
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	public static final String SQL_UPDATE_DICTATOR = "UPDATE DICTATOR SET IsCurrent = ? WHERE Username = ? AND Name = ?";
	
	@Override
	public void updateDictator(Dictator dictator, String user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_UPDATE_DICTATOR);
			stmt.setBoolean(1, dictator.isCurrent());
			stmt.setString(2, user);
			stmt.setString(3, dictator.getDictatorName());
			
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	private List<EUser> createUsers(ResultSet rs) throws SQLException{
		List<EUser> users = new ArrayList<EUser>();
		while (rs.next()) {
			 EUser user = new EUser();
			 user.setName(rs.getString("Name"));
			 user.setPassword(rs.getString("Password"));
			 user.setEnvironment(rs.getString("Environment"));
			 user.setCurrentDictator(rs.getString("CurrentDictator"));
			 user.setCurrent(rs.getBoolean("IsCurrent"));
			 user.setQbUserName(rs.getString("QBUserName"));
			 users.add(user);
		}
		return users;

	}
	
	private static final String SQL_GET_DICTATORS = "SELECT * FROM Dictator WHERE Username = ?;";
	
	@Override
	public List<Dictator> getDictatorsForUser(String user) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_DICTATORS);
			stmt.setString(1, user);
			rs = stmt.executeQuery();
			List<Dictator> dictators = createDictators(rs);
			return dictators;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}


	private static final String SQL_GET_USER_CURRENT_DICTATOR = "SELECT * FROM Dictator WHERE Username = ? and IsCurrent = ?;";
	
	@Override
	public Dictator getCurrentDictatorForUser(String user) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_USER_CURRENT_DICTATOR);
			stmt.setString(1, user);
			stmt.setBoolean(2, true);
			rs = stmt.executeQuery();
			List<Dictator> dictators = createDictators(rs);
			if(dictators.size()>0)
				return dictators.get(0);
			else 
				return null;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}


	private static final String SQL_GET_EUSER = "SELECT * FROM EUser WHERE Name = ? and Environment = ?;";
	
	@Override
	public boolean isUserExists(EUser user){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_EUSER);
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getEnvironment());
			rs = stmt.executeQuery();
			List<EUser> users = createUsers(rs);
			return users.size()!=0;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}		
	}

	private List<Dictator> createDictators(ResultSet rs) throws SQLException{
		List<Dictator> dictators = new ArrayList<Dictator>();
		while (rs.next()) {
			 Dictator dictator = new Dictator();
			 dictator.setDictatorName(rs.getString("Name"));
			 dictator.setClinicCode(rs.getString("Clinic"));
			 dictator.setDictatorID(rs.getLong("DictID"));
			 dictator.setCurrent(rs.getBoolean("IsCurrent"));
			 dictators.add(dictator);
		}
		return dictators;

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
	public ArrayList<Schedule> getRaawScheduleDetailsList(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteResources() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scheduleInsertUpdate(Schedule schedule) {
		// TODO Auto-generated method stub
		
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
