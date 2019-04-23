package com.chenxi.cebim.activity.model;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.RecentlyModelListClearEvent;
import com.chenxi.cebim.fragment.AllModelFragment;
import com.chenxi.cebim.fragment.ModelListRecentlyFragment;
import com.chenxi.cebim.utils.ACache;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ModelListActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout clearCache, back;
    private LinearLayout searchAndEdit;
    private TextView edit;
    private ImageView search;

    private List<Fragment> list;
    private ModelListFragmentAdapter adapter;
    private String projectName;
    private int projectId;

    int mPosition;//当前在哪个标签下
    private ACache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_list);
        Intent intent = getIntent();
        projectName = intent.getStringExtra("projectName");
        projectId = intent.getIntExtra("projectId", -1);

        mCache = ACache.get(ModelListActivity.this);

        //最近列表的清除按钮，用于清除缓存
        clearCache = (RelativeLayout) findViewById(R.id.rl_model_list_clear);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCache.remove("最近打开模型" + projectId);
                EventBus.getDefault().post(new RecentlyModelListClearEvent("清除最近列表"));
            }
        });

        //控件初始化
        initView();

    }

    private void initView() {
        back = (RelativeLayout) findViewById(R.id.rl_model_list_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchAndEdit = (LinearLayout) findViewById(R.id.rl_model_list_search);

        edit = findViewById(R.id.tv_model_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(edit.getText().toString().equals("编辑")){
                    edit.setText("取消");
                    //通知AllModelFragment显示底部导航栏
                    EventBus.getDefault().post(new CommonEven("打开AllModelFragment全部底部导航栏"));


                }else if(edit.getText().toString().equals("取消")){
                    edit.setText("编辑");
                    //通知AllModelFragment关闭底部导航栏
                    EventBus.getDefault().post(new CommonEven("关闭AllModelFragment全部底部导航栏"));

                }

            }
        });

        search = findViewById(R.id.iv_model_list_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ModelListActivity.this,ScreecModelActivity.class);
                startActivity(intent);
            }
        });

        //实例化
        viewPager = (ViewPager) findViewById(R.id.model_List_viewpager);
        tabLayout = (TabLayout) findViewById(R.id.model_List_tablayout);

        ModelListRecentlyFragment recentlyFragment = new ModelListRecentlyFragment();
        AllModelFragment allFragment = new AllModelFragment();
        //页面，数据源
        list = new ArrayList<>();
        list.add(recentlyFragment);
        list.add(allFragment);
        //ViewPager的适配器
        adapter = new ModelListFragmentAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);

        //绑定
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVerticalScrollbarPosition(0);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    searchAndEdit.setVisibility(View.GONE);
                    clearCache.setVisibility(View.VISIBLE);
                    mPosition = 0;
                } else if (tab.getPosition() == 1) {
                    clearCache.setVisibility(View.GONE);
                    searchAndEdit.setVisibility(View.VISIBLE);
                    mPosition = 1;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    //让fragment方便获取ProjectName
    public String getProjectName() {

        return projectName.trim();
    }

    //让fragment方便获取ProjectId
    public int getProjectId() {

        return projectId;
    }

    class ModelListFragmentAdapter extends FragmentPagerAdapter {

        private String[] title = {"最近", "全部"};
        private List<Fragment> fragmentList;

        public ModelListFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
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
