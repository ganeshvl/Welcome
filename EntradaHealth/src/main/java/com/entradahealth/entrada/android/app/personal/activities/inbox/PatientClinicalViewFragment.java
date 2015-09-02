package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.job_display.PatientInfoPageNewListAdapter;
import com.entradahealth.entrada.android.app.personal.activities.job_display.PatientInfoViewPagerAdapter;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Gender;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.viewpagerindicator.PageIndicator;

public class PatientClinicalViewFragment extends Fragment {

    private PatientInfoViewPagerAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;
    private String p_name = "UNKNOWN", p_mrn = "99999999", p_dob = "01/01/01", p_gender = "Unknown";
    private long patient_id;
    private UserState state;
    private Account currentAccount = null;
    private DomainObjectReader reader;
    private Patient patient;
    private TextView tvPatName, tvMRN, tvPatSex, tvPatDOB;
    private NewMessageFragment fragment;
    private EntradaApplication application;
    private ENTConversation conversation;
    private APIService service;
    private View view;
    private ProgressBar progressBar;
    
	public PatientClinicalViewFragment(NewMessageFragment fragment) {
		this.fragment = fragment;  
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (EntradaApplication) EntradaApplication.getAppContext();
		Bundle bundle = this.getArguments();		
		patient_id = bundle.getLong("patient_id");
		conversation = (ENTConversation) bundle.getSerializable("conversation");
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().setTitle("Patient");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);
		state = AndroidState.getInstance().getUserState();
		currentAccount = state.getCurrentAccount();
		progressBar.setVisibility(View.VISIBLE);
		if(currentAccount != null){
			reader = state.getProvider(currentAccount);
			this.patient = reader.getPatient(patient_id);
			if(patient!=null) {
				renderView(view);
			} else {
				new GetPatientDemographics(view).execute();	
			}
		} else {
			if(conversation!=null){
				new GetPatientDemographics(view).execute();
			}
		}
		
	};

	@Override
	public void onStart() {
		super.onStart();
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
		try {
			service = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		view = inflater.inflate(R.layout.sm_patient_page, container, false);
		mPager = (ViewPager) view.findViewById(R.id.pager);
		mIndicator = (PageIndicator) view.findViewById(R.id.indicator);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		return view;
	}
	
	class GetPatientDemographics extends AsyncTask{

		private View view;
		
		public GetPatientDemographics(View view) {
			this.view = view;
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub

			String responseData;
			try {
				responseData = service.getDemographicInfo(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId(), patient_id);
				JSONObject json = new JSONObject(responseData);
				patient = new Patient(json.getLong("PatientID"),
						json.getString("MRN"), json.getString("FirstName"),
						json.getString("MI"), json.getString("LastName"),
						json.getString("DOB"), json.getString("Gender"),
						json.getString("ClinicID"), json.getString("Address1"),
						json.getString("Address2"), json.getString("City"),
						json.getString("State"), json.getString("Zip"),
						json.getString("Phone1"));
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(patient!=null) {
				renderView(view);
			}
		}
	}  

	private void renderView(View view) {
		tvPatName = (TextView) view.findViewById(R.id.patientNameText);
        tvMRN = (TextView) view.findViewById(R.id.patientMRNText);
        tvPatSex = (TextView) view.findViewById(R.id.patientSex);
        tvPatDOB = (TextView) view.findViewById(R.id.patientDOB);

        tvPatName.setText(patient.getName());
        SharedPreferences spPatientInfo= getActivity().getSharedPreferences("PatientInfoPage", Context.MODE_WORLD_READABLE);
        if(!spPatientInfo.getString("PatientName", "").equalsIgnoreCase(patient.getName())){
        	PatientInfoPageNewListAdapter.hmMedicationsAllergiesStroedValues.clear();
			PatientInfoPageNewListAdapter.hmMedicationsStroedValues.clear();
			PatientInfoPageNewListAdapter.hmProblemsStroedValues.clear();
			PatientInfoPageNewListAdapter.hmPastMedicalStroedValues.clear();
			PatientInfoPageNewListAdapter.hmLastHpiStroedValues.clear();
        }
		spPatientInfo.edit().putString("PatientName", patient.getName()).commit();            
        if (patient.gender != Gender.UNKNOWN)
        {
            p_name = patient.getName();
            p_gender = patient.gender.toString().substring(0,1);
            p_gender = p_gender.equals("M")?"Male":"Female";
            tvPatSex.setText(p_gender);
        }else{
        	tvPatSex.setVisibility(View.GONE);
        }
        
        String dob = this.patient.dateOfBirth.trim();
        if(dob.trim().equals("") || dob.trim().equals(null)){
        	p_dob = "01/01/01";
        	tvPatDOB.setVisibility(View.GONE);
        }else{
        	p_dob = dob.trim();
        	tvPatDOB.setText(p_dob);
        }
        String mrn = this.patient.medicalRecordNumber.trim();
        p_mrn = mrn.trim();

        if(mrn.isEmpty()){
        	tvMRN.setVisibility(View.GONE);
        }else{
        	tvMRN.setText(mrn);
        }
        PatientInfoAysncTask patclinicalTask = new PatientInfoAysncTask();
        patclinicalTask.execute();
	}
	
    String response, demographics;
    int hasClinicals = 0;
    private class PatientInfoAysncTask extends AsyncTask<Void, Void, String>{
    	
    	@Override
    	protected void onPostExecute(String result) {
    		progressBar.setVisibility(View.GONE);
    		if(!result.contains("exceptionjobdisplay")){
    			try {
					JSONObject js=new JSONObject(result);
					if(js.length() == 0){
						hasClinicals = 1;
					}else{
						hasClinicals = 2;
					mAdapter=new PatientInfoViewPagerAdapter(demographics, ((SecureMessaging)getActivity()).getSupportFragmentManager(), result, new String[]{p_name,p_mrn,p_dob,p_gender});
					int count = js.length();
					if(demographics!=null){
						count = count+1;
					}
					mAdapter.setCount(count);
					mPager.setAdapter(mAdapter);
					mIndicator.setViewPager(mPager);					
					Log.e("key and pairs in result-->"+js.names().length(),""+js.length());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		}
    	};

		@Override
		protected String doInBackground(Void... params) {
			
			try {
				EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
				Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
				APIService service = new APIService(env.getApi());
				response=service.getClinicalInfo(patient.id);
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				demographics = service.getDemographicInfo(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), patient.id);
				JSONObject js=new JSONObject(response);
				//hasClinicals = true;
				if(js.length() == 0){
					hasClinicals = 1;
				}else{
				js.length();
				Log.e("key and pairs in result-->"+js.names().length(),""+js.length());
				}
					
				
			} catch (MalformedURLException e) {
				response="exceptionjobdisplay"+e.toString();
				e.printStackTrace();
			} catch (Exception e) {
				hasClinicals = 0;
				response="exceptionjobdisplay"+e.toString();
				e.printStackTrace();
			}

			return response;
		}
    	
    }

    @Override
    public void onStop() {
    	super.onStop();
    	fragment.onResume();
    }
}
