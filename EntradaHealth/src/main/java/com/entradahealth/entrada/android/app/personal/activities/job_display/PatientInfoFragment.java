package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;

public class PatientInfoFragment extends Fragment {
	private Map<String, String> clinicalTypes = new HashMap<String, String>();
	private String clinicalType;
	
	public static PatientInfoFragment newInstance(String result, String demographics, int pageType, String clinicalType, String... args){
		PatientInfoFragment demo=new PatientInfoFragment();
		Bundle b=new Bundle();
		b.putString("demographics", demographics);
		b.putString("response", result);
		b.putStringArray("patientInfo", args);
		b.putInt("pageType", pageType);
		b.putString("clinicalType", clinicalType);
		demo.setArguments(b);
		return demo;
	}
	
	String response;
	String demographics;
	JSONArray jsArray;
	int pageType;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle=getArguments();
		response=bundle.getString("response");
		JSONObject jsObj=getJSObj(response);
		demographics=bundle.getString("demographics");
		jsArray=	jsObj.names();		
		pageType=bundle.getInt("pageType");
		clinicalType = bundle.getString("clinicalType");
		if(demographics!=null) {
			clinicalTypes.put(Constants.DEMOGRAPHICS, Constants.DEMOGRAPHICS);
		}
		clinicalTypes.put(Constants.MEDICATION, Constants.MEDICATIONS);
		clinicalTypes.put(Constants.ALLERGIES, Constants.ALLERGIES);
		clinicalTypes.put(Constants.PROBLEMS, Constants.PROBLEMS);
		clinicalTypes.put(Constants.MEDSURGHX, Constants.PAST_MEDICAL_TITLE);
		clinicalTypes.put(Constants.PREVIOUSHPI, Constants.LAST_HPI_TITLE);
        
		return	setMedicationsLayout( inflater,  container,
					 savedInstanceState);
	}
	TextView pageTitle;
	ListView lvAdditionalInfo;
	PatientInfoPageNewListAdapter adapter;
	ArrayList<Integer> extenedGrpPos=new ArrayList<Integer>();
	private View setMedicationsLayout(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState){
		View view=	inflater.inflate(R.layout.patientinfofragment, container, false);
		pageTitle=(TextView)view.findViewById(R.id.tvPageTitle);
		
		lvAdditionalInfo=(ListView)view.findViewById(R.id.lvAdditionalInfo);

		adapter=new PatientInfoPageNewListAdapter(demographics, response, getActivity(),pageType, clinicalType);
		lvAdditionalInfo.setAdapter(adapter);
		pageTitle.setText(clinicalTypes.get(clinicalType));		
		return view;
		
	}

	public static JSONObject getJSObj(String value){
		return Utils.getJSObj(value);
	}
}
