package com.entradahealth.entrada.android.app.personal.activities.inbox;

import android.os.AsyncTask;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTSession;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBUserHandler;

public class QBSessionEstablishTask extends AsyncTask<Void, Void, Void>{
	
	private String qbLogin;
	private String qbPassword;
	
	public QBSessionEstablishTask(String qbLogin, String qbPassword){
		this.qbLogin = qbLogin;
		this.qbPassword = qbPassword;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			ENTUser user = new ENTUser();
			user.setLogin(qbLogin);
			user.setPassword(qbPassword);
			ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
			ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBUSER);
			ENTSession session = ((ENTQBUserHandler) handler).createSession(user);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		// QBLogin
		QBLoginTask loginTask = new QBLoginTask(qbLogin, qbPassword);
		loginTask.execute();
	}
}
