package com.chenxi.cebim.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.ProjectAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.Project;
import com.chenxi.cebim.utils.DelUnderLine;

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

public class ProjectFragment extends BaseFragment {

    View view;

    private List<Project> projectList = new ArrayList<>();//获取的所有project对象集合
    private List<Project> searchProjectList = new ArrayList<>();//获取的所有搜索后的对象集合
    private List<Project> temporyProjectList = new ArrayList<>();//用于类表中显示的对相集合
    private SwipeRefreshLayout swipeRefresh;
    private ProjectAdapter adapter;
    private RecyclerView recyclerView;

    private SearchView mSearchView;
    private LinearLayout llSearch;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_project, container, false);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.project_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        //加载加进度条，请求成功后消失。
        swipeRefresh.measure(0, 0);
        swipeRefresh.setRefreshing(true);
        initProject(0);//初始化项目数据

        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.project_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshProject();
            }
        });

        mSearchView = (SearchView) view.findViewById(R.id.sv_project);

        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setTextSize(16);

        //去掉下划线
        DelUnderLine.delUnderLine(mSearchView);

        llSearch = (LinearLayout) view.findViewById(R.id.ll_project_search_view);

        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.setIconified(false);//设置searchView处于展开状态
            }
        });
//        mSearchView.onActionViewExpanded();
        // 设置搜索文本监听
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        return view;
    }

    //搜索
    private void doSearch(String keyWord) {

        if(adapter==null)return;
        searchProjectList.clear();
        temporyProjectList.clear();

        for (int i = 0; i < projectList.size(); i++) {
            if (projectList.get(i).getProjectName().contains(keyWord)) {
                searchProjectList.add(projectList.get(i));
            }
        }
        temporyProjectList.addAll(searchProjectList);
        adapter.notifyDataSetChanged();
    }

    //刷新列表
    private void refreshProject() {
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
                        initProject(1);
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //下载列表
    private void initProject(final int isRefresh) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/Project")
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
                projectList.clear();
                if(response.code()==200){
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String projectName = jsonObject.getString("ProjectName");
                            int projectID = jsonObject.getInt("ProjectID");
                            Project project = new Project(projectName, projectID);
                            projectList.add(project);
                        }
                        temporyProjectList.clear();
                        temporyProjectList.addAll(projectList);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh == 0) {
                                    adapter = new ProjectAdapter(temporyProjectList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据解析出错");
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
                }else{
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
