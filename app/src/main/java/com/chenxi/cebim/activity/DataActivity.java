package com.chenxi.cebim.activity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.IsShowDataRadioButtonEven;
import com.chenxi.cebim.fragment.data.DataCommonUseFragment;
import com.chenxi.cebim.fragment.data.DataFileFragment;
import com.chenxi.cebim.fragment.data.DataRecentFregment;
import com.chenxi.cebim.utils.ActivityCollector;
import com.chenxi.cebim.utils.DirActivityCollector;
import com.chenxi.cebim.view.NoScrollViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class DataActivity extends FragmentActivity implements
        RadioGroup.OnCheckedChangeListener, View.OnLayoutChangeListener {


    private ProgressDialog progressDialog;
    private String projectName;
    private int projectId;

    // ViewPager控件
    private NoScrollViewPager data_viewPager;
    // RadioGroup控件
    private RadioGroup data_tab_RadioGroup;

    private LinearLayout ll_DataRadioGroup;

    // RadioButton控件
    private RadioButton radio_recent, radio_file, radio_common_use;
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

        DirActivityCollector.addActivity(this);//添加进DirActivityCollector中，在移动文件成功时，会finishAll掉此activity，并用Intent重新启动
        setContentView(R.layout.activity_data);

        //注册EvenBus
        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        projectName = intent.getStringExtra("projectName");
        projectId = intent.getIntExtra("projectId", -1);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//用于锁定屏幕，使屏幕不旋转

        activityRootView = findViewById(R.id.data_root_layout);
        //获取屏幕高度
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/3
        keyHeight = screenHeight / 3;
        ActivityCollector.addActivity(this);//由于该Activity不是继承自BaseActivity,此代码用于管理该活动。

        // 界面初始函数，用来获取定义的各控件对应的ID
        InitView(0);

        //设置默认ViewPager
        InitViewPager(1);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //添加layout大小发生改变监听器
        activityRootView.addOnLayoutChangeListener(this);
    }

    //让fragment方便获取ProjectId
    public int getProjectId() {
        return projectId;
    }

    //让fragment方便获取_Id
    public int get_Id() {
        return projectId;
    }

    public void getData() {
        //加载加进度条，加班类别请求成功后消失。
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中...");
        progressDialog.setCancelable(true);
        progressDialog.show();  //将进度条显示出来

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/YZEngineeringdata/GetList/ID=" + projectId)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    if (responseData.equals("true")) {
//                        startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
//                        finish();
                    } else {

                        progressDialog.dismiss();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //radioButtonNum是默认选择的radioButton，isShowRadioGroup用于控制是否显示下面的RadioGroup
    private void InitView(int radioButtonNum) {
        data_tab_RadioGroup = (RadioGroup) findViewById(R.id.data_tab_radiogroup);
        ll_DataRadioGroup = (LinearLayout) findViewById(R.id.ll_data_radiogroup);

        radio_recent = (RadioButton) findViewById(R.id.radio_recent);

        radio_file = (RadioButton) findViewById(R.id.radio_file);

        radio_common_use = (RadioButton) findViewById(R.id.radio_common_use);

        if (radioButtonNum == 0) {
            radio_recent.setChecked(true);
        } else if (radioButtonNum == 1) {
            radio_file.setChecked(true);
        } else if (radioButtonNum == 2) {
            radio_common_use.setChecked(true);
        }
        data_tab_RadioGroup.check(R.id.radio_file);//设置默认RadioButton

        data_tab_RadioGroup.setOnCheckedChangeListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowDataRadioButtonEven isShowDataRadioButtonEven) {
        if (isShowDataRadioButtonEven.getIsShowRadioButton().equals("显示")) {
            ll_DataRadioGroup.setVisibility(View.VISIBLE);
        } else if(isShowDataRadioButtonEven.getIsShowRadioButton().equals("隐藏")){
            ll_DataRadioGroup.setVisibility(View.GONE);
        }
    }

    private void InitViewPager(int radioButtonNum) {
        data_viewPager = (NoScrollViewPager) findViewById(R.id.data_viewPager);

        fragmentList = new ArrayList<Fragment>();

        Fragment dataRecentFregment = new DataRecentFregment();
        Fragment dataFileFragment = new DataFileFragment();
        Fragment dataCommonUseFragment = new DataCommonUseFragment();

        // 将各Fragment加入数组中
        fragmentList.add(dataRecentFregment);
        fragmentList.add(dataFileFragment);
        fragmentList.add(dataCommonUseFragment);

        // 设置ViewPager的设配器
        data_viewPager.setAdapter(new DataAdapter(getSupportFragmentManager(),
                fragmentList));
        // 当前为第一个页面
        data_viewPager.setCurrentItem(radioButtonNum);

        //设置缓存view 的个数,这里让三个一起缓存，避免间隔切换时的刷新问题；
        data_viewPager.setOffscreenPageLimit(3);

        // ViewPager的页面改变监听器
        data_viewPager.setOnPageChangeListener(new DataListner());
    }


    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        // 获取当前被选中的RadioButton的ID，用于改变ViewPager的当前页
        int current = 1;
        switch (i) {
            case R.id.radio_recent:
                current = 0;
                break;

            case R.id.radio_file:
                current = 1;
                break;

            case R.id.radio_common_use:
                current = 2;
                break;
        }
        if (data_viewPager.getCurrentItem() != current) {
            data_viewPager.setCurrentItem(current, true);//设置false用于消除动画效果
        }
    }

    private class DataAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public DataAdapter(FragmentManager fm, ArrayList<Fragment> list) {
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

    private class DataListner implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            // 获取当前页面用于改变对应RadioButton的状态
            int current = data_viewPager.getCurrentItem();
            switch (current) {
                case 0:
                    data_tab_RadioGroup.check(R.id.radio_recent);
                    break;

                case 1:
                    data_tab_RadioGroup.check(R.id.radio_file);
                    break;

                case 2:
                    data_tab_RadioGroup.check(R.id.radio_common_use);
                    break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
