package com.entradahealth.entrada.android.app.personal.activities.edit_account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.UserAuthenticate;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;

/**
 * Handles the update process for accounts accessed through ManageAccountsActivity.
 *
 * @author edr
 * @since 15 Jan 2013
 */
public class EditAccountTask extends DialogTask<Boolean>
{
    //final EditAccountActivity activity;

	SharedPreferences sp;
	Editor edit;
    final Account account;
    boolean isCurrent, isChanged;
    final String apiHost;
    final String displayName;
    final String username;
    final String password;
    final String smUsername;
    final String smPassword;
    final String clinicCode;

    public EditAccountTask(Activity thisActivity, Account account, String apiHost, String clinicCode, String displayName,
    							String username, String password, String smUsername, String smPassword, boolean isCurrent, boolean isChanged)
    {
        super(thisActivity, "Updating account", "Saving account changes...", false);
        this.isCurrent = isCurrent;
        this.isChanged = isChanged;
        this.account = account;
        this.apiHost = apiHost;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.smUsername = smUsername;
        this.smPassword = smPassword;
        this.clinicCode = clinicCode;
        
        sp = thisActivity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    }

    @Override
    protected Boolean doInBackground(Void... voids)
    {
        try
        {
            UserState state = AndroidState.getInstance().getUserState();
            synchronized (state)
            {
                UserPrivate up = state.getUserData();

                account.setApiHost(apiHost);
                account.setDisplayName(displayName);
                account.setRemoteUsername(username);
                account.setRemotePassword(password);
                account.setClinicCode(clinicCode);
                up.save();
            }
            return true;
        }
        catch (Exception ex)
        {
            Log.d("Entrada-EditAccount", "Account edit failure.", ex);
            String message = "Unhandled error in account editing. " +
                    "Contact support for assistance. (" + ex.getClass().getSimpleName() + ")";
            progress(ProgressUpdateType.TOAST, message);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean bool)
    {
        super.onPostExecute(bool);
        this.dialog.dismiss();
	        
        if(isCurrent){
        	edit = sp.edit();
        	edit.putString("sel_acc", displayName);
        	edit.commit();
        }
		UserAuthenticate task = new MyUserAuthenticate(apiHost, username, password, smUsername, smPassword, clinicCode, activity);
		task.execute();
    }
    
    public class MyUserAuthenticate extends UserAuthenticate{

        public MyUserAuthenticate(String apiHost, String username, String password, String smUsername, String smPassword, String clinicCode, Activity _activity){
        	super(apiHost, username, password,_activity);
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
        	super.doInBackground(params);
    		// Save Messages of Conversations
        	ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
        	ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
    		BundleKeys.QB_Conversations = ((ENTQBConversationHandler) handler).getConversations();
    		return null;
        }
        
        @Override
        protected void onPostSuccessful() {
        	// TODO Auto-generated method stub
        	super.onPostSuccessful();
            activity.startActivity(new Intent(activity, EntradaSettings.class));
        }
    }
}
