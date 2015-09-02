package com.entradahealth.entrada.android.app.personal.activities.schedule;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleConstants;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Gender;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.remote.APIService;



/**
 * DemoGraphic Fragment, holds all the UI components in the for the Patient details.
 */
public class DemoGraphicFragment extends Fragment {

	private static String TAG = DemoGraphicFragment.class.getSimpleName();
	private ScheduleActivity mActivity;

	private APIService mService;
	private EntradaApplication mApplication;
	private DomainObjectProvider mProvider; 
	private Account account = null;

	private TextView mPatientName;
	private TextView mPatientMRN;
	private TextView mPatientGender;
	private TextView mPatientDOB;
	private TextView mPatientAddress1;
	private TextView mPatientAddress2;
	private TextView mPatientCity;
	private TextView mPatientPhone;
	private Patient mPatient;
	private String mPatientId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Getting Activity reference. 
		mActivity = ((ScheduleActivity)getActivity());
		mActivity.getActionBar().setDisplayShowCustomEnabled(true);
		mApplication = (EntradaApplication) EntradaApplication.getAppContext();
		//Getting Application Environment.
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(mApplication.getStringFromSharedPrefs(Constants.ENVIRONMENT));
		try {
			mService = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		final UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			account = state.getCurrentAccount();
			mProvider = state.getProvider(account);
		}

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mPatient = (Patient) bundle.getSerializable(Constants.PATIENTID_OBJECT);
			mPatientId = bundle.getString(Constants.PATIENTID, null);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = null;
		// Inflate the layout for this fragment
		view  = inflater.inflate(R.layout.fragment_demographic, container, false);
		initViews(view);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	/**
	 * Initializing UI components for this fragment.  
	 * @param view
	 */
	private void initViews(View view) {

		mPatientName = (TextView)view.findViewById(R.id.tv_pt_name);
		mPatientMRN = (TextView)view.findViewById(R.id.tv_pt_mrn);
		mPatientGender = (TextView)view.findViewById(R.id.tv_pt_gender);
		mPatientDOB = (TextView)view.findViewById(R.id.tv_pt_dob);
		mPatientAddress1 = (TextView)view.findViewById(R.id.tv_address1);
		mPatientAddress2 = (TextView)view.findViewById(R.id.tv_address2);
		mPatientCity = (TextView)view.findViewById(R.id.tv_city);
		mPatientPhone = (TextView)view.findViewById(R.id.tv_phone1);
		RelativeLayout layout = (RelativeLayout)view.findViewById(R.id.parent);
		LinearLayout layout_ll = (LinearLayout)view.findViewById(R.id.parent2);
		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// to avoid the background fragments click functionality, just added empty listener for the parent in the  active fragment.
			}
		});
		
		layout_ll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// to avoid the background fragments click functionality, just added empty listener for the parent in the  active fragment.
			}
		});

		//Displaying the Patient data
		if(mPatient !=null){
			setpatientData(mPatient);
		}else{
			if(mPatientId !=null){
				new GetPatientDemographicInfo(Long.parseLong(mPatientId)).execute();
			}else{
				Toast.makeText(getActivity(), getResources().getString(R.string.no_patient_available), Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	/**
	 * Async task to get the Patient info from the server.
	 */
	public class GetPatientDemographicInfo extends AsyncTask<Void, Void, Patient>{

		private long patientId;
		private Patient patientDetails;
		private ProgressDialog dialog;

		GetPatientDemographicInfo(long patientId){
			this.patientId = patientId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(mActivity,"",getResources().getString(R.string.please_wait));
			dialog.setCancelable(false);
		}

		@Override
		protected Patient doInBackground(Void... params) {
			UserState state = AndroidState.getInstance().getUserState();
			Account account = state.getCurrentAccount();
			if(account!=null){
				DomainObjectReader reader = state.getProvider(account);
				try{
					return	patientDetails = reader.getPatientDemographicInfo(patientId);
				}catch(Exception e){
					if(Constants.LOG)Log.e("Number format exception", e.getMessage());
				}
			} else {
				if(Constants.LOG)Log.e("GetPatientDemographicInfo", "No User Found");
			}
			return null;
		}
		//TO-DO Implement sorting order... for names
		@Override
		protected void onPostExecute(Patient patient) {
			super.onPostExecute(patient);
			if(dialog.isShowing()){
				dialog.dismiss();
			}
			if(patient != null){ // Close the dialog
				setpatientData(patient);
			}else{
				if(dialog.isShowing()){
					dialog.dismiss();
				}
				return;
			}
		}
	}

	/**
	 * Setting the Patient data to the UI fields
	 * @param patient
	 */
	private void setpatientData(Patient patient) {
		String phNo = null;	
		String gender;
		//Name
		mPatientName.setText(patient.getName());
		//MRN
		if(patient.medicalRecordNumber != null && !patient.medicalRecordNumber.isEmpty()){
			mPatientMRN.setText(patient.medicalRecordNumber);
		}else{
			mPatientMRN.setVisibility(View.GONE);
		}
		//Gender
		if (patient.gender != Gender.UNKNOWN){
			gender = patient.gender.toString().substring(0,1);
			gender = gender.equals("M")?getResources().getString(R.string.male):getResources().getString(R.string.female);
			mPatientGender.setText(gender);
		}else{
			mPatientGender.setVisibility(View.GONE);
		}
		//Address1		
		if(patient.address1 != null && !patient.address1.trim().isEmpty()){
			mPatientAddress1.setText(patient.address1);
			mPatientCity.setText(patient.city + ", "+patient.state+" - "+patient.zip);
		}else{
			mPatientAddress1.setVisibility(View.GONE);
			mPatientCity.setVisibility(View.GONE);
		}
		//Address2
		if(patient.address2 != null && !patient.address2.trim().isEmpty()){
			mPatientAddress2.setText(patient.address2);
		}else{
			mPatientAddress2.setVisibility(View.GONE);
		}
		//Phone
		if(patient.phone != null && !patient.phone.isEmpty()){
			String formattedNumber = PhoneNumberUtils.formatNumber(patient.phone);
			mPatientPhone.setText(formattedNumber);

			if(patient.phone.contains("x")){
				String[] parts = patient.phone.split("x");
				phNo = parts[0]; 
			}else{
				phNo = patient.phone;
			}
		}else{
			mPatientPhone.setVisibility(View.GONE);
		}
		
		String dob = patient.dateOfBirth.trim();
		//Check if Server or Generic job
        if(dob.trim().equals("") || dob.trim().equals(null))
        	mPatientDOB.setVisibility(View.GONE);
        else{
        	setpatientDate(mPatientDOB, patient);
			
        }
	}

	/**
	 * Calculate the patient age based on current date and set the DOB.
	 * @param mPtDOB
	 * @param patient
	 */
	private void setpatientDate(TextView mPtDOB, Patient patient) {
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
			date1 = new SimpleDateFormat(ScheduleConstants.SECTIONLIST_DATE_FORMAT).parse(patient.dateOfBirth);
			date2 = new SimpleDateFormat(ScheduleConstants.SECTIONLIST_DATE_FORMAT).parse(currentDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    
	    cal1.setTime(date1);
	    cal2.setTime(date2);
	    if(cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
	          factor = -1; 
	    }
	    age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
	    mPtDOB.setText(patient.dateOfBirth+" ("+age+" YO)");
		
	}

}
