package com.entradahealth.entrada.core.inbox.service;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.Consts;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider.DatabaseProviderException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;

public class NewConversationBroadcastService extends Service{
	private static final String TAG = "BroadcastService";
	public static final String BROADCAST_ACTION = "com.entradahealth.broadcastnewmessage";
	
	public static final String BROADCAST_COUNTUPDATE_ACTION = "com.entradahealth.broadcastunreadcount";
	
	private final Handler handler = new Handler();
	private Intent intent;
	private boolean flag;
	private EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
	private NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 2;
	
	private Intent countIntent;
	private ENTHandler conversationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
		conversationHandler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
		application = (EntradaApplication) EntradaApplication.getAppContext();
    	intent = new Intent(BROADCAST_ACTION);	
    	countIntent = new Intent(BROADCAST_COUNTUPDATE_ACTION);
	}
	
    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 10000); // 10 second
   
    }

    private Runnable sendUpdatesToUI = new Runnable() {
    	public void run() {
    		broadcastMessages();    		
    	    handler.postDelayed(this, 10000); // 10 second
    	}
    };    
    
    private void broadcastMessages() {
    	if(application.is3GOr4GConnected() || application.isWifiConnected()) {
	    	GetDialogsTask task = new GetDialogsTask();
	    	task.execute();
    	}

    }

	class GetDialogsTask extends AsyncTask<Void, Void, Void> {
		private ArrayList<ENTConversation> conversationslist;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				flag= false;
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
				conversationslist = (ArrayList<ENTConversation>) ((ENTQBConversationHandler) handler).getPublicDialogs(null);
				UserState state = AndroidState.getInstance().getUserState();
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				state.setSMUser();
				SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				SMDomainObjectWriter writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
				int count = 0;
				for (int i = 0; i < conversationslist.size(); i++) {
					ENTConversation conversation = conversationslist.get(i);
					count = count + conversation.getUnreadMessagesCount();
					if(conversation.getUnreadMessagesCount()>0){
						flag = true;
						try{
							ENTConversation _conversation = reader.getConversationById(conversation.getId());
							 if(conversation.getUnreadMessagesCount()>_conversation.getUnreadMessagesCount()){
								 processNotification(conversation);
								 sendBroadcast(countIntent);
								 application.setIntIntoSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT, count);
							 }
							if(_conversation.getId().equals(conversation.getId())){
								conversation.setPatientID(_conversation.getPatientID());
								writer.updateConversation(conversation, false);
								((ENTQBConversationHandler) conversationHandler).saveMessages(conversation);
							}
						} catch(DatabaseProviderException ex){
							try {
								processNotification(conversation);
								sendBroadcast(countIntent);
								application.setIntIntoSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT, count);
								writer.addConversation(conversation,false);
								((ENTQBConversationHandler) conversationHandler).saveMessages(conversation);
							} catch (DomainObjectWriteException e) {
								e.printStackTrace();
							}
						} catch (DomainObjectWriteException e) {
							e.printStackTrace();
						}
					}
				}
				application.setIntIntoSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT, count);
				conversationslist.clear();
				conversationslist.addAll(((ENTQBConversationHandler) handler).getConversations());
				if(flag) {
					intent.putExtra("conversationslist", conversationslist);
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

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {		
        handler.removeCallbacks(sendUpdatesToUI);		
		super.onDestroy();
	}	
	
	protected void processNotification(ENTConversation conversation){
		Log.e("",BundleKeys.isFront+" "+ conversation.getId()+" "+((BundleKeys.CURRENT_CONVERSATION!=null)?BundleKeys.CURRENT_CONVERSATION.getId():""));
		if(BundleKeys.isFront && BundleKeys.CURRENT_CONVERSATION!=null && !conversation.getId().equals(BundleKeys.CURRENT_CONVERSATION.getId())){
			showNotification(conversation);
		} else if(!BundleKeys.isFront){
			showNotification(conversation);
		}
   }

	private void showNotification(ENTConversation conversation) {
		notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
   	   	  Intent intent = new Intent(application, JobListActivity.class);
          BundleKeys.fromSecureMessaging = true;
          PendingIntent contentIntent = PendingIntent.getActivity(application, 0, intent, 0);
          
          ENTUser user = getENTUserFromId(conversation.getLastMessageUserId());
          
          NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(application)
          .setAutoCancel(true)
          .setSmallIcon(R.drawable.icon)
          .setContentTitle(Consts.GCM_NOTIFICATION)
          .setContentText("Message from "+ user.getName());
          Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
  		  mBuilder.setSound(alarmSound);
  		  mBuilder.setVibrate(new long[] { 1000, 1000});
          mBuilder.setContentIntent(contentIntent);
          Log.e("isFront", Boolean.toString(BundleKeys.isFront));
          notificationManager.notify(NOTIFICATION_ID, mBuilder.getNotification());
	}
	
	public ENTUser getENTUserFromId(String id){
			for(ENTUser user : BundleKeys.QB_Users){
				if(user.getId().equals(id)){
					return user;
				}
			}
			return null;		
		}
}
