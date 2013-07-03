package com.tao.finder.logic;

import java.util.List;
import com.parse.ParseUser;
import com.tao.finder.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapters for searching person(ParseUser).
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class PersonSearchAdapter extends SearchListAdapter<ParseUser> {

	public PersonSearchAdapter(Context mContext, List<ParseUser> listItems) {
		super(mContext, listItems);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		ParseUser item = getItem(position);
		if (row == null) {
			row = LayoutInflater.from(mContext).inflate(
					R.layout.person_list_item, parent, false);

			holder = new ViewHolder();
			holder.upperText = (TextView) row
					.findViewById(R.id.person_item_upper_text);
			holder.lowerText = (TextView) row
					.findViewById(R.id.person_item_lower_text);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		holder.upperText.setText(item.getString(ParseContract.User.NAME));
		// TODO: take care of exception when email is empty
		holder.lowerText.setText(Utility.truncString(item.getEmail(), 15));
		return row;
	}

	/**
	 * View holder to speed up the list item creation process.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	private static class ViewHolder {
		TextView upperText;
		TextView lowerText;
	}
}
