package com.tao.finder.logic;

import java.util.List;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.tao.finder.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PersonAdapter extends BaseAdapter {

	private List<ParseUser> people;
	private Context mContext;
	
	public PersonAdapter(Context mContext, List<ParseUser> people)
	{
		this.people = people;
		this.mContext = mContext;
	}
	
	@Override
	public int getCount() {
		return people.size();
	}

	@Override
	public Object getItem(int arg0) {
		return people.get(arg0);
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
		ParseUser item = (ParseUser) getItem(position);
		if(row == null)
		{
			row = LayoutInflater.from(mContext).inflate(R.layout.person_list_item, parent, false);                

	        holder = new ViewHolder();
	        holder.upperText = (TextView)row.findViewById(R.id.person_item_upper_text);
	        holder.lowerText = (TextView)row.findViewById(R.id.person_item_lower_text);

	        row.setTag(holder);
	    } else {
	        holder = (ViewHolder) row.getTag();
	    }
		holder.upperText.setText(item.getString(ParseContract.User.NAME));
		//TODO: take care of exception when email is empty
		holder.lowerText.setText(Utility.truncString(item.getEmail(),15));
		return row;
	}
	
	public void addPeople(List<ParseUser> newPeople)
	{
		people.addAll(newPeople);
		notifyDataSetChanged();
	}
	
	private static class ViewHolder
	{
		TextView upperText;
		TextView lowerText;
	}

}
