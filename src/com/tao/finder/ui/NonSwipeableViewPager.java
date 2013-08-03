package com.tao.finder.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A ViewPager class that does not allow the user to use swiping to navigate
 * between pages.
 * 
 * It is used when one of the pages contains a map.
 * 
 * @author Tao Qian(taoqian_2015@depauw.edu)
 * 
 */
public class NonSwipeableViewPager extends ViewPager {

	public NonSwipeableViewPager(Context context) {
		super(context);
	}

	public NonSwipeableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		// Never allow swiping to switch between pages
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Never allow swiping to switch between pages
		return false;
	}
}