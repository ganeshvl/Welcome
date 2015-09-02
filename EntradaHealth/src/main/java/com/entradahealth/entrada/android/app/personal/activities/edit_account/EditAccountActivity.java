package com.entradahealth.entrada.android.app.personal.activities.edit_account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.acra.ACRA;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.manage_queues.ManageQueuesActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.android.app.personal.utils.TestConnectionTask;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.files.FileUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The U in CRUD.
 *
 * @author edr
 * @since 14 Jan 2013
 */

public class EditAccountActivity extends EntradaActivity
{
	private static final String LOG_NAME = "Edit-Account";
    Account editingAccount = null;
    Queue queue;
    DomainObjectProvider provider = null;
    SharedPreferences sp;
    int acc_total, qCounter = 0;
    List<Queue> queues = null;
    Button btnDelete;
    EditText etdispName, etAccUsername, etAccPassword, etClinicCode, etClinicName;
    EditText etdictatorName, etuserName;
    EditText etMRN, etLastname, etFirstname;
    RelativeLayout rlJobType;
    TextView tvSelQueue, tvEditQueue, tvExpressQueues, tvClearJobs, tvJobType;
    ImageView ivArrow;
    boolean hasDispName = false, hasCCode = false, hasRName = false, hasPWord = false, isCurrent = false, isQueueLoaded = false;
    String accountName, value, sel_user_name, selectedusername, jobtypeId;
    LinearLayout llQueues; 
    Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.edit_account);
    	
    	sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    	acc_total = sp.getInt("acc_total", 0);
    	sel_user_name = getIntent().getStringExtra("sel_user_name");
    	Log.e("sel_user_name", sel_user_name);    	
    	
    	context = this;
    	accountName = getIntent().getStringExtra(BundleKeys.SELECTED_ACCOUNT);
    	selectedusername = getIntent().getStringExtra("selected_user");
    	
    	etdispName = (EditText)findViewById(R.id.displayNameText);
    	etAccUsername = (EditText)findViewById(R.id.accountUsernameText);
    	etAccPassword = (EditText)findViewById(R.id.accountPasswordText);
    	etClinicCode = (EditText)findViewById(R.id.clinicCodeText);
    	
    	etdictatorName = (EditText)findViewById(R.id.dictatorName);
    	etuserName = (EditText) findViewById(R.id.userName);
    	etClinicName = (EditText) findViewById(R.id.clinicName);
    	
    	btnDelete = (Button)findViewById(R.id.btnDelete);   
    	btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(acc_total > 1)
					deleteUserMenuItemSelected(true);
				else
					deleteUserMenuItemSelected(false);
			}
		});
    	
    	etMRN = (EditText)findViewById(R.id.etMRN);
    	etLastname = (EditText)findViewById(R.id.etLastname);
    	etFirstname = (EditText)findViewById(R.id.etFirstname);
    	
    	tvSelQueue = (TextView)findViewById(R.id.tvSelQueue);
    	tvEditQueue = (TextView)findViewById(R.id.tvEditQueue);
    	tvEditQueue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//Check if there are any held jobs or any uploads in progress
				boolean isHeld = true, isPending = true;
		        try
		        {
		            List<Job> jobs = AndroidState.getInstance().getUserState()
		                                         .getProvider(editingAccount).getJobs();
		
		            for (Job j : jobs)
		            {
		                if (j.isPending()) isPending = false;
		                if(j.isFlagSet(Job.Flags.HOLD)) isHeld = false;
		                
		            }
		        }
		        catch (Exception ex)
		        {
		            ACRA.getErrorReporter().handleSilentException(ex);
		            isPending = false;
		            isHeld = false;
		        }
		
		        
				if(isPending && isHeld){
					Intent qIntent = new Intent(EditAccountActivity.this, ManageQueuesActivity.class);
					qIntent.putExtra("from_settings", true);
					qIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(qIntent);
					
				}else if(!isHeld){
					AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
					builder.setTitle(R.string.del_acc_err_title);
					builder.setMessage(R.string.manage_q_hold_error);
					builder.setPositiveButton("OK", null);
					builder.setCancelable(true);
			        builder.show();
				}else if(!isPending){
					AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
					builder.setTitle(R.string.del_acc_err_title);
					builder.setMessage(R.string.manage_q_upload_error);
					builder.setPositiveButton("OK", null);
					builder.setCancelable(true);
			        builder.show();
				}
				
			}
		});
    	
    	tvClearJobs = (TextView)findViewById(R.id.tvClearJobs);
    	tvClearJobs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//check if there are any jobs onhold
				boolean canClear = true;
				List<Job> jobs = AndroidState.getInstance().getUserState()
                        .getProvider(editingAccount).getJobs();

				for (Job j : jobs)
				{
					if(j.isFlagSet(Job.Flags.HOLD) && !j.isFlagSet(Job.Flags.LOCALLY_DELETED) || (!BundleKeys.SYNC_AFTER_DELETE))
						canClear = false;	
				}
				
				if(canClear){
					UserState state = AndroidState.getInstance().getUserState();
					DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());
		        	try {
		        			writer.clearJobs();
							AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
					        builder.setTitle(R.string.title_success);
					        builder.setMessage(R.string.clear_all_jobs);
					        builder.setPositiveButton("OK", null);
					        builder.show();
					        BundleKeys.IS_CLEAR = true;
				            //BundleKeys.IS_FIRST_SYNC = true;
				            Log.e(LOG_NAME, "Cleared all jobs successfully");
				            
				            
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e(LOG_NAME, "Clear all jobs failed due to some error");
						AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
				        builder.setTitle(R.string.title_error);
				        builder.setMessage(R.string.clear_all_jobs_error);
				        builder.setPositiveButton("OK", null);
				        builder.show();
					}
				}else{
					Log.e(LOG_NAME, "Clear all jobs failed due to local changes");
					AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
			        builder.setTitle(R.string.title_error);
			        builder.setMessage(R.string.clear_jobs_error);
			        builder.setPositiveButton("OK", null);
			        builder.show();
				}
			}
		});
    	
    	tvJobType = (TextView)findViewById(R.id.tvNewJobtype);
    	rlJobType = (RelativeLayout)findViewById(R.id.rlJobtype);
		rlJobType.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(jobtypeId != null) {
					dgJobType();
				} else {
					Toast.makeText(context, "You need to sync account, to select Job Type", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	llQueues = (LinearLayout)findViewById(R.id.llQueues);
    	tvExpressQueues = (TextView)findViewById(R.id.tvExpressQueues);
    	//expressQueuesInfo();
    }
    
	public void dgJobType() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Job type");
		builder.setCancelable(false);

		List<JobType> myJobTypes = new ArrayList<JobType>();
		myJobTypes.addAll(provider.getDefaultGenericJobTypes());
		final JobTypeAdapter adap = new JobTypeAdapter(this, myJobTypes);

		builder.setAdapter(adap, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					JobType sel_jtype = adap.getItem(which);
					tvJobType.setText(sel_jtype.name);
					editingAccount.putSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID, String.valueOf(sel_jtype.id));
					state.getUserData().save();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		builder.show();
	}
    
    private UserState state;
    private DomainObjectReader reader;
    private String GENERIC_PATIENT_ID;
    Patient genericPatient;

	@Override
	protected void onStart() {
		super.onStart();

		ActionBar ab = getActionBar();
		ab.setTitle("Account Settings");
		ab.setDisplayHomeAsUpEnabled(true);
		state = AndroidState.getInstance().getUserState();
		BundleKeys.CURR_ACCOUNT = editingAccount;
		if(editingAccount == null){
			state.addAccount(accountName);
			editingAccount = state.getAccount(accountName);
		}
		editingAccount = state.getAccount(accountName);
		jobtypeId = editingAccount.getSetting(AccountSettingKeys.DEFAULT_JOBTYPE_ID);
		if(jobtypeId != null) {
			JobType jType = AndroidState.getInstance().getUserState()
	                .getProvider(editingAccount).getJobType(Long.valueOf(jobtypeId));
			if(jType!=null) {
				tvJobType.setText(jType.name);
			} else {
				tvJobType.setText("None Selected");
			}
		} else {
			tvJobType.setText("None Selected");
		}
		etdispName.setText(editingAccount.getDisplayName());
		etAccUsername.setText(editingAccount.getRemoteUsername());
		etAccPassword.setText(editingAccount.getRemotePassword());
		etClinicCode.setText(editingAccount.getClinicCode());

		etdictatorName.setText(editingAccount.getDisplayName());
		etuserName.setText(selectedusername);
		etClinicName.setText(editingAccount.getClinicCode());		
		
		state = AndroidState.getInstance().getUserState();
        reader = state.getProvider(editingAccount);
        
        //if(!BundleKeys.IS_FIRST_SYNC){
	        GENERIC_PATIENT_ID = editingAccount.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
	        if(GENERIC_PATIENT_ID != null){
	        	genericPatient = reader.getPatient(Long.parseLong(GENERIC_PATIENT_ID));
	        	if(genericPatient != null)
	        	BundleKeys.LOCAL_MRN = genericPatient.medicalRecordNumber;
	        	//important = Arrays.asList(BundleKeys.LOCAL_MRN);
	        }
        //}
        
        /*String GENERIC_PATIENT_ID = editingAccount.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
        Patient genericPatient = reader.getPatient(Long.parseLong(GENERIC_PATIENT_ID));
        String MRN = genericPatient.medicalRecordNumber;*/
        
        //etMRN.setText(BundleKeys.LOCAL_MRN);
        if(BundleKeys.LOCAL_MRN.equals(""))
        	BundleKeys.LOCAL_MRN = "999999";
        etMRN.setText(BundleKeys.LOCAL_MRN);
        etLastname.setText(BundleKeys.LOCAL_LASTNAME);
        etFirstname.setText(BundleKeys.LOCAL_FIRSTNAME);
        
        etMRN.setKeyListener(null);
        etLastname.setKeyListener(null);
        etFirstname.setKeyListener(null);
       
        if(!isQueueLoaded)
        expressQueuesInfo();
            
    }
    
    public void expressQueuesInfo(){
    	isQueueLoaded = true;
    	 if(Boolean.parseBoolean(editingAccount.getSetting(AccountSettingKeys.EXPRESS_QUEUES))){
         	tvExpressQueues.setVisibility(View.GONE);
         	llQueues.setVisibility(View.VISIBLE);
         	provider = AndroidState.getInstance().getUserState().getProvider(editingAccount);
             queues = Lists.newArrayList(provider.getQueues());
             Collections.sort(queues);
             
             for(int i = 0;i<queues.size();i++){
             	queue = queues.get(i);
             	if(queue.isSubscribed){
             		BundleKeys.SEL_QUEUE = queue.name;
             		
             		//Add TextView dynamically for each queue selected
             		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
             		TextView tvQName = new TextView(this);
             		tvQName.setLayoutParams(linearParams);
             		tvQName.setPadding(8, 8, 8, 8);
             		tvQName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.size_content));
             		tvQName.setText(queue.name);
             		llQueues.addView(tvQName);
             		qCounter++;
             		
             		//break;
             	}else{
             		BundleKeys.SEL_QUEUE = "No Queue Selected";
             	}
             		
             }
             
             //Add Edit Queue Textview
             LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
     		TextView tvQName = new TextView(this);
     		tvQName.setLayoutParams(linearParams);
     		tvQName.setPadding(8, 8, 8, 8);
     		tvQName.setGravity(Gravity.CENTER);
     		tvQName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.size_content));
     		tvQName.setText(R.string.queues_edit);
     		llQueues.addView(tvQName);
     		tvQName.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					//Check if there are any held jobs or any uploads in progress
 					boolean isHeld = true, isPending = true;
 			        try
 			        {
 			            List<Job> jobs = AndroidState.getInstance().getUserState()
 			                                         .getProvider(editingAccount).getJobs();
 			
 			            for (Job j : jobs)
 			            {
 			                if (j.isPending()) isPending = false;
 			                if(j.isFlagSet(Job.Flags.HOLD)) isHeld = false;
 			                
 			            }
 			        }
 			        catch (Exception ex)
 			        {
 			            ACRA.getErrorReporter().handleSilentException(ex);
 			            isPending = false;
 			            isHeld = false;
 			        }
 			
 			        
 					if(isPending && isHeld){
 						Intent qIntent = new Intent(EditAccountActivity.this, ManageQueuesActivity.class);
 						qIntent.putExtra("from_settings", true);
 						qIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 						startActivity(qIntent);
 		                finish();
 						
 					}else if(!isHeld){
 						AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
 						builder.setTitle(R.string.del_acc_err_title);
 						builder.setMessage(R.string.manage_q_hold_error);
 						builder.setPositiveButton("OK", null);
 						builder.setCancelable(true);
 				        builder.show();
 					}else if(!isPending){
 						AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this);
 						builder.setTitle(R.string.del_acc_err_title);
 						builder.setMessage(R.string.manage_q_upload_error);
 						builder.setPositiveButton("OK", null);
 						builder.setCancelable(true);
 				        builder.show();
 					}
 				}
 			});
             
             if(qCounter == 0){
             	tvSelQueue.setText(BundleKeys.SEL_QUEUE);
             }else{
             	tvExpressQueues.setVisibility(View.GONE);
             	llQueues.setVisibility(View.VISIBLE);
             }
         }else{
         	llQueues.setVisibility(View.GONE);
         	tvExpressQueues.setVisibility(View.VISIBLE);
         }
    }

    protected void saveDictatorName(){
    	try {
    		if(!editingAccount.getDisplayName().equals(etdictatorName.getText().toString())) {
		    	MainUserDatabaseProvider provider = new MainUserDatabaseProvider(false);
		    	provider.updateDictatorName(Long.valueOf(accountName), etdictatorName.getText().toString());
		    	UserPrivate up = AndroidState.getInstance().getUserState().getUserData();
		    	editingAccount.setDisplayName(etdictatorName.getText().toString());
		    	up.save();
    		}
    	} catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    
    @Override
    public void onBackPressed()
    {
    	startActivity(new Intent(EditAccountActivity.this, EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void goBack()
    {
    	Intent intent = new Intent(this, EntradaSettings.class);
        intent.putExtra("deleted", true);
        intent.putExtra("del_accountName", sel_user_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_account, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch (item.getItemId()) {
        case android.R.id.home:
            startActivity(new Intent(EditAccountActivity.this, EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
       	 return true;
       	 
        case R.id.item_edit_done:
        	saveDictatorName();
        	
        	startActivity(new Intent(this, EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        	return true;
       	 
        case R.id.item_edit_cancel:
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
		startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
		//finish();
    }
    boolean isChanged = false;
    private void saveSettings() {
		// TODO Auto-generated method stub
		if(etdispName.getText().toString().isEmpty()){
			etdispName.setError("Display required");
		}else if(etAccUsername.getText().toString().isEmpty()){
			etAccUsername.setError("Remote username required");
		}else if(etAccPassword.getText().toString().isEmpty()){
			etAccPassword.setError("Remote password required");
		}else if(etClinicCode.getText().toString().isEmpty()){
			etClinicCode.setError("Clinic code required");
		} else if (etMRN.getText().toString().isEmpty()) {
			etMRN.setError("MRN required");
		}else if(etLastname.getText().toString().isEmpty()){
			etLastname.setError("Lastname required");
		}else if(etFirstname.getText().toString().isEmpty()){
			etFirstname.setError("Firstname required");
		} else {
			
			String displayName = etdispName.getText().toString();
	        String username = etAccUsername.getText().toString();
	        String password = etAccPassword.getText().toString();
	        String clinicCode = etClinicCode.getText().toString();
	        String apiHost = Account.getApiHostFromClinicCode(clinicCode);
	        
	        if(sp.getString("sel_acc", null).equals(editingAccount.getDisplayName())){
	        	isCurrent = true;
	        }else{
	        	isCurrent = false;
	        }
	        
	        //Check Account details only if a new clinic code or remote username or remote password are different than already entered
	        
	        if(!editingAccount.getClinicCode().equalsIgnoreCase(clinicCode.trim()) ||
	        		!editingAccount.getRemoteUsername().equalsIgnoreCase(username.trim()) ||
	        		!editingAccount.getRemotePassword().equalsIgnoreCase(password.trim())){

	        	UserState state = AndroidState.getInstance().getUserState();
				DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());
				try {
					writer.clearJobs();
					writer.clearReadonlyItems();
					
		    	} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				BundleKeys.ACC_EDITED = true;
	        	//BundleKeys.IS_FIRST_SYNC = true;
	            //BundleKeys.GOT_QUEUES = false;
	            BundleKeys.IS_CLEAR = false;
	        	TestConnectionTask task = new TestConnectionTask(this, apiHost, displayName, clinicCode, username, password, "", "", true, isCurrent, true);
	        	//task.execute();
	     	}else{
	     		TestConnectionTask task = new TestConnectionTask(this, apiHost, displayName, clinicCode, username, password, "", "", true, isCurrent, false);
	        	//task.execute();
	     	}
	        
	        
		}
	}

	
    private ImmutableList<User> users = null;
    File UserPath;
    void deleteUserMenuItemSelected(final boolean allow)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        if(allow){
        	builder.setTitle(R.string.del_acc_title);
            builder.setMessage(R.string.del_acc_msg);
        }else{
        	builder.setTitle(R.string.del_acc_err_title);
            builder.setMessage(R.string.del_acc_err_msg);
            Log.e(LOG_NAME, "Delete account not allowed");
        }
        
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
            	if(allow){
            		
	                UserState state = AndroidState.getInstance().getUserState();
	                synchronized (state)
	                {
	                    try
	                    {
	                        state.getUserData().deleteAccount(editingAccount);
	                        Log.e(LOG_NAME, "Delete account successfull for: "+editingAccount.getRemoteUsername()+"/"+editingAccount.getClinicCode());
	                    }
	                    catch (Exception ex)
	                    {
	                        Log.d("Entrada-DeleteUser", "Error deleting user.", ex);
	                        Toast.makeText(EditAccountActivity.this,
	                                       "Problem deleting account: " + ex.toString(),
	                                       Toast.LENGTH_LONG).show();
	                    }
	                }
	                String sdcard_state = Environment.getExternalStorageState();
	                if (Environment.MEDIA_MOUNTED.equals(sdcard_state)) {
            	    	UserPath = new File(User.getUserRoot(), sel_user_name);
                    }
	                Log.e("UserPath", UserPath.toString());
	                try {
						boolean deleted = FileUtils.deleteRecursive(UserPath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    goBack();
                    
            	}else{
            		dialogInterface.dismiss();
            	}
            }
        });
        builder.setCancelable(true);

        builder.show();
    }
    
    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }
    
    
    /*
	 * Adapter class for job types in New Job
	 */
	
	public class JobTypeAdapter extends ArrayAdapter<JobType>{
		
		public JobTypeAdapter(EditAccountActivity _activity, List<JobType> objects)
	    {
	        super(_activity, android.R.layout.simple_spinner_item, objects);
	     
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View row = convertView;
	        JobTypeHolder holder;

	        if(row == null)
	        {
	            LayoutInflater inflater = getLayoutInflater();
	            row = inflater.inflate(R.layout.simple_spinner_item, parent, false);

	            TextView nameView = (TextView)row.findViewById(R.id.SimpleSpinnerItem_Text);

	            holder = new JobTypeHolder(nameView);

	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (JobTypeHolder)row.getTag();
	        }

	        holder.jobType = getItem(position);
	        holder.nameView.setText(holder.jobType.name);

	        return row;
		}

		public class JobTypeHolder
	    {
	        public final TextView nameView;

	        public JobType jobType;


	        public JobTypeHolder(TextView nameView)
	        {
	            this.nameView = nameView;
	        }
	    }
		
	}
}
