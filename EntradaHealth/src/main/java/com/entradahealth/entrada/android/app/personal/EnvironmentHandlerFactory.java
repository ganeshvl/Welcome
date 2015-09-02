package com.entradahealth.entrada.android.app.personal;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentHandlerFactory {
	
	private Map<String, Environment> handlerMap = new HashMap<String, Environment>();
	private static EnvironmentHandlerFactory factory;
	public static final String PROD = "Production";
	public static final String SALES = "Sales";
	public static final String QA1 = "QA1";
	public static final String QA2 = "QA2";
	public static final String DEV = "Development";
	public static final String CANCEL = "Cancel";
	
	public static final String PROD_SERVER = "dictateapi.entradahealth.net";
	public static final String DEV_SERVER = "moosejaw.entradahealth.net";
	public static final String QA_SERVER = "dictateapi-qa.entradahealth.net";
	public static final String SALES_SERVER = "dictateapi-sales.entradahealth.net";
	public static final String QA2_SERVER = "dictateapi-qa2.entradahealth.net";

	public static final String QB_API_DOMAIN = "api.quickblox.com";
	public static final String QB_PROD_API_DOMAIN = "apientrada.quickblox.com";
	
	public static final String QB_CHAT_DOMAIN = "chat.quickblox.com";
	public static final String QB_PROD_CHAT_DOMAIN = "chatentrada.quickblox.com";
	
	public static final String QB_BUCKET_NAME = "qbprod";
	public static final String QB_PROD_BUCKET_NAME = "atlas-storage-entrada";
	
	public static final String DEV_QB_APPLICATION_ID = "19654";
	public static final String DEV_QB_AUTHORIZATION_KEY = "xWXzWEheDM5YHwO";
	public static final String DEV_QB_AUTHORIZATION_SECRET = "GvZvxbZ5MH6L5Pv";

	public static final String QA1_QB_APPLICATION_ID = "3";
	public static final String QA1_QB_AUTHORIZATION_KEY = "NeE8cRMnvrUGdYC";
	public static final String QA1_QB_AUTHORIZATION_SECRET = "fzGvDKYxeGRRxQ7";

	public static final String QA2_QB_APPLICATION_ID = "12";
	public static final String QA2_QB_AUTHORIZATION_KEY = "M58BkkbL39WOYma";
	public static final String QA2_QB_AUTHORIZATION_SECRET = "V82hKM3Pe4aqRp3";

	public static final String PROD_QB_APPLICATION_ID = "8";
	public static final String PROD_QB_AUTHORIZATION_KEY = "wRLBNGmyLQSDJZj";
	public static final String PROD_QB_AUTHORIZATION_SECRET = "PnZXn5UQScjQt5w";

	public static final String SALES_QB_APPLICATION_ID = "9";
	public static final String SALES_QB_AUTHORIZATION_KEY = "EVvh3fKZaWwG-7-";
	public static final String SALES_QB_AUTHORIZATION_SECRET = "Q9EngCKf-6Kv3E8";
	
	private EnvironmentHandlerFactory() {
		handlerMap.put(PROD, new Environment(PROD_SERVER, QB_PROD_API_DOMAIN, QB_PROD_CHAT_DOMAIN,  QB_PROD_BUCKET_NAME, PROD_QB_APPLICATION_ID, PROD_QB_AUTHORIZATION_KEY, PROD_QB_AUTHORIZATION_SECRET));
		handlerMap.put(DEV, new Environment(DEV_SERVER, QB_API_DOMAIN, QB_CHAT_DOMAIN,  QB_BUCKET_NAME, DEV_QB_APPLICATION_ID, DEV_QB_AUTHORIZATION_KEY, DEV_QB_AUTHORIZATION_SECRET));
		handlerMap.put(QA1, new Environment(QA_SERVER, QB_PROD_API_DOMAIN, QB_PROD_CHAT_DOMAIN, QB_PROD_BUCKET_NAME, QA1_QB_APPLICATION_ID, QA1_QB_AUTHORIZATION_KEY, QA1_QB_AUTHORIZATION_SECRET));
		handlerMap.put(QA2, new Environment(QA2_SERVER, QB_PROD_API_DOMAIN, QB_PROD_CHAT_DOMAIN, QB_PROD_BUCKET_NAME, QA2_QB_APPLICATION_ID, QA2_QB_AUTHORIZATION_KEY, QA2_QB_AUTHORIZATION_SECRET));
		handlerMap.put(SALES, new Environment(SALES_SERVER, QB_PROD_API_DOMAIN, QB_PROD_CHAT_DOMAIN, QB_PROD_BUCKET_NAME, SALES_QB_APPLICATION_ID, SALES_QB_AUTHORIZATION_KEY, SALES_QB_AUTHORIZATION_SECRET));
	}

	public synchronized static EnvironmentHandlerFactory getInstance() {
		if (factory == null) {
			factory = new EnvironmentHandlerFactory();
		}
		return factory;
	}

	public Environment getHandler(String key) {
		return handlerMap.get(key);
	}

}
