package com.chenxi.cebim.activity.coordination;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.DocumentAdapter;
import com.chenxi.cebim.adapter.GridImageAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.TaskReplyModel;
import com.chenxi.cebim.utils.StringUtil;
import com.chenxi.cebim.utils.UpLoadFileUtil;
import com.chenxi.cebim.view.FullyGridLayoutManager;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.xw.repo.BubbleSeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TaskFeedBackActivity extends BaseActivity {

    private ImageView back;
    private TextView submit, tv_taskName, tv_completePercent, picNum;
    private com.xw.repo.BubbleSeekBar bubbleSeekBar;
    private EditText actualLaborForce, actualunit, remark;
    private RecyclerView picRecyclerView, documentRecyclerView;
    private RelativeLayout rl_document;
    private ScrollView scrollerView;

    private ProgressDialog progressDialog;

    private TaskReplyModel taskReplyModel;

    private List<TaskReplyModel> feedBackList = new ArrayList<>();//获取回调接口中返回的
    private String taskName;

    private GridImageAdapter adapter;

    private List<LocalMedia> selectList = new ArrayList<>();//用于图片选取后装载图片对象
    private List<LocalMedia> showList = new ArrayList<>();//用于显示图片
    private int themeId;

    public static String feedBackdocumentString = new String();
    private List<DocumentModel> documentList = new ArrayList<>();
    private DocumentAdapter documentAdapter;

    private List<String> PathList = new ArrayList<>();//用于装载上传文件地址
    private List<String> picAnnexMd5CodeList = new ArrayList<>();//图片MD5列表

    private List<String> docuemntStrList = new ArrayList<>();//docuemnt列表字符串

    private int projectID;//项目

    private String TaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_feed_back);

        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        projectID = SPUtils.getInstance().getInt("projectID");

        feedBackdocumentString = "";

        Intent intent = getIntent();
        TaskId = intent.getStringExtra("ID");
        taskName = intent.getStringExtra("taskName");
        initView();//控件初始化
        getData(TaskId);
    }

    //控件初始化
    private void initView() {
        themeId = R.style.picture_default_style;

        back = findViewById(R.id.toolbar_left_btn);//返回
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submit = findViewById(R.id.toolbar_right_tv);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //加载加进度条，加班类别请求成功后消失。
                progressDialog = new ProgressDialog(TaskFeedBackActivity.this);
                progressDialog.setMessage("创建中...");
                progressDialog.setCancelable(false);
                progressDialog.show();  //将进度条显示出来
                //上传图片和视频
                for (int i = 0; i < showList.size(); i++) {
                    PathList.add(showList.get(i).getPath());
                }
                getArr(PathList);
            }
        });

        tv_completePercent = findViewById(R.id.tv_complete_percent_content);

        bubbleSeekBar = findViewById(R.id.sb_complete_percent);//进度条
        bubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                tv_completePercent.setText(progress + "%");
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });


        //修正bubbleSeekBar的偏移量
        scrollerView = findViewById(R.id.sv_feed_back);
        scrollerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                // 调用修正偏移方法
                bubbleSeekBar.correctOffsetWhenContainerOnScrolling();
            }
        });

        tv_taskName = findViewById(R.id.tv_task_feedback_name_content);
        tv_taskName.setText(taskName);

        actualLaborForce = findViewById(R.id.et_task_feedback_actual_labor_force);//实际工作量

        actualunit = findViewById(R.id.et_task_feedback_actualunit);//当前实际量

        remark = findViewById(R.id.et_task_feedback_remark);//备注

        picNum = findViewById(R.id.tv_task_feedback_pic_num);//图片数量

        //图片选择器
        FullyGridLayoutManager manager = new FullyGridLayoutManager(TaskFeedBackActivity.this, 4, GridLayoutManager.VERTICAL, false);
        picRecyclerView = findViewById(R.id.pic_feedback_recyclerView);//图片选择器RecyclerView
        picRecyclerView.setLayoutManager(manager);
        adapter = new GridImageAdapter(TaskFeedBackActivity.this, onAddPicClickListener, true);
        adapter.setList(showList);
        picRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (showList.size() > 0) {
                    LocalMedia media = showList.get(position);
                    String pictureType = media.getPictureType();
                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                        case 1:
                            // 预览图片 可自定长按保存路径
                            PictureSelector.create(TaskFeedBackActivity.this).themeStyle(themeId).openExternalPreview(position, showList);
                            break;
                        case 2:
                            // 预览视频
                            PictureSelector.create(TaskFeedBackActivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // 预览音频
                            PictureSelector.create(TaskFeedBackActivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });

        rl_document = findViewById(R.id.rl_task_feedback_attachment);//添加附件
        rl_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentDocumnet = new Intent(TaskFeedBackActivity.this, TaskFeedBackAddDocumentActivity.class);
                intentDocumnet.putExtra("documentFileString", feedBackdocumentString);
                intentDocumnet.putExtra("from", "PublishTaskActivity");
                startActivity(intentDocumnet);
            }
        });

        documentRecyclerView = findViewById(R.id.rv_feedback_attachment);//附件列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(TaskFeedBackActivity.this);
        documentRecyclerView.setLayoutManager(layoutManager);

    }

    //获取文件上传MD5
    private void getArr(final List<String> list) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (list.size() == 0) {
                    getAnnexMD5Code("", 0);
                } else {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        getAnnexMD5Code(list.get(i), i);
                    }
                }
            }
        }).start();

    }

    //获取AnnexMD5Code
    private void getAnnexMD5Code(final String filePath, int isLastOne) {

        if (filePath.equals("") && isLastOne == 0) {
            getDataFromInterNet("");//没有图片的情况
        } else {
            try {

                RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), new File(filePath));
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", UpLoadFileUtil.getFileName(filePath), fileBody)
                        .build();

                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(AppConst.innerIp + "/api/AnnexFile")
                        .build();

                Response execute = MyApplication.getOkHttpClient().newCall(request).execute();//涉及到图片的先后顺序，这里用同步上传

                if (execute.code() == 200) {
                    ResponseBody body = execute.body();
                    String string = body.string();

                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(string);
                    String AnnexMd5Code = jsonObject.getString("AnnexMd5Code");

                    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                    map.put("Name", "" + UpLoadFileUtil.getFileName(filePath));
                    map.put("ID", "" + AnnexMd5Code);
                    picAnnexMd5CodeList.add(JSON.toJSONString(map));

                    if (isLastOne == 0) {
                        getDataFromInterNet(picAnnexMd5CodeList.toString());
                    }
                } else {
                    ToastUtils.showShort(UpLoadFileUtil.getFileName(filePath) + "上传失败");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //从网络获取数据
    public void getDataFromInterNet(String picAnnexMd5CodeString) {

        //准备附件数据
        for (int i = 0; i < documentList.size(); i++) {
            //获取用于上传的附件字段
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("Name", documentList.get(i).getFileNama());
            map.put("ID", documentList.get(i).getFileString());
            docuemntStrList.add(JSON.toJSONString(map));
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("TaskId", TaskId)//TaskId
                .add("Percentage", "" + Double.parseDouble(tv_completePercent.getText().toString().replace("%", "")))//完成情况
                .add("PracticalLabor", actualLaborForce.getText() + "")//实际劳动力
                .add("ActualUnit", actualunit.getText() + "")//当前实际量
                .add("Remark", remark.getText() + "")//备注
                .add("Pictures", "" + picAnnexMd5CodeString)//图片
                .add("DocumentIds", docuemntStrList.toString())//附件
                .build();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/SynergyTask/Reply")
                .post(requestBody)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("问题创建失败");
                progressDialog.dismiss();  //将进度条隐藏
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ToastUtils.showShort("问题创建成功");
                    progressDialog.dismiss();  //将进度条隐藏

                    finish();

                    //刷新发布任务界面的反馈记录列表及记录数字,同时刷新已接收，已发布，全部这三个类表
                    EventBus.getDefault().post(new CommonEven("反馈记录刷新"));//发送消息，刷新TaskDetailActivity中的反馈列表及记录数字

                } else {
                    ToastUtils.showShort("问题创建失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {

            // 进入相册 以下是例子：不需要的api可以不写
            PictureSelector.create(TaskFeedBackActivity.this)
                    .openGallery(PictureMimeType.ofAll())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .maxSelectNum(9)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(3)// 每行显示个数
                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                    .previewImage(true)// 是否可预览图片 true or false
                    .isCamera(true)// 是否显示拍照按钮 true or false
                    .previewVideo(true)// 是否可预览视频 true or false
                    .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    .setOutputCameraPath(AppConst.savePath + "PHOTO")// 自定义拍照保存路径,可不填
                    .enableCrop(false)// 是否裁剪 true or false
                    .compress(true)// 是否压缩 true or false
                    .isGif(true)// 是否显示gif图片 true or false
                    .selectionMedia(showList)// 是否传入已选图片
                    .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    .synOrAsy(false)//同步true或异步false 压缩 默认同步
                    .videoQuality(1)// 视频录制质量 0 or 1 int
                    .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                    .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                    .recordVideoSecond(60)//视频秒数录制 默认60s int
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (selectList != null) {
            selectList.clear();
        }

        if (showList != null) {
            showList.clear();
        }
        //处理二维码扫描结果
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 图片选择结果回调
                selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
//                    for (LocalMedia media : selectList) {
//                        Log.i("图片-----》", media.getPath());
//                    }

                showList.addAll(selectList);
                adapter.setSelectMax(9);
                adapter.setList(showList);
                adapter.notifyDataSetChanged();
                picNum.setText(showList.size() + "/9");
            }
        }

    }

    //获取数据
    private void getData(String ID) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + ID + "/Reply")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.code() == 200) {
                    feedBackList.clear();
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                         JSONObject jsonObject = jsonArray.getJSONObject(0);

                        String ID = jsonObject.getString("ID");
                        String TaskId = jsonObject.getString("TaskId");
                        String Remark = jsonObject.getString("Remark");
                        String Pictures = jsonObject.getString("Pictures");
                        String ActualUnit = jsonObject.getString("ActualUnit");
                        String ModelUnit = jsonObject.getString("ModelUnit");

                        String CreatedAt = "";
                        if (!jsonObject.get("CreatedAt").toString().equals("null")) {
                            CreatedAt = jsonObject.get("CreatedAt").toString();
                        }
                        String UpdatedAt = "";
                        if (!jsonObject.get("UpdatedAt").toString().equals("null")) {
                            UpdatedAt = jsonObject.get("UpdatedAt").toString();
                        }

                        String CreatedInfo = jsonObject.get("CrUserInfo").toString();
                        String CreatedUserName = "";
                        int CreatedUserID = -1;
                        if (CreatedInfo != null) {
                            JSONObject crUserInfoObject = new JSONObject(CreatedInfo);
                            CreatedUserName = crUserInfoObject.getString("UserName");
                            CreatedUserID = crUserInfoObject.getInt("UserID");
                        }

                        String UpUserInfo = jsonObject.get("CrUserInfo").toString();
                        String UpdatedUserName = "";
                        int UpdatedUserID = -1;
                        if (UpUserInfo != null) {
                            JSONObject upUserInfoObject = new JSONObject(CreatedInfo);
                            UpdatedUserName = upUserInfoObject.getString("UserName");
                            UpdatedUserID = upUserInfoObject.getInt("UserID");
                        }


                        int PracticalLabor = -1;
                        if (!jsonObject.get("PracticalLabor").toString().equals("null")) {
                            PracticalLabor = jsonObject.getInt("PracticalLabor");
                        }

                        int CreatedBy = -1;
                        if (!jsonObject.get("CreatedBy").toString().equals("null")) {
                            CreatedBy = jsonObject.getInt("CreatedBy");
                        }

                        int UpdatedBy = -1;
                        if (!jsonObject.get("UpdatedBy").toString().equals("null")) {
                            UpdatedBy = jsonObject.getInt("UpdatedBy");
                        }

                        double Percentage = 0.00;
                        if (!jsonObject.get("Percentage").toString().equals("null")) {
                            Percentage = jsonObject.getInt("Percentage");
                        }

                        taskReplyModel = new TaskReplyModel(ID, TaskId, Remark, Pictures, ActualUnit, ModelUnit,
                                CreatedAt, UpdatedAt, CreatedUserName, UpdatedUserName, PracticalLabor, CreatedBy,
                                UpdatedBy, CreatedUserID, UpdatedUserID, Percentage);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bubbleSeekBar.setProgress((float) taskReplyModel.getPercentage());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");

                    }
                } else {
                    ToastUtils.showShort("数据请求出错");

                }
            }
        });

    }

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {

        if (commonEven.getInfo().contains("TaskFeedBackActivity附件字符串")) {//从TaskFeedBackAddDocumentActivity返回
            feedBackdocumentString = commonEven.getInfo().replace("TaskFeedBackActivity附件字符串:", "");

            //不加这句会造成数据重复
            if (documentList != null) {
                documentList.clear();
            }

            for (int i = 0; i < feedBackdocumentString.split("△").length; i++) {
                DocumentModel documentModel = new DocumentModel(feedBackdocumentString.split("△")[i].split("☆")[1],
                        feedBackdocumentString.split("△")[i].split("☆")[0]);
                documentList.add(documentModel);
            }

            //显示附件列表
            documentAdapter = new DocumentAdapter(MyApplication.getContext(), documentList,
                    SPUtils.getInstance().getInt("projectID"), true, "TaskFeedBackActivity", TaskFeedBackActivity.this);
            documentRecyclerView.setAdapter(documentAdapter);

        } else if (commonEven.getInfo().contains("TaskFeedBackActivity删除:")) {//DocumentAdapter返回的删除信息
            for (int i = 0; i < documentList.size(); i++) {
                String item = documentList.get(i).getFileString();
                if (commonEven.getInfo().split(":")[1].equals(item)) {

                    //把选中项从documentString中去掉
                    feedBackdocumentString = feedBackdocumentString.replace(documentList.get(i).getFileNama() + "☆"
                            + documentList.get(i).getFileString(), "");
                    if (feedBackdocumentString != null && feedBackdocumentString.contains("△△")) {
                        feedBackdocumentString = feedBackdocumentString.replace("△△", "△");//去掉△△,变为△
                    }

                    if (feedBackdocumentString.contains("△")) {
                        feedBackdocumentString = StringUtil.trimFirstAndLastChar(feedBackdocumentString, '△');//去掉字符串首尾的@#@#
                    } else {
                        feedBackdocumentString = "";
                    }

                    documentList.remove(documentList.get(i));
                }
            }
            documentAdapter.notifyDataSetChanged();
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
