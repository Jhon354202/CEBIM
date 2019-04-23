package com.chenxi.cebim.activity.coordination;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.DetailShowPicAdapter;
import com.chenxi.cebim.adapter.DocumentAdapter;
import com.chenxi.cebim.adapter.QuestionDetailAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.ProblemDetailIntegrationBean;
import com.chenxi.cebim.entity.ProblemDetailPicModel;
import com.chenxi.cebim.entity.QuestionCommentModel;
import com.chenxi.cebim.entity.QuestionDivideModel;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.utils.CountDownTimerUtils;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioRecordListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProblemDetail extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView detailRecyclerView;

    private LinearLayout ll_audioPlay, ll_detailAt, ll_audio_record;
    private RelativeLayout rl_deadline;
    private TextView userName, createTime, isFollow, title, content, tv_audioPlay, priority, state,
            systemType, atString, isComplete, isCompleteBtn, deadline, sendReply, detailCountDown;
    private ImageView back, iv_audioPlay, getAudio, getImage, takePhoto, getDocument, getAt, detailPressRecord;
    private EditText reply;
    private CircularProgressBar detailProgressBar;

    private String ID;//上个界面跳转过来时发送的ID
    private QuestionModel questionModel;//网络端获取的对象
    private int mProjectId;//项目ID
    boolean playOrStop = true;//是否播音

    private Dialog picOrVideoDialog;//picOrVideoDialog弹出框
    private View inflate;
    Button getPhoto, getVedio, cancel;

    String soundPath;//录音本地地址
    File soundFile;//录音文件

    String savePath;//图片、视频、录音存储路径
    private File mAudioDir;//录音文件
    private Uri audioURI;//录音成功返回的URI
    private int audioLength;//录音时长
    private int recLen = 0;
    Handler handler = new Handler();

    private List<LocalMedia> showList = new ArrayList<>();//用于显示图片

    private List<ProblemDetailPicModel> problemDetailPicList = new ArrayList<>();//装用于显示的图片
    private int themeId;

    CountDownTimerUtils mCountDown;
    CountDownTimerUtils spangled;
    int audioTime;//显示的录音时长
    String audioPath;

    private String atNameStr = "";//at人名字字符串

    private int chooseMode = PictureMimeType.ofAll();//直接进入拍照的可拍摄类型

    private List<DocumentModel> documentList = new ArrayList<>();
    private List<DocumentModel> documentShowList = new ArrayList<>();

    private DocumentAdapter documentAdapter;
    private QuestionDetailAdapter questionDetailAdapter;

    private List<QuestionCommentModel> QuestionCommentList = new ArrayList<>();//装评论对象

    LinearLayoutManager linearLayoutManager;

    final Handler handlerStop = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    recLen = 0;
                    handler.removeCallbacks(sound_record);
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_detail);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        Intent intent = getIntent();
        ID = intent.getStringExtra("problemDetailID");

        detailRecyclerView = findViewById(R.id.detail_recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        questionDetailAdapter = new QuestionDetailAdapter(ProblemDetail.this, ID);

        savePath = DiskCacheDirUtil.getDiskCacheDir(ProblemDetail.this);//图片、视频、录音存储路径
        initView();
        swipeRefresh.setRefreshing(true);
        getData();

        initRecord();//初始化录音
    }

    //定时器，用于录音计时
    Runnable sound_record = new Runnable() {
        public void run() {
            recLen++;
            if (recLen < 60) {
                detailCountDown.setText("00:" + recLen / 10 + recLen % 10);
            } else {
                detailCountDown.setText("01:00");
            }

            // 延时1s后又将线程加入到线程队列中
            if (recLen >= 60) {
                Message message = new Message();
                message.what = 1;
                handlerStop.sendMessage(message);
            }
            handler.postDelayed(sound_record, 1000);
        }
    };

    private void initRecord() {

        AudioRecordManager.getInstance(this).setMaxVoiceDuration(60);
        mAudioDir = new File(savePath, "AUDIO");
        if (!mAudioDir.exists()) {
            mAudioDir.mkdirs();
        }
        AudioRecordManager.getInstance(this).setAudioSavePath(mAudioDir.getAbsolutePath());
    }

    //控件初始化
    private void initView() {

        themeId = R.style.picture_default_style;
        back = findViewById(R.id.toolbar_left_btn);//返回按钮
        back.setOnClickListener(this);

        //SwipeRefreshLayout下拉刷新的逻辑
        swipeRefresh = findViewById(R.id.detail_swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        reply = findViewById(R.id.et_reply);//回复
        sendReply = findViewById(R.id.tv_sent_reply);//回复

        sendReply.setOnClickListener(this);

        getAudio = findViewById(R.id.iv_audio);//录音回复
        getAudio.setOnClickListener(this);
        getImage = findViewById(R.id.iv_image);//图片回复
        getImage.setOnClickListener(this);
        takePhoto = findViewById(R.id.iv_take_photo);//拍照回复
        takePhoto.setOnClickListener(this);
        getDocument = findViewById(R.id.iv_add_document);//附件回复
        getDocument.setOnClickListener(this);
        getAt = findViewById(R.id.iv_at);//at人
        getAt.setOnClickListener(this);

        ll_audio_record = findViewById(R.id.ll_detail_audio_record);//录音界面
        detailCountDown = findViewById(R.id.detail_count_down);// 录音时间界面
        detailProgressBar = findViewById(R.id.cb_detail);//录音进度条
        detailPressRecord = findViewById(R.id.detail_press_record);//录音按钮
        detailPressRecord.setOnTouchListener(this);//长按事件

    }

    private DetailShowPicAdapter.onAddPicClickListener onAddPicClickListener = new DetailShowPicAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {

        }

    };

    //获取网络数据
    private void getData() {

//        problemDetailList.clear();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion/" + ID)
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
                        JSONObject jsonObject = new JSONObject(responseData);

                        Integer ProjectId = jsonObject.getInt("ProjectId");
                        mProjectId = ProjectId;

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
                        SPUtils.getInstance().put("topicid",ID);//持久化topicid

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

                        Boolean State = jsonObject.getBoolean("State");

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

                        questionModel = new QuestionModel(ProjectId, ClosedUserId, Priority, UserId, UpdatedBy,
                                ID, Title, Comment, GroupId, Category, ViewportId, SystemType, At, Pictures,
                                Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers, DocumentIds, UserName, CategoryName, SystemTypeName,firstFrame,
                                ObservedUsers, State, Observed, IsFinishedAndDelay, CompletedAt, Deadline, Date, LastUpdate);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                questionDetailAdapter.getData().clear();
                                //把头部添加进来
                                ProblemDetailIntegrationBean<QuestionModel> itemData0 = new ProblemDetailIntegrationBean<>();
                                itemData0.setT(questionModel);
                                itemData0.setDataType(QuestionDetailAdapter.ITEM_TYPE.ITEM_TYPE_DETAIL.ordinal());
                                questionDetailAdapter.getData().add(itemData0);

                                //把分隔条添加
                                ProblemDetailIntegrationBean<QuestionDivideModel> itemData1 = new ProblemDetailIntegrationBean<>();
                                itemData1.setT(new QuestionDivideModel());
                                itemData1.setDataType(QuestionDetailAdapter.ITEM_TYPE.ITEM_TYPE_DIVIDE_LINE.ordinal());
                                questionDetailAdapter.getData().add(itemData1);

                                //获取和显示评论数据
                                getResponses();
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

    //获取和显示评论数据
    private void getResponses() {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion/" + ID + "/Comment")
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
                        if (responseData != null && (!responseData.equals("null")) && (!responseData.equals("[]"))) {
                            //刷新时避免数据重复
                            if (QuestionCommentList != null) {
                                QuestionCommentList.clear();
                            }

                            JSONArray jsonArray = null;
                            jsonArray = new JSONArray(responseData);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String ID = jsonObject.getString("ID");
                                String TopicId = jsonObject.getString("TopicId");
                                String UserId = jsonObject.getString("UserId");
                                String At = jsonObject.getString("At");
                                Object Date = jsonObject.get("Date");
                                String Comment = jsonObject.getString("Comment");
                                String Pictures = jsonObject.getString("Pictures");
                                String DocumentIds = jsonObject.getString("DocumentIds");
                                String Voice = jsonObject.getString("Voice");
                                String Video = jsonObject.getString("Video");

                                String UserInfo = jsonObject.getString("UserInfo");
                                String UserName = new JSONObject(UserInfo).getString("UserName");

                                QuestionCommentModel questionCommentModel = new QuestionCommentModel(ID,
                                        TopicId, UserId, At, Date, Comment, Pictures, DocumentIds, Voice, Video, UserName);

                                QuestionCommentList.add(questionCommentModel);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //显示评论列表
                                    for (int i = 0; i < QuestionCommentList.size(); i++) {
                                        ProblemDetailIntegrationBean<QuestionCommentModel> itemData2 = new ProblemDetailIntegrationBean<>();
                                        itemData2.setT(QuestionCommentList.get(i));
                                        itemData2.setDataType(QuestionDetailAdapter.ITEM_TYPE.ITEM_TYPE_RESPONSE.ordinal());
                                        questionDetailAdapter.getData().add(itemData2);
                                    }

                                    detailRecyclerView.addItemDecoration(new DividerItemDecoration(ProblemDetail.this, DividerItemDecoration.VERTICAL));
                                    detailRecyclerView.setLayoutManager(linearLayoutManager);
                                    detailRecyclerView.setAdapter(questionDetailAdapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //必须在主线程才能显示view
                                    detailRecyclerView.addItemDecoration(new DividerItemDecoration(ProblemDetail.this, DividerItemDecoration.VERTICAL));
                                    detailRecyclerView.setLayoutManager(linearLayoutManager);
                                    detailRecyclerView.setAdapter(questionDetailAdapter);
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            });

                        }

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


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.toolbar_left_btn://返回上一级按钮
                finish();
                break;

            case R.id.iv_audio://弹出录音界面
                //如果录音界面可见，则点击会隐藏，反之则点击会显示
                if (ll_audio_record.getVisibility() == View.GONE) {
                    ll_audio_record.setVisibility(View.VISIBLE);
                } else if (ll_audio_record.getVisibility() == View.VISIBLE) {
                    ll_audio_record.setVisibility(View.GONE);
                }
                break;

            case R.id.iv_image://弹出图片选择器界面
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(ProblemDetail.this)
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
                        .setOutputCameraPath(savePath + "PHOTO")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(true)// 是否压缩 true or false
                        .isGif(true)// 是否显示gif图片 true or false
                        .selectionMedia(QuestionResponseActivity.selectList)// 是否传入已选图片
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(false)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .recordVideoSecond(60)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                break;

            case R.id.iv_take_photo://弹出拍照界面

                picOrVedioDialog();

                break;

            case R.id.iv_add_document://弹出附件添加界面
                StringBuffer sb = new StringBuffer();
                String documentFileString = new String();

                if (documentList != null && documentList.size() > 0) {
                    for (int i = 0; i < documentList.size(); i++) {
                        sb.append(documentList.get(i).getFileString());
                        sb.append(":");
                        sb.append(documentList.get(i).getFileNama());
                        sb.append(",");
                    }
                    documentFileString = sb.substring(0, sb.length() - 1).toString();
                } else {
                    documentFileString = "";
                }

                Intent intentDocumnet = new Intent(ProblemDetail.this, AddDocumentActivity.class);
                intentDocumnet.putExtra("from", "ProblemDetail");
                intentDocumnet.putExtra("documentFileString", documentFileString);
                startActivity(intentDocumnet);
                break;


            case R.id.iv_at://弹出at界面
                Intent intentAt = new Intent(ProblemDetail.this, AtActivity.class);
                intentAt.putExtra("atListStr", atNameStr);
                intentAt.putExtra("from", "ProblemDetail");
                startActivity(intentAt);
                break;


            case R.id.bt_photo:
                // 单独拍照
                PictureSelector.create(ProblemDetail.this)
                        .openCamera(1)// 单独拍照
                        .theme(themeId)// 主题样式设置 具体参考 values/styles
                        .maxSelectNum(9)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片
                        .previewVideo(true)// 是否可预览视频
                        .enablePreviewAudio(true) // 是否可播放音频
                        .isCamera(true)// 是否显示拍照按钮
                        .enableCrop(false)// 是否裁剪
                        .compress(false)// 是否压缩
//                        .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
//                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
//                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .circleDimmedLayer(false)// 是否圆形裁剪
//                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .openClickSound(true)// 是否开启点击声音
                        .selectionMedia(QuestionResponseActivity.selectList)// 是否传入已选图片
                        .previewEggs(true)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 裁剪压缩质量 默认为100
//                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled() // 裁剪是否可旋转图片
                        //.scaleEnabled()// 裁剪是否可放大缩小图片
                        //.videoQuality()// 视频录制质量 0 or 1
                        //.videoSecond()////显示多少秒以内的视频or音频也可适用
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picOrVideoDialog.dismiss();
                break;

            case R.id.bt_vedio:

                // 单独拍视频
                PictureSelector.create(ProblemDetail.this)
                        .openCamera(2)// 单独拍视频
                        .theme(themeId)// 主题样式设置 具体参考 values/styles
                        .maxSelectNum(9)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片
                        .previewVideo(true)// 是否可预览视频
                        .enablePreviewAudio(true) // 是否可播放音频
                        .isCamera(true)// 是否显示拍照按钮
                        .enableCrop(false)// 是否裁剪
                        .compress(false)// 是否压缩
//                        .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
//                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
//                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .circleDimmedLayer(false)// 是否圆形裁剪
//                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .openClickSound(true)// 是否开启点击声音
                        .selectionMedia(QuestionResponseActivity.selectList)// 是否传入已选图片
                        .previewEggs(true)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 裁剪压缩质量 默认为100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled() // 裁剪是否可旋转图片
                        //.scaleEnabled()// 裁剪是否可放大缩小图片
                        //.videoQuality()// 视频录制质量 0 or 1
                        //.videoSecond()////显示多少秒以内的视频or音频也可适用
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                picOrVideoDialog.dismiss();
                break;

            case R.id.btn_cancel:
                picOrVideoDialog.dismiss();
                break;

            case R.id.tv_sent_reply:
                //发送回复
                sendReply();
                break;
        }
    }

    /**
     * 发送回复
     */
    private void sendReply() {

        if (reply.getText() == null || reply.getText().toString().equals("")) {
            ToastUtils.showShort("请输入回复内容");
        } else {

            RequestBody requestBody = new FormBody.Builder()
                    .add("UserName", SPUtils.getInstance().getString("UserName"))//用户名
                    .add("UserID", "" + SPUtils.getInstance().getInt("UserID"))//内容
                    .add("Comment", reply.getText().toString())//文本内容
                    .build();

            Request request = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion/" + ID + "/Comment")
                    .post(requestBody)
                    .build();

            MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ToastUtils.showShort("添加评论失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {

//                        try {
//                            String responseData = response.body().string();
//                            org.json.JSONObject jsonObject = null;
//                            jsonObject = new org.json.JSONObject(responseData);
//                            String ID = jsonObject.getString("ID");
//                            String TopicId = jsonObject.getString("TopicId");
//                            String UserId = jsonObject.getString("UserId");
//                            String At = jsonObject.getString("At");
//                            Object Date = jsonObject.get("Date");
//                            String Comment = jsonObject.getString("Comment");
//                            String Pictures = jsonObject.getString("Pictures");
//                            String DocumentIds = jsonObject.getString("DocumentIds");
//                            String Voice = jsonObject.getString("Voice");
//                            String Video = jsonObject.getString("Video");
//                            String UserInfo = jsonObject.getString("UserInfo");
//                            String UserName = new JSONObject(UserInfo).getString("UserName");
//
//                            QuestionCommentModel questionCommentModel = new QuestionCommentModel(ID, TopicId, UserId, At, Date, Comment, Pictures, DocumentIds, Voice, Video, UserName);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort("添加评论成功");
                                    InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(reply.getWindowToken(), 0); //强制隐藏键盘
                                    ((TextView) reply).setText(""); //清空输入框
                                    getData();
                                }
                            });

