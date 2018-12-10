package com.cdtsp.settings.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MySeekBar extends SeekBar {

    private boolean mDisable;
    public MySeekBar(Context context) {
        this(context, null);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDisable()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    public void disable() {
        mDisable = true;
        setSelected(false);
    }
    public void enable() {
        mDisable = false;
        setSelected(true);
    }
    public boolean isDisable() {
        return mDisable;
    }
}
