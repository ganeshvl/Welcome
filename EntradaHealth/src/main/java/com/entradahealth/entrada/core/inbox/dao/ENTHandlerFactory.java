package com.entradahealth.entrada.core.inbox.dao;

import java.util.HashMap;
import java.util.Map;

public class ENTHandlerFactory{
	private Map<String, ENTHandler> handlerMap = new HashMap<String, ENTHandler>();
	private static ENTHandlerFactory factory;
	public static final String QBMESSAGE = "QBMessage";
	public static final String QBCONVERSATION = "QBConversation";
	public static final String QBBUDDY = "QBBuddy";
	public static final String QBUSER = "QBUser";

	private ENTHandlerFactory() {
		handlerMap.put(QBMESSAGE, new ENTQBMessageHandler());
		handlerMap.put(QBCONVERSATION, new ENTQBConversationHandler());
		handlerMap.put(QBBUDDY, new ENTQBBuddyHandler());
		handlerMap.put(QBUSER, new ENTQBUserHandler());
	}

	public static ENTHandlerFactory getInstance() {
		if (factory == null) {
			factory = new ENTHandlerFactory();
		}
		return factory;
	}
	
	public static ENTHandlerFactory createInstance(){
		factory = new ENTHandlerFactory();
		return factory;
	}

	public ENTHandler getHandler(String key) {
		return handlerMap.get(key);
	}

}
