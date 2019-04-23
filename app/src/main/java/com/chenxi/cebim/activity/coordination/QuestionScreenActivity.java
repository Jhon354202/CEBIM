package com.chenxi.cebim.activity.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.QuestionAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.utils.DelUnderLine;

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

/**
 * 筛选
 */
public class QuestionScreenActivity extends BaseActivity {

    private int position;//QuestionFragment子fragment的position
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;
    private LinearLayout llSearch;
    private SearchView search;
    private ImageView back;

    private QuestionAdapter adapter;

    private List<QuestionModel> screenQuestionList = new ArrayList<>();//获取回调接口中返回的

    private ArrayList<QuestionModel> searchFileList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<QuestionModel> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_screen);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        initView();

        swipeRefresh.setRefreshing(true);
        if (position == 0) {
            //下载未完成列表
            downloadUncomplete(false);
        } else if (position == 1) {
            //下载已延期列表
            downloadDelay(false);
        } else if (position == 2) {
            //下载已完成列表
            downloadComplete(false);
        }

    }

    //下载未完成列表
    private void downloadUncomplete(final boolean isPull) {
        screenQuestionList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/"+ SPUtils.getInstance().getInt("projectID")+"/SynergyQuestion?where=State=false")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            Integer ProjectId = jsonObject.getInt("ProjectId");

                            Integer ClosedUserId;
                            if (jsonObject.get("ClosedUserId").toString().equals("null")) {
                                ClosedUserId = null;
                            } else {
                                ClosedUserId = jsonObject.getInt("GroupId");
                            }

                            Integer Priority;
                            if (jsonObject.get("Priority").toString().equals("null")) {
                                Priority = null;
                            } else {
                                Priority = jsonObject.getInt("Priority");
                            }

                            Integer UserId;
                            if (jsonObject.get("UserId").toString().equals("null")) {
                                UserId = null;
                            } else {
                                UserId = jsonObject.getInt("UserId");
                            }

                            Integer UpdatedBy;
                            if (jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = null;
                            } else {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            String ID = jsonObject.getString("ID");
                            String Title = jsonObject.getString("Title");
                            String Comment = jsonObject.getString("Comment");
                            String GroupId = jsonObject.getString("GroupId");
                            String Category = jsonObject.getString("Category");
                            String ViewportId = jsonObject.getString("ViewportId");
                            String SystemType = jsonObject.getString("SystemType");
                            String At = jsonObject.getString("At");
                            String Pictures = jsonObject.getString("Pictures");
                            String Uuids = jsonObject.getString("Uuids");
                            String SelectionSetIds = jsonObject.getString("SelectionSetIds");
                            String Video = jsonObject.getString("Video");
                            String Voice = jsonObject.getString("Voice");
                            String Tags = jsonObject.getString("Tags");
                            String ReadUsers = jsonObject.getString("ReadUsers");
                            String DocumentIds = jsonObject.getString("DocumentIds");

                            String UserName;
                            if(jsonObject.get("UserInfo").toString().equals("null")){
                                UserName=null;
                            }else{
                                UserName=new JSONObject(jsonObject.get("UserInfo").toString()).getString("UserName");
                            }

                            String CategoryName;
                            if(jsonObject.get("CategoryName").toString().equals("null")){
                                CategoryName=null;
                            }else{
                                CategoryName=new JSONObject(jsonObject.get("CategoryName").toString()).getString("Name");
                            }

                            String SystemTypeName;
                            if(jsonObject.get("SystemTypeName").toString().equals("null")){
                                SystemTypeName=null;
                            }else{
                                SystemTypeName=new JSONObject(jsonObject.get("SystemTypeName").toString()).getString("Name");
                            }

                            Boolean State;
                            if (jsonObject.get("State").toString().equals("null")) {
                                State = null;
                            } else {
                                State = jsonObject.getBoolean("State");
                            }

                            String firstFrame = "";//第一帧图片地址

                            String ObservedUsers=jsonObject.getString("ObservedUsers");

                            String sre=jsonObject.get("Observed").toString();

                            Boolean Observed;
                            if (jsonObject.get("Observed").toString().equals("null")) {
                                Observed = null;
                            } else {
                                Observed = jsonObject.getBoolean("Observed");
                            }

                            Boolean IsFinishedAndDelay;
                            if (jsonObject.get("IsFinishedAndDelay").toString().equals("null")) {
                                IsFinishedAndDelay = null;
                            } else {
                                IsFinishedAndDelay = jsonObject.getBoolean("IsFinishedAndDelay");
                            }

                            Object CompletedAt = jsonObject.get("CompletedAt");
                            Object Deadline = jsonObject.get("Deadline");
                            Object Date = jsonObject.get("Date");
                            Object LastUpdate = jsonObject.get("LastUpdate");

                            QuestionModel questionModel = new QuestionModel(ProjectId, ClosedUserId, Priority, UserId, UpdatedBy,
                                    ID, Title, Comment, GroupId,Category, ViewportId, SystemType, At, Pictures,
                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers,DocumentIds,UserName,CategoryName,firstFrame,
                                    ObservedUsers,SystemTypeName,State, Observed, IsFinishedAndDelay,CompletedAt, Deadline, Date, LastUpdate);

                            //数据源
                            tempList.add(questionModel);

//                            //数据源
//                            screenQuestionList.add(questionModel);
                        }

                        for (int i = 0; i < tempList.size(); i++) {

//                            if (screenQuestionList.get(i).getVideo() != null && (!screenQuestionList.get(i).getVideo().equals("null")) &&
//                                    (!screenQuestionList.get(i).getVideo().equals("[]"))) {
//                                //解析字符串
//                                String str = screenQuestionList.get(i).getVideo().replace("[", "").replace("]", "");
//                                JSONObject jb = new JSONObject(str);
//                                String name = jb.getString("Name").toString();
//                                String id = jb.getString("ID").toString();
//                                //获取视频第一帧
//                                getFirstFrame(name, id);
//                            }

                            if (tempList.get(i).getPictures() != null && (!tempList.get(i).getPictures().equals("null")) &&
                                    (!tempList.get(i).getPictures().equals("[]"))) {
                                //解析字符串
                                String[] arr = tempList.get(i).getPictures()
                                        .replace("{", "").replace("}", "")
                                        .replace("[", "").replace("]", "").split(",");

                                String id = arr[1].split(":")[1].replace("\"", "").replace("\"", "").toString();
                                String picUrl = AppConst.innerIp + "/api/AnnexFile/" + id;
                                tempList.get(i).setFirstFrame(picUrl);
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenQuestionList.addAll(tempList);
                                if (!isPull) {
                                    adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this,
                                            DividerItemDecoration.VERTICAL));//报控指针，可能是因为getActivity()为空，待解决
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else{
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter == null) {
                                        adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                        recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this, DividerItemDecoration.VERTICAL));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
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
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    //下载已延期列表
    private void downloadDelay(final boolean isPull) {
        screenQuestionList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/"+ SPUtils.getInstance().getInt("projectID")+"/SynergyQuestion?where=IsFinishedAndDelay=true")
//                .url(AppConst.innerIp + "/api/"+ SPUtils.getInstance().getInt("projectID")+"/SynergyQuestion")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            Integer ProjectId = jsonObject.getInt("ProjectId");

                            Integer ClosedUserId;
                            if (jsonObject.get("ClosedUserId").toString().equals("null")) {
                                ClosedUserId = null;
                            } else {
                                ClosedUserId = jsonObject.getInt("GroupId");
                            }

                            Integer Priority;
                            if (jsonObject.get("Priority").toString().equals("null")) {
                                Priority = null;
                            } else {
                                Priority = jsonObject.getInt("Priority");
                            }

                            Integer UserId;
                            if (jsonObject.get("UserId").toString().equals("null")) {
                                UserId = null;
                            } else {
                                UserId = jsonObject.getInt("UserId");
                            }

                            Integer UpdatedBy;
                            if (jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = null;
                            } else {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            String ID = jsonObject.getString("ID");
                            String Title = jsonObject.getString("Title");
                            String Comment = jsonObject.getString("Comment");
                            String GroupId = jsonObject.getString("GroupId");
                            String Category = jsonObject.getString("Category");
                            String ViewportId = jsonObject.getString("ViewportId");
                            String SystemType = jsonObject.getString("SystemType");
                            String At = jsonObject.getString("At");
                            String Pictures = jsonObject.getString("Pictures");
                            String Uuids = jsonObject.getString("Uuids");
                            String SelectionSetIds = jsonObject.getString("SelectionSetIds");
                            String Video = jsonObject.getString("Video");
                            String Voice = jsonObject.getString("Voice");
                            String Tags = jsonObject.getString("Tags");
                            String ReadUsers = jsonObject.getString("ReadUsers");
                            String DocumentIds = jsonObject.getString("DocumentIds");

                            String UserName;
                            if(jsonObject.get("UserInfo").toString().equals("null")){
                                UserName=null;
                            }else{
                                UserName=new JSONObject(jsonObject.get("UserInfo").toString()).getString("UserName");
                            }

                            String CategoryName;
                            if(jsonObject.get("CategoryName").toString().equals("null")){
                                CategoryName=null;
                            }else{
                                CategoryName=new JSONObject(jsonObject.get("CategoryName").toString()).getString("Name");
                            }

                            String SystemTypeName;
                            if(jsonObject.get("SystemTypeName").toString().equals("null")){
                                SystemTypeName=null;
                            }else{
                                SystemTypeName=new JSONObject(jsonObject.get("SystemTypeName").toString()).getString("Name");
                            }
                            String firstFrame = "";//第一帧图片地址

                            String ObservedUsers=jsonObject.getString("ObservedUsers");

                            Boolean State;
                            if (jsonObject.get("State").toString().equals("null")) {
                                State = null;
                            } else {
                                State = jsonObject.getBoolean("State");
                            }

                            String sre=jsonObject.get("Observed").toString();

                            Boolean Observed;
                            if (jsonObject.get("Observed").toString().equals("null")) {
                                Observed = null;
                            } else {
                                Observed = jsonObject.getBoolean("Observed");
                            }

                            Boolean IsFinishedAndDelay;
                            if (jsonObject.get("IsFinishedAndDelay").toString().equals("null")) {
                                IsFinishedAndDelay = null;
                            } else {
                                IsFinishedAndDelay = jsonObject.getBoolean("IsFinishedAndDelay");
                            }

                            Object CompletedAt = jsonObject.get("CompletedAt");
                            Object Deadline = jsonObject.get("Deadline");
                            Object Date = jsonObject.get("Date");
                            Object LastUpdate = jsonObject.get("LastUpdate");

                            QuestionModel questionModel = new QuestionModel(ProjectId, ClosedUserId, Priority, UserId, UpdatedBy,
                                    ID, Title, Comment, GroupId,Category, ViewportId, SystemType, At, Pictures,
                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers,DocumentIds,UserName,CategoryName,SystemTypeName,firstFrame,
                                    ObservedUsers,State, Observed, IsFinishedAndDelay,CompletedAt, Deadline, Date, LastUpdate);

                            //数据源
                            tempList.add(questionModel);
                        }

                        for (int i = 0; i < tempList.size(); i++) {

//                            if (screenQuestionList.get(i).getVideo() != null && (!screenQuestionList.get(i).getVideo().equals("null")) &&
//                                    (!screenQuestionList.get(i).getVideo().equals("[]"))) {
//                                //解析字符串
//                                String str = screenQuestionList.get(i).getVideo().replace("[", "").replace("]", "");
//                                JSONObject jb = new JSONObject(str);
//                                String name = jb.getString("Name").toString();
//                                String id = jb.getString("ID").toString();
//                                //获取视频第一帧
//                                getFirstFrame(name, id);
//                            }

                            if (tempList.get(i).getPictures() != null && (!tempList.get(i).getPictures().equals("null")) &&
                                    (!tempList.get(i).getPictures().equals("[]"))) {
                                //解析字符串
                                String[] arr = tempList.get(i).getPictures()
                                        .replace("{", "").replace("}", "")
                                        .replace("[", "").replace("]", "").split(",");

                                String id = arr[1].split(":")[1].replace("\"", "").replace("\"", "").toString();
                                String picUrl = AppConst.innerIp + "/api/AnnexFile/" + id;
                                tempList.get(i).setFirstFrame(picUrl);
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenQuestionList.addAll(tempList);
                                if (!isPull) {
                                    adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else{
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter == null) {
                                        adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                        recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this, DividerItemDecoration.VERTICAL));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
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
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }

        });
    }

    //下载已完成列表
    private void downloadComplete(final boolean isPull) {
        screenQuestionList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion?where=State=true")
//                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
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

                            Integer ProjectId = jsonObject.getInt("ProjectId");

                            Integer ClosedUserId;
                            if (jsonObject.get("ClosedUserId").toString().equals("null")) {
                                ClosedUserId = null;
                            } else {
                                ClosedUserId = jsonObject.getInt("GroupId");
                            }

                            Integer Priority;
                            if (jsonObject.get("Priority").toString().equals("null")) {
                                Priority = null;
                            } else {
                                Priority = jsonObject.getInt("Priority");
                            }

                            Integer UserId;
                            if (jsonObject.get("UserId").toString().equals("null")) {
                                UserId = null;
                            } else {
                                UserId = jsonObject.getInt("UserId");
                            }

                            Integer UpdatedBy;
                            if (jsonObject.get("UpdatedBy").toString().equals("null")) {
                                UpdatedBy = null;
                            } else {
                                UpdatedBy = jsonObject.getInt("UpdatedBy");
                            }

                            String ID = jsonObject.getString("ID");
                            String Title = jsonObject.getString("Title");
                            String Comment = jsonObject.getString("Comment");
                            String GroupId = jsonObject.getString("GroupId");
                            String Category = jsonObject.getString("Category");
                            String ViewportId = jsonObject.getString("ViewportId");
                            String SystemType = jsonObject.getString("SystemType");
                            String At = jsonObject.getString("At");
                            String Pictures = jsonObject.getString("Pictures");
                            String Uuids = jsonObject.getString("Uuids");
                            String SelectionSetIds = jsonObject.getString("SelectionSetIds");
                            String Video = jsonObject.getString("Video");
                            String Voice = jsonObject.getString("Voice");
                            String Tags = jsonObject.getString("Tags");
                            String ReadUsers = jsonObject.getString("ReadUsers");
                            String DocumentIds = jsonObject.getString("DocumentIds");

                            String UserName;
                            if (jsonObject.get("UserInfo").toString().equals("null")) {
                                UserName = null;
                            } else {
                                UserName = new JSONObject(jsonObject.get("UserInfo").toString()).getString("UserName");
                            }

                            String CategoryName;
                            if (jsonObject.get("CategoryName").toString().equals("null")) {
                                CategoryName = null;
                            } else {
                                CategoryName = new JSONObject(jsonObject.get("CategoryName").toString()).getString("Name");
                            }

                            String firstFrame = "";//第一帧图片地址

                            String ObservedUsers=jsonObject.getString("ObservedUsers");

                            String SystemTypeName;
                            if (jsonObject.get("SystemTypeName").toString().equals("null")) {
                                SystemTypeName = null;
                            } else {
                                SystemTypeName = new JSONObject(jsonObject.get("SystemTypeName").toString()).getString("Name");
                            }

                            Boolean State;
                            if (jsonObject.get("State").toString().equals("null")) {
                                State = null;
                            } else {
                                State = jsonObject.getBoolean("State");
                            }

                            String sre = jsonObject.get("Observed").toString();

                            Boolean Observed;
                            if (jsonObject.get("Observed").toString().equals("null")) {
                                Observed = null;
                            } else {
                                Observed = jsonObject.getBoolean("Observed");
                            }

                            Boolean IsFinishedAndDelay;
                            if (jsonObject.get("IsFinishedAndDelay").toString().equals("null")) {
                                IsFinishedAndDelay = null;
                            } else {
                                IsFinishedAndDelay = jsonObject.getBoolean("IsFinishedAndDelay");
                            }

                            Object CompletedAt = jsonObject.get("CompletedAt");
                            Object Deadline = jsonObject.get("Deadline");
                            Object Date = jsonObject.get("Date");
                            Object LastUpdate = jsonObject.get("LastUpdate");

                            QuestionModel questionModel = new QuestionModel(ProjectId, ClosedUserId, Priority, UserId, UpdatedBy,
                                    ID, Title, Comment, GroupId, Category, ViewportId, SystemType, At, Pictures,
                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers, DocumentIds, UserName, CategoryName, firstFrame,
                                    ObservedUsers,SystemTypeName, State, Observed, IsFinishedAndDelay, CompletedAt, Deadline, Date, LastUpdate);

                            //数据源
                            tempList.add(questionModel);
                        }

                        for (int i = 0; i < tempList.size(); i++) {

//                            if (screenQuestionList.get(i).getVideo() != null && (!screenQuestionList.get(i).getVideo().equals("null")) &&
//                                    (!screenQuestionList.get(i).getVideo().equals("[]"))) {
//                                //解析字符串
//                                String str = screenQuestionList.get(i).getVideo().replace("[", "").replace("]", "");
//                                JSONObject jb = new JSONObject(str);
//                                String name = jb.getString("Name").toString();
//                                String id = jb.getString("ID").toString();
//                                //获取视频第一帧
//                                getFirstFrame(name, id);
//                            }

                            if (tempList.get(i).getPictures() != null && (!tempList.get(i).getPictures().equals("null")) &&
                                    (!tempList.get(i).getPictures().equals("[]"))) {
                                //解析字符串
                                String[] arr = tempList.get(i).getPictures()
                                        .replace("{", "").replace("}", "")
                                        .replace("[", "").replace("]", "").split(",");

                                String id = arr[1].split(":")[1].replace("\"", "").replace("\"", "").toString();
                                String picUrl = AppConst.innerIp + "/api/AnnexFile/" + id;
                                tempList.get(i).setFirstFrame(picUrl);
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                screenQuestionList.addAll(tempList);
                                if (!isPull) {
                                    adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (screenQuestionList != null && screenQuestionList.size() > 0 && adapter == null) {
                                        adapter = new QuestionAdapter(MyApplication.getContext(), screenQuestionList);
                                        recyclerView.addItemDecoration(new DividerItemDecoration(QuestionScreenActivity.this, DividerItemDecoration.VERTICAL));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
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
                } else {
                    ToastUtils.showShort("数据请求出错");
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }


        });
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = findViewById(R.id.screen_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(QuestionScreenActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = findViewById(R.id.screen_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true,position);
            }
        });

        back=findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listIsnull=findViewById(R.id.rl_screen_list_is_null);

        search =findViewById(R.id.screen_sv_file);
        search.setVisibility(View.VISIBLE);//搜索框初始化时默认显示
        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) search.findViewById(R.id.search_src_text);
        searchAutoComplete.setTextSize(16);

        //去掉下划线
        DelUnderLine.delUnderLine(search);

        // 设置搜索文本监听
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }
        });

        llSearch = findViewById(R.id.ll_file_search_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });
    }

    //搜索
    private void doSearch(String keyWord) {

        if(adapter==null)return;
        searchFileList.clear();
        screenQuestionList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            if(tempList.get(i).getUserName()!=null&&tempList.get(i).getTitle()!=null){
                if (tempList.get(i).getUserName().contains(keyWord)||tempList.get(i).getTitle().contains(keyWord)) {

                    searchFileList.add(tempList.get(i));

                }
            }

        }
        screenQuestionList.addAll(searchFileList);
        adapter.notifyDataSetChanged();

    }

    /**
     *
     * @param isPull
     * @param listType 值为0、1、2与position对应，分别表示刷新相对应的列表
     */
    private void refreshFile(final boolean isPull,int listType) {
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
                        if (listType == 0) {
                            //下载未完成列表
                            downloadUncomplete(true);
                        } else if (listType == 1) {
                            //下载已延期列表
                            downloadDelay(true);
                        } else if (listType == 2) {
                            //下载已完成列表
                            downloadComplete(true);
                        }
                    }
                });
            }
        }).start();
    }
}
