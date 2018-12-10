package com.cdtsp.settings.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cdtsp.settings.R;

/**
 * Created by Administrator on 2018/2/27.
 */

public class SoundModeAdapter extends BaseAdapter {

    private String[] mSounds;
    private Context mContext;
    private int mCurPos = -1;

    public SoundModeAdapter(String[] mSounds, Context mContext) {
        this.mSounds = mSounds;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mSounds.length;
    }

    @Override
    public Object getItem(int position) {
        return mSounds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Viewholder viewholder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_sound_mode, null);
            viewholder = new Viewholder();
            viewholder.soundMode = convertView.findViewById(R.id.text);
            convertView.setTag(viewholder);
        } else {
            viewholder = (Viewholder) convertView.getTag();
        }
        if (mCurPos == position) {
            viewholder.soundMode.setSelected(true);
        } else {
            viewholder.soundMode.setSelected(false);
        }
        viewholder.soundMode.setText(mSounds[position]);
        return convertView;
    }

    private class Viewholder {
        TextView soundMode;
    }

    public void setCurPos(int pos) {
        mCurPos = pos;
        notifyDataSetChanged();
    }

    public int getCurPos() {
        return mCurPos==-1 ? 0 : mCurPos;
    }
}
