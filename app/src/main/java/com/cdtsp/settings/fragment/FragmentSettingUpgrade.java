package com.cdtsp.settings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingUpgrade extends Fragment implements View.OnClickListener {

    private static final String TAG = "FragmentSettingUpgrade";
    private TextView mTvSysVersion;
    private TextView mTvMcuVersion;
    private TextView mTvNaviVersion;
    private TextView mTvVoiceVersion;
    private TextView mTvBtVersion;
    private TextView mTvTagUSB, mTvTagWLAN;
    private FragmentManager mFragmentManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_upgrade, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        initViewTag(view);
        initViewRomInfo(view);
    }

    private void initViewTag(View view) {
        mTvTagUSB = view.findViewById(R.id.tv_tag_usb);
        mTvTagWLAN = view.findViewById(R.id.tv_tag_wlan);
        
        mTvTagUSB.setSelected(true);
        
        mTvTagUSB.setOnClickListener(this);
        mTvTagWLAN.setOnClickListener(this);
    }

    private void initViewRomInfo(View view) {
        mTvSysVersion = view.findViewById(R.id.tv_sys_version);
        mTvMcuVersion = view.findViewById(R.id.tv_mcu_version);
        mTvNaviVersion = view.findViewById(R.id.tv_navi_version);
        mTvVoiceVersion = view.findViewById(R.id.tv_voice_version);
        mTvBtVersion = view.findViewById(R.id.tv_bt_version);

        mTvSysVersion.setText(getResources().getString(R.string.sys_version) + getSysVersion());
        mTvMcuVersion.setText(getResources().getString(R.string.mcu_version) + getMcuVersion());
        mTvNaviVersion.setText(getResources().getString(R.string.navi_version) + getNaviVersion());
        mTvVoiceVersion.setText(getResources().getString(R.string.voice_version) + getVoiceVersion());
        mTvBtVersion.setText(getResources().getString(R.string.bt_version) + getBtVersion());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.container_upgrade, new FragmentUpgradeUSB(), FragmentUpgradeUSB.class.getSimpleName());
        FragmentUpgradeWLAN fragmentUpgradeWLAN = new FragmentUpgradeWLAN();
        transaction.add(R.id.container_upgrade, fragmentUpgradeWLAN, FragmentUpgradeWLAN.class.getSimpleName());
        transaction.hide(fragmentUpgradeWLAN);
        transaction.commit();
        mCurTag = FragmentUpgradeUSB.class.getSimpleName();
    }

    public String getSysVersion() {
        //TODO
        return "     ----------";
    }

    public String getMcuVersion() {
        //TODO
        return "     ----------";
    }

    public String getNaviVersion() {
        //TODO
        return "     ----------";
    }

    public String getVoiceVersion() {
        //TODO
        return "     ----------";
    }

    public String getBtVersion() {
        //TODO
        return "     ----------";

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tag_usb:
                switchFragmentTo(FragmentUpgradeUSB.class.getSimpleName());
                mTvTagUSB.setSelected(true);
                mTvTagWLAN.setSelected(false);
                break;
            case R.id.tv_tag_wlan:
                switchFragmentTo(FragmentUpgradeWLAN.class.getSimpleName());
                mTvTagUSB.setSelected(false);
                mTvTagWLAN.setSelected(true);
                break;
            default:
                Log.d(TAG, "onClick: default");
                break;
        }
    }

    private String mCurTag;
    private void switchFragmentTo(String toTag) {
        if (mCurTag != null && mCurTag.equals(toTag)) {
            return;
        }
        Fragment curFragment = mFragmentManager.findFragmentByTag(mCurTag);
        Fragment toFragment = mFragmentManager.findFragmentByTag(toTag);
        if (toFragment == null) {
            Log.d(TAG, "switchFragmentTo: switchFragmentTo " + toTag + " failed ! toFragment=" + toFragment);
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.show(toFragment);
        transaction.hide(curFragment);
        transaction.commit();

        mCurTag = toTag;
    }
}
