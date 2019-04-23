package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.CoordinationActivity;
import com.chenxi.cebim.activity.engineeringNews.EngineeringNewsActivity;
import com.chenxi.cebim.activity.inspection.ScanActivity;
import com.chenxi.cebim.activity.model.ModelListActivity;
import com.chenxi.cebim.adapter.GridViewAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.ProjectInfoItem;
import com.chenxi.cebim.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectActivity extends BaseActivity {

    private String projectName;
    private int projectId;
    private TextView titleName;
    private RelativeLayout back, search;
    private GridView mGridView;
    private GridViewAdapter adapter;
    public static List<ProjectInfoItem> mDatas, allFileNameList, mTemData;

    private String TAG = "ProjectActivity";

    private int[] picId = {R.drawable.gray_project_overview, R.drawable.model, R.drawable.gray_progress, R.drawable.team_work,
            R.drawable.gray_project_form, R.drawable.engineering_news,
            R.drawable.gray_material, R.drawable.data, R.drawable.gray_change, R.drawable.gray_cost,
            R.drawable.gray_component, R.drawable.gray_machine, R.drawable.project_scan, R.drawable.more};

    //    private int[] picId = {R.drawable.gray_project_overview, R.drawable.gray_model, R.drawable.gray_progress, R.drawable.gray_team_work,
//            R.drawable.gray_project_form, R.drawable.gray_engineering_news,
//            R.drawable.gray_material, R.drawable.data, R.drawable.gray_change, R.drawable.gray_cost,
//            R.drawable.component, R.drawable.gray_machine, R.drawable.more};
    private int[] picId2 = {R.drawable.model, R.drawable.team_work, R.drawable.engineering_news,
            R.drawable.data,R.drawable.component, R.drawable.project_scan, R.drawable.more};
    private int[] picId3 = {R.drawable.model, R.drawable.team_work, R.drawable.engineering_news,
            R.drawable.data, R.drawable.project_scan, R.drawable.more};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        //获取从ProjectAdapter传递过来的数据
        Intent intent = getIntent();
        projectName = intent.getStringExtra("projectName");
        projectId = intent.getIntExtra("projectId", -1);
        initDatas();
        initView();
    }

    /**
     * 数据初始化
     */
    private void initDatas() {
        mDatas = new ArrayList<ProjectInfoItem>();//用于本界面展示的数据的集合
        allFileNameList = new ArrayList<ProjectInfoItem>();//用于往FunctionalEditActivity传递数据的集合
        mTemData = new ArrayList<ProjectInfoItem>();//中转数据集合

        //从assets中的projectInfoItem.txt中获取数据
        InputStream is = null;
        String result = null;
        try {
            is = getAssets().open("projectInfoItem2.txt");
            int lenght = is.available();
            byte[] buffer = new byte[lenght];
            is.read(buffer);
            result = new String(buffer, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //读取assets中的文件信息
        JSONArray jsonArray = null;
        try {

            //获取持久化的数据
            String initDataResource = SPUtils.getInstance().getString("projectFunctionItem2", "无数据");

            //若获取的SharedPreferences数据无数据，则用assets中的projectInfoItem.txt中获取数据，否则，用SharedPreferences中的数据
            if (initDataResource.equals("无数据")) {
                jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String itemName = jsonObject.getString("itemName");
                    ProjectInfoItem projectInfoItem = new ProjectInfoItem(itemName, picId2[i], true);
                    mDatas.add(projectInfoItem);
                    allFileNameList.add(projectInfoItem);
                }

                LogUtil.i(TAG + "数据源：", "" + mDatas);
            } else {

                JSONArray array = new JSONArray(initDataResource);
                for (int i = 0; i < array.length(); i++) {

                    JSONObject jsonObject = array.getJSONObject(i);
                    String itemName = jsonObject.getString("itemName");
                    int image = Integer.parseInt(jsonObject.getString("image").toString());
                    Boolean isSelected = (Boolean) jsonObject.get("selected");

                    //如果SharedPreferences中返回的isSelected为true，则添加到本界面的集合mDatas中
                    if (isSelected) {
                        mDatas.add(new ProjectInfoItem(itemName, image, isSelected));
                    }

                    //把从SharedPreferences中获取的数据全部装入allFileNameList，用于传递给FunctionalEditActivity界面
                    allFileNameList.add(new ProjectInfoItem(itemName, image, isSelected));
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //获取进度数据
    private void getProgressData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/EngProjectProcess/" + projectId + "/GetList")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 界面初始化
     */
    private void initView() {
        titleName = (TextView) findViewById(R.id.fragment_project_title);
        titleName.setText(projectName);

        back = (RelativeLayout) findViewById(R.id.rl_project_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        search = (RelativeLayout) findViewById(R.id.rl_project_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showShort("你点击了搜索");
            }
        });

        mGridView = (GridView) findViewById(R.id.gv_show_button);
        adapter = new GridViewAdapter(ProjectActivity.this, mDatas);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (mDatas.get(position).getItemName().equals("工程概况")) {
//                    Intent intent = new Intent(ProjectActivity.this, ProjectSummaryActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("进度管理")) {
                    //跳到进度界面
//                    Intent intent = new Intent(ProjectActivity.this, ProgressActivity.class);
//                    intent.putExtra("projectName", projectName);
//                    intent.putExtra("projectId", projectId);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("问题")) {
                    //跳到问题界面
//                    Intent intent = new Intent(ProjectActivity.this, QuestionListActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("协同管理")) {
                    //跳到问题界面
                    Intent intent = new Intent(ProjectActivity.this, CoordinationActivity.class);
                    startActivity(intent);
                } else if (mDatas.get(position).getItemName().equals("任务")) {
                    //跳到任务界面
//                    Intent intent = new Intent(ProjectActivity.this, TaskListActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("模型")) {
                    //跳到模型设置界面
                    Intent intent = new Intent(ProjectActivity.this, ModelListActivity.class);
                    intent.putExtra("projectName", projectName);
                    intent.putExtra("projectId", projectId);
                    startActivity(intent);
//                    ToastUtils.showShort("正在加紧建设中,敬请期待...");

//                    Intent intent = new Intent(ProjectActivity.this, WebModelActivity.class);
//                    startActivity(intent);
                } else if (mDatas.get(position).getItemName().equals("表单")) {
                    //跳到表单界面
//                    Intent intent = new Intent(ProjectActivity.this, FormActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("工程动态")) {
                    //跳到工程动态界面
                    Intent intent = new Intent(ProjectActivity.this, EngineeringNewsActivity.class);
                    startActivity(intent);
                } else if (mDatas.get(position).getItemName().equals("材料")) {
                    //跳到材料界面
//                    Intent intent = new Intent(ProjectActivity.this, MaterialActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("资料")) {
                    //跳到资料界面
                    Intent intent = new Intent(ProjectActivity.this, DataActivity.class);
                    intent.putExtra("projectName", projectName);
                    intent.putExtra("projectId", projectId);
                    startActivity(intent);
                } else if (mDatas.get(position).getItemName().equals("变更")) {
                    //跳到变更界面
//                    Intent intent = new Intent(ProjectActivity.this, ChangeActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("成本")) {
                    //跳到变更界面
//                    Intent intent = new Intent(ProjectActivity.this, CostActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("部品部件")) {
                    //跳到变更界面
                    Intent intent = new Intent(ProjectActivity.this, ComponentActivity.class);
                    startActivity(intent);
//                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("大型机械")) {
                    //跳到变更界面
//                    Intent intent = new Intent(ProjectActivity.this, MechanicalActivity.class);
//                    startActivity(intent);
                    ToastUtils.showShort("正在加紧建设中,敬请期待...");
                } else if (mDatas.get(position).getItemName().equals("巡检")) {
                    //跳到扫码界面
                    Intent intent = new Intent(ProjectActivity.this, ScanActivity.class);
                    startActivity(intent);
                } else if (mDatas.get(position).getItemName().equals("更多")) {
                    //跳到更多界面
                    Intent intent = new Intent(ProjectActivity.this, FunctionalEditActivity.class);
                    intent.putExtra("checkedList", (Serializable) allFileNameList);
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {

                    List<ProjectInfoItem> returnData = (List<ProjectInfoItem>) data.getSerializableExtra("function_selected");

                    if (mTemData.size() > 0) {
                        mTemData.clear();
                    }

                    allFileNameList.clear();
                    for (int i = 0; i < returnData.size(); i++) {
                        if (returnData.get(i).getSelected()) {
                            mTemData.add(returnData.get(i));
                        }
                        allFileNameList.add(returnData.get(i));
                    }

                    mDatas.clear();
                    mDatas.addAll(mTemData);
                    adapter.notifyDataSetChanged();

                    //把从FunctionalEditActivity的list对象转为Json字符串，做持久化。
                    String json = com.alibaba.fastjson.JSONArray.toJSONString(allFileNameList).toString();
                    SPUtils.getInstance().put("projectFunctionItem", json);

                }
                break;
            default:
        }
    }

}
