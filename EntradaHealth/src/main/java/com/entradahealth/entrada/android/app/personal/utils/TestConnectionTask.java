package com.entradahealth.entrada.android.app.personal.utils;

import java.util.ArrayList;
import java.util.List;

import org.acra.ACRA;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.CreateAccountTask;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.edit_account.EditAccountTask;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.RemoteService;

/**
 * Task that wraps the API service for testing the existence of a clinic
 * service (by checking the version) as well as checking the authentication
 * credentials given in the form.
 *
 * @author edr
 * @since 12 Oct 2012
 */
public class TestConnectionTask extends DialogTask<Boolean>
{
	Account editingAccount = null;
	String accountName;
	private Activity _activity;
    private String apiHost;
    private String displayName;
    private String username;
    private String password;
    private String clinicCode;
    private String smUsername;
    private String smPassword;
    private boolean isEdit = false, isCurrent = false, isChanged = false;
    private EntradaApplication application;
    private List<Dictator> dictators;
    private String environment;
    private boolean authFailFlag;

    public TestConnectionTask(Activity activity, String apiHost, String displayName, String clinicCode, String username, 
    								String password, String smUsername, String smPassword, boolean isEdit, boolean isCurrent, boolean isChanged)
    {
        super(activity, "Testing credentials", "Please wait...", true);
        this._activity = activity;
        this.apiHost = apiHost;
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.clinicCode = clinicCode;
        this.isEdit = isEdit;
        this.isCurrent = isCurrent;
        this.isChanged = isChanged;
        this.smPassword = smPassword;
        this.smUsername = smUsername;
    }
    
    public TestConnectionTask(Activity activity, String apiHost, String userId, String password, String environment, boolean isEdit, boolean isCurrent, boolean isChanged) {
		// TODO Auto-generated constructor stub
    	super(activity, "Testing credentials", "Please wait...", true);
    	this._activity = activity;
    	this.apiHost = apiHost;
    	this.username = userId;
    	this.password = password;
    	this.environment = environment;
    	application = (EntradaApplication) EntradaApplication.getAppContext();
	}

	@Override
    protected Boolean doInBackground(Void... voids)
    {
        try
        {
            RemoteService svc = new APIService(apiHost, username, password);
			String m_androidId = Secure.getString(_activity.getContentResolver(), Secure.ANDROID_ID);
            Log.i("Entrada-AddAccount", "new service: ");

            Log.i("Entrada-AddAccount", "testing service");
            if (!svc.isValidHost())
            {
                Log.e("Entrada-AddAccount", "could not reach host " + apiHost);
                progress(ProgressUpdateType.TOAST, "Could not reach host. Please check your clinic code " +
                                                   "and that you are connected to the " +
                                                   "clinic network.");
            }
            MainUserDatabaseProvider provider = new MainUserDatabaseProvider();
			EUser _user = new EUser(username,"", environment,"",false, "");
			if(provider.isUserExists(_user)){
				progress(ProgressUpdateType.TOAST, "Account already added.");
				return false;
			}
            String str = svc.authenticate(username, password, m_androidId);
            try{
	      		JSONObject json = new JSONObject(str);
	        	Log.i("USER_AUTHENTICATE", "USER_AUTHENTICATE--"+str);
	        	application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_ID, null);

	            String sessionToken = json.getString("SessionToken").trim();
	            application.setStringIntoSharedPrefs(BundleKeys.SESSION_TOKEN, sessionToken);
	            Log.i("Entrada-AddAccount", "All test groovy.");
	            progress(ProgressUpdateType.TOAST,
	                     "Connection test worked! You are all set.");
	            String qbLogin = json.getString("QBLogin");
	        	// Parsing for permissions
				String _perStr = json.getString("ModulePermissions");
				String perStr = _perStr.substring(1, _perStr.length()-1);
				String[] modulePermissions = perStr.split(",");
				Boolean joblistPermission = new Boolean(modulePermissions[0]);
				Boolean secureMessagingPermission = new Boolean(modulePermissions[2]);
				application.setJobListPermission(joblistPermission);
				//if(joblistPermission) {
	            
		            dictators = svc.getAssociatedDictators(sessionToken);
		            Log.e("", "Dictators.size()--"+dictators.size());
		            boolean flag = true;
		            for (Dictator dictator : dictators) {
		            	dictator.setCurrent(flag);
			            provider.addDictator(dictator, username);
			            flag = false;
					}
				//} else {
				//	dictators = new ArrayList<Dictator>();
				//}
	            List<EUser> users = provider.getEUsers();
	            for (EUser user : users) {
	            	user.setCurrent(false);
					provider.updateUser(user);
				}		            
	            EUser user = new EUser(username, password, environment, (dictators.size()>0)? String.valueOf(dictators.get(0).getDictatorID()) : "", true, qbLogin);
	            application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_ID, (dictators.size()>0)? String.valueOf(dictators.get(0).getDictatorID()) : "");
	            application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_NAME, (dictators.size()>0)?String.valueOf(dictators.get(0).getDictatorName()) : "");
	            provider.addUser(user);
	            return true;
            } catch(Exception ex){
                Log.e("Entrada-AddAccount", ex.getMessage());
                authFailFlag = true;
                progress(ProgressUpdateType.TOAST,
                         "Authentication failure. Please check your username/password and environment.");
            }

        }
        catch (Exception ex)
        {
        	authFailFlag = true;
            progress(ProgressUpdateType.TOAST,
            		"Authentication failure. Please check your clinic code, username and password.");
            ACRA.getErrorReporter().handleSilentException(ex);
        }

        return false;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
    	// TODO Auto-generated method stub
    	super.onPostExecute(result);
    	try {
    		this.dialog.dismiss();
    	} catch(Exception ex){
    		ex.printStackTrace();
    	}
    	if(result){
    		if(!isEdit){
    			//CreateAccountTask task = new CreateAccountTask(this._activity, apiHost, clinicCode, displayName, username, password, smUsername, smPassword);
    			//task.execute();
    			CreateAccountTask task = new CreateAccountTask(this._activity, apiHost, dictators, username, password);
    			task.execute();
    		}else{
    			editingAccount = AndroidState.getInstance().getUserState().getCurrentAccount();
    			
    			EditAccountTask task = new EditAccountTask(activity, editingAccount, apiHost, clinicCode, 
    																displayName, username, password, smUsername, smPassword, isCurrent, isChanged);
    	        task.execute();
    		}
    	} else {
    		if(!authFailFlag){
	        	Intent i = new Intent(activity, JobListActivity.class);
	        	i.putExtra("qchanged", true);
	        	activity.startActivity(i);
	        	activity.finish();
    		}
    	}
    }
    
}
