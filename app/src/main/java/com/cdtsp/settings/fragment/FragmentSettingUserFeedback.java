package com.cdtsp.settings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingUserFeedback extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingUserFeedback";
    private RadioGroup mRadioGroup;
    private ImageView mImgSend, mImgVoice;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_user_feedback, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        mRadioGroup = view.findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.radio_sys:
                        //TODO
                        break;
                    case R.id.radio_media:
                        //TODO
                        break;
                    case R.id.radio_navi:
                        //TODO
                        break;
                    case R.id.radio_bt:
                        //TODO
                        break;
                    case R.id.radio_others:
                        //TODO
                        break;
                    default:
                        Log.d(TAG, "onCheckedChanged: default");
                        break;
                }
            }
        });

        mImgSend = view.findViewById(R.id.send);
        mImgVoice = view.findViewById(R.id.voice);
        mImgSend.setOnClickListener(this);
        mImgVoice.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                break;
            case R.id.voice:
                break;
            default:
                Log.d(TAG, "onClick: default");
                break;
        }
    }
}
