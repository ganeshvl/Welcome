package com.entradahealth.entrada.android.app.personal.activities.pin_entry;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.add_account.NewUserActivity;
import com.entradahealth.entrada.android.app.personal.activities.inbox.UserAuthenticate;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.google.common.collect.ImmutableList;

public class VerifyPinAsynTask extends DialogTask<Boolean> {

	    private UserPrivate curUser;
	    private String username;
	    private String pin;
	    private Handler handler;
		private SharedPreferences sp;
		private EntradaApplication application;
		private PinEntryFragment fragment;
		private boolean fromSettings, fromAddAccount, fromRedeemInvite;

		public VerifyPinAsynTask(PinEntryFragment fragment, UserPrivate curUser,
				String username, String pin, boolean fromSettings, boolean fromAddAccount, boolean fromRedeemInvite) {
			super(fragment.getActivity(), "Logging in", "Please wait...", false);
			this.fragment = fragment;
			this.username = username;
			this.pin = pin;
			this.fromSettings = fromSettings;
			this.fromAddAccount = fromAddAccount;
			this.fromRedeemInvite = fromRedeemInvite;
			this.curUser = curUser;
			sp = fragment.getActivity().getSharedPreferences("Entrada",
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
						fragment.getActivity(),
						"There was a problem loading your account. Please contact support.",
						Toast.LENGTH_SHORT).show();
				return false;
			} catch (InvalidPasswordException ex) {
				this.fragment.loginFailure();
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
			this.dialog.dismiss();

			if (result) {
				if(_username == null){
		    		fragment.getActivity().getSupportFragmentManager().popBackStack();
		    		fragment.getActivity().getActionBar().show();
		    		if(fromAddAccount){
		    			AddAccountActivity addAccount = (AddAccountActivity)fragment.getActivity();
		    			addAccount.sucessfulPinEntry();
		    		}
		    		if(fromRedeemInvite){
		    			NewUserActivity newUser = (NewUserActivity)fragment.getActivity();
		    			newUser.sucessfulPinEntry();
		    		}
				} else {
					new UserAuthenticateCase1(apiHost, _username, _password, fragment.getActivity()).execute();
				}
	        }
	        else {
	            this.fragment.loginFailure();
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
	    		InputMethodManager imm = (InputMethodManager) fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    		View view =  fragment.getActivity().getCurrentFocus();
	    		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	    		fragment.getActivity().getSupportFragmentManager().popBackStack();
	    		fragment.getActivity().getActionBar().show();
	    		if(fromSettings){
	    			EntradaSettings settings = (EntradaSettings)fragment.getActivity();
	    			settings.sucessfulPinEntry();
	    		}
	    		if(fromAddAccount){
	    			AddAccountActivity addAccount = (AddAccountActivity)fragment.getActivity();
	    			addAccount.sucessfulPinEntry();
	    		}
	    		if(fromRedeemInvite){
	    			NewUserActivity newUser = (NewUserActivity)fragment.getActivity();
	    			newUser.sucessfulPinEntry();
	    		}
	        }
	    }


}
