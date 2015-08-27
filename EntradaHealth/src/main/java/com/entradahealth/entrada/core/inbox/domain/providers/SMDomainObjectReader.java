package com.entradahealth.entrada.core.inbox.domain.providers;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.domain.TOU;
import com.google.common.collect.ImmutableList;

public interface SMDomainObjectReader {
	
	ENTUser getBuddyById(final String ID);
	List<ENTUser> getBuddies();
	ENTConversation getConversationById(final String ID);
	ImmutableList<ENTConversation> getConversations();
	ImmutableList<ENTMessage> getMessagesFromConversation(final String ID);
	ENTMessage getMessageById(String ID);
	ENTMessage getRecentMessageFromConversation(String ID);
	
	ImmutableList<TOU> getTOUVersion();
	void saveTOU(String id, String vno);
	void updateTOU(String id, String vno, boolean accepted);
	List<ENTUser> getPendingInvites();

}
