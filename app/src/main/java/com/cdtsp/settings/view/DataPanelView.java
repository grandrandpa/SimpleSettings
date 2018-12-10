package com.cdtsp.settings.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2018/3/5.
 */

public class DataPanelView extends View {

    private Paint mPaint;

    public DataPanelView(Context context) {
        this(context, null);
    }

    public DataPanelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画坐标轴
        mPaint.setColor(Color.GRAY);
        canvas.drawLine(0, 0, 0, getHeight(), mPaint);
        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), mPaint);

        //画曲线
    }
}

