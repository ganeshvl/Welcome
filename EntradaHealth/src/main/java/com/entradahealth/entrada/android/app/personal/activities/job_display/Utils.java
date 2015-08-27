package com.entradahealth.entrada.android.app.personal.activities.job_display;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	public static JSONObject getJSObj(String value){
		try {
			JSONObject jsObj=new JSONObject(value);
			return jsObj;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}
