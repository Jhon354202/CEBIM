package com.chenxi.cebim.fragment.data;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.DataActivity;
import com.chenxi.cebim.activity.data.FileMoveActivity;
import com.chenxi.cebim.adapter.DataFileAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonUseFileModel;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.IsShowDataRadioButtonEven;
import com.chenxi.cebim.entity.RecentDownLoadEvent;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.fragment.BaseFragment;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.DelUnderLine;
import com.chenxi.cebim.utils.GetFileType;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.params.InputParams;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class DataCommonUseFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private RecyclerView recyclerView;
    private RelativeLayout back, bottomBtn, listIsNull;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView search;
    private LinearLayout llSearch, choose, choose_cancel;
    private TextView share, downLoad, commonuse, more, allChoose, allNoChoose, cancle;

    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<TbFileShowmodel> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>
    private List<TbFileShowmodel> searchList = new ArrayList<>();//获取的所有搜索后的对象集合

    private DataFileAdapter adapter;

    private int delNum;

    private int FID, projectID;

    int renameFid;//用于重命名的fid
    int renameClassId;//用于重命名的classID
    private String fileName;

    ACache mCache;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_data_common_use, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        DataActivity da = (DataActivity) getActivity();//从DataActivity获取projectID
        projectID = da.getProjectId();

        initView();
        getData(0);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            getData(0);
        }

    }

    /**
     * 控件初始化
     */
    private void initView() {

        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.common_use_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //列表无数据时的图示界面
        listIsNull = view.findViewById(R.id.rl_common_list_is_null);

        //底部导航栏
        bottomBtn = (RelativeLayout) view.findViewById(R.id.rl_common_use_bottom_btn);

        //选择
        choose = view.findViewById(R.id.ll_common_use_choose);
        choose.setOnClickListener(this);

        //全选、全不选、取消三个控件的父控件
        choose_cancel = view.findViewById(R.id.ll_common_use_cancel);
        choose_cancel.setOnClickListener(this);

        //全选
        allChoose = (TextView) view.findViewById(R.id.tv_common_use_all_choose);
        allChoose.setOnClickListener(this);

        //全不选
        allNoChoose = (TextView) view.findViewById(R.id.tv_common_use_all_no_choose);
        allNoChoose.setOnClickListener(this);

        //取消
        cancle = (TextView) view.findViewById(R.id.tv_common_use_cancel);
        cancle.setOnClickListener(this);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.common_use_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        //返回
        back = (RelativeLayout) view.findViewById(R.id.rl_common_use_back);
        back.setOnClickListener(this);

        //搜索
        search = (SearchView) view.findViewById(R.id.sv_common_use);
        search.setVisibility(View.VISIBLE);//搜索框初始化时默认显示
        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) search.findViewById(R.id.search_src_text);
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

        llSearch = (LinearLayout) view.findViewById(R.id.ll_common_use_search_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });

        share = (TextView) view.findViewById(R.id.tv_common_use_share_file);
        share.setOnClickListener(this);

        downLoad = (TextView) view.findViewById(R.id.tv_common_use_download);
        downLoad.setOnClickListener(this);

        commonuse = (TextView) view.findViewById(R.id.tv_common_use_common_use);
        commonuse.setOnClickListener(this);

        more = (TextView) view.findViewById(R.id.tv_common_use_more);
        more.setOnClickListener(this);

    }

    /**
     * 获取列表数据源，获取文件数据
     *
     * @param isRefresh 值为1时刷新加载，值为0时进入界面时默认加载
     */
    public void getData(int isRefresh) {
        tempList.clear();
        showList.clear();
        //从数据库中获取对应useid下的commonUseFileModelList
        List<CommonUseFileModel> commonUseFileModelList = LitePal.where("useid = ?",
                "" + SPUtils.getInstance().getInt("UserID")).order("id").find(CommonUseFileModel.class);

        //commonUseFileModelList转成TbFileShowmodel对象list,便于复用DataFileAdapter
        for (int i = 0; i < commonUseFileModelList.size(); i++) {

            int FID = commonUseFileModelList.get(i).getFID();
            int ProjectID = commonUseFileModelList.get(i).getProjectID();
            int ClassID = commonUseFileModelList.get(i).getClassID();
            int ParentClassID = commonUseFileModelList.get(i).getParentClassID();
            int OperationUserID = commonUseFileModelList.get(i).getOperationUserID();

            String FileName = commonUseFileModelList.get(i).getFileName();
            String FileType = commonUseFileModelList.get(i).getFileType();
            String FileID = commonUseFileModelList.get(i).getFileID();

            Object AddTime = commonUseFileModelList.get(i).getAddTime();
            Object UpdateTime = commonUseFileModelList.get(i).getUpdateTime();

            Boolean IsCheck = false;
            Boolean IsMove = false;

            TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                    FileType, FileID, AddTime, UpdateTime, IsCheck, IsMove);

            //数据源
            tempList.add(tbFileShowmodel);
        }

        if (tempList.size() == 0) {
            swipeRefresh.setVisibility(View.GONE);
            listIsNull.setVisibility(View.VISIBLE);
            if (swipeRefresh.isRefreshing()) {
                swipeRefresh.setRefreshing(false);
            }
        } else {
            swipeRefresh.setVisibility(View.VISIBLE);
            listIsNull.setVisibility(View.GONE);
            showList.addAll(tempList);
            if (isRefresh == 0) {
                if (showList != null || showList.size() > 0) {
                    adapter = new DataFileAdapter(MyApplication.getContext(), showList, SPUtils.getInstance().getInt("projectID"));
                    recyclerView.setAdapter(adapter);
                }
                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            } else if (isRefresh == 1) {
                //如果一开始进入界面时列表为空，则adapter是空的。需要这里判断，如果为null，则这这里创建并setAdapter
                if (showList != null && showList.size() > 0 && adapter != null) {
                    adapter.notifyDataSetChanged();
                    if (swipeRefresh.isRefreshing()) {
                        swipeRefresh.setRefreshing(false);
                    }
                } else if (showList != null && showList.size() > 0 && adapter == null) {
                    adapter = new DataFileAdapter(MyApplication.getContext(), showList, SPUtils.getInstance().getInt("projectID"));
                    recyclerView.setAdapter(adapter);
                }
            }
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

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter != null) {
                            getData(1);
                        }
                    }
                });
            }
        }).start();
    }

    //搜索
    private void doSearch(String keyWord) {

        if (adapter == null) return;
        searchList.clear();
        showList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getFileName().contains(keyWord)) {
                searchList.add(tempList.get(i));
            }
        }
        showList.addAll(searchList);
        adapter.notifyDataSetChanged();
    }

    /**
     * 恢复原有界面的方法
     */
    private void reset() {
        choose.setVisibility(View.VISIBLE);
        choose_cancel.setVisibility(View.GONE);
        allChoose.setVisibility(View.VISIBLE);
        allNoChoose.setVisibility(View.GONE);
        bottomBtn.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);

        search.setVisibility(View.VISIBLE);//点击选择按钮时，搜索框隐藏

        EventBus.getDefault().post(new IsShowDataRadioButtonEven("显示"));//发送消息，显示DataActivity中的底部导航栏
        EventBus.getDefault().post(new IsShowBottomSettingButton("关闭底部导航栏"));//用于关闭DataFileFragmetn中的底部导航栏
        bottomBtn.setVisibility(View.GONE);
        adapter.isShowCheckBox(false);
        for (int i = 0; i < adapter.getList().size(); i++) {
            adapter.getList().get(i).setChecked(false);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 下载按钮方法
     */
    private void downLoad() {

        final ArrayList<TbFileShowmodel> downLoadFileList = new ArrayList<>();//被选中的需要设置成常用的对象集合

        Boolean isContainDir = false;//选中的item是否包含文件夹
        for (int i = 0; i < adapter.getList().size(); i++) {
            if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("file")) {
                downLoadFileList.add(adapter.getList().get(i));
            }

            //判断选中的item中是否包含dir
            if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("dir")) {
                isContainDir = true;
            }
        }

        if (isContainDir) {//包含文件夹的情况
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("文件夹暂不支持存在本地,是否继续下载其他文件");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventBus.getDefault().post(new RecentDownLoadEvent(downLoadFileList));//发送消息，需要下载的downLoadFileList发送出去
                    reset();
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("下载资料");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                int isLastDown = downLoadFileList.size();

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EventBus.getDefault().post(new RecentDownLoadEvent(downLoadFileList));//发送消息，需要下载的downLoadFileList发送出去
                    reset();
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
    }

    /**
     * 常用按钮方法
     */
    private void commonUse() {

        final ArrayList<TbFileShowmodel> setCommonUseFileList = new ArrayList<>();//被选中的需要设置成常用的对象集合
        String delCondition = null;//删除的条件

        for (int i = 0; i < adapter.getList().size(); i++) {
            if (adapter.getList().get(i).getChecked()) {
                setCommonUseFileList.add(adapter.getList().get(i));
            }
        }

        //如果选中的item只有一个，则将改选中项直接设为常用，否则弹出对话框
        if (setCommonUseFileList.size() == 1) {

            Boolean isTheSame = false;
            List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);

            if (commonUseFileModels == null || commonUseFileModels.size() == 0) {
                isTheSame = false;
            } else {
                for (int i = 0; i < commonUseFileModels.size(); i++) {
                    if (commonUseFileModels.get(i).getFileType().equals("dir") &&
                            commonUseFileModels.get(i).getClassID() == setCommonUseFileList.get(0).getClassID()) {
                        isTheSame = true;
                        delCondition = "dir:" + setCommonUseFileList.get(0).getClassID();
                        break;
                    } else if (commonUseFileModels.get(i).getFileType().equals("file") &&
                            commonUseFileModels.get(i).getFID() == setCommonUseFileList.get(0).getFID()) {
                        isTheSame = true;
                        delCondition = "file:" + setCommonUseFileList.get(0).getFID();
                        break;
                    }
                }
            }

            if (!isTheSame) {
                CommonUseFileModel commonUseFileModel = new CommonUseFileModel();
                commonUseFileModel.setUseID(SPUtils.getInstance().getInt("UserID"));
                commonUseFileModel.setFID(setCommonUseFileList.get(0).getFID());
                commonUseFileModel.setProjectID(setCommonUseFileList.get(0).getProjectID());
                commonUseFileModel.setClassID(setCommonUseFileList.get(0).getClassID());
                commonUseFileModel.setParentClassID(setCommonUseFileList.get(0).getParentClassID());
                commonUseFileModel.setOperationUserID(setCommonUseFileList.get(0).getOperationUserID());
                commonUseFileModel.setFileName(setCommonUseFileList.get(0).getFileName());
                commonUseFileModel.setFileType(setCommonUseFileList.get(0).getFileType());
                commonUseFileModel.setAddTime(setCommonUseFileList.get(0).getAddTime().toString());
                commonUseFileModel.setUpdateTime(setCommonUseFileList.get(0).getUpdateTime().toString());
                commonUseFileModel.setChecked(setCommonUseFileList.get(0).getChecked());
                commonUseFileModel.setMove(setCommonUseFileList.get(0).getMove());
                commonUseFileModel.save();
                ToastUtils.showShort("设置常用成功");
                EventBus.getDefault().post(new IsShowBottomSettingButton("设置常用成功"));//通知根目录关闭本他自身的底部导航栏

                reset();

            } else {//如果是一样的，则点击一下会取消常用
                String fileType = delCondition.split(":")[0];
                String fileID = delCondition.split(":")[1];

                if (fileType.equals("dir")) {
                    LitePal.deleteAll(CommonUseFileModel.class, "classid = ?", fileID);
                } else if (fileType.equals("file")) {
                    LitePal.deleteAll(CommonUseFileModel.class, "fid = ?", fileID);
                }

                ToastUtils.showShort("取消常用成功");
                EventBus.getDefault().post(new IsShowBottomSettingButton("取消设置常用成功"));//通知根目录关闭本他自身的底部导航栏

                reset();
            }

        } else if (setCommonUseFileList.size() > 1) {

            int commonUseNu = 0;//被勾选且在数据库中存储的数据数量
            List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);
            for (int j = 0; j < setCommonUseFileList.size(); j++) {
                Boolean isTheSame = false;//被选中的和数据库中的是否为相同数据，不一样则添加，否则不添加
                if (commonUseFileModels == null || commonUseFileModels.size() == 0) {
                    isTheSame = false;
                } else {
                    for (int k = 0; k < commonUseFileModels.size(); k++) {
                        if (commonUseFileModels.get(k).getFileType().equals("dir") &&
                                commonUseFileModels.get(k).getClassID() == setCommonUseFileList.get(j).getClassID()) {
                            commonUseNu++;
                            break;
                        } else if (commonUseFileModels.get(k).getFileType().equals("file") &&
                                commonUseFileModels.get(k).getFID() == setCommonUseFileList.get(j).getFID()) {
                            commonUseNu++;
                            break;
                        }
                    }
                }
            }

            if (commonUseFileModels.size() != 0 && commonUseNu == setCommonUseFileList.size()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("将这些文件取消常用？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);

                        for (int j = 0; j < setCommonUseFileList.size(); j++) {
                            for (int k = 0; k < commonUseFileModels.size(); k++) {
                                if (commonUseFileModels.get(k).getFileType().equals("dir") &&
                                        commonUseFileModels.get(k).getClassID() == setCommonUseFileList.get(j).getClassID() &&
                                        commonUseFileModels.get(k).getFileName().equals(setCommonUseFileList.get(j).getFileName())) {
                                    LitePal.deleteAll(CommonUseFileModel.class, "classid = ?", "" + commonUseFileModels.get(k).getClassID());
                                    break;
                                } else if (commonUseFileModels.get(k).getFileType().equals("file") &&
                                        commonUseFileModels.get(k).getFID() == setCommonUseFileList.get(j).getFID() &&
                                        commonUseFileModels.get(k).getFileName().equals(setCommonUseFileList.get(j).getFileName())) {
                                    LitePal.deleteAll(CommonUseFileModel.class, "fid = ?", "" + commonUseFileModels.get(k).getFID());
                                    break;
                                }
                            }
                        }

                        reset();
                        ToastUtils.showShort("取消常用成功");
                        EventBus.getDefault().post(new IsShowBottomSettingButton("取消设置常用成功"));//通知根目录关闭本他自身的底部导航栏
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("将这些文件设为常用？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        int commonUseNu = 0;//被勾选且在数据库中存储的数据数量

                        for (int j = 0; j < setCommonUseFileList.size(); j++) {
                            Boolean isTheSame = false;//被选中的和数据库中的是否为相同数据，不一样则添加，否则不添加
                            List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);

                            if (commonUseFileModels == null || commonUseFileModels.size() == 0) {
                                isTheSame = false;
                            } else {
                                for (int k = 0; k < commonUseFileModels.size(); k++) {
                                    if (commonUseFileModels.get(k).getFileType().equals("dir") &&
                                            commonUseFileModels.get(k).getClassID() == setCommonUseFileList.get(j).getClassID()) {
                                        isTheSame = true;
                                        commonUseNu++;
                                        break;
                                    } else if (commonUseFileModels.get(k).getFileType().equals("file") &&
                                            commonUseFileModels.get(k).getFID() == setCommonUseFileList.get(j).getFID()) {
                                        isTheSame = true;
                                        commonUseNu++;
                                        break;
                                    }
                                }
                            }

                            if (commonUseNu == setCommonUseFileList.size()) {
                                //若被选中的已全部设置成常用，则批量取消它们的常用
                                ToastUtils.showShort("取消设置常用成功！");
                                EventBus.getDefault().post(new IsShowBottomSettingButton("取消设置常用成功"));//通知根目录关闭本他自身的底部导航栏
                            } else {
                                //否则把其中未设置成常用的设置成常用
                                if (!isTheSame) {
                                    CommonUseFileModel commonUseFileModel = new CommonUseFileModel();
                                    commonUseFileModel.setUseID(SPUtils.getInstance().getInt("UserID"));
                                    commonUseFileModel.setFID(setCommonUseFileList.get(j).getFID());
                                    commonUseFileModel.setProjectID(setCommonUseFileList.get(j).getProjectID());
                                    commonUseFileModel.setClassID(setCommonUseFileList.get(j).getClassID());
                                    commonUseFileModel.setParentClassID(setCommonUseFileList.get(j).getParentClassID());
                                    commonUseFileModel.setOperationUserID(setCommonUseFileList.get(j).getOperationUserID());
                                    commonUseFileModel.setFileName(setCommonUseFileList.get(j).getFileName());
                                    commonUseFileModel.setFileType(setCommonUseFileList.get(j).getFileType());
                                    commonUseFileModel.setAddTime(setCommonUseFileList.get(j).getAddTime().toString());
                                    commonUseFileModel.setUpdateTime(setCommonUseFileList.get(j).getUpdateTime().toString());
                                    commonUseFileModel.setChecked(setCommonUseFileList.get(j).getChecked());
                                    commonUseFileModel.setMove(setCommonUseFileList.get(j).getMove());
                                    commonUseFileModel.save();
                                }
                            }
                        }
                        reset();
                        ToastUtils.showShort("设置常用成功");
                        EventBus.getDefault().post(new IsShowBottomSettingButton("设置常用成功"));//通知根目录关闭本他自身的底部导航栏
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        }

    }

    /**
     * 底部导航栏更多——列表
     */
    private void dialogList() {
        final String items[] = {"删除", "移动", "重命名"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("更多操作");

        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (items[which].equals("删除")) {

                    if (adapter == null) return;
                    delNum = 0;

                    for (int i = 0; i < adapter.getList().size(); i++) {
                        if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("dir")) {
                            delNum++;
                            delDir(adapter.getList().get(i).getClassID());
                        } else if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("file")) {
                            delNum++;
                            delFile(adapter.getList().get(i).getFID());
                        }
                    }

                } else if (items[which].equals("移动")) {

                    ArrayList<TbFileShowmodel> moveFileList = new ArrayList<>();//装被选中的项目参数
                    ArrayList<TbFileShowmodel> dirList = new ArrayList<>();//装未被选中的文件夹

                    for (int i = 0; i < adapter.getList().size(); i++) {
                        if (adapter.getList().get(i).getChecked()) {
                            moveFileList.add(adapter.getList().get(i));
                        }

                        //获取可以被转移进入的文件夹
                        if (adapter.getList().get(i).getFileType().equals("dir") && !adapter.getList().get(i).getChecked()) {
                            dirList.add(adapter.getList().get(i));
                        }
                    }

//                    SPUtils.getInstance().put("rootDirListString", JSON.toJSONString(dirList));//把dirList转成String后持久化,用于FileMoveActivity中用于移动文件

                    Intent intent = new Intent(getActivity(), FileMoveActivity.class);
                    intent.putExtra("skipType", "外部跳入");
                    intent.putExtra("moveFileList", JSON.toJSONString(moveFileList));
                    intent.putExtra("projectId", projectID);
                    startActivity(intent);

                } else if (items[which].equals("重命名")) {

                    int fileNum = 0;
                    int dirNum = 0;

                    for (int i = 0; i < adapter.getList().size(); i++) {
                        if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("dir")) {
                            renameClassId = adapter.getList().get(i).getClassID();
                            dirNum++;

                        } else if (adapter.getList().get(i).getChecked() && adapter.getList().get(i).getFileType().equals("file")) {
                            renameFid = adapter.getList().get(i).getFID();
                            fileName = adapter.getList().get(i).getFileName();
                            fileNum++;
                        }
                    }

                    if (fileNum == 1 && dirNum == 0) {
                        new CircleDialog.Builder(getActivity())
                                //添加标题，参考普通对话框
                                .setTitle("重命名")
                                .setInputHint("请输入新名称")//提示
                                .setInputHeight(120)//输入框高度
                                .setNegative("取消", null)
                                .configInput(new ConfigInput() {
                                    @Override
                                    public void onConfig(InputParams params) {

                                    }
                                }).setPositiveInput("确定", new OnInputClickListener() {
                            @Override
                            public void onClick(String text, View v) {
                                if (text.equals("") || text == null) {
                                    ToastUtils.showShort("新文件名不能为空");
                                } else {
                                    fileReName(text, renameFid);
                                }
                            }
                        }).show();
                    } else if (dirNum == 1 && fileNum == 0) {
                        new CircleDialog.Builder(getActivity())
                                //添加标题，参考普通对话框
                                .setTitle("重命名")
                                .setInputHint("请输入新名称")//提示
                                .setInputHeight(120)//输入框高度
                                .setNegative("取消", null)
                                .configInput(new ConfigInput() {
                                    @Override
                                    public void onConfig(InputParams params) {

                                    }
                                }).setPositiveInput("确定", new OnInputClickListener() {
                            @Override
                            public void onClick(String text, View v) {
                                if (text.equals("") || text == null) {
                                    ToastUtils.showShort("新文件名不能为空");
                                } else {
                                    dirReName(text, renameClassId);
                                }
                            }
                        }).show();
                    } else {
                        ToastUtils.showShort("不可同时重命名多个文件");
                    }
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    //修改文件名
    private void fileReName(final String newName, final int fid) {

        FormBody formBody = new FormBody.Builder()
                .add("FileName", newName + GetFileType.getFileTypeName(fileName))
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid)
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
            }

            @Override
            public void onResponse(Call call, final Response response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                ToastUtils.showShort("文件重命名成功");

                                CommonUseFileModel commonUseFileModel = new CommonUseFileModel();
                                commonUseFileModel.setFileName(newName + GetFileType.getFileTypeName(fileName));
                                commonUseFileModel.updateAll("fid = ?", "" + fid);

                                refreshFile(false);
                                EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//通知DataFileFragment刷新列表

                            } else {
                                ToastUtils.showShort("文件重命名失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件重命名失败");
                        }
                    }
                });
            }
        });
    }

    //修改文件夹名
    private void dirReName(String newName, int classId) {
        FormBody formBody = new FormBody.Builder()
                .add("FileName", newName)
                .build();

        String sre = AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class/" + classId;
        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class/" + classId)
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
                ToastUtils.showShort("文件重命名失败");
            }

            @Override
            public void onResponse(Call call, final Response response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                String str = response.body().string();
                                ToastUtils.showShort("文件重命名成功");
                            } else {
                                ToastUtils.showShort("文件重命名失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件重命名失败");
                        }
                    }
                });
            }
        });
    }


    /**
     * 文件夹删除方法
     *
     * @param classID
     */
    private void delDir(int classID) {

        FormBody formBody = new FormBody.Builder()
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class/" + classID + "/Recycle")
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("文件删除失败");
            }

            @Override
            public void onResponse(Call call, final Response response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                delNum--;
                                LitePal.deleteAll(CommonUseFileModel.class, "classid = ?", "" + classID);
                            } else {
                                ToastUtils.showShort("文件删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件删除失败");
                        }

                        if (delNum == 0) {
                            ToastUtils.showShort("文件删除成功");
                            EventBus.getDefault().post(new IsShowBottomSettingButton("取消设置常用成功"));//通知根目录关闭本他自身的底部导航栏
                            EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//通知DatafileFragment列表刷新
                            reset();
                            refreshFile(false);
                        }
                    }
                });
            }
        });

