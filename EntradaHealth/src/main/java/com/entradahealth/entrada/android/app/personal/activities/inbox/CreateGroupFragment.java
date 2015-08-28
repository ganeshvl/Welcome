package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Group;
import com.entradahealth.entrada.android.app.personal.activities.inbox.utils.Constants;

public class CreateGroupFragment extends DialogFragment{

	private View view;
	private ArrayList<String> contactsList;
	private Button newGroupButton;
	private Button existingGroupButton;
	private ViewAnimator viewAnimator;
	private EditText newGroupName;
	private ImageButton newGroupNameClear;
	private Button newGroupOkButton;
	private Button newGroupCancelButton;
	private Button existingGroupOkButton;
	private Button existingGroupCancelButton;
	private SharedPreferences  mPrefs;
	private Editor prefsEditor;
	private HashSet<String> sm_groups_set;
	private List<Group> groupList;
	private Spinner spinner;
	private SpinAdapter adapter;
	private TextView titleView;
	private TextView existingGrpValidationMsg;
	private TextView newGrpValidationMsg;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mPrefs = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
		titleView = (TextView) getDialog().findViewById(android.R.id.title);
		titleView.setText("Select New/Existing");
		titleView.setTextSize(16);
		titleView.setTypeface(null, Typeface.BOLD);
		titleView.setGravity(Gravity.CENTER);
		Bundle bundle = this.getArguments();
		contactsList = bundle.getStringArrayList("contactslist");
		view = inflater.inflate(R.layout.create_group_layout, null);	
		newGroupButton = (Button)view.findViewById(R.id.newgroupbtn);
		existingGroupButton = (Button)view.findViewById(R.id.existinggroupbtn);
		newGroupButton.setOnClickListener(new NewGroupButtonClickListener());
		existingGroupButton.setOnClickListener(new ExistingGroupButtonClickListener());
		viewAnimator = (ViewAnimator) view.findViewById(R.id.viewAnimator1);
		newGroupName = (EditText) view.findViewById(R.id.new_grp_name);
		newGroupNameClear = (ImageButton) view.findViewById(R.id.new_grp_name_clear);
		newGroupOkButton = (Button) view.findViewById(R.id.newgrp_ok);
		newGroupCancelButton = (Button) view.findViewById(R.id.newgrp_cancel);
		newGroupOkButton.setOnClickListener(new NewGroupOkButtonClickListener());
		newGroupCancelButton.setOnClickListener(new GroupCancelButtonClickListener());
		newGroupNameClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				newGroupName.setText("");
			}
		});		
		spinner = (Spinner) view.findViewById(R.id.groupsList);
		existingGroupOkButton = (Button) view.findViewById(R.id.existinggrp_ok);
		existingGroupOkButton.setOnClickListener(new ExistingGroupOkButtonClickListener());
		existingGroupCancelButton = (Button) view.findViewById(R.id.existinggrp_cancel);
		existingGroupCancelButton.setOnClickListener(new GroupCancelButtonClickListener());
		newGrpValidationMsg = (TextView) view.findViewById(R.id.newgrpvalidationMessage);
		existingGrpValidationMsg = (TextView) view.findViewById(R.id.existinggrpvalidationMessage);
		return view;
	}
	
	public static CreateGroupFragment newInstance() {
		return new CreateGroupFragment();
	}
	
	public class NewGroupButtonClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			viewAnimator.setInAnimation(AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.slide_in_left));
			viewAnimator.showNext();
			titleView.setText("Create New Group");
		}
		
	}
	
	public class ExistingGroupButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			viewAnimator.showNext();
			viewAnimator.setInAnimation(AnimationUtils.loadAnimation(
					getActivity(), android.R.anim.slide_in_left));
			viewAnimator.showNext();
			titleView.setText("Select Existing Group");
			groupList = getGroups();
			adapter = new SpinAdapter((Context)getActivity(),
		            android.R.layout.simple_spinner_item,
		            groupList);
			spinner.setAdapter(adapter);
		}

	}
	
	public List<Group> getGroups(){
		HashSet<String> set = (HashSet<String>) mPrefs.getStringSet(
				Constants.SM_GROUPS, null);
		groupList = new ArrayList<Group>();
		if (set != null) {			
			List<String> sortedList = new ArrayList<String>(set);
			Collections.sort(sortedList);
			for (int i = 0; i < set.size(); i++) {
				String[] temp;
				String delimiter = "/";
				temp = sortedList.get(i).toString().split(delimiter);
				groupList.add(new Group(Integer.valueOf(temp[0]),
							temp[1]));
			}
		}
		return groupList;
	}

	public class NewGroupOkButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			String groupName = newGroupName.getText().toString();
			boolean isGroupNameExists = false;
			if (groupName == null || groupName.isEmpty()) {
				newGrpValidationMsg.setVisibility(View.VISIBLE);
				newGrpValidationMsg.setText(R.string.str_empty_grp);
			} else {
				List<Group> grpList = getGroups();
				for(int i=0;i<grpList.size();i++){
					if(groupName.equalsIgnoreCase(grpList.get(i).getName())){
						isGroupNameExists = true;
					} 
				}
				if(isGroupNameExists){
					newGrpValidationMsg.setVisibility(View.VISIBLE);
					newGrpValidationMsg.setText(R.string.str_grp_exists);					
				} else {
					setContactsToGroup(groupName);
					newGrpValidationMsg.setVisibility(View.GONE);
				}
			}
		}

	}

	
	private void setContactsToGroup(String groupName) {
		prefsEditor = mPrefs.edit();
		Group group = new Group(1, groupName);
		sm_groups_set = (HashSet<String>) mPrefs.getStringSet(
				Constants.SM_GROUPS, null);
		if (sm_groups_set == null)
			sm_groups_set = new HashSet<String>();
		sm_groups_set.add(group.getAll());
		prefsEditor.putStringSet(Constants.SM_GROUPS, sm_groups_set);
		prefsEditor.commit();
		
		HashSet<String> sm_group_contacts_set = (HashSet<String>) mPrefs.getStringSet(groupName, null);
		if (sm_group_contacts_set == null) {
			sm_group_contacts_set = new HashSet<String>(contactsList);
		}
		else { 
			for(int i=0; i< contactsList.size() ; i++)
				sm_group_contacts_set.add(contactsList.get(i));
		}
		prefsEditor.putStringSet(groupName, sm_group_contacts_set);
		prefsEditor.commit();
		newGroupName.setText("");
		dismiss();
	}
	
	public class GroupCancelButtonClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			newGroupName.setText("");
			dismiss();
		}
		
	}
	
	public class ExistingGroupOkButtonClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			Group group = (Group) spinner.getSelectedItem();
			if (group==null || group.getName() == null || group.getName().isEmpty()) {
				existingGrpValidationMsg.setText(R.string.str_empty_grp);
				existingGrpValidationMsg.setVisibility(View.VISIBLE);
			} else {
				setContactsToGroup(group.getName());
				existingGrpValidationMsg.setVisibility(View.GONE);
			}
		}
		
	} 
	
	class SpinAdapter extends ArrayAdapter<Group>{

	    private Context context;
	    private List<Group> values;

	    public SpinAdapter(Context context, int textViewResourceId,
	    		List<Group> values) {
	        super(context, textViewResourceId, values);
	        this.context = context;
	        this.values = values;
	    }

	    public int getCount(){
	       return values.size();
	    }

	    public Group getItem(int position){
	       return values.get(position);
	    }

	    public long getItemId(int position){
	       return position;
	    }


	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        TextView label = new TextView(context);
	        label.setPadding(10, 10, 10, 10);
	        label.setTextColor(Color.BLACK);
	        label.setText(values.get(position).getName());
	        return label;
	    }

	    @Override
	    public View getDropDownView(int position, View convertView,
	            ViewGroup parent) {
	        TextView label = new TextView(context);
	        label.setPadding(10, 10, 10, 10);
	        label.setTextColor(Color.BLACK);
	        label.setText(values.get(position).getName());
	        return label;
	    }
	}

}

