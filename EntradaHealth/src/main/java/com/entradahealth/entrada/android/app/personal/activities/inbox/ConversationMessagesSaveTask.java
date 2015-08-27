package com.entradahealth.entrada.android.app.personal.activities.inbox;

import android.os.AsyncTask;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;

public class ConversationMessagesSaveTask extends AsyncTask<Void, Void, Void> {
	
	private ENTConversation conversation;
	private UserAuthenticate userAuthenticate;
	

	public ConversationMessagesSaveTask(ENTConversation conversation, UserAuthenticate userAuthenticate) {
		this.conversation = conversation;
		this.userAuthenticate = userAuthenticate;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
		ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
		((ENTQBConversationHandler) handler).saveMessages(conversation);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
	}		

}
