package com.cdtsp.settings.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.internal.app.LocalePicker;
import com.cdtsp.settings.R;
import com.cdtsp.settings.view.SettingItemGroup;
import com.cdtsp.settings.view.SettingProgressGroup;
import com.cdtsp.settings.view.SettingSwitchItem;

import java.util.Locale;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentSettingSystem extends Fragment implements SettingSwitchItem.OnSwitchCallback {

    private static final String TAG = "FragmentSettingSystem";
    public static final String PROPERTY_GESTURE_AIR = "persist.sys.gestureair";
    public static final String GESTURE_AIR_OFF = "off";
    public static final String GESTURE_AIR_ON = "on";
    private final String mLanguageZh = "zh";
    private final String mLanguageEn = "en";
    private final LocaleList mLocalListZh = new LocaleList(Locale.forLanguageTag(mLanguageZh), Locale.forLanguageTag(mLanguageEn));
    private final LocaleList mLocalListEn = new LocaleList(Locale.forLanguageTag(mLanguageEn), Locale.forLanguageTag(mLanguageZh));
    private AudioManager mAudioManager;
    private int mMaxVolumeMedia;
    private int mMaxVolumePhone;
    private int mMaxVolumeNavi;
    private SettingProgressGroup mMusicVolumeProgressGroup;
    private SettingProgressGroup mPhoneVolumeProgressGroup;
    private SettingProgressGroup mNaviVolumeProgressGroup;
    private SettingProgressGroup mRingVolumeProgressGroup;
    private SettingSwitchItem mSwitchGestureAir;
    private SettingSwitchItem mSwitchLogTool;
    private SettingItemGroup mItemGroupBtnVoice;
    private SettingItemGroup mItemsGroupLang;
    private VolumeChangeReceiver mVolumeChangeReceiver;
    private WindowManager mWm;
    private WindowManager.LayoutParams mWaitingViewParams;
    private ViewGroup mWaitingViewWindow;
    private static HandlerThread sSubThread = new HandlerThread("SubThread-SettingSystem");
    static {
        sSubThread.start();
    }
    private Handler mHanderSub = new Handler(sSubThread.getLooper());
    private final int MSG_LANGUAGE_SWITCH_COMPELTE = 0;
    private Handler mHandlerUI = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LANGUAGE_SWITCH_COMPELTE:
                    dismissWaitingView();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        //这要求本Fragment所attach的Activity必须是实现了FragmentMenu.Callback接口的
//        try {
//            mCallback = (FragmentSettingSystem.Callback) context;
//        } catch (ClassCastException e) {
//            Log.d(TAG, "onAttach: " + e.toString());
//            throw new RuntimeException("ClassCastException : FragmentMenu must be attach to a activity that implements FragmentMenu.Callback");
//        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_system, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    private void initViews(View view) {
        mItemsGroupLang = view.findViewById(R.id.item_group_language);
        String language = getCurLanguage();
        Log.d(TAG, "initViews: language=" +  language);
        if (mLanguageZh.equals(language)) {
            mItemsGroupLang.setSelected(0);
        } else {
            mItemsGroupLang.setSelected(1);
        }
        mItemsGroupLang.setOnItemClickCallback(new SettingItemGroup.OnItemClickCallback() {
            @Override
            public void onItemClick(int curSelectedPos) {
                String[] langs = getResources().getStringArray(R.array.language_opts);
                Log.d(TAG, "onItemClick: " + langs[curSelectedPos]);
                if (getResources().getString(R.string.lang_zh_cn).equals(langs[curSelectedPos])) {
                    showWaitingView(new Runnable() {
                        @Override
                        public void run() {
                            mHanderSub.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LocalePicker.updateLocales(mLocalListZh);
                                    mHandlerUI.sendEmptyMessage(MSG_LANGUAGE_SWITCH_COMPELTE);
                                }
                            }, 1000);//不延迟执行语言切换，将显示不出“等待切换语言”的等待提示框
                        }
                    });

//                    Locale locale = Locale.forLanguageTag("zh-Hans-CN");
//                    LocalePicker.updateLocale(locale);

//                    Locale locale = new Locale("zh-Hans-CN");
//                    Resources resources = getResources();
//                    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
//                    Configuration configuration = resources.getConfiguration();
//                    configuration.setLocale(locale);
//                    resources.updateConfiguration(configuration, displayMetrics, null);
                } else if (getResources().getString(R.string.lang_en).equals(langs[curSelectedPos])) {
                    showWaitingView(new Runnable() {
                        @Override
                        public void run() {
                            mHanderSub.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LocalePicker.updateLocales(mLocalListEn);
                                    mHandlerUI.sendEmptyMessage(MSG_LANGUAGE_SWITCH_COMPELTE);
                                }
                            }, 1000);//不延迟执行语言切换，将显示不出“等待切换语言”的等待提示框
                        }
                    });
                }
            }
        });

