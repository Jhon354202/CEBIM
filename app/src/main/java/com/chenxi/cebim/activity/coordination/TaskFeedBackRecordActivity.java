package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.FeedBackDetailAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.TaskReplyModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 反馈记录界面
 */
public class TaskFeedBackRecordActivity extends BaseActivity {

    ImageView back;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;

    String taskID;
    int createdBy;

    private TaskReplyModel taskReplyModel;
    private List<TaskReplyModel> feedBackRecordList = new ArrayList<>();//获取回调接口中返回的
    private List<TaskReplyModel> tempList = new ArrayList<>();//中转list

    FeedBackDetailAdapter feedBackDetailAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_feed_back_record);

        Intent intent = getIntent();
        taskID = intent.getStringExtra("TaskId");
        createdBy = intent.getIntExtra("CreatedBy", -1);

        initView();
        swipeRefreshLayout.setRefreshing(true);
        getData(false);
    }

    //控件初始化
    private void initView() {

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefreshLayout = findViewById(R.id.feed_back_record_swip_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        recyclerView = findViewById(R.id.feed_back_record_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(TaskFeedBackRecordActivity.this);
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 刷新列表
     */
    private void refreshFile(final boolean isPull) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //如果是下拉刷新，睡两秒，否则直接刷新列表
                if (isPull) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData(isPull);
                    }
                });
            }
        }).start();
    }

    //获取数据
    private void getData(boolean isPull) {

        if(feedBackRecordList!=null){
            feedBackRecordList.clear();
        }

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + taskID + "/Reply?where=CreatedBy=" + createdBy)
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String responseData = null;

                        responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String ID = jsonObject.getString("ID");
                            String TaskId = jsonObject.getString("TaskId");
                            String Remark = jsonObject.getString("Remark");
                            String Pictures = jsonObject.getString("Pictures");
                            String ActualUnit = jsonObject.getString("ActualUnit");
                            String ModelUnit = jsonObject.getString("ModelUnit");

                            String CreatedAt = "";
                            if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                                CreatedAt = jsonObject.get("CreatedAt").toString();
                            }
                            String UpdatedAt = "";
                            if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                                UpdatedAt = jsonObject.get("UpdatedAt").toString();
                            }

                            String CreatedInfo = jsonObject.get("CrUserInfo").toString();
                            String CreatedUserName = "";
                            int CreatedUserID = -1;
                            if (CreatedInfo != null && (!CreatedInfo.equals("null"))) {
                                JSONObject crUserInfoObject = new JSONObject(CreatedInfo);
                                CreatedUserName = crUserInfoObject.getString("UserName");
                                CreatedUserID = crUserInfoObject.getInt("UserID");
                            }

                            String UpUserInfo = jsonObject.get("CrUserInfo").toString();
                            String UpdatedUserName = "";
                            int UpdatedUserID = -1;
                            if (UpUserInfo != null && (!UpUserInfo.equals("null"))) {
                                JSONObject upUserInfoObject = new JSONObject(CreatedInfo);
                                UpdatedUserName = upUserInfoObject.getString("UserName");
                                UpdatedUserID = upUserInfoObject.getInt("UserID");
                            }


                            int PracticalLabor = -1;
                            if (!jsonObject.get("PracticalLabor").toString().equals("null")) {
                                PracticalLabor = jsonObject.getInt("PracticalLabor");
                            }

                            int CreatedBy = -1;
                            if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                                CreatedBy = jsonObject.getInt("CreatedBy");
                            }

                            int UpdatedBy = -1;
                            if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            double Percentage = 0.00;
                            if (!jsonObject.get("Percentage").toString().equals("null")) {
                                Percentage = jsonObject.getInt("Percentage");
                            }

                            taskReplyModel = new TaskReplyModel(ID, TaskId, Remark, Pictures, ActualUnit, ModelUnit,
                                    CreatedAt, UpdatedAt, CreatedUserName, UpdatedUserName, PracticalLabor, CreatedBy,
                                    UpdatedBy, CreatedUserID, UpdatedUserID, Percentage);
                            feedBackRecordList.add(taskReplyModel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //显示附件列表
                                feedBackDetailAdapter = new FeedBackDetailAdapter(TaskFeedBackRecordActivity.this, feedBackRecordList);
                                recyclerView.setAdapter(feedBackDetailAdapter);

                                if (swipeRefreshLayout.isRefreshing()) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

}
