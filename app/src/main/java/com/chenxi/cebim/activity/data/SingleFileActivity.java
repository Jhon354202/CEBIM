package com.chenxi.cebim.activity.data;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.activity.PlayVedioActivity;
import com.chenxi.cebim.activity.PreviewFileActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonUseFileModel;
import com.chenxi.cebim.entity.DataFile;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.params.InputParams;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;

import org.greenrobot.eventbus.EventBus;
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

public class SingleFileActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = "SingleFileActivity";
    private String fileName,fileID, tbFileShowmodelString;
    private int fid, projectID, classID;

    private TextView title, mFileName, preview, openFile, isDownLoad, shareFile, downLoadAndOpenFile, commonUse, more;
    private LinearLayout isDownLoadOrNot;
    private RelativeLayout back;

    DataFile dataFile;
    String savePath;//保存到本地SDCard/Android/data/你的应用包名/cache下面
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_file);

        savePath = "" + DiskCacheDirUtil.getDiskCacheDir(SingleFileActivity.this);// 已下载文件的本地存储地址

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        fid = intent.getIntExtra("FID", -1);
        fileID=intent.getStringExtra("fileID");
        projectID = intent.getIntExtra("projectID", -1);
        classID = intent.getIntExtra("classID", -1);
        tbFileShowmodelString = intent.getStringExtra("tbFileShowmodelString");

        initView();
    }

    //初始化控件
    private void initView() {
        back = (RelativeLayout) findViewById(R.id.rl_single_file_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        title = (TextView) findViewById(R.id.single_file_title);//标题
        title.setText(fileName);
        mFileName = (TextView) findViewById(R.id.tv_single_file_name);//文件名
        mFileName.setText(fileName);
        preview = (TextView) findViewById(R.id.tv_is_preview);//预览

        openFile = (TextView) findViewById(R.id.tv_open_file);
        openFile.setOnClickListener(this);

        isDownLoadOrNot = (LinearLayout) findViewById(R.id.ll_is_download);//是否下载,若已下载，显示这行，若为下载，隐藏此行

        shareFile = (TextView) findViewById(R.id.tv_share_file);//分享文件
        shareFile.setOnClickListener(this);

        downLoadAndOpenFile = (TextView) findViewById(R.id.tv_download_open);//下载并打开
        downLoadAndOpenFile.setOnClickListener(this);

        commonUse = (TextView) findViewById(R.id.tv_common_use);//常用
        commonUse.setOnClickListener(this);

        List<CommonUseFileModel> commonUseFileModelList = LitePal.where("fid = ?", "" + fid).find(CommonUseFileModel.class);
        if (commonUseFileModelList != null && commonUseFileModelList.size() > 0) {
            Drawable drawable = ContextCompat.getDrawable(SingleFileActivity.this, R.drawable.ng_common_use_light);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            commonUse.setCompoundDrawables(null, drawable, null, null);
        } else if (commonUseFileModelList == null || commonUseFileModelList.size() == 0) {
            Drawable drawable = ContextCompat.getDrawable(SingleFileActivity.this, R.drawable.ng_common_use);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            commonUse.setCompoundDrawables(null, drawable, null, null);
        }

        more = (TextView) findViewById(R.id.tv_more);//更多
        more.setOnClickListener(this);

    }

    //文件下载方法
    private void downLoadFile() {

        //加载加进度条，加班类别请求成功后消失。
        progressDialog = new ProgressDialog(SingleFileActivity.this);
        progressDialog.setMessage("文件下载中...");
        progressDialog.setCancelable(true);
        progressDialog.show();  //将进度条显示出来

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid + "/DownLoad")
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }

                            //视频和音频直接打开第三方播放工具，文档和图片跳转到预览界面
                            if (GetFileType.fileType(fileName).equals("视频")) {

                                Intent intent=new Intent(SingleFileActivity.this, PlayVedioActivity.class);
                                intent.putExtra("vedioUrl",AppConst.savePath + "/" + fileName);
                                intent.putExtra("vedioname",fileName);
                                startActivity(intent);

                            } else if (GetFileType.fileType(fileName).equals("音乐")) {

                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(savePath + "/" + fileName);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(SingleFileActivity.this, getApplicationContext().getPackageName() + ".FileProvider", file);
                                    intent.setDataAndType(contentUri, "audio/*");
                                } else {
                                    uri = Uri.fromFile(file);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(uri, "audio/*");
                                }
                                startActivity(intent);
                            } else {

                                //打开和预览该文件
                                Intent intent = new Intent(SingleFileActivity.this, PreviewFileActivity.class);
                                intent.putExtra("fileName", fileName);
                                intent.putExtra("FID", fid);
                                intent.putExtra("fileID", fileID);
                                intent.putExtra("projectID", projectID);
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

    }

    //检测本机是否安装wps
    private boolean checkWps() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");//WPS个人版的包名
        if (intent == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 列表
     */
    private void dialogList() {
        final String items[] = {"删除", "移动", "重命名"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更多操作");
        // builder.setMessage("是否确认退出?"); //设置内容

        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (items[which].equals("删除")) {
                    ToastUtils.showShort("你点击了删除");

                    FormBody formBody = new FormBody.Builder().build();
                    Request.Builder builder = new Request.Builder().
                            url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fid)
                            .delete(formBody);
                    Request request = builder.build();

                    MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.println(e);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (response.code() == 200) {
                                            ToastUtils.showShort("文件删除成功");
                                            if (classID == 0) {
                                                EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//发送消息，删除成功时刷新列表
                                            } else {
                                                EventBus.getDefault().post(new IsShowBottomSettingButton("刷新" + classID + "目录文件"));//发送消息，删除成功时刷新列表
                                            }
                                            finish();
                                        } else {
                                            ToastUtils.showShort("文件删除失败");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtils.showShort("文件删除失败");
                                    }
                                }
                            });
                        }
                    });

                } else if (items[which].equals("移动")) {

                    ArrayList<TbFileShowmodel> moveFileList = new ArrayList<>();//装被选中的项目参数

                    try {
                        JSONObject jsonObject = new JSONObject(tbFileShowmodelString);
                        int FID = jsonObject.getInt("fID");
                        int ProjectID = jsonObject.getInt("projectID");

                        int ClassID;
                        if (jsonObject.get("classID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("classID");
                        }

                        int ParentClassID = jsonObject.getInt("parentClassID");

                        int OperationUserID;
                        Object OperationUserIDObject = jsonObject.get("operationUserID");
                        if (!OperationUserIDObject.toString().equals("null")) {
                            OperationUserID = jsonObject.getInt("operationUserID");
                        } else {
                            OperationUserID = -1;
                        }

                        String FileName = jsonObject.getString("fileName");
                        String FileType = jsonObject.getString("fileType");
                        String FileID=jsonObject.getString("FileID");

                        Object AddTime = jsonObject.get("addTime");
                        Object UpdateTime = jsonObject.get("updateTime");

                        Boolean IsCheck = true;
                        Boolean IsMove = true;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        //数据源
                        moveFileList.add(tbFileShowmodel);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    SPUtils.getInstance().put("subdirClassID", classID);//把classID持久化,用于FileMoveActivity中用于移动文件

                    Intent intent = new Intent(SingleFileActivity.this, FileMoveActivity.class);
                    intent.putExtra("skipType", "外部跳入");
                    intent.putExtra("moveFileList", JSON.toJSONString(moveFileList));
                    intent.putExtra("projectId", projectID);
                    startActivity(intent);

                } else if (items[which].equals("重命名")) {

                    new CircleDialog.Builder(SingleFileActivity.this)
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
                                fileReName(text, fid);
                            }
                        }
                    }).show();
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

    //修改文件名称
    private void fileReName(final String newName, int fid) {

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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                if (classID == 0) {
                                    EventBus.getDefault().post(new IsShowBottomSettingButton("刷新根目录文件"));//发送消息，删除成功时刷新列表
                                } else {
                                    EventBus.getDefault().post(new IsShowBottomSettingButton("刷新" + classID + "目录文件"));//发送消息，删除成功时刷新列表
                                }
                                mFileName.setText(newName + GetFileType.getFileTypeName(fileName));
                                title.setText(newName + GetFileType.getFileTypeName(fileName));
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
     * 常用按钮方法
     */
    private void commonUse() {

        final ArrayList<TbFileShowmodel> setCommonUseFileList = new ArrayList<>();//被选中的需要设置成常用的对象集合
        String delCondition = null;//删除的条件

        try {
            JSONObject jsonObject = new JSONObject(tbFileShowmodelString);
            int FID = jsonObject.getInt("fID");
            int ProjectID = jsonObject.getInt("projectID");

            int ClassID;
            if (jsonObject.get("classID").toString().equals("null")) {
                ClassID = 0;
            } else {
                ClassID = jsonObject.getInt("classID");
            }

            int ParentClassID = jsonObject.getInt("parentClassID");

            int OperationUserID;
            Object OperationUserIDObject = jsonObject.get("operationUserID");
            if (!OperationUserIDObject.toString().equals("null")) {
                OperationUserID = jsonObject.getInt("operationUserID");
            } else {
                OperationUserID = -1;
            }

            String FileName = jsonObject.getString("fileName");
            String FileType = jsonObject.getString("fileType");
            String FileID=jsonObject.getString("FileID");

            Object AddTime = jsonObject.get("addTime");
            Object UpdateTime = jsonObject.get("updateTime");

            Boolean IsCheck = true;
            Boolean IsMove = true;

            TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                    FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

            //数据源
            setCommonUseFileList.add(tbFileShowmodel);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Boolean isTheSame = false;
        List<CommonUseFileModel> commonUseFileModels = LitePal.findAll(CommonUseFileModel.class);

        if (commonUseFileModels == null || commonUseFileModels.size() == 0) {
            isTheSame = false;
        } else {
            for (int i = 0; i < commonUseFileModels.size(); i++) {
                if (commonUseFileModels.get(i).getFileType().equals("file") &&
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

            Drawable drawable = ContextCompat.getDrawable(SingleFileActivity.this, R.drawable.ng_common_use_light);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            commonUse.setCompoundDrawables(null, drawable, null, null);

        } else {//如果是一样的，则点击一下会取消常用
            String fileType = delCondition.split(":")[0];
            String fileID = delCondition.split(":")[1];

            if (fileType.equals("dir")) {
                LitePal.deleteAll(CommonUseFileModel.class, "classid = ?", fileID);
            } else if (fileType.equals("file")) {
                LitePal.deleteAll(CommonUseFileModel.class, "fid = ?", fileID);
            }

            ToastUtils.showShort("取消常用成功");
            Drawable drawable = ContextCompat.getDrawable(SingleFileActivity.this, R.drawable.ng_common_use);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            commonUse.setCompoundDrawables(null, drawable, null, null);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_share_file:
                ToastUtils.showShort("你点击了分享");
                break;

            case R.id.tv_open_file:
            case R.id.tv_download_open:

                if (new File(savePath + "/" + fileName).exists()) {
                    //视频和音频直接打开第三方播放工具，文档和图片跳转到预览界面
                    if (GetFileType.fileType(fileName).equals("视频")) {

                        File file = new File(AppConst.savePath + "/" + fileName);
                        if (file.exists()) {
                            Intent intent=new Intent(SingleFileActivity.this, PlayVedioActivity.class);
                            intent.putExtra("vedioUrl",AppConst.savePath + "/" + fileName);
                            intent.putExtra("vedioname",fileName);
                            startActivity(intent);
                        } else {
                            downLoadFile();
                        }

                    } else if (GetFileType.fileType(fileName).equals("音乐")) {

                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        File file = new File(savePath + "/" + fileName);
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = FileProvider.getUriForFile(SingleFileActivity.this, getApplicationContext().getPackageName() + ".FileProvider", file);
                            intent.setDataAndType(contentUri, "audio/*");
                        } else {
                            uri = Uri.fromFile(file);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(uri, "audio/*");
                        }
                        startActivity(intent);

                    } else {
                        //打开和预览该文件
                        Intent intent = new Intent(SingleFileActivity.this, PreviewFileActivity.class);
                        intent.putExtra("fileName", fileName);
                        intent.putExtra("FID", fid);
                        intent.putExtra("fileID", fileID);
                        intent.putExtra("projectID", projectID);
                        startActivity(intent);
                    }
                } else {
                    downLoadFile();
                }
                break;

            case R.id.tv_common_use:
                commonUse();
                break;

            case R.id.tv_more:
                dialogList();
                break;
        }
    }
}
