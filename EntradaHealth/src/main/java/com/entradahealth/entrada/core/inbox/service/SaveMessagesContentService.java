package com.entradahealth.entrada.core.inbox.service;

import java.util.concurrent.ExecutorService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;

public class SaveMessagesContentService extends Service{

	private static final String LOG_NAME = "Entrada-SaveMessagesService";
	public static final String BROADCAST_ACTION = "com.entradahealth.broadupdatelist";
    private static boolean running = false;
    private ExecutorService executor;
    private ENTHandler handler;
    private Intent refreshIntent;
    
	public static boolean isRunning() {
		return running;
	}

    @Override
    public void onCreate() {
        super.onCreate();
        ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
        handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
        executor = EntradaApplication.getMessagesExecutor();
        refreshIntent = new Intent(BROADCAST_ACTION);
        Log.e("", "--OnCreate()-- **"+ executor);
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Runnable thread = new SaveMessagesThread(intent, startId);
		executor.execute(thread);
		return START_REDELIVER_INTENT;
	}
	
	public static void saveConversationMessages(ENTConversation conversation) {
        Intent i = new Intent(EntradaApplication.getAppContext(), SaveMessagesContentService.class);
        i.putExtra("conversation", conversation);
        EntradaApplication.getAppContext().startService(i);
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class SaveMessagesThread implements Runnable{
		
		private Intent intent;
		private int startId;
		
		SaveMessagesThread(Intent intent, int startId){
			this.intent = intent;
			this.startId = startId;
		}
		
		@Override
		public void run() {
			try{
				running = true;
				ENTConversation conversation = (ENTConversation) intent.getSerializableExtra("conversation");
				Log.e(LOG_NAME, Thread.currentThread().getName()+ "--Thread Start-- Loading messages of Conversation id "+ conversation.getId());
				((ENTQBConversationHandler) handler).saveMessages(conversation);
			} finally{
				stopSelf(startId);
				Log.e(LOG_NAME, Thread.currentThread().getName()+ "--Thread End");
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		running = false;
		sendBroadcast(refreshIntent);
//		executor.shutdown();
		Log.e("", "--OnDestroy()--"+executor);
	}
}
