package com.entradahealth.entrada.android.app.personal.activities.job_list;

import java.util.Comparator;
import java.util.List;
import android.os.AsyncTask;
import android.widget.ListView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.AndroidState;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.UserState;
import com.entradahealth.entrada.app.personal.menu_adapter.JobMenuAdapter;
import com.entradahealth.entrada.core.auth.Account;
import com.entradahealth.entrada.core.domain.Job;
import com.entradahealth.entrada.core.domain.providers.DomainObjectReader;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class JobCountTask extends AsyncTask<Void, Void, List<Job>>{
	
	JobListActivity jbList;
	int i = 1;
    protected final Account account ;
    protected final Predicate<Job> filter ;
    protected final Comparator<Job> comparator ;
    protected final boolean qchecked ;
    
	public JobCountTask(JobListActivity activity, Account account,
            Predicate<Job> filter, Comparator<Job> comparator, boolean qchecked){
		
		this.jbList = activity;
		this.account = account;
        this.filter = filter;
        this.comparator = comparator;
        this.qchecked = qchecked;
        
	}
	
	private Predicate<Job> getFilter(int i)
    {
        Predicate<Job> filter = jbList.ALL_JOBS;

        switch(i){
        
        case 1:
        	filter = jbList.TODAY_JOBS;
        	break;
        case 2:
        	filter = jbList.TOMORROW_JOBS;
        	break;
        case 3:
        	filter = jbList.STAT_JOBS;
        	break;
        case 4:
        	filter = jbList.TOTAL_JOBS;
        	break;
        case 5:
        	filter = jbList.HELD_JOBS;
        	break;
        case 6:
        	filter = jbList.DICTATED_JOBS;
        	break;
        case 7:
        	filter = jbList.DELETED_JOBS;
        	break;
        	
        default:
        	filter = jbList.ALL_JOBS;
        }

        return filter;
    }

	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected List<Job> doInBackground(Void... params) {
		// TODO Auto-generated method stub
	
		UserState state = AndroidState.getInstance().getUserState();
		//synchronized (state)
		//{
		DomainObjectReader reader = state.getProvider(account);
		
		return (reader != null) ? reader.searchJobs("")
		: ImmutableList.<Job>of();
		//}
	}
	
	@Override
	protected void onPostExecute(List<Job> jobs) {
		// TODO Auto-generated method stub
		List<Job> j_jobs;
		super.onPostExecute(jobs);
		
		if(qchecked){
			 for(int i = 1; i <= 7; i++){
				 if(i==1){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.TODAY_COUNT = j_jobs.size();
				 }else if(i==2){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.TOMORROW_COUNT = j_jobs.size();
				 }else if(i==3){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.STAT_COUNT = j_jobs.size();
				 }else if(i==4){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.ALL_COUNT = j_jobs.size();
				 }else if(i==5){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.HOLD_COUNT = j_jobs.size();
				 }else if(i==6){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.COMPLETED_COUNT = j_jobs.size();
				 }else if(i==7){
					 j_jobs = Lists.newArrayList(Iterables.filter(jobs, getFilter(i)));
					 BundleKeys.DELETED_COUNT = j_jobs.size();
				 }					 
			 }
		}else{
			BundleKeys.TODAY_COUNT = 0;
			BundleKeys.TOMORROW_COUNT = 0;
			BundleKeys.STAT_COUNT = 0;
			BundleKeys.ALL_COUNT = 0;
			BundleKeys.HOLD_COUNT = 0;
			BundleKeys.COMPLETED_COUNT = 0;
			BundleKeys.DELETED_COUNT = 0;
		}
		 
		 
		 JobMenuAdapter jBAdap = new JobMenuAdapter(jbList);
		 ListView lvSliding = (ListView)jbList.findViewById(R.id.lvSlidingMenu);
		 lvSliding.setAdapter(jBAdap); 
	        
	}
}
