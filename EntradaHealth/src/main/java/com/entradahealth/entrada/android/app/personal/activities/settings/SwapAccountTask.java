package com.entradahealth.entrada.android.app.personal.activities.settings;

import java.io.IOException;

import org.acra.ACRA;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Setup;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.pin_entry.PinEntryActivity;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.Job;
import com.google.android.gms.internal.ap;
import com.google.common.collect.ImmutableList;

public class SwapAccountTask extends DialogTask<Boolean> {
	
    UserPrivate curUser;
    String username;
    String pin;
    Handler handler;
		
    EntradaSettings pinActivity;

    public SwapAccountTask(EntradaSettings activity, UserPrivate curUser, String username, String pin)
                    {
        super(activity, "Logging in", "Please wait...", false);
                
        this.pinActivity = activity;
        this.username = username;
        this.pin = pin;
        this.curUser = curUser;
            }

    
    private ImmutableList<User> users = null;
	@Override
    protected Boolean doInBackground(Void... voids)
            {
        progress("Checking password.");
        if(username == null){
        	try {
    			users = User.getUsers();
    		} catch (UserLoadException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			//this.pinActivity.loginFailure();
    			return false;
            }
    		username = users.get(0).getDisplayName();
        }
        		
		try
	        {
            if (curUser == null)
	            {
                curUser = User.getPrivateUserInformation(username, pin);
                progress("Loading account information.");
                AndroidState.getInstance().clearUserState();
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
//            throw new RuntimeException(ex);
            return false;
	        }
	        catch (UserLoadException ex)
	        {
            Toast.makeText(pinActivity,
                           "There was a problem loading your account. Please contact support.",
                           Toast.LENGTH_SHORT).show();
//            throw new RuntimeException(ex);
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
            String currentJob = curUser.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
            if (curUser.getAccounts().size() == 0)
        {
            	EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
            	if(!application.isJobListEnabled()){
            		activity.finish();
            	} else {
                // navigate directly to Setup
            	Intent intent = new Intent(this.activity, AddAccountActivity.class);
            	//Intent intent = new Intent(this.activity, Setup.class);
                activity.startActivity(intent);
                activity.finish();
            	}
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
