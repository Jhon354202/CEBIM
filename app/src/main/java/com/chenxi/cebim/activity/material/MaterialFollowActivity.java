package com.chenxi.cebim.activity.material;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;

import com.chenxi.cebim.activity.zxing.activity.CaptureActivity;
import com.chenxi.cebim.activity.zxing.utils.Constant;
import com.chenxi.cebim.adapter.MaterialTraceAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialFollow;
import com.chenxi.cebim.entity.MaterialSettings;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.entity.MaterialTraceModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.mabeijianxi.smallvideorecord2.Log;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MaterialFollowActivity extends BaseActivity implements View.OnClickListener {

    private ImageView back, scan, search;
    private TextView allSelect, del, update, batchSetup;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private SwipeRecyclerView swipeRecyclerView;
    private int projectId, userId;

    private List<MaterialFollow> materialTraceList = new ArrayList<>();
    private MaterialTraceAdapter adapter;

    private int delNum, updNum;

    public final int REQUEST_CODE = 10001;
    public final int RESULT_CODE = 10002;
    public final int RESULT_CODE2 = 10003;

    private List<String> list = new ArrayList<>();
    private int position;
    private String userName;
    private List<MaterialSettings> materialSettings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_follow);
        projectId = SPUtils.getInstance().getInt("projectID", -1);
        userName = SPUtils.getInstance().getString("UserName");
        userId = SPUtils.getInstance().getInt("UserID", -1);
        //控件初始化
        initView();
        getStateData();
        //获取网络数据
        initData(0, true);

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


    private void initView() {
        //返回按钮
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(this);

        //扫码按钮
        scan = findViewById(R.id.toolbar_first_right_iv);
        scan.setOnClickListener(this);

        //搜索按钮
        search = findViewById(R.id.toolbar_second_right_iv);
        search.setOnClickListener(this);

        //全选
        allSelect = findViewById(R.id.tv_material_follow_all_selected);
        allSelect.setOnClickListener(this);

        //删除
        del = findViewById(R.id.tv_material_follow_del);
        del.setOnClickListener(this);

        //更新
        update = findViewById(R.id.tv_material_follow_update);
        update.setOnClickListener(this);

        //批量设置
        batchSetup = findViewById(R.id.tv_material_follow_batchSetup);
        batchSetup.setOnClickListener(this);

        swipeRefresh = findViewById(R.id.material_follow_swip_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        recyclerView = findViewById(R.id.material_follow_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        swipeRecyclerView = findViewById(R.id.swipe_recyclerview);
        swipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRecyclerView.setItemViewSwipeEnabled(true); // 侧滑删除，默认关闭。
//        swipeRecyclerView.addItemDecoration(new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divice_line)));
//        swipeRecyclerView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(View itemView, int position) {
//                // 根据原position判断该item是否是parent item
//                if (mAdapter.isParentItem(position)) {
//                    // 换取parent position
//                    final int parentPosition = mAdapter.parentItemPosition(position);
//
//                    // 判断parent是否打开了二级菜单
//                    if (mAdapter.isExpanded(parentPosition)) {
//                        mDataList.get(parentPosition).setExpanded(false);
//                        mAdapter.notifyParentChanged(parentPosition);
//
//                        // 关闭该parent下的二级菜单
//                        mAdapter.collapseParent(parentPosition);
//                    } else {
//                        mDataList.get(parentPosition).setExpanded(true);
//                        mAdapter.notifyParentChanged(parentPosition);
//
//                        // 打开该parent下的二级菜单
//                        mAdapter.expandParent(parentPosition);
//                    }
//                } else {
//                    // 换取parent position
//                    int parentPosition = mAdapter.parentItemPosition(position);
//                    // 换取child position
//                    int childPosition = mAdapter.childItemPosition(position);
//                    String message = String.format("我是%1$d爸爸的%2$d儿子", parentPosition, childPosition);
//                    Toast.makeText(ListActivity.this, message, Toast.LENGTH_LONG).show();
//                }
//            }
//        });

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (materialTraceList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new MaterialTraceAdapter(materialTraceList, projectId, MaterialFollowActivity.this,materialSettings);
                                    if (isFirstShow) {
                                        swipeRecyclerView.addItemDecoration(new DividerItemDecoration(MaterialFollowActivity.this, DividerItemDecoration.VERTICAL));
                                    }
                                    swipeRecyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (materialTraceList != null && materialTraceList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (materialTraceList != null && materialTraceList.size() > 0 && adapter == null) {
                                        adapter = new MaterialTraceAdapter(materialTraceList, projectId, MaterialFollowActivity.this,materialSettings);
                                        if (isFirstShow) {
                                            swipeRecyclerView.addItemDecoration(new DividerItemDecoration(MaterialFollowActivity.this, DividerItemDecoration.VERTICAL));
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
            }
        });
    }

    List<Integer> chooseNum = new ArrayList<>();

    @Override
    public void onClick(View view) {
        Intent intent;
        boolean hasChoose = false;

        switch (view.getId()) {
            case R.id.toolbar_left_btn:
                finish();
                break;

            case R.id.toolbar_first_right_iv:
                intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, Constant.REQ_QR_CODE);
//                new IntentIntegrator(MaterialFollowActivity.this)
//                        .setOrientationLocked(false)
//                        .setCaptureActivity(CaptureActivity.class) // 设置自定义的activity是CustomActivity
//                        .initiateScan(); // 初始化扫描
                break;

            case R.id.toolbar_second_right_iv:
                intent = new Intent(this, MaterialFollowSearchActivity.class);
                startActivity(intent);
                break;

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

            AlertDialog.Builder builder = new AlertDialog.Builder(MaterialFollowActivity.this);
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
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MaterialFollowActivity.this);
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
                                                // adapter.notifyDataSetChanged();
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
                        initData(1, false);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE) {
            switch (resultCode) {
                case RESULT_CODE:
                    try {
                        Bundle bundle = data.getExtras();
                        String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
                        System.out.println("sssssssssssssssss scan 0 =====================" + scanResult);
                        ToastUtils.showShort(scanResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(" Exceptionscan 0 =====================" + e);
                    }
                    break;
                case RESULT_CODE2:
                    Intent intent = getIntent();
                    list = (List<String>) data.getSerializableExtra("scanresult");
                    System.out.println("sssssssssssssssss list.size =====================" + list.size());
                    break;
            }
        } else if (requestCode == 1) {
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