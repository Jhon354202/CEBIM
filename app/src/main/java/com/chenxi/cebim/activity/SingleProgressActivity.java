package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.CacheDoubleUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.ProgressAdapter;
import com.chenxi.cebim.entity.ProgressModel;

import java.util.ArrayList;

public class SingleProgressActivity extends BaseActivity {

    private String processName;
    private int ePPID,projectId;
    private ArrayList<ProgressModel> allList = new ArrayList<>();
    private ArrayList<ProgressModel> childrenList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private com.chenxi.cebim.view.ClearEditText search;
    private RelativeLayout back;
    private TextView showProgressName;

    private ProgressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_progress);

        //获取从ProgressAdapter传递的参数
        Intent intent=getIntent();
        processName=intent.getStringExtra("ProcessName");
        ePPID=intent.getIntExtra("EPPID",-1);
        projectId=intent.getIntExtra("projectId",-1);
        allList = (ArrayList<ProgressModel>) CacheDoubleUtils.getInstance().getSerializable("progress" + projectId);

        initView();
        getData(0, 0);
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) findViewById(R.id.single_progress_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SingleProgressActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.single_progress_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile();
            }
        });

        back = (RelativeLayout) findViewById(R.id.rl_single_progress_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        showProgressName=(TextView)findViewById(R.id.tv_single_progress_name);
        showProgressName.setText(processName);
    }

    //获取数据
    public void getData(final int isRefresh, final int isChoose) {//isRefresh是否为刷新，isChoose是否为选择模式，1为是，0为否
        //获取和ePPID相等的ParentEPPID项
        for(int i=0;i<allList.size();i++){
            if(allList.get(i).getParentEPPID()==ePPID){
                childrenList.add(allList.get(i));
            }
        }

        if(isRefresh==0){
            adapter = new ProgressAdapter(childrenList, projectId, isChoose, ePPID);
            recyclerView.addItemDecoration(new DividerItemDecoration(SingleProgressActivity.this, DividerItemDecoration.VERTICAL));
            recyclerView.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
//            adapter = new DataFileAdapter(childrenList, projectID, isChoose, m_id);
//            recyclerView.addItemDecoration(new DividerItemDecoration(DirActivity.this, DividerItemDecoration.VERTICAL));
//            recyclerView.setAdapter(adapter);
            swipeRefresh.setRefreshing(false);
        }


    }

    private void refreshFile() {
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
                        getData(1, 0);
                    }
                });
            }
        }).start();
    }


}
