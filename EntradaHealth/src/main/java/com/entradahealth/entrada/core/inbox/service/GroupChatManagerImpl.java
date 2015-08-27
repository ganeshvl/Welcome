package com.entradahealth.entrada.core.inbox.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import android.util.Log;
import android.widget.Toast;

import com.entradahealth.entrada.android.app.personal.activities.inbox.NewMessageFragment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Attachment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
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

    public GroupChatManagerImpl(NewMessageFragment chatFragment) {
        this.chatFragment = chatFragment;
		if (!QBChatService.isInitialized()) {
		      QBChatService.init(chatFragment.getActivity());
		}
        groupChatManager = QBChatService.getInstance().getGroupChatManager();
        cipher = new AES256Cipher();
    }

    public void joinGroupChat(ENTConversation dialog, QBEntityCallback callback){
        groupChat = groupChatManager.createGroupChat(dialog.getXmpp_room_jid());
        join(groupChat, callback);
    }

    public boolean isJoined(){
    	try{
    		return groupChat.isJoined();
    	} catch(Exception e){
    		return true;
    	}
    }
    
    private void join(final QBGroupChat groupChat, final QBEntityCallback callback) {
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        if(!groupChat.isJoined()) {
        groupChat.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {

                groupChat.addMessageListener(GroupChatManagerImpl.this);

                chatFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();

                        //Toast.makeText(chatFragment.getActivity(), "Join successful", Toast.LENGTH_LONG).show();
                    }
                });
                Log.w("Chat", "Join successful");
            }

            @Override
            public void onError(final List list) {
//                chatFragment.getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        callback.onError(list);
//                    }
//                });


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
    public void sendMessage(ENTMessage message) throws NotConnectedException, XMPPException{
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
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        if (groupChat != null) {
            try {
                groupChat.sendMessage(message);
            } catch (SmackException.NotConnectedException nce){
                nce.printStackTrace();
            } catch (IllegalStateException e){
                e.printStackTrace();

                Toast.makeText(chatFragment.getActivity(), "You are still joining a group chat, please white a bit", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(chatFragment.getActivity(), "Join unsuccessful", Toast.LENGTH_LONG).show();
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
        chatFragment.showMessage(entMessage);
    }

    @Override
    public void processError(QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage){

    }
}
