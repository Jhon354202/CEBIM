package com.chenxi.cebim.activity.coordination;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
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
import com.chenxi.cebim.activity.PreviewFileActivity;
import com.chenxi.cebim.adapter.DocumentAdapter;
import com.chenxi.cebim.adapter.GridImageAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.utils.CountDownTimerUtils;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.chenxi.cebim.utils.UpLoadFileUtil;
import com.chenxi.cebim.view.ClearEditText;
import com.chenxi.cebim.view.FullyGridLayoutManager;
import com.kyleduo.switchbutton.SwitchButton;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.audio.IAudioRecordListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.EasyPermissions;

public class NewQuestion extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private SwipeRefreshLayout swipeRefresh;
    private TextView submit, tv_moreSetting, complete, uncomplete, reprieve, medium, emergent, picNum, countDown,
            category, systemTyp, deadline, discussionGroup, atNameList;
    private ClearEditText et_title, et_comment;
    private ImageView back, iv_moresetting_arrow, pressRecord;
    private LinearLayout ll_voice, ll_group, ll_documents, ll_at,
            ll_moresetting, ll_category, ll_systemTyp, ll_deadline, ll_Uuids, ll_show_hide;

    Button getPhoto, getVedio, cancel;

    private SwitchButton sb_observed;
    private RelativeLayout rl_document;
    private RecyclerView recyclerView, recyclerDocument;
    private GridImageAdapter adapter;
    private DocumentAdapter documentAdapter;
    private int maxSelectNum = 9;

    CircularProgressBar circularProgressBar;

    private Boolean isCompleteOrNo = false;//进度，默认为false
    private int priorityNu = 1;//优先级，取值为：0、暂缓（默认值），1、中等,2、紧急
    private Boolean isObserved = false;//是否关注，默认为不关注(false)
    private List<LocalMedia> selectList = new ArrayList<>();//用于图片选取后装载图片对象
    private List<LocalMedia> showList = new ArrayList<>();//用于显示图片

    private List<String> PathList = new ArrayList<>();//用于装载上传文件地址

    private int themeId;

    private int projectID;//项目
    private String GroupId = "";//讨论组ID
    private String Category = "";//类型id
    private String SystemType = "";//专业
    private String SystemTypeName="";//专业名
    private String Deadline = "";//期限
    private String atNameStr = "";//at人名字字符串

    private List<String> picAnnexMd5CodeList = new ArrayList<>();//图片MD5列表
    private List<String> vedioAnnexMd5CodeList = new ArrayList<>();//视频MD5列表
    private List<String> audioAnnexMd5CodeList = new ArrayList<>();//音频MD5列表

    private List<String> atlist = new ArrayList<>();//at列表字符串
    private List<String> docuemntStrList = new ArrayList<>();//docuemnt列表字符串

    private List<DocumentModel> documentList = new ArrayList<>();

    private ProgressDialog progressDialog;

    private RelativeLayout rl_playAudio;
    private LinearLayout ll_playAudio;//播放录音按钮
    private TextView playAudioTime;//播放录音时间
    private ImageView playAudioImage;//播放录音图片
    private ImageView recordAgain, delAudio;

    private Dialog audioDialog, picOrVideoDialog;//audioDialog录音弹出框、picOrVideoDialog弹出框
    private View inflate;

    String savePath;//图片、视频、录音存储路径

    String soundPath;//录音本地地址
    File soundFile;//录音文件
    boolean playOrStop = true;

    private File mAudioDir;//录音文件
    private Uri audioURI;//录音成功返回的URI
    private int audioLength;//录音时长

    CountDownTimerUtils mCountDown;
    CountDownTimerUtils spangled;

    String documentsId;//接收从AddDocumentActivity中返回的Fid字符串
    String categoryString;

    Handler handler = new Handler();
    private int recLen = 0;

    //权限申请 RequestCode
    public static final int REQUEST_CODE = 111;
    private boolean noFirstRequestLocation = false;
    private boolean noFirstRequestCarmera = false;

    //扫描结果
    String scanResult;

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
        setContentView(R.layout.activity_new_question);

        //注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);//注册EventBus
        }

        SPUtils.getInstance().remove(projectID + "新建问题");

        projectID = SPUtils.getInstance().getInt("projectID");
        savePath = DiskCacheDirUtil.getDiskCacheDir(NewQuestion.this);//图片、视频、录音存储路径

        //控件初始化
        initView();

        //初始化录音
        initRecord();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SPUtils.getInstance().remove(projectID + "新建问题");
    }

    //定时器，用于录音计时
    Runnable sound_record = new Runnable() {
        public void run() {
            recLen++;
            if (recLen < 60) {
                countDown.setText("00:" + recLen / 10 + recLen % 10);
            } else {
                countDown.setText("01:00");
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

        submit = findViewById(R.id.toolbar_right_tv);//发送按钮
        submit.setOnClickListener(this);

        et_title = findViewById(R.id.et_question_title);//标题填写框
        et_comment = findViewById(R.id.et_question_content);

        ll_voice = findViewById(R.id.ll_vedio);//录音按钮
        ll_voice.setOnClickListener(this);

        rl_playAudio = findViewById(R.id.rl_audio_play);//录音成功时显示此页面

        ll_playAudio = findViewById(R.id.ll_audio_play);//播放录音按钮
        ll_playAudio.setOnClickListener(this);

        playAudioTime = findViewById(R.id.tv_audio_play);//播放录音读秒时间
        playAudioImage = findViewById(R.id.iv_audio_play);

        recordAgain = findViewById(R.id.record_again);//重新录音
        recordAgain.setOnClickListener(this);

        delAudio = findViewById(R.id.del_record);//删除录音
        delAudio.setOnClickListener(this);

        picNum = findViewById(R.id.tv_pic_nu);//待上传图片数量
        picNum.setText("0/9");


        ll_group = findViewById(R.id.ll_group);//讨论组按钮
        ll_group.setOnClickListener(this);

        discussionGroup = findViewById(R.id.tv_discussion_group);

        ll_documents = findViewById(R.id.ll_documents);//附件按钮
        ll_documents.setOnClickListener(this);

        ll_at = findViewById(R.id.ll_at);//@人按钮
        ll_at.setOnClickListener(this);

        ll_moresetting = findViewById(R.id.ll_moresetting);//更多设置按钮
        ll_moresetting.setOnClickListener(this);

        ll_category = findViewById(R.id.ll_category);//类型
        ll_category.setOnClickListener(this);

        category = findViewById(R.id.tv_category);//类型

        ll_systemTyp = findViewById(R.id.ll_systemTyp);//专业
        ll_systemTyp.setOnClickListener(this);

        systemTyp = findViewById(R.id.tv_systemTyp);

        ll_deadline = findViewById(R.id.ll_deadline);//期限
        ll_deadline.setOnClickListener(this);

        atNameList = findViewById(R.id.at_name_list);

        deadline = findViewById(R.id.tv_deadline);

        ll_Uuids = findViewById(R.id.ll_Uuids);//优先级
        ll_Uuids.setOnClickListener(this);

        ll_show_hide = findViewById(R.id.ll_show_hide);//更多设置下面部分的内容父控件，用于更多设置和收起

        tv_moreSetting = findViewById(R.id.tv_moresetting);//更多设置文本
        iv_moresetting_arrow = findViewById(R.id.iv_arrow);//更多设置箭头

        complete = findViewById(R.id.tv_complete);//进度—已完成
        complete.setOnClickListener(this);

        uncomplete = findViewById(R.id.tv_uncomplete);//进度—未完成
        uncomplete.setOnClickListener(this);

        reprieve = findViewById(R.id.tv_reprieve);//优先级-暂缓
        reprieve.setOnClickListener(this);

        medium = findViewById(R.id.tv_medium);//优先级-中等
        medium.setOnClickListener(this);

        emergent = findViewById(R.id.emergent);//优先级-紧急
        emergent.setOnClickListener(this);

        sb_observed = findViewById(R.id.sb_isobserve);//是否关注选择按钮
        sb_observed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    isObserved = true;
                } else {
                    isObserved = false;
                }
            }
        });

        swipeRefresh = findViewById(R.id.new_question_swip_refresh);//下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(false);
            }
        });

        //图片选择器
        FullyGridLayoutManager manager = new FullyGridLayoutManager(NewQuestion.this, 4, GridLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.newquestion_recyclerView);
        recyclerView.setLayoutManager(manager);
        adapter = new GridImageAdapter(NewQuestion.this, onAddPicClickListener, true);
        adapter.setList(showList);
