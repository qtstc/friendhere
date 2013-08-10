package com.tao.friendhere.logic;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Content provider used to save the recent search history of the search in
 * events.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class SuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "com.tao.friendhere.logic.SuggestionProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public SuggestionProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
