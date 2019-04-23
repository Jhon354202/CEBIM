package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.ConstructionDeailsTrackAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.TbMaterialTraceTemplateStates;
import com.chenxi.cebim.entity.Templatestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class ConstructionDetailsTrackingActivity extends BaseActivity {
    private RelativeLayout back;
    private int mPeojectID;
    private SwipeRefreshLayout refresh;
    private RecyclerView recyclerView;
    private List<Templatestate> list = new ArrayList<Templatestate>();
    private List<TbMaterialTraceTemplateStates> tbMaterialTraceTemplateStates = new ArrayList<>();
    private ConstructionDeailsTrackAdapter constructionDeailsTrackAdapter;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_construction_details_tracking);
        mPeojectID = SPUtils.getInstance().getInt("projectID", -1);
        Intent intent = getIntent();
        id = intent.getStringExtra("ID");
        initView();
        getData();

    }


    private void initView() {
        back = findViewById(R.id.rl_edit_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        refresh = findViewById(R.id.construction_detail_track_refresh);
        recyclerView = findViewById(R.id.construction_detail_track_recyclerview);


    }


    private void getData() {
        String url = AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/MaterialTrace/TemplateState?where=ID=GUID(\"" + id + "\")";
        Request request = new Request.Builder()
                .url(url)
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String responseData = response.body().string();
                    List<Templatestate> templatestates = JSON.parseArray(responseData, Templatestate.class);
                    tbMaterialTraceTemplateStates = templatestates.get(0).getTbMaterialTraceTemplateStates();
                    constructionDeailsTrackAdapter = new ConstructionDeailsTrackAdapter(tbMaterialTraceTemplateStates);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ConstructionDetailsTrackingActivity.this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(constructionDeailsTrackAdapter);
                        }
                    });

                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }
}
