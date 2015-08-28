package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.acra.ACRA;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.job_type.JobTypeViewAdapter;
import com.entradahealth.entrada.android.app.personal.activities.user_select.UserSelectActivity;
import com.entradahealth.entrada.android.app.personal.audio.AudioManager;
import com.entradahealth.entrada.android.app.personal.sync.DictationUploadService;
import com.entradahealth.entrada.android.app.personal.utils.AccountSettingKeys;
import com.entradahealth.entrada.android.app.personal.utils.NetworkState;
import com.entradahealth.entrada.app.personal.menu_adapter.PatDetailsAdapter;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.domain.Dictation;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.ExpressNotesTags;
import com.entradahealth.entrada.core.domain.Gender;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Job.Flags;
import com.entradahealth.entrada.core.domain.JobType;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectProvider;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.remote.APIService;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

/**
 * Presents to the user a job list and provides the standard controls for
 * taking and manipulating a Dictation.
 *
 * @author edr
 * @since 28 Nov 2012
 */

public class JobDisplayActivity extends SlidingFragmentActivity implements OnClickListener, OnSeekBarChangeListener, SensorEventListener, OnPageChangeListener
{
    private static final DecimalFormat SECONDS_FORMAT = new DecimalFormat("0.0");
    public static final String JOB_IN_PROGRESS_ID = "JobInProgress_Id";
    public static final String JOB_IN_PROGRESS_ACCOUNT = "JobInProgress_Account";
    
    SharedPreferences sp;
    Editor edit;
    private PowerManager.WakeLock wakeLock;
    private UserState state;
    private DomainObjectReader reader;
    private long jobId;
    private String accountName = null;
    private String sel_job_str;
    private AlertDialog dgLimit;
    private boolean isNew = false, isErased = false, isInterrupted = false;
    private DomainObjectProvider provider;
    private Job job = null;
    private Account account = null;
    private Patient patient = null;
    private Encounter encounter = null;
    private AudioManager db;
    private AudioManager.AudioProcessor currentProcessor = null;
    ProgressBar pbAmp;
    private boolean wasPlaying = false;
    private boolean discardChanges = false;
    private boolean isInsert = false;
    private boolean isPlay = false;
    private boolean isModeInsert = false;
    private boolean isStop = true;
    private boolean forcedInterruption = true;
    private boolean isRewind = false;
    private boolean hasDictation = false, isModified = false, isFirst = false;
    private Spinner j_spnr;
    private SeekBar j_seek;
    private long timeElapsed, timeRemaining, timeOverwrite;
    private ImageView btnRecord, btnPlay, btnForward, btnRewind, ivCapture;
    private TextView tvPatName, tvMRN, tvPatSex, tvPatDOB, tvTimeElapsed, tvTimeRemaining, tvTimeRemaining1, tvFlags;
    Button leftBtn,rightBtn;
    TextView tvInsert, tvOverwrite, tvCount;
    Button btnInsert, btnOverwrite, btnDeleteToEnd;
    LinearLayout llEndcall;
    RelativeLayout rlMain, rlSeek, rlVolumeMeter, rlImageCount;
    LinearLayout llInsert, llPlayback;
    RatingBar rating;
    AlertDialog dgEmptyJob, dgImageCapture;
    Menu j_Menu;
    Dialog dgTrans;
    Button btnTranscript;
    ListView lvSliding;
    TextView tvListTitle;
    SlidingMenu slidingMenu;
    PatDetailsAdapter padap;
    LinearLayout llpat, lljtype, llstat, llhold, llspeaker;
    ImageView ivPatientImg, ivstat, ivhold, ivspeaker, ivpat, ivjtype;
    String p_name = "UNKNOWN", p_mrn = "99999999", p_dob = "01/01/01", p_gender = "Unknown";
    String str_org_jtype, str_sel_jtype, str_passed_jtype;
    JobTypeSpinnerAdapter spinnerAdapter;
    JobType sel_job_type;
    TextView dur_sec1, dur_min1, dur_sec2, dur_min2;
    int min = 0;
    SensorManager mySensorManager;
    Sensor myProximitySensor;
    android.media.AudioManager am;
    Long initTime = 0L;
    int progress_beg, total_duration;
    float lengthInSeconds;
    Long originalSeconds;
    float lengthInSeconds_beg;
    boolean limitReached, toCapture = false;
    
    /*
     * Handler for Timer
     */
    final int REFRESH_RATE = 100;
    final int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;
    StopWatch timer ;//= new StopWatch();
    
    Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_START_TIMER:
            	timer = new StopWatch(); 
                timer.start(); //start timer
                mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                break;

            case MSG_UPDATE_TIMER:
            	
            	originalSeconds = timer.getElapsedTimeSecs();
            	Log.e("ORG_SECS", Long.toString(originalSeconds));
            	
            	progress_beg = j_seek.getProgress();
            	lengthInSeconds_beg = db.getSamplesInMS(progress_beg) / 1000.0f;
                Log.e("progress_beg", Float.toString(lengthInSeconds_beg));
                
                if(lengthInSeconds_beg == 0.0f){
                	total_duration = (int) (long) originalSeconds;
                }else{
                	total_duration = (int) (long) originalSeconds + (int)(float)lengthInSeconds_beg;
                }
                
                
                if(!isInsert && lengthInSeconds_beg > 0.0f){
                	originalSeconds = originalSeconds + (long)(float)lengthInSeconds_beg;
                }
                Log.e("total_duration", Integer.toString(total_duration));
               
            	
            	long recordingLength = db.getRecordingLength();
    	        lengthInSeconds = db.getSamplesInMS(recordingLength) / 1000.0f;
            	Log.e("REC_LEN", Integer.toString((int)lengthInSeconds));
            	Log.e("OVERWRITE", Long.toString(timeOverwrite));
            	Log.e("PLAY", Boolean.toString(isPlay));
            	
            	if( (!isPlay && ((int)lengthInSeconds) >= 600 && (hasDictation && isInsert)) || 
            			(!isPlay && ((int)lengthInSeconds) >= 600 && (hasDictation && isModeInsert)) ||
            				(!isPlay && ((int)lengthInSeconds) >= 600 && (!hasDictation && !isInsert) ||
            					(!isPlay && ((int)lengthInSeconds) >= 600 && (!hasDictation && isInsert)) ||
            						(!isPlay && ((int)lengthInSeconds) >= 600 && total_duration >= 600)))
            	
            	{ 
            		limitReached = true;
            		stop();
            		timer.stop();
            		dur_min2.setText("0");
            		Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		            vb.vibrate(200);
            		dgLimit();
            		Log.e("limit", "1");
                }else{
            	
	            	if(hasDictation){
	            		
	            		/*if(total_duration <= 59) 
		                	dur_min2.setText("0");
		                
	            		dur_min2.setVisibility(View.VISIBLE);
	                	dur_sec1.setVisibility(View.VISIBLE);
	                	dur_sec2.setVisibility(View.VISIBLE);*/
	            		if(!isInsert){
		                	if(originalSeconds == timeOverwrite){
		                		Log.e("equal", "sss");
		                		isInMiddle = true;
		                		//llEndcall.setVisibility(View.VISIBLE);
		                        //tvTimeRemaining1.setVisibility(View.GONE);
		                	}
	                	}else{
	                		//llEndcall.setVisibility(View.VISIBLE);
	                        //tvTimeRemaining1.setVisibility(View.GONE);
	                        originalSeconds = originalSeconds + timeOverwrite;
	                	}
	                }else{
	                	//dur_min1.setVisibility(View.VISIBLE);
	                	dur_min2.setVisibility(View.GONE);
	                	dur_sec1.setVisibility(View.GONE);
	                	dur_sec2.setVisibility(View.GONE);
	                	dur_min1.setText("0");
	                	dur_min2.setText("0");
	                	dur_sec1.setText("0");
	                	dur_sec2.setText("0");
	                	llEndcall.setVisibility(View.GONE);
                        tvTimeRemaining1.setVisibility(View.GONE);
	                }
	            	
	            	dur_sec2.setText(""+ originalSeconds);
	                if(originalSeconds >= 10){
	                	dur_sec1.setVisibility(View.GONE);
	                }
	                //if(originalSeconds > 59){ 
	                	
	                	int mins = (int)(originalSeconds/60);
	                	int secs = (int)(originalSeconds%60);
	                	
	                	String seconds = "0";
	                	if(secs <= 9){
	                		seconds = "0"+Integer.toString(secs);
	                	}else{
	                		seconds = Integer.toString(secs);
	                	}
	                	
	                	dur_min2.setText(Integer.toString(mins));
	                	dur_sec1.setText(seconds);
	                	if(menuTvRemaining!=null)menuTvRemaining.setText(mins+":"+seconds); 
	                	dur_sec1.setVisibility(View.VISIBLE);
	                	dur_sec2.setVisibility(View.GONE);
	                	dur_sec1.setVisibility(View.GONE);
	                	dur_min2.setVisibility(View.GONE);
	                //}
	                	mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second, though the timer is still running 
                }
	                break;                                  
	                
	                
            case MSG_STOP_TIMER:
            	timer.stop();
            	dur_sec1.setVisibility(View.VISIBLE);
            	break;
                
