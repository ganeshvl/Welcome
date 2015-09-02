package com.entradahealth.entrada.android.app.personal.activities.inbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.android.app.personal.activities.add_job.PatientListItemAdapter;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTConversation;
import com.entradahealth.entrada.android.app.personal.thirdparty.org.droidparts.widget.ClearableEditText;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Patient;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.entradahealth.entrada.core.inbox.service.ChatManager;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;



public class PatientSearchFrament extends Fragment {
	
	EditText etSearch;
	private TextWatcher mSearchTw;
	ArrayList<String> mPatients;
	ArrayList<String> mMRNs;
	String recipient_name, patient_name;
	PatientSearchTask searchTask = null;
	List<? extends Patient> list_patients = null;
	ClearableEditText searchText;
	ListView patientList;
	private ENTConversation conversation;
	private FragmentManager fragmentManager;
	private ChatManager chat;
	
	public PatientSearchFrament(ChatManager chat) {
		this.chat = chat;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().setTitle("Add Patient");
		getActivity().getActionBar().setDisplayUseLogoEnabled(true);
		getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		getActivity().getActionBar().setDisplayShowTitleEnabled(true);
		getActivity().getActionBar().setDisplayShowCustomEnabled(false);
		fragmentManager = getFragmentManager();
		Bundle bundle = this.getArguments();
		if(bundle != null){
			patient_name = bundle.getString("patient_name"); 
			recipient_name = bundle.getString("recipient_name");
			conversation= (ENTConversation) bundle.getSerializable("conversation");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.frag_patient_search, container, false);
		patientList = (ListView)view.findViewById(R.id.AddJob_PatientList);
		searchText = (ClearableEditText)view.findViewById(R.id.AddJob_SearchText);
	    searchText.addTextChangedListener(new TextWatcher()
	        {
	            @Override
	            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
	            {

	            }

	            @Override
	            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
	            {

	            }

	            @Override
	            public void afterTextChanged(Editable editable)
	            {
	                doSearch();
	            }
	        });
	        
	        
	        patientList.setOnItemClickListener(new AdapterView.OnItemClickListener()
	        {
	            @Override
	            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	            {
	            	//Patient p = list_patients.get(i);
	            	//Log.e("p", p.toString());
	            }
	        });

		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        doSearch();
	}
	
	private void doSearch()
    {
        if (searchTask != null) searchTask.cancel(true);
        methodThatStartsTheAsyncTask(searchText.getText().toString());
        
        
    }
	
	private void methodThatStartsTheAsyncTask(String s) {
		searchTask = new PatientSearchTask(s, new FragmentCallback() {

			@Override
			public void onTaskDone() {
				// TODO Auto-generated method stub
				//Toast.makeText(getActivity(), "Task F=Done", 1000).show();
			}

            
        });

		searchTask.execute();
    }

    private void methodThatDoesSomethingWhenTaskIsDone() {
        /* Magic! */
    }
	
	public interface FragmentCallback {
        public void onTaskDone();
    }
	
	public class PatientSearchTask extends AsyncTask<Void, Void, Collection<? extends Patient>>
	{
	    private final String searchText;
	    private FragmentCallback mFragmentCallback;
	    
	    public PatientSearchTask(String searchText, FragmentCallback fragmentCallback) {
			// TODO Auto-generated constructor stub
	    	this.searchText = searchText;
	    	//mFragmentCallback = fragmentCallback;
		}
	    
	    

	    @Override
	    protected void onPreExecute()
	    {
	        super.onPreExecute();

	        patientList.setVisibility(View.GONE);
	        
	    }

	    @Override
	    protected Collection<? extends Patient> doInBackground(Void... voids)
	    {
	        Log.d("Entrada-PatientSearchTask", String.format("Search started: '%s'", searchText));

	        UserState state = AndroidState.getInstance().getUserState();

	        //synchronized (state)
	        //{
	            Account account = state.getCurrentAccount();
	            if(account != null){
		            DomainObjectReader reader = state.getProvider(account);
	
		            assert reader != null;
	
		            if (Strings.isNullOrEmpty(searchText))
		            {
		                return reader.getPatients();
		            }
		            return reader.searchPatients(searchText);
	            } else {
	            	return ImmutableList.copyOf(new ArrayList<Patient>());
	            }
	        //}
	    }

	    @Override
	    protected void onCancelled()
	    {
	        super.onCancelled();
	        Log.d("Entrada-PatientSearchTask", String.format("Cancelled: '%s'", searchText));
	    }

	    @Override
	    protected void onCancelled(Collection<? extends Patient> patients)
	    {
	        super.onCancelled(patients);
	        Log.d("Entrada-PatientSearchTask", String.format("Cancelled: '%s'", searchText));
	    }

	    @Override
	    protected void onPostExecute(Collection<? extends Patient> patients)
	    {
	        super.onPostExecute(patients);

	        Log.d("Entrada-PatientSearchTask", String.format("Success: %d results", patients.size()));

	        if (patients.size() == 0)
	        {
	            patientList.setVisibility(View.GONE);
	            return;
	        }

	        List<? extends Patient> p = Lists.newArrayList(patients);
	        Collections.sort(p);
	        list_patients = p;
	        try{
	        PatientListItemAdapter adapter =
	                new PatientListItemAdapter(getActivity(), android.R.layout.simple_list_item_1, p, true, recipient_name, conversation, fragmentManager, chat);
	        patientList.setAdapter(adapter);
	        } catch(Exception ex){
	        	
	        }
	        patientList.setVisibility(View.VISIBLE);
	        
	    }
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		BundleKeys.fromSecureMessaging = true;
		getActivity().finish();
	}
}
