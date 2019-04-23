package com.chenxi.cebim.activity.data;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.view.SuperFileView2;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PictureEditActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout back;
    private LinearLayout upLoadFile,showFileType;
    private com.chenxi.cebim.view.ClearEditText mFileName;
    private ImageView picPreview,fileTypePic;
    private TextView fileTypeName;
    private SuperFileView2 mSuperFileView;

    private String filePath, fileName;
    private double fileSize;
    private int projectID, classID;
    private File file;

    private ProgressDialog progressDialog;

    ACache mCache;

    private ArrayList<TbFileShowmodel> tempList = new ArrayList<>();//获取回调接口中返回的ArrayList<TbFileShowmodel>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_edit);

        mCache = ACache.get(PictureEditActivity.this);

        //数据初始化
        initData();
        initView();
    }

    //数据初始化
    private void initData() {
        //接收从DataFileFragment和DirActivity传过来的数据
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        fileName = FileUtils.getFileName(filePath);
        projectID = intent.getIntExtra("projectID", -1);
        classID = intent.getIntExtra("classID", -1);
        file = new File(filePath);
    }

    //控件初始化
    private void initView() {
        back = (RelativeLayout) findViewById(R.id.rl_pic_edit_back);
        back.setOnClickListener(this);
        upLoadFile = (LinearLayout) findViewById(R.id.ll_pic_edit_upload);
        upLoadFile.setOnClickListener(this);
        mFileName = (com.chenxi.cebim.view.ClearEditText) findViewById(R.id.pic_edit_filename);

        picPreview = (ImageView) findViewById(R.id.iv_preview);
        showFileType=findViewById(R.id.ll_filetype);
        fileTypePic=findViewById(R.id.iv_filetype_pic);
        fileTypeName=findViewById(R.id.tv_filetype);
        mSuperFileView=findViewById(R.id.superFileView_file_edit);

        if(GetFileType.fileType(fileName).equals("图片")){
            picPreview.setVisibility(View.VISIBLE);
            showFileType.setVisibility(View.GONE);
            mSuperFileView.setVisibility(View.GONE);
            Glide.with(this).load(filePath).into(picPreview);
        }else if(GetFileType.fileType(fileName).equals("文档")){
            picPreview.setVisibility(View.GONE);
            showFileType.setVisibility(View.GONE);
            mSuperFileView.setVisibility(View.VISIBLE);
            mSuperFileView.setOnGetFilePathListener(new SuperFileView2.OnGetFilePathListener() {
                @Override
                public void onGetFilePath(SuperFileView2 mSuperFileView2) {
                    mSuperFileView2.displayFile(new File(filePath));
                }
            });
            mSuperFileView.show();
        }else if(GetFileType.fileType(fileName).equals("视频")){
            picPreview.setVisibility(View.GONE);
            showFileType.setVisibility(View.VISIBLE);
            mSuperFileView.setVisibility(View.GONE);

            fileTypePic.setImageResource(R.drawable.video);
            fileTypeName.setText("视频文件");
        }else if(GetFileType.fileType(fileName).equals("音乐")){
            picPreview.setVisibility(View.GONE);
            showFileType.setVisibility(View.VISIBLE);
            mSuperFileView.setVisibility(View.GONE);

            fileTypePic.setImageResource(R.drawable.audio);
            fileTypeName.setText("音频文件");
        }else{
            picPreview.setVisibility(View.GONE);
            showFileType.setVisibility(View.VISIBLE);
            mSuperFileView.setVisibility(View.GONE);

            fileTypePic.setImageResource(R.drawable.unknow_format);
            fileTypeName.setText("未知格式文件");
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_pic_edit_back:
                finish();
                break;

            case R.id.ll_pic_edit_upload:
                //加载加进度条，加班类别请求成功后消失。
                progressDialog = new ProgressDialog(PictureEditActivity.this);
                progressDialog.setMessage("图片上传中...");
                progressDialog.setCancelable(true);
                progressDialog.show();  //将进度条显示出来
                getAnnexMD5Code();
                break;
        }
    }

    //获取AnnexMD5Code
    private void getAnnexMD5Code() {

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(AppConst.innerIp + "/api/AnnexFile")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = JSONObject.parseObject(responseData);
                    String AnnexMd5Code = jsonObject.getString("AnnexMd5Code");
                    upLoadFile(AnnexMd5Code);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    //上传文件
    private void upLoadFile(String AnnexMD5Code) {

        String upLoadFileName = new String();
        if ((mFileName.getText().toString().equals("")) || (mFileName.getText() == null)) {//标题为空，文件名为本地的名字，否则为标题输入框中的名字
            upLoadFileName = fileName;
        } else {
            upLoadFileName = mFileName.getText().toString() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        RequestBody requestBody;
        if (classID == -100) {
            requestBody = new FormBody.Builder()
                    .add("FileName", upLoadFileName)
                    .add("FileID", AnnexMD5Code)
                    .build();
        } else {
            requestBody = new FormBody.Builder()
                    .add("FileName", upLoadFileName)
                    .add("FileID", AnnexMD5Code)
                    .add("ClassID", "" + classID)
                    .build();
        }

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData")
                .post(requestBody)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();  //将进度条隐藏
                ToastUtils.showShort("文件上传失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ToastUtils.showShort("文件上传成功");
                    progressDialog.dismiss();  //将进度条隐藏
                    EventBus.getDefault().post(new IsShowBottomSettingButton("文件上传成功"));
                    try {
                        String responseData = response.body().string();
                        org.json.JSONObject jsonObject = null;
                        jsonObject = new org.json.JSONObject(responseData);

                        int FID = jsonObject.getInt("FID");
                        int ProjectID = jsonObject.getInt("ProjectID");

                        int ClassID;
                        if (jsonObject.get("ClassID").toString().equals("null")) {
                            ClassID = 0;
                        } else {
                            ClassID = jsonObject.getInt("ClassID");
                        }

                        int ParentClassID = 0;

                        int OperationUserID=0;

                        String FileName = jsonObject.getString("FileName");
                        String FileType = "file";
                        String FileID=jsonObject.getString("FileID");

                        Object AddTime = jsonObject.get("AddTime");
                        Object UpdateTime = jsonObject.get("UpdateTime");

                        Boolean IsCheck = false;
                        Boolean IsMove = false;

                        TbFileShowmodel tbFileShowmodel = new TbFileShowmodel(FID, ProjectID, ClassID, ParentClassID, OperationUserID, FileName,
                                FileType,FileID, AddTime, UpdateTime, IsCheck, IsMove);

                        String tbFileShowmodelString=mCache.getAsString(SPUtils.getInstance().getInt("UserID")+":"
                                +SPUtils.getInstance().getInt("projectID")+":recentupload");

                        if(tbFileShowmodelString==null||tbFileShowmodelString.equals("null")){//此处又是会出现一个"null"待排查
                            mCache.put(SPUtils.getInstance().getInt("UserID")+":"+SPUtils.getInstance()
                                    .getInt("projectID")+":recentupload",JSON.toJSONString(tbFileShowmodel),ACache.TIME_DAY*2);
                        }else {

                            String[] tbFileShowmodelArr = tbFileShowmodelString.split("&@&@&@&@&@");
                            StringBuffer sb = new StringBuffer();
                            sb.append(JSON.toJSONString(tbFileShowmodel));
                            sb.append("&@&@&@&@&@");
                                for (int i = 0; i < tbFileShowmodelArr.length; i++) {
                                if (!tbFileShowmodelArr[i].equals(JSON.toJSONString(tbFileShowmodel))) {
                                    sb.append(tbFileShowmodelArr[i]);
                                    if(i <tbFileShowmodelArr.length-1){
                                        sb.append("&@&@&@&@&@");
                                    }
                                }
                            }

                            mCache.put(SPUtils.getInstance().getInt("UserID") + ":" + SPUtils.getInstance()
                                    .getInt("projectID") + ":recentupload", sb.toString(), ACache.TIME_DAY * 2);

                        }
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("文件上传失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSuperFileView != null) {
            mSuperFileView.onStopDisplay();
        }
    }
}
