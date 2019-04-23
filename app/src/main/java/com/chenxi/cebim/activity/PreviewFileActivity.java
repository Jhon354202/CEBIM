package com.chenxi.cebim.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chenxi.cebim.R;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.chenxi.cebim.utils.PermissionUtil;
import com.chenxi.cebim.view.SuperFileView2;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

//预览文件
public class PreviewFileActivity extends BaseActivity {

    private com.tencent.smtt.sdk.WebView webView;//腾讯内核的webview
    private RelativeLayout back, previewFail;
    private SuperFileView2 mSuperFileView;
    private com.bm.library.PhotoView showPic;
    private TextView title, failTips;
    private String fileName, fileID;
    private int fid,projectID;
    // 储存下载文件的目录
    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面
    String TAG = "PreviewFileActivity";

    ACache mCache;
    List<TbFileShowmodel> tbFileShowmodelList;
    TbFileShowmodel tbFileShowmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview_file);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//（这个对宿主没什么影响，建议声明）
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //开启READ_PHONE_STATE权限
        PermissionUtil.addPermission(PreviewFileActivity.this, Manifest.permission.READ_PHONE_STATE, "预览需要开启READ_PHONE_STATE权限");

        initView();
        initData();

    }

    //初始化界面
    private void initView() {

        back = (RelativeLayout) findViewById(R.id.rl_preview_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        previewFail = (RelativeLayout) findViewById(R.id.rl_preview_faild);

        title = (TextView) findViewById(R.id.preview_title);
        failTips = (TextView) findViewById(R.id.tv_preview_fail);

        webView = (com.tencent.smtt.sdk.WebView) findViewById(R.id.webview);
        mSuperFileView = (SuperFileView2) findViewById(R.id.mSuperFileView);
        showPic = findViewById(R.id.iv_show_download_pic);
        showPic.enable();//图片可缩放
    }

    //初始化数据
    private void initData() {
        //接收从SingleFileActivity过来的数据
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        fileID = intent.getStringExtra("fileID");
        fid = intent.getIntExtra("FID",-1);
        projectID = intent.getIntExtra("projectID", -1);

        savePath = "" + DiskCacheDirUtil.getDiskCacheDir(PreviewFileActivity.this);// 已下载文件的本地存储地址
        title.setText(fileName);

        getFileData();

        openFile(mSuperFileView);
    }

    /**
     * 获取列表数据源，获取文件数据
     */
    private void getFileData() {

        tbFileShowmodelList = new ArrayList<>();

        String str=AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=FileID=" + fileID;

        Request fileRrequest = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=FID=" + fid)
//                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData?where=FileID=" + fileID)
                .build();

        MyApplication.getOkHttpClient().newCall(fileRrequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i("PreviewFileActivity的getFileData方法错误信息：", e.getMessage());
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
                            int FID = jsonObject.getInt("FID");
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
                            String FileID = jsonObject.getString("FileID");

                            Object AddTime = jsonObject.get("AddTime");
                            Object UpdateTime = jsonObject.get("UpdateTime");

                            Boolean IsCheck = false;//默认为未选中
                            Boolean IsMove = false;

                            tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                    FileType, FileID, AddTime, UpdateTime, IsCheck, IsMove);
                        }

                        //把打开的文件存入缓存,key："userID:projectID:recentopent"、value:打开的对象
                        mCache = ACache.get(PreviewFileActivity.this);

                        String tbFileShowmodelString = mCache.getAsString(SPUtils.getInstance().getInt("UserID") + ":"
                                + SPUtils.getInstance().getInt("projectID") + ":recentopent");

                        if (tbFileShowmodelString == null || tbFileShowmodelString.equals("null")) {//此处又是会出现一个"null"待排查
                            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                                    .getInt("projectID") + ":recentopent", JSON.toJSONString(tbFileShowmodel), ACache.TIME_DAY * 2);
                        } else {
                            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
                            StringBuffer sb = new StringBuffer();
                            sb.append(JSON.toJSONString(tbFileShowmodel));
                            sb.append("&@&@&@&@&@");
                            for (int i = 0; i < tbFileShowmodelArr.length; i++) {
                                if (!tbFileShowmodelArr[i].equals(JSON.toJSONString(tbFileShowmodel))) {
                                    sb.append(tbFileShowmodelArr[i]);
                                    if (i < tbFileShowmodelArr.length - 1) {
                                        sb.append("&@&@&@&@&@");
                                    }
                                }
                            }

                            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                                    .getInt("projectID") + ":recentopent", sb.toString(), ACache.TIME_DAY * 2);

                        }

                        EventBus.getDefault().post(new IsShowBottomSettingButton("刷新最近打开列表"));//刷新最近打开列表

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


    //文件下载方法
    private void openFile(SuperFileView2 mSuperFileView2) {

        if (GetFileType.fileType(savePath + "/" + fileName).equals("图片")) {
            previewFail.setVisibility(View.GONE);
            mSuperFileView.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);//请勿删除，用于改成X5内核的形式
            showPic.setVisibility(View.VISIBLE);

            //显示图片
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.load_fail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(PreviewFileActivity.this)
//                    .load(savePath + "/" + fileName)
                    .load(AppConst.innerIp + "/api/AnnexFile/"+fileID+"?isArt=true")
                    .apply(options)
                    .into(showPic);

        } else if (GetFileType.fileType(savePath + "/" + fileName).equals("文档")) {

            previewFail.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            showPic.setVisibility(View.GONE);
            mSuperFileView.setVisibility(View.VISIBLE);
            mSuperFileView.setOnGetFilePathListener(new SuperFileView2.OnGetFilePathListener() {
                @Override
                public void onGetFilePath(SuperFileView2 mSuperFileView2) {
                    mSuperFileView2.displayFile(new File(savePath + "/" + fileName));
                }
            });
            mSuperFileView.show();


        } else {
            previewFail.setVisibility(View.VISIBLE);
            failTips.setText("文件格式不明,打开失败");
        }
    }


    private void loadUrl(String url) {
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        if (GetFileType.fileType(url).equals("图片") || GetFileType.fileType(url).equals("文档")) {
            //支持屏幕缩放
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);

            //不显示webview缩放按钮
            webSettings.setDisplayZoomControls(false);
        }

        //内容自适应，居中显示
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //自动播放
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView var1, int var2, String var3, String var4) {
                Log.i("打印日志", "网页加载失败");
            }
        });
        //进度条
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    Log.i("打印日志", "加载完成");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) webView.destroy();

        if (mSuperFileView != null) {
            mSuperFileView.onStopDisplay();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }


}
