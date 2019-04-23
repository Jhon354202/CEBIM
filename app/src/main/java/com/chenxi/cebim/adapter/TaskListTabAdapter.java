package com.chenxi.cebim.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chenxi.cebim.fragment.TabFragment;

public class TaskListTabAdapter extends FragmentPagerAdapter {

    public static String[] TITLES=new String[]{"已接收","已发布","全部"};

    public TaskListTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        TabFragment fragment=new TabFragment(i);
        return fragment;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
