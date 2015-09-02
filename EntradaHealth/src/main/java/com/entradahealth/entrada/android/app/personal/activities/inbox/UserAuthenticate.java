package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.personal.push.utils.PlayServicesHelper;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBUserHandler;
import com.entradahealth.entrada.core.inbox.service.ENTQBChatManagerImpl;
import com.entradahealth.entrada.core.inbox.service.SaveMessagesContentService;
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
	private List<Dictator> dictatorsFromService;
	private List<Dictator> dictatorsFromDB;

    public UserAuthenticate(String apiHost, String username, String password, Activity _activity){
    	super(_activity, "Syncing server data", "Please wait...", false);
    	this.apiHost = apiHost;
    	this.username = username;
    	this.password = password;
    	this._activity = _activity;
    	dictatorsFromService = new ArrayList<Dictator>();
    	dictatorsFromDB = new ArrayList<Dictator>();
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
    		String sessionToken = json.getString("SessionToken").trim();
    		application.setStringIntoSharedPrefs(BundleKeys.SESSION_TOKEN, sessionToken);
    		application.setStringIntoSharedPrefs(BundleKeys.TOUVERSION, json.getString("TOUVersionNumber").trim());
    		application.setStringIntoSharedPrefs(BundleKeys.USERID, json.getString("UserId").trim());
        	// Parsing for permissions
			String _perStr = json.getString("ModulePermissions");
			String perStr = _perStr.substring(1, _perStr.length()-1);
			String[] modulePermissions = perStr.split(",");
			Boolean joblistPermission = new Boolean(modulePermissions[0]);
			Boolean secureMessagingPermission = new Boolean(modulePermissions[2]);
    		application.setJobListPermission(joblistPermission);
    		// Get Dictators, compare and update in database
    		dictatorsFromService = svc.getAssociatedDictators(sessionToken);
    		MainUserDatabaseProvider provider = new MainUserDatabaseProvider(false);
    		dictatorsFromDB = provider.getDictatorsForUser(username);
    		UserState state = AndroidState.getInstance().getUserState();
            UserPrivate u = state.getUserData();
            if(dictatorsFromService.size()==0) {
            	application.setJobListPermission(false);
            }
    		// Add dictators
            boolean flag = true;
    		for(int i=0; i<dictatorsFromService.size(); i++) {
    			Dictator i_dic = dictatorsFromService.get(i);
    			boolean isExist = false;
    			for(int j=0; j<dictatorsFromDB.size(); j++) {
    				Dictator j_dic = dictatorsFromDB.get(j);
    				if(i_dic.getDictatorID() == j_dic.getDictatorID()){
    					isExist = true;
    					break;
    				}
    			}
    			if(!isExist){
    				if(dictatorsFromDB.size() == 0){
    					i_dic.setCurrent(flag);
    				}
    				provider.addDictator(i_dic, username);
    				Log.e("", "Dictator Name--"+i_dic.getDictatorName()+"--username--"+username);
    				u.createAccount(String.valueOf(i_dic.getDictatorID()), i_dic.getDictatorName(), i_dic.getDictatorName(), "", apiHost, i_dic.getClinicName(), username, password);
    				u.save();
    				flag = false;
    			}
                try
                {
                    AndroidState.getInstance().clearUserState();
                    state = AndroidState.getInstance().getUserState();
                }
                catch (Exception ex)
                {
                	ex.printStackTrace();
                }
    		}
    		
    		// Delete Dictators
    		for(int i=0; i<dictatorsFromDB.size(); i++) {
    			Dictator i_dic = dictatorsFromDB.get(i);
    			boolean isExist = false;
    			for(int j=0; j<dictatorsFromService.size(); j++) {
    				Dictator j_dic = dictatorsFromService.get(j);
    				if(i_dic.getDictatorID() == j_dic.getDictatorID()){
    					isExist = true;
    					break;
    				}
    			}
    			if(!isExist){
    				boolean isCurrent = i_dic.isCurrent();
    				provider.deleteDictator(i_dic);
    				if(isCurrent){
    				dictatorsFromDB.get(0).setCurrent(true);	
    				}

    			}
    		}
    		Dictator dictator = provider.getCurrentDictatorForUser(username);
    		Long dictatorId = (dictator != null)? dictator.getDictatorID() : 0;
    		String dictatorName = (dictator != null)? dictator.getDictatorName() : "";
			application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_ID, String.valueOf(dictatorId));
			application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_NAME, dictatorName);

    		ENTUser user = new ENTUser();
    		user.setLogin(qbLogin);
    		user.setPassword(qbPassword);
    		ENTHandlerFactory handlerFactory = ENTHandlerFactory.createInstance();
    		ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBUSER);
    		// QB create session
    		((ENTQBUserHandler) handler).createSession(user);
    		// Login User
    		user = ((ENTQBUserHandler) handler).login(user);
    		Log.i("","Login User info - "+ user.getId() +" "+ user.getLogin()+" "+user.getName());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_USER_ID, user.getId());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_LOGIN, user.getLogin());
    		application.setStringIntoSharedPrefs(BundleKeys.CURRENT_QB_PASSWORD, qbPassword);
    		try {
    			state.setSMUser();
    			if(dictatorId != 0){
					AndroidState.getInstance().clearUserState();
                    state = AndroidState.getInstance().getUserState();
    				state.setCurrentAccount(String.valueOf(dictatorId));
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
		} catch (IOException e) {
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
	            	logout(chatService);
	            	login(chatService, qbUser);
	            }

	            @Override
	            public void onError(List<String> strings) {
	            }
	        });
	        
	    }
	    
	   	protected void logout(QBChatService chatService){
	   		boolean isLoggedIn = chatService.isLoggedIn();
	   		if(!isLoggedIn){
	   		    return;
	   		}
	    	chatService.logout(new QBEntityCallbackImpl() {
	    		 
	    	    @Override
	    	    public void onSuccess() {
	    	        // success
	    	    }
	    	 
	    	    @Override
	    	    public void onError(final List list) {
	    	 
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
	            	Log.e("","QBSDK_LOGIN_ERRORS--"+errors);
	            }
	        });
	    }
	
	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		instantiateQBSDK(activity);
		if(!SaveMessagesContentService.isRunning()){
			Intent intent = new Intent(activity, SaveSMContentService.class);
			activity.startService(intent);
		}
		onPostSuccessful();
	}
	
	protected void onPostSuccessful(){
		try{
			if(dialog!=null) {
				if(dialog.isShowing()) {
					dialog.dismiss();
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		//progress(ProgressUpdateType.TOAST, "Done syncing data");
	}
	
}

