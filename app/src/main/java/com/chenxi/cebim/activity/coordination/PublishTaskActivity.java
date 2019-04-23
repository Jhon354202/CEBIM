package com.chenxi.cebim.activity.coordination;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.DocumentAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.utils.DatePickerUtil;
import com.chenxi.cebim.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishTaskActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout rl_planStartTime, rl_planEndTime, rl_publishPerson, rl_relatedePerson,
            rl_associationConstruction, rl_publishAttachment;
    private EditText et_taskName, et_modelQuantity, et_laborForce, et_remark;
    private TextView submit, tv_planStartTime, tv_planEndTime, tv_responsiblePerson, tv_relatede_person;
    private RecyclerView rv_publishAttachment;
    private ImageView back;
    private String responsiblePerson="", relatedePerson="";

    //    List<DocumentModel> taskPublishDocumentList = new ArrayList<>();//装用于发布附件
    private List<DocumentModel> documentList = new ArrayList<>();

    public static String documentString = new String();

    private DocumentAdapter documentAdapter;

    private int projectID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_task);
        projectID = SPUtils.getInstance().getInt("projectID");
        documentString = "";
        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        initView();
    }

    //控件初始化
    private void initView() {
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(this);

        submit = findViewById(R.id.toolbar_right_tv);//发送
        submit.setOnClickListener(this);

        et_taskName = findViewById(R.id.et_publish_edit);//任务名

        rl_planStartTime = findViewById(R.id.rl_task_publish_plantostart);//计划开始时间
        rl_planStartTime.setOnClickListener(this);

        tv_planStartTime = findViewById(R.id.tv_publish_plantostart_content);//计划开始时间内容

        rl_planEndTime = findViewById(R.id.rl_publish_endofplan);//计划结束时间
        rl_planEndTime.setOnClickListener(this);

        tv_planEndTime = findViewById(R.id.tv_publish_endofplan_content);//计划结束时间内容

        et_modelQuantity = findViewById(R.id.et_task_publish_model_quantity);//模型量

        et_laborForce = findViewById(R.id.et_task_publish_planned_labor_force);//实际劳动力

        et_remark = findViewById(R.id.et_task_publish_remark);//备注

        rl_publishPerson = findViewById(R.id.rl_task_publish_responsible_person);//责任人
        rl_publishPerson.setOnClickListener(this);

        tv_responsiblePerson = findViewById(R.id.tv_task_publish_responsible_person_content);//责任人内容

        rl_relatedePerson = findViewById(R.id.rl_task_publish_relatede_person);//相关人
        rl_relatedePerson.setOnClickListener(this);

        tv_relatede_person = findViewById(R.id.tv_task_edit_relatede_person_content);//相关人内容

        rl_associationConstruction = findViewById(R.id.rl_task_publish_association_construction);//关联构建
        rl_associationConstruction.setOnClickListener(this);

        rl_publishAttachment = findViewById(R.id.rl_task_publish_attachment);
        rl_publishAttachment.setOnClickListener(this);

        rv_publishAttachment = findViewById(R.id.rv_publish_attachment);//附件列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(PublishTaskActivity.this);
        rv_publishAttachment.setLayoutManager(layoutManager);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_left_btn://返回
                finish();
                break;

            case R.id.toolbar_right_tv://提交
                submit();
                break;

            case R.id.rl_task_publish_plantostart://计划开始时间
                DatePickerUtil.showDatePickerDialog(PublishTaskActivity.this, tv_planStartTime, Calendar.getInstance());
                break;

            case R.id.rl_publish_endofplan://计划结束时间
                DatePickerUtil.showDatePickerDialog(PublishTaskActivity.this, tv_planEndTime, Calendar.getInstance());
                break;


            case R.id.rl_task_publish_responsible_person://责任人

                Intent intent1 = new Intent(PublishTaskActivity.this, TaskEditMemberActivity.class);
                if (responsiblePerson != null) {
                    intent1.putExtra("nameStr", responsiblePerson);
                } else {
                    intent1.putExtra("nameStr", "");
                }
                intent1.putExtra("type", "responsiblePerson");
                startActivityForResult(intent1, 1314);

                break;

            case R.id.rl_task_publish_relatede_person://相关人

                Intent intent2 = new Intent(PublishTaskActivity.this, TaskEditMemberActivity.class);
                if (relatedePerson != null) {
                    intent2.putExtra("nameStr", relatedePerson);
                } else {
                    intent2.putExtra("nameStr", "");
                }
                intent2.putExtra("type", "relatedePerson");
                startActivityForResult(intent2, 1315);

                break;

            case R.id.rl_task_publish_association_construction://关联构建

                break;

            case R.id.rl_task_publish_attachment://附件

                Intent intentDocumnet = new Intent(PublishTaskActivity.this, TaskPublishAddDocumentActivity.class);
                intentDocumnet.putExtra("documentFileString", documentString);
                intentDocumnet.putExtra("from", "PublishTaskActivity");
                startActivity(intentDocumnet);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 1314:
                //返回的责任人数据
                if (resultCode == RESULT_OK) {
                    String returnData = data.getStringExtra("responsiblePersonReturnData");
                    StringBuffer sb = new StringBuffer();
                    List<String> responsiblePersonList = new ArrayList<>();//责任人列表
                    for (int i = 0; i < returnData.split(",").length; i++) {
                        sb.append(returnData.split(",")[i].split(":")[0]);
                        if (i < returnData.split(",").length - 1) {
                            sb.append(",");
                        }

                        //组装责任人为Json格式
                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("ID", returnData.split(",")[i].split(":")[1]);
                        map.put("Name", returnData.split(",")[i].split(":")[0]);
                        responsiblePersonList.add(JSON.toJSONString(map));

                    }
                    responsiblePerson = responsiblePersonList.toString();//组装好的responsiblePerson
                    tv_responsiblePerson.setText(sb.toString());
                }
                break;

            case 1315:
                //返回的相关人数据
                if (resultCode == RESULT_OK) {
                    String returnData = data.getStringExtra("relatedePersonReturnData");
                    List<String> relatedePersonList = new ArrayList<>();//相关人列表
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < returnData.split(",").length; i++) {
                        sb.append(returnData.split(",")[i].split(":")[0]);
                        if (i < returnData.split(",").length - 1) {
                            sb.append(",");
                        }

                        //组装相关人为Json格式
                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("ID", returnData.split(",")[i].split(":")[1]);
                        map.put("Name", returnData.split(",")[i].split(":")[0]);
                        relatedePersonList.add(JSON.toJSONString(map));
                    }
                    relatedePerson = relatedePersonList.toString();//组装好的relatedePerson
                    tv_relatede_person.setText(sb.toString());
                }
                break;

            default:
        }
    }

    //提交
    private void submit() {

        if (et_taskName.getText().toString().isEmpty()) {
            ToastUtils.showShort("请输入任务名");
        }else if(tv_planStartTime.getText().toString().isEmpty()){
            ToastUtils.showShort("请输入计划开始时间");
        }else if(tv_planEndTime.getText().toString().isEmpty()){
            ToastUtils.showShort("请输入计划结束时间");
        }else {
            progressDialog = new ProgressDialog(PublishTaskActivity.this);
            progressDialog.setMessage("创建中...");
            progressDialog.setCancelable(false);
            progressDialog.show();  //将进度条显示出来

            List<String> docuemntStrList = new ArrayList<>();//docuemnt列表字符串

            //准备附件数据
            for (int i = 0; i < documentList.size(); i++) {
                //获取用于上传的附件字段
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Name", documentList.get(i).getFileNama());
                map.put("ID", documentList.get(i).getFileString());
                docuemntStrList.add(JSON.toJSONString(map));
            }

            RequestBody requestBody = new FormBody.Builder()
                    .add("Name", et_taskName.getText().toString())//标题
                    .add("StartDate", tv_planStartTime.getText().toString().isEmpty() ? "" :
                            tv_planStartTime.getText().toString().split(" ")[0])//计划开始时间
                    .add("FinishDate", tv_planEndTime.getText().toString().isEmpty() ? "" :
                            tv_planEndTime.getText().toString().split(" ")[0])//计划结束时间
                    .add("ModelUnit", et_modelQuantity.getText().toString().isEmpty() ? "" :
                            et_modelQuantity.getText().toString())//模型量
                    .add("PlanLabor", et_laborForce.getText().toString().isEmpty() ? "" :
                            et_laborForce.getText().toString())//计划劳动力
                    .add("Remark", et_remark.getText().toString().isEmpty() ? "" :
                            et_remark.getText().toString())//备注
                    .add("UserIds", responsiblePerson)//责任人
                    .add("RelativeUserIds", relatedePerson)//相关人
                    .add("DocumentIds", docuemntStrList.toString())//附件
                    .build();

            Request request = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/SynergyTask")
                    .post(requestBody)
                    .build();

            MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ToastUtils.showShort("任务发布失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        ToastUtils.showShort("任务发布成功");
                        progressDialog.dismiss();  //将进度条隐藏
                        EventBus.getDefault().post(new CommonEven("任务详细修改刷新"));//发送消息，刷新TaskDetailActivity
                        finish();

//                        try {
//                            String responseData = response.body().string();
//                            org.json.JSONObject jsonObject = null;
//                            jsonObject = new org.json.JSONObject(responseData);
//
//                            String ID = jsonObject.getString("ID");
//                            String UserIds = jsonObject.getString("UserIds");
//                            String RelativeUserIds = jsonObject.getString("RelativeUserIds");
//                            String ActualStartDate = jsonObject.getString("ActualStartDate");
//                            String ActualFinishDate = jsonObject.getString("ActualFinishDate");
//                            String Remark = jsonObject.getString("Remark");
//                            String MaterialStartAt = jsonObject.getString("MaterialStartAt");
//                            String MaterialFinishAt = jsonObject.getString("MaterialFinishAt");
//                            String ActualUnit = jsonObject.getString("ActualUnit");
//                            String ModelUnit = jsonObject.getString("ModelUnit");
//                            String DocumentIds = jsonObject.getString("DocumentIds");
//                            String Name = jsonObject.getString("Name");
//                            String StartDate = jsonObject.getString("StartDate");
//                            String FinishDate = jsonObject.getString("FinishDate");
//                            String NewDuration = jsonObject.getString("NewDuration");
//                            String DurationText = jsonObject.getString("DurationText");
//                            String StartText = jsonObject.getString("StartText");
//                            String FinishText = jsonObject.getString("FinishText");
//                            String Notes = jsonObject.getString("Notes");
//
//                            int PlanId = -1;
//                            if (!jsonObject.get("PlanId").toString().equals("null")) {
//                                PlanId = jsonObject.getInt("PlanId");
//                            }
//
//                            int ProjectId = jsonObject.getInt("ProjectId");
//
//                            int ActualDuration = jsonObject.getInt("ActualDuration");
//
//                            int PlanLabor = -1;
//                            if (!jsonObject.get("PlanLabor").toString().equals("null")) {
//                                PlanLabor = jsonObject.getInt("PlanLabor");
//                            }
//
//                            int PracticalLaborSum = -1;
//                            if (!jsonObject.get("PracticalLaborSum").toString().equals("null")) {
//                                PracticalLaborSum = jsonObject.getInt("PracticalLaborSum");
//                            }
//
//                            int Priority = -1;
//                            if (!jsonObject.get("Priority").toString().equals("null")) {
//                                Priority = jsonObject.getInt("Priority");
//                            }
//
//                            int Serial = -1;
//                            if (!jsonObject.get("Serial").toString().equals("null")) {
//                                Serial = jsonObject.getInt("Serial");
//                            }
//
//                            int Duration = jsonObject.getInt("Duration");
//
//                            int CreatedBy = -1;
//                            if (!jsonObject.get("CreatedBy").toString().equals("null")) {
//                                CreatedBy = jsonObject.getInt("CreatedBy");
//                            }
//
//                            int UpdatedBy = -1;
//                            if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
//                                UpdatedBy = jsonObject.getInt("UpdatedBy");
//                            }
//
//                            Object AssignedAt = "";
//                            if (!jsonObject.get("AssignedAt").toString().equals("null")) {
//                                AssignedAt = jsonObject.get("AssignedAt");
//                            }
//
//                            Object DistributedAt = "";
//                            if (!jsonObject.get("DistributedAt").toString().equals("null")) {
//                                DistributedAt = jsonObject.get("DistributedAt");
//                            }
//                            Object CreatedAt = "";
//                            if (!jsonObject.get("CreatedAt").toString().equals("null")) {
//                                CreatedAt = jsonObject.get("CreatedAt");
//                            }
//
//                            Object UpdatedAt = "";
//                            if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
//                                UpdatedAt = jsonObject.get("UpdatedAt");
//                            }
//
//
//                            SynergyTaskEntity synergyTaskEntity = new SynergyTaskEntity(ID, UserIds,
//                                    RelativeUserIds, ActualStartDate, ActualFinishDate, Remark, MaterialStartAt,
//                                    MaterialFinishAt, ActualUnit, ModelUnit, DocumentIds, Name, StartDate, FinishDate,
//                                    NewDuration, DurationText, StartText, FinishText, Notes, PlanId,
//                                    ProjectId, ActualDuration, PlanLabor, PracticalLaborSum, Priority,
//                                    Serial, Duration, CreatedBy, UpdatedBy, AssignedAt, DistributedAt, CreatedAt, UpdatedAt);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            ToastUtils.showShort("数据请求出错");
//                            progressDialog.dismiss();  //将进度条隐藏
//                        }
                    } else {
                        ToastUtils.showShort("任务发布失败");
                        progressDialog.dismiss();  //将进度条隐藏
                    }
                }
            });
        }

    }

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {

        if (commonEven.getInfo().contains("附件字符串:")) {//从GridImageAdapter返回
            documentString = commonEven.getInfo().replace("附件字符串:", "");

            //不加这句会造成数据重复
            if (documentList != null) {
                documentList.clear();
            }

            for (int i = 0; i < documentString.split("△").length; i++) {
                DocumentModel documentModel = new DocumentModel(documentString.split("△")[i].split("☆")[1],
                        documentString.split("△")[i].split("☆")[0]);
                documentList.add(documentModel);
            }

            //显示附件列表
            documentAdapter = new DocumentAdapter(MyApplication.getContext(), documentList,
                    SPUtils.getInstance().getInt("projectID"), true, "PublishTaskActivity", PublishTaskActivity.this);
            rv_publishAttachment.setAdapter(documentAdapter);

        } else if (commonEven.getInfo().contains("PublishTaskActivity删除:")) {
            for (int i = 0; i < documentList.size(); i++) {
                String item = documentList.get(i).getFileString();
                if (commonEven.getInfo().split(":")[1].equals(item)) {

                    //把选中项从documentString中去掉
                    documentString = documentString.replace(documentList.get(i).getFileNama() + "☆"
                            + documentList.get(i).getFileString(), "");
                    if (documentString != null && documentString.contains("△△")) {
                        documentString = documentString.replace("△△", "△");//去掉△△,变为△
                    }

                    if (documentString.contains("△")) {
                        documentString = StringUtil.trimFirstAndLastChar(documentString, '△');//去掉字符串首尾的@#@#
                    } else {
                        documentString = "";
                    }

                    documentList.remove(documentList.get(i));
                }
            }
            documentAdapter.notifyDataSetChanged();
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
