package com.entradahealth.entrada.core.inbox.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Attachment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTResponse;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.qb.gson.Gson;

public class ENTQBChatManagerImpl implements ENTChatManager {
	
	public static String APPLICATION_ID = "19779";
	public static String AUTHORIZATION_KEY = "kzFMnbMv2tMJbVj";
	public static String AUTHORIZATION_SECRET = "UzMuxXwJTVwtCGv";	

	private APIService service;
	private static final String QBTOKEN = "QBToken";
	private EntradaApplication application;
	private static final String SM_CREATESESSION = "SM_CREATESESSION";
	private static final String SM_LOGIN = "SM_LOGIN";
	private static final String SM_LOGOUT = "SM_LOGOUT";
	private static final String SM_USERS = "SM_USERS";
	private static final String SM_MESSAGE = "SM_MESSAGE";
	private static final String SM_MESSAGES = "SM_MESSAGES";
	private static final String SM_DIALOG = "SM_DIALOG";
	private static final String SM_IMAGE = "SM_IMAGE";
	private static final String SM_AUDIO = "SM_AUDIO";
	private static String QB_API_URL = "api.quickblox.com";
	private static String QB_CHAT_DOMAIN = "chat.quickblox.com";
	private static String QB_BUCKET_NAME = "qbprod";
	private EnvironmentHandlerFactory factory;
	
	public ENTQBChatManagerImpl() {
		application = (EntradaApplication) EntradaApplication.getAppContext();
		String environmentStr = application.getStringFromSharedPrefs("environment");
		factory = EnvironmentHandlerFactory.getInstance();
		Environment environment = factory.getHandler(environmentStr);
		QB_API_URL = environment.getQbApiDomain();
		APPLICATION_ID = environment.getQbApplicationId();
		AUTHORIZATION_KEY = environment.getQbAuthorizationKey();
		AUTHORIZATION_SECRET = environment.getQbAuthorizationSecret();
		QB_BUCKET_NAME = environment.getQbBucketName();
		QB_CHAT_DOMAIN = environment.getQbChatDomain();
		service = new APIService(EntradaApplication.getAppContext(), QB_API_URL);
	}
	
	public String getAPPLICATION_ID() {
		return APPLICATION_ID;
	}
	
	public String getAUTHORIZATION_KEY() {
		return AUTHORIZATION_KEY;
	}
	
	public String getAUTHORIZATION_SECRET() {
		return AUTHORIZATION_SECRET;
	}
	
	
	public static String getQBAPIURL() {
		return QB_API_URL;
	}

	public static String getQBCHATDOMAIN() {
		return QB_CHAT_DOMAIN;
	}

	public static String getQBBUCKETNAME() {
		return QB_BUCKET_NAME;
	}

	@Override
	public ENTResponse createSession(ENTUser user) throws Exception {
		Random random = new Random();
		String nonce = String.valueOf(random.nextInt());
		String timeStamp = service.getTimeStamp();
		String signatureBody = "application_id="+APPLICATION_ID+"&auth_key="+AUTHORIZATION_KEY+"&nonce="+nonce+"&timestamp="+timeStamp;
		String signature = service.hmacDigest(signatureBody, AUTHORIZATION_SECRET, "HmacSHA1");
		JSONObject json = new JSONObject();
		json.accumulate("application_id", APPLICATION_ID);
		json.accumulate("auth_key", AUTHORIZATION_KEY);
		json.accumulate("timestamp", timeStamp);
		json.accumulate("nonce", nonce);
		json.accumulate("signature", signature);
		HttpResponse response = service.sendPostRequest(SM_CREATESESSION, "https://"+QB_API_URL + "/session.json", json.toString());
		ENTResponse entResponse = service.buildENTResponse(SM_CREATESESSION, response);
		setTokeninSharedPrefs(entResponse.getResponseData());
		return entResponse;
	}
	
	public void setTokeninSharedPrefs(String responseData) throws ParseException, JSONException, IOException{
		JSONObject json = new JSONObject(responseData);
		JSONObject session = json.getJSONObject("session");
		String qbToken = (String) session.get("token");
		application.setStringIntoSharedPrefs(QBTOKEN, qbToken);
	}
	
