package com.entradahealth.entrada.android.app.personal.activities.schedule;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.schedule.calendar.CalendarViewActivity;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Schedule;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleConstants;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleUtils;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.UIUtil;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;



/**
 * Schedule screen fragment, holds all the UI components in the schedule screen.
 */
public class ScheduleFragment extends Fragment implements OnClickListener{

	private static String TAG = ScheduleFragment.class.getSimpleName();
	private ScheduleActivity mActivity;

	//UI views
	private TextView mFilter;
	private TextView mSchedule;
	private TextView mScheduleCount;
	private TextView mCalendar;
	private EditText mSearchET;
	private RelativeLayout mScheduleLayout;
	private PopupWindow mResourceNamesPopUp;
	private StickyListHeadersListView mListView;
	private Set<String> mResourceNames = new TreeSet<String>();
	private Set<String> mResourceIds = new TreeSet<String>();
	private LinkedHashSet<String> mSchedleNames = new LinkedHashSet<String>();

	//Calendar 
	public static final String DATE_TYPE_HEADER_STRING = "DATE_TYPE_STRING";
	public static final String SELECTED_DATE_IN_MILLIS = "SELECTED_DATE_IN_MILLIS";
	public static final String CALENDAR_START_DATE = "CALENDAR_START_DATE";
	public static final String CALENDAR_END_DATE = "CALENDAR_END_DATE";
	public static final int CALENDAR_INTENT_REQUEST_CODE = 100;
	public static final String RESOURCE_IDS_ARRAY = "RESOURCE_IDS_ARRAY";

	private Calendar mSelectedDateCal = GregorianCalendar.getInstance();
	private Calendar mStartDateCal = GregorianCalendar.getInstance();
	private Calendar mEndDateCal = GregorianCalendar.getInstance();

	private String mSelectedDateStr = null;
	private ScheduleListAdapter mAdapter; 

	//Load More
	private static boolean  isLoadeMore=false;
	private ArrayList<Schedule>  mDataList;
	private String mPullRefreshDate; 
	private boolean ispulldown = false;
	private boolean ispullsUp = false;
	private int pullDouwnCounter = 0;
	private int pullUpCounter = 0;
	private boolean isSearchComplete = false;

	private static final int LOADMORE_ATTEMPTS = 6;
	private static final int FORWARD_DAYS = 6;
	private static final int DEFAULT_DAYS = 1;

	private APIService service;
	private EntradaApplication application;
	private DomainObjectProvider provider; 
	private Account account = null;
	private PopupWindow mNamesPopupWindow = null;
	
	private String mSavedIdswithNames[];
	private CheckBox mCbFilter;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Getting Activity reference. 
		mActivity = ((ScheduleActivity)getActivity());
		//mActivity.getActionBar().setDisplayShowCustomEnabled(true);
		application = (EntradaApplication) EntradaApplication.getAppContext();
		//Getting Application Environment.
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(application.getStringFromSharedPrefs(Constants.ENVIRONMENT));
		try {
			service = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		final UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			account = state.getCurrentAccount();
			provider = state.getProvider(account);
		}
		//Clearing the last selected filter option.
		ScheduleUtils.saveBooleanInSP(mActivity, Constants.SHOW_CHECKIN, false);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = null;
		// Inflate the layout for this fragment
		view  = inflater.inflate(R.layout.fragment_schedule, container, false);
		initViews(view);
		// Get the last selected resources from the shared prefs and set them as selected.
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
		mFilter = (TextView)view.findViewById(R.id.tv_filter);
		mSchedule = (TextView)view.findViewById(R.id.tv_schedule);
		mScheduleCount = (TextView)view.findViewById(R.id.tv_schedule_count);
		mScheduleLayout = (RelativeLayout)view.findViewById(R.id.schedule_layout);
		mCalendar = (TextView)view.findViewById(R.id.tv_calendar);
		mSearchET = (EditText)view.findViewById(R.id.etSearch);
		
		mFilter.setOnClickListener(this);
		mSchedule.setOnClickListener(this);
		mCalendar.setOnClickListener(this);

		mListView = (StickyListHeadersListView)view.findViewById(android.R.id.list);
		mListView.setDrawingListUnderStickyHeader(true);
		mListView.setAreHeadersSticky(true);
		mAdapter = new ScheduleListAdapter(mActivity);
		mListView.setAdapter(mAdapter);
		
