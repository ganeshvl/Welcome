package com.entradahealth.entrada.android.app.personal.activities.manage_queues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Queue;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.google.common.collect.Lists;

public class ManageQueuesActivity extends EntradaActivity {
	UserState state;
	DomainObjectWriter writer;
	Account currentAccount = null;
	DomainObjectProvider provider = null;
	ListView queueListView;
	EditText etSearch;
	TextView  tvQueues;
	RelativeLayout rlGroups;
	LinearLayout llFavGroup;
	ArrayList<Queue> queues = null;
	boolean from_settings = false, qchanged = false;
	QueueListItemAdapter qAdapter;
	ArrayList<Queue> mOriginalValues;
	 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_queues);
		queueListView = (ListView) findViewById(R.id.queueListView);

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Edit Queues");
		from_settings = getIntent().getBooleanExtra("from_settings", false);

		etSearch = (EditText) findViewById(R.id.etQSearch);
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				qAdapter.getFilter().filter(s.toString());

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				qAdapter.notifyDataSetChanged();

			}
		});
		
		llFavGroup = (LinearLayout)findViewById(R.id.llFavGroup);
		rlGroups = (RelativeLayout)findViewById(R.id.rlGroups);
		rlGroups.setVisibility(View.GONE);
		tvQueues = (TextView)findViewById(R.id.tvQueues);
	}
	
	public void loadGroups(){
		rlGroups.setVisibility(View.GONE);
		tvQueues.setVisibility(View.VISIBLE);
		BundleKeys.isNewSelection = true;
		state = AndroidState.getInstance().getUserState();
		synchronized (state) { 
			 writer = state.getProvider(state.getCurrentAccount());
			  try { 
				  writer.getGroupName();
			  }catch(DomainObjectWriteException e1) {
				  e1.printStackTrace(); 
				  throw new RuntimeException(e1);
			  } 
			  
			  //get queue counts
			  BundleKeys.list_q_counts = Lists.newArrayList();
			  for(int i=0;i<BundleKeys.list_grp_ids.size();i++){
				  try { 
					  int count = writer.getQueueCount(Integer.parseInt(BundleKeys.list_grp_ids.get(i)));
					  BundleKeys.list_q_counts.add(count);
				  }catch(DomainObjectWriteException e1) {
					  e1.printStackTrace(); 
					  throw new RuntimeException(e1);
				  }
			  }
			  
			  if(BundleKeys.grpId != null && BundleKeys.grpName != null){
				  Log.e("grpId", BundleKeys.grpId);
				  Log.e("grpName", BundleKeys.grpName);
			  }
		}
		
		for(int i=0;i<BundleKeys.list_grp_ids.size();i++){
			Log.e("ID..Grp_Name", BundleKeys.list_grp_ids.get(i)+"--"+BundleKeys.list_grp_names.get(i));
		}
		
		if(BundleKeys.list_grp_ids.size() > 0){
			rlGroups.setVisibility(View.VISIBLE);
			llFavGroup.removeAllViews();
			for(int i=0;i<BundleKeys.list_grp_ids.size();i++){
				
				RelativeLayout rl = new RelativeLayout(this);
				final TextView tvGroupName = new TextView(this);
				TextView tvGroupCount = new TextView(this);
				final ImageView ivGrpEdit = new ImageView(this);
				
				RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    lay.setMargins(16, 16, 16, 16);
			    rl.setPadding(8, 16, 8, 16);
			    llFavGroup.addView(rl, lay);
				
				RelativeLayout.LayoutParams lay3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    lay3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			    lay3.addRule(RelativeLayout.CENTER_VERTICAL);
			    ivGrpEdit.setId(97);
			    ivGrpEdit.setBackgroundResource(R.drawable.btn_round);
			    ivGrpEdit.setImageResource(R.drawable.ic_arrow);
			    ivGrpEdit.setTag(R.id.id1, BundleKeys.list_grp_ids.get(i));
			    ivGrpEdit.setTag(R.id.id2, BundleKeys.list_grp_names.get(i));
			    rl.addView(ivGrpEdit, lay3);
			    ivGrpEdit.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						rlGroups.setVisibility(View.GONE);
						BundleKeys.isNewSelection = false;
						String qq = null;
						BundleKeys.Selected_Queues_Positions = Lists.newArrayList();
						BundleKeys.Selected_Queues = Lists.newArrayList();
						
						try {
							BundleKeys.Selected_Queues = writer.getGroupQueues(Integer.parseInt(ivGrpEdit.getTag(R.id.id1).toString()));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DomainObjectWriteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for(int i=0;i<BundleKeys.Selected_Queues.size();i++){
							for(int j=0;j<queueListView.getCount();j++){
								Object object = queueListView.getItemAtPosition(j);
								Queue q = (Queue) object;
							    String name = q.name;
							    if(BundleKeys.Selected_Queues.get(i).trim().equals(name)){
							    	qq = qq +" - "+ name;
							    	BundleKeys.Selected_Queues_Positions.add(j);
							    }
							}
						    
						}
						
						BundleKeys.grpId = ivGrpEdit.getTag(R.id.id1).toString();
						BundleKeys.grpName = ivGrpEdit.getTag(R.id.id2).toString();
						
						for(int i=0;i<BundleKeys.Selected_Queues_Positions.size();i++)
							queueListView.setItemChecked(BundleKeys.Selected_Queues_Positions.get(i), true);
						
						//Toast.makeText(getApplicationContext(), qq, 1000).show();
					}
				});
			    			    
			    RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    lay2.addRule(RelativeLayout.LEFT_OF, ivGrpEdit.getId());
			    lay2.addRule(RelativeLayout.CENTER_VERTICAL);
			    rl.addView(tvGroupCount, lay2);
			    tvGroupCount.setId(96);
			    tvGroupCount.setPadding(0, 0, 16, 0);
			    tvGroupCount.setText(Integer.toString(BundleKeys.list_q_counts.get(i)));
			    tvGroupCount.setTextColor(Color.parseColor("#898585"));
			    tvGroupCount.setTextAppearance(getApplicationContext(), R.style.boldText);
				
				RelativeLayout.LayoutParams lay1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    lay1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			    lay1.addRule(RelativeLayout.CENTER_VERTICAL);
			    lay1.addRule(RelativeLayout.LEFT_OF, tvGroupCount.getId());
			    tvGroupName.setId(98);
			    tvGroupName.setPadding(8, 0, 0, 0);
			    tvGroupName.setEllipsize(TruncateAt.END);
			    tvGroupName.setSingleLine(true);
			    tvGroupName.setTextAppearance(getApplicationContext(), R.style.boldText);
			    tvGroupName.setTextColor(Color.parseColor("#000000"));
			    rl.addView(tvGroupName, lay1);
			    tvGroupName.setText(BundleKeys.list_grp_names.get(i));
			    tvGroupName.setTag(BundleKeys.list_grp_ids.get(i));

			    String name = tvGroupName.getText().toString().trim();
			    if(!BundleKeys.selChks_grp_names.contains(name) && !BundleKeys.isNewSelection){
					BundleKeys.selChks_grp_names.add(name);
					tvGroupName.setTextColor(Color.parseColor("#000000"));
				}else if(BundleKeys.selChks_grp_names.contains(name)){
					tvGroupName.setTextColor(Color.parseColor("#00838f"));
				}
			    
			    
			    tvGroupName.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						performClick(tvGroupName, true);
					}
				});
			}
			
			BundleKeys.Selected_Queues_Positions = Lists.newArrayList();
			
			for(int i=0;i<BundleKeys.list_grp_names.size();i++){
				if(BundleKeys.selChks_grp_names.contains(BundleKeys.list_grp_names.get(i).toString().trim())){
					
					BundleKeys.Selected_Queues = Lists.newArrayList();
					try {
						BundleKeys.Selected_Queues = writer.getGroupQueues(Integer.parseInt(BundleKeys.list_grp_ids.get(i)));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String qq = null;
					for(int j=0;j<BundleKeys.Selected_Queues.size();j++){
						for(int k=0;k<queueListView.getCount();k++){
							Object object = queueListView.getItemAtPosition(k);
							Queue q = (Queue) object;
						    String name = q.name;
						    if(BundleKeys.Selected_Queues.get(j).trim().equals(name)){
						    	qq = qq +" - "+ name;
						    	BundleKeys.Selected_Queues_Positions.add(k);
						    }
						}
					    
					}
					
				}
			}
		}else{
			rlGroups.setVisibility(View.GONE);
			tvQueues.setVisibility(View.GONE);
		}
		
	}
	
	public void performClick(TextView tv, boolean clicked){
		BundleKeys.Selected_Queues = Lists.newArrayList();
		try {
			BundleKeys.Selected_Queues = writer.getGroupQueues(Integer.parseInt(tv.getTag().toString()));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String qq = null;
		BundleKeys.Selected_Queues_Positions = Lists.newArrayList();
		for(int i=0;i<BundleKeys.Selected_Queues.size();i++){
			for(int j=0;j<queueListView.getCount();j++){
				Object object = queueListView.getItemAtPosition(j);
				Queue q = (Queue) object;
			    String name = q.name;
			    if(BundleKeys.Selected_Queues.get(i).trim().equals(name)){
			    	qq = qq +" - "+ name;
			    	BundleKeys.Selected_Queues_Positions.add(j);
			    }
			}
		    
		}
		//Toast.makeText(getApplicationContext(), qq, 1000).show();
		
		//Highlight Group name
		if(clicked){
			String name = tv.getText().toString().trim();
			if(!BundleKeys.selChks_grp_names.contains(name)){
				BundleKeys.selChks_grp_names.add(name);
				tv.setTextColor(Color.parseColor("#00838f"));
			}else if(BundleKeys.selChks_grp_names.contains(name)){
				BundleKeys.selChks_grp_names.remove(name);
				tv.setTextColor(Color.parseColor("#000000"));
			}
		}
								
		//Highlight queues selected in group
		for(int i=0;i<BundleKeys.Selected_Queues_Positions.size();i++){
			int pos = BundleKeys.Selected_Queues_Positions.get(i);
			queueListView.performItemClick(queueListView.getAdapter().getView(pos, null, null), pos, queueListView.getItemIdAtPosition(pos));
		}
		
	}

	@Override
	public void onBackPressed() {
		if (qAdapter.selChks.size() > 0){
			qchanged = true;
			BundleKeys.QUEUE_CHANGED = true;
		}else{
			qchanged = false;
			BundleKeys.QUEUE_CHANGED = false;
		}

		saveQueue();

		if (from_settings) {
			Intent intent = new Intent(this, EntradaSettings.class);
			intent.putExtra("qchanged", qchanged);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(this, JobListActivity.class);
			intent.putExtra("qchanged", qchanged);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		UserState state = AndroidState.getInstance().getUserState();

		synchronized (state) {
			currentAccount = state.getCurrentAccount();
		}

		if (currentAccount == null) {
			Log.e("Entrada-JobList",
					"current account is null in onStart; kicking back to user select.");
			Intent intent = new Intent(this, UserSelectActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

		provider = AndroidState.getInstance().getUserState()
				.getProvider(currentAccount);
		queues = Lists.newArrayList(provider.getQueues());
		qAdapter = new QueueListItemAdapter(this, 0, queues);
		Collections.sort(queues, DEFAULT_COMPARATOR);
		qAdapter.notifyDataSetChanged();
		queueListView.setAdapter(qAdapter);

		queueListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		queueListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						int pos = 0;
						String selected_queue_name = ((TextView)view).getText().toString().trim();
						
						if(qAdapter.mOriginalValues == null)
							qAdapter.mOriginalValues = new ArrayList<Queue>(queues);
						if(qAdapter.mOriginalValues != null && qAdapter.mOriginalValues.size() > 0)
						{
							for(int i =0;i<qAdapter.mOriginalValues.size();i++){
								if(qAdapter.mOriginalValues.get(i).name.trim().equals(selected_queue_name)){
									pos = i;
									break;
								}
									
							}
							boolean isSelected = qAdapter.getItem(pos).isSubscribed;
							
							if(isSelected){
								if (qAdapter.selChks.contains(selected_queue_name)){
									qAdapter.selChks.remove(selected_queue_name);
									qAdapter.mOriginalValues.get(pos).isSubscribed = false;
								}
							}else{
								if (!qAdapter.selChks.contains(selected_queue_name)){
									qAdapter.selChks.add(selected_queue_name);
									qAdapter.mOriginalValues.get(pos).isSubscribed = true;
								}
							}
							
							
							TextView tvQ = (TextView) view
									.findViewById(R.id.tvQueueName);
							Log.e("qAdapter.mOriginalValues.get(pos).name", qAdapter.mOriginalValues.get(pos).name);
							tvQ.setTextColor(qAdapter.mOriginalValues.get(pos).name.trim().equals(selected_queue_name) && qAdapter.mOriginalValues.get(pos).isSubscribed ? Color
									.parseColor("#00838f") : Color
									.parseColor("#000000"));
							qAdapter.notifyDataSetChanged();
						}
					}

				});
		queueListView
				.setMultiChoiceModeListener(new QueueListMultiChoiceModeListener(
						this));
		BundleKeys.isNewSelection = true;
		
		
		mOriginalValues = new ArrayList<Queue>(queues);
		loadGroups();
	}

	public final Comparator<Queue> DEFAULT_COMPARATOR = new Comparator<Queue>() {

		@Override
		public int compare(Queue lhs, Queue rhs) {
			// TODO Auto-generated method stub

			int value1 = rhs.isSubscribed.compareTo(lhs.isSubscribed);
			if (value1 != 0) {
				return value1;
			} else {
				String left = lhs.name.toLowerCase(Locale.UK);
				String right = rhs.name.toLowerCase(Locale.UK);
				return left.compareTo(right);
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_account, menu);
		MenuItem item_cancel = menu.findItem(R.id.item_edit_cancel);
		item_cancel.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			if (qAdapter.selChks.size() > 0){
				qchanged = true;
				BundleKeys.QUEUE_CHANGED = true;
			}else{
				qchanged = false;
				BundleKeys.QUEUE_CHANGED = false;
			}

			saveQueue();

			if (from_settings) {
				Intent intent = new Intent(this, EntradaSettings.class);
				intent.putExtra("qchanged", qchanged);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, JobListActivity.class);
				intent.putExtra("qchanged", qchanged);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
			return true;

		case R.id.item_edit_done:
			if (qAdapter.selChks.size() > 0){
				qchanged = true;
				BundleKeys.QUEUE_CHANGED = true;
			}else{
				qchanged = false;
				BundleKeys.QUEUE_CHANGED = false;
			}

			saveQueue();
			if (from_settings) {
				Intent intent = new Intent(this, EntradaSettings.class);
				intent.putExtra("qchanged", qchanged);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, JobListActivity.class);
				intent.putExtra("qchanged", qchanged);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void saveQueue() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				try {
					for(Queue q:queues)
						if(q.isSubscribed)
							Log.e("Subscribed Queues", q.name);
					provider.writeQueues(queues);
				} catch (DomainObjectWriteException e) {
					e.printStackTrace(); // TODO: Handle this exception type.
				}
				return null;
			}
		}.execute();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		qAdapter.getFilter().filter(etSearch.getText().toString());
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (QueueListMultiChoiceModeListener.dgNameGroup != null
				&& QueueListMultiChoiceModeListener.dgNameGroup.isShowing())
			QueueListMultiChoiceModeListener.dgNameGroup.dismiss();
		//finish();
	}

}
