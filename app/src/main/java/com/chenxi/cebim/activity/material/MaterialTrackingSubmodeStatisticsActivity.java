package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.ModelstatisticsExpanAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.Modelstatistics;
import com.chenxi.cebim.entity.ModelstatisticsFather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MaterialTrackingSubmodeStatisticsActivity extends AppCompatActivity {

    @BindView(R.id.expanded_menu)
    ExpandableListView expandedMenu;
    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;
    @BindView(R.id.choose_all_text)
    TextView chooseAllText;
    @BindView(R.id.choose_all)
    RelativeLayout chooseAll;
    @BindView(R.id.confirm)
    RelativeLayout confirm;
    @BindView(R.id.search_img)
    ImageView searchImg;
    private ModelstatisticsExpanAdapter adapter;
    private List<ModelstatisticsFather> list;
    private List<Modelstatistics> modelstatistics = new ArrayList<>();
    private List<Integer> chooseIds = new ArrayList<>();
    int projectId;
    private ImageView back;
    private TextView toolbar_right_tv;
    ModelstatisticsFather modelstatisticsFather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_tracking_submode_statistics);
        ButterKnife.bind(this);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        bottomLayout.setVisibility(View.GONE);
        setData();
        initView();
    }


    private void setData() {
        if (list == null) {
            list = new ArrayList<>();
        }
        modelstatisticsFather = new ModelstatisticsFather();
        modelstatisticsFather.setTitle("无标签");
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
                    String jsonData = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            modelstatistics = JSON.parseArray(jsonData, Modelstatistics.class);
                            if (modelstatistics.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                            } else {
                                int downLoadNum = 0;
                                for (int i = 0; i < modelstatistics.size(); i++) {
                                    if (modelstatistics.get(i).getIsCompleted() == true) {
                                        downLoadNum++;
                                    }
                                    modelstatistics.get(i).setEdit(false);
                                }
                                modelstatisticsFather.setAllNum(modelstatistics.size());
                                modelstatisticsFather.setDownloadNum(downLoadNum);
                                modelstatisticsFather.setEdit(false);
                                modelstatisticsFather.setModelstatisticsList(modelstatistics);

                                list.add(modelstatisticsFather);
                                if (adapter == null) {
                                    adapter = new ModelstatisticsExpanAdapter(MaterialTrackingSubmodeStatisticsActivity.this, list);
                                    expandedMenu.setAdapter(adapter);
                                } else {
                                    adapter.flashData(list);
                                }
                            }
                        }
                    });

                } else {
                    ToastUtils.showShort("数据请求出错！");
                }
            }
        });
    }

    private void initView() {
        expandedMenu.setGroupIndicator(null);
        expandedMenu.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return false;
            }
        });
        expandedMenu.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (modelstatistics.get(childPosition).isCompleted()) {
                    Intent intent = new Intent(MaterialTrackingSubmodeStatisticsActivity.this, MaterialTrackingStatisticsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 1);
                    bundle.putString("modelName",modelstatistics.get(childPosition).getModelName());
                    bundle.putInt("model", modelstatistics.get(childPosition).getModelID());
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    ToastUtils.showShort("未完成轻量化，暂时无法打开");
                }
                return false;
            }
        });
        toolbar_right_tv = findViewById(R.id.toolbar_right_tv);
        toolbar_right_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modelstatistics.size() > 0) {
                    if (toolbar_right_tv.getText().equals("编辑")) {
                        toolbar_right_tv.setText("取消");
                        modelstatisticsFather.setEdit(true);
                        for (int i = 0; i < modelstatistics.size(); i++) {
                            modelstatistics.get(i).setEdit(true);
                        }
                        bottomLayout.setVisibility(View.VISIBLE);
                    } else {
                        toolbar_right_tv.setText("编辑");
                        modelstatisticsFather.setEdit(false);
                        for (int i = 0; i < modelstatistics.size(); i++) {
                            modelstatistics.get(i).setEdit(false);
                        }
                        bottomLayout.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    boolean hasChoose = false;

    @OnClick({R.id.choose_all, R.id.confirm, R.id.search_img})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.choose_all:
                if (chooseAllText.getText().equals("全选")) {
                    chooseAllText.setText("取消");
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setChoose(true);
                    }
                    for (int i = 0; i < modelstatistics.size(); i++) {
                        modelstatistics.get(i).setChoose(true);
                    }

                } else if (chooseAllText.getText().equals("取消")) {
                    chooseAllText.setText("全选");
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setChoose(false);
                    }
                    for (int i = 0; i < modelstatistics.size(); i++) {
                        modelstatistics.get(i).setChoose(false);
                    }

                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.confirm:
                for (int i = 0; i < modelstatistics.size(); i++) {
                    if (modelstatistics.get(i).isChoose()) {
                        hasChoose = true;
                        chooseIds.add(modelstatistics.get(i).getModelID());
                    }
                }
                if (true) {
                    intent = new Intent(this, MaterialTrackingStatisticsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 2);
                    bundle.putIntegerArrayList("chooseIds", (ArrayList<Integer>) chooseIds);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;
            case R.id.search_img:
                intent = new Intent(this, MaterialTrackingSubmodeStatisticsSearchActivity.class);
                startActivity(intent);
                break;
        }
    }
}
