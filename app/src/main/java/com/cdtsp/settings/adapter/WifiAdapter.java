package com.cdtsp.settings.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.cdtsp.settings.R;
import com.cdtsp.settings.util.MyUtils;

public class WifiAdapter extends BaseAdapter {
    private static final String TAG = "WifiAdapter";
    private Context mContext;
    private List<ScanResult> mScanResults;
    private WifiManager mWifiManager;
    private String mConnectedSsid;

    public WifiAdapter(Context mContext, List<ScanResult> scanResults) {
        this.mContext = mContext;
        mScanResults = new ArrayList<ScanResult>();
        setScanResults(scanResults);
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
    @Override
    public int getCount() {
        return mScanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return mScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_wifi, parent, false);
            convertView.setTag(new WifiViewHolder(convertView));
        }
        WifiViewHolder wifiViewHolder = (WifiViewHolder) convertView.getTag();

        final ScanResult result = mScanResults.get(position);
        wifiViewHolder.wifiName.setText(result.SSID);

        if (MyUtils.isLocked(result)) {
            wifiViewHolder.wifiImg.setImageResource(R.drawable.level_list_wifi_lock);
        } else {
            wifiViewHolder.wifiImg.setImageResource(R.drawable.level_list_wifi_unlock);
        }
        int level = mWifiManager.calculateSignalLevel(result.level, 5);
        wifiViewHolder.wifiImg.setImageLevel(level);

        if (getSavedWifiConfiguration(result.SSID) != null) {
            wifiViewHolder.wifiState.setVisibility(View.VISIBLE);
            wifiViewHolder.wifiState.setText(R.string.saved);
        } else {
            wifiViewHolder.wifiState.setVisibility(View.INVISIBLE);
            wifiViewHolder.wifiState.setText("");
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnWifiItemClickCallback != null) {
                    mOnWifiItemClickCallback.onWifiItemClick(result);
                }
            }
        });
        return convertView;
    }

    private static class WifiViewHolder{
        private TextView wifiName;
        private TextView wifiState;
        private ImageView wifiImg;
        public WifiViewHolder(View itemView) {
            wifiName = itemView.findViewById(R.id.tv_wifi_name);
            wifiState = itemView.findViewById(R.id.tv_wifi_state);
            wifiImg = itemView.findViewById(R.id.img_wifi);
        }
    }

    public void setConnectedSsid(String connectedSsid){
        mConnectedSsid = connectedSsid;
    }

    /**
     * 重新设置Wifi数据集合
     * @param scanResults
     */
    public void setScanResults(List<ScanResult> scanResults) {
        boolean isExits;
        this.mScanResults.clear();
        for(ScanResult newResult:scanResults){
            isExits = false;
            for(ScanResult result:mScanResults){
                if(result.SSID.equals(newResult.SSID)){
                    isExits = true;
                    break;
                }
            }
            if(!isExits && !isConnectedWifi(newResult.SSID)){
                //if don't have it in the list, add it to the list

                if (getSavedWifiConfiguration(newResult.SSID) != null) {
                    mScanResults.add(0, newResult);
                } else {
                    mScanResults.add(newResult);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 清空Wifi数据集合
     */
    public void clearScanResults() {
        this.mScanResults.clear();
        notifyDataSetChanged();
    }

    private OnWifiItemClickCallback mOnWifiItemClickCallback;
    public void setOnWifiItemClickCallback(OnWifiItemClickCallback onWifiItemClickCallback) {
        this.mOnWifiItemClickCallback = onWifiItemClickCallback;
    }
    public interface OnWifiItemClickCallback {
        void onWifiItemClick(ScanResult result);
    }
    private boolean isConnectedWifi(String ssid){
        if(mConnectedSsid == null){
            return false;
        }
        //get the SSID of the wifi that is conected
        String extraInfo = mConnectedSsid;
        //delete the double quotes in the start and end of the string.
        if(extraInfo.startsWith("\"") && extraInfo.endsWith("\"")){
            extraInfo = extraInfo.substring(1,extraInfo.length()-1);
        }
        if(extraInfo != null && extraInfo.compareTo(ssid) == 0){
            return true;
        }else{
            return false;
        }
    }

    /**
     *  查看该网络是否是配置过的
     */
    public WifiConfiguration getSavedWifiConfiguration(String SSID) {
        Log.d(TAG, "getSavedWifiConfiguration() called with: SSID = [" + SSID + "]");
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration existingConfig : existingConfigs) {

            if (existingConfig.SSID.toString().equals("\"" + SSID + "\"")) {
                Log.d(TAG, "getSavedWifiConfiguration: " + existingConfig.SSID);
                return existingConfig;
            }
        }
        return null;
    }
}
