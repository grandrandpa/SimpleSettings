package com.cdtsp.settings.util;

import android.app.Activity;
import android.content.pm.PackageManager;

/**
 * Created by Administrator on 2017/12/4.
 */

public class PermissionHelper {
    private String[] mAuthArr;
    private Activity mActivity;

    public PermissionHelper(Activity activity) {
        this.mActivity = activity;
    }

    public void setPermNeedToRequest(String[] authArr) {
        this.mAuthArr = authArr;
    }
    public boolean checkMyPermissions() {
        if (mAuthArr == null) {
            throw new RuntimeException("mAuthArr has not been initialized !");
        }
        PackageManager pm = mActivity.getPackageManager();
        for (String auth : mAuthArr) {
            if (pm.checkPermission(auth, mActivity.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public void getMyPermissions(int requestCode) {
        mActivity.requestPermissions(mAuthArr, requestCode);
    }
}
