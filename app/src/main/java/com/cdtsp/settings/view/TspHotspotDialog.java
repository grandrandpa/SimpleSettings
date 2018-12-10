package com.cdtsp.settings.view;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.util.Log;

import com.cdtsp.settings.R;

public class TspHotspotDialog extends Dialog {
    private static final String TAG = "TspHotspotDialog";

    public static final int OPEN_INDEX = 0;
    public static final int WPA_WPA2 = 1;
    public static final int WPA_PSK = 2;
    public static final int WPA2_PSK = 3;
    public static final int WEP = 4;

    //apBand 0 for 2GHz, 1 for 5GHz
    public static final int BAND_2G = 0;
    public static final int BAND_5G = 1;

    private CharSequence mTitle, mStrButton1, mStrButton2;
    private View.OnClickListener mOnClickListener1, mOnClickListener2;
    private TextView mTitleView;
    private EditText mNameView, mPasswordView;
    private Button mButton1, mButton2;
    private int mInputType = -1;
    private int mMaxLength = -1;

    private String mSSID = "hotspot";
    private String mPassword = "12345678";
    private int mBandIndex = BAND_2G;
    private int mSecurityTypeIndex = WPA_WPA2;

    private Spinner mSecSpinner;
    private ArrayAdapter mSecAdapter;

    private Spinner mBandSpinner;
    private ArrayAdapter mBandAdapter;

    public TspHotspotDialog(Context context) {
        super(context, R.style.DialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_hotspot);
        initViews();
        initSpinner();
        updateSettingInfo();
    }

    private void initViews() {
        mTitleView = findViewById(R.id.title_view);
        mNameView = findViewById(R.id.name_view);
        mPasswordView = findViewById(R.id.hotspot_password_view);
        mButton1 = findViewById(R.id.button_ok);
        mButton2 = findViewById(R.id.button_cancle);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnClickListener1 != null){
                    mOnClickListener1.onClick(v);
                } else {
                    dismiss();
                }
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnClickListener2 != null){
                    mOnClickListener2.onClick(v);
                } else {
                    dismiss();
                }
            }
        });

        if(mInputType != -1) {
            mPasswordView.setInputType(mInputType);
        }

        if(mMaxLength !=-1) {
            InputFilter[] name = {new InputFilter.LengthFilter(mMaxLength)};
            mNameView.setFilters(name);

            InputFilter[] filters = {new InputFilter.LengthFilter(mMaxLength)};
            mPasswordView.setFilters(filters);
        }
    }

    private void initSpinner() {
//        mSecSpinner = (Spinner) findViewById(R.id.hotspot_spinner);
//        String hottype[] = getContext().getResources().getStringArray(R.array.hotspot_type);
//
//        mSecAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, hottype);
//        mSecAdapter.setDropDownViewResource(R.layout.dropdown_style);
//        mSecSpinner.setAdapter(mSecAdapter);
//        mSecSpinner.setOnItemSelectedListener(new SpinnerSecSelectedListener());

        mBandSpinner = (Spinner) findViewById(R.id.hotspot_band_spinner);
        String bandtype[] = getContext().getResources().getStringArray(R.array.hotspot_band_type);

        mBandAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, bandtype);
        mBandAdapter.setDropDownViewResource(R.layout.dropdown_style);
        mBandSpinner.setAdapter(mBandAdapter);
        mBandSpinner.setOnItemSelectedListener(new SpinnerBandSelectedListener());
    }

//    class SpinnerSecSelectedListener implements AdapterView.OnItemSelectedListener {
//        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
//            Log.d(TAG, "selected security index: " + mSecAdapter.getItem(position));
//            mSecurityTypeIndex = position+1;
//        }
//
//        public void onNothingSelected(AdapterView<?> arg0) {
//        }
//    }

    class SpinnerBandSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
            Log.d(TAG, "selected band index: " + mBandAdapter.getItem(position));
            mBandIndex = position;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void updateSettingInfo() {
        mNameView.setText(mSSID);
        mPasswordView.setText(mPassword);
        mBandSpinner.setSelection(mBandIndex, true);
    }

    public TspHotspotDialog setButton1(int strId){
        mStrButton1 = getContext().getString(strId);
        if(mButton1 != null){
            mButton1.setText(strId);
        }
        return this;
    }

    public TspHotspotDialog setButton1(int strId, View.OnClickListener listener){
        mStrButton1 = getContext().getString(strId);
        mOnClickListener1 = listener;
        if(mButton1 != null){
            mButton1.setText(strId);
            if(listener != null){
                mButton1.setOnClickListener(listener);
            }
        }
        return this;
    }

    public TspHotspotDialog setButton2(int strId){
        mStrButton2 = getContext().getString(strId);
        if(mButton2 != null){
            mButton2.setText(strId);
        }
        return this;
    }

    public TspHotspotDialog setButton2(int strId, View.OnClickListener listener){
        mStrButton2 = getContext().getString(strId);
        mOnClickListener2 = listener;
        if(mButton2 != null){
            mButton2.setText(strId);
            if(listener != null){
                mButton2.setOnClickListener(listener);
            }
        }
        return this;
    }

    public void setNameInfo(String ssid) {
        mSSID = ssid;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void setBandType(int band) {
        mBandIndex = band;
    }

    public void setBandType(String band) {
        int index = BAND_2G;
        if (band == "5G") {
            index = BAND_5G;
        }
        setBandType(index);
    }

    public String getNameInfo() {
        return mNameView.getText().toString();
    }

    public String getPassword() {
        return mPasswordView.getText().toString();
    }

    public TspHotspotDialog setInputType(int type) {
        mInputType = type;
        if(mPasswordView != null) {
            mPasswordView.setInputType(type);
        }
        return this;
    }

    public TspHotspotDialog setMaxLength(int length) {
        mMaxLength = length;
        if(mPasswordView != null) {
            InputFilter[] filters = {new InputFilter.LengthFilter(length)};
            mPasswordView.setFilters(filters);
        }
        return this;
    }

    public WifiConfiguration getConfig() {
        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */
        config.SSID = mNameView.getText().toString();
        config.apBand = mBandIndex;

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case WEP:
                config.allowedKeyManagement.set(KeyMgmt.FT_EAP);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    config.preSharedKey = password;
                }
                break;

            case WPA_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    config.preSharedKey = password;
                }
                break;

            case WPA2_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    config.preSharedKey = password;
                }
                break;

            case WPA_WPA2:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
                    config.preSharedKey = password;
                }
                break;
        }
        Log.d(TAG, "SSID:"+config.SSID + ", band: " + config.apBand + ", pass word: " + config.preSharedKey);
        return config;
    }
}
