package com.chenxi.cebim.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.model.ModelListActivity;
import com.chenxi.cebim.adapter.AllModelAdapter;
import com.chenxi.cebim.entity.ModelEntity;
import com.chenxi.cebim.entity.RecentlyModelListClearEvent;
import com.chenxi.cebim.entity.RecentlyModelListFreshenEvent;
import com.chenxi.cebim.utils.ACache;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ModelListRecentlyFragment extends BaseFragment {
    View view;

    private int projectId;//用于存放从ModelListActivity中传过来的项目ID

    private ArrayList<ModelEntity> recentlyModelList = new ArrayList<>();//用于存放从缓存中获取的ModelList对象列表
    private SwipeRefreshLayout swipeRefresh;
    private AllModelAdapter adapter;
    private RecyclerView recyclerView;
    private RelativeLayout noRecentOpen;
    private ACache mCache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recently, container, false);
        EventBus.getDefault().register(this);//注册EvenBus
        mCache = ACache.get(getActivity());
        //从ModelListActivity获取projectId
        ModelListActivity ma = (ModelListActivity) getActivity();
        projectId = ma.getProjectId();

        initView();
        initData(0);
        return view;
    }

    private void initData(int isRefresh) {

        ArrayList<ModelEntity> tempList=(ArrayList<ModelEntity>) mCache.getAsObject("最近打开模型" + projectId);

        if(recentlyModelList!=null){
            recentlyModelList.clear();
        }

        if (recentlyModelList != null) {
            swipeRefresh.setVisibility(View.VISIBLE);
            noRecentOpen.setVisibility(View.GONE);
            if (isRefresh == 0) {
                if(tempList!=null&&tempList.size()>0){
                    swipeRefresh.setVisibility(View.VISIBLE);
                    noRecentOpen.setVisibility(View.GONE);
                    recentlyModelList.addAll(tempList);
                    adapter = new AllModelAdapter(recentlyModelList, projectId);
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    recyclerView.setAdapter(adapter);
                }else{
                    swipeRefresh.setVisibility(View.GONE);
                    noRecentOpen.setVisibility(View.VISIBLE);
                }

            } else if (isRefresh == 1) {
                recentlyModelList.addAll(tempList);
                //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                if (recentlyModelList != null && recentlyModelList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                } else if (recentlyModelList != null && recentlyModelList.size() > 0 && adapter == null) {
                    adapter = new AllModelAdapter(recentlyModelList, projectId);
                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                    recyclerView.setAdapter(adapter);
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }


                adapter = new AllModelAdapter(recentlyModelList, projectId);
                recyclerView.setAdapter(adapter);
                swipeRefresh.setRefreshing(false);
            }

        } else {
            swipeRefresh.setVisibility(View.GONE);
            noRecentOpen.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.recently_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.recently_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecently();
            }
        });

        noRecentOpen=view.findViewById(R.id.rl_model_no_recent_open);
    }

    public void refreshRecently() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initData(1);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //在点击标题栏右侧的清除后，利用evenbus来刷新列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(RecentlyModelListClearEvent messageEvent) {
        if(messageEvent.getMessage().contains("清除最近列表")){
            swipeRefresh.setVisibility(View.GONE);
            noRecentOpen.setVisibility(View.VISIBLE);
        }else {
            initData(1);
        }

    }

    //在从全部模型界面切换到最近界面时，用于刷新最近界面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void AllModelListEvent(RecentlyModelListFreshenEvent messageEvent) {
        initData(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
