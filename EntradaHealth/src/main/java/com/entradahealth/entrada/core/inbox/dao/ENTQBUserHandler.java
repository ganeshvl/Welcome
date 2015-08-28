package com.entradahealth.entrada.core.inbox.dao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTSession;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.inbox.exceptions.QBException;
import com.entradahealth.entrada.core.inbox.service.ENTChatManager;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.google.gson.Gson;

public class ENTQBUserHandler implements ENTUserHandler{

	private ENTChatManager chatManager;
	
	public ENTQBUserHandler(){
		chatManager = new ENTQBChatManagerImpl();
	}
	
	@Override
	public ENTSession createSession(ENTUser user) {
		ENTSession session = null;
		try {
			ENTResponse response = chatManager.createSession(user);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.createSession(user);
			}
			session= buildENTSessionfromResponse(response.getResponseData());
		} catch(QBException exception){
			throw exception;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return session;
	}
	
	@Override
	public ENTUser getUser(String userId){
		ENTUser user = null;
		try {
			ENTResponse response = chatManager.getUserById(userId);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response = chatManager.getUserById(userId);
			}
			user = buildENTUserfromResponse(response.getResponseData());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}
	
	@Override
	public ENTUser login(ENTUser user) {
		ENTUser eUser = null;
		try {
			ENTResponse response = chatManager.login(user);
			if(response.getStatusCode() == 422 || response.getStatusCode() == 401){
				chatManager.login();
				response =  chatManager.login(user);
			}			
			eUser = buildENTUserfromResponse(response.getResponseData());
		} catch(QBException exception){
			throw exception;
		}  catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eUser;
	}

	@Override
	public void logout() {
		try {
			chatManager.logout();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

   protected ENTSession buildENTSessionfromResponse(String responseData) throws JSONException {
	   JSONObject json = new JSONObject(responseData);
	   Gson gson = new Gson();
	   String sessionStr = null;
	   try {
		   sessionStr = json.getJSONObject("session").toString();
	   } catch (Exception e) {
		   sessionStr = responseData;
	   }
	   Log.i("", "SESSION_OBJ : "+sessionStr);
	   ENTSession session = gson.fromJson(sessionStr, ENTSession.class);
	   return session;
   }
	
	protected ENTUser buildENTUserfromResponse(String responseData) throws JSONException{
		JSONObject json = new JSONObject(responseData);
		Gson gson = new Gson();
		String userStr = null;
		try {
			userStr = json.getJSONObject("user").toString();
		} catch (Exception e) {
			userStr = responseData;
		}
  	    Log.i("", "USER_OBJ : "+userStr);		
		ENTUser user = gson.fromJson(userStr, ENTUser.class);
		return user;
	}

}
