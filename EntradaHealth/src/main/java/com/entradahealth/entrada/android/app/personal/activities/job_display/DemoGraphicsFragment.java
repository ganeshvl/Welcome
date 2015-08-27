package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.entradahealth.entrada.android.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DemoGraphicsFragment extends Fragment {
	
	private Map<String, String> clinicalTypes = new HashMap<String, String>();
	
	
	public static DemoGraphicsFragment newInstance(String result,int pageType,String... args){
		DemoGraphicsFragment demo=new DemoGraphicsFragment();
		Bundle b=new Bundle();
		b.putString("response", result);
		b.putStringArray("patientInfo", args);
		b.putInt("pageType", pageType);
		demo.setArguments(b);
		return demo;
	}
	public class AdditionalInfoObj{
		public AdditionalInfoObj(String key, String value, boolean isPresented) {
			this.key = key;
			this.value = value;
			this.isPresented = isPresented;
		}
		private String key;
		private String value;
		private boolean isPresented;
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public boolean isPresented() {
			return isPresented;
		}
		public void setPresented(boolean isPresented) {
			this.isPresented = isPresented;
		}
		
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle=getArguments();


		String[] values=bundle.getStringArray("patientInfo");
		View view=	inflater.inflate(R.layout.demographics, container, false);
		TextView tvName=(TextView)view.findViewById(R.id.tvNameValue);
		TextView tvMrn=(TextView)view.findViewById(R.id.tvMrnValue);
		TextView tvDob=(TextView)view.findViewById(R.id.tvDobVal);
		TextView tvSex=(TextView)view.findViewById(R.id.tvSexVal);
		TextView tvAdditionalInfo=(TextView)view.findViewById(R.id.tvAddInfoLbl);

		tvName.setText(values[0]);
		tvMrn.setText(values[1]);
		tvDob.setText(values[2]);
		tvSex.setText(values[3]);
		ArrayList<AdditionalInfoObj> alPatientInfo=new ArrayList<AdditionalInfoObj>();
		AdditionalInfoObj addObj=new AdditionalInfoObj(Constants.MEDICATION, Constants.MEDICATIONS,false);
		alPatientInfo.add(addObj);
		addObj=new AdditionalInfoObj(Constants.ALLERGIES, Constants.ALLERGIES,false);
		alPatientInfo.add(addObj);
		addObj=new AdditionalInfoObj(Constants.PROBLEMS, Constants.PROBLEMS,false);
		alPatientInfo.add(addObj);
		addObj=new AdditionalInfoObj(Constants.MEDSURGHX, Constants.PAST_MEDICAL_TITLE,false);
		alPatientInfo.add(addObj);
		addObj=new AdditionalInfoObj(Constants.PREVIOUSHPI, Constants.LAST_HPI_TITLE,false);
		alPatientInfo.add(addObj);
		
		/*clinicalTypes.put(Constants.MEDICATION, Constants.MEDICATIONS);
		clinicalTypes.put(Constants.ALLERGIES, Constants.ALLERGIES);
		clinicalTypes.put(Constants.PROBLEMS, Constants.PROBLEMS);
		clinicalTypes.put(Constants.MEDSURGHX, Constants.PAST_MEDICAL_TITLE);
		clinicalTypes.put(Constants.PREVIOUSHPI, Constants.LAST_HPI_TITLE);*/
		try{
			String response=bundle.getString("response");
			JSONObject jsObj=getJSObj(response);
			
			JSONArray jsArray=	jsObj.names();
			JSONArray newJSArray = new JSONArray();
			int count=0;
			for(int i=0; i<jsArray.length();i++) {
				String addInfo=jsArray.getString(i);
				for(AdditionalInfoObj addInfoObj: alPatientInfo){
					if(addInfo.equalsIgnoreCase(addInfoObj.getKey())){
						addInfoObj.setPresented(true);
						count++;
					}
				}
				
//				newJSArray.put(clinicalTypes.get(jsArray.getString(i)));
			}
			ArrayList<AdditionalInfoObj> alNewPatientInfo=new ArrayList<AdditionalInfoObj>();
			
			for(AdditionalInfoObj addInfoObj: alPatientInfo){
				if(addInfoObj.isPresented()){
					alNewPatientInfo.add(addInfoObj);
				}
			}
			ListView lvAddInfo=(ListView)view.findViewById(R.id.lvAdditionalInfo);
			lvAddInfo.setAdapter(new AddInfoAdapter(alNewPatientInfo,count));
		}catch(Exception e){
			e.printStackTrace();
			tvAdditionalInfo.setText("NO CLINICAL INFORMATION");
		}

		return view;//super.onCreateView(inflater, container, savedInstanceState);
	}

	public static JSONObject getJSObj(String value){
		try {
			JSONObject jsObj=new JSONObject(value);
			return jsObj;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	class AddInfoAdapter extends BaseAdapter{
		JSONArray jsArray;
		ArrayList<AdditionalInfoObj> alPatientInfo;
		int count;
		AddInfoAdapter(ArrayList<AdditionalInfoObj> alPatientInfo, int count){
			this. alPatientInfo= alPatientInfo;
			this.count=count;
		}
		AddInfoAdapter(JSONArray jsArray){
			this. jsArray= jsArray;
		}
		@Override
		public int getCount() {
			
//			return jsArray.length();
			return alPatientInfo.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			try {
				return alPatientInfo.get(arg0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view=	getActivity().getLayoutInflater().inflate(R.layout.demographics_addinfo_item, arg2,false);
			TextView tvAddInfoItem=(TextView) view.findViewById(R.id.tvAddInfoItem);
			try {
				if(alPatientInfo.get(arg0).isPresented()){
					tvAddInfoItem.setText(alPatientInfo.get(arg0).getValue());
					tvAddInfoItem.setVisibility(View.VISIBLE);
				}else{
					tvAddInfoItem.setVisibility(View.GONE);
				}
//				tvAddInfoItem.setText(jsArray.getString(arg0));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return view;
		}

	}
}
