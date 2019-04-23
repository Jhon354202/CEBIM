package com.chenxi.cebim.fragment.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.DataFileAdapter;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.fragment.BaseFragment;
import com.chenxi.cebim.utils.ACache;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RecentOpenFragment extends BaseFragment {
    private View view;
    private int projectId;//用于存放从ModelListActivity中传过来的项目ID

    private ArrayList<TbFileShowmodel> recentDataFileList = new ArrayList<>();//用于存放从缓存中获取的DataFile对象列表
    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//用于显示的对象列表
    private SwipeRefreshLayout swipeRefresh;
    private DataFileAdapter adapter;
    private RecyclerView recyclerView;
    private TextView clear;
    private RelativeLayout noRecentOpen;
    private LinearLayout recentOpen;

    ACache mCache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent_open, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        //获取ProjectId
        if(SPUtils.getInstance().getInt("projectID")==-1){
            ToastUtils.showShort("项目ID获取失败");
        }else{
            projectId = SPUtils.getInstance().getInt("projectID");
        }

        initView();
//        initData(0);
        getData(0);

        return view;
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.recent_open_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        noRecentOpen=(RelativeLayout) view.findViewById(R.id.rl_no_recent_open);
        recentOpen=view.findViewById(R.id.rl_recent_open);

        clear = (TextView) view.findViewById(R.id.tv_clear_recent_file);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCache.remove(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                        .getInt("projectID") + ":recentopent");
                //把打开的文件存入缓存,key："userID:projectID"、value:打开的对象
                recentOpen.setVisibility(View.GONE);
                noRecentOpen.setVisibility(View.VISIBLE);
            }
        });

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.recent_open_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecently(true);
            }
        });
    }

    private void getData(int isRefresh) {
        mCache = ACache.get(getActivity());
        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                + SPUtils.getInstance().getInt("projectID") + ":recentopent");

        if (tbFileShowmodelString != null&&!tbFileShowmodelString.equals("null")) {
            recentOpen.setVisibility(View.VISIBLE);
            noRecentOpen.setVisibility(View.GONE);
            recentDataFileList.clear();
            showList.clear();
            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
            for (int i = 0; i < tbFileShowmodelArr.length; i++) {
                try {
                    if(tbFileShowmodelArr[i]==null||tbFileShowmodelArr[i].equals("null")){
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject(tbFileShowmodelArr[i]);
                    int FID = jsonObject.getInt("fID");
                    int ProjectID = jsonObject.getInt("projectID");

                    int ClassID = jsonObject.getInt("classID");

                    int ParentClassID = jsonObject.getInt("parentClassID");

                    int OperationUserID = jsonObject.getInt("operationUserID");

                    String FileName = jsonObject.getString("fileName");
                    String FileType = jsonObject.getString("fileType");
                    String FileID=jsonObject.getString("fileID");

                    Object AddTime = jsonObject.get("addTime");
                    Object UpdateTime = jsonObject.get("updateTime");

                    Boolean IsCheck = false;
                    Boolean IsMove = false;

                    TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                            FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);
                    recentDataFileList.add(tbFileShowmodel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            showList.addAll(recentDataFileList);

            if (isRefresh == 0) {
                if(showList==null) return;
                adapter = new DataFileAdapter(getActivity(), showList, projectId);
                recyclerView.setAdapter(adapter);
                if(swipeRefresh!=null&&swipeRefresh.isRefreshing()){
                    swipeRefresh.setRefreshing(false);
                }

            } else if (isRefresh == 1) {
                //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                if (showList != null && showList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                    if(swipeRefresh!=null&&swipeRefresh.isRefreshing()){
                        swipeRefresh.setRefreshing(false);
                    }
                } else if (showList != null && showList.size() > 0 && adapter == null) {
                    adapter = new DataFileAdapter(getActivity(), showList, projectId);
                    recyclerView.setAdapter(adapter);
                    if(swipeRefresh!=null&&swipeRefresh.isRefreshing()){
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        } else {
            recentOpen.setVisibility(View.GONE);
            noRecentOpen.setVisibility(View.VISIBLE);
        }

    }

    public void refreshRecently(final Boolean isPull) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(isPull){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData(1);
                    }
                });
            }
        }).start();
    }

    //在点击标题栏右侧的清除后，利用evenbus来刷新列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowBottomSettingButton isShowBottomSettingButton) {
        if(isShowBottomSettingButton.getInfo().equals("刷新最近打开列表")){
            refreshRecently(false);
        }else if(isShowBottomSettingButton.getInfo().contains("常用列表删除成功刷新RecentOpenFragment列表")){
            refreshRecently(false);
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
