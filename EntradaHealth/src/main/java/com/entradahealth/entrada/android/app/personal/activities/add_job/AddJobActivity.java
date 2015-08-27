package com.entradahealth.entrada.android.app.personal.activities.add_job;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.android.app.personal.thirdparty.org.droidparts.widget.ClearableEditText;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;

import java.util.List;

/**
 * Interstitial activity to allow user to select either a generic job or
 * a patient from a searchable job list.
 *
 * @author edr
 * @since 4 April 2013
 */
public class AddJobActivity extends EntradaActivity
{
    Button genericJobButton, btnAddNewJob;
    RelativeLayout rlPlist;
    ClearableEditText searchText;
    ListView patientList;
    TextView searchFeedback, AddJob_PatientHelpText;
    PatientSearchTask searchTask = null;
    List<? extends Patient> patients = null;
    boolean isGeneric = true, isEdit = false;
    public String sel_job_type, sel_appt_time, sel_patient_name, accountName;
    Long jobId;
    
    
    @Override
    protected void onStart()
    {
        super.onStart();

        setContentView(R.layout.add_job);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        sel_job_type = getIntent().getStringExtra("sel_job_type");
        sel_appt_time = getIntent().getStringExtra("sel_appt_time");
        sel_patient_name = getIntent().getStringExtra("sel_patient_name");
        accountName = getIntent().getStringExtra("accountName");
        
        if(isEdit){
        	getActionBar().setTitle("Select Patient");
        	jobId = getIntent().getExtras().getLong("jobId");
        }else{
        	getActionBar().setTitle("Add Job");
        }
        
        final AddJobActivity thisActivity = this;

        long genericPatientId;

        final UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            Account acc = state.getCurrentAccount();
            String value = acc.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
            if (value == null)
            {
                (new AlertDialog.Builder(thisActivity))
                        .setTitle("Sync required first")
                        .setMessage("You need to sync with your clinic before you can add jobs.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                thisActivity.startActivity(new Intent(thisActivity, JobListActivity.class));
                                thisActivity.finish();
                            }
                        })
                        .create().show();

                return;
            }

            genericPatientId = Long.parseLong(value);
        }


        final long genericId = genericPatientId;
        genericJobButton = (Button)findViewById(R.id.AddJob_GenericJob);
        genericJobButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            	isGeneric = true;
            	
                synchronized (state)
                {
                    DomainObjectReader d = state.getProvider(state.getCurrentAccount());
                    Patient p = d.getPatient(genericId);
                    patientSelected(p);
                }
            	
            }
        });
        
        btnAddNewJob = (Button)findViewById(R.id.AddNewJob);
        btnAddNewJob.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BundleKeys.SEL_PATIENT = null;
				Intent i = new Intent();
		        i.setClass(AddJobActivity.this, NewJob.class);
		        startActivity(i);
			}
		});

        searchText = (ClearableEditText)findViewById(R.id.AddJob_SearchText);
        searchText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                doSearch();
            }
        });

        patientList = (ListView)findViewById(R.id.AddJob_PatientList);
        patientList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
            	isGeneric = false;
                Patient p = patients.get(i);
                patientSelected(p);
            }
        });

        searchFeedback = (TextView)findViewById(R.id.AddJob_SearchFeedback);
        AddJob_PatientHelpText = (TextView)findViewById(R.id.AddJob_PatientHelpText);
        
        rlPlist = (RelativeLayout)findViewById(R.id.rlPList);
        if(isEdit){
        	rlPlist.setVisibility(View.VISIBLE);
        	genericJobButton.setVisibility(View.GONE);
        	AddJob_PatientHelpText.setVisibility(View.GONE);
        	btnAddNewJob.setVisibility(View.GONE);
        }else{
        	rlPlist.setVisibility(View.GONE);
        	genericJobButton.setVisibility(View.VISIBLE);
        	AddJob_PatientHelpText.setVisibility(View.GONE);
        	btnAddNewJob.setVisibility(View.VISIBLE);
        }
        
        doSearch();
    }

    @Override
    public void onBackPressed()
    {
    	Intent i = new Intent();
    	if(isEdit){
    		// go back to the New Job Screen
	        i.putExtra("sel_patient_name", sel_patient_name);
	    	i.putExtra("sel_job_type", sel_job_type);
	    	i.putExtra("sel_appt_time", sel_appt_time);
	    	i.putExtra("isEdit", true);
	    	i.setClass(this, NewJob.class);
    	}else{
    		// go back to the job list
    		BundleKeys.SEL_PATIENT = null;
        	i.setClass(this, JobListActivity.class).putExtra("isUploading", true);
    	}
        startActivity(i);
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch (item.getItemId()) {
        	case android.R.id.home:
        		Intent i = new Intent();
            	if(isEdit){
            		// go back to the New Job Screen
        	        i.putExtra("sel_patient_name", sel_patient_name);
        	    	i.putExtra("sel_job_type", sel_job_type);
        	    	i.putExtra("sel_appt_time", sel_appt_time);
        	    	i.putExtra("isEdit", true);
        	    	i.setClass(this, NewJob.class);
            	}else{
            		// go back to the job list
            		BundleKeys.SEL_PATIENT = null;
				i.setClass(this, JobListActivity.class)
						.putExtra("isUploading", false)
						.putExtra("qchanged", true);
            	}
                startActivity(i);
                finish();
            	return true;
        	default:
                return super.onOptionsItemSelected(item);
    	}
    }

    private void doSearch()
    {
        if (searchTask != null) searchTask.cancel(true);

        searchTask = new PatientSearchTask(searchText.getText().toString(), this);
        searchTask.execute();
    }

    private void patientSelected(Patient patient)
    {
        Log.d("Entrada-AddJobActivity", String.format("Patient selected: %s", patient));

        if(isGeneric){
        	AddJobTask task = new AddJobTask(this, patient, isGeneric);
            task.execute();
        }else{
        	Intent i = new Intent();
            BundleKeys.SEL_PATIENT = patient;
        	i.putExtra("sel_job_type", sel_job_type);
        	i.putExtra("sel_appt_time", sel_appt_time);
        	i.putExtra("isEdit", true);
            i.setClass(this, NewJob.class);
            startActivity(i);
            finish();
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
}
