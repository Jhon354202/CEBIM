package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.chenxi.cebim.adapter.ComponentTrackingDetailsAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialTraceRecord;
import com.chenxi.cebim.entity.Model;
import com.chenxi.cebim.entity.TbMaterialTraceTemplateStates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ComponentTrackingDetailsActivity extends BaseActivity {

    private List<Model> models;
    private List<TbMaterialTraceTemplateStates> tbMaterialTraceTemplateStates;
    private RelativeLayout back;
    private int mPeojectID;
    private String id, componentUID;
    private RecyclerView recyclerView;
    private List<MaterialTraceRecord> materialTraceRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_componenttrackingdetails);
        mPeojectID = SPUtils.getInstance().getInt("projectID", -1);
        Intent intent = getIntent();
        id = intent.getStringExtra("ID");
        componentUID = intent.getStringExtra("ComponentUID");
        initView();
        getRecord();
        getData();

    }

    private void getRecord() {
        String url = AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/MaterialTrace/MaterialTraceRecord";
        Request request = new Request.Builder().url(url).build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错！1");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String jsonData = response.body().string();
                    materialTraceRecords = JSON.parseArray(jsonData, MaterialTraceRecord.class);
                } else {
                    ToastUtils.showShort("数据请求出错！");
                }
            }
        });
    }

    private void initView() {
        back = findViewById(R.id.rl_edit_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        refresh = findViewById(R.id.construction_detail_track_refresh);

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
                String responseData = response.body().string();
//                String responseData = "[\n" +
//                        "  {\n" +
//                        "    \"ID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "    \"ProjectID\": 983,\n" +
//                        "    \"Name\": \"初始模板\",\n" +
//                        "    \"OperationUserID\": 31,\n" +
//                        "    \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "    \"UpdateTime\": null,\n" +
//                        "    \"IsInitial\": true,\n" +
//                        "    \"tbMaterialTraceTemplateStates\": [\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"6404f91c-852e-4cde-8a51-2c1ab107d67a\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 1,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"安装\",\n" +
//                        "          \"ID\": \"6404f91c-852e-4cde-8a51-2c1ab107d67a\",\n" +
//                        "          \"Color\": \"128,128,225\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"ad85aff2-35b6-4184-84d1-2dc4e9d822b8\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 2,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"验收\",\n" +
//                        "          \"ID\": \"ad85aff2-35b6-4184-84d1-2dc4e9d822b8\",\n" +
//                        "          \"Color\": \"128,255,128\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"550323b0-f2c6-4f02-a520-45a0f972758d\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 3,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"进场\",\n" +
//                        "          \"ID\": \"550323b0-f2c6-4f02-a520-45a0f972758d\",\n" +
//                        "          \"Color\": \"255,192,128\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"ef6f50d3-5b7f-4e34-bdf1-a81581db7817\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 4,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"出场\",\n" +
//                        "          \"ID\": \"ef6f50d3-5b7f-4e34-bdf1-a81581db7817\",\n" +
//                        "          \"Color\": \"255,128,128\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"50e35753-0b57-4b4f-94fa-b3df4b773e22\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 5,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"领料\",\n" +
//                        "          \"ID\": \"50e35753-0b57-4b4f-94fa-b3df4b773e22\",\n" +
//                        "          \"Color\": \"255,128,255\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"24b205ec-dfb9-4c39-9833-fd8233f991ed\",\n" +
//                        "        \"StateName\": \"\",\n" +
//                        "        \"TempleteName\": \"\",\n" +
//                        "        \"Sort\": 6,\n" +
//                        "        \"OperationUserID\": 31,\n" +
//                        "        \"AddTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"UpdateTime\": \"2018-11-27 14:16:51\",\n" +
//                        "        \"IsInitial\": false,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"入库\",\n" +
//                        "          \"ID\": \"24b205ec-dfb9-4c39-9833-fd8233f991ed\",\n" +
//                        "          \"Color\": \"0,192,192\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"9cb8c010-ad1d-4fca-8acc-2332c99fa0eb\",\n" +
//                        "        \"StateName\": \"测试3\",\n" +
//                        "        \"TempleteName\": \"初始模板\",\n" +
//                        "        \"Sort\": 7,\n" +
//                        "        \"OperationUserID\": 125,\n" +
//                        "        \"AddTime\": \"2018-12-29 10:30:53\",\n" +
//                        "        \"UpdateTime\": \"2018-12-29 10:30:57\",\n" +
//                        "        \"IsInitial\": null,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"测试3\",\n" +
//                        "          \"ID\": \"9cb8c010-ad1d-4fca-8acc-2332c99fa0eb\",\n" +
//                        "          \"Color\": \"64,0,64\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"6eeff2c1-996f-4666-8b4e-09e98c9c93bc\",\n" +
//                        "        \"StateName\": \"测试5\",\n" +
//                        "        \"TempleteName\": \"初始模板\",\n" +
//                        "        \"Sort\": 8,\n" +
//                        "        \"OperationUserID\": 125,\n" +
//                        "        \"AddTime\": \"2018-12-29 10:32:09\",\n" +
//                        "        \"UpdateTime\": \"2018-12-29 10:32:12\",\n" +
//                        "        \"IsInitial\": null,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"测试5\",\n" +
//                        "          \"ID\": \"6eeff2c1-996f-4666-8b4e-09e98c9c93bc\",\n" +
//                        "          \"Color\": \"224,224,224\"\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      {\n" +
//                        "        \"TempleteID\": \"9ced6d69-901b-4cca-9ff4-c0d96c7f37f9\",\n" +
//                        "        \"StateID\": \"63ad85bd-30ae-441c-8006-552cb370e4f1\",\n" +
//                        "        \"StateName\": \"测试7\",\n" +
//                        "        \"TempleteName\": \"初始模板\",\n" +
//                        "        \"Sort\": 9,\n" +
//                        "        \"OperationUserID\": 125,\n" +
//                        "        \"AddTime\": \"2018-12-29 10:50:14\",\n" +
//                        "        \"UpdateTime\": \"2018-12-29 10:50:18\",\n" +
//                        "        \"IsInitial\": null,\n" +
//                        "        \"IsForbid\": false,\n" +
//                        "        \"idInfo\": {\n" +
//                        "          \"Name\": \"测试7\",\n" +
//                        "          \"ID\": \"63ad85bd-30ae-441c-8006-552cb370e4f1\",\n" +
//                        "          \"Color\": \"255,192,255\"\n" +
//                        "        }\n" +
//                        "      }\n" +
//                        "    ]\n" +
//                        "  }\n" +
//                        "]";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(ComponentTrackingDetailsActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
                        models = JSON.parseArray(responseData, Model.class);
                        if (models.size() != 0) {
                            tbMaterialTraceTemplateStates = models.get(0).getTbMaterialTraceTemplateStates();
                            ComponentTrackingDetailsAdapter adapter2 = new ComponentTrackingDetailsAdapter(ComponentTrackingDetailsActivity.this, tbMaterialTraceTemplateStates, componentUID, materialTraceRecords);
                            recyclerView.setAdapter(adapter2);
                            if (tbMaterialTraceTemplateStates.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                            }
                        } else {
                            ToastUtils.showShort("请求无数据！");
                        }
                    }
                });

            }
        });

    }
}
