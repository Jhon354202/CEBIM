package com.chenxi.cebim.fragment.coordination;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.QuestionAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.fragment.BaseFragment;

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

/**
 * 协同管理——问题——未完成Fragment
 */
public class QuestionUncompleteFragment extends BaseFragment {

    View view;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;

    private QuestionAdapter adapter;

    private List<QuestionModel> unCompleteQuestionList = new ArrayList<>();//获取回调接口中返回的

    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question_uncomplete, container, false);
        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        initView();
        swipeRefresh.setRefreshing(true);
        getData(false);//数据初始化
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity=activity;
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.uncomplete_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.uncomplete_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        listIsnull=view.findViewById(R.id.rl_uncomplete_list_is_null);
    }

    /**
     * 刷新列表
     */
    private void refreshFile(final boolean isPull) {
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

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData(isPull);
                    }
                });
            }
        }).start();
    }


    /**
     * 数据初始化
     */
    private void getData(final boolean isPull) {
        unCompleteQuestionList.clear();
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
                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers,DocumentIds,UserName,CategoryName,SystemTypeName,firstFrame,
                                    ObservedUsers,State, Observed, IsFinishedAndDelay,CompletedAt, Deadline, Date, LastUpdate);

                            //数据源
                            unCompleteQuestionList.add(questionModel);
                        }

                        for (int i = 0; i < unCompleteQuestionList.size(); i++) {

//                            if (completeQuestionList.get(i).getVideo() != null && (!completeQuestionList.get(i).getVideo().equals("null")) &&
//                                    (!completeQuestionList.get(i).getVideo().equals("[]"))) {
//                                //解析字符串
//                                String str = completeQuestionList.get(i).getVideo().replace("[", "").replace("]", "");
//                                JSONObject jb = new JSONObject(str);
//                                String name = jb.getString("Name").toString();
//                                String id = jb.getString("ID").toString();
//                                //获取视频第一帧
//                                getFirstFrame(name, id);
//                            }

                            if (unCompleteQuestionList.get(i).getPictures() != null && (!unCompleteQuestionList.get(i).getPictures().equals("null")) &&
                                    (!unCompleteQuestionList.get(i).getPictures().equals("[]"))) {
                                //解析字符串
                                String[] arr = unCompleteQuestionList.get(i).getPictures()
                                        .replace("{", "").replace("}", "")
                                        .replace("[", "").replace("]", "").split(",");

                                String id = arr[1].split(":")[1].replace("\"", "").replace("\"", "").toString();
                                String picUrl = AppConst.innerIp + "/api/AnnexFile/" + id;
                                unCompleteQuestionList.get(i).setFirstFrame(picUrl);
                            }

                        }

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isPull) {
                                    adapter = new QuestionAdapter(MyApplication.getContext(), unCompleteQuestionList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(mActivity,
                                            DividerItemDecoration.VERTICAL));//报控指针，可能是因为getActivity()为空，待解决
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else{
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (unCompleteQuestionList != null && unCompleteQuestionList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (unCompleteQuestionList != null && unCompleteQuestionList.size() > 0 && adapter == null) {
                                        adapter = new QuestionAdapter(MyApplication.getContext(), unCompleteQuestionList);
                                        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
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

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(NewQuestionDelEven newQuestionDelEven) {
        if (newQuestionDelEven.getInfo().contains("false")) {//从NewQuestion返回的信息，如果为false则刷新该页
            getData(false);
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