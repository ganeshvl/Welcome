package com.entradahealth.entrada.core.inbox.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.NewMessageFragment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Attachment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.inbox.encryption.AES256Cipher;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;

public class GroupChatManagerImpl extends QBMessageListenerImpl<QBGroupChat> implements ChatManager {
    private static final String TAG = "GroupChatManagerImpl";

    private NewMessageFragment chatFragment;

    private QBGroupChatManager groupChatManager;
    private QBGroupChat groupChat;
    private AES256Cipher cipher;
    private Activity activity;
    private ENTConversation dialog;
	public static final String BROADCAST_ACTION = "com.entradahealth.broadupdatelist";

    public GroupChatManagerImpl(Activity activity){
    	this.activity = activity;
		initialize();
    }

	private void initialize() {
		if (!QBChatService.isInitialized()) {
		      QBChatService.init(EntradaApplication.getAppContext());
		}
      groupChatManager = QBChatService.getInstance().getGroupChatManager();
      cipher = new AES256Cipher();
	}
    
    public GroupChatManagerImpl(NewMessageFragment chatFragment) {
        this.chatFragment = chatFragment;
		initialize();
    }

    public void joinGroupChat(ENTConversation dialog, ENTMessage message){
    	joinGroupChat(dialog, null, message);
    }

    public void joinGroupChat(ENTConversation dialog, QBEntityCallback callback){
    	joinGroupChat(dialog, callback, null);
    }
    
    public void joinGroupChat(ENTConversation dialog, QBEntityCallback callback, ENTMessage message){
    	this.dialog = dialog;
    	if(groupChatManager==null){
    		initialize();
    	}
    	Log.e("","dialog--"+ dialog+"--groupChatManager--"+groupChatManager);
        groupChat = groupChatManager.createGroupChat(dialog.getXmpp_room_jid());
        join(groupChat, callback, message);
    }

    public boolean isJoined(){
    	try{
    		return groupChat.isJoined();
    	} catch(Exception e){
    		return true;
    	}
    }
    
