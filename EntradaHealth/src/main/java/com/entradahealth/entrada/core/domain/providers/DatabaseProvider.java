package com.entradahealth.entrada.core.domain.providers;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.DatabaseUtils;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.db.H2Utils;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.ExpressNotesTags;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Physicians;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.ReferringPhysicians;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Uses a passed-in BoneCP connection pool to unleash query-ish magic onto
 * things. All of these methods create new objects.
 * 
 * @author edr
 * @since 4 Sep 2012
 */
public class DatabaseProvider implements DomainObjectProvider {
	// TODO: Evaluate ImmutableLists versus Collection.unmodifiableList().
	// about 80% of the way through this, I realized that maybe
	// Collections.unmodifiableList() is more appropriate than
	// ImmutableLists; it'll save on reference copies (which shouldn't
	// be a big deal) and may be otherwise neater; the big plus of
	// using strongly-typed ImmutableList everywhere is that it hammers
	// home that you Can't Edit These Things. Will need to think on this.

	// TODO: make the plural writeXXX methods smarter than just iterating.

	// HACK: I wrote myself into a corner with the interfaces and necessitated
	// all this JDBC boilerplate because I couldn't really pass in a SQL
	// connection in a meaningful way; it's possible that in the future this
	// causes perf issues because of grabbing connections out of the pool
	// often. Given that the whole database is effectively in-memory and the
	// connections are (hopefully) being reused efficiently, I don't expect
	// it to be a big problem, but I'm leaving this in here for me to swear
	// at later when it blows up.

	// TODO: Break this out into SQLCipherDatabaseProvider and
	// H2DatabaseProvider.
	// TODO: Break the schema out into database evolutions.

	private final Connection _conn;

	public DatabaseProvider(Connection conn) throws DomainObjectWriteException {
		_conn = conn;

		SchemaManager schema = new SchemaManager(_conn);
		try {
			schema.updateSchema();
		} catch (Exception e) {
			throw new DomainObjectWriteException(e);
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

	private static final String SQL_GET_DICTATIONS = "SELECT * FROM dictations;";
	private static final String SQL_GET_DICTATIONS_BY_JOB = "SELECT * FROM dictations WHERE "
			+ Job.FIELD_JOB_ID + " = ?;";

	@Override
	public ImmutableList<Dictation> getDictations() {
		return dictationQuery(SQL_GET_DICTATIONS, null);
	}

	public ImmutableList<Dictation> getDictationsByJob(Job j) {
		return dictationQuery(SQL_GET_DICTATIONS_BY_JOB, j.id);
	}

	@Override
	public ImmutableList<Dictation> getDictationsByJob(Long id) {
		return dictationQuery(SQL_GET_DICTATIONS_BY_JOB, id);
	}

	private ImmutableList<Dictation> dictationQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);
			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createDictations(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private ImmutableList<Dictation> createDictations(ResultSet rs)
			throws SQLException {

		List<Dictation> dicts = Lists.newArrayList();

		while (rs.next()) {
			// Ew. http://stackoverflow.com/a/2920420/53315
			int possibleQueueId = rs.getInt(Dictation.FIELD_QUEUEID);
			Integer actualQueueId = rs.wasNull() ? null : possibleQueueId;

			dicts.add(new Dictation(rs.getLong(Dictation.FIELD_DICTATIONID), rs
					.getLong(Dictation.FIELD_JOBNUMBER), rs
					.getLong(Dictation.FIELD_DICTATORID), rs
					.getLong(Dictation.FIELD_DICTATIONTYPEID), rs
					.getInt(Dictation.FIELD_STATUS), rs
					.getLong(Dictation.FIELD_DURATION), rs
					.getString(Dictation.FIELD_MACHINENAME), rs
					.getString(Dictation.FIELD_FILENAME), actualQueueId, rs
					.getString(Dictation.FIELD_CLIENTVERSION)));
		}

		return ImmutableList.copyOf(dicts);
	}

	private static final String SQL_GET_ENCOUNTERS = "SELECT * FROM encounters;";
	private static final String SQL_GET_ENCOUNTER_BY_ID = "SELECT * FROM encounters WHERE "
			+ Encounter.FIELD_ENCOUNTER_ID + " = ?;";
	private static final String SQL_GET_ENCOUNTER_BY_PATIENT = "SELECT * FROM encounters WHERE "
			+ Patient.FIELD_ID + "= ?;";

	@Override
	public Encounter getEncounter(long id) {
		List<Encounter> encs = encounterQuery(SQL_GET_ENCOUNTER_BY_ID, id);
		return encs.size() != 0 ? encs.get(0) : null;
	}

	@Override
	public ImmutableList<Encounter> getEncounters() {
		return ImmutableList.copyOf(encounterQuery(SQL_GET_ENCOUNTERS, null));
	}

	@Override
	public ImmutableList<Encounter> getEncountersByPatient(long patientId) {
		return ImmutableList.copyOf(encounterQuery(
				SQL_GET_ENCOUNTER_BY_PATIENT, patientId));
	}

	private List<Encounter> encounterQuery(String query, Long id) {
		Connection conn = _conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);
			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createEncounters(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<Encounter> createEncounters(ResultSet rs) throws SQLException {
		List<Encounter> encs = Lists.newArrayList();

		while (rs.next()) {
			encs.add(new Encounter(rs.getLong(Encounter.FIELD_ENCOUNTER_ID), rs
					.getString(Encounter.SQL_FIELD_APPOINTMENT_DATE), rs
					.getLong(Encounter.FIELD_PATIENT_ID), rs
					.getString(Encounter.FIELD_ATTENDING)));
		}

		return encs;
	}

	private static final String SQL_GET_JOB_BY_NUMBER = "SELECT * FROM jobs WHERE "
			+ Job.FIELD_JOB_NUMBER + " = ?;";
	private static final String SQL_GET_JOB_BY_ID = "SELECT * FROM jobs WHERE "
			+ Job.FIELD_JOB_ID + " = ?;";
	private static final String SQL_GET_JOBS = "SELECT * FROM jobs;";
	private static final String SQL_GET_JOBS_BY_ENCOUNTER = "SELECT * FROM jobs WHERE "
			+ Encounter.FIELD_ENCOUNTER_ID + " = ?;";
	private static final String SQL_GET_JOBS_BY_JOBTYPE = "SELECT * FROM jobs WHERE "
			+ JobType.SQL_FIELD_ID + " = ?;";
	private static final String SQL_GET_JOBS_BY_STAT = "SELECT * FROM jobs WHERE "
			+ Job.FIELD_STAT + " = ?;";
	private static final String SQL_GET_JOBS_BY_ANY_FLAGS = "SELECT * FROM jobs WHERE BITAND("
			+ Job.FIELD_LOCAL_FLAGS + ",?) != 0;";

	@Override
	public Job getJob(long jobId) {
		List<Job> jobs = jobQuery(SQL_GET_JOB_BY_ID, jobId, null, null);
		return jobs.size() == 0 ? null : jobs.get(0);
	}

	@Override
	public List<Job> getJobs() {
		// return ImmutableList.copyOf(jobQuery(SQL_GET_JOBS, null, null,
		// null));
		return Lists.newArrayList(jobQuery(SQL_GET_JOBS, null, null, null));
	}

	@Override
	public ImmutableList<Job> getJobsByEncounter(long encounterId) {
		return ImmutableList.copyOf(jobQuery(SQL_GET_JOBS_BY_ENCOUNTER, null,
				encounterId, null));
	}

	@Override
	public ImmutableList<Job> getJobsByJobType(long jobTypeId) {
		return ImmutableList.copyOf(jobQuery(SQL_GET_JOBS_BY_JOBTYPE, null,
				jobTypeId, null));
	}

	@Override
	public ImmutableList<Job> getJobsByStat(boolean stat) {
		return ImmutableList.copyOf(jobQuery(SQL_GET_JOBS_BY_STAT, null, null,
				stat));
	}

	@Override
	public ImmutableList<Job> getJobsByAllFlags(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(getJobsByAnyFlags(flags),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && (j.localFlags & flags) == flags;
					}
				}));
	}

