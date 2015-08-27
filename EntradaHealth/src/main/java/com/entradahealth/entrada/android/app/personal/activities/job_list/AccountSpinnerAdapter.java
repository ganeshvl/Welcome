package com.entradahealth.entrada.android.app.personal.activities.job_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.core.auth.Account;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Adapter for the ActionBar dropdown that allows the user to
 * select which active account they want to use.
 *
 * @author edr
 * @since 14 Jan 2013
 */
public class AccountSpinnerAdapter extends ArrayAdapter<Account> implements SpinnerAdapter
{
    private final JobListActivity activity;

    public AccountSpinnerAdapter(JobListActivity activity, Collection<Account> objects)
    {
        super(activity, android.R.layout.simple_spinner_dropdown_item, Lists.newArrayList(objects));
        this.activity = activity;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        AccountHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.account_spinner_item, parent, false);
            row.setBackgroundColor(row.getResources().getColor(R.color.unselected_list_item));

            TextView displayNameView = (TextView)row.findViewById(R.id.displayNameText);
            TextView loginNameView = (TextView)row.findViewById(R.id.AccountSpinnerItem_LoginName);

            holder = new AccountHolder(displayNameView, loginNameView);

            row.setTag(holder);
        }
        else
        {
            holder = (AccountHolder)row.getTag();
        }

        Account account = getItem(position);

        holder.displayNameView.setText(account.getDisplayName());
        holder.loginNameView.setText(account.toString());

        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return super.getDropDownView(position, convertView, parent);
    }


    public static class AccountHolder
    {
        public final TextView displayNameView;
        public final TextView loginNameView;


        public AccountHolder(TextView displayNameView, TextView loginNameView)
        {
            this.displayNameView = displayNameView;
            this.loginNameView = loginNameView;
        }
    }
}
