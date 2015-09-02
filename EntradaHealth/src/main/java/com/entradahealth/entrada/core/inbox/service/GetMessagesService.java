package com.entradahealth.entrada.core.inbox.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider.DatabaseProviderException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;

public class GetMessagesService extends IntentService{

	private static final String LOG_NAME = "GetMessagesService";
	private static EntradaApplication application;
	private ENTHandler conversationHandler;

	public GetMessagesService() {
		super(LOG_NAME);
		application = (EntradaApplication) EntradaApplication.getAppContext();
		ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
		conversationHandler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
    	if(application.is3GOr4GConnected() || application.isWifiConnected()) {
	    	GetDialogsTask task = new GetDialogsTask();
	    	task.execute();
    	}
	}

	class GetDialogsTask extends AsyncTask<Void, Void, Void> {
		private ArrayList<ENTConversation> conversationslist;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
				conversationslist = (ArrayList<ENTConversation>) ((ENTQBConversationHandler) handler).getPublicDialogs(null);
				UserState state = AndroidState.getInstance().getUserState();
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				SMDomainObjectWriter writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				int count = 0;
				for (int i = 0; i < conversationslist.size(); i++) {
					ENTConversation conversation = conversationslist.get(i);
					count = count + conversation.getUnreadMessagesCount();
					if(conversation.getUnreadMessagesCount()>0){
						try{
							ENTConversation _conversation = reader.getConversationById(conversation.getId());
							if(_conversation.getId().equals(conversation.getId())){
								conversation.setPatientID(_conversation.getPatientID());
								writer.updateConversation(conversation, true);
								((ENTQBConversationHandler) conversationHandler).saveMessages(conversation, false, false);
							}
						} catch(DatabaseProviderException ex){
							try {
								writer.addConversation(conversation,false);
								((ENTQBConversationHandler) conversationHandler).saveMessages(conversation, false, false);
							} catch (DomainObjectWriteException e) {
								e.printStackTrace();
							}
						} catch (DomainObjectWriteException e) {
							e.printStackTrace();
						}
					}
				}
			}  catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}		
	}


}