	@Override
	public List<Job> getLocalJobs(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(getJobsByAnyFlags(flags),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null)
								&& j.isFlagSet(Flags.LOCALLY_CREATED);
					}
				}));
	}

	@Override
	public ImmutableList<Job> getDeletedJobs(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(getJobsByAnyFlags(flags),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null)
								&& j.isFlagSet(Flags.LOCALLY_DELETED);
					}
				}));
	}

	@Override
	public ImmutableList<Job> getCompletedJobs(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(getJobsByAnyFlags(flags),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null)
								&& j.isFlagSet(Flags.UPLOAD_COMPLETED);
					}
				}));
	}

	@Override
	public ImmutableList<Job> getHeldJobs(final int flags) {
		return ImmutableList.copyOf(Iterables.filter(getJobsByAnyFlags(flags),
				new Predicate<Job>() {
					@Override
					public boolean apply(@Nullable Job j) {
						return (j != null) && j.isFlagSet(Flags.HOLD);
					}
				}));
	}
	
	
	@Override
	public ImmutableList<Job> getJobsByAnyFlags(final int flags) {
		return ImmutableList.copyOf(jobQuery(SQL_GET_JOBS_BY_ANY_FLAGS, null,
				(long) flags, null));
	}

	// so this is gross but it'll save me some typing
	private List<Job> jobQuery(String query, Long jobId, Long otherId, Boolean b) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);

			if (jobId != null)
				stmt.setLong(1, jobId);
			else if (otherId != null)
				stmt.setLong(1, otherId);
			else if (b != null)
				stmt.setBoolean(1, b);

			rs = stmt.executeQuery();

			return createJobs(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private static final String SQL_JOB_SEARCH_QUERY = "SELECT * FROM jobs JOIN encounters ON jobs.EncounterID = encounters.EncounterID "
			+ "WHERE encounters.PatientID IN (%s);";

	@Override
	public ImmutableList<Job> searchJobs(String searchText) {
		if (Strings.isNullOrEmpty(searchText)) {
			return ImmutableList.copyOf(Job.sortJobs(getJobs()));
		}

		final ImmutableSet<Patient> patients = searchPatients(searchText);

		final Iterable<Long> patientIds = Iterables.transform(patients,
				new Function<Patient, Long>() {
					@Override
					public Long apply(@Nullable Patient patient) {
						return (patient != null) ? patient.id : 0;
					}
				});

		String ids = Providers.COMMA_JOINER.join(patientIds);

		List<Job> jobs = null;

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(String.format(SQL_JOB_SEARCH_QUERY,
					ids));

			rs = stmt.executeQuery();

			jobs = createJobs(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}

		return ImmutableList.copyOf(Job.sortJobs(jobs));
	}

	private List<Job> createJobs(ResultSet rs) throws SQLException {
		List<Job> jobs = Lists.newArrayList();

		while (rs.next()) {
			jobs.add(new Job(rs.getLong(Job.FIELD_JOB_ID), rs
					.getString(Job.FIELD_JOB_NUMBER), rs
					.getLong(Encounter.FIELD_ENCOUNTER_ID), rs
					.getLong(JobType.SQL_FIELD_ID), rs
					.getBoolean(Job.FIELD_STAT), rs
					.getInt(Job.FIELD_LOCAL_FLAGS)));
		}

		return jobs;
	}

	private static final String SQL_GET_JOBTYPES = "SELECT * FROM job_types;";
	private static final String SQL_GET_JOBTYPES_BY_ID = "SELECT * FROM job_types WHERE "
			+ JobType.SQL_FIELD_ID + " = ?;";
	private static final String SQL_GET_JOBTYPES_BY_DISABLEGENERICJOBS = "SELECT * FROM job_types WHERE "
			+ JobType.SQL_FIELD_DISABLE + " = ?;";

	private static final String SQL_GET_JOBTYPES_BY_DISABLEGENERICJOBS_AND_ID = "SELECT * FROM job_types WHERE "
			+ JobType.SQL_FIELD_DISABLE + " = ? AND " + JobType.SQL_FIELD_ID + " =?;";

	@Override
	public JobType getJobType(long id) {
		List<JobType> jt = jobTypeQuery(SQL_GET_JOBTYPES_BY_ID, id);
		return jt.size() != 0 ? jt.get(0) : null;
	}

	@Override
	public ImmutableList<JobType> getJobTypes() {
		List<JobType> list = jobTypeQuery(SQL_GET_JOBTYPES, null);
		Collections.sort(list);
		return ImmutableList.copyOf(list);
	}
	
	@Override
	public JobType getDefaultGenericJobType() {
		List<JobType> list = getDefaultGenericJobTypes();
		return list.get(0);
	}

	@Override
	public List<JobType> getDefaultGenericJobTypes() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<JobType> list = null; 
		try {
			stmt = _conn.prepareStatement(SQL_GET_JOBTYPES_BY_DISABLEGENERICJOBS);
			stmt.setString(1, "false");
			rs = stmt.executeQuery();
			list = createJobTypes(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
		Collections.sort(list);
		return list;
	}
	
	@Override
	public boolean isExistsInDefaultGenericJobTypes(Long jobTypeId){
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_JOBTYPES_BY_DISABLEGENERICJOBS_AND_ID);
			stmt.setString(1, "false");
			stmt.setLong(2, jobTypeId);
			rs = stmt.executeQuery();
			List<JobType> list = createJobTypes(rs);
			return list.size()!=0;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<JobType> jobTypeQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);

			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createJobTypes(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	private static final String SQL_GET_EXPRESS_NOTES_TAGS = "SELECT DISTINCT JobTypeID,Name,Required FROM express_notes_tags;";
	private static final String SQL_GET_EXPRESS_NOTES_TAGS_BY_ID = "SELECT * FROM express_notes_tags WHERE "
			+ ExpressNotesTags.SQL_FIELD_ID + " = ?;";
	
	@Override
	public ExpressNotesTags getExpressNotesTags(long id) {
		List<ExpressNotesTags> tag = expressNotesTagsQuery(SQL_GET_EXPRESS_NOTES_TAGS_BY_ID, id);
		return tag.size() != 0 ? tag.get(0) : null;
	}

	@Override
	public ImmutableList<ExpressNotesTags> getExpressNotesTags() {
		// TODO Auto-generated method stub
		List<ExpressNotesTags> list = expressNotesTagsQuery(SQL_GET_EXPRESS_NOTES_TAGS, null);
		Collections.sort(list);
		return ImmutableList.copyOf(list);
	}
	
	private List<ExpressNotesTags> expressNotesTagsQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);

			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createExpressNotesTags(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<JobType> createJobTypes(ResultSet rs) throws SQLException {
		List<JobType> jt = Lists.newArrayList();

		while (rs.next()) {
			jt.add(new JobType(rs.getLong(JobType.SQL_FIELD_ID), rs
					.getString(JobType.SQL_FIELD_NAME), rs
					.getString(JobType.SQL_FIELD_DISABLE)));
		}
		return jt;
	}
	
	private List<ExpressNotesTags> createExpressNotesTags(ResultSet rs) throws SQLException {
		List<ExpressNotesTags> tag = Lists.newArrayList();

		while (rs.next()) {
			tag.add(new ExpressNotesTags(rs.getLong(ExpressNotesTags.SQL_FIELD_ID), rs
					.getString(ExpressNotesTags.SQL_FIELD_NAME), rs
					.getString(ExpressNotesTags.SQL_FIELD_REQUIRED)));
		}
		return tag;
	}


	private static final String SQL_GET_PATIENTS = "SELECT * FROM patients";
	private static final String SQL_GET_PATIENT_BY_ID = "SELECT * FROM patients WHERE "
			+ Patient.FIELD_ID + " = ?;";
	private static final String SQL_GET_PATIENT_BY_MRN = "SELECT * FROM patients WHERE "
			+ Patient.FIELD_MRN + " = ?;";

	@Override
	public Patient getPatient(long id) {
		List<Patient> p = patientQuery(SQL_GET_PATIENT_BY_ID, id, null);
		return p.size() != 0 ? p.get(0) : null;
	}


	@Override
	public Patient getPatientDemographicInfo(long patientId){
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		APIService service;
		try {
			EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
			Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
			service = new APIService(env.getApi());
			String responseData = service.getDemographicInfo(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), patientId);
			JSONObject json = new JSONObject(responseData);
			Patient p = new Patient(json.getLong("PatientID"),
					json.getString("MRN"), json.getString("FirstName"),
					json.getString("MI"), json.getString("LastName"),
					json.getString("DOB"), json.getString("Gender"),
					json.getString("ClinicID"), json.getString("Address1"),
					json.getString("Address2"), json.getString("City"),
					json.getString("State"), json.getString("Zip"),
					json.getString("Phone1"));
			writePatient(p);
			return p;
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Patient getPatientByMRN(String medicalRecordNumber) {
		List<Patient> p = patientQuery(SQL_GET_PATIENT_BY_MRN, null,
				medicalRecordNumber);
		return p.size() != 0 ? p.get(0) : null;
	}

	@Override
	public ImmutableList<Patient> getPatients() {
		return ImmutableList.copyOf(patientQuery(SQL_GET_PATIENTS, null, null));
	}

	private List<Patient> patientQuery(String query, Long id, String mrn) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);

			if (id != null)
				stmt.setLong(1, id);
			else if (mrn != null)
				stmt.setString(1, mrn);

			rs = stmt.executeQuery();

			return createPatients(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<Patient> createPatients(ResultSet rs) throws SQLException {
		List<Patient> patients = Lists.newArrayList();

		while (rs.next()) {
			String s = rs.getString(Patient.FIELD_GENDER);
			patients.add(new Patient(rs.getLong(Patient.FIELD_ID), rs
					.getString(Patient.FIELD_MRN), rs
					.getString(Patient.FIELD_FIRST_NAME), (String) rs
					.getObject(Patient.FIELD_MIDDLE_INITIAL), rs
					.getString(Patient.FIELD_LAST_NAME), rs
					.getString(Patient.FIELD_DOB), rs
					.getString(Patient.FIELD_GENDER),rs
					.getString(Patient.FIELD_PCPID), rs
					.getString(Patient.FIELD_ADDRESS1), rs
					.getString(Patient.FIELD_ADDRESS2), rs
					.getString(Patient.FIELD_CITY), rs
					.getString(Patient.FIELD_STATE), rs
					.getString(Patient.FIELD_ZIP), rs
					.getString(Patient.FIELD_PHONE)));
		}

		return patients;
	}

	private static final String SQL_PATIENT_SEARCH_PREFIX = "SELECT * FROM patients WHERE ";
	private static final String SQL_PATIENT_SEARCH_FRAGMENT = Patient.FIELD_MRN
			+ " LIKE {} OR " + Patient.FIELD_FIRST_NAME + " LIKE {} OR "
			+ Patient.FIELD_LAST_NAME + " LIKE {} ";

	private static final String SQL_PATIENT_SEARCH_FRAGMENT_BY_NUM = Patient.FIELD_MRN
			+ " LIKE {} ";

	@Override
	public ImmutableSet<Patient> searchPatients(String searchText) {
		String query;
		if (Strings.isNullOrEmpty(searchText)
				|| searchText.trim().length() == 0)
			return ImmutableSet.copyOf(getPatients());

		final Iterable<String> tokens = Providers.SEARCH_TEXT_SPLITTER
				.split(searchText);

		if (searchText.substring(0, 1).matches("[0-9]")) {
			LinkedList<String> fragments = new LinkedList<String>();
			for (String t : tokens)
				fragments.add(SQL_PATIENT_SEARCH_FRAGMENT_BY_NUM.replace("{}",
						DatabaseUtils.sqlEscapeString(t + "%")));
			query = SQL_PATIENT_SEARCH_PREFIX
					+ Providers.SQL_OR_JOINER.join(fragments) + ";";
		} else {
			LinkedList<String> fragments = new LinkedList<String>();
			for (String t : tokens)
				fragments.add(SQL_PATIENT_SEARCH_FRAGMENT.replace("{}",
						//DatabaseUtils.sqlEscapeString("%" + t + "%")));
						DatabaseUtils.sqlEscapeString(t + "%")));
			query = SQL_PATIENT_SEARCH_PREFIX
					+ Providers.SQL_OR_JOINER.join(fragments) + ";";

		}

		Log.println(Log.INFO, "SQL Query", query);

		ResultSet rs = null;
		try {
			rs = _conn.createStatement().executeQuery(query);

			return ImmutableSet.copyOf(createPatients(rs));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			// H2Utils.close(rs);
		}
	}

	private static final String SQL_GET_QUEUES = "SELECT * FROM queues;";
	private static final String SQL_GET_QUEUES_BY_ID = "SELECT * FROM queues WHERE "
			+ Queue.FIELD_ID + " = ?;";

	@Override
	public Queue getQueue(long id) {
		List<Queue> q = queueQuery(SQL_GET_QUEUES_BY_ID, id);
		return q.size() != 0 ? q.get(0) : null;
	}

	@Override
	public ImmutableList<Queue> getQueues() {
		return ImmutableList.copyOf(queueQuery(SQL_GET_QUEUES, null));
	}

	private List<Queue> queueQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);

			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createQueues(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<Queue> createQueues(ResultSet rs) throws SQLException {
		List<Queue> q = Lists.newArrayList();

		while (rs.next()) {
			q.add(new Queue(rs.getLong(Queue.FIELD_ID), rs
					.getString(Queue.FIELD_NAME), (String) rs
					.getObject(Queue.FIELD_DESCRIPTION), rs
					.getBoolean(Queue.FIELD_ISSUBSCRIBED)));
			// rs.getBoolean(Queue.FIELD_ISDICTATORQUEUE)));

		}

		return q;
	}

	private static final String SQL_MERGE_DICTATION = "MERGE INTO dictations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void writeDictation(Dictation dictation)
			throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_DICTATION);

			stmt.setLong(1, dictation.dictationId);
			stmt.setLong(2, dictation.jobId);
			stmt.setLong(3, dictation.dictationTypeId);
			stmt.setLong(4, dictation.dictatorId);

			if (dictation.queueId == null)
				stmt.setNull(5, Types.INTEGER);
			else
				stmt.setLong(5, dictation.queueId);

			stmt.setLong(6, dictation.status.value);
			stmt.setLong(7, dictation.duration);

			if (dictation.machineName == null)
				stmt.setNull(8, Types.VARCHAR);
			else
				stmt.setString(8, dictation.machineName);

			if (dictation.fileName == null)
				stmt.setNull(9, Types.VARCHAR);
			else
				stmt.setString(9, dictation.fileName);

			if (dictation.clientVersion == null)
				stmt.setNull(10, Types.VARCHAR);
			else
				stmt.setString(10, dictation.clientVersion);

			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void writeDictations(Iterable<Dictation> dicts)
			throws DomainObjectWriteException {
		for (Dictation d : dicts) {
			this.writeDictation(d);
		}
	}

	private static final String SQL_MERGE_ENCOUNTER = "MERGE INTO encounters VALUES (?, ?, ?, ?);";

	@Override
	public void writeEncounter(Encounter encounter)
			throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_ENCOUNTER);

			DateTime d = encounter.appointmentDate;

			stmt.setLong(1, encounter.id);
			stmt.setString(2, d.toString());
			stmt.setLong(3, encounter.patientId);
			stmt.setString(4, encounter.attending);

			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void writeEncounters(Iterable<Encounter> encs)
			throws DomainObjectWriteException {
		for (Encounter e : encs)
			writeEncounter(e);
	}

	private static final String SQL_CREATE_ENCOUNTER = "INSERT INTO encounters VALUES (?, ?, ?, ?)";

	@Override
	public Encounter createNewEncounter(Patient patient)
			throws DomainObjectWriteException {
		long eID = Providers.makeTempEncounterId();
		long pID = patient.id;
		DateTime date = DateTime.now();
		// DateTime appt_dt = DateTime.parse(BundleKeys.appt_date_time,
		// DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));

		String attending = "TEMPORARY";

		Encounter enc = new Encounter(eID, date, pID, attending);
		writeEncounter(enc);

		return enc;
	}

	public Encounter createNewEncounter(String patient)
			throws DomainObjectWriteException {
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

		// long eID = Providers.makeTempEncounterId();
		// long pID = patient.id;
		DateTime date = DateTime.now();
		// DateTime appt_dt = DateTime.parse(BundleKeys.appt_date_time,
		// DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));

		String attending = "TEMPORARY";

		Encounter enc = new Encounter(eID, date, pID, attending);
		writeEncounter(enc);

		return enc;
	}

	private static final String SQL_MERGE_JOB = "MERGE INTO jobs VALUES (?, ?, ?, ?, ?, ?);";

	@Override
	public void writeJob(Job job) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_JOB);

			stmt.setLong(1, job.id);
			stmt.setString(2, job.number);
			stmt.setLong(3, job.encounterId);
			stmt.setLong(4, job.jobTypeId);
			stmt.setBoolean(5, job.stat);
			stmt.setInt(6, job.localFlags);

			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void writeJobs(Iterable<Job> jobs) throws DomainObjectWriteException {
		for (Job j : jobs)
			writeJob(j);
	}

	public static final String SQL_MERGE_JOBTYPE = "MERGE INTO job_types VALUES (?, ?, ?);";

	@Override
	public void writeJobType(JobType jt) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_JOBTYPE);

			stmt.setLong(1, jt.id);
			stmt.setString(2, jt.name);
			stmt.setString(3, jt.disable);

			// if(!Boolean.parseBoolean(jt.disable)){
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			// }

		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	@Override
	public void writeExpressNotesTags(Iterable<ExpressNotesTags> tags)
			throws DomainObjectWriteException {
		BundleKeys.myTags = new ArrayList<ExpressNotesTags>();
		for (ExpressNotesTags entag : tags){
			writeExpressNotesTags(entag);
			BundleKeys.myTags.add(entag);
		}
	}
	
	public static final String SQL_MERGE_EXPRESS_NOTES_TAGS = "INSERT INTO express_notes_tags(JobTypeID, Name, Required) VALUES (?, ?, ?);";
	
	public static final String SQL_TRUNCATE_EXPRESS_NOTES_TAGS = "TRUNCATE TABLE express_notes_tags; ";
	
	public void truncExpressNotes(){
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_TRUNCATE_EXPRESS_NOTES_TAGS);

			boolean result = stmt.execute();
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	public static final String SQL_INSERT_EUSER = "INSERT INTO EUser(Name, Password, Environment, CurrentDictator, IsCurrent) VALUES (?, ?, ?, ?, ?);";

	public void addUser(EUser user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_INSERT_EUSER);
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getEnvironment());
			stmt.setString(4, user.getCurrentDictator());
			stmt.setBoolean(5, user.isCurrent());
			
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
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

	public static final String SQL_INSERT_DICTATOR = "INSERT INTO Dictator(DictID, Name, Clinic, Username, IsCurrent) VALUES (?, ?, ?, ?, ?);";

	public void addDictator(Dictator dictator, String user) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_INSERT_DICTATOR);
			stmt.setLong(1, dictator.getDictatorID());
			stmt.setString(2, dictator.getDictatorName());
			stmt.setString(3, dictator.getClinicCode());			
			stmt.setString(4, user);
			stmt.setBoolean(5, dictator.isCurrent());
			
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
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
	public void writeExpressNotesTags(ExpressNotesTags tag) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_EXPRESS_NOTES_TAGS);
			
			stmt.setLong(1, tag.id);
			stmt.setString(2, tag.name);
			stmt.setString(3, tag.required);

			// if(!Boolean.parseBoolean(jt.disable)){
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
			// }

		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void writeJobTypes(Iterable<JobType> jts)
			throws DomainObjectWriteException {
		for (JobType jt : jts)
			writeJobType(jt);
	}

	private static final String SQL_UPDATE_JOB = "UPDATE jobs SET LocalFlags = ?, JobTypeID = ?, Stat = ? WHERE JobId = ?";

	@Override
	public Job updateJob(Job newJob) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_JOB);

			stmt.setInt(1, newJob.localFlags);
			stmt.setLong(2, newJob.jobTypeId);
			stmt.setBoolean(3, newJob.stat);
			stmt.setLong(4, newJob.id);

			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows
						+ ", expected 1.");

			return getJob(newJob.id);
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			// H2Utils.close(stmt);
		}
	}

	@Override
	public Job createNewJob(Encounter encounter)
			throws DomainObjectWriteException {
		String jNumber = Providers.makeTempJobNumber();
		long eID = encounter.id;

		long jobTypeId = -1;

		final UserState state = AndroidState.getInstance().getUserState();
//		synchronized (state) {
			jobTypeId = Long.valueOf(state.getCurrentAccount().getSetting(
					AccountSettingKeys.DEFAULT_JOBTYPE_ID));
//		}
		Integer queueId = null;
		boolean stat = false;

		Job j = new Job(-Instant.now().getMillis(), jNumber, eID, jobTypeId,
				stat).setFlag(Job.Flags.LOCALLY_CREATED);
		writeJob(j);

		return j;
	}

	public Job createNewJob(String j) throws DomainObjectWriteException {

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

	private static final String SQL_MERGE_PATIENT = "MERGE INTO patients VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	@Override
	public void writePatient(Patient patient) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_PATIENT);

			stmt.setLong(1, patient.id);
			stmt.setString(2, patient.medicalRecordNumber);
			stmt.setString(3, patient.firstName);
			stmt.setObject(4, patient.middleInitial);
			stmt.setString(5, patient.lastName);
			stmt.setString(
					6,
					(patient.dateOfBirth != null) ? patient.dateOfBirth
							.toString() : null);
			stmt.setString(7, patient.gender.name());
			stmt.setString(8, patient.address1);
			stmt.setString(9, patient.address2);
			stmt.setString(10, patient.city);
			stmt.setString(11, patient.state);
			stmt.setString(12, patient.zip);
			stmt.setString(13, patient.phone);
			stmt.setString(14, patient.pcpid);
			
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}


	@Override
	public void writePatients(Iterable<Patient> patients)
			throws DomainObjectWriteException {
		for (Patient p : patients)
			writePatient(p);
	}

	private static final String SQL_MERGE_QUEUE = "MERGE INTO queues VALUES (?, ?, ?, ?);";

	@Override
	public void writeQueue(Queue queue) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_QUEUE);

			stmt.setLong(1, queue.id);
			stmt.setString(2, queue.name);
			stmt.setObject(3, queue.description);
			stmt.setObject(4, queue.isSubscribed);

			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
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

		if (BundleKeys.IS_CLEAR) {
			// clear Local jobs
			if (BundleKeys.List_Local_Jobs != null
					&& BundleKeys.List_Local_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Local_Jobs.size(); i++) {
					data.encounters.remove(BundleKeys.List_Local_Encounters
							.get(i));
					data.jobs.remove(BundleKeys.List_Local_Jobs.get(i));
				}

			}

			// clear deleted jobs and encounters
			if (BundleKeys.List_Local_Del_Jobs != null
					&& BundleKeys.List_Local_Del_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Local_Del_Jobs.size(); i++) {
					data.jobs.remove(BundleKeys.List_Local_Del_Jobs.get(i));
				}
			}
			if (BundleKeys.List_Local_Del_Encounters != null
					&& BundleKeys.List_Local_Del_Encounters.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Local_Del_Encounters.size(); i++)
					data.encounters.remove(BundleKeys.List_Local_Del_Encounters
							.get(i));
			}

			// clear completed jobs and encounters
			if (BundleKeys.List_Completed_Jobs != null
					&& BundleKeys.List_Completed_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Completed_Jobs.size(); i++) {
					data.jobs.remove(BundleKeys.List_Completed_Jobs.get(i));
				}
			}
			if (BundleKeys.List_Completed_Encounters != null
					&& BundleKeys.List_Completed_Encounters.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Completed_Encounters.size(); i++)
					data.encounters.remove(BundleKeys.List_Completed_Encounters
							.get(i));
			}

			// clear held jobs and encounters
			if (BundleKeys.List_Held_Jobs != null
					&& BundleKeys.List_Held_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Held_Jobs.size(); i++) {
					data.jobs.remove(BundleKeys.List_Held_Jobs.get(i));
				}
			}
			if (BundleKeys.List_Held_Encounters != null
					&& BundleKeys.List_Held_Encounters.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Held_Encounters.size(); i++)
					data.encounters.remove(BundleKeys.List_Held_Encounters
							.get(i));
			}

			BundleKeys.IS_CLEAR = false;

		}

		if (!BundleKeys.IS_CLEAR) {
			
			
			if (BundleKeys.List_Local_Jobs != null
					&& BundleKeys.List_Local_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Local_Jobs.size(); i++) {
					if(!BundleKeys.List_Local_Jobs.get(i).isFlagSet(Job.Flags.LOCALLY_DELETED)){
						data.encounters.add(BundleKeys.List_Local_Encounters.get(i));
						data.jobs.add(BundleKeys.List_Local_Jobs.get(i));
					}
				}

			}

			// Write completed jobs and encounters data
			if (BundleKeys.List_Completed_Jobs != null
					&& BundleKeys.List_Completed_Jobs.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Completed_Jobs.size(); i++) {
					data.jobs.add(BundleKeys.List_Completed_Jobs.get(i));
				}
			}
			if (BundleKeys.List_Completed_Encounters != null
					&& BundleKeys.List_Completed_Encounters.size() > 0) {
				for (int i = 0; i < BundleKeys.List_Completed_Encounters.size(); i++)
					data.encounters.add(BundleKeys.List_Completed_Encounters
							.get(i));
			}
			
			 //Write held jobs and encounters data
	        if(BundleKeys.List_Held_Jobs != null && BundleKeys.List_Held_Jobs.size() > 0){
	        	for(int i=0;i<BundleKeys.List_Held_Jobs.size();i++){
	        		data.jobs.add(BundleKeys.List_Held_Jobs.get(i));
	        	}
	        }
	        if(BundleKeys.List_Held_Encounters != null && BundleKeys.List_Held_Encounters.size() > 0){
	        	for(int i=0;i<BundleKeys.List_Held_Encounters.size();i++)
	        	data.encounters.add(BundleKeys.List_Held_Encounters.get(i));
	        }
	        
	      	    
		}

		if (BundleKeys.List_Held_Jobs != null)
			Log.e("list_held_jobs", BundleKeys.List_Held_Jobs.toString());
		if (BundleKeys.List_Held_Encounters != null)
			Log.e("List_Held_Encounters",
					BundleKeys.List_Held_Encounters.toString());
		
		if (BundleKeys.List_Local_Del_Jobs != null)
			Log.e("List_Local_Del_Jobs", BundleKeys.List_Local_Del_Jobs.size()
					+ "--" + BundleKeys.List_Local_Del_Jobs.toString());
		if (BundleKeys.List_Local_Jobs != null)
			Log.e("List_Local_Jobs", BundleKeys.List_Local_Jobs.size() + "--"
					+ BundleKeys.List_Local_Jobs.toString());
		if (BundleKeys.List_Local_Encounters != null)
			Log.e("List_Local_Encounters",
					BundleKeys.List_Local_Encounters.size() + "--"
							+ BundleKeys.List_Local_Encounters.toString());

		BundleKeys.SYNC_AFTER_DELETE = true;

		writeQueues(data.queues);
		writeJobTypes(data.jobTypes);
		truncExpressNotes();
		writeExpressNotesTags(data.expressNotesTags);
		writePatients(data.patients);
		writeEncounters(data.encounters);
		writeJobs(data.jobs);
		writeDictations(data.dictations);
		writePhysicians(data.physicians);
		writeReferringPhysicians(data.referringPhysicians);
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

	private static final String SQL_CLEAR_READONLY_ITEMS = "TRUNCATE TABLE queues; "
			+ "TRUNCATE TABLE encounters; " + "TRUNCATE TABLE jobs; " +
			// "TRUNCATE TABLE dictations; " +
			"TRUNCATE TABLE physicians; "+ "TRUNCATE TABLE referring_physicians; "+
			"TRUNCATE TABLE job_types; " + "TRUNCATE TABLE patients;";

	private static final String SQL_CLEAR_JOBS = "TRUNCATE TABLE encounters; "
			+ "TRUNCATE TABLE jobs; " + "TRUNCATE TABLE job_types; "
			+ "TRUNCATE TABLE patients;";

	@Override
	public void clearReadonlyItems() throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_CLEAR_READONLY_ITEMS);

			boolean result = stmt.execute();
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void clearJobs() throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_CLEAR_JOBS);

			boolean result = stmt.execute();
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	public static class DatabaseProviderException extends RuntimeException {
		public DatabaseProviderException() {
		}

		public DatabaseProviderException(String s) {
			super(s);
		}

		public DatabaseProviderException(String s, Throwable throwable) {
			super(s, throwable);
		}

		public DatabaseProviderException(Throwable throwable) {
			super(throwable);
		}
	}

	private static final String SQL_UPDATE_PASSCODE = "UPDATE extras SET RequirePassCode = ?";

	@Override
	public void updatePasscode(int code) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_PASSCODE);

			stmt.setInt(1, code);

			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows
						+ ", expected 1.");

		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			H2Utils.close(stmt);
		}
	}

	private static final String SQL_GET_GROUP_NAME = "SELECT * FROM favoritegroupname;";

	@Override
	public ResultSet getGroupName() throws DomainObjectWriteException {
		BundleKeys.list_grp_names = Lists.newArrayList();
		BundleKeys.list_grp_ids = Lists.newArrayList();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String grpId = null;
		String grpName = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_GROUP_NAME);
			rs = stmt.executeQuery();
			while (rs.next()) {
				BundleKeys.grpId = rs.getString("GroupID");
				BundleKeys.grpName = rs.getString("GroupName");
				BundleKeys.list_grp_ids.add(BundleKeys.grpId);
				BundleKeys.list_grp_names.add(BundleKeys.grpName);
			}
			return rs;
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}

	}
	
	//private static final String SQL_UPDATE_GROUP_NAME = "UPDATE favoritegroupname SET GroupName = ? WHERE GroupID = ?";
	private static final String SQL_INSERT_GROUP_NAME = "INSERT INTO favoritegroupname(GroupName) VALUES(?)";

	@Override
	public void insertFavGroupName(String grpName) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_INSERT_GROUP_NAME);

			stmt.setString(1, grpName);
			//stmt.setInt(2, Integer.parseInt(grpId));
			
			int rows = stmt.executeUpdate();
			//int rows = stmt.executeUpdate(SQL_INSERT_GROUP_NAME, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			while (rs.next()) {
				BundleKeys.favgrp_groupID = rs.getInt(1);
			}
			Log.e("BundleKeys.favgrp_groupID", Integer.toString(BundleKeys.favgrp_groupID));
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows
						+ ", expected 1.");

		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	private static final String SQL_UPDATE_GROUP_NAME = "UPDATE favoritegroupname SET GroupName = ? WHERE GroupID = ?";

	@Override
	public void updateFavGroupName(String grpId, String grpName) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_GROUP_NAME);

			stmt.setString(1, grpName);
			stmt.setInt(2, Integer.parseInt(grpId));
			
			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows
						+ ", expected 1.");

		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	private static final String SQL_TRUNCATE_GROUP_QUEUES = "TRUNCATE TABLE groupqueues";
	private static final String SQL_SET_GROUP_QUEUES = "INSERT INTO groupqueues VALUES (?,?)";

	@Override
	public void createGroupQueues(List<String> list_queues) throws DomainObjectWriteException {
		/*if(BundleKeys.Selected_Queues != null && BundleKeys.Selected_Queues.size()>0)
			BundleKeys.Selected_Queues.clear();*/
		PreparedStatement stmt = null;
		
		for(int i=0;i<list_queues.size();i++){
			try {
				stmt = _conn.prepareStatement(SQL_SET_GROUP_QUEUES);
				stmt.setInt(1, BundleKeys.favgrp_groupID);
				stmt.setString(2, list_queues.get(i));
				stmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private static final String SQL_DELETE_GROUP_QUEUES = "DELETE FROM groupqueues WHERE GroupID = ?";
	
	@Override
	public void updateGroupQueues(List<String> list_queues){
		
		PreparedStatement stmt = null;
		
			try {
				stmt = _conn.prepareStatement(SQL_DELETE_GROUP_QUEUES);
				stmt.setInt(1, Integer.parseInt(BundleKeys.grpId));
				stmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
	}
	
	private static final String SQL_GET_GROUP_QUEUES = "SELECT * FROM groupqueues WHERE GroupID = ?";
	@Override
	public List<String> getGroupQueues(int grpId) throws DomainObjectWriteException {
		List<String> queues = Lists.newArrayList();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
				stmt = _conn.prepareStatement(SQL_GET_GROUP_QUEUES);
				stmt.setInt(1, grpId);
				rs = stmt.executeQuery();
				while (rs.next()) 
					queues.add(rs.getString("Name"));
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return queues;
	}
	
	private static final String SQL_GET_QUEUE_COUNT = "SELECT COUNT(*) FROM groupqueues WHERE GroupID = ?";
	@Override
	public int getQueueCount(int qId) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		int qCount = 0;
		ResultSet rs = null;
		try {
				stmt = _conn.prepareStatement(SQL_GET_QUEUE_COUNT);
				stmt.setInt(1, qId);
				rs = stmt.executeQuery();
				
				while (rs.next()) 
					qCount = rs.getInt(1);
				//BundleKeys.list_q_counts.add(qCount);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return qCount;
	}

	@Override
	public int getPasscode() throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		return 0;
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

	private static final String SQL_DELETE_FAV_GROUPS = "DELETE FROM favoritegroupname WHERE GroupID = ?";
	private static final String SQL_DELETE_FAV_QUEUES = "DELETE FROM groupqueues WHERE GroupID = ?";
	
	@Override
	public void deleteFavGroups(int grpId) {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		
		try {
			stmt = _conn.prepareStatement(SQL_DELETE_FAV_QUEUES);
			stmt.setInt(1, grpId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			stmt = _conn.prepareStatement(SQL_DELETE_FAV_GROUPS);
			stmt.setInt(1, grpId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void writePhysicians(Iterable<Physicians> physician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		for (Physicians phy : physician) {
			this.writePhysician(phy);
		}
	}

	@Override
	public void writeReferringPhysicians(
			Iterable<ReferringPhysicians> referringPhysician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		for (ReferringPhysicians rphy : referringPhysician) {
			this.writeReferringPhysician(rphy);
		}
	}

	private static final String SQL_MERGE_PHYSICIANS = "MERGE INTO physicians VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	@Override
	public void writePhysician(Physicians physician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_PHYSICIANS);

			stmt.setLong(1, physician.refid);
			stmt.setString(2, physician.phyid);
			stmt.setLong(3, physician.clinicid);
			stmt.setString(4, physician.firstName);
			stmt.setString(5, physician.middleInitial);
			stmt.setString(6, physician.lastName);
			stmt.setString(
					7,
					(physician.dateOfBirth != null) ? physician.dateOfBirth
							.toString() : null);

			stmt.setString(8, physician.gender.name());
			stmt.setString(9, physician.address1);
			stmt.setString(10, physician.address2);
			stmt.setString(11, physician.city);
			stmt.setString(12, physician.state);
			stmt.setString(13, physician.zip);
			stmt.setString(14, physician.phone);

			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	//private static final String SQL_MERGE_REFERRINGPHYSICIANS = "MERGE INTO referring_physicians VALUES (?, ?, ?);";
	private static final String SQL_MERGE_REFERRINGPHYSICIANS = "INSERT INTO referring_physicians (ReferringID,JobID) VALUES (?, ?);";
	
	@Override
	public void writeReferringPhysician(ReferringPhysicians referrringPhysician)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_MERGE_REFERRINGPHYSICIANS);

			stmt.setLong(1, referrringPhysician.refid);
			stmt.setLong(2, referrringPhysician.jobid);
			
			int result = stmt.executeUpdate();
			if (stmt.executeUpdate() != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		}catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	
	private static final String SQL_GET_REFERRING_PHYSICIANS = "SELECT * FROM referring_physicians WHERE "+ Job.FIELD_JOB_ID + " = ?;";
	
	@Override
	public long getReferringPhysicians(long jobId) {
		return refPhysicianQuery(SQL_GET_REFERRING_PHYSICIANS, jobId);
	}
	
	private long refPhysicianQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);
			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createReferringPhysicians(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	private long createReferringPhysicians(ResultSet rs)
			throws SQLException {
		long refId = 0;
		while (rs.next()) {
			refId = rs.getLong(ReferringPhysicians.FIELD_REFERRINGID);
		}

		return refId;
	}
	
	private static final String SQL_GET_PHYSICIANS = "SELECT * FROM physicians WHERE "+ ReferringPhysicians.FIELD_REFERRINGID + " = ?;";
	
	@Override
	public ImmutableList<Physicians> getPhysicians(long refId) {
		return physiciansQuery(SQL_GET_PHYSICIANS, refId);
	}

	private ImmutableList<Physicians> physiciansQuery(String query, Long id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);
			if (id != null)
				stmt.setLong(1, id);

			rs = stmt.executeQuery();

			return createPhysicians(rs);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	private ImmutableList<Physicians> createPhysicians(ResultSet rs)
			throws SQLException {

		List<Physicians> phy = Lists.newArrayList();

		while (rs.next()) {
			
			phy.add(new Physicians(rs.getLong(Physicians.FIELD_REFERRINGID), 
						rs.getString(Physicians.FIELD_PHYSICIANID),
						rs.getLong(Physicians.FIELD_CLINICID),
						rs.getString(Physicians.FIELD_FIRST_NAME),
						rs.getString(Physicians.FIELD_MIDDLE_INITIAL),
						rs.getString(Physicians.FIELD_LAST_NAME),
						rs.getString(Physicians.FIELD_DOB),
						rs.getString(Physicians.FIELD_GENDER),
						rs.getString(Physicians.FIELD_ADDRESS1),
						rs.getString(Physicians.FIELD_ADDRESS2),
						rs.getString(Physicians.FIELD_CITY),
						rs.getString(Physicians.FIELD_STATE),
						rs.getString(Physicians.FIELD_ZIP),
						rs.getString(Physicians.FIELD_PHONE)
						));
		}

		return ImmutableList.copyOf(phy);
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

	public static final String SQL_INSERT_RESOURCE = "INSERT INTO ResourceNames(ResourceID, ResourceName) VALUES (?, ?);";

	@Override
	public void addResources(Resource resource) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {		
			stmt = _conn.prepareStatement(SQL_INSERT_RESOURCE);
			stmt.setString(1, resource.getResourceId());
			stmt.setString(2, resource.getResourceName());

			int result = stmt.executeUpdate();

			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			H2Utils.close(stmt);
		}
	}

	// Resource names operations.
	private static final String SQL_GET_RESOURCES = "SELECT * FROM ResourceNames ORDER BY ResourceName ASC;";
	private static final String SQL_DELETE_RESOURCE = "DELETE * FROM ResourceNames;";
	
	// Used to get the resource names form the database.
	@Override
	public List<Resource> getResources() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_RESOURCES);
			rs = stmt.executeQuery();
			List<Resource> resources = createresources(rs);
			return resources;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(rs);
			H2Utils.close(stmt);
		}
	}

	// Create resources from the result set.
	private List<Resource> createresources(ResultSet rs) throws SQLException{
		List<Resource> resources = new ArrayList<Resource>();
		while (rs.next()) {
			Resource resource = new Resource();
			resource.setResourceId(rs.getString("ResourceID"));
			resource.setResourceName(rs.getString("ResourceName"));
			resources.add(resource);
		}
		Collections.sort(resources, ALPHABETICAL_ORDER);
		return resources;
	}
	
	// Comparator to compare the names in the alphabetical order
	private static Comparator<Resource> ALPHABETICAL_ORDER = new Comparator<Resource>() {
	    public int compare(Resource resourcename1, Resource resourcename2) {
	    	int res = String.CASE_INSENSITIVE_ORDER.compare(resourcename1.getResourceName(), resourcename2.getResourceName());
	    	 if (res == 0) {
	             res = resourcename1.getResourceName().compareTo(resourcename2.getResourceName());
	         }
	    	return res;
	    }
	};

	
	/**
	 * Method to delete the resource names from DB.
	 */
	public void deleteResources() {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DELETE_RESOURCE);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	//Schedule DB quries
	private static final String SQL_INSERT_SCHEDULE = "INSERT INTO Schedule(ScheduleID, AppointmentStatus, PatientID, JobId, ReasonName, ResourceID, AppointmentDate) VALUES (?, ?, ?, ?, ?, ?, ?);";
	private static final String SQL_UPDATE_SCHEDULE = "UPDATE Schedule SET AppointmentStatus = ?, PatientID = ?, JobId = ?, ReasonName = ?, ResourceID = ?, AppointmentDate = ? WHERE ScheduleID = ?;";
	private static final String SQL_GET_SCHEDULE_BY_ID = "SELECT * FROM Schedule WHERE ScheduleID = ?;";
	private static final String SQL_GET_SCHEDULES = "SELECT * FROM Schedule;";
	/**
	 * Method to retrieve the schedule based on the id passed
	 * @param ID
	 * @return Schedule
	 */
	public Schedule getSchedulebyId(int ID) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_SCHEDULE_BY_ID);
			if (ID != -1)
				stmt.setInt(1, ID);
			rs = stmt.executeQuery();
			List<Schedule> schedule = createSchedules(rs);
			return schedule.get(0);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(rs);
			H2Utils.close(stmt);
		}
	}

	// Insert a schedule into DB.
	@Override
	public void scheduleInsertUpdate(Schedule schedule)  {

		try{
			Schedule _schedule = getSchedulebyId(schedule.getScheduleID());
			if(_schedule.getScheduleID() == (schedule.getScheduleID())){
				try {
					updateSchedule(_schedule);
				} catch (DomainObjectWriteException e) {
					e.printStackTrace();
				}
			}
		} catch(DatabaseProviderException ex){
			try {
				addSchedule(schedule);
			} catch (DomainObjectWriteException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Insert schedule to DB.
	 * @param schedule
	 * @throws DomainObjectWriteException
	 */
	public void addSchedule(Schedule schedule) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_INSERT_SCHEDULE);
			stmt.setInt(1, schedule.getScheduleID());
			stmt.setInt(2, schedule.getAppointmentStatus());
			stmt.setInt(3, schedule.getPatientID());
			stmt.setString(4, schedule.getJobId());
			stmt.setString(5, schedule.getReasonName());
			stmt.setString(6, schedule.getResourceID());
			stmt.setString(7, schedule.getAppointmentDate());

			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(stmt);
		}
	}

	/**
	 *  Method used to update the schedule in the Db
	 * @param schedule
	 * @throws DomainObjectWriteException
	 */
	public void updateSchedule(Schedule schedule) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_SCHEDULE);

			stmt.setInt(1, schedule.getAppointmentStatus());
			stmt.setInt(2, schedule.getPatientID());
			stmt.setString(3, schedule.getJobId());
			stmt.setString(4, schedule.getReasonName());
			stmt.setString(5, schedule.getResourceID());
			stmt.setString(6, schedule.getAppointmentDate());
			stmt.setInt(7, schedule.getScheduleID());
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(stmt);
		}
	}
	
	/**
	 * Method to get all the schedules from the Schedule table.
	 * @return ArrayList<Schedule>
	 */
	public ArrayList<Schedule> getSchedules() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_SCHEDULES);
			rs = stmt.executeQuery();
			ArrayList<Schedule> resources = createSchedules(rs);
			return resources;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(rs);
			H2Utils.close(stmt);
		}
	}
	
	
	
	/**
	 * Method to get the scheduled details with raw query when user selects 'Resource names' and Date from the calendar.
	 * @return List - ScheduleDetails
	 */
	@Override
	public ArrayList<Schedule> getRaawScheduleDetailsList(String query) {
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(query);
			rs = stmt.executeQuery();
			ArrayList<Schedule> resources = createSchedules(rs);
			return resources;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			H2Utils.close(rs);
			H2Utils.close(stmt);
		}
	}
	
	// create schedules from the result set.
	private ArrayList<Schedule> createSchedules(ResultSet rs) throws SQLException{
		ArrayList<Schedule> resources = new ArrayList<Schedule>();
		while (rs.next()) {
			Schedule schedule = new Schedule();
			schedule.setScheduleID(rs.getInt("ScheduleID"));
			schedule.setAppointmentStatus(rs.getInt("AppointmentStatus"));
			schedule.setPatientID(rs.getInt("PatientID"));
			schedule.setJobId(rs.getString("JobId"));
			schedule.setReasonName(rs.getString("ReasonName"));
			schedule.setResourceID(rs.getString("ResourceID"));
			schedule.setAppointmentDate(rs.getString("AppointmentDate"));

			resources.add(schedule);
		}
		return resources;
	}
	
	    private static final String SQL_SCHEDULE_SEARCH_QUERY = "SELECT * FROM Schedule JOIN patients ON Schedule.PatientID = patients.PatientID "
	            + "WHERE patients.PatientID IN (%s);";
	
	@Override
	public ArrayList<Schedule> searchSchedules(String searchText) {
	if (Strings.isNullOrEmpty(searchText)) {
	            //return ImmutableList.copyOf(Job.sortJobs(getJobs()));
	}
	final ImmutableSet<Patient> patients = searchPatients(searchText);
	
	final Iterable<Long> patientIds = Iterables.transform(patients,
	                           new Function<Patient, Long>() {
	                                          @Override
	                                          public Long apply(@Nullable Patient patient) {
	                                                         return (patient != null) ? patient.id : 0;
	                                          }
	                           });
	
	String ids = Providers.COMMA_JOINER.join(patientIds);
	
	PreparedStatement stmt = null;
	ResultSet rs = null;
	try {
	            stmt = _conn.prepareStatement(String.format(SQL_SCHEDULE_SEARCH_QUERY,ids));
	            rs = stmt.executeQuery();
	
	            return createSchedules(rs);
	} catch (Exception e) {
	            throw new DatabaseProviderException(e);
	} finally {
	            H2Utils.close(rs);
	            H2Utils.close(stmt);
	}
	}

	@Override
	public void deleteDictator(Dictator dictator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDictatorName(long dictatorId, String dictatorName)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	
}
