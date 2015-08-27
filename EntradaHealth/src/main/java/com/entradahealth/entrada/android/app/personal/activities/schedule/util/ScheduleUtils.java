package com.entradahealth.entrada.android.app.personal.activities.schedule.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.provider.Settings;

/**
 * Util class, holds utility methods used in the application.
 *
 */
public class ScheduleUtils {

	/**
	 * Method to convert the milliseconds to Calendar Date object.
	 * @param milliseconds
	 * @return - Converted Date String
	 */
	public static String convertMilliSecondsToDate(String milliseconds){
		if(milliseconds !=null){
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			long now = Long.parseLong(milliseconds);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(now);
			return formatter.format(calendar.getTime());
		}else{
			return "yyyy-MM-dd";
		}
	}
	
	/**
	 * Method to convert the milliseconds to Calendar Date object.
	 * @param milliseconds
	 * @return - Converted Date String
	 */
	public static String convertMilliSecondsToHeaderDate(String milliseconds){
		if(milliseconds !=null){
			DateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy");
			long now = Long.parseLong(milliseconds);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(now);
			return formatter.format(calendar.getTime());
		}else{
			return "EEEE, MMM dd, yyyy";
		}
	}
	

/**
* This method is used to convert required date string object
* @param dateString original date string
* @return dateFormat required format date string
*/
	public static String getDateFormattedString(String dateString) {
		SimpleDateFormat mTargetFormat = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
		SimpleDateFormat mOriginalFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

		String reqstring = null;
		try {
			reqstring = mTargetFormat.format(mOriginalFormat.parse(dateString));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return reqstring;
	}
	

	/**
	 * Converts string date to time
	 * @param date
	 * @return - converted date
	 */
	public static String convertDateToTime(String date){
		if(date!=null){
			String[] time = date.split("T");
			return time[1];
		}else{
			return "Invalid Time";
		}
	}

	/**
	 * If text doesn't end with clause append's clause to text, else append's
	 * delimiter.
	 * 
	 * @param delimeter
	 *            such as 'and'
	 * @param clause
	 *            such as WHERE
	 * @param text
	 * @return the appended Query.
	 */
	public static StringBuffer append(String delimeter, StringBuffer text) {
		// text is not not empty and text doen't end with with clause then
		// append delimeter.
		if (!text.toString().equalsIgnoreCase("")) {
			text.append(delimeter);
		}
		return text;
	}

	/**
	 * Return date in yyyy-MM-dd format
	 * @return date
	 */
	public static String getCurrentDate(){
		try{
			Date date = new Date();
			String DATE_FORMAT = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(date);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return "Invalid date";
	}

	/**
	 * Get device Unique id for unique reference device to device
	 */
	public static String getDeviceUniqueID() {

		return Settings.Secure.getString(com.entradahealth.entrada.android.app.personal.EntradaApplication.getAppContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	/**
	 * Checks the Internet connectivity of the device. R
	 * 
	 * @param activity
	 * @return false if there is no Internet connectivity.
	 */
	public static boolean checkInternetConnection(Activity activity) {

		ConnectivityManager conMgr = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnected())
			return true;
		else
			return false;
	}// checkInternetConnection()

	/**
	 * Shows the alert with the tile and message passed as params.
	 * 
	 * @param context
	 *            The context of this Dialog
	 * @param title
	 *            Tile to be shown for Dialog.
	 * @param MSG
	 *            Message for the Alert.
	 */
	public static void showAlert(Context context, String title, String MSG) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(title);
		alert.setCancelable(false);
		alert.setMessage(MSG);
		alert.setPositiveButton(
				context.getResources().getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog1, int which) {
						dialog1.dismiss();
					}
				});
		alert.show();
	}// showAlert()


	// Save value in SharedPreference
	public static void saveStringInSP(Activity activity, String key,
			String value) {
		SharedPreferences preferences = activity.getSharedPreferences(key,
				android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	// Retrive value from SharedPreference
	public static String getStringFromSP(Activity activity, String key) {
		SharedPreferences preferences = activity.getSharedPreferences(key,
				android.content.Context.MODE_PRIVATE);
		return preferences.getString(key, null);
	}

	// Save value in SharedPreference
	public static void saveBooleanInSP(Activity activity, String key,
			boolean value) {
		SharedPreferences preferences = activity.getSharedPreferences(key,
				android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	// Retrive value from SharedPreference
	public static boolean getbooleanFromSP(Activity activity, String key) {
		SharedPreferences preferences = activity.getSharedPreferences(key,
				android.content.Context.MODE_PRIVATE);
		return preferences.getBoolean(key, false);
	}

}
