package com.chenxi.cebim.activity.engineeringNews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.ProjectNewsAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.EngineeringNewsModel;
import com.chenxi.cebim.entity.EngineeringNewsRefreshEvenModel;
import com.chenxi.cebim.entity.ProjectNews;

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
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EngineeringNewsActivity extends BaseActivity implements View.OnClickListener {

    private ImageView back, publish;
    private LinearLayout ll_reply;
    private RelativeLayout rl_reply;
    private EditText et_reply;
    private TextView sent;
    private View view_reply;
    private List<ProjectNews> projectNewsList = new ArrayList<>();
    private List<EngineeringNewsModel> engineeringNewsList = new ArrayList<>();
    private List<EngineeringNewsModel> tempList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;

    private ProjectNewsAdapter adapter;

    String parentID;

    Dialog picOrVideoDialog;

    private int delParentPosition;
    private int sonPosition;
    private String sonMomentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineering_news);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        initView();

        swipeRefresh.setRefreshing(true);
        initProjectNews(false);

    }

    private void initView() {
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        publish = findViewById(R.id.toolbar_second_right_iv);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EngineeringNewsActivity.this, ProjectPublishActivity.class);
                startActivity(intent);
            }
        });

        sent = findViewById(R.id.tv_sent_reply);
        sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_reply != null && et_reply.length() > 0) {
                    //发送回复
                    reply();
                }
            }
        });

        ll_reply = findViewById(R.id.ll_reply);
        rl_reply = findViewById(R.id.rl_reply);
        et_reply = findViewById(R.id.et_reply);
        et_reply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    sent.setBackgroundResource(R.drawable.shape_discuss_light);
                    sent.setClickable(true);
                } else {
                    sent.setBackgroundResource(R.drawable.shape_discuss);
                    sent.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view_reply = findViewById(R.id.view_reply);
        view_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_reply.setVisibility(View.GONE);
            }
        });

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = findViewById(R.id.engineering_news_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initProjectNews(true);
            }
        });

        //RecyclerView逻辑
        recyclerView = findViewById(R.id.rv_project_news);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    //发送回复
    private void reply() {
        String str = et_reply.getText().toString();
        RequestBody requestBody = new FormBody.Builder()
                .add("Contens", str)//内容
                .add("ParentID", parentID)//ParentID
                .build();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic")
                .post(requestBody)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("回复失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("回复成功");

                            initProjectNews(true);//刷新列表
                            rl_reply.setVisibility(View.GONE);
                            et_reply.setText("");

                            //关闭软键盘
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(et_reply, InputMethodManager.SHOW_FORCED);
                            imm.hideSoftInputFromWindow(et_reply.getWindowToken(), 0); //强制隐藏键盘
                        }
                    });

                } else {
                    ToastUtils.showShort("回复失败");
                }
            }
        });

    }

    private void initProjectNews(boolean isRefresh) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic")
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
                        tempList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String MomentID = jsonObject.getString("MomentID");
                            String Contens = jsonObject.getString("Contens");
                            String ModelID = jsonObject.getString("ModelID");
                            String Location = jsonObject.getString("Location");
                            String Picture = jsonObject.getString("Picture");
                            String Video = jsonObject.getString("Video");
                            String Voice = jsonObject.getString("Voice");
                            String Likes = jsonObject.getString("Likes");

                            Object CreateAt = jsonObject.get("CreateAt");
                            Object UpdataAt = jsonObject.get("UpdataAt");

                            int ProjectID = jsonObject.getInt("ProjectID");
                            String ParentID = jsonObject.getString("ParentID");

                            com.alibaba.fastjson.JSONObject createbyInfo = com.alibaba.fastjson.JSONObject.
                                    parseObject(jsonObject.get("CreatebyInfo").toString());

                            String CreateByUserName;
                            int CreateByUserID;
                            if (createbyInfo == null) {
                                CreateByUserName = "";
                                CreateByUserID = -1;
                            } else {
                                CreateByUserName = createbyInfo.get("UserName").toString();

                                CreateByUserID = Integer.parseInt(createbyInfo.get("UserID").toString());
                            }

                            String UpdataByUserName = null;
                            int UpdataByUserID = -1;
                            if (!jsonObject.get("UpdataByInfo").toString().equals("null")) {
                                com.alibaba.fastjson.JSONObject updataByInfo = com.alibaba.fastjson.JSONObject.
                                        parseObject(jsonObject.get("UpdataByInfo").toString());
                                UpdataByUserName = updataByInfo.get("UserName").toString();
                                UpdataByUserID = Integer.parseInt(updataByInfo.get("UserID").toString());
                            }

                            int CreateBy;
                            if (jsonObject.get("CreateBy").toString().equals("null")) {
                                CreateBy = -1;
                            } else {
                                CreateBy = jsonObject.getInt("CreateBy");
                            }

                            int UpdataBy;
                            if (jsonObject.get("UpdataBy").toString().equals("null")) {
                                UpdataBy = -1;
                            } else {
                                UpdataBy = jsonObject.getInt("UpdataBy");
                            }

                            String Son = jsonObject.get("Son").toString();

                            EngineeringNewsModel engineeringNewsModel = new EngineeringNewsModel(MomentID, Contens, ModelID,
                                    Location, Picture, Video, Voice, Likes, CreateByUserName, UpdataByUserName, Son, CreateAt, UpdataAt,
                                    ProjectID, ParentID, CreateBy, UpdataBy, CreateByUserID, UpdataByUserID);
                            tempList.add(engineeringNewsModel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                engineeringNewsList.clear();
                                engineeringNewsList.addAll(tempList);
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                                if (isRefresh) {
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter = new ProjectNewsAdapter(EngineeringNewsActivity.this, engineeringNewsList);
                                    ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(EngineeringNewsActivity.this,
                                            DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("工程动态数据解析出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("工程动态数据请求出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }

                    }
                } else {
                    ToastUtils.showShort("工程动态数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });


        String tiem = TimeUtils.getNowString();
        for (int i = 0; i < 2; i++) {
            ProjectNews projectNews1 = new ProjectNews(BitmapFactory.decodeFile("R.mipmap.ic_launcher_round"), "福建晨曦1", R.drawable.del,
                    "主筋下料", BitmapFactory.decodeFile("R.mipmap.ic_launcher"), BitmapFactory.decodeFile("R.mipmap.ic_launcher"), BitmapFactory.decodeFile("R.mipmap.ic_launcher"),
                    "2010", R.drawable.like_dark, R.drawable.discuss, R.drawable.like_dark, "小明");
            projectNewsList.add(projectNews1);

        }
    }

    //EventBus 刷新列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EngineeringNewsRefreshEvenModel engineeringNewsRefreshEvenModel) {
        if (engineeringNewsRefreshEvenModel.getInfo().equals("刷新")) {
            initProjectNews(true);
        } else if (engineeringNewsRefreshEvenModel.getInfo().contains("评论界面")) {

            parentID = engineeringNewsRefreshEvenModel.getInfo().split(":")[1];
            if (rl_reply.getVisibility() == View.VISIBLE) {
                rl_reply.setVisibility(View.GONE);
            } else if (rl_reply.getVisibility() == View.GONE) {
                rl_reply.setVisibility(View.VISIBLE);
            }
        } else if (engineeringNewsRefreshEvenModel.getInfo().contains("删除评论")) {
            sonPosition = Integer.parseInt(engineeringNewsRefreshEvenModel.getInfo().split(":")[1]);
            delParentPosition= Integer.parseInt(engineeringNewsRefreshEvenModel.getInfo().split(":")[2]);
            sonMomentID = engineeringNewsRefreshEvenModel.getInfo().split(":")[3];
            picOrVedioDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    //图片和视频选择
    private void picOrVedioDialog() {
        picOrVideoDialog = new Dialog(EngineeringNewsActivity.this, R.style.ActionSheetDialogStyle);
        View inflate = LayoutInflater.from(EngineeringNewsActivity.this).inflate(R.layout.bottom_dialog, null);
        Button getPhoto = (Button) inflate.findViewById(R.id.bt_photo);
        getPhoto.setText("删除");
        Button cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        getPhoto.setOnClickListener(this);
        cancel.setOnClickListener(this);

        picOrVideoDialog.setContentView(inflate);
        Window dialogWindow = picOrVideoDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.y = 20;
        dialogWindow.setAttributes(lp);
        picOrVideoDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_photo:
                delReply();
                picOrVideoDialog.dismiss();
                initProjectNews(false);
                break;

            case R.id.btn_cancel:
                picOrVideoDialog.dismiss();
                break;
        }

    }

    //删除回复
    private void delReply() {
        String str = AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic/" + sonMomentID;
        FormBody formBody = new FormBody.Builder().build();
        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic/" + sonMomentID)
                .delete(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
            }

            @Override
            public void onResponse(Call call, final Response response) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                ToastUtils.showShort("item删除成功");

                                String responseData = response.body().string();
                                JSONObject jsonObject = new JSONObject(responseData);

                                String MomentID = jsonObject.getString("MomentID");
                                String Contens = jsonObject.getString("Contens");
                                String ModelID = jsonObject.getString("ModelID");
                                String Location = jsonObject.getString("Location");
                                String Picture = jsonObject.getString("Picture");
                                String Video = jsonObject.getString("Video");
                                String Voice = jsonObject.getString("Voice");
                                String Likes = jsonObject.getString("Likes");

                                Object CreateAt = jsonObject.get("CreateAt");
                                Object UpdataAt = jsonObject.get("UpdataAt");

                                int ProjectID = jsonObject.getInt("ProjectID");
                                String ParentID = jsonObject.getString("ParentID");

//                                com.alibaba.fastjson.JSONObject createbyInfo = com.alibaba.fastjson.JSONObject.
//                                        parseObject(jsonObject.get("CreatebyInfo").toString());
//
//                                String CreateByUserName;
//                                int CreateByUserID;
//                                if (createbyInfo == null) {
                                String CreateByUserName = "";
                                int CreateByUserID = -1;
//                                } else {
//                                    CreateByUserName = createbyInfo.get("UserName").toString();
//
//                                    CreateByUserID = Integer.parseInt(createbyInfo.get("UserID").toString());
//                                }

                                String UpdataByUserName = "";
                                int UpdataByUserID = -1;
//                                if (!jsonObject.get("UpdataByInfo").toString().equals("null")) {
//                                    com.alibaba.fastjson.JSONObject updataByInfo = com.alibaba.fastjson.JSONObject.
//                                            parseObject(jsonObject.get("UpdataByInfo").toString());
//                                    UpdataByUserName = updataByInfo.get("UserName").toString();
//                                    UpdataByUserID = Integer.parseInt(updataByInfo.get("UserID").toString());
//                                }

                                int CreateBy;
                                if (jsonObject.get("CreateBy").toString().equals("null")) {
                                    CreateBy = -1;
                                } else {
                                    CreateBy = jsonObject.getInt("CreateBy");
                                }

                                int UpdataBy;
                                if (jsonObject.get("UpdataBy").toString().equals("null")) {
                                    UpdataBy = -1;
                                } else {
                                    UpdataBy = jsonObject.getInt("UpdataBy");
                                }

//                                String Son = jsonObject.get("Son").toString();
                                String Son="";

                                EngineeringNewsModel engineeringNewsModel = new EngineeringNewsModel(MomentID, Contens, ModelID,
                                        Location, Picture, Video, Voice, Likes, CreateByUserName, UpdataByUserName, Son, CreateAt, UpdataAt,
                                        ProjectID, ParentID, CreateBy, UpdataBy, CreateByUserID, UpdataByUserID);

                                tempList.set(delParentPosition,engineeringNewsModel);
                                adapter.refresh(delParentPosition, tempList);

                            } else {
                                ToastUtils.showShort("item删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("item删除失败");
                        }
                    }
                });
            }
        });
    }
}