//        adapter.setSelectMax(maxSelectNum);
        recyclerView.setAdapter(adapter);
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
                            //PictureSelector.create(MainActivity.this).themeStyle(themeId).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(NewQuestion.this).themeStyle(themeId).openExternalPreview(position, showList);
                            break;
                        case 2:
                            // 预览视频
                            PictureSelector.create(NewQuestion.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // 预览音频
                            PictureSelector.create(NewQuestion.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });

        rl_document=findViewById(R.id.rl_document);

        recyclerDocument = findViewById(R.id.rv_document);
        LinearLayoutManager layoutManager = new LinearLayoutManager(NewQuestion.this);
        recyclerDocument.setLayoutManager(layoutManager);

        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(NewQuestion.this);
                } else {
                    Toast.makeText(NewQuestion.this,
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * 获取特定格式的当前时间
     *
     * @return
     */
    private String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
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
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ToastUtils.showShort(UpLoadFileUtil.getFileName(filePath)+"上传失败");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                try {
//                    if(response.code()==200){
//                        String responseData = response.body().string();
//                        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(responseData);
//                        String AnnexMd5Code = jsonObject.getString("AnnexMd5Code");
//                        HashMap<String,String> uploadFile=new HashMap<String,String>();
//                        uploadFile.put(UpLoadFileUtil.getFileName(filePath),AnnexMd5Code);
//                        AnnexMd5CodeList.add(uploadFile);
//                    }else{
//                        ToastUtils.showShort(UpLoadFileUtil.getFileName(filePath)+"上传失败");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    ToastUtils.showShort(UpLoadFileUtil.getFileName(filePath)+"上传失败");
//                }
//            }
//        });
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

        RequestBody requestBody = new FormBody.Builder()
                .add("Title", et_title.getText().toString())//标题
                .add("Comment", et_comment.getText().toString())//内容
                .add("ProjectId", "" + projectID)//项目ID
                .add("GroupId", GroupId)//讨论组id
                .add("Category", Category)//类型id
                .add("SystemType", SystemType)//专业id
                .add("State", "" + isCompleteOrNo)//进度
                .add("Deadline", Deadline)//完成日期
                .add("At", atlist.toString())//话题列表
                .add("UserID", "" + SPUtils.getInstance().getInt("UserID"))//用户ID
                .add("Date", getCurrentTime())//创建日期
                .add("Priority", "" + priorityNu)//优先级
                .add("Observed", "" + isObserved)//我关注的
                .add("Pictures", "" + picAnnexMd5CodeString)//图片
                .add("Video", "" + vedioAnnexMd5CodeString)//视频
                .add("Voice", "" + audioAnnexMd5CodeString)//录音
                .add("DocumentIds", docuemntStrList.toString())//附件
                .build();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/SynergyQuestion")
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
                    try {
                        String responseData = response.body().string();
                        org.json.JSONObject jsonObject = null;
                        jsonObject = new org.json.JSONObject(responseData);

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

                        String SystemTypeName;
                        if (jsonObject.get("SystemTypeName").toString().equals("null")) {
                            SystemTypeName = null;
                        } else {
                            SystemTypeName = new JSONObject(jsonObject.get("SystemTypeName").toString()).getString("Name");
                        }
                        String firstFrame = "";//第一帧图片地址

                        String ObservedUsers=jsonObject.getString("ObservedUsers");

                        Boolean State = jsonObject.getBoolean("State");

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
                                ID, Title, Comment, GroupId, Category, ViewportId, SystemType, At, Pictures,
                                Uuids, SelectionSetIds, Video, Voice, Tags, ReadUsers, DocumentIds, UserName, CategoryName,SystemTypeName,firstFrame,
                                ObservedUsers, State, Observed, IsFinishedAndDelay, CompletedAt, Deadline, Date, LastUpdate);

                        EventBus.getDefault().post(new NewQuestionDelEven("" + State));
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                        progressDialog.dismiss();  //将进度条隐藏
                    }
                } else {
                    ToastUtils.showShort("问题创建失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }
            }
        });
    }

    //图片和视频选择
    private void picOrVedioDialog() {
        picOrVideoDialog = new Dialog(NewQuestion.this, R.style.ActionSheetDialogStyle);
        inflate = LayoutInflater.from(NewQuestion.this).inflate(R.layout.bottom_dialog, null);
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

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {

//            if (showList != null && showList.size() > 0) {
//
//                if (GetFileType.fileType("" + showList.get(0).getPath()).equals("图片")) {
//                    //弹出一个dialog选择图片或者视频
//                    picOrVedioDialog();
//                    getVedio.setVisibility(View.GONE);
//                }
//
//            } else {
//                //弹出一个dialog选择图片或者视频
//                picOrVedioDialog();
//            }

            // 进入相册 以下是例子：不需要的api可以不写
            PictureSelector.create(NewQuestion.this)
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
                    .selectionMedia(showList)// 是否传入已选图片
                    .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    .synOrAsy(false)//同步true或异步false 压缩 默认同步
                    .videoQuality(1)// 视频录制质量 0 or 1 int
                    .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                    .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                    .recordVideoSecond(60)//视频秒数录制 默认60s int
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

//            picOrVideoDialog.dismiss();
        }

    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.toolbar_left_btn:
                finish();
                break;

            case R.id.toolbar_right_tv:

                if (et_title.getText().toString() == null || et_title.getText().toString().equals("")) {
                    ToastUtils.showShort("请输入标题");
                } else {
                    //加载加进度条，加班类别请求成功后消失。
                    progressDialog = new ProgressDialog(NewQuestion.this);
                    progressDialog.setMessage("创建中...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();  //将进度条显示出来
                    //上传图片和视频
                    for (int i = 0; i < showList.size(); i++) {
                        PathList.add(showList.get(i).getPath());
                    }

                    //add录音文件地址
                    if (soundPath != null) {
                        PathList.add(soundPath);
                    }

                    getArr(PathList);
                }

                break;

            case R.id.ll_vedio:
                audioDialog = new Dialog(NewQuestion.this, R.style.ActionSheetDialogStyle);
                inflate = LayoutInflater.from(this).inflate(R.layout.sound_record_dialog, null);
                countDown = inflate.findViewById(R.id.count_down);

                circularProgressBar = (CircularProgressBar) inflate.findViewById(R.id.cb_question);

                pressRecord = inflate.findViewById(R.id.press_record);
                pressRecord.setOnTouchListener(this);
                audioDialog.setContentView(inflate);
                Window dialogWindow = audioDialog.getWindow();
                dialogWindow.setGravity(Gravity.BOTTOM);
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.y = 0;
                dialogWindow.setAttributes(lp);
                audioDialog.show();
                break;

            case R.id.ll_group:
                Intent intentGroup = new Intent(NewQuestion.this, DiscussionGroupActivity.class);
                startActivity(intentGroup);
                break;

            case R.id.ll_documents:

                StringBuffer sb = new StringBuffer();
                String documentFileString = new String();

                if (documentList != null&& documentList.size() > 0) {
                    for (int i = 0; i < documentList.size(); i++) {
                        sb.append(documentList.get(i).getFileString());
                        sb.append(":");
                        sb.append(documentList.get(i).getFileNama());
                        if(i<documentList.size()-1){
                            sb.append(",");
                        }
                    }
                    documentFileString=sb.toString();

                } else {
                    documentFileString = "";
                }

                Intent intentDocumnet = new Intent(NewQuestion.this, AddDocumentActivity.class);
                intentDocumnet.putExtra("documentFileString", documentFileString);
                intentDocumnet.putExtra("from", "NewQuestion");
                startActivity(intentDocumnet);

                break;

            case R.id.ll_at:
                Intent intentAt = new Intent(NewQuestion.this, AtActivity.class);
                intentAt.putExtra("atListStr", atNameStr);
                intentAt.putExtra("from", "NewQuestion");
                startActivity(intentAt);
                break;

            case R.id.ll_moresetting:
                if (ll_show_hide.getVisibility() == View.VISIBLE) {
                    ll_show_hide.setVisibility(View.GONE);
                    tv_moreSetting.setText("更多设置");
                    iv_moresetting_arrow.setImageResource(R.drawable.double_down_arrow);
                } else {
                    ll_show_hide.setVisibility(View.VISIBLE);
                    tv_moreSetting.setText("收起");
                    iv_moresetting_arrow.setImageResource(R.drawable.double_up_arrow);
                }
                break;

            case R.id.ll_category:
                Intent intent1 = new Intent(NewQuestion.this, CategoryActivity.class);
                startActivity(intent1);
                break;

            case R.id.ll_systemTyp:
                Intent intent2 = new Intent(NewQuestion.this, SystemTypeActivity.class);
                startActivity(intent2);
                break;

            case R.id.ll_deadline:
                showDatePickerDialog(NewQuestion.this, deadline, Calendar.getInstance());
                break;

            case R.id.ll_Uuids:
                if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {//检查是否获取该权限
                    Intent intentUuids = new Intent(NewQuestion.this, CaptureActivity.class);
                    startActivityForResult(intentUuids, REQUEST_CODE);
                } else {
                    //第二个参数是被拒绝后再次申请该权限的解释
                    //第三个参数是请求码
                    //第四个参数是要申请的权限
                    EasyPermissions.requestPermissions(this, "扫二维码需要拍照权限", 0, Manifest.permission.CAMERA);

                    //如果用户勾选了“不在提示”，则直接打开app的管理菜单界面，用户可点击这个界面的权限管理，进行授权或关闭权限
                    if (noFirstRequestCarmera && EasyPermissions.permissionPermanentlyDenied(NewQuestion.this, Manifest.permission.CAMERA)) {
                        getAppDetailSettingIntent(NewQuestion.this);
                        ToastUtils.showShort("扫码需要拍照权限，请在权限管理菜单中开启拍照权限");
                    }
                    noFirstRequestCarmera = true;
                }

                break;

            case R.id.tv_uncomplete:

                if (isCompleteOrNo) {
                    uncomplete.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.white));
                    uncomplete.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_blue_shape, null));
                    complete.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    complete.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    isCompleteOrNo = false;
                }

                break;

            case R.id.tv_complete:

                if (!isCompleteOrNo) {
                    complete.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.white));
                    complete.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_blue_shape, null));
                    uncomplete.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    uncomplete.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    isCompleteOrNo = true;
                }

                break;

            case R.id.tv_reprieve:

                if (priorityNu != 0) {
                    reprieve.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.white));
                    reprieve.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_blue_shape, null));
                    medium.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    medium.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    emergent.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    emergent.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    priorityNu = 0;
                }
                break;

            case R.id.tv_medium:

                if (priorityNu != 1) {
                    reprieve.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    reprieve.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    medium.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.white));
                    medium.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_blue_shape, null));
                    emergent.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    emergent.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    priorityNu = 1;
                }

                break;

            case R.id.emergent:

                if (priorityNu != 2) {
                    reprieve.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    reprieve.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    medium.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.gray_text));
                    medium.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_gray_shape, null));
                    emergent.setTextColor(ContextCompat.getColor(NewQuestion.this, R.color.white));
                    emergent.setBackground(MyApplication.getContext().getResources().getDrawable(R.drawable.new_question_blue_shape, null));
                    priorityNu = 2;
                }

                break;

            case R.id.ll_audio_play:

                if (playOrStop) {
                    playOrStop = false;

                    AudioPlayManager.getInstance().startPlay(NewQuestion.this, audioURI, new IAudioPlayListener() {
                        @Override
                        public void onStart(Uri var1) {
                            //开播（一般是开始语音消息动画）
                            mCountDown = new CountDownTimerUtils(playAudioTime,
                                    audioLength * 1000, 1000, 0);
                            mCountDown.start();

                            spangled = new CountDownTimerUtils(playAudioImage,
                                    audioLength * 1000, 500, 1);
                            spangled.start();
                        }

                        @Override
                        public void onStop(Uri var1) {
                            //停播（一般是停止语音消息动画）
                            if (audioLength == 60) {
                                playAudioTime.setText("01:00");
                            } else {
                                playAudioTime.setText("00:" + audioLength / 10 + audioLength % 10);
                            }
                            playOrStop = true;
                            mCountDown.onFinish();
                            spangled.onFinish();
                        }

                        @Override
                        public void onComplete(Uri var1) {
                            //播完（一般是停止语音消息动画）
                            if (audioLength == 60) {
                                playAudioTime.setText("01:00");
                            } else {
                                playAudioTime.setText("00:" + audioLength / 10 + audioLength % 10);
                            }
                            playOrStop = true;
                        }
                    });
                } else {
                    playOrStop = true;
                    AudioPlayManager.getInstance().stopPlay();
                }
                break;

            case R.id.record_again:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(NewQuestion.this);
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

                        Window dialogWindow = audioDialog.getWindow();
                        dialogWindow.setGravity(Gravity.BOTTOM);
                        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.y = 0;
                        dialogWindow.setAttributes(lp);
                        ll_voice.setVisibility(View.VISIBLE);//点击播放录音描述界面显示
                        rl_playAudio.setVisibility(View.GONE);//录音播放界面隐藏
                        countDown.setText("00:00");
                        AudioPlayManager.getInstance().stopPlay();//停止播放录音
                        audioDialog.show();
                    }
                });
                builder1.create().show();
                break;

            case R.id.del_record:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(NewQuestion.this);
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
                        rl_playAudio.setVisibility(View.GONE);
                        ll_voice.setVisibility(View.VISIBLE);
                        //删除本地录音
                        FileUtils.deleteFile(soundPath);
                        ToastUtils.showShort("" + FileUtils.deleteFile(soundPath));
                    }
                });
                builder2.create().show();
                break;

            case R.id.bt_photo:
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(NewQuestion.this)
                        .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .imageSpanCount(3)// 每行显示个数
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片 true or false
                        .isCamera(true)// 是否显示拍照按钮 true or false
                        .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath(savePath + "PHOTO")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(true)// 是否压缩 true or false
                        .isGif(true)// 是否显示gif图片 true or false
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(false)//同步true或异步false 压缩 默认同步
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picOrVideoDialog.dismiss();
                break;

            case R.id.bt_vedio:
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(NewQuestion.this)
                        .openGallery(PictureMimeType.ofVideo())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(1)// 最小选择数量
                        .imageSpanCount(3)// 每行显示个数
                        .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片 true or false
                        .previewVideo(true)// 是否可预览视频 true or false
                        .enablePreviewAudio(true) // 是否可播放音频 true or false
                        .isCamera(true)// 是否显示拍照按钮 true or false
                        .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath(savePath + "VEDIO")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(false)// 是否压缩 true or false
                        .isGif(true)// 是否显示gif图片 true or false
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(false)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .recordVideoSecond(60)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picOrVideoDialog.dismiss();
                break;

            case R.id.btn_cancel:
                picOrVideoDialog.dismiss();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(selectList!=null){
            selectList.clear();
        }

        if(showList!=null){
            showList.clear();
        }
        //处理二维码扫描结果
        if (resultCode == RESULT_OK) {
            if ((requestCode == REQUEST_CODE)) {
                //处理扫描结果（在界面上显示）
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        scanResult = result;
                        Toast.makeText(NewQuestion.this, "扫码成功" + scanResult, Toast.LENGTH_LONG).show();
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        Toast.makeText(NewQuestion.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                    }
                }
            } else if (requestCode == PictureConfig.CHOOSE_REQUEST) {
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

                if (showList != null && showList.size() > 0) {

                    if (GetFileType.fileType("" + showList.get(0).getPath()).equals("视频")) {
                        adapter.setSelectMax(1);
                    } else {
                        adapter.setSelectMax(9);
                    }

                } else {
                    //弹出一个dialog选择图片或者视频
                    picOrVedioDialog();
                }
                adapter.setList(showList);
                adapter.notifyDataSetChanged();
                picNum.setText(showList.size() + "/9");
            }
        }

    }


    //跳转至app应用详情界面
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

    //日期选择器
    public void showDatePickerDialog(Activity activity, final TextView tv, Calendar calendar) {
        // Calendar 需要这样来得到
        // Calendar calendar = Calendar.getInstance();
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity,
                // 绑定监听器(How the parent is notified that the date is set.)
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // 此处得到选择的时间，可以进行你想要的操作
                        tv.setText(year + "-" + (monthOfYear + 1)
                                + "-" + dayOfMonth);
                        Deadline = year + "-" + (monthOfYear + 1)
                                + "-" + dayOfMonth;
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.press_record) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AudioRecordManager.getInstance(NewQuestion.this).startRecord();
                    int animationDuration = 60000;
                    circularProgressBar.setProgressWithAnimation(100, animationDuration);
                    handler.post(sound_record);//开始计时
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled(view, motionEvent)) {
                        AudioRecordManager.getInstance(NewQuestion.this).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance(NewQuestion.this).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance(NewQuestion.this).stopRecord();
                    AudioRecordManager.getInstance(NewQuestion.this).destroyRecord();
                    break;
            }

            AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {

                @Override
                public void initTipView() {
//                    ToastUtils.showShort("initTipView");
                    //开始录制

//                    int animationDuration = 60000;
//                    circularProgressBar.setProgressWithAnimation(100, animationDuration);
//                    handler.post(sound_record);//开始计时
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
                    if (audioDialog != null && audioDialog.isShowing()) {
                        audioDialog.dismiss();
                    }

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
                    ToastUtils.showShort("开始录制");
//                    //开始录制
//                    handler.post(sound_record);//开始计时
//                    int animationDuration = 60000;
//                    circularProgressBar.setProgressWithAnimation(100, animationDuration);
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

                        //关闭录音弹框
                        if (audioDialog != null && audioDialog.isShowing()) {
                            audioDialog.dismiss();
                            rl_playAudio.setVisibility(View.VISIBLE);
                            circularProgressBar.setProgress(0);

                            if (duration >= 60) {
                                playAudioTime.setText("01:00");
                            } else {
                                playAudioTime.setText("00:" + duration / 10 + duration % 10);
                            }

                            ll_voice.setVisibility(View.GONE);
                        }
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


    //EventBus 刷新图片视频右侧的文件数量
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(NewQuestionDelEven newQuestionDelEven) {

        if (newQuestionDelEven.getInfo().contains("图片数量")) {//从GridImageAdapter返回
            picNum.setText(newQuestionDelEven.getInfo().split(":")[1] + "/9");
        } else if (newQuestionDelEven.getInfo().contains("附件FileID")) {//从AddDocumentActivity的确认按钮那边返回
            documentsId = newQuestionDelEven.getInfo().split("@#@#@#")[1];//获取fid和fileName的字符串

            if(documentList!=null){
                documentList.clear();
            }

            for (int i = 0; i < newQuestionDelEven.getInfo().split("@#@#@#")[1].split(",").length; i++) {
                String fileIDStr = newQuestionDelEven.getInfo().split("@#@#@#")[1].split(",")[i].split(":")[0];
                String fileName = newQuestionDelEven.getInfo().split("@#@#@#")[1].split(",")[i].split(":")[1];
                DocumentModel documentModel = new DocumentModel(fileIDStr, fileName);
                documentList.add(documentModel);//获取附件的fileID和文件名
            }

            documentAdapter = new DocumentAdapter(MyApplication.getContext(), documentList, projectID, true, "NewQuestion", this);
            recyclerDocument.setAdapter(documentAdapter);

        } else if (newQuestionDelEven.getInfo().contains("删除")) {//从AddDocumentActivity的确认按钮那边返回

            for (int i = documentList.size() - 1; i >= 0; i--) {
                String item = documentList.get(i).getFileString();
                if (newQuestionDelEven.getInfo().split(":")[1].equals(item)) {
                    documentList.remove(documentList.get(i));
                }
            }

            documentAdapter.notifyDataSetChanged();
        } else if (newQuestionDelEven.getInfo().contains("预览")) {//从AddDocumentActivity的确认按钮那边返回

            for (int i = documentList.size() - 1; i >= 0; i--) {
                String item = documentList.get(i).getFileString();
                if (newQuestionDelEven.getInfo().split(":")[1].split("@@@")[0].equals(item)) {
                    //加载加进度条，加班类别请求成功后消失。
                    progressDialog = new ProgressDialog(NewQuestion.this);
                    progressDialog.setMessage("文件加载中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();  //将进度条显示出来
                    downLoadFile(item, newQuestionDelEven.getInfo().split(":")[1].split("@@@")[1]);
                }
            }
        } else if (newQuestionDelEven.getInfo().contains("CategoryActivity类型对象字符串")) {//从ShowStringAdapter点击事件中传过来
            String str = newQuestionDelEven.getInfo().split("@#@#@#")[1];
            Category = str.split(",")[0];
            String Name = str.split(",")[1];
            String CreatedAt = str.split(",")[2];
            int CreatedBy = Integer.parseInt(str.split(",")[3]);
            String UpdatedAt = str.split(",")[4];
            int tUpdatedBy = Integer.parseInt(str.split(",")[5]);
            category.setText(Name);//类型名
        } else if (newQuestionDelEven.getInfo().contains("SystemTypeActivity类型对象字符串")) {//从ShowStringAdapter点击事件中传过来
            String str = newQuestionDelEven.getInfo().split("@#@#@#")[1];
            SystemType = str.split(",")[0];
            SystemTypeName = str.split(",")[1];
            String CreatedAt = str.split(",")[2];
            int CreatedBy = Integer.parseInt(str.split(",")[3]);
            String UpdatedAt = str.split(",")[4];
            int tUpdatedBy = Integer.parseInt(str.split(",")[5]);
            systemTyp.setText(SystemTypeName);//类型名
        } else if (newQuestionDelEven.getInfo().contains("DiscussionGroupActivity类型对象字符串")) {//从ShowStringAdapter点击事件中传过来
            String str = newQuestionDelEven.getInfo().split("@#@#@#")[1];
            GroupId = str.split(",")[0];//讨论组ID
            String Name = str.split(",")[1];
            String User = str.split(",")[2];
            String CreatedAt = str.split(",")[3];
            int CreatedBy = Integer.parseInt(str.split(",")[4]);
            String UpdatedAt = str.split(",")[5];
            int tUpdatedBy = Integer.parseInt(str.split(",")[6]);
            discussionGroup.setText(Name);//讨论组名
        } else if (newQuestionDelEven.getInfo().contains("AtActivity")) {//从AtActivity点击事件中传过来
            String str = newQuestionDelEven.getInfo();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < str.split("@#@#@#")[1].split(",").length; i++) {
                //获取名字列表
                sb.append(str.split("@#@#@#")[1].split(",")[i].split(":")[0]);
                sb.append("、");

                //获取用于上传的at字段
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Name", str.split("@#@#@#")[1].split(",")[i].split(":")[0]);
                map.put("ID", str.split("@#@#@#")[1].split(",")[i].split(":")[1]);
                atlist.add(JSON.toJSONString(map));
            }
            atNameStr = sb.substring(0, sb.length() - 1).toString();

            atNameList.setText(atNameStr);
        }
    }

    //文件下载方法
    private void downLoadFile(final String fileStr, final String fileName) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/EngineeringData/" + fileStr + "/DownLoad")
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
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(savePath + "/" + fileName);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(NewQuestion.this, getApplicationContext().getPackageName() + ".FileProvider", file);
                                    intent.setDataAndType(contentUri, "video/*");
                                } else {
                                    uri = Uri.fromFile(file);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(uri, "video/*");
                                }
                                startActivity(intent);

                            } else if (GetFileType.fileType(fileName).equals("音乐")) {

                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(savePath + "/" + fileName);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(NewQuestion.this, getApplicationContext().getPackageName() + ".FileProvider", file);
                                    intent.setDataAndType(contentUri, "audio/*");
                                } else {
                                    uri = Uri.fromFile(file);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(uri, "audio/*");
                                }
                                startActivity(intent);
                            } else {
                                //打开和预览该文件
                                Intent intent = new Intent(NewQuestion.this, PreviewFileActivity.class);
                                intent.putExtra("fileName", fileName);
                                intent.putExtra("fileID", fileStr);//会出错
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
