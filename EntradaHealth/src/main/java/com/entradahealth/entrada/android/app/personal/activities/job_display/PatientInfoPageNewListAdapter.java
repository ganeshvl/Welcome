package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Physicians;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;

public class PatientInfoPageNewListAdapter extends BaseAdapter {
	
	JSONArray jsArray;
	Activity activity;
	private String json;
	boolean[] isExtended;
	ArrayList<Integer> extenedGrpPos;
	String checkSubItem1="Sig: ",checkSubItem2="Status: ",checkSubItem3="Severity: ";;
	SharedPreferences sp;
	private String clinicalType;
	private String demographics;
	
	public static JSONObject getJSObj(String value){
		return Utils.getJSObj(value);
	}
	public void setExtenedGrpPos(ArrayList<Integer> extenedGrpPos){
		this.extenedGrpPos=extenedGrpPos;
	}
	public PatientInfoPageNewListAdapter(String demographics, String json, Activity activity,int pageType, String clinicalType){
		this.json=json;
		this.demographics = demographics;
		this.activity=activity;
		sp=activity.getSharedPreferences("PatientInfo", Context.MODE_WORLD_READABLE);
		this.pageType=pageType;
		this.clinicalType = clinicalType;
		extenedGrpPos=new ArrayList<Integer>();
		
		try {
			if(clinicalType.equals(Constants.DEMOGRAPHICS)) {
				jsArray = new JSONArray();
				jsArray.put(getJSObj(demographics));
			} else {
				jsArray = (JSONArray) getJSObj(json).get(clinicalType);
			} 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	public View setLastHpiChildView(int arg0, View convertView, ViewGroup arg2){
		View view;
		if(convertView==null){
		 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_lasthpi_child_item, arg2,false);
		}else{
			view=convertView;
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getTag()!=null){
					if(hmLastHpiStroedValues.get(v.getTag())){
						hmLastHpiStroedValues.put((Integer) v.getTag(), false);
					}else{
						hmLastHpiStroedValues.put((Integer) v.getTag(), true);
					}
					/*if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}*/
				}else{
					hmLastHpiStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}
				/*if(v.getTag()!=null){
					if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}
				}else{
					v.setTag("true");
				}*/
				notifyDataSetChanged();
				
			}
		});
		getGroupView(arg0, view);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			if(hmLastHpiStroedValues.containsKey(arg0)&&hmLastHpiStroedValues.get(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				view.setTag(arg0);
				hmLastHpiStroedValues.put((Integer)arg0, true);
			}else{
				view.setTag(arg0);
				hmLastHpiStroedValues.put((Integer)arg0, false);
				llCompleteDesc.setVisibility(View.GONE);

			}
			/*if(view.getTag()!=null&&view.getTag().toString().equalsIgnoreCase("true")){
				llCompleteDesc.setVisibility(View.VISIBLE);
			}else{
				llCompleteDesc.setVisibility(View.GONE);
			}*/

		TextView tvLastHpilbl=(TextView)view.findViewById(R.id.tvLastHpiLbl);
		TextView tvLastHpiVal=(TextView)view.findViewById(R.id.tvLastHpiVal);


		boolean isHpi=false;
		String hpi="";
		for(int i=0;i<jsSubItem.length();i++){
			String subitemStr=jsSubItem.getString(i);
			try{
				String[] values=subitemStr.split(":");
				String subitemVal=values[0];
				if(subitemVal.contains("HPI")){
					isHpi=true;
					hpi=values[1];
				
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
		if(isHpi&&hpi!=null){
			tvLastHpiVal.setText(hpi);
			tvLastHpilbl.setVisibility(View.VISIBLE);
			tvLastHpiVal.setVisibility(View.VISIBLE);
		}else{
			tvLastHpilbl.setVisibility(View.GONE);
			tvLastHpiVal.setVisibility(View.GONE);
		}
		
	
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return view;
	}
	public View setPastMedicalChildView(int arg0, View convertView, ViewGroup arg2){
		//View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_pastmedical_child_item, arg2,false);
		View view;
		if(convertView==null){
		 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_pastmedical_child_item, arg2,false);
		}else{
			view=convertView;
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getTag()!=null){
					if(hmPastMedicalStroedValues.get(v.getTag())){
						hmPastMedicalStroedValues.put((Integer) v.getTag(), false);
					}else{
						hmPastMedicalStroedValues.put((Integer) v.getTag(), true);
					}
					/*if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}*/
				}else{
					hmPastMedicalStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}
/*				if(v.getTag()!=null){
					if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}
				}else{
					v.setTag("true");
				}*/
				notifyDataSetChanged();
				
			}
		});
		getGroupView(arg0, view);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			if(hmPastMedicalStroedValues.containsKey(arg0)&&hmPastMedicalStroedValues.get(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				view.setTag(arg0);
				hmPastMedicalStroedValues.put((Integer)arg0, true);
			}else{
				view.setTag(arg0);
				hmPastMedicalStroedValues.put((Integer)arg0, false);
				llCompleteDesc.setVisibility(View.GONE);

			}
			/*if(view.getTag()!=null&&view.getTag().toString().equalsIgnoreCase("true")){
				llCompleteDesc.setVisibility(View.VISIBLE);
			}else{
				llCompleteDesc.setVisibility(View.GONE);
			}
*/
		TextView tvHistoryNamelbl=(TextView)view.findViewById(R.id.tvHistoryNameLbl);
		TextView tvHistoryNameval=(TextView)view.findViewById(R.id.tvHistoryNameVal);


		TextView tvStartDateLbl=(TextView)view.findViewById(R.id.tvDateLbl);
		TextView tvStartDateVal=(TextView)view.findViewById(R.id.tvDateVal);

		TextView tvResolvedBylbl=(TextView)view.findViewById(R.id.tvResolvedBy);
		TextView tvResolvedByval=(TextView)view.findViewById(R.id.tvResolvedByVal);

		boolean isAllergyName=false,isResolvedBy = false,isStartDate=false;
		String medicationName="",startDate="",resolvedBy="";
		for(int i=0;i<jsSubItem.length();i++){
			String subitemStr=jsSubItem.getString(i);
			try{
				String[] values=subitemStr.split(":");
				String subitemVal=values[0];
				if(subitemVal.contains("History Name")){
					isAllergyName=true;
					medicationName=values[1];
				
				}else if(subitemVal.contains("Date")){
					isStartDate=true;
					startDate=values[1];
				}else if(subitemVal.contains("Resolved By")){
					isResolvedBy=true;
					resolvedBy=values[1];
				}

					/*tvShortDesc.setText(values[1]);
				}else{
					tvShortDesc.setText("More Details...");
				}*/
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
		if(isAllergyName&&medicationName!=null){
			tvHistoryNameval.setText(medicationName);
			tvHistoryNamelbl.setVisibility(View.VISIBLE);
			tvHistoryNameval.setVisibility(View.VISIBLE);
		}else{
			tvHistoryNamelbl.setVisibility(View.GONE);
			tvHistoryNameval.setVisibility(View.GONE);
		}
		
	
		if(isStartDate&&startDate!=null){
			tvStartDateVal.setText(startDate);
			tvStartDateLbl.setVisibility(View.VISIBLE);
			tvStartDateVal.setVisibility(View.VISIBLE);
		}else{
			tvStartDateVal.setVisibility(View.GONE);
			tvStartDateLbl.setVisibility(View.GONE);
		}
		if(isResolvedBy&&resolvedBy!=null){
			tvResolvedByval.setText(resolvedBy);
			tvResolvedBylbl.setVisibility(View.VISIBLE);
			tvResolvedByval.setVisibility(View.VISIBLE);
		}else{
			tvResolvedBylbl.setVisibility(View.GONE);
			tvResolvedByval.setVisibility(View.GONE);
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return view;
	}
	public View setProblemsChildView(int arg0,  View convertView, ViewGroup arg2){
		//View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_problems_child_item, arg2,false);
		View view;
		if(convertView==null){
		 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_problems_child_item, arg2,false);
		}else{
			view=convertView;
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getTag()!=null){
					if(hmProblemsStroedValues.get(v.getTag())){
						hmProblemsStroedValues.put((Integer) v.getTag(), false);
					}else{
						hmProblemsStroedValues.put((Integer) v.getTag(), true);
					}
					/*if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}*/
				}else{
					hmProblemsStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}
				/*if(v.getTag()!=null){
					if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}
				}else{
					v.setTag("true");
				}*/
				notifyDataSetChanged();
				
			}
		});
		getGroupView(arg0, view);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			if(hmProblemsStroedValues.containsKey(arg0)&&hmProblemsStroedValues.get(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				view.setTag(arg0);
				hmProblemsStroedValues.put((Integer)arg0, true);
			}else{
				view.setTag(arg0);
				hmProblemsStroedValues.put((Integer)arg0, false);
				llCompleteDesc.setVisibility(View.GONE);

			}
			/*if(view.getTag()!=null&&view.getTag().toString().equalsIgnoreCase("true")){
				llCompleteDesc.setVisibility(View.VISIBLE);
			}else{
				llCompleteDesc.setVisibility(View.GONE);
			}*/

		TextView tvProblemlbl=(TextView)view.findViewById(R.id.tvProblemNameLbl);
		TextView tvProblemval=(TextView)view.findViewById(R.id.tvProblemNameVal);


		TextView tvStartDateLbl=(TextView)view.findViewById(R.id.tvStartDateLbl);
		TextView tvStartDateVal=(TextView)view.findViewById(R.id.tvStartDateVal);


		boolean isAllergyName=false,isStartDate = false;
		String medicationName="",startDate="";
		for(int i=0;i<jsSubItem.length();i++){
			String subitemStr=jsSubItem.getString(i);
			try{
				String[] values=subitemStr.split(":");
				String subitemVal=values[0];
				if(subitemVal.contains("Problem Name")){
					isAllergyName=true;
					medicationName=values[1];
				
				}else if(subitemVal.contains("Start Date")){
					isStartDate=true;
					startDate=values[1];
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
		if(isAllergyName&&medicationName!=null){
			tvProblemval.setText(medicationName);
			tvProblemlbl.setVisibility(View.VISIBLE);
			tvProblemval.setVisibility(View.VISIBLE);
		}else{
			tvProblemlbl.setVisibility(View.GONE);
			tvProblemval.setVisibility(View.GONE);
		}
		
	
		if(isStartDate&&startDate!=null){
			tvStartDateVal.setText(startDate);
			tvStartDateLbl.setVisibility(View.VISIBLE);
			tvStartDateVal.setVisibility(View.VISIBLE);
		}else{
			tvStartDateVal.setVisibility(View.GONE);
			tvStartDateLbl.setVisibility(View.GONE);
		}
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return view;
	}
	public View setAllergiesChildView(int arg0,View convertView, ViewGroup arg2){
	//	View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_allergies_child_item, arg2,false);
		View view;
		if(convertView==null){
		 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_allergies_child_item, arg2,false);
		}else{
			view=convertView;
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getTag()!=null){
					if(hmMedicationsAllergiesStroedValues.get(v.getTag())){
						hmMedicationsAllergiesStroedValues.put((Integer) v.getTag(), false);
					}else{
						hmMedicationsAllergiesStroedValues.put((Integer) v.getTag(), true);
					}
					/*if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}*/
				}else{
					hmMedicationsAllergiesStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}
				/*if(v.getTag()!=null){
					if(hmMedicationsStroedValues.get(v.getTag())){
						hmMedicationsStroedValues.put((Integer) v.getTag(), true);
					}else{
						hmMedicationsStroedValues.put((Integer) v.getTag(), false);
					}
					if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}
				}else{
					hmMedicationsStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}*/
				notifyDataSetChanged();
				
			}
		});
		getGroupView(arg0, view);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			if(hmMedicationsAllergiesStroedValues.containsKey(arg0)&&hmMedicationsAllergiesStroedValues.get(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				view.setTag(arg0);
				hmMedicationsAllergiesStroedValues.put((Integer)arg0, true);
			}else{
				view.setTag(arg0);
				hmMedicationsAllergiesStroedValues.put((Integer)arg0, false);
				llCompleteDesc.setVisibility(View.GONE);

			}
			/*if(view.getTag()!=null&&view.getTag().toString().equalsIgnoreCase("true")){
				llCompleteDesc.setVisibility(View.VISIBLE);
			}else{
				llCompleteDesc.setVisibility(View.GONE);
			}*/

		TextView tvAllergylbl=(TextView)view.findViewById(R.id.tvAllergyNamelbl);
		TextView tvAllergyVal=(TextView)view.findViewById(R.id.tvAllergyNameVal);


		TextView tvStartDateLbl=(TextView)view.findViewById(R.id.tvStartDateLbl);
		TextView tvStartDateVal=(TextView)view.findViewById(R.id.tvStartDateVal);

		TextView tvReaction=(TextView)view.findViewById(R.id.tvReaction);
		TextView tvReactionval=(TextView)view.findViewById(R.id.tvReactionVal);

		boolean isAllergyName=false,isReaction = false,isStartDate=false;
		String medicationName="",startDate="",reaction="";
		for(int i=0;i<jsSubItem.length();i++){
			String subitemStr=jsSubItem.getString(i);
			try{
				String[] values=subitemStr.split(":");
				String subitemVal=values[0];
				if(subitemVal.contains("Allergy Name")){
					isAllergyName=true;
					medicationName=values[1];
				
				}else if(subitemVal.contains("Start Date")){
					isStartDate=true;
					startDate=values[1];
				}else if(subitemVal.contains("Reaction")){
					isReaction=true;
					reaction=values[1];
				}

					/*tvShortDesc.setText(values[1]);
				}else{
					tvShortDesc.setText("More Details...");
				}*/
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	
		if(isAllergyName&&medicationName!=null){
			tvAllergyVal.setText(medicationName);
			tvAllergylbl.setVisibility(View.VISIBLE);
			tvAllergyVal.setVisibility(View.VISIBLE);
		}else{
			tvAllergylbl.setVisibility(View.GONE);
			tvAllergyVal.setVisibility(View.GONE);
		}
		
	
		if(isStartDate&&startDate!=null){
			tvStartDateVal.setText(startDate);
			tvStartDateLbl.setVisibility(View.VISIBLE);
			tvStartDateVal.setVisibility(View.VISIBLE);
		}else{
			tvStartDateVal.setVisibility(View.GONE);
			tvStartDateLbl.setVisibility(View.GONE);
		}
		if(isReaction&&reaction!=null){
			tvReactionval.setText(reaction);
			tvReaction.setVisibility(View.VISIBLE);
			tvReactionval.setVisibility(View.VISIBLE);
		}else{
			tvReaction.setVisibility(View.GONE);
			tvReactionval.setVisibility(View.GONE);
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return view;
	}
	
	static ArrayList<Integer>  storedPos=new ArrayList<Integer>();
	public static HashMap<Integer,Boolean> hmMedicationsStroedValues=new HashMap<Integer,Boolean>();
	public static HashMap<Integer,Boolean> hmMedicationsAllergiesStroedValues=new HashMap<Integer,Boolean>();
	public static HashMap<Integer,Boolean> hmProblemsStroedValues=new HashMap<Integer,Boolean>();
	public static HashMap<Integer,Boolean> hmPastMedicalStroedValues=new HashMap<Integer,Boolean>();
	public static HashMap<Integer,Boolean> hmLastHpiStroedValues=new HashMap<Integer,Boolean>();
	
	public View setMedicaitonChildView(int arg0,
			 View convertView, ViewGroup arg2){
		View view;
		if(convertView==null){
		 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_medication_child_item, arg2,false);
		}else{
			view=convertView;
		}
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(v.getTag()!=null){
					if(hmMedicationsStroedValues.get(v.getTag())){
						hmMedicationsStroedValues.put((Integer) v.getTag(), false);
					}else{
						hmMedicationsStroedValues.put((Integer) v.getTag(), true);
					}
					/*if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}*/
				}else{
					hmMedicationsStroedValues.put((Integer) v.getTag(), true);
//					v.setTag("true");
				}
				/*if(v.getTag()!=null){
					if(v.getTag().toString().equalsIgnoreCase("true")){
						v.setTag("false");
					}else{
						v.setTag("true");
					}
				}else{
					v.setTag("true");
				}*/
				notifyDataSetChanged();
				
			}
		});
		getGroupView(arg0, view);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			if(hmMedicationsStroedValues.containsKey(arg0)&&hmMedicationsStroedValues.get(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				view.setTag(arg0);
				hmMedicationsStroedValues.put((Integer)arg0, true);
			}else{
				view.setTag(arg0);
				hmMedicationsStroedValues.put((Integer)arg0, false);
				llCompleteDesc.setVisibility(View.GONE);

			}
			/*if((view.getTag()!=null&&view.getTag().toString().equalsIgnoreCase("true"))||storedPos.contains(arg0)){
				llCompleteDesc.setVisibility(View.VISIBLE);
				storedPos.add(arg0);
			}else{
				llCompleteDesc.setVisibility(View.GONE);
				if(storedPos.contains(arg0)){
					storedPos.remove(Integer.valueOf(arg0));
				}
			}
			*/
			
		TextView tvMedicationLbl=(TextView)view.findViewById(R.id.tvMedicationNameLbl);
		TextView tvMedicationNameVal=(TextView)view.findViewById(R.id.tvMedicationNameVal);



		TextView tvGenericNamelbl=(TextView)view.findViewById(R.id.tvGenericNamelbl);		
		TextView tvGenericNameVal=(TextView)view.findViewById(R.id.tvGenericNameVal);

		TextView tvStopDateLbl=(TextView)view.findViewById(R.id.tvStopDateLbl);
		TextView tvStopDateVal=(TextView)view.findViewById(R.id.tvStopDateVal);

		TextView tvStartDateLbl=(TextView)view.findViewById(R.id.tvStartDateLbl);
		TextView tvStartDateVal=(TextView)view.findViewById(R.id.tvStartDateVal);

		TextView tvPrescribedElse=(TextView)view.findViewById(R.id.tvPrescribedElse);
		TextView tvPrescribedElseVal=(TextView)view.findViewById(R.id.tvPrescribedElseVal);

		TextView tvQuantity=(TextView)view.findViewById(R.id.tvQuantity);
		TextView tvQuantityVal=(TextView)view.findViewById(R.id.tvQuantityVal);
		TextView tvRefills=(TextView)view.findViewById(R.id.tvRefills);
		TextView tvRefillsVal=(TextView)view.findViewById(R.id.tvRefillsVal);
		boolean isMedicationName=false,isGenericName = false,isStartDate=false,isStopDate=false,isPreElse=false,isQuantity=false,
				isRefills=false,isSig=false;
		String medicationName="",genericName="",startDate="",stopDate="",sig="",quantity="",refills="",presElse = "";
		for(int i=0;i<jsSubItem.length();i++){
			String subitemStr=jsSubItem.getString(i);
			try{
				String[] values=subitemStr.split(":");
				String subitemVal=values[0];
				if(subitemVal.contains("Medication Name")){
					isMedicationName=true;
					medicationName=values[1];
				
				}else if(subitemVal.contains("Generic Name")){
					isGenericName=true;
					genericName=values[1];
				}else if(subitemVal.contains("Sig")){
					isSig=true;
					sig=values[1];
				}
				else if(subitemVal.contains("Quantity")){
					isQuantity=true;
					quantity=values[1];
				}else if(subitemVal.contains("Start Date")){
					isStartDate=true;
					startDate=values[1];
				}else if(subitemVal.contains("Stop Date")){
					isStopDate=true;
					stopDate=values[1];
				}else if(subitemVal.contains("Prescribed Elsewhere")){
					isPreElse=true;
					presElse=values[1];
				}else if(subitemVal.contains("Refills")){
					isRefills=true;
					refills=values[1];
				}
					/*tvShortDesc.setText(values[1]);
				}else{
					tvShortDesc.setText("More Details...");
				}*/
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(isGenericName&&genericName!=null){
			tvGenericNameVal.setText(genericName);
			tvGenericNamelbl.setVisibility(View.VISIBLE);
			tvGenericNameVal.setVisibility(View.VISIBLE);
		}else{
			tvGenericNamelbl.setVisibility(View.GONE);
			tvGenericNameVal.setVisibility(View.GONE);
		}
		if(isMedicationName&&medicationName!=null){
			tvMedicationNameVal.setText(medicationName);
			tvMedicationLbl.setVisibility(View.VISIBLE);
			tvMedicationNameVal.setVisibility(View.VISIBLE);
		}else{
			tvMedicationLbl.setVisibility(View.GONE);
			tvMedicationNameVal.setVisibility(View.GONE);
		}
		
		if(isStopDate&&stopDate!=null){
			tvStopDateVal.setText(stopDate);
			tvStopDateLbl.setVisibility(View.VISIBLE);
			tvStopDateVal.setVisibility(View.VISIBLE);
		}else{
			tvStopDateLbl.setVisibility(View.GONE);
			tvStopDateVal.setVisibility(View.GONE);
		}
		if(isStartDate&&startDate!=null){
			tvStartDateVal.setText(startDate);
			tvStartDateLbl.setVisibility(View.VISIBLE);
			tvStartDateVal.setVisibility(View.VISIBLE);
		}else{
			tvStartDateVal.setVisibility(View.GONE);
			tvStartDateLbl.setVisibility(View.GONE);
		}
		if(isPreElse&&presElse!=null){
			tvPrescribedElseVal.setText(presElse);
			tvPrescribedElse.setVisibility(View.VISIBLE);
			tvPrescribedElseVal.setVisibility(View.VISIBLE);
		}else{
			tvPrescribedElse.setVisibility(View.GONE);
			tvPrescribedElseVal.setVisibility(View.GONE);
		}
		if(isQuantity&&quantity!=null){
			tvQuantityVal.setText(quantity);
			tvQuantity.setVisibility(View.VISIBLE);
			tvQuantityVal.setVisibility(View.VISIBLE);
		}else{
			tvQuantity.setVisibility(View.GONE);
			tvQuantityVal.setVisibility(View.GONE);
		}
		if(isRefills&&refills!=null){
			tvRefillsVal.setText(refills);
			tvRefills.setVisibility(View.VISIBLE);
			tvRefillsVal.setVisibility(View.VISIBLE);
		}else{
			tvRefills.setVisibility(View.GONE);
			tvRefillsVal.setVisibility(View.GONE);
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return view;

	}
	String titleize(String source){
		source=source.toLowerCase(Locale.US);
        boolean cap = true;
        char[]  out = source.toCharArray();
        int i, len = source.length();
        for(i=0; i<len; i++){
            if((Character.isWhitespace(out[i])|| out[i]==','||(out[i]=='('))){
                cap = true;
                continue;
            }
            if(cap){
                out[i] = Character.toUpperCase(out[i]);
                cap = false;
            }
        }
        return new String(out);
    }
	int pageType;
	
	
	public View getGroupView(int arg0, 
			View view) {
//		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_parent_item, arg2,false);
//		if(!extenedGrpPos.contains(arg0)){
		try {
			
			JSONObject jsob=jsArray.getJSONObject(arg0);
			String subitemCheck=jsob.getString("Data");
			
			JSONObject jsData=Utils.getJSObj(subitemCheck);
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

//			LinearLayout llShortDesc=(LinearLayout)view.findViewById(R.id.llShortDesc);
			LinearLayout llParentInfo=(LinearLayout)view.findViewById(R.id.llParentInfo);
			llParentInfo.setTag(arg0);
			TextView tvShortTitle=(TextView)view.findViewById(R.id.tvShortTitle);
			TextView tShortDesc=(TextView)view.findViewById(R.id.tvShortDesc);
			String strTitle=jsData.getString("item");
			if(strTitle!=null){
				tvShortTitle.setText(titleize(strTitle));
			}
			if(subitemCheck.contains(checkSubItem1)||subitemCheck.contains(checkSubItem2)||subitemCheck.contains(checkSubItem3)){
				if(clinicalType.equalsIgnoreCase(Constants.MEDICATION)){
				String value=titleize(subitemCheck.substring(subitemCheck.indexOf("Sig:")+4,subitemCheck.indexOf("\"",subitemCheck.indexOf("Sig:"))));
				tShortDesc.setText(value);
				}else{
					String fvalue="";
					if(subitemCheck.contains(checkSubItem1)){
						fvalue=checkSubItem1;
					}
					if(subitemCheck.contains(checkSubItem2)){
						fvalue=checkSubItem2;
					}
					if(subitemCheck.contains(checkSubItem3)){
						fvalue=checkSubItem3;
					}
					int initIndex=subitemCheck.indexOf("subitem");
					String value=subitemCheck.substring(subitemCheck.indexOf(fvalue,initIndex),subitemCheck.indexOf("\"",subitemCheck.indexOf(fvalue,initIndex)));
					tShortDesc.setText(titleize(value));
				}
				
			}else{
				tShortDesc.setText("More Details...");
			}
			
		}catch(Exception e){
				
			}
		/*}else{
			view.setVisibility(View.GONE);
		}*/

		return view;
		

	}

	protected View setDemographicsChildView(int arg0, View convertView, ViewGroup arg2) {
		View view;
		TextView address = null, phoneNo = null, pcp = null, refp = null;
		if(convertView==null){
			 view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_demographics_item, arg2,false);
			 address = (TextView) view.findViewById(R.id.address);
			 phoneNo = (TextView) view.findViewById(R.id.phonenum);
			 refp = (TextView) view.findViewById(R.id.referringPhysician);
			 pcp = (TextView) view.findViewById(R.id.primcaryCarePhysician);
		}else{
			view=convertView;
		}
		try {
			JSONObject json = new JSONObject(demographics);
			address.setText(json.getString("Address1") + " "
					+ json.getString("Address2") + " ,"
					+ json.getString("City") + " ," + json.getString("State")
					+ " " + json.getString("Zip"));
			phoneNo.setText(json.getString("Phone1"));
			long pcpId = json.getLong("PrimaryCareProviderID");
			if(pcpId!=0) {
				UserState state = AndroidState.getInstance().getUserState();
				Account account = state.getCurrentAccount();
				DomainObjectProvider provider = state.getProvider(account);
				List<Physicians> physicians= provider.getPhysicians(pcpId);
				if(physicians.size()>0){
					pcp.setText(physicians.get(0).getName());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}

	@Override
	public int getCount() {
		if(jsArray.length()>0)return jsArray.length();
		return 0;
	}
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		if(clinicalType.equalsIgnoreCase(Constants.DEMOGRAPHICS)) {
			return setDemographicsChildView(arg0, arg1, arg2);
		}else if(clinicalType.equalsIgnoreCase(Constants.MEDICATION)){
			return setMedicaitonChildView(arg0, arg1, arg2);
		}else if(clinicalType.equalsIgnoreCase(Constants.ALLERGIES)){
			return setAllergiesChildView(arg0, arg1, arg2);
		}else if(clinicalType.equalsIgnoreCase(Constants.PROBLEMS)){
			return setProblemsChildView(arg0, arg1, arg2);
		}else if(clinicalType.equalsIgnoreCase(Constants.PREVIOUSHPI)){
			return setLastHpiChildView(arg0, arg1, arg2);
		}else if(clinicalType.equalsIgnoreCase(Constants.MEDSURGHX)){
			return setPastMedicalChildView(arg0, arg1, arg2);
		}
		return null;
		
	}

}
