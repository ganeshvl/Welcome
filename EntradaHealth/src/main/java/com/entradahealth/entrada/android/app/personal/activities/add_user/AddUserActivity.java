package com.entradahealth.entrada.android.app.personal.activities.add_user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.google.common.collect.ImmutableList;

/**
 * CRUD form form for creating new users on the device.
 *
 * @author edr
 * @since 12 Oct 2012
 */

public class AddUserActivity extends EntradaActivity
{
	private ImmutableList<User> users = null;
    Button btnCreateUser;
    EditText etUsername, etPin;
    boolean from_settings = false;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.add_user);
    	
    	from_settings = getIntent().getBooleanExtra("from_settings", false);
    	
    	/*
		 * Check if its the first time the user is accessing the app
		 * and handle Home Up visibility
		 */
    	
    	try
        {
            users = User.getUsers();
        }
        catch (UserLoadException ex)
        {
            throw new RuntimeException("Failed to get users: ", ex);
        }
    	
    	if(users.isEmpty()){
    		getActionBar().setDisplayHomeAsUpEnabled(false);
    	}else{
    		getActionBar().setDisplayHomeAsUpEnabled(true);
    	}
    	
    	
    	
    	etUsername = (EditText)findViewById(R.id.userNameText);
    	etUsername.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				btnCreateUser.setEnabled(false);
				btnCreateUser.setBackgroundResource(R.drawable.btn_round_inactive);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(etPin.length() >= 4 && etUsername.length() >0){
					btnCreateUser.setEnabled(true);
					btnCreateUser.setBackgroundResource(R.drawable.btn_round);
				}else{
					btnCreateUser.setEnabled(false);
					btnCreateUser.setBackgroundResource(R.drawable.btn_round_inactive);
				}
			}
		});
    	
    	etPin = (EditText)findViewById(R.id.pinText);
    	etPin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				btnCreateUser.setEnabled(false);
				btnCreateUser.setBackgroundResource(R.drawable.btn_round_inactive);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(etPin.length() >= 4 && etUsername.length() <= 10){
					btnCreateUser.setEnabled(true);
					btnCreateUser.setBackgroundResource(R.drawable.btn_round);
				}else{
					btnCreateUser.setEnabled(false);
					btnCreateUser.setBackgroundResource(R.drawable.btn_round_inactive);
				}
			}
		});
    	
    	btnCreateUser = (Button)findViewById(R.id.saveButton);
    	btnCreateUser.setEnabled(false);
    	btnCreateUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 String username = etUsername.getText().toString();
			     String password = etPin.getText().toString();
			     //CreateUserTask task = new CreateUserTask(AddUserActivity.this, username, password);
			     //task.execute();
			}
		});
    }
    
   @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		 switch (item.getItemId()) {
         case android.R.id.home:
        	 if(from_settings)
        		 startActivity(new Intent(this, EntradaSettings.class));
        	 else
        		 startActivity(new Intent(this, UserSelectActivity.class));
        	 return true;
        default:
        		 return super.onOptionsItemSelected(item); 
		 }
		 
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
		/*
		 * Handle the back key
		 */
		if(users.isEmpty()){
			System.exit(0);
		}else{
			if(from_settings)
				startActivity(new Intent(this, EntradaSettings.class));
	   	 	else
	   	 		startActivity(new Intent(this, UserSelectActivity.class));
		}
		
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startActivity(new Intent(this, UserSelectActivity.class));
	}
}
