package com.entradahealth.entrada.android.app.personal.activities.inbox.adapters;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.Environment;
import com.entradahealth.entrada.android.app.personal.EnvironmentHandlerFactory;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.NewMessageFragment;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.android.app.widget.BaseSwipeListViewListener;
import com.entradahealth.entrada.android.app.widget.SwipeListView;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBConversationHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTQBUserHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectWriter;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;

public class ConversationsListAdapter extends BaseAdapter {

	private List<ENTConversation> conversations;
	private LayoutInflater inflater;
	private Activity activity;
	private int currentItem = -1;
	private SwipeListView listView;
	private int previousItem = -1;
	private EntradaApplication application;
	private APIService service;

	public ConversationsListAdapter(Context context,
			List<ENTConversation> _conversations, SwipeListView _listView, final FragmentManager manager) {
		this.activity = (Activity) context;
		application = (EntradaApplication) EntradaApplication.getAppContext();
		EnvironmentHandlerFactory envFactory = EnvironmentHandlerFactory.getInstance();
		Environment env = envFactory.getHandler(application.getStringFromSharedPrefs("environment"));
		try {
			service = new APIService(env.getApi());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conversations = new ArrayList<ENTConversation>();
		conversations.clear();
		conversations.addAll(_conversations);
		this.inflater = LayoutInflater.from(context);
		this.listView = _listView;
		this.listView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));                
                activity.findViewById(R.id.moreoptions).setVisibility(View.GONE);
               	previousItem = currentItem;
               	currentItem = position;
               	if(previousItem != -1 && previousItem != currentItem)
                  listView.closeAnimate(previousItem);
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
                activity.findViewById(R.id.moreoptions).setVisibility(View.GONE);
               	previousItem = currentItem;
               	currentItem = position;
               	if(previousItem != -1 && previousItem != currentItem)
                  listView.closeAnimate(previousItem);
            }

            @Override
            public void onClickFrontView(int position) {
                super.onClickFrontView(position);
                Log.d("swipe", String.format("onClickFrontView %d", position));
               	previousItem = currentItem;
               	currentItem = position;
    			closePreviousRow();			     
        		Bundle b = new Bundle();
        		b.putSerializable("conversation", conversations.get(position));
        		//b.putString("patient_name", (conversations.get(position).getName().equals("NO NAME")) ? null :conversations.get(position).getName());
        		b.putString("patient_name", null);
            	BundleKeys.CURRENT_CONVERSATION = conversations.get(position);
        		NewMessageFragment msgFragment = new NewMessageFragment();
        		msgFragment.setArguments(b);
        		FragmentTransaction ft = manager.beginTransaction().addToBackStack(null);
        		ft.replace(R.id.fragcontent, msgFragment, "messages");
        		ft.commit();

            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
            }

        });
	}

	public void closePreviousRow() {
		if (previousItem != -1 && previousItem != currentItem)
			listView.closeAnimate(previousItem);
	}
	
	public class DelClickListener implements OnClickListener {

		int position;

		public DelClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			if(application.is3GOr4GConnected() || application.isWifiConnected()) {
				listView.closeAnimate(position);
				new DeleteConversationTask(conversations.get(position)).execute();
				conversations.remove(position);
				setConversations(conversations);
			}
		}

	}
	
	class DeleteConversationTask extends AsyncTask{
		
		ENTConversation conversation;
		
		DeleteConversationTask(ENTConversation conversation){
			this.conversation = conversation;
		}

		@Override
		protected Object doInBackground(Object... params) {
			ENTHandlerFactory handlerFactory = ENTHandlerFactory.getInstance();
			ENTHandler handler = handlerFactory.getHandler(ENTHandlerFactory.QBCONVERSATION);
			((ENTQBConversationHandler) handler).deleteDialog(conversation);
			return null;
		}
		
	}

	public class MoreClickListener implements OnClickListener {

		int position;

		public MoreClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			activity.findViewById(R.id.moreoptions).setVisibility(View.VISIBLE);
			activity.findViewById(R.id.clearConversation).setOnClickListener(new ClearConversationClickListener(position));
		}

	}

	public class ClearConversationClickListener implements OnClickListener {

		int position;

		public ClearConversationClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			listView.closeAnimate(position);
			activity.findViewById(R.id.moreoptions).setVisibility(View.GONE);
		}

	}

	
	public void setConversations(List<ENTConversation> conversations) {
		this.conversations = conversations;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return conversations.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return conversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	private class ViewHolder {
		TextView lastmessageDate;
		TextView recepient;
		TextView unreadMessagesCount;
		TextView lastmessage;

		Button delete;
		Button more;
		ImageView patientImage;
		TextView patientName;
		TextView msgType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int contentType = getItemViewType(position);

		if (convertView == null)
			convertView = getConvertView(contentType, position);

		ViewHolder holder = (ViewHolder) convertView.getTag();
		updateView(contentType, position, holder);
		return convertView;
	}

	private View getConvertView(int contentType, int position) {
		View view = null;
		ViewHolder holder = new ViewHolder();
		if (contentType == 0) {
			view = inflater.inflate(R.layout.nonpatient_conversationitem, null);
			holder.msgType = (TextView) view.findViewById(R.id.msgtype);
		} else {
			view = inflater.inflate(R.layout.patient_conversationitem, null);
			holder.patientImage = (ImageView) view
					.findViewById(R.id.patientImage);
			holder.patientName = (TextView) view.findViewById(R.id.patientName);
		}
		holder.lastmessage = (TextView) view.findViewById(R.id.lastmessage);
		holder.recepient = (TextView) view.findViewById(R.id.recepient);
		holder.unreadMessagesCount = (TextView) view
				.findViewById(R.id.unreadMessagesCount);
		holder.delete = (Button) view.findViewById(R.id.btn_delete);
		holder.more = (Button) view.findViewById(R.id.btn_more);
		holder.lastmessageDate= (TextView) view.findViewById(R.id.lastmessageDate); 
		view.setTag(holder);
		return view;
	}

	private void updateView(int contentType, int position, ViewHolder holder) {
		ENTConversation c = conversations.get(position);
		if (contentType == 1) {
			UserState state = AndroidState.getInstance().getUserState();
			Account account = state.getCurrentAccount();
			Patient patient = null;
			if(account != null) {
				DomainObjectReader reader = state.getProvider(account);
				patient = reader.getPatient(c.getPatientID());
				if(patient==null){
					new GetPatientDemographicInfo(c.getPatientID(), c).execute();
				}
				//String recipient_name = (patient!=null && patient.getFullName()!=null) ? patient.getFullName() : String.valueOf(c.getPatientID());
				String recipient_name = (patient!=null) ? (patient.getFirstName() + " " + patient.getLastName()) : "Loading Patient..";
				holder.patientName.setText(recipient_name);
			} else {
				new GetPatientDemographicInfo(c.getPatientID(),c, holder).execute();	
			}
		} 	
		holder.lastmessage.setText(c.getLastMessage());
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		String[] recepient = removeElements(c.getOccupantsIds(), application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID));
		if(contentType == 0){
			if(recepient.length>1) {
				holder.msgType.setText("Group Message");
			} else {
				holder.msgType.setText("Message");
			}
		}
		
		StringBuffer recipient = new StringBuffer();
		if(recepient.length>0){
			ENTUser user = getENTUserFromId(recepient[0]);
			recipient.append(user == null? recepient[0] : user.getName());
			if(recepient.length>1){
				recipient.append(" +"+(recepient.length-1));
			}
		}
		holder.recepient.setText(recipient);
		if (c.getUnreadMessagesCount() == 0) {
			holder.unreadMessagesCount.setVisibility(View.GONE);
		} else {
			holder.unreadMessagesCount.setVisibility(View.VISIBLE);
			holder.unreadMessagesCount.setText(String.valueOf(c.getUnreadMessagesCount()));
		}
		java.util.Date dateTime=new java.util.Date((long)c.getLastMessageDateSent()*1000);
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
		String formattedDate = formatter.format(dateTime);
		holder.lastmessageDate.setText(formattedDate);
		holder.delete.setOnClickListener(new DelClickListener(position));
		holder.more.setOnClickListener(new MoreClickListener(position));
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return (conversations.get(position).getPatientID()==0) ? 0 : 1; 
	}

	public View getViewByPosition(int pos, ListView listView) {
	    final int firstListItemPosition = listView.getFirstVisiblePosition();
	    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

	    if (pos < firstListItemPosition || pos > lastListItemPosition ) {
	        return listView.getAdapter().getView(pos, null, listView);
	    } else {
	        final int childIndex = pos - firstListItemPosition;
	        return listView.getChildAt(childIndex);
	    }
	}
	
	public ENTConversation getConversationById(String id){
		for(ENTConversation con : conversations){
			if(con.getId().equals(id)){
				return con;
			}
		}
		return null;
	}
	
	public ENTUser getENTUserFromId(String id){
		for(ENTUser user : BundleKeys.QB_Users){
			if(user.getId().equals(id)){
				return user;
			}
		}
		new GetUserTask(id).execute();
		return null;		
	}
	
	public class GetPatientDemographicInfo extends AsyncTask{

		private long patientId;
		private ViewHolder viewHolder;
		private String patientName;
		private ENTConversation conversation;
		
		GetPatientDemographicInfo(long patientId, ENTConversation conversation){
			this.patientId = patientId;
			this.conversation = conversation;
		}
		
		public GetPatientDemographicInfo(long patientId, ENTConversation conversation, ViewHolder viewHolder) {
			this.patientId = patientId;
			this.conversation = conversation;
			this.viewHolder = viewHolder;
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			UserState state = AndroidState.getInstance().getUserState();
			Account account = state.getCurrentAccount();
			if(account!=null){
	            DomainObjectReader reader = state.getProvider(account);
				Patient p = reader.getPatientDemographicInfo(patientId);
				if(p==null){
					getPatientDemographicInfo();
				}
			} else {
				getPatientDemographicInfo();
			}
			return null;
		}

		private void getPatientDemographicInfo() {
			try {
				String response = service.getDemographicInfo(application.getStringFromSharedPrefs(BundleKeys.SESSION_TOKEN), conversation.getId(), patientId);
				JSONObject json = new JSONObject(response);
				patientName = json.getString("FirstName")+ " " +json.getString("LastName");
			} catch (JSONException e) {
				e.printStackTrace();
				patientName = String.valueOf(patientId);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(viewHolder!=null) {
				viewHolder.patientName.setText(patientName);
			}	

		}
	}
	
	public class GetUserTask extends AsyncTask{

		String userId;
		
		public GetUserTask(String userId){
			this.userId = userId;
		}
		
		@Override
		protected Object doInBackground(Object... arg0) {
			ENTHandlerFactory factory= ENTHandlerFactory.getInstance();
			ENTHandler handler = factory.getHandler(ENTHandlerFactory.QBUSER);
			ENTUser user = ((ENTQBUserHandler) handler).getUser(userId);
			UserState state = AndroidState.getInstance().getUserState();
			EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
			try {
				state.setSMUser();
			} catch (DomainObjectWriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (AccountException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidPasswordException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			SMDomainObjectWriter writer = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
			try {
				writer.addBuddy(user);
			} catch (DomainObjectWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			notifyDataSetChanged();
		}
		
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
}
