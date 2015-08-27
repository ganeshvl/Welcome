package com.entradahealth.entrada.android.app.personal.activities.manage_queues;

import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.google.common.collect.Lists;

public class QueueListMultiChoiceModeListener implements
		AbsListView.MultiChoiceModeListener {

	private final ManageQueuesActivity activity;
	private QueueListItemAdapter adapter;
	ActionBar ab;
	static AlertDialog dgNameGroup, dgDeleteGroup;
	String grp_names;

	public QueueListMultiChoiceModeListener(ManageQueuesActivity activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		ab = activity.getActionBar();
		this.adapter = (QueueListItemAdapter) activity.queueListView
				.getAdapter();
		// BundleKeys.List_New_Group = new
		// ArrayList<String>(adapter.getCount());
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		mode.getMenuInflater().inflate(R.menu.menu_manage_queues, menu);
		if ((adapter = (QueueListItemAdapter) activity.queueListView
				.getAdapter()) != null) {
			for (int i = 0; i < adapter.checks.size(); ++i)
				adapter.checks.set(i, false);
		}
		if(BundleKeys.isNewSelection)
			ab.setTitle("Create Group");
		else
			ab.setTitle("Edit "+BundleKeys.grpName);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.create_done: {
			
			for(int i=0;i<checkedJobs.size();i++)
				Log.e("Item"+i, checkedJobs.get(i));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setCancelable(false);
			builder.setTitle("Name Group");
			LinearLayout layout = new LinearLayout(activity);
			layout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(16, 30, 30, 16);

			final EditText textBox = new EditText(activity);
			//Set limit for GroupName to 20 characters
			InputFilter[] FilterArray = new InputFilter[1];
			FilterArray[0] = new InputFilter.LengthFilter(20);
			textBox.setFilters(FilterArray);

			if(BundleKeys.isNewSelection)
				textBox.setText(null);
			else
				textBox.setText(BundleKeys.grpName);
			layout.addView(textBox, params);
			builder.setView(layout);

			builder.setPositiveButton("OK", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});

			textBox.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					if (s.length() > 0) {
						textBox.setError(null);
					} else {

					}
				}
			});

			builder.setNegativeButton("Cancel", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mode.finish();
					activity.rlGroups.setVisibility(View.VISIBLE);
				}
			});
			
			dgNameGroup = builder.create();
			dgNameGroup.show();
			// Overriding the handler immediately after show is probably a
			// better approach than OnShowListener as described below
			dgNameGroup.getButton(AlertDialog.BUTTON_POSITIVE)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (textBox.getText().toString().trim().equals("")) {
								textBox.setError("Enter a name for the group");
							} else {
								//if(BundleKeys.Selected_Queues != null && BundleKeys.Selected_Queues.size()>0)
									//BundleKeys.Selected_Queues.clear();
								UserState state = AndroidState.getInstance().getUserState();
								synchronized (state) { 
									DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());
									  try { 
										  	//writer.createGroupQueues(checkedJobs); 
										  	if(BundleKeys.isNewSelection)//Insert a new group
										  		writer.insertFavGroupName(textBox.getText().toString());
										  	else//Update existing group
										  		writer.updateFavGroupName(BundleKeys.grpId, textBox.getText().toString());
										  	
										  	if(BundleKeys.isNewSelection)
										  		writer.createGroupQueues(checkedJobs);
										  	else{
										  		BundleKeys.favgrp_groupID = Integer.parseInt(BundleKeys.grpId);
										  		writer.updateGroupQueues(checkedJobs);
										  		writer.createGroupQueues(checkedJobs);
										  	}
									  }catch(DomainObjectWriteException e1) {
										  e1.printStackTrace(); 
										  throw new RuntimeException(e1);
									  } 
								}
								
								//BundleKeys.selChks_grp_names.add(textBox.getText().toString());
								checkedJobs.clear();
								mode.finish();
								dgNameGroup.dismiss();
								//activity.loadGroups();
								//activity.onStart();
								activity.rlGroups.setVisibility(View.VISIBLE);
							}
						}
					});

			return true;
		}
		case R.id.create_cancel: 
		{
			//Delete group with queues
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setCancelable(false);
			builder.setTitle(R.string.grp_delete_title);
			builder.setMessage(activity.getResources().getString(R.string.grp_delete_msg) + " "+BundleKeys.grpName +"?");
			builder.setPositiveButton("Delete", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					UserState state = AndroidState.getInstance().getUserState();
					synchronized (state) { 
						DomainObjectWriter writer = state.getProvider(state.getCurrentAccount());
						writer.deleteFavGroups(Integer.parseInt(BundleKeys.grpId)); 
					}
					mode.finish();
					if(BundleKeys.selChks_grp_names.contains(BundleKeys.grpName.trim()))
						BundleKeys.selChks_grp_names.remove(BundleKeys.grpName);
						
					dgDeleteGroup.dismiss();
					activity.loadGroups();
					
					//activity.rlGroups.setVisibility(View.VISIBLE);
				}
			});
			
			builder.setNegativeButton("Cancel", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mode.finish();
					dgDeleteGroup.dismiss();
					activity.rlGroups.setVisibility(View.VISIBLE);
				}
			});
			
			dgDeleteGroup = builder.create();
			dgDeleteGroup.show();
			
			
			return true;
		}

		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		activity.loadGroups();
		ab.setTitle("Edit Queues");
		BundleKeys.isNewSelection = true;
		for (int i = 0; i < adapter.checks.size(); ++i)
			adapter.checks.set(i, false);
	}

	List<String> checkedJobs;
	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		View v = activity.queueListView.getChildAt(position
				- activity.queueListView.getFirstVisiblePosition());
		if (v != null) {
			Log.v("Entrada-MCDebug",
					String.format("Position: %d, value: %b", position, checked));
			v.setBackgroundColor(activity.getResources().getColor(
					checked ? R.color.selected_list_item
							: R.color.unselected_list_item));
		}

		adapter.checks.set(position, checked);

		checkedJobs = Lists
				.newArrayListWithCapacity(adapter.arrayList.size());
		for (int i = 0; i < adapter.checks.size(); ++i) {
			if (adapter.checks.get(i))
				checkedJobs.add(adapter.arrayList.get(i).name);
		}
		
		if(BundleKeys.isNewSelection)
			mode.setTitle("Create Group");
		else
			mode.setTitle("Edit "+BundleKeys.grpName);
		//mode.setTitle("Create Group");
	}

}
