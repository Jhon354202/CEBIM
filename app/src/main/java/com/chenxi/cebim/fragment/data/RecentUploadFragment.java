package com.chenxi.cebim.fragment.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.adapter.DataFileAdapter;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.fragment.BaseFragment;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.DiskCacheDirUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecentUploadFragment extends BaseFragment {
    private View view;

    private int projectId;//用于存放从ModelListActivity中传过来的项目ID

    private ArrayList<TbFileShowmodel> recentDataFileList = new ArrayList<>();//用于存放从缓存中获取的DataFile对象列表
    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//用于显示的对象列表
    private DataFileAdapter adapter;
    private RecyclerView rv_uping, rv_uped;
    private TextView clear;
    private RelativeLayout noRecentUpLoad, recentUpLoad;

    ACache mCache;

    // 储存下载文件的目录
    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent_upload, container, false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        //获取ProjectId
        if(SPUtils.getInstance().getInt("projectID")==-1){
            ToastUtils.showShort("获取项目ID失败");
        }else{
            projectId =SPUtils.getInstance().getInt("projectID");
        }
        savePath = "" + DiskCacheDirUtil.getDiskCacheDir(getActivity());// 已下载文件的本地存储地址

        mCache = ACache.get(getActivity());

        initView();
        getData(0);
        
        return view;
    }

    private void initView() {
        //RecyclerView逻辑
        rv_uping = (RecyclerView) view.findViewById(R.id.recent_uploading_recyclerview);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        rv_uping.setLayoutManager(layoutManager1);

        rv_uped = (RecyclerView) view.findViewById(R.id.recen_uploaded_recyclerview);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        rv_uped.setLayoutManager(layoutManager2);

        noRecentUpLoad = (RelativeLayout) view.findViewById(R.id.rl_no_recent_upload);
        recentUpLoad = view.findViewById(R.id.rl_recent_upload);

        clear = (TextView) view.findViewById(R.id.tv_uploaded_clear_recent);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCache.remove(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                        .getInt("projectID") + ":recentupload");
                //把打开的文件存入缓存,key："userID:projectID"、value:打开的对象
                recentUpLoad.setVisibility(View.GONE);
                noRecentUpLoad.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getData(int isRefresh) {
        mCache = ACache.get(getActivity());
        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                + SPUtils.getInstance().getInt("projectID") + ":recentupload");

        if (tbFileShowmodelString != null && !tbFileShowmodelString.equals("null")) {
            recentUpLoad.setVisibility(View.VISIBLE);
            noRecentUpLoad.setVisibility(View.GONE);
            recentDataFileList.clear();
            showList.clear();
            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
            for (int i = 0; i < tbFileShowmodelArr.length; i++) {
                try {
                    if (tbFileShowmodelArr[i] == null || tbFileShowmodelArr[i].equals("null")) {
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject(tbFileShowmodelArr[i]);
                    int FID = jsonObject.getInt("fID");
                    int ProjectID = jsonObject.getInt("projectID");

                    int ClassID = jsonObject.getInt("classID");

                    int ParentClassID = jsonObject.getInt("parentClassID");

                    int OperationUserID = jsonObject.getInt("operationUserID");

                    String FileName = jsonObject.getString("fileName");
                    String FileType = jsonObject.getString("fileType");
                    String FileID=jsonObject.getString("fileID");

                    Object AddTime = jsonObject.get("addTime");
                    Object UpdateTime = jsonObject.get("updateTime");

                    Boolean IsCheck = false;
                    Boolean IsMove = false;

                    TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                            FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);
                    recentDataFileList.add(tbFileShowmodel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            showList.addAll(recentDataFileList);

            if (isRefresh == 0) {
                if (showList == null) return;
                adapter = new DataFileAdapter(getActivity(), showList, projectId);
                rv_uped.setAdapter(adapter);
            } else if (isRefresh == 1) {
                //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                if (showList != null && showList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                } else if (showList != null && showList.size() > 0 && adapter == null) {
                    adapter = new DataFileAdapter(getActivity(), showList, projectId);
                    rv_uped.setAdapter(adapter);
                }
            }
        } else {
            recentUpLoad.setVisibility(View.GONE);
            noRecentUpLoad.setVisibility(View.VISIBLE);
        }
    }

    //EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowBottomSettingButton isShowBottomSettingButton) {
        if (isShowBottomSettingButton.getInfo().equals("文件上传成功")){
                getData(0);
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
