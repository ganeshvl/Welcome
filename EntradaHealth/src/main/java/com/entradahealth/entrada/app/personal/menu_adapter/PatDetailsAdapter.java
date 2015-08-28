package com.entradahealth.entrada.app.personal.menu_adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.SecureMessaging;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.core.domain.Gender;
import com.entradahealth.entrada.core.domain.Patient;



public class PatDetailsAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private String p_gender;	
	private Patient patient;
	private Context context;
	private String phNo = null;
	
	public PatDetailsAdapter(Context ctx, Patient patient) {
		// TODO Auto-generated constructor stub
		this.inflater = LayoutInflater.from(ctx);
		this.patient = patient;
		this.context = ctx;
	}
		
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
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
	
	public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

	@Override
	public View getView(int pos, View v, ViewGroup arg2) {
		// TODO Auto-generated method stub
		
		v = inflater.inflate(R.layout.pat_demographics, null);
		TextView tvPatName = (TextView)v.findViewById(R.id.tvPatName);
		TextView tvMRN = (TextView)v.findViewById(R.id.tvMRN);
		TextView tvGender = (TextView)v.findViewById(R.id.tvGender);
		TextView tvDOB = (TextView)v.findViewById(R.id.tvDOB);
		TextView tvAddress1 = (TextView)v.findViewById(R.id.tvAddress1);
		TextView tvAddress2 = (TextView)v.findViewById(R.id.tvAddress2);
		TextView tvPhone = (TextView)v.findViewById(R.id.tvPhone);
		TextView tvCity = (TextView)v.findViewById(R.id.tvCity);
		TextView tvState = (TextView)v.findViewById(R.id.tvState);
		TextView tvZip = (TextView)v.findViewById(R.id.tvZip);
		
		ImageView ivPDMessage = (ImageView)v.findViewById(R.id.ivPDMessage);
		ivPDMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Start SM conversation with this patient
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
		    		context.startActivity(new Intent(context, SecureMessaging.class)
		    			.putExtra("patient_name", patient.getFirstName()+" "+patient.getLastName())
		    			.putExtra("patient_id", patient.getPatientID())
		    			.putExtra("fromDemographics", true));
		    			
		    			((JobDisplayActivity)context).finish();
		    		
				}
			}
		});
		
		LinearLayout llPhyInfo = (LinearLayout)v.findViewById(R.id.llPhysicianInfo);
		LinearLayout llReferringPhyInfo = (LinearLayout)v.findViewById(R.id.llReferringPhyInfo);
		LinearLayout llPrimaryCareInfo = (LinearLayout)v.findViewById(R.id.llPrimaryCareInfo);
		
		//RP
		final TextView tvRPName = (TextView)v.findViewById(R.id.tvRPName);
		final TextView tvRPPhone = (TextView)v.findViewById(R.id.tvRPPhone);
		TextView tvRPAddress = (TextView)v.findViewById(R.id.tvRPAddress);
		TextView tvRPCity = (TextView)v.findViewById(R.id.tvRPCity);
		
		//PCP
		final TextView tvPCPName = (TextView)v.findViewById(R.id.tvPCPName);
		final TextView tvPCPPhone = (TextView)v.findViewById(R.id.tvPCPPhone);
		TextView tvPCPAddress = (TextView)v.findViewById(R.id.tvPCPAddress);
		TextView tvPCPCity = (TextView)v.findViewById(R.id.tvPCPCity);
		
		tvPatName.setText(patient.getName());
		
		if(patient.medicalRecordNumber != null && !patient.medicalRecordNumber.isEmpty())
			tvMRN.setText(patient.medicalRecordNumber);
		else
			tvMRN.setVisibility(View.GONE);
		
		if (patient.gender != Gender.UNKNOWN){
			p_gender = patient.gender.toString().substring(0,1);
	        p_gender = p_gender.equals("M")?"Male":"Female";
			tvGender.setText(p_gender);
		}else{
			tvGender.setVisibility(View.GONE);
		}
		
				
		if(patient.address1 != null && !patient.address1.trim().isEmpty()){
			tvAddress1.setText(patient.address1);
			tvCity.setText(patient.city + ", "+patient.state+" - "+patient.zip);
		}else{
			tvAddress1.setVisibility(View.GONE);
			tvCity.setVisibility(View.GONE);
		}
		
		if(patient.address2 != null && !patient.address2.trim().isEmpty()){
			tvAddress2.setText(patient.address2);
		}else{
			tvAddress2.setVisibility(View.GONE);
		}
		
		if(patient.phone != null && !patient.phone.isEmpty()){
			String formattedNumber = PhoneNumberUtils.formatNumber(patient.phone);
			tvPhone.setText(formattedNumber);
			
			if(patient.phone.contains("x")){
				String[] parts = patient.phone.split("x");
				phNo = parts[0]; 
			}else{
				phNo = patient.phone;
			}
		}else{
			tvPhone.setVisibility(View.GONE);
		}
		
		
		//Referring Physician
		
		if(BundleKeys.refID == 0){
			llReferringPhyInfo.setVisibility(View.GONE);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    		        LayoutParams.MATCH_PARENT,      
    		        LayoutParams.WRAP_CONTENT
    		);
    		params.setMargins(dpToPx(0), dpToPx(0), dpToPx(0), dpToPx(0));
    		llPrimaryCareInfo.setLayoutParams(params);
		}else{
			llReferringPhyInfo.setVisibility(View.VISIBLE);
			if(isListRPNotNull() && BundleKeys.list_rp.get(0).getName() != null && !BundleKeys.list_rp.get(0).getName().isEmpty()) {
				tvRPName.setText(BundleKeys.list_rp.get(0).getName());
			} else {
				tvRPName.setVisibility(View.GONE);
			}
			 
			if(isListRPNotNull() && BundleKeys.list_rp.get(0).address1 != null && !BundleKeys.list_rp.get(0).address1.isEmpty()){
				tvRPAddress.setText(BundleKeys.list_rp.get(0).address1);
			}else{
				tvRPAddress.setVisibility(View.GONE);
			}
			
			if (isListRPNotNull() && BundleKeys.list_rp.get(0).city != null
					&& !BundleKeys.list_rp.get(0).city.isEmpty()
					&& BundleKeys.list_rp.get(0).state != null
					&& !BundleKeys.list_rp.get(0).state.isEmpty()
					&& BundleKeys.list_rp.get(0).zip != null
					&& !BundleKeys.list_rp.get(0).zip.isEmpty()) {
				tvRPCity.setText(BundleKeys.list_rp.get(0).city + ", "
						+ BundleKeys.list_rp.get(0).state + " - "
						+ BundleKeys.list_rp.get(0).zip);
			} else {
				tvRPCity.setVisibility(View.GONE);
			}
						
			if(isListRPNotNull() && BundleKeys.list_rp.get(0).phone != null){
				String formattedNumber = PhoneNumberUtils.formatNumber(BundleKeys.list_rp.get(0).phone);
				tvRPPhone.setText(formattedNumber);
			}else{
				tvRPPhone.setVisibility(View.GONE);
			}
		}
		
		//PCP 
		
		if(isListPCPNotNull()){
			llPrimaryCareInfo.setVisibility(View.VISIBLE);
			if(BundleKeys.list_pcp.get(0).getName() != null && !BundleKeys.list_pcp.get(0).getName().isEmpty()) {
				tvPCPName.setText(BundleKeys.list_pcp.get(0).getName());
			} else {
				tvPCPName.setVisibility(View.GONE);
			}
			
			if(BundleKeys.list_pcp.get(0).address1 != null && !BundleKeys.list_pcp.get(0).address1.isEmpty()){
				tvPCPAddress.setText(BundleKeys.list_pcp.get(0).address1);
			}else{
				tvPCPAddress.setVisibility(View.GONE);
			}
			
			if (BundleKeys.list_pcp.get(0).city != null
					&& !BundleKeys.list_pcp.get(0).city.isEmpty()
					&& BundleKeys.list_pcp.get(0).state != null
					&& !BundleKeys.list_pcp.get(0).state.isEmpty()
					&& BundleKeys.list_pcp.get(0).zip != null
					&& !BundleKeys.list_pcp.get(0).zip.isEmpty()) {
				tvPCPCity.setText(BundleKeys.list_pcp.get(0).city + ", "
						+ BundleKeys.list_pcp.get(0).state + " - "
						+ BundleKeys.list_pcp.get(0).zip);
			} else {
				tvPCPCity.setVisibility(View.GONE);
			}
			
			if(BundleKeys.list_pcp.get(0).phone != null && !BundleKeys.list_pcp.get(0).phone.isEmpty()){
				String formattedNumber = PhoneNumberUtils.formatNumber(BundleKeys.list_pcp.get(0).phone);
				tvPCPPhone.setText(formattedNumber);
			}else{
				tvPCPPhone.setVisibility(View.GONE);
			}
		}else{
			llPrimaryCareInfo.setVisibility(View.GONE);
			}
			
		
		tvPhone.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//BundleKeys.toCall = true;
			/*Intent callIntent = new Intent(Intent.ACTION_DIAL);
			
            callIntent.setData(Uri.parse("tel:"+phone_no));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
            context.startActivity(callIntent);*/
			//String phone_no = tvPhone.getText().toString().trim();
            dgCall(phNo);
            
			}
		});

		
		//Calculate Age based on current date
		String dob = this.patient.dateOfBirth.trim();
		//Check if Server or Generic job
        if(dob.trim().equals("") || dob.trim().equals(null))
        	tvDOB.setVisibility(View.GONE);
        else{
        	
			Calendar cal1 = new GregorianCalendar();
		    Calendar cal2 = new GregorianCalendar();
		    Calendar c = Calendar.getInstance();
		    int year = c.get(Calendar.YEAR);
			int mon = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			String currentDate = (mon+1)+"/"+day+"/"+year;
		    int age = 0;
		    int factor = 0; 
		    Date date1 = null, date2 = null;
			try {
				date1 = new SimpleDateFormat("MM/dd/yyyy").parse(patient.dateOfBirth);
				date2 = new SimpleDateFormat("MM/dd/yyyy").parse(currentDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("pat-details-adapter", "unexpected date format");
			}
		    
			if(date1 != null && date2 != null)
			{
		    cal1.setTime(date1);
		    cal2.setTime(date2);
		    if(cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
		          factor = -1; 
		    }
		    age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
		    tvDOB.setText(patient.dateOfBirth+" ("+age+"yo)");
			}else{
				tvDOB.setText(null);
			}
        }
		
		return v;
		
	}
	
	public void dgCall(final String phNo){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(null);
		String formattedNumber = PhoneNumberUtils.formatNumber(phNo);
		builder.setMessage(formattedNumber);
		builder.setPositiveButton("Call", new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				BundleKeys.toCall = true;
				Uri number = Uri.parse("tel:"+phNo);
		        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
		        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		        context.startActivity(callIntent);
		        
			}
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.setCancelable(true);
		builder.create();
		builder.show();
	}

	protected boolean isListRPNotNull(){
		if(BundleKeys.list_rp != null && BundleKeys.list_rp.size()>0)
			return true;
		else 
			return false; 
	}
	
	protected boolean isListPCPNotNull(){
		if(BundleKeys.list_pcp != null && BundleKeys.list_pcp.size() > 0)
			return true;
		else 
			return false; 
	}
	
	
}
