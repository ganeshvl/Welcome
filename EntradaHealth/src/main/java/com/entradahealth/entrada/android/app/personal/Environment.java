package com.entradahealth.entrada.android.app.personal;

public class Environment {
	
	private String api;
	private String qbApiDomain;
	private String qbChatDomain;
	private String qbBucketName;
	private String qbApplicationId;
	private String qbAuthorizationKey;
	private String qbAuthorizationSecret;
	
	public Environment(String api, String qbApiDomain, String qbChatDomain, String qbBucketName, String qbApplicationId,
			String qbAuthorizationKey, String qbAuthorizationSecret) {
		super();
		this.api = api;
		this.qbApiDomain = qbApiDomain;
		this.qbChatDomain = qbChatDomain;
		this.qbBucketName = qbBucketName;
		this.qbApplicationId = qbApplicationId;
		this.qbAuthorizationKey = qbAuthorizationKey;
		this.qbAuthorizationSecret = qbAuthorizationSecret;
	}
	
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
	}
	
	public String getQbApiDomain() {
		return qbApiDomain;
	}

	public void setQbApiDomain(String qbApiDomain) {
		this.qbApiDomain = qbApiDomain;
	}

	public String getQbChatDomain() {
		return qbChatDomain;
	}

	public void setQbChatDomain(String qbChatDomain) {
		this.qbChatDomain = qbChatDomain;
	}

	public String getQbBucketName() {
		return qbBucketName;
	}

	public void setQbBucketName(String qbBucketName) {
		this.qbBucketName = qbBucketName;
	}

	public String getQbApplicationId() {
		return qbApplicationId;
	}
	public void setQbApplicationId(String qbApplicationId) {
		this.qbApplicationId = qbApplicationId;
	}
	public String getQbAuthorizationKey() {
		return qbAuthorizationKey;
	}
	public void setQbAuthorizationKey(String qbAuthorizationKey) {
		this.qbAuthorizationKey = qbAuthorizationKey;
	}
	public String getQbAuthorizationSecret() {
		return qbAuthorizationSecret;
	}
	public void setQbAuthorizationSecret(String qbAuthorizationSecret) {
		this.qbAuthorizationSecret = qbAuthorizationSecret;
	}
	
	

}
