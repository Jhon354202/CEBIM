package com.chenxi.cebim.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.chenxi.cebim.R;
import com.chenxi.cebim.fragment.NoticeFragment;
import com.chenxi.cebim.fragment.ProjectFragment;
import com.chenxi.cebim.fragment.SettingFragment;
import com.chenxi.cebim.utils.ActivityCollector;
import com.chenxi.cebim.utils.PermissionUtil;
import com.chenxi.cebim.view.NoScrollViewPager;

import java.util.ArrayList;

/**
 * Created by Zhang JianLong on 2018/9/4.
 * End Time:
 * Description:
 */

public class NavigationActivity extends FragmentActivity implements
        RadioGroup.OnCheckedChangeListener, View.OnLayoutChangeListener {

    // ViewPager控件
    private NoScrollViewPager main_viewPager;
    // RadioGroup控件
    private RadioGroup main_tab_RadioGroup;

    private LinearLayout ll_RadioGroup;

    // RadioButton控件
    private RadioButton radio_project, radio_setting;
    //, radio_notice
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
        setContentView(R.layout.activity_navigation);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//用于锁定屏幕，使屏幕不旋转

        activityRootView = findViewById(R.id.root_layout);
        //获取屏幕高度
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;
        ActivityCollector.addActivity(this);//由于该Activity不是继承自BaseActivity,此代码用于管理该活动。

        // 界面初始函数，用来获取定义的各控件对应的ID
        InitView(0);

        PermissionUtil.addPermission(NavigationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "需要手机存储权限");

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
        main_tab_RadioGroup = (RadioGroup) findViewById(R.id.main_tab_RadioGroup);
        ll_RadioGroup = (LinearLayout) findViewById(R.id.ll_RadioGroup);
        ll_RadioGroup.setVisibility(View.VISIBLE);

        radio_project = (RadioButton) findViewById(R.id.radio_project);
        // radio_notice= (RadioButton) findViewById(R.id.radio_notice);
        radio_setting = (RadioButton) findViewById(R.id.radio_setting);

        if (radioButtonNum == 0) {
            radio_project.setChecked(true);
        }
//        else if (radioButtonNum == 1) {
//            radio_notice.setChecked(true);
//        }
        else if (radioButtonNum == 1) {
            radio_setting.setChecked(true);
        }

        main_tab_RadioGroup.setOnCheckedChangeListener(this);
    }

    private void InitViewPager(int radioButtonNum) {
        main_viewPager = (NoScrollViewPager) findViewById(R.id.main_ViewPager);

        fragmentList = new ArrayList<Fragment>();

        Fragment projectFragment = new ProjectFragment();
        //Fragment noticeFragment = new NoticeFragment();
        Fragment settingFragment = new SettingFragment();

        // 将各Fragment加入数组中
        fragmentList.add(projectFragment);
        //fragmentList.add(noticeFragment);
        fragmentList.add(settingFragment);

        // 设置ViewPager的设配器
        main_viewPager.setAdapter(new NavigationAdapter(getSupportFragmentManager(),
                fragmentList));
        // 当前为第一个页面
        main_viewPager.setCurrentItem(radioButtonNum);
        main_viewPager.setOffscreenPageLimit(2);//设置缓存view 的个数,这里让三个一起缓存，避免间隔切换时的刷新问题；
        // ViewPager的页面改变监听器
        main_viewPager.setOnPageChangeListener(new MyListner());
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int CheckedId) {

        // 获取当前被选中的RadioButton的ID，用于改变ViewPager的当前页
        int current = 0;
        switch (CheckedId) {
            case R.id.radio_project:
                current = 0;
                break;

//            case R.id.radio_notice:
//                current = 1;
//                break;

            case R.id.radio_setting:
                current = 1;
                break;
        }
        if (main_viewPager.getCurrentItem() != current) {
            main_viewPager.setCurrentItem(current, true);//设置false用于消除动画效果
        }
    }

//    /**
//     * button点击事件
//     *
//     * @param view
//     */
//    public void onTabClicked(View view) {
//        switch (view.getId()) {
//            case R.id.radio_project:
//                index = 0;
//                break;
//            case R.id.radio_notice:
//                index = 1;
//                break;
//            case R.id.radio_setting:
//                index = 2;
//                break;
//        }
//        if (currentTabIndex != index) {
//            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
//            trx.hide(fragments[currentTabIndex]);
//            if (!fragments[index].isAdded()) {
//                trx.add(R.id.fragment_container, fragments[index]);
//            }
//            trx.show(fragments[index]).commit();
//        }
//        mTabs[currentTabIndex].setSelected(false);
//        // 把当前tab设为选中状态
//        mTabs[index].setSelected(true);
//        currentTabIndex = index;
//    }

    private class NavigationAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public NavigationAdapter(FragmentManager fm, ArrayList<Fragment> list) {
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

    private class MyListner implements ViewPager.OnPageChangeListener {

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
                    main_tab_RadioGroup.check(R.id.radio_project);
                    break;

//                case 1:
//                    main_tab_RadioGroup.check(R.id.radio_notice);
//                    break;

                case 1:
                    main_tab_RadioGroup.check(R.id.radio_setting);
                    break;
            }
        }

    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(NavigationActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
//                    System.exit(0);
                    ActivityCollector.finishAll();
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
