package com.entradahealth.entrada.android.app.personal.activities.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.SectionListItemInfo;

import android.content.Context;
import android.util.Log;


/**
 * This class prepares the Sticky List data.
 */
public class SectionListDataProvider {

	public static final String TAG = SectionListDataProvider.class.getSimpleName();

	public static SectionListDataProvider mInstance;
	public Context mContext;

	// instance creation
	public static SectionListDataProvider initialize(Context ctx) {
		if (mInstance == null)
			mInstance = new SectionListDataProvider();

		if (ctx != null)
			mInstance.mContext = ctx;

		return mInstance;
	}
	//get Instance
	public static SectionListDataProvider getInstance(Context ctx) {
		if (mInstance == null) {
			initialize(ctx);
		}
		return mInstance;
	}

	/**
	 * This method prepares all the 'SECTIONS' and "SECTION DATA" for the List View
	 * @param treeMap
	 * @return sectioned data
	 */
	public List<SideMenuSection> getAllData(Map<Long, ArrayList<Schedule>> treeMap) {
		ArrayList<ArrayList<Schedule>> sortedScheduleList = new ArrayList<ArrayList<Schedule>>();
		List<SideMenuSection> sidemenuData = new ArrayList<SideMenuSection>();
		
		// get the map and prepare section titles
		if(treeMap !=null &&treeMap.size()>0){
			Set<Long> keys = treeMap.keySet();
			Object[] titleStr = new String[treeMap.size()];
			// converts to Array.
			titleStr = keys.toArray();
			Iterator<Long> itr = keys.iterator();
			do{
				Long l = itr.next();
				sortedScheduleList.add(treeMap.get(l));
			}while(itr.hasNext());    	
			
			//Get The child information from the map header list.
			for(int j=0;j<sortedScheduleList.size();j++){
				ArrayList<Schedule> list = sortedScheduleList.get(j);
				//sort by time
				Collections.sort(list, new Comparator<Schedule>() {
				    public int compare(Schedule result1, Schedule result2) {
				        return result1.getAppointmentDate().split("T")[1].trim().compareTo(result2.getAppointmentDate().split("T")[1].trim());
				    }
				});
				//add data to side menu
				sidemenuData.add(getOneSection(list, titleStr[j]));
			}
		}else{
			Log.d(TAG, "Empty Tree Map.");
		}
		return sidemenuData;
	}
	
	/**
	 * Prepares a section data for one section.
	 * @param sortedScheduleList
	 * @param title
	 * @return SideMenuSection - section
	 */
	public SideMenuSection getOneSection( ArrayList<Schedule> sortedScheduleList, Object title) {
		ArrayList<SectionListItemInfo> resultList = new ArrayList<SectionListItemInfo>();

		// Add the sorted list item data to the corresponding header.
		if(sortedScheduleList !=null && sortedScheduleList.size()>0){
			for(int i=0; i<sortedScheduleList.size();i++){
				//Get the child and create a child for header.
				Schedule details = sortedScheduleList.get(i);
				
				resultList.add(new SectionListItemInfo(details.getAppointmentDate(), details.getAppointmentDate().split("T")[0].trim(), details.getAppointmentDate().split("T")[1].trim(), 
						details.getAppointmentStatus()+"", details.getJobId()+"", details.getPatientID()+"", "Pt Name", details.getReasonName(), details.getResourceID(), details.getScheduleID()+"", "MRN"));
			}
			return new SideMenuSection(title.toString(), resultList);
		}
		else{
			Log.d(TAG, "No data available to display.");
		}
		return null;
	}

}
