package com.cdtsp.settings.util;

import android.content.Context;


public class AudioBmtInfo {
    private int mBass;
    private int mMid;
    private int mTreble;

    public AudioBmtInfo(int bass, int mid, int treble) {
        this.mBass = bass;
        this.mMid = mid;
        this.mTreble = treble;
    }

    public int getBass() {
        return this.mBass;
    }
    public int getMid() {
        return this.mMid;
    }
    public int getTreble() {
        return this.mTreble;
    }

    public void setBass(int value) {
        this.mBass = value;
    }
    public void setMid(int value)  {
        this.mMid = value;
    }
    public void setTreble(int value)  {
        this.mTreble = value;
    }
}