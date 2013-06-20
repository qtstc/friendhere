package com.tao.finder.ui;

import java.util.ArrayList;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.tao.finder.R;
import com.tao.finder.logic.PersonAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class SearchResultFragment extends Fragment{
	
	public static final String ARG_SEARCH_STRING = "search_string";
	
	protected PullToRefreshListView resultList;
	protected String searchString;
	protected int maxResultSize;
	protected int resultSkip;
	protected int lastResultSize;
	protected Class navigationDestination;
	protected BaseAdapter adapter;
	
	public SearchResultFragment()
	{
		maxResultSize = 15;
		resultSkip = 0;
		lastResultSize = Integer.MAX_VALUE;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.fragment_search_result_list,
				container, false);
		resultList= (PullToRefreshListView)rootView.findViewById(R.id.result_list);
		resultList.setAdapter(adapter);
		getArgs(savedInstanceState);
		search();
		
		resultList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(lastResultSize<maxResultSize)
					return;
				loadMoreResult();
			}
		});
		
		resultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				ParseObject item = (ParseObject)parent.getAdapter().getItem(position);
				Intent intent = new Intent(getActivity(),navigationDestination);
				intent.putExtra(EventActivity.OBJECT_ID, item.getObjectId());
				startActivity(intent);
			}
		});
		
		return rootView;
	}
	
	public void newSearch(String searchString)
	{
		this.searchString = searchString;
		search();
	}
	
	abstract protected void search();
	abstract protected void loadMoreResult();
	
	protected void getArgs(Bundle b)
	{
		searchString = b.getString(ARG_SEARCH_STRING);
	}
}
