package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xiph.vorbis.recorder.VorbisRecorder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.adapters.ConversationAdapter;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ChatMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Contact;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTMessage;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.personal.activities.inbox.utils.ImageLoadingUtils;
import com.entradahealth.entrada.android.app.personal.activities.job_display.StopWatch;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.User;
import com.entradahealth.entrada.core.auth.UserPrivate;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.Encounter;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTQBMessageHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.inbox.encryption.AES256Cipher;
import com.entradahealth.entrada.core.inbox.service.ChatManager;
import com.entradahealth.entrada.core.inbox.service.GroupChatManagerImpl;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;

public class NewMessageFragment extends Fragment {
	String TAG ="New-Msg-Fragment";
	private ListView lvChatList;
	private Button btnRecordAudio, btnAttachContent;
	private EditText etMessage;
	private TextView tvDuration, btnSend;
	private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	private ConversationAdapter adapter;
	private String patient_name = null;
	private String recipient_name = null; 
	private Long patient_id;
	File root, accountPath, recipientPath, chatPath, audioPath, imagePath;
	Account currentAccount = null;
	private String accountName = null;
	private List<Encounter> list_encounter;
	public static List<Job> list_job;
	private DomainObjectReader reader;
	Calendar c;
	private int year, mon, day, hr, min;
	String str_hr, str_min;
	File[] acct_folders, job_folders, img_folders, saved_images;
	private VorbisRecorder recorder;
	private Handler timerUpdateHandler;
	private StopWatch stopWatch;
	ArrayList<String> list_selected_images, list_saved_images;
	boolean hasImages = false;
	private ENTConversation conversation;
	private String recipientId;
	private EntradaApplication application;
	private ArrayList<Contact> contacts;
	private UserState state;
	private SMDomainObjectWriter writer;
	private SMDomainObjectReader smReader;
	private boolean canSharePatientClinicalInformation = false;
	private APIService service;
	private ImageView ivPatPic;
	private TextView tvPatName, participantsLeftMsg;
	private NewMessageFragment fragment;
	private SecureMessaging activity;
	private String passPhrase;
	private AES256Cipher cipher;
	private ChatManager chat;
	private boolean fromPatientSearch, fromRecordingScreen;
	private ENTHandlerFactory handlerFactory;
	private boolean settingPatPermission = false;
	private View mCustomView;
	private com.entradahealth.entrada.android.app.personal.Environment env;
	private int recepientsCount = 0;
	
	public static final String GROUP_MESSAGE = "Group Message";
	public static final String MESSAGE = "Message";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	fragment = this;
    	handlerFactory = ENTHandlerFactory.getInstance();
		BundleKeys.fromSecureMessaging = false;
    	Bundle bundle = this.getArguments();
    	chat = new GroupChatManagerImpl(this);
    	cipher = new AES256Cipher();
    	activity = (SecureMessaging) getActivity();
    	application = (EntradaApplication) EntradaApplication.getAppContext();
    	state = AndroidState.getInstance().getUserState();
		try {
			EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
			env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
			service = new APIService(env.getApi());
			state.setSMUser();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DomainObjectWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
    	smReader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
		if(bundle != null){
			patient_name = bundle.getString("patient_name"); 
			recipient_name = bundle.getString("recipient_name");
			recipientId = bundle.getString("recipient_id");
			patient_id = bundle.getLong("patient_id");
			fromPatientSearch = bundle.getBoolean("fromPatientSearch");
			fromRecordingScreen = bundle.getBoolean("fromRecordingScreen"); 
			conversation= (ENTConversation) bundle.getSerializable("conversation");
			list_selected_images = bundle.getStringArrayList("list_selected_images");
			contacts = bundle.getParcelableArrayList("contacts");
			if(list_selected_images != null)
			Log.e(TAG, Integer.toString(list_selected_images.size()));
		}
		
		if(recipientId != null || contacts != null) {
	        if(contacts!=null) {
	    		StringBuffer buffer = new StringBuffer();
	    		recepientsCount = contacts.size();
	    		if(recepientsCount>0){
	    			buffer.append(contacts.get(0).getContactName());
					if(recepientsCount>1){
						buffer.append(" +"+(recepientsCount-1));
					}
	    		}
	    		recipient_name = buffer.toString();
	        }
			CreateNewDialogTask task = new CreateNewDialogTask();
			task.execute();
		} 

		BundleKeys.isFront = true;
		messages = new ArrayList<ChatMessage>();
				
		if(conversation != null) {
			String[] recipients = removeElements(conversation.getOccupantsIds(), application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID));
			StringBuffer buffer = new StringBuffer();
			recepientsCount = recipients.length;
			if(recepientsCount>0){
				ENTUser user = getENTUserFromId(recipients[0]);
				buffer.append(user == null? recipients[0] : user.getName());
				if(recepientsCount>1){
					buffer.append(" +"+(recepientsCount-1));
				}
			}
			patient_id = conversation.getPatientID();
			recipient_name = buffer.toString();
			UpdateDialogTask task = new UpdateDialogTask();
			task.execute();
		}
        synchronized (state) {
     	   currentAccount = state.getCurrentAccount();
        }
		
		if(patient_id != 0){
			Log.e("patient_id", Long.toString(patient_id));
			if(patient_name == null) {
				if(currentAccount != null) {
		            DomainObjectReader reader = state.getProvider(currentAccount);
					Patient patient = reader.getPatient(patient_id);
					patient_name = (patient!=null) ? patient.getFirstName() + " " + patient.getLastName() : "Loading Patient..";
				}
			}
			getImagesPath();
		}
		
