package com.entradahealth.entrada.android.app.personal.activities.add_job;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;

/**
 * Adds a job with a temporary job ID and sets the LOCALLY_CREATED flag.
 *
 * @author edr
 * @since 23 Apr 2013
 */
public class AddJobTask extends DialogTask<Job> {
	private final Patient patient;
	private final boolean isGeneric;
	Account currentAccount = null;

    protected AddJobTask(Activity activity, Patient patient, boolean isGeneric)
    {
        super(activity, "Creating new job", "Working...", false);
        this.patient = patient;
        this.isGeneric = isGeneric;
    }

    @Override
    protected Job doInBackground(Void... voids)
    {
        UserState state = AndroidState.getInstance().getUserState();

        synchronized (state)
        {
            DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());

            try
            {
                Encounter e = writer.createNewEncounter(patient);
                return writer.createNewJob(e);
            } catch (DomainObjectWriteException e1)
            {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }
        }
    }

    @Override
    protected void onPostExecute(Job job)
    {
        super.onPostExecute(job);
        this.dialog.dismiss();
        UserState state = AndroidState.getInstance().getUserState();

		synchronized (state) {
			Intent i;
			if (isGeneric) {
				i = new Intent(activity, JobDisplayActivity.class);
				Bundle b = new Bundle();
				job = job.setFlag(Flags.IS_FIRST);
				try {
					currentAccount = state.getCurrentAccount();
					AndroidState.getInstance().getUserState().getProvider(currentAccount).updateJob(job);
				} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				b.putLong(BundleKeys.SELECTED_JOB, job.id);
				b.putString(BundleKeys.SELECTED_JOB_ACCOUNT, state
						.getCurrentAccount().getName());
				b.putBoolean("isGeneric", true);
				b.putBoolean("isFirst", true);
                i.putExtras(b);

            }else{
            	i = new Intent(activity, NewJob.class);
                Bundle b = new Bundle();
                b.putLong(BundleKeys.SELECTED_JOB, job.id);
				b.putString(BundleKeys.SELECTED_JOB_ACCOUNT, state
						.getCurrentAccount().getName());
				b.putBoolean("isFirst", true);
                i.putExtras(b);

            }
            activity.startActivity(i);
            activity.finish();
        }
    }
}
