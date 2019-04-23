package com.chenxi.cebim.fragment.coordination;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.TaskAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.SynergyTaskEntity;
import com.chenxi.cebim.fragment.BaseFragment;
import com.yanyusong.y_divideritemdecoration.Y_Divider;
import com.yanyusong.y_divideritemdecoration.Y_DividerBuilder;
import com.yanyusong.y_divideritemdecoration.Y_DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

public class TaskPublishedFragment extends BaseFragment {
    View view;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;

    private TaskAdapter adapter;
    private List<SynergyTaskEntity> publishedTaskList = new ArrayList<>();//获取回调接口中返回的

    private String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面
    private Activity mActivity;

    private String userName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_published, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        userName = SPUtils.getInstance().getString("UserName");
        initView();
        getData(false);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    //控件初始化
    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.published_task_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.published_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        listIsnull = view.findViewById(R.id.rl_published_list_is_null);
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

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData(isPull);
                    }
                });
            }
        }).start();
    }

    //获取任务列表数据
    private void getData(final boolean isPull) {

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
                    publishedTaskList.clear();
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

                            if (CreatedBy==SPUtils.getInstance().getInt("UserID",-1)) {
                                SynergyTaskEntity synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
                                        RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
                                        MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
                                        NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
                                        ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
                                        Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);

                                //数据源
                                publishedTaskList.add(synergyTaskEntity);
                            }

                        }

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (publishedTaskList == null || publishedTaskList.size() == 0) {
                                    listIsnull.setVisibility(View.VISIBLE);
                                    swipeRefresh.setVisibility(View.GONE);
                                } else {
                                    listIsnull.setVisibility(View.GONE);
                                    swipeRefresh.setVisibility(View.VISIBLE);

                                    if (!isPull) {
                                        adapter = new TaskAdapter(mActivity, publishedTaskList,"TaskPublishedFragment");
                                        recyclerView.addItemDecoration(new PublishedDividerItemDecoration(MyApplication.getContext()));

                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else {
                                        //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                        if (publishedTaskList != null && publishedTaskList.size() > 0 && adapter != null) {
                                            adapter.notifyDataSetChanged();
                                            if (swipeRefresh.isRefreshing()) {
                                                swipeRefresh.setRefreshing(false);
                                            }
                                        } else if (publishedTaskList != null && publishedTaskList.size() > 0 && adapter == null) {
                                            adapter = new TaskAdapter(mActivity, publishedTaskList,"TaskPublishedFragment");
                                            recyclerView.addItemDecoration(new PublishedDividerItemDecoration(MyApplication.getContext()));
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

    class PublishedDividerItemDecoration extends Y_DividerItemDecoration {

        public PublishedDividerItemDecoration(Context context) {
            super(context);
        }

        @Override
        public Y_Divider getDivider(int itemPosition) {

            Y_Divider divider = null;
            divider = new Y_DividerBuilder()
                    .setBottomSideLine(true, getResources().getColor(R.color.divice_line, null), 6, 0, 0)
                    .create();
            return divider;

        }
    }

    //EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {
        if (commonEven.getInfo().equals("任务详细修改刷新")) {//刷新
            refreshFile(false);
        }else if(commonEven.getInfo().equals("反馈记录刷新")){
            refreshFile(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
