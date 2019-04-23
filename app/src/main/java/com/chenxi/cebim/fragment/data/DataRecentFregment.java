package com.chenxi.cebim.fragment.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;
import com.chenxi.cebim.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class DataRecentFregment extends BaseFragment {
    private View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout back;

    private List<Fragment> list;
    private RecentFragmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_data_recent, container, false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        //实例化
        viewPager = (ViewPager) view.findViewById(R.id.recent_viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.recent_tablayout);
        RecentOpenFragment recentOpenFragment = new RecentOpenFragment();
        RecentDownloadFragment recentDownloadFragment = new RecentDownloadFragment();
        RecentUploadFragment recentUploadFragment = new RecentUploadFragment();

        list = new ArrayList<>();
        list.add(recentOpenFragment);
        list.add(recentDownloadFragment);
        list.add(recentUploadFragment);
        //ViewPager的适配器
        adapter = new RecentFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        //绑定
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVerticalScrollbarPosition(0);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {


                } else if (tab.getPosition() == 1) {

                } else if (tab.getPosition() == 2) {

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        back = (RelativeLayout) view.findViewById(R.id.rl_recent_back);
        back.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    class RecentFragmentAdapter extends FragmentPagerAdapter {

        private String[] title = {"最近打开", "最近下载", "最近上传"};
        private List<Fragment> fragmentList;

        public RecentFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

}





