package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Contact;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBBuddyHandler;
import com.entradahealth.entrada.core.remote.APIService;

public class InviteContacts extends Fragment {
	private ListView lvContacts;
	private static String[] mProjection;
	private static Cursor cursor;
	private ProgressBar pbar;
	private APIService service;
	private UserState state;
	private Account currentAccount;
	private EntradaApplication application;
	private AlertDialog inviteAlert, inviteResponseAlert;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.e("onCreate", "called");
		state = AndroidState.getInstance().getUserState();
		application = (EntradaApplication) EntradaApplication.getAppContext();
//		synchronized (state) {
			currentAccount = state.getCurrentAccount();
//		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getActivity().getActionBar().setTitle("Inbox");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.invite_contacts, container, false);
		
		lvContacts = (ListView)view.findViewById(R.id.lvContacts);
		
		pbar = (ProgressBar)view.findViewById(R.id.pbar);
		pbar.setVisibility(View.VISIBLE);
		mProjection = new String[] {
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };
		//String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";
		// 1 - Home, 2 - Mobile, 3 - Work
		String selection = ContactsContract.CommonDataKinds.Phone.TYPE + "=2";
		cursor = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                mProjection,
                selection,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
			
		List<Contact> contacts = prepareContacts();
		
		ContactsAdapter mAllAdapter = new ContactsAdapter(getActivity(), contacts);
		lvContacts.setAdapter(mAllAdapter);
		lvContacts.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Contact contact = (Contact)parent.getItemAtPosition(position);
        		inviteUserAlert(contact);
			}
		});
        
		
    pbar.setVisibility(View.GONE);
 
	return view;
}
		
	class ContactsAdapter extends BaseAdapter{

		private List<Contact> list;
		private LayoutInflater inflater;
		
		public ContactsAdapter(Context context, List<Contact> list){
			this.list = list;
			this.inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			return list.get(index);
		}

		@Override
		public long getItemId(int index) {
			// TODO Auto-generated method stub
			return index;
		}

		private class ViewHolder {
			TextView name;
			TextView phone;
			TextView email;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null)
				convertView = getConvertView(position);

			ViewHolder holder = (ViewHolder) convertView.getTag();
			updateView(position, holder);
			return convertView;

		}
		
		private void updateView(int position, ViewHolder holder) {
			Contact contact = list.get(position);
			holder.name.setText(contact.getContactName());
			holder.phone.setText(contact.getContactNo());
			holder.email.setText(contact.getEmail());
		}

		private View getConvertView(int position) {
			ViewHolder holder = new ViewHolder();
			View view = inflater.inflate(R.layout.invitecontact, null);
			holder.name = (TextView) view.findViewById(R.id.name);
			holder.phone = (TextView) view.findViewById(R.id.phone);
			holder.email = (TextView) view.findViewById(R.id.email);
			view.setTag(holder);
			return view;
		}
		
	} 
	
	class InviteSMUser extends AsyncTask{
		
		private Contact contact;
		private int statusCode;
		
		public InviteSMUser(Contact contact){
			this.contact = contact;
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			try {
				EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
				Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
				service = new APIService(env.getApi());
				statusCode = service.inviteSMUser(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), contact);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(statusCode == 201){
				LoadSMContacts loadContacts = new LoadSMContacts(contact);
				loadContacts.execute();
			} else {
				inviteSentResponseAlert(contact, R.string.sminvite_failed_alert_title, R.string.sminvite_failed_alert_message, R.string.sminvite_failed_alert_pos_button_name);
			}
		}
		
	}

	class LoadSMContacts extends AsyncTask{
		private Contact contact;
		
		public LoadSMContacts(Contact contact){
			this.contact = contact;
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
			ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
			ENTHandler handler = handlerFactory
					.getHandler(ENTHandlerFactory.QBUSER);
			handler = handlerFactory.getHandler(ENTHandlerFactory.QBBUDDY);
			((ENTQBBuddyHandler) handler).saveContacts();
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			inviteSentResponseAlert(contact, R.string.sminvite_success_alert_title, R.string.sminvite_success_alert_message, R.string.sminvite_success_alert_pos_button_name);
		}
		
	}
	
	public void inviteSentResponseAlert(Contact contact, int title, int message, int posbutton){
		AlertDialog.Builder builder = new AlertDialog.Builder(SecureMessaging.getInstance());
		builder.setTitle(title);
		Resources res = SecureMessaging.getInstance().getResources();
		String text = String.format(res.getString(message), contact.getContactName());
		builder.setMessage(text);
		builder.setPositiveButton(posbutton, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int which) {
				try {
					getFragmentManager().popBackStack();
				} catch(Exception ex){
					
				} 
			}
		});
		builder.setCancelable(false);
		inviteResponseAlert = builder.create();
		inviteResponseAlert.show();
	}
	
	public void inviteUserAlert(final Contact contact){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.sminvite_alert_title);
		Resources res = getResources();
		String text = String.format(res.getString(R.string.sminvite_alert_message), contact.getContactName());
		builder.setMessage(text);
		builder.setPositiveButton(R.string.sminvite_alert_pos_button_name, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,	int which) {
						if(application.is3GOr4GConnected() || application.isWifiConnected()) {
							new InviteSMUser(contact).execute();
						}
					}
				});

		builder.setNegativeButton(R.string.sminvite_alert_neg_button_name, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		inviteAlert = builder.create();
		inviteAlert.show();
	}


		 private List<Contact> prepareContacts(){
			 HashSet<Contact> result = new HashSet<Contact>();
		        for(int position=0;position<cursor.getCount();position++){
		        	cursor.moveToPosition(position);
		        
		            String id = cursor.getString( cursor.getColumnIndex(mProjection[0]));
		            String name = cursor.getString( cursor.getColumnIndex(mProjection[1]));
		            String phone = cursor.getString( cursor.getColumnIndex(mProjection[2]));
		            String email = null;
		            ContentResolver cr = getActivity().getContentResolver();
		            Cursor emailCur = cr.query( 
		            		ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
		            		null,
		            		ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
		            		new String[]{id}, null); 
		            	while (emailCur.moveToNext()) { 
		            		if(email==null){
		            			email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		            		}
		             	} 
		             	emailCur.close();
		             	result.add(new Contact(Contact.ITEM, name, phone, email));
		        }
		        
		        result.addAll(getNameEmailDetails());
		        List<Contact> list = new ArrayList<Contact>();
		        list.addAll(result);
		        list = eliminateDuplicates(list);
		        Collections.sort(list);
             	cursor.close();
		        
				return list;
		 }
		 

		 public List<Contact> eliminateDuplicates(List<Contact> contacts) {
			 List<Contact> contactsList = new ArrayList<Contact>();
			 for(int i=0; i<contacts.size(); i++){
				 Contact iCon = contacts.get(i);
				 boolean flag = true;
				 for(int j=0; j<contacts.size(); j++){
					 if(i != j && i>j){
					 Contact jCon = contacts.get(j);
					
						 if(iCon.getEmail()!=null && jCon.getEmail()!=null){
							 if(iCon.getContactName().equals(jCon.getContactName()) && iCon.getEmail().equals(jCon.getEmail())){
								 flag = false;
								 break;
							 }
						 }
						 if(iCon.getContactNo()!=null && jCon.getContactNo()!=null){
							 if(iCon.getContactName().equals(jCon.getContactName()) && iCon.getContactNo().equals(jCon.getContactNo())){
								 flag = false;
								 break;
							 }
						 }
					 }
				 }
				 if(flag)
				 contactsList.add(iCon);
			 }
			 return contactsList;
		 }
		 
		 public List<Contact> getNameEmailDetails() {
			    List<Contact> emlRecs = new ArrayList<Contact>();
			    HashSet<String> emlRecsHS = new HashSet<String>();
			    Context context = getActivity();
			    ContentResolver cr = context.getContentResolver();
			    String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID, 
			            ContactsContract.Contacts.DISPLAY_NAME,
			            ContactsContract.Contacts.PHOTO_ID,
			            ContactsContract.CommonDataKinds.Email.DATA, 
			            ContactsContract.CommonDataKinds.Photo.CONTACT_ID };
			    String order = "CASE WHEN " 
			            + ContactsContract.Contacts.DISPLAY_NAME 
			            + " NOT LIKE '%@%' THEN 1 ELSE 2 END, " 
			            + ContactsContract.Contacts.DISPLAY_NAME 
			            + ", " 
			            + ContactsContract.CommonDataKinds.Email.DATA
			            + " COLLATE NOCASE";
			    String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
			    Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
			    if (cur.moveToFirst()) {
			        do {
			            // names comes in hand sometimes
			            String name = cur.getString(1);
			            String emlAddr = cur.getString(3);
			            String id = cur.getString(0);
			            // keep unique only
			            if (emlRecsHS.add(emlAddr.toLowerCase())) {
			            	Contact contact = new Contact(Contact.ITEM, name, null, emlAddr.toLowerCase());
			                emlRecs.add(contact);
			            }
			        } while (cur.moveToNext());
			    }

			    cur.close();
			    return emlRecs;
			}
		 
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
		getActivity().finish();
	}

}
