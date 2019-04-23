package com.chenxi.cebim.fragment.data;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.CacheDoubleUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.DataActivity;
import com.chenxi.cebim.activity.data.FileMoveActivity;
import com.chenxi.cebim.activity.data.PictureEditActivity;
import com.chenxi.cebim.adapter.DataFileAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.CommonUseFileModel;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.IsShowDataRadioButtonEven;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.fragment.BaseFragment;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.DelUnderLine;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.params.InputParams;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import static com.chenxi.cebim.utils.ImageUtil.getImageAbsolutePath;

public class DataFileFragment extends BaseFragment implements View.OnClickListener {
    private View view;

    private ArrayList<TbFileShowmodel> searchFileList = new ArrayList<>();//获取的所有搜索后的对象集合

    private ArrayList<TbFileShowmodel> showList = new ArrayList<>();//获取的所有搜索后的对象集合
    private ArrayList<TbFileShowmodel> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    private SwipeRefreshLayout swipeRefresh;
    private DataFileAdapter adapter;
    private RecyclerView recyclerView;
    private ImageView add, choose;
    private SearchView search;
    private LinearLayout llSearch;

    private int FID, projectID;
    private RelativeLayout back, bottomBtn, listIsNull;

    private LinearLayout add_choose, choose_cancel;
    private TextView allChoose, allNoChoose, cancle, share, downLoad, commonuse, more;
    ProgressDialog progressDialog;

    private int delNum;

    private Dialog dialog;
    private View inflate;
    private TextView addPic, addDir;
    private Uri uri;

    private String fileName;

    int renameFid;//用于重命名的fid
    int renameClassId;//用于重命名的classID
    ACache mCache;

