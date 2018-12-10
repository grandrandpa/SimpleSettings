package com.cdtsp.settings.util;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.util.Base64;
import android.util.Log;

public class AudioInterfaceUtil {
    private static final String TAG = "AudioInterfaceUtil";
    final static int FADER_BALANCE_MIN = -10;
    final static int FADER_BALANCE_MAX = 10;
    final static int TONE_MIN = -6;
    final static int TONE_MAX = 6;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_STANDARD = 0;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_POPULAR = 1;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_ROCK = 2;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_JAZZ = 3;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_CLASSICAL = 4;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_VOICE = 5;
    final static int AUDIO_HAL_PLUGIN_BMT_INFO_CUSTOM = 6;

    private AudioManager mAm;

    public AudioInterfaceUtil(AudioManager am) {this.mAm = am;}

    public boolean setFader(int fader) {
        if ((fader < FADER_BALANCE_MIN) || (fader > FADER_BALANCE_MAX)) {
            Log.e(TAG, "Error: setfader value " + fader + " is invalid");
            return false;
        }

        String key = "ext_hw_plugin_msg_type=5;ext_hw_plugin_usecase=0;ext_hw_plugin_fade=" + fader;
        Log.d(TAG, "setFader: " + key);
        mAm.setParameters(key);
        return true;
    };

    public boolean setBalance(int balance) {
        if ((balance < FADER_BALANCE_MIN) || (balance > FADER_BALANCE_MAX)) {
            Log.e(TAG, "Error: balance value " + balance + " is invalid");
            return false;
        }

        String key = "ext_hw_plugin_msg_type=6;ext_hw_plugin_usecase=0;ext_hw_plugin_balance=" + balance;
        Log.d(TAG, "setBalance: " + key);
        mAm.setParameters(key);
        return true;
    }

    /*
     * 自定义模式单独调用setBass, setMid, setTreble设置参数
     * 所以调用这几个方法+设置参数时，mode都为AUDIO_HAL_PLUGIN_BMT_INFO_CUSTOM
     */
    public boolean setBass(int bass)
    {
        if ((bass < TONE_MIN)||(bass > TONE_MAX))
        {
            Log.e(TAG,"Error: setBass value " +bass+" is invalid");
            return false;
        }
        String key = "ext_hw_plugin_msg_type=7;" +
                "ext_hw_plugin_usecase=0;" +
                "ext_hw_plugin_bmt_filter_type=1;" +
                "ext_hw_plugin_bmt_flag=1;" +
                "ext_hw_plugin_bmt_mode=6;" +
                "ext_hw_plugin_bmt_value="+bass;
        Log.d(TAG,"setBass: "+key);
        mAm.setParameters(key);
        return true;
    }

    public boolean setMiddle(int middle)
    {
        if ((middle < TONE_MIN)||(middle > TONE_MAX))
        {
            Log.e(TAG,"Error: setMiddle value " +middle+ " is invalid");
            return false;
        }
        String key = "ext_hw_plugin_msg_type=7;" +
                "ext_hw_plugin_usecase=0;" +
                "ext_hw_plugin_bmt_filter_type=2;" +
                "ext_hw_plugin_bmt_flag=1;" +
                "ext_hw_plugin_bmt_mode=6;" +
                "ext_hw_plugin_bmt_value="+middle;
        Log.d(TAG,"setMiddle: "+key);
        mAm.setParameters(key);
        return true;
    }

    public boolean setTreble(int treble)
    {
        if( (treble < TONE_MIN) || (treble > TONE_MAX) )
        {
            Log.e(TAG,"Error: setTreble value " +treble+" is invalid!");
            return false;
        }
        String key = "ext_hw_plugin_msg_type=7;" +
                "ext_hw_plugin_usecase=0;" +
                "ext_hw_plugin_bmt_filter_type=3;" +
                "ext_hw_plugin_bmt_flag=1;" +
                "ext_hw_plugin_bmt_mode=6;" +
                "ext_hw_plugin_bmt_value="+treble;
        Log.d(TAG,"setTreble: "+key);
        mAm.setParameters(key);
        return true;
    }

