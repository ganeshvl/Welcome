package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.util.List;

import javax.annotation.Nullable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class JobListMultiChoiceModeListener implements MultiChoiceModeListener
{
    private final JobListActivity activity;
    private JobListItemAdapter adapter;
	static AlertDialog dgDeleteJob;
	static ActionMode actionMode;

    public JobListMultiChoiceModeListener(JobListActivity activity)
    {
        this.activity = activity;
        this.adapter = (JobListItemAdapter)activity.jobListView.getAdapter();
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
    {
    	actionMode = mode;
        View v = activity.jobListView.getChildAt(position - activity.jobListView.getFirstVisiblePosition());
        if (v != null)
        {
            Log.v("Entrada-MCDebug", String.format("Position: %d, value: %b", position, checked));
            v.setBackgroundColor(activity.getResources().getColor(checked ? R.color.selected_list_item
                                                                          : R.color.unselected_list_item));
        }

        adapter.checks.set(position, checked);

        SparseBooleanArray checks = activity.jobListView.getCheckedItemPositions();
        List<Long> checkedJobs = Lists.newArrayListWithCapacity(adapter.jobIds.size());
        for (int i = 0; i < adapter.checks.size(); ++i)
        {
            if (adapter.checks.get(i)) checkedJobs.add(adapter.jobIds.get(i));
        }

        Menu menu = mode.getMenu();
        MenuItem delete = menu.findItem(R.id.deleteMenuItem);
        MenuItem undelete = menu.findItem(R.id.undeleteMenuItem);


        DomainObjectProvider provider = AndroidState.getInstance()
                                                    .getUserState().getProvider(activity.currentAccount);
        for (long jobId : checkedJobs)
        {
        	try {
	            Job j = provider.getJob(jobId);
	
	            if (j.isFlagSet(Job.Flags.LOCALLY_DELETED))
	                undelete.setVisible(true);
	            else
	                delete.setVisible(true);
        	} catch(Exception e){
        		
        	}
        }

        Log.d("Entrada-Selection", String.format("delete %b, undelete %b", delete.isVisible(), undelete.isVisible()));

        int count = activity.jobListView.getCheckedItemCount();
        String title = (count == 1) ? "1 job selected" : String.valueOf(count) + " jobs selected";
        mode.setTitle(title);
    }

	SparseBooleanArray checks;
	List<Job> checkedJobs;
    @Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		checks = activity.jobListView.getCheckedItemPositions();
		checkedJobs = Lists.newArrayListWithCapacity(checks.size());
		for (int i = 0; i < checks.size(); ++i) {
			Log.d("Entrada-StupidArray", String.valueOf(i) + ": keyAt "
					+ checks.keyAt(i) + " get " + checks.get(i) + " valueAt "
					+ checks.valueAt(i));
			if (checks.valueAt(i)) {
				Object o = activity.jobListView.getItemAtPosition(checks
						.keyAt(i));
				Account acct = AndroidState.getInstance().getUserState()
						.getCurrentAccount();
				Job j = AndroidState.getInstance().getUserState()
						.getProvider(acct).getJob((Long) o);
                checkedJobs.add(j);
            }
        }

		switch (item.getItemId()) {
		case R.id.deleteMenuItem: {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(null);
			if (checks.size() > 1)
				builder.setMessage("Delete " + checks.size() + " Jobs");
			else
				builder.setMessage("Delete " + checks.size() + " Job");
			builder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
                UserState state = AndroidState.getInstance().getUserState();
                Account currentAccount;
                synchronized (state)
                {
                    currentAccount = state.getCurrentAccount();
                }

                // filter out any jobs that have already been started
                List<Job> filteredJobs = Lists.newArrayList(Iterables.filter(checkedJobs, new Predicate<Job>()
                {
                    @Override
                    public boolean apply(@Nullable Job input)
                    {
                        return input != null && !input.isFlagSet(Job.Flags.UPLOAD_COMPLETED) &&
                                                !input.isFlagSet(Job.Flags.UPLOAD_PENDING) &&
                                                !input.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS); 
                    }
                }));

                if (filteredJobs.size() != checkedJobs.size())
                {
                    Toast.makeText(activity, "Jobs with dictations cannot be deleted. To " +
                                             "delete a held job, open the dictation window and Discard it.",
                                   Toast.LENGTH_LONG).show();
                }

                if (filteredJobs.size() > 0)
                {
                	DeleteFlagTask task = new DeleteFlagTask(activity, currentAccount, filteredJobs, true);
                	if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                    	task.execute();
                    }
                }

                mode.finish(); // action picked, so close the CAB
					dialog.dismiss();
				}
			});

			builder.setNegativeButton("Cancel", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mode.finish();
					dialog.dismiss();
				}
			});

			builder.setCancelable(false);
			// builder.show();
			dgDeleteJob = builder.create();
			dgDeleteJob.show();
                return true;
            }
		case R.id.undeleteMenuItem: {
			UserState state = AndroidState.getInstance().getUserState();
			Account currentAccount;
			synchronized (state) {
				currentAccount = state.getCurrentAccount();
			}

			DeleteFlagTask task = new DeleteFlagTask(activity, currentAccount,
					checkedJobs, false);
			task.execute();

			mode.finish(); // action picked, so close the CAB
			return true;
		}
		case R.id.JobList_Contextual_SendToQueue: {
			Toast.makeText(activity, "Not yet implemented.", 2500).show();
			return true;
		}
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.job_list_contextual, menu);
		if ((adapter = (JobListItemAdapter) activity.jobListView.getAdapter()) != null) {
			for (int i = 0; i < adapter.checks.size(); ++i)
				adapter.checks.set(i, false);
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// called when CAB is removed (fixups for leaving CAB mode)
		for (int i = 0; i < adapter.checks.size(); ++i)
			adapter.checks.set(i, false);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// fired when CAB receives invalidate()
		return false;
	}

}