		accountPath = new File(UserPrivate.getUserRoot(), application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
        recipientPath = new File(accountPath, recipient_name);
        recipientPath.mkdirs();
        chatPath = new File(recipientPath, "chats");
        if(!chatPath.exists())
     	   chatPath.mkdirs();
        audioPath = new File(chatPath, "audio");
        if(!audioPath.exists())
     	   audioPath.mkdirs();
        imagePath = new File(chatPath, "images");
        if(!imagePath.exists())
     	   imagePath.mkdirs();
        

		LayoutInflater mInflater = LayoutInflater.from(activity);
		mCustomView = mInflater.inflate(R.layout.acbar_new_msg, null);
		tvPatName = (TextView)mCustomView.findViewById(R.id.tvPatName);
		TextView tvRecipient = (TextView)mCustomView.findViewById(R.id.tvRecipient);
		TextView tvabTitle = (TextView)mCustomView.findViewById(R.id.tvabTitle);
		ivPatPic = (ImageView)mCustomView.findViewById(R.id.ivPatImage);

		tvPatName.setSelected(true);
		tvRecipient.setSelected(true);
		tvPatName.setText(patient_name);
		tvRecipient.setText(recipient_name);
		
		tvabTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int count = getFragmentManager().getBackStackEntryCount();
				if (count > 0) {
					activity.getFragmentManager().popBackStack();
				}else{
					activity.getFragmentManager().popBackStack();
				}
			}
		});
		if((conversation!=null && conversation.getUserId()!=null && conversation.getUserId().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))) || patient_id!=0){
			ivPatPic.setVisibility(View.VISIBLE);
		} else {
			ivPatPic.setVisibility(View.GONE);
		}
		
		ivPatPic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					Bundle b = new Bundle();
					if(patient_id == 0){
		        		b.putString("patient_name", patient_name);
		        		b.putString("recipient_name", recipient_name);
		        		b.putSerializable("conversation", conversation);
						PatientSearchFrament patSearch = new PatientSearchFrament(chat);
						patSearch.setArguments(b);
						FragmentTransaction ft = activity.getFragmentManager().beginTransaction().addToBackStack(null);
		        		ft.replace(R.id.fragcontent, patSearch, null);
		        		ft.commit();
					} else {
						b.putSerializable("conversation", conversation);
						b.putLong("patient_id", patient_id);
		        		android.support.v4.app.FragmentManager fragmentManager = ((SecureMessaging) activity).getSupportFragmentManager();
		        		android.support.v4.app.FragmentTransaction ft = fragmentManager.beginTransaction().addToBackStack(null);
						PatientClinicalViewFragment patClinical = new PatientClinicalViewFragment(fragment);
						patClinical.setArguments(b);
		        		ft.replace(R.id.fragcontent, patClinical, null);
		        		ft.commit();
					}
				}
			}
		});
		
		if(patient_name == null) {
			if(recepientsCount>1){
				tvPatName.setText(GROUP_MESSAGE);
			} else {
				tvPatName.setText(MESSAGE);
			}
		}
		else{
			tvPatName.setText(patient_name);
		}
		if(recipient_name != null) {
			tvRecipient.setText(recipient_name);
		}
    }
    
    class SendPatientAccessTask extends AsyncTask{

    	private String patientName;
    	private long patientID;
    	private ENTMessage entmessage;
    	
    	public SendPatientAccessTask(String patientName, long patientID){
    		this.patientName = patientName;
    		this.patientID = patientID;
    		 entmessage = new ENTMessage();
    	}
    	
		@Override
		protected Object doInBackground(Object... params) {			
			conversation.setPatientAccess(true);
			entmessage.setMessage("Granted access to patient "+patientName);
			entmessage.setCustomString("1");
			entmessage.setPatientID(patientID);
			entmessage.setChatDialogId(conversation.getId());
			entmessage.setPassPhrase(conversation.getPassPhrase());
			try {
				APIService service = new APIService(env.getApi());
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				service.revokePatienInfoSharingPermission(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try {
				chat.sendMessage(entmessage);
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    }

    
    class CreateNewDialogTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
				ENTMessage message = new ENTMessage();
				List<String> recipients = new ArrayList<String>();
				if(contacts!=null) {
					for(Contact con : contacts){
						recipients.add(con.getContactNo()); 
					}
				} else if(recipientId != null){
					recipients.add(recipientId);
				}
				String[] _recipients = new String[recipients.size()];
 				message.setRecipients(recipients.toArray(_recipients));
				conversation = ((ENTQBConversationHandler) handler).createDialog(message);
				passPhrase = service.createThread(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId());
				conversation.setPassPhrase(passPhrase);
			}  catch (Exception e) {
				passPhrase = "test";
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(application.is3GOr4GConnected() || application.isWifiConnected()){
				try{
					Log.e("", "conversation.getXmpp_room_jid()--"+conversation.getXmpp_room_jid());
		            ((GroupChatManagerImpl) chat).joinGroupChat(conversation, new QBEntityCallbackImpl() {
		                @Override
		                public void onSuccess() {
		                }
		
		                @Override
		                public void onError(List list) {
		                }
		            });
				} catch(Exception ex){
					conversation = smReader.getConversationById(conversation.getId());
					Log.e("", "conversation.getXmpp_room_jid()--"+conversation.getXmpp_room_jid());
		            ((GroupChatManagerImpl) chat).joinGroupChat(conversation, new QBEntityCallbackImpl() {
		                @Override
		                public void onSuccess() {
		                }
		
		                @Override
		                public void onError(List list) {
		                }
		            });
				}
			}
			if((conversation!=null && conversation.getUserId()!=null && conversation.getUserId().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))) || patient_id!=0){
				ivPatPic.setVisibility(View.VISIBLE);
			} else {
				ivPatPic.setVisibility(View.GONE);
			}		
	       	adapter = new ConversationAdapter(activity, messages, recipient_name, patient_name, conversation.getId(), passPhrase);
	       	lvChatList.setAdapter(adapter);
    		StringBuffer buffer = new StringBuffer();
    		for (int i = 0; i < contacts.size(); i++) {
    			buffer.append(contacts.get(i).getContactName()+",");
    		}
    		recipient_name = (buffer.length()>0)? buffer.substring(0, buffer.length()-1) : "";

			sendConvCreateMessage();
			BundleKeys.CURRENT_CONVERSATION = conversation;
		}

		private void sendConvCreateMessage() {
			ENTMessage entmessage = new ENTMessage();
			ENTUser user = getENTUserFromId(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID));
			entmessage.setMessage("Conversation started by "+user.getName()+" with "+recipient_name);
			entmessage.setCustomString("1");
			entmessage.setPatientID(patient_id);
			entmessage.setChatDialogId(conversation.getId());
			entmessage.setPassPhrase(passPhrase);
			try {
				chat.sendMessage(entmessage);
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if(!conversation.getPatientAccess())
		    {
	        if(fromPatientSearch && patient_id!=0 || fromRecordingScreen && patient_id!=0){
				SendPatientAccessTask task = new SendPatientAccessTask(patient_name, patient_id);
				task.execute();
			}
		
		}
		}		
    	
    }
    
	class UpdateDialogTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
			}  catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
	       	adapter = new ConversationAdapter(activity, messages, recipient_name, patient_name, conversation.getId(), passPhrase);
	       	lvChatList.setAdapter(adapter);
			BundleKeys.CURRENT_CONVERSATION = conversation;
			GetMessagesTask task = new GetMessagesTask();
			task.execute();
		}		
	}
	public String getTimeStamp() {
		String time = new Long(new Date().getTime()).toString();
		String timestamp = time.substring(0, time.length() - 3);
		return timestamp;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	
    	View view = inflater.inflate(R.layout.conversation, container, false); 
    	lvChatList = (ListView)view.findViewById(R.id.lvChatList);
    	etMessage = (EditText)view.findViewById(R.id.etmessagebox);
    	btnSend = (TextView)view.findViewById(R.id.btnSend);
    	btnAttachContent = (Button) view.findViewById(R.id.btnattach);
    	btnRecordAudio = (Button) view.findViewById(R.id.btnrecordaudio);
    	tvDuration = (TextView)view.findViewById(R.id.tvRecDuration);
		participantsLeftMsg = (TextView) view.findViewById(R.id.participantsLeftMsg);
		
	    lvChatList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ChatMessage cMsg = (ChatMessage)lvChatList.getItemAtPosition(position);
				if(cMsg.getMessageType() == 1)
					Log.e("sel_img_path", cMsg.getImagePath());
				
			}
	    	
		});
	    
	    //final List<ChatMessage> received = getRecepientData();
    	btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					if(etMessage.getText().toString()!=null && !etMessage.getText().toString().isEmpty()){
						String str = etMessage.getText().toString().trim();
						String newString = etMessage.getText().toString().trim();
						try {
							newString = new String(str.getBytes("UTF-8"), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						ChatMessage chatMessage = new ChatMessage(newString, true);
						chatMessage.setMessageType(ChatMessage.MSGTYPE_TEXT);
						chatMessage.setMessageTime(new Date());
						SendMessageTask messageTask = new SendMessageTask(chatMessage);
						messageTask.execute();
					}
				}
			}
		});
    	
    	
    	
    	etMessage.addTextChangedListener(new TextWatcher() {

    	      @Override
    	      public void onTextChanged(CharSequence s, int start, int before, int count) {
    	    	  if(s.toString().trim().equals("") || s.length()==0) {
    	    		  btnRecordAudio.setVisibility(View.VISIBLE);
    	    		  btnSend.setVisibility(View.GONE);
    	    	  } else {
    	    		  btnRecordAudio.setVisibility(View.GONE);
    	    		  btnSend.setVisibility(View.VISIBLE);
    	    	  }
    	      }

    	      @Override
    	      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    	    	  
    	      }

    	      @Override
    	      public void afterTextChanged(Editable arg0) {
    	    	  
    	      }
    	      
    	    });
    	
    	
    	
    	//Listener for mic release to stop recording
    	btnRecordAudio.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_UP) {
					btnAttachContent.setVisibility(View.VISIBLE);
		            etMessage.setVisibility(View.VISIBLE);
		            tvDuration.setVisibility(View.GONE);
			        //uploadAudio(outputFile);
					if (recorder != null && recorder.isRecording()) {
						 recorder.stop();
				    }
	    	    	scrollListViewToBottom();
			    } else if(event.getAction() == MotionEvent.ACTION_DOWN){
			    	Vibrator vb = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
		            vb.vibrate(100);
		            stopWatch = new StopWatch();
		            //Hide Attach, Msg, Send view & Make duration tv visible
		            btnAttachContent.setVisibility(View.GONE);
		            etMessage.setVisibility(View.GONE);
		            btnSend.setVisibility(View.GONE);
		            tvDuration.setVisibility(View.VISIBLE);
		            timerUpdation();
			    }
				return true;
			}
		});
    	
 
    
    	//Attachment for message
    	btnAttachContent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					PopupMenu popupMenu = new PopupMenu(activity, v);
					popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							// TODO Auto-generated method stub
							switch(item.getItemId()){
							case R.id.item_take_photo:
								//open camera
								if(application.is3GOr4GConnected() || application.isWifiConnected()){
									startCameraActivity();
								}
								break;
							case R.id.item_choose_photo:
								if(application.is3GOr4GConnected() || application.isWifiConnected()){
									if(patient_id != 0){
										hasImages = getImagesPath();
										if(hasImages){
											Bundle b = new Bundle();
							        		b.putString("patient_name", patient_name);
							        		b.putString("recipient_name", recipient_name);
							        		b.putLong("patient_id", patient_id);
							        		b.putSerializable("conversation", conversation);
											ChooseImagesFragment imgFrag = new ChooseImagesFragment();
											imgFrag.setArguments(b);
							        		FragmentTransaction ft = activity.getFragmentManager().beginTransaction().addToBackStack(null);
							        		ft.replace(R.id.fragcontent, imgFrag, null);
							        		ft.commit();
										}else{
											Toast.makeText(activity, "No images found", Toast.LENGTH_SHORT).show();
										}
									}else{
										Toast.makeText(activity, "You must select a patient first", Toast.LENGTH_SHORT).show();
									}
								}
								break;
							case R.id.item_access_clinical_data:
								if(application.is3GOr4GConnected() || application.isWifiConnected() && !settingPatPermission){
									settingPatPermission = true;
									String title = (String) item.getTitle();
									if(title.equals(getResources().getString(R.string.pmenu_revoke_access_clinical_data))){
										new SetPatientInfoSharingPermission(false).execute();
									} else {
										new SetPatientInfoSharingPermission(true).execute();
									}
								}
								break;
							}
							return true;
						}
					});
					popupMenu.getMenuInflater().inflate(R.menu.popup_menu_attach, popupMenu.getMenu());
					if(conversation.getPatientID()!=0 && conversation.getUserId()!=null && conversation.getUserId().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))){
						popupMenu.getMenu().findItem(R.id.item_access_clinical_data).setVisible(true);
						if(canSharePatientClinicalInformation) {
							popupMenu.getMenu().findItem(R.id.item_access_clinical_data).setTitle(getResources().getString(R.string.pmenu_revoke_access_clinical_data));
						} else {
							popupMenu.getMenu().findItem(R.id.item_access_clinical_data).setTitle(getResources().getString(R.string.pmenu_grant_access_clinical_data));
						}
					} else {
						popupMenu.getMenu().findItem(R.id.item_access_clinical_data).setVisible(false);
					}
					popupMenu.show();
				}
			}
		});
    	
    	return view;
    }
    
    class GetPatientInfoSharingPermission extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			
			try {
				if(application.is3GOr4GConnected() || application.isWifiConnected()){
					try {
						String str = service.getPatienInfoSharingPermission(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId());
						JSONObject json = null;
						try{
							json = new JSONObject(str);
							canSharePatientClinicalInformation = json.getBoolean("Permission");
						} catch(JSONException e){
							//String errorMsg = json.getString("ErrorMessage");
							//if(errorMsg.contains("Message thread does not exist")){
								canSharePatientClinicalInformation = false;
							//}
						}
					} catch (ServiceException e) {
						e.printStackTrace();
					} 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
    }
    
    class SetPatientInfoSharingPermission extends AsyncTask{

    	private boolean isGrant;
    	private String message = null;
    	private ChatMessage chatMessage = null;
    	private ENTMessage entmessage = null;
    	int statusCode;
    	
    	public SetPatientInfoSharingPermission(boolean isGrant){
    		this.isGrant = isGrant;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		if(isGrant){
    			canSharePatientClinicalInformation = true;
    		} else {
    			canSharePatientClinicalInformation = false;
    		}
    	}
    	
		@Override
		protected Object doInBackground(Object... params) {
			try {
				if(application.is3GOr4GConnected() || application.isWifiConnected()){
					if(isGrant) {
						statusCode = service.grantPatienInfoSharingPermission(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation);
						if(statusCode == 200){
							message = "You have been Granted Access to the Patients Clinical Data";
							canSharePatientClinicalInformation = true;
						} else {
							canSharePatientClinicalInformation = false;
							return null;
						} 
					} else {
						statusCode = service.revokePatienInfoSharingPermission(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation);
						if(statusCode == 200){
							message = "Access to the Patients Clinical Data has now been Revoked.";
							canSharePatientClinicalInformation = false;
						} else {
							canSharePatientClinicalInformation = true;
							return null;
						}
					}
					if(message!=null) {
						entmessage = new ENTMessage();
						entmessage.setMessage(message);
						entmessage.setCustomString("1");
						entmessage.setPatientID(patient_id);
						entmessage.setChatDialogId(conversation.getId());
						entmessage.setPassPhrase(passPhrase);
						chat.sendMessage(entmessage);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			settingPatPermission = false;
			if(statusCode == 401) {
				new Authenticate(isGrant).execute();
			} 
		}
    	
    }

    class Authenticate extends AuthenticateTask{
    	
    	private boolean isGrant;
    	
    	public Authenticate(boolean isGrant){
    		this.isGrant = isGrant;
    	}
    	
    	@Override
    	protected void onSucsessfullExecute() {
    		super.onSucsessfullExecute();
    		new SetPatientInfoSharingPermission(isGrant).execute();
    	}
    	
    }

    public void showMessage(ChatMessage chatMessage){
		adapter.addMessage(chatMessage);
		adapter.notifyDataSetChanged();

		etMessage.setText(null);
		btnRecordAudio.setVisibility(View.VISIBLE);
		btnSend.setVisibility(View.GONE);

		scrollListViewToBottom();

    }
    
    class SendMessageTask extends AsyncTask<Void, Void, Void> {

    	private ChatMessage message;
    	private ENTMessage entmessage;
    	
    	public SendMessageTask(ChatMessage message) {
    		this.message = message;
		}
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		btnSend.setClickable(false);
    	}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				entmessage = new ENTMessage();
				entmessage.setChatDialogId(conversation.getId());
				entmessage.setAttachmentID(message.getAttachmentId());
				entmessage.setContentType(message.getMessageType());
				entmessage.setMessage(message.getMessage());
				entmessage.setPassPhrase(passPhrase);
				chat.sendMessage(entmessage);
			}  catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			etMessage.setText("");
			btnSend.setClickable(true);

		}		
	}
        
	class GetMessagesTask extends AsyncTask<Void, Void, Void> {
		
		private List<ENTMessage> _messagesList;
		
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBMESSAGE);
				_messagesList = ((ENTQBMessageHandler) handler).getMessages(conversation);
			}  catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			List<ChatMessage> _messages = new ArrayList<ChatMessage>();
			for (ENTMessage message : _messagesList) {
				boolean isMine = (message.getSender().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID)))? true : false;
				ChatMessage chatMessage = new ChatMessage(message.getMessage(), isMine);
				chatMessage.setRead(message.isRead());
				chatMessage.setMessageType(message.getContentType());
				chatMessage.setAttachmentId(message.getAttachmentID());
				if(conversation.getOccupantsIds().length>2){
					chatMessage.setGroup(true);
				}
				if(message.getPatientID()!=0 && (tvPatName.getText().equals(MESSAGE) || tvPatName.getText().equals(GROUP_MESSAGE))){
					patient_id = message.getPatientID();
					new GetPatientDemographics().execute();
				}
				new UpdateLastMessage(message).execute();

				ENTUser user = getENTUserFromId(message.getSender());
				chatMessage.setFromContact(user!=null? user.getName() : message.getSender());
				chatMessage.setMessageTime(new Date(message.getSentDate()*1000));
				_messages.add(chatMessage);
				BundleKeys.LASTMESSAGETIME = message.getSentDate();
				BundleKeys.LASTMESSAGEID = message.getId();
			}
			adapter.addMessages(_messages);
			adapter.notifyDataSetChanged();
			scrollListViewToBottom();
	        if(list_selected_images != null && list_selected_images.size() > 0){
	      	   for(int i=0;i<list_selected_images.size();i++){
	      		 new UploadImageTask(list_selected_images.get(i), false).execute();
	      	   }
	        }
			if(application.is3GOr4GConnected() || application.isWifiConnected()){
				Log.e("", conversation+" "+conversation.getXmpp_room_jid());
	            ((GroupChatManagerImpl) chat).joinGroupChat(conversation, new QBEntityCallbackImpl() {
	                @Override
	                public void onSuccess() {
	                }
	
	                @Override
	                public void onError(List list) {
	                }
	            });
				passPhrase = application.getPassPhrase(conversation.getId());
				adapter.setPassPhrase(passPhrase);
				if(passPhrase==null){
					new GetPassPhraseTask().execute();
				}
				conversation.setPassPhrase(passPhrase);
				new GetPatientInfoSharingPermission().execute(); 
			}

		}
	}
	
	class GetPassPhraseTask extends AsyncTask{

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			try {
				passPhrase = service.getMessageThreadDetails(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId());
				adapter.setPassPhrase(passPhrase);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
  
	public void timerUpdation() {
		timerUpdateHandler = new Handler();
		if (stopWatch != null) {
			Long originalSeconds = stopWatch.getElapsedTimeSecs();
			int mins = (int) (originalSeconds / 60);
			int secs = (int) (originalSeconds % 60);
			String smins, ssecs;
			
			//Add leading zero to mins/secs
			if(mins < 10)
				smins = String.format("%02d", mins);
			else
				smins = String.valueOf(mins);
			
			if(secs < 10)
				ssecs = String.format("%02d", secs);
			else
				ssecs = String.valueOf(secs);
				
			tvDuration.setText(smins + " : "
					+ ssecs);
			timerUpdateHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					timerUpdation();
				}
			}, 100);
		}
	}
	
   	
   	@Override
   	public void onActivityCreated(Bundle savedInstanceState) {
   		// TODO Auto-generated method stub
   		super.onActivityCreated(savedInstanceState);
   	}
   	
	@Override
    public void onStart() {
       super.onStart();
       
	}
	
	//open camera 
	
	protected void startCameraActivity() {
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
		//String date_ext = year+"-"+(mon+1)+"-"+day+"-"+str_hr+"-"+str_min;
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    //String imageFileName = "IMG_" + timeStamp + "_";
		
		//Long tsLong = System.currentTimeMillis()/1000;
		//String time_stamp_ext = tsLong.toString();
		File file = null;
		file = new File(imagePath.getAbsolutePath(), "IMG_"+ timeStamp +".jpg");
		Uri outputFileUri = Uri.fromFile(file);
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		activity.setCurrentPhotoPath(outputFileUri.getPath());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0){
			if(resultCode == Activity.RESULT_OK){
				//Add photo to listview
				String imagePath = activity.getCurrentPhotoPath();
				if(imagePath != null){
					ImageCompressionAsyncTask compressionTask = new ImageCompressionAsyncTask(imagePath);
					compressionTask.execute();
				}
		    	
			}else if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(activity, "Cancelled", 500).show();
			}
		}
	}

	private void uploadAudio(final String audioPath){
		File audioFile = new File(audioPath);
		// Upload new file
		//
		QBContent.uploadFileTask(audioFile, true, null, new QBEntityCallbackImpl<QBFile>() {
		    @Override
		    public void onSuccess(QBFile qbFile, Bundle bundle) {
		    	int attachmentId = qbFile.getId();
				ChatMessage c = new ChatMessage("", true);
				c.setMessageType(ChatMessage.MSGTYPE_AUDIO);
				c.setSelectedContact(recipient_name);
				c.setMessageTime(new Date());

	    		String path = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversation.getId()+"/"+attachmentId+".ogg";
		    	try {
		    		File source = new File(audioPath);
		    		File destination = new File(path);
					copyDirectory(source, destination);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	c.setAttachmentId(String.valueOf(attachmentId));		    
		    	SendMessageTask task = new SendMessageTask(c);
		    	task.execute();
		    }

		    @Override
		    public void onError(List<String> strings) {

		    }
		}, new QBProgressCallback() {
		    @Override
		    public void onProgressUpdate(int progress) {
		    }
		});	
	}

	class UploadContent{
		
		private File encImageFile;
		private String imagePath;
		
		public UploadContent(File encImageFile, String imagePath){
			this.encImageFile = encImageFile;
			this.imagePath = imagePath;
		}
		
		public File getEncImageFile() {
			return encImageFile;
		}
		public String getImagePath() {
			return imagePath;
		}
	}
	
	class UploadImageTask extends AsyncTask{

		private String imagePath;
		private UploadContent content;
		private boolean deleteSource = true;
		
		public UploadImageTask(String imagePath){
			this.imagePath = imagePath;
		}
		
		public UploadImageTask(String imagePath, boolean deleteSource){
			this.imagePath = imagePath;
			this.deleteSource = deleteSource;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			final File imageFile = new File(imagePath);
			final File encimageFile = new File(getFilename());
			try {
				copyDirectory(imageFile, encimageFile);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				byte[] array = FileUtils.readFileToByteArray(imageFile);
				byte[] encrypted = cipher.encrypt(array, passPhrase);
				FileUtils.writeByteArrayToFile(encimageFile, encrypted);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			content = new UploadContent(encimageFile, imagePath);
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			// Upload new file
			QBContent.uploadFileTask(content.getEncImageFile(), true, null, new QBEntityCallbackImpl<QBFile>() {
			    @Override
			    public void onSuccess(QBFile qbFile, Bundle bundle) {
			    	int attachmentId = qbFile.getId();
			    	String destination = User.getUserRoot()+"/"+application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN)+"/"+conversation.getId()+"/images/"+attachmentId+".jpg";
			    	try {
			    		File tFile = new File(content.getImagePath());
						copyDirectory(tFile, new File(destination));
						if(deleteSource) {
							tFile.delete();
						}
						content.getEncImageFile().delete();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	ChatMessage c = new ChatMessage("", true);
					c.setMessageType(ChatMessage.MSGTYPE_IMAGE);
					c.setSelectedContact(recipient_name);
					c.setMessageTime(new Date());
					c.setImagePath(content.getImagePath());	
			    	c.setAttachmentId(String.valueOf(attachmentId));
			    	SendMessageTask task = new SendMessageTask(c);
			    	task.execute();
			    	deleteLastPhotoTaken();

			    }

			    @Override
			    public void onError(List<String> strings) {

			    }
			}, new QBProgressCallback() {
			    @Override
			    public void onProgressUpdate(int progress) {
			    }
			});
		}
		
	}
	
	public String getFilename() {
		File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
		if (!file.exists()) {
			file.mkdirs();
		}
		String uriSting = (file.getAbsolutePath() + "/"+ System.currentTimeMillis() + ".jpg");
		return uriSting;

	}
	
	
	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation , File targetLocation)
	throws IOException {

	    if (sourceLocation.isDirectory()) {
	        if (!targetLocation.exists() && !targetLocation.mkdirs()) {
	            throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
	        }

	        String[] children = sourceLocation.list();
	        for (int i=0; i<children.length; i++) {
	            copyDirectory(new File(sourceLocation, children[i]),
	                    new File(targetLocation, children[i]));
	        }
	    } else {

	        // make sure the directory we plan to store the recording in exists
	        File directory = targetLocation.getParentFile();
	        if (directory != null && !directory.exists() && !directory.mkdirs()) {
	            throw new IOException("Cannot create dir " + directory.getAbsolutePath());
	        }

	        InputStream in = new FileInputStream(sourceLocation);
	        OutputStream out = new FileOutputStream(targetLocation);

	        // Copy the bits from instream to outstream
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        in.close();
	        out.close();
	    }
	}
	
	//routine to delete last taken picture from DCIM folder
	private void deleteLastPhotoTaken() {

	    String[] projection = new String[] {
	            MediaStore.Images.ImageColumns._ID,
	            MediaStore.Images.ImageColumns.DATA,
	            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
	            MediaStore.Images.ImageColumns.DATE_TAKEN,
	            MediaStore.Images.ImageColumns.MIME_TYPE };

	    final Cursor cursor = EntradaApplication.getAppContext().getContentResolver().query(
	            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, 
	                            null,null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		try {
			if (cursor != null) {
				cursor.moveToFirst();

				int column_index_data = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

				String image_path = cursor.getString(column_index_data);

				File file = new File(image_path);
				if (file.exists()) {
					file.delete();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	class ImageCompressionAsyncTask extends AsyncTask<String, Void, String>{
		private boolean fromGallery = false;
		private ImageLoadingUtils utils;
		private String filePath;
		
		public ImageCompressionAsyncTask(String imagePath){
			utils = new ImageLoadingUtils(getActivity());
			this.filePath = imagePath;
		}

		@Override
		protected String doInBackground(String... params) {
			filePath = compressImage();
			return filePath;
		}
		
		public String compressImage() {
			
			Bitmap scaledBitmap = null;
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;						
			Bitmap bmp = BitmapFactory.decodeFile(filePath,options);
			
			int actualHeight = options.outHeight;
			int actualWidth = options.outWidth;

			actualHeight = options.outHeight;
			actualWidth = options.outWidth;     
					
			options.inSampleSize = utils.calculateInSampleSize(options, actualWidth, actualHeight);
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inTempStorage = new byte[16*1024];
				
			try{	
				bmp = BitmapFactory.decodeFile(filePath,options);
			}
			catch(OutOfMemoryError exception){
				exception.printStackTrace();
				
			}
			try{
				scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
			}
			catch(OutOfMemoryError exception){
				exception.printStackTrace();
			}
							
			float ratioX = actualWidth / (float) options.outWidth;
			float ratioY = actualHeight / (float)options.outHeight;
			float middleX = actualWidth / 2.0f;
			float middleY = actualHeight / 2.0f;
				
			Matrix scaleMatrix = new Matrix();
			scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

			Canvas canvas = new Canvas(scaledBitmap);
			canvas.setMatrix(scaleMatrix);
			canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

							
			ExifInterface exif;
			try {
				exif = new ExifInterface(filePath);
			
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
				Log.d("EXIF", "Exif: " + orientation);
				Matrix matrix = new Matrix();
				if (orientation == 6) {
					matrix.postRotate(90);
					Log.d("EXIF", "Exif: " + orientation);
				} else if (orientation == 3) {
					matrix.postRotate(180);
					Log.d("EXIF", "Exif: " + orientation);
				} else if (orientation == 8) {
					matrix.postRotate(270);
					Log.d("EXIF", "Exif: " + orientation);
				}
				scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileOutputStream out = null;
			String filename = getFilename();
			try {
				out = new FileOutputStream(filename);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			return filename;

		}
		
		public String getFilename() {
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
			if (!file.exists()) {
				file.mkdirs();
			}
			String uriSting = (file.getAbsolutePath() + "/"+ System.currentTimeMillis() + ".jpg");
			return uriSting;

		}
		
		@Override
		protected void onPostExecute(String result) {			 
			new UploadImageTask(result).execute();
		}
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(lvChatList!=null && adapter!=null){
			lvChatList.setAdapter(adapter);
			scrollListViewToBottom();
		}
		if(chat!=null && !chat.isJoined()) {
	        ((GroupChatManagerImpl) chat).joinGroupChat(conversation, new QBEntityCallbackImpl() {
	            @Override
	            public void onSuccess() {
	            }
	
	            @Override
	            public void onError(List list) {
	            }
	        });
		}
        activity.getActionBar().setDisplayUseLogoEnabled(false);
        activity.getActionBar().setDisplayShowHomeEnabled(false);
        activity.getActionBar().setDisplayShowTitleEnabled(false);
        activity.getActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getActionBar().setDisplayShowCustomEnabled(false);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		activity.getActionBar().setCustomView(mCustomView);
		activity.getActionBar().setDisplayShowCustomEnabled(true);
		if(recepientsCount == 0){
			participantsLeftMsg.setVisibility(View.VISIBLE);
            btnAttachContent.setVisibility(View.GONE);
            etMessage.setVisibility(View.GONE);
            btnSend.setVisibility(View.GONE);
            tvDuration.setVisibility(View.GONE);
            btnRecordAudio.setVisibility(View.GONE);
		} else {
			participantsLeftMsg.setVisibility(View.GONE);
            btnAttachContent.setVisibility(View.VISIBLE);
            etMessage.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.GONE);
            tvDuration.setVisibility(View.GONE);
            btnRecordAudio.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BundleKeys.isFront = false;
		try {
			chat.release();
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	/*
	 * Scroll istview to bottom after content added
	 */
	protected void scrollListViewToBottom() {
	    lvChatList.post(new Runnable() {
	        @Override
	        public void run() {
	            // Select the last row so it will scroll into view...
	        	lvChatList.setSelection(adapter.getCount() - 1);
	        }
	    });
	}
	
	/*
	 * Routine to get JobID from the selected Patient object
	 * 1. Get EncounterID from Patient Object
	 * 2. Get JobID from Encounter Object
	 */
	Long enc_id, job_id;
	public boolean getImagesPath(){
		if(currentAccount != null) {
			UserState state = AndroidState.getInstance().getUserState();
			accountName = state.getCurrentAccount().getName();
			currentAccount = state.getAccount(accountName);
			reader = state.getProvider(currentAccount);
			accountPath = new File(state.getUserData()
					.getUserAccountsDir(), accountName);
			//get encounter from patient id
			list_encounter = reader.getEncountersByPatient(patient_id);
			if(list_encounter != null && list_encounter.size() > 0){
				enc_id = list_encounter.get(0).id;
				Log.e("enc", Long.toString(enc_id));
				
				//get jobid from encounter
				list_job = reader.getJobsByEncounter(enc_id);
				if(list_job != null && list_job.size() > 0){
					job_id = list_job.get(0).id;
					Log.e("jobID", Long.toString(job_id));
	
					//Search inside 'accountPath'(1144....) for images under 'Images' folder in JobID folders
					acct_folders = accountPath.listFiles();
					if (acct_folders == null || acct_folders.length == 0) {
						Log.e(TAG, "No jobs dictated yet!!");
					}else{
						for (int i = 0; i < acct_folders.length; i++) {
							if(acct_folders[i].getName().trim().equals(String.valueOf(job_id))){
								job_folders = acct_folders[i].listFiles();
								for (int j = 0; j < job_folders.length; j++) {
									if(job_folders[j].isDirectory() && job_folders[j].getName().trim().equalsIgnoreCase("temp")){
										img_folders = job_folders[j].listFiles();
										if(img_folders.length > 0)
											hasImages = true;
										for (int k = 0; k < img_folders.length; k++) {
											Log.e("img"+k, img_folders[k].getAbsolutePath());
										}
										break;
									}								
								}
								break;
							}
							
						}
					}
				}
			}
		}
		return hasImages;
	}
	
	public ENTUser getENTUserFromId(String id){
		for(ENTUser user : BundleKeys.QB_Users){
			if(user.getId().equals(id)){
				return user;
			}
		}
		return null;		
	}
		
	class GetPatientDemographics extends AsyncTask{

		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				Patient patient;
				if(currentAccount != null) {
				    DomainObjectReader reader = state.getProvider(currentAccount);
					patient = reader.getPatient(patient_id);
					if(patient!=null) {
						patient_name = patient!=null? (patient.getFirstName() + " " + patient.getLastName()): "Loading patient..";
						//patient_name = (patient!=null && patient.getFullName()!=null) ? patient.getFullName() : String.valueOf(patient_id);
					} else {
						getDemographics();
					}
				}else {
						getDemographics();
					}
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		private void getDemographics() throws ServiceException {
			String response = service.getDemographicInfo(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId(), patient_id);
			JSONObject json;
			try {
				json = new JSONObject(response);
				patient_name = json.getString("FirstName")+" "+json.getString("LastName");
			} catch (JSONException e) {
				e.printStackTrace();
				patient_name = String.valueOf(patient_id);
			}
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			tvPatName.setText(patient_name);
			ivPatPic.setVisibility(View.VISIBLE);
		}
		
	}
	private void renderMessageUI(ENTMessage message,
			ChatMessage chatMessage, int type, String attachmentID) {
		conversation = BundleKeys.CURRENT_CONVERSATION;
		if(conversation.getOccupantsIds().length>2){
			chatMessage.setGroup(true);
		}
		try {
			writer.messageInsertUpdate(message);
		} catch (DomainObjectWriteException e) {
			e.printStackTrace();
		}
		ENTUser user = getENTUserFromId(message.getSender());
		chatMessage.setFromContact(user.getName());
		chatMessage.setMessageTime(new Date(message.getSentDate()*1000));
		chatMessage.setMessageType(type);
		chatMessage.setAttachmentId(attachmentID);
		adapter.addMessage(chatMessage);
		adapter.notifyDataSetChanged();
		BundleKeys.LASTMESSAGETIME = message.getSentDate();
		BundleKeys.LASTMESSAGEID = message.getId();
		if(message.getPatientID()!=0 && (tvPatName.getText().equals(MESSAGE) || tvPatName.getText().equals(GROUP_MESSAGE))){
			patient_id = message.getPatientID();
			new GetPatientDemographics().execute();
		}
		new UpdateLastMessage(message).execute();
	}
	
	public static String[] removeElements(String[] input, String deleteMe) {
		if (input != null) {
			List<String> list = new ArrayList<String>(Arrays.asList(input));
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).equals(deleteMe)) {
					list.remove(i);
				}
			}
			return list.toArray(new String[0]);
		} else {
			return new String[0];
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	class UpdateLastMessage extends AsyncTask{

		private ENTMessage message;
		
		public UpdateLastMessage(ENTMessage message){
			this.message = message;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			writer.updateLastMessageInConversationTable(message);
			return null;
		}
		
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
		//getActivity().finish();
	}

	public void showMessage(final ENTMessage entmessage) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isMine = (entmessage.getSender().equals(application
						.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))) ? true
						: false;
				String decrypted = cipher.decryptText(entmessage.getMessage(),
						passPhrase);
				entmessage.setMessage(decrypted);

				ChatMessage chatMessage = new ChatMessage(decrypted, isMine);
				if(conversation.getOccupantsIds().length>2){
					chatMessage.setGroup(true);
				}
				ENTUser user = getENTUserFromId(entmessage.getSender());
				chatMessage.setFromContact(user.getName());
				chatMessage.setAttachmentId(entmessage.getAttachmentID());
				chatMessage.setMessageType(entmessage.getContentType());
				chatMessage.setMessageTime(new Date(
						entmessage.getSentDate() * 1000));
				adapter.addMessage(chatMessage);
				adapter.notifyDataSetChanged();
				scrollListViewToBottom();
				try {
					if(!isMine) {
						new UpdateMessageAsRead(entmessage).execute();
					}
					writer.addMessageToConversation(entmessage);
				} catch (DomainObjectWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BundleKeys.LASTMESSAGETIME = entmessage.getSentDate();
				BundleKeys.LASTMESSAGEID = entmessage.getId();
				if(entmessage.getPatientID()!=0 && (tvPatName.getText().equals(MESSAGE) || tvPatName.getText().equals(GROUP_MESSAGE))){
					patient_id = entmessage.getPatientID();
					new GetPatientDemographics().execute();
				}
				new UpdateLastMessage(entmessage).execute();
			}
		});
	}
	
	class UpdateMessageAsRead extends AsyncTask{

		private ENTMessage entmessage;
		
		public UpdateMessageAsRead(ENTMessage entmessage){
			this.entmessage = entmessage;
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				StringifyArrayList<String> messages = new StringifyArrayList<String>();
				messages.add(entmessage.getId());
				QBChatService.markMessagesAsRead(entmessage.getChatDialogId(), messages);
			} catch (QBResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}

}
