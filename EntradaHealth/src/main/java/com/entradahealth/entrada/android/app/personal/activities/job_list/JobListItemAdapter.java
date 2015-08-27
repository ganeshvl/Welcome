package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.Days;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.android.app.personal.utils.NetworkState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;
import com.google.common.collect.Lists;

/**
 * Transforms the job list into a set of views for JobListActivity's list view.
 *
 * @author eropple
 * @since 15 Oct 2012
 */
public class JobListItemAdapter extends BaseAdapter
{
	SharedPreferences sp;
    private final Activity activity;
    public List<Long> jobIds = new ArrayList<Long>();
    public List<Boolean> checks;
    private Account account;
    private UserPrivate currentUser;
    User user;
    UserState us;

    public JobListItemAdapter(Activity activity, int textViewResourceId,
			List<Long> jobIds, Account account) {

		this.activity = activity;
		this.jobIds = jobIds;
		// BundleKeys.ALL_COUNT = jobIds.size();
		this.checks = Lists.newArrayListWithCapacity(jobIds.size());
		for (int i = 0; i < jobIds.size(); ++i)
			checks.add(false);

		this.account = account;
		
		/*
		 * Check Job Type and Assign respective Job count
		 */
		sp = activity.getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
    }
    
	/**
	 * Method to get the number of items
	 * 
	 */
	public int getCount() {
		return jobIds.size();
	}

	/**
	 * Method to get the item at the specified position
	 */
	public Long getItem(int position) {

		return jobIds.get(position);
	}

	/**
	 * Method to get the id of the item at the specified position
	 */
	public long getItemId(int position) {

		return position;
	}
	
    public void addJobs(List<Long> jobIdss){
    	jobIds.addAll(jobIdss);
    	List<Boolean> tempChecks = checks;
    	this.checks = Lists.newArrayListWithCapacity(jobIds.size());
    	for (int i = 0; i < jobIds.size(); ++i){
    		try {
    			checks.add(tempChecks.get(i));
    		} catch(Exception e){
    			checks.add(false);
    		}
    	}	
    	
		//for (int i = 0; i < jobIds.size(); ++i)
		//	checks.add(false);

    	Log.e(" ","JobIds.size()--"+jobIds.size());
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        JobHolder holder;
        final TextView tvApptTime;
		TextView tvName = null, tvJType = null;
		final TextView tvMRN, tvFlag;
		ProgressBar pdUpload = null;
        

        if(row == null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.job_list_item, parent, false);
            row.setBackgroundColor(row.getResources().getColor(R.color.unselected_list_item));
            pdUpload = (ProgressBar)row.findViewById(R.id.pdUpload);
            tvApptTime = (TextView)row.findViewById(R.id.tvDateTime);
            tvName = (TextView)row.findViewById(R.id.tvPatName);
            tvMRN = (TextView)row.findViewById(R.id.tvMedRecNo);
            ImageView ivStat = (ImageView)row.findViewById(R.id.ivStatus);
            ImageView ivArrow = (ImageView)row.findViewById(R.id.ivArrow);
            ImageView ivCompleted = (ImageView)row.findViewById(R.id.ivCompleted);
            tvJType = (TextView)row.findViewById(R.id.tvJobType);
            
            holder = new JobHolder(tvApptTime, tvJType, tvMRN, tvName, ivStat, ivArrow, ivCompleted, pdUpload, this);

            row.setTag(holder);
        }
        else
        {
            holder = (JobHolder)row.getTag();
        }

        //Log.e("account", account.toString());
        DomainObjectReader reader = AndroidState.getInstance().getUserState().getProvider(account);
        DomainObjectWriter writer = AndroidState.getInstance().getUserState().getProvider(account);
        
        Job job = reader.getJob(jobIds.get(position));
        
