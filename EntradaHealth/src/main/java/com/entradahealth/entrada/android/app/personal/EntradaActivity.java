package com.entradahealth.entrada.android.app.personal;

import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class EntradaActivity extends Activity {

	SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("Entrada",Context.MODE_WORLD_READABLE);
		if(sp.getBoolean("SECURE_MSG", true))
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,LayoutParams.FLAG_SECURE);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
		
		/*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
	        @Override
	        public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
	            Log.e("Entrada-Activity","Uncaught-Exception");
	            System.exit(2);
	        }
	    });*/
	}
}
