package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.QuestionAtAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.AtMembersModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TaskPublishMemberActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ImageView back;
    private TextView sure;
    QuestionAtAdapter questionAtAdapter;
    ArrayList<AtMembersModel> list = new ArrayList<>();
    ArrayList<AtMembersModel> showList = new ArrayList<>();

    String nameStr;
    String type;//从哪里床过来的数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_publish_member);
        Intent intent = getIntent();
        nameStr = intent.getStringExtra("nameStr");
        type = intent.getStringExtra("type");//接受传递过来数据的类名

        initView();
        initData(0);
    }

    private void initData(final int isRefresh) {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectMember")
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
                            int ProjectID = jsonObject.getInt("ProjectID");
                            int UserID = jsonObject.getInt("UserID");
                            String RoleID = jsonObject.getString("RoleID");

                            Object InTime = jsonObject.get("InTime");

                            int AddUserID;
                            if (jsonObject.get("AddUserID").toString().equals("null")) {
                                AddUserID = -1;
                            } else {
                                AddUserID = jsonObject.getInt("AddUserID");
                            }

                            Object AddTime = jsonObject.get("AddTime");

                            int UpdateUserID;
                            if (jsonObject.get("UpdateUserID").toString().equals("null")) {
                                UpdateUserID = -1;
                            } else {
                                UpdateUserID = jsonObject.getInt("UpdateUserID");
                            }

                            Object UpdateTime = jsonObject.get("UpdateTime");

                            //解析UserInfo
                            com.alibaba.fastjson.JSONObject userInfoJson = com.alibaba.fastjson.JSONObject.
                                    parseObject(jsonObject.get("UserInfo").toString());
                            String UserName = userInfoJson.get("UserName").toString();

                            int UserInfoID;
                            if (userInfoJson.get("UserID").toString().equals("null")) {
                                UserInfoID = -1;
                            } else {
                                UserInfoID = (int) userInfoJson.get("UserID");
                            }

                            com.alibaba.fastjson.JSONObject roleInfoJson = com.alibaba.fastjson.JSONObject.
                                    parseObject(jsonObject.get("RoleInfo").toString());
                            String RoleName = "";
                            String RoleInfoID = "";
                            //解析RoleInfo
                            if (!jsonObject.get("RoleInfo").toString().equals("null")) {
                                RoleName = roleInfoJson.get("Name").toString();//要非空判断
                                RoleInfoID = roleInfoJson.get("ID").toString();
                            }

                            boolean isChecked;
                            if (nameStr != null && nameStr != "" && nameStr.contains(UserName)) {
                                isChecked = true;
                            } else {
                                isChecked = false;
                            }

                            AtMembersModel atMembersModel = new AtMembersModel(ProjectID, UserID, AddUserID, UpdateUserID, RoleID,
                                    InTime, AddTime, UpdateTime, UserInfoID, UserName, RoleInfoID, RoleName, isChecked);
                            list.add(atMembersModel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh == 0) {
                                    questionAtAdapter = new QuestionAtAdapter(list);
                                    recyclerView.setAdapter(questionAtAdapter);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });

    }

    private void initView() {
        recyclerView = findViewById(R.id.seting_member_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(TaskPublishMemberActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sure = findViewById(R.id.toolbar_right_tv);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = questionAtAdapter.getList().size();
                showList.clear();
                StringBuffer sb = new StringBuffer();
                boolean hasChecked = false;
                for (int i = 0; i < size; i++) {
                    if (questionAtAdapter.getList().get(i).isChecked()) {
                        showList.add(questionAtAdapter.getList().get(i));
                        sb.append(questionAtAdapter.getList().get(i).getUserName());
                        sb.append(":");
                        sb.append(questionAtAdapter.getList().get(i).getUserID());
                        sb.append(",");
                        hasChecked = true;
                    }
                }

                if (hasChecked) {

                    String atName = sb.substring(0, sb.length() - 1).toString();

                    if (type.equals("responsiblePerson")) {
                        Intent intent = new Intent();
                        intent.putExtra("responsiblePersonReturnData", atName);
                        setResult(RESULT_OK, intent);
                    } else if (type.equals("relatedePerson")) {
                        Intent intent = new Intent();
                        intent.putExtra("relatedePersonReturnData", atName);
                        setResult(RESULT_OK, intent);
                    }

                    finish();
                } else {
                    ToastUtils.showShort("没有选中的成员，请添加成员");
                }
            }
        });
    }

}
