package com.entradahealth.entrada.android.app.personal.activities.add_job;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.PatientSearchFrament.FragmentCallback;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Takes a name or MRN as search string, returns a sorted list of patients that
 * match it.
 *
 * @author edr
 * @since 8 Apr 2013
 */
public class PatientSearchTask extends AsyncTask<Void, Void, Collection<? extends Patient>>
{
    private final String searchText;
    private final AddJobActivity activity;
    

    public PatientSearchTask(String searchText, AddJobActivity addJobActivity)
    {
        this.searchText = searchText;
        this.activity = addJobActivity;
    }

	@Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        activity.patientList.setVisibility(View.GONE);
        activity.searchFeedback.setVisibility(View.VISIBLE);
        activity.searchFeedback.setTextColor(R.color.base1);
        activity.searchFeedback.setText(R.string.search_feedback_searching);
    }

    @Override
    protected Collection<? extends Patient> doInBackground(Void... voids)
    {
        Log.d("Entrada-PatientSearchTask", String.format("Search started: '%s'", searchText));

        UserState state = AndroidState.getInstance().getUserState();

        synchronized (state)
        {
            Account account = state.getCurrentAccount();
            DomainObjectReader reader = state.getProvider(account);

            assert reader != null;

            if (Strings.isNullOrEmpty(searchText))
            {
                return reader.getPatients();
            }
            return reader.searchPatients(searchText);
        }
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        Log.d("Entrada-PatientSearchTask", String.format("Cancelled: '%s'", searchText));
    }

    @Override
    protected void onCancelled(Collection<? extends Patient> patients)
    {
        super.onCancelled(patients);
        Log.d("Entrada-PatientSearchTask", String.format("Cancelled: '%s'", searchText));
    }

    @Override
    protected void onPostExecute(Collection<? extends Patient> patients)
    {
        super.onPostExecute(patients);

        Log.d("Entrada-PatientSearchTask", String.format("Success: %d results", patients.size()));

        if (patients.size() == 0)
        {
            activity.patientList.setVisibility(View.GONE);
            activity.searchFeedback.setVisibility(View.VISIBLE);
            activity.searchFeedback.setTextColor(R.color.error);
            activity.searchFeedback.setText(R.string.search_feedback_no_results);
            return;
        }

        List<? extends Patient> p = Lists.newArrayList(patients);
        Collections.sort(p);

        activity.patients = p;

        // type erasure causes this warning, but it's safe
        PatientListItemAdapter adapter =
                new PatientListItemAdapter(activity, android.R.layout.simple_list_item_1, p, false, null);
        activity.patientList.setAdapter(adapter);
        activity.patientList.setVisibility(View.VISIBLE);

        activity.searchFeedback.setVisibility(View.GONE);
    }
}
