package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.provider.Settings.Secure;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;

public class AuthenticateTask extends AsyncTask{

	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		String m_androidId = Secure.getString(application.getContentResolver(), Secure.ANDROID_ID);
		APIService service;
		try {
			MainUserDatabaseProvider provider = new MainUserDatabaseProvider(false);
			EUser user = provider.getCurrentUser();
			EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
			Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
			service = new APIService(env.getApi());
		 	String s = service.authenticate(user.getName(), user.getPassword(), m_androidId);
			JSONObject json = new JSONObject(s);
		 	application.setStringIntoSharedPrefs(BundleKeys.SESSION_TOKEN, json.getString("SessionToken"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (DomainObjectWriteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		onSucsessfullExecute();
	}
	
	protected void onSucsessfullExecute(){
		
	}
}
