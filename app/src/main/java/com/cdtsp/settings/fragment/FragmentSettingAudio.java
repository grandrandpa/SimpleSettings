package com.cdtsp.settings.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cdtsp.settings.R;
import com.cdtsp.settings.adapter.SoundModeAdapter;
import com.cdtsp.settings.util.MyUtils;
import com.cdtsp.settings.util.AudioInterfaceUtil;
import com.cdtsp.settings.view.SettingProgressGroup;
import com.cdtsp.settings.view.SoundTouchView;

import android.util.Log;
import android.media.AudioManager;
import android.content.Context;
import android.app.Service;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
import com.cdtsp.settings.util.AudioBmtInfo;
/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingAudio extends Fragment{

    private static final String TAG = "FragmentSettingAudio";
    private final int PROGRESS_MAX = 12;
    private final int PROGRESS_DEF = 6;

    private final int BMT_STANDARD = 0;
    private final int BMT_POPULAR = 1;
    private final int BMT_ROCK = 2;
    private final int BMT_JAZZ = 3;
    private final int BMT_CLASSICAL = 4;
    private final int BMT_VOICE = 5;
    private final int BMT_CUSTOM = 6;

    private AudioManager mAudioManager;
    private AudioBmtInfo[] mBmtInfoArray;
    private int mCurrBmtMode;
    private AudioInterfaceUtil mAudioUtil;
    private int[] mCurrentSoundFild;

    private int mMaxSound;
    private SettingProgressGroup mSoundHighProgressGroup;
    private SettingProgressGroup mSoundMidProgressGroup;
    private SettingProgressGroup mSoundLowProgressGroup;
    private TextView mSoundMode;
    private TextView mTvResetField;
    private SoundTouchView mSoundTouchView;
    private ViewGroup mSoundModeLayout;
    private WindowManager mWindowManager;
    private View mWindowView;
    private WindowManager.LayoutParams mWindowViewParams;
    private SoundModeAdapter mSoundModeAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_audio, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initViews(view);
    }

    private void initData() {
        if(mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Service.AUDIO_SERVICE);
        }
        mBmtInfoArray = new AudioBmtInfo[7];
        mBmtInfoArray[BMT_STANDARD] = new AudioBmtInfo(6, 6, 6);
        mBmtInfoArray[BMT_POPULAR] = new AudioBmtInfo(6, 9, 9);
        mBmtInfoArray[BMT_ROCK] = new AudioBmtInfo(11, 6, 11);
        mBmtInfoArray[BMT_JAZZ] = new AudioBmtInfo(6, 9, 11);
        mBmtInfoArray[BMT_CLASSICAL] = new AudioBmtInfo(9, 6, 9);
        mBmtInfoArray[BMT_VOICE] = new AudioBmtInfo(2, 9, 2);
        mBmtInfoArray[BMT_CUSTOM] = new AudioBmtInfo(6, 6, 8);

        mAudioUtil = new AudioInterfaceUtil(mAudioManager);
        mCurrentSoundFild = new int[2];
        mCurrentSoundFild[0] = mAudioUtil.getFader();
        mCurrentSoundFild[1] = mAudioUtil.getBalance();
        
        int[] bmtInfo = mAudioUtil.getBmtInfo();
        mCurrBmtMode = bmtInfo[0];
        Log.d(TAG, "initData balance: " + mCurrentSoundFild[0] + ", Fader: " + mCurrentSoundFild[1] + ", btm mode:" + mCurrBmtMode);

        if (mCurrBmtMode<BMT_STANDARD || BMT_CUSTOM<mCurrBmtMode) {
            mCurrBmtMode = BMT_STANDARD;
        }

        if (mCurrBmtMode == BMT_CUSTOM) {
            mBmtInfoArray[BMT_CUSTOM].setBass(bmtInfo[2]+6);
            mBmtInfoArray[BMT_CUSTOM].setMid(bmtInfo[6]+6);
            mBmtInfoArray[BMT_CUSTOM].setTreble(bmtInfo[10]+6);
        }
    }

    private void initViews(View view) {
        mSoundMode = view.findViewById(R.id.sound_mode);
        mSoundModeLayout = view.findViewById(R.id.sound_mode_layout);
        mSoundModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSoundModeWindow();
            }
        });

        if (mCurrBmtMode == BMT_CUSTOM) {
            Log.d(TAG, "initViews sound name: customize");
            mSoundMode.setText(getContext().getResources().getString(R.string.customize));
        } else {
            String[] sounds = getContext().getResources().getStringArray(R.array.sound_mode);
            Log.d(TAG, "initViews sound name: " + sounds[mCurrBmtMode]);
            mSoundMode.setText(sounds[mCurrBmtMode]);
        }

        initViewProgressGroup(view);
        initViewSoundFeild(view);
    }

    private void initViewSoundFeild(View view) {
        mSoundTouchView = view.findViewById(R.id.sound_filed);
        mTvResetField = view.findViewById(R.id.sound_field_reset);
        mTvResetField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSoundTouchView.reset();
            }
        });
        mSoundTouchView.addOnClickUpCallback(new SoundTouchView.OnClickUpCallback() {
            public void onClickUp(int balance, int fade) {
                Log.d(TAG, "addOnClickUpCallback onProgressChanged balance: " + balance + "fade: " + fade);
                mAudioUtil.setBalance(balance);
                mAudioUtil.setFader(fade);
            };
        });

        Log.d(TAG, "initViewSoundFeild balance: " + mCurrentSoundFild[0] + ", Fader: " + mCurrentSoundFild[1]);
        mSoundTouchView.setPositionByFaderAndBalance(mCurrentSoundFild[0], mCurrentSoundFild[1]);
    }

    /**
     * 获取当前声场位置
     * @return
     */
    private int[] getCurSoundFiledPos() {
        return new int[]{195, 195};
    }

    private void initViewProgressGroup(View view) {
        int treble = 6;
        int middle = 6;
        int bass = 6;
        treble = mBmtInfoArray[mCurrBmtMode].getTreble();
        middle = mBmtInfoArray[mCurrBmtMode].getMid();
        bass = mBmtInfoArray[mCurrBmtMode].getBass();

        mSoundHighProgressGroup = view.findViewById(R.id.sound_seekbar_high);
        mSoundHighProgressGroup.setMax(PROGRESS_MAX);
        mSoundHighProgressGroup.setProgress(treble);
        mSoundHighProgressGroup.setStartValue(String.valueOf(- PROGRESS_MAX / 2));
        mSoundHighProgressGroup.setEndValue(String.valueOf(PROGRESS_MAX / 2));
        mSoundHighProgressGroup.setCurValue(treble-PROGRESS_DEF);
        mSoundHighProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                mSoundHighProgressGroup.setProgress(progress);
                mSoundHighProgressGroup.setCurValue(progress-PROGRESS_DEF);

                if(mAudioManager != null) {
                    Log.d(TAG, "mSoundHighProgressGroup onProgressChanged: " + progress);
                    mAudioUtil.setTreble(progress-6);

                    if(mSoundHighProgressGroup.isDisable() == false) {
                        mBmtInfoArray[BMT_CUSTOM].setTreble(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mSoundMidProgressGroup = view.findViewById(R.id.sound_seekbar_mid);
        mSoundMidProgressGroup.setMax(PROGRESS_MAX);
        mSoundMidProgressGroup.setProgress(middle);
        mSoundMidProgressGroup.setStartValue(String.valueOf(- PROGRESS_MAX / 2));
        mSoundMidProgressGroup.setEndValue(String.valueOf(PROGRESS_MAX / 2));
        mSoundMidProgressGroup.setCurValue(middle-PROGRESS_DEF);
        mSoundMidProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                mSoundMidProgressGroup.setProgress(progress);
                mSoundMidProgressGroup.setCurValue(progress-PROGRESS_DEF);

                if(mAudioManager != null) {
                    Log.d(TAG, "mSoundMidProgressGroup onProgressChanged: " + progress);
                    mAudioUtil.setMiddle(progress-6);

                    if(mSoundHighProgressGroup.isDisable() == false) {
                        mBmtInfoArray[BMT_CUSTOM].setMid(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mSoundLowProgressGroup = view.findViewById(R.id.sound_seekbar_low);
        mSoundLowProgressGroup.setMax(PROGRESS_MAX);
        mSoundLowProgressGroup.setProgress(bass);
        mSoundLowProgressGroup.setStartValue(String.valueOf(- PROGRESS_MAX / 2));
        mSoundLowProgressGroup.setEndValue(String.valueOf(PROGRESS_MAX / 2));
        mSoundLowProgressGroup.setCurValue(bass-PROGRESS_DEF);
        mSoundLowProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                mSoundLowProgressGroup.setProgress(progress);
                mSoundLowProgressGroup.setCurValue(progress-PROGRESS_DEF);

                if(mAudioManager != null) {
                    Log.d(TAG, "mSoundLowProgressGroup onProgressChanged: " + progress);
                    mAudioUtil.setBass(progress-6);

                    if(mSoundHighProgressGroup.isDisable() == false) {
                        mBmtInfoArray[BMT_CUSTOM].setBass(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (mCurrBmtMode==BMT_CUSTOM) {
            enableProgressGroup(BMT_CUSTOM);
        } else {
            disableProgressGroup(mCurrBmtMode);
        }
    }

    private void updateAudioBmt(int pos) {
        int progress = 0;

        if(mAudioManager != null) {
            progress = mBmtInfoArray[pos].getTreble();
            mSoundHighProgressGroup.setProgress(progress);
            mSoundHighProgressGroup.setCurValue(progress-PROGRESS_DEF);
            Log.d(TAG, "setAudioBmt mSoundHighProgressGroup onProgressChanged: " + progress);

            progress = mBmtInfoArray[pos].getMid();
            mSoundMidProgressGroup.setProgress(progress);
            mSoundMidProgressGroup.setCurValue(progress-PROGRESS_DEF);
            Log.d(TAG, "setAudioBmt mSoundMidProgressGroup onProgressChanged: " + progress);

            progress = mBmtInfoArray[pos].getBass();
            mSoundLowProgressGroup.setProgress(progress);
            mSoundLowProgressGroup.setCurValue(progress-PROGRESS_DEF);
            Log.d(TAG, "setAudioBmt mSoundLowProgressGroup onProgressChanged: " + progress);
        }
    }

    private void enableProgressGroup(int pos) {
        mSoundHighProgressGroup.enableDrag();
        mSoundMidProgressGroup.enableDrag();
        mSoundLowProgressGroup.enableDrag();
        updateAudioBmt(pos);
    }

    private void disableProgressGroup(int pos) {
        mSoundHighProgressGroup.disableDrag();
        mSoundMidProgressGroup.disableDrag();
        mSoundLowProgressGroup.disableDrag();
        //mAudioUtil.setBmtInfo(pos);
        updateAudioBmt(pos);
    }

    /**
     * 弹出选择音效窗口
     */
    private void showSoundModeWindow() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);

            //解析出音效选择窗口的View
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            mWindowView = inflater.inflate(R.layout.layout_sound_mode_choose, null);
            View content = mWindowView.findViewById(R.id.content);
            GridView gridView = mWindowView.findViewById(R.id.sound_grid);
            ImageView imgBack = mWindowView.findViewById(R.id.img_back);
            final View customizeSoundView = mWindowView.findViewById(R.id.mode_customize);

            //音效选择窗口：设置点击事件，点击内容区域外的位置可退出音效选择页面
            mWindowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWindowManager.removeView(mWindowView);
                }
            });
            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyUtils.toast(getContext(), "aaaaaaaaaa");
                }
            });

            //音效列表Grid：初始化音效选择窗口的音效列表Grid
            String[] sounds = getContext().getResources().getStringArray(R.array.sound_mode);
            mSoundModeAdapter = new SoundModeAdapter(sounds, getContext());
            gridView.setAdapter(mSoundModeAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String soundMode = (String) parent.getItemAtPosition(position);
                    mSoundMode.setText(soundMode);
                    mWindowManager.removeView(mWindowView);
                    SoundModeAdapter adapter = (SoundModeAdapter) parent.getAdapter();
                    adapter.setCurPos(position);
                    if (customizeSoundView.isSelected()) {
                        customizeSoundView.setSelected(false);
                    }
                    disableProgressGroup(position);
                    mAudioUtil.setBmtInfo(position);
                    mCurrBmtMode = position;
                }
            });

            //返回按键：设置音效选择窗口返回按键的点击事件
            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWindowManager.removeView(mWindowView);
                }
            });

            //自定义音效按钮：设置点击事件
            customizeSoundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSoundModeAdapter.setCurPos(-1);
                    mWindowManager.removeView(mWindowView);
                    customizeSoundView.setSelected(true);
                    mSoundMode.setText(getContext().getResources().getString(R.string.customize));
                    enableProgressGroup(BMT_CUSTOM);
                    mAudioUtil.setBass(mBmtInfoArray[BMT_CUSTOM].getBass()-6);
                    mAudioUtil.setMiddle(mBmtInfoArray[BMT_CUSTOM].getMid()-6);
                    mAudioUtil.setTreble(mBmtInfoArray[BMT_CUSTOM].getTreble()-6);
                }
            });

            //设置默认音效类型
            //Log.d(TAG, "showSoundModeWindow last pos: " + mCurrBmtMode);
            if (mCurrBmtMode == BMT_CUSTOM) {
                mSoundModeAdapter.setCurPos(-1);
                customizeSoundView.setSelected(true);
            } else {
                mSoundModeAdapter.setCurPos(mCurrBmtMode);
            }

            mWindowViewParams = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            mWindowViewParams.format = PixelFormat.RGBA_8888;
            mWindowViewParams.gravity = Gravity.CENTER;
            mWindowViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mWindowViewParams.type = TYPE_APPLICATION_PANEL;
        }
        mWindowManager.addView(mWindowView, mWindowViewParams);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
