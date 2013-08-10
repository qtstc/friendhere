/**
 * 
 */
package com.tao.friendhere.logic;

import java.util.List;

import com.parse.ParseObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Parent class for the adapters for search lists.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 * @param The
 *            object associated with each list item. It can be either ParseUser
 *            or ParseObject depending on the subjects being searched.
 */
public abstract class SearchListAdapter<T extends ParseObject> extends
		BaseAdapter {

	protected List<T> listItems;// The list of objects corresponding to each
								// list item.

	protected Context mContext;

	public SearchListAdapter(Context mContext, List<T> listItems) {
		this.listItems = listItems;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public T getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(List<T> newItems) {
		listItems.addAll(newItems);
		notifyDataSetChanged();
	}

	@Override
	public abstract View getView(int position, View convertView,
			ViewGroup parent);

}
