package com.cdtsp.settings.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothPbapClient;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdtsp.hmilib.util.BluetoothUtil;
import com.cdtsp.settings.R;
import com.cdtsp.settings.adapter.BtBondedAdapter;
import com.cdtsp.settings.adapter.BtFoundAdapter;
import com.cdtsp.settings.util.MyUtils;
import com.cdtsp.settings.view.SettingSwitchItem;
import com.cdtsp.hmilib.ui.dialog.TspPasswordDialog;
import android.text.InputType;
import android.bluetooth.BluetoothHeadsetClient;
import java.util.Set;

/**
 * Created by Administrator on 2018/1/18.
 * 1、开机后第一次显示本界面的时候，会加载出已经配对过的蓝牙设备列表
 * 2、开机后第一次显示本界面的时候，会自动扫描周围的新设备
 * 3、若连接到新的设备，或自动将已经连接的设备显示到已配对的蓝牙设备列表中，并置顶显示(若没有锁定置顶位置的设备的话)
 */
public class FragmentSettingBt extends Fragment{

    private static final String TAG = "FragmentSettingBt";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothUtil mBluetoothUtil;
    private Set<BluetoothDevice> mFoundDevices = new ArraySet<BluetoothDevice>();
    private Set<BluetoothDevice> mBondedDevices = new ArraySet<BluetoothDevice>();
    private RecyclerView mListViewBonded;
    private RecyclerView mListViewFound;
    private BtBondedAdapter mBtBondedAdapter;
    private BtFoundAdapter mBtFoundAdapter;
    private View mSearch;
    private ImageView mImgSearch;
    private SettingSwitchItem mSwitchBluetooth;
    private SettingSwitchItem mSwitchSyncContact;
    private SettingSwitchItem mSwitchVoiceCall;
    private TextView mTvBtName;
    private View mTagLayout;
    private View mRootLayoutList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_bt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBluetoothUtil = BluetoothUtil.get(getActivity().getApplicationContext());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        //若设备不支持蓝牙，则mBluetoothAdapter为null
//        if (mBluetoothAdapter != null) {
//            mBondedDevices = mBluetoothAdapter.getBondedDevices();
//            Log.d(TAG, "onViewCreated: mBondedDevices.size = " + mBondedDevices.size());
//        } else {
//            Log.d(TAG, "onViewCreated: mBluetoothAdapter=" + mBluetoothAdapter + ", no bluetooth device");
//            mBondedDevices = new ArraySet<>();
//        }
        initViews(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerBluetoothReceiver();

        //Settings启动后，第一次切换到蓝牙设置界面时，若蓝牙为打开状态，则扫描
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            updateButtonState();
            mTvBtName.setEnabled(true);

        }else if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
            mSwitchSyncContact.setEnabled(false);
            mSwitchVoiceCall.setEnabled(false);
            mTvBtName.setEnabled(false);

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBluetoothReceiver();
        //停止扫描蓝牙设备
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void updateButtonState() {
        if (1 == Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_SYNC_CONTACT_ON, 1)) {
            mSwitchSyncContact.switchStateTo(true);
        } else {
            mSwitchSyncContact.switchStateTo(false);
        }
        if (1 == Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_INCOMING_CALL_VOICE_PROMPT, 1)) {
            mSwitchVoiceCall.switchStateTo(true);
        } else {
            mSwitchVoiceCall.switchStateTo(false);
        }
    }

    /**
     * 注册蓝牙广播接收器
     */
    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_BLE_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        filter.addAction(BluetoothPbapClient.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        getActivity().registerReceiver(mBluetoothReceiver, filter);
    }

    /**
     * 反注册蓝牙广播接收器
     */
    private void unregisterBluetoothReceiver() {
        getActivity().unregisterReceiver(mBluetoothReceiver);
    }

    private void initViews(View view) {
        mTagLayout = view.findViewById(R.id.tag_bt_off);
        mRootLayoutList = view.findViewById(R.id.root_layout_list);

        initViewDeviceList(view);

        initViewSwitchItems(view);

        initViewSearchs(view);

        initViewBtName(view);

        Log.d(TAG, "initViews: mBluetoothAdapter=" + mBluetoothAdapter);
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "initViews: mBluetoothAdapter.getState() = " + mBluetoothAdapter.getState());
            if (mBluetoothAdapter.isEnabled()) {
                mSwitchBluetooth.switchStateTo(true);

                onBtTurnOn();

                mBondedDevices = mBluetoothAdapter.getBondedDevices();
                mBtBondedAdapter.addDevice(mBondedDevices);
            } else {
                //若蓝牙未打开
                mSwitchBluetooth.switchStateTo(false);
                //隐藏配对列表和显示蓝牙关闭状态的提示layout
                onBtTurnOff();
            }
            String thisName = mBluetoothAdapter.getName();
            mSwitchBluetooth.setSubTitleText(getResources().getString(R.string.search_on_phone) + thisName);
            mTvBtName.setText(thisName);

        } else {
            Log.d(TAG, "initViews: Bluetooth support failed !!");

            //若当前设备不支持蓝牙
            mSwitchBluetooth.setEnabled(false);
            mSwitchSyncContact.setEnabled(false);
            mSwitchVoiceCall.setEnabled(false);
            onBtTurnOff();


        }

        if (mBluetoothAdapter != null) {
            String thisName = mBluetoothAdapter.getName();
            mSwitchBluetooth.setSubTitleText(getResources().getString(R.string.search_on_phone) + thisName);
        }
    }

    private void onBtTurnOn() {
        //显示配对列表和隐藏蓝牙关闭状态的提示layout
        mRootLayoutList.setVisibility(View.VISIBLE);
        mTagLayout.setVisibility(View.GONE);
    }

    private void onBtTurnOff() {
        //隐藏配对列表和显示蓝牙关闭状态的提示layout
        mRootLayoutList.setVisibility(View.INVISIBLE);
        mTagLayout.setVisibility(View.VISIBLE);
    }

    private void initViewBtName(View view) {
        mTvBtName = view.findViewById(R.id.bt_name);
        mTvBtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null) {
                    showRenameDialog();
                }
            }
        });
    }

    private void initViewSearchs(View view) {
        mImgSearch = view.findViewById(R.id.img_search);
        mSearch = view.findViewById(R.id.search);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();
            }
        });
    }

    private void initViewSwitchItems(View view) {
        mSwitchBluetooth = view.findViewById(R.id.switch_bluetooth);
        mSwitchSyncContact = view.findViewById(R.id.switch_sync_contact);
        mSwitchVoiceCall = view.findViewById(R.id.switch_voice_call);

        mSwitchBluetooth.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                if (!mBluetoothAdapter.isEnabled()) {
                    mSwitchBluetooth.setEnabled(false);
                    mSwitchSyncContact.setEnabled(false);
                    mSwitchVoiceCall.setEnabled(false);

                    mBluetoothAdapter.enable();
                }
            }

            @Override
            public void onSwitchOff() {
                if (mBluetoothAdapter.isEnabled()) {
                    mSwitchBluetooth.setEnabled(false);
                    mSwitchSyncContact.setEnabled(false);
                    mSwitchVoiceCall.setEnabled(false);

                    mBluetoothAdapter.disable();
                    onBtTurnOff();
                }
            }
        });

        mSwitchSyncContact.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_SYNC_CONTACT_ON, 1);
            }

            @Override
            public void onSwitchOff() {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_SYNC_CONTACT_ON,  0);
            }
        });

        mSwitchVoiceCall.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_INCOMING_CALL_VOICE_PROMPT, 1);
            }

            @Override
            public void onSwitchOff() {
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.BLUETOOTH_INCOMING_CALL_VOICE_PROMPT, 0);
            }
        });
    }

    private void initViewDeviceList(View view) {
        mListViewBonded = view.findViewById(R.id.list_view_bonded);
        mListViewFound = view.findViewById(R.id.list_view_found);

        mListViewBonded.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBtBondedAdapter = new BtBondedAdapter(mBondedDevices, getActivity());
        mListViewBonded.setAdapter(mBtBondedAdapter);

        mListViewFound.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBtFoundAdapter = new BtFoundAdapter(mFoundDevices, getContext());
        mListViewFound.setAdapter(mBtFoundAdapter);
    }

    private RotateAnimation anim;
    /**
     * 搜索设备时，启动搜索按钮的旋转动画
     */
    private void startSearchAnim() {
        if (anim == null) {
            anim = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(800);
            anim.setRepeatCount(Animation.INFINITE);
        }
        mImgSearch.startAnimation(anim);
    }

    /**
     * 结束搜索时，停止搜索按钮的旋转动画
     */
    private void cancelSearchAnim() {
        if (anim != null) {
            anim.cancel();
        }
    }

    private AlertDialog mDialog;
    private void showDialog(String text) {
        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog)
                    .setTitle("蓝牙已连接")
                    .setMessage("即将为您同步通讯录")
                    .create();
        }
        mDialog.show();
        MyUtils.toast(getContext(), "通讯录同步暂未开发");
    }

    private TspPasswordDialog mRenameDialog;

    private void showRenameDialog() {
        if (mRenameDialog == null) {
            mRenameDialog = new TspPasswordDialog(getActivity())
                    .setButton1(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String btName = mRenameDialog.getPassword();
                            if (btName.isEmpty()) {
                                return;
                            }
                            mBluetoothAdapter.setName(btName);
                            mRenameDialog.dismiss();
                        }
                    })
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .setMaxLength(50)
                    .setButton2(R.string.no);
            mRenameDialog.setTitle(R.string.bt_device_rename);
            mRenameDialog.show();
        } else {
            mRenameDialog.show();
        }
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action = " + action);
            if (BluetoothAdapter.ACTION_BLE_STATE_CHANGED.equals(action)) {//蓝牙设备状态改变
                int state = mBluetoothAdapter.getState();
                Log.d(TAG, "onReceive: ACTION_STATE_CHANGED, state=" + state);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mSwitchBluetooth.setEnabled(false);
                        mSwitchSyncContact.setEnabled(false);
                        mSwitchVoiceCall.setEnabled(false);
                        mTvBtName.setEnabled(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if (mBluetoothAdapter.isDiscovering()) {
                            Log.d(TAG, "onReceive: STATE_TURNING_OFF, cancelDiscovery");
                            mBluetoothAdapter.cancelDiscovery();
                        }
                        mSwitchBluetooth.setEnabled(false);
                        mSwitchSyncContact.setEnabled(false);
                        mSwitchVoiceCall.setEnabled(false);
                        mTvBtName.setEnabled(false);

                        break;
                    case BluetoothAdapter.STATE_ON://蓝牙功能被打开后
                        //mSwitchBluetooch设为enable的彩色状态
                        mSwitchBluetooth.switchStateTo(true);
                        mSwitchBluetooth.setEnabled(true);
                        mSwitchSyncContact.setEnabled(true);
                        mSwitchVoiceCall.setEnabled(true);
                        mTvBtName.setEnabled(true);
                        updateButtonState();
                        onBtTurnOn();
                        //显示已配对设备列表
                        mBtBondedAdapter.addDevice(mBluetoothAdapter.getBondedDevices());
                        //蓝牙打开之后要执行扫描操作
                        mBluetoothAdapter.startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_OFF://蓝牙功能被关闭后
                        mSwitchBluetooth.switchStateTo(false);
                        mSwitchBluetooth.setEnabled(true);
                        mSwitchSyncContact.setEnabled(false);
                        mSwitchVoiceCall.setEnabled(false);
                        mTvBtName.setEnabled(false);
                        onBtTurnOff();
                        //清空列表
                        mBtBondedAdapter.clearData();
                        mBtFoundAdapter.clearData();
                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {//蓝牙设备连接状态改变
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = device.getBondState();
                Log.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED, device=" + device + ", bondState=" + bondState);
                switch (bondState) {
                    case BluetoothDevice.BOND_BONDING:
                        //正在绑定，更新新蓝牙设备列表UI
                        mBtFoundAdapter.notifyDataSetChanged();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        //绑定成功，从新蓝牙设备列表中去除已绑定的设备
                        mFoundDevices.removeAll(mBluetoothAdapter.getBondedDevices());
                        mBtFoundAdapter.addDevice(mFoundDevices);
                        //绑定成功，往绑定过的蓝牙设备列表中添加新绑定的设备
                        mBtBondedAdapter.addDevice(mBluetoothAdapter.getBondedDevices());
                        //绑定成功，连接蓝牙设备
                        mBluetoothUtil.connectBtDevice(device);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        //解绑成功，从绑定过的蓝牙设备列表中去除解绑的设备
                        mBtBondedAdapter.addDevice(mBluetoothAdapter.getBondedDevices());
                        //绑定失败的时候，要重新更新新蓝牙设备列表UI
                        mBtFoundAdapter.notifyDataSetChanged();
                        break;
                }
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {

                // 收到连接状态改变的广播就刷新绑定过的蓝牙设备列表，
                // BtBondedAdapter会根据列表中每个BluetoothDevice的状态更新UI
                mBtBondedAdapter.notifyDataSetChanged();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {//发现设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    mFoundDevices.add(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mFoundDevices.clear();
                startSearchAnim();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mFoundDevices.removeAll(mBluetoothAdapter.getBondedDevices());//将已经绑定的设备从发现列表中去除
                mBtFoundAdapter.addDevice(mFoundDevices);
                cancelSearchAnim();
            } else if (BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED.equals(action)) {
                String newName = intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME);
                mTvBtName.setText(newName);
                mSwitchBluetooth.setSubTitleText("可在手机中搜索" + newName);
            } else if(BluetoothPbapClient.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
                switch (state) {
                    case BluetoothProfile.STATE_CONNECTED:
                    case BluetoothProfile.STATE_DISCONNECTED:
                        if(mBluetoothAdapter.isEnabled()) {
                            mSwitchSyncContact.setEnabled(true);
                        }
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                    case BluetoothProfile.STATE_DISCONNECTING:
                        if(mBluetoothAdapter.isEnabled()) {
                            mSwitchSyncContact.setEnabled(false);
                        }
                        break;
                    default:
                        break;
                }
            }else if(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                mBtBondedAdapter.notifyDataSetChanged();
            }
        }
    };
}