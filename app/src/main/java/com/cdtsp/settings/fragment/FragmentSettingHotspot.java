package com.cdtsp.settings.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.text.InputType;
import static android.net.ConnectivityManager.TETHERING_WIFI;
import com.cdtsp.settings.SettingsActivity;
import com.cdtsp.settings.view.TspHotspotDialog;
import com.cdtsp.settings.R;
import com.cdtsp.settings.wifi.WifiApEnabler;
import com.cdtsp.settings.util.MyUtils;
import com.cdtsp.settings.util.PermissionHelper;
import com.cdtsp.settings.view.SettingSwitchItem;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingHotspot extends Fragment implements SettingSwitchItem.OnSwitchCallback {
    private static final String TAG = "FragmentSettingHotspot";
    private static final int REQUEST_PERMISSIONS = 0;
    private final int MSG_SHOW_CONNECTED_WIFI = 0;
    private final int MSG_NOTIFY_WIFI_DATA_CHANGED = 1;

    private final String[] AUTH = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private SettingSwitchItem mSettingSwitchItem;
    private ViewGroup mHotspotSetLayout;
    private TspHotspotDialog mSetDialog;

    private WifiManager mWifiManager;
    private WifiApEnabler mWifiApEnabler;
    private ConnectivityManager mCm;
    private WifiConfiguration mWifiConfig = null;

    private OnStartTetheringCallback mStartTetheringCallback;
    private BroadcastReceiver mTetherChangeReceiver;

    private String[] mWifiRegexs;
    private Button mHotspotSwitcher;
    private boolean mRestartWifiApAfterConfigChange;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_hotspot, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTetherChangeReceiver = new TetherChangeReceiver();
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mStartTetheringCallback = new OnStartTetheringCallback(this);

        final Activity activity = getActivity();
        mWifiConfig = mWifiManager.getWifiApConfiguration();
        mWifiRegexs = mCm.getTetherableWifiRegexs();
        mRestartWifiApAfterConfigChange = false;

        initViews(view);
        mWifiApEnabler = new WifiApEnabler(activity, mSettingSwitchItem);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mWifiApEnabler != null) {
            registerWifiReceiver();
            mWifiApEnabler.resume();
        }
    }

    private void initViews(View view) {
        //初始化mSettingSwitchItem
        mSettingSwitchItem = view.findViewById(R.id.switch_hotspot);
        mSettingSwitchItem.registerSwitchCallback(this);
        boolean isWifiEnabled = isWifiApOpen();
        mSettingSwitchItem.switchStateTo(isWifiEnabled);
        mSettingSwitchItem.setSubTitleText(mWifiConfig.SSID.toString());

        mHotspotSetLayout = view.findViewById(R.id.hotspot_set_layout);
        mHotspotSetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHotspotSetWindow();
            }
        });

        mHotspotSwitcher = view.findViewById(R.id.btn_wifi_switcher);
        mHotspotSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "switch to wifi fragment");
                switchToWifiFragment();
            }
        });
    }

    private void initDialogViews() {
        if (mWifiConfig != null) {
            //Log.d(TAG, "initDialogViews SSID:" + mWifiConfig.SSID + ", password: " + mWifiConfig.preSharedKey.toString());
            mSetDialog.setNameInfo(mWifiConfig.SSID.toString());
            mSetDialog.setPassword(mWifiConfig.preSharedKey.toString());
            mSetDialog.setBandType(mWifiConfig.apBand);
        }
    }

    public boolean isWifiApOpen() {
        try {
            //通过放射获取 getWifiApState()方法
            Method method = mWifiManager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(mWifiManager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = mWifiManager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(mWifiManager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 弹出热点设置窗口
     */
    private void showHotspotSetWindow() {
        if (mSetDialog == null) {
            mSetDialog = new TspHotspotDialog(getActivity())
                    .setButton1(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String strHotSpotPS = mSetDialog.getPassword();
                            String strHotSpotName = mSetDialog.getNameInfo();
                            if (strHotSpotPS.isEmpty() || strHotSpotName.isEmpty()) {
                                toast(R.string.hotspot_input_info);
                                return;
                            }

                            mWifiConfig = mSetDialog.getConfig();
                            if (mWifiConfig != null) {
                                /**
                                 * if soft AP is stopped, bring up
                                 * else restart with new config
                                 * TODO: update config on a running access point when framework support is added
                                 */
                                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                                    Log.d(TAG, "Wifi AP config changed while enabled, stop and restart");
                                    mRestartWifiApAfterConfigChange = true;
                                    mCm.stopTethering(TETHERING_WIFI);
                                }
                                Log.d(TAG, "Wifi AP config password:" + mWifiConfig.preSharedKey);
                                mWifiManager.setWifiApConfiguration(mWifiConfig);
                                //int index = TspHotspotDialog.getSecurityTypeIndex(mWifiConfig);
                                //mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
                                //        mWifiConfig.SSID,
                                //        mSecurityType[index]));
                                mSettingSwitchItem.setSubTitleText(mWifiConfig.SSID);
                            }
                            mSetDialog.dismiss();
                        }
                    })
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .setMaxLength(50);
                    //.setButton2(R.string.no);
            initDialogViews();
            mSetDialog.show();
        } else {
            initDialogViews();
            mSetDialog.show();
        }
    }

     private void switchToWifiFragment() {
        SettingsActivity settingsActivity = (SettingsActivity) getActivity();
        settingsActivity.switchToFragmentByName("FragmentSettingNetwork");
    }

    private class TetherChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
                // TODO - this should understand the interface types
//                ArrayList<String> available = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_AVAILABLE_TETHER);
//                ArrayList<String> active = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_ACTIVE_TETHER);
//                ArrayList<String> errored = intent.getStringArrayListExtra(
//                        ConnectivityManager.EXTRA_ERRORED_TETHER);
//                updateState(available.toArray(new String[available.size()]),
//                        active.toArray(new String[active.size()]),
//                        errored.toArray(new String[errored.size()]));
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLED && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    startTethering(TETHERING_WIFI);
                }
            } else if (action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, 0);
                if (state == WifiManager.WIFI_AP_STATE_DISABLED && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    startTethering(TETHERING_WIFI);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerWifiReceiver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterWifiReceiver();
        mWifiApEnabler.pause();
    }

    @Override
    public void onSwitchOn() {
        Log.d(TAG, "onSwitchOn ");
        if (mWifiConfig == null) {
            Log.d(TAG, "onSwitchOn mWifiConfig is null");
            mWifiConfig.SSID = "hotspot";
            mWifiConfig.apBand = 0;
            mWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            mWifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            mWifiConfig.preSharedKey = "12345678";

            mWifiManager.setWifiApConfiguration(mWifiConfig);
            mSettingSwitchItem.setSubTitleText(mWifiConfig.SSID);
        }
        startTethering(TETHERING_WIFI);

        //注册广播接收器
        //registerWifiReceiver();
    }

    @Override
    public void onSwitchOff() {
        Log.d(TAG, "onSwitchOff: ");
        mCm.stopTethering(TETHERING_WIFI);
    }

    private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<FragmentSettingHotspot> mTetherSettings;

        OnStartTetheringCallback(FragmentSettingHotspot settings) {
            mTetherSettings = new WeakReference<FragmentSettingHotspot>(settings);
        }

        @Override
        public void onTetheringStarted() {
            update();
        }

        @Override
        public void onTetheringFailed() {
            update();
        }

        private void update() {
            FragmentSettingHotspot settings = mTetherSettings.get();
            if (settings != null) {
                settings.updateState();
            }
        }
    }

    private void updateState() {
        String[] available = mCm.getTetherableIfaces();
        String[] tethered = mCm.getTetheredIfaces();
        String[] errored = mCm.getTetheringErroredIfaces();
    }

    private void startTethering(int choice) {
        mCm.startTethering(choice, true, mStartTetheringCallback, mHandler);
    }

    /**
     * 注册wifiReceiver
     */
    private void registerWifiReceiver() {
        final Activity activity = getActivity();
        IntentFilter filter = new IntentFilter(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        Intent intent = activity.registerReceiver(mTetherChangeReceiver, filter);
        if (intent != null) {
            mTetherChangeReceiver.onReceive(activity, intent);
        }
    }

    /**
     * 反注册wifiReceiver
     */
    private void unregisterWifiReceiver() {
        getActivity().unregisterReceiver(mTetherChangeReceiver);
        mTetherChangeReceiver = null;
        mStartTetheringCallback = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_PERMISSIONS == requestCode) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: permission granted failed !");
                    return;
                }
            }
            Log.d(TAG, "onRequestPermissionsResult: permission granted success ! now begin scanning wifi again");
        }
    }

    private Handler mHandler = new Handler();

    private Toast mToast;
    private void toast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        }
        mToast.setText(resId);
        mToast.show();
    }
}
