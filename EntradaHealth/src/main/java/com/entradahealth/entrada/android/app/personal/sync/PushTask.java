package com.entradahealth.entrada.android.app.personal.sync;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.senders.UploadData;
import com.entradahealth.entrada.core.remote.APIService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.acra.ACRA;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 1/4/13
 */
@Deprecated
public class PushTask extends DialogTask<List<Account>> {
    private final UserState userState;
    private final List<String> errors;
    private final Runnable postUpdateRunnable;

    private String toastErrors = null;

    public PushTask(Activity activity, Runnable postUpdateRunnable)
    {
        super(activity, "Syncing with server", "", true);

        this.userState = AndroidState.getInstance().getUserState();
        Preconditions.checkNotNull(this.userState, "UserState is null. Should never happen.");

        this.errors = Lists.newArrayList();
        this.postUpdateRunnable = postUpdateRunnable;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected List<Account> doInBackground(Void... voids)
    {
        progress("Initializing push...");
        Log.d("ENTRADA-PUSH-PERF", "initializing push.");

        List<Account> result = new LinkedList<Account>();

      //  synchronized (userState)
        //{
            for (Account account : userState.getAccounts())
            {
                try
                {
                    progress("Pushing " + account.toString() + "...");

                    APIService service = new APIService(account);

                    List<Job> jobs = new LinkedList<Job>();
                    List<Dictation> dictations = new LinkedList<Dictation>();
                    // TODO: Sort jobs by STAT field, possibly other priority criteria
                    for (Job j : userState.getProvider(account).getJobs()) {
                        if ((j.isFlagSet(Job.Flags.LOCALLY_MODIFIED) || j.isFlagSet(Job.Flags.LOCALLY_DELETED))
                                && !j.isFlagSet(Job.Flags.HOLD)) {

                            jobs.add(j);

                            for (Dictation d : userState.getProvider(account).getDictationsByJob(j.id)) {
                                dictations.add(d);
                            }
                        }
                    }

                    UploadData dat = new UploadData(jobs, dictations);
                    service.sendServiceData(dat);

                    for (Job j : jobs) {
                        userState.getProvider(account).updateJob(j.clearFlag(Job.Flags.LOCALLY_MODIFIED));
                    }

                    Log.d("Entrada-PushTask", dat.toString());

                    // If everything was successful to this point, add the account to the result list so PullTask will sync it.
                    result.add(account);
                }
                catch (Exception e)
                {
                    Log.e("Entrada-PushTask", "Failed to push data for account " + account, e);
                    ACRA.getErrorReporter().handleSilentException(e);
                    errors.add("Error with '" + account + "': " + e.getClass().getSimpleName());
                    toastErrors = "Failed to push to server: " + e.getClass().getSimpleName() +
                            ". Please contact support if this persists.";
                }
            }
       // }

        return result;
    }

    @Override
    protected void onPostExecute(List<Account> result)
    {
        super.onPostExecute(result);

        if (toastErrors != null)
        {
            Toast.makeText(activity,
                           toastErrors,
                           Toast.LENGTH_LONG).show();
        }

        PullTask task = new PullTask(activity, result, errors, postUpdateRunnable);
        task.execute();

        Log.d("ENTRADA-PUSH-PERF", "push complete, sync already started.");
    }
}
