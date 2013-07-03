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

/**
 * Parent class for fragments that display a list as search result.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public abstract class SearchListFragment extends Fragment {

	// public static final String ARG_SEARCH_STRING = "search_string";
	private static final String NO_SEARCH = "user_did_not_search_yet";

	protected String searchString;
	// Used to stop list from refreshing when
	// all the results are loaded.
	// However, this feature is not used potentially because
	// of a bug is the PullToRefreshList library.
	protected int maxResultSize;
	protected int resultSkip;
	protected int lastResultSize;

	@SuppressWarnings("rawtypes")
	protected Class navigationDestination;// The destination activity when an
											// item is selected.
	protected OnSearchListener onSearchListener;

	public SearchListFragment() {
		initializeParameters();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Initialize the PullToRefreshList.
		View rootView = inflater.inflate(R.layout.fragment_search_result_list,
				container, false);
		final PullToRefreshListView resultList = (PullToRefreshListView) rootView
				.findViewById(R.id.result_list);
		resultList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				if (/* lastResultSize<maxResultSize|| */searchString
						.equals(NO_SEARCH)) {
					resultList.onRefreshComplete();
					return;
				}
				loadMoreResult();
			}
		});

		resultList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Start a new activity and include the objectId of the selected
				// item.
				ParseObject item = (ParseObject) parent.getAdapter().getItem(
						position);
				Intent intent = new Intent(getActivity(), navigationDestination);
				intent.putExtra(EventActivity.OBJECT_ID, item.getObjectId());
				startActivity(intent);
			}
		});
		return rootView;
	}

	protected void initializeParameters() {
		maxResultSize = 1;
		resultSkip = 0;
		lastResultSize = Integer.MAX_VALUE;
		searchString = NO_SEARCH;
	}

	/**
	 * Add new search result to the list. Used for pull to refresh.
	 * 
	 * @param adapter
	 *            the original adapter
	 * @param results
	 *            the new results.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void addSearchResult(SearchListAdapter adapter, List results) {
		resultSkip += results.size();
		lastResultSize = results.size();
		adapter.addItem(results);
	}

	/**
	 * Start a new search with the current searchString. It calls
	 * onSearchStarted and onSearchEnded at the beginning and end respectively.
	 * Result is updated to the PullToRefreshList.
	 */
	abstract protected void search();

	/**
	 * Load more result for the current search. It calls onSearchStarted and
	 * onSearchEnded at the beginning and the end respectively. Result is
	 * updated to the PullToRefreshList.
	 */
	abstract protected void loadMoreResult();

	/**
	 * Interface used for the communication between SearchlistFragment and the
	 * activity containing the fragment.
	 * 
	 * @author Tao Qian(taoqian_2015@depauw.edu)
	 * 
	 */
	public interface OnSearchListener {
		/**
		 * Called before search. Disable GUI element.
		 */
		public void onSearchStarted();

		/**
		 * Called after search. Enable GUI element.
		 */
		public void onSearchEnded();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Require the activity using this class to implement the
		// OnSearchListener interface.
		try {
			onSearchListener = (OnSearchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onSearchListener");
		}
	}
}
