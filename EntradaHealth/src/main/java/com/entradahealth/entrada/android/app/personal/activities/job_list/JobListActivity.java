package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.acra.ACRA;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_job.AddJobActivity;
import com.entradahealth.entrada.android.app.personal.activities.inbox.SecureMessaging;
import com.entradahealth.entrada.android.app.personal.activities.job_display.JobDisplayActivity;
import com.entradahealth.entrada.android.app.personal.activities.manage_queues.ManageQueuesActivity;
import com.entradahealth.entrada.android.app.personal.activities.schedule.ScheduleActivity;
import com.entradahealth.entrada.android.app.personal.activities.schedule.util.GetResourcesTask;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.android.app.personal.sync.DictationUploadService;
import com.entradahealth.entrada.android.app.personal.sync.SyncService;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.android.app.personal.utils.AndroidUtils;
import com.entradahealth.entrada.android.app.personal.utils.NetworkState;
import com.entradahealth.entrada.app.personal.menu_adapter.JobMenuAdapter;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.service.NewConversationBroadcastService;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.readystatesoftware.viewbadger.BadgeView;

/**
 * The "main" activity for the app; shows a list of jobs, allows for switching
 * between accounts, and all manner of other delicious things.
 * 
 * @author edr
 * @since 12 Oct 2012
 */
public class JobListActivity extends SlidingActivity {
	public static final String LOG_NAME = "Job-List-Activity";
	public JobSearchTask searchTask = null;
	public JobCountTask countTask = null;
	public boolean isSyncing = false;
	public boolean isLive = false;
	public boolean isSync = false;
	NetworkState ns;
	Boolean isConnected, isSetting = false;
	Handler handler;
	SharedPreferences sp;
	Editor edit;

	ListView lvSliding;
	TextView tvListTitle, tvUpdating;
	SlidingMenu slidingMenu;
	JobMenuAdapter jBAdap;
	EditText searchEdit;
	JobListRefreshTask refreshTask = null;
	ListView jobListView;
	EditText etSearch;
	RelativeLayout rlSearch;
	public Menu j_Menu;
	//MenuItem menuItem;
	//MenuItem item_add, item_sync;
	private DomainObjectProvider provider;
	Account currentAccount = null;
	AccountSpinnerAdapter spinnerAdapter = null;
	List<Long> jobIds = null;
	TextView tvNoResults, tvDate, tvTime, tvSyncFailed;
	ImageView ivDrawer;
	RelativeLayout rlUpdated, rlSyncError;
	boolean showDeletedJobs = false, qchanged = true, isUploading = false;
	AlertDialog dgManageQ, dgSyncError;
	JobListActivity activity;
    JobListItemAdapter adapter; 
    private boolean itemClicked = false;
    List<Job> jobsSource;

	// UserState state;
    
    TextView tvAcTitle;
    ImageView ivInbox, ivAddJob, ivSync;
    private ImageView ivSchedule;
    private EntradaApplication application;
	private BadgeView badge;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		application = (EntradaApplication) EntradaApplication.getAppContext();
		sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        if(sp.getBoolean("SECURE_MSG", true))
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        else
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
		final JobListActivity thisActivity = this;
		
		//Custom view as action bar
		LayoutInflater mInflater = LayoutInflater.from(this);
		View mCustomView = mInflater.inflate(R.layout.acbar_job_list, null);
		tvAcTitle = (TextView)mCustomView.findViewById(R.id.tvAcTitle);
		ivInbox = (ImageView)mCustomView.findViewById(R.id.ivInbox);
		ivAddJob = (ImageView)mCustomView.findViewById(R.id.ivAddJob);
		ivSync = (ImageView)mCustomView.findViewById(R.id.ivSync);
		ivSchedule = (ImageView)mCustomView.findViewById(R.id.ivSchedule);
		tvAcTitle.setText(BundleKeys.title);
		
		//Sample icon badger on Action bar item
		badge = new BadgeView(this, ivInbox);
		if(BundleKeys.fromSecureMessaging || !application.isJobListEnabled()){
			startActivity(new Intent(JobListActivity.this, SecureMessaging.class));
			finish();
		}
		
		ivInbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(JobListActivity.this, SecureMessaging.class));
				finish();
			}
		});
		
		ivAddJob.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent addJobIntent = new Intent(JobListActivity.this, AddJobActivity.class);
				addJobIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(addJobIntent);
				finish();
			}
		});
		
		ivSync.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ns = new NetworkState(getApplicationContext());
				isConnected = ns.isConnectingToInternet();
				if (isConnected) {
					running = false;
					ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					for (RunningServiceInfo service : manager
							.getRunningServices(Integer.MAX_VALUE)) {
						if ("com.entradahealth.entrada.android.app.personal.sync.SyncService"
								.equals(service.service.getClassName())
								|| "com.entradahealth.entrada.android.app.personal.sync.DictationUploadService"
										.equals(service.service.getClassName())) {
							running = true;
						}
					}

					if (!running) {
						etSearch.setText(null);
						if (!isFinishing()) {

							//retryUploads();
							boolean canSync = true;
							try {
								List<Job> jobs = AndroidState.getInstance()
										.getUserState().getProvider(currentAccount)
										.getJobs();

								for (Job j : jobs) {
									if (j.isPending()) {
										canSync = false;
										UserState state = AndroidState
												.getInstance().getUserState();
										Log.e("", "onOptionsItemSelected-syncMenuItem--"+j.id);
										DictationUploadService.startUpload(JobListActivity.this,
												state, currentAccount, j);
									}
								}
							} catch (Exception ex) {
								ACRA.getErrorReporter().handleSilentException(ex);
								canSync = false;
							}

							if (!canSync) {
								Toast.makeText(
										JobListActivity.this,
										"Please wait for all uploads to complete before syncing.",
										Toast.LENGTH_SHORT).show();
								
							} else {
								rlSyncError.setVisibility(View.GONE);
								rlUpdated.setVisibility(View.INVISIBLE);
								tvUpdating.setVisibility(View.VISIBLE);

								Intent i = new Intent(JobListActivity.this, SyncService.class);
								startService(i);

							}

							isSyncing = true;
							BundleKeys.SYNC_FOR_ACC = currentAccount
									.getDisplayName();
							//BundleKeys.SYNC_FOR_CLINIC = currentAccount.getClinicCode(); 

							/*task1 = buildMinderTask();
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							} else {
								task1.execute();
							}*/

						}
					} else {
						if (!isResumed)
							Toast.makeText(
									JobListActivity.this,
									"Please wait for all uploads to complete before syncing.",
									Toast.LENGTH_SHORT).show();
					}
					isResumed = false;
				} else {
					rlSyncError.setVisibility(View.VISIBLE);
					rlUpdated.setVisibility(View.GONE);
					tvUpdating.setVisibility(View.GONE);
				}
				
			}
		});
		
		ivSchedule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(JobListActivity.this, ScheduleActivity.class));
				finish();
			}
		});
		
		getActionBar().setCustomView(mCustomView);
		getActionBar().setDisplayShowCustomEnabled(true);
		
		//ActionBar ab = getActionBar();
		//ab.setTitle(BundleKeys.title);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		registerReceiver(broadcastReceiver, new IntentFilter("CONNECTIVITY_CHANGED"));
		jobIds = new ArrayList<Long>();
		BundleKeys.fromImageDisplay = false;
		BundleKeys.fromCaputreImages = false;
		BundleKeys.fromSecureMessaging = false;
		
		/*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
	        @Override
	        public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
	            Log.e("Job-List-Activity","Uncaught-Exception");
	            System.exit(2);
	        }
	    });*/
		
		setBehindContentView(R.layout.job_list_sliding_menu);
		tvListTitle = (TextView) findViewById(R.id.tvListTitle);
		tvListTitle.setText("Jobs");
		lvSliding = (ListView) findViewById(R.id.lvSlidingMenu);
		lvSliding.setBackgroundColor(Color.parseColor("#262b38"));

		// Get screen width and set sliding width to 3/4
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int req_width = width * 3 / 4;

		slidingMenu = getSlidingMenu();
		slidingMenu.setFadeEnabled(true);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setBehindWidth(req_width);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

		setContentView(R.layout.job_list);

		ivDrawer = (ImageView) findViewById(R.id.ivDrawer);
		ivDrawer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				slidingMenu.toggle();
				// JobCountTask();
			}
		});

		rlUpdated = (RelativeLayout) findViewById(R.id.rlDateTime);
		rlSyncError = (RelativeLayout) findViewById(R.id.rlSyncError);
		rlSyncError.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(
						JobListActivity.this);
				builder.setTitle(R.string.title_sync_error);
				builder.setMessage(R.string.msg_sync_error);
				builder.setPositiveButton("OK", null);
				dgSyncError = builder.create();
				dgSyncError.show();
			}
		});

		tvDate = (TextView) findViewById(R.id.tvDate);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvUpdating = (TextView) findViewById(R.id.lblUpdating);
		tvSyncFailed = (TextView) findViewById(R.id.lblSyncError);

		handler = new Handler();
		handler.postDelayed(runnable, BundleKeys.mins_to_sync * 60 * 1000);
		handler.postDelayed(runnable_logs, 5 * 60 * 1000);

		jobListView = (ListView) findViewById(R.id.jobListView);
		jobListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		jobListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int position, long id) {
						// TODO: evaluate whether to save search text in
						// UserState to repopulate on back press.

						itemClicked = true;
						if(searchTask!=null){
							searchTask.cancel(true);
						}
						
						Log.d("", "-- Job ItemClick --");
						List<Long> jobList = thisActivity.getJobIdList();
						Preconditions.checkNotNull(jobList,
								"null List<Job> when adapter view clicked.");
						Job job = AndroidState.getInstance().getUserState()
								.getProvider(currentAccount)
								.getJob(jobList.get(position));
						if(job != null) {
							Preconditions
									.checkNotNull(job,
											"null item in List<Job> when adapter view clicked.");
	
							if (job.isFlagSet(Job.Flags.UPLOAD_COMPLETED)
									|| job.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS)
									|| (job.isFlagSet(Job.Flags.UPLOAD_PENDING) && BundleKeys.which == 6))
	
							{
								Toast.makeText(thisActivity,
										"Cannot open a completed dictation.",
										Toast.LENGTH_SHORT).show();
								return;
							} else if (job.isFlagSet(Job.Flags.LOCALLY_DELETED)) {
								return;
							}
	
							Intent intent = new Intent(thisActivity,
									JobDisplayActivity.class);
							Bundle b = new Bundle();
							if(!job.isFlagSet(Flags.HOLD))
								job = job.setFlag(Flags.IS_FIRST);
							else
								job = job.clearFlag(Flags.IS_FIRST);
							try {
								AndroidState.getInstance().getUserState()
								.getProvider(currentAccount).updateJob(job);
							} catch (DomainObjectWriteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							b.putBoolean("isFirst", true);
							b.putBoolean("isFromList", true);
							b.putBoolean("isNew", false);
							b.putLong(BundleKeys.SELECTED_JOB, job.id);
							b.putString(BundleKeys.SELECTED_JOB_ACCOUNT,
									currentAccount.getName());
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtras(b);
							thisActivity.startActivity(intent);
							thisActivity.finish();
						}
					}
					
				});

		jobListView
				.setMultiChoiceModeListener(new JobListMultiChoiceModeListener(
						this));

		slidingMenu.setOnOpenListener(new OnOpenListener() {

			@Override
			public void onOpen() {
				// TODO Auto-generated method stub

				openSlide();
			}
		});

		lvSliding.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				
				switch (pos) {

				case 1:
					//getActionBar().setTitle("Today's Jobs");
					tvAcTitle.setText("Today's Jobs");
					BundleKeys.title = "Today's Jobs";
					BundleKeys.which = 1;
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					etSearch.setText("");
					break;
				case 2:
					//getActionBar().setTitle("Tomorrow's Jobs");
					tvAcTitle.setText("Tomorrow's Jobs");
					BundleKeys.which = 2;
					BundleKeys.title = "Tomorrow's Jobs";
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					etSearch.setText("");
					break;
				case 3:
					//getActionBar().setTitle("Stat Jobs");
					tvAcTitle.setText("Stat Jobs");
					BundleKeys.which = 3;
					BundleKeys.title = "Stat Jobs";
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					etSearch.setText("");
					break;
				case 4:
					//getActionBar().setTitle("All Jobs");
					tvAcTitle.setText("All Jobs");
					BundleKeys.which = 4;
					BundleKeys.title = "All Jobs";
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					etSearch.setText("");
					break;
				case 6:
					//getActionBar().setTitle("Hold Jobs");
					tvAcTitle.setText("Hold Jobs");
					BundleKeys.which = 5;
					BundleKeys.title = "Hold Jobs";
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
					etSearch.setText("");
					break;
				case 7:
					//getActionBar().setTitle("Deleted Jobs");
					tvAcTitle.setText("Deleted Jobs");
					BundleKeys.which = 7;
					BundleKeys.title = "Deleted Jobs";
					// Hide "Add Job" menu item
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(false);
					ivAddJob.setVisibility(View.GONE);
					jobListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
					etSearch.setText("");
					break;
				case 8:
					//getActionBar().setTitle("Completed Jobs");
					tvAcTitle.setText("Completed Jobs");
					BundleKeys.which = 6;
					BundleKeys.title = "Completed Jobs";
					// Hide "Add Job" menu item
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(false);
					ivAddJob.setVisibility(View.GONE);
					jobListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
					etSearch.setText("");
					break;

				case 10:
					isSetting = true;
					//getActionBar().setTitle("Settings");
					tvAcTitle.setText("Settings Jobs");
					startActivity(new Intent(JobListActivity.this,
							EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					finish();
					break;

				case 11:
					// Check if there are any held jobs or any uploads in
					// progress
					boolean isHeld = true,
					isPending = true;
					try {
						List<Job> jobs = AndroidState.getInstance()
								.getUserState().getProvider(currentAccount)
								.getJobs();

						for (Job j : jobs) {
							if (j.isPending())
								isPending = false;
							if (j.isFlagSet(Job.Flags.HOLD))
								isHeld = false;

						}
					} catch (Exception ex) {
						ACRA.getErrorReporter().handleSilentException(ex);
						isPending = false;
						isHeld = false;
					}
					if(SyncService.isRunning()){
						AlertDialog.Builder builder = new AlertDialog.Builder(
								JobListActivity.this);
						builder.setTitle(R.string.del_acc_err_title);
						builder.setMessage(R.string.manage_q_upload_error);
						builder.setPositiveButton("OK", null);
						builder.setCancelable(true);
						dgManageQ = builder.create();
						dgManageQ.show();						
					} else if (isPending && isHeld) {
						Intent qIntent = new Intent(JobListActivity.this,
								ManageQueuesActivity.class);
						qIntent.putExtra("from_settings", false);
						qIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(qIntent);
						finish();
					} else if (!isHeld) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								JobListActivity.this);
						builder.setTitle(R.string.del_acc_err_title);
						builder.setMessage(R.string.manage_q_hold_error);
						builder.setPositiveButton("OK", null);
						builder.setCancelable(true);
						dgManageQ = builder.create();
						dgManageQ.show();
					} else if (!isPending) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								JobListActivity.this);
						builder.setTitle(R.string.del_acc_err_title);
						builder.setMessage(R.string.manage_q_upload_error);
						builder.setPositiveButton("OK", null);
						builder.setCancelable(true);
						dgManageQ = builder.create();
						dgManageQ.show();
					}

					break;

				default:
					//getActionBar().setTitle("Today's Jobs");
					tvAcTitle.setText("Today's Jobs");
					BundleKeys.title = "Today's Jobs";
					BundleKeys.which = 1;
					//menuItem = j_Menu.findItem(R.id.addJobMenuItem);
					//menuItem.setVisible(true);
					ivAddJob.setVisibility(View.VISIBLE);
					jobListView
							.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
				}
				Log.e(LOG_NAME, Integer.toString(BundleKeys.which)+" - "+BundleKeys.title);
				isSync = false;
				slidingMenu.toggle(true);
				/*
				 * Log.e("isSetting", Boolean.toString(isSetting));
				 * if(BundleKeys.which != 8 && BundleKeys.which != 9 &&
				 * !isSetting) etSearch.setText("");
				 */
			}

		});

		rlSearch = (RelativeLayout) findViewById(R.id.rlSearch);

		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				//launchSearchTask();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				launchSearchTask();
			}
		});
		
		GetResourcesTask task =  new GetResourcesTask();
    	task.execute();

	}

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
			
	    	//SyncData();
	    	retryUploads();
	    	ns = new NetworkState(getApplicationContext());
			isConnected = ns.isConnectingToInternet();
			if(!isConnected)
				setUnSentCount();
			else{
				rlSyncError.setVisibility(View.GONE);
				if (BundleKeys.last_updated_date == null
						|| BundleKeys.last_updated_time == null) {
					rlUpdated.setVisibility(View.INVISIBLE);
				} else {
					tvUpdating.setVisibility(View.VISIBLE);
					rlUpdated.setVisibility(View.VISIBLE);
					tvDate.setText(BundleKeys.last_updated_date);
					tvTime.setText(BundleKeys.last_updated_time);
				}
			}
				
		}
        
    };

	
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			/* do what you need to do */
			boolean canSync = true;
			//jobListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
			try {
				UserState state = AndroidState.getInstance().getUserState();

				synchronized (state) {
					currentAccount = state.getCurrentAccount();
				}

				List<Job> jobs = AndroidState.getInstance().getUserState()
						.getProvider(currentAccount).getJobs();

				for (Job j : jobs) {
					if (j.isPending())
						canSync = false;
				}
			} catch (Exception ex) {
				ACRA.getErrorReporter().handleSilentException(ex);
				canSync = false;
			}

			
			//jobListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			if (canSync)
				SyncData();
			/* and here comes the "trick" */
			handler.postDelayed(this, BundleKeys.mins_to_sync * 60 * 1000);

		}
	};

	//Runnable for logs
	private Runnable runnable_logs = new Runnable() {
		@Override
		public void run() {
			//sendLogs();
			handler.postDelayed(this, 5* 60 * 1000);

		}
	};
	
	Calendar c;
	private int year, mon, day, hr, min;
	String str_hr, str_min;
	File root;
	private static final String processId = Integer.toString(android.os.Process.myPid());
	String file_name, response;
	File logFile, newFile;
	File mFolder = null;
	FileOutputStream fOut;
	OutputStreamWriter myOutWriter;
	
	public void sendLogs()
	{
		c = Calendar.getInstance();
		hr = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
		year = c.get(Calendar.YEAR);
		mon = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		
		if(hr >= 10)
			str_hr = Integer.toString(hr);
		else
			str_hr = "0"+ Integer.toString(hr);
			
		
		if(min >= 10)
			str_min = Integer.toString(min);
		else
			str_min = "0"+ Integer.toString(min);
			
		
		//file_name = BundleKeys.SYNC_FOR_CLINIC+"-"+BundleKeys.SYNC_FOR_ACC+"-"+year+"-"+(mon+1)+"-"+day+"-"+str_hr+"-"+str_min;
		file_name = year+"-"+(mon+1)+"-"+day+"-"+str_hr+"-"+str_min;
		
		
		
		try {
				root = AndroidUtils.ENTRADA_DIR;
				Process process = Runtime.getRuntime().exec("logcat -d");
				mFolder = new File(root + "/Logs");
	            if (!mFolder.exists()) {
	                mFolder.mkdir();
            }
			//File logFile = new File(mFolder.getAbsolutePath(), file_name+".txt");
            logFile = new File(mFolder.getAbsolutePath(), "current.txt");
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder log = null;
            log = new StringBuilder();
			if(!logFile.exists()){
				
					      line = "Version Name: "+sp.getString("APP_VER_NAME", "5.3.0") + "\n";
					      line = line +"Build: "+sp.getInt("APP_VER_CODE", 1) + "\n";
					      line = line +"Android: "+android.os.Build.VERSION.RELEASE + "\n";
					      line = line +"Manufacturer: "+android.os.Build.MANUFACTURER + "\n";
					      line = line +"Model: "+android.os.Build.MODEL + "\n";
					      log.append(line+"\n");
					      
					      while ((line = bufferedReader.readLine()) != null) {
					    	  if(line.contains(processId))
					    		  log.append(line+"\n");
					      }
					      
					      fOut = new FileOutputStream(logFile);
			              myOutWriter = new OutputStreamWriter(fOut);
			              myOutWriter.write(log.toString());
				          myOutWriter.close();
				          Log.e("VER -- BUILD", sp.getString("APP_VER_NAME", "5.3.0")+" -- "+sp.getInt("APP_VER_CODE", 1));
			}else{
				long filesizeInKB;
				long filesize = logFile.length();
		        filesizeInKB = filesize / 1024;   
		        
		        if(filesizeInKB >= 250){
		        	AsyncTaskSendLogFiles sendLogs = new AsyncTaskSendLogFiles();
		        	sendLogs.execute();
		        	//newFile = new File(mFolder.getAbsolutePath(), file_name+".txt");
		        	
		        }else{
		        	while ((line = bufferedReader.readLine()) != null) {
				    	  if(line.contains(processId))
				    		  log.append(line+"\n");
				      }
		        	
		        	fOut = new FileOutputStream(logFile);
		            myOutWriter = new OutputStreamWriter(fOut);
		            myOutWriter.write(log.toString());
			        myOutWriter.close();
		        }
					
			}
			
		      root = AndroidUtils.ENTRADA_DIR;
		      
	            
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), 2000).show();
		    	
		    }
	}
	
	int responseCode;
	FileInputStream is = null;
	MessageDigest md  = null;
	String checksum = null;
	DigestInputStream dis = null;
	
	public class AsyncTaskSendLogFiles extends AsyncTask<String, String, Void> {

		public AsyncTaskSendLogFiles() {
			// TODO Auto-generated constructor stub
			
			newFile = new File(mFolder.getAbsolutePath(), file_name+".txt");
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
		}
		
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			try {
				InputStream in = new FileInputStream(logFile);
			    OutputStream out = new FileOutputStream(newFile);
	
			    // Transfer bytes from in to out
			    byte[] buf = new byte[1024];
			    int len;
		    
				while ((len = in.read(buf)) > 0) {
				    out.write(buf, 0, len);
				}
				in.close();
			    out.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		    
			
			/*try {
				FileUtils.copyDirectory(logFile, newFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			File logFileToUpload = new File(mFolder.getAbsolutePath(), file_name+".txt");
			try{ 
				  md = MessageDigest.getInstance("MD5"); 
			}catch(NoSuchAlgorithmException e1) { 
			  // TODO Auto-generated catch block
			  e1.printStackTrace(); 
			}
			
			try {
				is = new FileInputStream(logFileToUpload);
				dis = new DigestInputStream(is, md);
				while (dis.read() != -1)
					continue;
				checksum = Strings.padStart(
						new BigInteger(1, md.digest()).toString(16),
						32, '0');
			} catch (Exception ex) {
				Log.e("Entrada-SendLogs",
						"Failure in checksum construction: ", ex);
			}
			
			try {
			 	APIService	service = new APIService(currentAccount);
			 	service.uploadLogs(is, checksum, logFileToUpload);
			 }catch (ServiceException e) { 
				  // TODO Auto-generated catch block
				  e.printStackTrace(); 
			 } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			newFile.delete();
		}
		
	}


	public void openSlide() {

		countTask = new JobCountTask(this, currentAccount, getFilter(),
				getComparator(), qchanged);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			countTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			countTask.execute();
		}

	}

	private String GENERIC_PATIENT_ID;
   	private List<String> important;
   	Patient genericPatient;
   	
	@Override
	protected void onStart() {
		super.onStart();

		BundleKeys.isCapture = true;
		qchanged = getIntent().getBooleanExtra("qchanged", true);
		isUploading = getIntent().getBooleanExtra("isUploading", false);

		UserState state = AndroidState.getInstance().getUserState();

		if(!application.isJobListEnabled()){
			return;
		}
		
		synchronized (state) {
			currentAccount = state.getCurrentAccount();
		}

		if (currentAccount == null) {
			Log.e("Entrada-JobList",
					"current account is null in onStart; kicking back to user select.");
			Intent intent = new Intent(this, UserSelectActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

		if (BundleKeys.last_updated_date == null
				|| BundleKeys.last_updated_time == null) {
			rlUpdated.setVisibility(View.INVISIBLE);
		} else {
			rlUpdated.setVisibility(View.VISIBLE);
			tvDate.setText(BundleKeys.last_updated_date);
			tvTime.setText(BundleKeys.last_updated_time);
		}

		try {
			UserPrivate user = AndroidState.getInstance().getUserState()
					.getUserData();
			AndroidState.getInstance().clearUserState();
			AndroidState.getInstance().createUserState(user);
			user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
			user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, null);
			user.save();
		} catch (Exception ex) {
			ACRA.getErrorReporter().handleSilentException(ex);
			Toast.makeText(this, "Failed to save user state.",
					Toast.LENGTH_LONG).show();
		}
		/*//get Generic ID
        Account acct = AndroidState.getInstance().getUserState().getCurrentAccount();
        state = AndroidState.getInstance().getUserState();
        reader = state.getProvider(acct);
        
        GENERIC_PATIENT_ID = acct.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
        if(GENERIC_PATIENT_ID != null){
        	genericPatient = reader.getPatient(Long.parseLong(GENERIC_PATIENT_ID));
        	BundleKeys.LOCAL_MRN = genericPatient.medicalRecordNumber;
        	important = Arrays.asList(BundleKeys.LOCAL_MRN);
        }*/

		Log.e("Days_to_Sync", Integer.toString(BundleKeys.days_to_sync));
		Log.e("Mins_to_Sync", Integer.toString(BundleKeys.mins_to_sync));
		launchSearchTask();

		if(EntradaApplication.isNew()) {
			try{
				updateJobsStatus();
				Log.e("", "--updateJobsStatus--");
			} catch(Exception e){
			} finally {
				EntradaApplication.setAppStatus(false);
			}
		}
		
		ns = new NetworkState(getApplicationContext());
		isConnected = ns.isConnectingToInternet();
		if (isConnected)
			retryUploads();

	}
	
    private void updateJobsStatus() throws Exception{
		UserState state = AndroidState.getInstance().getUserState();
		Account account = AndroidState.getInstance().getUserState().getCurrentAccount();
		DomainObjectProvider provider = state.getProvider(account);
		for (Job job : provider.getJobsByAllFlags(Job.Flags.UPLOAD_IN_PROGRESS.value)) {
			job = job.clearFlag(Job.Flags.UPLOAD_IN_PROGRESS);
            job = job.setFlag(Job.Flags.UPLOAD_PENDING);
            provider.writeJob(job);
		}

    }


	private void retryUploads() {
		UserState state = AndroidState.getInstance().getUserState();
		for (Job j : state.getProvider(currentAccount).getJobsByAllFlags(
				Job.Flags.UPLOAD_PENDING.value)) {
			Log.d("Entrada-JobListActivity", String.format(
					"Performing upload of job %d. Flags = [%s].", j.id,
					j.getFlagsString()));
			Log.e("", "retryUploads()--"+j.id);
			DictationUploadService.startUpload(this, state, currentAccount, j);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(!application.isJobListEnabled()){
			return;
		}
		UserState state = AndroidState.getInstance().getUserState();
		SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		int count = reader.getUnreadMessagesCount();
		badge.setText(String.valueOf(count));
		badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
		if(count>0)
			badge.show();
		else 
			badge.hide();

		registerReceiver(conbroadcastReceiver, new IntentFilter(NewConversationBroadcastService.BROADCAST_COUNTUPDATE_ACTION));
		registerReceiver(messagebroadcastReceiver, new IntentFilter(NewConversationBroadcastService.BROADCAST_ACTION));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
		ns = new NetworkState(getApplicationContext());
		isConnected = ns.isConnectingToInternet();
		
		if (qchanged) {
			ns = new NetworkState(getApplicationContext());
			isConnected = ns.isConnectingToInternet();

			if (isConnected) {
				if (!isUploading) {
					isResumed = true;
					//MenuItem menuItem = j_Menu.findItem(R.id.syncMenuItem);
					//onOptionsItemSelected(menuItem);
					SyncData();
				}
			} else {
				rlSyncError.setVisibility(View.VISIBLE);
				rlUpdated.setVisibility(View.GONE);
			}

		} else {
			jobListView.setAdapter(null);
		}

		// Hide "Add Job" menu item for Hold, Deleted, Completed buckets
		if (BundleKeys.which == 6 || BundleKeys.which == 7) {
			ivAddJob.setVisibility(View.GONE);
			jobListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		}

		openSlide();
		if (refreshTask != null) {
			refreshTask.cancel(true);
		}
		refreshTask = new JobListRefreshTask(this);
		refreshTask.execute();

		isLive = true;

		if (SyncService.isRunning()) {
			Intent i = new Intent(this, SyncService.class);
			startService(i);
			rlSyncError.setVisibility(View.GONE);
			rlUpdated.setVisibility(View.INVISIBLE);
			tvUpdating.setVisibility(View.VISIBLE);
		}

		// To get unsent job count
		BundleKeys.UNSENT_COUNT = 0;

		if (!isConnected) {

			rlSyncError.setVisibility(View.VISIBLE);
			rlUpdated.setVisibility(View.GONE);
			tvUpdating.setVisibility(View.GONE);

			List<Job> jobs = AndroidState.getInstance().getUserState()
					.getProvider(currentAccount).getJobs();

			for (Job j : jobs) {
				if (j.isFlagSet(Flags.UPLOAD_IN_PROGRESS)
						|| j.isFlagSet(Flags.UPLOAD_PENDING)) {
					BundleKeys.UNSENT_COUNT++;
				}
			}

			if (BundleKeys.UNSENT_COUNT > 0)
				tvSyncFailed.setText("Sync Failed: " + BundleKeys.UNSENT_COUNT
						+ " Unsent");
			else
				tvSyncFailed.setText("Sync Failed");
		} else {
			rlSyncError.setVisibility(View.GONE);
			if (BundleKeys.last_updated_date == null
					|| BundleKeys.last_updated_time == null) {
				rlUpdated.setVisibility(View.INVISIBLE);
			} else {
				tvUpdating.setVisibility(View.VISIBLE);
				rlUpdated.setVisibility(View.VISIBLE);
				tvDate.setText(BundleKeys.last_updated_date);
				tvTime.setText(BundleKeys.last_updated_time);
			}
		}

	}
	
	
	
	private void setUnSentCount(){
		// To get unsent job count
				BundleKeys.UNSENT_COUNT = 0;

				if (!isConnected) {

					rlSyncError.setVisibility(View.VISIBLE);
					rlUpdated.setVisibility(View.GONE);
					tvUpdating.setVisibility(View.GONE);

					List<Job> jobs = AndroidState.getInstance().getUserState()
							.getProvider(currentAccount).getJobs();

					for (Job j : jobs) {
						if (j.isFlagSet(Flags.UPLOAD_IN_PROGRESS)
								|| j.isFlagSet(Flags.UPLOAD_PENDING)) {
							BundleKeys.UNSENT_COUNT++;
						}
					}

					if (BundleKeys.UNSENT_COUNT > 0)
						tvSyncFailed.setText("Sync Failed: " + BundleKeys.UNSENT_COUNT
								+ " Unsent");
					else
						tvSyncFailed.setText("Sync Failed");
				} else {
					rlSyncError.setVisibility(View.GONE);
					if (BundleKeys.last_updated_date == null
							|| BundleKeys.last_updated_time == null) {
						rlUpdated.setVisibility(View.INVISIBLE);
					} else {
						tvUpdating.setVisibility(View.VISIBLE);
						rlUpdated.setVisibility(View.VISIBLE);
						tvDate.setText(BundleKeys.last_updated_date);
						tvTime.setText(BundleKeys.last_updated_time);
					}
				}
	}

	// handler for received Intents for the "my-event" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    String message = intent.getStringExtra("message");
	    if(message.equalsIgnoreCase(BundleKeys.SYNC_SERVICE_END)) {
	    	if(!itemClicked) {
			TextView tvDate = (TextView) findViewById(R.id.tvDate);
			tvDate.setText(BundleKeys.last_updated_date);
			TextView tvTime = (TextView) findViewById(R.id.tvTime);
			tvTime.setText(BundleKeys.last_updated_time);
			RelativeLayout rlUpdated = (RelativeLayout) findViewById(R.id.rlDateTime);
			UserState state = AndroidState.getInstance().getUserState();
			Account acc = state.getCurrentAccount();
	        String value = acc.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
	        if (value == null)
	        	rlUpdated.setVisibility(View.INVISIBLE);
	        else
	        	rlUpdated.setVisibility(View.VISIBLE);
	        /*if(!BundleKeys.GOT_QUEUES){
	        	SyncData();
	        	BundleKeys.GOT_QUEUES = true;
	        }*/
		    launchSearchTask();
	    	}
	    } else if(message.equalsIgnoreCase(BundleKeys.SYNC_SERVICE_START)) {
	    	//if(!itemClicked) {
	    	rlSyncError.setVisibility(View.GONE);
			rlUpdated.setVisibility(View.INVISIBLE);
			tvUpdating.setVisibility(View.VISIBLE);
	    	//}
	    }
	    else {
	    	if(!itemClicked) {
	        TextView tvNoResults = (TextView)activity.findViewById(R.id.tvNoResults);
	        if(jobIds.size() > 0){
	        	jobListView.setVisibility(View.VISIBLE);
	        	jobListView.setOnScrollListener(new OnScrollListener() {
	        		
	        		private int localCount;
	        		private int previousTotalItemCount;
					
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onScroll(AbsListView view, int firstVisibleItem,
							int visibleItemCount, int totalItemCount) {
						// TODO Auto-generated method stub
						if(adapter != null && ((firstVisibleItem + visibleItemCount) == totalItemCount) && (previousTotalItemCount != totalItemCount)) {
								localCount = localCount + BundleKeys.NUMBER_OF_ITEMS_SHOW_ONSCROLL;
								if(Math.min(jobIds.size(), localCount+BundleKeys.NUMBER_OF_ITEMS_SHOW_ONSCROLL) <= jobIds.size()) {
									List<Long> _list = new ArrayList<Long>();
									for(int i=localCount; i<Math.min(jobIds.size(),localCount+BundleKeys.NUMBER_OF_ITEMS_SHOW_ONSCROLL);i++)
										_list.add(jobIds.get(i));
									
									/*if(searchTask != null) {
										searchTask.cancel(true);
									}
									searchTask = new JobSearchTask(JobListActivity.this, etSearch.getText().toString(),currentAccount, getFilter(), getComparator());
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
										searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									} else {
										searchTask.execute();
									}*/
									adapter.addJobs(_list);
									adapter.notifyDataSetChanged();									
								}
								previousTotalItemCount = totalItemCount;
						}						
					}
				});
	        	tvNoResults.setVisibility(View.GONE);
	        	
	        }else{
	        	jobListView.setVisibility(View.GONE);
	        	tvNoResults.setVisibility(View.VISIBLE);
	        }

	        List<Long> subJobIds = new ArrayList<Long>();
	        if(jobIds.size()>0){ 
	        	for(int i=0; i<Math.min(jobIds.size(), BundleKeys.NUMBER_OF_ITEMS_SHOW_ONSCROLL);i++)
	        		subJobIds.add(jobIds.get(i));
	        } else {
	        	subJobIds = jobIds;
	        }
	        adapter = new JobListItemAdapter(activity, android.R.layout.simple_list_item_1, subJobIds, currentAccount);
        	jobListView.setAdapter(adapter);
        	if(JobListMultiChoiceModeListener.actionMode != null){
        		JobListMultiChoiceModeListener.actionMode.finish();
        	}
    		if (JobListMultiChoiceModeListener.dgDeleteJob != null
    				&& JobListMultiChoiceModeListener.dgDeleteJob.isShowing())
    			JobListMultiChoiceModeListener.dgDeleteJob.dismiss();
	    	}
	    }
	  }
	};

	@Override
	protected void onPause() {
		// Unregister since the activity is not visible
		if(!application.isJobListEnabled()){
			return;
		}
		unregisterReceiver(conbroadcastReceiver);
		unregisterReceiver(messagebroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onPause();
		if (refreshTask != null) {
			refreshTask.cancel(true);
			refreshTask = null;
		}

		isLive = false;
		//finish();
	}
	
	private BroadcastReceiver conbroadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
			// Icon badger on Action bar item
	    	try {
				badge.setText(String.valueOf(application.getIntFromSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT)));
				if(application.getIntFromSharedPrefs(BundleKeys.UNREAD_MESSAGES_COUNT)>0)
					badge.show();
				else 
					badge.hide();
		    } catch(Exception ex){
		    	ex.printStackTrace();
		    }
	    }
	};  

	private BroadcastReceiver messagebroadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
			// Icon badger on Action bar item
			UserState state = AndroidState.getInstance().getUserState();
			SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
			int count = reader.getUnreadMessagesCount();
			try {
				badge.setText(count);
				if(count>0)
					badge.show();
				else 
					badge.hide();
			} catch(Exception ex){
				ex.printStackTrace();
			}
	    }
	};  

	boolean isResumed = false;
	boolean running;

	public void SyncData() {

		if (!isFinishing()) {

			if(BundleKeys.which == 6 || BundleKeys.which == 7)
				jobListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
			else
				jobListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			
			ns = new NetworkState(getApplicationContext());
			isConnected = ns.isConnectingToInternet();

			if (isConnected) {
				running = false;
				ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				for (RunningServiceInfo service : manager
						.getRunningServices(Integer.MAX_VALUE)) {
					if ("com.entradahealth.entrada.android.app.personal.sync.SyncService"
							.equals(service.service.getClassName())
							|| "com.entradahealth.entrada.android.app.personal.sync.DictationUploadService"
									.equals(service.service.getClassName())) {
						running = true;
					}
				}

				if (!running) {
					etSearch.setText(null);
					if (!isFinishing()) {

						//retryUploads();
						boolean canSync = true;
						try {
							List<Job> jobs = AndroidState.getInstance()
									.getUserState().getProvider(currentAccount)
									.getJobs();

							for (Job j : jobs) {
								if (j.isPending()) {
									canSync = false;
									UserState state = AndroidState
											.getInstance().getUserState();
									Log.e("", "syncData()--"+j.id);
									DictationUploadService.startUpload(this,
											state, currentAccount, j);
								}
							}
						} catch (Exception ex) {
							ACRA.getErrorReporter().handleSilentException(ex);
							canSync = false;
						}

						if (!canSync) {
							//Toast.makeText(
								//	this,
									//"Please wait for all uploads to complete before syncing.",
									//Toast.LENGTH_SHORT).show();

						} else {
							rlSyncError.setVisibility(View.GONE);
							rlUpdated.setVisibility(View.INVISIBLE);
							tvUpdating.setVisibility(View.VISIBLE);

							Intent i = new Intent(this, SyncService.class);
							startService(i);
						}

						isSyncing = true;
						BundleKeys.SYNC_FOR_ACC = currentAccount
								.getDisplayName();
						//BundleKeys.SYNC_FOR_CLINIC = currentAccount.getClinicCode();

						/*task1 = buildMinderTask();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						} else {
							task1.execute();
						}*/

					}
				} else {
					/*if (!isResumed)
						Toast.makeText(
								this,
								"Please wait for all uploads to complete before syncing.",
								Toast.LENGTH_SHORT).show();*/
				}
				isResumed = false;

				BundleKeys.ACC_EDITED = false;

			}else {
				rlSyncError.setVisibility(View.VISIBLE);
				rlUpdated.setVisibility(View.GONE);
				tvUpdating.setVisibility(View.GONE);
			}
		} 
	}

	@Override
	public void onBackPressed() {
		if (slidingMenu.isMenuShowing()) {
			this.toggle();
		} else {
			// goBackToUserList();
			finish();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			ns = new NetworkState(getApplicationContext());
			isConnected = ns.isConnectingToInternet();
			if (!isConnected) {
				rlSyncError.setVisibility(View.VISIBLE);
				rlUpdated.setVisibility(View.GONE);
				tvUpdating.setVisibility(View.GONE);
				if (BundleKeys.UNSENT_COUNT > 0)
					tvSyncFailed.setText("Sync Failed: "
							+ BundleKeys.UNSENT_COUNT + " Unsent");
				else
					tvSyncFailed.setText("Sync Failed");
			} else {
				rlSyncError.setVisibility(View.GONE);
				if (BundleKeys.last_updated_date == null
						|| BundleKeys.last_updated_time == null) {
					rlUpdated.setVisibility(View.INVISIBLE);
				} else {
					if(SyncService.isRunning()) {
						tvUpdating.setVisibility(View.VISIBLE);
						rlUpdated.setVisibility(View.INVISIBLE);
					} else {
						tvUpdating.setVisibility(View.INVISIBLE);
						rlUpdated.setVisibility(View.VISIBLE);
						tvDate.setText(BundleKeys.last_updated_date);
						tvTime.setText(BundleKeys.last_updated_time);						
					}					
				}
			}
		}
	}

	public void goBackToUserList() {
		saveState();
		Intent intent = new Intent(this, UserSelectActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void saveState() {
		UserState state = AndroidState.getInstance().getUserState();
		synchronized (state) {
			try {
				state.getUserData().save();
			} catch (IOException ex) {
				Log.d("Entrada-JobList", "error saving user state on back.", ex);
			}
		}
	}

	public List<Long> getJobIdList() {
		return jobIds;
	}

	public void setJobIdList(List<Long> jobIds) {
		this.jobIds = jobIds;
	}

	public List<Job> getJobsSource(){
		return jobsSource;
	}
	
	public void setJobsSource(List<Job> jobsSource){
		this.jobsSource = jobsSource;
	}
	
	
	void launchSearchTask() {
		//get Generic ID
        Account acct = AndroidState.getInstance().getUserState().getCurrentAccount();
        state = AndroidState.getInstance().getUserState();
        reader = state.getProvider(acct);
        
        //if(!BundleKeys.IS_FIRST_SYNC){
	        GENERIC_PATIENT_ID = acct.getSetting(AccountSettingKeys.GENERIC_PATIENT_ID);
	        if(GENERIC_PATIENT_ID != null && !GENERIC_PATIENT_ID.equals("")){
	        	genericPatient = reader.getPatient(Long.parseLong(GENERIC_PATIENT_ID));
	        	if(genericPatient != null){
	        	BundleKeys.LOCAL_MRN = genericPatient.medicalRecordNumber;
	        	important = Arrays.asList(BundleKeys.LOCAL_MRN);
	        }
        }
        //}
		
//		if (qchanged && !BundleKeys.ACC_EDITED) {
			Preconditions.checkNotNull(currentAccount, "current account is null");
			if(searchTask != null) {
				searchTask.cancel(true);
			}
			searchTask = new JobSearchTask(this, etSearch.getText().toString(),currentAccount, getFilter(), getComparator());

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				searchTask.execute();
			}
		}
//	}

	private void showLicensing() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.license_dialog, null);

		TextView textview = (TextView) view.findViewById(R.id.license_text);
		InputStream is = getResources().openRawResource(R.raw.licenses);

		try {
			textview.setText(CharStreams
					.toString(new InputSupplier<InputStreamReader>() {
						public InputStreamReader getInput() throws IOException {
							return new InputStreamReader(getResources()
									.openRawResource(R.raw.licenses));
						}
					}));
		} catch (IOException ex) {
			Toast.makeText(
					this,
					"Sorry, error displaying licenses. Contact support for license details.",
					Toast.LENGTH_SHORT);
		}

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("License Information");
		// alertDialog.setMessage("Here is a really long message.");
		alertDialog.setView(view);
		AlertDialog alert = alertDialog.create();
		alert.show();
	}

	private Predicate<Job> getFilter() {
		Predicate<Job> filter = ALL_JOBS;

		switch (BundleKeys.which) {

		case 1:
			filter = TODAY_JOBS;
			break;
		case 2:
			filter = TOMORROW_JOBS;
			break;
		case 3:
			filter = STAT_JOBS;
			break;
		case 4:
			filter = TOTAL_JOBS;
			break;
		case 5:
			filter = HELD_JOBS;
			break;
		case 6:
			filter = DICTATED_JOBS;
			break;
		case 7:
			filter = DELETED_JOBS;
			break;
		default:
			filter = ALL_JOBS;
		}

		return filter;
	}

	private Comparator<Job> getComparator() {
		return DEFAULT_COMPARATOR;
	}

	public final static Predicate<Job> ALL_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return job != null && !job.isFlagSet(Job.Flags.SERVER_HOLD);
		}
	};

	public final static Predicate<Job> TOTAL_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return ALL_JOBS.apply(job)
					&& !job.isFlagSet(Job.Flags.LOCALLY_DELETED)
					&& !job.isFlagSet(Job.Flags.UPLOAD_COMPLETED)
					&& !job.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS)
					&& !job.isFlagSet(Job.Flags.UPLOAD_PENDING);
		}
	};

	public final Predicate<Job> TODAY_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job input) {
			if (!TODO_JOBS.apply(input))
				return false;

			Account account = AndroidState.getInstance().getUserState()
					.getCurrentAccount();
			try {
				Encounter enc = AndroidState.getInstance().getUserState()
						.getProvider(account).getEncounter(input.encounterId);

				DateTime todayStart = LocalDate.now().toDateTimeAtStartOfDay();
				DateTime tmrwStart = LocalDate.now().plusDays(1)
						.toDateTimeAtStartOfDay();

				return (enc.appointmentDate.equals(todayStart)
						|| enc.appointmentDate.isBefore(todayStart) || enc.appointmentDate
							.isAfter(todayStart))
						&& enc.appointmentDate.isBefore(tmrwStart);
			} catch (Exception ex) {
				return false;
			}
		}
	};

	public final Predicate<Job> TOMORROW_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job input) {
			if (!TODO_JOBS.apply(input))
				return false;

			Account account = AndroidState.getInstance().getUserState()
					.getCurrentAccount();
			try {
				Encounter enc = AndroidState.getInstance().getUserState()
						.getProvider(account).getEncounter(input.encounterId);

				DateTime tmrwStart = LocalDate.now().plusDays(1)
						.toDateTimeAtStartOfDay();
				DateTime dayAfterStart = LocalDate.now().plusDays(2)
						.toDateTimeAtStartOfDay();

				return (enc.appointmentDate.equals(tmrwStart) || enc.appointmentDate
						.isAfter(tmrwStart))
						&& enc.appointmentDate.isBefore(dayAfterStart);
			} catch (Exception ex) {
				return false;
			}
		}
	};

	public final Predicate<Job> STAT_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job input) {
			return TODO_JOBS.apply(input) && input.stat;
		}
	};

	public Predicate<Job> HELD_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return ALL_JOBS.apply(job)
					&& (job.isFlagSet(Job.Flags.HOLD) && !job
							.isFlagSet(Job.Flags.LOCALLY_DELETED));
		}
	};

	public static final Predicate<Job> TODO_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return ALL_JOBS.apply(job)
					&& !job.isFlagSet(Job.Flags.LOCALLY_DELETED)
					&& !job.isFlagSet(Job.Flags.UPLOAD_COMPLETED)
					&& !job.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS)
					&& !job.isFlagSet(Job.Flags.UPLOAD_PENDING);
		}
	};
	public Predicate<Job> DICTATED_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return ALL_JOBS.apply(job)
					&& (job.isFlagSet(Job.Flags.UPLOAD_IN_PROGRESS)
							|| job.isFlagSet(Job.Flags.UPLOAD_COMPLETED)
							|| job.isFlagSet(Job.Flags.UPLOAD_PENDING) || job
								.isFlagSet(Job.Flags.FAILED));
		}
	};
	public static final Predicate<Job> DELETED_JOBS = new Predicate<Job>() {
		@Override
		public boolean apply(@Nullable Job job) {
			return ALL_JOBS.apply(job)
					&& job.isFlagSet(Job.Flags.LOCALLY_DELETED);

		}
	};

    private DomainObjectReader reader;
    private UserState state;
    
	public final Comparator<Job> DEFAULT_COMPARATOR = new Comparator<Job>() {
		@Override
		public int compare(Job lhs, Job rhs) {
			try
            {
/*              Account acct = AndroidState.getInstance().getUserState().getCurrentAccount();
                state = AndroidState.getInstance().getUserState();
                reader = state.getProvider(acct);

                Encounter left = AndroidState.getInstance()
                        .getUserState()
                        .getProvider(acct)
                        .getEncounter(lhs.encounterId);
                Encounter right = AndroidState.getInstance()
                        .getUserState()
                        .getProvider(acct)
                        .getEncounter(rhs.encounterId);*/
                
    	    	String Lstring = application.getEncounter(lhs.encounterId).getDateTimeText().trim();
    	    	String Rstring = application.getEncounter(rhs.encounterId).getDateTimeText().trim();
    	    	
    	    	Date ldate = null, rdate = null;
    	    	DateFormat date = null;
                
                try {
                    date = new SimpleDateFormat("MM-dd-yyyy");
                    DateFormat f = new SimpleDateFormat("MM-dd-yyyy h:mm aa");
                    ldate = f.parse(Lstring);
                    rdate = f.parse(Rstring);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                
                
                if(genericPatient!= null && !isNotValidLong(genericPatient.id) && !isNotValidLong(application.getEncounter(lhs.encounterId).patientId) && 
             		   !isNotValidLong(application.getEncounter(rhs.encounterId).patientId) && date != null && ldate != null && rdate != null &&
             				  application.getEncounter(lhs.encounterId) != null && application.getEncounter(lhs.encounterId).appointmentDate != null && application.getEncounter(rhs.encounterId) != null && application.getEncounter(rhs.encounterId).appointmentDate != null) {
             	   
             	   if(application.getEncounter(lhs.encounterId).patientId == genericPatient.id && application.getEncounter(rhs.encounterId).patientId == genericPatient.id){
                    		if(application.getEncounter(lhs.encounterId) != null && application.getEncounter(lhs.encounterId).appointmentDate != null && application.getEncounter(rhs.encounterId) != null && application.getEncounter(rhs.encounterId).appointmentDate != null){
                 			int genComp = application.getEncounter(lhs.encounterId).appointmentDate.compareTo(application.getEncounter(rhs.encounterId).appointmentDate);
                         	if(genComp == 0){
                 				return (application.getEncounter(lhs.encounterId).appointmentDate.compareTo(application.getEncounter(rhs.encounterId).appointmentDate));
                 			}else{
                 				return -(application.getEncounter(lhs.encounterId).appointmentDate.compareTo(application.getEncounter(rhs.encounterId).appointmentDate));
                 			}
                 		}
                    }
             	   	if( application.getEncounter(lhs.encounterId).patientId == genericPatient.id && application.getEncounter(rhs.encounterId).patientId != genericPatient.id){
                 		return -1;
                 	}
                    	if(application.getEncounter(rhs.encounterId).patientId == genericPatient.id && application.getEncounter(lhs.encounterId).patientId != genericPatient.id){
                 		return 1;
                 	}
                 
                 	if(date == null || ldate == null || rdate == null){
                 		return 0;
                 	}
                     if(date.format(ldate).compareTo(date.format(rdate)) == 0){ //both dates are equal, (ldate.compareTo(rdate)>0)..ldate is after rdate, (ldate.compareTo(rdate)<0)..ldate is before rdate
                     	if(application.getEncounter(lhs.encounterId) == null || application.getEncounter(lhs.encounterId).appointmentDate == null || application.getEncounter(rhs.encounterId) == null || application.getEncounter(rhs.encounterId).appointmentDate == null){
                     		return 0;
                     	}
                     	return (application.getEncounter(lhs.encounterId).appointmentDate.compareTo(application.getEncounter(rhs.encounterId).appointmentDate));
         			}else{
         				if(application.getEncounter(lhs.encounterId) == null || application.getEncounter(lhs.encounterId).appointmentDate == null || application.getEncounter(rhs.encounterId) == null || application.getEncounter(rhs.encounterId).appointmentDate == null){
                     		return 0;
                     	}
                     	return -(application.getEncounter(lhs.encounterId).appointmentDate.compareTo(application.getEncounter(rhs.encounterId).appointmentDate));
                    }
                 }else{
                 	return -1;
                 }
                
            }catch (Exception ex)
            {
                return 0;
            }
		}
	};

	protected boolean isNotValidList(List<String> value){
		if(value == null || value.size() == 0)
			return true;
		else 
			return false;
	}	
	
	protected boolean isNotValidString(String value){
		if(value == null || value == "")
			return true;
		else 
			return false;
	}
	
	protected boolean isNotValidLong(Long value){
		if(Long.valueOf(value) == null)
			return true;
		else 
			return false;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(!application.isJobListEnabled()){
			return;
		}
		if (dgSyncError != null && dgSyncError.isShowing())
			dgSyncError.dismiss();
		if (dgManageQ != null && dgManageQ.isShowing())
			dgManageQ.dismiss();
		if (JobListMultiChoiceModeListener.dgDeleteJob != null
				&& JobListMultiChoiceModeListener.dgDeleteJob.isShowing())
			JobListMultiChoiceModeListener.dgDeleteJob.dismiss();
		
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(!application.isJobListEnabled()){
			return;
		}
		unregisterReceiver(broadcastReceiver);
	}

	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		finish();
	}
	
}