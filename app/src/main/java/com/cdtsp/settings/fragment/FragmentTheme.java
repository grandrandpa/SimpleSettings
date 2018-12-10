package com.cdtsp.settings.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdtsp.settings.R;
import com.cdtsp.settings.SettingsActivity;
import com.cdtsp.settings.SettingsApplication;
import com.cdtsp.settings.util.MyUtils;

import java.util.ArrayList;

import one.cluster.ClusterInteractive;
import android.provider.Settings;
import android.widget.ImageView;

/**
 * Created by Administrator on 2018/3/5.
 */

public class FragmentTheme extends Fragment implements SettingsActivity.OnSwitchThemeCallback {

    private final String TAG = FragmentTheme.class.getSimpleName();
    private ArrayList<View> mThemeViews = new ArrayList<>();
//    private int mCurThemePos;
    private String mCurrentTheme;
    //Mod-S-HMI-2018-03-20，拖拽实现主题切换功能
//    private ArrayList<View> mThemeSevViews = new ArrayList<>();
    private ArrayList<ClusterThemeInfo> mThemeInfos = new ArrayList<>();
    //Mod-E-HMI-2018-03-20，拖拽实现主题切换功能
    private int mCurThemeSevPos = -1;
    private int mTempCurThemePos;
    private View mThemeDefaultView;
    private ImageView mThemeDefaultIcon;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        settingsActivity.setOnSwitchThemeCallback(this);
        getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.SYSTEM_THEME),
                true,mThemeObserver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().getContentResolver().unregisterContentObserver(mThemeObserver);
    }

    private void initViews(View view) {
        mCurrentTheme = Settings.Global.getString(getContext().getContentResolver(), Settings.Global.SYSTEM_THEME);
        mThemeDefaultView = view.findViewById(R.id.theme_default);
        mThemeDefaultIcon = view.findViewById(R.id.theme_default_icon);
        updateThemeView();
        View themeBlueView = view.findViewById(R.id.theme_blue);
        themeBlueView.setTag(Settings.Global.THEME_BLUE);
        mThemeViews.add(mThemeDefaultView);
        mThemeViews.add(themeBlueView);
        int countThemeViews = mThemeViews.size();

        for (int i = 0; i < countThemeViews; i++) {
            View v = mThemeViews.get(i);
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String theme = (String)v.getTag();
                    if (theme.equals(mCurrentTheme)) {
                        return;
                    }
                    resetAllTheme();
                    v.setSelected(true);
                    switchTheme(theme);
                }
            });
            if(mCurrentTheme.equals((String)v.getTag())){
                resetAllTheme();
                v.setSelected(true);
            }
        }

        View themeClassSevView = view.findViewById(R.id.theme_class_sev);
        //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
        mThemeInfos.add(new ClusterThemeInfo(themeClassSevView, R.drawable.theme_cluster_1, R.drawable.theme_cluster_1, ClusterInteractive.eTheme.THEME_BMW_SPORT_VALUE));
        //Add-E-HMI-2018-03-20，拖拽实现主题切换功能
        View themeSportSevView = view.findViewById(R.id.theme_sport_sev);
        //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
        mThemeInfos.add(new ClusterThemeInfo(themeSportSevView, R.drawable.theme_cluster_2, R.drawable.theme_cluster_2, ClusterInteractive.eTheme.THEME_METAL_WARRIOR_VALUE));
        //Add-E-HMI-2018-03-20，拖拽实现主题切换功能
        //Mod-S-HMI-2018-03-20，拖拽实现主题切换功能
