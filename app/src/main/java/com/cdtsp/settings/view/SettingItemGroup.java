package com.cdtsp.settings.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/2/26.
 */
public class SettingItemGroup extends LinearLayout {

    private Context mContext;
    private int mCurSelectedPos;
    private LinearLayout mItemLayout;

    public SettingItemGroup(Context context) {
        this(context, null);
    }

    public SettingItemGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(VERTICAL);

        //获取属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemGroup);
        String titleStr = ta.getString(R.styleable.SettingItemGroup_title);
        int textSize = ta.getDimensionPixelSize(R.styleable.SettingItemGroup_textSize, 20);
        int optArrayResId = ta.getResourceId(R.styleable.SettingItemGroup_optArray, -1);
        String[] stringArray = context.getResources().getStringArray(optArrayResId);
        int count = stringArray.length;
        ta.recycle();

        addTitleView(titleStr, textSize);

        addItemLayout();

        addItemBtn(textSize, stringArray[0], R.drawable.selector_comm_btn_left);
        if (count > 2) {
            for (int i = 1; i < count-1; i++) {
                addItemBtn(textSize, stringArray[i], R.drawable.selector_comm_btn_mid);
            }
        }
        addItemBtn(textSize, stringArray[count-1], R.drawable.selector_comm_btn_right);

        //为每个item设置点击事件
        for (int i = 0; i < count; i++) {
            final TextView ctv = (TextView) mItemLayout.getChildAt(i);
            final int pos = i;
            ctv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemLayout.getChildAt(mCurSelectedPos).setSelected(false);
                    ctv.setSelected(true);
                    mCurSelectedPos = pos;
                    if (mOnItemClickCallback != null) {
                        mOnItemClickCallback.onItemClick(mCurSelectedPos);
                    }
                }
            });
        }

        //设置初始状态的选中item
        mCurSelectedPos = 0;
        mItemLayout.getChildAt(mCurSelectedPos).setSelected(true);
    }

    /**
     * 添加item按钮
     * @param textSize
     * @param text
     * @param bgResId
     */
    private void addItemBtn(int textSize, String text, int bgResId) {
        TextView textViewLeft = createItemBtn(textSize, text, bgResId);
        LinearLayout.LayoutParams paramsTextViewLeft = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTextViewLeft.weight = 1;
        mItemLayout.addView(textViewLeft, paramsTextViewLeft);
    }

    /**
     * 创建Item按钮
     * @param textSize
     * @param text
     * @param bgResId
     * @return
     */
    private TextView createItemBtn(int textSize, String text, int bgResId) {
        TextView textView = new TextView(mContext);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(this.mContext.getResources().getColor(R.color.selector_text_color, null));
        textView.setBackgroundResource(bgResId);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    /**
     * 添加item容器布局
     */
    private void addItemLayout() {
        mItemLayout = new LinearLayout(mContext);
        mItemLayout.setOrientation(HORIZONTAL);
        mItemLayout.setPadding(0, 0, 0, 10);
        ViewGroup.LayoutParams paramsLayout = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mItemLayout, paramsLayout);
    }

    /**
     * 添加标题View
     * @param titleStr
     * @param textSize
     */
    private void addTitleView(String titleStr, int textSize) {
        TextView titleTextView = new TextView(mContext);
        titleTextView.setText(titleStr);
        titleTextView.setTextSize(textSize);
        titleTextView.setTextColor(Color.GRAY);
        titleTextView.setPadding(0, 0, 0, 10);
        addView(titleTextView);
    }

    /**
     * 设置选中项
     * @param pos
     */
    public void setSelected(int pos) {
        if (pos == mCurSelectedPos) {
            return;
        }
        mItemLayout.getChildAt(mCurSelectedPos).setSelected(false);
        mItemLayout.getChildAt(pos).setSelected(true);
        mCurSelectedPos = pos;
    }

    private OnItemClickCallback mOnItemClickCallback;
    public void setOnItemClickCallback(OnItemClickCallback onItemClickCallback) {
        this.mOnItemClickCallback = onItemClickCallback;
    }
    public interface OnItemClickCallback {
        void onItemClick(int curSelectedPos);
    }
}