//        GestureAirManager gestureAirManager = (GestureAirManager) getActivity().getSystemService(Context.GESTURE_AIR_SERVICE);
//        mSwitchGestureAir = view.findViewById(R.id.switch_gesture_air);
//        if (gestureAirManager.isInitHandWorkSucced()) {//只有当手势识别初始化成功，才初始化其切换开关，否则将开关设为不可用
//            Log.d(TAG, "initViews: gestureAirManager.isInitHandWorkSucced()=" + gestureAirManager.isInitHandWorkSucced());
//            String statusGestureAir = SystemProperties.get(PROPERTY_GESTURE_AIR);
//            Log.d(TAG, "initViews: statusGestureAir=" + statusGestureAir);
//            if (GESTURE_AIR_ON.equals(statusGestureAir)) {
//                mSwitchGestureAir.setSelected(true);
//            } else {
//                mSwitchGestureAir.setSelected(false);
//            }
//            mSwitchGestureAir.registerSwitchCallback(this);
//        } else {
//            mSwitchGestureAir.setEnabled(false);
//            mSwitchGestureAir.setSubTitleText(getResources().getString(R.string.gesuter_air_init_failed));
//        }

        /**
         * 初始化log开关
         */
        mSwitchLogTool = view.findViewById(R.id.switch_log_tool);
        boolean logEnable = SystemProperties.getBoolean("boot.tsplogtool.enable", false);
        Log.d(TAG, "initViews: logEnable=" + logEnable);
        if (logEnable) {
            mSwitchLogTool.setSelected(true);
        } else {
            mSwitchLogTool.setSelected(false);
        }
        mSwitchLogTool.registerSwitchCallback(new SettingSwitchItem.OnSwitchCallback() {
            @Override
            public void onSwitchOn() {
                //打开log开关
                boolean logEnable = SystemProperties.getBoolean("boot.tsplogtool.enable", false);
                if (!logEnable) {
                    SystemProperties.set("boot.tsplogtool.enable", "true");
                }
            }

            @Override
            public void onSwitchOff() {
                //关闭log开关
                boolean logEnable = SystemProperties.getBoolean("boot.tsplogtool.enable", false);
                if (logEnable) {
                    SystemProperties.set("boot.tsplogtool.enable", "false");
                }
            }
        });

        setupVolumeProgressGroup(view);

        initBtnVoiceView(view);
    }

    /**
     * 初始化按键音开关
     */
    private void initBtnVoiceView(View view) {
        mItemGroupBtnVoice = (SettingItemGroup) view.findViewById(R.id.item_group_btn_voice);
        boolean btnVoice = false;
        try {
            btnVoice = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED) == 1 ? true : false;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (btnVoice) {
            int streamVolumeSystem = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            if (streamVolumeSystem > 0 && streamVolumeSystem <= 1) {
                mItemGroupBtnVoice.setSelected(1);
            } else if (streamVolumeSystem > 1 && streamVolumeSystem <= 4) {
                mItemGroupBtnVoice.setSelected(2);
            } else if (streamVolumeSystem > 4 && streamVolumeSystem <= 7) {
                mItemGroupBtnVoice.setSelected(3);
            }
        } else {
            mItemGroupBtnVoice.setSelected(0);
        }
        mItemGroupBtnVoice.setOnItemClickCallback(new SettingItemGroup.OnItemClickCallback() {
            @Override
            public void onItemClick(int curSelectedPos) {
                switch (curSelectedPos) {
                    case 0://关闭按键音
//                        mAudioManager.unloadSoundEffects();
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                                0);
                        break;
                    case 1://按键音低
//                        mAudioManager.loadSoundEffects();
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                                1);
                        mAudioManager.setStreamVolume(
                                AudioManager.STREAM_SYSTEM,
                                1,
                                AudioManager.FLAG_PLAY_SOUND
                        );
                        break;
                    case 2://按键音中
//                        mAudioManager.loadSoundEffects();
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                                1);
                        mAudioManager.setStreamVolume(
                                AudioManager.STREAM_SYSTEM,
                                4,
                                AudioManager.FLAG_PLAY_SOUND
                        );
                        break;
                    case 3://按键音高
//                        mAudioManager.loadSoundEffects();
                        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED,
                                1);
                        mAudioManager.setStreamVolume(
                                AudioManager.STREAM_SYSTEM,
                                7,
                                AudioManager.FLAG_PLAY_SOUND
                        );
                        break;
                }
            }
        });
    }

    /**
     * 配置音量调节进度条
     * @param view
     */
    private void setupVolumeProgressGroup(View view) {
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mMaxVolumeMedia = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolumeMedia = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMusicVolumeProgressGroup = view.findViewById(R.id.media_volume);
        mMusicVolumeProgressGroup.setMax(mMaxVolumeMedia);
        mMusicVolumeProgressGroup.setProgress(curVolumeMedia);
        mMusicVolumeProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress,
                        AudioManager.FLAG_PLAY_SOUND
                );
                mMusicVolumeProgressGroup.setCurValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        mMaxVolumePhone = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int curVolumePhone = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        mPhoneVolumeProgressGroup = view.findViewById(R.id.phone_volume);
        mPhoneVolumeProgressGroup.setMax(mMaxVolumePhone);
        mPhoneVolumeProgressGroup.setProgress(curVolumePhone);
        mPhoneVolumeProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        progress,
                        AudioManager.FLAG_PLAY_SOUND
                );
                mPhoneVolumeProgressGroup.setCurValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mMaxVolumeNavi = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);
        int curVolumeNavi = mAudioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY);
        mNaviVolumeProgressGroup = view.findViewById(R.id.navi_volume);
        mNaviVolumeProgressGroup.setMax(mMaxVolumeNavi);
        mNaviVolumeProgressGroup.setProgress(curVolumeNavi);
        mNaviVolumeProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_ACCESSIBILITY,
                        progress,
                        AudioManager.FLAG_PLAY_SOUND
                );
                mNaviVolumeProgressGroup.setCurValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        int maxVolumeRing = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int curVolumeRing = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        mRingVolumeProgressGroup = view.findViewById(R.id.ring_volume);
        mRingVolumeProgressGroup.setMax(maxVolumeRing);
        mRingVolumeProgressGroup.setProgress(curVolumeRing);
        mRingVolumeProgressGroup.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        progress,
                        AudioManager.FLAG_PLAY_SOUND
                );
                mRingVolumeProgressGroup.setCurValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVolumeChangeReceiver = new VolumeChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VolumeChangeReceiver.VOLUME_CHANGED_ACTION);
        getActivity().registerReceiver(mVolumeChangeReceiver, filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mVolumeChangeReceiver);
        mHanderSub.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSwitchOn() {
//        GestureAirManager gestureAirManager = (GestureAirManager) getActivity().getSystemService(Context.GESTURE_AIR_SERVICE);
//        gestureAirManager.activateGestureAir();//激活手势识别
    }

    @Override
    public void onSwitchOff() {
//        GestureAirManager gestureAirManager = (GestureAirManager) getActivity().getSystemService(Context.GESTURE_AIR_SERVICE);
//        gestureAirManager.cancelGestureAir();//关闭手势识别
    }

    private String getCurLanguage() {
        Locale locale = LocalePicker.getLocales().get(0);
        return locale.getLanguage();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void showWaitingView(Runnable r) {
        if (mWm == null) {
            mWm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            mWaitingViewParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            mWaitingViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mWaitingViewParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mWaitingViewParams.format = PixelFormat.RGBA_8888;
            mWaitingViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            View waitingView = LayoutInflater.from(getContext()).inflate(com.cdtsp.hmilib.R.layout.dialog_wait, null);
            waitingView.setBackgroundResource(com.cdtsp.hmilib.R.drawable.dialog_bg);
            TextView messageView = waitingView.findViewById(R.id.message_view);
            messageView.setText(R.string.language_switching);
            RelativeLayout.LayoutParams waitingViewParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            waitingViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            mWaitingViewWindow = new RelativeLayout(getContext());
            mWaitingViewWindow.setBackgroundResource(R.color.colorBgWaiting);
            mWaitingViewWindow.addView(waitingView, waitingViewParams);
        }
        mWm.addView(mWaitingViewWindow, mWaitingViewParams);
        mWaitingViewWindow.getViewTreeObserver().addOnWindowShownListener(new ViewTreeObserver.OnWindowShownListener() {
            @Override
            public void onWindowShown() {
                Log.d(TAG, "onWindowShown() called");
                mWaitingViewWindow.getViewTreeObserver().removeOnWindowShownListener(this);
                r.run();
            }
        });
        mWaitingViewWindow.getViewTreeObserver().addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
            @Override
            public void onWindowAttached() {
                Log.d(TAG, "onWindowAttached() called");
                mWaitingViewWindow.getViewTreeObserver().removeOnWindowAttachListener(this);
                r.run();
            }

            @Override
            public void onWindowDetached() {
                //
            }
        });
    }
    private void dismissWaitingView() {
        if (mWm != null) {
            mWm.removeView(mWaitingViewWindow);
        }
    }

    /**
     * 监听音量变化的广播
     */
    private class VolumeChangeReceiver extends BroadcastReceiver {
        private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
        private static final String EXTRA_VOLUME_STREAM_TYPE="android.media.EXTRA_VOLUME_STREAM_TYPE";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            String action = intent.getAction();
            if (VOLUME_CHANGED_ACTION.equals(action)) {
                int streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1);
                if (streamType == AudioManager.STREAM_MUSIC) {
                    mMusicVolumeProgressGroup.setProgress(mAudioManager.getStreamVolume(streamType));
                } else if (streamType == AudioManager.STREAM_VOICE_CALL) {
                    mPhoneVolumeProgressGroup.setProgress(mAudioManager.getStreamVolume(streamType));
                } else if (streamType == AudioManager.STREAM_ACCESSIBILITY) {
                    mNaviVolumeProgressGroup.setProgress(mAudioManager.getStreamVolume(streamType));
                }
            }
        }
    }
}
