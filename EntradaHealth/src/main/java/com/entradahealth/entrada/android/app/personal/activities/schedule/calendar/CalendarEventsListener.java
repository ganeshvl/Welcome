package com.entradahealth.entrada.android.app.personal.activities.schedule.calendar;

import java.util.GregorianCalendar;

/**
 * Calendar Events interface.
 */
public interface CalendarEventsListener {
	
	/**
	 * Used to updated the current month changes. 
	 * @param selectedDateCal
	 */
	public void onUpdateSelectedMonth(GregorianCalendar selectedDateCal);
	/**
	 * Used to get the call backs on months navigation. 
	 * @param selectedDateCal
	 */
	public void onUpdateNavigatedMonth(GregorianCalendar navigatedDateCal);
}
