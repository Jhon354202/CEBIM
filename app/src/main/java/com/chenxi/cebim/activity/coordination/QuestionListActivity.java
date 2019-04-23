package com.chenxi.cebim.activity.coordination;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.fragment.CompleteFragment;
import com.chenxi.cebim.fragment.DelayFragment;
import com.chenxi.cebim.fragment.UnDoneFragment;

import java.util.ArrayList;
import java.util.List;

public class QuestionListActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private List<Fragment> list;
    private QuestionListadapter adapter;
    private RelativeLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        //实例化
        viewPager = (ViewPager) findViewById(R.id.question_list_viewpager);
        tabLayout = (TabLayout) findViewById(R.id.question_list_tablayout);

        back=(RelativeLayout)findViewById(R.id.rl_question_list_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        UnDoneFragment unDoneFragment = new UnDoneFragment();
        DelayFragment delayFragment = new DelayFragment();
        CompleteFragment completeFragment = new CompleteFragment();
        //页面，数据源
        list = new ArrayList<>();
        list.add(unDoneFragment);
        list.add(delayFragment);
        list.add(completeFragment);
        //ViewPager的适配器
        adapter = new QuestionListadapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager);
    }

    class QuestionListadapter extends FragmentPagerAdapter {

        private String[] title = {"未完成", "已延期", "已完成"};
        private List<Fragment> fragmentList;

        public QuestionListadapter(FragmentManager fm, List<Fragment> fragmentList) {
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
