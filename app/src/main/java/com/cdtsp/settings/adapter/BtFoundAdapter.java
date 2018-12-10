package com.cdtsp.settings.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdtsp.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/3/2.
 */

public class BtFoundAdapter extends RecyclerView.Adapter {

    private final String TAG = "BtBondedAdapter";
    private List<Object> mFoundDevices;
    private Context mContext;

    public BtFoundAdapter(Set<BluetoothDevice> foundDevices, Context mContext) {
        this.mFoundDevices = Arrays.asList(foundDevices.toArray());
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_bt_found, parent, false);
        FoundItemViewHolder itemViewHolder = new FoundItemViewHolder(itemView);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final BluetoothDevice device = (BluetoothDevice) mFoundDevices.get(position);
        Log.d(TAG, "onBindViewHolder: device=" + device.getName());
        FoundItemViewHolder itemViewHolder = (FoundItemViewHolder) holder;

        itemViewHolder.mTvDeviceName.setText(device.getName());

        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.createBond();
            }
        });

        if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
            itemViewHolder.mTvDeviceStatus.setVisibility(View.VISIBLE);
        } else {
            itemViewHolder.mTvDeviceStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mFoundDevices.size();
    }

    private class FoundItemViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvDeviceName;
        private TextView mTvDeviceStatus;
        public FoundItemViewHolder(View itemView) {
            super(itemView);
            mTvDeviceName = itemView.findViewById(R.id.tv_bt_device_name);
            mTvDeviceStatus = itemView.findViewById(R.id.tv_bt_device_status);
        }
    }

    private int mCurConnectingPos = -1;
    public void setCurConnectingDevice(int pos) {
        this.mCurConnectingPos = pos;
        notifyDataSetChanged();
    }

    public void addDevice(Set<BluetoothDevice> foundDevices) {
        mFoundDevices = new ArrayList<>(Arrays.asList(foundDevices.toArray()));
        notifyDataSetChanged();
    }

    public void clearData() {
        mFoundDevices.clear();
        notifyDataSetChanged();
    }
}
