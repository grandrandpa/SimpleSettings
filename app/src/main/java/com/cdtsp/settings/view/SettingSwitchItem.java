package com.cdtsp.settings.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/2/27.
 */

public class SettingSwitchItem extends RelativeLayout {
    private static final String TAG = "SettingSwitchItem";
    private Context mContext;
    private TextView mTvSubTitle;
    private TextView mTvStatus;
    private ImageView mImgSwitch;

    public SettingSwitchItem(Context context) {
        this(context, null);
    }

    public SettingSwitchItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        //获取属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingSwitchItem);
        String title = ta.getString(R.styleable.SettingSwitchItem_title);
        String subTitle = ta.getString(R.styleable.SettingSwitchItem_subtitle);
        String statusText = ta.getString(R.styleable.SettingSwitchItem_subtitle);
        int textSize = ta.getDimensionPixelSize(R.styleable.SettingSwitchItem_textSize, 20);
        int switchDrawableResId = ta.getResourceId(R.styleable.SettingSwitchItem_switchDrawable, -1);
        Drawable switchDrawable = context.getResources().getDrawable(switchDrawableResId, null);
        ta.recycle();

        //添加标题
        addTitleView(title, textSize);

        //添加Switch图标
        addSwitchDrawable(switchDrawable);

        if (subTitle != null) {
            addSubTitleView(subTitle);
        }

        if (statusText != null) {
            addStatusTextView(statusText);
        }
    }

    /**
     * 添加Switch图标Drawable
     * @param switchDrawable
     */
    private void addSwitchDrawable(Drawable switchDrawable) {
        mImgSwitch = new ImageView(mContext);
        mImgSwitch.setImageDrawable(switchDrawable);
        LayoutParams paramsImageView = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);//靠右摆放
        addView(mImgSwitch, paramsImageView);

        mImgSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchStateTo(!v.isSelected());
            }
        });
    }

    /**
     * 改变Switch状态
     * @param on
     */
    public void switchStateTo(boolean on) {
        mImgSwitch.setSelected(on);

        //若设置了Switch状态的回调接口，那么执行回调
        if (mOnSwitchCallback != null) {
            if (mImgSwitch.isSelected()) {
                mOnSwitchCallback.onSwitchOn();
            } else {
                mOnSwitchCallback.onSwitchOff();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mImgSwitch.setEnabled(enabled);
    }

    /**
     * 添加标题View
     * @param titleStr
     * @param textSize
     */
    private void addTitleView(String titleStr, int textSize) {
        TextView textView = new TextView(mContext);
        textView.setId(R.id.title_id);
        textView.setText(titleStr);
        textView.setTextSize(textSize);
        textView.setTextColor(Color.GRAY);
        addView(textView);
    }

    /**
     * 添加子标题View
     * @param subTitle
     */
    private void addSubTitleView(String subTitle) {
        mTvSubTitle = new TextView(mContext);
        mTvSubTitle.setText(subTitle);
        mTvSubTitle.setTextSize(30);
        mTvSubTitle.setTextColor(Color.DKGRAY);
        LayoutParams paramsTextView = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTextView.addRule(RelativeLayout.BELOW, R.id.title_id);//靠右摆放
        addView(mTvSubTitle, paramsTextView);
    }

    public void setSubTitleText(String text) {
        mTvSubTitle.setText(text);
    }

    /**
     * 添加状态View
     * @param text
     */
    private void addStatusTextView(String text) {
        mTvStatus = new TextView(mContext);
        mTvStatus.setText(text);
        mTvStatus.setTextSize(30);
        mTvStatus.setTextColor(Color.LTGRAY);
        mTvStatus.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams paramsTextView = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTextView.leftMargin = 50;
        paramsTextView.addRule(RelativeLayout.RIGHT_OF, R.id.title_id);//靠右摆放
        paramsTextView.addRule(RelativeLayout.ALIGN_TOP, R.id.title_id);
        paramsTextView.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.title_id);
        addView(mTvStatus, paramsTextView);
        mTvStatus.setVisibility(INVISIBLE);
    }

    public void showStatusText(String text) {
        mTvStatus.setText(text);
        mTvStatus.setVisibility(VISIBLE);
    }

    public void hideStatusText() {
        mTvStatus.setVisibility(INVISIBLE);
        mTvStatus.setText("");
    }

    private OnSwitchCallback mOnSwitchCallback;
    public void registerSwitchCallback(OnSwitchCallback onSwitchCallback) {
        this.mOnSwitchCallback = onSwitchCallback;
    }
    public interface OnSwitchCallback {
        void onSwitchOn();
        void onSwitchOff();
    }
}
