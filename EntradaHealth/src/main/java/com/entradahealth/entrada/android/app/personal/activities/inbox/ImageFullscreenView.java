package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.io.File;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
//import android.support.v4.view.ViewPager.PageTransformer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ImageFullscreenView extends Fragment implements OnPageChangeListener{

	String TAG = "FullScreen-View";
	Account currentAccount = null;
	private String accountName = null;
	File accountPath;
	File[] acct_folders, recipient_folder, chat_folder, img_folder, imgs;
	ViewPager mViewPager;
	CustomPagerAdapter mCustomPagerAdapter;
	String recipient_name, patient_name, conversationId;
	int index, new_index;
	String[] arr_img_paths;
	private String attachmentId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setTitle("Choose Images");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle bundle = this.getArguments();
		if(bundle != null){
			index = bundle.getInt("index");
			patient_name = bundle.getString("patient_name"); 
			recipient_name = bundle.getString("recipient_name");
			conversationId = bundle.getString("conversationId");
			attachmentId = bundle.getString("attachmentId");
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.img_fullscreen, container, false);
		mViewPager = (ViewPager) view.findViewById(R.id.viewPagerFullscreen);
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		UserState state = AndroidState.getInstance().getUserState();
        synchronized (state) {
     	   currentAccount = state.getCurrentAccount();
        }
		accountName = currentAccount.getName();
		state = AndroidState.getInstance().getUserState();

		String path = User.getUserRoot()
				+ "/"
				+ application
						.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)
				+ "/" + conversationId + "/images";
		imgs = new File(path).listFiles();
		for (int j = 0; j < imgs.length; j++) {
			arr_img_paths = new String[imgs.length];
			for (int k = 0; k < imgs.length; k++) {
				Log.e("img" + k, imgs[k].getAbsolutePath());
				arr_img_paths[k] = imgs[k].getAbsolutePath();
				if(attachmentId!=null){
					if(arr_img_paths[k].contains(attachmentId)){
						index =k; 
					}
				}
			}
		}
						
        mViewPager.setOffscreenPageLimit(0);
		mCustomPagerAdapter = new CustomPagerAdapter(getActivity(), arr_img_paths);
		mViewPager.setAdapter(mCustomPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
		String acTitle = (index+1) + " of "+imgs.length;
		getActivity().getActionBar().setTitle(acTitle);
		mViewPager.setCurrentItem(index);
		
//		mViewPager.setPageTransformer(false, new PageTransformer() {
//			
//			@Override
//			public void transformPage(View page, float position) {
//				// TODO Auto-generated method stub
//				final float normalizedposition = Math.abs(Math.abs(position) - 1);
//			    page.setScaleX(normalizedposition / 2 + 0.5f);
//			    page.setScaleY(normalizedposition / 2 + 0.5f);
//			}
//		});
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//on Fragment resume set the current pos of pager as index
		index = new_index;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	getFragmentManager().popBackStackImmediate();
	    	break;
		}
	    	
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int pos, float arg1, int arg2) {
		// TODO Auto-generated method stub
		String acTitle = (pos+1) + " of "+imgs.length;
		getActivity().getActionBar().setTitle(acTitle);
		new_index = pos;
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}
