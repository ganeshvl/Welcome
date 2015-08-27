package com.entradahealth.entrada.android.app.personal.activities.job_list;

import android.util.Log;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;

import java.util.Collection;

/**
 * Marks for server sync (or unsync) all given jobs.
 *
 * @author eropple
 * @since 14 Dec 2012
 */
public class SyncFlagTask extends DialogTask<Void>
{
    private final Collection<Job> jobs;
    private final Account account;
    private final boolean deletionStatus;

    public SyncFlagTask(JobListActivity activity, Account account, Collection<Job> jobs, boolean deletionStatus)
    {
        super(activity, "Marking jobs...",
              String.format("Marking %d jobs...", jobs.size()), false);
        this.jobs = jobs;
        this.account = account;
        this.deletionStatus = deletionStatus;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        try
        {
            UserState state = AndroidState.getInstance().getUserState();
            synchronized (state)
            {
                DomainObjectProvider provider = state.getProvider(account);

                for (Job job : jobs)
                {
                    Log.d("Entrada-SyncFlagTask", "Attempting to (un)sync job " + job.id);

					Job changedJob = deletionStatus ? provider.updateJob(job
							.clearFlag(Job.Flags.CLEAR_LOCAL_CHANGES_ON_SYNC)
							.clearFlag(Job.Flags.LOCALLY_DELETED)) : provider
							.updateJob(job.setFlag(
									Job.Flags.CLEAR_LOCAL_CHANGES_ON_SYNC)
									.clearFlag(Job.Flags.LOCALLY_DELETED));
					Log.d("Entrada-SyncFlagTask", provider
							.getJob(changedJob.id).toString());
				}
			}
		} catch (Exception ex) {
			progress(ProgressUpdateType.TOAST,
					"Unhandled exception: " + ex.toString());
			Log.d("Entrada-SyncFlagTask", "Sync marking failed: ", ex);
		}

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        // TODO: the refresh does not appear to be working.

        ((JobListActivity)activity).launchSearchTask();
    }
}

