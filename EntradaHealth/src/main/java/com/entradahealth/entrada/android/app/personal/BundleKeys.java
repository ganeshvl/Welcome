package com.entradahealth.entrada.android.app.personal;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.ExpressNotesTags;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Physicians;
import com.google.common.collect.Lists;

/**
 * The names of various keys to be passed as part of intents' "extras" function.
 * 
 * @author edr
 * @since 24 Sep 2011
 */
public class BundleKeys {
	private BundleKeys() {
	}

	//public static String SECURE_MSG_RECIPIENT;
	public static Context PinContext;
	public static boolean isAppKilled = false;
	public static boolean isTOUAccepted = false;
	public static String TOUContent = null;
	public static String[] recepients;
	public static boolean isFront = false;
	///public static String APP_VER_NAME = null;
	//public static int APP_VER_CODE;
	public static final String SELECTED_USER = "selected_user";
	public static final String SELECTED_ACCOUNT = "selected_account";
	public static final String SELECTED_JOB = "selected_job";
	public static final String SELECTED_JOB_ACCOUNT = "selected_job_account";
	public static final String UPLOADING_DICTATION = "dictation_upload";
	//public static String PIN_SAVED = "";
	public static int which = 1;
	public static String title = "Today's Jobs";
	public static String last_updated_date = null;
	public static String last_updated_time = null;
	public static int TODAY_COUNT = 0;
	public static int TOMORROW_COUNT = 0;
	public static int STAT_COUNT = 0;
	public static int ALL_COUNT = 0;
	public static int HOLD_COUNT = 0;
	public static int DELETED_COUNT = 0;
	public static int COMPLETED_COUNT = 0;
	public static int UNSENT_COUNT = 0;
	public static String cur_uname = "User_1";
	//public static String cur_pin;
	public static String appt_date_time = null;
	public static String job_type;
	public static int days_to_sync = 1;
	public static int mins_to_sync = 5;
	public static int STATUS_CODE = 500;
	public static int POSITION = 0;
	public static boolean isCapture = true;
	//public static Long dictId = 0L;
	public static String LOCAL_MRN = "999999";
	public static String LOCAL_LASTNAME = "PATIENT";
	public static String LOCAL_FIRSTNAME = "GENERIC";
	public static List<String> Selected_Queues;
	public static List<Integer> Selected_Queues_Positions;
	public static String grpName = null;
	public static String grpId = "1";
	public static int favgrp_groupID;
	public static boolean queue_edit = false;
	public static String SEL_QUEUE = null;
	
	public static ArrayList<String> selChks_grp_names = Lists.newArrayList();
	public static ArrayList<String> list_grp_names;
	public static ArrayList<String> list_grp_ids;
	public static ArrayList<Integer> list_q_counts;
	public static List<ExpressNotesTags> myTags;
	public static List<JobType> myJobTypes;
	public static long refID = 0;
	public static String pcpID = null;
	public static List<Physicians> list_pcp;
	public static List<Physicians> list_rp;
	
	public static int PASSCODE_MINUTES = 0;
	public static int PASSCODE = 0;
	public static boolean IS_INTERRUPTED = true;
	public static boolean IS_DIRTY = false;
	public static boolean IS_CLEAR = false;
	public static boolean ACC_EDITED = false;
	public static boolean PASSCODE_EXPIRED = true;
	public static boolean VIB_ON_RECORD = false;
	public static boolean VIB_ON_STOP = false;
	public static boolean SECURE_MSG = true;
	public static boolean PROXIMITY_LOCK = true;
	//public static boolean IS_FIRST_SYNC = true;
	public static boolean QUEUE_CHANGED = false;
	public static boolean SYNC_AFTER_DELETE = true;
	public static boolean fromCaputreImages = false;
	public static boolean fromImageDisplay = false;
	public static boolean fromSecureMessaging = false;
	public static String current_img_path;
	public static int current_img_position, img_total;
	public static Account CURR_ACCOUNT;
	public static Patient SEL_PATIENT = null;
	public static JobType SEL_JOB_TYPE = null;
	public static String SYNC_FOR_USER;
	public static String SYNC_FOR_ACC;
	public static String SYNC_FOR_CLINIC;
	public static Long NEW_JOB_ID;
	public static ArrayList<Long> Held_Encounter_IDs;
	public static ArrayList<Long> Deleted_Encounter_IDs;
	public static ArrayList<DateTime> Deleted_Date;
	public static ArrayList<Long> Completed_Encounter_IDs;
	public static List<Job> List_Held_Jobs;
	public static List<Job> List_Local_Del_Jobs;
	public static List<Job> List_Completed_Jobs;
	public static List<Encounter> List_Held_Encounters;
	public static List<Encounter> List_Local_Del_Encounters;
	public static List<Encounter> List_Completed_Encounters;
	public static List<Job> List_Local_Jobs;
	public static List<Encounter> List_Local_Encounters;
	public static List<Patient> List_Local_Patients;
	public static boolean isNewSelection = true;
	public static long TIME_START;
	public static long TIME_GONE;
	public static int[] passcode_values = {0, 5 , 10, 15, 30, 60, 120};
	public static boolean toCall = false;
	public static boolean isPatientInfoShowing;
    public static int patientPageNumber;
    public static boolean isjtInfoShowing;
    public static int jtPageNumber;
	public static String SYNC_SERVICE_START = "SyncServiceStart";
    public static String SYNC_SERVICE_END = "SyncServiceEnd";
    public static String JOB_SEARCH_TASK = "SearchTask";
    public static int NUMBER_OF_ITEMS_SHOW_ONSCROLL = 100;
    public static ArrayList<String> img_paths;

    public static List<ENTUser> QB_Users;
    public static List<ENTConversation> QB_Conversations;
    public static String CURRENT_QB_USER_ID = "CURRENT_QB_USER_ID";
    public static String CURRENT_QB_LOGIN = "CURRENT_QB_LOGIN";
    public static String CURRENT_QB_PASSWORD = "CURRENT_QB_PASSWORD";
    public static ENTConversation CURRENT_CONVERSATION;
    public static long LASTMESSAGETIME;
    public static String LASTMESSAGEID;
    public static final String APPLICATION_ID = null;
    public static final String AUTHORIZATION_KEY = null;
    public static final String AUTHORIZATION_SECRET = null;
    public static final String SESSION_TOKEN = "SessionToken";
    public static final String DICTATOR_ID = "DictatorId";
    public static final String DICTATOR_NAME = "DictatorName";
    public static final String UNREAD_MESSAGES_COUNT = "UnreadMessagesCount";

    public static final String JOBLIST_PERMISSION = "joblistpermission";
    public static final String TOUVERSION = "touversion";
    public static final String USERID = "userid";
}
