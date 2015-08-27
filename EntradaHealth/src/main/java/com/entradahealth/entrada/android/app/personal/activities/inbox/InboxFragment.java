package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_account.EUser;
import com.entradahealth.entrada.android.app.personal.activities.inbox.adapters.ConversationsListAdapter;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.job_list.JobListActivity;
import com.entradahealth.entrada.android.app.personal.activities.settings.EntradaSettings;
import com.entradahealth.entrada.android.app.personal.utils.NetworkState;
import com.entradahealth.entrada.android.app.widget.SwipeListView;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.TOU;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.MainUserDatabaseProvider;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBBuddyHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.service.NewConversationBroadcastService;
import com.entradahealth.entrada.core.inbox.service.SaveMessagesContentService;
import com.entradahealth.entrada.core.inbox.service.SaveSMContentService;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;

public class InboxFragment extends Fragment {
	private final static String TAG = "SecureMsg";
	String name;
	private SwipeListView conversationsListView;
	private ConversationsListAdapter adapter;	
	private Context context;
	private List<ENTConversation> conversations;
	private SMDomainObjectReader reader;
    private EntradaApplication application;
    String curTOUVersionNumber = "0", TOUVersionNumber = "-1", UserId = null, SessionToken = null;
    boolean touAccepted = false, hasVerno = false;
    public static List<TOU> list_tou;
    ProgressDialog dialog;
    String m_androidId, session_token, patient, recipient;
    boolean isAlertShowing = false;
    private Menu menu;
    MenuItem composeMenuItem;
	private TextView conversationsLoadingTV;
	private APIService service;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		BundleKeys.fromSecureMessaging = false;
		getActivity().getActionBar().setTitle("Messages");
		getActivity().registerReceiver(broadcastReceiver, new IntentFilter("CONNECTIVITY_CHANGED"));
		application = (EntradaApplication) EntradaApplication.getAppContext();
		m_androidId = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID);
		UserState state = AndroidState.getInstance().getUserState();
		try {
			state.setSMUser();
			EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
			Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
		 	service = new APIService(env.getApi());
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(false);
		new ShowTOUTask().execute();
	}
	
	class ShowTOUTask extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {
			/*
			 * Check if TOUVersion number has been saved. If Yes, call WS to check for new version else,
			 * if new version exists, update the local number and the flag accepted
			 * if TOUVersionNumber is not saved at all, create table and call WS and save version number
			 * 
			 */
			Log.d(TAG, "checking for TOU version in db");
			//fetch TOU from DB
	    	list_tou =  reader.getTOUVersion();
	    	if(list_tou != null && list_tou.size() > 0){
				for(int i=0;i<list_tou.size();i++){
					curTOUVersionNumber = list_tou.get(i).getTOUVersion();
					touAccepted = list_tou.get(i).getTOUAccepted();
				}
			}

			BundleKeys.isTOUAccepted = touAccepted;
			
			if(curTOUVersionNumber != null)
				Log.e(TAG, "Current-TOUVersion: "+curTOUVersionNumber);
			
			
			if(list_tou == null || list_tou.size() == 0 || curTOUVersionNumber == null){ //No TOU table in DB- first login
				hasVerno = false;
				Log.d(TAG, "TOU version not found in db");
			}else{ 
				hasVerno = true;
				Log.d(TAG, "TOU version found in db");
			}
			
			TOUVersionNumber = application.getStringFromSharedPrefs(BundleKeys.TOUVERSION);
			UserId = application.getStringFromSharedPrefs(BundleKeys.USERID);

			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//if WS returned zero(0), verno is saved and tou is accepted, then show sec msg
			if(TOUVersionNumber.trim().equals("0") && hasVerno && touAccepted){
				Log.d(TAG, "TOU Service error");
				// Commenting this.. Why do we need to launch it again when we are in the same inbox screen..
				//InboxFragment inboxFragment = new InboxFragment();
				//getFragmentManager().beginTransaction().replace(R.id.fragcontent, inboxFragment, null).addToBackStack(null).commit();
			}else 
				//if WS returned zero(0) due to connection problem, verno is not saved and tou is not accepted, then finish this activity
				if(TOUVersionNumber.trim().equals("-1") && !hasVerno && !touAccepted){
					//Log.d(TAG, "TOU Service error - No Ver no found - reverting back to previous screen");
					//		"\nPlease chech your intenet connection and retry", Toast.LENGTH_LONG).show();
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			        dialog.setIcon(R.drawable.icon_dark);
			        dialog.setCancelable(false);
			        dialog.setTitle("Terms of Use");
			        dialog.setMessage(getResources().getString(R.string.sec_msg_conn_error));
			        dialog.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							getActivity().finish();
						}
					});
			        dialog.show();
					
			}else{
				if(hasVerno){//TOU table exists and data is there
					//check if TOU is accepted and show pop up accordingly
					if(touAccepted){
						Log.d(TAG, "TOU accepted");
						BundleKeys.isTOUAccepted = true;
					//If curVerNo < TOUVerNo, update TOUVerNo from WS and set Accepted falg to 'false'
						if( Integer.parseInt(curTOUVersionNumber) < Integer.parseInt(TOUVersionNumber) ){
							Log.d(TAG, "New TOU version found..Updating db");
							reader.updateTOU(UserId, TOUVersionNumber, false);
							if(BundleKeys.TOUContent != null){
								if(getActivity() != null&&!getActivity().isFinishing())
								dgTOU(BundleKeys.TOUContent);
							}else{
								getTOUContent(SessionToken);
							}
							
						}else{
							conversationsListView.setFocusable(true);
					        conversationsListView.setFocusableInTouchMode(true);
					        conversationsListView.setEnabled(true);
						}
					}else{
						//Show pop up
						BundleKeys.isTOUAccepted = false;
						Log.d(TAG, "TOU not accepted - Show TOU pop up");
						
						if(BundleKeys.TOUContent != null){
							if(getActivity() != null&&!getActivity().isFinishing())
							dgTOU(BundleKeys.TOUContent);
						}else{
							getTOUContent(SessionToken);
						}
					}
				}else{//No TOU table - create,save and show TOU pop-up
					BundleKeys.isTOUAccepted = false;
					reader.saveTOU(UserId, TOUVersionNumber);
					Log.d(TAG, "First time: TOU not accepted - Show TOU pop up");
					
					if(BundleKeys.TOUContent != null){
						if(getActivity() != null&&!getActivity().isFinishing())
						dgTOU(BundleKeys.TOUContent);
					}else{
						getTOUContent(SessionToken);
					}
				}
			}
		}
		
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		conversationsListView.setFocusable(true);
        conversationsListView.setFocusableInTouchMode(true);
        conversationsListView.setEnabled(true);
		if(SaveMessagesContentService.isRunning()){
			getActivity().registerReceiver(messagesLoadedReceiver, new IntentFilter(SaveMessagesContentService.BROADCAST_ACTION));
			conversationsLoadingTV.setVisibility(View.VISIBLE);
		}
		getActivity().getActionBar().setTitle("Messages");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);
        GetConversationsTask task = new GetConversationsTask();
        task.execute();
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		dialog = null;
		getActivity().unregisterReceiver(broadcastReceiver);
	}
	
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
			try {
				NetworkState ns = new NetworkState(
						EntradaApplication.getAppContext());
				boolean isConnected = ns.isConnectingToInternet();
				if (!isConnected) {
					composeMenuItem = menu.findItem(R.id.item_new_msg);
					composeMenuItem.setEnabled(false);
					composeMenuItem.getIcon().setAlpha(80);
				} else {
					composeMenuItem = menu.findItem(R.id.item_new_msg);
					composeMenuItem.setEnabled(true);
					composeMenuItem.getIcon().setAlpha(255);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	    }
        
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.conversations_list, container, false);
		context = getActivity();
		conversationsLoadingTV = (TextView) view.findViewById(R.id.loadingText);
		conversationsListView = (SwipeListView) view.findViewById(R.id.conversationsList);
		conversationsListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
		conversationsListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
		conversationsListView.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		conversationsListView.setOffsetLeft(convertDpToPixel(dpWidth-85));
		conversationsListView.setOffsetRight(convertDpToPixel(0));
		conversationsListView.setAnimationTime(0);
        conversationsListView.setSwipeOpenOnLongPress(false);
        conversationsListView.setFocusable(false);
        conversationsListView.setFocusableInTouchMode(false);
        conversationsListView.setEnabled(false);
		return view;
	}
	
	public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }
	
	private BroadcastReceiver conbroadcastReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	ArrayList<ENTConversation> conversationslist = (ArrayList<ENTConversation>) intent.getSerializableExtra("conversationslist");
	    	int index = conversationsListView.getFirstVisiblePosition();
	    	View v = conversationsListView.getChildAt(0);
	    	int top = (v == null) ? 0 : (v.getTop() - conversationsListView.getPaddingTop());
			adapter = new ConversationsListAdapter(context, conversationslist, conversationsListView, getFragmentManager());
			conversationsListView.setAdapter(adapter);
			conversationsListView.setSelectionFromTop(index, top);
	    }
	};  

	private BroadcastReceiver messagesLoadedReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	GetConversationsTask task = new GetConversationsTask();
	    	task.execute();
	    	conversationsLoadingTV.setVisibility(View.GONE);
	    }
	};  

	class GetConversationsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBBUDDY);
				BundleKeys.QB_Users = ((ENTQBBuddyHandler) handler).getBuddies();
				handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
				//conversations = ((ENTQBConversationHandler) handler).getPublicDialogs(null);
				conversations = ((ENTQBConversationHandler) handler).getConversations();
			}  catch (Exception e) {
				conversations = new ArrayList<ENTConversation>();
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(conversations!=null && conversationsListView!=null) {
				adapter = new ConversationsListAdapter(context, conversations, conversationsListView, getFragmentManager());
				conversationsListView.setAdapter(adapter);
				if(getActivity()!=null) {
					getActivity().registerReceiver(conbroadcastReceiver, new IntentFilter(NewConversationBroadcastService.BROADCAST_ACTION));
				}
			}
		}		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			getActivity().unregisterReceiver(messagesLoadedReceiver);
			getActivity().unregisterReceiver(conbroadcastReceiver);
		} catch(Exception e){
			
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		this.menu = menu;
		inflater.inflate(R.menu.menu_sec_msg, menu);
		
		NetworkState ns = new NetworkState(EntradaApplication.getAppContext());
		boolean isConnected = ns.isConnectingToInternet();
		if(!isConnected){
			composeMenuItem = menu.findItem(R.id.item_new_msg);
			composeMenuItem.setEnabled(false);
			composeMenuItem.getIcon().setAlpha(80);
		}

		if(!application.isJobListEnabled()) {
			MenuItem setting = menu.findItem(R.id.item_setting);
			setting.setVisible(true);
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	      case R.id.item_new_msg:
	    	  NetworkState ns = new NetworkState(EntradaApplication.getAppContext());
	    	  boolean isConnected = ns.isConnectingToInternet();
	    	  if(isConnected){
	    	  fetchTOU();
	    	  if(touAccepted){
	    		  getFragmentManager().beginTransaction().replace(R.id.fragcontent, new SMContacts(), "contacts").addToBackStack(null).commit();
	    	  }else{
	    		  if(BundleKeys.TOUContent != null){
	    			  if(getActivity() != null&&!getActivity().isFinishing())
	    			  dgTOU(BundleKeys.TOUContent);
	    		  }else{
	    			  getTOUContent(SessionToken);
	    		  }
	    	  }
	    	  }
	    	  return true;
	      case R.id.item_setting:
				startActivity(new Intent(getActivity(), EntradaSettings.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	      	  return true;
	      default:
	         return super.onOptionsItemSelected(item);
	   }
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
		getActivity().finish();
	}
	
	//Fetch TOUVersion number from DB
    public void fetchTOU(){
    	list_tou =  reader.getTOUVersion();
    	if(list_tou != null && list_tou.size() > 0){
			for(int i=0;i<list_tou.size();i++){
				curTOUVersionNumber = list_tou.get(i).getTOUVersion();
				touAccepted = list_tou.get(i).getTOUAccepted();
			}
		}
    }
    
    String resString = null;
    //Async task for TOU
    public void getTOUFromWS(final boolean hasVerno, final String vno){
    	
	    new AsyncTask<Void, Void, Void>() {
	    	
	    	@Override
	    	protected void onPreExecute() {
	    		// TODO Auto-generated method stub
	    		super.onPreExecute();
	    		if(dialog != null){
					dialog.show();
					dialog.setContentView(R.layout.progress_spinner_only);
	    		}
	    		Log.d(TAG, "fetching TOU from WS");
	    	}
	    	
			@Override
			protected Void doInBackground(Void... voids) {
				try {
					MainUserDatabaseProvider provider = new MainUserDatabaseProvider(false);
					EUser user = provider.getCurrentUser();
				 	Log.e("TOU-params", user.getName()+"/"+user.getPassword()+"/"+m_androidId);
				 	resString = service.TOU(user.getName(), user.getPassword(), m_androidId);
				 }catch (ServiceException e) { 
					  // TODO Auto-generated catch block
					  e.printStackTrace(); 
				 } catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if(dialog != null){
					if(dialog.isShowing()){
					dialog.dismiss();
					}
				}
					
				//Log.e(TAG, resString);
				JSONObject JsonObjTOU = null;
				try {
					if(resString != null && resString.length() > 0)
					JsonObjTOU = new JSONObject(resString);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(JsonObjTOU instanceof JSONObject){
					Log.d(TAG, "TOU fetch from WS complete");
						try {
							TOUVersionNumber = JsonObjTOU.getString("TOUVersionNumber");
							UserId = JsonObjTOU.getString("UserId");
							SessionToken = JsonObjTOU.getString("SessionToken");
							application.setStringIntoSharedPrefs(BundleKeys.SESSION_TOKEN, SessionToken);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.e(TAG, "TOUVersion from WS: "+TOUVersionNumber);
				}
				
				//if WS returned zero(0), verno is saved and tou is accepted, then show sec msg
				if(TOUVersionNumber.trim().equals("0") && hasVerno && touAccepted){
					Log.d(TAG, "TOU Service error");
				}else 
					//if WS returned zero(0) due to connection problem, verno is not saved and tou is not accepted, then finish this activity
					if(TOUVersionNumber.trim().equals("-1") && !hasVerno && !touAccepted){
						//Log.d(TAG, "TOU Service error - No Ver no found - reverting back to previous screen");
						//		"\nPlease chech your intenet connection and retry", Toast.LENGTH_LONG).show();
						AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				        dialog.setIcon(R.drawable.icon_dark);
				        dialog.setCancelable(false);
				        dialog.setTitle("Terms of Use");
				        dialog.setMessage(getResources().getString(R.string.sec_msg_conn_error));
				        dialog.setPositiveButton("OK", new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								getActivity().finish();
							}
						});
				        dialog.show();
						
				}else{
					if(hasVerno){//TOU table exists and data is there
						//check if TOU is accepted and show pop up accordingly
						if(touAccepted){
							Log.d(TAG, "TOU accepted");
							BundleKeys.isTOUAccepted = true;
						//If curVerNo < TOUVerNo, update TOUVerNo from WS and set Accepted falg to 'false'
							if( Integer.parseInt(curTOUVersionNumber) < Integer.parseInt(TOUVersionNumber) ){
								Log.d(TAG, "New TOU version found..Updating db");
								reader.updateTOU(UserId, TOUVersionNumber, false);
								if(BundleKeys.TOUContent != null){
									if(getActivity() != null&&!getActivity().isFinishing())
									dgTOU(BundleKeys.TOUContent);
								}else{
									getTOUContent(SessionToken);
								}
								
							}else{
								conversationsListView.setFocusable(true);
						        conversationsListView.setFocusableInTouchMode(true);
						        conversationsListView.setEnabled(true);
							}
						}else{
							//Show pop up
							BundleKeys.isTOUAccepted = false;
							Log.d(TAG, "TOU not accepted - Show TOU pop up");
							
							if(BundleKeys.TOUContent != null){
								if(getActivity() != null&&!getActivity().isFinishing())
								dgTOU(BundleKeys.TOUContent);
							}else{
								getTOUContent(SessionToken);
							}
						}
					}else{//No TOU table - create,save and show TOU pop-up
						BundleKeys.isTOUAccepted = false;
						reader.saveTOU(UserId, TOUVersionNumber);
						Log.d(TAG, "First time: TOU not accepted - Show TOU pop up");
						
						if(BundleKeys.TOUContent != null){
							if(getActivity() != null&&!getActivity().isFinishing())
							dgTOU(BundleKeys.TOUContent);
						}else{
							getTOUContent(SessionToken);
						}
					}
				}
			};
		}.execute();
    }
    
    public void getTOUContent(final String SessionToken){
    	new AsyncTask<Void, Void, Void>() {
    		
    		protected void onPreExecute() {
    			//if(!hasVerno && !touAccepted){
    			if(dialog != null){
    				dialog.show();
    				dialog.setContentView(R.layout.progress_spinner_only);
    			}
    			//}
    			
    		};
    		
    		@Override
    		protected Void doInBackground(Void... params) {
    			// TODO Auto-generated method stub
    			try {
				 	//Log.e("TOU-Content-params", SessionToken);
				 	resString = service.getTOUContent(SessionToken);
				 	Log.e("TOU-Content", resString);
				 }catch (ServiceException e) { 
					  // TODO Auto-generated catch block
					  e.printStackTrace(); 
				 } 
				return null;
    		}
    		
    		@Override
    		protected void onPostExecute(Void result) {
    			// TODO Auto-generated method stub
    			super.onPostExecute(result);
    			if(dialog != null){
    				if(dialog.isShowing()){
    				dialog.dismiss();
					}
				}
    			if(getActivity() != null&&!getActivity().isFinishing())
    			dgTOU(resString);
    			BundleKeys.TOUContent = resString;
    		}
    	}.execute();
		
    }

    /*
     * TOU Content popup
     */
	AlertDialog TOUalert;
    public void dgTOU(String content){
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder.setIcon(R.drawable.icon_dark);
    	builder.setCancelable(false);
    	builder.setTitle("Terms of Use");
        //replace double quotes in string
        //content = content.replace("\"","");
        //builder.setMessage(content);
    	builder.setMessage(getResources().getString(R.string.eula));
        
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	Log.d(TAG, "TOU accepted");
            	//update db
            	reader.updateTOU(UserId, TOUVersionNumber, true);
            	BundleKeys.isTOUAccepted = true;
            	isAlertShowing = false;
            	onResume();
            }
        });
        
        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Log.d(TAG, "TOU rejected");
                BundleKeys.isTOUAccepted = false;
                isAlertShowing = false;
                getActivity().finish();
                
            }
        });
        
        TOUalert = builder.create();
        if(!isAlertShowing){
        	TOUalert.show();
        	isAlertShowing = true;
        }
        
    }
    
}
