package com.entradahealth.entrada.core.inbox.dao;

import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTSession;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;

public interface ENTUserHandler extends ENTHandler{

	public ENTSession createSession(ENTUser user);
	public ENTUser login(ENTUser user);
	public ENTUser getUser(String userId);
	public void logout();
}
