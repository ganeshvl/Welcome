package com.entradahealth.entrada.core.inbox.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;

public interface ENTChatManager {

	ENTResponse createSession(ENTUser user) throws Exception;
    ENTResponse login(ENTUser user) throws ClientProtocolException, IOException;
    ENTResponse getBuddyList() throws UnsupportedEncodingException, ClientProtocolException, IOException;
    ENTResponse sendMessage(ENTMessage message) throws ClientProtocolException, IOException, ParseException, JSONException;
    ENTResponse onReceiveMessage();
    ENTResponse createDialog(ENTMessage message) throws ClientProtocolException, IOException, ParseException, JSONException;
    ENTResponse updateDialog(ENTConversation conversation) throws ClientProtocolException, IOException;
	ENTResponse getPrivateDialogs(String customString) throws UnsupportedEncodingException, ClientProtocolException, IOException;
	ENTResponse getPublicDialogs(String customString) throws UnsupportedEncodingException, ClientProtocolException, IOException;
	ENTResponse getAllDialogs(String customString) throws UnsupportedEncodingException, ClientProtocolException, IOException;
	ENTResponse getChatMessages(ENTConversation conversation) throws UnsupportedEncodingException, ClientProtocolException,	IOException;
	ENTResponse getUserById(String userId) throws UnsupportedEncodingException, ClientProtocolException, IOException;
	void login() throws Exception, ClientProtocolException, IOException;
	ENTResponse deleteDialog(ENTConversation conversation) throws UnsupportedEncodingException, ClientProtocolException, IOException;
	ENTResponse logout() throws ParseException, IOException;
    
}
