package com.entradahealth.entrada.android.app.personal.activities.add_account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_user.CreateUserTask;

public class ChoosePin extends EntradaActivity {

	SharedPreferences sp;
	Editor edit;
	RelativeLayout rlPin_01, rlPin_02;
	EditText etPinEdit, etConfPin;
    Button btnLogin;
    ImageView ivOK, ivcOK;
    Intent i;
    Bundle b;
    AlertDialog dialog;
    String pin_01, pin_02;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	    	
    	sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    	
    	setContentView(R.layout.pin_entry);
    	
    	rlPin_01 = (RelativeLayout)findViewById(R.id.rlPin_01);
    	rlPin_02 = (RelativeLayout)findViewById(R.id.rlPin_02);
    	
    	etPinEdit = (EditText)findViewById(R.id.xetpin);
    	etPinEdit.setHint("Choose PIN");
    	etConfPin = (EditText)findViewById(R.id.xetconfpin);
    	
    	ivOK = (ImageView)findViewById(R.id.ivButton);
    	ivOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pin_01 = etPinEdit.getText().toString();
				rlPin_01.setVisibility(View.GONE);
				rlPin_02.setVisibility(View.VISIBLE);
				etConfPin.setHint("Confirm PIN");
				etConfPin.requestFocus();
			}
		});
    	
    	etPinEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length()>=4 && s.length()<=10){
					ivOK.setVisibility(View.VISIBLE);
				}else{
					ivOK.setVisibility(View.GONE);
				}
			}
		});
    	
    	ivcOK = (ImageView)findViewById(R.id.ivButton1);
    	ivcOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					pin_02 = etConfPin.getText().toString();
					checkPin();
			}
		});
    	
    	etConfPin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length()>=4 && s.length()<=10){
					ivcOK.setVisibility(View.VISIBLE);
				}else{
					ivcOK.setVisibility(View.GONE);
				}
			}
		});
		    	
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	rlPin_01.setVisibility(View.VISIBLE);
    	rlPin_02.setVisibility(View.GONE);
    	
    	etPinEdit.requestFocus();
    	etPinEdit.setText(null);
    	etConfPin.setText(null);
    	
        etPinEdit.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (textView == etPinEdit)
                {
                	if(etPinEdit.getText().length() < 4 || etPinEdit.getText().length() > 10){
                		dgPin(2);//Pin length error
                	}else{
                		pin_01 = etPinEdit.getText().toString();
    					rlPin_01.setVisibility(View.GONE);
    					rlPin_02.setVisibility(View.VISIBLE);
    					etConfPin.setHint("Confirm PIN");
    					etConfPin.requestFocus();
                	}
                	
					return true;
                }
                return false;
            }
        });
        
        etConfPin.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (textView == etConfPin)
                {
                	pin_02 = etConfPin.getText().toString();
                	checkPin();
                	return true;
                }
                return false;
            }
        });
    }
    
    private void checkPin(){
    	
    	if(pin_01.equals(pin_02)){
    		edit = sp.edit();
			edit.putString("PIN_SAVED", pin_01);
			edit.commit();
    		CreateUserTask task = new CreateUserTask(ChoosePin.this, "User_1", pin_01);
		    task.execute();
    	}else{
    		dgPin(1);//Pin Mismatch
    	}
    }
    
    private void dgPin(int errCode){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	if(errCode == 1){//Pin Mismatch
    		builder.setMessage(R.string.pin_mismatch)
            .setTitle(R.string.pin_entry_error_title);
    	}else if(errCode == 2){
    		builder.setMessage(R.string.pin_entry_pin_length)
            .setTitle(R.string.pin_entry_error_title);
    	}
    	
	    builder.setCancelable(false);
	    builder.setPositiveButton(R.string.pin_entry_ok,
	        new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	            etPinEdit.setText(null);
	            etConfPin.setText(null);
	        	dialog.dismiss();
	        	
	        	//show choose pin again after compare fails
	        	pin_01 = "";
				rlPin_01.setVisibility(View.VISIBLE);
				rlPin_02.setVisibility(View.GONE);
				etPinEdit.setHint("Choose PIN");
				etPinEdit.requestFocus();
	        }
	    });
      
	    dialog = builder.create();
	    dialog.show();
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	finish();
    }
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	if(dialog != null && dialog.isShowing()){
    		dialog.dismiss();
    	}
    }
}
