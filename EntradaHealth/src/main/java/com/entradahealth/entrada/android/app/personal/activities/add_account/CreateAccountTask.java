package com.entradahealth.entrada.android.app.personal.activities.add_account;

import java.util.List;

import org.joda.time.Instant;

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
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.google.common.collect.ImmutableList;

/**
 * Task that creates a new account (async'd off of the main thread to
 * give a nice happy throbber).
 *
 * @author edr
 * @since 12 Oct 2012
 */
public class CreateAccountTask extends DialogTask<Boolean>
{
    private String apiHost;
    private String username;
    private String password;
    private String displayName;
    private String clinicCode;
    private String smUsername;
    private String smPassword;    
    private Activity activity;
    SharedPreferences sp;
    private ImmutableList<User> users = null;
    private List<Dictator> dictators;

    public CreateAccountTask(Activity activity, String apiHost, String clinicCode, String displayName, String username, String password, String smUsername, String smPassword)
    {
        super(activity, "Creating account", "Please wait...", false);
        this.activity = activity;

        this.apiHost = apiHost;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.clinicCode = clinicCode;
        this.smPassword = smPassword;
        this.smUsername = smUsername;

    }

    public CreateAccountTask(Activity activity, String apiHost, List<Dictator> dictators, String username, String password) {
    	super(activity, "Creating account", "Please wait...", false);
    	this.activity = activity;
    	this.apiHost = apiHost;
    	this.dictators = dictators;
    	this.username = username;
    	this.password = password;
	}

	@Override
    protected Boolean doInBackground(Void... voids)
    {
        try
        {
            // the name basically doesn't matter, it's just the directory name
            final String name = String.valueOf(Instant.now().getMillis());

            
            UserPrivate u = AndroidState.getInstance().getUserState().getUserData();

            for(Dictator dictator : dictators){
            	
	            for (Account a : u.getAccounts())
	            {
	                if (a.getName().equals(dictator.getDictatorID()))
	                {
	                    progress(ProgressUpdateType.TOAST,
	                             "An account by that display name already exists. Please use another.");
	                    return false;
	                }
	            }
	
	            u.createAccount(String.valueOf(dictator.getDictatorID()), dictator.getDictatorName(), dictator.getDictatorName(), "", apiHost, dictator.getClinicName(), username, password);
	            u.save();
            }
            progress(ProgressUpdateType.TOAST, "Account added");
            return true;
        }
        catch (Exception ex)
        {
            Log.d("Entrada-AddAccount", "Account creation failure.", ex);
            String message = "Unhandled error in account creation. " +
                    "Contact support for assistance. (" + ex.getClass().getSimpleName() + ")";
            progress(ProgressUpdateType.TOAST, message);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        super.onPostExecute(result);
        dialog.dismiss();
        if (result)
        {
            UserState state = AndroidState.getInstance().getUserState();
            synchronized (state)
            {
                UserPrivate user = state.getUserData();
                AndroidState.getInstance().clearUserState();
                try
                {
                    AndroidState.getInstance().createUserState(user);
                }
                catch (Exception ex)
                {
                    progress(ProgressUpdateType.DIALOG, "Unexpected failure: " + ex.getMessage());
                }
                sp = this.activity.getSharedPreferences("Entrada",
        				Context.MODE_WORLD_READABLE);
                Editor edit = sp.edit();
    			edit.putString("sel_acc", displayName);
    			edit.commit();
                BundleKeys.cur_uname = user.getUserDirectory().getName();
    			//BundleKeys.IS_FIRST_SYNC = true;
    			//BundleKeys.GOT_QUEUES = false;
                
    			try
    	        {
    	            users = User.getUsers();
    	            BundleKeys.POSITION = users.size() - 1;
    	            //Toast.makeText(activity, Integer.toString(BundleKeys.POSITION), 1000).show();
    	            //BundleKeys.POSITION
    	        }
    	        catch (UserLoadException ex)
    	        {
    	            throw new RuntimeException("Failed to get users: ", ex);
    	        }
    			UserAuthenticate authenticate = new MyUserAuthenticate(apiHost, username, password, activity);
    			authenticate.execute();
            }
        }
    }
    
    public class MyUserAuthenticate extends UserAuthenticate{

        public MyUserAuthenticate(String apiHost, String username, String password, Activity _activity){
        	super(apiHost, username, password, _activity);
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
        	super.doInBackground(params);
    		// Save Messages of Conversations
        	ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
        	ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
    		BundleKeys.QB_Conversations = ((ENTQBConversationHandler) handler).getConversations();
//    		for(ENTConversation conversation : BundleKeys.QB_Conversations){
//    			((ENTQBConversationHandler) handler).saveMessages(conversation);
//    		}
    		return null;
        }
        
        @Override
        protected void onPostSuccessful() {
        	// TODO Auto-generated method stub
        	super.onPostSuccessful();
        	Intent i = new Intent(activity, JobListActivity.class);
        	i.putExtra("qchanged", true);
        	activity.startActivity(i);
        	activity.finish();
        }
    }
    
}