            default:	
            	
            }
        }
    };
    

    JobTypeViewAdapter jtAdapter;
    PatientInfoViewPagerAdapter mAdapter;
    ViewPager mPager, jtPager;
    PageIndicator mIndicator;
    RelativeLayout llPatientInfoLayer, rlJobTypeinfoLayer;
    RelativeLayout rlTransparentLayer, rljtTransparentLayer;
    @Override
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setBehindContentView(R.layout.pat_demographics_sliding_menu);
        //tvListTitle = (TextView)findViewById(R.id.tvListTitle);
        //tvListTitle.setText(null);
        lvSliding = (ListView)findViewById(R.id.lvSlidingMenu);
        //lvSliding.setBackgroundColor(Color.parseColor("#f5f5f5"));
        
        //Get screen width and set sliding width to 3/4 
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int req_width = width*3/4;
        
        
        slidingMenu = getSlidingMenu();
        slidingMenu.setFadeEnabled(true);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindWidth(req_width);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setOnOpenedListener(new OnOpenedListener() {
			
			@Override
			public void onOpened() {
				// TODO Auto-generated method stub
				//ivpat.setImageResource(R.drawable.patient_active);
				//ivpat.setTag("1");
			}
		});
        
        slidingMenu.setOnClosedListener(new OnClosedListener() {
			
			@Override
			public void onClosed() {
				// TODO Auto-generated method stub
				//ivpat.setImageResource(R.drawable.patient);
				//ivpat.setTag("0");
			}
		});
        
        limitReached = false;
        //Check for proximity sensor and initialize if available
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        //Get reference to Audio manager
        am = (android.media.AudioManager)getSystemService(Context.AUDIO_SERVICE); 
        
        
        setContentView(R.layout.job_display);
        
        
        rlMain = (RelativeLayout)findViewById(R.id.rlMain);
        rlSeek = (RelativeLayout)findViewById(R.id.rlSeekBar);
        rlVolumeMeter = (RelativeLayout)findViewById(R.id.rlVolumeMeter);
        rating = (RatingBar)findViewById(R.id.rating);
        rating.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
        });
                
        //Fly out for Patient Clinical Info
        llPatientInfoLayer=(RelativeLayout) findViewById(R.id.llPatientInfoLayer);
        llPatientInfoLayer.setVisibility(View.GONE);
        rlTransparentLayer=(RelativeLayout)findViewById(R.id.rlTransparentLayer);
        rlTransparentLayer.setVisibility(View.GONE);
        rlTransparentLayer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
				if(llPatientInfoLayer.isShown()){
					llPatientInfoLayer.setVisibility(View.GONE);
					rlTransparentLayer.setVisibility(View.GONE);
					return true;
				}
			}
				return false;
			}
		});
        
        rlTransparentLayer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(llPatientInfoLayer.isShown()){
					rlTransparentLayer.setVisibility(View.GONE);
					llPatientInfoLayer.setVisibility(View.GONE);
				}
				
			}
		});
        
        mPager = (ViewPager)findViewById(R.id.pager);
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        
        leftBtn = (Button)findViewById(R.id.leftSelector);
        leftBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(jtPager.getCurrentItem()>0){
					jtPager.setCurrentItem(jtPager.getCurrentItem()-1,true);
				}
				if(jtPager.getCurrentItem()==0){
					//jtPager.setCurrentItem(BundleKeys.myJobTypes.size()+1,true);
				}  
			}
		});
        
		rightBtn = (Button)findViewById(R.id.rightSelector);
		rightBtn.setOnClickListener(new OnClickListener() {
        
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(jtPager.getCurrentItem()<BundleKeys.myJobTypes.size()+2){
					jtPager.setCurrentItem(jtPager.getCurrentItem()+1,true);
				}
				if(jtPager.getCurrentItem()==BundleKeys.myJobTypes.size()+1){
					jtPager.setCurrentItem(0,true);
				
				}
			}
		});
        
        //Fly out for Job Type
        rlJobTypeinfoLayer=(RelativeLayout) findViewById(R.id.rlJobTypeInfoLayer);
        rlJobTypeinfoLayer.setVisibility(View.GONE);
        rljtTransparentLayer=(RelativeLayout)findViewById(R.id.rljtTransparentLayer);
        rljtTransparentLayer.setVisibility(View.GONE);
        rljtTransparentLayer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
				if(rlJobTypeinfoLayer.isShown()){
					rlJobTypeinfoLayer.setVisibility(View.GONE);
					rljtTransparentLayer.setVisibility(View.GONE);
					return true;
				}
			}
				return false;
			}
		});
        
        rljtTransparentLayer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(rlJobTypeinfoLayer.isShown()){
					rljtTransparentLayer.setVisibility(View.GONE);
					rlJobTypeinfoLayer.setVisibility(View.GONE);
				}
				
			}
		});
        
        llPlayback = (LinearLayout)findViewById(R.id.llPlayBack);

        jtPager = (ViewPager)findViewById(R.id.jtpager);
        jtPager.setOnPageChangeListener(this);
        //mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        
      //Get reference to footer icons
      		ivpat = (ImageView)findViewById(R.id.ivftPat);
      		ivpat.setTag("0");
      		ivjtype = (ImageView)findViewById(R.id.ivftJtype);
      		ivjtype.setTag("0");
      		ivstat = (ImageView)findViewById(R.id.ivftStat);
      		ivstat.setTag("0");
      		ivhold = (ImageView)findViewById(R.id.ivftHold);
      		ivhold.setTag("0");
      		ivspeaker = (ImageView)findViewById(R.id.ivftSpeaker);
      		ivspeaker.setTag("0");
      		
      		menuTvRemaining=(TextView) findViewById(R.id.tvRemainingTime);
      		menuTvRemaining.setVisibility(View.GONE);
        
      //Get reference to footer layout
      		llpat = (LinearLayout)findViewById(R.id.llPatient);
      		llpat.setOnClickListener(new OnClickListener() {
				
      			@Override
				public void onClick(View v) {
      				if(Boolean.parseBoolean(account.getSetting(AccountSettingKeys.PATIENT_CLINICALS))){
	      				if(clinicInfoDisplayed){
					DisplayMetrics dp=new DisplayMetrics();
			    	getWindowManager().getDefaultDisplay().getMetrics(dp);
			    	int[] locations = new int[2];
			        btnRecord.getLocationOnScreen(locations);
			        int x = locations[0];
			        int y = locations[1];
			   int height= 	dp.heightPixels-y;
			   llPatientInfoLayer.getLayoutParams().height=height;
						
					if(llPatientInfoLayer.isShown()){
						llPatientInfoLayer.setVisibility(View.GONE);
					}else{
						
						if(hasClinicals == 0){
							Toast.makeText(getApplicationContext(), "Clinic does not support clinicals", Toast.LENGTH_SHORT).show();
						}else if(hasClinicals == 1){
							Toast.makeText(getApplicationContext(), "Clinical information not available", Toast.LENGTH_SHORT).show();
						}else{
						llPatientInfoLayer.setVisibility(View.VISIBLE);
						rlTransparentLayer.setVisibility(View.VISIBLE);
						if(BundleKeys.isPatientInfoShowing){
			        		mPager.setCurrentItem(BundleKeys.patientPageNumber);
			        	}
					}
				}
	      				}else{
	      					PatientInfoAysncTask patclinicalTask = new PatientInfoAysncTask();
	      		            patclinicalTask.execute();
	      				}
      				}else{
      					Toast.makeText(getApplicationContext(), "Clinic does not support clinicals", Toast.LENGTH_SHORT).show();
      				}
				}
			});
      		
      	/*	json.put("hmMedicationsAllergiesStroedValues", PatientInfoPageNewListAdapter.hmMedicationsAllergiesStroedValues);
			json.put("hmMedicationsStroedValues", PatientInfoPageNewListAdapter.hmMedicationsStroedValues);
			json.put("hmProblemsStroedValues", PatientInfoPageNewListAdapter.hmProblemsStroedValues);
			json.put("hmPastMedicalStroedValues", PatientInfoPageNewListAdapter.hmPastMedicalStroedValues);
			json.put("hmLastHpiStroedValues", PatientInfoPageNewListAdapter.hmLastHpiStroedValues);*/
			SharedPreferences spPatientInfo=getSharedPreferences("PatientInfoPage", Context.MODE_WORLD_READABLE);
			String str=spPatientInfo.getString("PatientInfoSave", "");
			try{
				
				JSONObject json=new JSONObject(str);
				if(json.get("hmMedicationsAllergiesStroedValues") instanceof HashMap)
				PatientInfoPageNewListAdapter.hmMedicationsAllergiesStroedValues=	(HashMap<Integer, Boolean>) json.get("hmMedicationsAllergiesStroedValues");
				if(json.get("hmMedicationsStroedValues") instanceof HashMap)
				PatientInfoPageNewListAdapter.hmMedicationsStroedValues=	(HashMap<Integer, Boolean>) json.get("hmMedicationsStroedValues");
				if(json.get("hmProblemsStroedValues") instanceof HashMap)
				PatientInfoPageNewListAdapter.hmProblemsStroedValues=	(HashMap<Integer, Boolean>) json.get("hmProblemsStroedValues");
				if(json.get("hmPastMedicalStroedValues") instanceof HashMap)
				PatientInfoPageNewListAdapter.hmPastMedicalStroedValues=	(HashMap<Integer, Boolean>) json.get("hmPastMedicalStroedValues");
				if(json.get("hmLastHpiStroedValues") instanceof HashMap)
				PatientInfoPageNewListAdapter.hmLastHpiStroedValues=	(HashMap<Integer, Boolean>) json.get("hmLastHpiStroedValues");
			}catch(Exception e){
				e.printStackTrace();
			}
      		
			ivPatientImg = (ImageView)findViewById(R.id.ivPatientImg);
			ivPatientImg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					slidingMenu.toggle();
				}
			});
      		
      		
      		lljtype = (LinearLayout)findViewById(R.id.llJobType);
      		lljtype.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
										
					if(menuTvRemaining.getVisibility() == View.VISIBLE)
						showJobTypeFlyout(true);
					else
						showJobTypeFlyout(false);
			
					}
			});
			
      		
      		llstat = (LinearLayout)findViewById(R.id.llStat);
      		llstat.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(ivstat.getTag().toString().trim().equals("0")){
						ivstat.setImageResource(R.drawable.stat_active);
						ivstat.setTag("1");
						
						UserState state = AndroidState.getInstance().getUserState();
				        synchronized (state)
				        {
				            try
				            {
				                job = job.setStat(true).setFlag(Job.Flags.LOCALLY_MODIFIED);
				            }
				            catch (Exception ex)
				            {
				                Toast.makeText(JobDisplayActivity.this, "Exception: " + ex.toString(), 2500);
				            }
				        }
						
					}else{
						ivstat.setImageResource(R.drawable.stat);
						ivstat.setTag("0");
						
						UserState state = AndroidState.getInstance().getUserState();
				        synchronized (state)
				        {
				            try
				            {
				                job = job.setStat(false).setFlag(Job.Flags.LOCALLY_MODIFIED);
				            }
				            catch (Exception ex)
				            {
				                Toast.makeText(JobDisplayActivity.this, "Exception: " + ex.toString(), 2500);
				            }
				        }
					}
				}
			});
      		
      		llhold = (LinearLayout)findViewById(R.id.llHold);
      		llhold.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(ivhold.getTag().toString().trim().equals("0")){
						ivhold.setImageResource(R.drawable.hold_active);
						ivhold.setTag("1");
						
						job = job.setFlag(Job.Flags.HOLD);
				        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
				        menuItem.setTitle("SAVE");
				        
					}else{
						BundleKeys.Held_Encounter_IDs = new ArrayList<Long>();
						//Remove this job from List_Held_Jobs, Encounter
						if(BundleKeys.Held_Encounter_IDs.contains(job.encounterId)){
							BundleKeys.Held_Encounter_IDs.remove(job.encounterId);
						}
						
						ivhold.setImageResource(R.drawable.hold);
						ivhold.setTag("0");
						
						
						job = job.clearFlag(Job.Flags.HOLD);
						
				        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
				        menuItem.setTitle("SEND");
				        
					}
				}
			});
      		
      		llspeaker = (LinearLayout)findViewById(R.id.llSpeaker);
      		llspeaker.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(ivspeaker.getTag().toString().trim().equals("0")){
						ivspeaker.setImageResource(R.drawable.speaker_active);
						ivspeaker.setTag("1");
						//am.setSpeakerphoneOn(true);
						int maxVolume = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
						am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, maxVolume, android.media.AudioManager.FLAG_PLAY_SOUND);
						
						/*if(isPlay){
							db.stop();
							assert (db.getState() == AudioManager.AudioState.IDLE);
							currentProcessor = null;
							int startSample = j_seek.getProgress();
					        currentProcessor = db.beginPlayback(startSample, true);
					        currentProcessor.setMonitor(new JobDisplayAudioDBProgressMonitor());
						}*/
						
						
						
					}else{
						ivspeaker.setImageResource(R.drawable.speaker);
						ivspeaker.setTag("0");
						am.setSpeakerphoneOn(false);
						//int maxVolume = am.getStreamVolume(android.media.AudioManager.MODE_IN_CALL);
						//am.setStreamVolume(android.media.AudioManager.MODE_IN_CALL, maxVolume, android.media.AudioManager.FLAG_PLAY_SOUND);
						
						/*if(isPlay){
							db.stop();
							assert (db.getState() == AudioManager.AudioState.IDLE);
							currentProcessor = null;
							int startSample = j_seek.getProgress();
					        currentProcessor = db.beginPlayback(startSample, false);
					        currentProcessor.setMonitor(new JobDisplayAudioDBProgressMonitor());
						}*/
						
						
					}
				}
			});
      		
      		ivCapture = (ImageView) findViewById(R.id.iv_capture);
    		

    		rlImageCount = (RelativeLayout) findViewById(R.id.rlImageCount);
    		rlImageCount.setOnClickListener(new OnClickListener() {

    			@Override
    			public void onClick(View v) {
    				// TODO Auto-generated method stub
    				toCapture = true;
    				BundleKeys.isCapture = false;
    				BundleKeys.job_type = getActionBar().getTitle().toString();
    				Intent cIntent = new Intent();
    				cIntent.putExtra("job_type", getActionBar().getTitle()
    						.toString());
    				cIntent.putExtra("isModified", isModified);
    				cIntent.putExtra("isDeleted", isDeleted);
    				cIntent.putExtra("isFromList", isFromList);
    				cIntent.putExtra("isFirst", false);
    				cIntent.putExtra("isNew", true);
    				cIntent.putExtra("img_count", img_count);
    				cIntent.putExtra(BundleKeys.SELECTED_JOB, jobId);
    				cIntent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
    				cIntent.putExtra("sel_job_str", sel_job_str);
    				cIntent.putExtra("interrupted", false);
    				cIntent.setClass(JobDisplayActivity.this, CaptureImages.class);
    				cIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				startActivity(cIntent);
    			}
    		});
    		tvCount = (TextView) findViewById(R.id.tvCount);

      		
      		//Speaker should default to ON
      		ivspeaker.setImageResource(R.drawable.speaker_active);
			ivspeaker.setTag("1");
			am.setSpeakerphoneOn(true);
      		
      		j_spnr = (Spinner)findViewById(R.id.jobTypeSpinner);	

      		llInsert = (LinearLayout)findViewById(R.id.llInsert);
      		      		
      		btnInsert = (Button)findViewById(R.id.btnInsert);
      		btnInsert.setOnClickListener(this);
      		btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_white);
      		
      		btnOverwrite = (Button)findViewById(R.id.btnOverwrite);
      		btnOverwrite.setOnClickListener(this);
      		btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_left_blue);
      		
      		btnDeleteToEnd = (Button)findViewById(R.id.btnDeleteToEnd);
      		btnDeleteToEnd.setOnClickListener(this);
      		btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
        
      		dur_sec2 = (TextView) findViewById(R.id.dur_sec2);
    		dur_sec2.setText("0");
    		
    		dur_min2 = (TextView)findViewById(R.id.dur_min2);
    		dur_min2.setText("0");
    		
    		dur_sec1 = (TextView) findViewById(R.id.dur_sec1);
    		dur_sec1.setText("0");
    						
    		dur_min1 = (TextView) findViewById(R.id.dur_min1);
    		dur_min1.setText("0");
    		
        j_seek = (SeekBar)findViewById(R.id.seekBar);
        j_seek.setOnSeekBarChangeListener(this);
        
        btnPlay = (ImageView)findViewById(R.id.playButton);
        btnPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (db.getState() == AudioManager.AudioState.IDLE && j_seek.getProgress() != db.getRecordingLength())
		        {
					isPlay = true;
		            beginPlayback();
		        } else
		        {
		        	isPlay = true;
		        	enableControlsForPlay();
		        }
			}
		});
        
        btnRecord = (ImageView)findViewById(R.id.recordButton);
        btnRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (db.getState() == AudioManager.AudioState.IDLE)
		        {
					if(BundleKeys.VIB_ON_RECORD){
						Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			            vb.vibrate(100);
					}
					isPlay = false;
					limitReached = false;
					beginRecord(!isInsert);
		        } else
		        {
		        	if(BundleKeys.VIB_ON_STOP){
		    			Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		                vb.vibrate(100);
		    		}
		        	isPlay = false;
		        	isStop = true;
		        	forcedInterruption = true;
		            stop(); 
		        }
			}
		});
        
        /*int[] values = new int[2]; 
        btnRecord.getLocationOnScreen(values);
        Log.e("X & Y",values[0]+" "+values[1]);*/
        
        Log.e("top, bottom, left", Integer.toString(btnRecord.getTop()));
        
        btnForward = (ImageView)findViewById(R.id.fastForwardButton);
        btnForward.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isRewind = false;
				j_seek.setProgress((int) db.getRecordingLength());
			}
		});
        
        btnRewind = (ImageView)findViewById(R.id.rewindButton);
        btnRewind.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isRewind = true;
				timeOverwrite = 0;
				time1 = 0;
				j_seek.setProgress(0);
			}
		});
        
                
        tvPatName = (TextView)findViewById(R.id.patientNameText);
        tvMRN = (TextView)findViewById(R.id.patientMRNText);
        tvPatSex = (TextView)findViewById(R.id.patientSex);
        tvPatDOB = (TextView)findViewById(R.id.patientDOB);
        
        tvTimeElapsed = (TextView)findViewById(R.id.timeElapsedText);
        tvTimeRemaining = (TextView)findViewById(R.id.timeRemainingText);
        tvTimeRemaining1 = (TextView)findViewById(R.id.timeRemainingText1);
        
        llEndcall = (LinearLayout)findViewById(R.id.llEndCall);
        
        // TODO: the error handling in this is gross. fix this up.

        IMG_COUNT = getIntent().getExtras().getInt("img_count");
		isFirst = getIntent().getExtras().getBoolean("isFirst");
		isModified = getIntent().getExtras().getBoolean("isModified");
		isDeleted = getIntent().getExtras().getBoolean("isDeleted");
		isFromList = getIntent().getExtras().getBoolean("isFromList");
		isNew = getIntent().getExtras().getBoolean("isNew");
		jobId = getIntent().getExtras().getLong(BundleKeys.SELECTED_JOB);
		accountName = getIntent().getExtras().getString(
				BundleKeys.SELECTED_JOB_ACCOUNT);
		sel_job_str = getIntent().getExtras().getString("sel_job_str");
		isInterrupted = getIntent().getExtras().getBoolean("interrupted");

        
        if (jobId == -1337)
        {
            Log.d("Entrada-JobDisplay", "JobDisplayActivity created with no job number.");
            Intent intent = new Intent(this, JobListActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (accountName == null)
        {
            Log.d("Entrada-JobDisplay", "JobDisplayActivity created with no account name.");
            Intent intent = new Intent(this, JobListActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d("Entrada-JobDisplay", "Job id: " + jobId);
        
        sp = getSharedPreferences("Entrada",
				Context.MODE_WORLD_READABLE);
        if(sp.getBoolean("SECURE_MSG", true))
        	getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        else
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        
        if(isInterrupted)
        	if(!BundleKeys.isAppKilled)
        		dgInterrupted(getResources().getString(R.string.dic_interupt_short), false);
        	else
        		dgInterrupted(getResources().getString(R.string.dic_interupt_long), true);
        


    }
    
    /*
     * Show JobType fly out
     */
    
    public void showJobTypeFlyout(boolean isRecord){
    	
    	if(!isRecord){
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    		        LayoutParams.MATCH_PARENT,      
    		        LayoutParams.WRAP_CONTENT
    		);
    		params.setMargins(0, dpToPx(120), 0, dpToPx(80));
    		rlJobTypeinfoLayer.setLayoutParams(params);
    		//rlJobTypeinfoLayer.setPadding(0, 160, 0, 64);
    	}else{
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    		        LayoutParams.MATCH_PARENT,      
    		        LayoutParams.WRAP_CONTENT
    		);
    		params.setMargins(dpToPx(8), dpToPx(275), dpToPx(8), dpToPx(70));
    		rlJobTypeinfoLayer.setLayoutParams(params);
    		//rlJobTypeinfoLayer.setPadding(8, 660, 8, 180);
    	}
    	
    	DisplayMetrics dp=new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dp);
    	int[] locations = new int[2];
    	llPlayback.getLocationOnScreen(locations);
        int x = locations[0];
        int y = locations[1];
        int height = dp.heightPixels-y;
        rlJobTypeinfoLayer.getLayoutParams().height=height;
		;
		if(rlJobTypeinfoLayer.isShown()){
			rlJobTypeinfoLayer.setVisibility(View.GONE);
		}else{
			
			rlJobTypeinfoLayer.setVisibility(View.VISIBLE);
			rljtTransparentLayer.setVisibility(View.VISIBLE);
			//jtPager.setCurrentItem(0);
			if(BundleKeys.isjtInfoShowing){
				jtPager.setCurrentItem(BundleKeys.jtPageNumber);
        	}else{
        		//if(isFirst)
					for(int i=0;i<BundleKeys.myJobTypes.size();i++){
						if(BundleKeys.myJobTypes.get(i).name.trim().equals(getActionBar().getTitle().toString().trim())){
							jtPager.setCurrentItem(i);
							break;
						}
					}
        	}
		}
    }
    
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    
    /*
     * Dictation interrupted dialog
     */
    AlertDialog dgInterrupt;
    private void dgInterrupted(String msg, boolean isAppKilled){
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setTitle(R.string.dic_interupt_title);
        builder.setCancelable(false);
        
        if(isAppKilled){
        	builder.setPositiveButton("OK", null);
        }else{
        	builder.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    				dgInterrupt.dismiss();
    			}
    		});
            builder.setNegativeButton("Resume", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				// TODO Auto-generated method stub
    				dgInterrupt.dismiss();
    				if (db.getState() == AudioManager.AudioState.IDLE)
    		        {
    					if(BundleKeys.VIB_ON_RECORD){
    						Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    			            vb.vibrate(100);
    					}
    					isPlay = false;
    					beginRecord(!isInsert);
    		        } else
    		        {
    		        	if(BundleKeys.VIB_ON_STOP){
    		    			Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    		                vb.vibrate(100);
    		    		}
    		        	isPlay = false;
    		        	isStop = true;
    		            stop(); 
    		        }
    			}
    		});

        }
                
        dgInterrupt = builder.create();
        dgInterrupt.show();
        forcedInterruption = true;
        edit = sp.edit();
        edit.putBoolean("IS_INTERRUPTED", false);
        edit.commit();
    }

    int img_count, IMG_COUNT;
	private boolean isFromList = true, isDeleted = false;
    
    @Override
    protected void onStart()
    {
        super.onStart();
        BundleKeys.toCall = false;
        BundleKeys.fromImageDisplay = false;
        BundleKeys.fromCaputreImages = false;
        BundleKeys.fromSecureMessaging = false;
        state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            this.account = state.getAccount(accountName);
            if (this.account == null)
            {
                Log.d("Entrada-JobDisplay", "Account name could not be found in user state.");
                super.onBackPressed();
                return;
            }

            reader = state.getProvider(account);

            this.job = reader.getJob(jobId);
            if (this.job == null)
            {
                Log.d("Entrada-JobDisplay", "Job " + jobId + " could not be found in user state.");
                super.onBackPressed();
                return;
            }
            else if (job.isComplete()) //|| job.isPending())
            {
                Log.e("Entrada-JobDisplay", "Redirected to display for job " + jobId + " but job is done.");
                super.onBackPressed();
                return;
            }

            this.encounter = reader.getEncounter(job.encounterId);
            if (this.encounter == null)
            {
                Log.d("Entrada-JobDisplay", "Encounter " + job.encounterId + " could not be found in user state.");
                super.onBackPressed();
                return;
            }

            this.patient = reader.getPatient(encounter.patientId);
            if (this.patient == null)
            {
                Log.d("Entrada-JobDisplay", "Patient " + encounter.patientId + " could not be found in user state.");
                super.onBackPressed();
                return;
            }
            
            tvPatName.setText(patient.getName());
            SharedPreferences spPatientInfo=getSharedPreferences("PatientInfoPage", Context.MODE_WORLD_READABLE);
            if(!spPatientInfo.getString("PatientName", "").equalsIgnoreCase(patient.getName())){
            	PatientInfoPageNewListAdapter.hmMedicationsAllergiesStroedValues.clear();
				PatientInfoPageNewListAdapter.hmMedicationsStroedValues.clear();
				PatientInfoPageNewListAdapter.hmProblemsStroedValues.clear();
				PatientInfoPageNewListAdapter.hmPastMedicalStroedValues.clear();
				PatientInfoPageNewListAdapter.hmLastHpiStroedValues.clear();
            }
			spPatientInfo.edit().putString("PatientName", patient.getName()).commit();            
            if (patient.gender != Gender.UNKNOWN)
            {
                p_name = patient.getName();
                p_gender = patient.gender.toString().substring(0,1);
                p_gender = p_gender.equals("M")?"Male":"Female";
                tvPatSex.setText(p_gender);
            }else{
            	tvPatSex.setVisibility(View.GONE);
            }

            
            
            String dob = this.patient.dateOfBirth.trim();
            if(dob.trim().equals("") || dob.trim().equals(null)){
            	p_dob = "01/01/01";
            	tvPatDOB.setVisibility(View.GONE);
            }else{
            	p_dob = dob.trim();
            	tvPatDOB.setText(p_dob);
            }
            String mrn = this.patient.medicalRecordNumber.trim();
            p_mrn = mrn.trim();

            if(mrn.isEmpty()){
            	tvMRN.setVisibility(View.GONE);
            }else{
            	tvMRN.setText(mrn);
            }
            
            
            provider = state.getProvider(account);
            Log.e("JOB-ID", Long.toString(jobId));
          //Get Referring Physician data
            BundleKeys.refID = provider.getReferringPhysicians(jobId);
            if(BundleKeys.refID != 0){
            	BundleKeys.list_rp = provider.getPhysicians(BundleKeys.refID);
            }else{
            	BundleKeys.list_rp = null;
            }
            
            BundleKeys.pcpID = patient.pcpid;
            if(BundleKeys.pcpID != null && !BundleKeys.pcpID.isEmpty()){
            	BundleKeys.list_pcp = provider.getPhysicians(Long.parseLong(BundleKeys.pcpID));
            }else{
            	BundleKeys.list_pcp = null;
            }

            //PatientInfoAysncTask patclinicalTask = new PatientInfoAysncTask();
            //patclinicalTask.execute();

            j_spnr.setAdapter(new JobTypeSpinnerAdapter(this, provider.getJobTypes()));
            setSpinnerToDefault(j_spnr);
            File accountPath = new File(state.getUserData()
					.getUserAccountsDir(), accountName);
			File dbPath = new File(accountPath, String.valueOf(jobId));
			dbPath.mkdirs();

			File finalPath;
			// create folder for images
			if(job.isFlagSet(Flags.IS_FIRST)){// && !job.isFlagSet(Flags.HOLD)){
				finalPath = new File(dbPath, "Images");
			}else{
				finalPath = new File(dbPath, "temp");
			}
			
			if(!finalPath.exists())
				finalPath.mkdirs();
			Log.e("current_path", finalPath.toString());
			File[] contents = finalPath.listFiles();

			img_count = contents.length;
			//IMG_COUNT = img_count;
			if (img_count > 0) {
				/*if(isFirst){
					final File images = new File(dbPath, "Images");
					final File temp = new File(dbPath, "temp");
					try {
						FileUtils.copyDirectory(temp,images);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
				//isFirst = false;
				ivCapture.setVisibility(View.GONE);
				rlImageCount.setVisibility(View.VISIBLE);
				tvCount.setText(Integer.toString(img_count));
			} else {
				//isFirst = true;
				ivCapture.setVisibility(View.VISIBLE);
				rlImageCount.setVisibility(View.GONE);
			}


            try {
                this.db = AudioManager.loadOrCreate(dbPath);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            
            
        }


       //check if stat or hold and highlight the corresponding footer icons
        //STAT
        if(job.stat){
        	ivstat.setImageResource(R.drawable.stat_active);
			ivstat.setTag("1");
		}else{
        	ivstat.setImageResource(R.drawable.stat);
			ivstat.setTag("0");
        }
        
        //HOLD
        if(job.isFlagSet(Job.Flags.HOLD)){
			ivhold.setImageResource(R.drawable.hold_active);
			ivhold.setTag("1");
			tvTimeRemaining.setText("-"+msToTimeString(db.getSamplesInMS(j_seek.getMax()))); 
		}else{
			ivhold.setImageResource(R.drawable.hold);
			ivhold.setTag("0");
			tvTimeElapsed.setText("0:00");
	        tvTimeRemaining.setText("0:00"); 
		}
                
        //Save state of HOLD, STAT flags
        if(isFirst){
        	edit = sp.edit();
	        edit.putBoolean("STAT", job.stat);
	        edit.putBoolean("HOLD", job.isFlagSet(Job.Flags.HOLD));
	        edit.commit();
        }
        
        j_seek.setMax((int) db.getRecordingLength());
        if(db.getDurationInSecs() != 0L){
        	j_seek.setProgress(j_seek.getMax());//Set seek thumb to end
        }
        
        Log.e("duration", Long.toString(db.getDurationInSecs())); 
        Log.e("duration", Long.toString(db.getRecordingLength()));
        Log.e("duration", Integer.toString((int)db.getRecordingLength()));
        Log.d("Entrada-JobDisplay", "job: " + job.toString());
        
        padap = new PatDetailsAdapter(this, patient);//new PatDetailsAdapter(getApplicationContext(), p_name, p_mrn, p_dob, p_gender);
        lvSliding.setAdapter(padap);
        
        ivCapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(Boolean.parseBoolean(account.getSetting(AccountSettingKeys.IMAGE_CAPTURE))){
					toCapture = true;
					BundleKeys.isCapture = true;
					BundleKeys.job_type = getActionBar().getTitle().toString();
					Intent cIntent = new Intent();
					cIntent.putExtra("isModified", isModified);
					cIntent.putExtra("isDeleted", isDeleted);
					cIntent.putExtra("isFirst", true);
					cIntent.putExtra("job_type", getActionBar().getTitle().toString());
					cIntent.putExtra("isFromList", isFromList);
					cIntent.putExtra("isNew", true);
					cIntent.putExtra("img_count", 0);
					cIntent.putExtra(BundleKeys.SELECTED_JOB, jobId);
					cIntent.putExtra(BundleKeys.SELECTED_JOB_ACCOUNT, accountName);
					cIntent.putExtra("sel_job_str", sel_job_str);
					cIntent.putExtra("interrupted", false);
					cIntent.setClass(JobDisplayActivity.this, CaptureImages.class);
					cIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(cIntent);
				}else{
					AlertDialog.Builder builder = new AlertDialog.Builder(JobDisplayActivity.this);
					builder.setTitle(null);
					builder.setMessage(R.string.dic_image_capture_not_enabled);
					builder.setPositiveButton("OK", null);
					builder.setCancelable(false);
					dgImageCapture = builder.create();
					dgImageCapture.show();
					
				}
			}
		});
        
        List<JobType> myJobTypes = new ArrayList<JobType>();
		BundleKeys.myJobTypes = new ArrayList<JobType>();
		for (int i = 0; i < provider.getJobTypes().size(); i++) {
			if(job.isFlagSet(Flags.LOCALLY_CREATED)){
				if (!Boolean.parseBoolean(provider.getJobTypes().get(i).disable)) {
					myJobTypes.add(provider.getJobTypes().get(i));
					BundleKeys.myJobTypes.add(provider.getJobTypes().get(i));
				}
			}else{
				myJobTypes.add(provider.getJobTypes().get(i));
				BundleKeys.myJobTypes.add(provider.getJobTypes().get(i));
			}
		}
		
		SharedPreferences spExpressNotes = getSharedPreferences("ExpressNotes", Context.MODE_WORLD_READABLE);
		String enotes = spExpressNotes.getString("ExpressNotes", null);
		
		BundleKeys.myTags = new ArrayList<ExpressNotesTags>();
		for (int i = 0; i < provider.getExpressNotesTags().size(); i++) {
			BundleKeys.myTags.add(provider.getExpressNotesTags().get(i));
		}
		
		
		jtAdapter = new JobTypeViewAdapter(getSupportFragmentManager());
		jtPager.setAdapter(jtAdapter);
		
		
        
    }
    
    String response;
    int hasClinicals = 0;
    boolean clinicInfoDisplayed = false;
    //0 - if clinic doesnt support clinicals
    //1 - if no clinic info found
    //2 - if clinic info is available
    
    private class PatientInfoAysncTask extends AsyncTask<Void, Void, String>{
    	
    	protected void onPostExecute(String result) {
    		
    		if(!result.contains("exceptionjobdisplay") && result.startsWith("{")){
    			clinicInfoDisplayed = true;
    			try {
					JSONObject js=new JSONObject(result);
					if(js.length() == 0){
						hasClinicals = 1;
					}else{
						hasClinicals = 2;
					mAdapter=new PatientInfoViewPagerAdapter(getSupportFragmentManager(), result, new String[]{p_name,p_mrn,p_dob,p_gender});
						mAdapter.setCount(js.length());
					mPager.setAdapter(mAdapter);
					mIndicator.setViewPager(mPager);					
					Log.e("key and pairs in result-->"+js.names().length(),""+js.length());
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			DisplayMetrics dp=new DisplayMetrics();
		    	getWindowManager().getDefaultDisplay().getMetrics(dp);
		    	int[] locations = new int[2];
		        btnRecord.getLocationOnScreen(locations);
		        int x = locations[0];
		        int y = locations[1];
		        int height = dp.heightPixels-y;
		        llPatientInfoLayer.getLayoutParams().height=height;
			
				if(llPatientInfoLayer.isShown()){
					llPatientInfoLayer.setVisibility(View.GONE);
				}else{
					
					if(hasClinicals == 0){
						Toast.makeText(getApplicationContext(), "Clinic does not support clinicals", Toast.LENGTH_SHORT).show();
					}else if(hasClinicals == 1){
						Toast.makeText(getApplicationContext(), "Clinical information not available", Toast.LENGTH_SHORT).show();
					}else{
						llPatientInfoLayer.setVisibility(View.VISIBLE);
						rlTransparentLayer.setVisibility(View.VISIBLE);
						if(BundleKeys.isPatientInfoShowing){
			        		mPager.setCurrentItem(BundleKeys.patientPageNumber);
						}
					}
				}
    			
    		}
    	};

		@Override
		protected String doInBackground(Void... params) {
			
			try {
				APIService	service = new APIService(account);
				response=service.getClinicalInfo(patient.id);
				Log.e("clinical-response", response);
				if(response.startsWith("{")){
				JSONObject js=new JSONObject(response);
				if(js.length() == 0){
					hasClinicals = 1;
				}else{
				js.length();
				Log.e("key and pairs in result-->"+js.names().length(),""+js.length());
				}
				}
					
				
			} catch (MalformedURLException e) {
				response="exceptionjobdisplay"+e.toString();
				e.printStackTrace();
			} catch (Exception e) {
				hasClinicals = 0;
				response="exceptionjobdisplay"+e.toString();
				e.printStackTrace();
			}

			return response;
		}
    	
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
    
    TextView menuTvRemaining;
    public boolean onCreateOptionsMenu(Menu menu) {
    	this.j_Menu = menu;
    	getMenuInflater().inflate(R.menu.job_send, menu);
    	/*menuTvRemaining=(TextView) j_Menu.findItem(R.id.menuTvRemaining).getActionView();
    	menuTvRemaining.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); 
    	menuTvRemaining.setTextColor(Color.WHITE);
    	menuTvRemaining.setGravity(Gravity.RIGHT);
    	DisplayMetrics dp=new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dp);
    	int cP=dp.widthPixels/2-10;
//    	cP=cP-menuTvRemaining.getWidth()/2;
    	menuTvRemaining.setPadding(0, 0, cP, 0);
    	menuTvRemaining.setVisibility(View.GONE);*/
    	//HOLD
        if(job.isFlagSet(Job.Flags.HOLD)){
			ivhold.setImageResource(R.drawable.hold_active);
			ivhold.setTag("1");
			 MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
		     menuItem.setTitle("SAVE");
		}else{
			ivhold.setImageResource(R.drawable.hold);
			ivhold.setTag("0");
			 MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
		     menuItem.setTitle("SEND");
		}
    	
        return true;
    }
    
    boolean isSend = false;
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId()) {
    	 case R.id.item_send_job:
    		 
    		File accountPath = new File(state.getUserData()
 					.getUserAccountsDir(), accountName);
 			File dbPath = new File(accountPath, String.valueOf(jobId));
 			dbPath.mkdirs();
 			final File images = new File(dbPath, "Images");
 			final File temp = new File(dbPath, "temp");
    		 
    		 if( (llPatientInfoLayer!=null&&llPatientInfoLayer.isShown()) || (rlJobTypeinfoLayer!= null && rljtTransparentLayer.isShown())){
    	    		llPatientInfoLayer.setVisibility(View.GONE);
    				rlTransparentLayer.setVisibility(View.GONE);
    				rlJobTypeinfoLayer.setVisibility(View.GONE);
    				rljtTransparentLayer.setVisibility(View.GONE);
    	    		
    	    }else{
    	    	if(!job.isFlagSet(Flags.LOCALLY_CREATED)){
        	        if(job.isFlagSet(Flags.HOLD)){
        	        	job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
        	        }else{
        	        	if(oldJobType != null)
        	        		job = job.setJobType(oldJobType).clearFlag(Flags.LOCALLY_MODIFIED);
        	        }
                }else{
                	//job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
                }
    	    	BundleKeys.IS_DIRTY = false;
				if (img_count == 0 && (job.isFlagSet(Flags.HOLD)
						|| (isErased && db.getRecordingLength() <= 0)
						|| (job.stat && db.getRecordingLength() <= 0)
						|| db.getRecordingLength() <= 0)) {
					//Save images
					Log.e("BundleKeys.isCapture", Boolean.toString(BundleKeys.isCapture));
					if(!BundleKeys.isCapture){
						
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.cleanDirectory(images);
										FileUtils.copyDirectory(temp,images);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
								
								@Override
								protected void onPostExecute(Void result) {
									// TODO Auto-generated method stub
									super.onPostExecute(result);
									try {
										FileUtils.deleteDirectory(temp);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
							}.execute();
						
					}
					saveJob();
					goBack();
				} else {
					if (img_count > 0 && !job.isFlagSet(Flags.HOLD)) {
						if(db.getRecordingLength() <= 0)
							dgEmptyJob();
						else
							saveSend();
						
					}else if(img_count > 0) {
						if(!job.isFlagSet(Flags.IS_FIRST)){
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.cleanDirectory(images);
										FileUtils.copyDirectory(temp,images);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
							}.execute();
						}else{
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.copyDirectory(images,temp);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
							}.execute();
						}
						if(db.getRecordingLength() <= 0 || job.isFlagSet(Flags.HOLD)){
							saveJob();
							goBack();
						}else{
							saveSend();
						}
					}else{
						saveSend();
					}
				}
    	    }
    	}
    	return discardChanges;
    	
    }
    
    /*
     * Routine to send dictation
     */
    
    public void saveSend(){
    	if (db.getRecordingLength() <= 0)
        {
            Toast.makeText(getApplicationContext(), "You must dictate something before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
    	        if(ivhold.getTag().toString().equals("0")){
                	sendJob();
                }
                
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
             
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        launchRingDialog();
    }
    
    /*
     * A temp dialog to buy sometime to change job status of completed job
     */
    ProgressDialog ringProgressDialog;
    public void launchRingDialog() {
		ringProgressDialog = ProgressDialog.show(this, "Uploading",	"Please wait...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {

				}
				ringProgressDialog.dismiss();
				goBack();
			}
		}).start();
	}
    
    /*
	 * (non-Javadoc)
	 * AlertDialog for Hold & Save for a job having images and no dictation
	 */
	public void dgEmptyJob(){
		File accountPath = new File(state.getUserData()
				.getUserAccountsDir(), accountName);
		File dbPath = new File(accountPath, String.valueOf(jobId));
		dbPath.mkdirs();
		final File images = new File(dbPath, "Images");
		final File temp = new File(dbPath, "temp");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(JobDisplayActivity.this);
		builder.setTitle(R.string.dic_empty_title);
		builder.setMessage(R.string.dic_empty_msg);
		builder.setPositiveButton("Hold & Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
						job = job.setFlag(Job.Flags.HOLD);
						if(!job.isFlagSet(Flags.IS_FIRST)){
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.cleanDirectory(images);
										FileUtils.copyDirectory(temp,images);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
							}.execute();
						}else{
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.copyDirectory(images,temp);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
							}.execute();
						}
						saveJob();
						goBack();
					}
				});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				isModified = true;
			}
		});
		builder.setCancelable(false);
		dgEmptyJob = builder.create();
		dgEmptyJob.show();
	}
    
    //Alert dialog for Job Type items
    AlertDialog dgJType;
    String[] str_jtype;
    JobType newJobType, oldJobType;
    
    public void jtypeDialog(){
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Select Job type");
    	builder.setCancelable(false);
    			
    	dgJType = builder.create();
    	dgJType.show();
		dgJType.setContentView(R.layout.job_type_spinner);
		dgJType.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		j_spnr = (Spinner)dgJType.findViewById(R.id.jobTypeSpinner);
		
		List<JobType> myJobTypes = new ArrayList<JobType>();
		BundleKeys.myJobTypes = new ArrayList<JobType>();
		for (int i = 0; i < provider.getJobTypes().size(); i++) {
			if(job.isFlagSet(Flags.LOCALLY_CREATED)){
				if (!Boolean.parseBoolean(provider.getJobTypes().get(i).disable)) {
					myJobTypes.add(provider.getJobTypes().get(i));
					BundleKeys.myJobTypes.add(provider.getJobTypes().get(i));
				}
			}else{
				myJobTypes.add(provider.getJobTypes().get(i));
				BundleKeys.myJobTypes.add(provider.getJobTypes().get(i));
			}
		}
		
		spinnerAdapter = new JobTypeSpinnerAdapter(this, myJobTypes);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		
        j_spnr.setAdapter(spinnerAdapter);
		setSpinnerToDefault(j_spnr);
        
        j_spnr.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				// TODO Auto-generated method stub
				newJobType = (JobType)arg0.getItemAtPosition(arg2);
				UserState state = AndroidState.getInstance().getUserState();
		        synchronized (state)
		        {
		            try
		            {
		                if (job.jobTypeId != newJobType.id)
		                {
		                    job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
		                    dgJType.dismiss();
		                    ivjtype.setImageResource(R.drawable.job_type);
		    				ivjtype.setTag("0");
		                    getActionBar().setTitle(j_spnr.getSelectedItem().toString());
		                }
		            } catch (Exception ex)
		            {
		            	Toast.makeText(JobDisplayActivity.this, "Exception: " + ex.toString(), 2500).show();
		            }
		        }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
        
        Button btnCancel = (Button)dgJType.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ivjtype.setImageResource(R.drawable.job_type);
				ivjtype.setTag("0");
				dgJType.dismiss();
			}
		});
		
        
    }

    private void beginPlayback() {
        assert (db.getState() == AudioManager.AudioState.IDLE);

        if (j_seek.getProgress() == j_seek.getMax()) {
            j_seek.setProgress(0);
        }
        btnPlay.setImageResource(R.drawable.pause);
        btnRecord.setEnabled(false);
        btnRecord.setAlpha(0.6f);

        disableControlsForPlay();

        final int startSample = j_seek.getProgress();
        boolean isSpeakerOn = ivspeaker.getTag().toString().trim().equals("1")?true:false;
        currentProcessor = db.beginPlayback(startSample, isSpeakerOn);
        currentProcessor.setMonitor(new JobDisplayAudioDBProgressMonitor());
    }

    
    private void beginRecord(boolean recordMode) {

    	//Set Listener on proximity sensor
    	if (myProximitySensor != null){
    		mySensorManager.registerListener(this, myProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    	
    	isInMiddle = false;
    	
    	if(j_seek.getProgress() == j_seek.getMax()){
    		isInMiddle = true;
    	}
    	Log.e("isinMiddle", Boolean.toString(isInMiddle));
    	
    	rlSeek.setVisibility(View.GONE);
    	rlVolumeMeter.setVisibility(View.VISIBLE);
    	assert (db.getState() == AudioManager.AudioState.IDLE);
        job = job.setFlag(Job.Flags.LOCALLY_MODIFIED);
        btnRecord.setImageResource(R.drawable.stop_recording);
        disableControls();
        currentProcessor = db.beginRecord(j_seek.getProgress(), !isInsert, rating);
        Log.e("j_sek_progress", Integer.toString(j_seek.getProgress()));
        Log.e("rec_mode", Boolean.toString(isInsert));
        currentProcessor.setMonitor(new JobDisplayAudioDBProgressMonitor());
    }

    private void stop() {
    	//Remove listener for Proximity sensor
    	mHandler.removeCallbacksAndMessages(null);
    	mySensorManager.unregisterListener(this); 
    	
    	//if(!limitReached){
	    	if(isPlay)
	    		enableControlsForPlay();
	    	else
	    		enableControls();
    	//}
    	
        db.stop();
        isPlay = false;
        currentProcessor = null;
        rlSeek.setVisibility(View.VISIBLE);
    	rlVolumeMeter.setVisibility(View.GONE);
        
    }
    
    private void disableControlsForPlay(){
    	btnInsert.setEnabled(false);
        btnOverwrite.setEnabled(false);
        btnDeleteToEnd.setEnabled(false);
        llInsert.setAlpha(0.6f);
        
        btnPlay.setEnabled(true);
    	btnPlay.setImageResource(R.drawable.pause);
    	btnPlay.setAlpha(1.0f);
    	
    	btnForward.setEnabled(false);
        btnForward.setAlpha(0.6f);
        btnRewind.setEnabled(false);
        btnRewind.setAlpha(0.6f);
        
        ivCapture.setEnabled(false);
        ivCapture.setAlpha(0.6f);
        rlImageCount.setEnabled(false);
        rlImageCount.setAlpha(0.6f);
        
        //Hide action bar item
        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
        menuItem.setVisible(false);
       
    }
    

    int progress_a, progress_b;
    long remTime;
    float lengthInSeconds1;
    
    private void disableControls() {
    	forcedInterruption = false;
    	//Check if there is any dictation
    	if(db.getDurationInSecs() != 0L){
        	hasDictation = true;
        }else{
        	hasDictation = false;
        }
    	Log.e("hasDictation", Boolean.toString(hasDictation));
    	
    	btnInsert.setEnabled(false);
        btnOverwrite.setEnabled(false);
        btnDeleteToEnd.setEnabled(false);
        llInsert.setAlpha(0.6f);
        
        btnPlay.setEnabled(false); 
        btnPlay.setAlpha(0.6f);
        
        if(hasDictation && !isModeInsert){
	        if(j_seek.getProgress() == (int) db.getRecordingLength())
	        	isInsert = true;
	        else if(j_seek.getProgress() != (int) db.getRecordingLength())
	        	isInsert = false;
        }
        
        	
        Log.e("isinsert", Boolean.toString(isInsert));
        
        //get the current progress before recording
        progress_a = j_seek.getProgress();
    	lengthInSeconds1 = db.getSamplesInMS(progress_a) / 1000.0f;
        Log.e("lengthInSeconds1", Float.toString(lengthInSeconds1));

        //Overwrite logic/seek track position
        if(hasDictation && isStop){//There is a dictation and the mode is overwrite
        	//dont update time untill it crosses the overwrite duration
        	timer = new StopWatch();
        	long time1 = timer.getElapsedTimeSecs();
        	long time2 = db.getDurationInSecs();
        	timeOverwrite = time2;
        	
        	Log.e("time1", Long.toString(time1));
            Log.e("time2", Long.toString(time2));
            
            mHandler.sendEmptyMessage(0);
                        
            int remTime = (int)((timeElapsed*10+timeRemaining*10)/10000);
            Log.e("rem_time", Integer.toString(remTime));
            
            if(remTime <= 9){
            	tvTimeRemaining1.setText("0:0"+remTime);
            }else if(remTime > 9 && remTime <= 59){
            	tvTimeRemaining1.setText("0:"+remTime);
            }else{
            	int mins = remTime/60;
            	int secs = remTime%60;
            	String seconds = "0";
            	if(secs <= 9){
            		seconds = "0"+Integer.toString(secs);
            	}else{
            		seconds = Integer.toString(secs);
            	}
            	Log.e("mins", Integer.toString(mins));
            	tvTimeRemaining1.setText(mins+":"+seconds);
            }
            
            if(time1 == 10){
            	dur_sec1.setVisibility(View.GONE);
            }else{
            	dur_sec1.setVisibility(View.VISIBLE);
            	
            }
            
            
           
	            if(time1<time2){
	            	llEndcall.setVisibility(View.GONE);
	                //tvTimeRemaining1.setVisibility(View.VISIBLE);
	                
	            }else{
	            	llEndcall.setVisibility(View.GONE);
	                tvTimeRemaining1.setVisibility(View.GONE);
	            }
           
            
        }else{
        	
        	mHandler.sendEmptyMessage(0);
        }
        
        //calculate total duration for remaining time
        Log.e("remaining time", Long.toString((timeElapsed*10+timeRemaining*10)/10000));
        
        btnForward.setEnabled(false);
        btnForward.setAlpha(0.6f);
        btnRewind.setEnabled(false);
        btnRewind.setAlpha(0.6f);
        
        ivCapture.setEnabled(false);
        ivCapture.setAlpha(0.6f);
        rlImageCount.setEnabled(false);
        rlImageCount.setAlpha(0.6f);
        
        //Hide action bar item
        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
        menuItem.setVisible(false);
       /* getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff0000")));
        j_Menu.findItem(R.id.menuTvRemaining).setVisible(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);*/
        getActionBar().hide();
        menuTvRemaining.setVisibility(View.VISIBLE);
        findViewById(R.id.rlTimeRemaining).setVisibility(View.VISIBLE);
        
        //showJobTypeFlyout(true);
        
    }

    private void enableControlsForPlay(){
    	db.stop();
        currentProcessor = null;
        rlSeek.setVisibility(View.VISIBLE);
    	rlVolumeMeter.setVisibility(View.GONE);
    	
    	btnPlay.setImageResource(R.drawable.play);
    	btnRecord.setImageResource(R.drawable.record_dictation);
    	btnRecord.setAlpha(1.0f);
        btnPlay.setEnabled(true);
        btnPlay.setAlpha(1.0f);
        btnRecord.setEnabled(true);

        btnInsert.setEnabled(true);
        btnOverwrite.setEnabled(true);
        btnDeleteToEnd.setEnabled(true);
        llInsert.setAlpha(1.0f);
        
        btnForward.setEnabled(true);
        btnForward.setAlpha(1.0f);
        btnRewind.setEnabled(true);
        btnRewind.setAlpha(1.0f);
        
        ivCapture.setEnabled(true);
        ivCapture.setAlpha(1.0f);
        rlImageCount.setEnabled(true);
        rlImageCount.setAlpha(1.0f);
        
      //Show action bar item
        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
        menuItem.setVisible(true);

    }

    
    long prev_time, time1, time2;
    private void enableControls() {
    	
    	btnPlay.setImageResource(R.drawable.play);
    	btnRecord.setImageResource(R.drawable.record_dictation);
    	btnRecord.setAlpha(1.0f);
        btnPlay.setEnabled(true);
        btnPlay.setAlpha(1.0f);
        btnRecord.setEnabled(true);
        btnInsert.setEnabled(true);
        btnOverwrite.setEnabled(true);
        btnDeleteToEnd.setEnabled(true);
        llInsert.setAlpha(1.0f);
        
        tvTimeElapsed.setVisibility(View.VISIBLE);
        
        btnForward.setEnabled(true);
        btnForward.setAlpha(1.0f);
        btnRewind.setEnabled(true);
        btnRewind.setAlpha(1.0f);
        
        ivCapture.setEnabled(true);
        ivCapture.setAlpha(1.0f);
        rlImageCount.setEnabled(true);
        rlImageCount.setAlpha(1.0f);
        
        Log.e("b4..time1 -", Long.toString(time1));        
        
      //Overwrite logic/seek track position
        if(hasDictation){	
        	time1 = time1 + timer.getElapsedTimeSecs();
        	time2 = db.getDurationInSecs();
        	Log.e("time1 -", Long.toString(time1));        
            Log.e("time2 -", Long.toString(time2));
            
             if(time1<time2){//seek the track thumb to position
            	if(!isModeInsert && !isInsert){
	            	if(lengthInSeconds1 <= 0){
	            		int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
	                	j_seek.setProgress((int)time1*divider);
	                	Log.e("where", "Loop_01");
	            	}else{
	            		int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
	            		if(lengthInSeconds1 <= 0){
	            			//int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
		            		j_seek.setProgress((int)lengthInSeconds1 + (int)time1*divider);
	            		}else{
	            			//int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
	            			if(!isDragged){
		            			j_seek.setProgress((int)lengthInSeconds1 + (int)time1*divider);
	            			}else{
	            				j_seek.setProgress(total_duration*divider);
	            			}
	            		}
	            		
	            		if(originalSeconds >= timeOverwrite){
	            			//j_seek.setProgress(total_duration*divider);
	            			j_seek.setProgress((int)db.getRecordingLength());
	            			Log.e("where", "Loop_02222");
	            		}
	            		
	            		Log.e("where", "Loop_02");
	            	}
            		
            		
            	}else if((!isModeInsert && isInsert) || isInMiddle){
            		j_seek.setProgress((int)db.getRecordingLength());
            	}else{
            		
            		if(lengthInSeconds1 <= 0){
            			int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
	            		j_seek.setProgress((int)lengthInSeconds1 + (int)time1*divider);
            		}else{
            			int divider = ((int)(j_seek.getMax()/db.getDurationInSecs()));
            			if(!isDragged){
	            			j_seek.setProgress((int)lengthInSeconds1 + (int)time1*divider);
            			}else{
            				j_seek.setProgress(total_duration*divider);
            			}
            		}

            		
                	Log.e("where", "Loop_04");

            	}
            	
            }else{//do nothing
            	j_seek.setProgress((int) db.getRecordingLength());
            	
            }
        }else{
        	if(isStop)
        		j_seek.setProgress((int) db.getRecordingLength());
        }
        timer = new StopWatch();
        
        
        //Show action bar item
        MenuItem menuItem = j_Menu.findItem(R.id.item_send_job);
        menuItem.setVisible(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dedfde")));
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        j_Menu.findItem(R.id.menuTvRemaining).setVisible(false);
        getActionBar().show();
        menuTvRemaining.setVisibility(View.GONE);
        findViewById(R.id.rlTimeRemaining).setVisibility(View.GONE);

    }
    
    private void dgLimit(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setCancelable(false);
    	builder.setTitle(R.string.app_name);
    	builder.setMessage(R.string.limit_msg);
    	builder.setPositiveButton("OK", null);
    	
    	dgLimit = builder.create();
    	dgLimit.show();
    }


    static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("m:ss");
    private String msToTimeString(long ms) {
    	Date d = new Date(ms);
    	return DATE_FORMATTER.format(d);
    }

    private void saveJob() {
        try {        	
        	Job _job = AndroidState.getInstance().getUserState().getProvider(account).getJob(job.id);
        	if(_job.isFlagSet(Flags.FAILED)){
        		job = job.setFlag(Flags.FAILED);
        	}
        	if(_job.isFlagSet(Flags.UPLOAD_COMPLETED)){
        		job = job.setFlag(Flags.UPLOAD_COMPLETED);
        	}
        	if(_job.isFlagSet(Flags.UPLOAD_PENDING)){
        		job = job.setFlag(Flags.UPLOAD_PENDING);
        	}
        	if(_job.isFlagSet(Flags.UPLOAD_IN_PROGRESS)){
        		job = job.setFlag(Flags.UPLOAD_IN_PROGRESS);
        	}
        	if(job.isFlagSet(Flags.UPLOAD_PENDING) && job.isFlagSet(Flags.UPLOAD_IN_PROGRESS)){
        		job = job.clearFlag(Flags.UPLOAD_PENDING);
        	}
        	if(job.isFlagSet(Flags.UPLOAD_PENDING) && job.isFlagSet(Flags.UPLOAD_COMPLETED)){
        		job = job.clearFlag(Flags.UPLOAD_PENDING);
        	}
            Log.d("", "Save Job--"+job.id+"--"+"job.localflags-"+job.localFlags+"--"+job.getFlagsString());	                  
            job = provider.updateJob(job);
            if (!job.isFlagSet(Job.Flags.LOCALLY_CREATED)) {
                Dictation dict = provider.getDictationsByJob(job.id).get(0);
                dict = dict.setDuration(db.getDurationInSecs());
                provider.writeDictation(dict);
            }

            db.stop();
            db.save();

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        } catch (Exception e) {
            Log.e("Entrada-JobDisplay", "Could not save dictation.", e);
        }
    }

    private void sendJob() {
        //TODO: Should the job itself be sent to the webservice here, rather than having sync on the job list screen?

        saveJob();
        
        NetworkState ns = new NetworkState(getApplicationContext());
		Boolean isConnected = ns.isConnectingToInternet();
		try
        {
            job = job.setFlag(Job.Flags.UPLOAD_PENDING);
        	DomainObjectProvider provider = AndroidState.getInstance().getUserState().getProvider(account);
            provider.writeJob(job);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Error saving job flag. Please contact support.", Toast.LENGTH_SHORT).show();
            ACRA.getErrorReporter().handleSilentException(ex);
        }
        
    	if(isConnected){
        
	        DictationUploadService.startUpload(JobDisplayActivity.this, state, account, job);
	
	        this.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {

        }
	        });
        }
        
    	//Remove held status of job if being put on hold
        	BundleKeys.List_Held_Encounters = new ArrayList<Encounter>();
	        BundleKeys.List_Held_Jobs = reader.getHeldJobs(4);
	        
	        if(BundleKeys.List_Held_Jobs != null)
	            Log.e("list_held_jobs", BundleKeys.List_Held_Jobs.toString());
	    	
	    		if(job.isFlagSet(Flags.HOLD)){
	    			if(BundleKeys.Held_Encounter_IDs.contains(job.encounterId)){
	    				BundleKeys.Held_Encounter_IDs.remove(job.encounterId);
	    				if(BundleKeys.Held_Encounter_IDs != null && BundleKeys.Held_Encounter_IDs.size() > 0){
	    		        	for(int i=0;i<BundleKeys.Held_Encounter_IDs.size();i++){
	    		        		Encounter enc = reader.getEncounter(BundleKeys.List_Held_Jobs.get(i).encounterId);
	    		        		if(enc != null)
	    							BundleKeys.List_Held_Encounters.remove(enc);
	    		        	}
	    		    	}
	    			}
    		
		}
    }

    private void goBack()
    {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();

        try
        {
            UserPrivate user = AndroidState.getInstance().getUserState().getUserData();
            user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ID, null);
            user.setStateValue(JobDisplayActivity.JOB_IN_PROGRESS_ACCOUNT, null);
            user.save();
        }
        catch (Exception ex)
        {
            ACRA.getErrorReporter().handleSilentException(ex);
            Toast.makeText(JobDisplayActivity.this, "Failed to save user state.", Toast.LENGTH_LONG).show();
	    }
        
        /*if(!job.isFlagSet(Flags.LOCALLY_CREATED)){
	        if(job.isFlagSet(Flags.HOLD)){
	        	job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
	        }else{
	        	if(oldJobType != null)
	        		job = job.setJobType(oldJobType).clearFlag(Flags.LOCALLY_MODIFIED);
	        }
        }else{
        	//job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
        }*/
        
        finish();
        Intent intent = new Intent(this, JobListActivity.class);
        intent.putExtra("isUploading", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    AlertDialog dialog;
    @Override
    public void onBackPressed()
    {
    	File accountPath = new File(state.getUserData()
				.getUserAccountsDir(), accountName);
		File dbPath = new File(accountPath, String.valueOf(jobId));
		dbPath.mkdirs();
		final File images = new File(dbPath, "Images");
		final File temp = new File(dbPath, "temp");
    	forcedInterruption = true;
      	    	
    	//Toast.makeText(getApplicationContext(), Boolean.toString(job.isFlagSet(Flags.LOCALLY_MODIFIED)), 1000).show();
    	
    	if(llPatientInfoLayer.isShown()){
    		llPatientInfoLayer.setVisibility(View.GONE);
    		rlTransparentLayer.setVisibility(View.GONE);
    	}else if(rlJobTypeinfoLayer.isShown()){
    		rlJobTypeinfoLayer.setVisibility(View.GONE);
    		rljtTransparentLayer.setVisibility(View.GONE);
    	}else{
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.job_display_dialog_message)
                .setTitle(R.string.job_display_dialog_title);

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.job_display_save,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
            	if(!job.isFlagSet(Flags.LOCALLY_CREATED)){
        	        if(job.isFlagSet(Flags.HOLD)){
        	        	job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
        	        }else{
        	        	if(oldJobType != null)
        	        		job = job.setJobType(oldJobType).clearFlag(Flags.LOCALLY_MODIFIED);
        	        }
                }
            	
            	BundleKeys.IS_DIRTY = false;
				if (img_count == 0 && (job.isFlagSet(Flags.HOLD)
						|| (isErased && db.getRecordingLength() <= 0)
						|| (job.stat && db.getRecordingLength() <= 0)
						|| db.getRecordingLength() <= 0)) {
					//Save images
					Log.e("BundleKeys.isCapture", Boolean.toString(BundleKeys.isCapture));
					if(!BundleKeys.isCapture){
						
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.cleanDirectory(images);
										FileUtils.copyDirectory(temp,images);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
								
								@Override
								protected void onPostExecute(Void result) {
									// TODO Auto-generated method stub
									super.onPostExecute(result);
									try {
										FileUtils.deleteDirectory(temp);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
							}.execute();
						
					}
					saveJob();
					goBack();
				} else {
					if (img_count > 0 && !job.isFlagSet(Flags.HOLD)) {
						if(db.getRecordingLength() <= 0)
							dgEmptyJob();
						else
							saveSend();
						
					}else if(img_count > 0) {
						if(!job.isFlagSet(Flags.IS_FIRST)){
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.cleanDirectory(images);
										FileUtils.copyDirectory(temp,images);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
								
								@Override
								protected void onPostExecute(Void result) {
									// TODO Auto-generated method stub
									super.onPostExecute(result);
									
								}
								
							}.execute();
						}else{
							new AsyncTask<Void, Void, Void>(){

								@Override
								protected Void doInBackground(
										Void... params) {
									// TODO Auto-generated method stub
									try {
										FileUtils.copyDirectory(images,temp);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return null;
								}
							}.execute();
						}
						if(db.getRecordingLength() <= 0 || job.isFlagSet(Flags.HOLD)){
							saveJob();
							goBack();
						}else{
							saveSend();
						}
					}else{
						saveSend();
					}
				}
			
            }
        });

        builder.setNeutralButton(R.string.job_display_dialog_discard, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
            	//Toast.makeText(getApplicationContext(), Boolean.toString(job.isFlagSet(Flags.LOCALLY_MODIFIED)), 1000).show();
            	
            	if(oldJobType != null)
	        		job = job.setJobType(oldJobType).clearFlag(Flags.LOCALLY_MODIFIED);
            	
            	if(job.isFlagSet(Flags.IS_FIRST)){//Dictation is opened for the first time
					if(img_count > 0){
						try {
								FileUtils.deleteDirectory(images);
								FileUtils.deleteDirectory(temp);
							} catch(IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
						
						
				}else{
					new AsyncTask<Void, Void, Void>(){

						@Override
						protected Void doInBackground(
								Void... params) {
							// TODO Auto-generated method stub
							try {
								FileUtils.cleanDirectory(temp);
								FileUtils.copyDirectory(images,temp);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							return null;
						}
						
						@Override
						protected void onPostExecute(Void result) {
							// TODO Auto-generated method stub
							super.onPostExecute(result);
						}
						
					}.execute();
					
				}
            	
            	if( (db.getRecordingLength() > 0 && !job.isFlagSet(Flags.HOLD)) || (img_count > 0 && !job.isFlagSet(Flags.HOLD)) ){
					BundleKeys.Held_Encounter_IDs = new ArrayList<Long>();
            		if(!BundleKeys.Held_Encounter_IDs.contains(job.encounterId)){
						BundleKeys.Held_Encounter_IDs.add(job.encounterId);
					}
            		job = job.setFlag(Flags.HOLD);
            		try {
						provider.updateJob(job);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
				            	
            	if(!isFromList || job.isFlagSet(Flags.IS_FIRST)){//Job has dictation and reopened, clear the dictation if discard selected
            		db.truncateRecording(0);
            		if(job.isFlagSet(Flags.HOLD))
            			job = job.clearFlag(Flags.HOLD);
            		job = job.setStat(false).setFlag(Job.Flags.LOCALLY_MODIFIED);
            		BundleKeys.IS_DIRTY = false;
            		goBack();
            	}else{
	            	//isDiscard = true;
	            	BundleKeys.IS_DIRTY = false;
	                discardChanges = true;
	                goBack();
            	}

            	//Restore STAT, HOLD status
            	if(sp.getBoolean("HOLD", false)){
            		BundleKeys.Held_Encounter_IDs = new ArrayList<Long>();
            		if(!BundleKeys.Held_Encounter_IDs.contains(job.encounterId)){
						BundleKeys.Held_Encounter_IDs.add(job.encounterId);
					}
            		job = job.setFlag(Flags.HOLD);
            		try {
						provider.updateJob(job);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	
            	if(sp.getBoolean("STAT", false)){
            		job = job.setStat(true).setFlag(Job.Flags.LOCALLY_MODIFIED);
            		try {
						provider.updateJob(job);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}

            }
        });

        builder.setNegativeButton(R.string.job_display_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing.
            }
        });
        
        stop();

        Log.e("disc_changes", Boolean.toString(discardChanges));
        Log.e("IS_Dirty", Boolean.toString(BundleKeys.IS_DIRTY));
        Log.e("job.isDirty()", Boolean.toString(job.isDirty()));
        
        if (job.isDirty() || BundleKeys.IS_DIRTY || isModified || (job.isFlagSet(Flags.IS_FIRST) && db.getRecordingLength() > 0)){  //|| !discardChanges) {
            // There is a dictation. Ask what the user wants to do with it.
            dialog = builder.create();
            dialog.show();
        }else if(isErased){
            // Save in case the user deleted the whole recording.
        	saveJob();
            this.goBack();
        }else{
        	this.goBack();
        }
    	}
        
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    
    @Override
    protected void onPause()
    { 
    	super.onPause();
    	JSONObject json=new JSONObject();
    	try {
			json.putOpt("hmMedicationsAllergiesStroedValues", PatientInfoPageNewListAdapter.hmMedicationsAllergiesStroedValues);
			json.putOpt("hmMedicationsStroedValues", PatientInfoPageNewListAdapter.hmMedicationsStroedValues);
			json.putOpt("hmProblemsStroedValues", PatientInfoPageNewListAdapter.hmProblemsStroedValues);
			json.putOpt("hmPastMedicalStroedValues", PatientInfoPageNewListAdapter.hmPastMedicalStroedValues);
			json.putOpt("hmLastHpiStroedValues", PatientInfoPageNewListAdapter.hmLastHpiStroedValues);
			SharedPreferences spPatientInfo=getSharedPreferences("PatientInfoPage", Context.MODE_WORLD_READABLE);
			spPatientInfo.edit().putString("PatientInfoSave", json.toString()).commit();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    
    	//Save ExpressTags
    	SharedPreferences spExpressNotes = getSharedPreferences("ExpressNotes", Context.MODE_WORLD_READABLE);
    	edit = spExpressNotes.edit();
    	edit.putString("expr", BundleKeys.myTags.toString());
    	edit.commit();
    
    	if (this.discardChanges == false) {
			if(toCapture){// && db.getRecordingLength() > 0)
//				isModified = true;
				if(db.getRecordingLength() > 0)
					Toast.makeText(getApplicationContext(), "Dictation saved", 500).show();
			}else{
				if(job.isFlagSet(Flags.IS_FIRST) && img_count > 0){
					//job = job.clearFlag(Flags.IS_FIRST);
					try {
			        	Job _job = AndroidState.getInstance().getUserState().getProvider(account).getJob(job.id);
			        	if(_job.isFlagSet(Flags.FAILED)){
			        		job = job.setFlag(Flags.FAILED);
			        	}
			        	if(_job.isFlagSet(Flags.UPLOAD_COMPLETED)){
			        		job = job.setFlag(Flags.UPLOAD_COMPLETED);
			        	}
			        	if(_job.isFlagSet(Flags.UPLOAD_PENDING)){
			        		job = job.setFlag(Flags.UPLOAD_PENDING);
			        	}
			        	if(_job.isFlagSet(Flags.UPLOAD_IN_PROGRESS)){
			        		job = job.setFlag(Flags.UPLOAD_IN_PROGRESS);
			        	}
			        	if(job.isFlagSet(Flags.UPLOAD_PENDING) && job.isFlagSet(Flags.UPLOAD_IN_PROGRESS)){
			        		job = job.clearFlag(Flags.UPLOAD_PENDING);
			        	}
			        	if(job.isFlagSet(Flags.UPLOAD_PENDING) && job.isFlagSet(Flags.UPLOAD_COMPLETED)){
			        		job = job.clearFlag(Flags.UPLOAD_PENDING);
			        	}
						Log.d("", "Save Jobhere--"+job.id+"--"+"job.localflags-"+job.localFlags+"--"+job.getFlagsString());
						provider.updateJob(job);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					
			}
			if (!isSend) {
				edit = sp.edit();
				if (db.getRecordingLength() > 0) {
					if (forcedInterruption){
						//BundleKeys.IS_INTERRUPTED = false;
						edit.putBoolean("IS_INTERRUPTED", false);
						
					}else{
						//BundleKeys.IS_INTERRUPTED = true;
						edit.putBoolean("IS_INTERRUPTED", true);
					}
					//edit.commit();
					//BundleKeys.IS_DIRTY = true;
				} else {
					//BundleKeys.IS_INTERRUPTED = false;
					edit.putBoolean("IS_INTERRUPTED", false);
					if(toCapture && job.isDirty())
						BundleKeys.IS_DIRTY = true;
					else
						BundleKeys.IS_DIRTY = false;
				}
				edit.commit();
				saveJob();
			}

			state.getUserData().setStateValue(JOB_IN_PROGRESS_ID,
					Long.toString(job.id));
			state.getUserData().setStateValue(JOB_IN_PROGRESS_ACCOUNT,
					accountName);

			try {
				state.getUserData().save();
			} catch (IOException e) {
				ACRA.getErrorReporter().handleSilentException(e);
			}

		}
		db.stop();
		if (wakeLock != null) {
			if (wakeLock.isHeld())
				wakeLock.release();
			wakeLock = null;
		}
		mySensorManager.unregisterListener(this); 
        BundleKeys.patientPageNumber=mPager.getCurrentItem();
		BundleKeys.isPatientInfoShowing=llPatientInfoLayer.isShown();
		BundleKeys.jtPageNumber = jtPager.getCurrentItem();
		BundleKeys.isjtInfoShowing = rlJobTypeinfoLayer.isShown();
		Log.e("BundleKeys.toCall", Boolean.toString(BundleKeys.toCall));
			
		//if(!BundleKeys.toCall)
        //finish();
		
    }
    
    @Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		if(!BundleKeys.toCall)
		startActivity(new Intent(this, UserSelectActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

    @Override
    protected void onStop()
    {
        super.onStop();
        
        if (ringProgressDialog != null && ringProgressDialog.isShowing())
			ringProgressDialog.dismiss();
		if (dgLimit != null && dgLimit.isShowing())
			dgLimit.dismiss();
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
		if (dgInterrupt != null && dgInterrupt.isShowing())
			dgInterrupt.dismiss();
		if (dgEmptyJob != null && dgEmptyJob.isShowing())
			dgEmptyJob.dismiss();
		if (dgImageCapture != null && dgImageCapture.isShowing())
			dgImageCapture.dismiss();
        
    }

    private void setSpinnerToDefault(Spinner spinner)
    {
        spinnerAdapter = (JobTypeSpinnerAdapter)spinner.getAdapter();
        str_jtype = new String[spinnerAdapter.getCount()];
        for (int i = 0; i < spinnerAdapter.getCount(); i++)
        {
        	str_jtype[i] = spinnerAdapter.getItem(i).toString();
            JobType jt = spinnerAdapter.getItem(i);
                       
            if(!isNew){
            	//If from JobList
	            if (jt.id == job.jobTypeId)
	            {
	            	spinner.setSelection(i);
	                getActionBar().setTitle(spinner.getSelectedItem().toString());
	                str_org_jtype = spinner.getSelectedItem().toString();
	                oldJobType = spinnerAdapter.getItem(i);
	                newJobType = spinnerAdapter.getItem(i);
	                break;
	            }
	            
	            if( (i == spinnerAdapter.getCount() - 1) && str_org_jtype == null){
	            	 job = job.setJobType(spinnerAdapter.getItem(0));
	            	 job = job.setJobTypeId(job.jobTypeId);
	            	try {
						job = provider.updateJob(job);
					} catch (DomainObjectWriteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	spinner.setSelection(0);
                    getActionBar().setTitle(spinner.getSelectedItem().toString());
                    str_org_jtype = spinner.getSelectedItem().toString();
                    oldJobType = spinnerAdapter.getItem(i);
                    newJobType = spinnerAdapter.getItem(i);
                    break;
	            }
	            
            }else{
            	//If from Add Job
                //TRY CHECKING WITH JOBTYPE-> NAME INSTEAD
            	if(jt.name.equals(sel_job_str)){
                	spinner.setSelection(i);
                    getActionBar().setTitle(spinner.getSelectedItem().toString());
                    str_org_jtype = spinner.getSelectedItem().toString();
                    oldJobType = spinnerAdapter.getItem(i);
                    newJobType = spinnerAdapter.getItem(i);
                    break;
                }
            }

        }
    }
    

    class JobDisplayAudioDBProgressMonitor implements AudioManager.AudioProgressMonitor
    {
        long cursor = 0;

        @Override
        public void setSampleCursor(long samples) {
            final long recordingLength = db.getRecordingLength();
            cursor = Math.max(cursor, samples);
            j_seek.setMax((int)recordingLength);
            j_seek.setProgress((int)cursor);
            
        }

        @Override
        public void stopped() {
            j_seek.setProgress((int)cursor);
            j_seek.setMax((int) db.getRecordingLength());
            JobDisplayActivity.this.stop();
            
        }
    }


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		tvTimeElapsed.setText(msToTimeString(db.getSamplesInMS(progress)));
		timeElapsed = db.getSamplesInMS(progress);
//		invalidateOptionsMenu();
		/*if(isRewind){
			tvTimeRemaining.setText("-"+msToTimeString(db.getSamplesInMS(seekBar.getMax() - progress)));
		}
		else{
			tvTimeRemaining.setText("-"+msToTimeString(db.getSamplesInMS(seekBar.getMax() - progress)));
		}*/
		
		tvTimeRemaining.setText("-"+msToTimeString(db.getSamplesInMS(seekBar.getMax() - progress)));
		
		timeRemaining = db.getSamplesInMS(seekBar.getMax() - progress);
	}	

	boolean isInMiddle = false, isDragged = false;
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		// Remember if we were playing when the user started scrubbing
        wasPlaying = (db.getState() == AudioManager.AudioState.PLAYING);
        if (wasPlaying)
            stop();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		// If we were playing, resume at the new location.
		isDragged = true;
		if (wasPlaying)
            beginPlayback();
		else
			time1 = 0;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==btnInsert){
			isInsert = true;
			isModeInsert = true;
			btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_blue);
			btnInsert.setTextColor(Color.parseColor("#ffffff"));
			btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_white);
			btnOverwrite.setTextColor(Color.parseColor("#007bff"));
			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
		}else if(v==btnOverwrite){
			isInsert = false;
			isModeInsert = false;
			btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_blue);
			btnOverwrite.setTextColor(Color.parseColor("#ffffff"));
			btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_white);
			btnInsert.setTextColor(Color.parseColor("#007bff"));
			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
		}else if(v==btnDeleteToEnd){
			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_blue);
			btnDeleteToEnd.setTextColor(Color.parseColor("#ffffff"));
			btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_white);
			btnInsert.setTextColor(Color.parseColor("#007bff"));
			btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_white);
			btnOverwrite.setTextColor(Color.parseColor("#007bff"));
			
			//Truncate Functionality
			final int progress = j_seek.getProgress();
	        final long recordingLength = db.getRecordingLength();
	        final float lengthInSeconds = db.getSamplesInMS(recordingLength - progress) / 1000.0f;

	        new AlertDialog.Builder(JobDisplayActivity.this).setMessage(String.format("Are you sure you want to delete %s seconds of audio?", SECONDS_FORMAT.format(lengthInSeconds))).setCancelable(false).setPositiveButton("Erase", new DialogInterface.OnClickListener()
	        {
	            @Override
	            public void onClick(DialogInterface dialogInterface, int i)
	            {
	            	job = job.setFlag(Job.Flags.LOCALLY_MODIFIED);
	            	isErased = true;
	                db.truncateRecording(j_seek.getProgress());
	                j_seek.setMax(j_seek.getProgress());
	                
	                timeElapsed = 0;
	                timeRemaining = 0;
	                
	                if(isInsert){
	            		btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_blue);
	        			btnInsert.setTextColor(Color.parseColor("#ffffff"));
	        			btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_white);
	        			btnOverwrite.setTextColor(Color.parseColor("#007bff"));
	        			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
	        			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
	            	}else{
	            		btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_blue);
	        			btnOverwrite.setTextColor(Color.parseColor("#ffffff"));
	        			btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_white);
	        			btnInsert.setTextColor(Color.parseColor("#007bff"));
	        			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
	        			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
	            	}
	            }
	        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	        {
	            @Override
	            public void onClick(DialogInterface dialogInterface, int i)
	            {
	            	if(isInsert){
	            		btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_blue);
	        			btnInsert.setTextColor(Color.parseColor("#ffffff"));
	        			btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_white);
	        			btnOverwrite.setTextColor(Color.parseColor("#007bff"));
	        			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
	        			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
	            	}else{
	            		btnOverwrite.setBackgroundResource(R.drawable.cfo_split_ctrl_middle_blue);
	        			btnOverwrite.setTextColor(Color.parseColor("#ffffff"));
	        			btnInsert.setBackgroundResource(R.drawable.cfo_split_ctrl_left_white);
	        			btnInsert.setTextColor(Color.parseColor("#007bff"));
	        			btnDeleteToEnd.setBackgroundResource(R.drawable.cfo_split_ctrl_right_white);
	        			btnDeleteToEnd.setTextColor(Color.parseColor("#007bff"));
	            	}
	            }
	        }).show();
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	WindowManager.LayoutParams params;
	String originalBrightness;
	int brightness;
	
	/*
	 * I have used FLAG_IGNORE_CHEEK_PRESSES to implement proximity lock functionality (Its what the main feature of locking in this app)
	 * Like iPhone, Android wont allow third party apps to get access to Power Management API's and are available to OEM's only.
	 * Also setting brightness to 0.0f blacks out the screen, but its also locking the screen, which seems to be the default behavior on Android
	 */
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(BundleKeys.PROXIMITY_LOCK){
			if (event.values[0] == 0) {//Near
				params = getWindow().getAttributes(); 
				params.flags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
				params.screenBrightness = 0.01f; 
				getWindow().setAttributes(params);
				getActionBar().hide();
				rlMain.setVisibility(View.INVISIBLE);
				getWindow().getDecorView().findViewById(android.R.id.content).setBackgroundColor(Color.BLACK);
								
			}else { //Far 
				int curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS,-1);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
				params = getWindow().getAttributes(); 
				params.screenBrightness = curBrightnessValue/100f; 
				getWindow().setAttributes(params);
				if(!findViewById(R.id.rlTimeRemaining).isShown())
				getActionBar().show();
				rlMain.setVisibility(View.VISIBLE);
				getWindow().getDecorView().findViewById(android.R.id.content).setBackgroundColor(Color.parseColor("#f7f7f7"));
				
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		job = job.setJobType(BundleKeys.myJobTypes.get(arg0)).setFlag(Job.Flags.LOCALLY_MODIFIED);
		getActionBar().setTitle(BundleKeys.myJobTypes.get(arg0).name);
		newJobType = (JobType)(BundleKeys.myJobTypes.get(arg0));
		UserState state = AndroidState.getInstance().getUserState();
        synchronized (state)
        {
            try
            {
                if (job.jobTypeId != newJobType.id)
                {
                    job = job.setJobType(newJobType).setFlag(Job.Flags.LOCALLY_MODIFIED);
                    dgJType.dismiss();
                    ivjtype.setImageResource(R.drawable.job_type);
    				ivjtype.setTag("0");
                    getActionBar().setTitle(j_spnr.getSelectedItem().toString());
                }
            } catch (Exception ex)
            {
            	Toast.makeText(JobDisplayActivity.this, "Exception: " + ex.toString(), 2500).show();
            }
        }
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		finish();
	}
}
