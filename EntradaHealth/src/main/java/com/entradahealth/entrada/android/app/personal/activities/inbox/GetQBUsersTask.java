package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.util.List;

import android.os.AsyncTask;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBBuddyHandler;

public class GetQBUsersTask extends AsyncTask<Void, Void, Void> {
	
	protected List<ENTUser> users;
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
			ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBBUDDY);
			users = ((ENTQBBuddyHandler) handler).getBuddyList();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		BundleKeys.QB_Users = users;
		onSucessfulPostExecute();
		
	}
	
	protected void onSucessfulPostExecute(){
		
	}
}