    private void join(final QBGroupChat grpChat, final QBEntityCallback callback, final ENTMessage message) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        if(!grpChat.isJoined()) {
        	grpChat.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
            	grpChat.addMessageListener(GroupChatManagerImpl.this);
                if(message !=null) {
                	try {
						sendMessage(message);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } else {
	                if(chatFragment != null) {
		                chatFragment.getActivity().runOnUiThread(new Runnable() {
		                    @Override
		                    public void run() {
		                        callback.onSuccess();
		                    }
		                });
	                } else {
	                	activity.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
		                        callback.onSuccess();
							}
						});
	                }
                }
                Log.w("Chat", "Join successful");
            }

            @Override
            public void onError(final List list) {
            	try{
            	 if(chatFragment != null) {
	                chatFragment.getActivity().runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	try{
		                    	if(Arrays.toString(list.toArray()).contains("NotConnectedException")){
		        	           		initialize();
		        	            	Log.e("","dialog--"+ dialog+"--groupChatManager--"+groupChatManager);
		        	                groupChat = groupChatManager.createGroupChat(dialog.getXmpp_room_jid());
		        	                join(groupChat, callback, message);
		                    	}
	                    	} catch(Exception ex){
	                    		ex.printStackTrace();
	                    	}
	                       // callback.onError(list);
	                    }
	                });
            	 } else {
 	                activity.runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	try {
		                    	if(Arrays.toString(list.toArray()).contains("NotConnectedException")){
		        	           		initialize();
		        	            	Log.e("","dialog--"+ dialog+"--groupChatManager--"+groupChatManager);
		        	                groupChat = groupChatManager.createGroupChat(dialog.getXmpp_room_jid());
		        	                join(groupChat, callback, message);
		                    	}
	                    	} catch(Exception ex){
	                    		ex.printStackTrace();
	                    	}
//	                        callback.onError(list);
	                    }
	                });
            	 }
            	} catch(Exception ex){
            		ex.printStackTrace();
            		
            	}
                Log.w("Could not join chat, errors:", Arrays.toString(list.toArray()));
            }
        });
        }
    }

    @Override
    public void release() throws XMPPException {
        if (groupChat != null) {
            try {
                groupChat.leave();
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
            }
            groupChat.removeMessageListener(this);
        }
    }

    @Override
    public void sendMessage(ENTMessage message) throws NotConnectedException, XMPPException, IllegalStateException{
    	QBChatMessage chatMessage = new QBChatMessage();
		String passPhrase = message.getPassPhrase();		
		String encryptedText = cipher.encryptText(message.getMessage(), passPhrase);
		encryptedText = encryptedText.trim();
    	chatMessage.setBody(encryptedText);
    	if(message.getAttachmentID()!=null){
    		List<QBAttachment> attachments = new ArrayList<QBAttachment>();
    		QBAttachment attachment = null;
			if(message.getContentType() == ENTMessage.IMAGE){
				attachment = new QBAttachment("image");
			} else if(message.getContentType() == ENTMessage.AUDIO) {
				attachment = new QBAttachment("audio");
			}
			attachment.setId(message.getAttachmentID());
			attachments.add(attachment);
    		chatMessage.setAttachments(attachments);
    		chatMessage.setProperty("type", String.valueOf(message.getContentType()));
    	}
    	chatMessage.setDialogId(message.getChatDialogId());
        chatMessage.setProperty("save_to_history", "1");
        if(message.getPatientID()!=0){
        	chatMessage.setProperty("patient_id", String.valueOf(message.getPatientID()));
        }
        chatMessage.setMarkable(true);
		if(message.getCustomString()!=null)
			chatMessage.setProperty("type", message.getCustomString());
        chatMessage.setDateSent(new Date().getTime()/1000);
    	sendMessage(chatMessage);
    }
    
    @Override
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException, IllegalStateException {
        if (groupChat != null) {
            try {
                groupChat.sendMessage(message);
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
                throw nce;
            } catch (IllegalStateException e){
                e.printStackTrace();
                throw e;
            }
        } else {
        	if(chatFragment != null) {
        		Toast.makeText(chatFragment.getActivity(), "Join unsuccessful", Toast.LENGTH_LONG).show();
        	}
        }
    }
    
	@Override
	public void processMessageRead(QBGroupChat sender, String messageID) {
		super.processMessageRead(sender, messageID);
		Log.w(TAG, "message read: " + messageID);
	}

    @Override
    public void processMessageDelivered(QBGroupChat sender, String messageID) {
    	super.processMessageDelivered(sender, messageID);
    	Log.w(TAG, "message delivered: " + messageID);
    }
    
    @Override
    public void processMessage(QBGroupChat groupChat, QBChatMessage chatMessage) {
        // Show message
        Log.w(TAG, "new incoming message: " + chatMessage);
        ENTMessage entMessage = new ENTMessage();
        entMessage.setMessage(chatMessage.getBody());
        entMessage.setSender(String.valueOf(chatMessage.getSenderId()));
        entMessage.setSentDate(new Date().getTime()/1000);
        entMessage.setId(chatMessage.getId());
        entMessage.setChatDialogId(chatMessage.getDialogId());
        entMessage.setAsRead(chatMessage.isRead()? 1: 0);
        List<QBAttachment> attachments = (List<QBAttachment>) chatMessage.getAttachments();
        List<Attachment> _attachments = new ArrayList<Attachment>();
        for(QBAttachment attachment :attachments){
        	entMessage.setAttachmentID(attachment.getId());
        	entMessage.setContentType(entMessage.getTypeFromContent(attachment.getType()));
        	Attachment _attachObj = new Attachment(attachment.getType(), attachment.getId());
        	_attachments.add(_attachObj);
        }
        entMessage.setAttachments(_attachments);
        try{
        	entMessage.setPatientID(Long.valueOf(chatMessage.getProperty("patient_id")));
        } catch(Exception ex){
        }
        try{
        	entMessage.setContentType(Integer.valueOf(chatMessage.getProperty("type")));
        } catch(Exception ex){
        }
        if(chatFragment != null) {
        	try {
        		chatFragment.showMessage(entMessage);
        	} catch(Exception ex){
            	addMessage(entMessage);
        	}
        } else {
        	addMessage(entMessage);

        }
    }

	private void addMessage(ENTMessage entMessage) {
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		UserState state = AndroidState.getInstance().getUserState();
		SMDomainObjectWriter writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		entMessage.setType(entMessage.getContentType());
		try {
			writer.addMessageToConversation(entMessage);
			writer.updateLastMessageInConversationTable(entMessage);
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent refreshIntent = new Intent(BROADCAST_ACTION);
		application.sendBroadcast(refreshIntent);
		try {
			release();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    @Override
    public void processError(QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage){

    }
}
