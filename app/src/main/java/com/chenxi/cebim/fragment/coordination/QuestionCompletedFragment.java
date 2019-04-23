package com.chenxi.cebim.fragment.coordination;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
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
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.chenxi.cebim.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 协同管理——问题——已完成Fragment
 */
public class QuestionCompletedFragment extends BaseFragment {

    View view;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private RelativeLayout listIsnull;

    private QuestionAdapter adapter;
    private List<QuestionModel> completeQuestionList = new ArrayList<>();//获取回调接口中返回的

    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面

    Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question_completed, container, false);
        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        savePath = "" + DiskCacheDirUtil.getDiskCacheDir(mActivity);// 已下载文件的本地存储地址
        initView();
        swipeRefresh.setRefreshing(true);
        getData(false);//数据初始化
        return view;
    }

    private void initView() {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.complete_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(layoutManager);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.complete_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        listIsnull = view.findViewById(R.id.rl_complete_list_is_null);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity=activity;
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

                mActivity.runOnUiThread(new Runnable() {//getActivity()获取不到Activity
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
        completeQuestionList.clear();
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
                                    ID, Title, Comment, GroupId,Category, ViewportId, SystemType, At, Pictures,
                                    Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers,DocumentIds,UserName,CategoryName,SystemTypeName,firstFrame,
                                    ObservedUsers,State, Observed, IsFinishedAndDelay,CompletedAt, Deadline, Date, LastUpdate);

                            //数据源
                            completeQuestionList.add(questionModel);
                        }

                        for (int i = 0; i < completeQuestionList.size(); i++) {

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

                            if (completeQuestionList.get(i).getPictures() != null && (!completeQuestionList.get(i).getPictures().equals("null")) &&
                                    (!completeQuestionList.get(i).getPictures().equals("[]"))) {
                                //解析字符串
                                String[] arr = completeQuestionList.get(i).getPictures()
                                        .replace("{", "").replace("}", "")
                                        .replace("[", "").replace("]", "").split(",");

                                String id = arr[1].split(":")[1].replace("\"", "").replace("\"", "").toString();
                                String picUrl = AppConst.innerIp + "/api/AnnexFile/" + id;
                                completeQuestionList.get(i).setFirstFrame(picUrl);
                            }

                        }

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (!isPull) {
                                    adapter = new QuestionAdapter(MyApplication.getContext(), completeQuestionList);
                                    recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
                                    recyclerView.setAdapter(adapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                } else {
                                    //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                                    if (completeQuestionList != null && completeQuestionList.size() > 0 && adapter != null) {
                                        adapter.notifyDataSetChanged();
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
                                    } else if (completeQuestionList != null && completeQuestionList.size() > 0 && adapter == null) {
                                        adapter = new QuestionAdapter(MyApplication.getContext(), completeQuestionList);
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

    //显示视频第一帧
    private void getFirstFrame(String fileName, String fileID) {
        String str = AppConst.innerIp + "/api/AnnexFile/" + fileID;

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/" + fileID)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("打开文件失败");
            }

            @Override
            public void onResponse(Call call, Response response) {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, fileName);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                    }
                    fos.flush();
                    // 下载完成

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            if (progressDialog != null) {
//                                progressDialog.dismiss();
//                            }
                            MediaUtils.getImageForVideo(savePath + "/" + fileName, new MediaUtils.OnLoadVideoImageListener() {
                                @Override
                                public void onLoadImage(File file) {
                                    File f = file;
                                    System.out.println(f);
                                }
                            });


                            //视频和音频直接打开第三方播放工具，文档和图片跳转到预览界面
                            if (GetFileType.fileType(fileName).equals("视频")) {
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(savePath + "/" + fileName);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(mActivity, mActivity.getApplicationContext().getPackageName() + ".FileProvider", file);
                                    intent.setDataAndType(contentUri, "video/*");
                                } else {
                                    uri = Uri.fromFile(file);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(uri, "video/*");
                                }
                                startActivity(intent);

                            }
                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("SingleFileActivity文件下载出错信息", e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ToastUtils.showShort(e.getMessage().toString());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//
//                if (response.code() == 200) {
//                    try {
//                        String responseData = null;
//                        responseData = response.body().string();
//                        JSONObject jsonObject = new JSONObject(responseData);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        ToastUtils.showShort("数据解析出错");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        ToastUtils.showShort("数据请求出错");
//                    }
//
//                } else {
//                    ToastUtils.showShort("登陆失败，请检查用户名和密码");
//                }
//            }
//        });
    }

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(NewQuestionDelEven newQuestionDelEven) {
        if (newQuestionDelEven.getInfo().contains("true")) {//从NewQuestion返回的信息，如果为true则刷新该页
            getData(false);//数据初始化
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
