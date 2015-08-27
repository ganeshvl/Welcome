package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.personal.push.utils.PlayServicesHelper;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBUserHandler;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.entradahealth.entrada.core.inbox.service.SaveSMContentService;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.RemoteService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;

public class UserAuthenticate extends DialogTask<Boolean>{

    protected String apiHost;
    protected String username;
    protected String password;
    private Activity _activity;
    private JSONObject json;
	private PlayServicesHelper playServicesHelper;

    public UserAuthenticate(String apiHost, String username, String password, Activity _activity){
    	super(_activity, "Syncing server data", "Please wait...", false);
    	this.apiHost = apiHost;
    	this.username = username;
    	this.password = password;
    	this._activity = _activity;
    }
    
	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			RemoteService svc = new APIService(apiHost, username, password);
			String m_androidId = Secure.getString(_activity.getContentResolver(), Secure.ANDROID_ID);
    		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			if(application.is3GOr4GConnected() || application.isWifiConnected()) {
			String s = svc.authenticate(username, password, m_androidId);
			json = new JSONObject(s);
			Log.i("USER_AUTHENTICATE", "USER_AUTHENTICATE--"+s);
    		String qbLogin = json.getString("QBLogin");
    		String qbPassword = json.getString("QBPassword");
    		application.setStringIntoSharedPrefs(BundleKeys.SESSION_TOKEN, json.getString("SessionToken").trim());
    		application.setStringIntoSharedPrefs(BundleKeys.TOUVERSION, json.getString("TOUVersionNumber").trim());
    		application.setStringIntoSharedPrefs(BundleKeys.USERID, json.getString("UserId").trim());
        	// Parsing for permissions
			String _perStr = json.getString("ModulePermissions");
			String perStr = _perStr.substring(1, _perStr.length()-1);
			String[] modulePermissions = perStr.split(",");
			Boolean joblistPermission = new Boolean(modulePermissions[0]);
			Boolean secureMessagingPermission = new Boolean(modulePermissions[2]);
    		application.setJobListPermission(joblistPermission);
    		ENTUser user = new ENTUser();
    		user.setLogin(qbLogin);
    		user.setPassword(qbPassword);
    		ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
    		ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBUSER);
    		// QB create session
    		((ENTQBUserHandler) handler).createSession(user);
    		// Login User
    		user = ((ENTQBUserHandler) handler).login(user);
    		Log.i("","Login User info - "+ user.getId() +" "+ user.getLogin()+" "+user.getName());
    		UserPrivate u = AndroidState.getInstance().getUserState().getUserData();
    		u.createUser(user.getLogin());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_USER_ID, user.getId());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_LOGIN, user.getLogin());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_PASSWORD, qbPassword);
    		UserState state = AndroidState.getInstance().getUserState();
    		try {
    			state.setSMUser();
    			if(application.isJobListEnabled()){
    					state.setCurrentAccount(application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID));
    			}
    		} catch (DomainObjectWriteException e) {
    			e.printStackTrace();
    		} catch (AccountException e) {
    			e.printStackTrace();
    		} catch (InvalidPasswordException e) {
    			e.printStackTrace();
    		}
			}
    		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			json = null;
			e.printStackTrace();
		} catch (ServiceException e) {
			json = null;
			e.printStackTrace();
		} catch (JSONException e) {
			json = null;
			e.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void instantiateQBSDK(Activity activity){
		ENTQBChatManagerImpl chatManager = new ENTQBChatManagerImpl();
		QBSettings.getInstance().fastConfigInit(String.valueOf(chatManager.getAPPLICATION_ID()), chatManager.getAUTHORIZATION_KEY(), chatManager.getAUTHORIZATION_SECRET());
		  // specify custom domains
        QBSettings.getInstance().setServerApiDomain(chatManager.getQBAPIURL());
        QBSettings.getInstance().setChatServerDomain(chatManager.getQBCHATDOMAIN());
        QBSettings.getInstance().setContentBucketName(chatManager.getQBBUCKETNAME());
		if (!QBChatService.isInitialized()) {
	      QBChatService.init(activity);
	    }
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		createSession(application);
	}
	   private void createSession(EntradaApplication application) {
	        final QBUser qbUser = new QBUser(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN), application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_PASSWORD));
	        final QBChatService chatService = QBChatService.getInstance();
	        // Create QuickBlox session
	        //
	        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
	            @Override
	            public void onSuccess(QBSession qbSession, Bundle bundle) {
	            	qbUser.setId(qbSession.getUserId());
	            	login(chatService, qbUser);
	            }

	            @Override
	            public void onError(List<String> strings) {
	            }
	        });
	        
	    }
	    
	    protected void login(QBChatService chatService, QBUser qbUser){
	    	chatService.login(qbUser, new QBEntityCallbackImpl() {
	            @Override
	            public void onSuccess() {
	        		playServicesHelper = new PlayServicesHelper(_activity);
	            }

	            @Override
	            public void onError(List errors) {
	            }
	        });
	    }
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		instantiateQBSDK(activity);
//		Intent intent = new Intent(activity, SaveSMContentService.class);
//		activity.startService(intent);
		onPostSuccessful();
	}
	
	protected void onPostSuccessful(){
		dialog.dismiss();
		progress(ProgressUpdateType.TOAST, "Done syncing data");
	}
	
}

