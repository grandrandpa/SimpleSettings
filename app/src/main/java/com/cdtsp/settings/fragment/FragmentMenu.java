package com.cdtsp.settings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdtsp.settings.R;
import com.cdtsp.settings.view.MenuButton;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/18.
 */

public class FragmentMenu extends Fragment{

    private static final String TAG = "FragmentMenu";
    private int mCurPos = -1;
    private Callback mCallback;
    private ArrayList<MenuButton> mMenuButtons = new ArrayList<MenuButton>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //这要求本Fragment所attach的Activity必须是实现了FragmentMenu.Callback接口的
        try {
            mCallback = (FragmentMenu.Callback) context;
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: " + e.toString());
            throw new RuntimeException("ClassCastException : FragmentMenu must be attach to a activity that implements FragmentMenu.Callback");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup menuLayout = view.findViewById(R.id.menu_layout);
        int menuCount = menuLayout.getChildCount();
        for (int i = 0; i < menuCount; i ++) {
            MenuButton menuButton = (MenuButton) menuLayout.getChildAt(i);
            final int pos = i;
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchMediaList(pos);
                }
            });
            mMenuButtons.add(menuButton);
        }
        switchMediaList(0);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 切换多媒体列表
     * @param pos
     */
    public void switchMediaList(int pos) {
        if (pos == mCurPos) {
            return;
        }
        if (mCurPos != -1) {
            mMenuButtons.get(mCurPos).setChecked(false);
        }
        mMenuButtons.get(pos).setChecked(true);
        mCurPos = pos;
        if (mCallback != null) mCallback.onSwitchSettingType(pos);
    }

    public interface Callback{
        void onSwitchSettingType(int pos);
    }
}
