package com.entradahealth.entrada.android.app.personal.sync;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.auth.Account;
import com.google.common.base.Strings;

public class SyncServiceMinderTask extends DialogTask<Void> {
	private Runnable runAtCompletion;
	private JobListActivity jl_activity;

	public SyncServiceMinderTask(Activity activity, Runnable runAtCompletion) {
		super(activity, "Syncing with server", "Initializing...", false);
		this.runAtCompletion = runAtCompletion;
		this.jl_activity = (JobListActivity) activity;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String lastCurrentStatus = "";

		if (!SyncService.isRunning()) {
			Intent i = new Intent(activity, SyncService.class);
			activity.startService(i);

		}

		try {
			while (!SyncService.isRunning()) {
				// spin until the service is actually up
				Thread.sleep(100);
			}

			while (SyncService.isRunning()) {
				String status = SyncService.getCurrentStatus();
				if (!status.equals(lastCurrentStatus)
						&& !Strings.isNullOrEmpty(status)) {
					progress(ProgressUpdateType.DIALOG, status);
				}
				Thread.sleep(250);
			}
		} catch (InterruptedException ex) {
			return null;
		}

		if (runAtCompletion != null)
			runAtCompletion.run();

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		this.dialog.dismiss();
		TextView tvDate = (TextView) activity.findViewById(R.id.tvDate);
		tvDate.setText(BundleKeys.last_updated_date);
		TextView tvTime = (TextView) activity.findViewById(R.id.tvTime);
		tvTime.setText(BundleKeys.last_updated_time);
		RelativeLayout rlUpdated = (RelativeLayout) activity
				.findViewById(R.id.rlDateTime);
		UserState state = AndroidState.getInstance().getUserState();
		Account acc = state.getCurrentAccount();
        String value = acc.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
        if (value == null)
        	rlUpdated.setVisibility(View.INVISIBLE);
        else
        	rlUpdated.setVisibility(View.VISIBLE);
        
        /*if(!BundleKeys.GOT_QUEUES){
        	this.jl_activity.SyncData();
        	BundleKeys.GOT_QUEUES = true;
        }*/
        
	}
}
