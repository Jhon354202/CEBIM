package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.CacheDoubleUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.ProgressAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.ProgressModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ProgressActivity extends BaseActivity {

    private int projectId;
    private RelativeLayout back;
    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;
    private ArrayList<ProgressModel> progressDataList = new ArrayList<>();//远程获取的进度数据源
    private ArrayList<ProgressModel> rootProgressDataList = new ArrayList<>();//最顶层的进度数据，ParentEPPIDObject为-1的数据

    private ProgressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        //获取ProjectActivity传过来的数据
        Intent intent = getIntent();
        projectId = intent.getIntExtra("projectId", -1);

        initView();
        getProgressData(0, 0);
    }

    private void initView() {
        back = (RelativeLayout) findViewById(R.id.rl_progress_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //RecyclerView逻辑
        recyclerView = (RecyclerView) findViewById(R.id.rv_progress);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        refresh = (SwipeRefreshLayout) findViewById(R.id.progress_swip_refresh);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(1, 0);
            }
        });
    }

    //获取进度数据
    private void getProgressData(final int isRefresh, final int isChoose) {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/EngProjectProcess/" + projectId + "/GetList")
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
                    int n = jsonArray.length();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int EPPID = jsonObject.getInt("EPPID");

                        int ParentEPPID;
                        Object ParentEPPIDObject = jsonObject.get("ParentEPPID");
                        if (!ParentEPPIDObject.toString().equals("null")) {
                            ParentEPPID = jsonObject.getInt("ParentEPPID");
                        } else {
                            Random rand = new Random();
                            ParentEPPID = -1; //生成0-100以内的随机数
                        }

                        int ProjectID = jsonObject.getInt("ProjectID");

                        int PlanTimeLimit;
                        Object PlanTimeLimitObject = jsonObject.get("PlanTimeLimit");
                        if (!PlanTimeLimitObject.toString().equals("null")) {
                            PlanTimeLimit = jsonObject.getInt("PlanTimeLimit");
                        } else {
                            Random rand = new Random();
                            PlanTimeLimit = 100000 * rand.nextInt(100); //生成0-100以内的随机数
                        }

                        int Sort = jsonObject.getInt("Sort");
                        int GrowthType = jsonObject.getInt("GrowthType");
                        int OperationUserID;
                        Object OperationUserIDObject = jsonObject.get("OperationUserID");
                        if (!OperationUserIDObject.toString().equals("null")) {
                            OperationUserID = jsonObject.getInt("OperationUserID");
                        } else {
                            Random rand = new Random();
                            OperationUserID = 100000 * rand.nextInt(100); //生成0-100以内的随机数
                        }

                        String SerialNumber = jsonObject.getString("SerialNumber");
                        String ProcessName = jsonObject.getString("ProcessName");
                        String OnlySign = jsonObject.getString("OnlySign");

                        Boolean IsTaskGroup = jsonObject.getBoolean("IsTaskGroup");
                        Boolean IsShow = jsonObject.getBoolean("IsShow");

                        Object PlanBeginTime = jsonObject.get("PlanBeginTime");
                        Object PlanEndTime = jsonObject.get("PlanEndTime");
                        Object ActualBeginTime = jsonObject.get("ActualBeginTime");
                        Object ActualEndTime = jsonObject.get("ActualEndTime");
                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");
                        Object Progress = jsonObject.get("Progress");

                        ProgressModel progressModel = new ProgressModel(EPPID, ParentEPPID, ProjectID, PlanTimeLimit,
                                Sort, GrowthType, OperationUserID, SerialNumber, ProcessName, OnlySign, IsTaskGroup, IsShow, PlanBeginTime,
                                PlanEndTime, ActualBeginTime, ActualEndTime, AddTime, UpdateTime, Progress);
                        progressDataList.add(progressModel);

                        if (ParentEPPID == -1) {
                            rootProgressDataList.add(progressModel);
                        }
                    }

                    CacheDoubleUtils.getInstance().put("progress" + projectId, progressDataList);//缓存progressDataList

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDataList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new ProgressAdapter(rootProgressDataList, projectId, isChoose, -1);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(ProgressActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshFile(int isRefresh, int isChoose) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getProgressData(1, 0);
//                        adapter = new ProgressAdapter(progressDataList, projectId, 0, -1);
                        adapter.notifyDataSetChanged();
//                        recyclerView.setAdapter(adapter);
                        refresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }
}
