package com.chenxi.cebim.activity.model;

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

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.AllModelAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.ModelEntity;
import com.chenxi.cebim.utils.DelUnderLine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ScreecModelActivity extends BaseActivity {


    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;
    private LinearLayout llSearch;
    private SearchView search;
    private ImageView back;

    private AllModelAdapter adapter;

    private ArrayList<ModelEntity> screenModelList = new ArrayList<>();//获取回调接口中返回的

    private ArrayList<ModelEntity> searchModleList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<ModelEntity> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screec_model);

        initView();
        swipeRefresh.setRefreshing(true);
        initData(0);
    }


    private void initView() {
        //RecyclerView逻辑
        recyclerView = findViewById(R.id.screen_model_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ScreecModelActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = findViewById(R.id.screen_model_swip_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listIsnull = findViewById(R.id.rl_screen_model_list_is_null);

        search = findViewById(R.id.sv_screen_model);
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

        llSearch = findViewById(R.id.ll_model_search_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });
    }

    //获取远程数据
    private void initData(final int isRefresh) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/Model")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                screenModelList.clear();
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int ModelID = jsonObject.getInt("ModelID");
                        int ProjectID = jsonObject.getInt("ProjectID");
                        int FileSize = jsonObject.getInt("FileSize");

                        int OrderNo = -1;
                        if (!jsonObject.get("OrderNo").toString().equals("null")) {
                            OrderNo = jsonObject.getInt("OrderNo");
                        }

                        String ModelName = jsonObject.getString("ModelName");
                        String DBName = jsonObject.getString("DBName");
                        String OnlySign = jsonObject.getString("OnlySign");
                        String AddTime = jsonObject.get("AddTime").toString();
                        String UpdateTime = jsonObject.get("UpdateTime").toString();
                        String ModelFile = jsonObject.getString("ModelFile");
                        String OperationUserID = jsonObject.getString("OperationUserID");
                        String FileType = jsonObject.getString("FileType");
                        String FileTypeInfo = jsonObject.getString("FileTypeInfo");

                        boolean IsChecked = false;
                        boolean IsCompleted = jsonObject.getBoolean("IsCompleted");

                        Byte[] FileContent = null;

                        ModelEntity modelEntity = new ModelEntity(ModelID, ProjectID, FileSize, OrderNo, ModelName, DBName,
                                OnlySign, AddTime, UpdateTime, ModelFile, OperationUserID
                                , FileType, FileTypeInfo, IsCompleted, IsChecked, FileContent);
                        tempList.add(modelEntity);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            screenModelList.addAll(tempList);
                            if (screenModelList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new AllModelAdapter(screenModelList, SPUtils.getInstance().getInt("projectID"));
                                    recyclerView.addItemDecoration(new DividerItemDecoration(ScreecModelActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
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
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }


    //搜索
    private void doSearch(String keyWord) {

        if (adapter == null) return;
        searchModleList.clear();
        screenModelList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getModelName().contains(keyWord)) {

                searchModleList.add(tempList.get(i));

            }

        }
        screenModelList.addAll(searchModleList);
        adapter.notifyDataSetChanged();

    }

    /**
     * @param isPull
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //下载未完成列表
                        initData(0);
                    }
                });
            }
        }).start();
    }
}
