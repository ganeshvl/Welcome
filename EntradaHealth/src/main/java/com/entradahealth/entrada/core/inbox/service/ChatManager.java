package com.entradahealth.entrada.core.inbox.service;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.quickblox.chat.model.QBChatMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

public interface ChatManager {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;

	void sendMessage(ENTMessage message) throws NotConnectedException, XMPPException;
	
	boolean isJoined();
}
