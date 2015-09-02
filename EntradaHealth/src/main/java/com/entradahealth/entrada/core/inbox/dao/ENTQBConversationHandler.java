package com.entradahealth.entrada.core.inbox.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.inbox.encryption.AES256Cipher;
import com.entradahealth.entrada.core.inbox.service.ENTChatManager;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.entradahealth.entrada.core.inbox.service.NewConversationBroadcastService;
import com.entradahealth.entrada.core.remote.APIService;
import com.google.gson.Gson;

public class ENTQBConversationHandler implements ENTConversationHandler{

	private ENTChatManager chatManager;
	private UserState state;
	private EntradaApplication application;
	private SMDomainObjectWriter writer;
	private SMDomainObjectReader reader;
	private AES256Cipher cipher;
	
	public ENTQBConversationHandler() {
		chatManager = new ENTQBChatManagerImpl();
		application = (EntradaApplication) EntradaApplication.getAppContext();
		cipher = new AES256Cipher();
	}
	
	@Override
	public ENTConversation createDialog(ENTMessage message) {
		ENTConversation conversation = null;
		try {
			ENTResponse response = chatManager.createDialog(message);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.createDialog(message);
			}
			conversation = buildDialogFromResponse(response.getResponseData());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conversation;
	}
	
	public void deleteDialog(ENTConversation conversation){
		try {
			state = AndroidState.getInstance().getUserState();
			application = (EntradaApplication) EntradaApplication.getAppContext();
			writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
			writer.deleteConversation(conversation);
			ENTResponse response = chatManager.deleteDialog(conversation);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.deleteDialog(conversation);
			}			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ENTConversation updateDialog(ENTConversation conversation) {
		try {
			ENTResponse response = chatManager.updateDialog(conversation);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.updateDialog(conversation);
			}			
			conversation = buildDialogFromResponse(response.getResponseData());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return conversation;
	}
	
	@Override
	public List<ENTConversation> getPrivateDialogs(String customString) {
		List<ENTConversation> conversations = null;
		try {
			ENTResponse response = chatManager.getPrivateDialogs(customString);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getPrivateDialogs(customString);
			}
			conversations = buildDialogsListFromResponse(response.getResponseData());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conversations;				
	}

	@Override
	public List<ENTConversation> getPublicDialogs(String customString) {
		List<ENTConversation> conversations = null;
		try {
			ENTResponse response = chatManager.getPublicDialogs(customString);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getPublicDialogs(customString);
			}
			conversations = buildDialogsListFromResponse(response.getResponseData());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conversations;		
	}

	public List<ENTConversation> getConversations(){
		state = AndroidState.getInstance().getUserState();
	/*	try {
			state.setSMUser();
		} catch (DomainObjectWriteException e) {
			e.printStackTrace();
		} catch (AccountException e) {
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		}*/
		application = (EntradaApplication) EntradaApplication.getAppContext();
		reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		return reader.getConversations();
	}
	
	public List<ENTConversation> saveConversations(){
		List<ENTConversation> conversations = getPublicDialogs(null);
		state = AndroidState.getInstance().getUserState();
		application = (EntradaApplication) EntradaApplication.getAppContext();
		writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		if(conversations!=null){
			for (int i = 0; i < conversations.size(); i++) {
				try {
					ENTConversation conversation = conversations.get(i);
					writer.conversationInsertUpdate(conversation);
				} catch (DomainObjectWriteException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return conversations;
	}

	public void saveMessages(ENTConversation conversation){
		saveMessages(conversation, true);
	}
	
	public void saveMessages(ENTConversation conversation, boolean markMessagesRead){
		saveMessages(conversation, markMessagesRead, true);
	}
	
	public void saveMessages(ENTConversation conversation, boolean markMessagesRead, boolean triggerNotification){
		ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
		ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBMESSAGE);
			try {
				state = AndroidState.getInstance().getUserState();
				application = (EntradaApplication) EntradaApplication.getAppContext();
				reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				ENTMessage _message = reader.getRecentMessageFromConversation(conversation.getId());
				if(_message!=null){
					conversation.setCustomString("&date_sent[gt]="+_message.getSentDate());
				}
				EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
				Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
				APIService service = new APIService(env.getApi());
				if(conversation.getPassPhrase() == null) { 
					conversation.setPassPhrase(application.getPassPhrase(conversation.getId()));
				}
				if(conversation.getPassPhrase()==null || conversation.getPassPhrase().isEmpty()) {
					String passPhrase = service.getMessageThreadDetails(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId());
					application.addPassPhrase(conversation.getId(), passPhrase);
					conversation.setPassPhrase(passPhrase);
				}
				List<ENTMessage> messages = ((ENTQBMessageHandler) handler).getMessagesFromDialog(conversation);
				for(ENTMessage message : messages){
					if(!message.getSender().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))){
						message.setAsRead(0);
					}
					if(markMessagesRead){
						message.setAsRead(1);
					} 
					writer.addMessageToConversation(message);
					conversation.setUnreadMessagesCount(conversation.getUnreadMessagesCount());
					conversation.setLastMessage(message.getMessage());
					conversation.setLastMessageDateSent(message.getSentDate());
					conversation.setPatientID(message.getPatientID());
					if(!markMessagesRead) {
						if(triggerNotification) {
							NewConversationBroadcastService.processNotification(conversation);
						}
					}
				}
				writer.updateConversation(conversation);
				application.setIntIntoSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT, reader.getUnreadMessagesCount());
				if(!markMessagesRead) {
					//NewConversationBroadcastService.processNotification(conversation);
					Intent intent = NewConversationBroadcastService.getCountIntent();
					application.sendBroadcast(intent);
				}
			} catch (DomainObjectWriteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	

	@Override
	public List<ENTConversation> getAllDialogs(String customString) {
		List<ENTConversation> conversations = null;
		try {
			ENTResponse response = chatManager.getAllDialogs(customString);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getAllDialogs(customString);
			}
			conversations = buildDialogsListFromResponse(response.getResponseData());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conversations;
	}

	
	protected ENTConversation buildDialogFromResponse(String responseData){
		Gson gson = new Gson();
		ENTConversation conversation = gson.fromJson(responseData, ENTConversation.class);  
		return conversation;
	}
	
	protected List<ENTConversation> buildDialogsListFromResponse(String responseData){
		List<ENTConversation> conversationsList = new ArrayList<ENTConversation>();
		try {
			JSONObject json = new JSONObject(responseData);
			String items = null;
			try{
				items = json.getString("items");
			} catch(Exception e){
				items = responseData;
			}
			JSONArray array = new JSONArray(items);
			Gson gson = new Gson();
			for(int i=0; i< array.length(); i++) {
				ENTConversation conversation = gson.fromJson(array.getJSONObject(i).toString(),ENTConversation.class);
				conversationsList.add(conversation);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return conversationsList;
	}

}
