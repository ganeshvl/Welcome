package com.entradahealth.entrada.android.app.personal.activities.settings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.DialogTask;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.AddAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.add_account.Dictator;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.edit_account.EditAccountActivity;
import com.entradahealth.entrada.android.app.personal.activities.inbox.UserAuthenticate;
import com.entradahealth.entrada.android.app.personal.activities.pin_entry.PinEntryActivity;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.android.app.personal.files.AndroidFileResolver;
import com.entradahealth.entrada.android.app.personal.utils.AndroidUtils;
import com.entradahealth.entrada.android.app.personal.utils.NetworkState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.auth.exceptions.UserLoadException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.files.FileResolver;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class EntradaSettings extends EntradaActivity {
	
	ExpandableListView lvUsers;
	private ImmutableList<User> users = null;
	SharedPreferences sp;
	Menu j_Menu;
	AlertDialog dgJType;
	ProgressDialog dialog;
	String sel_acc, del_accountName;
	Account deletedAccount = null;
	User user;
	RelativeLayout rlAddAccount, rlPasscode, rlSecureMessaging;
	ToggleButton tbBluetooth, tbProximity, tbVibeOnRecord, tbVibeOnStop, tbSecureMessaging;
	TextView tvSyncDaysCtr, tvSyncMinsCtr, tvPasscodeTime, tvVersion, tvBuild, tvLogs, tvDiagnostics;
	Button btnDaysCtrIncr, btnDaysCtrDecr, btnMinsIncr, btnMinsDecr;
	int days_ct, mins_ct;
	String[] passcode_names = {"Immediately", "After 5 minutes", "After 10 minutes", "After 15 minutes",
								"After 30 minutes", "After an hour", "After 2 hours"};
	//int[] passcode_values = {0, 5 , 10, 15, 30, 60, 120}; 
	private List<Account> accounts = null;
	boolean deleted = false, qchanged = false, isEmail = false;
	int total_user = 0;
	List<Account> str_arr_acc_names;
	SensorManager mySensorManager;
	Sensor myProximitySensor;
	String dev_model, dev_version, app_version_name, sel_api;
	int app_version_code;  
	NetworkState ns;
	Boolean isConnected;
	private Context context;
	private EntradaApplication application;
	private ExpandableListAdapter adapter;
	UserPrivate userPrivate;
	UserState state;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrada_settings);
		context = this;
		try {
			sp = getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
			userPrivate = User.getPrivateUserInformation(sp.getString("sel_user", "User_1"), sp.getString("PIN_SAVED", "1111"));
			state = new UserState(userPrivate);
		} catch (AccountException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidPasswordException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DomainObjectWriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UserLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		application = (EntradaApplication) EntradaApplication.getAppContext();
		getActionBar().setTitle("Settings");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		dialog = new ProgressDialog(this);
		
		sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
		
		del_accountName = getIntent().getStringExtra("del_accountName");
		//if(del_accountName != null && !del_accountName.isEmpty())
			//deletedAccount = AndroidState.getInstance().getUserState().getAccount(del_accountName);
		lvUsers = (ExpandableListView)findViewById(R.id.userList);
		LoadUserDictatorsAsyncTask task = new LoadUserDictatorsAsyncTask();
		task.execute();
		rlAddAccount = (RelativeLayout)findViewById(R.id.rlAddAccount);
		rlAddAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				running = false;
				ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	        if ("com.entradahealth.entrada.android.app.personal.sync.SyncService".equals(service.service.getClassName()) ||
	    	        		"com.entradahealth.entrada.android.app.personal.sync.DictationUploadService".equals(service.service.getClassName())) {
	    	            running = true;
	    	        }
	    	    }
				
				if(!running)
					createUser(1);
				else
					Toast.makeText(EntradaSettings.this,
	                         "Please wait until the current account has finished syncing to add account.",
	                         Toast.LENGTH_SHORT).show();
				
			}
		});
		
		rlPasscode  =(RelativeLayout)findViewById(R.id.rlPasscode);
		rlPasscode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dgPassCodeInterval();
			}
		});
		
		rlSecureMessaging = (RelativeLayout)findViewById(R.id.rlSecureMessaging);
		
		tbBluetooth = (ToggleButton)findViewById(R.id.tbBluetooth);
		final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();   
		if(mBluetoothAdapter.isEnabled()){
			tbBluetooth.setChecked(true);
		}else{
			tbBluetooth.setChecked(false);
		}
		tbBluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					 
					if ( ! mBluetoothAdapter.isEnabled()) {
					    	mBluetoothAdapter.enable(); 
					}
				}else{
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
					if (mBluetoothAdapter.isEnabled()) {
					    mBluetoothAdapter.disable(); 
					}
				}
			}
		});
		
		
		tbProximity = (ToggleButton)findViewById(R.id.tbProximity);
		tbProximity.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					BundleKeys.PROXIMITY_LOCK = true;
				}else{
					BundleKeys.PROXIMITY_LOCK = false;
				}
				Editor edit = sp.edit();
		    	edit.putBoolean("PROXIMITY_LOCK", BundleKeys.PROXIMITY_LOCK);
				edit.commit();
			}
		});
		
		if(sp.getBoolean("PROXIMITY_LOCK", true))
			tbProximity.setChecked(true);
		else
			tbProximity.setChecked(false);
		/*
		 * Check if device has proximity sensor
		 */
		mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (myProximitySensor == null){
			tbProximity.setEnabled(false);
        }else{
        	tbProximity.setEnabled(true);
        }
		
		tbVibeOnRecord = (ToggleButton)findViewById(R.id.tbVibeOnRecord);
		if(sp.getBoolean("VIB_ON_RECORD", false)){
			tbVibeOnRecord.setChecked(true);
		}else{
			tbVibeOnRecord.setChecked(false);
		}
		tbVibeOnRecord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					BundleKeys.VIB_ON_RECORD = true;
				}else{
					BundleKeys.VIB_ON_RECORD = false;
				}
				Editor edit = sp.edit();
		    	edit.putBoolean("VIB_ON_RECORD", BundleKeys.VIB_ON_RECORD);
				edit.commit();
			}
		});
		
		tbVibeOnStop = (ToggleButton)findViewById(R.id.tbVibeOnStop);
		if(sp.getBoolean("VIB_ON_STOP", false)){
			tbVibeOnStop.setChecked(true);
		}else{
			tbVibeOnStop.setChecked(false);
		}
		tbVibeOnStop.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					BundleKeys.VIB_ON_STOP = true;
				}else{
					BundleKeys.VIB_ON_STOP = false;
				}
				Editor edit = sp.edit();
		    	edit.putBoolean("VIB_ON_STOP", BundleKeys.VIB_ON_STOP);
				edit.commit();
			}
		});
		
		tbSecureMessaging = (ToggleButton)findViewById(R.id.tbSecureMessaging);
		if(sp.getBoolean("SECURE_MSG", true)){
			tbSecureMessaging.setChecked(true);
		}else{
			tbSecureMessaging.setChecked(false);
		}
		tbSecureMessaging.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					BundleKeys.SECURE_MSG = true;
				}else{
					BundleKeys.SECURE_MSG = false;
				}
				Editor edit = sp.edit();
		    	edit.putBoolean("SECURE_MSG", BundleKeys.SECURE_MSG);
				edit.commit();
			}
		});
		
		days_ct = sp.getInt("DAYS_TO_SYNC", 1);
		mins_ct = sp.getInt("MINS_TO_SYNC", 5);
		
		//Days To Sync Controls
		tvSyncDaysCtr = (TextView)findViewById(R.id.tvSyncDaysCtr);
		//tvSyncDaysCtr.setText(Integer.toString(BundleKeys.days_to_sync));
		tvSyncDaysCtr.setText(Integer.toString(days_ct));
		
		btnDaysCtrIncr = (Button)findViewById(R.id.btnDaysIncr);
		btnDaysCtrIncr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				days_ct = Integer.parseInt(tvSyncDaysCtr.getText().toString());
				if(days_ct != 5)
					days_ct++;
				tvSyncDaysCtr.setText(Integer.toString(days_ct));
				
				BundleKeys.days_to_sync = days_ct;
				
			}
		});
		
		btnDaysCtrDecr = (Button)findViewById(R.id.btnDaysDecr);
		btnDaysCtrDecr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				days_ct = Integer.parseInt(tvSyncDaysCtr.getText().toString());
				if(days_ct != 1)
					days_ct--;
				tvSyncDaysCtr.setText(Integer.toString(days_ct));
				
				BundleKeys.days_to_sync = days_ct;
			}
		});
		
		//Minutes To Sync Controls
		tvSyncMinsCtr = (TextView)findViewById(R.id.tvSyncMinsCtr);
		//tvSyncMinsCtr.setText(Integer.toString(BundleKeys.mins_to_sync));
		tvSyncMinsCtr.setText(Integer.toString(mins_ct));
				
		btnMinsIncr = (Button)findViewById(R.id.btnMinsIncr);
		btnMinsIncr.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mins_ct = Integer.parseInt(tvSyncMinsCtr.getText().toString());
						if(mins_ct != 60)
							mins_ct++;
						tvSyncMinsCtr.setText(Integer.toString(mins_ct));
						
						BundleKeys.mins_to_sync = mins_ct;
					}
				});
				
		btnMinsDecr = (Button)findViewById(R.id.btnMinsDecr);
		btnMinsDecr.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mins_ct = Integer.parseInt(tvSyncMinsCtr.getText().toString());
						if(mins_ct != 1)
							mins_ct--;
						tvSyncMinsCtr.setText(Integer.toString(mins_ct));
						
						BundleKeys.mins_to_sync = mins_ct;
					}
				});
		
		tvPasscodeTime = (TextView)findViewById(R.id.tvPasscodeTime);
		getPasscode();
		
		tvVersion = (TextView)findViewById(R.id.tvVersion);
		tvBuild = (TextView)findViewById(R.id.tvBuild);
		
			tvVersion.setText(sp.getString("APP_VER_NAME", "5.3.0"));
			tvBuild.setText(Integer.toString(sp.getInt("APP_VER_CODE", 1)));
		
		tvLogs = (TextView)findViewById(R.id.tvLogs);
		tvLogs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isEmail = false;
				//sendLogs();
				ns = new NetworkState(getApplicationContext());
				isConnected = ns.isConnectingToInternet();
				if(isConnected)
					sendLogToSupport();
				else
					Toast.makeText(getApplicationContext(), "No connection available", Toast.LENGTH_SHORT).show();
			}
		});
		
		tvDiagnostics = (TextView)findViewById(R.id.tvDiagnostics) ;
		tvDiagnostics.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isEmail = true;
				sendDiagnostics();
			}
		});
		
	}

	class LoadUserDictatorsAsyncTask extends AsyncTask{

		private List<EUser> eUsers;
		private HashMap<EUser, List<Dictator>> listChildData;
		MainUserDatabaseProvider provider;
		
		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				provider = new MainUserDatabaseProvider(false);
				eUsers = provider.getEUsers();
				listChildData = new HashMap<EUser, List<Dictator>>();
				for(EUser user : eUsers){
					List<Dictator> dictators = provider.getDictatorsForUser(user.getName());
					Log.e("","dictators--"+dictators.size());
					listChildData.put(user, dictators);
				}
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			adapter = new ExpandableListAdapter(context, eUsers, listChildData);
	        lvUsers.setAdapter(adapter);
	        setListViewHeight(lvUsers);
		}
	}
		
	class AuthenticateUser extends UserAuthenticate{

		private List<EUser> eUsers;
		private MainUserDatabaseProvider provider;
		private String username;
		private String dictatorId;
		private String dictatorName;
		private List<Dictator> dictators;
		
		public AuthenticateUser(MainUserDatabaseProvider provider, List<EUser> eUsers, List<Dictator> dictators, String dictatorId, String dictatorName, String apiHost, String username, String password, Activity _activity) {
			super(apiHost, username, password, _activity);
			this.eUsers = eUsers;
			this.username  = username;
			this.dictatorId = dictatorId;
			this.dictatorName = dictatorName;
			this.provider = provider;
			this.dictators = dictators;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_ID, dictatorId);
			application.setStringIntoSharedPrefs(BundleKeys.DICTATOR_NAME, dictatorName);

		}
		
		@Override
		protected void onPostSuccessful() {
			super.onPostSuccessful();
            for (EUser user : eUsers) {
            	if(username.equals(user.getName())) {
            		user.setCurrent(true);
					user.setCurrentDictator(dictatorId);
				} else {
            		user.setCurrent(false);
            	}
				try {
					provider.updateUser(user);
				} catch (DomainObjectWriteException e) {
					e.printStackTrace();
				}
			}
            for(Dictator dictator : dictators){
            	try {
            		if(dictator.getDictatorID()==Long.valueOf(dictatorId)){
            			dictator.setCurrent(true);
            		} else {
            			dictator.setCurrent(false);
            		}
					provider.updateDictator(dictator, username);
				} catch (DomainObjectWriteException e) {
					e.printStackTrace();
				}
            }
            new LoadUserDictatorsAsyncTask().execute(); 
		}
	}
	
	String uname;
	private void createUser(int i){
		/*
		 * Check if folder with username exists, if so increment and check again
		 */
		String sdcard_state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(sdcard_state)) {
			final FileResolver resolver = new AndroidFileResolver();
	    	File UserPath = resolver.resolve("Users");
            
            uname = "User_"+Integer.toString(i);
            
            File[] root = UserPath.listFiles();
            for (File f : root)
            {
                //if (f.isDirectory()) {
                	if(f.getName().toLowerCase().equals(uname.toLowerCase())){
                		i = i + 1;
                		createUser(i);
                	}
                //}
            }
            
            //uname = "User_"+Integer.toString((root.length+1));
            // navigate directly to AddAccountActivity
            Intent intent = new Intent(EntradaSettings.this, AddAccountActivity.class);
        	//Intent intent = new Intent(this.activity, Setup.class);
			intent.putExtra("from_settings", true);
			intent.putExtra("user_name", uname);
			intent.putExtra("from_settings", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
	    }
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
   	 	saveChanges(sel_acc, sp.getString("PIN_SAVED", "1111"));
	}
	
	File userPath;
	boolean fdeleted;
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		isEmail = false;
		deleted = getIntent().getBooleanExtra("deleted", false);
		qchanged = getIntent().getBooleanExtra("qchanged", false);
		
		if(deleted){
			
			BundleKeys.POSITION = 0;
			Editor edit = sp.edit();
        	edit.putInt("cur_acc_pos", BundleKeys.POSITION);
			edit.commit();
			
			userPath = new File(User.getUserRoot(), del_accountName);
            boolean success = false;
            success = userPath.mkdirs();
            Log.e("mkdirs", Boolean.toString(success));
			try
	        {
	            users = User.getUsers();
	        }
	        catch (UserLoadException ex)
	        {
	            throw new RuntimeException("Failed to get users: ", ex);
	        }
			
			UserState state = AndroidState.getInstance().getUserState();
	        synchronized (state)
	        {
	            accounts = Lists.newArrayList(state.getAccounts());
	            
	        }
			
	        getAccountForUser(0, true);
		}else{
			BundleKeys.POSITION = sp.getInt("cur_acc_pos", 0);
		}
			
        //refreshUserList();
	}

	int responseCode;
	String file_name;
	File logFile;
	
	private void sendLogToSupport(){
		
			new AsyncTask<Void, Void, Void>() {
				FileInputStream is = null;
				MessageDigest md  = null;
				String checksum = null;
				DigestInputStream dis = null;
				int response;
				
				protected void onPreExecute() {
					dialog.setCancelable(false);
					dialog.setMessage("Sending Logs...Please wait...");
					dialog.show();
					
				};
				
				@Override
				protected Void doInBackground(Void... voids) {
					
					//Generate log files
					c = Calendar.getInstance();
					hr = c.get(Calendar.HOUR_OF_DAY);
					min = c.get(Calendar.MINUTE);
					year = c.get(Calendar.YEAR);
					mon = c.get(Calendar.MONTH);
					day = c.get(Calendar.DAY_OF_MONTH);
					
					if(hr >= 10)
						str_hr = Integer.toString(hr);
					else
						str_hr = "0"+ Integer.toString(hr);
						
					
					if(min >= 10)
						str_min = Integer.toString(min);
					else
						str_min = "0"+ Integer.toString(min);
						
					
					sp = getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
					BundleKeys.SYNC_FOR_ACC = sp.getString("SYNC_FOR_ACC", null);
					BundleKeys.SYNC_FOR_CLINIC = sp.getString("SYNC_FOR_CLINIC", null);
					
					file_name = BundleKeys.SYNC_FOR_CLINIC+"-"+BundleKeys.SYNC_FOR_ACC+"-"+year+"-"+(mon+1)+"-"+day+"-"+str_hr+"-"+str_min;
					
					File mFolder = null;
					
					try {
						
					      Process process = Runtime.getRuntime().exec("logcat -d");
						  BufferedReader bufferedReader = new BufferedReader(
					    
						  new InputStreamReader(process.getInputStream()));
					      StringBuilder log=new StringBuilder();
					      String line;
					      line = "Version Name: "+sp.getString("APP_VER_NAME", "5.3.0") + "\n";
					      line = line +"Build: "+sp.getInt("APP_VER_CODE", 1) + "\n";
					      line = line +"Android: "+android.os.Build.VERSION.RELEASE + "\n";
					      line = line +"Manufacturer: "+android.os.Build.MANUFACTURER + "\n";
					      line = line +"Model: "+android.os.Build.MODEL + "\n";
					      log.append(line+"\n");
					      while ((line = bufferedReader.readLine()) != null) {
					    	  if(line.contains(processId))
					    		  log.append(line+"\n");
					      }
					      
					      root = AndroidUtils.ENTRADA_DIR;
					      
					       mFolder = new File(root + "/Logs");
				            if (!mFolder.exists()) {
				                mFolder.mkdir();
				            }
							
				          logFile = new File(mFolder.getAbsolutePath(), file_name+".txt");
				          //File logFile = new File(mFolder.getAbsolutePath(), "Logs.txt");
				          if(!logFile.exists())
				        	  logFile.createNewFile();
				          
				          FileOutputStream fOut = new FileOutputStream(logFile);
			              OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			              myOutWriter.write(log.toString());
				          myOutWriter.close();
				            
						} catch (IOException e) {
							Toast.makeText(getApplicationContext(), e.getMessage(), 2000).show();
					    	
					    }
					
					/*ArrayList<String> list_files;
					String[] logFiles = null;
					File[] listOfFiles = mFolder.listFiles();
					list_files = new ArrayList<String>(listOfFiles.length);
					
					for (int i = 0; i < listOfFiles.length; i++) {
						list_files.add(listOfFiles[i].getAbsolutePath());
					}
					
					logFiles = list_files.toArray(new String[list_files.size()]);
					Compress compress = new Compress(logFiles, root+"/Logs.zip");
					compress.zip();*/
					
					
					
					
					//final File logZip = new File(root+"/Logs.zip");
					//File logZip = new File(root+"/Logs/Logs.txt");
					//Log.e("file_desc", Long.toString(logZip.length()));
					try{ 
						  md = MessageDigest.getInstance("MD5"); 
					}catch(NoSuchAlgorithmException e1) { 
					  // TODO Auto-generated catch block
					  e1.printStackTrace(); 
					}
					
					try {
						is = new FileInputStream(logFile);
						dis = new DigestInputStream(is, md);
						while (dis.read() != -1)
							continue;
						checksum = Strings.padStart(
								new BigInteger(1, md.digest()).toString(16),
								32, '0');
					} catch (Exception ex) {
						Log.e("Entrada-SendLogs",
								"Failure in checksum construction: ", ex);
					}
					
					
					
					try {
						EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
						com.entradahealth.entrada.android.app.personal.Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
						APIService service = new APIService(env.getApi());
					 	response = service.uploadLogs(is, checksum, logFile);
					 }catch (ServiceException e) { 
						  // TODO Auto-generated catch block
						  e.printStackTrace(); 
					 } catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
				
				protected void onPostExecute(Void result) {
					dialog.dismiss();
					logFile.delete();
					if(!isFinishing())
						dgLogSent(response);
						
				};
				
			}.execute();
			
			
		}/*catch (Exception ex) {
			Log.e("Entrada-SendLogs",
					"Failure in checksum construction: ", ex);
		}*/
		
	AlertDialog dgLogs;
	private void dgLogSent(int res) {
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(true);
		if(res == 201)
			builder.setMessage(R.string.dg_logs_success);
		else
			builder.setMessage(R.string.dg_logs_fail);
		
		builder.setPositiveButton("OK", null);
		dgLogs = builder.create();
		dgLogs.show();
	}
	
	private void sendEmail(String which){
		
		String email_subject = "["+BundleKeys.SYNC_FOR_ACC +"-"+ sel_api+"]"+ "eDictate diagnostics report"; 
		String mailTo = getResources().getString(R.string.diagnostics_recepient);
		Intent email_intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",mailTo, null)); 
		email_intent.putExtra(android.content.Intent.EXTRA_SUBJECT, email_subject);
		email_intent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.diagnostics_text)); 
		email_intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+root+"/"+which));
		startActivity(Intent.createChooser(email_intent, "Complete action with"));
	}
	
	File root;
	Calendar c;
	private int year, mon, day, hr, min;
	String str_hr, str_min;
	
	private static final String processId = Integer.toString(android.os.Process.myPid());
	public void sendLogs(){
		
		c = Calendar.getInstance();
		hr = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
		year = c.get(Calendar.YEAR);
		mon = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		
		if(hr > 10)
			str_hr = Integer.toString(hr);
		else
			str_hr = "0"+ Integer.toString(hr);
			
		
		if(min > 10)
			str_min = Integer.toString(min);
		else
			str_min = "0"+ Integer.toString(min);
			
		
		file_name = year+"-"+(mon+1)+"-"+day+"-"+str_hr+"-"+str_min;
		
		File mFolder = null;
		
		try {
			
		      Process process = Runtime.getRuntime().exec("logcat -d");
			  BufferedReader bufferedReader = new BufferedReader(
		    
			  new InputStreamReader(process.getInputStream()));
		      StringBuilder log=new StringBuilder();
		      String line;
		      line = "Version Name: "+sp.getString("APP_VER_NAME", "5.3.0") + "\n";
		      line = line +"Build: "+sp.getInt("APP_VER_CODE", 1) + "\n";
		      line = line +"Android: "+android.os.Build.VERSION.RELEASE + "\n";
		      line = line +"Manufacturer: "+android.os.Build.MANUFACTURER + "\n";
		      line = line +"Model: "+android.os.Build.MODEL + "\n";
		      log.append(line+"\n");
		      while ((line = bufferedReader.readLine()) != null) {
		    	  if(line.contains(processId))
		    		  log.append(line+"\n");
		      }
		      
		      root = AndroidUtils.ENTRADA_DIR;
		      
		       mFolder = new File(root + "/Logs");
	            if (!mFolder.exists()) {
	                mFolder.mkdir();
	            }
				
	          File logFile = new File(mFolder.getAbsolutePath(), file_name+".txt");
	          if(!logFile.exists())
	        	  logFile.createNewFile();
	          
	          FileOutputStream fOut = new FileOutputStream(logFile);
              OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
              myOutWriter.write(log.toString());
	          myOutWriter.close();
	          Log.e("VER -- BUILD", sp.getString("APP_VER_NAME", "5.3.0")+" -- "+sp.getInt("APP_VER_CODE", 1));  
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), 2000).show();
		    	
		    }
		
		ArrayList<String> list_files;
		String[] logFiles = null;
		File[] listOfFiles = mFolder.listFiles();
		list_files = new ArrayList<String>(listOfFiles.length);
		
		for (int i = 0; i < listOfFiles.length; i++) {
			list_files.add(listOfFiles[i].getAbsolutePath());
		}
		
		logFiles = list_files.toArray(new String[list_files.size()]);
		Compress compress = new Compress(logFiles, root+"/Logs.zip");
		compress.zip();
		
		//sendEmail("Logs.zip");
		//sendLogToSupport();
		
	}
	
	public void sendDiagnostics(){
		// Device model
		dev_model = android.os.Build.MODEL;

		// Android version
		dev_version = android.os.Build.VERSION.RELEASE;
				
		
		//Write data to text file
		try {
			root = AndroidUtils.ENTRADA_DIR;
			
			
            File myFile = new File(root+"/Diagnostics.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = 
                                    new OutputStreamWriter(fOut);
            myOutWriter.append("Device Model: "+dev_model+"\n"+"Device Version: "+dev_version+"\n"+"Application Version: "+sp.getString("APP_VER_NAME", "5.3.0"));
            myOutWriter.close();
            fOut.close();
            
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
		
		/*
		 * Send Email for Diagnostics Repot
		 */
		sendEmail("Diagnostics.txt");
		
	}
	
	
	/*
	 * Compress to Zip for sending logs to support
	 */
	
	public class Compress { 
		private static final int BUFFER = 2048; 
		private String[] _files; 
		private String _zipFile; 
		public Compress(String[] files, String zipFile) { 
			_files = files; 
		    _zipFile = zipFile;
		}
		
	public boolean zip() { 
	    try  { 
	    	
	      BufferedInputStream origin = null; 
	      FileOutputStream dest = new FileOutputStream(_zipFile); 
	 
	      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest)); 
	 
	      byte data[] = new byte[BUFFER]; 
	      Log.e("_files_Len", Integer.toString(_files.length));
	 
	      for(int i=0; i < _files.length; i++) { 
	        Log.i("Compress", "Adding: " + _files[i]); 
	        FileInputStream fi = new FileInputStream(_files[i]); 
	        origin = new BufferedInputStream(fi, BUFFER); 
	        ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1)); 
	        out.putNextEntry(entry); 
	        int count; 
	        while ((count = origin.read(data, 0, BUFFER)) != -1) { 
	          out.write(data, 0, count); 
	        } 
	        origin.close(); 
	      } 
	      out.close();
	      return true; 
	    } catch(Exception e) { 
	      e.printStackTrace(); 
	      return false; 
	    }
		
	 
	  } 
	}
	
	public class getAllAccountsTask extends DialogTask<Boolean> {

		protected getAllAccountsTask(Activity activity, CharSequence title,
				CharSequence defaultText, boolean isCancelable) {
			super(activity, "Account Settings", "Loading user accounts \nPlease wait...", false);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try
	        {
	            users = User.getUsers();
	            total_user = users.size();
	        }
	        catch (UserLoadException ex)
	        {
	            throw new RuntimeException("Failed to get users: ", ex);
	        }
	        
	        str_arr_acc_names = new ArrayList<Account>(users.size());
	        
	        for(int i = 0;i <total_user; i++){
	        	getAccountForUser(i, false);
	        }
	        
	        
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			this.dialog.dismiss();
			getAccounts();
			super.onPostExecute(result);
		}
		
	}
	
	private void setListViewHeight(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
		View listItem = listAdapter.getView(i, null, listView);
		listItem.measure(0, 0);
		totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight	+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
	
	private void setListViewHeight(ExpandableListView listView,
			int group) {
			ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
			int totalHeight = 0;
			int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
			MeasureSpec.AT_MOST);
			for (int i = 0; i < listAdapter.getGroupCount(); i++) {
			View groupItem = listAdapter.getGroupView(i, false, null, listView);
			groupItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);

			totalHeight += groupItem.getMeasuredHeight();

			if (((listView.isGroupExpanded(i)) && (i != group))
			|| ((!listView.isGroupExpanded(i)) && (i == group))) {
			for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
			View listItem = listAdapter.getChildView(i, j, false, null,
			listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);

			totalHeight += listItem.getMeasuredHeight();

			}
			}
			}

			ViewGroup.LayoutParams params = listView.getLayoutParams();
			int height = totalHeight
			+ (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
			if (height < 10)
			height = 200;
			params.height = height;
			listView.setLayoutParams(params);
			listView.requestLayout();

			}

	getAllAccountsTask accTask;
	public void refreshUserList()
    {
		accTask = new getAllAccountsTask(this, "Getting Accounts", "Getting Accounts", false);
		accTask.execute();
	}
		
		
   
	UserPrivate curUser;

	private void getAccountForUser(int loc, boolean isReload){
		
		String uname = users.get(loc).getDisplayName();
				
		if (uname != null) 
        {
            try
            {            	
                UserState us = AndroidState.getInstance().getUserState();
                if(us == null)
                {
                	startActivity(new Intent(this, PinEntryActivity.class).putExtra(BundleKeys.SELECTED_USER, uname).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                	return;
            	}
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
	            	curUser = User.getPrivateUserInformation(uname, sp.getString("PIN_SAVED", "1111"));
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
		//Get account name associated with the current user
		
		UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            accounts = Lists.newArrayList(state.getAccounts());
            
        }
        if(!isReload){
        	if( ! accounts.isEmpty())
            	str_arr_acc_names.add(state.getCurrentAccount());
        }else{
        	Editor edit = sp.edit();
        	
        	try{
        		edit.putString("sel_acc", state.getCurrentAccount().getDisplayName());
				edit.putString("sel_user", user.getDisplayName());
				edit.commit();
				sel_acc = user.getDisplayName();
        	}catch(IllegalStateException ex){
        		loc = loc + 1;
        		getAccountForUser(loc, true);
        	}
        	
        }
               
    }

	private void getAccounts(){
		
		try
        {
            users = User.getUsers();
            total_user = users.size();
        }
        catch (UserLoadException ex)
        {
            throw new RuntimeException("Failed to get users: ", ex);
        }
		
//        ListAdapter adapter =
//                new AccountListItemAdapter(this, android.R.layout.simple_list_item_1, str_arr_acc_names);
       // updateListViewHeight(lvUsers);
      //Hide Secure Messaging option for PROD User
/*        if(BundleKeys.CURR_ACCOUNT.getApiHost().equalsIgnoreCase("dictateapi.entradahealth.net")){
      		rlSecureMessaging.setVisibility(View.GONE);
      		BundleKeys.SECURE_MSG = true;
      	}else{
      		rlSecureMessaging.setVisibility(View.VISIBLE);
      		BundleKeys.SECURE_MSG = false;
      	}        
*/	}
	
	public static void updateListViewHeight(ListView myListView) {
	     ListAdapter myListAdapter = myListView.getAdapter();
	     if (myListAdapter == null) {            
	              return;
	     }
	    //get listview height
	    int totalHeight = 0;
	    int adapterCount = myListAdapter.getCount();
	    for (int size = 0; size < adapterCount ; size++) {
	        View listItem = myListAdapter.getView(size, null, myListView);
	        listItem.measure(0, 0);
	        totalHeight += listItem.getMeasuredHeight();
	    }
	    //Change Height of ListView 
	    ViewGroup.LayoutParams params = myListView.getLayoutParams();
	    params.height = totalHeight + (myListView.getDividerHeight() * (adapterCount - 1));
	    myListView.setLayoutParams(params);
	}
	
	/*
	 * Get passcode from DB
	 */
	
	public void getPasscode(){
		/*UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());

            try
            {
            	BundleKeys.PASSCODE = writer.getPasscode();
            } catch (DomainObjectWriteException e1)
            {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }
        }*/
        
		BundleKeys.PASSCODE = sp.getInt("PASSCODE", 0);
        BundleKeys.PASSCODE_MINUTES = BundleKeys.passcode_values[BundleKeys.PASSCODE];
        tvPasscodeTime.setText(passcode_names[BundleKeys.PASSCODE]);
        
        ///////////
        Editor edit = sp.edit();
    	edit.putInt("PASSCODE_MINUTES", BundleKeys.PASSCODE_MINUTES);
		edit.commit();
	}
	
	AlertDialog dgInterval;
	private void dgPassCodeInterval(){
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("Settings");
		builder.setCancelable(true);
		builder.setSingleChoiceItems(passcode_names, BundleKeys.PASSCODE, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				BundleKeys.PASSCODE = which;
				Editor edit = sp.edit();
		    	edit.putInt("PASSCODE", BundleKeys.PASSCODE);
				edit.commit();
		        getPasscode();
				dialog.dismiss();
			}
		});
		
		builder.setPositiveButton(R.string.acc_passcode_tag, null);
		dgInterval = builder.create();
		dgInterval.show();
		
		Button theButton = dgInterval.getButton(DialogInterface.BUTTON_POSITIVE);
		theButton.setOnClickListener(new CustomListener(dgInterval));
		
	}
	
	class CustomListener implements View.OnClickListener {
	    private final Dialog dialog;
	    public CustomListener(Dialog dialog) {
	        this.dialog = dialog;
	    }
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
		}
	    
	}
    
	Account acc;
	
	boolean running;
	VerifySwapPinTask pinTask;
	
	public class AccountListItemAdapter extends ArrayAdapter<Account>{
		
		private int selectedIndex;
		
		
		public AccountListItemAdapter(Activity activity, int textViewResourceId, List<Account> str_arr_acc_names)
	    {
	        super(activity, textViewResourceId, str_arr_acc_names);
	        selectedIndex = -1;
	        Editor edit = sp.edit();
			edit.putInt("acc_total", str_arr_acc_names.size());
			edit.commit();
	    }
		
		public void setSelectedIndex(int ind)
        {
        	selectedIndex = ind;
            acc = getItem(ind);
            notifyDataSetChanged();
        }

		boolean isSelected = false;
	    @Override
	    public View getView(final int position, View convertView, ViewGroup parent)
	    {
	        
	        final AccountHolder holder;

	        acc = getItem(position);
	        
	            LayoutInflater inflater = getLayoutInflater();
	            final View row = inflater.inflate(R.layout.user_select_list_item, parent, false);
	            TextView tvName = (TextView)row.findViewById(R.id.UserSelect_DisplayName);
                ImageView ivArrow = (ImageView)row.findViewById(R.id.ivArrow);
                ImageView ivTick = (ImageView)row.findViewById(R.id.ivTick);
	            holder = new AccountHolder(tvName, ivArrow, ivTick);
	            row.setTag(holder);
	            
	            //if(sp.getString("sel_acc", null).equals(acc.getDisplayName()) && BundleKeys.POSITION == position){
	            if(BundleKeys.POSITION == position){
                	ivTick.setVisibility(View.VISIBLE);
                	selectedIndex = position;
                	sel_acc = users.get(position).getName();
                	sel_api = acc.getApiHostFromClinicCode(acc.getClinicCode());
                	row.setTag(R.string.row_tag_02, "true");
                	ivArrow.setTag(R.string.row_tag_02, "true");
                	BundleKeys.CURR_ACCOUNT = acc;
                	
                	BundleKeys.SYNC_FOR_USER = acc.getDisplayName();
					BundleKeys.SYNC_FOR_ACC = acc.getRemoteUsername();
					BundleKeys.SYNC_FOR_CLINIC = acc.getClinicCode(); 
					
					Editor edit = sp.edit();
					edit.putString("SYNC_FOR_ACC", BundleKeys.SYNC_FOR_ACC);
					edit.putString("SYNC_FOR_CLINIC", BundleKeys.SYNC_FOR_CLINIC);
					edit.commit();
                	//isSelected = true;
                }else{
                	ivTick.setVisibility(View.INVISIBLE);
                	ivArrow.setOnClickListener(null);
                	row.setTag(R.string.row_tag_02, "false");
                	ivArrow.setTag(R.string.row_tag_02, "false");
                }
	            
	            ivArrow.setBackgroundResource(R.drawable.btn_round);
            	ivArrow.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						running = false;
						ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			    	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			    	        if ("com.entradahealth.entrada.android.app.personal.sync.SyncService".equals(service.service.getClassName()) ||
			    	        		"com.entradahealth.entrada.android.app.personal.sync.DictationUploadService".equals(service.service.getClassName())) {
			    	            running = true;
			    	        }
			    	    }
						
						if(!running){
							BundleKeys.POSITION = position;
							Editor edit = sp.edit();
				        	edit.putInt("cur_acc_pos", BundleKeys.POSITION);
							edit.commit();
								pinTask = new VerifySwapPinTask(EntradaSettings.this, currentUser, 
										holder.ivTick.getTag(R.string.row_tag_01).toString(), holder.displayNameView.getText().toString(), true);
						        pinTask.execute();
						    
						}else{
							Toast.makeText(EntradaSettings.this,
			                         "Please wait until the current account has finished syncing to edit account.",
			                         Toast.LENGTH_SHORT).show();
						}
						
					}
				});
	        
	        
	        holder.displayNameView.setText(acc.getDisplayName());
	        holder.ivTick.setTag(R.string.row_tag_01, users.get(position).getDisplayName());
	        
	        row.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
    				if(row.getTag(R.string.row_tag_02).toString().equals("true")){
    					//VerifySwapPinTask pinTask = new VerifySwapPinTask(EntradaSettings.this, currentUser, 
								//holder.ivTick.getTag(R.string.row_tag_01).toString(), holder.displayNameView.getText().toString(), false);
				        //pinTask.execute();
					}else{
						running = false;
						ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			    	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			    	        if ("com.entradahealth.entrada.android.app.personal.sync.SyncService".equals(service.service.getClassName()) ||
			    	        		"com.entradahealth.entrada.android.app.personal.sync.DictationUploadService".equals(service.service.getClassName())) {
			    	            running = true;
			    	        }
			    	    }
						
						if(!running){
							acc = getItem(position);
							setSelectedIndex(position);
							BundleKeys.POSITION = position;
							Editor edit = sp.edit();
				        	edit.putInt("cur_acc_pos", BundleKeys.POSITION);
							edit.commit();
							pinTask = new VerifySwapPinTask(EntradaSettings.this, currentUser, 
									holder.ivTick.getTag(R.string.row_tag_01).toString(), holder.displayNameView.getText().toString(), false);
					        pinTask.execute();
						}else{
							Toast.makeText(EntradaSettings.this,
     	                           "Please wait until the current account has finished syncing to switch accounts.",
     	                           Toast.LENGTH_SHORT).show();
						}
					}
				}
			});

	        if(selectedIndex!= -1 && position == selectedIndex)
            {
                holder.ivTick.setVisibility(View.VISIBLE);
            }
            else
            {
            	holder.ivTick.setVisibility(View.INVISIBLE);
            	
            }
	        
	        return row;
	    }

	    public class AccountHolder
	    {
	    	public final TextView displayNameView;
            public final ImageView ivTick;
            public final ImageView ivArrow;

	        public AccountHolder(TextView displayNameView, ImageView ivArrow, ImageView ivTick)
	        {
	        	this.displayNameView = displayNameView;
                this.ivTick = ivTick;
                this.ivArrow = ivArrow;
	        }
	    }
	
	}
	    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		this.j_Menu = menu;
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_account, menu);
    	MenuItem menuItem = j_Menu.findItem(R.id.item_edit_cancel);
    	menuItem.setVisible(false);
        
    	return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		 switch (item.getItemId()) {
         case android.R.id.home:
        	 saveChanges(sel_acc, sp.getString("PIN_SAVED", "1111"));
        	 return true;
        	 
         case R.id.item_edit_done:
        	 saveChanges(sel_acc, sp.getString("PIN_SAVED", "1111"));
        	 return true;
         	 
         default:
             return super.onOptionsItemSelected(item);
		 }
		 
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		if(accTask != null){
    		if(accTask.getDialog() != null && accTask.getDialog().isShowing())
    			accTask.getDialog().dismiss();
    	}
		
		if(pinTask != null){
    		if(pinTask.getDialog() != null && pinTask.getDialog().isShowing())
    			pinTask.getDialog().dismiss();
    	}
		
		if(dialog != null && dialog.isShowing())
        	dialog.dismiss();
		
		if(dgInterval != null && dgInterval.isShowing())
			dgInterval.dismiss();
		
		if(dgLogs != null && dgLogs.isShowing())
			dgLogs.dismiss();
		
		Editor edit = sp.edit();
    	edit.putLong("TIME_START", System.currentTimeMillis());
    	edit.putInt("DAYS_TO_SYNC", days_ct);
    	edit.putInt("MINS_TO_SYNC", mins_ct);
		edit.commit();
		BundleKeys.TIME_START = System.currentTimeMillis();
		//BundleKeys.days_to_sync = days_ct;
		//BundleKeys.mins_to_sync = mins_ct;
		
		//if(!isEmail)
			//finish();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Editor edit = sp.edit();
    	edit.putLong("TIME_START", System.currentTimeMillis());
    	edit.putInt("DAYS_TO_SYNC", days_ct);
    	edit.putInt("MINS_TO_SYNC", mins_ct);
		edit.commit();
		BundleKeys.TIME_START = System.currentTimeMillis();
		//BundleKeys.days_to_sync = days_ct;
		//BundleKeys.mins_to_sync = mins_ct;
		
		//if(!isEmail)
			//finish();
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		//startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	private UserPrivate currentUser;
	String pword;
	public void saveChanges(String username, String pin){
		
		Editor edit = sp.edit();
    	edit.putLong("TIME_START", System.currentTimeMillis());
    	edit.putInt("DAYS_TO_SYNC", days_ct);
    	edit.putInt("MINS_TO_SYNC", mins_ct);
		edit.commit();
		BundleKeys.TIME_START = System.currentTimeMillis();
		//BundleKeys.days_to_sync = days_ct;
		//BundleKeys.mins_to_sync = mins_ct;
		
		SwapAccountTask task = new SwapAccountTask(EntradaSettings.this, null, username, pin);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
        	task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
        	task.execute();
        }
		
		/*Intent intent = new Intent(this, JobListActivity.class);
    	intent.putExtra("qchanged", true);
        this.startActivity(intent);
        this.finish();*/
	}
	
	public class ExpandableListAdapter extends BaseExpandableListAdapter {
		 
	    private Context _context;
	    private List<EUser> _listDataHeader; // header titles
	    // child data in format of header title, child title
	    private HashMap<EUser, List<Dictator>> _listDataChild;
	 
	    public ExpandableListAdapter(Context context, List<EUser> listDataHeader,
	            HashMap<EUser, List<Dictator>> listChildData) {
	        this._context = context;
	        this._listDataHeader = listDataHeader;
	        this._listDataChild = listChildData;
	    }
	 
	    @Override
	    public Object getChild(int groupPosition, int childPosititon) {
	        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
	    }
	 
	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	 
	    @Override
	    public View getChildView(int groupPosition, final int childPosition,
	            boolean isLastChild, View convertView, ViewGroup parent) {
	    	EUser user = (EUser) getGroup(groupPosition);
	        Dictator dictator = (Dictator) getChild(groupPosition, childPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) this._context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.user_dictator_item, null);
	        }
	        if(user.isCurrent()){
	        	convertView.setBackgroundColor(getResources().getColor(R.color.selecteduser_background_color));
	        }
	        TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
	        ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
	        TextView dictatortv = (TextView) convertView.findViewById(R.id.dictator);
	        ImageView editDictator = (ImageView) convertView.findViewById(R.id.editDictator);
	        
	        txtListChild.setText(dictator.getDictatorName());
	        txtListChild.setOnClickListener(new ItemClickListener(user, dictator));
	        arrow.setOnClickListener(new ItemClickListener(user, dictator));
	        dictatortv.setOnClickListener(new ItemClickListener(user, dictator));
	        editDictator.setOnClickListener(new EditClickListener(user,dictator));
	        if(user.isCurrent() && dictator.isCurrent()) {
	        	txtListChild.setTypeface(txtListChild.getTypeface(), Typeface.BOLD);
	        }
	        return convertView;
	    }
	 
	    class ItemClickListener implements OnClickListener{

	    	private EUser user;
	    	private Dictator dictator;
	    	
	    	public ItemClickListener(EUser user, Dictator dictator) {
	    		this.user = user;
	    		this.dictator = dictator;
			}

			@Override
			public void onClick(View v) {
				EnvironmentHandlerFactory factory = EnvironmentHandlerFactory
						.getInstance();
				MainUserDatabaseProvider provider;
				try {
					provider = new MainUserDatabaseProvider(false);
					if (!user.isCurrent()) {
						application.setStringIntoSharedPrefs("environment",
								user.getEnvironment());
						com.entradahealth.entrada.android.app.personal.Environment environment = factory
								.getHandler(user.getEnvironment());
						UserAuthenticate authenticateUserTask = new AuthenticateUser(
								provider, _listDataHeader,
								_listDataChild.get(user),
								String.valueOf(dictator.getDictatorID()),
								dictator.getDictatorName(),
								environment.getApi(), user.getName(),
								user.getPassword(), (Activity) context);
						authenticateUserTask.execute();
					} else {
						state.setCurrentAccount(String.valueOf(dictator.getDictatorID()));
						application.setStringIntoSharedPrefs(
								BundleKeys.DICTATOR_ID,
								String.valueOf(dictator.getDictatorID()));
						application.setStringIntoSharedPrefs(
								BundleKeys.DICTATOR_NAME,
								dictator.getDictatorName());
						for (Dictator _dictator : _listDataChild.get(user)) {
							try {
								if (_dictator.getDictatorID() == Long
										.valueOf(dictator.getDictatorID())) {
									_dictator.setCurrent(true);
								} else {
									_dictator.setCurrent(false);
								}
								provider.updateDictator(_dictator,
										user.getName());
							} catch (DomainObjectWriteException e) {
								e.printStackTrace();
							}
						}
						new LoadUserDictatorsAsyncTask().execute();
					}
				} catch (DomainObjectWriteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    }
	    
	    class EditClickListener implements OnClickListener{

	    	private EUser user;
	    	private Dictator dictator;
	    	
	    	public EditClickListener(EUser user, Dictator dictator){
	    		this.user = user;
	    		this.dictator = dictator;
	    	}
	    	
			@Override
			public void onClick(View v) {
				Account acc = AndroidState.getInstance().getUserState().getCurrentAccount();
            	BundleKeys.CURR_ACCOUNT = acc;
            	BundleKeys.SYNC_FOR_ACC = acc.getRemoteUsername();
            	Editor edit = sp.edit();
				edit = sp.edit();
				edit.putString("SYNC_FOR_ACC", BundleKeys.SYNC_FOR_ACC);
				edit.commit();
                
        		Intent intent = new Intent(context, EditAccountActivity.class);
		        Bundle b = new Bundle();
		        b.putString(BundleKeys.SELECTED_ACCOUNT, String.valueOf(dictator.getDictatorID()));
		        b.putString("selected_user", String.valueOf(user.getName()));
		        UserState us = AndroidState.getInstance().getUserState();
		        curUser = us.getUserData();
		        b.putString("sel_user_name", curUser.getName());
		        intent.putExtras(b);
		        context.startActivity(intent);				
			}
	    	
	    }
	    
	    
	    class GroupItemClickListener implements OnClickListener{

	    	private EUser user;
	    	
	    	public GroupItemClickListener(EUser user) {
	    		this.user = user;
			}

			@Override
			public void onClick(View v) {
				EnvironmentHandlerFactory factory = EnvironmentHandlerFactory
						.getInstance();
				MainUserDatabaseProvider provider;
				try {
					provider = new MainUserDatabaseProvider(false);
					if (!user.isCurrent()) {
						application.setStringIntoSharedPrefs("environment",
								user.getEnvironment());
						com.entradahealth.entrada.android.app.personal.Environment environment = factory
								.getHandler(user.getEnvironment());
						long dictatorId = 0;
						String dictatorName = null; 
						Dictator dictator = provider.getCurrentDictatorForUser(user.getName());
						if(dictator != null){
							dictatorId = dictator.getDictatorID();
							dictatorName = dictator.getDictatorName();
						}
						UserAuthenticate authenticateUserTask = new AuthenticateUser(
								provider, _listDataHeader,
								_listDataChild.get(user),
								String.valueOf(dictatorId),
								dictatorName,
								environment.getApi(), user.getName(),
								user.getPassword(), (Activity) context);
						authenticateUserTask.execute();
					}
				} catch (DomainObjectWriteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    }
	    
	    @Override
	    public int getChildrenCount(int groupPosition) {
	        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
	    }
	 
	    @Override
	    public Object getGroup(int groupPosition) {
	        return this._listDataHeader.get(groupPosition);
	    }
	 
	    @Override
	    public int getGroupCount() {
	        return this._listDataHeader.size();
	    }
	 
	    @Override
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }
	 
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded,
	            View convertView, ViewGroup parent) {
	        EUser user = (EUser) getGroup(groupPosition);
	        if (convertView == null) {
	            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = infalInflater.inflate(R.layout.user_dictator_group, null);
	        }
	        if(user.isCurrent()){
	        	convertView.setBackgroundColor(getResources().getColor(R.color.selecteduser_background_color));
	        }
	        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
	        lblListHeader.setTypeface(null, Typeface.BOLD);
	        lblListHeader.setText(user.getName()+"("+user.getEnvironment()+")");
	        lblListHeader.setOnClickListener(new GroupItemClickListener(user));
	        TextView userText = (TextView) convertView.findViewById(R.id.usertext);
	        userText.setOnClickListener(new GroupItemClickListener(user));
	        lvUsers.expandGroup(groupPosition);
	        return convertView;
	    }
	 
	    @Override
	    public boolean hasStableIds() {
	        return false;
	    }
	 
	    @Override
	    public boolean isChildSelectable(int groupPosition, int childPosition) {
	        return true;
	    }
	}
	
	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub
		super.onTrimMemory(level);
		finish();
	}
}
