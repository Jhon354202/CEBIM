package com.chenxi.cebim.activity.coordination;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.adapter.DocumentAdapter;
import com.chenxi.cebim.adapter.GridImageAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.config.IntentConfig;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.entity.QuestionCommentModel;
import com.chenxi.cebim.utils.CountDownTimerUtils;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.UpLoadFileUtil;
import com.chenxi.cebim.view.FullyGridLayoutManager;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.audio.IAudioRecordListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

public class QuestionResponseActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private EditText responseText;
    private RelativeLayout rl_audioPlay;

    private ImageView back, iv_audioPlay, delRecord, getAudio, getImage, takePhoto, getDocument, getAt, audioRecordBtn;
    private TextView send, tv_audioPlay, tv_responseAt, responseCountDown;

    private CircularProgressBar circularProgressBar;

    private RecyclerView recyclerViewPic, recyclerDocument;

    private LinearLayout ll_responseAt, ll_response_audio_record;

    private Dialog audioDialog, picOrVideoDialog;//picOrVideoDialog弹出框
    private View inflate;
    Button getPhoto, getVedio, cancel;

    private ProgressDialog progressDialog;

    private String topicid;

    private String soundPath = null;//录音本地地址
    private int audioLength = 0;
    private Uri soundUri = null;

    private String filePathStr = null;//图片和视频地址

    boolean playOrStop = true;

    CountDownTimerUtils mCountDown;
    CountDownTimerUtils spangled;

    private GridImageAdapter gridImageAdapter;

    private DocumentAdapter documentAdapter;

    private String atNameStr = "";//at人名字字符串

    private int themeId;

    public static List<LocalMedia> selectList = new ArrayList<>();//用于图片选取后装载图片对象
    private List<LocalMedia> picAndVideoList = new ArrayList<>();//用于图片和视频对象
    private String atListStrForResponse = "";//用于取At的返回值
    String documentFileString;//用于存放附件字符串

    private List<DocumentModel> documentList = new ArrayList<>();
    private List<DocumentModel> tempDocumentList = new ArrayList<>();
    private List<String> docuemntStrList = new ArrayList<>();//docuemnt列表字符串

    private int projectID;//项目
    String savePath;//图片、视频、录音存储路径

    private List<String> atlist = new ArrayList<>();//at列表字符串

    private List<String> PathList = new ArrayList<>();//用于装载上传文件地址

    private List<String> picAnnexMd5CodeList = new ArrayList<>();//图片MD5列表
    private List<String> vedioAnnexMd5CodeList = new ArrayList<>();//视频MD5列表
    private List<String> audioAnnexMd5CodeList = new ArrayList<>();//音频MD5列表

    private File mAudioDir;//录音文件

    private Uri audioURI;//录音成功返回的URI
    File soundFile;//录音文件

    private int recLen = 0;
    Handler handler = new Handler();


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
        setContentView(R.layout.activity_question_response);

        topicid=SPUtils.getInstance().getString("topicid");
        projectID = SPUtils.getInstance().getInt("projectID");
        savePath = DiskCacheDirUtil.getDiskCacheDir(QuestionResponseActivity.this);//图片、视频、录音存储路径
        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        //图片选择器需单独初始化
        FullyGridLayoutManager manager = new FullyGridLayoutManager(QuestionResponseActivity.this, 3, GridLayoutManager.VERTICAL, false);
        recyclerViewPic = findViewById(R.id.response_pic_recyclerView);
        recyclerViewPic.setLayoutManager(manager);

        //附件列表单独初始化
        LinearLayoutManager layoutManager = new LinearLayoutManager(QuestionResponseActivity.this);
        recyclerDocument = findViewById(R.id.response_document_recyclerView);
        recyclerDocument.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        if (intent.getStringExtra("from").equals("audio")) {//从录音跳转过来
            audioLength = intent.getIntExtra("audioLength", 0);
            soundPath = intent.getStringExtra("soundPath");
            soundUri = Uri.parse(soundPath);//获取声音的Uri

        } else if (intent.getStringExtra("from").equals("album")) {//从选择选择图片跳转过来
            recyclerViewPic.setVisibility(View.VISIBLE);
            picAndVideoList = selectList;
            gridImageAdapter = new GridImageAdapter(QuestionResponseActivity.this, onAddPicClickListener, false);
            gridImageAdapter.setList(picAndVideoList);
            recyclerViewPic.setAdapter(gridImageAdapter);
            gridImageAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    if (picAndVideoList.size() > 0) {
                        LocalMedia media = picAndVideoList.get(position);
                        String pictureType = media.getPictureType();
                        int mediaType = PictureMimeType.pictureToVideo(pictureType);
                        switch (mediaType) {
                            case 1:
                                // 预览图片 可自定长按保存路径
                                PictureSelector.create(QuestionResponseActivity.this).themeStyle(themeId).openExternalPreview(position, picAndVideoList);
                                break;
                            case 2:
                                // 预览视频
                                PictureSelector.create(QuestionResponseActivity.this).externalPictureVideo(media.getPath());
                                break;
                            case 3:
                                // 预览音频
                                PictureSelector.create(QuestionResponseActivity.this).externalPictureAudio(media.getPath());
                                break;
                        }
                    }
                }
            });
        } else if (intent.getStringExtra("from").equals("at")) {
            atListStrForResponse = intent.getStringExtra("atListStrForResponse");//从AtActiviy中传过来的数据
        } else if (intent.getStringExtra("from").equals("AddDocumentActivity")) {
            documentFileString = intent.getStringExtra("documentFileString");
            recyclerDocument.setVisibility(View.VISIBLE);

            for (int i = 0; i < documentFileString.split(",").length; i++) {
                String fidStr = documentFileString.split(",")[i].split(":")[0];
                String fileName = documentFileString.split(",")[i].split(":")[1];
                DocumentModel documentModel = new DocumentModel(fidStr, fileName);
                documentList.add(documentModel);//获取附件的fid和文件名
            }

            documentAdapter = new DocumentAdapter(MyApplication.getContext(), documentList, projectID, true, "QuestionResponseActivity", this);
            recyclerDocument.setAdapter(documentAdapter);
        }
