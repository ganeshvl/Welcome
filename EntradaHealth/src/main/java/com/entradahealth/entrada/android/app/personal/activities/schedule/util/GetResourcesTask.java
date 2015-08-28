package com.entradahealth.entrada.android.app.personal.activities.schedule.util;

import java.net.MalformedURLException;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;

/**
 * Async Task to get the resource names form the server.
 */
@SuppressWarnings("rawtypes")
public class GetResourcesTask extends AsyncTask{

	@SuppressWarnings("unused")
	private Activity activity;
	private EntradaApplication application;
	private APIService service;
	private List<Resource> resources;
	private DomainObjectProvider provider; 
	private Account account = null;
	
	 public GetResourcesTask(){
		//Getting environment variables.
		application = (EntradaApplication) EntradaApplication.getAppContext();
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
		try {
			provider= new MainUserDatabaseProvider(false);
		} catch (DomainObjectWriteException e1) {
			e1.printStackTrace();
		}
		try {
			service = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		final UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			account = state.getCurrentAccount();
			provider = state.getProvider(account);
		}
	}

	@Override
	protected Object doInBackground(Object... arg0) {
			try {
				//getting the resource names from the server.
				resources = service.getResourceNames(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID));
				if(resources !=null && resources.size()>0){
				Log.e("", "Resources.size()--"+resources.size());
	            //Clear the previous data before inserting.
				try {
					provider.deleteResources();
				} catch (DomainObjectWriteException e1) {
					e1.printStackTrace();
				}
				//insert in DB
	            for (Resource resource : resources) {
		            try {
						provider.addResources(resource);
					} catch (DomainObjectWriteException e) {
						e.printStackTrace();
					}
				}
				}else{
					Log.e("", "Resources.size()--"+ "== 0");
				}
				
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);

	}
}