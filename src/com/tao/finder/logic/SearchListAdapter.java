/**
 * 
 */
package com.tao.finder.logic;

import java.util.List;

import com.parse.ParseObject;
import com.parse.ParseUser;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author Tao
 *
 */
public abstract class SearchListAdapter<T extends ParseObject> extends BaseAdapter {
	
	protected List<T> listItems;
	protected Context mContext;

	public SearchListAdapter(Context mContext, List<T> listItems)
	{
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
	
	public void addItem(List<T> newItems)
	{
		listItems.addAll(newItems);
		notifyDataSetChanged();
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
