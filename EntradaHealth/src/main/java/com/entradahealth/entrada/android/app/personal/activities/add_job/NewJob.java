package com.entradahealth.entrada.android.app.personal.activities.add_job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaActivity;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.domain.providers.DomainObjectWriter;

public class NewJob extends EntradaActivity {

	private DomainObjectProvider provider;
	private DomainObjectReader reader;
	private UserState state;
	private Account account = null;
	private String accountName = null;
	private Job job = null;
	private Encounter encounter = null;
	private Patient patient = null;
	String[] job_types;
	JobType sel_jtype;
	RelativeLayout rlPatient, rlJobType, rlApptTime;
	String sel_job, sel_appt_time, sel_patient_name;
	TextView tvPatient, tvJobType, tvApptTime;
	ImageView ivArrow;
	private int year, mon, day, hr, min, sec;
	int am_pm;
	long jobId;
	boolean isEdit = false;
	Menu j_Menu;
	MenuItem menuOk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_job);

		getActionBar().setTitle("New Job");
		getActionBar().setDisplayHomeAsUpEnabled(true);

		patient = BundleKeys.SEL_PATIENT;
		sel_job = getIntent().getStringExtra("sel_job_type");
		sel_appt_time = getIntent().getStringExtra("sel_appt_time");

		rlPatient = (RelativeLayout) findViewById(R.id.rlPatient);
		rlPatient.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent();
				i.putExtra("sel_job_type", tvJobType.getText().toString());
				i.putExtra("sel_appt_time", sel_appt_time);
				i.putExtra("sel_patient_name", tvPatient.getText().toString());
				i.putExtra("isEdit", true);
				i.putExtra("jobId", jobId);
				i.putExtra("accountName", accountName);
				i.setClass(NewJob.this, AddJobActivity.class);
				startActivity(i);
			}
		});

		rlJobType = (RelativeLayout) findViewById(R.id.rlJobtype);
		rlJobType.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dgJobType();
			}
		});

		rlApptTime = (RelativeLayout) findViewById(R.id.rlAppTime);
		rlApptTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateTimeDialog(sel_appt_time);
			}
		});

		tvPatient = (TextView) findViewById(R.id.tvNewPatient);
		tvJobType = (TextView) findViewById(R.id.tvNewJobtype);
		tvJobType.setText(sel_job);
		tvApptTime = (TextView) findViewById(R.id.tvNewAppTime);

		if (patient != null) {
			tvPatient.setText(patient.getName());
		}

		ivArrow = (ImageView) findViewById(R.id.ivArrow);

		
	}

	List<JobType> list_job_types;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		if (sel_appt_time == null) {
			Date date;
			date = new Date();

			String strDateFormat = "h:mm a";
			SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
			String str_time = sdf.format(date);

			String strDateFormat1 = "MM/dd";
			SimpleDateFormat sdf1 = new SimpleDateFormat(strDateFormat1);
			String str_date = sdf1.format(date);

			stringToDateTime(date);
			tvApptTime.setText(str_date + " " + str_time);
		} else {
			Date date = null;
			SimpleDateFormat formatter = new SimpleDateFormat(
					"MM/dd/yyyy h:mm a");
			try {
				date = formatter.parse(sel_appt_time);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String strDateFormat = "h:mm a";
			SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
			String str_time = sdf.format(date);

			String strDateFormat1 = "MM/dd";
			SimpleDateFormat sdf1 = new SimpleDateFormat(strDateFormat1);
			String str_date = sdf1.format(date);

			stringToDateTime(date);
			tvApptTime.setText(str_date + " " + str_time);
			setAfter(sel_appt_time);

		}

		final UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			account = state.getCurrentAccount();
			provider = state.getProvider(account);
			list_job_types = provider.getJobTypes();
			job_types = new String[list_job_types.size()];

			for (int i = 0; i < list_job_types.size(); i++) {
				job_types[i] = list_job_types.get(i).name;
			}

		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		BundleKeys.SEL_PATIENT = null;
		startActivity(new Intent(NewJob.this, AddJobActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		this.j_Menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_account, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			BundleKeys.SEL_PATIENT = null;
			startActivity(new Intent(NewJob.this, AddJobActivity.class));
			return true;

		case R.id.item_edit_done:
			menuOk = j_Menu.findItem(R.id.item_edit_done);
			menuOk.setEnabled(false);
			checkJob();
			return true;

		case R.id.item_edit_cancel:
			BundleKeys.SEL_PATIENT = null;
			startActivity(new Intent(NewJob.this, AddJobActivity.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void stringToDateTime(Date dt) {
		// For appt time in Datetime format
		String strApptDT = "MM/dd/yyyy hh:mm:ss";
		SimpleDateFormat foramt01 = new SimpleDateFormat(strApptDT);
		String strApptDate = foramt01.format(dt);
		BundleKeys.appt_date_time = strApptDate;
		
	}

	public void checkJob() {
		
		if (tvPatient.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(),
					"Patient must not be empty", Toast.LENGTH_SHORT).show();
			menuOk = j_Menu.findItem(R.id.item_edit_done);
			menuOk.setEnabled(true);
		} else if (tvJobType.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(),
					"Job Type must not be empty", Toast.LENGTH_SHORT).show();
			menuOk = j_Menu.findItem(R.id.item_edit_done);
			menuOk.setEnabled(true);
		} else {
			setCurrentDateTime(sel_appt_time);
			
			BundleKeys.appt_date_time = twoD(mon + 1) + "/"
					+ twoD(day) + "/" + year + " " + twoD(hr) + ":"
					+ twoD(min) + ":" + twoD(sec);
			if (c.get(Calendar.AM_PM) == Calendar.AM)
				str_AM_PM = "AM";
			else if (c.get(Calendar.AM_PM) == Calendar.PM)
				str_AM_PM = "PM";
			
			if (hr > 12)
				hr = hr - 12;
			sel_appt_time = twoD(mon + 1) + "/" + twoD(day) + "/"
					+ year + " " + twoD(hr) + ":" + twoD(min) + " "
					+ str_AM_PM;
			
			createNewJob();
		}
	}

	private void createNewJob() {
		new AsyncTask<Void, Void, Job>() {

			@Override
			protected Job doInBackground(Void... voids) {
				// TODO Auto-generated method stub
				UserState state = AndroidState.getInstance().getUserState();

				synchronized (state) {
					DomainObjectWriter writer = state.getProvider(state
							.getCurrentAccount());

					try {
						Encounter e = writer.createNewEncounter(patient);
						return writer.createNewJob(e);
					} catch (DomainObjectWriteException e1) {
						e1.printStackTrace();
						menuOk = j_Menu.findItem(R.id.item_edit_done);
						menuOk.setEnabled(true);
						throw new RuntimeException(e1);
						
					}
				}

			}

			@Override
			protected void onPostExecute(Job job) {
				// TODO Auto-generated method stub
				super.onPostExecute(job);
				UserState state = AndroidState.getInstance().getUserState();
				accountName = state.getCurrentAccount().getName();
				account = state.getAccount(accountName);
				reader = state.getProvider(account);

				job = reader.getJob(job.id);
				encounter = reader.getEncounter(job.encounterId);
				patient = reader.getPatient(encounter.patientId);

				job = job.setJobType(BundleKeys.SEL_JOB_TYPE).setFlag(
						Job.Flags.LOCALLY_MODIFIED);
				Encounter enc = reader.getEncounter(job.encounterId);
				JobType jt = reader.getJobType(job.jobTypeId);
				DateTime appt_dt = DateTime.parse(BundleKeys.appt_date_time,
						DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
				enc = new Encounter(job.encounterId, appt_dt, patient.id, null);
				try {
					provider.writeEncounter(enc);
					job = provider.updateJob(job);
				} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					menuOk = j_Menu.findItem(R.id.addJobMenuItem);
					menuOk.setEnabled(true);
					e.printStackTrace();
				}

				BundleKeys.SEL_PATIENT = null;
				Intent intent = new Intent(NewJob.this, JobListActivity.class)
						.putExtra("isUploading", true);
				startActivity(intent);
			}

		}.execute();

	}

	JobType sel_job_type;
	String sel_job_str;

	public void dgJobType() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Job type");
		builder.setCancelable(false);

		List<JobType> myJobTypes = new ArrayList<JobType>();
		for (int i = 0; i < provider.getJobTypes().size(); i++) {
			if (!Boolean.parseBoolean(provider.getJobTypes().get(i).disable)) {
				myJobTypes.add(provider.getJobTypes().get(i));
			}
		}

		final JobTypeAdapter adap = new JobTypeAdapter(this, myJobTypes);

		builder.setAdapter(adap, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				tvJobType.setText(adap.getItem(which).name);
				sel_job_type = adap.getItem(which);
				sel_job_str = adap.getItem(which).name;
				tvJobType.setVisibility(View.VISIBLE);
				sel_jtype = adap.getItem(which);
				BundleKeys.SEL_JOB_TYPE = sel_jtype;
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						ivArrow.setVisibility(View.VISIBLE);

					}
				});
		builder.show();
	}

	AlertDialog alert;
	String str_AM_PM;
	TimePicker timePicker;
	NumberPicker amPmView;
	Calendar c;

	private Dialog showDateTimeDialog(String apptTime) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);

		alert = builder.create();
		alert.show();
		alert.setContentView(R.layout.date_time);
		alert.getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		setCurrentDateTime(apptTime);

		alert.show();
		alert.takeKeyEvents(true);
		alert.setContentView(R.layout.date_time);

		timePicker = (TimePicker) alert.findViewById(R.id.timePicker1);
		timePicker.setIs24HourView(false);
		timePicker.setCurrentHour(hr);
		timePicker.setCurrentMinute(min);
		timePicker.setIs24HourView(false);

		DatePicker datePicker = (DatePicker) alert
				.findViewById(R.id.datePicker1);

		timePicker
				.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

					public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {
						hr = hourOfDay;
						min = minute;

						c.set(Calendar.HOUR_OF_DAY, hourOfDay);
						c.set(Calendar.MINUTE, minute);

					}
				});

		if (Integer.parseInt(android.os.Build.VERSION.SDK) < 17)
			amPmView = (NumberPicker) ((ViewGroup) timePicker.getChildAt(0))
					.getChildAt(3);
		else
			amPmView = (NumberPicker) ((ViewGroup) timePicker.getChildAt(0))
					.getChildAt(1);

		amPmView.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				// TODO Auto-generated method stub
				if (picker.getValue() == 1) {
					if (timePicker.getCurrentHour() < 12)
						timePicker.setCurrentHour(timePicker.getCurrentHour() + 12);
				} else {
					if (timePicker.getCurrentHour() >= 12)
						timePicker.setCurrentHour(timePicker.getCurrentHour() - 12);
				}
			}
		});

		datePicker.init(year, mon, day, new DatePicker.OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker view, int curYear,
					int monthOfYear, int dayOfMonth) {

				mon = monthOfYear;
				day = dayOfMonth;
				year = curYear;

			}

		});

		alert.findViewById(R.id.btnSet).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Button b = (Button) arg0;
						BundleKeys.appt_date_time = twoD(mon + 1) + "/"
								+ twoD(day) + "/" + year + " " + twoD(hr) + ":"
								+ twoD(min) + ":" + twoD(sec);
						if (c.get(Calendar.AM_PM) == Calendar.AM)
							str_AM_PM = "AM";
						else if (c.get(Calendar.AM_PM) == Calendar.PM)
							str_AM_PM = "PM";
						
						if (hr > 12)
							hr = hr - 12;
						sel_appt_time = twoD(mon + 1) + "/" + twoD(day) + "/"
								+ year + " " + twoD(hr) + ":" + twoD(min) + " "
								+ str_AM_PM;
						String sel_dt = twoD(mon + 1) + "/" + twoD(day) + " "
								+ twoD(hr) + ":" + twoD(min) + " " + str_AM_PM;// +getAMPM();
						
						tvApptTime.setText(sel_dt);
						alert.dismiss();

					}
				});

		alert.findViewById(R.id.btnCancel).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						alert.dismiss();

					}
				});
		return alert;

	}
	
	public void setAfter(String apptTime){
		
		c = Calendar.getInstance();
		if (apptTime != null) {
			Date date = null;
			String strApptDT = "MM/dd/yyyy hh:mm a";
			SimpleDateFormat foramt01 = new SimpleDateFormat(strApptDT);
			try {
				date = foramt01.parse(apptTime);
				c.setTime(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		hr = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
		sec = c.get(Calendar.SECOND);
		year = c.get(Calendar.YEAR);
		mon = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		am_pm = c.get(Calendar.AM_PM);
		
		BundleKeys.appt_date_time = twoD(mon + 1) + "/"
				+ twoD(day) + "/" + year + " " + twoD(hr) + ":"
				+ twoD(min) + ":" + twoD(sec);
	}
	public static String twoD(int val) {
		return String.format("%02d", val);
	}
	
	/*
	 * set AM/PM accordingly based on current hour
	 */
	public void setCurrentDateTime(String apptTime){
		c = Calendar.getInstance();

		if (apptTime != null) {
			Date date = null;
			String strApptDT = "MM/dd/yyyy hh:mm a";
			SimpleDateFormat foramt01 = new SimpleDateFormat(strApptDT);
			try {
				date = foramt01.parse(apptTime);
				c.setTime(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		hr = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
		sec = c.get(Calendar.SECOND);
		year = c.get(Calendar.YEAR);
		mon = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		am_pm = c.get(Calendar.AM_PM);
	}

	/*
	 * Adapter class for job types in New Job
	 */

	public class JobTypeAdapter extends ArrayAdapter<JobType> {

		public JobTypeAdapter(NewJob _activity, List<JobType> objects) {
			super(_activity, android.R.layout.simple_spinner_item, objects);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View row = convertView;
			JobTypeHolder holder;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.simple_spinner_item, parent,
						false);

				TextView nameView = (TextView) row
						.findViewById(R.id.SimpleSpinnerItem_Text);

				holder = new JobTypeHolder(nameView);

				row.setTag(holder);
			} else {
				holder = (JobTypeHolder) row.getTag();
			}

			holder.jobType = getItem(position);
			holder.nameView.setText(holder.jobType.name);

			return row;
		}

		public class JobTypeHolder {
			public final TextView nameView;

			public JobType jobType;

			public JobTypeHolder(TextView nameView) {
				this.nameView = nameView;
			}
		}

	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//finish();
	}

}
