package com.entradahealth.entrada.android.app.personal.activities.job_display;

import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.core.domain.JobType;


/**
 * Used by JobDisplayAdapter to populate the spinner with
 */
public class JobTypeSpinnerAdapter extends ArrayAdapter<JobType>
{
    private final JobDisplayActivity activity;

    public JobTypeSpinnerAdapter(JobDisplayActivity activity, List<JobType> objects)
    {
        super(activity, android.R.layout.simple_spinner_item, objects);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        JobTypeHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.simple_spinner_item, parent, false);

            TextView nameView = (TextView)row.findViewById(R.id.SimpleSpinnerItem_Text);

            holder = new JobTypeHolder(nameView);

            row.setTag(holder);
        }
        else
        {
            holder = (JobTypeHolder)row.getTag();
        }

        holder.jobType = getItem(position);
        holder.nameView.setText(holder.jobType.name);

        return row;
    }

    public static class JobTypeHolder
    {
        public final TextView nameView;

        public JobType jobType;


        public JobTypeHolder(TextView nameView)
        {
            this.nameView = nameView;
        }
    }
}
