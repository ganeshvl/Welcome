package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.Contact;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.auth.exceptions.AccountException;
import com.entradahealth.entrada.core.auth.exceptions.InvalidPasswordException;
import com.entradahealth.entrada.core.domain.exceptions.DomainObjectWriteException;
import com.entradahealth.entrada.core.inbox.dao.ENTHandler;
import com.entradahealth.entrada.core.inbox.dao.ENTHandlerFactory;
import com.entradahealth.entrada.core.inbox.dao.ENTQBBuddyHandler;
import com.entradahealth.entrada.core.inbox.domain.providers.SMDomainObjectReader;
import com.entradahealth.entrada.core.remote.APIService;
import com.entradahealth.entrada.core.remote.exceptions.ServiceException;
import com.hb.views.PinnedSectionListView;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;

public class SMContacts extends Fragment{
	
	private PinnedSectionListView lvAllContacts, lvFavorites;
	private Button all, favorites;
	private AllContactsListAdapter mAllAdapter;
	private FavoritesListAdapter mFavoritesAdapter;
	private Account currentAccount;
	private String m_androidId;
	private Set<Contact> allUsers = new HashSet<Contact>();
	private HashSet<String> setFavorites;
	private HashSet<String> set;
	private EditText etSearch;
	private boolean isAllSelected = true;
	private ActionMode actionMode;
	private EntradaApplication application;
	private String patient_name = null;
	private long patient_id;
	private boolean fromRecordingScreen = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		BundleKeys.fromSecureMessaging = false;
		application = (EntradaApplication) EntradaApplication.getAppContext();
		m_androidId = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID);
		Bundle bundle = this.getArguments();
		if(bundle != null){
			patient_name = bundle.getString("patient_name"); 
			patient_id = bundle.getLong("patient_id");
			fromRecordingScreen = bundle.getBoolean("fromRecordingScreen");
		}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	
    	View view = inflater.inflate(R.layout.inbox_contacts, container, false);
		lvAllContacts = (PinnedSectionListView)view.findViewById(R.id.lvAllContacts);
		lvFavorites = (PinnedSectionListView)view.findViewById(R.id.lvFavorites);
		lvAllContacts.setFastScrollEnabled(true);
		lvFavorites.setFastScrollEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            lvAllContacts.setFastScrollAlwaysVisible(true);
            lvFavorites.setFastScrollAlwaysVisible(true);
        }
		all = (Button) view.findViewById(R.id.allcontacts);
		favorites = (Button) view.findViewById(R.id.favcontacts);
		etSearch = (EditText)view.findViewById(R.id.etSearch);
		all.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				lvAllContacts.setVisibility(View.VISIBLE);
				lvFavorites.setVisibility(View.GONE);
				favorites.setBackgroundResource(R.drawable.search_bg_round);
				all.setBackgroundResource(R.drawable.grouptypes_background);
				isAllSelected = true;
				etSearch.setText("");
			}
		});
		favorites.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				lvAllContacts.setVisibility(View.GONE);
				lvFavorites.setVisibility(View.VISIBLE);
				all.setBackgroundResource(R.drawable.search_bg_round);
				favorites.setBackgroundResource(R.drawable.grouptypes_background);
				isAllSelected = false;
				if(actionMode!=null) {
					actionMode.finish();
				}
				etSearch.setText("");
			}
		});
    	return view;
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
		allUsers.clear();
		etSearch.setText("");
		LoadQBUsers task = new LoadQBUsers();
		task.execute();
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_contacts, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	      case R.id.item_new_msg:
	    	  if(application.is3GOr4GConnected() || application.isWifiConnected()){
	    	  	getFragmentManager().beginTransaction().replace(R.id.fragcontent, new InviteContacts(), "phonecontacts").addToBackStack(null).commit();
	    	  }
	    	  return true;
	      default:
	         return super.onOptionsItemSelected(item);
	   }
	}

	class LoadQBUsers extends AsyncTask{

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			try {
				EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
				if(BundleKeys.QB_Users == null) {
					UserState state = AndroidState.getInstance().getUserState();
					SMDomainObjectReader reader = state.getSMProvider(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_LOGIN));
					BundleKeys.QB_Users = new ArrayList<ENTUser>();
					BundleKeys.QB_Users.addAll(reader.getBuddies());
					BundleKeys.QB_Users.addAll(reader.getPendingInvites());
				}
				for (ENTUser user : BundleKeys.QB_Users) {
					if(!application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID).equals(user.getId())){
						Contact c = new Contact(Contact.ITEM, user.getName(), user.getId());
						allUsers.add(c);
					}
				}
			} catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			setAdapters();
		}
	}
	
	protected ArrayList<Contact> sortUsers(ArrayList<Contact> _users){
		ArrayList<Contact> localList = new ArrayList<Contact>();
		Collections.sort(_users);
		char checkChar = ' ';
		for (Contact contact: _users) {
			if(contact.getContactName().length()>0){
				char ch = contact.getContactName().charAt(0);
				if((Character.toUpperCase(checkChar) != Character.toUpperCase(ch)) || (checkChar == ' ' && ch == ' ')){
					Contact cn = new Contact(Contact.SECTION, String.valueOf(ch), "");
					localList.add(cn);
					checkChar = ch;
				} 
				localList.add(contact);
			}
		}
		
		return localList;
	}
	
	public void performOnContactClick(AdapterView<?> parent, int position) {
		Contact it = (Contact)parent.getItemAtPosition(position);
		String sel_recipient = it.getContactName(); 
		String sel_no = it.getContactNo();
		Bundle b = new Bundle();
		b.putString("recipient_name", sel_recipient);
		b.putString("recipient_id", sel_no);
		b.putString("patient_name", patient_name);
		b.putLong("patient_id", patient_id);
		b.putBoolean("fromRecordingScreen", fromRecordingScreen);
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		if(application.is3GOr4GConnected() || application.isWifiConnected()) {
    		if(sel_no!=null && !sel_no.isEmpty()) {
    			NewMessageFragment msgFragment = new NewMessageFragment();
    			msgFragment.setArguments(b);
    			getFragmentManager().beginTransaction().replace(R.id.fragcontent, msgFragment, null).addToBackStack(null).commit();
    		}
		}
	}
	
	public void setAdapters(){
		Set<Contact> _allUsers = allUsers;
		lvAllContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lvFavorites.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		if(getActivity()!=null && allUsers!=null){
			Set<Contact> _users = new HashSet<Contact>();
			String searchText = etSearch.getText().toString();
			if (searchText != null && !searchText.isEmpty() && allUsers != null) {
				Iterator itr = allUsers.iterator();
				while (itr.hasNext()) {
					Contact item = (Contact) itr.next();
					if (item.text.toLowerCase().contains(searchText)) {
						_users.add(item);
					}
				}
				_allUsers = _users;
			}
		mAllAdapter = new AllContactsListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(new ArrayList<Contact>(_allUsers)));
		lvAllContacts.setAdapter(mAllAdapter);
		mFavoritesAdapter = new FavoritesListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(getFavorites()));
		lvFavorites.setAdapter(mFavoritesAdapter);
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int start, int before,
					int count) {
				if(actionMode!=null){
					actionMode.finish();
				}
				ArrayList<Contact> favourites = getFavorites();
				String searchText = charSequence.toString().toLowerCase();
				if(mAllAdapter!=null && mFavoritesAdapter!=null) {
					if(isAllSelected){
						 Set<Contact> _users = new HashSet<Contact>();
						 if(charSequence != null && allUsers!=null) {
				             int length=allUsers.size();
				             Iterator itr = allUsers.iterator();
				             while(itr.hasNext()){
				            	 Contact item = (Contact) itr.next();
				            	 	if(item.text.toLowerCase().contains(searchText)){
				            	 		_users.add(item);
				            		}
				            	 }
				             }
							mAllAdapter = new AllContactsListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(new ArrayList<Contact>(_users)));
							lvAllContacts.setFastScrollEnabled(false);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					            lvAllContacts.setFastScrollAlwaysVisible(false);
					        }
							lvAllContacts.setAdapter(mAllAdapter);
							lvAllContacts.setFastScrollEnabled(true);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					            lvAllContacts.setFastScrollAlwaysVisible(true);
					        }
							mAllAdapter.notifyDataSetChanged();
					} else {
						 ArrayList<Contact> _users = new ArrayList<Contact>();
						 if(charSequence != null && favourites!=null) {
				             int length=favourites.size();
				             Iterator itr = favourites.iterator();
				             while(itr.hasNext()){
				            	 Contact item = (Contact) itr.next();
				            	 	if(item.text.toLowerCase().contains(searchText)){
				            	 		_users.add(item);
				            		}
				            	 }
				             }
						mFavoritesAdapter = new FavoritesListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(_users));
						lvFavorites.setFastScrollEnabled(false);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				            lvFavorites.setFastScrollAlwaysVisible(false);
				        }
	    				lvFavorites.setAdapter(mFavoritesAdapter);
						lvFavorites.setFastScrollEnabled(true);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				            lvFavorites.setFastScrollAlwaysVisible(true);
				        }
						mFavoritesAdapter.notifyDataSetChanged();
					}					
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
	    });
		
		lvAllContacts.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
        		//performOnContactClick(parent, position);
			}


	      });
		lvFavorites.setOnItemClickListener(new OnItemClickListener() {
        	@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
        		//performOnContactClick(parent, position);
			}
	      });
        
        lvAllContacts.setMultiChoiceModeListener(new MultiChoiceModeListener() {
    		private int nr = 0;
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				nr = 0;
				actionMode = mode;
				MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_contact_list, menu);
                return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				actionMode = mode;
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				actionMode = mode;
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
				switch (item.getItemId()) {
			
                case R.id.item_sel_done:
                    nr = 0;
                    for(int i=0;i<mAllAdapter.mSelectedPersons.size();i++){
                    	Log.e("sel-per", mAllAdapter.mSelectedPersons.valueAt(i));
                    }
                    mAllAdapter.clearSelection();
                    mode.finish();
                    break;
                case R.id.item_create_group:
                	ArrayList<Contact> contacts = new ArrayList<Contact>();
                    for(int i=0;i<mAllAdapter.mSelection.size();i++){
                    	if(mAllAdapter.mSelection.valueAt(i) == true){
                    		int j = mAllAdapter.mSelection.keyAt(i);
                    		Contact it = mAllAdapter.getItem(j);
                    		contacts.add(it);
                    	}
                    }
                    mode.finish();
	        		Bundle b = new Bundle();
	        		b.putParcelableArrayList("contacts", contacts);
	        		b.putString("patient_name", patient_name);
	        		b.putLong("patient_id", patient_id);
	        		b.putBoolean("fromRecordingScreen", fromRecordingScreen);
	        		FragmentTransaction ft = getFragmentManager().beginTransaction();
	        		if(fromRecordingScreen) {
		        		Fragment frg = getFragmentManager().findFragmentByTag("contacts");
		        		ft.remove(frg);
	        		} else {
	        			getFragmentManager().popBackStack();
	        		}
	        		NewMessageFragment msgFragment = new NewMessageFragment();
	        		msgFragment.setArguments(b);
	        		ft.replace(R.id.fragcontent, msgFragment, null).addToBackStack(null).commit();
                	break;
                case R.id.item_add_to_favorite:
                	nr = 0;
                	set = application.getStringSetFromSharedPrefs("MyFavorites");
                	if(set == null)
                		set = new HashSet<String>();
                	ArrayList<Contact> list_contacts = new ArrayList<Contact>();
                    for(int i=0;i<mAllAdapter.mSelection.size();i++){
                    	if(mAllAdapter.mSelection.valueAt(i) == true){
                    		int j = mAllAdapter.mSelection.keyAt(i);
                    		Contact it = mAllAdapter.getItem(j);
                    		list_contacts.add(it);
                    	}
                    }
                    
                    for(int i=0;i<list_contacts.size();i++){
                    	set.add(list_contacts.get(i).getAll());
                    }
                    
                    application.setStringSetIntoSharedPrefs("MyFavorites", set);
                    
                	mAllAdapter.clearSelection();
                	mAllAdapter.clearSelectedPersons();
                    mode.finish();
                    
        			lvFavorites.setVisibility(View.VISIBLE);
        			
        			
    				mFavoritesAdapter = new FavoritesListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(getFavorites()));
    				lvFavorites.setFastScrollEnabled(false);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			            lvFavorites.setFastScrollAlwaysVisible(false);
			        }
    				lvFavorites.setAdapter(mFavoritesAdapter);
					lvFavorites.setFastScrollEnabled(true);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			            lvFavorites.setFastScrollAlwaysVisible(true);
			        }
    				lvFavorites.setVisibility(View.VISIBLE);
    				lvAllContacts.setVisibility(View.GONE);
    				favorites.setBackgroundResource(R.drawable.grouptypes_background);
    				all.setBackgroundResource(R.drawable.search_bg_round);
    				isAllSelected = false;
				}
				}
				return false;
			}			

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				mAllAdapter.clearSelection();
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// TODO Auto-generated method stub
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					if(!mAllAdapter.getItem(position).getContactName().contains("Pending")){
					if (checked) {
	                    nr++;
	                    mAllAdapter.setNewSelection(position, checked);  
	                    mAllAdapter.setNewPerson(position, mAllAdapter.getItem(position).getContactName());
	                    mAllAdapter.setNumber(position, mAllAdapter.getItem(position).getContactNo());
	                } else {
	                    nr--;
	                    mAllAdapter.removeSelection(position);
	                    mAllAdapter.removeNewPerson(position);
	                    mAllAdapter.removeNumber(position);
	                }
					mode.setTitle(Integer.toString(nr));
					}
				}
				if(nr == 0)
					mode.finish();
				
				/*if(nr == 1)
					mode.setTitle(nr + " contact selected");
				else
					mode.setTitle(nr + " contacts selected");*/
			}
            
    	});
        
        lvFavorites.setMultiChoiceModeListener(new MultiChoiceModeListener() {
    		private int nr = 0;
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				nr = 0;
				actionMode = mode;
				MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.fav_menu_contact_list, menu);
                return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				actionMode = mode;
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// TODO Auto-generated method stub
				actionMode = mode;
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
				switch (item.getItemId()) {
                
                case R.id.item_sel_done:
                    nr = 0;
                    for(int i=0;i<mFavoritesAdapter.mSelectedPersons.size();i++){
                    	Log.e("sel-per", mFavoritesAdapter.mSelectedPersons.valueAt(i));
                    }
                    mFavoritesAdapter.clearSelection();
                    mode.finish();
                    break;
                case R.id.item_create_group:
                	ArrayList<Contact> contacts = new ArrayList<Contact>();
                    for(int i=0;i<mFavoritesAdapter.mSelection.size();i++){
                    	if(mFavoritesAdapter.mSelection.valueAt(i) == true){
                    		int j = mFavoritesAdapter.mSelection.keyAt(i);
                    		Contact it = mFavoritesAdapter.getItem(j);
                    		contacts.add(it);
                    	}
                    }
                    mode.finish();
	        		Bundle b = new Bundle();
	        		b.putParcelableArrayList("contacts", contacts);
	        		b.putString("patient_name", patient_name);
	        		b.putLong("patient_id", patient_id);
	        		b.putBoolean("fromRecordingScreen", fromRecordingScreen);
	        		FragmentTransaction ft = getFragmentManager().beginTransaction();
	        		if(fromRecordingScreen) {
		        		Fragment frg = getFragmentManager().findFragmentByTag("contacts");
		        		ft.remove(frg);
	        		} else {
	        			getFragmentManager().popBackStack();
	        		}
	        		NewMessageFragment msgFragment = new NewMessageFragment();
	        		msgFragment.setArguments(b);
	        		ft.replace(R.id.fragcontent, msgFragment, null).addToBackStack(null).commit();
                	break;
                	
                case R.id.item_remove_contacts:
                	set = application.getStringSetFromSharedPrefs("MyFavorites");
                	ArrayList<Contact> list_contacts = new ArrayList<Contact>();
                    for(int i=0;i<mFavoritesAdapter.mSelection.size();i++){
                    	if(mFavoritesAdapter.mSelection.valueAt(i) == true){
                    		int j = mFavoritesAdapter.mSelection.keyAt(i);
                    		Contact it = mFavoritesAdapter.getItem(j);
                    		list_contacts.add(it);
                    	}
                    }
                    
                    for(int i=0;i<list_contacts.size();i++){
                    	set.remove(list_contacts.get(i).getAll());
                    }
                    
                    application.setStringSetIntoSharedPrefs("MyFavorites", set);
                    mFavoritesAdapter.clearSelection();
                    mFavoritesAdapter.clearSelectedPersons();
                    mFavoritesAdapter = new FavoritesListAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, sortUsers(getFavorites()));
                    lvFavorites.setAdapter(mFavoritesAdapter);
                    mode.finish();
                	break;
				}
				}
				return false;
			}			

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				mFavoritesAdapter.clearSelection();
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// TODO Auto-generated method stub
				if(application.is3GOr4GConnected() || application.isWifiConnected()) {
					if (checked) {
	                    nr++;
	                    mFavoritesAdapter.setNewSelection(position, checked);  
	                    mFavoritesAdapter.setNewPerson(position, mFavoritesAdapter.getItem(position).getContactName());
	                    mFavoritesAdapter.setNumber(position, mFavoritesAdapter.getItem(position).getContactNo());
	                } else {
	                    nr--;
	                    mFavoritesAdapter.removeSelection(position);
	                    mFavoritesAdapter.removeNewPerson(position);
	                    mFavoritesAdapter.removeNumber(position);
	                }
					/*if(nr == 1)
						mode.setTitle(nr + " contact selected");
					else
						mode.setTitle(nr + " contacts selected");*/
					mode.setTitle(Integer.toString(nr));
				}
				if(nr == 0)
					mode.finish();
			}
            
    	});	        
	}
	} 
	
	/*
	 * Custom section header adapter for all contacts
	 */
	class AllContactsListAdapter extends ArrayAdapter<Contact> implements PinnedSectionListAdapter, SectionIndexer, Filterable {
		HashMap<String, Integer> alphaIndexer;
	    String[] sections;
		private SparseBooleanArray mSelection = new SparseBooleanArray();
		private SparseArray<String> mSelectedPersons = new SparseArray<String>();
		private SparseArray<String> mSelectedNumbers = new SparseArray<String>();
		ArrayList<Contact> list_selected_contacts;
		List<Contact> objects;
		  public AllContactsListAdapter(Context context, int resource, int textViewResourceId, List<Contact> objects) {
			  super(context, resource, textViewResourceId, objects);
			  list_selected_contacts = new ArrayList<Contact>();
			  this.objects = objects;
			  for(int i=0;i<list_selected_contacts.size();i++){
				  list_selected_contacts.add(i, null);
			  }
			  Log.e("list_selected_contacts", Integer.toString(list_selected_contacts.size()));
			  
			  alphaIndexer = new HashMap<String, Integer>();
			  int size = objects.size();
			  for (int x = 0; x < size; x++) {
				  Contact c = objects.get(x);
	 
			// get the first letter of the store
	                String ch =  c.text.substring(0, 1);
			// convert to uppercase otherwise lowercase a -z will be sorted after upper A-Z
	                ch = ch.toUpperCase();
	 
			// HashMap will prevent duplicates
	                alphaIndexer.put(ch, x);
	            }
	 
	            Set<String> sectionLetters = alphaIndexer.keySet();
	 
		    // create a list from the set to sort
	            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters); 
	 
	            Collections.sort(sectionList);
	 
	            sections = new String[sectionList.size()];
	 
	            sectionList.toArray(sections);
		  }
		  
		  public void setNewSelection(int position, boolean value) {
	            mSelection.put(position, value);
	            notifyDataSetChanged();
	        }
		  
		  public void removeSelection(int position) {
	            mSelection.put(position, false);
	            notifyDataSetChanged();
	      }
		  
		  public void clearSelection() {
	          mSelection = new SparseBooleanArray();
	          notifyDataSetChanged();
	      }
		  
		  public void setNewPerson(int position, String name){
			  mSelectedPersons.put(position, name);
		  }

		  public void removeNewPerson(int position){
			  mSelectedPersons.remove(position);
		  }
		  
		  public void clearSelectedPersons() {
	          mSelectedPersons = new SparseArray<String>();
	          notifyDataSetChanged();
	      }
		  
		  public void setNumber(int position, String name){
			  mSelectedNumbers.put(position, name);
		  }

		  public void removeNumber(int position){
			  mSelectedNumbers.remove(position);
		  }
		  
		  public void clearNumbers() {
			  mSelectedNumbers = new SparseArray<String>();
	          notifyDataSetChanged();
	      }

		  
		  @Override 
		  public View getView(int position, View convertView, ViewGroup parent) {
			  View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
		  TextView view = (TextView) super.getView(position, convertView, parent);
		  v.setBackgroundColor(getResources().getColor(R.color.unselected_list_item)); //default color
          if (mSelection.get(position)) {
              v.setBackgroundColor(getResources().getColor(R.color.selected_list_item));// this is a selected position so make it red
          }
		  if (getItem(position).type == Contact.SECTION) {
			  v.setBackgroundColor(parent.getResources().getColor(R.color.dark_gray));
		  }
		  if(getItem(position).text.contains("(Pending)")){
			  ((TextView) v).setTextColor(Color.parseColor("#B8B8B8"));
		  } else {
			  ((TextView) v).setTextColor(Color.parseColor("#000000"));
		  }
		  
		  	return v;
		  
		  }
		  
		  @Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			  if (getItem(position).type == Contact.SECTION)
				  return false;
			  else 
				  return true;
		}
		  
		  @Override 
		  public int getViewTypeCount() {
			  return 2;
		  }
		  
		  @Override 
		  public int getItemViewType(int position) {
			  return objects.get(position).type;
			  
		  }

		  @Override 
		  public boolean isItemViewTypePinned(int viewType) {
			  return viewType == Contact.SECTION;
		  }
		  
		  @Override
		public Contact getItem(int position) {
			// TODO Auto-generated method stub
			return objects.get(position);
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return sections;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			// TODO Auto-generated method stub
			if(sections.length > 1 )
				try{
					return alphaIndexer.get(sections[sectionIndex]);
				} catch(Exception ex){
					return alphaIndexer.get(sections[sections.length-1]);
				}
			else
				try {
					return alphaIndexer.get(sections[sectionIndex-1]);
				} catch(Exception ex){
					return alphaIndexer.get(sections[sections.length-1]);
				}
		}

		@Override
		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			 return 1;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return objects.size();
		}
		  
	}

	
	class FavoritesListAdapter extends ArrayAdapter<Contact> implements PinnedSectionListAdapter, SectionIndexer, Filterable {
		HashMap<String, Integer> alphaIndexer;
	    String[] sections;  
		private SparseBooleanArray mSelection = new SparseBooleanArray();
		private SparseArray<String> mSelectedPersons = new SparseArray<String>();
		private SparseArray<String> mSelectedNumbers = new SparseArray<String>();
		ArrayList<Contact> list_selected_contacts;
		List<Contact> objects;
		  public FavoritesListAdapter(Context context, int resource, int textViewResourceId, List<Contact> objects) {
			  super(context, resource, textViewResourceId, objects);
			  this.objects = objects;
			  list_selected_contacts = new ArrayList<Contact>();
			  for(int i=0;i<list_selected_contacts.size();i++){
				  list_selected_contacts.add(i, null);
			  }
			  Log.e("list_selected_contacts", Integer.toString(list_selected_contacts.size()));
			  
			  alphaIndexer = new HashMap<String, Integer>();
			  int size = objects.size();
			  for (int x = 0; x < size; x++) {
				  Contact c = objects.get(x);
	 
			// get the first letter of the store
	                String ch =  c.text.substring(0, 1);
			// convert to uppercase otherwise lowercase a -z will be sorted after upper A-Z
	                ch = ch.toUpperCase();
	 
			// HashMap will prevent duplicates
	                alphaIndexer.put(ch, x);
	            }
	 
	            Set<String> sectionLetters = alphaIndexer.keySet();
	 
		    // create a list from the set to sort
	            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters); 
	 
	            Collections.sort(sectionList);
	 
	            sections = new String[sectionList.size()];
	 
	            sectionList.toArray(sections);
		  }
		  
		  public void setNewSelection(int position, boolean value) {
	            mSelection.put(position, value);
	            notifyDataSetChanged();
	        }
		  
		  public void removeSelection(int position) {
	            mSelection.put(position, false);
	            notifyDataSetChanged();
	      }
		  
		  public void clearSelection() {
	          mSelection = new SparseBooleanArray();
	          notifyDataSetChanged();
	      }
		  
		  public void setNewPerson(int position, String name){
			  mSelectedPersons.put(position, name);
		  }

		  public void removeNewPerson(int position){
			  mSelectedPersons.remove(position);
		  }
		  
		  public void clearSelectedPersons() {
	          mSelectedPersons = new SparseArray<String>();
	          notifyDataSetChanged();
	      }
		  
		  public void setNumber(int position, String name){
			  mSelectedNumbers.put(position, name);
		  }

		  public void removeNumber(int position){
			  mSelectedNumbers.remove(position);
		  }
		  
		  public void clearNumbers() {
			  mSelectedNumbers = new SparseArray<String>();
	          notifyDataSetChanged();
	      }

		  
		  @Override 
		  public View getView(int position, View convertView, ViewGroup parent) {
			  View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
		  TextView view = (TextView) super.getView(position, convertView, parent);
		  v.setBackgroundColor(getResources().getColor(R.color.unselected_list_item)); //default color
          if (mSelection.get(position)) {
              v.setBackgroundColor(getResources().getColor(R.color.selected_list_item));// this is a selected position so make it red
          }
		  if (getItem(position).type == Contact.SECTION) {
			  v.setBackgroundColor(parent.getResources().getColor(R.color.dark_gray));
		  }
		  if(getItem(position).text.contains("(Pending)")){
			  ((TextView) v).setTextColor(Color.parseColor("#B8B8B8"));
		  } else {
			  ((TextView) v).setTextColor(Color.parseColor("#000000"));
		  }
		  	return v;
		  
		  }
		  
		  @Override
		public boolean isEnabled(int position) {
			// TODO Auto-generated method stub
			  if (getItem(position).type == Contact.SECTION)
				  return false;
			  else 
				  return true;
		}
		  
		  @Override 
		  public int getViewTypeCount() {
			  return 2;
		  }
		  
		  @Override 
		  public int getItemViewType(int position) {
			  return objects.get(position).type;
		  }

		  @Override 
		  public boolean isItemViewTypePinned(int viewType) {
			  return viewType == Contact.SECTION;
		  }
		  
		  @Override
		public Contact getItem(int position) {
			// TODO Auto-generated method stub
			return objects.get(position);
		}

		@Override
		public Object[] getSections() {
			// TODO Auto-generated method stub
			return sections;
		}

		@Override
		public int getPositionForSection(int sectionIndex) {
			// TODO Auto-generated method stub
			//return alphaIndexer.get(sections[sectionIndex]);
			if(sections.length > 1 )
				try {
					return alphaIndexer.get(sections[sectionIndex]);
				} catch(Exception ex){
					return alphaIndexer.get(sections[sections.length-1]);
				}
			else
				try {
					return alphaIndexer.get(sections[sectionIndex-1]);
				} catch(Exception ex){
					return alphaIndexer.get(sections[sections.length-1]);
				}
		}

		@Override
		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			return 1;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return objects.size();
		}
	}
	
	public ArrayList<Contact> getFavorites(){
		ArrayList<Contact> lList = new ArrayList<Contact>();
		ArrayList<String> sortedList = new ArrayList<String>();
		EntradaApplication application = (EntradaApplication) EntradaApplication.getAppContext();
		setFavorites = new HashSet<String>();
		setFavorites = (HashSet<String>) application.getStringSetFromSharedPrefs("MyFavorites");
		
		if(setFavorites != null){
			Log.e("setFavorites", Integer.toString(setFavorites.size()));
			sortedList = new ArrayList<String>(setFavorites);
			Collections.sort(sortedList);
		}
		for(int i=0;i<sortedList.size();i++){
			String[] temp;
			String delimiter = "/";
			temp = sortedList.get(i).toString().split(delimiter);
			String name= "", no="";
			for(int j=0; j < temp.length ; j++){
				try{
					name = temp[0];
					no = temp[1];
				} catch(Exception e){
				}
			}
			lList.add(new Contact(Contact.ITEM, name, no));
		}

		return lList;
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
		getActivity().finish();
	}
	
}
