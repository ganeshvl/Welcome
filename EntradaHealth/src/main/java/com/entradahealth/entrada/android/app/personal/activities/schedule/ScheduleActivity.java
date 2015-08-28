package com.entradahealth.entrada.android.app.personal.activities.schedule;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.entradahealth.entrada.android.R;

/**
 * This Activity holds - schedule screen and functionality.
 *
 */
public class ScheduleActivity extends SherlockActivity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_schedule);
		
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
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragtransacion = fragmentManager.beginTransaction();
		//ft.addToBackStack("schedule");
		fragtransacion.add(R.id.fragment_container, scheduleFragment, "schedule").commit();
		
	}

	@Override
	public void onClick(View v) {
		
	}
}
