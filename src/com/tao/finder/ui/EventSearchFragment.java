package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.tao.finder.R;
import com.tao.finder.logic.EventSearchAdapter;
import com.tao.finder.logic.ParseContract;

public class EventSearchFragment extends SearchListFragment {

	public EventSearchFragment() {
		super();
		navigationDestination = EventActivity.class;
	}

	public void newSearch(String searchString)
	{
		initializeParameters();
		this.searchString = searchString;
		search();
	}
	
	@Override
	protected void search() {
		onSearchListener.onSearchStarted();
		final PullToRefreshListView resultList = ((PullToRefreshListView)getView().findViewById(R.id.result_list));
		final EventSearchAdapter adapter = new EventSearchAdapter(getActivity(), new ArrayList<ParseObject>());
		resultList.setAdapter(adapter);
		ParseContract.Event.searchEvent(searchString, maxResultSize,
				resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						addSearchResult(adapter, objects);
						onSearchListener.onSearchEnded();
					}
				});
	}

	@Override
	protected void loadMoreResult() {
		final PullToRefreshListView resultList = ((PullToRefreshListView)getView().findViewById(R.id.result_list));
		final EventSearchAdapter adapter = (EventSearchAdapter) ((HeaderViewListAdapter)resultList.getRefreshableView().getAdapter()).getWrappedAdapter();
		ParseContract.Event.searchEvent(searchString, maxResultSize,
				resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						addSearchResult(adapter, objects);
						resultList.onRefreshComplete();
					}
				});
	}

}
