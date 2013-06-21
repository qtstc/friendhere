package com.tao.finder.ui;


import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.ParseObject;
import com.tao.finder.R;
import com.tao.finder.logic.SearchListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class SearchListFragment extends Fragment{
	
	public static final String ARG_SEARCH_STRING = "search_string";
	private static final String NO_SEARCH = "user_did_not_search_yet";
	
	//protected PullToRefreshListView resultList;
	protected String searchString;
	protected int maxResultSize;
	protected int resultSkip;
	protected int lastResultSize;
	@SuppressWarnings("rawtypes")
	protected Class navigationDestination;
	protected OnSearchListener onSearchListener;
	
	public SearchListFragment()
	{
		initializeParameters();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.fragment_search_result_list,
				container, false);
		final PullToRefreshListView resultList= (PullToRefreshListView)rootView.findViewById(R.id.result_list);
		resultList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if(/*lastResultSize<maxResultSize||*/searchString.equals(NO_SEARCH))
				{
					resultList.onRefreshComplete();
					return;
				}
				loadMoreResult();
			}
		});
		
		resultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				ParseObject item = (ParseObject)parent.getAdapter().getItem(position);
				Intent intent = new Intent(getActivity(),navigationDestination);
				intent.putExtra(EventActivity.OBJECT_ID, item.getObjectId());
				startActivity(intent);
			}
		});
		
		return rootView;
	}
	
	
	protected void initializeParameters()
	{
		maxResultSize = 1;
		resultSkip = 0;
		lastResultSize = Integer.MAX_VALUE;
		searchString = NO_SEARCH;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void addSearchResult(SearchListAdapter adapter, List results)
	{
		resultSkip += results.size();
		lastResultSize = results.size();
		adapter.addItem(results);
	}
	
	
	abstract protected void search();
	abstract protected void loadMoreResult();
	
    public interface OnSearchListener {
        public void onSearchStarted();
        public void onSearchEnded();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onSearchListener = (OnSearchListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSearchListener");
        }
    }   
}
