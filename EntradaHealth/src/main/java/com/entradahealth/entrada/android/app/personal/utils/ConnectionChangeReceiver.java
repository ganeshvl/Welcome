package com.entradahealth.entrada.android.app.personal.utils;

import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	JobListActivity jlist;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		final android.net.NetworkInfo mobile = conMan
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		final android.net.NetworkInfo wifi = conMan
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    
	    if ( mobile!=null || mobile==null || wifi!=null || wifi==null){
	    	context.sendBroadcast(new Intent("CONNECTIVITY_CHANGED"));
	    }

	}

}
