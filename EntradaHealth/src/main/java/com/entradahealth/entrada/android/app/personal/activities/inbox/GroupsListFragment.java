package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.activities.inbox.adapters.GroupExpandableListAdapter;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Contact;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Group;
import com.entradahealth.entrada.android.app.personal.activities.inbox.utils.Constants;

public class GroupsListFragment extends Fragment{

	private View view;
	private ExpandableListView groupsListView;
	private SharedPreferences  mPrefs;
	private Editor prefsEditor;
	private List<Group> groupList;
	private Button done;
	private static GroupsListFragment fragment;
	private GroupExpandableListAdapter adapter;
	private ImageButton delete;
	private TextView noGroupsTV;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.group_listview_layout, null);
		setAdapterToList();
		return view;
	}

	private void setAdapterToList() {
		mPrefs = getActivity().getSharedPreferences("MyPref",
				Context.MODE_PRIVATE);
		prefsEditor = mPrefs.edit();		
		groupsListView = (ExpandableListView) view.findViewById(R.id.groupsLV);
		delete = (ImageButton) view.findViewById(R.id.grpdelete);
		noGroupsTV = (TextView) view.findViewById(R.id.noGroupsFoundTV);
		groupsListView.setGroupIndicator(null);
		groupList = getParents();
		if (groupList != null && groupList.size() > 0) {
			adapter = new GroupExpandableListAdapter(getActivity(), groupList);
			groupsListView.setAdapter(adapter);
			// The choice mode has been moved from list view to adapter in order
			// to not extend the class ExpansibleListView
			adapter.setChoiceMode(GroupExpandableListAdapter.CHOICE_MODE_MULTIPLE);

			// Handle the click when the user clicks an any child
			groupsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					delete.setVisibility(View.VISIBLE);
					adapter.setClicked(groupPosition, childPosition);
					return false;
				}
			});

			noGroupsTV.setVisibility(View.GONE);
		} else {
			groupsListView.setVisibility(View.GONE);
			noGroupsTV.setText(R.string.str_no_grps);
			noGroupsTV.setVisibility(View.VISIBLE);

		}
		done = (Button) view.findViewById(R.id.done);
		done.setOnClickListener(new DoneClickListener());
		delete.setOnClickListener(new DeleteClickListener());
	}

	public static GroupsListFragment newInstance() {
		fragment = new GroupsListFragment(); 
		return fragment;
	}
	
	public class DeleteClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			
			SparseArray<SparseBooleanArray> sparsebArray = new SparseArray<SparseBooleanArray>();
			if(adapter!=null) {
				sparsebArray = adapter.getCheckedPositions();
				for (int i = 0; i < sparsebArray.size(); i++) {
					SparseBooleanArray bA = sparsebArray.valueAt(i);
					int grpPos = sparsebArray.keyAt(i);
					Group group = getGroupByPosition(grpPos);
					HashSet<String> set = (HashSet<String>) mPrefs
							.getStringSet(group.getName(), null);
					List<String> sortedList = new ArrayList<String>(set);
					Collections.sort(sortedList);
					List<String> tempList = new ArrayList<String>(sortedList);
					for (int j = 0; j < bA.size(); j++) {
						if (bA.valueAt(j) == true) {
							sortedList.remove(tempList.get(bA.keyAt(j)));
						}
					}
					HashSet<String> sm_group_contacts_set = new HashSet<String>(
							sortedList);
					prefsEditor.putStringSet(group.getName(),
							sm_group_contacts_set);
					prefsEditor.commit();

				}
				noGroupsTV.setVisibility(View.GONE);
				setAdapterToList();
			} else {
				groupsListView.setVisibility(View.GONE);
				noGroupsTV.setText(R.string.str_no_grps_del);
				noGroupsTV.setVisibility(View.VISIBLE);
			}
		}
		
	}
	
	public Group getGroupByPosition(int position){
		HashSet<String> set = (HashSet<String>) mPrefs.getStringSet(
				Constants.SM_GROUPS, null);
		Group grp = null;
		if (set != null) {			
			List<String> sortedList = new ArrayList<String>(set);
			Collections.sort(sortedList);
				String[] temp;
				String delimiter = "/";
				temp = sortedList.get(position).toString().split(delimiter);
				grp = new Group(Integer.valueOf(temp[0]), temp[1]);
			}
		return grp;
	}
	
	public List<Group> getParents(){
		HashSet<String> set = (HashSet<String>) mPrefs.getStringSet(
				Constants.SM_GROUPS, null);
		List<Group> groupList = new ArrayList<Group>();
		if (set != null) {			
			List<String> sortedList = new ArrayList<String>(set);
			Collections.sort(sortedList);
			for (int i = 0; i < set.size(); i++) {
				String[] temp;
				String delimiter = "/";
				temp = sortedList.get(i).toString().split(delimiter);
				List<Contact> children = getGroupChildren(temp[1]);
				if(children.size() > 0){
					groupList.add(new Group(Integer.valueOf(temp[0]), temp[1], children));
				} else {
					sortedList.remove(i);
					HashSet<String> sm_groups_set = new HashSet<String>(sortedList);
					prefsEditor.putStringSet(Constants.SM_GROUPS, sm_groups_set);
					prefsEditor.commit();
					set = (HashSet<String>) mPrefs.getStringSet(Constants.SM_GROUPS, null);
				}
			}
		}
		return groupList;
	}
		
	public List<Contact> getGroupChildren(String groupName) {
		List<Contact> _contacts = new ArrayList<Contact>();
		HashSet<String> set = (HashSet<String>) mPrefs.getStringSet(groupName, null);
		if (set != null) {
			List<String> sortedList = new ArrayList<String>(set);
			Collections.sort(sortedList);
			for (int j = 0; j < set.size(); j++) {
				String[] temp;
				String delimiter = "/";
				temp = sortedList.get(j).toString().split(delimiter);
				Contact contact = new Contact(1, temp[0], temp[1]);
				_contacts.add(contact);
			}
		}
		return _contacts;
	}
	
	public List<List<Contact>> getChildren(){
		List<List<Contact>> contacts = new ArrayList<List<Contact>>(); 
		for(int i=0; i<groupList.size();i++){
			HashSet<String> set = (HashSet<String>) mPrefs.getStringSet(groupList.get(i).getName(), null);
			if(set!=null) {
				List<Contact> _contacts = new ArrayList<Contact>();
				List<String> sortedList = new ArrayList<String>(set);
				Collections.sort(sortedList);
				for (int j = 0; j < set.size(); j++) {
					String[] temp;
					String delimiter = "/";
					temp = sortedList.get(j).toString().split(delimiter);
					Contact contact = new Contact(1, temp[0], temp[1]);
					_contacts.add(contact);
				}
				contacts.add(_contacts);
			}
		}
		return contacts;
	}
	
	public class DoneClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			 getActivity().getFragmentManager().popBackStack();	
	    }
		
	}
		
}
