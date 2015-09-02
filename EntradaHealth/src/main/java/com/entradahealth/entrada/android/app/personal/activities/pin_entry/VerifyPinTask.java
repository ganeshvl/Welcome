package com.entradahealth.entrada.android.app.personal.activities.pin_entry;

import java.io.IOException;

import org.acra.ACRA;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.inbox.SecureMessaging;
import com.entradahealth.entrada.android.app.personal.activities.inbox.UserAuthenticate;
import com.entradahealth.entrada.android.app.personal.activities.job_display.CaptureImages;
import com.entradahealth.entrada.android.app.personal.activities.job_display.ImageDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.google.common.collect.ImmutableList;

/**
 * Async task for PinEntryActivity to check a user's PIN.
 *
 * @author ste
 * @since 14 Nov 2012
 */
public class VerifyPinTask extends DialogTask<Boolean> {

    UserPrivate curUser;
    String username;
    String pin;
    Handler handler;
	SharedPreferences sp;
	PinEntryActivity pinActivity;
	EntradaApplication application;

	public VerifyPinTask(PinEntryActivity activity, UserPrivate curUser,
			String username, String pin) {
		super(activity, "Logging in", "Please wait...", false);
		this.pinActivity = activity;
		this.username = username;
		this.pin = pin;
		this.curUser = curUser;
		sp = pinActivity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
		application = (EntradaApplication) EntradaApplication.getAppContext();
	}

    private ImmutableList<User> users = null;

