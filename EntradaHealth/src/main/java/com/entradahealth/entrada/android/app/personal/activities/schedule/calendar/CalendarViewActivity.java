package com.entradahealth.entrada.android.app.personal.activities.schedule.calendar;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.activities.schedule.ScheduleFragment;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.ScheduleDays;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleConstants;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleUtils;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * CalendarViewActivity, Prepares and displays in Grid format in month View. 
 */
public class CalendarViewActivity extends Activity implements OnClickListener, CalendarEventsListener {

	private CalendarGridAdapter mCalendarGridAdapter;

	private TextView mDateTypeTextView;
	private ImageView mCloseDatePickerImageView;
	private TextView mMonthYearHeaderTextView;
	private RelativeLayout mPreviousMonthArrowLayout;
	private RelativeLayout mNextMonthArrowLayout;
	private GridView mGridview;

	private GregorianCalendar mSelectedDateCal = (GregorianCalendar) GregorianCalendar.getInstance();
	private long mCalendarStartDateInMillis;
	private long mCalendarEndDateInMillis;

	// Event days variables
	private ProgressBar mProgressDialog;
	private String[] mResourceIds;
	//Server Environment
	private APIService service;
	private EntradaApplication application;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set Width and hide title for the activity as we are displaying it as dialog 
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = (int) (metrics.widthPixels);
		setContentView(R.layout.calendar);
		getWindow().setLayout(screenWidth, LayoutParams.WRAP_CONTENT);
		Locale.setDefault(Locale.getDefault());

