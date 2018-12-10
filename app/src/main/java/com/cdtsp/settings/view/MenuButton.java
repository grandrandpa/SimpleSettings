package com.cdtsp.settings.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;

import com.cdtsp.settings.R;


/**
 * Created by Administrator on 2018/1/18.
 */

public class MenuButton extends FrameLayout {

    private CheckedTextView mTextView;
    public MenuButton(@NonNull Context context) {
        this(context, null);
    }

    public MenuButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //获取属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MenuButton);
        String text = ta.getString(R.styleable.MenuButton_text);
        int drawableResId = ta.getResourceId(R.styleable.MenuButton_drawable, -1);
        int textSize = ta.getDimensionPixelSize(R.styleable.MenuButton_textSize, 20);
        ta.recycle();

        //初始化文字控件
        mTextView = new CheckedTextView(context);
        mTextView.setTextColor(getResources().getColorStateList(R.color.selector_text_color, null));
        if (drawableResId != -1) {
            Drawable compoundDrawable = ContextCompat.getDrawable(context, drawableResId);
            compoundDrawable.setBounds(0, 0, compoundDrawable.getIntrinsicWidth(), compoundDrawable.getIntrinsicHeight());
            mTextView.setCompoundDrawables(compoundDrawable, null, null, null);
            mTextView.setCompoundDrawablePadding(
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            20,
                            getResources().getDisplayMetrics()
                    )
            );
        }
        mTextView.setTextSize(textSize);
        if (text != null) {
            mTextView.setText(text);
        }
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        setPadding(30, 0, 0, 0);

        //添加文字控件
        addView(mTextView, params);

        //设置本控件背景
        setBackgroundResource(R.drawable.selector_bg_menu_btn);
    }

    /**
     * 设置MenuButton为选中状态
     * @param checked
     */
    public void setChecked(Boolean checked) {
        setSelected(checked);
        mTextView.setChecked(checked);
    }
}
