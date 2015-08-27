package com.entradahealth.entrada.android.app.personal.activities.job_list;

import android.os.AsyncTask;

/**
 * Runs while JobListActivity is visible to refresh
 */
public class JobListRefreshTask extends AsyncTask<Void, Void, Void>
{
    public final JobListActivity activity;

    public JobListRefreshTask(JobListActivity activity)
    {
        this.activity = activity;
    }


    @Override
    protected Void doInBackground(Void... params)
    {
        try
        {
            while (true)
            {
                publishProgress();
                Thread.sleep(250);
            }
        }
        catch (InterruptedException ignored) {}

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        if (activity.isSyncing ||
            (activity.searchTask != null && Status.RUNNING.equals(activity.searchTask.getStatus()))) return;


        JobListItemAdapter adapter = (JobListItemAdapter)(activity.jobListView.getAdapter());
        if (adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
    }
}
