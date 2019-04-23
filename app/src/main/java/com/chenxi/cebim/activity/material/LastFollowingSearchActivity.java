package com.chenxi.cebim.activity.material;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.MyLastFollowingRecordAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
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

//我的最近跟踪记录搜索
public class LastFollowingSearchActivity extends BaseActivity {

    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;
    private LinearLayout llSearch;
    private SearchView search;
    private ImageView back;

    private List<MaterialTraceModel> materialTraceList = new ArrayList<>();
    private List<MaterialTraceModel> searchProjectList = new ArrayList<>();//获取的所有搜索后的对象集合
    private List<MaterialTraceModel> temporyProjectList = new ArrayList<>();//用于类表中显示的对相集合

    private MyLastFollowingRecordAdapter adapter;

    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        setContentView(R.layout.activity_last_following_search);
        initView();
        initData(0, true);

    }

    //控件初始化
    private void initView() {

        llSearch = findViewById(R.id.ll_last_following_search_view);
        search = findViewById(R.id.sv_last_following);

        refresh = findViewById(R.id.last_following_swip_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });
        recyclerView = findViewById(R.id.last_following_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listIsnull = findViewById(R.id.rl_last_following_list_is_null);

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });

        // 设置搜索文本监听
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }

        });
    }

    private void doSearch(String newText) {

        if (adapter == null) return;
        searchProjectList.clear();
        temporyProjectList.clear();

        for (int i = 0; i < materialTraceList.size(); i++) {
            if (materialTraceList.get(i).getComponentName().contains(newText) ||
                    materialTraceList.get(i).getComponentID().contains(newText)) {
                searchProjectList.add(materialTraceList.get(i));
            }
        }
        temporyProjectList.addAll(searchProjectList);
        adapter.notifyDataSetChanged();

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
                ToastUtils.showShort("数据请求出错！");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);
                        materialTraceList = JSON.parseArray(responseData, MaterialTraceModel.class);
                        temporyProjectList.clear();
                        temporyProjectList.addAll(materialTraceList);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (temporyProjectList.size() == 0) {
                                    ToastUtils.showShort("请求无数据");
                                    if (refresh.isRefreshing()) {
                                        refresh.setRefreshing(false);
                                    }
                                } else {
                                    if (isRefresh == 0) {
                                        adapter = new MyLastFollowingRecordAdapter(temporyProjectList, projectId);
                                        if (isFirstShow) {
                                            recyclerView.addItemDecoration(new DividerItemDecoration(LastFollowingSearchActivity.this, DividerItemDecoration.VERTICAL));
                                        }
                                        recyclerView.setAdapter(adapter);
                                        if (refresh.isRefreshing()) {
                                            refresh.setRefreshing(false);
                                        }
                                    } else {
                                        //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                        if (materialTraceList != null && materialTraceList.size() > 0 && adapter != null) {
                                            adapter.notifyDataSetChanged();
                                            if (refresh.isRefreshing()) {
                                                refresh.setRefreshing(false);
                                            }
                                        } else if (materialTraceList != null && materialTraceList.size() > 0 && adapter == null) {
                                            adapter = new MyLastFollowingRecordAdapter(materialTraceList, projectId);
                                            if (isFirstShow) {
                                                recyclerView.addItemDecoration(new DividerItemDecoration(LastFollowingSearchActivity.this, DividerItemDecoration.VERTICAL));
                                            }
                                            recyclerView.setAdapter(adapter);
                                            if (refresh.isRefreshing()) {
                                                refresh.setRefreshing(false);
                                            }
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
                } else {
                    ToastUtils.showShort("数据请求出错！");
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
