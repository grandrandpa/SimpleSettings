package com.cdtsp.settings.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class ListViewSafe extends ListView {
    private String TAG = getClass().getSimpleName();
    public ListViewSafe(Context context) {
        super(context);
    }

    public ListViewSafe(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "dispatchTouchEvent: " + e.getMessage());
        }
        return false;
    }
}
