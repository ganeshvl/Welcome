package com.entradahealth.entrada.android.app.personal.activities.add_job;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.core.domain.Gender;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.inbox.service.ChatManager;
import com.entradahealth.entrada.core.inbox.service.GroupChatManagerImpl;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.quickblox.core.QBEntityCallbackImpl;

/**
 * Transforms the selPatient list into a set of views for AddJobActivity's list view.
 *
 * @author eropple
 * @since 8 Apr 2012
 */
public class PatientListItemAdapter<TPatient extends Patient> extends ArrayAdapter<TPatient>
{
    private final Activity activity;
    private final List<TPatient> patients;
    String p_gender;	
	Patient patient;
	String phNo = null;
	String recepient;
	boolean isChat;
	ENTConversation conversation = null;
	FragmentManager manager = null;
	EntradaApplication application;
	private ChatManager chat;

	
    public PatientListItemAdapter(Activity activity, int textViewResourceId,
            List<TPatient> patients, boolean isChat, String recepient, ENTConversation conversation, FragmentManager manager, ChatManager chat)
    {
    	super(activity, textViewResourceId, patients);
    	application = (EntradaApplication) EntradaApplication.getAppContext();
    	this.activity = activity;
    	this.patients = patients;
    	this.isChat = isChat;
    	this.recepient = recepient;
    	this.conversation = conversation;
    	this.chat = chat;
        ((GroupChatManagerImpl) chat).joinGroupChat(conversation, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(List list) {
            }
        });

    }

    public PatientListItemAdapter(Activity activity, int textViewResourceId,
                                  List<TPatient> patients, boolean isChat, String recepient)
    {       
    	super(activity, textViewResourceId, patients);
    	application = (EntradaApplication) EntradaApplication.getAppContext();
        this.activity = activity;
        this.patients = patients;
        this.isChat = isChat;
        this.recepient = recepient;
        this.conversation = null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        JobHolder holder;
        final TPatient selPatient = getItem(position);
        if(row == null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.patient_row_item, parent, false);
            row.setBackgroundColor(row.getResources().getColor(R.color.unselected_list_item));
            TextView nameView = (TextView)row.findViewById(R.id.tvPatientName);
            TextView mrnView = (TextView)row.findViewById(R.id.tvPatientMRN);
            ImageView goView = (ImageView)row.findViewById(R.id.ivGo);
            holder = new JobHolder(mrnView, nameView, goView);
            row.setTag(holder);
           
        }
        else
        {
            holder = (JobHolder)row.getTag();
        }

        if(isChat){
        	holder.goView.setVisibility(View.VISIBLE);
        }else{
        	holder.goView.setVisibility(View.GONE);
        }
        
        holder.goView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(application.isWifiConnected() || application.is3GOr4GConnected()) {
					Log.e("p", getItem(position).toString());
					Bundle b = new Bundle();
	        		b.putString("patient_name", getItem(position).getFullName());
	        		b.putString("recipient_name", recepient);
	        		b.putLong("patient_id", getItem(position).getPatientID());
	        		conversation.setName(getItem(position).getFullName());
	        		conversation.setPatientID(getItem(position).getPatientID());
	        		b.putSerializable("conversation", conversation);
	        		b.putBoolean("fromPatientSearch", true);
	        		SendPatientAccessTask task = new SendPatientAccessTask();
	        		task.execute();
//					NewMessageFragment msgFragment = new NewMessageFragment();
//	        		msgFragment.setArguments(b);
//	        		activity.getFragmentManager().popBackStack();
	        		activity.getFragmentManager().popBackStack();
//	        		FragmentTransaction ft = manager.beginTransaction().addToBackStack(null);
//	        		ft.replace(R.id.fragcontent, msgFragment, "message");
//	        		ft.commit();
				}
			}
		});
        
              
        holder.nameView.setTag(selPatient);
        holder.nameView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(application.isWifiConnected() || application.is3GOr4GConnected()) {
				// TODO Auto-generated method stub
				
				Patient selPatient = (Patient)v.getTag();
				Dialog dialog = new Dialog(activity);
				dialog.setTitle("Demographics");
				dialog.setContentView(R.layout.pat_demographics);
				TextView tvPatName = (TextView)dialog.findViewById(R.id.tvPatName);
				TextView tvMRN = (TextView)dialog.findViewById(R.id.tvMRN);
				TextView tvGender = (TextView)dialog.findViewById(R.id.tvGender);
				TextView tvDOB = (TextView)dialog.findViewById(R.id.tvDOB);
				TextView tvAddress1 = (TextView)dialog.findViewById(R.id.tvAddress1);
				TextView tvAddress2 = (TextView)dialog.findViewById(R.id.tvAddress2);
				final TextView tvPhone = (TextView)dialog.findViewById(R.id.tvPhone);
				TextView tvCity = (TextView)dialog.findViewById(R.id.tvCity);
//				TextView tvState = (TextView)dialog.findViewById(R.id.tvState);
				TextView tvZip = (TextView)dialog.findViewById(R.id.tvZip);
				
				LinearLayout llPhyInfo = (LinearLayout)dialog.findViewById(R.id.llPhysicianInfo);
				LinearLayout llReferringPhyInfo = (LinearLayout)dialog.findViewById(R.id.llReferringPhyInfo);
				LinearLayout llPrimaryCareInfo = (LinearLayout)dialog.findViewById(R.id.llPrimaryCareInfo);
				
				//RP
				TextView tvRPName = (TextView)dialog.findViewById(R.id.tvRPName);
				TextView tvRPPhone = (TextView)dialog.findViewById(R.id.tvRPPhone);
				TextView tvRPAddress = (TextView)dialog.findViewById(R.id.tvRPAddress);
				TextView tvRPCity = (TextView)dialog.findViewById(R.id.tvRPCity);
				
				//PCP
				TextView tvPCPName = (TextView)dialog.findViewById(R.id.tvPCPName);
				TextView tvPCPPhone = (TextView)dialog.findViewById(R.id.tvPCPPhone);
				TextView tvPCPAddress = (TextView)dialog.findViewById(R.id.tvPCPAddress);
				TextView tvPCPCity = (TextView)dialog.findViewById(R.id.tvPCPCity);
				
				tvPatName.setText(selPatient.getName());
				
				if(selPatient.medicalRecordNumber != null && !selPatient.medicalRecordNumber.isEmpty())
					tvMRN.setText(selPatient.medicalRecordNumber);
				else
					tvMRN.setVisibility(View.GONE);
				
				if (selPatient.gender != Gender.UNKNOWN){
					p_gender = selPatient.gender.toString().substring(0,1);
			        p_gender = p_gender.equals("M")?"Male":"Female";
					tvGender.setText(p_gender);
				}else{
					tvGender.setVisibility(View.GONE);
				}
				
						
				if(selPatient.address1 != null && !selPatient.address1.trim().isEmpty()){
					tvAddress1.setText(selPatient.address1);
					tvCity.setText(selPatient.city + ", "+selPatient.state+" - "+selPatient.zip);
				}else{
					tvAddress1.setVisibility(View.GONE);
					tvCity.setVisibility(View.GONE);
				}
				
				if(selPatient.address2 != null && !selPatient.address2.trim().isEmpty()){
					tvAddress2.setText(selPatient.address2);
				}else{
					tvAddress2.setVisibility(View.GONE);
				}
				
				if(selPatient.phone != null && !selPatient.phone.isEmpty()){
					String formattedNumber = PhoneNumberUtils.formatNumber(selPatient.phone);
					tvPhone.setText(formattedNumber);
					
					if(selPatient.phone.contains("x")){
						String[] parts = selPatient.phone.split("x");
						phNo = parts[0]; 
					}else{
						phNo = selPatient.phone;
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
				String dob = selPatient.dateOfBirth.trim();
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
						date1 = new SimpleDateFormat("MM/dd/yyyy").parse(selPatient.dateOfBirth);
						date2 = new SimpleDateFormat("MM/dd/yyyy").parse(currentDate);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
				    cal1.setTime(date1);
				    cal2.setTime(date2);
				    if(cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
				          factor = -1; 
				    }
				    age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
				    tvDOB.setText(selPatient.dateOfBirth+" ("+age+"yo)");
		        }
		        dialog.show();
			}
			}
		});

        UserState state = AndroidState.getInstance().getUserState();

        synchronized (state)
        {
            holder.mrnView.setText(selPatient.medicalRecordNumber);
            holder.nameView.setText(selPatient.getName());
        }
        
        

        return row;
    }
    
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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
	
	public void dgCall(final String phNo){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
		        activity.startActivity(callIntent);
		        
			}
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.setCancelable(true);
		builder.create();
		builder.show();
	}

    public static class JobHolder
    {
        public final TextView nameView;
        public final TextView mrnView;
        public final ImageView goView;

        public JobHolder(TextView mrnView, TextView nameView, ImageView goView)
        {
            this.mrnView = mrnView;
            this.nameView = nameView;
            this.goView = goView;
        }
    }
    
    public class SendPatientAccessTask extends AsyncTask{

    	private ENTMessage entmessage;
    	private EntradaApplication application; 
    	
    	public SendPatientAccessTask() {
    		entmessage = new ENTMessage();
    		application = (EntradaApplication) EntradaApplication.getAppContext();
		}
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		BundleKeys.openPatientSearch = false;
    	}
    	
		@Override
		protected Object doInBackground(Object... params) {			
			conversation.setPatientAccess(true);
			entmessage.setMessage("Granted access to patient "+conversation.getName());
			entmessage.setCustomString("1");
			entmessage.setPatientID(conversation.getPatientID());
			entmessage.setChatDialogId(conversation.getId());
			entmessage.setPassPhrase(conversation.getPassPhrase());
			try {
				EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
				Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
				APIService service = new APIService(env.getApi());
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				service.revokePatienInfoSharingPermission(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				chat.sendMessage(entmessage);
			} catch (Exception e) {
				((GroupChatManagerImpl) chat).joinGroupChat(conversation, entmessage);
			} 
			BundleKeys.openPatientSearch = true;
		}
    	
    }
  

}