		LayoutInflater mInflater = LayoutInflater.from(mActivity);
		View mCustomView = mInflater.inflate(R.layout.progressbar_loader, null);
		mProgressBar = (ProgressBar)mCustomView.findViewById(R.id.progressBar);
		mActivity.getActionBar().setCustomView(mCustomView);
		mActivity.getActionBar().setDisplayShowCustomEnabled(true);
		//mActivity.getActionBar().setTitle(getResources().getString(R.string.schedule));
		
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				/*int threshold = 1;
				int count = mListView.getChildCount();
 
				if (scrollState == SCROLL_STATE_IDLE) {
					if (mListView.getLastVisiblePosition() >= count - threshold && pullDouwnCounter < 2) {
						Log.i(TAG, "loading more data");
						// Execute LoadMoreDataTask AsyncTask
						//pullDown();
						if(pullDouwnCounter < LOADMORE_ATTEMPTS){
							pullDown();
						}else{
							Toast.makeText(mActivity, getResources().getString(R.string.pullup_alert), Toast.LENGTH_SHORT).show();
						}
					}
				}*/
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) 
			{
				if(isLoadeMore && isSearchComplete){
					if(Constants.LOG)Log.e("SearchTask Calling", "calling...1");
					if( ispulldown && firstVisibleItem + visibleItemCount >= (totalItemCount)){
						ispulldown = false;
						if(Constants.LOG)Log.e("SearchTask Calling", "calling...2" + isSearchComplete+"");
						if(pullDouwnCounter <LOADMORE_ATTEMPTS){
							pullDown();
						}else{
							Toast.makeText(mActivity, getResources().getString(R.string.pullup_alert), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
		mSearchET.setText("");
		isSearchComplete = true;
		//Adding search listener.
		mSearchET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(s!=null && s.toString().trim().length()>0){
					isSearchComplete = false;
					launchSearchTask(s.toString().trim());
				}else{
					mSearchET.post(new Runnable() {
						@Override
						public void run() {
							isSearchComplete = true;
							prepareSectionedListData(mDataList);			
						}
					});
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}

		});
		
		//Instantiate Resource Popup window
		mResourceNamesPopUp = new PopupWindow(mActivity);
		mResourceNamesPopUp.setWindowLayoutMode(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		mResourceNamesPopUp.setFocusable(true);
		mSchedule.setText(getResources().getString(R.string.schedule_alert));
		mScheduleCount.setText("");
		//Calendar
		mCalendar.setText(getResources().getString(R.string.calendar));
		//mCalendar.setTag(mSelectedDateCal);
		
		//Getting the last selected values from preference and set them to views and load the data.
		String lastSelectedIds = ScheduleUtils.getStringFromSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID));
		String lastSavedName = ScheduleUtils.getStringFromSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)+"#"+ScheduleConstants.LAST_SELECTED_RESOURCES);
		String lastSelectedDate = ScheduleUtils.getStringFromSP(mActivity,application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)+"$"+ScheduleConstants.LAST_SELECTED_DATE);
		//Setting Date
		if(lastSelectedDate !=null){
			SimpleDateFormat format = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT);
			Date date = null;
			try {
				date = format.parse(lastSelectedDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Calendar savedTime =GregorianCalendar.getInstance();
			savedTime.setTime(date);
			mCalendar.setTag(savedTime);
		}else{
			mCalendar.setTag(mSelectedDateCal);
		}
		//Setting Names
		if(lastSavedName !=null && lastSavedName.length()>0){
			String nameAndCount[] = lastSavedName.split("#");
			if(nameAndCount !=null && nameAndCount.length>0){
			mSchedule.setText(nameAndCount[0]);
			}
			if(nameAndCount !=null&&nameAndCount.length>1){
			mScheduleCount.setText(nameAndCount[1]);
			}
		}
		//Setting id's.
		if(lastSelectedIds!=null && lastSelectedIds.length()>0){
			lastSelectedIds = lastSelectedIds.replace("[", "");
			lastSelectedIds = lastSelectedIds.replace("]", "");
			mSavedIdswithNames = lastSelectedIds.split(",");
			 for(String i:mSavedIdswithNames){  
				 mResourceIds.add(i.trim()); 
			   }
			 String time = ScheduleUtils.getStringFromSP(mActivity, Arrays.toString(mResourceNames.toArray()));	
				String lastSyncDate = null;
				if(time !=null && time.length()>0){
					lastSyncDate = ScheduleUtils.convertMilliSecondsToDate(time);
				}else{
					lastSyncDate = "null";
				}
             mSelectedDateStr = lastSelectedDate;
             //loading data from locally, if not found then hit the server.
             ArrayList<Schedule> scheduleList = filterReults(mResourceIds, lastSelectedDate,null);
             if(scheduleList!=null &&scheduleList.size()>0){
            	 if(mDataList ==null){
            		 mDataList=scheduleList;
            	 }else{
            		 mDataList.addAll(scheduleList);	 
            	 }
            	 prepareSectionedListData(mDataList);
            	 isSearchComplete = true;
            	 isLoadeMore = true;
            	 ispulldown = true;
             }else{
            	 new GetScheduleListTask(mResourceIds, lastSelectedDate, lastSyncDate, FORWARD_DAYS+"",false).execute();	 
             }
             
		}
	}
	private void launchSearchTask(String searchString) {
		if(searchString !=null &&searchString.trim().length()>0)
		new ScheduleSearchTask(searchString.trim()).execute();
	}
	// Need to implement in next phase.
	private void pullUp(){
		if(Constants.LOG)Log.d("pullUp", "Called"+ System.currentTimeMillis());
		ispulldown  = true;
		if(ScheduleUtils.checkInternetConnection(mActivity)){
			String time = ScheduleUtils.getStringFromSP(mActivity, Arrays.toString(mResourceNames.toArray()));	
			String lastSyncDate = null;
			String dateArray[];
			if(time !=null && time.length()>0){
				lastSyncDate = ScheduleUtils.convertMilliSecondsToDate(time);
			}else{
				lastSyncDate = "null";
			}
			SimpleDateFormat sdf = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT);
			Calendar c = Calendar.getInstance();
			if(mPullRefreshDate !=null && mPullRefreshDate.length()>0){
			try {
				c.setTime(sdf.parse(mPullRefreshDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			c.add(Calendar.DATE, -1);
			mPullRefreshDate = sdf.format(c.getTime());  
			
			new GetScheduleListTask(mResourceIds, mPullRefreshDate, lastSyncDate, FORWARD_DAYS+"",false).execute();
			mSelectedDateStr = mPullRefreshDate;
			}else{
			}

		}else{
			ScheduleUtils.showAlert(mActivity, getResources().getString(R.string.alert),getResources().getString(R.string.no_network));
		}
	}

	private void pullDown(){
		ispullsUp = false;
		Log.e("pullDown", "Called"+ System.currentTimeMillis());
		if(ScheduleUtils.checkInternetConnection(mActivity)){
			String time = ScheduleUtils.getStringFromSP(mActivity, Arrays.toString(mResourceNames.toArray()));	
			String lastSyncDate = null;
			if(time !=null && time.length()>0){
				lastSyncDate = ScheduleUtils.convertMilliSecondsToDate(time);
			}else{
				lastSyncDate = "null";
			}

			SimpleDateFormat sdf = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT);
			Calendar c = Calendar.getInstance();
			try {
				c.setTime(sdf.parse(mSelectedDateStr));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			c.add(Calendar.DATE, 1);
			mSelectedDateStr = sdf.format(c.getTime());  
			
			//loading data from locally, if not found then hit the server.
            ArrayList<Schedule> scheduleList = filterReults(mResourceIds, mSelectedDateStr,null);
            if(scheduleList!=null &&scheduleList.size()>0){
           	 if(mDataList ==null){
           		 mDataList=scheduleList;
           	 }else{
           		 mDataList.addAll(scheduleList);	 
           	 }
           	 prepareSectionedListData(mDataList);
           	 isSearchComplete = true;
           	 isLoadeMore = true;
           	 ispulldown = true;
            }else{
           	 new GetScheduleListTask(mResourceIds, mSelectedDateStr, lastSyncDate, FORWARD_DAYS+"",false).execute();	 
            }
			//new GetScheduleListTask(mResourceIds, mSelectedDateStr, lastSyncDate, DEFAULT_DAYS+"", false).execute();
			
		}else{
			ScheduleUtils.showAlert(mActivity, getResources().getString(R.string.alert),getResources().getString(R.string.no_network));
		}
	}

	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	
	/**
	 * Shows Resource name pop-up.
	 */
	private void showResourceNamesPopUp(){
		resourceNamesPopup(mResourceNamesPopUp, mScheduleLayout);
	}

	// TODO - Need to remove above method and handle the functionality in one method.
	private void resourceNamesPopup(PopupWindow popupWindow,RelativeLayout bindedTV){
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
		} else{
			showPopupWindow(popupWindow, bindedTV);
		}
	}

	/**
	 * Prepare and displays popup window.
	 * @param popupWindow
	 * @param bindedTV
	 */
	private void showPopupWindow(final PopupWindow popupWindow, final RelativeLayout bindedTV) {

		if (popupWindow.getContentView() == null) {
			TableLayout tableLayout = new TableLayout(mActivity) {
				@Override
				protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
					setMinimumWidth(bindedTV.getWidth());
					super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				}
			};
			tableLayout.setBackgroundColor(Color.WHITE);

			TextView doneTV = new TextView(mActivity);

			doneTV.setText(getResources().getString(R.string.done));
			doneTV.setTextColor(Color.BLACK);
			doneTV.setTypeface(null, Typeface.BOLD);
			int padding = (int) UIUtil.convertDpToPixel(5, mActivity);
			doneTV.setPadding(padding+8, padding, padding+8, padding);
			doneTV.setBackgroundResource(R.drawable.selector_button);
			doneTV.setClickable(true);
			doneTV.setGravity(Gravity.RIGHT);
			doneTV.setVisibility(View.VISIBLE);
			
			// "Done" Button
			doneTV.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mNamesPopupWindow !=null && mNamesPopupWindow.isShowing())
					mNamesPopupWindow.dismiss();
					mDataList = null;
					pullUpCounter = 0;
					pullDouwnCounter = 0;
					if(ScheduleUtils.checkInternetConnection(mActivity)){
						//get the last sync time from SP
						String time = ScheduleUtils.getStringFromSP(mActivity, Arrays.toString(mResourceNames.toArray()));	
						String lastSyncDate = null;
						if(time !=null && time.length()>0){
							lastSyncDate = ScheduleUtils.convertMilliSecondsToDate(time);
						}else{
							lastSyncDate = "null";
						}	
						
						//call the schedule task on resource selection
//						mSelectedDateStr = "2014-04-09";
						mPullRefreshDate = 	mSelectedDateStr;
						if(mResourceIds.size()>0){
							new GetScheduleListTask(mResourceIds, mSelectedDateStr, lastSyncDate, FORWARD_DAYS+"",false).execute();
						}
					}else{
						ScheduleUtils.showAlert(mActivity, getResources().getString(R.string.alert),getResources().getString(R.string.no_network));
					}
				
				}
			});
			
			TableRow headingTableRow = new TableRow(mActivity);
			headingTableRow.setBackgroundColor(getResources().getColor(R.color.gray));
			TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
			tableParams.setMargins(padding - 1, padding, padding - 1, padding);
			headingTableRow.setPadding(padding, padding, padding, padding);
			headingTableRow.setLayoutParams(tableParams);
			headingTableRow.addView(doneTV);
			tableLayout.addView(headingTableRow);
			TableLayout.LayoutParams scrollParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
			ScrollView scrollview = prepareResourceNamesPopup(bindedTV,popupWindow);
			scrollview.setLayoutParams(scrollParams);
			tableLayout.addView(scrollview);
			popupWindow.setContentView(tableLayout);
		}
		//Displaying the popup as dropdown.
		popupWindow.showAsDropDown(bindedTV);
	}


	/**
	 * Prepare and return a popup for resource names.
	 * @param bindedTV - For the location where popup should display.
	 * @param namesPopup - popup object.
	 * @return - popup.
	 */
	
	private ScrollView prepareResourceNamesPopup(RelativeLayout bindedTV, final PopupWindow namesPopup) {
		ScrollView scrollView = new ScrollView(mActivity);
		TableLayout tableLayout = UIUtil.initPopupWindow(mActivity, bindedTV, namesPopup);
		mNamesPopupWindow = namesPopup;

		List<Resource> names = null;
		try {
			names = provider.getResources();
		} catch (DomainObjectWriteException e1) {
			e1.printStackTrace();
		}
		//Prepares Resource names
		if(names !=null && names.size()>0) {
			for(int index=0; index<names.size();index++){
				Resource resourceName = names.get(index);
								
				final TableRow tblRow = UIUtil.createTableRow(mActivity, resourceName);
				TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
				tblRow.setLayoutParams(tableParams);
				tblRow.setBackgroundColor(Color.WHITE);
				
				if(mSavedIdswithNames !=null && mSavedIdswithNames.length>0){
					for(int j=0;j<mSavedIdswithNames.length;j++){
						if(mSavedIdswithNames[j].trim().equalsIgnoreCase(resourceName.getResourceId().trim())){
							tblRow.setBackgroundColor(getResources().getColor(R.color.header_normal));
							Resource details =  (Resource)((TextView)(tblRow).getChildAt(0)).getTag();
							details.setChecked(true);
							mSchedleNames.add(details.getResourceName().trim());
							mResourceNames.add(details.getResourceName().trim());
						}
					}
				}
				
				tblRow.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						try{
							//Getting the Table row and its child.
							TextView resourceName = ((TextView)((TableRow)view).getChildAt(0));
							resourceName.setTextColor(getResources().getColor(R.color.black));
							Resource details =  (Resource)((TextView)((TableRow)view).getChildAt(0)).getTag();
							boolean isSelected =  details.isChecked();
							if(isSelected){
								(((TableRow)view).getChildAt(0)).setBackgroundColor(Color.WHITE);
								isSelected = false;
								details.setChecked(isSelected);
								mResourceNames.remove(resourceName.getText().toString().trim());
								mSchedleNames.remove(resourceName.getText().toString().trim());
								mResourceIds.remove(details.getResourceId().trim());

								if(mSchedleNames.size() ==0){
									mSchedule.setText(getResources().getString(R.string.schedule_alert));
									mScheduleCount.setText("");
								}else{
									if(mSchedleNames.size() !=0 ){
										Iterator iterator = mSchedleNames.iterator();
										while(iterator.hasNext())
										{
											if(mSchedleNames.size() ==1){
												mSchedule.setText(iterator.next()+"");
												break;
											}else{
												mSchedule.setText(iterator.next()+"");
												mScheduleCount.setText(" + "+(mSchedleNames.size()-1));
											}
										}
									}
								}
								ScheduleUtils.saveStringInSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID),
																		Arrays.toString(mResourceIds.toArray()));
								ScheduleUtils.saveStringInSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)+"#"+ScheduleConstants.LAST_SELECTED_RESOURCES,
																		mSchedule.getText().toString().trim()+"#"+mScheduleCount.getText().toString().trim());
							}else{
								(((TableRow)view).getChildAt(0)).setBackgroundColor(getResources().getColor(R.color.header_normal));
								isSelected = true;
								details.setChecked(isSelected);
								mResourceNames.add(resourceName.getText().toString().trim());
								mSchedleNames.add(resourceName.getText().toString().trim());
								mResourceIds.add(details.getResourceId().trim());
								mSchedule.setText(getResources().getString(R.string.schedule));
								if(mSchedleNames.size() !=0 ){
									Iterator iterator = mSchedleNames.iterator();
									while(iterator.hasNext())
									{
										if(mSchedleNames.size() ==1){
											mSchedule.setText(iterator.next()+"");
											break;
										}else{
											mSchedule.setText(iterator.next()+"");
											mScheduleCount.setText(" + "+(mSchedleNames.size()-1));
										}
									}
								}else{
									mSchedule.setText(getResources().getString(R.string.schedule_alert));
									mScheduleCount.setText("");
								}
								ScheduleUtils.saveStringInSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID), Arrays.toString(mResourceIds.toArray()));
								ScheduleUtils.saveStringInSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)+"#"+ScheduleConstants.LAST_SELECTED_RESOURCES,
																		mSchedule.getText().toString().trim()+"#"+mScheduleCount.getText().toString().trim());
								// we are getting the date as "null" type string. so added that below condition.	
								if(mSelectedDateStr == null || mSelectedDateStr.equalsIgnoreCase("null")){
									mSelectedDateStr = ScheduleUtils.getCurrentDate();
								}
							}
							((TextView)((TableRow)view).getChildAt(0)).setTag(details);
						}catch(Exception e){

						}
					}
				});
				tblRow.setTag(index);
				tableLayout.addView(tblRow);
			}
		}
		scrollView.addView(tableLayout);
		return scrollView;
	}
	
	/**
	 * Prepare and shows Calendar.
	 */
	private void showCalendar(){
		//Launching the Calendar.
		Intent calendarIntent = new Intent(mActivity, CalendarViewActivity.class);
		calendarIntent.putExtra(DATE_TYPE_HEADER_STRING, "Today");
		calendarIntent.putExtra(RESOURCE_IDS_ARRAY, mResourceIds.toArray(new String[mResourceIds.size()]));

		if(mCalendar.getTag() != null) {
			mSelectedDateCal = (Calendar) mCalendar.getTag();
		}
		calendarIntent.putExtra(SELECTED_DATE_IN_MILLIS, mSelectedDateCal.getTimeInMillis());
		calendarIntent.putExtra(CALENDAR_START_DATE, mStartDateCal.getTimeInMillis());
		calendarIntent.putExtra(CALENDAR_END_DATE, mEndDateCal.getTimeInMillis());
		startActivityForResult(calendarIntent, CALENDAR_INTENT_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Based on selected date in calendar, get schedule list task will be performed.
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == CALENDAR_INTENT_REQUEST_CODE) {
				if(data != null && data.hasExtra(SELECTED_DATE_IN_MILLIS)) {
					long selectedDateInMills = data.getLongExtra(SELECTED_DATE_IN_MILLIS, 
							GregorianCalendar.getInstance().getTimeInMillis());

					// Create a DateFormatter object for displaying date in specified format. 
					// TODO - Need to change in future.
					SimpleDateFormat formatter = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT);
					Calendar cal = GregorianCalendar.getInstance();
					cal.setTimeInMillis(selectedDateInMills);
					mSelectedDateStr = formatter.format(cal.getTime());   
					//mCalendar.setText(mSelectedDateStr+"");
					mCalendar.setTag(cal);

					if(ScheduleUtils.checkInternetConnection(mActivity)){
						String time = ScheduleUtils.getStringFromSP(mActivity, Arrays.toString(mResourceNames.toArray()));	
						String lastSyncDate = null;
						if(time !=null && time.length()>0){
							lastSyncDate = ScheduleUtils.convertMilliSecondsToDate(time);
						}else{
							lastSyncDate = "null";
						}
						mAdapter.setData(null);
						mAdapter.notifyDataSetChanged();
						isLoadeMore = false;
						mDataList = null;
						new GetScheduleListTask(mResourceIds, mSelectedDateStr, lastSyncDate, FORWARD_DAYS+"", false).execute();
						ScheduleUtils.saveStringInSP(mActivity,application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID)+"$"+ScheduleConstants.LAST_SELECTED_DATE, mSelectedDateStr+"");
						ScheduleUtils.saveStringInSP(mActivity, application.getStringFromSharedPrefs(BundleKeys.DICTATOR_ID), Arrays.toString(mResourceIds.toArray()));
					}else{
						ScheduleUtils.showAlert(mActivity, getResources().getString(R.string.alert),getResources().getString(R.string.no_network));
					}
				}
			}  
		}
	}

	/**
	 * Prepares request data for schedule list API.
	 */
	private String prepareResuestString(String[] resourceIds,String appointmentDate, String lastSync, String daysFwd) {
		//Sending request
		StringBuffer reqestData = new StringBuffer();
		reqestData.append("ResourceIds=");
		for(int i=0;i<resourceIds.length;i++){
			reqestData.append(resourceIds[i]);
			if (resourceIds.length > 1&& i < resourceIds.length - 1){
				reqestData.append(",");
			}
		}
		
		reqestData.append("&");
		if(appointmentDate == null || appointmentDate.equalsIgnoreCase("null")){
			reqestData.append("AppointmentDate="+ScheduleUtils.getCurrentDate());	
		}else{
			reqestData.append("AppointmentDate="+appointmentDate);//2015-06-01 00:00:00
		}
		reqestData.append("&");
		reqestData.append("DaysForward="+daysFwd);
		reqestData.append("&");
		reqestData.append("DaysPast="+"0");
		reqestData.append("&");
		reqestData.append("LastSync="+/*lastSync*/"null");
		reqestData.append("&");
		reqestData.append("CurrentPage="+"1");
		reqestData.append("&");
		reqestData.append("SelectSize="+"100");

		Log.d("Get Schedule List request data :", reqestData.toString());

		return reqestData.toString();
	}

	
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		//Filter tab event
		case R.id.tv_filter:
			showFilterPopup(v);
			break;
			//Schedule tab event
		case R.id.tv_schedule:
			showResourceNamesPopUp();
			break;
			//Calendar tab event
		case R.id.tv_calendar:
			if(mResourceNames!=null && mResourceNames.size()>0){
				showCalendar();
			}else{
				Toast.makeText(mActivity, getResources().getString(R.string.calendar_alert), Toast.LENGTH_SHORT).show();
			}
			//If user doesn't selects any resource from the 'schedule' drop down, display a alert to user to select resource. 
			break;

		default:
			break;
		}

	}

	// Display anchored popup menu based on view selected
	private void showFilterPopup(View filterView) {
		
		final PopupWindow popup = new PopupWindow(getActivity());
	    View view = getActivity().getLayoutInflater().inflate(R.layout.filter_layout, null);
	    
	    mCbFilter = (CheckBox)view.findViewById(R.id.cb_filter);
	    mCbFilter.setChecked(ScheduleUtils.getbooleanFromSP(mActivity, Constants.SHOW_CHECKIN));
	    popup.setContentView(view);
	    popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	    popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
	    popup.setOutsideTouchable(true);
	    popup.setFocusable(true);
	    popup.showAsDropDown(filterView);	
	    
	    mCbFilter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mCbFilter.isChecked()){
					ScheduleUtils.saveBooleanInSP(mActivity, Constants.SHOW_CHECKIN, true);
					prepareSectionedListData(mDataList);
				}else{
					ScheduleUtils.saveBooleanInSP(mActivity, Constants.SHOW_CHECKIN, false);
					prepareSectionedListData(mDataList);
				}
			}
		});
	}

	/**
	 *AsyncTask to get the schedule list details 
	 */
	private class GetScheduleListTask extends AsyncTask<Void, Void, String> {
		private Set<String> resourceIds;
		private String appointmentDate; 
		private String lastSync; 
		private boolean isPullUp = false;
		private String requestData = null;
		private String daysFwd=null;
		String[] ids;

		GetScheduleListTask(Set<String> resourceIds, String appointmentDate, 
				String lastSync, String daysFwd, boolean isPullUp){
			this.resourceIds = resourceIds;
			this.appointmentDate = appointmentDate;
			this.lastSync = lastSync;
			this.isPullUp = isPullUp;
			this.daysFwd = daysFwd;
			ids = resourceIds.toArray(new String[resourceIds.size()]);
			requestData = prepareResuestString(ids, appointmentDate, lastSync, daysFwd);
		}


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected String doInBackground(Void... arg0) {
			//request schedule list API
			try {
				
				return service.getScheuleList(requestData);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result == null){ // Close the dialog
				mProgressBar.setVisibility(View.INVISIBLE);
				return;
			}
			if(result !=null && result.length()>0){
				JSONObject object = null;
				try {
					object = new JSONObject(result);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				ScheduleUtils.saveStringInSP(mActivity, Arrays.toString(mResourceNames.toArray()), System.currentTimeMillis()+"");
				JSONArray schedules;
				try {
					//Parse results.
					if(object.has("Schedules")){
						schedules = object.getJSONArray("Schedules");
						for(int i=0;i<schedules.length();i++){
							Schedule bean = new Schedule();
							try {
								JSONObject schedule = schedules.getJSONObject(i);
								bean.setScheduleID(schedule.optInt("ScheduleID"));
								String resourceId =schedule.optString("ResourceID"); 
								bean.setResourceID(resourceId);
								bean.setPatientID(schedule.optInt("PatientID"));
								bean.setReasonName(schedule.optString("ReasonName"));
								bean.setAppointmentStatus(schedule.optInt("AppointmentStatus"));
								String appointmentDate = schedule.optString("AppointmentDate");
								bean.setAppointmentDate(appointmentDate);
								bean.setJobId(schedule.optString("JobId"));

								provider.scheduleInsertUpdate(bean);
								
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						//Get the results from the database.
						ArrayList<Schedule> scheduleList = filterReults(resourceIds, appointmentDate,null);
						
						if(scheduleList == null ||(scheduleList !=null && scheduleList.size() == 0)){
							if(isPullUp){
								pullUpCounter++;
							}else{
								pullDouwnCounter++;
							}
						}else{
							isLoadeMore = true;
							pullDouwnCounter = 0;
							pullUpCounter= 0;
						}
						
						if(mDataList == null){
							mDataList = scheduleList;
							prepareSectionedListData(mDataList);
						}else{
							if(scheduleList !=null){
								if(isPullUp){
									mDataList.addAll(0,scheduleList);
								}
								else{
									mDataList.addAll(scheduleList);
								}
								prepareSectionedListData(mDataList);
							}
						}
						
						ispulldown = true;
						ispullsUp  = true;
					}
				}catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(isPullUp){
				 //mPullToRefreshLayout.setRefreshComplete();
				}
				//close the dialog
				mProgressBar.setVisibility(View.INVISIBLE);
			}else{
				mProgressBar.setVisibility(View.INVISIBLE);
				Toast.makeText(mActivity, getResources().getString(R.string.alert_server), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	/**
	 * Method to perform the filter operation, based on the user selected values from the 'Resource Names' 
	 * and 'Calendar' date.
	 * @param resourceNames
	 */
	private ArrayList<Schedule> filterReults(Set<String> resourceIds, String date, String searchString) {
		//date = "2014-04-08";
		if(resourceIds!=null && resourceIds.size()>0){
			try {
				if(date == null || date.equalsIgnoreCase("null")){
					date = ScheduleUtils.getCurrentDate();
				}
				//Prepare sql query
				String query = prepareSqlQuery(resourceIds, date, searchString);
				if(Constants.LOG)Log.e(TAG, query);
				return provider.getRaawScheduleDetailsList(query);
				//Execute sql query and show results
			}catch(Exception e){
			}
		}else{
			Log.d(TAG, "No Resource selected, Please select atleast one Resource");
			return null;
		}
		return null;
	}
	
	/**
	 * Method to sort events on DATE basis.
	 * @param scheduleList
	 * @return
	 */
	private ArrayList<ArrayList<Schedule>> prepareSectionedListData(ArrayList<Schedule> schedulesList){
		Map<Long, ArrayList<Schedule>> treeMap = null;
		ArrayList<ArrayList<Schedule>> sortedScheduleList = null; 
		ArrayList<Schedule> eventsList = null;
		ArrayList<Schedule> scheduleList = null;
		
		//Filter the list based on the AppointmentStatus, if status = 200 display green, otherwise display all. 
		if(ScheduleUtils.getbooleanFromSP(mActivity, Constants.SHOW_CHECKIN)){
			scheduleList = getFilteredCheckInList(schedulesList);
		}else{
			scheduleList = schedulesList;
		}
		if(scheduleList !=null && scheduleList.size()>0){
			treeMap = new TreeMap<Long, ArrayList<Schedule>>();
			for(int id=0; id<scheduleList.size();id++){
				Date d = null;
				try {
					d = ScheduleConstants.SERVER_DATE_FORMAT.parse(scheduleList.get(id).getAppointmentDate().split("T")[0].trim());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				//Sorting base on the time.
				long time = cal.getTimeInMillis();
				if(treeMap.containsKey(time)) {
					// If map contains key, we add to the same key in map or else we add it to a new key in map.
					treeMap.get(time).add(scheduleList.get(id));
				}else{
					eventsList = new ArrayList<Schedule>();
					eventsList.add(scheduleList.get(id));
					treeMap.put(time, eventsList);
				}
			}
			sortedScheduleList = new ArrayList<ArrayList<Schedule>>();
			Set<Long> keys = treeMap.keySet();
			Iterator<Long> itr = keys.iterator();
			do{
				Long l = itr.next();
				sortedScheduleList.add(treeMap.get(l));
			}while(itr.hasNext());
			List<SideMenuSection> sections = null;
			//Get the schedule list total data
			sections = SectionListDataProvider.getInstance(getActivity()).getAllData(treeMap);
			mAdapter.setData(sections);
			mAdapter.notifyDataSetChanged();

		}else{
			mAdapter.setData(null);
			mAdapter.notifyDataSetChanged();
		}
		return sortedScheduleList;
	}
	
	/**
	 * Method to filter the only checked schedules from all the available schedule in the listview.
	 * @param list
	 * @return onlyCheckedList
	 */
	private ArrayList<Schedule> getFilteredCheckInList(ArrayList<Schedule> list){
		ArrayList<Schedule> onlyCheckedList= new ArrayList<Schedule>(); 
		if(list!=null&& list.size()>0){
		for(int position=0; position<list.size();position++){
			Schedule schedule= list.get(position);
			if((schedule.getAppointmentStatus()==200)||((schedule.getAppointmentStatus()+"").equalsIgnoreCase("200"))){
				onlyCheckedList.add(schedule);
			}
		}
		return onlyCheckedList;
		}
		return list;
	}
	
	/**
	 * Method to prepare sql query when user request specific resource details or combination of resource details.
	 * @param resourceId
	 * @param date
	 * @return query
	 */
	public String prepareSqlQuery(Set<String> resourceId, String date, String searchQuery){

		String	filterdScheduleSQLQuery = "SELECT * FROM Schedule ";

		// Creating where clause
		StringBuffer whereSB = new StringBuffer();
		if (resourceId != null && resourceId.size() > 0) {
			String[] myResourceNames = resourceId.toArray(new String[resourceId.size()]);
			whereSB.append("(");
			for(int index = 0; index<myResourceNames.length;index++){
				whereSB.append("(");
				whereSB.append("ResourceID"
						+ " = '" );
				whereSB.append(myResourceNames[index] +"'") ;
				whereSB.append(")");
				if (myResourceNames.length > 1&& index < myResourceNames.length - 1){
					whereSB.append(" OR ");
				}
				
			}
			whereSB.append(")");
			
			// Appending date
			if(date !=null && date.length()>0){
				whereSB.append(" AND ");
				whereSB.append("(");
				whereSB.append("AppointmentDate LIKE"
						+ " '" );
				whereSB.append( date +"%"+"'") ;
				whereSB.append(")");
			}
			//Appending default sort.
			//whereSB.append(" ORDER BY " +MySQLiteHelper.SCH_RESOURCE_NAME+ " ASC");
		}
		
		return filterdScheduleSQLQuery = filterdScheduleSQLQuery + "WHERE "+ whereSB.toString();

	}
	
	
	/**
	 * Asynchronous task for pulling schedules from the schedule provider. A blank
	 * string should pull all schedules; any other string should pull any schedule
	 * that meets all of the space-separated tokens in the search string.
	 */
	public class ScheduleSearchTask extends AsyncTask<Void, Void, ArrayList<Schedule>>
	{
	    protected final String searchText;
	    private ArrayList<Schedule> schedules;
	    private Account account;
	    
	    public ScheduleSearchTask(String searchText){
	        this.searchText = searchText;
	        UserState state = AndroidState.getInstance().getUserState();
			synchronized (state) {
				account = state.getCurrentAccount();
			}
	    }
	    
		@Override
		protected ArrayList<Schedule> doInBackground(Void... voids) {
			if(Constants.LOG)Log.d("Entrada-ScheduleSearchTask", String.format(
					"Search started: '%s', account '%s'", searchText,
					account.getName()));

			UserState state = AndroidState.getInstance().getUserState();
			DomainObjectReader reader = state.getProvider(account);
			 schedules =  reader.searchSchedules(searchText);
			 return schedules;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if(Constants.LOG)Log.d("Entrada-ScheduleSearchTask",
					String.format("Cancelled: '%s'", searchText));
		}

		@Override
		protected void onCancelled(ArrayList<Schedule> schedules) {
			super.onCancelled(schedules);
			if(Constants.LOG)Log.d("Entrada-ScheduleSearchTask",
					String.format("Cancelled: '%s'", searchText));
		}

		@Override
		protected void onPostExecute(ArrayList<Schedule> schedules) {
			super.onPostExecute(schedules);
			if(!isCancelled()) {
				ArrayList<Schedule> scheduleList = schedules;
				if(scheduleList !=null){
					mAdapter.notifyDataSetChanged();
					prepareSectionedListData(scheduleList);
				}
			}
		}


	}
}
