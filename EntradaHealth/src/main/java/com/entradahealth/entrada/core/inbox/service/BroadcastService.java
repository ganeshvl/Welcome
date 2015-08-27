package com.entradahealth.entrada.core.inbox.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTQBMessageHandler;

public class BroadcastService extends Service{
	private static final String TAG = "BroadcastService";
	public static final String BROADCAST_ACTION = "com.entradahealth.broadcastmessage";
	private final Handler handler = new Handler();
	private Intent intent;
	private static ENTConversation conversation;
	private EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
	
	public static void initialize(ENTConversation entConversation){
		conversation = entConversation;
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = (EntradaApplication) EntradaApplication.getAppContext();
		conversation = BundleKeys.CURRENT_CONVERSATION;
    	intent = new Intent(BROADCAST_ACTION);	
	}
	
    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.removeCallbacks(sendUpdatesToDB);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
        handler.postDelayed(sendUpdatesToDB, 10000); // 10 seconds
   
    }

    private Runnable sendUpdatesToUI = new Runnable() {
    	public void run() {
    		broadcastMessages();    		
    	    handler.postDelayed(this, 1000); // 1 second
    	}
    };    
    
    private Runnable sendUpdatesToDB = new Runnable() {
    	public void run() {
    		updateConversations();    		
    	    handler.postDelayed(this, 10000); // 10 seconds
    	}

    };   

	private void updateConversations() {
		//UpdateConversationsTask task = new UpdateConversationsTask();
		//task.execute();
	}
    
    private void broadcastMessages() {
    	if(application.is3GOr4GConnected() || application.isWifiConnected()) {
    		GetMessagesTask task = new GetMessagesTask();
    		task.execute();
    	}

    }

	class GetMessagesTask extends AsyncTask<Void, Void, Void> {
		private ArrayList<ENTMessage> messageslist;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBMESSAGE);
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				conversation.setCustomString("sender_id[ne]="+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID)+"&date_sent[gte]="+BundleKeys.LASTMESSAGETIME+"&_id[ne]="+BundleKeys.LASTMESSAGEID);
				messageslist = (ArrayList<ENTMessage>) ((ENTQBMessageHandler) handler).getMessagesFromDialog(conversation);
				if(messageslist != null && messageslist.size()>0) {
					intent.putExtra("messageslist", messageslist);
					sendBroadcast(intent);
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
    
	class UpdateConversationsTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
	    		((ENTQBConversationHandler) handler).saveConversations();
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
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {		
        handler.removeCallbacks(sendUpdatesToUI);
        handler.removeCallbacks(sendUpdatesToDB);	
		super.onDestroy();
	}	
	
}