	@Override
	public ENTResponse getUserById(String userId) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		String url = "https://"+QB_API_URL + "/users/"+userId+".json";
		HttpResponse response = service.sendGetRequest(SM_DIALOG, url, application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_DIALOG, response);
		return entResponse;
	}
	
	@Override
	public ENTResponse login(ENTUser user) throws ClientProtocolException, IOException {
		JSONObject json = new JSONObject();
		try {
			json.accumulate("login", user.getLogin());
			json.accumulate("password", user.getPassword());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpResponse response = service.sendPostRequest(SM_LOGIN, "https://"+QB_API_URL + "/login.json", json.toString(), application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_LOGIN, response);
		return entResponse;
	}

	@Override
	public ENTResponse getBuddyList() throws UnsupportedEncodingException, ClientProtocolException, IOException {
		HttpResponse response = service.sendGetRequest(SM_USERS, "https://"+QB_API_URL + "/users.json?page=1&per_page=100", application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_USERS, response);
		return entResponse;
	}

	@Override
	public ENTResponse createDialog(ENTMessage message) throws ClientProtocolException, IOException, ParseException, JSONException{
		JSONObject json = new JSONObject();
		try {
			json.accumulate("type", 2);
			json.accumulate("name", "NO NAME");
			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < message.getRecipients().length; i++) {
				strBuf.append(message.getRecipients()[i]);
				strBuf.append(",");
			}
			String str = strBuf.toString();
			json.accumulate("occupants_ids", str.substring(0, str.length()-1));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpResponse response = service.sendPostRequest(SM_MESSAGE, "https://"+ QB_API_URL + "/chat/Dialog.json", json.toString(), application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_MESSAGE, response);
		return entResponse;
	}
	
	@Override
	public ENTResponse sendMessage(ENTMessage message) throws ClientProtocolException, IOException, ParseException, JSONException {
		JSONObject json = new JSONObject();
		try {
			json.accumulate("recipient_id", null);
			json.accumulate("chat_dialog_id", message.getChatDialogId());
			json.accumulate("message", message.getMessage());
			json.accumulate("application_id", APPLICATION_ID);
			if(message.getPatientID()!=0)
				json.accumulate("patient_id", message.getPatientID());
			if(message.getCustomString()!=null)
				json.accumulate("type", message.getCustomString());
			if(message.getAttachmentID()!=null){
				Attachment attachment = new Attachment();
				if(message.getContentType() == ENTMessage.IMAGE){
					attachment.setType("image");
				} else if(message.getContentType() == ENTMessage.AUDIO) {
					attachment.setType("audio");
				}
				attachment.setId(message.getAttachmentID());
				Gson gson = new Gson();
				JSONObject jsonOb = new JSONObject();
				jsonOb.accumulate("0", new JSONObject(gson.toJson(attachment)));
				json.accumulate("attachments", jsonOb);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpResponse response = service.sendPostRequest(SM_MESSAGE, "https://"+ QB_API_URL + "/chat/Message.json", json.toString(), application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_MESSAGE, response);
		return entResponse;
	}

	@Override
	public ENTResponse onReceiveMessage() {
		return null;
	}

	@Override
	public ENTResponse getChatMessages(ENTConversation conversation) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		String url = "https://"+ QB_API_URL + "/chat/Message.json?chat_dialog_id="+conversation.getId()+((conversation.getCustomString() ==null) ? "" : "&"+conversation.getCustomString());
		HttpResponse response = service.sendGetRequest(SM_MESSAGES, url, application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_MESSAGES, response);
		return entResponse;
	}
	
	public ENTResponse getDialogs(String queryParams) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		String url = "https://"+ QB_API_URL + "/chat/Dialog.json"+((queryParams == null) ? "" : "?"+queryParams);
		HttpResponse response = service.sendGetRequest(SM_DIALOG, url, application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_DIALOG, response);
		return entResponse;
	}

	@Override
	public ENTResponse getPrivateDialogs(String customString) throws UnsupportedEncodingException,
			ClientProtocolException, IOException {
		return getDialogs("type=3"+(customString == null? "":"&"+customString));
	}

	@Override
	public ENTResponse getPublicDialogs(String customString) throws UnsupportedEncodingException,
			ClientProtocolException, IOException {
		return getDialogs("type=2"+(customString == null? "":"&"+customString));
	}
	
	@Override
	public ENTResponse getAllDialogs(String customString) throws UnsupportedEncodingException, ClientProtocolException, IOException{
		return getDialogs(null+(customString == null? "":customString));
	}

	@Override
	public ENTResponse deleteDialog(ENTConversation conversation) throws UnsupportedEncodingException, ClientProtocolException, IOException {
		HttpResponse response = service.sendDeleteRequest(SM_DIALOG, "https://"+QB_API_URL + "/chat/Dialog/"+conversation.getId()+".json", application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_DIALOG, response);
		return entResponse;
	}
	
	@Override
	public ENTResponse logout() throws ParseException, IOException{
		HttpResponse response = service.sendDeleteRequest(SM_LOGOUT, "https://"+ QB_API_URL+"/login.json", application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_LOGOUT, response);
		return entResponse;
	}
	
	@Override
	public ENTResponse updateDialog(ENTConversation conversation) throws ClientProtocolException, IOException {
		JSONObject json = new JSONObject();
		try {
			if(!conversation.getName().isEmpty() && conversation.getName()!=null) {
				json.accumulate("name", conversation.getName());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpResponse response = service.sendPutRequest(SM_DIALOG, "https://"+ QB_API_URL + "/chat/Dialog/"+conversation.getId()+".json", json.toString(), application.getStringFromSharedPrefs(QBTOKEN));
		ENTResponse entResponse = service.buildENTResponse(SM_DIALOG, response);
		return entResponse;
	}
	
	@Override
	public void login() throws Exception, ClientProtocolException, IOException {
		ENTUser user = new ENTUser();
		String login = application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN);
		String password = application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_PASSWORD);
		user.setLogin(login);
		user.setPassword(password);
		createSession(user);
		login(user);
		getBuddyList();
	}	
	
}
