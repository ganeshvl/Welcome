package com.entradahealth.entrada.android.app.personal.sync;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.entradahealth.entrada.core.remote.APIService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.acra.ACRA;

import java.util.List;
import java.util.Map;

/**
 * Handles the first stage of the sync process. Requests SyncData from
 * the server.
 */
@Deprecated
public class PullTask extends DialogTask<Map<Account, SyncData>>
{
    private final UserState userState;
    private final List<String> errors;
    private final Runnable postUpdateRunnable;
    private final List<Account> accountsToSync;

    private String toastError = null;

    public PullTask(Activity activity,
                    List<Account> accounts,
                    List<String> errorCarryover,
                    Runnable postUpdateRunnable)
    {
        super(activity, "Syncing with server", "", true);

        this.userState = AndroidState.getInstance().getUserState();
        Preconditions.checkNotNull(this.userState, "UserState is null. Should never happen.");

        this.errors = errorCarryover;
        this.accountsToSync = accounts;
        this.postUpdateRunnable = postUpdateRunnable;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected Map<Account, SyncData> doInBackground(Void... voids)
    {
        progress("Initializing sync...");
        Log.d("ENTRADA-SYNC-PERF", "initializing sync.");

        Map<Account, SyncData> result = Maps.newHashMap();

        //synchronized (userState)
       // {
            for (Account account : this.accountsToSync)
            {
                try
                {
                    progress("Syncing " + account.toString() + "...");

                    APIService service = new APIService(account);

                    SyncData data = service.retrieveServiceData();
                    result.put(account, data);
                }
                catch (Exception e)
                {
                    Log.e("Entrada-PullTask", "Failed to sync account " + account, e);
                    ACRA.getErrorReporter().handleSilentException(e);
                    errors.add("Error with '" + account + "': " + e.getClass().getSimpleName());
                    toastError = "Failed to pull from server: " + e.getClass().getSimpleName() +
                            ". Please contact support if this persists.";
                }
            }
        //}

        return result;
    }

    @Override
    protected void onPostExecute(Map<Account, SyncData> result)
    {
        super.onPostExecute(result);

        if (toastError != null)
        {
            Toast.makeText(activity, toastError, Toast.LENGTH_LONG).show();
        }

        for (Map.Entry<Account, SyncData> entry : result.entrySet())
        {
            Account acc = entry.getKey();
            SyncData data = entry.getValue();
            String dataResult = String.format("%d, %d, %d, %d, %d",
                                              data.jobs.size(), data.jobTypes.size(),
                                              data.encounters.size(), data.patients.size(),
                                              data.queues.size());
            //Log.d("Entrada-PullTask", entry.getKey().toString() + ": " + dataResult);
        }

        UpdateTask task = new UpdateTask(activity, result, errors, postUpdateRunnable);
        task.execute();

        Log.d("ENTRADA-SYNC-PERF", "sync complete, update already started.");
    }
}
