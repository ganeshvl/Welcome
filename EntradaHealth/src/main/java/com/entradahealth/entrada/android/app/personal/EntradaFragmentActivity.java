package com.entradahealth.entrada.android.app.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class EntradaFragmentActivity extends FragmentActivity{
	
	SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		sp = getSharedPreferences("Entrada",Context.MODE_WORLD_READABLE);
		if(sp.getBoolean("SECURE_MSG", true))
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,LayoutParams.FLAG_SECURE);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

	}
}
