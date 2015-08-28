package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;

public class PatientInfoItemPagesAdapter extends BaseExpandableListAdapter {
	
	JSONArray jsArray;
	Activity activity;
	private String json;
	boolean[] isExtended;
	ArrayList<Integer> extenedGrpPos;
	String checkSubItem1="Sig",checkSubItem2="Status:",checkSubItem3="Severity:";;
	public static JSONObject getJSObj(String value){
		return Utils.getJSObj(value);
	}
	public void setExtenedGrpPos(ArrayList<Integer> extenedGrpPos){
		this.extenedGrpPos=extenedGrpPos;
	}
	public PatientInfoItemPagesAdapter(String json, Activity activity,int pageType){
		this.json=json;
		this.activity=activity;
		this.pageType=pageType;
		extenedGrpPos=new ArrayList<Integer>();
		try {
			if(pageType==PatientInfoViewPagerAdapter.MEDICATIONS){
			jsArray=(JSONArray) getJSObj(json).get("MEDICATION");
			}else if(pageType==PatientInfoViewPagerAdapter.ALLERGIES){
				
				jsArray=(JSONArray) getJSObj(json).get("ALLERGIES");
			}else if(pageType==PatientInfoViewPagerAdapter.PROBLEMS){
				jsArray=(JSONArray) getJSObj(json).get("PROBLEMS");
			}else if(pageType==PatientInfoViewPagerAdapter.PAST_MEDICAL){
				jsArray=(JSONArray) getJSObj(json).get("MEDSURGHX");
			}else if(pageType==PatientInfoViewPagerAdapter.LAST_HPI){
				jsArray=(JSONArray) getJSObj(json).get("PREVIOUSHPI");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public Object getChild(int arg0, int arg1) {
		if(jsArray!=null)
			try {
				jsArray.get(arg1);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return arg1;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}
	public View setLastHpiChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2){
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_lasthpi_child_item, arg2,false);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

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
	public View setPastMedicalChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2){
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_pastmedical_child_item, arg2,false);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

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
	public View setProblemsChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2){
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_problems_child_item, arg2,false);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

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
	public View setAllergiesChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2){
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_allergies_child_item, arg2,false);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

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
	
	public View setMedicaitonChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2){
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_medication_child_item, arg2,false);
		
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			
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
int pageType;
	@Override
	public View getChildView(int arg0, int childPosition,
			boolean isLastChild, View convertView, ViewGroup arg2) {
		if(pageType==PatientInfoViewPagerAdapter.MEDICATIONS){
			return setMedicaitonChildView(arg0, childPosition, isLastChild, convertView, arg2);
		}else if(pageType==PatientInfoViewPagerAdapter.ALLERGIES){
			return setAllergiesChildView(arg0, childPosition, isLastChild, convertView, arg2);
		}else if(pageType==PatientInfoViewPagerAdapter.PROBLEMS){
			return setProblemsChildView(arg0, childPosition, isLastChild, convertView, arg2);
		}else if(pageType==PatientInfoViewPagerAdapter.LAST_HPI){
			return setLastHpiChildView(arg0, childPosition, isLastChild, convertView, arg2);
		}else if(pageType==PatientInfoViewPagerAdapter.PAST_MEDICAL){
			return setPastMedicalChildView(arg0, childPosition, isLastChild, convertView, arg2);
		}
		return null;
		
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		/*if(jsArray!=null){
			isExtended=new boolean[jsArray.length()];
			for(int i=0;i<jsArray.length();i++){
				isExtended[i]=false;
			}
			return jsArray.length();
		}*/
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if(jsArray!=null)
			try {
				jsArray.get(groupPosition);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return groupPosition;
	}

	@Override
	public int getGroupCount() {
		if(jsArray!=null){
			isExtended=new boolean[jsArray.length()];
			for(int i=0;i<jsArray.length();i++){
				isExtended[i]=false;
			}
			return jsArray.length();
		}
		return 0;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int arg0, boolean isExpanded,
			View convertView, ViewGroup arg2) {
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_parent_item, arg2,false);
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
				tvShortTitle.setText(strTitle);
			}
			if(subitemCheck.contains(checkSubItem1)||subitemCheck.contains(checkSubItem2)||subitemCheck.contains(checkSubItem3)){
				if(pageType==PatientInfoViewPagerAdapter.MEDICATIONS){
				String value=subitemCheck.substring(subitemCheck.indexOf("Sig:")+4,subitemCheck.indexOf("\"",subitemCheck.indexOf("Sig:")));
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
					tShortDesc.setText(value);
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

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