//        mThemeSevViews.add(themeClassSevView);
//        mThemeSevViews.add(themeSportSevView);
//        int countThemeSevViews = mThemeSevViews.size();
        int countThemeSevViews = mThemeInfos.size();
        //Mod-E-HMI-2018-03-20，拖拽实现主题切换功能

        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        int clusterTheme = settingsActivity.getClusterTheme(new Runnable() {
            @Override
            public void run() {
                int clusterTheme = settingsActivity.getClusterTheme(this);
                Log.d(TAG, "run: getClusterTheme again, clusterTheme = " + clusterTheme);
                for (int i = 0; i < countThemeSevViews; i++) {
                    ClusterThemeInfo themeInfo = mThemeInfos.get(i);
                    if (themeInfo.theme == clusterTheme) {
                        mCurThemeSevPos = i;
                        themeInfo.view.setSelected(true);
                    }
                }
            }
        });
        Log.d(TAG, "initViews: clusterTheme = " + clusterTheme);

        for (int i = 0; i < countThemeSevViews; i++) {
            final int finalI = i;
            ClusterThemeInfo themeInfo = mThemeInfos.get(i);
            //Mod-S-HMI-2018-03-20，拖拽实现主题切换功能
//            View v = mThemeSevViews.get(i);
            View v = themeInfo.view;
            //Mod-E-HMI-2018-03-20，拖拽实现主题切换功能
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (finalI == mCurThemeSevPos) {
                        return;
                    }
                    mTempCurThemePos = finalI;
                    //Mod-S-HMI-2018-03-20，拖拽实现主题切换功能
//                    switchThemeSev();
                    switchThemeSev(mThemeInfos.get(mTempCurThemePos));
                    //Mod-E-HMI-2018-03-20，拖拽实现主题切换功能
                }
            });
            if (themeInfo.theme == clusterTheme) {
                mCurThemeSevPos = i;
                v.setSelected(true);
            }
        }
    }

    private void resetAllTheme(){
        for(View view:mThemeViews){
            view.setSelected(false);
        }
    }

    /**
     * 切换中控主题
     */
    private void switchTheme(String theme) {
//        MyUtils.toast(getContext().getApplicationContext(), "切换中控主题");
        //TODO
        Settings.Global.putString(getContext().getContentResolver(), Settings.Global.SYSTEM_THEME, theme);
        mCurrentTheme = theme;
    }


    /**
     * 切换仪表主题
     * @param clusterThemeInfo
     */
    //Mod-S-HMI-2018-03-20，拖拽实现主题切换功能
//    private void switchThemeSev() {
    private void switchThemeSev(ClusterThemeInfo clusterThemeInfo) {
    //Mod-E-HMI-2018-03-20，拖拽实现主题切换功能
        MyUtils.toast(getContext().getApplicationContext(), "切换仪表主题");
        //TODO
        //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        settingsActivity.addFloatView(clusterThemeInfo);
        //Add-E-HMI-2018-03-20，拖拽实现主题切换功能
    }

    @Override
    public void onSwitchTheme() {
        mThemeInfos.get(mTempCurThemePos).view.setSelected(true);
        mThemeInfos.get(mCurThemeSevPos).view.setSelected(false);
        mCurThemeSevPos = mTempCurThemePos;
        mTempCurThemePos = -1;
    }

    private void updateThemeView(){
        int device = Settings.Global.getInt(getContext().getContentResolver(),
                Settings.Global.DEVICE_CODE, Settings.Global.DEVICE_DEFAULT);
        if(device == Settings.Global.DEVICE_HONE_QI){
            mThemeDefaultView.setTag(Settings.Global.THEME_HONE_QI);
            mThemeDefaultIcon.setImageResource(R.drawable.theme_hong_qi_preview);
        }else{
            mThemeDefaultView.setTag(Settings.Global.THEME_DEFAULT);
            mThemeDefaultIcon.setImageResource(R.drawable.theme_default_preview);
        }
    }

    private ContentObserver mThemeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mCurrentTheme = Settings.Global.getString(getContext().getContentResolver(), Settings.Global.SYSTEM_THEME);
            updateThemeView();
            resetAllTheme();
            for (int i = 0; i < mThemeViews.size(); i++) {
                View v = mThemeViews.get(i);
                if(mCurrentTheme.equals((String)v.getTag())){
                    v.setSelected(true);
                }
            }
        }
    };

    //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
    public class ClusterThemeInfo {
        public View view;
        public int smallIconResId;
        public int largeIconResId;
        public int theme;

        public ClusterThemeInfo(View view, int smallIconResId, int largeIconResId, int theme) {
            this.view = view;
            this.smallIconResId = smallIconResId;
            this.largeIconResId = largeIconResId;
            this.theme = theme;
        }
    }
    //Add-S-HMI-2018-03-20，拖拽实现主题切换功能
}
