package com.chenxi.cebim.activity.data;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.DataActivity;
import com.chenxi.cebim.adapter.DataFileAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.DirActivityCollector;
import com.chenxi.cebim.utils.FileMoveActivityCollector;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.params.InputParams;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;

import org.greenrobot.eventbus.EventBus;
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
import okhttp3.Response;

public class FileMoveActivity extends AppCompatActivity implements View.OnClickListener {

    private List<TbFileShowmodel> moveFileList = new ArrayList<>();//接收从DataFileFragment和DirActivity中传过来的需要移动的文件夹

    private List<TbFileShowmodel> tempList = new ArrayList<>();//中转集合

    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//用于本界面展示列表的list

    private int projectID;
    private String projectName;
    private int classId, parentClassId;//从DataFileAdapter传回的数据
    private String dirName;//从DataFileAdapter传回的数据

    private RelativeLayout back;
    private TextView title, previous, newDir, move;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;

    private DataFileAdapter adapter;

    private int moveNum = 0;//需要移动的文件和文件夹数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileMoveActivityCollector.addActivity(this);
        setContentView(com.chenxi.cebim.R.layout.activity_file_move);

        initView();
        initData();
    }

    /**
     * 数据初始化
     */
    private void initData() {

        projectID = SPUtils.getInstance().getInt("projectID");
        projectName = SPUtils.getInstance().getString("projectName");

        Intent intent = getIntent();//接收从DataFileFragment、DirActivity、DataFileAdapter传递的数据
        String skipType = intent.getStringExtra("skipType");
        if (skipType != null && skipType.equals("外部跳入")) {

            String moveFileListString = intent.getStringExtra("moveFileList");
            SPUtils.getInstance().put("moveFileList", moveFileListString);//持久化moveFileList

            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(moveFileListString);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int FID = jsonObject.getInt("fID");
                    int ProjectID = jsonObject.getInt("projectID");

                    int ClassID;
                    if (jsonObject.get("classID").toString().equals("null")) {
                        ClassID = 0;
                    } else {
                        ClassID = jsonObject.getInt("classID");
                    }

                    int ParentClassID = jsonObject.getInt("parentClassID");

                    int OperationUserID;
                    Object OperationUserIDObject = jsonObject.get("operationUserID");
                    if (!OperationUserIDObject.toString().equals("null")) {
                        OperationUserID = jsonObject.getInt("operationUserID");
                    } else {
                        OperationUserID = -1;
                    }

                    String FileName = jsonObject.getString("fileName");
                    String FileType = jsonObject.getString("fileType");
                    String FileID=jsonObject.getString("fileID");

                    Object AddTime = jsonObject.get("addTime");
                    Object UpdateTime = jsonObject.get("updateTime");

                    Boolean IsCheck = true;
                    Boolean IsMove = true;

                    TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                            FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                    //数据源
                    moveFileList.add(tbFileShowmodel);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            String dirListString = SPUtils.getInstance().getString("rootDirListString");//获取根目录下的文件夹列表字符串
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(dirListString);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int FID = jsonObject.getInt("fID");
                    int ProjectID = jsonObject.getInt("projectID");

                    int ClassID;
                    if (jsonObject.get("classID").toString().equals("null")) {
                        ClassID = 0;
                    } else {
                        ClassID = jsonObject.getInt("classID");
                    }

                    int ParentClassID = jsonObject.getInt("parentClassID");

                    int OperationUserID;
                    Object OperationUserIDObject = jsonObject.get("operationUserID");
                    if (!OperationUserIDObject.toString().equals("null")) {
                        OperationUserID = jsonObject.getInt("operationUserID");
                    } else {
                        OperationUserID = -1;
                    }

                    String FileName = jsonObject.getString("fileName");
                    String FileType = jsonObject.getString("fileType");
                    String FileID=jsonObject.getString("fileID");;

                    Object AddTime = jsonObject.get("addTime");
                    Object UpdateTime = jsonObject.get("updateTime");

                    Boolean IsCheck = false;
                    Boolean IsMove = true;

                    TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                            FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                    //数据源，用于显示的文件夹列表
                    tempList.add(tbFileShowmodel);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            title.setText("根目录");
            move.setText("移动" + moveFileList.size());
            showList.addAll(tempList);
            adapter = new DataFileAdapter(MyApplication.getContext(), showList, projectID);
            recyclerView.setAdapter(adapter);

        } else {//从FileMoveActivity跳转自身时接收数据
            classId = intent.getIntExtra("classID", -1);
            parentClassId = intent.getIntExtra("parentClassId", -1);
            dirName = intent.getStringExtra("dirName");
            title.setText(dirName);
            getDirList(false);//初始化数据
        }

    }

    /**
     * 界面初始化
     */
    private void initView() {

        back = (RelativeLayout) findViewById(R.id.rl_move_file_back);
        back.setOnClickListener(this);

        title = (TextView) findViewById(R.id.move_file_title);
        previous = (TextView) findViewById(R.id.tv_move_file_pevious);
        previous.setOnClickListener(this);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.remove_file_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.remove_file_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(FileMoveActivity.this);
        recyclerView.setLayoutManager(layoutManager);


        newDir = (TextView) findViewById(R.id.tv_move_newdir);
        newDir.setOnClickListener(this);
        move = (TextView) findViewById(R.id.tv_move_num);
        move.setOnClickListener(this);

    }

    private void getDirList(Boolean isRefresh) {

        String dirListString = SPUtils.getInstance().getString("subdirListString");//获取从DirActivity中传过来的dirListString
        int subDirClassID = SPUtils.getInstance().getInt("subdirClassID", -1);//获取从DirActivity中传过来的父文件夹classID

        //从DirActivity中传过来的父文件夹subDirClassID和全局的classId对比，如果相等，则说明本次要显示的是DirActivity中传过来的父文件夹subDirClassID下面的文件
        if (subDirClassID != -1 && subDirClassID == classId) {
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(dirListString);

                showList.clear();
                tempList.clear();

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int FID = jsonObject.getInt("fID");
                    int ProjectID = jsonObject.getInt("projectID");

                    int ClassID;
                    if (jsonObject.get("classID").toString().equals("null")) {
                        ClassID = 0;
                    } else {
                        ClassID = jsonObject.getInt("classID");
                    }

                    int ParentClassID = jsonObject.getInt("parentClassID");

                    int OperationUserID;
                    Object OperationUserIDObject = jsonObject.get("operationUserID");
                    if (!OperationUserIDObject.toString().equals("null")) {
                        OperationUserID = jsonObject.getInt("operationUserID");
                    } else {
                        OperationUserID = -1;
                    }

                    String FileName = jsonObject.getString("fileName");
                    String FileType = jsonObject.getString("fileType");
                    String FileID=jsonObject.getString("fileID");

                    Object AddTime = jsonObject.get("addTime");
                    Object UpdateTime = jsonObject.get("updateTime");

                    Boolean IsCheck = false;
                    Boolean IsMove = true;

                    Boolean isTheSame = false;

                    TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                            FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                    //数据源
                    tempList.add(tbFileShowmodel);
                }

                //获取文件数据
                if (tempList.size() != 0) {
                    showList.addAll(tempList);
                    if (isRefresh) {
                        if (showList != null && showList.size() > 0 && adapter != null) {
                            adapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                        }
                    } else {
                        if (showList != null || showList.size() > 0) {
                            adapter = new DataFileAdapter(FileMoveActivity.this, showList, projectID);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }
//                else{
//                    getDirData(isRefresh);//若传过来的list的size为0说明DirActivity中选中了全部的文件夹
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            getDirData(isRefresh);
        }
    }

    //远程请求文件夹数据
    private void getDirData(final Boolean isRefresh) {

        //请求文件夹
        Request dirRequest;
        if(classId==0){
            dirRequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class?where=ParentClassID=null " + "and Recycle=false")
                    .build();
        }else{
            dirRequest = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class?where=ParentClassID=" + classId + "and Recycle=false")
                    .build();
        }

        MyApplication.getOkHttpClient().newCall(dirRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {

                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);

                    showList.clear();

                    if(classId!=0){
                        tempList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int FID = -1;
                        int ProjectID = jsonObject.getInt("ProjectID");
                        int ClassID = jsonObject.getInt("ClassID");

                        int ParentClassID;
                        Object ParentClassIDObject=jsonObject.get("ParentClassID");
                        if(!ParentClassIDObject.toString().equals("null")){
                            ParentClassID= jsonObject.getInt("ParentClassID");
                        }else{
                            ParentClassID=0;
                        }

                        int OperationUserID;
                        Object OperationUserIDObject = jsonObject.get("OperationUserID");
                        if (!OperationUserIDObject.toString().equals("null")) {
                            OperationUserID = jsonObject.getInt("OperationUserID");
                        } else {
                            OperationUserID = -1;
                        }

                        String FileName = jsonObject.getString("ClassName");
                        String FileType = "dir";
                        String FileID="";

                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");

                        Boolean IsCheck = false;
                        Boolean IsMove = true;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);
                        //数据源
                        tempList.add(tbFileShowmodel);

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //获取文件数据
                            if (tempList.size() != 0) {
                                showList.addAll(tempList);
                                if (isRefresh) {
                                    if (showList != null && showList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        swipeRefresh.setRefreshing(false);
                                    }else{
                                        adapter = new DataFileAdapter(FileMoveActivity.this, showList, projectID);
                                        recyclerView.setAdapter(adapter);
                                    }
                                } else {
                                    if (showList != null || showList.size() > 0) {
                                        adapter = new DataFileAdapter(FileMoveActivity.this, showList, projectID);
                                        recyclerView.setAdapter(adapter);
                                    }
                                }
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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
                        getDirList(true);
                    }
                });
            }
        }).start();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_move_file_back://返回
                FileMoveActivityCollector.finishAll();
                break;

            case R.id.tv_move_file_pevious://上一级
                finish();
                break;

            case R.id.tv_move_newdir://新建文件夹
                new CircleDialog.Builder(FileMoveActivity.this)
                        //添加标题，参考普通对话框
                        .setTitle("创建新文件夹")
                        .setInputHint("输入文件夹名")//提示
                        .setInputHeight(120)//输入框高度
                        .setNegative("取消", null)
                        .configInput(new ConfigInput() {
                            @Override
                            public void onConfig(InputParams params) {

                            }
                        }).setPositiveInput("确定", new OnInputClickListener() {
                    @Override
                    public void onClick(String text, View v) {
                        if (text.equals("") || text == null) {
                            ToastUtils.showShort("文件夹名不能为空");
                        } else {
                            createDir(text);
                        }
                    }
                }).show();
                break;

            case R.id.tv_move_num://移动文件

                String moveFileListString = SPUtils.getInstance().getString("moveFileList", null);

                try {
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(moveFileListString);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int FID = jsonObject.getInt("fID");
                        int ProjectID = jsonObject.getInt("projectID");

                        int ClassID;
                        if (jsonObject.get("classID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("classID");
                        }

                        int ParentClassID = jsonObject.getInt("parentClassID");

                        int OperationUserID;
                        Object OperationUserIDObject = jsonObject.get("operationUserID");
                        if (!OperationUserIDObject.toString().equals("null")) {
                            OperationUserID = jsonObject.getInt("operationUserID");
                        } else {
                            OperationUserID = -1;
                        }

                        String FileName = jsonObject.getString("fileName");
                        String FileType = jsonObject.getString("fileType");
                        String FileID=jsonObject.getString("fileID");

                        Object AddTime = jsonObject.get("addTime");
                        Object UpdateTime = jsonObject.get("updateTime");

                        Boolean IsCheck = true;
                        Boolean IsMove = true;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        //数据源
                        moveFileList.add(tbFileShowmodel);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(FileMoveActivity.this);

                if (dirName == null || dirName.equals("")) {
                    builder.setMessage("移动到根目录文件夹下?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            moveNum = moveFileList.size();
                            for (int i = 0; i < moveFileList.size(); i++) {
                                if (moveFileList.get(i).getFileType().equals("dir")) {
                                    moveDir(null, moveFileList.get(i).getClassID());
                                } else if (moveFileList.get(i).getFileType().equals("file")) {
                                    moveFile(null, moveFileList.get(i).getFID());
                                }
                            }
                        }
                    });

                } else {
                    builder.setMessage("移动到" + dirName + "文件夹下?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveNum = moveFileList.size();
                            for (int i = 0; i < moveFileList.size(); i++) {
                                if (moveFileList.get(i).getFileType().equals("dir")) {
                                    moveDir("" + classId, moveFileList.get(i).getClassID());
                                } else if (moveFileList.get(i).getFileType().equals("file")) {
                                    moveFile("" + classId, moveFileList.get(i).getFID());
                                }
                            }
                        }
                    });
                }

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();

                break;
        }

    }

    //移动文件
    private void moveFile(String ClassID, int fid) {

        FormBody formBody = new FormBody.Builder()
                .add("ClassID", "" + ClassID)
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid)
                .put(formBody);
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
                                moveNum--;
                            } else {
                                ToastUtils.showShort("文件移动失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件移动失败");
                        }

                        if (moveNum == 0) {
                            ToastUtils.showShort("文件移动成功");
                            FileMoveActivityCollector.finishAll();
                            DirActivityCollector.finishAll();

                            //此处本不应跳转。之前尝试用EventBus发送消息，调整DataActivity中的DataFileFragment界面，但传值失败，没查出原因，改用此方法
                            Intent intent = new Intent(FileMoveActivity.this, DataActivity.class);
                            intent.putExtra("projectName", projectName);
                            intent.putExtra("projectId", projectID);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }

    //移动文件夹
    private void moveDir(String ParentClassID, int classId) {

        FormBody formBody = new FormBody.Builder()
                .add("ParentClassID", "" + ParentClassID)
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class/" + classId)
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("文件夹移动失败");
            }

            @Override
            public void onResponse(Call call, final Response response) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                moveNum--;
                            } else {
                                ToastUtils.showShort("文件夹移动失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件夹移动失败");
                        }

                        if (moveNum == 0) {
                            ToastUtils.showShort("文件移动成功");
                            FileMoveActivityCollector.finishAll();
                            DirActivityCollector.finishAll();

                            //此处本不应跳转。之前尝试用EventBus发送消息，调整DataActivity中的DataFileFragment界面，但传值失败，没查出原因，改用此方法
                            Intent intent = new Intent(FileMoveActivity.this, DataActivity.class);
                            intent.putExtra("projectName", projectName);
                            intent.putExtra("projectId", projectID);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }

    //创建文件夹方法
    private void createDir(String dirName) {

        FormBody formBody;
        if (classId == 0) {
            formBody = new FormBody
                    .Builder()
                    .add("ClassName", dirName)
                    .add("ParentClassID", "" + null)
                    .build();
        } else {
            formBody = new FormBody
                    .Builder()
                    .add("ClassName", dirName)
                    .add("ParentClassID", "" + classId)
                    .build();
        }

        Request request = new Request
                .Builder()
                .post(formBody)
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    ToastUtils.showShort("文件夹创建成功");
                    if(classId==0){
                        EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//通知DataFileFragment刷新列表
                    }else{
                        EventBus.getDefault().post(new IsShowBottomSettingButton("刷新" + classId + "目录文件"));
                    }
                    refreshFile(false);
                } else {
                    ToastUtils.showShort("文件夹创建失败");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileMoveActivityCollector.removeActivity(this);//用于一次性返回到DataFileFragment界面
    }
}
