package com.entradahealth.entrada.app.personal.menu_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.core.auth.Account;

public class JobMenuAdapter extends BaseAdapter {
	private String[] navMenuTitles;
	private LayoutInflater inflater;
	private boolean isExpQueues;
	
	public JobMenuAdapter(Context ctx) {
		// TODO Auto-generated constructor stub
		// load slide menu items
        navMenuTitles = ctx.getResources().getStringArray(R.array.nav_drawer_items);
        this.inflater = LayoutInflater.from(ctx);
        
        final UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            Account acc = state.getCurrentAccount();
            isExpQueues = Boolean.parseBoolean(acc.getSetting(AccountSettingKeys.EXPRESS_QUEUES));
        }
	}
	
	
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return navMenuTitles.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View v, ViewGroup arg2) {
		// TODO Auto-generated method stub
		
		if(pos == 0 || pos == 5 || pos == 9){
			v = inflater.inflate(R.layout.nav_drawer_header, null);
			TextView tvTitle = (TextView) v.findViewById(R.id.navmenusection_label);
			tvTitle.setText(navMenuTitles[pos]);
			
		}else{
			
			v = inflater.inflate(R.layout.nav_drawer_section_item, null);
			TextView tvTitle = (TextView) v.findViewById(R.id.navmenuitem_label);
			tvTitle.setText(navMenuTitles[pos]);
			ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcon);
			RelativeLayout rlCount = (RelativeLayout)v.findViewById(R.id.rlCount);
			TextView tvJCount = (TextView) v.findViewById(R.id.jobcount);
			rlCount.setVisibility(View.GONE);
			
			if(pos == 1){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.TODAY_COUNT));
			}else if(pos == 2){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.TOMORROW_COUNT));
			}else if(pos == 3){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.STAT_COUNT));
			}else if(pos == 4){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.ALL_COUNT));
			}else if(pos == 6){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.HOLD_COUNT));
			}else if(pos == 7){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.DELETED_COUNT));
			}else if(pos == 8){
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText(Integer.toString(BundleKeys.COMPLETED_COUNT));
			}else{
				rlCount.setVisibility(View.VISIBLE);
				tvJCount.setText("0");
			}
			
			//Show icon only for Settings row
			if(pos == 10){
				ivIcon.setVisibility(View.VISIBLE);
				rlCount.setVisibility(View.GONE);
			}else if(pos == 11){
				if(!isExpQueues){
					v.setVisibility(View.GONE);
				}else{
					v.setVisibility(View.VISIBLE);
					ivIcon.setVisibility(View.VISIBLE);
					rlCount.setVisibility(View.GONE);
				}
				
			}else{
				ivIcon.setVisibility(View.GONE);
			}
			
			
		}
				
		return v;
		
	}

}