//            else if (newQuestionDelEven.getInfo().contains("删除")) {//从AddDocumentActivity的确认按钮那边返回
//
//                for (int i = documentList.size() - 1; i >= 0; i--) {
//                    String item = documentList.get(i).getFidString();
//                    if (newQuestionDelEven.getInfo().split(":")[1].equals(item)) {
//                        documentList.remove(documentList.get(i));
//                    }
//                }
//
//                documentAdapter.notifyDataSetChanged();
//            }

        initView();
        initRecord();//初始化录音
    }

    //定时器，用于录音计时
    Runnable sound_record = new Runnable() {
        public void run() {
            recLen++;
            if (recLen < 60) {
                responseCountDown.setText("00:" + recLen / 10 + recLen % 10);
            } else {
                responseCountDown.setText("01:00");
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

    private void initView() {

        themeId = R.style.picture_default_style;

        back = findViewById(R.id.toolbar_left_btn);//返回按钮
        back.setOnClickListener(this);

        responseText = findViewById(R.id.et_response);//回复文本框

        send = findViewById(R.id.toolbar_right_tv);//发送按钮
        send.setOnClickListener(this);


        rl_audioPlay = findViewById(R.id.rl_response_audio_play);//点击播放录音
        rl_audioPlay.setOnClickListener(this);

        iv_audioPlay = findViewById(R.id.iv_response_audio_play);//播放录音图片
        tv_audioPlay = findViewById(R.id.tv_response_audio_play);//录音时间

        if (soundPath == null) {
            rl_audioPlay.setVisibility(View.GONE);
        } else {
            rl_audioPlay.setVisibility(View.VISIBLE);
            if (audioLength >= 60) {
                tv_audioPlay.setText("01:00");
            } else {
                tv_audioPlay.setText("00:" + audioLength / 10 + audioLength % 10);
            }
        }

        delRecord = findViewById(R.id.del_response_record);//删除录音按钮
        delRecord.setOnClickListener(this);

        ll_responseAt = findViewById(R.id.ll_response_at);//显示at列表
        tv_responseAt = findViewById(R.id.tv_response_at);//显示at名字列表
        ll_response_audio_record = findViewById(R.id.ll_response_audio_record);

        //at人的显示与隐藏
        if (atListStrForResponse != null && atListStrForResponse != "" && atListStrForResponse.length() > 0) {
            ll_responseAt.setVisibility(View.VISIBLE);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < atListStrForResponse.split(",").length; i++) {
                sb.append(atListStrForResponse.split(",")[i].split(":")[0]);
                sb.append("、");
            }
            tv_responseAt.setText(sb.subSequence(0, sb.length() - 1).toString());
        } else {
            ll_responseAt.setVisibility(View.GONE);
        }

        getAudio = findViewById(R.id.iv_response_audio);//录音回复
        getAudio.setOnClickListener(this);
        getImage = findViewById(R.id.iv_response_image);//图片回复
        getImage.setOnClickListener(this);
        takePhoto = findViewById(R.id.iv_response_take_photo);//拍照回复
        takePhoto.setOnClickListener(this);
        getDocument = findViewById(R.id.iv_response_add_document);//附件回复
        getDocument.setOnClickListener(this);
        getAt = findViewById(R.id.iv_response_at);//at人
        getAt.setOnClickListener(this);

        ll_response_audio_record = findViewById(R.id.ll_response_audio_record);//录音界面
        responseCountDown = findViewById(R.id.response_count_down);// 录音时间界面
        circularProgressBar = findViewById(R.id.cb_response);//录音进度条
        audioRecordBtn = findViewById(R.id.response_press_record);//录音按钮
        audioRecordBtn.setOnTouchListener(this);//长按事件

    }

    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(NewQuestionDelEven newQuestionDelEven) {
        if(newQuestionDelEven.getInfo().contains("QuestionResponseActivity@#@#@#")){

            //从AddDocument中返回的数据
            String returnData=newQuestionDelEven.getInfo().split("@#@#@#")[1];

            if (tempDocumentList != null) {
                tempDocumentList.clear();
            }
            documentList.clear();
            for (int i = 0; i < returnData.split(",").length; i++) {
                String fidStr = returnData.split(",")[i].split(":")[0];
                String fileName = returnData.split(",")[i].split(":")[1];
                DocumentModel documentModel = new DocumentModel(fidStr, fileName);
                tempDocumentList.add(documentModel);//获取附件的fid和文件名
            }
            documentList.addAll(tempDocumentList);
            if(documentAdapter!=null){
                documentAdapter.notifyDataSetChanged();//奔溃原因待查
            }else{
                documentAdapter=new DocumentAdapter(MyApplication.getContext(), documentList, projectID, true, "QuestionResponseActivity", this);;
                recyclerDocument.setAdapter(documentAdapter);
            }
        }
//        else if(newQuestionDelEven.getInfo().contains("QuestionResponseActivity@#@#@#")){
//
//        }
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
        }

    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_left_btn://返回按钮
                selectList.clear();
                finish();
                break;

            case R.id.toolbar_right_tv://发送按钮
                //加载加进度条，加班类别请求成功后消失。
                progressDialog = new ProgressDialog(QuestionResponseActivity.this);
                progressDialog.setMessage("创建中...");
                progressDialog.setCancelable(true);
                progressDialog.show();  //将进度条显示出来

                //上传图片和视频
                for (int i = 0; i < picAndVideoList.size(); i++) {
                    PathList.add(picAndVideoList.get(i).getPath());
                }

                //add录音文件地址
                if (soundPath != null) {
                    PathList.add(soundPath);
                }

                getArr(PathList);

                break;

            case R.id.rl_response_audio_play://播音按钮
                if (playOrStop) {
                    playOrStop = false;

                    AudioPlayManager.getInstance().startPlay(QuestionResponseActivity.this,
                            soundUri, new IAudioPlayListener() {
                                @Override
                                public void onStart(Uri var1) {
                                    //开播（一般是开始语音消息动画）
                                    mCountDown = new CountDownTimerUtils(tv_audioPlay,
                                            audioLength * 1000, 1000, 0);
                                    mCountDown.start();

                                    spangled = new CountDownTimerUtils(iv_audioPlay,
                                            audioLength * 1000, 500, 1);
                                    spangled.start();
                                }

                                @Override
                                public void onStop(Uri var1) {
                                    //停播（一般是停止语音消息动画）
                                    if (audioLength == 60) {
                                        tv_audioPlay.setText("01:00");
                                    } else {
                                        tv_audioPlay.setText("00:" + audioLength / 10 + audioLength % 10);
                                    }
                                    playOrStop = true;
                                    mCountDown.onFinish();
                                    spangled.onFinish();
                                }

                                @Override
                                public void onComplete(Uri var1) {
                                    //播完（一般是停止语音消息动画）
                                    if (audioLength == 60) {
                                        tv_audioPlay.setText("01:00");
                                    } else {
                                        tv_audioPlay.setText("00:" + audioLength / 10 + audioLength % 10);
                                    }
                                    playOrStop = true;
                                }
                            });
                } else {
                    playOrStop = true;
                    AudioPlayManager.getInstance().stopPlay();
                }
                break;

            case R.id.del_response_record://删除录音按钮

                AlertDialog.Builder builder2 = new AlertDialog.Builder(QuestionResponseActivity.this);
                builder2.setTitle("提示");
                builder2.setMessage("确定删除该条录音吗？");

                builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rl_audioPlay.setVisibility(View.GONE);
                        //删除本地录音
                        FileUtils.deleteFile(soundPath);//删除本地文件
                        soundPath = null;
                        soundUri = null;
                    }
                });
                builder2.create().show();
                break;

            case R.id.iv_response_audio://录音按钮

                //如果已经有录好的音频，则弹出提示框，提示是否录制
                if (soundPath != null) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(QuestionResponseActivity.this);
                    builder1.setTitle("提示");
                    builder1.setMessage("仅支持一条录音，确定需要重新录制，替换当前的录音吗？");

                    builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ll_response_audio_record.setVisibility(View.VISIBLE);

                        }
                    });
                    builder1.create().show();

                } else {
                    //如果录音界面可见，则点击会隐藏，反之则点击会显示
                    if (ll_response_audio_record.getVisibility() == View.GONE) {
                        ll_response_audio_record.setVisibility(View.VISIBLE);
                    } else if (ll_response_audio_record.getVisibility() == View.VISIBLE) {
                        ll_response_audio_record.setVisibility(View.GONE);
                    }
                }

                break;

            case R.id.iv_response_image://相册按钮

                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(QuestionResponseActivity.this)
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
                        .selectionMedia(selectList)// 是否传入已选图片
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(false)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .recordVideoSecond(60)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                break;

            case R.id.iv_response_take_photo://拍照按钮
                if(selectList.size()<9){
                    picOrVedioDialog();
                }else{
                    ToastUtils.showShort("已选9张图片，无法继续添加");
                }

                break;

            case R.id.iv_response_add_document://附件按钮
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

                //把已选中的发到AddDocumentActivity，用于防止重复选中
                Intent intentDocumnet = new Intent(QuestionResponseActivity.this, AddDocumentActivity.class);
                intentDocumnet.putExtra("from", "QuestionResponseActivity");
                intentDocumnet.putExtra("documentFileString", documentFileString);
                startActivity(intentDocumnet);
                break;

            case R.id.iv_response_at://at按钮
                Intent intentAt = new Intent(QuestionResponseActivity.this, AtActivity.class);
                intentAt.putExtra("atListStr", atNameStr);
                intentAt.putExtra("from", "QuestionResponseActivity");
                startActivityForResult(intentAt, IntentConfig.RESPONSE_AT);
                break;

            case R.id.bt_photo:
                // 单独拍照
                PictureSelector.create(QuestionResponseActivity.this)
                        .openCamera(1)// 单独拍照
                        .theme(themeId)// 主题样式设置 具体参考 values/styles
                        .maxSelectNum(9)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片
                        .previewVideo(true)// 是否可预览视频
                        .enablePreviewAudio(true) // 是否可播放音频
                        .isCamera(true)// 是否显示拍照按钮
                        .enableCrop(true)// 是否裁剪
                        .compress(true)// 是否压缩
                        .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .circleDimmedLayer(false)// 是否圆形裁剪
                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .openClickSound(true)// 是否开启点击声音
                        .selectionMedia(selectList)// 是否传入已选图片
                        .previewEggs(true)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picOrVideoDialog.dismiss();
                break;

            case R.id.bt_vedio:

                // 单独拍视频
                PictureSelector.create(QuestionResponseActivity.this)
                        .openCamera(2)// 单独拍视频
                        .theme(themeId)// 主题样式设置 具体参考 values/styles
                        .maxSelectNum(9)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选PictureConfig.MULTIPLE : PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片
                        .previewVideo(true)// 是否可预览视频
                        .enablePreviewAudio(true) // 是否可播放音频
                        .isCamera(true)// 是否显示拍照按钮
                        .enableCrop(true)// 是否裁剪
                        .compress(true)// 是否压缩
                        .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                        .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                        .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                        .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                        .circleDimmedLayer(false)// 是否圆形裁剪
                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                        .openClickSound(true)// 是否开启点击声音
                        .selectionMedia(selectList)// 是否传入已选图片
                        .previewEggs(true)//预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                picOrVideoDialog.dismiss();
                break;

            case R.id.btn_cancel:
                picOrVideoDialog.dismiss();
                break;
        }

    }

    //获取文件上传MD5
    private void getArr(final List<String> list) {

        selectList.clear();

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
            getDataFromInterNet("", "", "");//没有图片和视频和音频的情况
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

                    if (GetFileType.fileType(filePath).equals("图片")) {

                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("Name", "" + UpLoadFileUtil.getFileName(filePath));
                        map.put("ID", "" + AnnexMd5Code);
                        picAnnexMd5CodeList.add(JSON.toJSONString(map));
                    } else if (GetFileType.fileType(filePath).equals("视频")) {
                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("Name", "" + UpLoadFileUtil.getFileName(filePath));
                        map.put("ID", "" + AnnexMd5Code);
                        vedioAnnexMd5CodeList.add(JSON.toJSONString(map));
                    } else if (GetFileType.fileType(filePath).equals("音乐")) {
                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("Name", "" + UpLoadFileUtil.getFileName(filePath));
                        map.put("ID", "" + AnnexMd5Code);
                        audioAnnexMd5CodeList.add(JSON.toJSONString(map));
                    }

                    if (isLastOne == 0) {
                        getDataFromInterNet(picAnnexMd5CodeList.toString(), vedioAnnexMd5CodeList.toString(), audioAnnexMd5CodeList.toString());
                    }
                } else {
                    ToastUtils.showShort(UpLoadFileUtil.getFileName(filePath) + "上传失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }

            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();  //将进度条隐藏
            }
        }

    }

    //从网络获取数据
    public void getDataFromInterNet(String picAnnexMd5CodeString, String vedioAnnexMd5CodeString, String audioAnnexMd5CodeString) {

        //准备附件数据
        for (int i = 0; i < documentList.size(); i++) {
            //获取用于上传的附件字段
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("Name", documentList.get(i).getFileNama());
            map.put("ID", documentList.get(i).getFileString());
            docuemntStrList.add(JSON.toJSONString(map));
        }

        //组装at数据
        if (atListStrForResponse != null && (!atListStrForResponse.equals("")) &&
                (!atListStrForResponse.equals("[]")) && (!atListStrForResponse.equals("null"))) {
            for (int i = 0; i < atListStrForResponse.split(",").length; i++) {
                //获取用于上传的at字段
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Name", atListStrForResponse.split(",")[i].split(":")[0]);
                map.put("ID", atListStrForResponse.split(",")[i].split(":")[1]);
                atlist.add(JSON.toJSONString(map));
            }
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("UserName", SPUtils.getInstance().getString("UserName"))//用户名
                .add("UserID", "" + SPUtils.getInstance().getInt("UserID"))//内容
                .add("At", atlist.toString())//At内容
                .add("Pictures", "" + picAnnexMd5CodeString)//图片
                .add("Video", "" + vedioAnnexMd5CodeString)//视频
                .add("Voice", "" + audioAnnexMd5CodeString)//录音
                .add("Comment", responseText.getText().toString())//文本内容
                .add("DocumentIds", docuemntStrList.toString())//文本内容
                .build();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/SynergyQuestion/" + topicid + "/Comment")
                .post(requestBody)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("添加评论失败");
                progressDialog.dismiss();  //将进度条隐藏
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ToastUtils.showShort("添加评论成功");
                    EventBus.getDefault().post(new CommonEven("刷新问题详情"));//发送消息,刷新问题详情
                    progressDialog.dismiss();  //将进度条隐藏
                    try {
                        String responseData = response.body().string();
                        org.json.JSONObject jsonObject = null;
                        jsonObject = new org.json.JSONObject(responseData);
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

                        QuestionCommentModel questionCommentModel = new QuestionCommentModel(ID, TopicId, UserId, At, Date, Comment, Pictures, DocumentIds, Voice, Video, UserName);

                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据解析出错");
                        progressDialog.dismiss();  //将进度条隐藏
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        progressDialog.dismiss();  //将进度条隐藏
                        finish();
                    }
                } else {
                    ToastUtils.showShort("添加评论失败");
                    progressDialog.dismiss();  //将进度条隐藏
                    finish();
                }
            }
        });
    }


