package com.tao.finder.logic;

import java.util.List;
import com.parse.ParseObject;
import com.tao.finder.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EventAdapter extends BaseAdapter {

	private List<ParseObject> events;
	private Context mContext;
	
	public EventAdapter(Context mContext, List<ParseObject> events)
	{
		this.events = events;
		this.mContext = mContext;
	}
	
	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public Object getItem(int arg0) {
		return events.get(arg0);
	}

	@Override
	public long getItemId(int arg0){
		//throw new UnsupportedOperationException("This method is not implemented");
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		ParseObject item = (ParseObject) getItem(position);
		if(row == null)
		{
			//LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = LayoutInflater.from(mContext).inflate(R.layout.event_list_item, parent, false);                
	        holder = new ViewHolder();
	        holder.upperText = (TextView)row.findViewById(R.id.event_item_upper_text);
	        holder.lowerText = (TextView)row.findViewById(R.id.event_item_lower_text);

	        row.setTag(holder);
	    } else {
	        holder = (ViewHolder) row.getTag();
	    }
		holder.upperText.setText(item.getString(ParseContract.Event.NAME));
		holder.lowerText.setText(Utility.truncString(item.getString(ParseContract.Event.DESCRIPTION),15));
		return row;
	}
	
	public void addEvents(List<ParseObject> newEvents)
	{
		events.addAll(newEvents);
		notifyDataSetChanged();
	}
	
	private static class ViewHolder
	{
		TextView upperText;
		TextView lowerText;
	}

}
