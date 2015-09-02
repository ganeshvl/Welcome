package com.entradahealth.entrada.core.inbox.domain.providers;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;

public interface SMDomainObjectWriter {
	
	void addBuddy(ENTUser buddy) throws DomainObjectWriteException;
	void updateBuddy(ENTUser buddy) throws DomainObjectWriteException;
	void addConversation(ENTConversation conversation) throws DomainObjectWriteException;
	void updateConversation(ENTConversation conversation) throws DomainObjectWriteException, Exception;
	void addMessageToConversation(ENTMessage message) throws DomainObjectWriteException;
	void updateMessageOfConversation(ENTMessage message) throws DomainObjectWriteException;
	void deleteConversation(ENTConversation conversation);
	void conversationInsertUpdate(ENTConversation conversation) throws DomainObjectWriteException;
	void buddyInsertUpdate(ENTUser user) throws DomainObjectWriteException;
	void messageInsertUpdate(ENTMessage message) throws DomainObjectWriteException;
	void updateLastMessageInConversationTable(ENTMessage message);
	void pendingInviteInsertUpdate(ENTUser user) throws DomainObjectWriteException;
	void addPendingInvite(ENTUser buddy) throws DomainObjectWriteException;
	void deletePendingInvites();
	void addConversation(ENTConversation conversation, boolean fetchMessage) throws DomainObjectWriteException;
	void updateConversation(ENTConversation conversation, boolean fetchMessage) throws DomainObjectWriteException, Exception;
	void markConversationMessagesAsRead(String conversationId);
	void writePatient(Patient patient) throws DomainObjectWriteException;
	void writePatients(Iterable<Patient> patients) throws DomainObjectWriteException;
	
}
