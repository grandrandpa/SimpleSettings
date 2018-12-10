package com.cdtsp.settings.fragment;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

import com.cdtsp.settings.R;
import com.cdtsp.settings.util.MyUtils;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingReset extends Fragment implements View.OnClickListener {

    private static final String TAG = "FragmentSettingReset";
    private TextView mTvReset;
    
    private Timer mTimer;
    static int mClickCount = 0;
    private final int PRESET_TIMES = 5;
    
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mClickCount = 0;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_reset, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvReset = view.findViewById(R.id.reset);
        mTvReset.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset:
                MyUtils.toast(getContext().getApplicationContext(), "恢复出厂设置");
                triggerOnce();
                break;
            default:
                Log.d(TAG, "onClick: default");
                break;
        }
    }
    
    public void  triggerOnce() {
        Log.d(TAG, " triggerOnce" + mClickCount);
        checkClickTimes();
    }
    
    private void checkClickTimes() {
        mClickCount++;
        Log.d(TAG, "checkClickTimes : " + mClickCount);
        stopTimer();
        if(PRESET_TIMES == mClickCount) {
            Intent in = new Intent();
            in.setClassName("com.cdtsp.engineermode", "com.cdtsp.engineermode.EngineerActivity");
            if(in != null) {
                startActivity(in);
            }
        }

        startTimer();
    }

    private void startTimer(){

        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mClickCount = 0;
                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, 2000);
    }

    private void stopTimer(){

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
