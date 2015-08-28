package com.entradahealth.entrada.android.app.personal.activities.schedule.model;

/**
 * This bean class used set and get the ScheduleDays details. 
 */
public class ScheduleDays {

	private String days;
	private String monthAndYear;
	private String id;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDays() {
		return days;
	}
	public void setDays(String days) {
		this.days = days;
	}
	public String getMonthAndYear() {
		return monthAndYear;
	}
	public void setMonthAndYear(String monthAndYear) {
		this.monthAndYear = monthAndYear;
	}
}
