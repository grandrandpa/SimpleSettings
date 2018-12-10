package com.cdtsp.settings.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import com.cdtsp.hmilib.ui.dialog.TspConfirmDialog;
import com.cdtsp.hmilib.ui.dialog.TspPasswordDialog;
import com.cdtsp.settings.R;
import com.cdtsp.settings.adapter.WifiAdapter;
import com.cdtsp.settings.util.MyUtils;
import com.cdtsp.settings.util.PermissionHelper;
import com.cdtsp.settings.view.SettingSwitchItem;
import com.cdtsp.settings.view.TspHotspotDialog;
import com.cdtsp.settings.wifi.WifiApEnabler;
import static android.net.ConnectivityManager.TETHERING_WIFI;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingNetwork extends Fragment implements WifiAdapter.OnWifiItemClickCallback {

    private static final String TAG = "FragmentSettingNetwork";
    private static final int REQUEST_PERMISSIONS = 0;
    private final int MSG_SHOW_CONNECTED_WIFI = 0;
    private final int MSG_NOTIFY_WIFI_DATA_CHANGED = 1;
    private final String[] AUTH = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private SettingSwitchItem mSettingSwitchItem;
    private ListView mWifiListView;
    private WifiManager mWifiManager;
    private WifiAdapter mWifiAdapter;
//    private AlertDialog mDialogPassword;
//    private TspPasswordDialog mDialogPassword;
    private int miConnectingNetId;

    //AP view layout
    private static final int WIFILAYOUT =1;
    private static final int APLAYOUT =2;
    private int mCurrentShowingLayout = WIFILAYOUT;
    private boolean mRestartWifiApAfterConfigChange = false;
    private LinearLayout mWifiLayout;
    private LinearLayout mApLayout;
    private SettingSwitchItem mHotspotSwitchItem;
    private ViewGroup mHotspotSetLayout;
    private TspHotspotDialog mSetDialog;

    private WifiApEnabler mWifiApEnabler;
    private ConnectivityManager mCm;
    private WifiConfiguration mWifiApConfig = null;

    private OnStartTetheringCallback mStartTetheringCallback;

    /**
     * wifi广播接收器
     */
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            Log.d(TAG, "onReceive: action=" + action);
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                boolean isScanned = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (isScanned) {
                    List<ScanResult> scanResults = mWifiManager.getScanResults();
                    Log.d(TAG, "onReceive: scanResults.size=" + scanResults.size() + ", isScanned=" + isScanned);
                    mWifiAdapter.setScanResults(scanResults);
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int wifiState = mWifiManager.getWifiState();
                if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    Log.d(TAG, "onReceive: WIFI_STATE_ENABLING");
                    mSettingSwitchItem.showStatusText(getResources().getString(R.string.wifi_activing));
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    Log.d(TAG, "onReceive: WIFI_STATE_ENABLED");
                    mSettingSwitchItem.setEnabled(true);
                    mSettingSwitchItem.hideStatusText();
                } else if (wifiState == WifiManager.WIFI_STATE_DISABLING) {
                    Log.d(TAG, "onReceive: WIFI_STATE_DISABLING");
                    mSettingSwitchItem.showStatusText(getResources().getString(R.string.wifi_closing));
                    mSettingSwitchItem.switchStateTo(false);
                    mSettingSwitchItem.setEnabled(false);
                } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    if (!mSwitchingOn) {//每次打开wifi，收到的第一条WIFI_STATE_CHANGED_ACTION的广播时，wifi状态为WIFI_STATE_DISABLED
                        Log.d(TAG, "onReceive: WIFI_STATE_DISABLED");
                        mSettingSwitchItem.setEnabled(true);
                        mSettingSwitchItem.hideStatusText();
                        //情况扫描出来的wifi列表
                        mWifiAdapter.clearScanResults();
                        //反注册wifi广播接收器
                        unregisterWifiReceiver();
                        if(mWifiListView.getHeaderViewsCount() > 0){
                            mWifiListView.removeHeaderView(mHeaderView);
                        }
                    }
                }
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
                Log.d(TAG, "onReceive: pa action= SUPPLICANT_STATE_CHANGED_ACTION: " + linkWifiResult);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    Log.d(TAG, "wifi Password is wrong!");
                    toast(R.string.wifi_password_err);
                    deletConfigItem(miConnectingNetId);
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "onReceive: action=" + action);
                //get the network information
                final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                Log.d(TAG, "onReceive: networkInfo.getState().name()=" + networkInfo.getState().name());
                Message msg = mHandler.obtainMessage(MSG_SHOW_CONNECTED_WIFI, networkInfo);
                mHandler.removeMessages(MSG_SHOW_CONNECTED_WIFI);
                mHandler.sendMessageDelayed(msg, 100);
            } else if (action.equals(ConnectivityManager.ACTION_TETHER_STATE_CHANGED)) {
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
                    Log.d(TAG, "Restarting WifiAp due to prior config change. WIFI_AP_STATE_DISABLED");
                    startTethering(TETHERING_WIFI);
                }
            } else if (action.equals(WifiManager.WIFI_AP_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, 0);
                if (state == WifiManager.WIFI_AP_STATE_DISABLED && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change. WIFI_AP_STATE_CHANGED_ACTION");
                    startTethering(TETHERING_WIFI);
                }
            }
        }
    };

    //HeadView: show the wifi information that is connecting or connected
    private View mHeaderView;
    //TextView: show the name of the wifi that is connecting or connected
    private TextView mWifiName;
    //TextView: show the detail state of the wifi
    private TextView mWifistate;
    //ImageView: show the signal of the wifi
    private ImageView mWifiImg;

    private Button mHotspotSwitcher;

    /**
     * create a head view to show the wifi information that is connecting or connected
     */
    private void createHeadView(){
        mHeaderView = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_wifi, null, false);
        mWifiName = (TextView)mHeaderView.findViewById(R.id.tv_wifi_name);
        mWifistate = (TextView)mHeaderView.findViewById(R.id.tv_wifi_state);
        mWifiImg = (ImageView)mHeaderView.findViewById(R.id.img_wifi);

        mHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //断开连接
