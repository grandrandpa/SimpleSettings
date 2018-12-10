package com.cdtsp.settings.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Administrator on 2018/3/5.
 */

public class TextViewTag extends TextView {

    private Paint mPaint;
    public TextViewTag(Context context) {
        this(context, null);
    }

    public TextViewTag(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isSelected()) {
            canvas.drawRect(0, getHeight() - 4, getWidth(), getHeight(), mPaint);
        }
    }
}
