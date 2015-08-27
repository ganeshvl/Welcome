package com.entradahealth.entrada.android.app.personal.activities.add_account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;

public class Setup extends EntradaActivity {

	RelativeLayout rlHaveCCode, rlNotHaveCCode, rlDemoAcc;
	String uname;
	boolean from_settings = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setup);
		uname = getIntent().getStringExtra("user_name");
		from_settings = getIntent().getBooleanExtra("from_settings", false);
		
		rlHaveCCode = (RelativeLayout)findViewById(R.id.rlHaveCCode);
		rlHaveCCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Setup.this, AddAccountActivity.class);
				intent.putExtra("user_name", uname);
				intent.putExtra("isFromEdit", from_settings);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(from_settings){
			startActivity(new Intent(Setup.this, EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			finish();
		}else
			finish();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e("onPause", "called");
		finish();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.e("onStop", "called");
		//this.finish();
	}
	
}
