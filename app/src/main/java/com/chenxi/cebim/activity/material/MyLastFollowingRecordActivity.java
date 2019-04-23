package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.MyLastFollowingRecordAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.entity.MaterialTraceModel;

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

public class MyLastFollowingRecordActivity extends BaseActivity {

    private ImageView back, search;
    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;
    private RelativeLayout list_is_null;

    private int projectId;

    private List<MaterialTraceModel> materialTraceList = new ArrayList<>();
    private MyLastFollowingRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_last_following_record);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        initView();

        //获取网络数据
        initData(0, true);
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

        search = findViewById(R.id.toolbar_second_right_iv);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyLastFollowingRecordActivity.this, LastFollowingSearchActivity.class);
                startActivity(intent);
            }
        });

        refresh = findViewById(R.id.last_followint_record_swip_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        recyclerView = findViewById(R.id.last_followint_record_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        list_is_null = findViewById(R.id.rl_last_followint_record_list_is_null);
    }

    //获取材料跟踪数据
    private void initData(int isRefresh, boolean isFirstShow) {
        materialTraceList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);
                    materialTraceList = JSON.parseArray(responseData, MaterialTraceModel.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (materialTraceList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                                if (refresh.isRefreshing()) {
                                    refresh.setRefreshing(false);
                                }
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new MyLastFollowingRecordAdapter(materialTraceList, projectId);
                                    if (isFirstShow) {
                                        recyclerView.addItemDecoration(new DividerItemDecoration(MyLastFollowingRecordActivity.this, DividerItemDecoration.VERTICAL));
                                    }
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setAdapter(adapter);
                                    if (refresh.isRefreshing()) {
                                        refresh.setRefreshing(false);
                                    }
                                } else {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (materialTraceList != null && materialTraceList.size() > 0 && adapter != null) {

                                        // adapter.notifyDataSetChanged();
                                        if (refresh.isRefreshing()) {
                                            refresh.setRefreshing(false);
                                        }
                                        adapter.updateData(materialTraceList);
                                        //adapter = new MyLastFollowingRecordAdapter(materialTraceList, projectId);
                                    } else if (materialTraceList != null && materialTraceList.size() > 0 && adapter == null) {
                                        adapter = new MyLastFollowingRecordAdapter(materialTraceList, projectId);
                                        if (isFirstShow) {
                                            recyclerView.addItemDecoration(new DividerItemDecoration(MyLastFollowingRecordActivity.this, DividerItemDecoration.VERTICAL));
                                        }
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setAdapter(adapter);
                                        if (refresh.isRefreshing()) {
                                            refresh.setRefreshing(false);
                                        }
                                        adapter.setHasStableIds(true);
                                    }
                                }
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (refresh.isRefreshing()) {
                        refresh.setRefreshing(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (refresh.isRefreshing()) {
                        refresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    /**
     * 刷新列表
     */
    private void refreshFile(final Boolean isPull) {
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
                        initData(1, false);
                    }
                });
            }
        }).start();
    }
}
