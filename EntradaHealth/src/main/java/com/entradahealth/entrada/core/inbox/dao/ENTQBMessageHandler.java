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

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.encryption.AES256Cipher;
import com.entradahealth.entrada.core.inbox.service.ENTChatManager;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.google.gson.Gson;

public class ENTQBMessageHandler implements ENTMessageHandler{

	private ENTChatManager chatManager;
	private UserState state;
	private EntradaApplication application;
	private SMDomainObjectReader reader;
	private AES256Cipher cipher;

	public ENTQBMessageHandler() {
		chatManager = new ENTQBChatManagerImpl();
		application = (EntradaApplication) EntradaApplication.getAppContext();
		cipher = new AES256Cipher();
	}
	
	@Override
	public ENTMessage sendMessage(ENTMessage message) {
		try {
			// Message AES Encryption
			String passPhrase = message.getPassPhrase();
			String encryptedText = cipher.encryptText(message.getMessage(), passPhrase);
			message.setMessage(encryptedText);
			// Sending the encrypted Message
			ENTResponse response = chatManager.sendMessage(message);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.sendMessage(message);
			}
			message = buildENTMessageFromResponse(response.getResponseData());
			// Decrypting the message
			String decryptedMessage = cipher.decryptText(message.getMessage(), passPhrase);
			message.setMessage(decryptedMessage);
			return message;
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
		return null;
	}

	public ENTMessage buildENTMessageFromResponse(String response){
		Gson gson = new Gson();
		ENTMessage message = gson.fromJson(response, ENTMessage.class);
		return message;
	}

	public List<ENTMessage> getMessages(ENTConversation conversation){
		state = AndroidState.getInstance().getUserState();
		try {
			state.setSMUser();
		} catch (DomainObjectWriteException e) {
			e.printStackTrace();
		} catch (AccountException e) {
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		}
		application = (EntradaApplication) EntradaApplication.getAppContext();
		reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		return reader.getMessagesFromConversation(conversation.getId());
	}	
	
	@Override
	public List<ENTMessage> getMessagesFromDialog(ENTConversation conversation) {
		try {
			ENTResponse response = chatManager.getChatMessages(conversation);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getChatMessages(conversation);
			}
			return buildENTMessageListFromResponse(response.getResponseData(), conversation.getPassPhrase());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ENTMessage> buildENTMessageListFromResponse(String responseData, String passPhrase){
		List<ENTMessage> messagesList = new ArrayList<ENTMessage>();
		try {
			JSONObject json = new JSONObject(responseData);
			String items = json.getString("items");			
			JSONArray array = new JSONArray(items);
			Gson gson = new Gson();
			for(int i=0; i< array.length(); i++) {
				ENTMessage message = gson.fromJson(array.getJSONObject(i).toString(),ENTMessage.class);
				try {
					message = decryptMessage(cipher, message, passPhrase);
				} catch (Exception e) {
					e.printStackTrace();
				}
				messagesList.add(message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return messagesList;
	}
	
	protected ENTMessage decryptMessage(AES256Cipher cipher, ENTMessage message, String passPhrase) throws Exception{
		String decryptedMessage = cipher.decryptText(message.getMessage(), passPhrase);
		message.setMessage(decryptedMessage);
		return message;
	}
}