    // 储存下载文件的目录
    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_data_file, container, false);
        DataActivity da = (DataActivity) getActivity();//从DataActivity获取projectID
        projectID = da.getProjectId();

        mCache = ACache.get(getActivity());
        savePath = "" + DiskCacheDirUtil.getDiskCacheDir(getActivity());// 已下载文件的本地存储地址

        initView(view);//界面初始化及交互

        //加载加进度条，请求成功后消失。
        swipeRefresh.measure(0, 0);
        swipeRefresh.setRefreshing(true);
        getData(0, 0, false);//初始化数据
        return view;
    }

    @Override
    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }
        super.onResume();
    }

    private void initView(View view) {
        //RecyclerView逻辑
        recyclerView = (RecyclerView) view.findViewById(R.id.file_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        bottomBtn = (RelativeLayout) view.findViewById(R.id.rl_bottom_btn);//底部导航栏
        listIsNull = view.findViewById(R.id.rl_file_list_is_null);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.file_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFile(true);
            }
        });

        back = (RelativeLayout) view.findViewById(R.id.rl_data_file_back);
        back.setOnClickListener(this);

        add = (ImageView) view.findViewById(R.id.iv_file_add);
        add.setOnClickListener(this);

        search = (SearchView) view.findViewById(R.id.sv_file);
        search.setVisibility(View.VISIBLE);//搜索框初始化时默认显示
        //用于设置字体字号等
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) search.findViewById(R.id.search_src_text);
        searchAutoComplete.setTextSize(16);

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

        llSearch = (LinearLayout) view.findViewById(R.id.ll_file_search_view);
        //点击llSearch后才使searchView处于展开状态
        llSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);//设置searchView处于展开状态
            }
        });

        add_choose = (LinearLayout) view.findViewById(R.id.ll_add_choose);
        add_choose.setVisibility(View.VISIBLE);

        choose_cancel = (LinearLayout) view.findViewById(R.id.ll_choose_cancel);
        choose_cancel.setVisibility(View.GONE);

        allChoose = (TextView) view.findViewById(R.id.tv_all_choose);
        allChoose.setOnClickListener(this);

        allNoChoose = (TextView) view.findViewById(R.id.tv_all_no_choose);
        allNoChoose.setOnClickListener(this);

        cancle = (TextView) view.findViewById(R.id.tv_cancel);
        cancle.setOnClickListener(this);

        choose = (ImageView) view.findViewById(R.id.iv_file_choose);
        choose.setOnClickListener(this);

        //底部导航栏点击事件
        share = (TextView) view.findViewById(R.id.tv_share_file);
        share.setOnClickListener(this);
        downLoad = (TextView) view.findViewById(R.id.tv_download);
        downLoad.setOnClickListener(this);
        commonuse = (TextView) view.findViewById(R.id.tv_common_use);
        commonuse.setOnClickListener(this);
        more = (TextView) view.findViewById(R.id.tv_more);
        more.setOnClickListener(this);
    }

    //搜索
    private void doSearch(String keyWord) {

        if (adapter == null) return;
        searchFileList.clear();
        showList.clear();

        for (int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getFileName().contains(keyWord)) {
                searchFileList.add(tempList.get(i));
            }
        }
        showList.addAll(searchFileList);
        adapter.notifyDataSetChanged();

    }

    /**
     * 获取列表数据源,获取文件夹数据
     *
     * @param isRefresh    值为1时刷新加载，值为0时进入界面时默认加载
     * @param isCheck      值为1时可选择，值为0时不可选择
     * @param isAllChoosed 是否全选
     */
    private void getData(final int isRefresh, final int isCheck, final Boolean isAllChoosed) {//isRefresh是否为刷新，isChoose是否为选择模式，1为是，0为否

        CacheDoubleUtils.getInstance().remove("dataFileList" + projectID);//清楚缓存中相应projectID的dataFileList

        Request dirRequest = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class?where=ParentClassID=null and Recycle=false")
                .build();

        MyApplication.getOkHttpClient().newCall(dirRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {

                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);

                    showList.clear();
                    tempList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int FID = -1;
                        int ProjectID = jsonObject.getInt("ProjectID");

                        int ClassID;
                        if (jsonObject.get("ClassID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("ClassID");
                        }

                        int ParentClassID = 0;

                        int OperationUserID;
                        Object OperationUserIDObject = jsonObject.get("OperationUserID");
                        if (!OperationUserIDObject.toString().equals("null")) {
                            OperationUserID = jsonObject.getInt("OperationUserID");
                        } else {
                            OperationUserID = -1;
                        }

                        String FileName = jsonObject.getString("ClassName");
                        String FileType = "dir";
                        String FileID="";

                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");

                        Boolean IsCheck = false;
                        Boolean IsMove = false;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        //数据源
                        tempList.add(tbFileShowmodel);
                    }

                    SPUtils.getInstance().put("rootDirListString", JSON.toJSONString(tempList));//把tempList转成String后持久化,用于FileMoveActivity中用于移动文件

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //获取文件数据
                            getFileData(isRefresh);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
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
            }
        });
    }

    /**
     * 获取列表数据源，获取文件数据
     *
     * @param isRefresh 值为1时刷新加载，值为0时进入界面时默认加载
     */
    private void getFileData(final int isRefresh) {

        Request fileRrequest = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=ClassID=null and Recycle=false")
                .build();

        MyApplication.getOkHttpClient().newCall(fileRrequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i("DataFileFragment的getFileData方法错误信息：", e.getMessage());

                if (swipeRefresh.isRefreshing()) {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                LogUtil.i("DataFileFragment的getFileData方法错误信息：", response.message());
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        FID = jsonObject.getInt("FID");
                        int ProjectID = jsonObject.getInt("ProjectID");

                        int ClassID;
                        if (jsonObject.get("ClassID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("ClassID");
                        }

                        int ParentClassID = -100;
                        int OperationUserID = jsonObject.getInt("OperationUserID");

                        String FileName = jsonObject.getString("FileName");
                        String FileType = "file";
                        String FileID=jsonObject.getString("FileID");

                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");

                        Boolean IsCheck = false;//默认为未选中
                        Boolean IsMove = false;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        //数据源
                        tempList.add(tbFileShowmodel);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (tempList.size() == 0 || tempList == null) {
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
                                    adapter = new DataFileAdapter(MyApplication.getContext(), showList, projectID);
                                    recyclerView.setAdapter(adapter);
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
                                        adapter = new DataFileAdapter(getActivity(), showList, projectID);
                                        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                                        recyclerView.setAdapter(adapter);
                                        if (swipeRefresh.isRefreshing()) {
                                            swipeRefresh.setRefreshing(false);
                                        }
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
            }
        });
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
                        getData(1, 0, false);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_data_file_back://返回
                getActivity().finish();
                break;

            case R.id.iv_file_choose://是否可选
                if (adapter == null) return;
                //右上角添加和选择按钮可见于不可见的逻辑
                add_choose.setVisibility(View.GONE);
                choose_cancel.setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);//点击选择按钮时，搜索框隐藏

                EventBus.getDefault().post(new IsShowDataRadioButtonEven("隐藏"));//发送消息，隐藏DataActivity中的底部导航栏
                adapter.isShowCheckBox(true);
                adapter.notifyDataSetChanged();

                break;

            case R.id.tv_cancel://取消可选

                reset();
                break;

            case R.id.tv_all_choose://全选
                if (adapter == null) return;
                allChoose.setVisibility(View.GONE);
                allNoChoose.setVisibility(View.VISIBLE);

                for (int i = 0; i < adapter.getList().size(); i++) {
                    adapter.getList().get(i).setChecked(true);
                }

                adapter.notifyDataSetChanged();

                EventBus.getDefault().post(new IsShowBottomSettingButton("打开底部导航栏"));//用于打开底部导航栏

                break;

            case R.id.tv_all_no_choose://全不选
                if (adapter == null) return;
                allNoChoose.setVisibility(View.GONE);
                allChoose.setVisibility(View.VISIBLE);
                bottomBtn.setVisibility(View.GONE);
                for (int i = 0; i < adapter.getList().size(); i++) {
                    adapter.getList().get(i).setChecked(false);
                }

                adapter.notifyDataSetChanged();

                break;

            case R.id.iv_file_add://添加文件

                dialog = new Dialog(getActivity(), R.style.ActionSheetDialogStyle);
                inflate = LayoutInflater.from(getActivity()).inflate(R.layout.choose_file_dialog, null);
                addPic = (TextView) inflate.findViewById(R.id.add_picture);
                addPic.setOnClickListener(this);
                addDir = (TextView) inflate.findViewById(R.id.add_dir);
                addDir.setOnClickListener(this);
                dialog.setContentView(inflate);
                Window dialogWindow = dialog.getWindow();
                dialogWindow.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.y = 0;
                dialogWindow.setAttributes(lp);
                dialog.show();

                break;

            case R.id.add_picture://图片选择器
                // 进入相册 以下是例子：用不到的api可以不写
                PictureSelector.create(DataFileFragment.this)
                        .openGallery(PictureMimeType.ofAll())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .maxSelectNum(1)// 最大图片选择数量 int
                        .minSelectNum(1)// 最小选择数量 int
                        .imageSpanCount(3)// 每行显示个数 int
                        .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片 true or false
                        .previewVideo(false)// 是否可预览视频 true or false
                        .enablePreviewAudio(true) // 是否可播放音频 true or false
                        .isCamera(true)// 是否显示拍照按钮 true or false
                        .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath("/DCIM/Camera")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(false)// 是否压缩 true or false
                        .isGif(true)// 是否显示gif图片 true or false
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(false)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(600)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .recordVideoSecond(600)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                dialog.dismiss();
                break;

            case R.id.add_dir://文件选择器
                //选择文件
                dialog.dismiss();
                final String[] items = new String[]{"创建文件夹", "上传文件"};//创建item
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setItems(items, new DialogInterface.OnClickListener() {//添加列表
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {

                                    new CircleDialog.Builder(getActivity())
                                            //添加标题，参考普通对话框
                                            .setTitle("新建文件夹")
                                            .setInputHint("请输入文件夹名称")//提示
                                            .setInputHeight(120)//输入框高度
                                            .setNegative("取消", null)
                                            .configInput(new ConfigInput() {
                                                @Override
                                                public void onConfig(InputParams params) {

                                                }
                                            }).setPositiveInput("确定", new OnInputClickListener() {
                                        @Override
                                        public void onClick(String text, View v) {
                                            Boolean isSamename = false;
                                            for (int i = 0; i < showList.size(); i++) {
                                                if (text.equals(showList.get(i).getFileName())) {
                                                    isSamename = true;
                                                    break;
                                                }
                                            }

                                            if (isSamename) {
                                                ToastUtils.showShort("该文件夹已存在，请重新命名");
                                            } else {
                                                createDir(text);
                                            }
                                        }
                                    }).show();

                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。。
                                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                                    startActivityForResult(intent, 2);
                                    dialog.dismiss();
                                }
                            }
                        })
                        .create();
                alertDialog.show();

                break;

            case R.id.tv_share_file://分享
                break;

            case R.id.tv_download://下载
                downLoad();
                break;

            case R.id.tv_common_use://常用
                commonUse();
                break;

            case R.id.tv_more://更多
                dialogList();
                break;

        }
    }

    /**
     * 恢复原有界面的方法
     */
    private void reset() {
        add_choose.setVisibility(View.VISIBLE);
        choose_cancel.setVisibility(View.GONE);
        allChoose.setVisibility(View.VISIBLE);
        allNoChoose.setVisibility(View.GONE);
        bottomBtn.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);

        search.setVisibility(View.VISIBLE);//点击选择按钮时，搜索框隐藏

        EventBus.getDefault().post(new IsShowDataRadioButtonEven("显示"));//发送消息，显示DataActivity中的底部导航栏
        EventBus.getDefault().post(new IsShowBottomSettingButton("关闭底部导航栏"));//用于关闭DataCommonUserFragment中的底部导航栏
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
//                    EventBus.getDefault().post(new RecentDownLoadEvent(downLoadFileList));//发送消息，需要下载的downLoadFileList发送出去
                    //下载文件
                    //加载加进度条，加班类别请求成功后消失。
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("文件下载中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();  //将进度条显示出来
                    downLoad(downLoadFileList);
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

                    //加载加进度条，加班类别请求成功后消失。
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("文件下载中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();  //将进度条显示出来
                    downLoad(downLoadFileList);
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
     * 下载按钮方法
     */
    private void downLoad(List<TbFileShowmodel> fileList) {

        int isLastDownload = fileList.size();
        for (int j = 0; j < fileList.size(); j++) {
            isLastDownload--;
            downLoadFile(fileList.get(j).getFID(), fileList.get(j).getFileName(), fileList.get(j), isLastDownload);
        }
    }

    //文件下载方法

    /**
     * @param fid
     * @param fileName
     * @param tbFileShowmodel 传入TbFileShowmodel对象
     * @param islastDownLoad  是否为最后一个上传
     */
    private void downLoadFile(int fid, final String fileName, final TbFileShowmodel tbFileShowmodel, final int islastDownLoad) {

        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                + SPUtils.getInstance().getInt("projectID") + ":recentdownload");

        if (tbFileShowmodelString == null || tbFileShowmodelString.equals("null")) {//此处又是会出现一个"null"待排查
            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                    .getInt("projectID") + ":recentdownload", JSON.toJSONString(tbFileShowmodel), ACache.TIME_DAY * 2);
        } else {

            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
            StringBuffer sb = new StringBuffer();
            sb.append(JSON.toJSONString(tbFileShowmodel));
            sb.append("&@&@&@&@&@");
//            for (int i = tbFileShowmodelArr.length - 1; i >= 0; i--) {
            for (int i = 0; i < tbFileShowmodelArr.length; i++) {
                if (!tbFileShowmodelArr[i].equals(JSON.toJSONString(tbFileShowmodel))) {
                    sb.append(tbFileShowmodelArr[i]);
                    if (i <tbFileShowmodelArr.length-1) {
                        sb.append("&@&@&@&@&@");
                    }
                }
            }

            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                    .getInt("projectID") + ":recentdownload", sb.toString(), ACache.TIME_DAY * 2);

        }

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/EngineeringData/" + fid + "/DownLoad")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("文件下载失败");
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
                        final int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        LogUtil.i(fileName.toString() + "DataFileFragment下载进度：", "" + progress);
                    }
                    // 下载成功
                    fos.flush();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (islastDownLoad == 0) {
                                EventBus.getDefault().post(new CommonEven("刷新最近下载列表"));//发送消息，需要下载的downLoadFileList发送出去
                                reset();
                                progressDialog.dismiss();
                                ToastUtils.showLong("文件目录:" + savePath);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.out.println(e);
                    //下载出错
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }
            }
        });
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


    //创建文件夹方法
    private void createDir(String dirName) {

        FormBody formBody = new FormBody
                .Builder()
                .add("ClassName", dirName)
                .add("ParentClassID", "" + null)
                .build();

        Request request = new Request
                .Builder()
                .post(formBody)
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/Class")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    if (response.code() == 200) {
                        ToastUtils.showShort("文件夹创建成功");
                        refreshFile(false);
                    } else {
                        ToastUtils.showShort("文件夹创建失败");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {

            case Activity.RESULT_OK:
                uri = data.getData();
                //文件上传
                if (requestCode == 2) {

                    Intent intent = new Intent(getActivity(), PictureEditActivity.class);
                    intent.putExtra("filePath", getRealFilePath(getActivity(), uri).toString());
                    intent.putExtra("projectID", projectID);
                    intent.putExtra("classID", -100);
                    startActivity(intent);

//                    int isTheSameFile = 0;//用来判别是不是同一个文件
//                    final File file = new File(getRealFilePath(getActivity(), uri).toString());
//                    String str=getRealFilePath(getActivity(), uri).toString();
//                    fileName = FileUtils.getFileName(file);
//                    final double fileSize = file.length() / (1024 * 1024);//待上传文件大小
//
//                    //判断是否有同名文件
//                    for (int i = 0; i < showList.size(); i++) {
//
//                        if (UpLoadFileUtil.getFileName(getRealFilePath(getActivity(), uri).toString())
//                                .equals(showList.get(i).getFileName())) {
//                            isTheSameFile = 1;
//                        }
//                    }
//
//                    if (isTheSameFile == 0) {
//                        //用于网络个性化设置，无网络连接类型
//                        if (!NetworkUtils.isAvailableByPing()) {
//                            ToastUtils.showShort("网络无连接，请连接网络后在试");
//                        } else if (NetworkUtils.is4G()) {//在移动网络下耗流量提醒
//                            AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                                    .setCancelable(false)
//                                    .setNegativeButton("取消", null)
//                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//
//                                            getAnnexMD5Code(file);
//
////                                            if (NumberUtil.doubleToString(fileSize) > 3.00) {
////                                                //大于3M的文件用大文件上传
//////                                                upLoadBigFile(getRealFilePath(UpLoadFileActivity.this, uri).toString());
////                                            } else {
////                                                //小于3M的文件用普通上传
////                                                upLoadFile(getRealFilePath(UpLoadFileActivity.this, uri).toString());
////                                            }
//                                        }
//                                    })
//                                    .setMessage("当前文件大小为" + fileSize + "M,您正在使用移动网络，继续上传将消耗流量").create();
//
//                            dialog.show();
//
//                        } else {//在WiFi连接状态下
//
//                            getAnnexMD5Code(file);
//
////                            if (NumberUtil.doubleToString(fileSize) > 3.00) {
////                                //大于3M的文件用大文件上传
////                                upLoadBigFile(getRealFilePath(UpLoadFileActivity.this, uri).toString());
////                            } else {
////                                //小于3M的文件用普通上传
////                                upLoadFile(getRealFilePath(UpLoadFileActivity.this, uri).toString());
////                            }
//                        }
//
//                    } else {
//                        ToastUtils.showShort("已有同名文件，请修改文件名");
//                    }

                } else if (requestCode == PictureConfig.CHOOSE_REQUEST && data != null) {//图片上传
                    List<LocalMedia> selectList = new ArrayList<>();
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);

                    Intent intent = new Intent(getActivity(), PictureEditActivity.class);
                    intent.putExtra("filePath", selectList.get(0).getPath());
//                    intent.putExtra("TbFileObject", selectList.get(0).getPath());
                    intent.putExtra("projectID", projectID);
                    intent.putExtra("classID", -100);

                    startActivity(intent);

                } else {
                    ToastUtils.showShort("文件获取失败！");
                }
                break;
        }
    }

    //根据uri返回绝对路径
    private static String getRealFilePath(Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
            if (data == null) {
                data = getImageAbsolutePath(context, uri);
            }

        }
        return data;
    }