    @Override
	protected Boolean doInBackground(Void... voids) {
		progress("Checking password.");
		if (username == null) {
			try {
				users = User.getUsers();
			} catch (UserLoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// this.pinActivity.loginFailure();
				return false;
			}
			username = users.get(0).getDisplayName();
		}

		try {
			if (curUser == null) {
				curUser = User.getPrivateUserInformation(username, pin);
				progress("Loading account information.");
				AndroidState.getInstance().clearUserState();
				AndroidState.getInstance().createUserState(curUser);

			}
			return curUser.matchPassword(pin);

		} catch (AccountException ex) {
			Toast.makeText(
					pinActivity,
					"There was a problem loading your account. Please contact support.",
					Toast.LENGTH_SHORT).show();
			return false;
		} catch (InvalidPasswordException ex) {
			this.pinActivity.loginFailure();
			return false;
		} catch (UserLoadException ex) {
			/*
			 * Toast.makeText(pinActivity,
			 * "There was a problem loading your account. Please contact support."
			 * , Toast.LENGTH_SHORT).show();
			 */
			// this.pinActivity.loginFailure();
			return false;
		}

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		MainUserDatabaseProvider provider;
		String apiHost=null, _username=null, _password = null;
		try {
			provider = new MainUserDatabaseProvider();
        	EUser user = provider.getCurrentUser();
        	if(user!=null) {
	        	EnvironmentHandlerFactory factory = EnvironmentHandlerFactory.getInstance();
	        	Environment environment = factory.getHandler(user.getEnvironment());
	        	application.setStringIntoSharedPrefs("environment", user.getEnvironment());
	        	apiHost = environment.getApi();
	        	_username = user.getName();
	        	_password = user.getPassword();
        	}
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			if(this.dialog!=null) {
				if(this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}

		if (result) {
			Editor edit = sp.edit();
			// save app_version_name, app-version_code in bundle keys
			       	
			edit.putString("CUR_UNAME", username);
			edit.putString("PIN_SAVED", pin);
			edit.commit();

			//Re-run passcode logic
			BundleKeys.TIME_START = System.currentTimeMillis();
			edit.putLong("TIME_START", System.currentTimeMillis());
			BundleKeys.PASSCODE = sp.getInt("PASSCODE", 0);
			edit.putInt("PASSCODE", BundleKeys.PASSCODE);
			edit.putInt("PASSCODE_MINUTES", BundleKeys.passcode_values[BundleKeys.PASSCODE]);
			edit.commit();
			
			BundleKeys.cur_uname = username;
			String currentJob = curUser
					.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
			if (curUser.getAccounts().size() == 0) {
				// navigate directly to Setup
                // navigate directly to AddAccountActivity
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				Intent intent ;
				if((application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN)) == null) {
					intent = new Intent(this.activity, AddAccountActivity.class);
				} else {
					intent = new Intent(this.activity, SecureMessaging.class);
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activity.startActivity(intent);
				activity.finish();
			} else if (currentJob != null) {

				// Session Creation and QBLogin
			    Account currentAccount = null;
		   		UserState state = AndroidState.getInstance().getUserState();
		   		if(state!=null) {
		   			currentAccount = state.getCurrentAccount();
		   		}
		   		if(currentAccount!=null) {
		   			UserAuthenticate authenticate = new UserAuthenticateCase1(apiHost, _username, _password, pinActivity);
		   			authenticate.execute();
		   		}
            } else
            {
			    Account currentAccount = null;
		   		UserState state = AndroidState.getInstance().getUserState();
		   		if(state!=null) {
		   			currentAccount = state.getCurrentAccount();
		   		}
		   		if(currentAccount!=null) {
		   			UserAuthenticate authenticate = new UserAuthenticateCase2(apiHost, _username, _password, pinActivity);
		   			authenticate.execute();
		   		}
            }
        }
        else {
            this.pinActivity.loginFailure();
        }
    }
	
    public class UserAuthenticateCase1 extends UserAuthenticate{

        public UserAuthenticateCase1(String apiHost, String username, String password, Activity _activity){
        	super(apiHost, username, password,_activity);
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
        	// TODO Auto-generated method stub
        	UserState state = AndroidState.getInstance().getUserState();
        	try {
				state.setSMUser();
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AccountException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return true;
        }
        
        @Override
        protected void onPostSuccessful() {
        	// TODO Auto-generated method stub
        	super.onPostSuccessful();
        	try {
    			String currentJob = curUser
    					.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
				long jobId = Long.parseLong(currentJob);
				Intent intent = null;
				String accountName = curUser
						.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT);
		   		Job j = AndroidState.getInstance().getUserState()
						.getProvider(accountName).getJob(jobId);
				if (j != null) {
					if (BundleKeys.fromCaputreImages){
						intent = new Intent();
						intent.putExtra("isModified", true);
						if(j.isFlagSet(Flags.IS_FIRST))
							intent.putExtra("isFirst", true);
						else
							intent.putExtra("isFirst", false);
						//intent.putExtra("isFirst", false);
						intent.putExtra("isDeleted", true);
						intent.putExtra("img_count", 5);
						intent.putExtra("job_type", BundleKeys.job_type);
						intent.putExtra("isFromList", true);
						intent.putExtra("isNew", true);
						intent.putExtra(BundleKeys.SELECTED_JOB, jobId);
						intent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
						intent.putExtra("sel_job_str", "");
						intent.putExtra("interrupted", false);
						intent.setClass(pinActivity, CaptureImages.class);
						//activity.startActivity(intent);
					}else if (BundleKeys.fromImageDisplay){
						intent = new Intent();
						intent.putExtra("isModified", true);
						intent.putExtra("img_path", BundleKeys.current_img_path);
						intent.putExtra("pos", BundleKeys.current_img_position);
						intent.putExtra("total", BundleKeys.img_total);
						intent.putExtra(BundleKeys.SELECTED_JOB, jobId);
						intent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
						intent.setClass(activity, ImageDisplayActivity.class);
					} else {
						BundleKeys.IS_DIRTY = true;// mark job as dirty in
													// case of data loss
						intent = new Intent(activity,
								JobDisplayActivity.class);
                    Bundle b = new Bundle();
						intent.putExtra("isDeleted", true);
						intent.putExtra("isModified", true);
						b.putBoolean("isFirst", false);
						intent.putExtra("isFromList", true);
						b.putLong(BundleKeys.SELECTED_JOB, jobId);
						b.putString(BundleKeys.SELECTED_JOB_ACCOUNT,
								accountName);
						boolean isInterrupted = sp.getBoolean("IS_INTERRUPTED", false);
						b.putBoolean("interrupted", isInterrupted);
						intent.putExtras(b);
					}
				} else {
					intent = new Intent(activity,
							JobListActivity.class);
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                	try {
                		Bundle extras = activity.getIntent().getExtras();
                		BundleKeys.fromSecureMessaging = extras.getBoolean("fromSecureMessaging"); 
                    } catch(Exception ex){
                    }
                activity.finish();
            } catch (Exception e) {
                ACRA.getErrorReporter().handleSilentException(e);
			} finally {
				curUser.setStateValue(
						JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
				curUser.setStateValue(
						JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, null);

				try {
                    curUser.save();
				} catch (IOException e) {
                    ACRA.getErrorReporter().handleSilentException(e);
                }
            }
        }
    }

    public class UserAuthenticateCase2 extends UserAuthenticate{

        public UserAuthenticateCase2(String apiHost, String username, String password, Activity _activity){
        	super(apiHost, username, password, _activity);
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
        	// TODO Auto-generated method stub
        	UserState state = AndroidState.getInstance().getUserState();
        	try {
				state.setSMUser();
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AccountException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPasswordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return true;
        }
        
        @Override
        protected void onPostSuccessful() {
        	// TODO Auto-generated method stub
        	super.onPostSuccessful();

            Intent intent = new Intent(activity, JobListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            try {
            	Bundle extras = activity.getIntent().getExtras();
            	BundleKeys.fromSecureMessaging = extras.getBoolean("fromSecureMessaging"); 
            } catch(Exception ex){
            }
            activity.finish();
        }
    }

    
    
}
