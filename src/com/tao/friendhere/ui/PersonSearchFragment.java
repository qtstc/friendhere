package com.tao.friendhere.ui;

import java.util.ArrayList;
import java.util.List;

import android.widget.HeaderViewListAdapter;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.tao.friendhere.R;
import com.tao.friendhere.logic.ParseContract;
import com.tao.friendhere.logic.PersonSearchAdapter;

/**
 * Fragment that takes care of the listing of the result of person search.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class PersonSearchFragment extends SearchListFragment {

	private String eventId;// The objectId of the event in which we are
							// searching for the person.

	public PersonSearchFragment() {
		super();
		navigationDestination = PersonActivity.class;
	}

	/**
	 * Start a new search with the given search string in the event with the
	 * given Id. Need to be called before doing any other operation on this
	 * fragment. Necessary because the constructor of fragments cannot take any
	 * argument.
	 * 
	 * @param searchString
	 *            the search string
	 * @param eventId
	 *            the id of the event to be searched.
	 */
	public void newSearch(String searchString, String eventId) {
		initializeParameters();
		this.eventId = eventId;
		this.searchString = searchString;
		search();
	}

	@Override
	protected void search() {
		onSearchListener.onSearchStarted();
		final PullToRefreshListView resultList = ((PullToRefreshListView) getView()
				.findViewById(R.id.result_list));
		final PersonSearchAdapter adapter = new PersonSearchAdapter(
				getActivity(), new ArrayList<ParseUser>());
		resultList.setAdapter(adapter);
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize,
				resultSkip, new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> objects, ParseException e) {
						onSearchListener.onSearchEnded();
						if (e == null)
							addSearchResult(adapter, objects);
						else
							Toast.makeText(getActivity(),
									R.string.connection_error_toast_message,
									Toast.LENGTH_SHORT).show();
					}
				});
	}

	@Override
	protected void loadMoreResult() {
		final PullToRefreshListView resultList = ((PullToRefreshListView) getView()
				.findViewById(R.id.result_list));
		final PersonSearchAdapter adapter = (PersonSearchAdapter) ((HeaderViewListAdapter) resultList
				.getRefreshableView().getAdapter()).getWrappedAdapter();
		ParseContract.User.searchPerson(searchString, eventId, maxResultSize,
				resultSkip, new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> objects, ParseException e) {
						resultList.onRefreshComplete();
						if (e == null)
							addSearchResult(adapter, objects);
						else
							Toast.makeText(getActivity(),
									R.string.connection_error_toast_message,
									Toast.LENGTH_SHORT).show();
					}
				});
	}

}