//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            ToastUtils.showShort("数据解析出错");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            ToastUtils.showShort("数据请求出错");
//                        }
                    } else {
                        ToastUtils.showShort("添加评论失败");
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                // 图片选择结果回调
                QuestionResponseActivity.selectList = PictureSelector.obtainMultipleResult(data);
                Intent intent = new Intent(ProblemDetail.this, QuestionResponseActivity.class);
                intent.putExtra("from", "album");
                startActivity(intent);
            }
        }
    }

    //图片和视频选择
    private void picOrVedioDialog() {
        picOrVideoDialog = new Dialog(ProblemDetail.this, R.style.ActionSheetDialogStyle);
        inflate = LayoutInflater.from(ProblemDetail.this).inflate(R.layout.bottom_dialog, null);
        getPhoto = (Button) inflate.findViewById(R.id.bt_photo);
        getVedio = (Button) inflate.findViewById(R.id.bt_vedio);
        cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        getPhoto.setOnClickListener(this);
        getVedio.setOnClickListener(this);
        cancel.setOnClickListener(this);

        picOrVideoDialog.setContentView(inflate);
        Window dialogWindow = picOrVideoDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.y = 20;
        dialogWindow.setAttributes(lp);
        picOrVideoDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //发送EvenBus，传递图片和视频对象列表到QuestionResponseActivity
//        EventBus.getDefault().post(new QuestionResponseEvenBusModel(QuestionResponseActivity.selectList));
//        EventBus.getDefault().post(new NewQuestionResponseEvenBusModel("图片和视频获取成功"));
    }

    /**
     * @param field   待修改的字段
     * @param changed 改后的字段
     */
    private void changeQuestion(String field, String changed) {

        FormBody formBody = new FormBody.Builder()
                .add(field, changed)
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + mProjectId + "/SynergyQuestion/" + ID)
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
                                String responseData = response.body().string();
                                if (field.equals("Observed") && changed.equals("true")) {
                                    ToastUtils.showShort("关注成功");
                                    isFollow.setText("已关注");
                                    isFollow.setBackgroundResource(R.drawable.shape_follow);//设置背景
                                    isFollow.setTextColor(getResources().getColor(R.color.gray_text, null));//设置字体颜色
                                    questionModel.setObserved(true);
                                } else if (field.equals("Observed") && changed.equals("false")) {
                                    ToastUtils.showShort("取消关注成功");
                                    isFollow.setText("关注问题");
                                    isFollow.setBackgroundResource(R.drawable.question_detail_shape);//设置背景
                                    isFollow.setTextColor(getResources().getColor(R.color.white, null));//设置字体颜色
                                    questionModel.setObserved(false);
                                } else if (field.equals("State") && changed.equals("true")) {
                                    isCompleteBtn.setVisibility(View.GONE);
                                    isComplete.setText("已完成");
                                    questionModel.setState(true);
                                }

                            } else {
                                if (field.equals("Observed")) {
                                    ToastUtils.showShort("关注问题失败");
                                } else if (field.equals("State")) {
                                    ToastUtils.showShort("标记完成失败");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (field.equals("Observed")) {
                                ToastUtils.showShort("关注问题失败");
                            } else if (field.equals("State")) {
                                ToastUtils.showShort("标记完成失败");
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.detail_press_record) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AudioRecordManager.getInstance(ProblemDetail.this).startRecord();

                    handler.post(sound_record);//开始计时
                    int animationDuration = 60000;
                    detailProgressBar.setProgressWithAnimation(100, animationDuration);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled(view, motionEvent)) {
                        AudioRecordManager.getInstance(ProblemDetail.this).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance(ProblemDetail.this).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance(ProblemDetail.this).stopRecord();
                    AudioRecordManager.getInstance(ProblemDetail.this).destroyRecord();
                    break;
            }

            AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {

                @Override
                public void initTipView() {
//                    ToastUtils.showShort("initTipView");
                }

                @Override
                public void setTimeoutTipView(int counter) {
//                    ToastUtils.showShort("setTimeoutTipView");
                }

                @Override
                public void setRecordingTipView() {
                    //倒计时10s
//                    ToastUtils.showShort("setRecordingTipView");
                }

                @Override
                public void setAudioShortTipView() {
//                    ToastUtils.showShort("setAudioShortTipView");
                    Message message = new Message();
                    message.what = 1;
                    handlerStop.sendMessage(message);
                    ToastUtils.showShort("录音时间太短");
                }

                @Override
                public void setCancelTipView() {
//                    ToastUtils.showShort("setCancelTipView");
                }

                @Override
                public void destroyTipView() {
//                    ToastUtils.showShort("destroyTipView");
                }

                @Override
                public void onStartRecord() {

                }

                @Override
                public void onFinish(Uri audioPath, int duration) {
                    audioURI = audioPath;
                    soundPath = audioPath.getPath();
                    soundFile = new File(soundPath);
                    if (soundFile.exists()) {
                        Toast.makeText(getApplicationContext(), "录制成功", Toast.LENGTH_SHORT).show();
                        audioLength = duration;
                        recLen = 0;

                        Message message = new Message();
                        message.what = 1;
                        handlerStop.sendMessage(message);

                        ll_audio_record.setVisibility(View.GONE);
                        detailProgressBar.setProgress(0);
                        detailCountDown.setText("00:00");

                        //把声音信息传递到QuestionResponseActivity
                        Intent intent = new Intent(ProblemDetail.this, QuestionResponseActivity.class);
                        intent.putExtra("audioLength", audioLength);
                        intent.putExtra("soundPath", soundPath);
                        intent.putExtra("from", "audio");
                        startActivity(intent);
                    }
                }

                @Override
                public void onAudioDBChanged(int db) {
//                    ToastUtils.showShort("onAudioDBChanged");
                }
            });
        }

        return false;
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth() || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }

    //EventBus 打开底部导航栏（分享、打开、常用、更多）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(CommonEven commonEven) {
        if (commonEven.getInfo().equals("刷新问题详情")) {
            getData();
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
