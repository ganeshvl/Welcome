package com.entradahealth.entrada.core.inbox.domain.providers;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Attachment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.db.H2Utils;
import com.entradahealth.entrada.core.domain.TOU;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DatabaseProvider.DatabaseProviderException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBMessageHandler;
import com.entradahealth.entrada.core.remote.APIService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SMDatabaseProvider implements SMDomainObjectProvider {

	private final Connection _conn;
	private Map<String, Integer> contentTypes = new HashMap<String, Integer>(); 
	private ENTHandlerFactory handlerFactory;
	private ENTHandler handler;
	private APIService service;
	private EntradaApplication application;
	private EnvironmentHandlerFactory envFactory;

	public SMDatabaseProvider(Connection conn) throws DomainObjectWriteException {
		_conn = conn;
		handlerFactory = ENTHandlerFactory.getInstance();
		handler = handlerFactory.getHandler(ENTHandlerFactory.QBMESSAGE);
		application = (EntradaApplication) EntradaApplication.getAppContext();
		try {
			envFactory = EnvironmentHandlerFactory.getInstance();
			Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
			service = new APIService(env.getApi());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		contentTypes.put("text", 0);
		contentTypes.put("alert", 1);
		contentTypes.put("image", 2);
		contentTypes.put("audio", 3);
		SMSchemaManager schema = new SMSchemaManager(_conn);
		try {
			schema.updateSchema();
		} catch (Exception e) {
			throw new DomainObjectWriteException(e);
		}
	}

	public Connection getRawConnection() {
		return _conn;
	}

	private boolean _isClosed = false;

	public void close() {
		// H2Utils.close(_conn);
		_isClosed = true;
	}

	public boolean isClosed() {
		return _isClosed;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (!_isClosed)
				close();
		} catch (Exception e) {
			super.finalize();
			throw e;
		}
	}

	private static final String SQL_GET_BUDDIES = "SELECT * FROM Buddies;";
	private static final String SQL_GET_BUDDY_BY_ID = "SELECT * FROM Buddies WHERE ID = ?;";
	private static final String SQL_GET_MESSAGE_BY_ID = "SELECT * FROM Messages WHERE ID = ?;";
	private static final String SQL_GET_RECENT_MESSGAGE_BY_CONVERSATION_ID = "SELECT TOP 1 * FROM MESSAGES WHERE ConversationID = ? ORDER BY SentDateTime DESC;";
	private static final String SQL_GET_CONVERSATIONS = "SELECT * FROM Conversations ORDER BY LastUpdated DESC;";
	private static final String SQL_GET_CONVERSATION_BY_ID = "SELECT * FROM Conversations WHERE ID = ?;";
	private static final String SQL_DEL_CONVERSATION_BY_ID = "DELETE FROM Conversations WHERE ID = ?;";
	private static final String SQL_GET_MESSAGES_BY_CONVERSATION_ID = "SELECT * FROM Messages WHERE ConversationID = ? ORDER BY SentDateTime ASC;";
	private static final String SQL_DEL_MESSAGES_BY_CONVERSATION_ID = "DELETE FROM Messages WHERE ConversationID = ?;";
	private static final String SQL_INSERT_BUDDY = "INSERT INTO Buddies VALUES (?, ?, ?, ?, ?, ?);";
	private static final String SQL_INSERT_MESSAGE = "INSERT INTO Messages VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String SQL_INSERT_CONVERSATION = "INSERT INTO Conversations VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String SQL_UPDATE_BUDDY = "UPDATE Buddies SET Username = ?, IsFavorite = ?, FirstName = ?, MI = ?, LastName = ? WHERE ID = ?;";
	private static final String SQL_UPDATE_MESSAGE = "UPDATE Messages SET Text = ?, Type = ?, PatientID = ?, AuthorID = ?, ConversationID = ?, AttachmentID = ?, SentDateTime = ?, IsOutgoing = ?, IsRead = ?, IsDelivered = ? WHERE ID = ?;";
	private static final String SQL_UPDATE_CONVERSATION = "UPDATE Conversations SET OwnerID = ?, LastUpdated = ?, LastMessage = ?, UnreadMessagesCount = ? WHERE ID = ?;";
	private static final String SQL_INSERT_PENDING_INVITE = "INSERT INTO PendingInvites VALUES (?, ?, ?, ?, ?);";
	private static final String SQL_GET_PENDING_INVITES = "SELECT * FROM PendingInvites;";
	private static final String SQL_DELETE_PENDING_INVITES = "DELETE FROM PendingInvites;";
	
	@Override
	public ENTUser getBuddyById(String ID) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_BUDDY_BY_ID);
			if (ID != null)
				stmt.setString(1, ID);
			rs = stmt.executeQuery();
			List<ENTUser> users = createUsers(rs);
			return users.get(0);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	@Override
	public ENTMessage getMessageById(String ID) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_MESSAGE_BY_ID);
			if (ID != null)
				stmt.setString(1, ID);
			rs = stmt.executeQuery();
			List<ENTMessage> messages = createMessages(rs);
			return messages.get(0);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	@Override
	public List<ENTUser> getBuddies() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_BUDDIES);
			rs = stmt.executeQuery();
			List<ENTUser> users = createUsers(rs);
			return users;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	@Override
	public List<ENTUser> getPendingInvites() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_PENDING_INVITES);
			rs = stmt.executeQuery();
			List<ENTUser> users = createUsers(rs, true);
			return users;
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	
	private List<ENTUser> createUsers(ResultSet rs) throws SQLException {
		return createUsers(rs, false);
	}
	
	private List<ENTUser> createUsers(ResultSet rs, boolean isPendingInvite) throws SQLException {
		List<ENTUser> users = new ArrayList<ENTUser>();
		while (rs.next()) {
			 ENTUser user = new ENTUser();
			 if(!isPendingInvite){
				 user.setId(rs.getString("ID"));
				 user.setLogin(rs.getString("Username"));
			 }
			 user.setFavorite(rs.getBoolean("IsFavorite"));
			 String fname = rs.getString("FirstName"); 
			 String lname = rs.getString("LastName");
			 String name = ((fname == null)? "" : fname )+ " "+ ((lname == null)? "" : lname);
			if(isPendingInvite){
				 name = name + " (Pending)";
			}
			user.setName(name);
			user.setMI(rs.getString("MI"));
			users.add(user);
		}
		return users;
	}

	@Override
	public ENTConversation getConversationById(String ID) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_CONVERSATION_BY_ID);
			if (ID != null)
				stmt.setString(1, ID);
			rs = stmt.executeQuery();
			List<ENTConversation> conversations = createConversations(rs);
			return conversations.get(0);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	@Override
	public void deletePendingInvites(){
		PreparedStatement stmt = null;
		
			try {
				stmt = _conn.prepareStatement(SQL_DELETE_PENDING_INVITES);
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
				H2Utils.close(stmt);
			}
						
	}


	@Override
	public ImmutableList<ENTConversation> getConversations() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_CONVERSATIONS);
			rs = stmt.executeQuery();
			List<ENTConversation> conversations = createConversations(rs);
			return ImmutableList.copyOf(conversations);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}

	private List<ENTConversation> createConversations(ResultSet rs)	throws SQLException {
		List<ENTConversation> conversations = new ArrayList<ENTConversation>();
		while (rs.next()) {
			 ENTConversation conversation = new ENTConversation();
			 conversation.setId(rs.getString("ID"));
			 conversation.setPatientID(rs.getLong("PatientID"));
			 conversation.setUserId(String.valueOf(rs.getLong("OwnerID")));
			 conversation.setLastMessageDateSent(Long.valueOf(rs.getString("LastUpdated")));
			 conversation.setLastMessage(rs.getString("LastMessage"));
			 conversation.setXmpp_room_jid(rs.getString("Roomjid"));
			 String str = rs.getString("RecipientIDs");
			 String[] recipients = str.substring(1, str.length()-1).split(",");
			 conversation.setOccupantsIds(recipients);
			 conversation.setUnreadMessagesCount(rs.getInt("UnreadMessagesCount"));
			 conversations.add(conversation);
		}
		return conversations;
	}
	
	@Override
	public ImmutableList<ENTMessage> getMessagesFromConversation(String ID) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_MESSAGES_BY_CONVERSATION_ID);
			if (ID != null)
				stmt.setString(1, ID);
			rs = stmt.executeQuery();
			List<ENTMessage> messages = createMessages(rs);
			return ImmutableList.copyOf(messages);
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	@Override
	public ENTMessage getRecentMessageFromConversation(String ID) {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				stmt = _conn.prepareStatement(SQL_GET_RECENT_MESSGAGE_BY_CONVERSATION_ID);
				if (ID != null)
					stmt.setString(1, ID);
				rs = stmt.executeQuery();
				List<ENTMessage> messages = createMessages(rs);
				if(messages!=null && messages.size()>0) {
					return ImmutableList.copyOf(messages).get(0);
				} else {
					return null;
				}
			} catch (Exception e) {
				throw new DatabaseProviderException(e);
			} finally {
				 H2Utils.close(rs);
				 H2Utils.close(stmt);
			}
	}
	
	private List<ENTMessage> createMessages(ResultSet rs)	throws SQLException {
		List<ENTMessage> messages = new ArrayList<ENTMessage>();
		while (rs.next()) {
			 ENTMessage message = new ENTMessage();
			 message.setId(rs.getString("ID"));
			 message.setMessage(rs.getString("Text"));
			 message.setContentType(Integer.valueOf(rs.getString("Type")));
			 message.setPatientID(rs.getLong("PatientID"));
			 message.setSender(String.valueOf(rs.getLong("AuthorID")));
			 message.setSentDate(Long.valueOf(rs.getString("SentDateTime")));
			 message.setAttachmentID(rs.getString("AttachmentID"));
			 boolean read = rs.getBoolean("IsRead");
			 message.setAsRead((read == true)? 1 : 0);
			 message.setOutgoing(rs.getBoolean("IsOutgoing"));
			 message.setDelivered(rs.getBoolean("IsDelivered"));
			 message.setChatDialogId(rs.getString("ConversationID"));
			 messages.add(message);
		}
		return messages;
	}

	@Override
	public void pendingInviteInsertUpdate(ENTUser user) throws DomainObjectWriteException {
		addPendingInvite(user);
	}
	
	@Override
	public void buddyInsertUpdate(ENTUser user) throws DomainObjectWriteException{
		try{
			ENTUser _user = getBuddyById(user.getId());
			if(_user.getId().equals(user.getId())){
				updateBuddy(user);
			}
		} catch(DatabaseProviderException ex){
			addBuddy(user);
		}
	}
	
	@Override
	public void conversationInsertUpdate(ENTConversation conversation) throws DomainObjectWriteException{
		try{
			ENTConversation _conversation = getConversationById(conversation.getId());
			conversation.setPatientID(_conversation.getPatientID());
			if(_conversation.getId().equals(conversation.getId())){
				conversation.setLastMessage(_conversation.getLastMessage());
				updateConversation(conversation, false);
			}
		} catch(DatabaseProviderException ex){
			addConversation(conversation, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void messageInsertUpdate(ENTMessage message)
			throws DomainObjectWriteException {
		try{
			ENTMessage _message = getMessageById(message.getId());
			if(_message.getId().equals(message.getId())){
				updateMessageOfConversation(_message);
			}
		} catch(DatabaseProviderException ex){
			addMessageToConversation(message);
		}
	}
	
	@Override
	public void addBuddy(ENTUser buddy) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_INSERT_BUDDY);

			stmt.setString(1, buddy.getId());
			stmt.setString(2, buddy.getLogin());
			stmt.setBoolean(3, buddy.isFavorite());
			stmt.setString(4, buddy.getName() != null ? buddy.getName() :buddy.getFirstName());
			stmt.setString(5, buddy.getMI());
			stmt.setString(6, buddy.getLastName());
			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}
	}
	
	@Override
	public void addPendingInvite(ENTUser buddy) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_INSERT_PENDING_INVITE);

			stmt.setString(1, buddy.getLogin());
			stmt.setBoolean(2, buddy.isFavorite());
			stmt.setString(3, buddy.getFirstName());
			stmt.setString(4, buddy.getMI());
			stmt.setString(5, buddy.getLastName());
			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void addConversation(ENTConversation conversation) throws DomainObjectWriteException {
		addConversation(conversation, true);
	}

	@Override
	public void addConversation(ENTConversation conversation, boolean fetchMessage) throws DomainObjectWriteException {
			PreparedStatement stmt = null;
			try {
				stmt = _conn.prepareStatement(SQL_INSERT_CONVERSATION);
				stmt.setString(1, conversation.getId());
				stmt.setLong(2, conversation.getPatientID());
				stmt.setLong(3, Long.valueOf(conversation.getUserId()));
				stmt.setString(4, String.valueOf(conversation.getLastMessageDateSent()));
				if(fetchMessage){
					conversation = decrypt(conversation);
				}
				stmt.setString(5, "No messages");
				StringBuffer strBuf = new StringBuffer();
				for (int i = 0; i < conversation.getOccupantsIds().length; i++) {
					strBuf.append(conversation.getOccupantsIds()[i]);
					strBuf.append(",");
				}
				String str = strBuf.toString();
				stmt.setString(6, "["+str.substring(0, str.length()-1)+"]");
				stmt.setLong(7, conversation.getUnreadMessagesCount());
				stmt.setString(8, conversation.getXmpp_room_jid());
				int rows = stmt.executeUpdate();
				if (rows != 1)
					throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
			} catch (Exception ex) {
				throw new DomainObjectWriteException(ex);
			} finally {
				 H2Utils.close(stmt);
			}		
	}
	@Override
	public void addMessageToConversation(ENTMessage message) throws DomainObjectWriteException {
			PreparedStatement stmt = null;
			try {
				stmt = _conn.prepareStatement(SQL_INSERT_MESSAGE);
				stmt.setString(1, message.getId());
				stmt.setString(2, message.getMessage());
				int type = 0;
				String attachmentID = "0";
				if(message.getType()!=1) {
					try{
						if(message.getAttachments()!=null && message.getAttachments().size()>0) {
							type = contentTypes.get(message.getAttachments().get(0).getType());
							attachmentID =  message.getAttachments().get(0).getId();
						}
					} catch(Exception ex){
						type = 0;
						attachmentID="0";
					}
				} else {
					type = 1;
				}
				stmt.setLong(3, Long.valueOf(type));
				stmt.setLong(4, message.getPatientID());
				stmt.setLong(5, Long.valueOf(message.getSender()));
				stmt.setString(6, message.getChatDialogId());
					
				stmt.setString(7, attachmentID);
				stmt.setString(8, String.valueOf(message.getSentDate()));
				stmt.setBoolean(9, message.isOutgoing());
				stmt.setBoolean(10, message.isRead());
				stmt.setBoolean(11, message.isDelivered());
				int rows = stmt.executeUpdate();
				//if (rows != 1)
				//	throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
				if(message.getPatientID()!=0) {
					updatePatientIdInCoversation(message);
				}
			} catch (Exception ex) {
				throw new DomainObjectWriteException(ex);
			} finally {
				 H2Utils.close(stmt);
			}
	}

	public void updatePatientIdInCoversation(ENTMessage message) throws DomainObjectWriteException{
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement("UPDATE Conversations SET PatientID = ? WHERE ID = ?;");
			
			stmt.setLong(1, message.getPatientID());
			stmt.setString(2, message.getChatDialogId());
			int rows = stmt.executeUpdate();
//			if (rows != 1)
//				throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}		
	}
	
	@Override
	public void deleteConversation(ENTConversation conversation) {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DEL_CONVERSATION_BY_ID);
			stmt.setString(1, conversation.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		deleteMessages(conversation);
	}
	
	protected void deleteMessages(ENTConversation conversation) {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_DEL_MESSAGES_BY_CONVERSATION_ID);
			stmt.setString(1, conversation.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateBuddy(ENTUser buddy) throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_BUDDY);

			stmt.setString(1, buddy.getLogin());
			stmt.setBoolean(2, buddy.isFavorite());
			stmt.setString(3, buddy.getFirstName());
			stmt.setString(4, buddy.getMI());
			stmt.setString(5, buddy.getLastName());
			stmt.setString(6, buddy.getId());
			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	@Override
	public void updateConversation(ENTConversation conversation)
			throws Exception {
		updateConversation(conversation, true);
	}

	@Override
	public void updateConversation(ENTConversation conversation, boolean fetchMessage)
			throws Exception {
		//if(conversation.getUnreadMessagesCount()>0) {
			PreparedStatement stmt = null;
			try {
				stmt = _conn.prepareStatement(SQL_UPDATE_CONVERSATION);
				
				stmt.setLong(1, Long.valueOf(conversation.getUserId()));
				stmt.setString(2, String.valueOf(conversation.getLastMessageDateSent()));
				if(fetchMessage){
					conversation = decrypt(conversation);
				}
				stmt.setString(3, conversation.getLastMessage());
				stmt.setLong(4, conversation.getUnreadMessagesCount());
				stmt.setString(5, conversation.getId());
				int rows = stmt.executeUpdate();
			} catch (Exception ex) {
				throw ex;
			} finally {
				 H2Utils.close(stmt);
			}
			//}		
	}

	protected ENTConversation decrypt(ENTConversation conversation){
		conversation.setCustomString("skip=0&limit=1&sort_desc=date_sent");
		String passPhrase = null;
		if(application.getPassPhrase(conversation.getId()) != null){
			passPhrase = application.getPassPhrase(conversation.getId());
		} else {
			passPhrase = service.createThread(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId());
			application.addPassPhrase(conversation.getId(), passPhrase);
		}
		conversation.setPassPhrase(passPhrase);
		List<ENTMessage> _messagesList = ((ENTQBMessageHandler) handler).getMessagesFromDialog(conversation);
		ENTMessage message = _messagesList.get(_messagesList.size()-1);
		if(message.getPatientID()!=0) {
			try {
				updatePatientIdInCoversation(message);
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(message!=null && message.getAttachments()!=null && message.getAttachments().size()>0){
			Attachment attachment = message.getAttachments().get(0);
			switch(contentTypes.get(attachment.getType())){
			case 2:
				conversation.setLastMessage("Image");
				break;
			case 3:
				conversation.setLastMessage("Audio");
				break;
			}
		} else {
			conversation.setLastMessage(message.getMessage());
		}
		return conversation;
	}
	
	@Override
	public void updateMessageOfConversation(ENTMessage message)
			throws DomainObjectWriteException {
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_MESSAGE);
			
			stmt.setString(1, message.getMessage());
			int type = 0;
			String attachmentID = "0";
			try{
				if(message.getAttachments()!=null && message.getAttachments().size()>0) {
					type = contentTypes.get(message.getAttachments().get(0).getType());
					attachmentID =  message.getAttachments().get(0).getId();
				}
			} catch(Exception ex){
				type = 0;
				attachmentID="0";
			}
			stmt.setLong(2, type);
			stmt.setLong(3, message.getPatientID());
			stmt.setLong(4, Long.valueOf(message.getSender()));
			stmt.setString(5, message.getChatDialogId());
			stmt.setString(6, attachmentID);
			stmt.setString(7, String.valueOf(message.getSentDate()));
			stmt.setBoolean(8, message.isOutgoing());
			stmt.setBoolean(9, message.isRead());
			stmt.setBoolean(10, message.isDelivered());
			stmt.setString(11, message.getId());
			int rows = stmt.executeUpdate();
			if (rows != 1)
				throw new DomainObjectWriteException("update updated " + rows + ", expected 1.");
		} catch (Exception ex) {
			throw new DomainObjectWriteException(ex);
		} finally {
			 H2Utils.close(stmt);
		}					
	}

	@Override
	public void updateLastMessageInConversationTable(ENTMessage message){
			PreparedStatement stmt = null;
			try {
				stmt = _conn.prepareStatement("UPDATE Conversations SET LastUpdated = ?, LastMessage = ?, UnreadMessagesCount = ? WHERE ID = ?;");
				
				stmt.setString(1, String.valueOf(message.getSentDate()));
				if(message!=null && message.getAttachments()!=null && message.getAttachments().size()>0){
					Attachment attachment = message.getAttachments().get(0);
					switch(contentTypes.get(attachment.getType())){
					case 2:
						message.setMessage("Image");
						break;
					case 3:
						message.setMessage("Audio");
						break;
					}
				} 
				stmt.setString(2, message.getMessage());
				stmt.setLong(3, 0);
				stmt.setString(4, message.getChatDialogId());
				int rows = stmt.executeUpdate();
			} catch (Exception ex) {
			} finally {
				 H2Utils.close(stmt);
			}		
	}

	private static final String SQL_GET_TOUVERSION = "SELECT * FROM tou;";
	@Override
	public ImmutableList<TOU> getTOUVersion() {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = _conn.prepareStatement(SQL_GET_TOUVERSION);
			rs = stmt.executeQuery();
			return createTOU(rs);
			
		} catch (Exception e) {
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(rs);
			 H2Utils.close(stmt);
		}
	}
	
	private ImmutableList<TOU> createTOU(ResultSet rs)
			throws SQLException {

		List<TOU> tou = Lists.newArrayList();

		while (rs.next()) {
			
			tou.add(new TOU(rs.getString("UserId"),
					rs.getString("TOUVersionNumber"),
					rs.getBoolean("TOUAccepted")));
		}

		return ImmutableList.copyOf(tou);
	}

	private static final String SQL_SAVE_TOU = "INSERT INTO tou (UserID, TOUVersionNumber, TOUAccepted) VALUES (?, ?, ?);";
	@Override
	public void saveTOU(String id, String vno) {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_SAVE_TOU);
			stmt.setString(1, id);
			stmt.setString(2, vno);
			stmt.setBoolean(3, false);
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		}catch (Exception e) {
			Log.e("exc", e.getMessage());
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

	private static final String SQL_UPDATE_TOU = "UPDATE tou SET TOUVersionNumber=?, TOUAccepted=? WHERE UserId=?;";
	@Override
	public void updateTOU(String id, String vno, boolean accepted) {
		// TODO Auto-generated method stub
		PreparedStatement stmt = null;
		try {
			stmt = _conn.prepareStatement(SQL_UPDATE_TOU);
			stmt.setString(1, vno);
			stmt.setBoolean(2, accepted);
			stmt.setString(3, id);
			int result = stmt.executeUpdate();
			if (result != 1)
				throw new DomainObjectWriteException(String.format(
						"Write failed, %d rows affected.", result));
		}catch (Exception e) {
			Log.e("exc", e.getMessage());
			throw new DatabaseProviderException(e);
		} finally {
			 H2Utils.close(stmt);
		}
	}

}
