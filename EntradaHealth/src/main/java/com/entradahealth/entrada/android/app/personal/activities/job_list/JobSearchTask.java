package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Asynchronous task for pulling jobs from the job provider. A blank
 * string should pull all jobs; any other string should pull any job
 * that meets all of the space-separated tokens in the search string.
 */
public class JobSearchTask extends AsyncTask<Void, Void, List<Job>>
{
    protected final JobListActivity activity;
    protected final String searchText;
    protected final Account account;
    protected final Predicate<Job> filter;
    protected final Comparator<Job> comparator;
    ProgressDialog pDialog;
    int ct;//count for results returned from search
    private EntradaApplication application;

    public JobSearchTask(JobListActivity activity, String searchText, Account account,
                         Predicate<Job> filter, Comparator<Job> comparator)
    {
        this.activity = activity;
        this.searchText = searchText;
        this.account = account;
        this.filter = filter;
        this.comparator = comparator;
        SharedPreferences sp = activity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        application = (EntradaApplication) EntradaApplication.getAppContext();
    }
    
	@Override
	protected List<Job> doInBackground(Void... voids) {
		Log.d("Entrada-JobSearchTask", String.format(
				"Search started: '%s', account '%s'", searchText,
				account.getName()));

		UserState state = AndroidState.getInstance().getUserState();
		List<Job> jobs;
		DomainObjectReader reader = state.getProvider(account);
		jobs =  (reader != null) ? reader.searchJobs(searchText)
					: ImmutableList.<Job> of();
		Job[] array = new Job[jobs.size()];
		jobs.toArray(array); // fill the array

		for (int i = 0; i < array.length; i++) {
			Log.i("jobs2array", array[i].toString());
		}

		Log.i("Total_Jobs", Integer.toString(array.length));

			// Save locally deleted jobs, encounters to a static list

			BundleKeys.Held_Encounter_IDs = new ArrayList<Long>();
			BundleKeys.Deleted_Encounter_IDs = new ArrayList<Long>();
			BundleKeys.Deleted_Date = new ArrayList<DateTime>();
			BundleKeys.Completed_Encounter_IDs = new ArrayList<Long>();

			BundleKeys.List_Local_Jobs = reader.getLocalJobs(1);
			BundleKeys.List_Local_Encounters = reader.getEncounters();
			BundleKeys.List_Local_Del_Jobs = reader.getDeletedJobs(2);
			BundleKeys.List_Completed_Jobs = reader.getCompletedJobs(64);
			BundleKeys.List_Held_Jobs = reader.getHeldJobs(16);

			BundleKeys.List_Local_Del_Encounters = new ArrayList<Encounter>();
			BundleKeys.List_Completed_Encounters = new ArrayList<Encounter>();
			BundleKeys.List_Held_Encounters = new ArrayList<Encounter>();

			if (!BundleKeys.IS_CLEAR) {

				// get the encounter IDs of completed jobs
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.UPLOAD_COMPLETED)) {
						if (!BundleKeys.Completed_Encounter_IDs.contains(jobs
								.get(i).encounterId)) {
							BundleKeys.Completed_Encounter_IDs
									.add(jobs.get(i).encounterId);
						}
					}
				}
				if (BundleKeys.List_Completed_Jobs != null
						&& BundleKeys.List_Completed_Jobs.size() > 0
						&& !BundleKeys.List_Completed_Jobs.isEmpty()) {
					for (int i = 0; i < BundleKeys.Completed_Encounter_IDs
							.size(); i++) {
						try{
							Encounter enc = reader
									.getEncounter(BundleKeys.List_Completed_Jobs
											.get(i).encounterId);
							if (enc != null)
								BundleKeys.List_Completed_Encounters.add(enc);
						} catch(Exception e){
							Log.e("Job Search Task","Error in List_Completed_Encounters");
						}
					}
				}

				// get the encounter IDs of held jobs
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.HOLD)
							&& !jobs.get(i).isFlagSet(Flags.LOCALLY_DELETED)) { 
						if (!BundleKeys.Held_Encounter_IDs
								.contains(jobs.get(i).encounterId)) {
							BundleKeys.Held_Encounter_IDs
									.add(jobs.get(i).encounterId);
            			}
            		}
            	}
				
				if (BundleKeys.List_Held_Jobs != null
						&& BundleKeys.List_Held_Jobs.size() > 0
						&& !BundleKeys.List_Held_Jobs.isEmpty()) {
					for (int i = 0; i < BundleKeys.Held_Encounter_IDs.size(); i++) {
						Encounter enc = reader
								.getEncounter(BundleKeys.List_Held_Jobs.get(i).encounterId);
						if (enc != null)
							BundleKeys.List_Held_Encounters.add(enc);
					}
				}

				// Deleted and held job
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.HOLD)
							&& jobs.get(i).isFlagSet(Flags.LOCALLY_DELETED)) {
						if (BundleKeys.Held_Encounter_IDs
								.contains(jobs.get(i).encounterId)) {
							BundleKeys.Held_Encounter_IDs
									.remove(jobs.get(i).encounterId);
						}

						if (BundleKeys.List_Held_Jobs != null
								&& BundleKeys.List_Held_Jobs.size() > 0
								&& !BundleKeys.List_Held_Jobs.isEmpty()) {
							for (int j = 0; i < BundleKeys.Held_Encounter_IDs
									.size(); j++) {
								Encounter enc = reader
										.getEncounter(BundleKeys.List_Held_Jobs
												.get(j).encounterId);
								if (enc != null)
									BundleKeys.List_Held_Encounters.remove(enc);
							}
						}
					}
				}

			} else {
				// get the encounter IDs of locally deleted jobs
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.LOCALLY_DELETED)
							|| (jobs.get(i).isFlagSet(Flags.HOLD) && jobs
									.get(i).isFlagSet(Flags.LOCALLY_DELETED))) {
						if (BundleKeys.Deleted_Encounter_IDs.contains(jobs
								.get(i).encounterId)) {
							BundleKeys.Deleted_Encounter_IDs
									.remove(jobs.get(i).encounterId);
						}
					}
				}
				if (BundleKeys.List_Local_Del_Jobs != null
						&& BundleKeys.List_Local_Del_Jobs.size() > 0
						&& !BundleKeys.List_Local_Del_Jobs.isEmpty()) {
					for (int i = 0; i < BundleKeys.Deleted_Encounter_IDs.size(); i++) {
						Encounter enc = reader
								.getEncounter(BundleKeys.List_Local_Del_Jobs
										.get(i).encounterId);
						if (enc != null) {
							BundleKeys.List_Local_Del_Encounters.remove(enc);
						}
					}
				}

				// get the encounter IDs of completed jobs
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.UPLOAD_COMPLETED)) {
						if (BundleKeys.Completed_Encounter_IDs.contains(jobs
								.get(i).encounterId)) {
							BundleKeys.Completed_Encounter_IDs.remove(jobs
									.get(i).encounterId);
						}
					}
				}
				if (BundleKeys.List_Completed_Jobs != null
						&& BundleKeys.List_Completed_Jobs.size() > 0
						&& !BundleKeys.List_Completed_Jobs.isEmpty()) {
					for (int i = 0; i < BundleKeys.Completed_Encounter_IDs
							.size(); i++) {
						Encounter enc = reader
								.getEncounter(BundleKeys.List_Completed_Jobs
										.get(i).encounterId);
						if (enc != null)
							BundleKeys.List_Completed_Encounters.remove(enc);
					}
				}

				// get the encounter IDs of held jobs
				for (int i = 0; i < jobs.size(); i++) {
					if (jobs.get(i).isFlagSet(Flags.HOLD)) {
						if (BundleKeys.Held_Encounter_IDs
								.contains(jobs.get(i).encounterId)) {
							BundleKeys.Held_Encounter_IDs
									.remove(jobs.get(i).encounterId);
						}
					}
				}
				if (BundleKeys.List_Held_Jobs != null
						&& BundleKeys.List_Held_Jobs.size() > 0
						&& !BundleKeys.List_Held_Jobs.isEmpty()) {
					for (int i = 0; i < BundleKeys.Held_Encounter_IDs.size(); i++) {
						Encounter enc = reader
								.getEncounter(BundleKeys.List_Held_Jobs.get(i).encounterId);
						if (enc != null)
							BundleKeys.List_Held_Encounters.remove(enc);
					}
				}

			}

		
		if (BundleKeys.List_Held_Jobs != null)
			Log.e("Job_Search...list_held_jobs", BundleKeys.List_Held_Jobs.toString());
		if (BundleKeys.List_Held_Encounters != null)
			Log.e("Job_Search...List_Held_Encounters",
					BundleKeys.List_Held_Encounters.toString());
		
        jobs = Lists.newArrayList(Iterables.filter(jobs, filter));
        List<Encounter> encounters = reader.getEncounters();
        for(Encounter encounter : encounters){
        	application.addEncounter(encounter.getId(), encounter);
        }
        Log.e("", "Encounters count--"+application.getEncounters().size());
        Collections.sort(jobs, comparator);
        List<Long> jobIds = Lists.newArrayList(Iterables.transform(jobs, new Function<Job, Long>()
			        {
			            @Nullable
			            @Override
			            public Long apply(@Nullable Job input)
			            {
			                return input.id;
			            }
			        }));
			        
			        
		activity.setJobIdList(jobIds);
		switch (BundleKeys.which) {

		case 1:
			BundleKeys.TODAY_COUNT = jobIds.size();
			break;
		case 2:
			BundleKeys.TOMORROW_COUNT = jobIds.size();
			break;
		case 3:
			BundleKeys.STAT_COUNT = jobIds.size();
			break;
		case 4:
			BundleKeys.ALL_COUNT = jobIds.size();
			break;
		case 5:
			BundleKeys.HOLD_COUNT = jobIds.size();
			break;
		case 6:
			BundleKeys.COMPLETED_COUNT = jobIds.size();
			break;

		default:
			// BundleKeys.TODAY_COUNT = jobIds.size();
		}
        return jobs;
        
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		Log.d("Entrada-JobSearchTask",
				String.format("Cancelled: '%s'", searchText));
	}

	@Override
	protected void onCancelled(List<Job> jobs) {
		super.onCancelled(jobs);
		Log.d("Entrada-JobSearchTask",
				String.format("Cancelled: '%s'", searchText));
	}

	@Override
	protected void onPostExecute(List<Job> jobs) {
		super.onPostExecute(jobs);
		if(!isCancelled()) {
	        Intent i = new Intent("my-event");
	        i.putExtra("message", BundleKeys.JOB_SEARCH_TASK);
	        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
		}
	}


}
