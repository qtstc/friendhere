package com.tao.finder.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tao.finder.logic.ParseContract;
import com.tao.finder.logic.PersonAdapter;

public class PersonSearchFragment extends SearchResultFragment {
	
	private String eventId;
	
	public PersonSearchFragment()
	{
		super();
		navigationDestination = PersonActivity.class;
	}
	
	public void newSearch(String searchString,String eventId)
	{
		this.eventId=eventId;
		newSearch(searchString);
	}
	
	@Override
	protected void search() {
		//onSearchListener.onSearchStarted();
		adapter = new PersonAdapter(getActivity(),new ArrayList<ParseUser>());
		resultList.setAdapter(adapter);
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize, resultSkip, new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> objects, ParseException e) {
				resultSkip += objects.size();
				lastResultSize = objects.size();
				((PersonAdapter)adapter).addPeople(objects);
				//onSearchListener.onSearchEnded();
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
