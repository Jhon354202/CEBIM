package com.chenxi.cebim.activity.material;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.StatisticsSearchAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.Modelstatistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MaterialTrackingSubmodeStatisticsSearchActivity extends AppCompatActivity {

    @BindView(R.id.sv_project)
    SearchView searchView;
    @BindView(R.id.ll_project_search_view)
    LinearLayout llProjectSearchView;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private int projectId;
    private StatisticsSearchAdapter adapter;
    private List<Modelstatistics> modelstatisticsList = new ArrayList<>();
    private List<Modelstatistics> searchProjectList = new ArrayList<>();//获取的所有搜索后的对象集合
    private List<Modelstatistics> temporyProjectList = new ArrayList<>();//用于类表中显示的对相集合
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_tracking_submode_statistics_search);
        ButterKnife.bind(this);
        llProjectSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        getData();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                doSearch(s);
                return true;
            }
        });
    }

    private void doSearch(String s) {
        if (adapter == null) return;
        searchProjectList.clear();
        temporyProjectList.clear();

        for (int i = 0; i < modelstatisticsList.size(); i++) {
            if (modelstatisticsList.get(i).getModelName().contains(s)) {
                searchProjectList.add(modelstatisticsList.get(i));
            }
        }
        temporyProjectList.addAll(searchProjectList);
        adapter.notifyDataSetChanged();
    }

    private void getData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/Model")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String resultJson = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            modelstatisticsList = JSON.parseArray(resultJson, Modelstatistics.class);
                            temporyProjectList.clear();
                            temporyProjectList.addAll(modelstatisticsList);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(MaterialTrackingSubmodeStatisticsSearchActivity.this);
                            recycler.setLayoutManager(layoutManager);
                            adapter = new StatisticsSearchAdapter(MaterialTrackingSubmodeStatisticsSearchActivity.this, temporyProjectList);
                            recycler.setAdapter(adapter);
                        }
                    });
                } else {
                    ToastUtils.showShort("数据请求出错！");
                }
            }
        });
    }
}
