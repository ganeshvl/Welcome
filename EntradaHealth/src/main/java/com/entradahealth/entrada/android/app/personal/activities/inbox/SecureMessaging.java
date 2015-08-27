package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.io.IOException;

import org.acra.ACRA;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.inbox.service.NewConversationBroadcastService;

public class SecureMessaging extends FragmentActivity {

	// Storage for camera image URI components
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;
    private String m_androidId;
    private Intent intent;
    private static SecureMessaging activity;
    boolean fromDemographics = false;
    public static SecureMessaging getInstance(){
    	return activity; 
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sec_msg);
		activity = this;
		//Get Android ID
		m_androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		//Check if TOU is accepted and the current TOUVersion
		//getTOU();
		intent = new Intent(this, NewConversationBroadcastService.class);
		fromDemographics = getIntent().getBooleanExtra("fromDemographics", false);
		if(fromDemographics){
			String patient_name = getIntent().getStringExtra("patient_name");
			long patient_id = getIntent().getLongExtra("patient_id", 0);
			Bundle b = new Bundle();
			b.putString("patient_name", patient_name);
			b.putLong("patient_id", patient_id);
			b.putBoolean("fromRecordingScreen", true);
			SMContacts contactsFrag = new SMContacts();
			contactsFrag.setArguments(b);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.fragcontent, contactsFrag, "contacts");
			ft.commit();
			
		}else{
		InboxFragment inboxFragment = new InboxFragment();
		getFragmentManager().beginTransaction().replace(R.id.fragcontent, inboxFragment, "inbox").commit();
	}
	
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startService(intent);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		stopService(intent);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		int sup_count = getSupportFragmentManager().getBackStackEntryCount();
		int count = getFragmentManager().getBackStackEntryCount();
		if(sup_count>0){
			getSupportFragmentManager().popBackStack();	
		} else {
			if (count == 0) {
				super.onBackPressed();
				SharedPreferences sp = getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
				UserPrivate curUser = null;
				try {
					curUser = User.getPrivateUserInformation(sp.getString("sel_user", "User_1"), sp.getString("PIN_SAVED", "1111"));
	    			String currentJob = curUser
	    					.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID);
					long jobId = Long.parseLong(currentJob);
					Intent intent = null;
					String accountName = curUser
							.getStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT);
			   		Job j = AndroidState.getInstance().getUserState()
							.getProvider(accountName).getJob(jobId);
			   		
			   		if (j != null) {
			   			BundleKeys.IS_DIRTY = true;// mark job as dirty in
						// case of data loss
						intent = new Intent(activity,
							JobDisplayActivity.class);
						Bundle b = new Bundle();
						intent.putExtra("isDeleted", true);
						intent.putExtra("isModified", true);
						b.putBoolean("isFirst", false);
						intent.putExtra("isFromList", true);
						b.putLong(BundleKeys.SELECTED_JOB, jobId);
						b.putString(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
						boolean isInterrupted = sp.getBoolean("IS_INTERRUPTED", false);
						b.putBoolean("interrupted", isInterrupted);
						intent.putExtras(b);
			   		
					}else {
						intent = new Intent(activity,
								JobListActivity.class);
					}
			   		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(intent);
				}catch (Exception e) {
	                ACRA.getErrorReporter().handleSilentException(e);
				} finally {
					curUser.setStateValue(
							JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
					curUser.setStateValue(
							JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, null);

					try {
	                    curUser.save();
					} catch (IOException e) {
	                    ACRA.getErrorReporter().handleSilentException(e);
	                }
	            }
				
				//startActivity(new Intent(this, JobListActivity.class));
			} else {
				getFragmentManager().popBackStack();
			}
		}
	}
	
	/**
     * Getters and setters.
     */

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    public Uri getCapturedImageURI() {
        return mCapturedImageURI;
    }

    public void setCapturedImageURI(Uri mCapturedImageURI) {
        this.mCapturedImageURI = mCapturedImageURI;
    }
    
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
//		finish();
	}

}
