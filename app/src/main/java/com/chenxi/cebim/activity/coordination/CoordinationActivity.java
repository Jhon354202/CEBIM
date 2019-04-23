package com.chenxi.cebim.activity.coordination;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.chenxi.cebim.R;
import com.chenxi.cebim.fragment.coordination.QuestionFragment;
import com.chenxi.cebim.fragment.coordination.TaskFragment;
import com.chenxi.cebim.utils.ActivityCollector;
import com.chenxi.cebim.view.NoScrollViewPager;

import java.util.ArrayList;

public class CoordinationActivity extends FragmentActivity implements
        RadioGroup.OnCheckedChangeListener, View.OnLayoutChangeListener {

    // ViewPager控件
    private NoScrollViewPager main_viewPager;
    // RadioGroup控件
    private RadioGroup coordinationRadioGroup;

    private LinearLayout ll_RadioGroup;

    // RadioButton控件
    private RadioButton radio_question, radio_task;
    // 类型为Fragment的动态数组
    private ArrayList<Fragment> fragmentList;

    //Activity最外层的Layout视图
    private View activityRootView;
    //屏幕高度
    private int screenHeight = 0;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordination);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//用于锁定屏幕，使屏幕不旋转

        activityRootView = findViewById(R.id.coordination_root_layout);
        //获取屏幕高度
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;
        ActivityCollector.addActivity(this);//由于该Activity不是继承自BaseActivity,此代码用于管理该活动。

        // 界面初始函数，用来获取定义的各控件对应的ID
        InitView(0);

        // ViewPager初始化函数
        InitViewPager(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //添加layout大小发生改变监听器
        activityRootView.addOnLayoutChangeListener(this);
    }

    private void InitView(int radioButtonNum) {
        coordinationRadioGroup = (RadioGroup) findViewById(R.id.coordination_RadioGroup);
        ll_RadioGroup = (LinearLayout) findViewById(R.id.ll_coordination_RadioGroup);
        ll_RadioGroup.setVisibility(View.VISIBLE);

        radio_question = (RadioButton) findViewById(R.id.radio_question);
        radio_task= (RadioButton) findViewById(R.id.radio_task);

        if (radioButtonNum == 0) {
            radio_question.setChecked(true);
        } else if (radioButtonNum == 1) {
            radio_task.setChecked(true);
        }

        coordinationRadioGroup.setOnCheckedChangeListener(this);
    }

    private void InitViewPager(int radioButtonNum) {
        main_viewPager = (NoScrollViewPager) findViewById(R.id.coordination_ViewPager);

        fragmentList = new ArrayList<Fragment>();

        Fragment questionFragment = new QuestionFragment();
        Fragment taskFragment = new TaskFragment();

        // 将各Fragment加入数组中
        fragmentList.add(questionFragment);
        fragmentList.add(taskFragment);

        // 设置ViewPager的设配器
        main_viewPager.setAdapter(new CoordinationActivity.CoordinationAdapter(getSupportFragmentManager(),
                fragmentList));
        // 当前为第一个页面
        main_viewPager.setCurrentItem(radioButtonNum);
        main_viewPager.setOffscreenPageLimit(2);//设置缓存view 的个数,这里让三个一起缓存，避免间隔切换时的刷新问题；
        // ViewPager的页面改变监听器
        main_viewPager.setOnPageChangeListener(new CoordinationListner());
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int CheckedId) {

        // 获取当前被选中的RadioButton的ID，用于改变ViewPager的当前页
        int current = 0;
        switch (CheckedId) {
            case R.id.radio_question:
                current = 0;
                break;

            case R.id.radio_task:
                current = 1;
                break;
        }
        if (main_viewPager.getCurrentItem() != current) {
            main_viewPager.setCurrentItem(current, true);//设置false用于消除动画效果
        }
    }

    private class CoordinationAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public CoordinationAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    private class CoordinationListner implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            // 获取当前页面用于改变对应RadioButton的状态
            int current = main_viewPager.getCurrentItem();
            switch (current) {
                case 0:
                    coordinationRadioGroup.check(R.id.radio_question);
                    break;

                case 1:
                    coordinationRadioGroup.check(R.id.radio_task);
                    break;
            }
        }

    }
}
