package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.TaskDataFileAdapter;
import com.chenxi.cebim.entity.RoleInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskDataActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ImageView back;

    private List<RoleInfo> taskDataList=new ArrayList<>();
    private List<RoleInfo> tempList=new ArrayList<>();

    private TaskDataFileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_data);

        initView();
        swipeRefresh.setRefreshing(true);
        initData(false);
    }

    private void initView() {
        swipeRefresh=findViewById(R.id.task_data_swip_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        recyclerView=findViewById(R.id.task_data_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        back=findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
                        initData(isPull);
                    }
                });
            }
        }).start();
    }

    private void initData(boolean isPull) {
        if(taskDataList!=null){
            taskDataList.clear();
        }

        if(tempList!=null){
            tempList.clear();
        }

        Intent intent=getIntent();
        String DocumentIds=intent.getStringExtra("DocumentIds");

        JSONArray array = null;
        try {
            array = new JSONArray(DocumentIds);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length(); i++) {
                RoleInfo roleInfo=new RoleInfo();
                JSONObject jsonObject = array.getJSONObject(i);
                roleInfo.setID(jsonObject.get("ID").toString());
                roleInfo.setName(jsonObject.get("Name").toString());
                tempList.add(roleInfo);
            }

            taskDataList.addAll(tempList);

            if(isPull){
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }else{
                    adapter = new TaskDataFileAdapter(this,taskDataList);
                    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
                    recyclerView.setAdapter(adapter);
                }
            }else{
                adapter = new TaskDataFileAdapter(this,taskDataList);
                recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
                recyclerView.setAdapter(adapter);
            }

            if(swipeRefresh.isRefreshing()){
                swipeRefresh.setRefreshing(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            if(swipeRefresh.isRefreshing()){
                swipeRefresh.setRefreshing(false);
            }
        }
    }
}