		//Getting Application Environment.
		application = (EntradaApplication) EntradaApplication.getAppContext();
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
		try {
			service = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		// get intent date
		String dateTypeString = "";
		if(getIntent() != null) {
			if(getIntent().hasExtra(ScheduleFragment.DATE_TYPE_HEADER_STRING)) {
				dateTypeString = getIntent().getExtras().getString(ScheduleFragment.DATE_TYPE_HEADER_STRING);

			}

			if(getIntent().hasExtra(ScheduleFragment.SELECTED_DATE_IN_MILLIS)) {
				long selectedDateInMills = getIntent().getExtras().getLong(ScheduleFragment.SELECTED_DATE_IN_MILLIS, 
						GregorianCalendar.getInstance().getTimeInMillis());
				mSelectedDateCal.setTimeInMillis(selectedDateInMills);

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(selectedDateInMills);
				int mYear = calendar.get(Calendar.YEAR);
				int mMonth = calendar.get(Calendar.MONTH)+1;

				//get the resource Ids and hit the server for available dates and display them in calendar.
				if(getIntent().hasExtra(ScheduleFragment.RESOURCE_IDS_ARRAY)) {
					mResourceIds = getIntent().getExtras().getStringArray(ScheduleFragment.RESOURCE_IDS_ARRAY);
					if(ScheduleUtils.checkInternetConnection(CalendarViewActivity.this)){
						new GetCalendarDaysTask(CalendarViewActivity.this, mResourceIds , mYear+"", mMonth+"").execute();
					}else{
						ScheduleUtils.showAlert(CalendarViewActivity.this, getResources().getString(R.string.alert),  getResources().getString(R.string.no_network));
					}
				}
			}

			// calendar start date 
			if(getIntent().hasExtra(ScheduleFragment.CALENDAR_START_DATE)) {
				mCalendarStartDateInMillis = getIntent().getExtras().getLong(ScheduleFragment.CALENDAR_START_DATE, 
						GregorianCalendar.getInstance().getTimeInMillis());
			}

			// calendar end date 
			if(getIntent().hasExtra(ScheduleFragment.CALENDAR_END_DATE)) {
				mCalendarEndDateInMillis = getIntent().getExtras().getLong(ScheduleFragment.CALENDAR_END_DATE, 
						GregorianCalendar.getInstance().getTimeInMillis());
			}
		}

		// get layout views
		mDateTypeTextView = (TextView) findViewById(R.id.dateTypeTextView);
		mCloseDatePickerImageView = (ImageView) findViewById(R.id.closeDatePickerImageView);
		mMonthYearHeaderTextView = (TextView) findViewById(R.id.monthYearHeaderTextView);
		mPreviousMonthArrowLayout = (RelativeLayout) findViewById(R.id.previousMonthArrowLayout);
		mNextMonthArrowLayout = (RelativeLayout) findViewById(R.id.nextMonthArrowLayout);
		mGridview = (GridView) findViewById(R.id.gridview);
		

		// set event listeners
		mCloseDatePickerImageView.setOnClickListener(this);
		mPreviousMonthArrowLayout.setOnClickListener(this);
		mNextMonthArrowLayout.setOnClickListener(this);

		// set Date Type header text retrieved from intent data
		if(!TextUtils.isEmpty(dateTypeString)) {
			mDateTypeTextView.setText(dateTypeString);
		}
		// Instantiate calendar GridView adapter
		mCalendarGridAdapter = new CalendarGridAdapter(this, mSelectedDateCal, 
				mCalendarStartDateInMillis, mCalendarEndDateInMillis);
		mGridview.setAdapter(mCalendarGridAdapter);

		// set current month for header textview.
		mMonthYearHeaderTextView.setText(android.text.format.DateFormat.format(ScheduleConstants.GRID_DATE_FORMAT, mSelectedDateCal).toString().toUpperCase());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.closeDatePickerImageView:
			finish();
			break;

		case R.id.previousMonthArrowLayout:
			mCalendarGridAdapter.setPreviousMonth();
			mCalendarGridAdapter.refreshCalendar();

			break;

		case R.id.nextMonthArrowLayout:
			mCalendarGridAdapter.setNextMonth();
			mCalendarGridAdapter.refreshCalendar();

			break;

		default:
			break;
		}
	}

	@Override
	public void onUpdateSelectedMonth(GregorianCalendar selectedDateCal) {
		String selectedMonth = android.text.format.DateFormat.format(ScheduleConstants.GRID_DATE_FORMAT, selectedDateCal).toString().toUpperCase();

		mMonthYearHeaderTextView.setText(selectedMonth);

		Intent intent = new Intent();  
		intent.putExtra(ScheduleFragment.SELECTED_DATE_IN_MILLIS, selectedDateCal.getTimeInMillis());  
		setResult(RESULT_OK, intent);  
		finish();
	}

	@Override
	public void onUpdateNavigatedMonth(GregorianCalendar navigatedDateCal) {
		String selectedMonth = android.text.format.DateFormat.format(ScheduleConstants.GRID_DATE_FORMAT, navigatedDateCal).toString().toUpperCase();
		mMonthYearHeaderTextView.setText(selectedMonth);
		Log.d("Navigated Calendar", navigatedDateCal.get(Calendar.MONTH)+"-"+navigatedDateCal.get(Calendar.YEAR));

		//TODO - This functionality should be there. need to confirm with client.
		//Load the days from the server on the months selected in the calendar.
		if(ScheduleUtils.checkInternetConnection(CalendarViewActivity.this)){
			new GetCalendarDaysTask(CalendarViewActivity.this, mResourceIds , navigatedDateCal.get(Calendar.YEAR)+"", (navigatedDateCal.get(Calendar.MONTH)+1)+"").execute();
		}else{
			ScheduleUtils.showAlert(CalendarViewActivity.this, getResources().getString(R.string.alert),  getResources().getString(R.string.no_network));
		}
	}

	private String prepareDatesRequest(String[] resourceIds,String month, String year){

		StringBuffer reqestData = new StringBuffer();
		reqestData.append("ResourceIds=");
		for(int i=0;i<resourceIds.length;i++){
			reqestData.append(resourceIds[i]);
			if (resourceIds.length > 1&& i < resourceIds.length - 1){
				reqestData.append(",");
			}
		}
		reqestData.append("&");
		reqestData.append("Month="+month);
		reqestData.append("&");
		reqestData.append("Year="+year);
		return reqestData.toString();
	}

	/**
	 * AsyncTask to get the appointment days from the server.
	 */
	private class GetCalendarDaysTask extends AsyncTask<Void, Void, JSONObject> {
		String[] resourceids;
		String month;
		String year;
		Activity activity;
		String requestData = null;
		String response = null;
		//2014-04-09
		GetCalendarDaysTask(Activity activity, String[] resourceids, String year, String month){
			this.resourceids = resourceids;
			this.year = year;
			this.month = month;
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//display dialog
			mProgressDialog  = (ProgressBar)findViewById(R.id.progressdialog);
			mProgressDialog.setVisibility(View.VISIBLE);
		}
		@Override
		protected JSONObject doInBackground(Void... arg0) {
			requestData = prepareDatesRequest(resourceids, month,year);
//			requestData = prepareDatesRequest(resourceids, "04","2014");
			try {
				response = service.getScheduleDatesList(requestData);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			try {
				if(response!=null)
					return new JSONObject(response);
				else
					return null;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			ArrayList<ScheduleDays> eventdays = new ArrayList<ScheduleDays>();
			if(result == null){
				mProgressDialog.setVisibility(View.INVISIBLE);
				return;
			}else{

				if (result.has("ErrorMessage")) { // failure
					String error = result.optString("ErrorMessage");
					ScheduleUtils.showAlert(CalendarViewActivity.this, getResources().getString(R.string.alert),error);
					return;
				}
				//success
				//parse the response days.
				JSONArray days;

				try {
					days = result.getJSONArray("Days");
					if(days !=null && days.length()>0){
						for(int i=0;i<days.length();i++){
							ScheduleDays bean = new ScheduleDays();
							try {
								JSONObject day = days.getJSONObject(i);
								bean.setDays(day.optString("Day"));
								bean.setMonthAndYear(month+"-"+year);
								eventdays.add(bean);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			mProgressDialog.setVisibility(View.INVISIBLE);
			mCalendarGridAdapter.addCalendardays(eventdays);
			mGridview.setAdapter(mCalendarGridAdapter);
			mCalendarGridAdapter.notifyDataSetChanged();	

		}

	}


}