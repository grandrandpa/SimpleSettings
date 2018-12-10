package com.cdtsp.settings.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/2/26.
 */
public class SettingProgressGroup extends LinearLayout {

    private Context mContext;
    private int mCurSelectedPos;
    private LinearLayout mSeekBarLayout;
    private MySeekBar mSeekBar;
    private TextView mStartValueView;
    private TextView mEndValueView;
    private TextView mTitleTextView;

    public SettingProgressGroup(Context context) {
        this(context, null);
    }

    public SettingProgressGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);

        //获取属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingProgressGroup);
        String titleStr = ta.getString(R.styleable.SettingProgressGroup_title);
        int textSize = ta.getDimensionPixelSize(R.styleable.SettingProgressGroup_textSize, 20);
        int progressDrawableResId = ta.getResourceId(R.styleable.SettingProgressGroup_progressStyle, -1);
        Drawable progressDrawable = mContext.getResources().getDrawable(progressDrawableResId, null);
        int thumbDrawableResId = ta.getResourceId(R.styleable.SettingProgressGroup_thumbDrawable, -1);
        Drawable thumbDrawable = null;
        if (thumbDrawableResId != -1) {
            thumbDrawable = mContext.getResources().getDrawable(thumbDrawableResId, null);
        }
        ta.recycle();

        //添加标题
        addTitleView(titleStr, textSize);

        addSeekBarLayout();

        mStartValueView = addValueView(textSize, String.valueOf(0));
        addSeekBar(progressDrawable, thumbDrawable);
        mEndValueView = addValueView(textSize, String.valueOf(100));
    }

    private void addSeekBar(Drawable progressDrawable, Drawable thumbDrawable) {
        mSeekBar = new MySeekBar(mContext);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(45);
        mSeekBar.setProgressDrawable(progressDrawable);
        if (thumbDrawable != null) {
            mSeekBar.setThumb(thumbDrawable);
        }
        LinearLayout.LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        params.gravity = Gravity.CENTER_VERTICAL;
        mSeekBarLayout.addView(mSeekBar, params);
    }

    /**
     * 添加item按钮
     * @param textSize
     * @param text
     */
    private TextView addValueView(int textSize, String text) {
        TextView valueView = createValueView(35, text);
        LinearLayout.LayoutParams params = new LayoutParams(65, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        mSeekBarLayout.addView(valueView, params);
        return valueView;
    }

    /**
     * 创建Item按钮
     * @param textSize
     * @param text
     * @return
     */
    private TextView createValueView(int textSize, String text) {
        TextView textView = new TextView(mContext);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(this.mContext.getResources().getColor(R.color.selector_text_color, null));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * 添加item容器布局
     */
    private void addSeekBarLayout() {
        mSeekBarLayout = new LinearLayout(mContext);
        mSeekBarLayout.setOrientation(HORIZONTAL);
        ViewGroup.LayoutParams paramsLayout = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mSeekBarLayout, paramsLayout);
    }

    /**
     * 添加标题View
     * @param titleStr
     * @param textSize
     */
    private void addTitleView(String titleStr, int textSize) {
        mTitleTextView = new TextView(mContext);
        mTitleTextView.setText(titleStr + ": 0");
        mTitleTextView.setTextSize(textSize);
        mTitleTextView.setTextColor(Color.GRAY);
        addView(mTitleTextView);
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress);
        setCurValue(progress);
    }
    public void setMax(int max) {
        mSeekBar.setMax(max);
        setEndValue(String.valueOf(max));
    }

    public void setEndValue(String text) {
        mEndValueView.setText(text);
    }

    public void setStartValue(String text) {
        mStartValueView.setText(text);
    }

    public void setCurValue(int value) {
        String text = (String) mTitleTextView.getText();
        int index = text.lastIndexOf(":");
        text = text.substring(0, index + 1) + " " + String.valueOf(value);
        mTitleTextView.setText(text);
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void disableDrag() {
        mSeekBar.disable();
    }
    public void enableDrag() {
        mSeekBar.enable();
    }
    public boolean isDisable() {
        return mSeekBar.isDisable();
    }
}
