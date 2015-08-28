package com.entradahealth.entrada.android.app.personal.activities.job_type;

import com.entradahealth.entrada.android.app.personal.BundleKeys;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class JobTypeViewAdapter extends FragmentPagerAdapter{

	protected static final String[] CONTENT = new String[] { "This", "Is", "A", "Test", };
	private int mCount = BundleKeys.myJobTypes.size();
	
	public JobTypeViewAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int pos) {
		// TODO Auto-generated method stub
		return new JobTypeFragment(BundleKeys.myJobTypes.get(pos), pos);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCount;
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
      return JobTypeViewAdapter.CONTENT[position % CONTENT.length];
    }

   
    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
    
}
