package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.FeedBackRecordAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.SynergyTaskEntity;
import com.chenxi.cebim.entity.TaskReplyModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

public class TaskDetailActivity extends BaseActivity {

    private SynergyTaskEntity synergyTaskEntity;

    private ImageView back;
    private TextView title, feedBack, edit, plannedShip, plantoStart, endOfPlan, actualStart, actualEnd,
            predecessorsTask, trackingProgress, currentActualQuantity, modelQuantity, plannedLaborForce,
            actualLaborForce, responsiblePerson, relatedePerson, remark, tv_relatedData, feedBackRecordNum;

    private RelativeLayout relatedData;

    private RecyclerView recyclerView;

    private TaskReplyModel taskReplyModel;
    private List<TaskReplyModel> feedBackRecordList = new ArrayList<>();//获取回调接口中返回的
    private List<TaskReplyModel> tempList = new ArrayList<>();//中转list

    private String ID;
    private FeedBackRecordAdapter feedBackRecordAdapter;

    private int userID;
    private String userName;

    int practicalLaborSum = 0;//实际劳动力总和

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        userID = SPUtils.getInstance().getInt("UserID", -1);
        userName = SPUtils.getInstance().getString("UserName");
        synergyTaskEntity = (SynergyTaskEntity) getIntent().getSerializableExtra("SynergyTaskEntity");
        initView();
        getData();
        getPracticalLaborSum();

    }

    @Override
    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        super.onResume();
    }

    //控件初始化
    private void initView() {
        back = findViewById(R.id.toolbar_left_btn);//返回
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        title = findViewById(R.id.toolbar_title_tv);//标题
        feedBack = findViewById(R.id.toolbar_right_tv);//反馈
        feedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(TaskDetailActivity.this, TaskFeedBackActivity.class);
                intent.putExtra("ID", ID);
                intent.putExtra("taskName", synergyTaskEntity.getName());
                startActivity(intent);
            }
        });

        edit = findViewById(R.id.tv_task_edit);//编辑
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("SynergyTaskEntity", synergyTaskEntity);
                intent.setClass(TaskDetailActivity.this, TaskEditActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        plannedShip = findViewById(R.id.tv_task_planned_ship);//所属计划
        plantoStart = findViewById(R.id.tv_task_plantostart);//计划开始
        endOfPlan = findViewById(R.id.tv_task_endofplan);//计划结束
        actualStart = findViewById(R.id.tv_task_actualstart);//实际开始
        actualEnd = findViewById(R.id.tv_task_actualend);//实际结束
//        predecessorsTask = findViewById(R.id.tv_task_predecessors);//前置任务
//        trackingProgress = findViewById(R.id.tv_task_tracking_progress);//跟踪进度
        currentActualQuantity = findViewById(R.id.tv_task_current_actual_quantity);//当前实际量
        modelQuantity = findViewById(R.id.tv_task_model_quantity);//模型量
        plannedLaborForce = findViewById(R.id.tv_task_planned_labor_force);//计划劳动力
        actualLaborForce = findViewById(R.id.tv_task_actual_labor_force);//实际劳动力
        responsiblePerson = findViewById(R.id.tv_task_responsible_person);//责任人
        relatedePerson = findViewById(R.id.tv_task_relatede_person);//相关人员
        remark = findViewById(R.id.tv_task_remark);//备注

        relatedData = findViewById(R.id.rl_task_related_data);//关联资料
        relatedData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (synergyTaskEntity.getDocumentIds() == null || synergyTaskEntity.getDocumentIds().equals("null") ||
                        synergyTaskEntity.getDocumentIds().equals("")) {
                    ToastUtils.showShort("暂无相关资料");
                } else {
                    Intent intent = new Intent(TaskDetailActivity.this, TaskDataActivity.class);
                    intent.putExtra("DocumentIds", synergyTaskEntity.getDocumentIds());
                    intent.putExtra("taskName", synergyTaskEntity.getName());
                    startActivity(intent);
                }
            }
        });

        tv_relatedData = findViewById(R.id.tv_task_related_data_num);

        recyclerView = findViewById(R.id.feedback_record_recyclerView);//反馈记录列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(TaskDetailActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        feedBackRecordNum = findViewById(R.id.tv_task_feedback_record);
    }

    //数据初始化
    private void getData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + synergyTaskEntity.getID())
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        ID = jsonObject.getString("ID");
                        String UserIds = jsonObject.getString("UserIds");
                        String RelativeUserIds = jsonObject.getString("RelativeUserIds");
                        String ActualStartDate = jsonObject.getString("ActualStartDate");
                        String ActualFinishDate = jsonObject.getString("ActualFinishDate");
                        String Remark = jsonObject.getString("Remark");
                        String MaterialStartAt = jsonObject.getString("MaterialStartAt");
                        String MaterialFinishAt = jsonObject.getString("MaterialFinishAt");
                        String ActualUnit = jsonObject.getString("ActualUnit");
                        String ModelUnit = jsonObject.getString("ModelUnit");
                        String DocumentIds = jsonObject.getString("DocumentIds");
                        String Name = jsonObject.getString("Name");
                        String StartDate = jsonObject.getString("StartDate");
                        String FinishDate = jsonObject.getString("FinishDate");
                        String NewDuration = jsonObject.getString("NewDuration");
                        String DurationText = jsonObject.getString("DurationText");
                        String StartText = jsonObject.getString("StartText");
                        String FinishText = jsonObject.getString("FinishText");
                        String Notes = jsonObject.getString("Notes");

                        int PlanId = -1;
                        if (!jsonObject.get("PlanId").toString().equals("null")) {
                            PlanId = jsonObject.getInt("PlanId");
                        }

                        int ProjectId = jsonObject.getInt("ProjectId");

                        int ActualDuration = jsonObject.getInt("ActualDuration");

                        int PlanLabor = -1;
                        if (!jsonObject.get("PlanLabor").toString().equals("null")) {
                            PlanLabor = jsonObject.getInt("PlanLabor");
                        }

                        int PracticalLaborSum = -1;
                        if (!jsonObject.get("PracticalLaborSum").toString().equals("null")) {
                            PracticalLaborSum = jsonObject.getInt("PracticalLaborSum");
                        }

                        int Priority = -1;
                        if (!jsonObject.get("Priority").toString().equals("null")) {
                            Priority = jsonObject.getInt("Priority");
                        }

                        int Serial = -1;
                        if (!jsonObject.get("Serial").toString().equals("null")) {
                            Serial = jsonObject.getInt("Serial");
                        }

                        int Duration = jsonObject.getInt("Duration");

                        int CreatedBy = -1;
                        if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                            CreatedBy = jsonObject.getInt("CreatedBy");
                        }

                        int UpdatedBy = -1;
                        if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                            UpdatedBy = jsonObject.getInt("UpdatedBy");
                        }

                        Object AssignedAt = "";
                        if (!jsonObject.get("AssignedAt").toString().equals("null")) {
                            AssignedAt = jsonObject.get("AssignedAt");
                        }

                        Object DistributedAt = "";
                        if (!jsonObject.get("DistributedAt").toString().equals("null")) {
                            DistributedAt = jsonObject.get("DistributedAt");
                        }
                        Object CreatedAt = "";
                        if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                            CreatedAt = jsonObject.get("CreatedAt");
                        }

                        Object UpdatedAt = "";
                        if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                            UpdatedAt = jsonObject.get("UpdatedAt");
                        }

                        synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
                                RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
                                MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
                                NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
                                ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
                                Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                title.setText(synergyTaskEntity.getName());

                                //所属计划
                                if (synergyTaskEntity.getPlanId() == -1) {
                                    plannedShip.setText("暂无");
                                } else {
                                    plannedShip.setText(synergyTaskEntity.getPlanId());
                                }

                                //计划开始时间
                                if (synergyTaskEntity.getStartDate() == null || synergyTaskEntity.getStartDate().equals("null") ||
                                        synergyTaskEntity.getStartDate().equals("")) {
                                    plantoStart.setText("暂无");
                                } else {
                                    plantoStart.setText(synergyTaskEntity.getStartDate().split(" ")[0]);
                                }


                                //计划结束时间
                                if (synergyTaskEntity.getFinishDate() == null || synergyTaskEntity.getFinishDate().equals("null") ||
                                        synergyTaskEntity.getFinishDate().equals("")) {
                                    endOfPlan.setText("暂无");
                                } else {
                                    endOfPlan.setText(synergyTaskEntity.getFinishDate().split(" ")[0]);
                                }

                                //实际开始时间
                                if (synergyTaskEntity.getActualStartDate() == null || synergyTaskEntity.getActualStartDate().equals("null") ||
                                        synergyTaskEntity.getActualStartDate().equals("")) {
                                    actualStart.setText("暂无");
                                } else {
                                    actualStart.setText(synergyTaskEntity.getActualStartDate().split(" ")[0]);
                                }

                                //实际结束时间
                                if (synergyTaskEntity.getActualFinishDate() == null || synergyTaskEntity.getActualFinishDate().equals("null") ||
                                        synergyTaskEntity.getActualFinishDate().equals("")) {
                                    actualEnd.setText("暂无");
                                } else {
                                    actualEnd.setText(synergyTaskEntity.getActualFinishDate().split(" ")[0]);
                                }

                                //前置任务

                                //进度跟踪

                                //当前实际量
                                if (synergyTaskEntity.getActualUnit() == null || synergyTaskEntity.getActualUnit().equals("null") ||
                                        synergyTaskEntity.getActualUnit().equals("")) {
                                    currentActualQuantity.setText("暂无");
                                } else {
                                    currentActualQuantity.setText(synergyTaskEntity.getActualUnit());
                                }

                                //模型量
                                if (synergyTaskEntity.getModelUnit() == null || synergyTaskEntity.getModelUnit().equals("") ||
                                        synergyTaskEntity.getModelUnit().equals("null")) {
                                    modelQuantity.setText("暂无");
                                } else {
                                    modelQuantity.setText(synergyTaskEntity.getModelUnit());
                                }

                                //计划劳动力
                                if (synergyTaskEntity.getPlanLabor() == -1) {
                                    plannedLaborForce.setText("0(工日)");
                                } else {
                                    plannedLaborForce.setText(synergyTaskEntity.getPlanLabor() + "(工日)");
                                }

                                //责任人员
                                if (synergyTaskEntity.getUserIds() == null || synergyTaskEntity.getUserIds().equals("null")
                                        || synergyTaskEntity.getUserIds().equals("")) {
                                    responsiblePerson.setText("暂无");
                                } else {
                                    JSONArray array = null;
                                    try {
                                        array = new JSONArray(synergyTaskEntity.getUserIds());
                                        StringBuffer sb = new StringBuffer();
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject jsonObject = array.getJSONObject(i);
                                            sb.append(jsonObject.get("Name"));
                                            if (i < array.length() - 1) {
                                                sb.append(",");
                                            }
                                        }
                                        responsiblePerson.setText(sb.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //相关人员
                                if (synergyTaskEntity.getRelativeUserIds() == null || synergyTaskEntity.getRelativeUserIds().equals("null")
                                        || synergyTaskEntity.getRelativeUserIds().equals("")) {
                                    relatedePerson.setText("暂无");
                                } else {
                                    JSONArray array = null;
                                    try {
                                        array = new JSONArray(synergyTaskEntity.getRelativeUserIds());
                                        StringBuffer sb = new StringBuffer();
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject jsonObject = array.getJSONObject(i);
                                            sb.append(jsonObject.get("Name"));
                                            getFeedBack(jsonObject.get("ID").toString());
                                            if (i < array.length() - 1) {
                                                sb.append(",");
                                            }
                                        }
                                        relatedePerson.setText(sb.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //当相关人员不包含当前账号用户时，反馈按钮隐藏，否则显示
                                if (synergyTaskEntity.getRelativeUserIds().contains("" + userID) &&
                                        synergyTaskEntity.getRelativeUserIds().contains(userName)) {
                                    feedBack.setVisibility(View.VISIBLE);
                                } else {
                                    feedBack.setVisibility(View.GONE);
                                }

                                //备注
                                if (synergyTaskEntity.getRemark() == null || synergyTaskEntity.getRemark().equals("") ||
                                        synergyTaskEntity.getRemark().equals("null")) {
                                    remark.setText("暂无");
                                } else {
                                    remark.setText(synergyTaskEntity.getRemark());
                                }

                                //关联资料
                                if (synergyTaskEntity.getDocumentIds() == null || synergyTaskEntity.getDocumentIds().equals("null") ||
                                        synergyTaskEntity.getDocumentIds().equals("")) {
                                    tv_relatedData.setText("(0)");
                                } else {
                                    JSONArray array = null;

                                    try {
                                        array = new JSONArray(synergyTaskEntity.getDocumentIds());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    tv_relatedData.setText("(" + array.length() + ")");
                                }

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    //获取反馈记录
    private void getFeedBack(String createdBy) {

        feedBackRecordList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + synergyTaskEntity.getID() + "/Reply?where=CreatedBy=" + createdBy)
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        String ID = jsonObject.getString("ID");
                        String TaskId = jsonObject.getString("TaskId");
                        String Remark = jsonObject.getString("Remark");
                        String Pictures = jsonObject.getString("Pictures");
                        String ActualUnit = jsonObject.getString("ActualUnit");
                        String ModelUnit = jsonObject.getString("ModelUnit");

                        String CreatedAt = "";
                        if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                            CreatedAt = jsonObject.get("CreatedAt").toString();
                        }
                        String UpdatedAt = "";
                        if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                            UpdatedAt = jsonObject.get("UpdatedAt").toString();
                        }

                        String CreatedInfo = jsonObject.get("CrUserInfo").toString();
                        String CreatedUserName = "";
                        int CreatedUserID = -1;
                        if (CreatedInfo != null && (!CreatedInfo.equals("null"))) {
                            JSONObject crUserInfoObject = new JSONObject(CreatedInfo);
                            CreatedUserName = crUserInfoObject.getString("UserName");
                            CreatedUserID = crUserInfoObject.getInt("UserID");
                        }

                        String UpUserInfo = jsonObject.get("CrUserInfo").toString();
                        String UpdatedUserName = "";
                        int UpdatedUserID = -1;
                        if (UpUserInfo != null && (!UpUserInfo.equals("null"))) {
                            JSONObject upUserInfoObject = new JSONObject(CreatedInfo);
                            UpdatedUserName = upUserInfoObject.getString("UserName");
                            UpdatedUserID = upUserInfoObject.getInt("UserID");
                        }


                        int PracticalLabor = -1;
                        if (!jsonObject.get("PracticalLabor").toString().equals("null")) {
                            PracticalLabor = jsonObject.getInt("PracticalLabor");
                        }

                        int CreatedBy = -1;
                        if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                            CreatedBy = jsonObject.getInt("CreatedBy");
                        }

                        int UpdatedBy = -1;
                        if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                            UpdatedBy = jsonObject.getInt("UpdatedBy");
                        }

                        double Percentage = 0.00;
                        if (!jsonObject.get("Percentage").toString().equals("null")) {
                            Percentage = jsonObject.getInt("Percentage");
                        }

                        taskReplyModel = new TaskReplyModel(ID, TaskId, Remark, Pictures, ActualUnit, ModelUnit,
                                CreatedAt, UpdatedAt, CreatedUserName, UpdatedUserName, PracticalLabor, CreatedBy,
                                UpdatedBy, CreatedUserID, UpdatedUserID, Percentage);

                        feedBackRecordList.add(taskReplyModel);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (feedBackRecordList != null && feedBackRecordList.size() > 0) {
                                    feedBackRecordNum.setText("反馈记录(" + feedBackRecordList.size() + ")");
                                    //显示附件列表
                                    feedBackRecordAdapter = new FeedBackRecordAdapter(MyApplication.getContext(), feedBackRecordList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(TaskDetailActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(feedBackRecordAdapter);
                                } else {
                                    feedBackRecordNum.setText("反馈记录(" + 0 + ")");
                                    //显示附件列表
                                    feedBackRecordAdapter = new FeedBackRecordAdapter(MyApplication.getContext(), feedBackRecordList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(TaskDetailActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(feedBackRecordAdapter);
                                }

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    //获取实际劳动力
    private void getPracticalLaborSum() {
        practicalLaborSum=0;
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + synergyTaskEntity.getID() + "/Reply")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            int PracticalLabor = 0;
                            if (!jsonObject.get("PracticalLabor").toString().equals("null")) {
                                PracticalLabor = jsonObject.getInt("PracticalLabor");
                            }
                            practicalLaborSum += PracticalLabor;

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualLaborForce.setText(practicalLaborSum + "(工日)");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    //EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {
        if (commonEven.getInfo().equals("任务详细修改刷新")) {//刷新
            getData();
        } else if (commonEven.getInfo().equals("反馈记录刷新")) {
            getData();
            getPracticalLaborSum();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
