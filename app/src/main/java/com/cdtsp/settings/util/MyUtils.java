package com.cdtsp.settings.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.widget.Toast;

import one.cluster.ClusterInteractive;

/**
 * Created by Administrator on 2018/2/27.
 */

public class MyUtils {
    public static Toast sToast;
    public static void toast(Context context, String text) {
//        if (sToast == null) {
//            sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
//        }
//        sToast.setText(text);
//        sToast.show();
    }

    /**
     * 判断ScanResult是否加密
     * @param result
     * @return
     */
    public static boolean isLocked(ScanResult result) {
        return result.capabilities.contains("WEP")
                || result.capabilities.contains("PSK")
                || result.capabilities.contains("EAP");
    }

    //Add-S-HMI-2018-03-22，通过DPC向仪表发送数据
    public static ClusterInteractive.CModuleCoverInfo.Builder createCoverInfo(String coverName, String coverPath, int coverWidth, int coverHeight, String coverFormart) {
        ClusterInteractive.CModuleCoverInfo.Builder coverInfoBuilder = ClusterInteractive.CModuleCoverInfo.newBuilder();
        coverInfoBuilder.setName(coverName);
        coverInfoBuilder.setPath(coverPath);
        coverInfoBuilder.setWidth(coverWidth);
        coverInfoBuilder.setHeight(coverHeight);
        coverInfoBuilder.setFormat(coverFormart);
        return coverInfoBuilder;
    }

    public static ClusterInteractive.CModuleCoverPosition.Builder createCoverPosition(int corX, int corY) {
        ClusterInteractive.CModuleCoverPosition.Builder positionBuilder = ClusterInteractive.CModuleCoverPosition.newBuilder();
        positionBuilder.setX(corX);
        positionBuilder.setY(corY);
        return positionBuilder;
    }

    public static ClusterInteractive.CModuleInterActive.Builder createModuleInteractive(ClusterInteractive.eModuleChangeState state, ClusterInteractive.CModuleCoverInfo info, ClusterInteractive.CModuleCoverPosition coverposition) {
        ClusterInteractive.CModuleInterActive.Builder interactiveBuilder = ClusterInteractive.CModuleInterActive.newBuilder();
        interactiveBuilder.setState(state);
        interactiveBuilder.setInfo(info);
        interactiveBuilder.setPosition(coverposition);
        return interactiveBuilder;
    }

    public static ClusterInteractive.CTheme.Builder createClusterTheme(int theme) {
        ClusterInteractive.CTheme.Builder themBuilder = ClusterInteractive.CTheme.newBuilder();
        themBuilder.setTheme(theme);
        return themBuilder;
    }
    //Add-E-HMI-2018-03-22，通过DPC向仪表发送数据

}
