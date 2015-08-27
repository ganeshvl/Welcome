package com.entradahealth.entrada.core.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Accounts define the specifics of connecting to an Entrada
 * clinic server. They map to "Dictators" on the server side.
 *
 * @author edr
 * @since 12 Sep 2012
 */
public class Account implements Comparable<Account> {
	private final String name;
	private String displayName;
	private String remoteUsername;
	private String remotePassword;
	private String apiHost;
	private String clinicCode;
	private String smUsername;
    private String smPassword;    

	private Map<String, String> settings;

	@JsonCreator
	public Account(@JsonProperty("name") String name,
			@JsonProperty("display_name") String displayName,
			@JsonProperty("remote_username") String remoteUsername,
			@JsonProperty("remote_password") String remotePassword,
			@JsonProperty("api_host") String apiHost,
			@JsonProperty("clinic_code") String clinicCode,
			@JsonProperty("sm_username") String smUsername,
			@JsonProperty("sm_password") String smPassword,
			@JsonProperty("settings") Map<?, ?> settingsMap) {
		this.name = name;
		this.displayName = displayName;
		this.remoteUsername = remoteUsername;
		this.remotePassword = remotePassword;
		this.apiHost = apiHost;
		this.clinicCode = clinicCode;
		this.smUsername = smUsername;
		this.smPassword = smPassword;

		settings = new HashMap<String, String>();
		if (settingsMap != null) {
			for (Map.Entry<?, ?> entry : settingsMap.entrySet()) {
				settings.put(entry.getKey().toString(), entry.getValue()
						.toString());
			}
		}
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("display_name")
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@JsonProperty("remote_username")
	public String getRemoteUsername() {
		return remoteUsername;
	}

	public void setRemoteUsername(String remoteUsername) {
		this.remoteUsername = remoteUsername;
	}

	@JsonProperty("remote_password")
	public String getRemotePassword() {
		return remotePassword;
	}

	@JsonProperty("settings")
	protected Map<String, String> getSettings() {
		return settings;
	}

	public void setRemotePassword(String remotePassword) {
		this.remotePassword = remotePassword;
	}

	@JsonProperty("api_host")
	public String getApiHost() {
		return apiHost;
	}

	public void setApiHost(String apiHost) {
		this.apiHost = apiHost;
	}

	@JsonProperty("clinic_code")
	public String getClinicCode() {
		return clinicCode;
	}

	public void setClinicCode(String clinicCode) {
		this.clinicCode = clinicCode;
	}
	
	@JsonProperty("sm_username")
	public String getsmUsername() {
		return smUsername;
	}

	public void setsmUsername(String smUsername) {
		this.smUsername = smUsername;
	}
	
	@JsonProperty("sm_password")
	public String getsmPassword() {
		return smPassword;
	}

	public void setsmPassword(String smPassword) {
		this.smPassword = smPassword;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Account account = (Account) o;

		if (apiHost != null ? !apiHost.equals(account.apiHost)
				: account.apiHost != null)
			return false;
		if (displayName != null ? !displayName.equals(account.displayName)
				: account.displayName != null)
			return false;
		if (name != null ? !name.equals(account.name) : account.name != null)
			return false;
		if (remotePassword != null ? !remotePassword
				.equals(account.remotePassword)
				: account.remotePassword != null)
			return false;
		if (remoteUsername != null ? !remoteUsername
				.equals(account.remoteUsername)
				: account.remoteUsername != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return remoteUsername + "@"
				+ apiHost.replace("http://", "").replace("https://", "");
	}

	public String getSetting(String key) {
		return settings.get(key.toLowerCase());
	}

	public String putSetting(String key, String value) {
		return settings.put(key.toLowerCase(), value);
	}

	@Override
	public int compareTo(Account account) {
		return displayName.compareTo(account.displayName);
	}

	public static final String PROD_SERVER = "dictateapi.entradahealth.net";
	public static final String DEV_SERVER = "moosejaw.entradahealth.net";
	public static final String QA_SERVER = "dictateapi-qa.entradahealth.net";
	public static final String SALES_SERVER = "dictateapi-sales.entradahealth.net";
	public static final String QA2_SERVER = "dictateapi-qa2.entradahealth.net";

	public static String getApiHostFromClinicCode(final String code) {
		// return code.startsWith("22") ? DEV_SERVER : PROD_SERVER;
		if (code.startsWith("22"))
			return DEV_SERVER;
		else if (code.startsWith("23"))
			return QA_SERVER;
		else if (code.startsWith("24"))
			return SALES_SERVER;
		else if (code.startsWith("25"))
			return QA2_SERVER;
    	else
    		return PROD_SERVER;

	}
}