    /*
     * AUDIO_HAL_PLUGIN_BMT_INFO_STANDARD
     * AUDIO_HAL_PLUGIN_BMT_INFO_POPULAR
     * AUDIO_HAL_PLUGIN_BMT_INFO_ROCK
     * AUDIO_HAL_PLUGIN_BMT_INFO_JAZZ
     * AUDIO_HAL_PLUGIN_BMT_INFO_CLASSICAL
     * AUDIO_HAL_PLUGIN_BMT_INFO_VOICE
     *
     * 调用setBmtInfo设置预定义模式
     */
    public boolean setBmtInfo(int bmt_mode)
    {
        if( (bmt_mode < AUDIO_HAL_PLUGIN_BMT_INFO_STANDARD) || (bmt_mode > AUDIO_HAL_PLUGIN_BMT_INFO_VOICE) )
        {
            if (bmt_mode == 6)
                Log.i(TAG, "Please use setBass/Middle/Treble to custom tone!");
            else
                Log.e(TAG, "setBmtInfo bmt_mode "+ bmt_mode+ " is invalid!");

            return false;
        }
        String key = "ext_hw_plugin_msg_type=7;" +
                "ext_hw_plugin_usecase=0;" +
                "ext_hw_plugin_bmt_flag=1;" +
                "ext_hw_plugin_bmt_mode="+bmt_mode;
        Log.d(TAG,"setBmtInfo: "+key);
        mAm.setParameters(key);
        return true;
    }

    public int getFader()
    {
        String key = "ext_hw_plugin_msg_type=11;ext_hw_plugin_usecase=0";
        String result = mAm.getParameters(key);
        String[] aa = result.split(";");
        String[] a1 = aa[0].split("=");
        byte[] ret = Base64.decode(a1[1],Base64.NO_WRAP | Base64.NO_PADDING);
        Log.d(TAG,"getFader: "+ret[4]);
        return ret[4];
    }

    public int getBalance()
    {
        String key = "ext_hw_plugin_msg_type=12;ext_hw_plugin_usecase=0";
        String result = mAm.getParameters(key);
        String[] aa = result.split(";");
        String[] a1 = aa[0].split("=");
        byte[] ret = Base64.decode(a1[1],Base64.NO_WRAP | Base64.NO_PADDING);
        Log.d(TAG, "getBalance: "+ret[4]);
        return ret[4];
    };

    /*
     * getBmtInfo 的返回值 ret_array数组各项含义如下：
     *
     * ret_array[0], bmt_mode
     *
     * ret_array[1-4], bass
     * ret_array[1], bass mask, can ignore now
     * ret_array[2], bass value
     * ret_array[3], bass min
     * ret_array[4], bass max
     *
     * ret_array[5-8] mid
     * ret_array[5], mid mask
     * ret_array[6], mid value
     * ret_array[7], mid min
     * ret_array[8], mid max
     *
     * ret_array[9-12],treble
     * ret_array[9], treble mask
     * ret_array[10], treble value
     * ret_array[11], treble min
     * ret_array[12], treble max
     *
     * the return values like:
     * [4, 0, 3, -6, 6, 0, 0, -6, 6, 0, 3, -6, 6]
     * */
    public int[] getBmtInfo()
    {
        int i;
        int[] ret_array;
        ret_array = new int[13];

        String key = "ext_hw_plugin_msg_type=13;ext_hw_plugin_usecase=0;";
        String result = mAm.getParameters(key);
        String[] aa = result.split(";");
        String[] a1 = aa[0].split("=");
        byte[] ret = Base64.decode(a1[1],Base64.NO_WRAP | Base64.NO_PADDING);

        for (i = 0; i<13;i++) {
            ret_array[i] = ret[i*4];
            //Log.d(TAG, "getBmtInfo bmt_mode: index:"+ i + ", vaule: " + ret_array[i]);
        }

        return ret_array;
    }

}