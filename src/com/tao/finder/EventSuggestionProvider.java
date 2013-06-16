package com.tao.finder;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Content provider used to save the recent search history of the search in events.
 * @author Tao Qian
 *
 */
public class EventSuggestionProvider extends SearchRecentSuggestionsProvider {
	 public final static String AUTHORITY = "com.tao.finder.EventSuggestionProvider";
	    public final static int MODE = DATABASE_MODE_QUERIES;

	    public EventSuggestionProvider() {
	        setupSuggestions(AUTHORITY, MODE);
	    }
}
