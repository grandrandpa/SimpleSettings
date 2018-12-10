package com.cdtsp.settings.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/2/27.
 */

public class SoundTouchView extends View {

    private static final String TAG = "SoundTouchView";
    private static final int FADE_BALANCE_TOTAL = 20;
    private int mWidth;
    private int mHeight;
    private float mTouchX = -1;
    private float mTouchY = -1;
    private Bitmap mBitmapYuan;
    private Bitmap mBitmapBg;
    private int mYuanWidth;
    private int mYuanHeight;
    private Bitmap mBitmapLineH;
    private Bitmap mBitmapLineV;
    private int mLineWidth;
    private Paint mPaint;

    /**
     * click up callback interface
     */
    public abstract static class OnClickUpCallback {
        public void onClickUp(int balance, int fade) {};
    }

    private ArrayList<OnClickUpCallback> mOnClickUpCallbacks = new ArrayList<>();
    public void addOnClickUpCallback(OnClickUpCallback onClickUpCallback) {
        mOnClickUpCallbacks.add(onClickUpCallback);
    }
    public void doOnClickUp(float x, float y ) {
        Log.d(TAG, "doOnClickUp: mWidth=" + mWidth + ", mHeight=" + mHeight + ", x=" + x + ", y=" + y + ", mYuanWidth = " + mYuanWidth);
        x = x - mYuanWidth/2;
        if (x < 0) {
            x = 0;
        } else if(x > mWidth - mYuanWidth) {
            x = mWidth - mYuanWidth;
        }

        y = y - mYuanHeight/2;
        if (y < 0) {
            y = 0;
        } else if(y > mHeight - mYuanHeight) {
            y = mHeight - mYuanHeight;
        }
        float balance = (x/(mWidth - mYuanWidth))*FADE_BALANCE_TOTAL - FADE_BALANCE_TOTAL/2;
        float fade = (y/(mHeight - mYuanHeight))*FADE_BALANCE_TOTAL - FADE_BALANCE_TOTAL/2;

        Log.d(TAG, "doOnClickUp: balance=" + balance + ", fade=" + fade);
        for (OnClickUpCallback onClickUpCallback : mOnClickUpCallbacks) {
            onClickUpCallback.onClickUp((int)balance, (int)fade);
        }
    }

    public SoundTouchView(Context context) {
        this(context, null);
    }

    public SoundTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBitmapYuan = BitmapFactory.decodeResource(context.getResources(), R.drawable.yuandian);
        mYuanWidth = mBitmapYuan.getWidth();
        mYuanHeight = mBitmapYuan.getHeight();

        mBitmapBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.chair);
        mWidth = mBitmapBg.getWidth();
        mHeight = mBitmapBg.getHeight();

        mBitmapLineH = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio_line_h);
        mBitmapLineV = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio_line_v);
        mLineWidth = mBitmapLineV.getWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getWidth();
            mHeight = getHeight();
        }
        if (mTouchX == -1) {
            mTouchX = mWidth / 2f;
        }
        if (mTouchY == -1) {
            mTouchY = mHeight / 2f;
        }
        Log.d(TAG, "onDraw: mWidth=" + mWidth + ", mHeight=" + mHeight + ", mTouchX=" + mTouchX + ", mTouchY=" + mTouchY);
        drawLineH(canvas, mPaint);
        drawLineV(canvas, mPaint);
        drawYuan(canvas, mPaint);
    }

    private void drawLineV(Canvas canvas, Paint paint) {
        float left = mTouchX - mLineWidth/2;
        if (left < mYuanWidth/2 - mLineWidth/2) {
            left = mYuanWidth/2 - mLineWidth/2;
        } else if (left > mWidth - (mYuanWidth/2 + mLineWidth/2)){
            left = mWidth - (mYuanWidth/2 + mLineWidth/2);
        }
        canvas.drawBitmap(mBitmapLineV, left, 0, mPaint);
    }

    private void drawLineH(Canvas canvas, Paint paint) {
        float top = mTouchY - mLineWidth/2;
        if (top < mYuanHeight/2 - mLineWidth/2) {
            top = mYuanHeight/2 - mLineWidth/2;
        } else if (top > mHeight - (mYuanHeight/2 + mLineWidth/2)){
            top = mHeight - (mYuanHeight/2 + mLineWidth/2);
        }
        canvas.drawBitmap(mBitmapLineH, 0, top, mPaint);
    }

    /**
     * 绘制手指触摸处的圆点
     * @param canvas
     * @param paint
     */
    private void drawYuan(Canvas canvas, Paint paint) {
        float left = mTouchX - mYuanWidth/2;
        if (left < 0) {
            left = 0;
        } else if (left > mWidth - mYuanWidth){
            left = mWidth - mYuanWidth;
        }
        float top = mTouchY - mYuanHeight/2;
        if (top < 0) {
            top = 0;
        } else if (top > mHeight - mYuanHeight){
            top = mHeight - mYuanHeight;
        }
        canvas.drawBitmap(mBitmapYuan, left, top, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        mTouchX = event.getX();
        mTouchY = event.getY();
        invalidate();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                doOnClickUp(mTouchX, mTouchY);
                break;
            default:
                Log.d(TAG, "onTouchEvent: ");
                break;
        }
        return true;
    }

    /**
     * 复位，圆点回到中间位置
     */
    public void reset() {
        mTouchX = mWidth/2f;
        mTouchY = mHeight/2f;
        invalidate();
        /*2018.5.23 修改reset后，声场没有实际复位的问题*/
        doOnClickUp(mTouchX, mTouchY);
    }

    public void setPosition(int x, int y) {
        mTouchX = x;
        mTouchY = y;
        invalidate();
        doOnClickUp(mTouchX, mTouchY);
    }

    public void setPositionByFaderAndBalance(int balance, int fader) {
        Log.d(TAG, "setPositionByFaderAndBalance: balance=" + balance + ", fade=" + fader);

        float x = ((mWidth - mYuanWidth) * (balance + FADE_BALANCE_TOTAL/2))/FADE_BALANCE_TOTAL;
        float y = ((mHeight - mYuanHeight) * (fader + FADE_BALANCE_TOTAL/2))/FADE_BALANCE_TOTAL;
        x = x + mYuanWidth/2;
        y = y + mYuanHeight/2;
        if (x < 0) {
            x = 0;
        } else if(x > mWidth - mYuanWidth) {
            x = mWidth - mYuanWidth;
        }

        if (y < 0) {
            y = 0;
        } else if(y > mHeight - mYuanHeight) {
            y = mHeight - mYuanHeight;
        }
        Log.d(TAG, "setPositionByFaderAndBalance: x=" + x + ", y=" + y);
        
        mTouchX = x;
        mTouchY = y;
        invalidate();
    }
}
