package com.entradahealth.entrada.android.app.personal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.entradahealth.entrada.android.R;

/**
 * TODO: Document this!
 *
 * @author edwards
 * @since 2/7/13
 */
@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.DIALOG, resToastText = R.string.crash_toast_text, // optional,
																												// displayed
																												// as
																												// soon
																												// as
																												// the
																												// crash
																												// occurs,
																												// before
																												// collecting
																												// data
																												// which
																												// can
																												// take
																												// a
																												// few
																												// seconds
resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_alert, // optional.
																								// default
																								// is
																								// a
																								// warning
																								// sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your
												// application name
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast
													// message when the user
													// accepts to send a report.
        )
public class EntradaApplication extends MultiDexApplication {
    private static Context context;
    private static boolean isNew;
    private static ExecutorService executor;
    private static ExecutorService messagesExecutor;
    private int THREAD_POOL_SIZE = 3;
    private int MESSAGES_THREAD_POOL_SIZE = 10;
	private SharedPreferences preferences;
	private Editor editor;
	private String MY_PREFS_NAME = "Entrada";
	private HashMap<String, String> conversationMap;

    public static Context getAppContext() {
        return EntradaApplication.context;
    }

	public static boolean isNew() {
		return isNew;
	}

	public static void setAppStatus(boolean isNew) {
		EntradaApplication.isNew = isNew;
	}
	
	public static ExecutorService getExecutor(){
		return executor;
	}
	public static ExecutorService getMessagesExecutor(){
		return messagesExecutor;
	}

    @Override
    public void onCreate() {
        super.onCreate();
        EntradaApplication.context = getApplicationContext();
        ACRA.init(this);
        ACRA.getConfig().setFormKey(getString(R.string.hockeyapp_api_key));
        ACRA.getErrorReporter().setReportSender(new HockeySender());
        setAppStatus(true);
        try {
			InputStream inputStream = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc;

			// Check if sdcard/properties.xml exists
			File sdDir = new File(Environment.getExternalStorageDirectory()
					.getPath());
			Log.d("", "sdDir.getPath()-----" + sdDir.getPath());
			if (sdDir.isDirectory()) {
				File readFile = new File(sdDir.getPath()+ "/properties.xml");
				Log.d("", "readFileExists--"+ readFile.exists());
				if (readFile.exists()) {
					inputStream = new FileInputStream(sdDir.getPath()+ "/properties.xml");
				}
			}
			doc = db.parse(inputStream);
			HashMap propertiesMap = new HashMap<String, String>();
			NodeList nodeList = doc.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				String key = nodeList.item(i).getNodeName();
				String value = nodeList.item(i).getChildNodes().item(0)
						.getNodeValue();
				propertiesMap.put(key, value);
				//THREAD_POOL_SIZE = Integer.valueOf(propertiesMap.get("thread_pool_size").toString());
			}
		} catch (Exception e) {
			THREAD_POOL_SIZE = 3;
		}

        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        messagesExecutor = Executors.newFixedThreadPool(MESSAGES_THREAD_POOL_SIZE);
		preferences = getSharedPreferences(MY_PREFS_NAME, Context.MODE_WORLD_READABLE);
		editor = preferences.edit();
		conversationMap = new HashMap<String, String>();

    }
    
	public void setIntIntoSharedPrefs(String key, int value){
		editor.putInt(key, value);
		editor.commit();
	}

	public Integer getIntFromSharedPrefs(String key){
		return preferences.getInt(key, 0);
	}
	
	public void setStringIntoSharedPrefs(String key, String value){
		editor.putString(key, value);
		editor.commit();
	}

	public String getStringFromSharedPrefs(String key){
		return preferences.getString(key, null);
	}
	
	public void setStringSetIntoSharedPrefs(String key, HashSet<String> value){
		editor.putStringSet(key, value);
		editor.commit();
	}
	
	public HashSet<String> getStringSetFromSharedPrefs(String key){
		return (HashSet<String>) preferences.getStringSet(key, null);
	}
	
	
	public void setJobListPermission(boolean permission){
		editor.putBoolean(BundleKeys.JOBLIST_PERMISSION, permission);
		editor.commit();
	}
	
	public boolean isJobListEnabled(){
		return preferences.getBoolean(BundleKeys.JOBLIST_PERMISSION, false);
	}
	
	/**
	 * Method gives Wifi connection state
	 * 
	 * @return true if wifi connected otherwise false
	 */
	public boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());
	}
	
	/**
	 * Method to find if device is in 3G or not
	 * 
	 * @return true if it is in 3G otherwise false
	 */
	public boolean is3GOr4GConnected() {
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo mobile = conMan
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		final android.net.NetworkInfo wifi = conMan
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mobile != null) {
			if ((mobile.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)
					&& (wifi.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED || wifi
							.getDetailedState() == NetworkInfo.DetailedState.IDLE))
				return true;
			else
				return false;
		} else
			return false;
	}
	
	public String getPassPhrase(String convId){
		if(this.conversationMap!=null && this.conversationMap.size()>0)
			return this.conversationMap.get(convId);
		else 
			return null;
	}
	public void addPassPhrase(String convId, String passPhrase){
		this.conversationMap.put(convId, passPhrase);
	}

	@Override protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	
}
