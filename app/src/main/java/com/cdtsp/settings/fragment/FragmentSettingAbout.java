package com.cdtsp.settings.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settingslib.DeviceInfoUtils;
import com.cdtsp.settings.R;
import com.cdtsp.settings.view.SettingProgressGroup;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingAbout extends Fragment{

    private static final String TAG = "FragmentSettingAbout";
    private AudioManager mAudioManager;
    private int mMaxVolume;
    private SettingProgressGroup mVolumeProgressGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView deviceModelTv = view.findViewById(R.id.device_model);
        TextView firmwareVersionTv = view.findViewById(R.id.firmware_version);
        TextView buildNumberTv = view.findViewById(R.id.build_number);
        TextView kernelVersion = view.findViewById(R.id.kernel_version);

        Resources res = getResources();
        deviceModelTv.setText(Build.MODEL);
        firmwareVersionTv.setText(Build.VERSION.RELEASE);
        buildNumberTv.setText(Build.DISPLAY);
        kernelVersion.setText(DeviceInfoUtils.getFormattedKernelVersion());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
