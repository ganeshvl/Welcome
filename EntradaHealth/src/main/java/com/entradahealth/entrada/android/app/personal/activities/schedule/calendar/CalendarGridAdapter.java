package com.entradahealth.entrada.android.app.personal.activities.schedule.calendar;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.ScheduleDays;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.ScheduleConstants;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Calendar Grid View Adapter class.
 */
public class CalendarGridAdapter extends BaseAdapter {

	private CalendarViewActivity mCalendarActivity;
	private GregorianCalendar mCurrentDateCal;
	private GregorianCalendar mStartDateCal;
	private GregorianCalendar mEndDateCal;
	private DateFormat mDateFormat;
	public GregorianCalendar  itemmonth;// calendar instances.

	private String mCurrentDateString;
	private String mSelectedDateString;
	public List<String> mDateString;
	private ArrayList<String> mEventDayItems;

	private int mMonthFirstDayOfWeek; // month start day. ie; sun, mon, etc 
	private int mMaxWeeksInMonth; // holds number of weeks in current month
	private int mPreviousMonthMaxDay; // previous month maximum day 31,30....

	private View mPreviousView;
	private TextView mPreviousDayTextView;
	private static NumberFormat nf = new DecimalFormat("#00"); // to maintain dates and months in ("00") format 


	private	ArrayList<String> mEvent;// container to store calendar items which needs showing the event marker
	public Handler mHandler;// for grabbing some event values for showing the different color.

	public CalendarGridAdapter(CalendarViewActivity context, GregorianCalendar currentDateCal, 
			long calendarStartDateInMillis, long calendarEndDateInMillis) {
		mCalendarActivity = context;
		this.mCurrentDateCal = currentDateCal;
		itemmonth = (GregorianCalendar) GregorianCalendar.getInstance();

		//Initialize the handle to update the days. 
		mHandler = new Handler();
		mHandler.post(calendarUpdater);

		// prepare start date calendar
		mStartDateCal = (GregorianCalendar) GregorianCalendar.getInstance();
		mStartDateCal.setTimeInMillis(calendarStartDateInMillis);

		// prepare end date calendar
		mEndDateCal = (GregorianCalendar) GregorianCalendar.getInstance();
		mEndDateCal.setTimeInMillis(calendarEndDateInMillis);

		Locale.setDefault(Locale.getDefault());
		mDateFormat = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT, Locale.getDefault());

		// initialize date string List which will hold all date values that needs to be displayed in GridView 
		mDateString = new ArrayList<String>();
		this.mEventDayItems = new ArrayList<String>();

		// get current date String value in yyyy-MM-dd format
		GregorianCalendar currentDate = (GregorianCalendar) GregorianCalendar.getInstance();
		mCurrentDateString = mDateFormat.format(currentDate.getTime());

		GregorianCalendar selectedDate = (GregorianCalendar) mCurrentDateCal.clone();
		mSelectedDateString = mDateFormat.format(selectedDate.getTime());

		mCurrentDateCal.set(GregorianCalendar.DAY_OF_MONTH, 1);

		// prepare dates to be displayed on GridView for the selected month 
		refreshDays();
	}

	/**
	 * Sets the dates, which has the events.
	 * @param items
	 */
	public void setItems(ArrayList<String> items) {
		for (int i = 0; i != items.size(); i++) {
			if (items.get(i).length() == 1) {
				items.set(i, "0" + items.get(i));
			}
		}
		this.mEventDayItems = items;
	}


	/**
	 * returns count
	 */
	public int getCount() {
		return mDateString.size();
	}

	/**
	 * Returns Item-selected.
	 */
	public Object getItem(int position) {
		return mDateString.get(position);
	}

	/**
	 * Returns selected item Id
	 */
	public long getItemId(int position) {
		return position;
	}

	// create a new view for each item referenced by the Adapter
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) { 
			// if it's not recycled, initialize some attributes
			LayoutInflater inflatedView = (LayoutInflater) mCalendarActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflatedView.inflate(R.layout.calendar_item, null);
		}
		final TextView dayTextView = (TextView) convertView.findViewById(R.id.dateTextView);

		// separates daystring into parts.
		String[] separatedDate = mDateString.get(position).split("-");

		// taking last part of date. ie; 2 from 2012-12-02
		String gridvalueString = separatedDate[2].replaceFirst("^0*", "");
		final int gridvalue = Integer.parseInt(gridvalueString);

		// checking whether the day is in current month or not.
		if ((gridvalue > 1) && (position < mMonthFirstDayOfWeek)) {
			// setting previous month dates / offdays to gray color.
			dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.gray));
			dayTextView.setClickable(false);
			dayTextView.setFocusable(false);
		} else if ((gridvalue < 7) && (position > 28)) {
			// setting next month dates / offdays to gray color.
			dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.gray));
			dayTextView.setClickable(false);
			dayTextView.setFocusable(false);
		} else {
			// setting current month's days in black color.
			dayTextView.setTextColor(Color.BLACK);
		}

		// set current calendar date background
		if (mDateString.get(position).equals(mCurrentDateString)) {
			setCurrentDateBackground(convertView, dayTextView);
		} else {
			convertView.setBackgroundResource(R.drawable.list_item_background);
		}

		// set selected calendar date background
		if (mDateString.get(position).equals(mSelectedDateString) && !mSelectedDateString.equals(mCurrentDateString)) {
			convertView.setBackgroundResource(R.drawable.calendar_cel_selectl);
			dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.white));

			mPreviousView = convertView;
			mPreviousDayTextView = dayTextView;
		}

		convertView.setTag(mDateString.get(position));
		dayTextView.setText(gridvalue+"");

		// create date string for comparison
		String date = mDateString.get(position);

		if (date.length() == 1) {
			date = "0" + date;
		}
		String monthStr = "" + (mCurrentDateCal.get(GregorianCalendar.MONTH) + 1);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}
		// show a different color for date if the date has an event.
