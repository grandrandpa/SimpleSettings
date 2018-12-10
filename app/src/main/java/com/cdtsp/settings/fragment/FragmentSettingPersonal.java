package com.cdtsp.settings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdtsp.settings.R;
import com.cdtsp.settings.view.TextViewTag;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingPersonal extends Fragment implements View.OnClickListener {

    private static final String TAG = "FragmentSettingPersonal";
    private FragmentManager mFragmentManager;
    private TextViewTag mTvTagTheme, mTvTagWallpaer;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_personal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {

        initViewTitleTag(view);
    }

    private void initViewTitleTag(View view) {
        mTvTagTheme = view.findViewById(R.id.tv_tag_theme);
        mTvTagWallpaer = view.findViewById(R.id.tv_tag_wallpaer);

        mTvTagTheme.setOnClickListener(this);
        mTvTagWallpaer.setOnClickListener(this);

        //初始状态，显示主题页面
        mTvTagTheme.setSelected(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragmentManager = getFragmentManager();

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        FragmentTheme fragmentTheme = new FragmentTheme();
        transaction.add(R.id.container_personal, fragmentTheme, fragmentTheme.getClass().getSimpleName());
        FragmentWallpaper fragmentWallpaper = new FragmentWallpaper();
        transaction.add(R.id.container_personal, fragmentWallpaper, fragmentWallpaper.getClass().getSimpleName());
        transaction.hide(fragmentWallpaper);
        transaction.commit();
        mCurTag = fragmentTheme.getClass().getSimpleName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tag_theme:
                switchFragmentTo(FragmentTheme.class.getSimpleName());
                mTvTagTheme.setSelected(true);
                mTvTagWallpaer.setSelected(false);
                break;
            case R.id.tv_tag_wallpaer:
                switchFragmentTo(FragmentWallpaper.class.getSimpleName());
                mTvTagTheme.setSelected(false);
                mTvTagWallpaer.setSelected(true);
                break;
            default:
                Log.d(TAG, "onClick: default");
                break;
        }
    }

    private String mCurTag;
    private void switchFragmentTo(String toTag) {
        if (mCurTag != null && mCurTag.equals(toTag)) {
            return;
        }
        Fragment curFragment = mFragmentManager.findFragmentByTag(mCurTag);
        Fragment toFragment = mFragmentManager.findFragmentByTag(toTag);
        if (toFragment == null) {
            Log.d(TAG, "switchFragmentTo: switchFragmentTo " + toTag + " failed ! toFragment=" + toFragment);
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(curFragment);
        transaction.show(toFragment);
        transaction.commit();

        mCurTag = toTag;
    }
}
