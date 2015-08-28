package com.entradahealth.entrada.android.app.personal.sync;

import android.app.Activity;
import android.util.Log;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.retrievers.SyncData;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The second part of the sync process. Takes as input a map
 * of account and sync data, which will then be injected into
 * the database.
 *
 * @author edr
 * @since 8 Nov 2012
 */
@Deprecated
public class UpdateTask extends DialogTask<Void> {
	private final Map<Account, SyncData> data;
	private final UserState userState;
	private final List<String> errors;
	private final Runnable postExecute;

    public UpdateTask(Activity activity, Map<Account, SyncData> data,
			List<String> errorCarryover, Runnable postExecute) {
		super(activity, "Updating local database", "", false);
		this.userState = AndroidState.getInstance().getUserState();
		this.data = data;
		this.errors = errorCarryover;
		this.postExecute = postExecute;
	}

    @Override
	protected Void doInBackground(Void... voids) {
		progress("Finalizing sync...");
		Log.d("ENTRADA-SYNC-PERF", "starting update task.");

		synchronized (userState) {
			for (Map.Entry<Account, SyncData> entry : data.entrySet()) {
				Account account = entry.getKey();
				SyncData data = entry.getValue();
				try {
					DomainObjectProvider provider = userState
							.getProvider(account);
					Preconditions.checkNotNull(provider, "Provider for '"
							+ account + "' is null.");

                    provider.writeSyncData(data);
				} catch (Exception e) {
					Log.e("Entrada-UpdateTask", "Failed to update account "
							+ account, e);
					errors.add("Error with '" + account + "': "
							+ e.getClass().getSimpleName());
				}

                account.putSetting(AccountSettingKeys.GENERIC_PATIENT_ID,
                                   String.valueOf(data.systemSettings.genericPatientID));
                account.putSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID,
                                   String.valueOf(data.dictatorInfo.defaultJobTypeID));
            }

			try {
				userState.getUserData().save();
			} catch (IOException e) {
				Log.e("Entrada-UpdateTask",
						"Error saving userdata after UpdateTask: ", e);
				throw new RuntimeException(e);
			}
		}

        return null;
    }

    @Override
    protected void onPostExecute(Void v)
    {
        super.onPostExecute(v);
        Log.d("ENTRADA-SYNC-PERF", "update task finished.");

        if (this.postExecute != null) this.postExecute.run();
    }
}
