package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.adapters.ParticipantsGroupListAdapter;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;

public class ParticipantsGroupInfoFragment extends Fragment{
	
	private SecureMessaging activity;
	private View mCustomView;
	private ListView participantsList;
	private ENTConversation conversation;
	private List<ENTUser> participants;
	private String patientName;
	private EntradaApplication application;
	private int count;

	public ParticipantsGroupInfoFragment(ENTConversation conversation, String patientName){
		this.conversation = conversation;
		this.patientName = patientName;
		application = (EntradaApplication) EntradaApplication.getAppContext();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activity = (SecureMessaging) getActivity();
		String[] occupants = conversation.getOccupantsIds();
		occupants = removeElements(conversation.getOccupantsIds(), application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID));
		participants = new ArrayList<ENTUser>();
		for (String occupant : occupants) {
			participants.add(getENTUserFromId(occupant));	
		}
		Collections.sort(participants);
		participants.add(getENTUserFromId(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))); // Self
		count = participants.size();
		participants.add(new ENTUser("-1","")); // Empty item 
		LayoutInflater mInflater = LayoutInflater.from(activity);
		mCustomView = mInflater.inflate(R.layout.acbar_grpinfo, null);
		TextView tvabTitle = (TextView) mCustomView.findViewById(R.id.tvabTitle);
		tvabTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int count = getFragmentManager().getBackStackEntryCount();
				if (count > 0) {
					activity.getFragmentManager().popBackStack();
				}
			}
		});

	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        activity.getActionBar().setDisplayUseLogoEnabled(false);
        activity.getActionBar().setDisplayShowHomeEnabled(false);
        activity.getActionBar().setDisplayShowTitleEnabled(false);
        activity.getActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getActionBar().setDisplayShowCustomEnabled(false);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		activity.getActionBar().setCustomView(mCustomView);
		activity.getActionBar().setDisplayShowCustomEnabled(true);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.participants_group_info_layout, null);
		TextView countTV = (TextView) view.findViewById(R.id.grp_members_count);
		countTV.setText("("+count+")");
		TextView patientNameTV = (TextView) view.findViewById(R.id.patient_name);
		ImageView patientImg = (ImageView) view.findViewById(R.id.patient_img);
		if(conversation.getPatientID()!=0){
			patientImg.setImageResource(R.drawable.pat_photo);
		} else {
			patientImg.setImageResource(R.drawable.patient_conversation_no_photo_icon);
		}
		patientNameTV.setText(patientName);
		participantsList = (ListView) view.findViewById(R.id.participantsList);
		participantsList.setAdapter(new ParticipantsGroupListAdapter(activity, participants, conversation.getUserId()));
		return view;
	}
	
	public ENTUser getENTUserFromId(String id){
		for(ENTUser user : BundleKeys.QB_Users){
			if(user.getId().equals(id)){
				return user;
			}
		}
		return null;		
	}

	public static String[] removeElements(String[] input, String deleteMe) {
		if (input != null) {
			List<String> list = new ArrayList<String>(Arrays.asList(input));
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).equals(deleteMe)) {
					list.remove(i);
				}
			}
			return list.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}
	
}
