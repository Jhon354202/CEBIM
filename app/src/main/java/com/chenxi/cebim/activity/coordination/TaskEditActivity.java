package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.SynergyTaskEntity;
import com.chenxi.cebim.utils.DatePickerUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class TaskEditActivity extends BaseActivity implements View.OnClickListener {

    private SynergyTaskEntity synergyTaskEntity;
    private TextView submit, tv_plantostart, tv_endofplan, tv_actualstart_content, tv_actualend_content,
            actual_labor_force_content, responsible_person_content, relatede_person_content, tv_task_name,
            tv_task_edit_plantostart, tv_task_edit_endofplan, tv_task_edit_actualstart, tv_task_edit_actualend,
            tv_task_edit_current_actual_quantity, tv_task_edit_model_quantity, tv_task_edit_planned_labor_force,
            tv_task_edit_actual_labor_force, tv_task_edit_responsible_person, tv_task_edit_relatede_person, tv_task_edit_remark;
    private EditText et_taskName, et_currentActualQuantity, et_modelQuantity, et_plannedLaborForce, et_remark;
    private RelativeLayout rl_plantostart, rl_endofplan, rl_actualStart, rl_actualEnd, responsible_person, relatede_person;
    private ImageView back;

    private String taskName, plantostart, endofplan, actualstart, actualend, currentActualQuantity, modelQuantity,
            plannedLaborForce, actualLaborForce, responsiblePerson, relatedePerson, remark;

    private int userID;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        synergyTaskEntity = (SynergyTaskEntity) getIntent().getSerializableExtra("SynergyTaskEntity");
        userID = SPUtils.getInstance().getInt("UserID", -1);
        userName = SPUtils.getInstance().getString("UserName");
        initView();
    }

    //控件初始化
    private void initView() {

        //这几个TextView，用于设置字体颜色用
        tv_task_name = findViewById(R.id.tv_task_name);//任务名

        tv_task_edit_plantostart = findViewById(R.id.tv_task_edit_plantostart);//计划开始时间


        tv_task_edit_endofplan = findViewById(R.id.tv_task_edit_endofplan);//计划结束时间


        tv_task_edit_actualstart = findViewById(R.id.tv_task_edit_actualstart);//实际开始时间
        tv_task_edit_actualend = findViewById(R.id.tv_task_edit_actualend);//实际结束时间
        tv_task_edit_current_actual_quantity = findViewById(R.id.tv_task_edit_current_actual_quantity);//当前实际量
        tv_task_edit_model_quantity = findViewById(R.id.tv_task_edit_model_quantity);//模型量
        tv_task_edit_planned_labor_force = findViewById(R.id.tv_task_edit_planned_labor_force);//计划劳动力
        tv_task_edit_actual_labor_force = findViewById(R.id.tv_task_edit_actual_labor_force);//实际劳动力
        tv_task_edit_responsible_person = findViewById(R.id.tv_task_edit_responsible_person);//责任人
        tv_task_edit_relatede_person = findViewById(R.id.tv_task_edit_relatede_person);//相关人
        tv_task_edit_remark = findViewById(R.id.tv_task_edit_remark);//备注

        back = findViewById(R.id.toolbar_left_btn);//返回
        back.setOnClickListener(this);

        submit = findViewById(R.id.toolbar_right_tv);//提交
        submit.setOnClickListener(this);

        et_taskName = findViewById(R.id.et_task_edit);//任务名称
        if (synergyTaskEntity.getName() != null && (!synergyTaskEntity.getName().equals(""))
                && (!synergyTaskEntity.getName().equals("null"))) {
            et_taskName.setText(synergyTaskEntity.getName());
        }

        Editable b = et_taskName.getText();//光标显示到最后面
        et_taskName.setSelection(b.length());

        //权限，只有创建者可以改任务名，责任人和相关人不可改
        if (synergyTaskEntity.getCreatedBy() != userID) {
            et_taskName.setEnabled(false);
            tv_task_name.setTextColor(getResources().getColor(R.color.gray_text, null));
        } else {
            et_taskName.setEnabled(true);
        }

        rl_plantostart = findViewById(R.id.rl_task_edit_plantostart);//计划开始时间，点击处
        rl_plantostart.setOnClickListener(this);

        tv_plantostart = findViewById(R.id.tv_task_edit_plantostart_content);//计划开始时间内容
        if (synergyTaskEntity.getStartDate() != null && (!synergyTaskEntity.getStartDate().equals(""))
                && (!synergyTaskEntity.getStartDate().equals("null"))) {
            tv_plantostart.setText(synergyTaskEntity.getStartDate().split(" ")[0]);
        }

        rl_endofplan = findViewById(R.id.rl_task_edit_endofplan);//计划结束时间，点击处
        rl_endofplan.setOnClickListener(this);

        tv_endofplan = findViewById(R.id.tv_task_edit_endofplan_content);//计划结束时间内容

        if (synergyTaskEntity.getFinishDate() != null && (!synergyTaskEntity.getFinishDate().equals(""))
                && (!synergyTaskEntity.getFinishDate().equals("null"))) {
            tv_endofplan.setText(synergyTaskEntity.getFinishDate().split(" ")[0]);
        }

        rl_actualStart = findViewById(R.id.rl_task_edit_actualstartdate);
        rl_actualStart.setOnClickListener(this);

        tv_actualstart_content = findViewById(R.id.tv_task_edit_actualstart_content);//实际开始内容
        if (synergyTaskEntity.getActualStartDate() != null && (!synergyTaskEntity.getActualStartDate().equals(""))
                && (!synergyTaskEntity.getActualStartDate().equals("null"))) {
            tv_actualstart_content.setText(synergyTaskEntity.getActualStartDate().split(" ")[0]);
        }

        rl_actualEnd = findViewById(R.id.rl_task_edit_actualfinishdate);
        rl_actualEnd.setOnClickListener(this);
        tv_actualend_content = findViewById(R.id.tv_task_edit_actualend_content);//实际结束内容
        if (synergyTaskEntity.getActualFinishDate() != null && (!synergyTaskEntity.getActualFinishDate().equals(""))
                && (!synergyTaskEntity.getActualFinishDate().equals("null"))) {
            tv_actualend_content.setText(synergyTaskEntity.getActualFinishDate().split(" ")[0]);
        }

        et_currentActualQuantity = findViewById(R.id.et_task_edit_current_actual_quantity);//当前实际量
        if (synergyTaskEntity.getActualUnit() != null && (!synergyTaskEntity.getActualUnit().equals(""))
                && (!synergyTaskEntity.getActualUnit().equals("null"))) {
            et_currentActualQuantity.setText(synergyTaskEntity.getActualUnit());
        }

        et_modelQuantity = findViewById(R.id.et_task_edit_model_quantity);//模型量
        if (synergyTaskEntity.getModelUnit() != null && (!synergyTaskEntity.getModelUnit().equals(""))
                && (!synergyTaskEntity.getModelUnit().equals("null"))) {
            et_modelQuantity.setText(synergyTaskEntity.getModelUnit());
        }

        et_plannedLaborForce = findViewById(R.id.et_task_edit_planned_labor_force);//计划劳动力
        if (synergyTaskEntity.getPlanLabor() == 0 || synergyTaskEntity.getPlanLabor() == -1) {
            et_plannedLaborForce.setText("" + 0);
        } else {
            et_plannedLaborForce.setText("" + synergyTaskEntity.getPlanLabor());
        }

        actual_labor_force_content = findViewById(R.id.tv_task_edit_actual_labor_force_content);//实际劳动力
        if (synergyTaskEntity.getPracticalLaborSum() == 0 || synergyTaskEntity.getPracticalLaborSum() == -1) {
            actual_labor_force_content.setText("" + 0);
        } else {
            actual_labor_force_content.setText("" + synergyTaskEntity.getPracticalLaborSum());
        }

        responsible_person = findViewById(R.id.rl_task_edit_responsible_person);//责任人员
        responsible_person.setOnClickListener(this);

        responsible_person_content = findViewById(R.id.tv_task_edit_responsible_person_content);//责任人内容
        if (synergyTaskEntity.getUserIds() != null && (!synergyTaskEntity.getUserIds().equals(""))
                && (!synergyTaskEntity.getUserIds().equals("null")) && (!synergyTaskEntity.getUserIds().equals("[]"))) {
            responsiblePerson = synergyTaskEntity.getUserIds().toString();//responsiblePerson赋值

            //显示责任人
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
                responsible_person_content.setText(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            responsiblePerson = "";//responsiblePerson赋值
        }

        relatede_person = findViewById(R.id.rl_task_edit_relatede_person);//相关人员
        relatede_person.setOnClickListener(this);

        relatede_person_content = findViewById(R.id.tv_task_edit_relatede_person_content);//相关人员内容
        if (synergyTaskEntity.getRelativeUserIds() != null && (!synergyTaskEntity.getRelativeUserIds().equals(""))
                && (!synergyTaskEntity.getRelativeUserIds().equals("null"))) {
            relatedePerson = synergyTaskEntity.getRelativeUserIds().toString();
            JSONArray array = null;
            try {
                array = new JSONArray(synergyTaskEntity.getRelativeUserIds());
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    sb.append(jsonObject.get("Name"));
                    if (i < array.length() - 1) {
                        sb.append(",");
                    }
                }
                relatede_person_content.setText(sb.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            relatedePerson = "";
        }

        et_remark = findViewById(R.id.et_task_edit_remark);//备注


        //不是创建人，没有权限的情况下，计划开始时间字体变灰色
        if (synergyTaskEntity.getCreatedBy() != userID) {
            tv_task_edit_plantostart.setTextColor(getResources().getColor(R.color.gray_text, null));
            tv_plantostart.setTextColor(getResources().getColor(R.color.gray_text, null));
            rl_plantostart.setEnabled(false);
        }

        //不是创建人，没有权限的情况下，计划结束时间字体变灰色
        if (synergyTaskEntity.getCreatedBy() != userID) {//创建人不是当前账号，字体变灰色
            tv_task_edit_endofplan.setTextColor(getResources().getColor(R.color.gray_text, null));
            tv_endofplan.setTextColor(getResources().getColor(R.color.gray_text, null));
        }

        //不是责任人，没有权限的情况下，实际开始时间字体变灰色
        if (!synergyTaskEntity.getUserIds().contains("" + userID)) {
            tv_task_edit_actualstart.setTextColor(getResources().getColor(R.color.gray_text, null));
            tv_actualstart_content.setTextColor(getResources().getColor(R.color.gray_text, null));
        }

        //不是责任人，没有权限的情况下，实际结束时间字体变灰色
        if (!synergyTaskEntity.getUserIds().contains("" + userID)) {
            tv_task_edit_actualend.setTextColor(getResources().getColor(R.color.gray_text, null));
            tv_actualend_content.setTextColor(getResources().getColor(R.color.gray_text, null));
        }

        //不是责任人，没有权限的情况下，当前实际量字体变灰色
        if (synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) {
            et_currentActualQuantity.setEnabled(true);
        } else {
            tv_task_edit_current_actual_quantity.setTextColor(getResources().getColor(R.color.gray_text, null));
            et_currentActualQuantity.setEnabled(false);
        }

        //不是责任人、创建人，没有权限的情况下，模型量字体变灰色
        if ((synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) ||
                synergyTaskEntity.getCreatedBy() == userID) {
            et_modelQuantity.setEnabled(true);
        } else {
            tv_task_edit_model_quantity.setTextColor(getResources().getColor(R.color.gray_text, null));
            et_modelQuantity.setEnabled(false);
        }

        //不是创建人，没有权限的情况下，计划劳动力字体变灰色
        if (synergyTaskEntity.getCreatedBy() != userID) {
            tv_task_edit_planned_labor_force.setTextColor(getResources().getColor(R.color.gray_text, null));
            et_plannedLaborForce.setEnabled(false);
        }

        //不是创建人，没有权限的情况下，责任人字体变灰色，不可点击
        if (synergyTaskEntity.getCreatedBy() != userID) {
            tv_task_edit_responsible_person.setTextColor(getResources().getColor(R.color.gray_text, null));
            responsible_person.setEnabled(false);
        }

        //不是责任人、创建人，没有权限的情况下，相关人员字体变灰色
        if ((synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) ||
                synergyTaskEntity.getCreatedBy() == userID) {
            relatede_person.setEnabled(true);
        } else {
            tv_task_edit_relatede_person.setTextColor(getResources().getColor(R.color.gray_text, null));
            relatede_person.setEnabled(false);
        }

        //不是责任人、创建人，没有权限的情况下，备注体变灰色
        if ((synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) ||
                synergyTaskEntity.getCreatedBy() == userID) {
            et_remark.setEnabled(true);
        } else {
            tv_task_edit_remark.setTextColor(getResources().getColor(R.color.gray_text, null));
            et_remark.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_left_btn://返回
                finish();
                break;

            case R.id.toolbar_right_tv://提交
                edit();
                break;

            case R.id.rl_task_edit_plantostart://计划开始时间

                DatePickerUtil.showDatePickerDialog(TaskEditActivity.this, tv_plantostart, Calendar.getInstance());
                break;

            case R.id.rl_task_edit_endofplan://计划结束时间
                //权限，只有创建者可以改计划结束时间，责任人和相关人不可改
                if (synergyTaskEntity.getCreatedBy() == userID) {
                    DatePickerUtil.showDatePickerDialog(TaskEditActivity.this, tv_endofplan, Calendar.getInstance());
                }
                break;

            case R.id.rl_task_edit_actualstartdate://实际开始时间
                //权限，责任人可改
                if (synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) {
                    DatePickerUtil.showDatePickerDialog(TaskEditActivity.this, tv_actualstart_content, Calendar.getInstance());
                }
                break;

            case R.id.rl_task_edit_actualfinishdate://实际结束时间
                //权限，责任人可改
                if (synergyTaskEntity.getUserIds().contains("" + userID) && synergyTaskEntity.getUserIds().contains(userName)) {
                    DatePickerUtil.showDatePickerDialog(TaskEditActivity.this, tv_actualend_content, Calendar.getInstance());
                }
                break;

            case R.id.rl_task_edit_responsible_person://责任人员

                Intent intent1 = new Intent(TaskEditActivity.this, TaskEditMemberActivity.class);
                if (responsiblePerson != null) {
                    intent1.putExtra("nameStr", responsiblePerson);
                } else {
                    intent1.putExtra("nameStr", "");
                }
                intent1.putExtra("type", "responsiblePerson");
                startActivityForResult(intent1, 520);

                break;

            case R.id.rl_task_edit_relatede_person://相关人员

                Intent intent2 = new Intent(TaskEditActivity.this, TaskEditMemberActivity.class);
                if (relatedePerson != null) {
                    intent2.putExtra("nameStr", relatedePerson);
                } else {
                    intent2.putExtra("nameStr", "");
                }
                intent2.putExtra("type", "relatedePerson");
                startActivityForResult(intent2, 521);

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 520:
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
                    responsible_person_content.setText(sb.toString());
                }
                break;

            case 521:
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
                    relatede_person_content.setText(sb.toString());
                }
                break;

            default:
        }
    }

    //编辑
    private void edit() {

        if (et_taskName.getText().toString().isEmpty()) {
            ToastUtils.showShort("请输入任务名");
        } else {
            FormBody formBody = new FormBody.Builder()
                    .add("Name", et_taskName.getText().toString())//任务名
                    .add("StartDate", tv_plantostart.getText().toString().isEmpty() ? "暂无" : tv_plantostart.getText().toString())//计划开始时间
                    .add("FinishDate", tv_endofplan.getText().toString().isEmpty() ? "暂无" : tv_endofplan.getText().toString())//计划结束时间
                    .add("ActualStartDate", tv_actualstart_content.getText().toString().isEmpty() ? "暂无" : tv_actualstart_content.getText().toString())//实际开始时间
                    .add("ActualFinishDate", tv_actualend_content.getText().toString().isEmpty() ? "暂无" : tv_actualend_content.getText().toString())//实际结束时间
                    .add("UpdatedBy", "" + SPUtils.getInstance().getInt("UserID"))//修改人
                    .add("ActualUnit", et_currentActualQuantity.getText().toString())//当前实际量
                    .add("ModelUnit", et_modelQuantity.getText().toString())//模型量
                    .add("PlanLabor", et_plannedLaborForce.getText().toString())//计划劳动力
                    .add("PracticalLaborSum", actual_labor_force_content.getText().toString())//实际劳动力
                    .add("UserIds", responsiblePerson)//责任人员
                    .add("RelativeUserIds", relatedePerson)//相关人员
                    .add("Remark", et_remark.getText().toString())//备注
                    .build();

            Request.Builder builder = new Request.Builder().
                    url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + synergyTaskEntity.getID())
                    .put(formBody);
            Request request = builder.build();

            MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, final Response response) {

                    if (response.code() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("修改成功");
                                EventBus.getDefault().post(new CommonEven("任务详细修改刷新"));//发送消息，刷新TaskDetailActivity
                                finish();
                            }
                        });
                    } else {
                        ToastUtils.showShort("修改失败");
                    }
                }
            });
        }
    }

}
