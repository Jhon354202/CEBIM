package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.TaskAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.SynergyTaskEntity;
import com.chenxi.cebim.utils.DelUnderLine;
import com.chenxi.cebim.view.TaskDividerItemDecoration;

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

public class TaskScreenActivity extends BaseActivity {

    private int position;//QuestionFragment子fragment的position
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;
    private LinearLayout llSearch;
    private SearchView search;
    private ImageView back;

    private TaskAdapter adapter;

    private List<SynergyTaskEntity> screenTaskList = new ArrayList<>();//获取回调接口中返回的

    private ArrayList<SynergyTaskEntity> searchFileList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<SynergyTaskEntity> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<SynergyTaskEntity>

    String userName;
    String mFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_screen);

        userName = SPUtils.getInstance().getString("UserName");

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        mFrom=intent.getStringExtra("from");
        initView();

        swipeRefresh.setRefreshing(true);
        if (position == 0) {
            //下载已接收列表
            downloadReceived(false);
        } else if (position == 1) {
            //下载已发布列表
            downloadPublished(false);
        } else if (position == 2) {
            //下载全部列表
            downloadAll(false);
        }
    }

    //已接收列表展示
    private void downloadReceived(boolean isPull) {
        if (screenTaskList != null) {
            screenTaskList.clear();
        }

        if (tempList != null) {
            tempList.clear();
        }
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            String ID = jsonObject.getString("ID");
                            String UserIds = jsonObject.getString("UserIds");
                            String RelativeUserIds = jsonObject.getString("RelativeUserIds");
                            String ActualStartDate = jsonObject.getString("ActualStartDate");
                            String ActualFinishDate = jsonObject.getString("ActualFinishDate");
                            String Remark = jsonObject.getString("Remark");
                            String MaterialStartAt = jsonObject.getString("MaterialStartAt");
                            String MaterialFinishAt = jsonObject.getString("MaterialFinishAt");
                            String ActualUnit = jsonObject.getString("ActualUnit");
                            String ModelUnit = jsonObject.getString("ModelUnit");
                            String DocumentIds = jsonObject.getString("DocumentIds");
                            String Name = jsonObject.getString("Name");
                            String StartDate = jsonObject.getString("StartDate");
                            String FinishDate = jsonObject.getString("FinishDate");
                            String NewDuration = jsonObject.getString("NewDuration");
                            String DurationText = jsonObject.getString("DurationText");
                            String StartText = jsonObject.getString("StartText");
                            String FinishText = jsonObject.getString("FinishText");
                            String Notes = jsonObject.getString("Notes");

                            int PlanId = -1;
                            if (!jsonObject.get("PlanId").toString().equals("null")) {
                                PlanId = jsonObject.getInt("PlanId");
                            }

                            int ProjectId = jsonObject.getInt("ProjectId");

                            int ActualDuration = jsonObject.getInt("ActualDuration");

                            int PlanLabor = -1;
                            if (!jsonObject.get("PlanLabor").toString().equals("null")) {
                                PlanLabor = jsonObject.getInt("PlanLabor");
                            }

                            int PracticalLaborSum = -1;
                            if (!jsonObject.get("PracticalLaborSum").toString().equals("null")) {
                                PracticalLaborSum = jsonObject.getInt("PracticalLaborSum");
                            }

                            int Priority = -1;
                            if (!jsonObject.get("Priority").toString().equals("null")) {
                                Priority = jsonObject.getInt("Priority");
                            }

                            int Serial = -1;
                            if (!jsonObject.get("Serial").toString().equals("null")) {
                                Serial = jsonObject.getInt("Serial");
                            }

                            int Duration = jsonObject.getInt("Duration");

                            int CreatedBy = -1;
                            if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                                CreatedBy = jsonObject.getInt("CreatedBy");
                            }

                            int UpdatedBy = -1;
                            if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            Object AssignedAt = "";
                            if (!jsonObject.get("AssignedAt").toString().equals("null")) {
                                AssignedAt = jsonObject.get("AssignedAt");
                            }

                            Object DistributedAt = "";
                            if (!jsonObject.get("DistributedAt").toString().equals("null")) {
                                DistributedAt = jsonObject.get("DistributedAt");
                            }
                            Object CreatedAt = "";
                            if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                                CreatedAt = jsonObject.get("CreatedAt");
                            }

                            Object UpdatedAt = "";
                            if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                                UpdatedAt = jsonObject.get("UpdatedAt");
                            }

                            if ((UserIds != null && UserIds.contains(userName)) ||
                                    (RelativeUserIds != null && RelativeUserIds.contains(userName))) {
                                SynergyTaskEntity synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
                                        RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
                                        MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
                                        NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
                                        ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
                                        Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);

                                //数据源
                                tempList.add(synergyTaskEntity);
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenTaskList.addAll(tempList);
                                if (screenTaskList == null || screenTaskList.size() == 0) {
                                    listIsnull.setVisibility(View.VISIBLE);
                                    swipeRefresh.setVisibility(View.GONE);
                                } else {
                                    listIsnull.setVisibility(View.GONE);
                                    swipeRefresh.setVisibility(View.VISIBLE);


                                    if (!isPull) {
                                        adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskReceivedFragment");
                                        recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else {
                                        //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                        if (screenTaskList != null && screenTaskList.size() > 0 && adapter != null) {
                                            adapter.notifyDataSetChanged();
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        } else if (screenTaskList != null && screenTaskList.size() > 0 && adapter == null) {
                                            adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskReceivedFragment");
                                            recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                            recyclerView.setAdapter(adapter);
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        }
                                    }
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    //已发布
    private void downloadPublished(boolean isPull) {
        if (screenTaskList != null) {
            screenTaskList.clear();
        }

        if (tempList != null) {
            tempList.clear();
        }
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            String ID = jsonObject.getString("ID");
                            String UserIds = jsonObject.getString("UserIds");
                            String RelativeUserIds = jsonObject.getString("RelativeUserIds");
                            String ActualStartDate = jsonObject.getString("ActualStartDate");
                            String ActualFinishDate = jsonObject.getString("ActualFinishDate");
                            String Remark = jsonObject.getString("Remark");
                            String MaterialStartAt = jsonObject.getString("MaterialStartAt");
                            String MaterialFinishAt = jsonObject.getString("MaterialFinishAt");
                            String ActualUnit = jsonObject.getString("ActualUnit");
                            String ModelUnit = jsonObject.getString("ModelUnit");
                            String DocumentIds = jsonObject.getString("DocumentIds");
                            String Name = jsonObject.getString("Name");
                            String StartDate = jsonObject.getString("StartDate");
                            String FinishDate = jsonObject.getString("FinishDate");
                            String NewDuration = jsonObject.getString("NewDuration");
                            String DurationText = jsonObject.getString("DurationText");
                            String StartText = jsonObject.getString("StartText");
                            String FinishText = jsonObject.getString("FinishText");
                            String Notes = jsonObject.getString("Notes");

                            int PlanId = -1;
                            if (!jsonObject.get("PlanId").toString().equals("null")) {
                                PlanId = jsonObject.getInt("PlanId");
                            }

                            int ProjectId = jsonObject.getInt("ProjectId");

                            int ActualDuration = jsonObject.getInt("ActualDuration");

                            int PlanLabor = -1;
                            if (!jsonObject.get("PlanLabor").toString().equals("null")) {
                                PlanLabor = jsonObject.getInt("PlanLabor");
                            }

                            int PracticalLaborSum = -1;
                            if (!jsonObject.get("PracticalLaborSum").toString().equals("null")) {
                                PracticalLaborSum = jsonObject.getInt("PracticalLaborSum");
                            }

                            int Priority = -1;
                            if (!jsonObject.get("Priority").toString().equals("null")) {
                                Priority = jsonObject.getInt("Priority");
                            }

                            int Serial = -1;
                            if (!jsonObject.get("Serial").toString().equals("null")) {
                                Serial = jsonObject.getInt("Serial");
                            }

                            int Duration = jsonObject.getInt("Duration");

                            int CreatedBy = -1;
                            if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                                CreatedBy = jsonObject.getInt("CreatedBy");
                            }

                            int UpdatedBy = -1;
                            if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            Object AssignedAt = "";
                            if (!jsonObject.get("AssignedAt").toString().equals("null")) {
                                AssignedAt = jsonObject.get("AssignedAt");
                            }

                            Object DistributedAt = "";
                            if (!jsonObject.get("DistributedAt").toString().equals("null")) {
                                DistributedAt = jsonObject.get("DistributedAt");
                            }
                            Object CreatedAt = "";
                            if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                                CreatedAt = jsonObject.get("CreatedAt");
                            }

                            Object UpdatedAt = "";
                            if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                                UpdatedAt = jsonObject.get("UpdatedAt");
                            }

                            if (UserIds != null && UserIds.contains(userName)) {
                                SynergyTaskEntity synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
                                        RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
                                        MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
                                        NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
                                        ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
                                        Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);

                                //数据源
                                tempList.add(synergyTaskEntity);
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenTaskList.addAll(tempList);
                                if (screenTaskList == null || screenTaskList.size() == 0) {
                                    listIsnull.setVisibility(View.VISIBLE);
                                    swipeRefresh.setVisibility(View.GONE);
                                } else {
                                    listIsnull.setVisibility(View.GONE);
                                    swipeRefresh.setVisibility(View.VISIBLE);


                                    if (!isPull) {
                                        adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskScreenActivity");
                                        recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else {
                                        //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                        if (screenTaskList != null && screenTaskList.size() > 0 && adapter != null) {
                                            adapter.notifyDataSetChanged();
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        } else if (screenTaskList != null && screenTaskList.size() > 0 && adapter == null) {
                                            adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskScreenActivity");
                                            recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                            recyclerView.setAdapter(adapter);
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        }
                                    }
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    //全部
    private void downloadAll(boolean isPull) {
        if (screenTaskList != null) {
            screenTaskList.clear();
        }

        if (tempList != null) {
            tempList.clear();
        }
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            String ID = jsonObject.getString("ID");
                            String UserIds = jsonObject.getString("UserIds");
                            String RelativeUserIds = jsonObject.getString("RelativeUserIds");
                            String ActualStartDate = jsonObject.getString("ActualStartDate");
                            String ActualFinishDate = jsonObject.getString("ActualFinishDate");
                            String Remark = jsonObject.getString("Remark");
                            String MaterialStartAt = jsonObject.getString("MaterialStartAt");
                            String MaterialFinishAt = jsonObject.getString("MaterialFinishAt");
                            String ActualUnit = jsonObject.getString("ActualUnit");
                            String ModelUnit = jsonObject.getString("ModelUnit");
                            String DocumentIds = jsonObject.getString("DocumentIds");
                            String Name = jsonObject.getString("Name");
                            String StartDate = jsonObject.getString("StartDate");
                            String FinishDate = jsonObject.getString("FinishDate");
                            String NewDuration = jsonObject.getString("NewDuration");
                            String DurationText = jsonObject.getString("DurationText");
                            String StartText = jsonObject.getString("StartText");
                            String FinishText = jsonObject.getString("FinishText");
                            String Notes = jsonObject.getString("Notes");

                            int PlanId = -1;
                            if (!jsonObject.get("PlanId").toString().equals("null")) {
                                PlanId = jsonObject.getInt("PlanId");
                            }

                            int ProjectId = jsonObject.getInt("ProjectId");

                            int ActualDuration = jsonObject.getInt("ActualDuration");

                            int PlanLabor = -1;
                            if (!jsonObject.get("PlanLabor").toString().equals("null")) {
                                PlanLabor = jsonObject.getInt("PlanLabor");
                            }

                            int PracticalLaborSum = -1;
                            if (!jsonObject.get("PracticalLaborSum").toString().equals("null")) {
                                PracticalLaborSum = jsonObject.getInt("PracticalLaborSum");
                            }

                            int Priority = -1;
                            if (!jsonObject.get("Priority").toString().equals("null")) {
                                Priority = jsonObject.getInt("Priority");
                            }

                            int Serial = -1;
                            if (!jsonObject.get("Serial").toString().equals("null")) {
                                Serial = jsonObject.getInt("Serial");
                            }

                            int Duration = jsonObject.getInt("Duration");

                            int CreatedBy = -1;
                            if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                                CreatedBy = jsonObject.getInt("CreatedBy");
                            }

                            int UpdatedBy = -1;
                            if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            Object AssignedAt = "";
                            if (!jsonObject.get("AssignedAt").toString().equals("null")) {
                                AssignedAt = jsonObject.get("AssignedAt");
                            }

                            Object DistributedAt = "";
                            if (!jsonObject.get("DistributedAt").toString().equals("null")) {
                                DistributedAt = jsonObject.get("DistributedAt");
                            }
                            Object CreatedAt = "";
                            if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                                CreatedAt = jsonObject.get("CreatedAt");
                            }

                            Object UpdatedAt = "";
                            if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                                UpdatedAt = jsonObject.get("UpdatedAt");
                            }

                            SynergyTaskEntity synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
                                    RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
                                    MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
                                    NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
                                    ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
                                    Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);

                            //数据源
                            tempList.add(synergyTaskEntity);


                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenTaskList.addAll(tempList);
                                if (screenTaskList == null || screenTaskList.size() == 0) {
                                    listIsnull.setVisibility(View.VISIBLE);
                                    swipeRefresh.setVisibility(View.GONE);
                                } else {
                                    listIsnull.setVisibility(View.GONE);
                                    swipeRefresh.setVisibility(View.VISIBLE);

                                    if (!isPull) {
                                        adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskScreenActivity");
                                        recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else {
                                        //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                        if (screenTaskList != null && screenTaskList.size() > 0 && adapter != null) {
                                            adapter.notifyDataSetChanged();
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        } else if (screenTaskList != null && screenTaskList.size() > 0 && adapter == null) {
                                            adapter = new TaskAdapter(TaskScreenActivity.this, screenTaskList,"TaskScreenActivity");
                                            recyclerView.addItemDecoration(new TaskDividerItemDecoration(MyApplication.getContext()));
                                            recyclerView.setAdapter(adapter);
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        }
                                    }
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = findViewById(R.id.recyclerview_task_screen);
        LinearLayoutManager layoutManager = new LinearLayoutManager(TaskScreenActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = findViewById(R.id.swip_refresh_task_screen);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true, position);
            }
        });

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listIsnull = findViewById(R.id.rl_task_screen_list_is_null);

        search = findViewById(R.id.sv_task_screen);
        search.setVisibility(View.VISIBLE);//搜索框初始化时默认显示
        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) search.findViewById(R.id.search_src_text);
        searchAutoComplete.setTextSize(16);

        //去掉下划线
        DelUnderLine.delUnderLine(search);

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

        llSearch = findViewById(R.id.ll_task_search_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });
    }

    //搜索
    private void doSearch(String keyWord) {

        if (adapter == null) return;
        searchFileList.clear();
        screenTaskList.clear();

        for (int i = 0; i < tempList.size(); i++) {

            if (tempList.get(i).getName().contains(keyWord) ||
                    tempList.get(i).getUserIds().contains(keyWord) ||
                    tempList.get(i).getRelativeUserIds().contains(keyWord) ||
                    tempList.get(i).getCreatedAt().toString().contains(keyWord)) {
                searchFileList.add(tempList.get(i));
            }


        }
        screenTaskList.addAll(searchFileList);
        adapter.notifyDataSetChanged();

    }

    /**
     * @param isPull
     * @param listType 值为0、1、2与position对应，分别表示刷新相对应的列表
     */
    private void refreshFile(final boolean isPull, int listType) {
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
                        if (listType == 0) {
                            //下载未完成列表
                            downloadReceived(false);
                        } else if (listType == 1) {
                            //下载已延期列表
//                            downloadDelay(true);
                        } else if (listType == 2) {
                            //下载已完成列表
//                            downloadComplete(true);
                        }
                    }
                });
            }
        }).start();
    }
}
