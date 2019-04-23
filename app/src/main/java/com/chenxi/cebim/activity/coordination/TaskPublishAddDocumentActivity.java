package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.chenxi.cebim.adapter.TaskAddDocumentAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.IsShowDocumentSureBtn;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.FileMoveActivityCollector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TaskPublishAddDocumentActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsNull;

    private int projectID;

    private TaskAddDocumentAdapter adapter;
    private ImageView back;
    private TextView cancle, bottomSure;

    private String dirName;
    private int classID;
    String documentFileString;
    String from;


    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<TbFileShowmodel> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_publish_add_document);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        Intent intent = getIntent();
        from=intent.getStringExtra("from");
        documentFileString = intent.getStringExtra("documentFileString");

        //获取从AddDocumentAdapter传来的文件夹数据。
        classID = intent.getIntExtra("classID", -1);
        dirName = intent.getStringExtra("dirName");

        FileMoveActivityCollector.addActivity(this);
        initView();
        initData();
    }

    //控件初始化
    private void initView() {
        swipeRefresh = findViewById(R.id.task_publish_document_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        recyclerView = findViewById(R.id.task_publish_document_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listIsNull = findViewById(R.id.rl_task_publish_document_list_is_null);

        back = findViewById(R.id.toolbar_left_btn);//返回按钮
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cancle = findViewById(R.id.toolbar_right_tv);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSure.setVisibility(View.GONE);//确认按钮隐藏
                cancle.setVisibility(View.GONE);//取消按钮隐藏
                finish();
            }
        });

        //底部确认按钮
        bottomSure = findViewById(R.id.tv_task_publish_bottom_sure);
        bottomSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //用EvenBus发数据回PublishTaskActivity
                EventBus.getDefault().post(new CommonEven("附件字符串:"+adapter.getDocumentString()));
                FileMoveActivityCollector.finishAll();
            }
        });
    }

    //数据初始化
    private void initData() {
        projectID = SPUtils.getInstance().getInt("projectID");//获取projectID
        swipeRefresh.setRefreshing(true);
        getData(0, 0, false);

    }

    /**
     * 获取列表数据源,获取文件夹数据
     *
     * @param isRefresh    值为1时刷新加载，值为0时进入界面时默认加载
     * @param isCheck      值为1时可选择，值为0时不可选择
     * @param isAllChoosed 是否全选
     */
    private void getData(final int isRefresh, final int isCheck, final Boolean isAllChoosed) {//isRefresh是否为刷新，isChoose是否为选择模式，1为是，0为否

        Request dirRequest;
        if (classID == -1 && dirName == null) {//根目录下的
            dirRequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class?where=ParentClassID=null and Recycle=false")
                    .build();
        } else {//子目录下的
            dirRequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class?where=ParentClassID=" + classID + " and Recycle=false")
                    .build();
        }

        MyApplication.getOkHttpClient().newCall(dirRequest).enqueue(new Callback() {
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

                        showList.clear();
                        tempList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int FID = -1;
                            int ProjectID = jsonObject.getInt("ProjectID");

                            int ClassID;
                            if (jsonObject.get("ClassID").toString().equals("null")) {
                                ClassID = 0;
                            } else {
                                ClassID = jsonObject.getInt("ClassID");
                            }

                            int ParentClassID = 0;

                            int OperationUserID;
                            Object OperationUserIDObject = jsonObject.get("OperationUserID");
                            if (!OperationUserIDObject.toString().equals("null")) {
                                OperationUserID = jsonObject.getInt("OperationUserID");
                            } else {
                                OperationUserID = -1;
                            }

                            String FileName = jsonObject.getString("ClassName");
                            String FileType = "dir";
                            String FileID = "";

                            Object AddTime = jsonObject.get("AddTime");
                            Object UpdateTime = jsonObject.get("UpdateTime");

                            Boolean IsCheck = false;
                            Boolean IsMove = false;

                            TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                    FileType, FileID, AddTime, UpdateTime, IsCheck, IsMove);

                            //数据源
                            tempList.add(tbFileShowmodel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //获取文件数据
                                getFileData(isRefresh);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    /**
     * 获取列表数据源，获取文件数据
     *
     * @param isRefresh 值为1时刷新加载，值为0时进入界面时默认加载
     */
    private void getFileData(final int isRefresh) {

        Request fileRrequest;
        if (classID == -1 && dirName == null) {//根目录下
            fileRrequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=ClassID=null and Recycle=false")
                    .build();
        } else {//子目录下
            fileRrequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=ClassID=" + classID + " and Recycle=false")
                    .build();
        }

        MyApplication.getOkHttpClient().newCall(fileRrequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int FID = jsonObject.getInt("FID");
                        int ProjectID = jsonObject.getInt("ProjectID");

                        int ClassID;
                        if (jsonObject.get("ClassID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("ClassID");
                        }

                        int ParentClassID = -100;
                        int OperationUserID = jsonObject.getInt("OperationUserID");

                        String FileName = jsonObject.getString("FileName");
                        String FileType = "file";
                        String FileID = jsonObject.getString("FileID");

                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");

                        Boolean IsCheck;
                        if (documentFileString.contains("" + FID)) {
                            IsCheck = true;
                        } else {
                            IsCheck = false;//默认为未选中
                        }

                        Boolean IsMove = false;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType, FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        //数据源
                        tempList.add(tbFileShowmodel);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (tempList.size() == 0 || tempList == null) {
                                listIsNull.setVisibility(View.VISIBLE);
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            } else {
                                listIsNull.setVisibility(View.GONE);
                                showList.addAll(tempList);
                                if (isRefresh == 0) {
                                    adapter = new TaskAddDocumentAdapter(MyApplication.getContext(), showList, projectID, documentFileString, from);
                                    adapter.isShowCheckBox(true);//显示选择框
                                    recyclerView.setAdapter(adapter);

                                    if (adapter.getDocumentString()!=null&&adapter.getDocumentString().equals("")) {
                                        cancle.setVisibility(View.VISIBLE);
                                        bottomSure.setVisibility(View.VISIBLE);
                                    } else {
                                        cancle.setVisibility(View.GONE);
                                        bottomSure.setVisibility(View.GONE);
                                    }

                                    //如果刷新图标还在刷新，则取消
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else if (isRefresh == 1) {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (showList != null && showList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (showList != null && showList.size() > 0 && adapter == null) {
                                        adapter = new TaskAddDocumentAdapter(TaskPublishAddDocumentActivity.this, showList, projectID, documentFileString, from);
                                        adapter.isShowCheckBox(true);//显示选择框
                                        recyclerView.addItemDecoration(new DividerItemDecoration(TaskPublishAddDocumentActivity.this, DividerItemDecoration.VERTICAL));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    }
                                }
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("数据解析出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    /**
     * 刷新列表
     */
    private void refreshFile(final Boolean isPull) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //如果是下拉刷新，睡两秒，否则直接刷新列表
                if (isPull) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        getData(1, 0, false);//注释掉了，这个地方刷新会影响已选中的数据，而且，此处刷新没什么用处

                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                });
            }
        }).start();
    }

    //EventBus 打开底部导航栏（分享、打开、常用、更多）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowDocumentSureBtn isShowDocumentSureBtn) {
        if (isShowDocumentSureBtn.getInfo().equals("有选中Item")) {
            cancle.setVisibility(View.VISIBLE);
            bottomSure.setVisibility(View.VISIBLE);
        } else if (isShowDocumentSureBtn.getInfo().equals("无选中Item")) {
            cancle.setVisibility(View.GONE);
            bottomSure.setVisibility(View.GONE);
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