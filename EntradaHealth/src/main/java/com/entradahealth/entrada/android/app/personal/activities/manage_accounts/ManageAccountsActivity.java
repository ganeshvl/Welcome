package com.entradahealth.entrada.android.app.personal.activities.manage_accounts;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.edit_account.EditAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.google.common.collect.Lists;
import com.googlecode.androidannotations.annotations.*;

import java.util.List;

@EActivity(R.layout.manage_accounts)
@OptionsMenu(R.menu.manage_accounts)
public class ManageAccountsActivity extends Activity {
    private List<Account> accounts = null;

    @ViewById     ListView accountsList;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    
    ListView lvAccounts;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_accounts);
        lvAccounts = (ListView)findViewById(R.id.accountsList);
        
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ActionBar ab = getActionBar();
        ab.setTitle(R.string.manage_accounts_title);
        ab.setDisplayHomeAsUpEnabled(false);

        UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            accounts = Lists.newArrayList(state.getAccounts());
        }

        ListAdapter adapter =
                new AccountListItemAdapter(this, android.R.layout.simple_list_item_1, accounts);
        lvAccounts.setAdapter(adapter);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public void onBackPressed()
    {
        int accountCount = AndroidState.getInstance().getUserState().getAccounts().size();
        Intent intent = new Intent(this, (accountCount > 0) ? JobListActivity.class
                                                            : UserSelectActivity.class);
        startActivity(intent);
        finish();
    }
    
    

    @ItemClick
    void accountsListItemClicked(Account account)
    {
        Log.d("Entrada-Account", "Account selected: " + account);

        Intent intent = new Intent(this, EditAccountActivity.class);

        Bundle b = new Bundle();
        b.putString(BundleKeys.SELECTED_ACCOUNT, account.getName());

        intent.putExtras(b);
        startActivity(intent);
    }

    @OptionsItem
    void addAccountMenuItemSelected()
    {
        Intent intent = new Intent(this, AddAccountActivity.class);
        startActivity(intent);
    }
}

