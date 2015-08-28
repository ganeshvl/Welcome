package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.entradahealth.entrada.android.R;
import com.viewpagerindicator.IconPagerAdapter;

public class PatientInfoViewPagerAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {
	protected static final String[] CONTENT = new String[] { "Welcome", "Erada", "Wait..", "Thank you", };
	public static final int MEDICATIONS=1,ALLERGIES=2,PROBLEMS=3,PAST_MEDICAL=4,LAST_HPI=5;
	private static String[] clinicalTypeArr = {Constants.MEDICATION, Constants.ALLERGIES, Constants.PROBLEMS, Constants.MEDSURGHX, Constants.PREVIOUSHPI};
	protected static final int[] ICONS = new int[] {
		R.drawable.ab_item_settings,
		R.drawable.ab_item_settings,
		R.drawable.ab_item_settings,
		R.drawable.ab_item_settings
	};

	private int mCount = CONTENT.length;

	private String result;String[] args;
	private JSONObject clinicalsJS;
	private JSONObject demographicsJS;
	private List<String> clinicalTypes;
	private String demographics;

	public PatientInfoViewPagerAdapter(FragmentManager fm,String jsonResponse,String... args) {
		super(fm);
		this.result=jsonResponse;
		this.args=args;
		try {
			clinicalsJS = new JSONObject(jsonResponse);
			clinicalTypes = new ArrayList<String>();
			for(int j=0;j< clinicalTypeArr.length; j++) {
				for(int i=0;i< clinicalsJS.names().length();i++){
					if(clinicalTypeArr[j].equalsIgnoreCase(clinicalsJS.names().getString(i))){
						clinicalTypes.add(clinicalTypeArr[j]);
						break;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PatientInfoViewPagerAdapter(String demographics, FragmentManager fm,String jsonResponse,String... args) {
		super(fm);
		this.result=jsonResponse;
		this.demographics = demographics;
		this.args=args;
		try {
			clinicalsJS = new JSONObject(jsonResponse);
			demographicsJS = new JSONObject(demographics);
			clinicalTypes = new ArrayList<String>();
			if(demographics!=null){
				clinicalTypes.add(Constants.DEMOGRAPHICS);
			}
			for(int j=0;j< clinicalTypeArr.length; j++) {
				for(int i=0;i< clinicalsJS.names().length();i++){
					if(clinicalTypeArr[j].equalsIgnoreCase(clinicalsJS.names().getString(i))){
						clinicalTypes.add(clinicalTypeArr[j]);
						break;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Fragment getItem(int position) {
		return PatientInfoFragment.newInstance(result, demographics, position, clinicalTypes.get(position));
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return PatientInfoViewPagerAdapter.CONTENT[position % CONTENT.length];
	}

	@Override
	public int getIconResId(int index) {
		return ICONS[index % ICONS.length];
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}