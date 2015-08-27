package com.entradahealth.entrada.android.app.personal.activities.manage_queues;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.core.domain.Queue;
import com.google.common.collect.Lists;

/**
 * TODO: Document this!
 * 
 * @author edwards
 * @since 11/15/13
 */
public class QueueListItemAdapter extends ArrayAdapter<Queue> implements
		Filterable {

	private final Activity _activity;
	public final List<Boolean> checks;
	ArrayList<String> selChks;
	ArrayList<Queue> arrayList;
	ArrayList<Queue> mOriginalValues; // Original Values
	LayoutInflater inflater;
	Context context;

	public QueueListItemAdapter(Activity activity, int textViewResourceId,
			ArrayList<Queue> objects) {
		super(activity, textViewResourceId, objects);
		this._activity = activity;
		this.checks = Lists.newArrayListWithCapacity(objects.size());
		for (int i = 0; i < objects.size(); ++i)
			checks.add(false);
		this.arrayList = objects;
		//this.mOriginalValues = objects;
		selChks = new ArrayList<String>(objects.size());
		inflater = LayoutInflater.from(activity);
		this.context = activity;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrayList.size();
	}

	static class ViewHolder {
		protected TextView text;
		// protected CheckBox checkbox;
	}

	ViewHolder viewHolder;

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		viewHolder = null;
		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(context);
			convertView = inflator.inflate(R.layout.manage_queues_list_item,
					null);
			viewHolder = new ViewHolder();
			viewHolder.text = (TextView) convertView
					.findViewById(R.id.tvQueueName);

			convertView.setTag(viewHolder);
			//convertView.setTag(R.id.tvQueueName, viewHolder.text);
			// convertView.setTag(R.id.cbQueue, viewHolder.checkbox);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.text.setText(arrayList.get(position).name);

		/*
		 * if(arrayList.get(position).isSubscribed)
		 * viewHolder.text.setTextColor(Color.parseColor("#00838f")); else
		 * viewHolder.text.setTextColor(Color.parseColor("#000000"));
		 */

		if (getItem(position).isSubscribed) {
			// cbQueue.setChecked(true);
			if (!selChks.contains(getItem(position).name))
				selChks.add(getItem(position).name);
		}

		viewHolder.text
				.setTextColor(arrayList.get(position).isSubscribed ? Color
						.parseColor("#00838f") : Color.parseColor("#000000"));
		

		boolean checked = checks.get(position);
		convertView.setBackgroundColor(_activity.getResources().getColor(
				checked ? R.color.selected_list_item
						: R.color.unselected_list_item));
		// viewHolder.text.setOnClickListener(new MyClickListiner(position));
		return convertView;

	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				arrayList = (ArrayList<Queue>) results.values; // has the
																// filtered
																// values
				notifyDataSetChanged(); // notifies the data with new filtered
										// values
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults(); // Holds the
																// results of a
																// filtering
																// operation in
																// values
				ArrayList<Queue> FilteredArrList = new ArrayList<Queue>();

				if (mOriginalValues == null) {
					mOriginalValues = new ArrayList<Queue>(arrayList); // saves
																		// the
																		// original
																		// data
																		// in
																		// mOriginalValues
				}

				/********
				 * 
				 * If constraint(CharSequence that is received) is null returns
				 * the mOriginalValues(Original) values else does the Filtering
				 * and returns FilteredArrList(Filtered)
				 * 
				 ********/
				if (constraint == null || constraint.length() == 0) {

					// set the Original result to return
					results.count = mOriginalValues.size();
					results.values = mOriginalValues;
				} else {
					constraint = constraint.toString().toLowerCase();
					for (int i = 0; i < mOriginalValues.size(); i++) {
						String data = mOriginalValues.get(i).name;
						if (data.toLowerCase().contains(constraint.toString())) {
							FilteredArrList.add(mOriginalValues.get(i));
						}
					}
					// set the Filtered result to return
					results.count = FilteredArrList.size();
					results.values = FilteredArrList;
				}
				return results;
			}
		};
		return filter;
	}

}
