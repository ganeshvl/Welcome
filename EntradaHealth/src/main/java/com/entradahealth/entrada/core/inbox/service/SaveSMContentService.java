package com.entradahealth.entrada.core.inbox.service;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBBuddyHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;

import android.app.IntentService;
import android.content.Intent;

public class SaveSMContentService extends IntentService {

	private static final String TAG = "Entrada-SaveSMContentService";
	
	public SaveSMContentService() {
		super(TAG);
		if (running)
			throw new RuntimeException("Saving Content");
		running = true;
	}

	private static boolean running = false;

	public static boolean isRunning() {
		return running;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			if(application.is3GOr4GConnected() || application.isWifiConnected()) {
				// Add Contacts
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory
						.getHandler(ENTHandlerFactory.QBUSER);
				handler = handlerFactory.getHandler(ENTHandlerFactory.QBBUDDY);
				((ENTQBBuddyHandler) handler).saveContacts();
				BundleKeys.QB_Users = ((ENTQBBuddyHandler) handler).getBuddies();
				// Save Conversations
				handler = handlerFactory
						.getHandler(ENTHandlerFactory.QBCONVERSATION);
				List<ENTConversation> conversations = ((ENTQBConversationHandler) handler).saveConversations();
				for (ENTConversation conversation : conversations) {
					SaveMessagesContentService.saveConversationMessages(conversation);
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		} finally {
			running = false;
		}
	}

}
