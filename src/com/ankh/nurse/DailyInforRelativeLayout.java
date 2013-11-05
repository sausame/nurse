package com.ankh.nurse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class DailyInforRelativeLayout extends RelativeLayout {

	public DailyInforRelativeLayout(Context context) {
		this(context, null);
	}
	
    public DailyInforRelativeLayout(final Context context, AttributeSet attrs) {
        this(context, attrs, android.R.style.Widget_Button);
    }	
	
    public DailyInforRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		setClickable(true);
    }
/*
    @Override   
    public boolean dispatchTouchEvent(MotionEvent ev)  {
        return super.dispatchTouchEvent(ev);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	Log.v("Touch " + ev.getAction());
        return super.onTouchEvent(ev);
    }
*/
/*
    @Override
    public boolean hasFocusable() {
       return false;
    }
*/
}
