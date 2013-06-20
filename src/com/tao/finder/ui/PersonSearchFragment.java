package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.PersonAdapter;

public class PersonSearchFragment extends SearchResultFragment {

	public static final String ARG_EVENT_ID = "event_id";
	
	private String eventId;
	
	public PersonSearchFragment()
	{
		super();
		navigationDestination = PersonActivity.class;
		adapter = new PersonAdapter(getActivity(),new ArrayList<ParseUser>());
	}
	
	public static PersonSearchFragment newInstance(String searchString, String eventId) {
		PersonSearchFragment fragment = new PersonSearchFragment();

	    Bundle args = new Bundle();
	    args.putString(ARG_EVENT_ID, eventId);
	    args.putString(ARG_SEARCH_STRING, searchString);
	    fragment.setArguments(args);

	    return fragment;
	}
	
	@Override
	protected void getArgs(Bundle b)
	{
		super.getArgs(b);
		eventId = b.getString(ARG_EVENT_ID);
	}
	
	@Override
	protected void search() {
		getActivity().setProgressBarIndeterminate(true);
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize, resultSkip, new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				resultSkip += objects.size();
				lastResultSize = objects.size();
				((PersonAdapter)adapter).addPeople(objects);
				getActivity().setProgressBarIndeterminate(false);
			}
		});
	}

	@Override
	protected void loadMoreResult() {
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize, resultSkip, new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				resultSkip += objects.size();
				lastResultSize = objects.size();
				((PersonAdapter)adapter).addPeople(objects);
				resultList.onRefreshComplete();
			}
		});
	}

}