//    //获取AnnexMD5Code
//    private void getAnnexMD5Code(File file) {
//
//        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
//
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", fileName, fileBody)
//                .build();
//
//        Request request = new Request.Builder()
//                .post(requestBody)
//                .url(AppConst.innerIp + "/api/AnnexFile")
//                .build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                try {
//                    String responseData = response.body().string();
//                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(responseData);
//                    String AnnexMd5Code = jsonObject.getString("AnnexMd5Code");
//                    upLoadFile(AnnexMd5Code);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

//    //上传文件
//    private void upLoadFile(String AnnexMD5Code) {
//
//        RequestBody requestBody = new FormBody.Builder()
//
//                .add("FileName", fileName)
//                .add("FileID", AnnexMD5Code)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData")
//                .post(requestBody)
//                .build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                LogUtil.i("DataFileFragment文件上传失败", e.getMessage());
//                ToastUtils.showShort("文件上传失败");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseData = response.body().string();
//                if (response.code() == 200) {
//                    LogUtil.i("DataFileFragment文件上传成功", response.message());
//                    ToastUtils.showShort("文件上传成功");
//                    refreshFile(false);
//                } else {
//                    LogUtil.i("DataFileFragment文件上传出错", response.message());
//                    ToastUtils.showShort("文件上传出错");
//                }
//            }
//        });
//    }

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

                    if (dirList == null || dirList.size() == 0) {
                        ToastUtils.showShort("不能同时选中根目录所有文件夹");
                        return;
                    }

                    SPUtils.getInstance().put("rootDirListString", JSON.toJSONString(dirList));//把dirList转成String后持久化,用于FileMoveActivity中用于移动文件

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
    private void fileReName(String newName, int fid) {

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
                                refreshFile(false);
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
                .add("ClassName", newName)
                .build();

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
                                ToastUtils.showShort("文件重命名成功");
                                refreshFile(false);
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
                            } else {
                                ToastUtils.showShort("文件删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件删除失败");
                        }

                        if (delNum == 0) {
                            ToastUtils.showShort("文件删除成功");
                            refreshFile(false);
                        }
                    }
                });
            }
        });
    }

    /**
     * 删除文件方法
     *
     * @param fid
     */
    private void delFile(int fid) {

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
                        try {
                            if (response.code() == 200) {
                                delNum--;
                            } else {
                                ToastUtils.showShort("文件删除失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("文件删除失败");
                        }

                        if (delNum == 0) {
                            ToastUtils.showShort("文件删除成功");
                            refreshFile(false);
                        }
                    }
                });
            }
        });
    }

    //EventBus 打开底部导航栏（分享、打开、常用、更多）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IsShowBottomSettingButton isShowBottomSettingButton) {
        if (isShowBottomSettingButton.getInfo().equals("关闭底部导航栏")) {
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

        } else if (isShowBottomSettingButton.getInfo().equals("文件上传成功")) {
            refreshFile(false);
        } else if (isShowBottomSettingButton.getInfo().equals("刷新根目录文件")) {
            refreshFile(false);
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
