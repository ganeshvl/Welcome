package com.entradahealth.entrada.android.app.personal.activities.pin_entry;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * UserSelectActivity flows into this activity to get the users'
 * PIN. Once the PIN is entered, will attempt to pull the UserPrivate
 * object and store it in the Application object; it will handle
 * the bad-password case and do something smart about it.
 *
 * @author edr
 * @since 24 Sep 2012
 */

public class PinEntryActivity extends EntradaActivity
{
	private String TAG="Entrada";
	String username;
	private UserPrivate currentUser;
    TextView tvDisplayName, tvPinErr;
    EditText etPinEdit;
    Button btnLogin;
    ImageView ivLogo, ivOK;
    Intent i;
    Bundle b;
    AlertDialog dialog;
    AsyncTaskLoadUsers myAsyncTaskLoadUsers;
    Animation animFade;
	private ProgressBar progressBar;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	Log.e("PinEntryActivity", "onCreate");    	
    	setContentView(R.layout.activity_splash);
    	//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    	ivLogo = (ImageView)findViewById(R.id.ivLogo);
		
    	Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int req_width = width * 1 / 4;
		int req_height = width * 3 / 4;
		
		/*ivLogo.getLayoutParams().width = req_width;
		ivLogo.getLayoutParams().height = req_height;
		ivLogo.requestLayout();*/
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		animFade  = AnimationUtils.loadAnimation(this, R.anim.logo_up);
		//ivLogo.startAnimation(animFade);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        
        sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        Log.e("PinEntryActivity", "onStart");
        
        BundleKeys.PinContext = this;
        //Toast.makeText(getApplicationContext(), BundleKeys.cur_uname+"--"+BundleKeys.PIN_SAVED, 1500).show();
        
        getActionBar().hide();

         i = getIntent();
         b = i.getExtras();
         username = b.getString(BundleKeys.SELECTED_USER);
         if(username != null)
        	 Log.e("Username_in_PinEntry", username);
        //User user;
         
        myAsyncTaskLoadUsers = new AsyncTaskLoadUsers();
 		myAsyncTaskLoadUsers.execute();

        
    }
    
    public class AsyncTaskLoadUsers extends AsyncTask<Void, String, Void> {

    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		progressBar.setVisibility(View.VISIBLE);
    	}
    	
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (username != null) // we came from UserSelectActivity
	        {
	                UserState us = AndroidState.getInstance().getUserState();
	                if (us != null)
	                {
	                    UserPrivate up = us.getUserData();
	                    if (up != null && !username.trim().toLowerCase().equals(us.getUserData().getName().trim().toLowerCase()))
	                    {
	                        AndroidState.getInstance().clearUserState();
	                        currentUser = null;
	                    }
	                    else
	                    {
	                        currentUser = us.getUserData();
	                    }
	                }

	        }
    	    	
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			ivLogo.startAnimation(animFade);
    	setContentView(R.layout.pin_entry);
    	tvPinErr = (TextView)findViewById(R.id.tvpasswordResult);
    	etPinEdit = (EditText)findViewById(R.id.xetpin);
    	etPinEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
    	ivOK = (ImageView)findViewById(R.id.ivButton);
    	ivOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startLogin();
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
				tvPinErr.setVisibility(View.GONE);
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
	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        etPinEdit.requestFocus();
        etPinEdit.setText(null);
			InputMethodManager imm = (InputMethodManager)getSystemService(
				    Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etPinEdit, 0);
			Log.e("Pinentry", "Input methos set");	
        etPinEdit.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (textView == etPinEdit)
                {
                    startLogin();
                    return true;
                }
                return false;
            }
        });
    }
    
    }
    
    SharedPreferences sp;
    private ImmutableList<User> users = null;
    private List<Account> accounts = null;
    
    private void getAccountForUser(int loc){
    	try {
			users = User.getUsers();
		} catch (UserLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String uname = users.get(loc).getDisplayName();
			
		try
	        {
	            if (currentUser == null)
	            {
	            	currentUser = User.getPrivateUserInformation(uname, sp.getString("PIN_SAVED", "1111"));
	            	AndroidState.getInstance().clearUserState();
	            	AndroidState.getInstance().createUserState(currentUser);
	               
	            }
 
	        }
	        catch (AccountException ex)
	        {
	        }
	        catch (UserLoadException ex)
	        {
	        }
	        catch (InvalidPasswordException ex)
	        {
	            
	        }
		
		//Get account name associated with the current user
		
				UserState state = AndroidState.getInstance().getUserState();
		        synchronized (state)
		        {
		            accounts = Lists.newArrayList(state.getAccounts());
		            
		        }
		        	Editor edit = sp.edit();
					edit.putString("sel_acc", state.getCurrentAccount().getDisplayName());
					edit.commit();
		       
		
    }


    public void loginFailure() {
        this.etPinEdit.setText(null);
    	this.etPinEdit.setHint("Try Again");
        this.etPinEdit.requestFocus();
        
        //dgPin(true);
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	finish();
    }

    private void startLogin()
    {
        String pin = etPinEdit.getText().toString();
        //etPinEdit.setText("");
        if(pin.length() >=4 && pin.length() <=10){ 
        	VerifyPinTask pinTask = new VerifyPinTask(this, currentUser, username, pin);
            pinTask.execute();
        }else{
        	dgPin(false);
        }
       
    }
    
    private void dgPin(boolean isErrorPin){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	if(isErrorPin){//User entered invalid pin
    		builder.setMessage(R.string.pin_entry_invalid_pin)
            .setTitle(R.string.pin_entry_error_title);
    	}else{//User entered pin is short in length
    		builder.setMessage(R.string.pin_entry_pin_length)
            .setTitle(R.string.pin_entry_error_title);
    	}
    	
	    builder.setCancelable(false);
	    builder.setPositiveButton(R.string.pin_entry_ok,
	        new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
	            dialog.dismiss();
	        }
	    });
      
	    dialog = builder.create();
	    dialog.show();
    }
    
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	if(dialog != null && dialog.isShowing()){
    		dialog.dismiss();
    	}
    	finish();
    }
    
}