//        FormBody formBody = new FormBody.Builder().build();
//        Request.Builder builder = new Request.Builder().
//                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class/" + classID)
//                .delete(formBody);
//        Request request = builder.build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                System.out.println(e);
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) {
//
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if (response.code() == 200) {
//                                delNum--;
//                            } else {
//                                ToastUtils.showShort("文件删除失败");
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            ToastUtils.showShort("文件删除失败");
//                        }
//
//                        if (delNum == 0) {
//                            ToastUtils.showShort("文件删除成功");
//                            refreshFile(false);
//                        }
//                    }
//                });
//            }
//        });
    }

    /**
     * 删除文件方法
     *
     * @param fid
     */
    private void delFile(int fid) {
//        FormBody formBody = new FormBody.Builder().build();
//        Request.Builder builder = new Request.Builder().
//                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid)
//                .delete(formBody);
//        Request request = builder.build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                System.out.println(e);
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) {
//
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if (response.code() == 200) {
//                                delNum--;
//                            } else {
//                                ToastUtils.showShort("文件删除失败");
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            ToastUtils.showShort("文件删除失败");
//                        }
//
//                        if (delNum == 0) {
//                            ToastUtils.showShort("文件删除成功");
//                            refreshFile(false);
//                        }
//                    }
//                });
//            }
//        });

        FormBody formBody = new FormBody.Builder()
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid + "/Recycle")
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("文件删除失败");
            }

            @Override
            public void onResponse(Call call, final Response response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> delFidList = new ArrayList<>();
                        try {
                            if (response.code() == 200) {
                                delNum--;
                                LitePal.deleteAll(CommonUseFileModel.class, "fid = ?", "" + fid);
                                delFidList.add("" + fid);
                            } else {
                                ToastUtils.showShort("文件删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件删除失败");
                        }

                        if (delNum == 0) {
                            ToastUtils.showShort("文件删除成功");
                            EventBus.getDefault().post(new IsShowBottomSettingButton("取消设置常用成功"));//通知根目录关闭本他自身的底部导航栏
                            EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//通知DatafileFragment列表刷新

                            //查看最近打开中是否包含这些被删除的文件，如果有，则删除最近打开中的相应文件，并发送消息刷新最近打开列表
                            mCache = ACache.get(getActivity());
                            String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                                    + SPUtils.getInstance().getInt("projectID") + ":recentopent");

                            List<String> tbFileShowmodelStringlist = new ArrayList<>();

                            StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < tbFileShowmodelString.split("&@&@&@&@&@").length; i++) {
                                String str = tbFileShowmodelString.split("&@&@&@&@&@")[i];
                                for (int j = 0; j < delFidList.size(); j++) {
                                    if (!str.contains(delFidList.get(j))) {
                                        sb.append(str);
                                        if (i > 0) {
                                            sb.append("&@&@&@&@&@");
                                        }
                                    }
                                }
                            }
                            mCache.put(SPUtils.getInstance().getInt("UserID") + ":"
                                    + SPUtils.getInstance().getInt("projectID") + ":recentopent", sb.toString());

                            EventBus.getDefault().post(new IsShowBottomSettingButton("常用列表删除成功刷新RecentOpenFragment列表"));//通知RecentOpenFragment列表如果有当前删掉的记录，则那边列表也删除

                            String rrr = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                                    + SPUtils.getInstance().getInt("projectID") + ":recentopent");

                            refreshFile(false);
                        }
                    }
                });
            }
        });
    }


    //EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowBottomSettingButton isShowBottomSettingButton) {
        if (isShowBottomSettingButton.getInfo().equals("设置常用成功") ||
                isShowBottomSettingButton.getInfo().equals("取消设置常用成功")) {
            refreshFile(false);
        } else if (isShowBottomSettingButton.getInfo().equals("关闭底部导航栏")) {
            bottomBtn.setVisibility(View.GONE);//关闭底部导航栏
            swipeRefresh.setEnabled(true);//打开下拉刷新
        } else if (isShowBottomSettingButton.getInfo().equals("打开底部导航栏")) {
            bottomBtn.setVisibility(View.VISIBLE);//打开底部导航栏
            swipeRefresh.setEnabled(false);//关闭下拉刷新

            int allSelectedNum = 0;//checkBox=true的item数
            int isCommonUse = 0;//已经在常用数据库中的数据量
            List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);

            for (int i = 0; i < adapter.getList().size(); i++) {
                if (adapter.getList().get(i).getChecked()) {
                    for (int j = 0; j < commonUseFileModels.size(); j++) {
                        if (commonUseFileModels.get(j).getProjectID() == adapter.getList().get(i).getProjectID() &&
                                commonUseFileModels.get(j).getFileType().equals(adapter.getList().get(i).getFileType()) &&
                                commonUseFileModels.get(j).getFileName().equals(adapter.getList().get(i).getFileName()) &&
                                commonUseFileModels.get(j).getClassID() == adapter.getList().get(i).getClassID() &&
                                commonUseFileModels.get(j).getFID() == adapter.getList().get(i).getFID()) {
                            isCommonUse++;
                        }
                    }
                    allSelectedNum++;
                }
            }

            if (isCommonUse != 0 && isCommonUse == allSelectedNum) {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ng_common_use_light);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                commonuse.setCompoundDrawables(null, drawable, null, null);
            } else {
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ng_common_use);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                commonuse.setCompoundDrawables(null, drawable, null, null);
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_common_use_back:
                getActivity().finish();
                break;

            case R.id.ll_common_use_choose:
                if (adapter == null) return;
                //右上角添加和选择按钮可见于不可见的逻辑
                choose.setVisibility(View.GONE);
                choose_cancel.setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);//点击选择按钮时，搜索框隐藏

                EventBus.getDefault().post(new IsShowDataRadioButtonEven("隐藏"));//发送消息，隐藏DataActivity中的底部导航栏
                adapter.isShowCheckBox(true);
                adapter.notifyDataSetChanged();
                break;

            case R.id.tv_common_use_all_choose:

                if (adapter == null) return;
                allChoose.setVisibility(View.GONE);
                allNoChoose.setVisibility(View.VISIBLE);

                for (int i = 0; i < adapter.getList().size(); i++) {
                    adapter.getList().get(i).setChecked(true);
                }

                adapter.notifyDataSetChanged();
                bottomBtn.setVisibility(View.VISIBLE);

                Drawable drawable1 = ContextCompat.getDrawable(getActivity(), R.drawable.ng_common_use_light);
                drawable1.setBounds(0, 0, drawable1.getMinimumWidth(), drawable1.getMinimumHeight());
                commonuse.setCompoundDrawables(null, drawable1, null, null);
                break;

            case R.id.tv_common_use_all_no_choose:

                if (adapter == null) return;
                allNoChoose.setVisibility(View.GONE);
                allChoose.setVisibility(View.VISIBLE);
                bottomBtn.setVisibility(View.GONE);
                for (int i = 0; i < adapter.getList().size(); i++) {
                    adapter.getList().get(i).setChecked(false);
                }

                adapter.notifyDataSetChanged();
                bottomBtn.setVisibility(View.GONE);
                Drawable drawable2 = ContextCompat.getDrawable(getActivity(), R.drawable.ng_common_use);
                drawable2.setBounds(0, 0, drawable2.getMinimumWidth(), drawable2.getMinimumHeight());
                commonuse.setCompoundDrawables(null, drawable2, null, null);
                break;

            case R.id.tv_common_use_cancel:
                reset();
                break;

            case R.id.tv_common_use_share_file:
                ToastUtils.showShort("你点击了分享");
                break;

            case R.id.tv_common_use_download:
                downLoad();
                break;

            case R.id.tv_common_use_common_use:
                commonUse();
                break;

            case R.id.tv_common_use_more:
                dialogList();
                break;
        }

    }
}
