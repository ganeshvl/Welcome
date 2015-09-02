package com.entradahealth.entrada.android.app.personal.push.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.Consts;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.service.GetMessagesService;
import com.entradahealth.entrada.core.inbox.service.NewConversationBroadcastService;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

    private static final String TAG = GCMIntentService.class.getSimpleName();

    private NotificationManager notificationManager;
    public static final String BROADCAST_COUNTUPDATE_ACTION = "com.entradahealth.broadcastunreadcount";
    private Intent countIntent;
    private EntradaApplication application;
    
    public GCMIntentService() {
        super(Consts.GCM_INTENT_SERVICE);
        countIntent = new Intent(BROADCAST_COUNTUPDATE_ACTION);
        application = (EntradaApplication) EntradaApplication.getAppContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "new push");

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = googleCloudMessaging.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                processNotification(Consts.GCM_SEND_ERROR, extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                processNotification(Consts.GCM_DELETED_MESSAGE, extras);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                processNotification(Consts.GCM_RECEIVED, extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
	private void processNotification(String type, Bundle extras) {
		if(!NewConversationBroadcastService.isRunning()) {
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			//Intent intent = new Intent(this, UserSelectActivity.class);
			BundleKeys.fromSecureMessaging = true;
			//PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
	
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setAutoCancel(true).setSmallIcon(R.drawable.icon)
					.setContentTitle(Consts.GCM_NOTIFICATION)
					.setContentText(extras.getString("message"));
			Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(alarmSound);
			mBuilder.setVibrate(new long[] { 1000, 1000});
			//mBuilder.setContentIntent(contentIntent);
			if (Build.VERSION.SDK_INT == 19) {
				   getNotificationPendingIntent().cancel();
			}
			mBuilder.setContentIntent(getNotificationPendingIntent());
			mBuilder.setTicker(extras.getString("message"));
			Log.e("isFront", Boolean.toString(BundleKeys.isFront));
			if(application == null) {
				application = (EntradaApplication) EntradaApplication.getAppContext();
			}
			if(application!=null){
				try{
					UserState state = AndroidState.getInstance().getUserState();
					SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
					int count = reader.getUnreadMessagesCount();
					if(!NewConversationBroadcastService.isRunning()) {
						Intent conintent = new Intent(this, GetMessagesService.class);
						startService(conintent);
					}
					application.setIntIntoSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT, (count+Integer.valueOf(extras.getString("badge"))));
				} catch(Exception ex){
					ex.printStackTrace();
				}
			}
			notificationManager.notify(NOTIFICATION_ID, mBuilder.getNotification());
			sendBroadcast(countIntent);
		}
	}
	
	 private PendingIntent getNotificationPendingIntent() {
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, UserSelectActivity.class);
			resultIntent.putExtra("fromSecureMessaging" ,true);
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(UserSelectActivity.class);

			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);

			return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		}
	
}