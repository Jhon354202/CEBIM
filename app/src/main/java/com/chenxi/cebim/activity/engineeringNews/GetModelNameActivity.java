package com.chenxi.cebim.activity.engineeringNews;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.ChooseModeAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.ChooseModelEntity;
import com.chenxi.cebim.utils.DelUnderLine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class GetModelNameActivity extends BaseActivity {

//    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;
    private LinearLayout llSearch;
    private SearchView search;
    private ImageView back;
    private TextView sure;

    private ChooseModeAdapter adapter;

    private List<ChooseModelEntity> chooseModelList = new ArrayList<>();//获取回调接口中返回的

    private ArrayList<ChooseModelEntity> searchFileList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<ChooseModelEntity> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    private int projectID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_model_name);

        projectID = SPUtils.getInstance().getInt("projectID");

        initData();//数据初始化，模拟数据
        initView();
    }

    private void initData() {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/Model")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                tempList.clear();
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int ModelID = jsonObject.getInt("ModelID");
                        String ModelName = jsonObject.getString("ModelName");
                        ChooseModelEntity chooseModelEntity = new ChooseModelEntity();
                        chooseModelEntity.setIschoosed(false);
                        chooseModelEntity.setModelID(ModelID);
                        chooseModelEntity.setModelName(ModelName);

                        tempList.add(chooseModelEntity);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            chooseModelList.addAll(tempList);

                            if (chooseModelList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                            } else {

                                adapter = new ChooseModeAdapter(GetModelNameActivity.this, chooseModelList);
                                recyclerView.addItemDecoration(new DividerItemDecoration(GetModelNameActivity.this, DividerItemDecoration.VERTICAL));
                                recyclerView.setAdapter(adapter);
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

    private void initView() {

        recyclerView = findViewById(R.id.choose_model_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(GetModelNameActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listIsnull = findViewById(R.id.rl_choose_model_list_is_null);

        search = findViewById(R.id.sv_choose_model);
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

        llSearch = findViewById(R.id.ll_choose_model_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });

        sure = findViewById(R.id.toolbar_right_tv);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回模型信息给ProjectPublishActivity
                Intent intent = new Intent();
                intent.putExtra("model_info", (Serializable)adapter.getChooseModelList());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    //搜索
    private void doSearch(String keyWord) {

        if (adapter == null) return;
        searchFileList.clear();
        chooseModelList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getModelName().contains(keyWord)) {
                searchFileList.add(tempList.get(i));
            }
        }
        chooseModelList.addAll(searchFileList);
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

                    }
                });
            }
        }).start();
    }
}
