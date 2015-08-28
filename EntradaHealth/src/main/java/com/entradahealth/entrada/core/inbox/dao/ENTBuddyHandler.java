package com.entradahealth.entrada.core.inbox.dao;

import java.util.List;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;

public interface ENTBuddyHandler extends ENTHandler{

	public List<ENTUser> getBuddyList();
	public List<ENTUser> getRecentCommunicatedUsersList(String currentUserID);
}
