package com.cdtsp.settings.fragment;

import android.car.tsp.TspCarInfoManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cdtsp.settings.R;
import com.cdtsp.settings.SettingsActivity;
import com.cdtsp.settings.view.SettingItemGroup;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingDisplay extends Fragment{

    private static final String TAG = "FragmentSettingDisplay";
    private final int MAX = 10;
    private WindowManager.LayoutParams mAttributes;
    private Window mWindow;
    private SeekBar mSeekBar;
    private TextView mMaxValueView;
    private TextView mTitleView;
    private SettingItemGroup mOffScreenGroup;
    private ImageView mImgTag;
    private final int[] mTagResId = new int[] {
            R.drawable.xitong_bg_digital,
            R.drawable.xitong_bg_analog,
            R.drawable.xitong_bg_none
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_display, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWindow = getActivity().getWindow();
        mAttributes = mWindow.getAttributes();

        initViews(view);
    }

    private void initViews(View view) {
        initViewLightnessSetting(view);
        initViewTag(view);
        initViewOffScreen(view);
    }

    /**
     * 初始化右侧的Tag图片
     * @param view
     */
    private void initViewTag(View view) {
        mImgTag = view.findViewById(R.id.tag);
        mImgTag.setImageResource(mTagResId[0]);
    }

    /**
     * 初始化关屏相关View
     * @param view
     */
    private void initViewOffScreen(View view) {
        view.findViewById(R.id.turn_off_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        mOffScreenGroup = view.findViewById(R.id.off_screen_opt_group);
        mOffScreenGroup.setOnItemClickCallback(new SettingItemGroup.OnItemClickCallback() {
            @Override
            public void onItemClick(int curSelectedPos) {
                mImgTag.setImageResource(mTagResId[curSelectedPos]);
            }
        });
    }

    /**
     * 初始化亮度设置相关的View
     * @param view
     */
    private void initViewLightnessSetting(View view) {
        mTitleView = view.findViewById(R.id.lightness);
        mMaxValueView = view.findViewById(R.id.max);
        mSeekBar = view.findViewById(R.id.seekbar);

        mMaxValueView.setText(String.valueOf(MAX));
        mSeekBar.setMax(MAX);

        //获取当前的亮度值，并根据MAX计算出seekbar当前的进度
        int systemBrightness = getSystemBrightness();
        int progress = (int) (systemBrightness * 1.0f / TspCarInfoManager.MAX_DISPLAY_BRIGHTNESS * MAX);
        mSeekBar.setProgress(progress);
        mTitleView.setText(getResources().getString(R.string.brightness) + ":" + String.valueOf(progress));

        //为seekbar设置监听
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeBrightness(progress);
                mTitleView.setText(getResources().getString(R.string.brightness) + ":" + String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });
    }

    /**
     * 修改亮度
     * @param progress
     */
    private void changeBrightness(int progress) {
//        if (mAttributes.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
//            //说明是此时亮度是跟随系统的，这时应将mAttributes.screenBrightness设置称系统亮度
//            mAttributes.screenBrightness = getSystemBrightness() / 255f;
//            Log.d(TAG, "changeBrightness: mAttributes.screenBrightness=" + mAttributes.screenBrightness);
//        } else {
//            mAttributes.screenBrightness = progress * 255f / 100;
//        }

//        mAttributes.screenBrightness = progress * 255f / MAX;
//        if (mAttributes.screenBrightness >= 0f && mAttributes.screenBrightness <= 1f) {
//            mWindow.setAttributes(mAttributes);
//        }
        ((SettingsActivity)getActivity()).setDisplayBrightness((int) (progress * 100f / MAX));
    }

    /**
     * 获取系统的亮度
     * @return 返回的值0~100
     */
    private int getSystemBrightness() {
//        int systemBrightness = 0;
//        try {
//            systemBrightness = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
//        return systemBrightness;
        return ((SettingsActivity)getActivity()).getDisplayBrightness();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
