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
import com.chenxi.cebim.fragment.PlanningNodeFragment;
import com.chenxi.cebim.fragment.ReceivedFragment;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private List<Fragment> list;
    private TaskListadapter adapter;
    private RelativeLayout back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        //实例化
        viewPager = (ViewPager) findViewById(R.id.task_list_viewpager);
        tabLayout = (TabLayout) findViewById(R.id.task_list_tablayout);

        back=(RelativeLayout)findViewById(R.id.rl_task_list_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ReceivedFragment receivedFragment = new ReceivedFragment();
        PlanningNodeFragment planningNodeFragment = new PlanningNodeFragment();

        //页面，数据源
        list = new ArrayList<>();
        list.add(receivedFragment);
        list.add(planningNodeFragment);
        //ViewPager的适配器
        adapter = new TaskListadapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        //绑定
        tabLayout.setupWithViewPager(viewPager);
    }

    class TaskListadapter extends FragmentPagerAdapter {

        private String[] title = {"已接收任务", "计划节点"};
        private List<Fragment> fragmentList;

        public TaskListadapter(FragmentManager fm, List<Fragment> fragmentList) {
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
