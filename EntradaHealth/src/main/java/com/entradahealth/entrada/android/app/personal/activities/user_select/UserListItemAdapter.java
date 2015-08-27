package com.entradahealth.entrada.android.app.personal.activities.user_select;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.core.auth.User;
import com.google.common.collect.ImmutableList;


public class UserListItemAdapter extends ArrayAdapter<User>
{
    private final Activity _activity;

    public UserListItemAdapter(Activity activity, int textViewResourceId,
                               ImmutableList<User> objects)
    {
        super(activity, textViewResourceId, objects);
        this._activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        UserHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = _activity.getLayoutInflater();
            row = inflater.inflate(R.layout.user_select_list_item, parent, false);

            TextView dn = (TextView)row.findViewById(R.id.UserSelect_DisplayName);

            holder = new UserHolder(dn);

            row.setTag(holder);
        }
        else
        {
            holder = (UserHolder)row.getTag();
        }

        User user = getItem(position);
        Log.d("Entrada-User", String.valueOf(position) + ": " + (user != null ? user : "null"));
        holder.displayNameView.setText(user.getDisplayName());
        return row;
    }

    public static class UserHolder
    {
        public final TextView displayNameView;
        public UserHolder(TextView displayNameView)
        {
            this.displayNameView = displayNameView;
        }
    }
}
