package com.entradahealth.entrada.core.inbox.domain.providers;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.TOU;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.google.common.collect.ImmutableList;

public class SMMemoryStoreProvider implements SMDomainObjectReader,
		SMDomainObjectWriter {

	@Override
	public void addBuddy(ENTUser buddy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addConversation(ENTConversation conversation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMessageToConversation(ENTMessage message) {
		// TODO Auto-generated method stub
		
	}

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
	public void deleteConversation(ENTConversation conversation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBuddy(ENTUser buddy) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateConversation(ENTConversation conversation)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMessageOfConversation(ENTMessage message)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conversationInsertUpdate(ENTConversation conversation)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buddyInsertUpdate(ENTUser user)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageInsertUpdate(ENTMessage message)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
	public void updateLastMessageInConversationTable(ENTMessage message) {
		// TODO Auto-generated method stub
		
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
	public void pendingInviteInsertUpdate(ENTUser user)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPendingInvite(ENTUser buddy)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ImmutableList<ENTUser> getPendingInvites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deletePendingInvites() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addConversation(ENTConversation conversation,
			boolean fetchMessage) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateConversation(ENTConversation conversation,
			boolean fetchMessage) throws DomainObjectWriteException, Exception {
		// TODO Auto-generated method stub
		
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
	public void markConversationMessagesAsRead(String conversationId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePatient(Patient patient) throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePatients(Iterable<Patient> patients)
			throws DomainObjectWriteException {
		// TODO Auto-generated method stub
		
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
