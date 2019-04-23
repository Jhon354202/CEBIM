package com.chenxi.cebim.fragment.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.PublishTaskActivity;
import com.chenxi.cebim.activity.coordination.TaskScreenActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.fragment.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TaskFragment extends BaseFragment {

    View view;
    ImageView back, add, screen;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> list;
    private TaskFragmentAdapter adapter;

    private List<QuestionModel> questionList = new ArrayList<>();//获取回调接口中返回的
    int mPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task, container, false);

        initView();//控件初始化
        initData();//数据初始化

        return view;
    }

    private void initView() {
        back = view.findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        add = view.findViewById(R.id.toolbar_first_right_iv);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), PublishTaskActivity.class);
                startActivity(intent);
            }
        });
        screen = view.findViewById(R.id.toolbar_second_right_iv);
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),TaskScreenActivity.class);
                intent.putExtra("position",mPosition);
                startActivity(intent);
            }
        });
        //实例化
        viewPager = (ViewPager) view.findViewById(R.id.task_viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.task_tablayout);

        TaskReceivedFragment taskReceivedFragment = new TaskReceivedFragment();
        TaskPublishedFragment taskPublishedFragment = new TaskPublishedFragment();
        TaskAllFragment taskAllFragment = new TaskAllFragment();

        list = new ArrayList<>();
        list.add(taskReceivedFragment);
        list.add(taskPublishedFragment);
        list.add(taskAllFragment);
        //ViewPager的适配器
        adapter = new TaskFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        //绑定
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVerticalScrollbarPosition(0);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mPosition=0;
                } else if (tab.getPosition() == 1) {
                    mPosition=1;
                } else if (tab.getPosition() == 2) {
                    mPosition=2;
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

    /**
     * 数据初始化
     */
    private void initData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            Integer ProjectId = jsonObject.getInt("ProjectId");

                            Integer ClosedUserId;
                            if (jsonObject.get("ClosedUserId").toString().equals("null")) {
                                ClosedUserId = null;
                            } else {
                                ClosedUserId = jsonObject.getInt("GroupId");
                            }

                            Integer Priority;
                            if (jsonObject.get("Priority").toString().equals("null")) {
                                Priority = null;
                            } else {
                                Priority = jsonObject.getInt("Priority");
                            }

                            Integer UserId;
                            if (jsonObject.get("UserId").toString().equals("null")) {
                                UserId = null;
                            } else {
                                UserId = jsonObject.getInt("UserId");
                            }

                            Integer UpdatedBy;
                            if (jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = null;
                            } else {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            String ID = jsonObject.getString("ID");
                            String Title = jsonObject.getString("Title");
                            String Comment = jsonObject.getString("Comment");
                            String GroupId = jsonObject.getString("GroupId");
                            String Category = jsonObject.getString("Category");
                            String ViewportId = jsonObject.getString("ViewportId");
                            String SystemType = jsonObject.getString("SystemType");
                            String At = jsonObject.getString("At");
                            String Pictures = jsonObject.getString("Pictures");
                            String Uuids = jsonObject.getString("Uuids");
                            String SelectionSetIds = jsonObject.getString("SelectionSetIds");
                            String Video = jsonObject.getString("Video");
                            String Voice = jsonObject.getString("Voice");
                            String Tags = jsonObject.getString("Tags");
                            String ReadUsers = jsonObject.getString("ReadUsers");
                            String DocumentIds = jsonObject.getString("DocumentIds");

                            Boolean State;
                            if (jsonObject.get("State").toString().equals("null")) {
                                State = null;
                            } else {
                                State = jsonObject.getBoolean("State");
                            }

                            Boolean Observed;
                            if (jsonObject.get("Observed").toString().equals("null")) {
                                Observed = null;
                            } else {
                                Observed = jsonObject.getBoolean("Observed");
                            }

                            Boolean IsFinishedAndDelay;
                            if (jsonObject.get("IsFinishedAndDelay").toString().equals("null")) {
                                IsFinishedAndDelay = null;
                            } else {
                                IsFinishedAndDelay = jsonObject.getBoolean("IsFinishedAndDelay");
                            }

                            Object CompletedAt = jsonObject.get("CompletedAt");
                            Object Deadline = jsonObject.get("Deadline");
                            Object Date = jsonObject.get("Date");
                            Object LastUpdate = jsonObject.get("LastUpdate");

                            Object CategoryName = jsonObject.get("CategoryName");
                            Object UserInfo = jsonObject.get("UserInfo");
                            Object SystemTypeName = jsonObject.get("SystemTypeName");

//                            QuestionModel questionModel = new QuestionModel(ProjectId, ClosedUserId, Priority, UserId, UpdatedBy,
//                                    ID, Title, Comment, GroupId,Category, ViewportId, SystemType, At, Pictures,
//                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers,DocumentIds, State, Observed, IsFinishedAndDelay,
//                                    CompletedAt, Deadline, Date, LastUpdate, CategoryName,UserInfo, SystemTypeName);
//
//                            //数据源
//                            questionList.add(questionModel);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据解析出错");
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    class TaskFragmentAdapter extends FragmentStatePagerAdapter {

        private String[] title = {"已接收", "已发布", "全部"};
        private List<Fragment> fragmentList;

        public TaskFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
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