//    //添加评论
//    private void addComment() {
//
//        //组装at数据
//        for (int i = 0; i < atListStrForResponse.split(",").length; i++) {
//            //获取用于上传的at字段
//            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//            map.put("Name", atListStrForResponse.split(",")[i].split(":")[0]);
//            map.put("ID", atListStrForResponse.split(",")[i].split(":")[1]);
//            atlist.add(JSON.toJSONString(map));
//        }
//
//        //组装图片和视频字段
//        for (int i = 0; i < selectList.size(); i++) {
//
//            if (GetFileType.fileType(selectList.get(i).getPath()).equals("图片")) {
//                //获取用于上传的图片字段
//                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//                map.put("Name", new File(selectList.get(i).getPath()).getName());
//                map.put("ID", selectList.split(",")[i].split(":")[1]);
//                atlist.add(JSON.toJSONString(map));
//            } else if (GetFileType.fileType(selectList.get(i).getPath()).equals("视频")) {
//                //获取用于上传的视频字段
//                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//                map.put("Name", atListStrForResponse.split(",")[i].split(":")[0]);
//                map.put("ID", atListStrForResponse.split(",")[i].split(":")[1]);
//                atlist.add(JSON.toJSONString(map));
//            }
//
//
//        }
//
//        RequestBody requestBody = new FormBody.Builder()
//                .add("UserName", SPUtils.getInstance().getString("UserName"))//用户名
//                .add("UserID", "" + SPUtils.getInstance().getInt("UserID"))//内容
////                .add("At", atlist.toString())//At内容
//                .add("Pictures", "")//图片
////                .add("GroupId", GroupId)//讨论组id
//                .build();
//
//        Request request = new Request.Builder()
//                .url(AppConst.innerIp + "/api/" + projectID + "/SynergyQuestion/" + topicid + "/Comment")
//                .post(requestBody)
//                .build();
//
//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ToastUtils.showShort("添加评论失败");
////                progressDialog.dismiss();  //将进度条隐藏
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.code() == 200) {
//                    ToastUtils.showShort("添加评论成功");
////                    progressDialog.dismiss();  //将进度条隐藏
//                    try {
//                        String responseData = response.body().string();
//                        org.json.JSONObject jsonObject = null;
//                        jsonObject = new org.json.JSONObject(responseData);
//                        String TopicId = jsonObject.getString("TopicId");
//                        String UserId = jsonObject.getString("UserId");
//                        int At = jsonObject.getInt("At");
//                        Object Date = jsonObject.get("Date");
//                        String Comment = jsonObject.getString("Comment");
//                        String Pictures = jsonObject.getString("Pictures");
//                        String DocumentIds = jsonObject.getString("DocumentIds");
//                        String Voice = jsonObject.getString("Voice");
//                        String Video = jsonObject.getString("TopVideoicId");
//
//                        QuestionCommentModel questionCommentModel = new QuestionCommentModel(TopicId, UserId, At, Date, Comment, Pictures, DocumentIds, Voice, Video);
//                        finish();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        ToastUtils.showShort("数据请求出错");
////                        progressDialog.dismiss();  //将进度条隐藏
//                    }
//                } else {
//                    ToastUtils.showShort("添加评论失败");
////                    progressDialog.dismiss();  //将进度条隐藏
//                }
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                if (picAndVideoList != null) {
                    picAndVideoList.clear();
                }
                // 图片选择结果回调
                selectList = PictureSelector.obtainMultipleResult(data);

                //如果gridImageAdapter不等于null，则直接刷新，否则创建gridImageAdapter，显示数据
                if (gridImageAdapter != null) {
                    if (selectList != null && selectList.size() > 0) {
                        recyclerViewPic.setVisibility(View.VISIBLE);
                        picAndVideoList.addAll(selectList);
                        gridImageAdapter.notifyDataSetChanged();
                    } else {
                        recyclerViewPic.setVisibility(View.GONE);
                    }
                } else {
                    if (selectList != null && selectList.size() > 0) {
                        recyclerViewPic.setVisibility(View.VISIBLE);
                        picAndVideoList.addAll(selectList);
                        gridImageAdapter = new GridImageAdapter(QuestionResponseActivity.this, onAddPicClickListener, false);
                        gridImageAdapter.setList(picAndVideoList);
                        recyclerViewPic.setAdapter(gridImageAdapter);
                        gridImageAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position, View v) {
                                if (picAndVideoList.size() > 0) {
                                    LocalMedia media = picAndVideoList.get(position);
                                    String pictureType = media.getPictureType();
                                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                                    switch (mediaType) {
                                        case 1:
                                            // 预览图片 可自定长按保存路径
                                            PictureSelector.create(QuestionResponseActivity.this).themeStyle(themeId).openExternalPreview(position, picAndVideoList);
                                            break;
                                        case 2:
                                            // 预览视频
                                            PictureSelector.create(QuestionResponseActivity.this).externalPictureVideo(media.getPath());
                                            break;
                                        case 3:
                                            // 预览音频
                                            PictureSelector.create(QuestionResponseActivity.this).externalPictureAudio(media.getPath());
                                            break;
                                    }
                                }
                            }
                        });
                    } else {
                        recyclerViewPic.setVisibility(View.GONE);
                    }

                }

            } else if (requestCode == IntentConfig.RESPONSE_AT) {
                atListStrForResponse = data.getStringExtra("data_return");
                //at人的显示与隐藏
                if (atListStrForResponse != null && atListStrForResponse != "" && atListStrForResponse.length() > 0) {
                    ll_responseAt.setVisibility(View.VISIBLE);
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < atListStrForResponse.split(",").length; i++) {
                        sb.append(atListStrForResponse.split(",")[i].split(":")[0]);
                        sb.append("、");
                    }
                    tv_responseAt.setText(sb.subSequence(0, sb.length() - 1).toString());
                } else {
                    ll_responseAt.setVisibility(View.GONE);
                }

            }
        }
    }

    //图片和视频选择
    private void picOrVedioDialog() {
        picOrVideoDialog = new Dialog(QuestionResponseActivity.this, R.style.ActionSheetDialogStyle);
        inflate = LayoutInflater.from(QuestionResponseActivity.this).inflate(R.layout.bottom_dialog, null);
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
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        SPUtils.getInstance().remove(projectID + "新建问题");//移除持久化的中数据
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.response_press_record) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AudioRecordManager.getInstance(QuestionResponseActivity.this).startRecord();

                    handler.post(sound_record);//开始计时
                    int animationDuration = 60000;
                    circularProgressBar.setProgressWithAnimation(100, animationDuration);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled(view, motionEvent)) {
                        AudioRecordManager.getInstance(QuestionResponseActivity.this).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance(QuestionResponseActivity.this).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance(QuestionResponseActivity.this).stopRecord();
                    AudioRecordManager.getInstance(QuestionResponseActivity.this).destroyRecord();
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
//                    ToastUtils.showShort("onStartRecord");
                    //开始录制

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

                        circularProgressBar.setProgress(0);
                        responseCountDown.setText("00:00");
                        ll_response_audio_record.setVisibility(View.GONE);

                        if (soundPath == null) {
                            rl_audioPlay.setVisibility(View.GONE);
                        } else {
                            rl_audioPlay.setVisibility(View.VISIBLE);
                            if (audioLength >= 60) {
                                tv_audioPlay.setText("01:00");
                            } else {
                                tv_audioPlay.setText("00:" + audioLength / 10 + audioLength % 10);
                            }
                        }

//                        //把声音信息传递到QuestionResponseActivity
//                        Intent intent = new Intent(QuestionResponseActivity.this, QuestionResponseActivity.class);
//                        intent.putExtra("audioLength", audioLength);
//                        intent.putExtra("soundPath", soundPath);
//                        intent.putExtra("topicid", questionModel.getID());
//                        intent.putExtra("from", "audio");
//                        startActivity(intent);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        selectList.clear();
    }
}
