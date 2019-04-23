package com.chenxi.cebim.activity.material;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.StatisticRecyclerAdapter;
import com.chenxi.cebim.adapter.StatisticsDataAdapter;
import com.chenxi.cebim.adapter.StatisticsPartAdapter;
import com.chenxi.cebim.adapter.StatisticsTemplatenameAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.Data;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.entity.MaterialTraceModel;
import com.chenxi.cebim.entity.MaterialTraceRecord;
import com.chenxi.cebim.entity.Part;
import com.chenxi.cebim.entity.State;
import com.chenxi.cebim.entity.StateNum;
import com.chenxi.cebim.entity.TbMaterialTraceTemplateStates;
import com.chenxi.cebim.entity.Template;
import com.chenxi.cebim.entity.Templatestate;
import com.chenxi.cebim.utils.StringUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectOverallDataStatisticsActivity extends AppCompatActivity {

    @BindView(R.id.part)
    TextView part;
    @BindView(R.id.part_icon)
    ImageView partIcon;
    @BindView(R.id.r_part)
    RelativeLayout rPart;
    @BindView(R.id.templatename)
    TextView templatename;
    List<Template> templateList = new ArrayList<>();
    @BindView(R.id.templatename_icon)
    ImageView templatenameIcon;
    @BindView(R.id.r_templatename)
    RelativeLayout rTemplatename;
    @BindView(R.id.data)
    TextView data;
    @BindView(R.id.data_icon)
    ImageView dataIcon;
    @BindView(R.id.r_data)
    RelativeLayout rData;
    @BindView(R.id.choose)
    LinearLayout choose;
    @BindView(R.id.statistic_recycler)
    RecyclerView statisticRecycler;
    @BindView(R.id.location)
    Button location;
    @BindView(R.id.part_list)
    RecyclerView partListView;
    @BindView(R.id.part_dialog)
    RelativeLayout partDialog;
    @BindView(R.id.template_list)
    RecyclerView templateListView;
    @BindView(R.id.templatename_dialog)
    RelativeLayout templatenameDialog;
    @BindView(R.id.data_list)
    RecyclerView dataListView;
    @BindView(R.id.data_dialog)
    RelativeLayout dataDialog;
    @BindView(R.id.dialog)
    RelativeLayout dialog;
    StatisticsTemplatenameAdapter templatenameAdapter;
    StatisticsPartAdapter partAdapter;
    List<Part> partList = new ArrayList<>();
    StatisticsDataAdapter dataAdapter;
    List<Data> dataList = new ArrayList<>();
    List<TbMaterialTraceTemplateStates> templateStates = new ArrayList<>();
    List<Templatestate> templatestateList = new ArrayList<>();
    private StatisticRecyclerAdapter statisticRecyclerAdapter;
    private ImageView back;
    private int projectId;
    List<StateNum> stateNums = new ArrayList<>();
    List<MaterialTraceRecord> materialTraceModels = new ArrayList<>();
    List<MaterialTraceRecord> templateMaterialTraceModels = new ArrayList<>();
    List<MaterialTraceRecord> dataMaterialTraceModels = new ArrayList<>();
    List<MaterialTraceRecord> partMaterialTraceModels = new ArrayList<>();
    List<StateNum> stateNums2 = new ArrayList<>();
    private String selectName = null;
    private String selectPart = null;
    private String selectData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_overall_data_statistics);
        ButterKnife.bind(this);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        location.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        partList.add(new Part("全部数据", true));
        partList.add(new Part("与我相关", false));
        partAdapter = new StatisticsPartAdapter(this, partList);
        partListView.setLayoutManager(layoutManager);
        partListView.setAdapter(partAdapter);
        partAdapter.setOnItemClickListener(new StatisticsPartAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

                part.setText(partList.get(position).getPartName());
                selectPart = partList.get(position).getPartName();
                dialog.setVisibility(View.GONE);
                part.setTextColor(Color.BLACK);
                partIcon.setImageResource(R.drawable.dialog_close);
                location.setClickable(false);
                location.setBackgroundResource(R.drawable.button_box_an);
                for (int i = 0; i < partList.size(); i++) {
                    if (i == position) {
                        partList.get(i).setCheck(true);
                    } else {
                        partList.get(i).setCheck(false);
                    }
                }
                partAdapter.notifyDataSetChanged();
                //筛选条件----与我相关
                if (partList.get(position).getPartName().equals("与我相关")) {
                    partMaterialTraceModels.clear();
                    stateNums.clear();
                    stateNums2.clear();
                    for (int i = 0; i < materialTraceModels.size(); i++) {
                        if (materialTraceModels.get(i).getOperationUserID() == SPUtils.getInstance().getInt("UserID")) {
                            partMaterialTraceModels.add(materialTraceModels.get(i));
                        }
                    }
                    if (partMaterialTraceModels.size() == 0) {
                        ToastUtils.showShort("与我相关无数据");
                        location.setVisibility(View.GONE);
                    } else {
                        //筛选条件：与我相关--未选择模板
                        if (selectName == null) {
                            //筛选条件：与我相关--未选择模板--累计数据
                            if (selectData == null || selectData.equals("累计数据")) {
                                setData(partMaterialTraceModels);
                                stateNums.addAll(stateNums2);
                                statisticRecyclerAdapter.notifyDataSetChanged();
                            } else {
                                //筛选条件：与我相关--未选择模板--最新数据
                                stateNums.clear();
                                stateNums2.clear();
                                dataMaterialTraceModels.clear();
                                for (int i = 0; i < partMaterialTraceModels.size(); i++) {
                                    if (partMaterialTraceModels.get(i).isCz()) {
                                        dataMaterialTraceModels.add(partMaterialTraceModels.get(i));
                                    }
                                }
                                if (dataMaterialTraceModels.size() == 0) {
                                    location.setVisibility(View.GONE);
                                    ToastUtils.showShort("与我相关无数据");
                                } else {
                                    setData(dataMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            //筛选条件：与我相关--已选择模板
                            stateNums.clear();
                            stateNums2.clear();
                            templateMaterialTraceModels.clear();
                            for (int i = 0; i < partMaterialTraceModels.size(); i++) {
                                if (partMaterialTraceModels.get(i).getTemplateInfo().getName().equals(selectName)) {
                                    templateMaterialTraceModels.add(partMaterialTraceModels.get(i));
                                }
                            }
                            if (templateMaterialTraceModels.size() == 0) {
                                ToastUtils.showShort("与我相关无数据");
                                location.setVisibility(View.GONE);
                            } else {
                                // //筛选条件：与我相关--未选择模板--累计数据
                                if (selectData == null || selectData.equals("累计数据")) {
                                    setData(templateMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                } else {
                                    dataMaterialTraceModels.clear();
                                    stateNums.clear();
                                    stateNums2.clear();
                                    for (int i = 0; i < templateMaterialTraceModels.size(); i++) {
                                        if (templateMaterialTraceModels.get(i).isCz()) {
                                            dataMaterialTraceModels.add(templateMaterialTraceModels.get(i));
                                        }
                                    }//筛选条件：与我相关--未选择模板--最新数据
                                    if (dataMaterialTraceModels.size() == 0) {
                                        location.setVisibility(View.GONE);
                                        ToastUtils.showShort("与我相关无数据");
                                    } else {
                                        setData(dataMaterialTraceModels);
                                        stateNums.addAll(stateNums2);
                                        statisticRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }
                } else {//筛选条件-----全部数据
                    stateNums.clear();
                    stateNums2.clear();
                    if (materialTraceModels.size() == 0) {
                        ToastUtils.showShort("请求无数据！");
                        location.setVisibility(View.GONE);
                    } else {
                        //筛选条件：全部数据--未选择模板
                        stateNums.clear();
                        stateNums2.clear();
                        //筛选条件：全部数据--未选择模板
                        if (selectName == null) {
                            //筛选条件：全部数据--未选择模板--累计数据
                            if (selectData == null || selectData.equals("累计数据")) {
                                setData(materialTraceModels);
                                stateNums.addAll(stateNums2);
                                statisticRecyclerAdapter.notifyDataSetChanged();
                            } else {
                                //筛选条件：全部数据--未选择模板--最新数据
                                stateNums.clear();
                                stateNums2.clear();
                                dataMaterialTraceModels.clear();
                                for (int i = 0; i < materialTraceModels.size(); i++) {
                                    if (materialTraceModels.get(i).isCz()) {
                                        dataMaterialTraceModels.add(materialTraceModels.get(i));
                                    }
                                }
                                if (dataMaterialTraceModels.size() == 0) {
                                    ToastUtils.showShort("请求无数据");
                                    location.setVisibility(View.GONE);
                                } else {
                                    setData(dataMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            //筛选条件：全部数据--已选择模板
                            templateMaterialTraceModels.clear();
                            for (int i = 0; i < materialTraceModels.size(); i++) {
                                if (materialTraceModels.get(i).getTemplateInfo().getName().equals(selectName)) {
                                    templateMaterialTraceModels.add(materialTraceModels.get(i));
                                }
                            }
                            if (templateMaterialTraceModels.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                                location.setVisibility(View.GONE);
                            } else {
                                //筛选条件：全部数据--已选择模板--累计数据
                                if (selectData == null || selectData.equals("累计数据")) {
                                    setData(templateMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                } else {
                                    //筛选条件：全部数据--已选择模板--最新数据
                                    stateNums.clear();
                                    stateNums2.clear();
                                    dataMaterialTraceModels.clear();
                                    for (int i = 0; i < templateMaterialTraceModels.size(); i++) {
                                        if (templateMaterialTraceModels.get(i).isCz()) {
                                            dataMaterialTraceModels.add(templateMaterialTraceModels.get(i));
                                        }
                                    }
                                    if (dataMaterialTraceModels.size() == 0) {
                                        ToastUtils.showShort("请求无数据");
                                        location.setVisibility(View.GONE);
                                    } else {
                                        setData(dataMaterialTraceModels);
                                        stateNums.addAll(stateNums2);
                                        statisticRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }


                }
            }
        });
        dataList.add(new Data("累计数据", true));
        dataList.add(new Data("最新数据", false));
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        dataAdapter = new StatisticsDataAdapter(dataList, this);
        dataListView.setLayoutManager(layoutManager2);
        dataListView.setAdapter(dataAdapter);
        dataAdapter.setOnItemClickListener(new StatisticsDataAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                data.setText(dataList.get(position).getDataName());
                dialog.setVisibility(View.GONE);
                data.setTextColor(Color.BLACK);
                dataIcon.setImageResource(R.drawable.dialog_close);
                location.setClickable(false);
                location.setBackgroundResource(R.drawable.button_box_an);
                selectData = dataList.get(position).getDataName();
                for (int i = 0; i < dataList.size(); i++) {
                    if (i == position) {
                        dataList.get(i).setCheck(true);
                    } else {
                        dataList.get(i).setCheck(false);
                    }
                }
                dataAdapter.notifyDataSetChanged();
                dataMaterialTraceModels.clear();
                stateNums.clear();
                stateNums2.clear();
                stateNums.clear();
                //筛选条件：最新数据
                if (selectData.equals("最新数据")) {
                    for (int i = 0; i < materialTraceModels.size(); i++) {
                        if (materialTraceModels.get(i).isCz()) {
                            dataMaterialTraceModels.add(materialTraceModels.get(i));
                        }
                    }
                    if (dataMaterialTraceModels.size() == 0) {
                        ToastUtils.showShort("无最新数据");
                        location.setVisibility(View.GONE);
                    } else {
                        //筛选条件：最新数据--全部数据
                        if (selectPart == null || selectPart.equals("全部数据")) {
                            setData(dataMaterialTraceModels);
                            stateNums.addAll(stateNums2);
                            statisticRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            //筛选条件：最新数据--与我相关
                            partMaterialTraceModels.clear();
                            stateNums.clear();
                            stateNums2.clear();
                            for (int i = 0; i < dataMaterialTraceModels.size(); i++) {
                                if (dataMaterialTraceModels.get(i).getOperationUserID() == SPUtils.getInstance().getInt("UserID")) {
                                    partMaterialTraceModels.add(dataMaterialTraceModels.get(i));
                                }
                            }
                            if (partMaterialTraceModels.size() == 0) {
                                ToastUtils.showShort("无最新数据");
                                location.setVisibility(View.GONE);
                            } else {
                                //筛选条件：最新数据--与我相关--未选择模板
                                if (selectName == null) {
                                    setData(partMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                } else {
                                    templateMaterialTraceModels.clear();
                                    stateNums.clear();
                                    stateNums2.clear();
                                }
                            }
                        }
                    }

                } else {
                    //筛选条件：累计数据
                    stateNums.clear();
                    stateNums2.clear();

                    //筛选条件：累计数据--全部数据
                    if (selectPart == null || selectPart.equals("全部数据")) {
                        //筛选条件：累计数据--全部数据--未选择模板
                        if (selectName == null) {
                            setData(materialTraceModels);
                            stateNums.addAll(stateNums2);
                            statisticRecyclerAdapter.notifyDataSetChanged();
                        } else {
                            //筛选条件：累计数据--全部数据--已选择模板
                            templateMaterialTraceModels.clear();
                            stateNums.clear();
                            stateNums2.clear();
                            for (int i = 0; i < materialTraceModels.size(); i++) {
                                if (materialTraceModels.get(i).getTemplateInfo().getName().equals(selectName)) {
                                    templateMaterialTraceModels.add(materialTraceModels.get(i));
                                }
                            }
                            if (templateMaterialTraceModels.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                                location.setVisibility(View.GONE);
                            } else {
                                setData(templateMaterialTraceModels);
                                stateNums.addAll(stateNums2);
                                statisticRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        //筛选条件：累计数据--与我相关
                        partMaterialTraceModels.clear();
                        stateNums.clear();
                        stateNums2.clear();
                        for (int i = 0; i < materialTraceModels.size(); i++) {
                            if (materialTraceModels.get(i).getOperationUserID() == SPUtils.getInstance().getInt("UserID")) {
                                partMaterialTraceModels.add(materialTraceModels.get(i));
                            }
                        }
                        if (partMaterialTraceModels.size() == 0) {
                            ToastUtils.showShort("请求无数据");
                            location.setVisibility(View.GONE);
                        } else {
                            //筛选条件：累计数据--与我相关--未选择模板
                            if (selectName == null) {
                                setData(partMaterialTraceModels);
                                stateNums.addAll(stateNums2);
                                statisticRecyclerAdapter.notifyDataSetChanged();
                            } else {
                                stateNums.clear();
                                stateNums2.clear();
                                templateMaterialTraceModels.clear();
                                for (int i = 0; i < partMaterialTraceModels.size(); i++) {
                                    if (partMaterialTraceModels.get(i).getTemplateInfo().getName().equals(selectName)) {
                                        templateMaterialTraceModels.add(partMaterialTraceModels.get(i));
                                    }
                                }
                                if (templateMaterialTraceModels.size() == 0) {
                                    ToastUtils.showShort("请求无数据");
                                    location.setVisibility(View.GONE);
                                } else {
                                    setData(templateMaterialTraceModels);
                                    stateNums.addAll(stateNums2);
                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        });

        getRecyclerData();
        getTemplateData();
    }

    private void getRecyclerData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/MaterialTraceRecord")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据1请求出错！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            materialTraceModels = JSON.parseArray(result, MaterialTraceRecord.class);
                            if (materialTraceModels.size() == 0) {
                                ToastUtils.showShort("请求无数据！");
                                location.setVisibility(View.GONE);
                            } else {
                                setData(materialTraceModels);
                                stateNums.addAll(stateNums2);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(ProjectOverallDataStatisticsActivity.this);
                                statisticRecycler.setLayoutManager(layoutManager);
                                statisticRecyclerAdapter = new StatisticRecyclerAdapter(ProjectOverallDataStatisticsActivity.this, stateNums);
                                statisticRecycler.setAdapter(statisticRecyclerAdapter);
                                statisticRecyclerAdapter.setOnItemClickListener(new StatisticRecyclerAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, StatisticRecyclerAdapter.ViewName viewName, int position) {
                                        boolean iscleck = false;
                                        switch (view.getId()) {
                                            case R.id.check:
                                                if (stateNums.get(position).isClick()) {
                                                    stateNums.get(position).setClick(false);
                                                } else {
                                                    stateNums.get(position).setClick(true);
                                                }

                                                for (int i = 0; i < stateNums.size(); i++) {
                                                    if (stateNums.get(i).isClick()) {
                                                        iscleck = true;
                                                    }
                                                }
                                                if (iscleck) {
                                                    location.setBackgroundResource(R.drawable.button_box_light);
                                                    location.setClickable(true);
                                                } else {
                                                    location.setBackgroundResource(R.drawable.button_box_an);
                                                    location.setClickable(false);
                                                }
                                                break;
                                            default:
                                                if (stateNums.get(position).isClick()) {
                                                    stateNums.get(position).setClick(false);
                                                } else {
                                                    stateNums.get(position).setClick(true);
                                                }
                                                for (int i = 0; i < stateNums.size(); i++) {
                                                    if (stateNums.get(i).isClick()) {
                                                        iscleck = true;
                                                    }
                                                }
                                                if (iscleck) {
                                                    location.setBackgroundResource(R.drawable.button_box_light);
                                                    location.setClickable(true);
                                                } else {
                                                    location.setBackgroundResource(R.drawable.button_box_an);
                                                    location.setClickable(false);
                                                }
                                                break;
                                        }

                                        statisticRecyclerAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });

                } else {
                    ToastUtils.showShort("数据2请求出错！");
                }
            }
        });
    }

    private void getTemplateData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/Template")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String resresponseData = response.body().string();
                    templateList = JSON.parseArray(resresponseData, Template.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            templateList.get(0).setChoose(true);
                            String id = templateList.get(0).getID();
                            getTemplateState(id);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ProjectOverallDataStatisticsActivity.this);
                            templatenameAdapter = new StatisticsTemplatenameAdapter(ProjectOverallDataStatisticsActivity.this, templateList);
                            templateListView.setLayoutManager(layoutManager);
                            templateListView.setAdapter(templatenameAdapter);
                            templatenameAdapter.setOnItemClickListener(new StatisticsTemplatenameAdapter.OnItemClickListener() {
                                @Override
                                public void onClick(int position) {
                                    selectName = templateList.get(position).getName();
                                    String selectId = templateList.get(position).getID();
                                    getTemplateState(selectId);
                                    //templateList.get(position).setChoose(true);
                                    location.setClickable(false);
                                    location.setBackgroundResource(R.drawable.button_box_an);
                                    for (int i = 0; i < templateList.size(); i++) {
                                        if (i == position) {
                                            templateList.get(i).setChoose(true);
                                        } else {
                                            templateList.get(i).setChoose(false);
                                        }
                                    }
                                    templatenameAdapter.notifyDataSetChanged();
                                    templatename.setText(selectName);
                                    stateNums.clear();
                                    stateNums2.clear();
                                    //筛选条件：选择模板
                                    templateMaterialTraceModels.clear();
                                    for (int i = 0; i < materialTraceModels.size(); i++) {
                                        if (materialTraceModels.get(i).getTemplateInfo().getName().equals(templateList.get(position).getName())) {
                                            templateMaterialTraceModels.add(materialTraceModels.get(i));
                                        }
                                    }
                                    if (templateMaterialTraceModels.size() == 0) {
                                        ToastUtils.showShort("请求无数据！");
                                        location.setVisibility(View.GONE);
                                    } else {
                                        stateNums.clear();
                                        stateNums2.clear();
                                        //筛选条件：选择模板--全部数据
                                        if (selectPart == null || selectPart.equals("全部数据")) {
                                            //筛选条件：选择模板--全部数据--累计数据
                                            if (selectData == null || selectData.equals("累计数据")) {
                                                setData(templateMaterialTraceModels);
                                                stateNums.addAll(stateNums2);
                                                statisticRecyclerAdapter.notifyDataSetChanged();
                                            } else {
                                                //筛选条件：选择模板--全部数据--最新数据
                                                stateNums2.clear();
                                                stateNums.clear();
                                                dataMaterialTraceModels.clear();
                                                for (int i = 0; i < templateMaterialTraceModels.size(); i++) {
                                                    if (templateMaterialTraceModels.get(i).isCz()) {
                                                        dataMaterialTraceModels.add(templateMaterialTraceModels.get(i));
                                                    }
                                                }
                                                if (dataMaterialTraceModels.size() == 0) {
                                                    ToastUtils.showShort("请求无数据！");
                                                    location.setVisibility(View.GONE);
                                                } else {
                                                    setData(templateMaterialTraceModels);
                                                    stateNums.addAll(stateNums2);
                                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        } else {
                                            //筛选条件：选择模板--与我相关
                                            partMaterialTraceModels.clear();
                                            stateNums.clear();
                                            stateNums2.clear();
                                            for (int i = 0; i < templateMaterialTraceModels.size(); i++) {
                                                if (templateMaterialTraceModels.get(i).getOperationUserID() == SPUtils.getInstance().getInt("UserID")) {
                                                    partMaterialTraceModels.add(templateMaterialTraceModels.get(i));
                                                }
                                            }
                                            if (partMaterialTraceModels.size() == 0) {
                                                ToastUtils.showShort("请求无数据！");
                                                location.setVisibility(View.GONE);
                                            } else {
                                                stateNums.clear();
                                                stateNums2.clear();
                                                //筛选条件：选择模板--与我相关--累计数据
                                                if (selectData == null || selectData.equals("累计数据")) {
                                                    setData(partMaterialTraceModels);
                                                    stateNums.addAll(stateNums2);
                                                    statisticRecyclerAdapter.notifyDataSetChanged();
                                                } else {
                                                    //筛选条件：选择模板--与我相关--最新数据
                                                    stateNums.clear();
                                                    stateNums2.clear();
                                                    dataMaterialTraceModels.clear();
                                                    for (int i = 0; i < partMaterialTraceModels.size(); i++) {
                                                        if (partMaterialTraceModels.get(i).isCz()) {
                                                            dataMaterialTraceModels.add(partMaterialTraceModels.get(i));
                                                        }
                                                    }
                                                    if (dataMaterialTraceModels.size() == 0) {
                                                        ToastUtils.showShort("请求无数据");
                                                        location.setVisibility(View.GONE);
                                                    } else {
                                                        setData(dataMaterialTraceModels);
                                                        stateNums.addAll(stateNums2);
                                                        statisticRecyclerAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    dialog.setVisibility(View.GONE);
                                    templatename.setTextColor(Color.BLACK);
                                    templatenameIcon.setImageResource(R.drawable.dialog_close);
                                }
                            });
                        }
                    });

                } else {
                    ToastUtils.showShort("数据请求出错！");
                }
            }
        });
    }

    private void getTemplateState(String id) {
        templatestateList.clear();
        String url = AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/Template?where=ID=Guid(\"" + id + "\")";
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
                    String jsondata = response.body().string();
                    templatestateList = JSON.parseArray(jsondata, Templatestate.class);
                    templateStates = templatestateList.get(0).getTbMaterialTraceTemplateStates();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (templateStates.size() == 0) {
                                ToastUtils.showShort("无模板状态");
                                // location.setVisibility(View.GONE);
                            }
                        }
                    });

                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    @OnClick({R.id.r_part, R.id.r_templatename, R.id.r_data, R.id.location, R.id.dialog})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.r_part:
                showPartDialog();
                break;
            case R.id.r_templatename:
                showTemplatenameDialog();
                break;
            case R.id.r_data:
                showDataDialog();
                break;
            case R.id.location:
                List<StateNum> chooseItem = new ArrayList<>();
                StringBuffer completedSb = new StringBuffer();
                StringBuffer colorSb = new StringBuffer();
                StringBuffer componentIdSbs = new StringBuffer();
                for (int i = 0; i < stateNums.size(); i++) {
                    if (stateNums.get(i).isClick()) {
                        chooseItem.add(stateNums.get(i));
                        for (Integer s : stateNums.get(i).getModelIds()) {
                            completedSb.append(s);
                            completedSb.append(",");
                        }
                        for (String s : stateNums.get(i).getList()) {
                            componentIdSbs.append(s);
                            componentIdSbs.append(",");
                        }
                        colorSb.append("'" + stateNums.get(i).getColor() + "'");
                        colorSb.append(",");
                    }
                }
                String modelIdString = StringUtil.trimFirstAndLastChar(completedSb.toString(), ',');
                String colorString = StringUtil.trimFirstAndLastChar(colorSb.toString(), ',');
                String componentIdSbstring = StringUtil.trimFirstAndLastChar(componentIdSbs.toString(), ',');
                Intent intent = new Intent(this, OpenOverallActivity.class);
                intent.putExtra("chooseItem", (Serializable) chooseItem);
                intent.putExtra("modelIds", modelIdString);
                intent.putExtra("colors", colorString);
                intent.putExtra("componentIdSbs", componentIdSbstring);
                startActivity(intent);
                break;
            case R.id.dialog:
                dialog.setVisibility(View.GONE);
                partIcon.setImageResource(R.drawable.dialog_close);
                part.setTextColor(Color.BLACK);
                templatenameIcon.setImageResource(R.drawable.dialog_close);
                templatename.setTextColor(Color.BLACK);
                dataIcon.setImageResource(R.drawable.dialog_close);
                data.setTextColor(Color.BLACK);
                break;
        }
    }

    private void showPartDialog() {
        dialog.setVisibility(View.VISIBLE);
        if (partDialog.getVisibility() == View.GONE) {
            partDialog.setVisibility(View.VISIBLE);
            partIcon.setImageResource(R.drawable.dialog_open);
            part.setTextColor(Color.BLUE);
        } else {
            partDialog.setVisibility(View.GONE);
            dialog.setVisibility(View.GONE);
            partIcon.setImageResource(R.drawable.dialog_close);
            part.setTextColor(Color.BLACK);
        }
        if (templatenameDialog.getVisibility() == View.VISIBLE) {
            templatenameDialog.setVisibility(View.GONE);
            templatenameIcon.setImageResource(R.drawable.dialog_close);
            templatename.setTextColor(Color.BLACK);
        }
        if (dataDialog.getVisibility() == View.VISIBLE) {
            dataDialog.setVisibility(View.GONE);
            dataIcon.setImageResource(R.drawable.dialog_close);
            data.setTextColor(Color.BLACK);
        }

    }

    public void setData(List<MaterialTraceRecord> list) {
        stateNums2.clear();
        if (templateStates.size() == 0) {
            ToastUtils.showShort("无模板状态");
        } else {
            List<String> list1 = new ArrayList<>();
            String color = "";
            for (TbMaterialTraceTemplateStates states : templateStates) {
                int i = 0;
                list1.clear();
                for (MaterialTraceRecord materialTraceRecord : list) {
                    if (materialTraceRecord.getStateInfo().getName().equals(states.getIdInfo().getName())) {
                        i++;
                        list1.add(materialTraceRecord.getComponentID());
                        color = materialTraceRecord.getStateInfo().getColor();
                    }
                }
                if (i == 0) {
                } else {
                    stateNums2.add(new StateNum(states.getIdInfo().getName() + "总数", i, color, list1));
                }
            }
            if (stateNums2.size() == 0) {
                location.setVisibility(View.GONE);
            } else {
                location.setVisibility(View.VISIBLE);
            }
        }
//        int num = 0, num2 = 0, num3 = 0, num4 = 0, num5 = 0, num6 = 0;
//        String color1 = null, color2 = null, color3 = null, color4 = null, color5 = null, color6 = null;
//        List<String> list1 = new ArrayList<>(), list2 = new ArrayList<>(),
//                list3 = new ArrayList<>(), list4 = new ArrayList<>(),
//                list5 = new ArrayList<>(), list6 = new ArrayList<>();
//        List<Integer> modelIdList1 = new ArrayList<>(), modelIdList2 = new ArrayList<>(), modelIdList3 = new ArrayList<>(),
//                modelIdList4 = new ArrayList<>(), modelIdList5 = new ArrayList<>(), modelIdList6 = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            String name = list.get(i).getStateInfo().getName();
//            switch (name) {
//                case "安装":
//                    num++;
//                    color1 = list.get(i).getStateInfo().getColor();
//                    list1.add(list.get(i).getComponentID());
//                    modelIdList1.add(list.get(i).getModelID());
//                    break;
//                case "领料":
//                    num2++;
//                    color2 = list.get(i).getStateInfo().getColor();
//                    list2.add(list.get(i).getComponentID());
//                    modelIdList2.add(list.get(i).getModelID());
//                    break;
//                case "入库":
//                    num3++;
//                    color3 = list.get(i).getStateInfo().getColor();
//                    list3.add(list.get(i).getComponentID());
//                    modelIdList3.add(list.get(i).getModelID());
//                    break;
//                case "进场":
//                    num4++;
//                    color4 = list.get(i).getStateInfo().getColor();
//                    list4.add(list.get(i).getComponentID());
//                    modelIdList4.add(list.get(i).getModelID());
//                    break;
//                case "出场":
//                    num5++;
//                    color5 = list.get(i).getStateInfo().getColor();
//                    list5.add(list.get(i).getComponentID());
//                    modelIdList5.add(list.get(i).getModelID());
//                    break;
//                case "验收":
//                    num6++;
//                    color6 = list.get(i).getStateInfo().getColor();
//                    list6.add(list.get(i).getComponentID());
//                    modelIdList6.add(list.get(i).getModelID());
//                    break;
//            }
//        }
//        if (num != 0) {
//            stateNums2.add(new StateNum("安装总数", num, color1, list1, modelIdList1));
//        }
//        if (num2 != 0) {
//            stateNums2.add(new StateNum("领料总数", num2, color2, list2, modelIdList2));
//        }
//        if (num3 != 0) {
//            stateNums2.add(new StateNum("入库总数", num3, color3, list3, modelIdList3));
//        }
//        if (num4 != 0) {
//            stateNums2.add(new StateNum("进场总数", num4, color4, list4, modelIdList4));
//        }
//        if (num5 != 0) {
//            stateNums2.add(new StateNum("出场总数", num5, color5, list5, modelIdList5));
//        }
//        if (num6 != 0) {
//            stateNums2.add(new StateNum("验收总数", num6, color6, list6, modelIdList6));
//        }

    }

    private void showTemplatenameDialog() {
        dialog.setVisibility(View.VISIBLE);
        if (partDialog.getVisibility() == View.VISIBLE) {
            partDialog.setVisibility(View.GONE);
            part.setTextColor(Color.BLACK);
            partIcon.setImageResource(R.drawable.dialog_close);
        }
        if (templatenameDialog.getVisibility() == View.GONE) {
            templatenameDialog.setVisibility(View.VISIBLE);
            templatename.setTextColor(Color.BLUE);
            templatenameIcon.setImageResource(R.drawable.dialog_open);
        } else {
            templatenameDialog.setVisibility(View.GONE);
            dialog.setVisibility(View.GONE);
            templatename.setTextColor(Color.BLACK);
            templatenameIcon.setImageResource(R.drawable.dialog_close);
        }
        if (dataDialog.getVisibility() == View.VISIBLE) {
            dataDialog.setVisibility(View.GONE);
            data.setTextColor(Color.BLACK);
            dataIcon.setImageResource(R.drawable.dialog_close);
        }

    }


    private void showDataDialog() {
        dialog.setVisibility(View.VISIBLE);
        if (partDialog.getVisibility() == View.VISIBLE) {
            partDialog.setVisibility(View.GONE);
            part.setTextColor(Color.BLACK);
            partIcon.setImageResource(R.drawable.dialog_close);
        }
        if (templatenameDialog.getVisibility() == View.VISIBLE) {
            templatenameDialog.setVisibility(View.GONE);
            templatename.setTextColor(Color.BLACK);
            templatenameIcon.setImageResource(R.drawable.dialog_close);
        }
        if (dataDialog.getVisibility() == View.GONE) {
            dataDialog.setVisibility(View.VISIBLE);
            data.setTextColor(Color.BLUE);
            dataIcon.setImageResource(R.drawable.dialog_open);
        } else {
            dataDialog.setVisibility(View.GONE);
            dialog.setVisibility(View.GONE);
            data.setTextColor(Color.BLACK);
            dataIcon.setImageResource(R.drawable.dialog_close);
        }

    }
}
