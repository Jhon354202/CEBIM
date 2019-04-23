package com.chenxi.cebim.activity.coordination;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.DiscussionGroupAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.DiscussionGroupModel;
import com.chenxi.cebim.entity.NewQuestionDelEven;

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

public class DiscussionGroupActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ImageView back;
    DiscussionGroupAdapter discussionGroupAdapter;
    List<DiscussionGroupModel> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion_group);

        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        initView();
        getGroup(0);
    }


    private void initView() {
        recyclerView = findViewById(R.id.discussion_group_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(DiscussionGroupActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getGroup(final int isRefresh) {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/"+ SPUtils.getInstance().getInt("projectID")+"/SynergyQuestion/Groups")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if(response.code()==200){
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String ID = jsonObject.getString("ID");
                            String Name = jsonObject.getString("Name");
                            String Users= jsonObject.getString("Users");
                            int ProjectId= jsonObject.getInt("ProjectId");

                            int CreatedBy;
                            if(jsonObject.get("CreatedBy").toString().equals("null")){
                                CreatedBy=-1;
                            }else{
                                CreatedBy=jsonObject.getInt("CreatedBy");
                            }

                            int UpdatedBy;
                            if(jsonObject.get("UpdatedBy").toString().equals("null")){
                                UpdatedBy=-1;
                            }else{
                                UpdatedBy=jsonObject.getInt("UpdatedBy");
                            }

                            Object CreatedAt= jsonObject.get("CreatedAt");
                            Object UpdatedAt= jsonObject.get("UpdatedAt");

                            DiscussionGroupModel discussionGroupModel = new DiscussionGroupModel(ID, Name,Users,ProjectId,CreatedBy
                                    ,UpdatedBy,CreatedAt,UpdatedAt);
                            list.add(discussionGroupModel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh == 0) {
                                    discussionGroupAdapter = new DiscussionGroupAdapter(list,"DiscussionGroupActivity");
                                    recyclerView.setAdapter(discussionGroupAdapter);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据解析出错");

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");

                    }
                }else{
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });

    }

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(NewQuestionDelEven newQuestionDelEven) {

        if(newQuestionDelEven.getInfo().equals("DiscussionGroupActivityfinish")){//从DiscussionGroupAdapter返回,用于finish本界面
            finish();
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
