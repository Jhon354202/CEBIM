package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.MaterialtrackingSettingsAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MaterialtrackingSettingsActivity extends BaseActivity {

    @BindView(R.id.material_setting_recyclerview)
    RecyclerView materialSettingRecyclerview;
    List<MaterialSettings> materialSettingsList = new ArrayList<>();
    private int projectId;
    MaterialtrackingSettingsAdapter adapter;
    String state;
    ImageView back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materialtracking_settings);
        ButterKnife.bind(this);
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        state = getIntent().getStringExtra("State");
        getData();
    }

    private void getData() {
        materialSettingsList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/State")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String responseData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayoutManager layoutManager = new LinearLayoutManager(MaterialtrackingSettingsActivity.this);
                            materialSettingRecyclerview.setLayoutManager(layoutManager);
                            materialSettingsList = JSON.parseArray(responseData, MaterialSettings.class);

                            if (materialSettingsList.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                            } else {
                                for (int i = 0; i < materialSettingsList.size(); i++) {
                                    if (state.equals("") || state.equals(null)) {
                                    } else {
                                        if (state.equals(materialSettingsList.get(i).getName().trim())) {
                                            materialSettingsList.get(i).setChoose(true);
                                        }
                                    }
                                }
                                adapter = new MaterialtrackingSettingsAdapter(materialSettingsList, MaterialtrackingSettingsActivity.this);
                                materialSettingRecyclerview.setAdapter(adapter);
                                adapter.setOnRecyclerItemClickListener(new MaterialtrackingSettingsAdapter.OnRecyclerItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, String data, int position) {
                                        ToastUtils.showShort(data);
                                        for (int i = 0; i < materialSettingsList.size(); i++) {
                                            if (i == position) {
                                                materialSettingsList.get(i).setChoose(true);
                                            } else {
                                                materialSettingsList.get(i).setChoose(false);
                                            }
                                        }
                                        Intent intent = new Intent();
                                        intent.putExtra("state", data);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }

                                });
                            }
                        }
                    });
                } else {
                    ToastUtils.showShort("数据请求出错！");
                }
            }
        });
    }
}
