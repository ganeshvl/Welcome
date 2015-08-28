package com.entradahealth.entrada.android.app.personal.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.edit_account.EditAccountActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;

public class VerifySwapPinTask extends DialogTask<Boolean> {

    UserPrivate curUser;
    Account acc;
    String username, sel_uname;
    String pin;
    EntradaSettings pinActivity;
    SharedPreferences sp;
    Editor edit;
    boolean isEdit = false;
    String accName, isCurrent;

    public VerifySwapPinTask(EntradaSettings activity, UserPrivate curUser, String username, String sel_uname, boolean isEdit)
    {
        super(activity, "Getting Account Data", "Please wait...", false);

        this.pinActivity = activity;
        this.username = username;
        this.sel_uname = sel_uname;
        this.curUser = null;
        this.isEdit = isEdit;
        sp = activity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        this.pin = sp.getString("PIN_SAVED", "1111");
    }

    
	@Override
    protected Boolean doInBackground(Void... voids)
    {
        
        try
        {
                curUser = User.getPrivateUserInformation(username, pin);
                progress("Loading account information.");
                AndroidState.getInstance().clearUserState();
                AndroidState.getInstance().createUserState(curUser);
                return true;

        }
        catch (AccountException ex)
        {
            Toast.makeText(pinActivity,
                           "There was a problem loading your account. Please contact support.",
                           Toast.LENGTH_SHORT).show();
            return false;
        }
        catch (UserLoadException ex)
        {
            Toast.makeText(pinActivity,
                           "There was a problem loading your account. Please contact support.",
                           Toast.LENGTH_SHORT).show();
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

        //this.dialog.dismiss();
        if (result){
        	
        		Editor edit = sp.edit();
				edit.putString("sel_user", username);
				edit.commit();
        		BundleKeys.cur_uname = username;
                edit = sp.edit();
                edit.putString("sel_acc", sel_uname);
                edit.putString("CUR_UNAME", username);
    			edit.putString("PIN_SAVED", pin);
                edit.commit();
                if(!isEdit)
                this.pinActivity.refreshUserList();
                
                if (curUser.getAccounts().size() == 0)
                {
                    // navigate directly to AddAccountActivity
                    Intent intent = new Intent(this.activity, AddAccountActivity.class);
                    intent.putExtra("isFromEdit", true);
                    activity.startActivity(intent);
                    activity.finish();
	            }
        	
                if(isEdit){
                	acc = AndroidState.getInstance().getUserState().getCurrentAccount();
                	BundleKeys.CURR_ACCOUNT = acc;
                	BundleKeys.SYNC_FOR_ACC = acc.getRemoteUsername();
					
					edit = sp.edit();
					edit.putString("SYNC_FOR_ACC", BundleKeys.SYNC_FOR_ACC);
					edit.commit();
                    accName = acc.getName();
                    
            		Intent intent = new Intent(activity, EditAccountActivity.class);
    		        Bundle b = new Bundle();
    		        b.putString(BundleKeys.SELECTED_ACCOUNT, accName);
    		        b.putString("sel_user_name", curUser.getName());
    		        intent.putExtras(b);
    		        activity.startActivity(intent);
    		    }
        }
    }
}

