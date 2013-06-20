package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.tao.finder.logic.EventAdapter;
import com.tao.finder.logic.ParseContract;

public class EventSearchFragment extends SearchResultFragment {

	public EventSearchFragment() {
		super();
		navigationDestination = EventActivity.class;
	}

	@Override
	protected void search() {
		onSearchListener.onSearchStarted();
		adapter = new EventAdapter(getActivity(), new ArrayList<ParseObject>());
		resultList.setAdapter(adapter);
		ParseContract.Event.searchEvent(searchString, maxResultSize,
				resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						resultSkip += objects.size();
						lastResultSize = objects.size();
						((EventAdapter) adapter).addEvents(objects);
						onSearchListener.onSearchEnded();
					}
				});
	}

	@Override
	protected void loadMoreResult() {
		ParseContract.Event.searchEvent(searchString, maxResultSize,
				resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						// TODO Auto-generated method stub
						resultSkip += objects.size();
						lastResultSize = objects.size();
						((EventAdapter) adapter).addEvents(objects);
						resultList.onRefreshComplete();
					}
				});
	}

}
