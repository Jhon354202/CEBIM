package com.chenxi.cebim.activity.coordination;

import android.support.v7.app.AppCompatActivity;
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
import com.chenxi.cebim.adapter.ShowStringAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.entity.QuestionCategoriesModel;

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

public class SystemTypeActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ImageView back;
    ShowStringAdapter showStringAdapter;
    List<QuestionCategoriesModel> list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_type);

        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        initView();
        initSystemType(0);
    }

    private void initView() {
        recyclerView = findViewById(R.id.system_type_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SystemTypeActivity.this);
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

    private void initSystemType(final int isRefresh) {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/"+SPUtils.getInstance().getInt("projectID")+"/SynergyQuestion/Specialty")
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

                            QuestionCategoriesModel questionCategoriesModel = new QuestionCategoriesModel(ID, Name,ProjectId,CreatedBy
                                    ,UpdatedBy,CreatedAt,UpdatedAt);
                            list.add(questionCategoriesModel);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRefresh == 0) {
                                    showStringAdapter = new ShowStringAdapter(list,"SystemTypeActivity");
                                    recyclerView.setAdapter(showStringAdapter);
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

        if(newQuestionDelEven.getInfo().equals("SystemTypeActivityfinish")){//从ShowStringAdapter返回,用于finish本界面
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