//                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//                if (mWifiAdapter != null) {
//                    mWifiAdapter.setConnectedSsid(null);
//                }

//                mWifiManager.setEnableAutoJoinWhenAssociated()
//                int networkId = mWifiManager.getConnectionInfo().getNetworkId();
//                mWifiManager.disableNetwork(networkId);
//                mWifiManager.removeNetwork(networkId);

                TspConfirmDialog confirmDialog = new TspConfirmDialog(getContext())
                        .setButton1(R.string.bt_disconnect, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int networkId = mWifiManager.getConnectionInfo().getNetworkId();
                                mWifiManager.disableNetwork(networkId);
                            }
                        })
                        .setButton2(R.string.bt_cancel)
                        .setMessage(R.string.wifi_disconnecting_question);
                confirmDialog.setTitle(mWifiManager.getConnectionInfo().getSSID().replace("\"", ""));
                confirmDialog.show();

            }
        });
    }

    /**
     * show the wifi information that is connecting or connected
     * @param networkInfo: current network
     * @param clickedWifi  clicked wifi
     */
    private void showConnectedWifi(NetworkInfo networkInfo){
        mWifistate.setVisibility(View.VISIBLE);
        if(networkInfo == null){
            //have connected a network, show the text "已断开连接"
            mWifistate.setText(R.string.wifi_disconnected);
            return;
        }

        DetailedState detailedState = detailedState = networkInfo.getDetailedState();
        String ssid = mWifiName.getText().toString();

        if(networkInfo.getState().name().equals("DISCONNECTED")){
            //have connected a network, show the text "已断开连接"
            mWifistate.setText(R.string.wifi_disconnected);
            if (mWifiAdapter != null) {
                if(mWifiListView.getHeaderViewsCount() > 0){
                    mWifiAdapter.setConnectedSsid(null);
                    mWifiAdapter.setScanResults(mWifiManager.getScanResults());
                    mWifiListView.removeHeaderView(mHeaderView);
                }
            }
            return;
        }

        ssid = networkInfo.getExtraInfo();
        if(ssid.equalsIgnoreCase("<unknown ssid>")){
            if(mWifiListView.getHeaderViewsCount() > 0){
                mWifiListView.removeHeaderView(mHeaderView);
            }
            return;
        }
        //delete the double quotes in the start and end of the string.
        if(ssid.startsWith("\"") && ssid.endsWith("\"")){
            ssid = ssid.substring(1,ssid.length()-1);
        }

        //show the wifi name
        mWifiName.setText(ssid);

        //show the state of the wifi
        mWifistate.setText(getStateString(detailedState));

        List<ScanResult> scanResults = mWifiManager.getScanResults();
        ScanResult tmpWifi = null;
        for(ScanResult result:scanResults){
            if(ssid.equals(result.SSID)){
                tmpWifi = result;
                break;
            }
        }

        //show the icon of wifi
        if(tmpWifi != null){
            mWifiImg.setVisibility(View.VISIBLE);
            if (MyUtils.isLocked(tmpWifi)) {
                mWifiImg.setImageResource(R.drawable.level_list_wifi_lock);
            } else {
                mWifiImg.setImageResource(R.drawable.level_list_wifi_unlock);
            }
            int level = mWifiManager.calculateSignalLevel(tmpWifi.level, 5);
            mWifiImg.setImageLevel(level);
        }

        //update the wifi list
        if(mWifiAdapter != null){
            //set the network information to the adapter
            mWifiAdapter.setConnectedSsid(ssid);
            mWifiAdapter.setScanResults(mWifiManager.getScanResults());
            if(mWifiListView.getHeaderViewsCount() <= 0){
                mWifiListView.addHeaderView(mHeaderView);
            }
            //notify the adapter to update the wifi list
            mWifiAdapter.notifyDataSetChanged();
        }else{
            Log.d(TAG, "mWifiAdapter == null");
        }
    }
    private String getStateString(DetailedState detailedState){
        String stateString = "";
        if(detailedState == DetailedState.SCANNING){
            //have connected a network, show the text "正在扫描..."
            stateString = getString(R.string.wifi_scaning);
        }else if(detailedState == DetailedState.AUTHENTICATING){
            //have connected a network, show the text "正在进行身份验证..."
            stateString = getString(R.string.wifi_authenticating);
        }else if(detailedState == DetailedState.OBTAINING_IPADDR){
            //have connected a network, show the text "正在获取IP地址..."
            stateString = getString(R.string.wifi_obtaining_ipaddr);
        }else if(detailedState == DetailedState.DISCONNECTING){
            //have connected a network, show the text "正在断开连接..."
            stateString = getString(R.string.wifi_disconnecting);
        }else if(detailedState == DetailedState.DISCONNECTED){
            //have connected a network, show the text "已断开连接"
            stateString = getString(R.string.wifi_disconnected);
        }else if(detailedState == DetailedState.CONNECTING){
            //Is connecting a network, show the text "正在连接..."
            stateString = getString(R.string.wifi_connecting);
        }else if(detailedState == DetailedState.CONNECTED){
            //have connected a network, show the text "已连接"
            stateString = getString(R.string.wifi_connected);
        }
        return stateString;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_network, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiApConfig = mWifiManager.getWifiApConfiguration();
        createHeadView();
        initViews(view);
        initAPViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mWifiApEnabler != null) {
            mWifiApEnabler.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWifiApEnabler != null) {
            mWifiApEnabler.pause();
        }
    }

    private void initViews(View view) {

        //初始化Wifi信息列表View
        mWifiListView = view.findViewById(R.id.list_view_wifi);
//        mWifiListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mWifiAdapter = new WifiAdapter(getContext(), new ArrayList<ScanResult>());
        mWifiListView.setAdapter(mWifiAdapter);
        mWifiAdapter.setOnWifiItemClickCallback(this);

        //初始化mSettingSwitchItem
        mSettingSwitchItem = view.findViewById(R.id.switch_wifi);
        mSettingSwitchItem.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                switchOnWifi();
            }

            @Override
            public void onSwitchOff() {
                switchOffWifi();
            }
        });

        boolean isWifiEnabled = mWifiManager.isWifiEnabled();
        mSettingSwitchItem.switchStateTo(isWifiEnabled);

        mWifiLayout = view.findViewById(R.id.wifi_layout);
        mHotspotSwitcher = view.findViewById(R.id.btn_hotspot_switcher);
        mHotspotSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "switch to hotspot or AP layout");
                swapWifiAndAp();
            }
        });
    }

    private void initAPViews(View view) {
        mApLayout = view.findViewById(R.id.ap_layout);
        mHotspotSwitchItem = view.findViewById(R.id.switch_hotspot);
        mHotspotSetLayout = view.findViewById(R.id.hotspot_set_layout);

        boolean isWifiEnabled = isWifiApOpen();
        mHotspotSwitchItem.switchStateTo(isWifiEnabled);
        mHotspotSwitchItem.setSubTitleText(mWifiApConfig.SSID.toString());

        mStartTetheringCallback = new OnStartTetheringCallback(this);
        mHotspotSwitchItem.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                Log.d(TAG, "onSwitchOn AP status:" + isWifiApOpen());
                if (isWifiApOpen()) {
                    return;
                }

                if (mWifiApConfig == null) {
                    Log.d(TAG, "onSwitchOn mWifiApConfig is null");
                    mWifiApConfig.SSID = "hotspot";
                    mWifiApConfig.apBand = 0;
                    mWifiApConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                    mWifiApConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    mWifiApConfig.preSharedKey = "12345678";

                    mWifiManager.setWifiApConfiguration(mWifiApConfig);
                    mHotspotSwitchItem.setSubTitleText(mWifiApConfig.SSID);
                }
                mHotspotSwitchItem.setEnabled(false);
                startTethering(TETHERING_WIFI);
                mSettingSwitchItem.switchStateTo(false);
            }

            @Override
            public void onSwitchOff() {
                Log.d(TAG, "onSwitchOff AP status:" + isWifiApOpen());
                if (!isWifiApOpen()) {
                    return;
                }
                mCm.stopTethering(TETHERING_WIFI);
            }
        });

        mHotspotSetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHotspotSetWindow();
            }
        });

        final Activity activity = getActivity();
        mWifiApEnabler = new WifiApEnabler(activity, mHotspotSwitchItem);
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
    }

    private boolean mSwitchingOn;
    private void switchOnWifi() {
        Log.d(TAG, "onSwitchOn: ");
        mSwitchingOn = true;

        //如果wifi没有打开，那么打开wifi
        if (!mWifiManager.isWifiEnabled()) {
            mSettingSwitchItem.showStatusText(getResources().getString(R.string.wifi_prepare_for_active));
            mSettingSwitchItem.setEnabled(false);
            mWifiManager.setWifiEnabled(true);
        }

        //注册wifi广播接收器
        registerWifiReceiver();

        //扫描wifi前检查权限
        PermissionHelper permissionHelper = new PermissionHelper(getActivity());
        permissionHelper.setPermNeedToRequest(AUTH);
        if (!permissionHelper.checkMyPermissions()) {
            Log.d(TAG, "onSwitchOn: lack permission, now get permission......");
            permissionHelper.getMyPermissions(REQUEST_PERMISSIONS);
        } else {
            Log.d(TAG, "onSwitchOn: now begin scanning wifi");
            scanWifi();
        }
    }

    private void switchOffWifi() {
        if (!mSwitchingOn) {
            return;
        }
        Log.d(TAG, "onSwitchOff: mSwitchingOn"+ mSwitchingOn);
        mSwitchingOn = false;

        //关闭Wifi
        if (mWifiManager.isWifiEnabled()) {
            mSettingSwitchItem.showStatusText(getResources().getString(R.string.wifi_prepare_for_close));
            mSettingSwitchItem.setEnabled(false);
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 扫描wifi
     */
    private void scanWifi() {
        Log.d(TAG, "scanWifi: startTime=" + SystemClock.uptimeMillis());
        boolean b = mWifiManager.startScan();
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        Log.d(TAG, "scanWifi: endTime=" + SystemClock.uptimeMillis() + ", scanResults.size=" + scanResults.size() + ", b=" + b);
        mWifiAdapter.setScanResults(scanResults);
    }

    /**
     * 注册wifiReceiver
     */
    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        filter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mWifiReceiver, filter);
    }

    /**
     * 反注册wifiReceiver
     */
    private void unregisterWifiReceiver() {
        getActivity().unregisterReceiver(mWifiReceiver);
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
            scanWifi();
        }
    }

    /**
     * 根据不同的加密方式，来创建WifiConfiguration
     * @param result
     * @param password
     * @return
     */
    public WifiConfiguration createWifiConfiguration(ScanResult result, String password)
    {
        Log.d(TAG, "createWifiConfiguration() called with: SSID = [" + result.SSID + "], Password = [" + password + "], Type = [" + result.capabilities + "]");
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + result.SSID + "\"";
        config.BSSID = result.BSSID;

        //若该wifi已经连接过，那么将其从已连接列表中移除
        WifiConfiguration tempConfig = this.checkExsits(result.SSID);
        if(tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if(result.capabilities.contains("WEP")) { //WEP加密方式，未验证
            Log.d(TAG, "createWifiConfiguration: WEP");
//            config.hiddenSSID = true;
//            config.wepKeys[0]= "\""+password+"\"";
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
            int i = password.length();
            if (((i == 10 || (i == 26) || (i == 58))) && (password.matches("[0-9A-Fa-f]*"))) {
                Log.d(TAG, "createWifiConfiguration: ((i == 10 || (i == 26) || (i == 58))) && (password.matches(\"[0-9A-Fa-f]*\"))");
                config.wepKeys[0] = password;
            } else {
                Log.d(TAG, "createWifiConfiguration: else");
                config.wepKeys[0] = "\"" + password + "\"";
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if(result.capabilities.contains("WPA")) { //WPA-PSK加密方式，已验证
            Log.d(TAG, "createWifiConfiguration: WPA");
            config.preSharedKey = "\""+password+"\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        } else { //无密码，已验证
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    private WifiConfiguration checkExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\""+SSID+"\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    @Override
    public void onWifiItemClick(final ScanResult result) {
        Log.d(TAG, "onWifiItemClick: ");

        final WifiConfiguration wifiConfiguration = mWifiAdapter.getSavedWifiConfiguration(result.SSID);
        if (wifiConfiguration != null) {
            TspConfirmDialog dialogConfirm = new TspConfirmDialog(getContext())
                    .setCountBtn(3)
                    .setButton1(R.string.bt_connect, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
                            Log.d(TAG, "onWifiItemClick Get config password then connect network id: " + wifiConfiguration.networkId);
                        }
                    })
                    .setButton2(R.string.bt_remove, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWifiManager.forget(wifiConfiguration.networkId, new WifiManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "onSuccess() called, forget network success");
                                    mHandler.sendEmptyMessage(MSG_NOTIFY_WIFI_DATA_CHANGED);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.d(TAG, "onFailure() called, forget network failed");
                                }
                            });
                        }
                    })
                    .setButton3(R.string.bt_cancel)
                    .setMessage(R.string.wifi_operate_question);
            dialogConfirm.setTitle(result.SSID);
            dialogConfirm.show();
        } else {
            if (MyUtils.isLocked(result)) {
                //弹出dialog，提示用户输入密码
//                if (mDialogPassword == null) {
//                final EditText editText = new EditText(getContext());
//                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                mDialogPassword = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog)
//                        .setTitle(result.SSID)
//                        .setMessage("请输入密码")
//                        .setView(editText)
//                        .setPositiveButton("连接", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String password = editText.getText().toString();
//                                WifiConfiguration config = createWifiConfiguration(result, password);
//                                int netWorkId = mWifiManager.addNetwork(config);
//                                mWifiManager.enableNetwork(netWorkId, true);
//                            }
//                        })
//                        .setNegativeButton("取消", null)
//                        .create();
//                mDialogPassword.show();
//                    mDialogPassword = new TspPasswordDialog(getContext())
//                            .setButton1(R.string.bt_connect, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    String password = mDialogPassword.getPassword();
//                                    if (TextUtils.isEmpty(password)) {
//                                        toast(R.string.wifi_password_input_err);
//                                    } else {
//                                        mDialogPassword.dismiss();
//                                        WifiConfiguration config = createWifiConfiguration(result, password);
//                                        int netWorkId = mWifiManager.addNetwork(config);
//                                        mWifiManager.enableNetwork(netWorkId, true);
//                                    }
//                                }
//                            })
//                            .setButton2(R.string.bt_cancel)
//                            .setMessage(R.string.wifi_password_input);
//                }
                TspPasswordDialog dialogPassword = new TspPasswordDialog(getContext());
                dialogPassword.setButton1(R.string.bt_connect, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String password = dialogPassword.getPassword();
                                if (TextUtils.isEmpty(password)) {
                                    toast(R.string.wifi_password_input_err);
                                } else {
                                    dialogPassword.dismiss();
                                    WifiConfiguration config = createWifiConfiguration(result, password);
                                    Log.d(TAG, "onWifiItemClick dialogPassword password: " + password);
                                    int netWorkId = mWifiManager.addNetwork(config);
                                    mWifiManager.enableNetwork(netWorkId, true);
                                    miConnectingNetId = netWorkId;
                                    Log.d(TAG, "onWifiItemClick Input password then connect network id: " + netWorkId);
                                }
                            }
                        })
                        .setButton2(R.string.bt_cancel)
                        .setMessage(R.string.wifi_password_input);
                dialogPassword.setTitle(result.SSID);
                dialogPassword.show();
            } else {
                WifiConfiguration config = createWifiConfiguration(result, "");
                int netWorkId = mWifiManager.addNetwork(config);
                mWifiManager.enableNetwork(netWorkId, true);
            }
        }
    }

    private void deletConfigItem(int networkId) {
        mWifiManager.forget(networkId, new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess() called, forget network success");
                mHandler.sendEmptyMessage(MSG_NOTIFY_WIFI_DATA_CHANGED);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure() called, forget network failed");
            }
        });
    }

    //for AP
    private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<FragmentSettingNetwork> mTetherSettings;

        OnStartTetheringCallback(FragmentSettingNetwork settings) {
            mTetherSettings = new WeakReference<FragmentSettingNetwork>(settings);
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
            FragmentSettingNetwork settings = mTetherSettings.get();
            if (settings != null) {
                //settings.updateState();
            }
        }
    }

    private void swapWifiAndAp() {
        if (mCurrentShowingLayout == WIFILAYOUT) {
            mWifiLayout.setVisibility(View.GONE);
            mApLayout.setVisibility(View.VISIBLE);
            mHotspotSwitcher.setText(R.string.networks);
            mCurrentShowingLayout = APLAYOUT;
            //mApLayout.invalidate();
        } else {
            mWifiLayout.setVisibility(View.VISIBLE);
            mApLayout.setVisibility(View.GONE);
            mHotspotSwitcher.setText(R.string.hotspot);
            mCurrentShowingLayout = WIFILAYOUT;
            //mWifiLayout.invalidate();
        }
    }

    private void initDialogViews() {
        if (mWifiApConfig != null) {
            //Log.d(TAG, "initDialogViews SSID:" + mWifiConfig.SSID + ", password: " + mWifiConfig.preSharedKey.toString());
            mSetDialog.setNameInfo(mWifiApConfig.SSID.toString());
            mSetDialog.setPassword(mWifiApConfig.preSharedKey.toString());
            mSetDialog.setBandType(mWifiApConfig.apBand);
        }
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

                            mWifiApConfig = mSetDialog.getConfig();
                            if (mWifiApConfig != null) {
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
                                Log.d(TAG, "Wifi AP config password:" + mWifiApConfig.preSharedKey);
                                mWifiManager.setWifiApConfiguration(mWifiApConfig);
                                //int index = TspHotspotDialog.getSecurityTypeIndex(mWifiApConfig);
                                //mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
                                //        mWifiApConfig.SSID,
                                //        mSecurityType[index]));
                                mHotspotSwitchItem.setSubTitleText(mWifiApConfig.SSID);
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

    private boolean isWifiApOpen() {
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

    private void startTethering(int choice) {
        mCm.startTethering(choice, true, mStartTetheringCallback, mHandler);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_SHOW_CONNECTED_WIFI:{
                    NetworkInfo networkInfo = (NetworkInfo)msg.obj;
                    showConnectedWifi(networkInfo);
                }
                    break;
                case MSG_NOTIFY_WIFI_DATA_CHANGED:
                    mWifiAdapter.notifyDataSetChanged();
                default:{
                    //nothing to do
                }
                 break;
            }
        }
    };

    private Toast mToast;
    private void toast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        }
        mToast.setText(resId);
        mToast.show();
    }
}
