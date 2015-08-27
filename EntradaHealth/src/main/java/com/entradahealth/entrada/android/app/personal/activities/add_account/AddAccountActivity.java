package com.entradahealth.entrada.android.app.personal.activities.add_account;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.utils.TestConnectionTask;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The C in CRUD.
 *
 * @author edr
 * @since 27 Sep 2012
 */

public class AddAccountActivity extends EntradaActivity
{
    
    private static Button loginButton ;
    private EditText userId_editText, password_edit_Text; 
    private boolean hasDispName = false, hasCCode = false, hasRName = false, hasPWord = false, from_settings = false;
    private String uname;
    private SharedPreferences sp;
    private static EntradaApplication application;
    private String userId, password, apiHost;
    private EnvironmentHandlerFactory factory;
    public static String selectedEnvironment;
    private TextView receivedInvitation;
    private Context context;
   
    @Override
    protected void onStart()
    {
        super.onStart();

        ActionBar ab = getActionBar();
        ab.setTitle(R.string.list_accounts_add_account_button);
        sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.add_account_new);
    	context = this;
    	factory = EnvironmentHandlerFactory.getInstance();
    	uname = getIntent().getStringExtra("user_name");
    	application = (EntradaApplication) EntradaApplication.getAppContext();
    	from_settings = getIntent().getBooleanExtra("isFromEdit", false);
    	if(from_settings)
    		getActionBar().setDisplayHomeAsUpEnabled(true);
    	else
    		getActionBar().setDisplayHomeAsUpEnabled(false);

    	userId_editText = (EditText)findViewById(R.id.userid);
    	password_edit_Text = (EditText)findViewById(R.id.password);
    	loginButton = (Button)findViewById(R.id.loginButton);
    	receivedInvitation = (TextView) findViewById(R.id.receivedInvitation);
    	receivedInvitation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
	            Intent intent = new Intent(context, NewUserActivity.class);
	            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            context.startActivity(intent);

			}
		});
    	
    	userId_editText.addTextChangedListener(new TextWatcher() {
			
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
				if(s.length() >0 && password_edit_Text.length()>0 ){
					loginButton.setEnabled(true);
					loginButton.setBackgroundResource(R.drawable.btn_round);
				}else{
					loginButton.setEnabled(false);
					loginButton.setBackgroundResource(R.drawable.btn_round_inactive);
				}
			}
		});
    	
    	userId_editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (textView == userId_editText)
                {
                	//testConnection();
                    return true;
                }
                return false;
            }
        });

    	password_edit_Text.addTextChangedListener(new TextWatcher() {
			
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
				if(s.length() >0 && userId_editText.length()>0 ){
					loginButton.setEnabled(true);
					loginButton.setBackgroundResource(R.drawable.btn_round);
				}else{
					loginButton.setEnabled(false);
					loginButton.setBackgroundResource(R.drawable.btn_round_inactive);
				}
			}
		});
    	
    	password_edit_Text.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (textView == password_edit_Text)
                {
                	//testConnection();
                    return true;
                }
                return false;
            }
        });


    	loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					if(selectedEnvironment!=null && !selectedEnvironment.equals(EnvironmentHandlerFactory.CANCEL)){
						Environment environment = factory.getHandler(selectedEnvironment);
						apiHost = environment.getApi();
						userId = userId_editText.getText().toString();
						password = password_edit_Text.getText().toString();
						authenticateUser();					
					}
				}else{
					Toast.makeText(getApplicationContext(), "Could not reach host. Please check your internet connection", Toast.LENGTH_SHORT).show();
				}
		    }
		});
    	
    	loginButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				DialogFragment fragment = MyDialogFragment.newInstance();
				fragment.show(ft, "dialog");
				return false;
			}
		});
    	
    }
    
    public static class MyDialogFragment extends DialogFragment {

    	private String[] values = {EnvironmentHandlerFactory.PROD, EnvironmentHandlerFactory.SALES, EnvironmentHandlerFactory.QA1, EnvironmentHandlerFactory.QA2, EnvironmentHandlerFactory.DEV, EnvironmentHandlerFactory.CANCEL};
    	private ArrayAdapter<String> adapter;
    	private Dialog dialog;
    	
        static MyDialogFragment newInstance() {
            MyDialogFragment f = new MyDialogFragment();
            return f;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, 0);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	// TODO Auto-generated method stub
        	dialog = super.onCreateDialog(savedInstanceState);
        	dialog.setTitle("Choose Environment");
        	return dialog;
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.login_environments_layout, container, false);
            ListView listView = (ListView) v.findViewById(R.id.environmentsListView);
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, values);   
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new ListItemClickListener());
            return v;
        }
        
        class ListItemClickListener implements OnItemClickListener{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				selectedEnvironment = adapter.getItem(position);
				if(selectedEnvironment.equals(EnvironmentHandlerFactory.CANCEL)){
					loginButton.setText("Login");
				} else {
					loginButton.setText("Login to "+selectedEnvironment);
				}
				application.setStringIntoSharedPrefs("environment", selectedEnvironment);
				dialog.dismiss();
			}
        	
        }
    }
    
    private void authenticateUser(){
    	  if(from_settings){
          	createUser();
          }else{
          	TestConnectionTask task = new TestConnectionTask(AddAccountActivity.this, apiHost, userId, password, selectedEnvironment, false, false, false);
  	        task.execute();
          }
    }
    
    User user;
    UserPrivate curUser;
    private void createUser(){
    	new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            	try {
					user = User.createNewUser(uname, uname, sp.getString("PIN_SAVED", "1111"));
					user.save();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return null;
            }
            
            protected void onPostExecute(Void result) {
            	Editor edit = sp.edit();
				edit.putString("CUR_UNAME", uname);
				edit.putString("PIN_SAVED", sp.getString("PIN_SAVED", "1111"));
				edit.putString("sel_user", uname);
				edit.commit();
        		BundleKeys.cur_uname = uname;
                
                User user;

                	if (uname != null) // we came from UserSelectActivity
                    {
                        try
                        {            	
                            UserState us = AndroidState.getInstance().getUserState();
                            synchronized (us){
	                            if (us != null)
	                            {
	                                UserPrivate up = us.getUserData();
	                                if (up != null && !uname.trim().toLowerCase().equals(us.getUserData().getName().trim().toLowerCase()))
	                                {
	                                	curUser = us.getUserData();
	                                	AndroidState.getInstance().clearUserState();
	                                    curUser = null;
	                                }
	                                else
	                                {
	                                	curUser = us.getUserData();
	                                }
	                            }
                            }
                            user = User.getPublicUserInformation(uname);
                            
                        }
                        catch (UserLoadException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                    		
            		try
            	        {
            	            if (curUser == null)
            	            {
            	            	curUser = User.getPrivateUserInformation(BundleKeys.cur_uname, sp.getString("PIN_SAVED", "1111"));
            	                AndroidState.getInstance().createUserState(curUser);
            	                
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
            	
            };
            
        }.execute();
    }
    
    // Make this user as current user
    private UserPrivate currentUser;
    private ImmutableList<User> users = null;
    private List<Account> accounts = null;
    
    private void setCurrentUser(String username){
    	if (username != null) // we came from UserSelectActivity
        {
            try
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

                user = User.getPublicUserInformation(username);
            }
            catch (UserLoadException ex)
            {
                throw new RuntimeException(ex);
            }
        }else
        {
        	getAccountForUser(0);
        }
    	
    }
    
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
             startActivity(new Intent(this, EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        	 return true;
        default:
       		 return super.onOptionsItemSelected(item); 
		 }
	}

    @Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
    }
	
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
    
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		finish();
	}

}
