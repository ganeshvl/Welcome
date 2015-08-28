package com.entradahealth.entrada.android.app.personal.activities.schedule;

import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.SectionListItemInfo;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleUtils;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.entradahealth.entrada.core.remote.APIService;

/**
 * List view adapter class.
 */

public class ScheduleListAdapter extends ArrayAdapter<SectionListItemInfo>
implements StickyListHeadersAdapter, SectionIndexer {

	private List<SideMenuSection> mSections;
	/**
	 * Array keeping the first index of the section. Indexes correspond to
	 * mSections.
	 */
	private int[] mSectionHeads;

	private LayoutInflater mInflater;
	private Patient mPatient = null;
	private APIService mService;
	private Context mContext;
	private Account mAccount;
	private DomainObjectReader mReader;
    private DomainObjectWriter mWriter;
	

	public ScheduleListAdapter(Context context) {
		super(context, 0);
		mInflater = LayoutInflater.from(context);
		setData(null);
		this.mContext = context;
		UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			mAccount = state.getCurrentAccount();
		}
		mReader = AndroidState.getInstance().getUserState().getProvider(mAccount);
		mWriter = AndroidState.getInstance().getUserState().getProvider(mAccount);
	}
;
	/**
	 * This method set the data to list view adapter sections and section heads.
	 * @param data
	 */
	public void setData(List<SideMenuSection> data) {
		clear();
		if (data == null) {
			mSections = Collections.emptyList();
		} else {
			mSections = data;
		}
		int numSections = mSections.size();
		mSectionHeads = new int[numSections];

		int sectionHead = 0;
		// Prepares data for each section
		for (int i = 0; i < numSections; ++i) {
			mSectionHeads[i] = sectionHead;
			SideMenuSection section = mSections.get(i);
			int sectionSize = section.size();
			List<SectionListItemInfo> menuItems = section.menuItems;
			for (int j = 0; j < sectionSize; ++j) {
				add(menuItems.get(j));
			}
			sectionHead += sectionSize;
		}
		notifyDataSetChanged();
	}
	
	/**
	 * Prepare the UI for List
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ItemHolder holder;
		final SectionListItemInfo info = getItem(position);

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.layout_list_row_item, parent,false);

			TextView patientName = (TextView) view.findViewById(R.id.tv_patient_name);
			TextView appointmentTime = (TextView) view.findViewById(R.id.tv_appointment_time);
			TextView mrn = (TextView) view.findViewById(R.id.tv_mrn);
			TextView reasonToVisit = (TextView) view.findViewById(R.id.tv_reason);
			ImageView appointmentStatus = (ImageView)view.findViewById(R.id.appointment_status);

			holder = new ItemHolder(patientName, appointmentTime, mrn, reasonToVisit,appointmentStatus);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ItemHolder) view.getTag();
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Patient patient = (Patient)v.findViewById(R.id.tv_patient_name).getTag();
				Bundle bundle = new Bundle();
				bundle.putSerializable(Constants.PATIENTID_OBJECT, patient);
				bundle.putString(Constants.PATIENTID, info.getPatientID());
				
				DemoGraphicFragment fragment = new DemoGraphicFragment();
				fragment.setArguments(bundle);
				FragmentManager fragmentManager = ((ScheduleActivity)mContext).getFragmentManager();
				FragmentTransaction fragtransacion = fragmentManager.beginTransaction();
				fragtransacion.addToBackStack(null);
				fragtransacion.add(android.R.id.content,fragment, "demographic").commit();
				
			}
		});
		
		// setting data to list items
		updateUI(position, holder, info);
		  if(Constants.LOG)Log.e("account", mAccount.toString());
	        	
		return view;
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void updateUI(int position, ItemHolder holder, SectionListItemInfo info){
		String[] time = info.getAppointmentTime().split(":");
		holder.appointmentTime.setText(ScheduleUtils.getDateFormattedString(time[0]+":"+time[1]));
		holder.reasonToVisit.setText(info.getReasonName());
		if(info.getAppointmentStatus().equalsIgnoreCase("200")){
			holder.appointmentStatus.setVisibility(View.VISIBLE);	
		}else{
			holder.appointmentStatus.setVisibility(View.INVISIBLE);
		}
		Patient patient = mReader.getPatient(Long.parseLong(info.getPatientID()));
		//Checking for Patient details
		if(patient !=null){
			holder.patientName.setText(patient.getName());
			holder.mrn.setText(patient.medicalRecordNumber);
			holder.patientName.setTag(patient);
			
		}else{
			holder.patientName.setText(mContext.getResources().getString(R.string.patient_loading));
			holder.mrn.setText("");
			// if the patient is null, we can get the patient details from server.
			if(patient == null){
				 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					 new GetPatientDemographicInfo(info, null,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				 }else{
					 new GetPatientDemographicInfo(info, null,holder).execute();	 
				 }
				
			}
		}
		
	}

	/**
	 * Header section
	 */
	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		int sectionIndex = getSectionIndex(position);
		SideMenuSection section = mSections.get(sectionIndex);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_header, parent,
					false);
		}
		// convert milli seconds to date and display in list header.
		TextView title = (TextView) convertView.findViewById(R.id.tv_header);
		title.setText(ScheduleUtils.convertMilliSecondsToHeaderDate(section.title));

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		return getSectionIndex(position);
	}

	private int getSectionIndex(int position) {
		int numSections = mSectionHeads.length;
		int sectionIndex = 0;

		for (; sectionIndex < numSections; ++sectionIndex) {
			// This should be the only check. We've reached the section head
			// that is above the position.
			// It accounts for empty sections (e.g. mSectionHeads = [0, 2, 2, 8]
			// and for
			// position 2, we want to return index 2, not 1).
			if (position < mSectionHeads[sectionIndex]) {
				break;
			}
		}

		return sectionIndex - 1;
	}

	/**
	 * View holder class
	 */
	private class ItemHolder {
		final TextView patientName;
		final TextView appointmentTime;
		final TextView mrn;
		final TextView reasonToVisit;
		final ImageView appointmentStatus;

		ItemHolder(TextView patientName,TextView  appointmentTime, TextView mrn, TextView reasonToVisit, ImageView appointmentStatus) {
			this.patientName = patientName;
			this.appointmentTime = appointmentTime;
			this.mrn = mrn;
			this.reasonToVisit = reasonToVisit;
			this.appointmentStatus = appointmentStatus;
		}

	}

	// returns position for selected section
	@Override
	public int getPositionForSection(int sectionIndex) {
		return sectionIndex;
	}

	//returns section position
	@Override
	public int getSectionForPosition(int position) {
		return position;
	}
	// returns sections count.
	@Override
	public Object[] getSections() {
		return null;
	}
	
	/**
	 * Async task to get the Patient info from the server.
	 */
	public class GetPatientDemographicInfo extends AsyncTask<Void, Void, Patient>{

		private long patientId;
		private ItemHolder viewHolder;
		private String patientName;
		private Patient patientDetails = null;
		private SectionListItemInfo itemInfo = null;
		
		GetPatientDemographicInfo(long patientId, ENTConversation conversation){
			this.patientId = patientId;
		}
		
		public GetPatientDemographicInfo(SectionListItemInfo info, ENTConversation conversation, ItemHolder viewHolder) {
			this.patientId = patientId;
			this.viewHolder = viewHolder;
			this.itemInfo = info;
		}

		@Override
		protected Patient doInBackground(Void... params) {
			UserState state = AndroidState.getInstance().getUserState();
			Account account = state.getCurrentAccount();
			if(account!=null){
	            DomainObjectReader reader = state.getProvider(account);
	            try{
	            	return patientDetails = reader.getPatientDemographicInfo(Long.parseLong(itemInfo.getPatientID()));
	            }catch(Exception e){
	            	if(Constants.LOG)Log.e("Number format exception", e.getMessage());
	            }
			} else {
				if(Constants.LOG)Log.e("GetPatientDemographicInfo", "No User Found");
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Patient patient) {
			super.onPostExecute(patient);
			if(viewHolder!=null && patient !=null) {
				viewHolder.patientName.setText(patient.getName());
				viewHolder.patientName.setTag(patient);
				viewHolder.mrn.setText(patient.medicalRecordNumber);
				notifyDataSetChanged();
			}	
		}
	}

}
