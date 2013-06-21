package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.widget.HeaderViewListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.tao.finder.R;
import com.tao.finder.logic.EventSearchAdapter;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.PersonSearchAdapter;

public class PersonSearchFragment extends SearchListFragment {
	
	private String eventId;
	
	public PersonSearchFragment()
	{
		super();
		navigationDestination = PersonActivity.class;
	}
	
	public void newSearch(String searchString,String eventId)
	{
		initializeParameters();
		this.eventId=eventId;
		this.searchString = searchString;
		search();
	}
	
	@Override
	protected void search() {
		onSearchListener.onSearchStarted();
		final PullToRefreshListView resultList = ((PullToRefreshListView)getView().findViewById(R.id.result_list));
		final PersonSearchAdapter adapter = new PersonSearchAdapter(getActivity(), new ArrayList<ParseUser>());
		resultList.setAdapter(adapter);
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize, resultSkip, new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				addSearchResult(adapter, objects);
				onSearchListener.onSearchEnded();
			}
		});
	}

	@Override
	protected void loadMoreResult() {
		final PullToRefreshListView resultList = ((PullToRefreshListView)getView().findViewById(R.id.result_list));
		final PersonSearchAdapter adapter = (PersonSearchAdapter) ((HeaderViewListAdapter)resultList.getRefreshableView().getAdapter()).getWrappedAdapter();
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize, resultSkip, new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				addSearchResult(adapter, objects);
				resultList.onRefreshComplete();
			}
		});
	}

}
