package com.entradahealth.entrada.android.app.personal.activities.job_type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.core.domain.ExpressNotesTags;
import com.entradahealth.entrada.core.domain.JobType;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public final class JobTypeFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";

    JobType jt;
    int pos;
	
	
	public JobTypeFragment(JobType jt, int pos) {
		this.jt = jt;
		this.pos = pos;
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
        	//pos = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.frag_job_type, null);
    	TextView tvJobTitle = (TextView)root.findViewById(R.id.tvJobTypeTitle);
    	tvJobTitle.setText(BundleKeys.myJobTypes.get(pos).name);
    	int count = 0;
    	ArrayList<String> list_tags = new ArrayList<String>();
    	for(int i= 0;i<BundleKeys.myTags.size();i++){
    		if(BundleKeys.myTags.get(i).id == BundleKeys.myJobTypes.get(pos).id){
    			list_tags.add(BundleKeys.myTags.get(i).name);
    			count++;
    		}
    	}
    	
    	Collections.sort(list_tags);
    	
    	TextView tvTagCount = (TextView)root.findViewById(R.id.tvTagCount);
    	if(count == 0)
    		tvTagCount.setText("No Available Tags");
    	else
    		tvTagCount.setText(Integer.toString(count)+" Available Tags");
    	
    	ListView lvTags = (ListView)root.findViewById(R.id.lvTags);
    	lvTags.setAdapter(new TagsAdapter(list_tags,count));
    	
		setRetainInstance(true);
        return root;
    }
    
    class TagsAdapter extends BaseAdapter{
		
		ArrayList<String> tagsInfo;
		int count;
		
		TagsAdapter(ArrayList<String> tagsInfo, int count){
			this.tagsInfo = tagsInfo;
			this.count=count;
		}
		
		@Override
		public int getCount() {
			
//			return jsArray.length();
			return tagsInfo.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			try {
				return tagsInfo.get(arg0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int pos, View arg1, ViewGroup arg2) {
			View view=	getActivity().getLayoutInflater().inflate(R.layout.tags_list_item, arg2,false);
			TextView tvAddInfoItem = (TextView) view.findViewById(R.id.tvTagName);
			tvAddInfoItem.setText(tagsInfo.get(pos));
			return view;
		}

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(KEY_CONTENT, pos);
    }
}
