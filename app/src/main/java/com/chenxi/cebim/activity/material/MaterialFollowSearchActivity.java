package com.chenxi.cebim.activity.material;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.MaterialTraceAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialFollow;
import com.chenxi.cebim.entity.MaterialSettings;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.utils.DelUnderLine;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MaterialFollowSearchActivity extends AppCompatActivity {


    @BindView(R.id.material_follow_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.material_follow_swip_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.swipe_recyclerview)
    SwipeRecyclerView swipeRecyclerView;
    @BindView(R.id.tv_material_follow_all_selected)
    TextView allSelect;
    @BindView(R.id.tv_material_follow_del)
    TextView del;
    @BindView(R.id.tv_material_follow_update)
    TextView update;
    @BindView(R.id.tv_material_follow_batchSetup)
    TextView batchSetup;
    @BindView(R.id.ll_material_follow_setting)
    LinearLayout llMaterialFollowSetting;
    @BindView(R.id.sv_project)
    SearchView mSearchView;
    private int projectId, userId, delNum, updNum;
    private List<MaterialFollow> materialTraceList = new ArrayList<>();
    private List<MaterialFollow> searchList = new ArrayList<>();
    private List<MaterialFollow> temporyMaterialTraceList = new ArrayList<>();//用于类表中显示的对相集合
    private MaterialTraceAdapter adapter;
    private int position;
    private String userName;
    List<Integer> chooseNum = new ArrayList<>();
    private List<MaterialSettings> materialSettings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_follow_search);
        ButterKnife.bind(this);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        userName = SPUtils.getInstance().getString("UserName");
        userId = SPUtils.getInstance().getInt("UserID", -1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        swipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRecyclerView.setItemViewSwipeEnabled(true); // 侧滑删除，默认关闭。
        getStateData();
        initData(0, true);
        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setTextSize(16);
        mSearchView.setIconified(false);
        //去掉下划线
        DelUnderLine.delUnderLine(mSearchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                doSearch(s);
                return true;
            }
        });
    }

    private void getStateData() {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/State")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错！");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    String jsonData = response.body().string();
                    materialSettings = JSON.parseArray(jsonData, MaterialSettings.class);
                    if (materialSettings.size() == 0) {
                        ToastUtils.showShort("请求无数据");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    private void doSearch(String s) {
        if (adapter == null) {
            return;
        }
        searchList.clear();
        temporyMaterialTraceList.clear();
        for (int i = 0; i < materialTraceList.size(); i++) {
            if (materialTraceList.get(i).getEntity_name().contains(s)) {
                searchList.add(materialTraceList.get(i));
            }
        }
        temporyMaterialTraceList.addAll(searchList);
        adapter.notifyDataSetChanged();
    }

    //获取材料跟踪数据
    private void initData(int isRefresh, boolean isFirstShow) {
        materialTraceList.clear();
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/QRCode/QRCodeMeteria")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    materialTraceList = JSON.parseArray(responseData, MaterialFollow.class);
                    temporyMaterialTraceList.addAll(materialTraceList);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (temporyMaterialTraceList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new MaterialTraceAdapter(temporyMaterialTraceList, projectId, MaterialFollowSearchActivity.this, materialSettings);
                                    if (isFirstShow) {
                                        swipeRecyclerView.addItemDecoration(new DividerItemDecoration(MaterialFollowSearchActivity.this, DividerItemDecoration.VERTICAL));
                                    }
                                    swipeRecyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (temporyMaterialTraceList != null && temporyMaterialTraceList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (temporyMaterialTraceList != null && temporyMaterialTraceList.size() > 0 && adapter == null) {
                                        adapter = new MaterialTraceAdapter(temporyMaterialTraceList, projectId, MaterialFollowSearchActivity.this, materialSettings);
                                        if (isFirstShow) {
                                            swipeRecyclerView.addItemDecoration(new DividerItemDecoration(MaterialFollowSearchActivity.this, DividerItemDecoration.VERTICAL));
                                        }
                                        swipeRecyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    }
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                    if (swipeRefresh.isRefreshing()) {
//                        swipeRefresh.setRefreshing(false);
//                    }
//                }
            }
        });
    }

    @OnClick({R.id.tv_material_follow_all_selected, R.id.tv_material_follow_del, R.id.tv_material_follow_update, R.id.tv_material_follow_batchSetup})
    public void onViewClicked(View view) {
        boolean hasChoose = false;
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_material_follow_all_selected:
                if (allSelect.getText().equals("全选")) {
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        adapter.getList().get(i).setChoosed(true);
                    }
                    allSelect.setText("取消");
                } else if (allSelect.getText().equals("取消")) {
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        adapter.getList().get(i).setChoosed(false);
                    }
                    allSelect.setText("全选");
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.tv_material_follow_del:
                delMaterialTrace();
                break;
            case R.id.tv_material_follow_update:
                updMaterialTrace();
                break;
            case R.id.tv_material_follow_batchSetup:
                chooseNum.clear();
                for (int i = 0; i < adapter.getList().size(); i++) {
                    if (adapter.getList().get(i).isChoosed()) {
                        hasChoose = true;
                        chooseNum.add(i);
                    }
                }
                if (hasChoose) {
                    intent = new Intent(this, MaterialFollowBatchSetupActivity.class);
                    startActivityForResult(intent, 1);
                }
                System.out.println(chooseNum.toString());
                break;
        }
    }

    //删除材料跟踪记录
    private void delMaterialTrace() {
        boolean hasAnyChoosed = false;
        delNum = 0;
        for (int i = 0; i < adapter.getList().size(); i++) {
            if (adapter.getList().get(i).isChoosed()) {
                hasAnyChoosed = true;
                delNum++;
            }
        }

        if (hasAnyChoosed) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MaterialFollowSearchActivity.this);
            builder.setTitle("提示");

            builder.setMessage("确定删除？");

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int size = adapter.getList().size();
                    for (int j = 0; j < adapter.getList().size(); j++) {
                        if (adapter.getList().get(j).isChoosed()) {

                            del(adapter.getList().get(j).getPrintGUID());
                        }
                    }

                }

            });
            builder.create().show();

        } else {
            ToastUtils.showShort("请选择材料");
        }
    }

    //先删除远程，远程删除成功后删除本地
    private void del(String ID) {

        FormBody formBody = new FormBody.Builder()
                .build();
        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectId + "/QRCode/QRCodeMeteria/" + ID)
                .delete(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("材料删除失败");
            }

            @Override
            public void onResponse(Call call, final Response response) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                delNum--;
                            } else {
                                ToastUtils.showShort("材料删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("材料删除失败");
                        }

                        if (delNum == 0) {
                            ToastUtils.showShort("文件删除成功");
                            initData(0, false);
                        }

                    }
                });
            }
        });

    }

    private List<MaterialTrace> materialTraces = new ArrayList<>();

    private void updMaterialTrace() {
        updNum = 0;
        boolean hasAnyChoosed = false;
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace?where=ComponentUID="
                        + "\"" + materialTraceList.get(position).getPrintGUID() + "\"")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        materialTraces = JSON.parseArray(responseData, MaterialTrace.class);
                    }
                });

            }
        });
        for (int i = 0; i < adapter.getList().size(); i++) {
            if (adapter.getList().get(i).isChoosed()) {
                hasAnyChoosed = true;
                updNum++;
            }
        }
        if (hasAnyChoosed) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MaterialFollowSearchActivity.this);
            alertBuilder.setTitle("提示");
            alertBuilder.setMessage("确定更新？");

            alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < adapter.getList().size(); i++) {
                        if (adapter.getList().get(i).isChoosed()) {
                            if (materialTraces.size() != 0) {
                                JSONObject jsonObject = new JSONObject();
                                JSONObject jsonObject1 = new JSONObject();
                                try {
                                    jsonObject.put("ID", materialTraces.get(i).getID());
                                    jsonObject.put("ProjectID", materialTraces.get(i).getProjectID());
                                    jsonObject.put("TempleteID", materialTraces.get(i).getTempleteID());
                                    jsonObject.put("ComponentUID", materialTraces.get(i).getComponentUID());
                                    jsonObject.put("ComponentName", materialTraces.get(i).getComponentName());
                                    jsonObject.put("ComponentID", materialTraces.get(i).getComponentID());
                                    jsonObject.put("StateID", materialTraces.get(i).getStateID());
                                    jsonObject.put("Specialty", materialTraces.get(i).getSpecialty());
                                    jsonObject.put("Category", materialTraces.get(i).getCategory());
                                    jsonObject.put("Storey", materialTraces.get(i).getStorey());
                                    jsonObject.put("OperationUserID", materialTraces.get(i).getOperationUserID());
                                    jsonObject1.put("UserName", userName);
                                    jsonObject1.put("UserID", userId);
                                    jsonObject.put("CrUserInfo", jsonObject1);
                                    //  ToastUtils.showShort(jsonObject.toString());
                                    System.out.println(jsonObject.toString());
                                    //FormBody formBody = new FormBody.Builder().build();
                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                    RequestBody body = RequestBody.create(JSON, String.valueOf(jsonObject));
                                    Request request1 = new Request.Builder()
                                            .url(AppConst.innerIp + "/api/" + projectId + "/MaterialTrace/Next")
                                            .post(body)
                                            .build();
                                    MyApplication.getOkHttpClient().newCall(request1).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {

                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String responseJson = response.body().string();
                                            if (response.code() == 200) {
                                                ToastUtils.showShort("更新成功!");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                                //
                                                // adapter.updateData(materialTraceList);
                                            } else {
                                                ToastUtils.showShort("更新失败！");
                                            }
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            alertBuilder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    Bundle bundle = data.getExtras();
                    String str = bundle.getString("newstate");
                    position = bundle.getInt("position", -1);
                    ToastUtils.showShort(str);
                    materialTraceList.get(position).setNextstate(str);
                    adapter.notifyDataSetChanged();
                    break;
                case 111:
                    Bundle bundle1 = data.getExtras();
                    String res = bundle1.getString("newstate");
                    for (Integer j : chooseNum) {
                        materialTraceList.get(j).setNextstate(res);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }
}
