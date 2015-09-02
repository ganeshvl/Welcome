package com.entradahealth.entrada.core.inbox.domain.providers;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.TOU;
import com.google.common.collect.ImmutableList;

public class SMNullReader implements SMDomainObjectReader
{

	@Override
	public ENTUser getBuddyById(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<ENTUser> getBuddies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ENTConversation getConversationById(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<ENTConversation> getConversations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<ENTMessage> getMessagesFromConversation(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ENTMessage getMessageById(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ENTMessage getRecentMessageFromConversation(String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImmutableList<TOU> getTOUVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveTOU(String id, String vno) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTOU(String id, String vno, boolean accepted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImmutableList<ENTUser> getPendingInvites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getUnreadMessagesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getConversationUnreadMessagesCount(String conversationId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Patient getPatient(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getConversationMessagesCount(String conversationId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImmutableList<ENTMessage> getMessagesFromConversation(String ID,
			int offset, int limit) {
		// TODO Auto-generated method stub
		return null;
	}
  
}
