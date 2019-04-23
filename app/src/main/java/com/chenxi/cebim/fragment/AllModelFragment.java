package com.chenxi.cebim.fragment;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.model.ModelListActivity;
import com.chenxi.cebim.activity.model.WebModelActivity;
import com.chenxi.cebim.adapter.AllModelAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.ModelEntity;
import com.chenxi.cebim.entity.ModelList;
import com.chenxi.cebim.entity.RecentlyModelListClearEvent;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.Division;
import com.chenxi.cebim.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class AllModelFragment extends BaseFragment {
    private View view;
    private String projectName;
    private int projectId;

    private ArrayList<ModelEntity> allModelList = new ArrayList<>();
    private ArrayList<ModelEntity> cacheList = new ArrayList<ModelEntity>();
    private SwipeRefreshLayout swipeRefresh;
    private AllModelAdapter adapter;
    private RecyclerView recyclerView;
    private RelativeLayout bottomBtn;
    private TextView allSelected, choosedNum, downLoadModel, openModel;
    private ACache mCache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all, container, false);
        ModelListActivity ma = (ModelListActivity) getActivity();
        projectName = ma.getProjectName();
        projectId = ma.getProjectId();

        mCache = ACache.get(getActivity());

        //控件初始化
        initView();

        swipeRefresh.setRefreshing(true);//开启进度条
        initAll(0);
        return view;
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.all_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.all_swip_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAll();
            }
        });

        bottomBtn = view.findViewById(R.id.rl_model_bottom);//底部导航栏

        allSelected = view.findViewById(R.id.tv_all_select_or_no);//全选
        allSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (allSelected.getText().toString().equals("全选")) {
                    allSelected.setText("全不选");
                    long modelsize = 0;
                    for (int i = 0; i < adapter.getList().size(); i++) {
                        adapter.getList().get(i).setChecked(true);
                        modelsize += adapter.getList().get(i).getFileSize();
                    }

                    adapter.notifyDataSetChanged();

                    //底部导航栏选择模型数量及文件大小
                    choosedNum.setText("选择" + adapter.getList().size() + "个子模型(" + Division.division(modelsize, 1024 * 1024) + "MB)");

                } else {
                    allSelected.setText("全选");
                    for (int i = 0; i < adapter.getList().size(); i++) {
                        adapter.getList().get(i).setChecked(false);
                    }

                    adapter.notifyDataSetChanged();
                    choosedNum.setText("选择0个子模型(0)");
                }
            }
        });

        choosedNum = view.findViewById(R.id.tv_choosed_model_num);//已选数量
        downLoadModel = view.findViewById(R.id.tv_model_down_load);//下载
        openModel = view.findViewById(R.id.tv_model_open);//打开
        openModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuffer completedSb = new StringBuffer();
                        StringBuffer unCompletedSb = new StringBuffer();
                        StringBuffer completedNameSb = new StringBuffer();
                        String modelName = "";
                        int checkedNum=0;//被选中的数量
                        int aCheDNum=0;//已添加入缓存的数量
                        for (int i = 0; i < adapter.getList().size(); i++) {

                            if (adapter.getList().get(i).isChecked()) {
                                if (adapter.getList().get(i).isCompleted()) {
                                    completedSb.append("" + adapter.getList().get(i).getModelID());
                                    completedSb.append(",");
                                    modelName = adapter.getList().get(i).getModelName();
                                    completedNameSb.append("" + modelName);
                                    completedNameSb.append(",");
                                } else {
                                    unCompletedSb.append("" + adapter.getList().get(i).getModelName());
                                    unCompletedSb.append(",");
                                }
                                checkedNum++;
                            }
                        }

                        //只打开已完成轻量化的模型
                        String modelIdString = StringUtil.trimFirstAndLastChar(completedSb.toString(), ',');
                        String completedNameString = StringUtil.trimFirstAndLastChar(completedNameSb.toString(), ',');
                        Intent intent = new Intent(view.getContext(), WebModelActivity.class);
                        intent.putExtra("ModelID", modelIdString);
                        intent.putExtra("ProjectID", adapter.getList().get(0).getProjectID());
                        intent.putExtra("ModelName", completedNameString);
                        view.getContext().startActivity(intent);

                        //缓存已打开的
                        for (int i = 0; i < adapter.getList().size(); i++) {

                            if (adapter.getList().get(i).isChecked()) {
                                if (adapter.getList().get(i).isCompleted()) {

                                    //缓存最近打开
                                    aCheDNum++;
                                    if ((ArrayList<ModelList>) mCache.getAsObject("最近打开模型" + projectId) != null) {
                                        cacheList = (ArrayList<ModelEntity>) mCache.getAsObject("最近打开模型" + projectId);//缓存List
                                        if(aCheDNum==checkedNum){
                                            isTheSameElement(i,true);
                                        }else{
                                            isTheSameElement(i,false);
                                        }

                                    } else {
                                        cacheList.add(adapter.getList().get(i));//添加点击的list
                                        mCache.put("最近打开模型" + projectId, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
                                        EventBus.getDefault().post(new RecentlyModelListClearEvent("刷新最近列表"));
                                    }

                                    //缓存最近打开
                                    mCache.put("" + adapter.getList().get(i).getModelID(), adapter.getList().get(i), ACache.TIME_DAY * 2);//直接缓存对象
                                }
                            }

                        }

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //未完成的不打开，给个tost提示
                                if(unCompletedSb!=null&&unCompletedSb.toString().length()>0){
                                    String unCompletedModelIdString = StringUtil.trimFirstAndLastChar(unCompletedSb.toString(), ',');
                                    ToastUtils.showLong(unCompletedModelIdString+"未完成轻量化，暂时无法打开");
                                }
                            }
                        });

                    }
                }).start();


            }
        });

    }

    //用于判断所点击的mModelList是否有和缓存中相同的，如有，不添加进cacheList，没有则添加进去
    private void isTheSameElement(int position,boolean ispostEven) {
        //用于判断所点击的mModelList是否有和缓存中相同的，如有，则needToDelPostion记录下标，先删除这个元素，然后在0位置插入这个元素
        int needToDelPostion = -1;
        int modelId = -1;
        ModelEntity needToDel=null;
        for (int i = 0; i < cacheList.size(); i++) {
            if (adapter.getList().get(position).getModelID()==cacheList.get(i).getModelID()) {
                needToDelPostion = position;//记录需要删除的位置
                needToDel=adapter.getList().get(position);//记录需要调整到最前面的对象
                modelId=adapter.getList().get(position).getModelID();
            }
        }

        if (needToDelPostion == -1) {
            cacheList.add(0,adapter.getList().get(position));//添加点击的list
            mCache.put("最近打开模型" + projectId, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
        }else{
            //把这个元素放在列表最前面(先删除这个元素，再在最前面插入这个元素)
            Iterator<ModelEntity> it = cacheList.iterator();
            while (it.hasNext())
            {
                ModelEntity modelEntity = it.next();
                if (modelEntity.getModelID() == modelId)
                {
                    it.remove();
                }
            }

            cacheList.add(0,needToDel);
            mCache.put("最近打开模型" + projectId, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
        }

        if(ispostEven){
            EventBus.getDefault().post(new RecentlyModelListClearEvent("刷新最近列表"));
        }

    }

    @Override
    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        super.onResume();
    }

    private void initAll(final int isRefresh) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectId + "/Model")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                allModelList.clear();
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int ModelID = jsonObject.getInt("ModelID");
                        int ProjectID = jsonObject.getInt("ProjectID");
                        int FileSize = jsonObject.getInt("FileSize");

                        int OrderNo = -1;
                        if (!jsonObject.get("OrderNo").toString().equals("null")) {
                            OrderNo = jsonObject.getInt("OrderNo");
                        }

                        String ModelName = jsonObject.getString("ModelName");
                        String DBName = jsonObject.getString("DBName");
                        String OnlySign = jsonObject.getString("OnlySign");
                        String AddTime = jsonObject.get("AddTime").toString();
                        String UpdateTime = jsonObject.get("UpdateTime").toString();
                        String ModelFile = jsonObject.getString("ModelFile");
                        String OperationUserID = jsonObject.getString("OperationUserID");
                        String FileType = jsonObject.getString("FileType");
                        String FileTypeInfo = jsonObject.getString("FileTypeInfo");

                        boolean IsChecked = false;
                        boolean IsCompleted = jsonObject.getBoolean("IsCompleted");

                        Byte[] FileContent = null;

                        ModelEntity modelEntity = new ModelEntity(ModelID, ProjectID, FileSize, OrderNo, ModelName, DBName,
                                OnlySign, AddTime, UpdateTime, ModelFile, OperationUserID
                                , FileType, FileTypeInfo, IsCompleted, IsChecked, FileContent);
                        allModelList.add(modelEntity);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (allModelList.size() == 0) {
                                ToastUtils.showShort("请求无数据");
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            } else {
                                if (isRefresh == 0) {
                                    adapter = new AllModelAdapter(allModelList, projectId);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
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
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });

    }

    private void refreshAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAll(1);
                        adapter.notifyDataSetChanged();
                        if (swipeRefresh.isRefreshing()) {
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //EventBus 打开底部导航栏（分享、打开、常用、更多）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {
        if (commonEven.getInfo().equals("关闭AllModelFragment全部底部导航栏")) {
            bottomBtn.setVisibility(View.GONE);//关闭底部导航栏

            for (int i = 0; i < adapter.getList().size(); i++) {
                adapter.getList().get(i).setChecked(false);
            }

            adapter.isShowCheckBox(false);
            adapter.notifyDataSetChanged();
        } else if (commonEven.getInfo().equals("打开AllModelFragment全部底部导航栏")) {
            bottomBtn.setVisibility(View.VISIBLE);//打开底部导航栏
            adapter.isShowCheckBox(true);
            adapter.notifyDataSetChanged();
        } else if (commonEven.getInfo().equals("选中或取消选中模型")) {

            long modelsize = 0;
            int num = 0;//选中的数量
            for (int i = 0; i < adapter.getList().size(); i++) {
                if (adapter.getList().get(i).isChecked()) {
                    num++;
                    modelsize += adapter.getList().get(i).getFileSize();
                }
            }

            //底部导航栏选择模型数量及文件大小
            choosedNum.setText("选择" + num + "个子模型(" + Division.division(modelsize, 1024 * 1024) + "MB)");

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