        if(reader != null && job!=null){
        	
	        Encounter enc = reader.getEncounter(job.encounterId);
	        if(enc == null) {
	        	//Account acc = AndroidState.getInstance().getUserState().getCurrentAccount();
	        	String value = account.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
                Patient p = reader.getPatient(Long.parseLong(value));
                try {
                	if(writer != null && p != null){
                		enc = writer.createNewEncounter(p);
                	} else {
                		return row;
                	}
				} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	        }
	        JobType jt = reader.getJobType(job.jobTypeId);
	        Patient p = reader.getPatient(enc.patientId);
	
	        if(p != null)
		        holder.tvMRN.setText(p.medicalRecordNumber);
	        else
	        	holder.tvMRN.setText(BundleKeys.LOCAL_MRN);
	        
	        
	        holder.tvApptTime.setText(enc.getDateTimeText());
	        
	        if(jt != null)
	        holder.tvJType.setText(jt.name);
	        
	        if(p != null)
	        	holder.tvName.setText(p.getName());
	        else
	        	holder.tvName.setText(BundleKeys.LOCAL_LASTNAME +","+ BundleKeys.LOCAL_FIRSTNAME );
	    	
	        String str_date = enc.getDateTimeText().substring(0, enc.getDateTimeText().indexOf(" "));
	        String str_time = enc.getDateTimeText().substring(enc.getDateTimeText().indexOf(" "), enc.getDateTimeText().lastIndexOf(" "));
	        String str_ampm = enc.getDateTimeText().substring(enc.getDateTimeText().lastIndexOf(" "), enc.getDateTimeText().length());
	        
	      //Today, Yesterday logic
	        Date date, date1 = null;
	        date = new Date();
	        
	        String strDateFormat1 = "MM-dd-yy";
			SimpleDateFormat sdf1 = new SimpleDateFormat(strDateFormat1);
	    	String string = enc.getDateTimeText();
	    	try {
	    		SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy h:mm aa"); 
	    		date1 = df.parse(string);
	    		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	    	String strDateFormat2 = "MM-dd";
			SimpleDateFormat sdf2 = new SimpleDateFormat(strDateFormat2);
			String temp_date = sdf2.format(date1);
			
			DateMidnight start = new DateMidnight(date);
	        DateMidnight end = new DateMidnight(date1);
	
	    	
			if(BundleKeys.which != 2){
				int days = Days.daysBetween(start, end).getDays();
		    	if(days == 0){
		    		String strDateFormat = "h:mm a";
		    		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		    		String temp = "Today ";
		            holder.tvApptTime.setText(Html.fromHtml(temp+"<b><big>"+str_time+"</big></b>"+str_ampm));
		    	}else if(days == -1 || (days == 1 && !date1.after(date))){
		    		String strDateFormat = "h:mm a";
		    		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		    		String temp = "Yesterday ";
		    		holder.tvApptTime.setText(Html.fromHtml(temp+"<b><big>"+str_time+"</big></b>"+str_ampm));
		    	}else{
		            holder.tvApptTime.setText(Html.fromHtml(temp_date+"<b><big>"+str_time+"</big></b>"+str_ampm));
		    	}
			}else{
				holder.tvApptTime.setText(Html.fromHtml(temp_date+"<b><big>"+str_time+"</big></b>"+str_ampm));
			}
	    	
			holder.ivStat.setVisibility(job.stat ? View.VISIBLE
					: View.INVISIBLE);
			holder.tvName.setTextColor(job.isFlagSet(Job.Flags.HOLD)
					&& !job.isFlagSet(Job.Flags.LOCALLY_DELETED) ? Color.RED
					: Color.BLACK);
	        
	        
	        holder.ivStat.setVisibility(job.stat ? View.VISIBLE : View.INVISIBLE);
	        holder.tvName.setTextColor(job.isFlagSet(Job.Flags.HOLD) && !job.isFlagSet(Job.Flags.LOCALLY_DELETED) ? Color.RED: Color.BLACK);
	        
	        if(job.isFlagSet(Flags.UPLOAD_PENDING) || job.isFlagSet(Flags.UPLOAD_IN_PROGRESS) || job.isFlagSet(Flags.FAILED)){
	        	NetworkState ns = new NetworkState(activity);
	    		Boolean isConnected = ns.isConnectingToInternet();
	    		if(isConnected){
	    			holder.pdUpload.setVisibility(View.VISIBLE);
	    			holder.ivCompleted.setVisibility(View.INVISIBLE);
	    		}else{
	    			holder.pdUpload.setVisibility(View.GONE);
	    			holder.ivCompleted.setVisibility(View.VISIBLE);
	            	holder.ivCompleted.setImageResource(R.drawable.ic_sync_error);
	            	holder.ivArrow.setVisibility(View.INVISIBLE);
	    		}
	    			
	        }
	        
	        if(job.isFlagSet(Flags.UPLOAD_PENDING)){
	        	NetworkState ns = new NetworkState(activity);
	    		Boolean isConnected = ns.isConnectingToInternet();
	    		if(!isConnected){
	    			if(BundleKeys.which == 6){
	    				holder.tvName.setTextColor(Color.BLACK);
	        			holder.tvMRN.setTextColor(Color.BLACK);
	        			holder.tvJType.setTextColor(Color.parseColor("#a8a8a8"));
	        			holder.tvJType.setText(R.string.upload_error);
	    			}
	    			
	    			holder.pdUpload.setVisibility(View.GONE);
	    			holder.ivCompleted.setVisibility(View.VISIBLE);
	            	holder.ivCompleted.setImageResource(R.drawable.ic_sync_error);
	            	holder.ivArrow.setVisibility(View.INVISIBLE);
	    		}
	    		
	        }
	        
	        
	        
	        if(BundleKeys.which == 6 && job.isComplete()){
	        	holder.pdUpload.setVisibility(View.GONE);
	        	holder.ivCompleted.setVisibility(View.VISIBLE);
	        	holder.ivCompleted.setImageResource(R.drawable.completed);
	        	holder.ivArrow.setVisibility(View.INVISIBLE);
	        	holder.tvName.setTextColor(Color.parseColor("#a8a8a8"));
	        	holder.tvJType.setTextColor(Color.parseColor("#a8a8a8"));
	        }else if(BundleKeys.which == 6 && job.isFailed()){
	        	holder.pdUpload.setVisibility(View.GONE);
	        	holder.ivCompleted.setVisibility(View.VISIBLE);
	        	holder.ivCompleted.setImageResource(R.drawable.ic_sync_error);
	        	holder.ivArrow.setVisibility(View.INVISIBLE);
	        	holder.tvName.setTextColor(Color.BLACK);
    			holder.tvMRN.setTextColor(Color.BLACK);
	        	holder.tvJType.setTextColor(Color.parseColor("#a8a8a8"));
	        	holder.tvJType.setText(R.string.upload_error);
	        }
	        
	        
			// Log.e("Local_Flag", job.getFlagsString());
	        
	        	boolean checked = checks.get(position);
	            row.setBackgroundColor(activity.getResources().getColor(checked ? R.color.selected_list_item
	                                                                            : R.color.unselected_list_item));
        }
        return row;
    }

    
    public static class JobHolder
    {
        public final TextView tvName;
        public final TextView tvMRN;
        public final TextView tvJType;
        public final TextView tvApptTime;
        public final ImageView ivStat;
        public final ImageView ivArrow;
        public final ImageView ivCompleted;
        public final ProgressBar pdUpload;
        public final JobListItemAdapter adapter;

        public JobHolder(TextView tvApptTime, TextView tvJType, TextView tvMRN,
                         TextView tvName, ImageView ivStat, ImageView ivArrow, ImageView ivCompleted,
                         ProgressBar pdUpload, JobListItemAdapter adapter)
        {
            this.tvApptTime = tvApptTime;
            this.tvJType = tvJType;
            this.tvMRN = tvMRN;
            this.tvName = tvName;
            this.ivStat = ivStat;
            this.ivArrow = ivArrow;
            this.ivCompleted = ivCompleted;
            this.pdUpload = pdUpload;
            this.adapter = adapter;
        }
    }
}
