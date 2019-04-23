package com.chenxi.cebim.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;
import com.chenxi.cebim.fragment.CustomFormFragment;
import com.chenxi.cebim.fragment.UniversalFormFragment;

import java.util.ArrayList;
import java.util.List;

public class FormActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private List<Fragment> list;
    private FragmentAdapter adapter;

    private RelativeLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        //实例化
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);

        back=(RelativeLayout)findViewById(R.id.rl_edit_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        UniversalFormFragment universalFormFragment = new UniversalFormFragment();
        CustomFormFragment customFormFragment = new CustomFormFragment();
        //页面，数据源
        list = new ArrayList<>();
        list.add(universalFormFragment);
        list.add(customFormFragment);
        //ViewPager的适配器
        adapter = new FragmentAdapter(getSupportFragmentManager(),list);
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager);
    }

    class FragmentAdapter extends FragmentPagerAdapter {

        private String [] title = {"通用表单","自定义表单"};
        private List<Fragment> fragmentList;
        public FragmentAdapter(FragmentManager fm,List<Fragment> fragmentList) {
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
