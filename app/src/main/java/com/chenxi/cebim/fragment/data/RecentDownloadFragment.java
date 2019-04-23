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
import com.chenxi.cebim.entity.CommonEven;
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

public class RecentDownloadFragment extends BaseFragment {
    private View view;

    private int projectId;//用于存放从ModelListActivity中传过来的项目ID

    private ArrayList<TbFileShowmodel> recentDataFileList = new ArrayList<>();//用于存放从缓存中获取的DataFile对象列表
    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//用于显示的对象列表
    private DataFileAdapter adapter;
    private RecyclerView rv_downing, rv_downed;
    private TextView clear;
    private RelativeLayout noRecentDownLoad, recentDownLoad;

    ACache mCache;

    // 储存下载文件的目录
    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent_download, container, false);

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
        rv_downing = (RecyclerView) view.findViewById(R.id.recent_downloading_recyclerview);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        rv_downing.setLayoutManager(layoutManager1);

        rv_downed = (RecyclerView) view.findViewById(R.id.recen_downloaded_recyclerview);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        rv_downed.setLayoutManager(layoutManager2);

        noRecentDownLoad = (RelativeLayout) view.findViewById(R.id.rl_no_recent_download);
        recentDownLoad = view.findViewById(R.id.rl_recent_download);

        clear = (TextView) view.findViewById(R.id.tv_downloaded_clear_recent);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCache.remove(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                        .getInt("projectID") + ":recentdownload");
                //把打开的文件存入缓存,key："userID:projectID"、value:打开的对象
                recentDownLoad.setVisibility(View.GONE);
                noRecentDownLoad.setVisibility(View.VISIBLE);
            }
        });

    }

    private void getData(int isRefresh) {
        mCache = ACache.get(getActivity());
        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                + SPUtils.getInstance().getInt("projectID") + ":recentdownload");

        if (tbFileShowmodelString != null && !tbFileShowmodelString.equals("null")) {
            recentDownLoad.setVisibility(View.VISIBLE);
            noRecentDownLoad.setVisibility(View.GONE);
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
                rv_downed.setAdapter(adapter);
            } else if (isRefresh == 1) {
                //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                if (showList != null && showList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                } else if (showList != null && showList.size() > 0 && adapter == null) {
                    adapter = new DataFileAdapter(getActivity(), showList, projectId);
                    rv_downed.setAdapter(adapter);
                }
            }
        } else {
            recentDownLoad.setVisibility(View.GONE);
            noRecentDownLoad.setVisibility(View.VISIBLE);
        }

    }

    public void refreshRecently(final Boolean isPull) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (isPull) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData(1);
                    }
                });
            }
        }).start();
    }

    //在点击标题栏右侧的清除后，利用evenbus来刷新列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {
        if (commonEven.getInfo().equals("刷新最近下载列表")) {
            refreshRecently(false);
        }
    }

//    /**
//     * 下载按钮方法
//     */
//    private void downLoad(List<TbFileShowmodel> fileList) {
//
//        int isLastDownload=fileList.size();
//        for (int j = 0; j < fileList.size(); j++) {
//            isLastDownload--;
//            downLoadFile(fileList.get(j).getFID(), fileList.get(j).getFileName(), fileList.get(j), isLastDownload);
//        }
//    }
//
//    //文件下载方法
//
//    /**
//     * @param fid
//     * @param fileName
//     * @param tbFileShowmodel 传入TbFileShowmodel对象
//     * @param islastDownLoad  是否为最后一个上传
//     */
//    private void downLoadFile(int fid, final String fileName, final TbFileShowmodel tbFileShowmodel, final int islastDownLoad) {
//
//        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
//                + SPUtils.getInstance().getInt("projectID") + ":recentdownload");
//
//        if (tbFileShowmodelString == null || tbFileShowmodelString.equals("null")) {//此处又是会出现一个"null"待排查
//            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
//                    .getInt("projectID") + ":recentdownload", JSON.toJSONString(tbFileShowmodel),ACache.TIME_DAY*2);
//        } else {
//
//            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
//            StringBuffer sb = new StringBuffer();
//            sb.append(JSON.toJSONString(tbFileShowmodel));
//            sb.append("&@&@&@&@&@");
//            for (int i = tbFileShowmodelArr.length-1; i >= 0; i--) {
//                if (!tbFileShowmodelArr[i].equals(JSON.toJSONString(tbFileShowmodel))) {
//                    sb.append(tbFileShowmodelArr[i]);
//                    if(i>0){
//                        sb.append("&@&@&@&@&@");
//                    }
//                }
//            }
//
//            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
//                    .getInt("projectID") + ":recentdownload", sb.toString(), ACache.TIME_DAY * 2);
//
//        }
//
//        if(islastDownLoad==0){
//            refreshRecently(false);
//        }
//
//        Request request = new Request.Builder()
//                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/EngineeringData/" + fid + "/DownLoad")
//                .build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ToastUtils.showShort("文件下载失败");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//
//                InputStream is = null;
//                byte[] buf = new byte[2048];
//                int len = 0;
//                FileOutputStream fos = null;
//
//                try {
//                    is = response.body().byteStream();
//                    long total = response.body().contentLength();
//                    File file = new File(savePath, fileName);
//                    fos = new FileOutputStream(file);
//                    long sum = 0;
//                    while ((len = is.read(buf)) != -1) {
//                        fos.write(buf, 0, len);
//                        sum += len;
//                        final int progress = (int) (sum * 1.0f / total * 100);
//                        // 下载中
//                        LogUtil.i(fileName.toString() + "DataFileFragment下载进度：", "" + progress);
//                    }
//                    // 下载成功
//                    fos.flush();
//
//                } catch (Exception e) {
//                    //下载出错
//                } finally {
//                    try {
//                        if (is != null)
//                            is.close();
//                    } catch (IOException e) {
//                    }
//                    try {
//                        if (fos != null)
//                            fos.close();
//                    } catch (IOException e) {
//                    }
//                }
//            }
//        });
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
