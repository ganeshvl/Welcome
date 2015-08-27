package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.io.File;
import java.util.Collection;
import android.os.AsyncTask;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_display.CaptureImages.AsyncTaskDeleteFiles;
import com.entradahealth.entrada.android.app.personal.activities.job_display.CaptureImages.AsyncTaskLoadFiles;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;

/**
 * Marks for deletion (or undeletion) all given jobs.
 *
 * @author eropple
 * @since 14 Dec 2012
 */
public class DeleteFlagTask extends DialogTask<Void> {
	AsyncTaskDeleteFiles myAsyncTaskDeleteFiles;
    private final Collection<Job> jobs;
    private final Account account;
    private final boolean deletionStatus;
	private UserState state;

    public DeleteFlagTask(JobListActivity activity, Account account, Collection<Job> jobs, boolean deletionStatus)
    {
        super(activity, "Deleting jobs...",
              String.format("Marking %d jobs for deletion...", jobs.size()), false);
        this.jobs = jobs;
        this.account = account;
        this.deletionStatus = deletionStatus;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
    	Log.i("Delete Job", "inside doInBackground");
		try {
			state = AndroidState.getInstance().getUserState();
			synchronized (state) {
                DomainObjectProvider provider = state.getProvider(account);
                DomainObjectReader reader = state.getProvider(account);

				for (Job job : jobs) {
					Log.d("Entrada-DeleteFlagTask",
							"Attempting to (un)delete job " + job.id);
					job = provider.updateJob(job.clearFlag(Job.Flags.HOLD));

					Job changedJob = deletionStatus ? provider.updateJob(job
							.setFlag(Job.Flags.LOCALLY_DELETED).clearFlag(
									Job.Flags.CLEAR_LOCAL_CHANGES_ON_SYNC))
							: provider.updateJob(job
									.clearFlag(Job.Flags.LOCALLY_DELETED));

                    for (Dictation d : provider.getDictationsByJob(changedJob.id)) {
                        provider.writeDictation(d.setStatus(Dictation.Status.DELETED));
                    }

                    BundleKeys.SYNC_AFTER_DELETE = false;
                    Log.d("Entrada-DeleteFlagTask", provider.getJob(changedJob.id).toString());
					// Delete folder with images and dictaion
					myAsyncTaskDeleteFiles = new AsyncTaskDeleteFiles();
					myAsyncTaskDeleteFiles.execute(job.id);
                }
                
                Log.e("del_jobs-count", Integer.toString(jobs.size()));
            }
        }
        catch(Exception ex)
        {
            progress(ProgressUpdateType.TOAST, "Unhandled exception: " + ex.toString());
            Log.d("Entrada-DeleteFlagTask", "Deletion failed: ", ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        this.dialog.dismiss();
        // TODO: the refresh does not appear to be working.
        Log.i("Delete Job", "PostExecute");
        ((JobListActivity)activity).launchSearchTask();
    }
	public class AsyncTaskDeleteFiles extends AsyncTask<Long, String, Void> {

		public AsyncTaskDeleteFiles() {
		}

		@Override
		protected Void doInBackground(Long... params) {
			Long jobId = params[0];
			File accountPath = new File(state.getUserData()
					.getUserAccountsDir(), account.getName());
			File dbPath = new File(accountPath, String.valueOf(jobId));
			DeleteRecursive(dbPath);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
}

	}

	void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}
}
