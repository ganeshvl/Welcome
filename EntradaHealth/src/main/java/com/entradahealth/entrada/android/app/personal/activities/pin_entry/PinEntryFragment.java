package com.entradahealth.entrada.android.app.personal.activities.pin_entry;

import java.util.List;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class PinEntryFragment extends Fragment{

	String username;
	private UserPrivate currentUser;
    TextView tvDisplayName, tvPinErr;
    EditText etPinEdit;
    Button btnLogin;
    ImageView ivLogo, ivOK;
    Bundle b;
    AlertDialog dialog;
    AsyncTaskLoadUsers myAsyncTaskLoadUsers;
    Animation animFade;
	//private ProgressBar progressBar;
	private boolean fromSettings = false;
	private boolean fromAddAccount = false;
	private boolean fromRedeemInvite = false;
	private SharedPreferences sp;
	View view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.pin_entry, container, false);
    	ivLogo = (ImageView)view.findViewById(R.id.ivLogo);
    	tvPinErr = (TextView) view.findViewById(R.id.tvpasswordResult);
    	etPinEdit = (EditText) view.findViewById(R.id.xetpin);
    	ivOK = (ImageView) view.findViewById(R.id.ivButton);
    	b = getArguments();
    	Display display = getActivity().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int req_width = width * 1 / 4;
		int req_height = width * 3 / 4;
		
		/*ivLogo.getLayoutParams().width = req_width;
		ivLogo.getLayoutParams().height = req_height;
		ivLogo.requestLayout();*/
		//progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		//animFade  = AnimationUtils.loadAnimation(getActivity(), R.anim.logo_up);
		//ivLogo.startAnimation(animFade);

		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        sp = getActivity().getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        Log.e("PinEntryActivity", "onStart");
        
        BundleKeys.PinContext = getActivity();
        //Toast.makeText(getApplicationContext(), BundleKeys.cur_uname+"--"+BundleKeys.PIN_SAVED, 1500).show();
        
        getActivity().getActionBar().hide();

         username = b.getString(BundleKeys.SELECTED_USER);
         fromSettings = b.getBoolean(BundleKeys.FROM_SETTINGS);
         fromAddAccount = b.getBoolean(BundleKeys.FROM_ADD_ACCOUNT);
         fromRedeemInvite = b.getBoolean(BundleKeys.FROM_REDEEM_INVITE);
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
    		//progressBar.setVisibility(View.VISIBLE);
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
			//progressBar.setVisibility(View.GONE);
			//ivLogo.startAnimation(animFade);
    	//setContentView(R.layout.pin_entry);
    	etPinEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
    	try {
	    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
        etPinEdit.requestFocus();
        etPinEdit.setText(null);
        try {
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				    Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etPinEdit, 0);
			Log.e("Pinentry", "Input methos set");
        } catch(Exception ex){
        	ex.printStackTrace();
        }
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
    
    private void startLogin()
    {
        String pin = etPinEdit.getText().toString();
        //etPinEdit.setText("");
        if(pin.length() >=4 && pin.length() <=10){ 
        	VerifyPinAsynTask pinTask = new VerifyPinAsynTask(this, currentUser, username, pin, fromSettings, fromAddAccount, fromRedeemInvite);
            pinTask.execute();
        } else {
        	dgPin(false);
        }
    }
    
    private void dgPin(boolean isErrorPin){
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    public void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	if(dialog != null && dialog.isShowing()){
    		dialog.dismiss();
    	}
    }
}
