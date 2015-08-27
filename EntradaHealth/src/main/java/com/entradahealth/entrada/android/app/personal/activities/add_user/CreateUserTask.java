package com.entradahealth.entrada.android.app.personal.activities.add_user;

import java.io.IOException;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.ChoosePin;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Setup;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.Job;

/**
 * Handles creation of a new user in the user list.
 *
 * @author edr
 * @since 12 Oct 2012
 */
public class CreateUserTask extends DialogTask<Boolean>
{
    private final String username;
    private final String password;
    private final String displayName;
    private UserPrivate currentUser;
    User user;
    ChoosePin _activity;
    SharedPreferences sp;
    Editor edit;

    public CreateUserTask(ChoosePin activity,
                          String username, String password)
    {
        super(activity, "Creating user", "Please wait...", false);

        this.username = username;
        this.password = password;
        this.displayName = username;
        this._activity = activity;
        sp = this._activity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    }
    
	@Override
    protected Boolean doInBackground(Void... voids)
    {
        try
        {
            for (User u : User.getUsers())
            {
                if (u.getName().equals(username) || u.getDisplayName().equals(password))
                {
                    progress(ProgressUpdateType.TOAST,
                             "Username and display name must be unique for all users.");
                    return false;
                }
            }

            User user = User.createNewUser(username, displayName, password);
            user.save();
            return true;
        }
        catch (Exception ex)
        {
            Log.d("Entrada-AddUser", "User creation failure.", ex);
            progress(ProgressUpdateType.TOAST,
                     "Unhandled error in user creation. " +
                             "Contact support for assistance. (" +
                             ex.getClass().getSimpleName() + ")");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        super.onPostExecute(result);
        this.dialog.dismiss();
        if (result)
        {
        	BundleKeys.cur_uname = username;
        	edit = sp.edit();
			edit.putString("CUR_UNAME", username);
			edit.putString("PIN_SAVED", password);
			edit.putString("sel_user", username);
			edit.commit();
        	
	        if (username != null) 
	        {
	            try
	            {
	                UserState us = AndroidState.getInstance().getUserState();
	                if (us != null)
	                {
	                    UserPrivate up = us.getUserData();
	                    if (up != null && !username.trim().toLowerCase().equals(us.getUserData().getName().trim().toLowerCase()))
	                    {
	                        AndroidState.getInstance().clearUserState();
	                        currentUser = null;
	                    }
	                    else
	                    {
	                        currentUser = us.getUserData();
	                    }
	                }

	                user = User.getPublicUserInformation(username);
	            }
	            catch (UserLoadException ex)
	            {
	                throw new RuntimeException(ex);
	            }
	        }
	        
	        
	        VerifyPinTask pinTask = new VerifyPinTask(_activity, currentUser, username, password);
            pinTask.execute();
        }
    }
    
    public class VerifyPinTask extends DialogTask<Boolean> {

        UserPrivate curUser;
        String username;
        String pin;
        Handler handler;

        ChoosePin pinActivity;

        public VerifyPinTask(ChoosePin activity, UserPrivate curUser, String username, String pin)
        {
            super(activity, "Logging in", "Please wait...", false);

            this.pinActivity = activity;
            this.username = username;
            this.pin = pin;
            this.curUser = curUser;
        }

        
    	@Override
        protected Boolean doInBackground(Void... voids)
        {
            progress("Checking password.");
            try
            {
                if (curUser == null)
                {
                    curUser = User.getPrivateUserInformation(sp.getString("sel_user", "User_1"), sp.getString("PIN_SAVED", "1111"));
                    progress("Loading account information.");
                    AndroidState.getInstance().createUserState(curUser);
                    return true;
                }

                return curUser.matchPassword(pin);
            }
            catch (AccountException ex)
            {
                Toast.makeText(pinActivity,
                               "There was a problem loading your account. Please contact support.",
                               Toast.LENGTH_SHORT).show();
//                throw new RuntimeException(ex);
                return false;
            }
            catch (UserLoadException ex)
            {
                Toast.makeText(pinActivity,
                               "There was a problem loading your account. Please contact support.",
                               Toast.LENGTH_SHORT).show();
//                throw new RuntimeException(ex);
                return false;
            }
            catch (InvalidPasswordException ex)
            {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            this.dialog.dismiss();
            if (result)  {
            	        	
            	//BundleKeys.PIN_SAVED = pin;
            	
                String currentJob = curUser.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
                if (curUser.getAccounts().size() == 0)
                {
                    // navigate directly to AddAccountActivity
                    Intent intent = new Intent(this.activity, AddAccountActivity.class);
                	//Intent intent = new Intent(this.activity, Setup.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                else if (currentJob != null) {
                    long jobId = Long.parseLong(currentJob);
                    String accountName = curUser.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT);

                    try {
                        Job j = AndroidState.getInstance().getUserState().getProvider(accountName).getJob(jobId);

                        Intent intent;

                        if (j != null)
                        {
                            intent = new Intent(activity, JobDisplayActivity.class);
                            Bundle b = new Bundle();
                            b.putLong(BundleKeys.SELECTED_JOB, jobId);
                            b.putString(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
                            intent.putExtras(b);
                        }
                        else
                        {
                            intent = new Intent(this.activity, JobListActivity.class);
                        }

                        activity.startActivity(intent);
                        activity.finish();
                    } catch (Exception e) {
                        ACRA.getErrorReporter().handleSilentException(e);
                    }
                    finally
                    {
                        curUser.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
                        curUser.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, null);

                        try
                        {
                            curUser.save();
                        }
                        catch (IOException e)
                        {
                            ACRA.getErrorReporter().handleSilentException(e);
                        }
                    }
                }
                else
                {
                    Intent intent = new Intent(this.activity, JobListActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
            
        }
        
    }
}
