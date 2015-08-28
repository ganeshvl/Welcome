package com.entradahealth.entrada.android.app.personal.activities.inbox;

import android.os.AsyncTask;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBUserHandler;

public class QBLoginTask extends AsyncTask<Void, Void, Void> {

	private ENTUser user;
	
	public QBLoginTask(String qbLogin, String qbPassword){
		user = new ENTUser();
		user.setLogin(qbLogin);
		user.setPassword(qbPassword);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
			ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBUSER);
			user = ((ENTQBUserHandler) handler).login(user);
			Log.i("","Login User info - "+ user.getId() +" "+ user.getLogin()+" "+user.getName());
		}  catch (Exception e) {
			e.printStackTrace();
			user = null;
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if(user != null) {
			UserPrivate u = AndroidState.getInstance().getUserState().getUserData();
			try {
				u.createUser(user.getLogin());
			} catch (AccountException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_USER_ID, user.getId());
			application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_LOGIN, user.getLogin());
			application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_PASSWORD, user.getPassword());
			// Add Contacts
			SaveSMContactsTask task = new SaveSMContactsTask();
			task.execute();
		}
	}		

}
