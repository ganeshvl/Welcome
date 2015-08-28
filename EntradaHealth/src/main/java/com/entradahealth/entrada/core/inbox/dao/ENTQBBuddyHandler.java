package com.entradahealth.entrada.core.inbox.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.inbox.service.ENTChatManager;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.google.gson.Gson;

public class ENTQBBuddyHandler implements ENTBuddyHandler{

	private ENTChatManager chatManager;
	private EntradaApplication application;
	private SMDomainObjectWriter writer;
	private SMDomainObjectReader reader;
	private UserState state;
	
	public ENTQBBuddyHandler() {
		chatManager = new ENTQBChatManagerImpl();
		application = (EntradaApplication) EntradaApplication.getAppContext();
	}
	
	@Override
	public List<ENTUser> getBuddyList() {
		try {
			ENTResponse response = chatManager.getBuddyList();
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getBuddyList();
			}
			return buildUsersListFromResponse(response.getResponseData());
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

	public List<ENTUser> getBuddies(){
		state = AndroidState.getInstance().getUserState();
		try {
			state.setSMUser();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		return reader.getBuddies();
	}
	
	public void saveContacts(){
		state = AndroidState.getInstance().getUserState();
		try {
			state.setSMUser();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
			Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
			APIService service = new APIService(env.getApi());
			String response = service.getSMContacts(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN));
			JSONObject object = new JSONObject(response);
			String contacts = object.getString("Contacts");
			JSONArray array = new JSONArray(contacts);
			writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
			writer.deletePendingInvites();
			for(int i=0; i< array.length(); i++) {
				JSONObject contact = new JSONObject(array.getJSONObject(i).toString());
				ENTUser user = new ENTUser();
				try {
					user.setFavorite(contact.getBoolean("IsFavorite"));
				} catch (Exception e) {
					user.setFavorite(false);
				}
				try {
					user.setFirstName(contact.getString("FirstName"));
				} catch(Exception e){
					user.setFirstName("");
				}
				try {
					user.setLastName(contact.getString("LastName"));
				} catch(Exception e){
					user.setLastName("");
				}
				try {
					user.setMI(contact.getString("MI"));
				} catch(Exception e){
					user.setMI("");
				}
				if(contact.getInt("UserId") != 0){
					user.setId(contact.getString("QuickBloxUserID"));
					user.setLogin(contact.getString("QBUserLogin"));
					if(!user.getFirstName().isEmpty() || !user.getLastName().isEmpty())
						writer.buddyInsertUpdate(user);
				} else {
					if(!user.getFirstName().isEmpty() || !user.getLastName().isEmpty())
						writer.pendingInviteInsertUpdate(user);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (DomainObjectWriteException e) {
			e.printStackTrace();
		}

	}
	
	public List<ENTUser> buildUsersListFromResponse(String responseData){
		List<ENTUser> usersList = new ArrayList<ENTUser>();
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
				ENTUser user = gson.fromJson(array.getJSONObject(i).getJSONObject("user").toString(),ENTUser.class);
				usersList.add(user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return usersList;
	}

	@Override
	public List<ENTUser> getRecentCommunicatedUsersList(String currentUserID) {
		List<ENTUser> users = new ArrayList<ENTUser>();
		try {
			ENTResponse response = chatManager.getPublicDialogs(null);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response =  chatManager.getPublicDialogs(null);
			}
			Set<String> usersList = buildUserListFromResponse(response.getResponseData());
			usersList.remove(currentUserID);
			for (String user : usersList) {
				Log.i("","ENTUser--"+ user);
				ENTResponse res = chatManager.getUserById(user);	
				ENTUser entUser = buildUserFromResponse(res.getResponseData());
				Log.i("","ENTUser--"+ entUser.getName());
				users.add(entUser);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return users;
	}
	
	protected Set<String> buildUserListFromResponse(String responseData){
		Set<String> users = new HashSet<String>();
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
				Collections.addAll(users, conversation.getOccupantsIds());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return users;
	}

	protected ENTUser buildUserFromResponse(String responseData){
		ENTUser user = null;
		try {
			JSONObject json = new JSONObject(responseData);
			Gson gson = new Gson();
			user = gson.fromJson(json.getJSONObject("user").toString(), ENTUser.class);
		} catch (JSONException e) {
			e.printStackTrace();
		}
			return user;
	}

}
