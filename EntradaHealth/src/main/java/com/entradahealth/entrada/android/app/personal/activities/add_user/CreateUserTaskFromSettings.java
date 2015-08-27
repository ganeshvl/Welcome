package com.entradahealth.entrada.android.app.personal.activities.add_user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.utils.TestConnectionTask;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;

public class CreateUserTaskFromSettings extends DialogTask<Boolean> {
	UserPrivate curUser;
	private final String apiHost;
	private final String clinicCode;
	private final String rUsername;
	private final String rPassword;
    private final String username;
    private final String password;
    private final String displayName;
    Activity _activity;
    SharedPreferences sp;
    Editor edit;

    public CreateUserTaskFromSettings(Activity activity, String username, String password, String apiHost, String clinicCode, String rUsername, String rPassword)
    {
        super(activity, "Creating user", "Please wait...", false);

        this.username = username;
        this.password = password;
        this.displayName = username;
        this.apiHost = apiHost;
        this.clinicCode = clinicCode;
        this.rUsername = rUsername;
        this.rPassword = rPassword;
        this._activity = activity;
        sp = this._activity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    }
    
	@Override
    protected Boolean doInBackground(Void... voids)
    {
		try{
			User user = User.createNewUser(username, displayName, password);
            user.save();
		}catch (Exception ex)
        {
            Log.d("Entrada-AddUser", "User creation failure.", ex);
            progress(ProgressUpdateType.TOAST,
                     "Unhandled error in user creation. " +
                             "Contact support for assistance. (" +
                             ex.getClass().getSimpleName() + ")");
            return false;
        }
		
		 try
	        {
	            if (curUser == null)
	            {
	                curUser = User.getPrivateUserInformation(username, password);
	                progress("Loading account information.");
	                AndroidState.getInstance().createUserState(curUser);
	                return true;
	            }

	            return curUser.matchPassword(password);
	        }
	        catch (AccountException ex)
	        {
	            Toast.makeText(_activity,
	                           "There was a problem loading your account. Please contact support.",
	                           Toast.LENGTH_SHORT).show();
//	            throw new RuntimeException(ex);
	            return false;
	        }
	        catch (UserLoadException ex)
	        {
	            Toast.makeText(_activity,
	                           "There was a problem loading your account. Please contact support.",
	                           Toast.LENGTH_SHORT).show();
//	            throw new RuntimeException(ex);
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
        if (result)
        {
        	edit = sp.edit();
			edit.putString("sel_user", username);
			edit.putString("sel_pin", password);
			edit.commit();
        	
        	TestConnectionTask task = new TestConnectionTask(_activity, apiHost, displayName, clinicCode, rUsername, rPassword,"", "", false, false,false);
	        task.execute();
        }
    }
}
