package com.entradahealth.entrada.core.inbox.dao;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;

public interface ENTConversationHandler extends ENTHandler {

	public ENTConversation createDialog(ENTMessage message);
	public ENTConversation updateDialog(ENTConversation conversation);
	public List<ENTConversation> getPrivateDialogs(String cutomString);
	public List<ENTConversation> getPublicDialogs(String customString);
	public List<ENTConversation> getAllDialogs(String customString);
}
