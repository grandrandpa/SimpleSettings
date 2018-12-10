package com.cdtsp.settings;

import android.app.Application;
import com.cdtsp.hmilib.skin.SkinChangeHelper;

public class SettingsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initSkinLoader();
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        // 初始化皮肤框架
        SkinChangeHelper.getInstance(getApplicationContext()).init(this);
        //初始化上次缓存的皮肤
        SkinChangeHelper.getInstance(getApplicationContext()).refreshSkin(null);
    }
}
