package com.entradahealth.entrada.android.app.personal.activities.schedule;

import org.acra.ACRA;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.pin_entry.PinEntryFragment;

/**
 * This Activity holds - schedule screen and functionality.
 *
 */
public class ScheduleActivity extends FragmentActivity implements OnClickListener{
	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_schedule);
		sp = getSharedPreferences("Entrada", Context.MODE_WORLD_READABLE);
		if(savedInstanceState == null){
			addFragment();
		}
	}

	/**
	 * This method adds fragment to Activity.
	 * @param fragment
	 */
	private void addFragment() {
		
		ScheduleFragment scheduleFragment = new ScheduleFragment();
		FragmentTransaction fragtransacion = getSupportFragmentManager().beginTransaction();
		//ft.addToBackStack("schedule");
		fragtransacion.add(R.id.fragment_container, scheduleFragment, "schedule").commit();
		
	}

	@Override
	public void onClick(View v) {
	}
	@Override
	public void onBackPressed() {
		
		int sup_count = getSupportFragmentManager().getBackStackEntryCount();
		int count = getFragmentManager().getBackStackEntryCount();
		if(sup_count>0){
			android.support.v4.app.Fragment demoFragment = getSupportFragmentManager().findFragmentByTag("demographic");
			if(demoFragment==null){
				finish();
			}else{
				android.support.v4.app.Fragment pinFragment = getSupportFragmentManager().findFragmentByTag("pin");
				if(pinFragment!=null){
					finish();
				}else{
					getSupportFragmentManager().popBackStack();	
				}
			}
		} else {
			if (count == 0) {
				super.onBackPressed();
				startActivity(new Intent(this, JobListActivity.class));
				finish();
			}else{
				android.app.Fragment pinFragment = getFragmentManager().findFragmentByTag("demographic");
				if(pinFragment==null){
					getFragmentManager().popBackStack();
				}
			}
		}
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		if(!BundleKeys.CAPTURE_IMAGE) {
			BundleKeys.cur_uname = sp.getString("CUR_UNAME", null);
			Bundle b = new Bundle();
			b.putString(BundleKeys.SELECTED_USER,
					sp.getString("sel_user", null));
			PinEntryFragment pinFragment = new PinEntryFragment();
			pinFragment.setArguments(b);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction().addToBackStack(null);
			ft.replace(android.R.id.content, pinFragment, "pin");
			ft.commitAllowingStateLoss();
		} else {
			BundleKeys.CAPTURE_IMAGE = false;
		}
	}
}
