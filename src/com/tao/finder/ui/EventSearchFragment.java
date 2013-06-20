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
		navigationDestination = PersonActivity.class;
		adapter = new EventAdapter(getActivity(), new ArrayList<ParseObject>());
	}

	public static EventSearchFragment newInstance(String searchString) {
		EventSearchFragment fragment = new EventSearchFragment();

		Bundle args = new Bundle();
		args.putString(ARG_SEARCH_STRING, searchString);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	protected void search() {
		getActivity().setProgressBarIndeterminate(true);
		ParseContract.Event.searchEvent(searchString, maxResultSize,
				resultSkip, new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> objects, ParseException e) {
						// TODO Auto-generated method stub
						resultSkip += objects.size();
						lastResultSize = objects.size();
						((EventAdapter) adapter).addEvents(objects);
						getActivity().setProgressBarIndeterminate(false);
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
