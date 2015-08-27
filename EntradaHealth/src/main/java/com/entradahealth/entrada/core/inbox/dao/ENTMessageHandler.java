package com.entradahealth.entrada.core.inbox.dao;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;

public interface ENTMessageHandler extends ENTHandler {

	public ENTMessage sendMessage(ENTMessage message);
	public List<ENTMessage> getMessagesFromDialog(ENTConversation conversation);

}
