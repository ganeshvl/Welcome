package com.entradahealth.entrada.android.app.personal.activities.job_display;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;

public class DeprectedMedicaitonPageAdapter extends BaseAdapter {
	private String json;
	JSONArray jsArray;
	Activity activity;
	boolean[] isExtended;
	public static JSONObject getJSObj(String value){
		return Utils.getJSObj(value);
	}
	public DeprectedMedicaitonPageAdapter(String json, Activity activity){
		this.json=json;
		this.activity=activity;
		try {
			jsArray=(JSONArray) getJSObj(json).get("MEDICATION");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
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
	public Object getItem(int arg0) {
		if(jsArray!=null)
			try {
				jsArray.get(arg0);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View view=	activity.getLayoutInflater().inflate(R.layout.patientinfo_fragment_item, arg2,false);
		try {
			JSONObject jsob=jsArray.getJSONObject(arg0);
			JSONObject jsData=Utils.getJSObj(jsob.getString("Data"));
			JSONArray jsSubItem=jsData.getJSONArray("subitem");

			LinearLayout llShortDesc=(LinearLayout)view.findViewById(R.id.llShortDesc);
			LinearLayout llCompleteDesc=(LinearLayout)view.findViewById(R.id.llCompleteDesc);
			LinearLayout llParentInfo=(LinearLayout)view.findViewById(R.id.llParentInfo);
			if(/*llParentInfo.getTag()!=null*/isExtended[arg0]){
				llCompleteDesc.setVisibility(View.VISIBLE);
				/*if((Boolean)llParentInfo.getTag()){
				llCompleteDesc.setVisibility(View.VISIBLE);
				}else{
					llCompleteDesc.setVisibility(View.INVISIBLE);
				}*/
			}else{
//				llParentInfo.setTag(false);
				llCompleteDesc.setVisibility(View.INVISIBLE);
			}
			llParentInfo.setTag(arg0);

			
			llParentInfo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Integer val=(Integer) v.getTag();
					if(isExtended[val]){
						isExtended[val]=false;
					}else{
						isExtended[val]=true;
					}
					/*Boolean value=(Boolean) v.getTag();
					if(value){
						v.setTag(false);
					}else{
						v.setTag(true);
					}*/
					notifyDataSetChanged();
					notifyDataSetInvalidated();
					
				}
			});
			

			TextView tvShortTitle=(TextView)view.findViewById(R.id.tvShortTitle);
			String strTitle=jsData.getString("item");
			if(strTitle!=null){
				tvShortTitle.setText(strTitle);
			}
			TextView tvShortDesc=(TextView)view.findViewById(R.id.tvShortDesc);
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
					if(e instanceof NullPointerException){
						tvShortDesc.setText(subitemStr);
					}
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
				tvMedicationLbl.setVisibility(View.VISIBLE);
				tvMedicationNameVal.setVisibility(View.VISIBLE);
			}else{
				tvMedicationLbl.setVisibility(View.GONE);
				tvMedicationNameVal.setVisibility(View.GONE);
			}
			if(isSig&&sig!=null){
				tvShortDesc.setText(sig);
			}else{
				tvShortDesc.setText("More Details...");
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
				tvStopDateVal.setVisibility(View.VISIBLE);
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

}
