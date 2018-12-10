package com.cdtsp.settings.adapter;

import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothPbapClient;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cdtsp.hmilib.ui.dialog.TspConfirmDialog;
import com.cdtsp.hmilib.util.BluetoothUtil;
import com.cdtsp.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import android.os.Handler;


/**
 * Created by Administrator on 2018/3/2.
 */

public class BtBondedAdapter extends RecyclerView.Adapter implements BluetoothUtil.OnProxyAllReadyCallback {

    private final String TAG = "SettingsBtBondedAdapter";
//    private BluetoothDevice[] mBondedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private List<Object> mBondedDevices;
    private Context mContext;
    private BluetoothUtil mBluetoothUtil;

    public BtBondedAdapter(Set<BluetoothDevice> bondedDevices, Context context) {
//        this.mBondedDevices = (BluetoothDevice[]) bondedDevices.toArray();
        this.mBondedDevices = Arrays.asList(bondedDevices.toArray());
        this.mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothUtil = BluetoothUtil.get(mContext.getApplicationContext());
        if (!mBluetoothUtil.isProxyReadyAll()) {
            mBluetoothUtil.registerProxyAllReadyCallback(this);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_bt_bonded, parent, false);
        BondedItemViewHolder itemViewHolder = new BondedItemViewHolder(itemView);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final BluetoothDevice device = (BluetoothDevice) mBondedDevices.get(position);
        Log.d(TAG, "onBindViewHolder: device=" + device.getName());
        BondedItemViewHolder itemViewHolder = (BondedItemViewHolder) holder;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.img_up:
                        //将选中的device移到列表的第一位
                        if(mBondedDevices.size() > 1) {
                            BluetoothDevice oldFirstDevice = (BluetoothDevice) mBondedDevices.get(0);
                            mBondedDevices.remove(device);
                            mBondedDevices.remove(oldFirstDevice);
                            mBondedDevices.add(0, device);
                            mBondedDevices.add(position, oldFirstDevice);
                            notifyDataSetChanged();
                        }
                        break;
                    case R.id.img_delete:
                        mBluetoothUtil.disconnectBtDevice(mContext, device);
                        device.removeBond();
                        mBondedDevices = Arrays.asList(mBluetoothAdapter.getBondedDevices().toArray());
                        notifyDataSetChanged();
                        break;
                    default:
                        Log.d(TAG, "onClick: no match id");
                        break;
                }
            }
        };
        itemViewHolder.mImgUp.setOnClickListener(onClickListener);
        itemViewHolder.mImgDelete.setOnClickListener(onClickListener);
        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int connectionState = mBluetoothUtil.getConnectionState(device);
                Log.d(TAG, "onClick: click item when connectionState is " + connectionState);
                if (connectionState != BluetoothAdapter.STATE_CONNECTED
                        && connectionState != BluetoothAdapter.STATE_CONNECTING) {
                    mBluetoothUtil.connectBtDevice(device);
                                 new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if((mBluetoothUtil.getConnectionState(device) != BluetoothAdapter.STATE_CONNECTED) &&
                                    (device.getBondState() == BluetoothDevice.BOND_NONE)) {
                                device.createBond();
                            }
                        }
                    }, 2500);
                }
                if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
                    TspConfirmDialog dialog = new TspConfirmDialog(mContext);
                    dialog.setTitle(mContext.getResources().getString(R.string.dialog_bt_disconnect_title));
                    dialog.setMessage(mContext.getResources().getString(R.string.dialog_bt_disconnect_content) + device.getName())
                            .setButton1(R.string.dialog_bt_disconnect_confirm, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mBluetoothUtil.disconnectBtDevice(mContext, device);
                                }
                            })
                            .setButton2(R.string.dialog_bt_disconnect_cancel).show();
                }
            }
        });

        itemViewHolder.mTvDeviceName.setText(device.getName());
        int connectionState = mBluetoothUtil.getConnectionState(device);
        Log.d(TAG, "onBindViewHolder: device=" + device.getName() + ", connectionState : " + connectionState);
        if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
            Log.d(TAG, "onBindViewHolder: connected");
            itemViewHolder.mTvDeviceStatus.setText(mContext.getString(R.string.wifi_connected));
            itemViewHolder.mTvDeviceStatus.setVisibility(View.VISIBLE);
        } else if (connectionState == BluetoothAdapter.STATE_CONNECTING){
            Log.d(TAG, "onBindViewHolder: connecting...");
            itemViewHolder.mTvDeviceStatus.setText(mContext.getString(R.string.wifi_connecting));
            itemViewHolder.mTvDeviceStatus.setVisibility(View.VISIBLE);
        } else {
            itemViewHolder.mTvDeviceStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mBondedDevices.size();
    }

    @Override
    public void onReady(BluetoothA2dpSink a2dpSink, BluetoothPbapClient pbapClient, BluetoothHeadsetClient headsetClient) {
        Log.d(TAG, "onReady() Bug7758 called with: a2dpSink = [" + a2dpSink + "], pbapClient = [" + pbapClient + "], headsetClient = [" + headsetClient + "]");
        notifyDataSetChanged();
    }

    private class BondedItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView mImgUp;
        private ImageView mImgDelete;
        private TextView mTvDeviceName;
        private TextView mTvDeviceStatus;
        public BondedItemViewHolder(View itemView) {
            super(itemView);
            mImgUp = itemView.findViewById(R.id.img_up);
            mImgDelete = itemView.findViewById(R.id.img_delete);
            mTvDeviceName = itemView.findViewById(R.id.tv_bt_device_name);
            mTvDeviceStatus = itemView.findViewById(R.id.tv_bt_device_status);
        }
    }

    public void addDevice(Set<BluetoothDevice> bondedDevices) {
        mBondedDevices = new ArrayList<>(Arrays.asList(bondedDevices.toArray()));
        notifyDataSetChanged();
    }

    public void clearData() {
        mBondedDevices.clear();
        notifyDataSetChanged();
    }
}
