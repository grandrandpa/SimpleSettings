package com.cdtsp.settings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cdtsp.settings.R;
import com.cdtsp.settings.SettingsActivity;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingDataFlow extends Fragment implements View.OnClickListener {

    private static final String TAG = "FragmentSettingDataFlow";
    private TextView mTvGoToNetWork;
    private TextView mTvDataToday;
    private TextView mTvDataMonth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_data_flow, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        mTvGoToNetWork = view.findViewById(R.id.go_to_network_setting);
        mTvGoToNetWork.setOnClickListener(this);

        mTvDataToday = view.findViewById(R.id.tv_data_today);
        mTvDataMonth = view.findViewById(R.id.tv_data_month);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_network_setting:
                ((SettingsActivity) getActivity()).switchToSettingOnName(FragmentSettingNetwork.class.getSimpleName());
                break;
            default:
                Log.d(TAG, "onClick: default");
                break;
        }
    }
}