//		if (date.length() > 0 && mEventDayItems != null && mEventDayItems.contains(date)) {
//			dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.tab_bar_background_top));
//		} else {
//			// Need to set the other color for dates which dont have events.
//		}
		
		// show icon if date is not empty and it exists in the items array
				ImageView dotImage = (ImageView) convertView.findViewById(R.id.date_icon);
				
				if (date.length() > 0 && mEventDayItems != null && mEventDayItems.contains(date)) {
					dotImage.setVisibility(View.VISIBLE);
				} else {
					// Need to set the other color for dates which dont have events.
					dotImage.setVisibility(View.INVISIBLE);
				}
				

		// restrict click event for dates beyond start and end date
		// TODO - Need to handle this in future. Highlight the date(grid) where it has events
		/*Calendar rowDateCal = (GregorianCalendar) GregorianCalendar.getInstance();
		try {
			Date rowDate = mDateFormat.parse(mDateString.get(position));
			rowDateCal.setTime(rowDate);

			if(rowDateCal.compareTo(mStartDateCal) == -1 || rowDateCal.compareTo(mEndDateCal) == 1) {
				convertView.setEnabled(false);
				dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.gray));
			} else {
				convertView.setEnabled(true);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}*/

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// navigate to next or previous month on clicking offdays.
				if ((gridvalue > 10) && (position < 8)) {
					setPreviousMonth();
					refreshCalendar();
				} else if ((gridvalue < 7) && (position > 28)) {
					setNextMonth();
					refreshCalendar();
				}
				// update current date string to selected date value
				try {
					mSelectedDateString = mDateFormat.format(mDateFormat.parse((String) view.getTag()));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				setSelectedDate(view, dayTextView);
			}
		});


		return convertView;
	}

	public void setCurrentDateBackground(View view, TextView dayTextView) {
		// set current  date background to selected color/red
		view.setBackgroundResource(R.drawable.calendar_cel_current_date);
		dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.tab_bar_background_top));
	}

	public View setSelectedDate(View view, TextView dayTextView) {
		// reset previously selected date background to default
		if (mPreviousView != null && mPreviousDayTextView != null) {
			mPreviousView.setBackgroundResource(R.drawable.list_item_background);
			mPreviousDayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.black));
		}
		mPreviousView = view;
		mPreviousDayTextView = dayTextView;

		// set current selected date background to selected color/red
		view.setBackgroundResource(R.drawable.calendar_cel_selectl);
		dayTextView.setTextColor(mCalendarActivity.getResources().getColor(R.color.white));

		// return selected date, month, year
		GregorianCalendar selectedDateCal = (GregorianCalendar) GregorianCalendar.getInstance();
		Date selectedDate = selectedDateCal.getTime();
		try {
			selectedDate = mDateFormat.parse((String) view.getTag());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		selectedDateCal.setTime(selectedDate);
		mCalendarActivity.onUpdateSelectedMonth(selectedDateCal);

		return view;
	}

	// prepare dates to be displayed on GridView for the selected month 
	public void refreshDays() {
		// clear items
		mDateString.clear();
		Locale.setDefault(Locale.getDefault());

		// calendar instance for previous month
		GregorianCalendar previousMonthCal = (GregorianCalendar) mCurrentDateCal.clone();

		// month start day. ie; sun, mon, etc
		mMonthFirstDayOfWeek = mCurrentDateCal.get(GregorianCalendar.DAY_OF_WEEK);

		// finding number of weeks in current month.
		mMaxWeeksInMonth = mCurrentDateCal.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);

		// allocating maximum row number for the gridview.
		int monthlength = mMaxWeeksInMonth * 7;

		// previous month maximum day 31,30....
		mPreviousMonthMaxDay = getPreviousMonthMaxDay(previousMonthCal);

		// calendar offday starting 24,25 ...
		int calMaxP = mPreviousMonthMaxDay - (mMonthFirstDayOfWeek - 1);
		/**
		 * Calendar instance for getting a complete gridview including the three
		 * month's (previous,current,next) dates.
		 */
		GregorianCalendar calToCalculateDays = (GregorianCalendar) previousMonthCal.clone();
		/**
		 * setting the start date as previous month's required date.
		 */
		calToCalculateDays.set(GregorianCalendar.DAY_OF_MONTH, calMaxP + 1);

		/**
		 * filling calendar gridview.
		 */
		for (int idx = 0; idx < monthlength; idx++) {
			String itemvalue = mDateFormat.format(calToCalculateDays.getTime());
			calToCalculateDays.add(GregorianCalendar.DATE, 1);
			mDateString.add(itemvalue);
		}
	}

	// Returns previous month maximum day 31,30....
	private int getPreviousMonthMaxDay(GregorianCalendar previousMonthCal) {
		int maxP;
		if (mCurrentDateCal.get(GregorianCalendar.MONTH) == mCurrentDateCal.getActualMinimum(GregorianCalendar.MONTH)) {
			previousMonthCal.set((mCurrentDateCal.get(GregorianCalendar.YEAR) - 1), 
					mCurrentDateCal.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			previousMonthCal.set(GregorianCalendar.MONTH, mCurrentDateCal.get(GregorianCalendar.MONTH) - 1);
		}
		maxP = previousMonthCal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

		return maxP;
	}

	/**
	 * Prepares next month dates
	 */
	public void setNextMonth() {
		if (mCurrentDateCal.get(GregorianCalendar.MONTH) == mCurrentDateCal.getActualMaximum(GregorianCalendar.MONTH)) {
			mCurrentDateCal.set((mCurrentDateCal.get(GregorianCalendar.YEAR) + 1),
					mCurrentDateCal.getActualMinimum(GregorianCalendar.MONTH), 1);
		} else {
			mCurrentDateCal.set(GregorianCalendar.MONTH, mCurrentDateCal.get(GregorianCalendar.MONTH) + 1);
		}
	}
	/**
	 * Prepares previous month dates
	 */
	public void setPreviousMonth() {
		if (mCurrentDateCal.get(GregorianCalendar.MONTH) == mCurrentDateCal.getActualMinimum(GregorianCalendar.MONTH)) {
			mCurrentDateCal.set((mCurrentDateCal.get(GregorianCalendar.YEAR) - 1),
					mCurrentDateCal.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			mCurrentDateCal.set(GregorianCalendar.MONTH, mCurrentDateCal.get(GregorianCalendar.MONTH) - 1);
		}
	}
	/**
	 * Refreshes dates of the month.
	 */
	public void refreshCalendar() {
		refreshDays();
		this.notifyDataSetChanged();
		mCalendarActivity.onUpdateNavigatedMonth(mCurrentDateCal);
	}

	public Runnable calendarUpdater = new Runnable() {
		@Override
		public void run() {
			mEventDayItems.clear();
			// Print dates of the current week
			DateFormat df = new SimpleDateFormat(ScheduleConstants.DATE_FORMAT, Locale.US);
			String itemvalue;
			mEvent = readCalendarEvent();
			if(evntDates !=null && evntDates.size()>0){
				
				for (int i = 0; i < evntDates.size(); i++) {
					itemvalue = df.format(itemmonth.getTime());
					itemmonth.add(GregorianCalendar.DATE, 1);
					mEventDayItems.add(evntDates.get(i).toString());
				}
			}
			setItems(mEventDayItems);
			notifyDataSetChanged();
		}
	};

	// Dates list which has events
	public static ArrayList<String> evntDates = new ArrayList<String>();

	/**
	 * To read the list of dates in the current month.
	 * @return
	 */
	public static ArrayList<String> readCalendarEvent() {
		return evntDates;
	}

	/**
	 * Adding calendar events to highlight the days. 
	 * @param days
	 * @return list
	 */
	public  ArrayList<String> addCalendardays(ArrayList<ScheduleDays> days) {
		evntDates.clear();
		if(days !=null){
			//get the dates from the Calendar month activity add to evntDates and display here.
			for(int i = 0; i<days.size();i++){
				String[] date = days.get(i).getMonthAndYear().split("-");
				evntDates.add(date[1]+"-"+String.valueOf(nf.format(Integer.parseInt(date[0])))+"-"+String.valueOf(nf.format(Integer.parseInt(days.get(i).getDays()))));
			}
		}
		mHandler.post(calendarUpdater); 	 	
		return evntDates;
	}

}