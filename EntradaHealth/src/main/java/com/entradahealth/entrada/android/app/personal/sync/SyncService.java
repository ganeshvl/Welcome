package com.entradahealth.entrada.android.app.personal.sync;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.acra.ACRA;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.domain.senders.UploadData;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.remote.APIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class SyncService extends IntentService
{
	private static final String LOG_NAME = "Sync-Service";
	private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TAG = "Entrada-Sync-Service";
    private EntradaApplication application;

    private static boolean running = false;
    public static boolean isRunning()
    {
        return running;
    }

    private static Multimap<Account, String> userFacingErrors;
    public static ImmutableMultimap<Account, String> getUserFacingErrors()
    {
        return ImmutableMultimap.copyOf(userFacingErrors);
    }

    private static String currentStatus = "";
    public static String getCurrentStatus() { return currentStatus; }

    public SyncService()
    {
        super(TAG);
        application = (EntradaApplication) EntradaApplication.getAppContext();
        if (running) throw new RuntimeException("Can't create two SyncServices.");
        	currentStatus = "Preparing to start sync...";
        userFacingErrors = ArrayListMultimap.create();
        running = true;
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final UserState userState = AndroidState.getInstance().getUserState();
        Intent i = new Intent("my-event");
        i.putExtra("message", BundleKeys.SYNC_SERVICE_START);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        NotificationManager notificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setContentTitle("Performing sync for " + BundleKeys.SYNC_FOR_ACC)
                .setContentText("Performing sync.");
        notificationMgr.notify(R.string.notification_sync_service, builder.getNotification());

        try
        {

            Log.d(TAG, "initializing push.");
            List<Account> pushResult = push(userState);
            Map<Account, SyncData> pullResult = pull(pushResult, userState);
            update(pullResult, userState);
        }
        finally
        {
            running = false;
            notificationMgr.cancel(R.string.notification_sync_service);
            Intent newi = new Intent("my-event");
            newi.putExtra("message", BundleKeys.SYNC_SERVICE_END);
            LocalBroadcastManager.getInstance(this).sendBroadcast(newi);
        }
    }


    private List<Account> push(final UserState userState)
    {
        List<Account> result = Lists.newArrayList();
//        synchronized (userState)
//        {
            for (Account account : userState.getAccounts())
            {
            	if(account.getName().equals(application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID))) {
                try
                {
                		currentStatus = ("Pushing " + account.toString() + "...");
                	Log.e("currentStatus", currentStatus);
                    DomainObjectProvider provider = userState.getProvider(account);
                    
                    APIService service = new APIService(account);
                    
                    SyncData data = service.retrieveServiceData();
                    //provider.writeSyncData(data);
                    if(!BundleKeys.QUEUE_CHANGED){
                    queues = Lists.newArrayList(data.queues);
                    	try {
                            provider.writeQueues(queues);
                    
                        } catch (DomainObjectWriteException e) {
                            e.printStackTrace();  // TODO: Handle this exception type.
                        }
                    }
                    
                    /*for(int i = 0;i<queues.size();i++){
                    	queue = queues.get(i);
                    	if(queue.id == data.dictatorInfo.defaultQueueID){//&& queue.isDictatorQueue){ 
                    		queue.isSubscribed = true;
                    	}
                    }*/
                    
                    /*try {
                        provider.writeQueues(queues);
                        
                    } catch (DomainObjectWriteException e) {
                        e.printStackTrace();  // TODO: Handle this exception type.
                    }*/
                    
                    Collection<Queue> subscribedQueues = Collections2.filter(provider.getQueues(), new Predicate<Queue>() {
                        @Override
                        public boolean apply(@Nullable Queue queue) {
                            return queue != null && queue.isSubscribed;
                        }
                    });
                    if(data.systemSettings.expressQueues)
                    service.subscribeToExpressQueues(subscribedQueues);

                            List < Job > jobs = Lists.newArrayList();
                    List<Dictation> dictations = Lists.newArrayList();
                    // TODO: Sort jobs by STAT field, possibly other priority criteria
                    
                    
                    for (Job j : provider.getJobs()) {
                    	if ((j.isFlagSet(Job.Flags.LOCALLY_MODIFIED) || j.isFlagSet(Job.Flags.LOCALLY_DELETED))
                                && !j.isFlagSet(Job.Flags.HOLD) && !j.isFlagSet(Job.Flags.LOCALLY_CREATED)
                                && !j.isPending() && !j.isComplete()) {
                        	
                            jobs.add(j);
                            
                            for (Dictation d : provider.getDictationsByJob(j.id)) {
                                dictations.add(d);
                            }
                        }
                    }

                    UploadData dat = new UploadData(jobs, dictations);
                    service.sendServiceData(dat);
                    
                    for (Job j : jobs) {
                        provider.updateJob(j.clearFlag(Job.Flags.LOCALLY_MODIFIED));
                    }
                    
                    // If everything was successful to this point, add the account to the result list so PullTask will sync it.
                    result.add(account);
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Failed to push data for account " + account, e);
                    ACRA.getErrorReporter().handleSilentException(e);
                    //                    errors.add("Error with '" + account + "': " + e.getClass().getSimpleName());
                    userFacingErrors.put(account, "Failed to push to server: " + e.getClass().getSimpleName());
                }
                }
            }
 //       }

        return result;
    }

    DomainObjectProvider provider = null;
    ArrayList<Queue> queues = null;
    Queue queue;

    private Map<Account, SyncData> pull(List<Account> accountsToSync, final UserState userState)
    {
        Map<Account, SyncData> result = Maps.newHashMap();

//        synchronized (userState)
//        {
            for (Account account : accountsToSync)
            {
            	if(account.getName().equals(application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID))) {
                try
                {
                		currentStatus = ("Syncing " + account.toString() + "...");
                	Log.e("currentStatus", currentStatus);
                		Log.e(LOG_NAME, "Started sync with server");

                	APIService service = new APIService(account);
                    
                	SyncData data = service.retrieveServiceData();
                    result.put(account, data);
                    
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Failed to sync account " + account, e);
                    ACRA.getErrorReporter().handleSilentException(e);
                    userFacingErrors.put(account, "Failed to pull from server: " + e.getClass().getSimpleName() +
                            ".");
                }
            }
            }
    //    }

        return result;
    }

    private void update(Map<Account, SyncData> pullResult, final UserState userState)
    {
//        synchronized (userState)
//        {
            for(Map.Entry<Account, SyncData> entry : pullResult.entrySet())
            {
                Account account = entry.getKey();
                SyncData data = entry.getValue();
            	if(account.getName().equals(application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID))) {

            		currentStatus = "Finalizing " + account.toString() + "...";
                Log.e("currentStatus", currentStatus);
                try
                {
                    final DomainObjectProvider provider = userState.getProvider(account);
                    SMDomainObjectWriter smProvider = null;
                    try{
                    	smProvider = userState.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
                    } catch(Exception ex){
                    	ex.printStackTrace();
                    }
                    Preconditions.checkNotNull(provider, "Provider for '" + account + "' is null.");

                    final Map<Long, Job> heldJobs = Maps.uniqueIndex(provider.getJobsByAnyFlags(Job.Flags.HOLD.value),
                                                                       new Function<Job, Long>() {
                        @Nullable
                        @Override
                        public Long apply(@Nullable Job input)
                        {
                            return input.id;
                        }
                    });

                    final Map<Long, Encounter> heldEncounters = Maps.uniqueIndex(
                                Iterables.filter(provider.getEncounters(),
                                    new Predicate<Encounter>() {
                                        @Override
                                        public boolean apply(@Nullable Encounter input)
                                        {
                                            return heldJobs.containsKey(input.id);
                                        }
                                    }
                                ),
                                new Function<Encounter, Long>() {
                                    @Nullable
                                    @Override
                                    public Long apply(@Nullable Encounter input)
                                    {
                                        return input.id;
                                    }
                                }
                            );

                    final Map<Long, Patient> heldPatients = Maps.uniqueIndex(Iterables.filter(
                                    provider.getPatients(),
                                    new Predicate<Patient>() {
                                        @Override
                                        public boolean apply(@Nullable Patient input)
                                        {
                                            return heldEncounters.containsKey(input.id);
                                        }
                                    }
                                ),
                                new Function<Patient, Long>() {
                                    @Nullable
                                    @Override
                                    public Long apply(@Nullable Patient input)
                                    {
                                        return input.id;
                                    }
                                }
                            );
                    
                    
                    provider.writeSyncData(data);
                    try {
                    	smProvider.writePatients(data.patients);
                    } catch(Exception ex){
                    	ex.printStackTrace();
                    }
                    provider.writePatients(Iterables.filter(heldPatients.values(),
                                                            new Predicate<Patient>() {
                                                                @Override
                                                                public boolean apply(@Nullable Patient input)
                                                                {
                                                                    // if they're not in heldPatientIds, we need to restore them to avoid a deletion anomaly.
                                                                    return provider.getPatient(input.id) == null;
                                                                }
                                                            }));
                    provider.writeEncounters(Iterables.filter(heldEncounters.values(),
                                                              new Predicate<Encounter>() {
                                                                  @Override
                                                                  public boolean apply(@Nullable Encounter input)
                                                                  {
                                                                      return provider.getEncounter(input.id) != null;
                                                                  }
                                                              }));

                    provider.writeJobs(Iterables.transform(Iterables.filter(provider.getJobs(),
                            new Predicate<Job>() {
                                @Override
                                public boolean apply(@Nullable Job input) {
                                    return heldJobs.containsKey(input.id);
                                }
                            }
                    ),
                            new Function<Job, Job>() {
                                @Nullable
                                @Override
                                public Job apply(@Nullable Job input) {
                                    final Job old = heldJobs.get(input.id);
                                    return input.setStat(old.stat)
                                            .setJobTypeId(old.jobTypeId)
                                            .setFlagToValue(Job.Flags.HOLD, old.isFlagSet(Job.Flags.HOLD));
                                }
                            }
                    )
                    );
                    String defaultJobType = account.getSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID);
                    if(defaultJobType == null) {
                    	JobType jType = provider.getDefaultGenericJobType();
                    	account.putSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID, String.valueOf(jType.id));
                    } else {
                    	if(!provider.isExistsInDefaultGenericJobTypes(Long.valueOf(defaultJobType))){
                        	JobType jType = provider.getDefaultGenericJobType();
                        	account.putSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID, String.valueOf(jType.id));
                    	}
                    }
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Failed to update account " + account, e);
                    userFacingErrors.put(account, "Error during update: " + e.getClass().getSimpleName());
                }
                BundleKeys.QUEUE_CHANGED = false;
                account.putSetting(AccountSettingKeys.GENERIC_PATIENT_ID,
                                   String.valueOf(data.systemSettings.genericPatientID));
                account.putSetting(AccountSettingKeys.DEFAULT_QUEUE_ID,
                        String.valueOf(data.dictatorInfo.defaultQueueID));
                account.putSetting(AccountSettingKeys.EXPRESS_QUEUES,
                        String.valueOf(data.systemSettings.expressQueues));
				account.putSetting(AccountSettingKeys.IMAGE_CAPTURE,
						String.valueOf(data.systemSettings.captureEnabled));
				account.putSetting(AccountSettingKeys.PATIENT_CLINICALS,
						String.valueOf(data.systemSettings.clinicalsEnabled));
                BundleKeys.CURR_ACCOUNT = account;
                if(BundleKeys.CURR_ACCOUNT.getApiHost().equalsIgnoreCase("dictateapi.entradahealth.net")){
              		BundleKeys.SECURE_MSG = true;
              	} 
            }
            }

            try
            {
                userState.getUserData().save();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error saving userdata after update pass: ", e);
                throw new RuntimeException(e);
            }
        //}
    
    SharedPreferences sp = getSharedPreferences("Entrada",
			Context.MODE_WORLD_READABLE);
    
  //Set date and time for Last update fields
    Date date;
    date = new Date();
    
		String strDateFormat = "h:mm a";
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		BundleKeys.last_updated_time = sdf.format(date);
		
	
		String strDateFormat1 = "MM-dd-yy";
		SimpleDateFormat sdf1 = new SimpleDateFormat(strDateFormat1);
		BundleKeys.last_updated_date = sdf1.format(date);
		
		
		Editor edit = sp.edit();
		edit.putString("last_updated_time", sdf.format(date));
		edit.putString("last_updated_date", sdf1.format(date));
		edit.commit();
		
		
    }
}
