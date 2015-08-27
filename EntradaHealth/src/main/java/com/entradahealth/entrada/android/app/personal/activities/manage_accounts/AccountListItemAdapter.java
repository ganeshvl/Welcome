package com.entradahealth.entrada.android.app.personal.activities.manage_accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.activities.edit_account.EditAccountActivity;
import com.entradahealth.entrada.core.auth.Account;

import java.util.List;


public class AccountListItemAdapter extends ArrayAdapter<Account>
{
    private final Activity _activity;

    public AccountListItemAdapter(Activity activity, int textViewResourceId, List<Account> objects)
    {
        super(activity, textViewResourceId, objects);
        this._activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        AccountHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = _activity.getLayoutInflater();
            row = inflater.inflate(R.layout.manage_accounts_list_item, parent, false);

            TextView dn = (TextView)row.findViewById(R.id.ManageAccounts_DisplayName);
            TextView un = (TextView)row.findViewById(R.id.ManageAccounts_UserString);

            holder = new AccountHolder(dn, un);

            row.setTag(holder);
        }
        else
        {
            holder = (AccountHolder)row.getTag();
        }

        final Account acc = getItem(position);
        holder.displayNameView.setText(acc.getDisplayName());
        holder.nameView.setText(acc.toString());
        
        row.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        Intent intent = new Intent(_activity, EditAccountActivity.class);
		        Bundle b = new Bundle();
		        b.putString(BundleKeys.SELECTED_ACCOUNT, acc.getName());
		        intent.putExtras(b);
		        _activity.startActivity(intent);
			}
		});

        return row;
    }

    public static class AccountHolder
    {
        public final TextView displayNameView;
        public final TextView nameView;

        public AccountHolder(TextView displayNameView, TextView nameView)
        {
            this.displayNameView = displayNameView;
            this.nameView = nameView;
        }
    }
}
