package com.entradahealth.entrada.android.app.personal.activities.add_account;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;


public class NewUserActivity extends EntradaActivity{

	private EditText registrationCode;
	private TextView validateMessage;
	private Map<String, String> environmentsMap;
	private LinearLayout userDetailsLayout;
	private APIService service;
	private Button createAccount;
	private Context context;
	private String invitationCode;
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\w{6}(-(DEV|QA1|QA2|STAGE))?$");
	
    @Override
    protected void onStart()
    {
        super.onStart();

        ActionBar ab = getActionBar();
        ab.setTitle(R.string.redeem_invitation);
        
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newuser_registration_layout);
		environmentsMap = new HashMap<String, String>();
		context = this;
		fillMapValues();
		registrationCode = (EditText) findViewById(R.id.registrationCode);
		validateMessage = (TextView) findViewById(R.id.validateMessage);
		userDetailsLayout = (LinearLayout) findViewById(R.id.newUserdetailsLayout);
		createAccount = (Button) findViewById(R.id.createAccount);
		final TextView password = (TextView) findViewById(R.id.password);
		final TextView confirmPassword = (TextView) findViewById(R.id.confirmPassword);
		createAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(password.getText().toString().isEmpty() || password.getText()==null || confirmPassword.getText().toString().isEmpty() || confirmPassword.getText()==null){
					validateMessage.setText("Password can't be empty");
					validateMessage.setTextColor(Color.RED);
					return;
				} 
				if(password.getText().toString().length()>0 && password.getText().toString().length()<6){
					validateMessage.setText("Password should be minimum 6 characters");
					validateMessage.setTextColor(Color.RED);					
					return;
				}
				if(!password.getText().toString().equals(confirmPassword.getText().toString())){
					validateMessage.setText("Password mismatch");
					validateMessage.setTextColor(Color.RED);
					return;
				}
				validateMessage.setText("");
				ENTUser user = new ENTUser();
				user.setFirstName(((TextView)findViewById(R.id.firstName)).getText().toString());
				user.setLastName(((TextView)findViewById(R.id.lastName)).getText().toString());
				user.setMI(((TextView)findViewById(R.id.MI)).getText().toString());
				user.setPhoneNumber(((TextView)findViewById(R.id.phoneNumber)).getText().toString());
				user.setEmailAddress(((TextView)findViewById(R.id.emailAddress)).getText().toString());
				user.setPassword(password.getText().toString());
				user.setRegistrationCode(invitationCode);
				RegisterUserTask task = new RegisterUserTask(user);
				task.execute();
			}
		});
		registrationCode.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable str) {
				// TODO Auto-generated method stub
				if(str.length()==0){
					validateMessage.setText("Enter your invitation code above");
					validateMessage.setTextColor(Color.BLACK);
				} else if(str.length()>0 && str.length()<6){
					validateMessage.setText("Code too short");
					validateMessage.setTextColor(Color.BLACK);
				} else {
					String code = str.toString().toUpperCase();
					if(code.contains("-") && (code.contains("DEV") || code.contains("QA1") || code.contains("QA2"))){
						validateMessage.setText("Checking..");
						validateMessage.setTextColor(Color.BLACK);
						ValidateRegistrationCode validateTask = new ValidateRegistrationCode(code);
						validateTask.execute();
					} else {
						validateMessage.setText("Invalid Code");
						validateMessage.setTextColor(Color.RED);						
					}
				}
			}
		});
	}	
	
	protected void fillMapValues(){
		environmentsMap.put("DEV", EnvironmentHandlerFactory.DEV);
		environmentsMap.put("QA1", EnvironmentHandlerFactory.QA1);
		environmentsMap.put("QA2", EnvironmentHandlerFactory.QA2);
	}
	
	class ValidateRegistrationCode extends AsyncTask{
		
		private String code;
		private String response;
		
		public ValidateRegistrationCode(String code){
			this.code = code;
		}

		@Override
		protected Object doInBackground(Object... params) {
			String[] str = code.split("-");
			String env = str[1], codedigits = str[0];
			EnvironmentHandlerFactory factory = EnvironmentHandlerFactory.getInstance();
			Environment environment = factory.getHandler(environmentsMap.get(env));
			try {
				service = new APIService(environment.getApi(), "", "");
				response = service.validateRegistrationCode(code);
				invitationCode = code;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			ENTUser user = null;
			try {
				String email = "";
				String phoneNumber = "";
				JSONObject json = new JSONObject(response);
				try{
					email = json.getString("EmailAddress");
				} catch(JSONException ex){
					email = "";
				}
				try{
					phoneNumber = json.getString("PhoneNumber");
				} catch(JSONException ex){
					phoneNumber = "";
				}
				user = new ENTUser(json.getString("MI"), json.getString("FirstName"), json.getString("LastName"), phoneNumber, email);
				validateMessage.setText("Code Verified, Please fill in your details");
				validateMessage.setTextColor(Color.BLACK);
				populateUserDetails(user);
			} catch (JSONException e) {
				validateMessage.setText("Invalid code");
				validateMessage.setTextColor(Color.RED);
			}
		}
	}
	
	protected void populateUserDetails(ENTUser user){
		registrationCode.setEnabled(false);
		userDetailsLayout.setVisibility(View.VISIBLE);
		((TextView)findViewById(R.id.firstName)).setText(user.getFirstName());
		((TextView)findViewById(R.id.lastName)).setText(user.getLastName());
		((TextView)findViewById(R.id.MI)).setText(user.getMI());
		((TextView)findViewById(R.id.phoneNumber)).setText(user.getPhoneNumber());
		((TextView)findViewById(R.id.emailAddress)).setText(user.getEmailAddress());
	}
	
	class RegisterUserTask extends AsyncTask{

		private ENTUser user;
		int statusCode;
		
		public RegisterUserTask(ENTUser user){
			this.user = user;
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			createAccount.setText("Redeeming..");
			createAccount.setEnabled(false);
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				statusCode = service.registerUser(user);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			createAccount.setEnabled(true);
			createAccount.setText("Create Account");
			if(statusCode==201){
				Intent intent = new Intent(context, AddAccountActivity.class);
				startActivity(intent);
			} else {
				validateMessage.setText("Redeeming failed");
				validateMessage.setTextColor(Color.RED);
			}
		}
	}
}
