package com.entradahealth.entrada.android.app.personal.activities.inbox.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.BundleKeys;
import com.entradahealth.entrada.android.app.personal.EntradaApplication;
import com.entradahealth.entrada.android.app.personal.activities.inbox.models.ENTUser;

public class ParticipantsGroupListAdapter extends BaseAdapter{

	private Context mContext;
	private List<ENTUser> participants;
	private LayoutInflater inflater;
	private String threadOwnwer;
	private EntradaApplication application;
	
	public ParticipantsGroupListAdapter(Context context, List<ENTUser> participants, String threadOwner){
		this.mContext = context;
		this.participants = participants;
		this.threadOwnwer = threadOwner;
		inflater = LayoutInflater.from(mContext);
		application = (EntradaApplication) EntradaApplication.getAppContext();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return participants.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return participants.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
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
	
	private View getConvertView(int position) {
		ViewHolder holder = new ViewHolder();
		View view = inflater.inflate(R.layout.participant_list_item, null);
		holder.paticipantName = (TextView) view.findViewById(R.id.participant_name);
		holder.paticipantRole = (TextView) view.findViewById(R.id.participant_role);
		view.setTag(holder);
		return view;
	}

	private void updateView(int position, ViewHolder holder){
		ENTUser participant = participants.get(position);
		if(participant.getId().equals(threadOwnwer)){
			holder.paticipantRole.setText(mContext.getResources().getString(R.string.owner_str));
			holder.paticipantName.setTextColor(getColorCode(position));
		} else if(participant.getId().equals(application.getStringFromSharedPrefs(BundleKeys.CURRENT_QB_USER_ID))){
			holder.paticipantRole.setText(mContext.getResources().getString(R.string.you_str));
			holder.paticipantName.setTextColor(Color.BLACK);
		} else {
			holder.paticipantName.setTextColor(getColorCode(position));
			holder.paticipantRole.setText("");
		}

		holder.paticipantName.setText(participant.getName());
	}
	
	public int getColorCode(int position){
		float hue = new Float((191 + 137*position) % 360);
		float sat = 0.8f;
		float brightness = (float) 0.4;
		float[] hsb = {hue, sat, brightness};
		int rgb = HSLToColor(hsb);
		return rgb;
		
	}
	
	public static int HSLToColor(float[] hsl) {
		final float h = hsl[0];
		final float s = hsl[1];
		final float l = hsl[2];

		final float c = (1f - Math.abs(2 * l - 1f)) * s;
		final float m = l - 0.5f * c;
		final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

		final int hueSegment = (int) h / 60;

		int r = 0, g = 0, b = 0;

		switch (hueSegment) {
		case 0:
			r = Math.round(255 * (c + m));
			g = Math.round(255 * (x + m));
			b = Math.round(255 * m);
			break;
		case 1:
			r = Math.round(255 * (x + m));
			g = Math.round(255 * (c + m));
			b = Math.round(255 * m);
			break;
		case 2:
			r = Math.round(255 * m);
			g = Math.round(255 * (c + m));
			b = Math.round(255 * (x + m));
			break;
		case 3:
			r = Math.round(255 * m);
			g = Math.round(255 * (x + m));
			b = Math.round(255 * (c + m));
			break;
		case 4:
			r = Math.round(255 * (x + m));
			g = Math.round(255 * m);
			b = Math.round(255 * (c + m));
			break;
		case 5:
		case 6:
			r = Math.round(255 * (c + m));
			g = Math.round(255 * m);
			b = Math.round(255 * (x + m));
			break;
		}

		r = Math.max(0, Math.min(255, r));
		g = Math.max(0, Math.min(255, g));
		b = Math.max(0, Math.min(255, b));

		return Color.rgb(r, g, b);
	}
	
	private static class ViewHolder
	{
		TextView paticipantName;
		TextView paticipantRole;
	}


}
