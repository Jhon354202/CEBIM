package com.chenxi.cebim.activity.engineeringNews;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.BaseActivity;
import com.chenxi.cebim.activity.PlayVedioActivity;
import com.chenxi.cebim.activity.coordination.SlidePreviewPicActivity;
import com.chenxi.cebim.adapter.ShowPicGridViewAdapter;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.ChooseModelEntity;
import com.chenxi.cebim.entity.EngineeringNewsRefreshEvenModel;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;
import com.chenxi.cebim.utils.PermissionUtil;
import com.chenxi.cebim.utils.UpLoadFileUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.mabeijianxi.smallvideorecord2.LocalMediaCompress;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.model.AutoVBRMode;
import com.mabeijianxi.smallvideorecord2.model.LocalMediaConfig;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;
import com.mabeijianxi.smallvideorecord2.model.OnlyCompressOverBean;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class ProjectPublishActivity extends BaseActivity {

    private TextView publish, showModelName;
    EditText content, position;
    private ImageView back;
    //    private RecyclerView recyclerView;
    private RelativeLayout chooseModel;

    private ProgressDialog progressDialog;

    //    private GridImageAdapter adapter;
    private ShowPicGridViewAdapter adapter;
    private List<LocalMedia> selectList = new ArrayList<>(); //当前选择的所有图片
    private int maxSelectNum = 9;//允许选择图片最大数
    private int themeId;
    List<ChooseModelEntity> chooseModelList=new ArrayList<>();

    private List<String> picAnnexMd5CodeList = new ArrayList<>();//图片MD5列表
    private List<String> vedioAnnexMd5CodeList = new ArrayList<>();//视频MD5列表
    private List<String> audioAnnexMd5CodeList = new ArrayList<>();//音频MD5列表
    private List<String> modelAnnexMd5CodeList = new ArrayList<>();//模型MD5列表

    String savePath;//图片、视频、录音存储路径

    private List<String> tempPathList = new ArrayList<>();//用于装载临时上传文件地址
    private List<String> pathList = new ArrayList<>();//用于装载上传文件地址
    private List<String> vedioPathList = new ArrayList<>();//用于装载视频地址

    Dialog picOrVideoDialog;

    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_publish);
        PermissionUtil.addPermission(ProjectPublishActivity.this, Manifest.permission.CAMERA, "需要手机拍照权限");
        savePath = DiskCacheDirUtil.getDiskCacheDir(ProjectPublishActivity.this);//图片、视频、录音存储路径
        initWidget();
    }

    private void initWidget() {
        content = findViewById(R.id.et_description);
        position = findViewById(R.id.et_position);
        //图片选择器
//        FullyGridLayoutManager manager = new FullyGridLayoutManager(ProjectPublishActivity.this, 4, GridLayoutManager.VERTICAL, false);
//        recyclerView = (RecyclerView) findViewById(R.id.project_public_recyclerView);
//        recyclerView.setLayoutManager(manager);
//        adapter = new GridImageAdapter(ProjectPublishActivity.this, onAddPicClickListener, true);
////        adapter.setList(selectList);
//        adapter.setSelectMax(maxSelectNum);
//        recyclerView.setAdapter(adapter);
//        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position, View v) {
//                if (selectList.size() > 0) {
//                    LocalMedia media = selectList.get(position);
//                    String pictureType = media.getPictureType();
//                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
//                    switch (mediaType) {
//                        case 1:
//                            // 预览图片 可自定长按保存路径
//                            //PictureSelector.create(MainActivity.this).themeStyle(themeId).externalPicturePreview(position, "/custom_file", selectList);
//                            PictureSelector.create(ProjectPublishActivity.this).themeStyle(themeId).openExternalPreview(position, selectList);
//                            break;
//                        case 2:
//                            // 预览视频
//                            PictureSelector.create(ProjectPublishActivity.this).externalPictureVideo(media.getPath());
//                            break;
//                        case 3:
//                            // 预览音频
//                            PictureSelector.create(ProjectPublishActivity.this).externalPictureAudio(media.getPath());
//                            break;
//                    }
//                }
//            }
//        });

        mGridView = (GridView) findViewById(R.id.gv_showfile);
        adapter = new ShowPicGridViewAdapter(ProjectPublishActivity.this, tempPathList, pListener);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position < tempPathList.size()) {

                    if (GetFileType.fileType(tempPathList.get(position)).equals("视频")) {
                        //如果本地已有视频，则直接打开，否则下载到本地再打开
                        File file = new File(tempPathList.get(position));
                        if (file.exists()) {
                            Intent intent=new Intent(ProjectPublishActivity.this, PlayVedioActivity.class);
                            intent.putExtra("vedioUrl", tempPathList.get(position));
                            intent.putExtra("vedioname",UpLoadFileUtil.getFileName(tempPathList.get(position)));
                            startActivity(intent);
                        }

                    } else if (GetFileType.fileType(tempPathList.get(position)).equals("图片")) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < tempPathList.size(); i++) {
                            sb.append(tempPathList.get(i));
                            sb.append("@@@@@@");
                            sb.append("本地图片");
                            if (i < tempPathList.size() - 1) {
                                sb.append("@#@#@#");
                            }
                        }

                        Intent intent = new Intent(ProjectPublishActivity.this, SlidePreviewPicActivity.class);
                        intent.putExtra("position", position);
                        intent.putExtra("preViewPic", sb.toString());
                        startActivity(intent);
                    }

                } else if (position == tempPathList.size() && position < 9) {
                    picOrVedioDialog();
                }

            }
        });


        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(ProjectPublishActivity.this);
                } else {
                    Toast.makeText(ProjectPublishActivity.this,
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

        publish = (TextView) findViewById(R.id.toolbar_right_tv);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pathList.size() == 0 && position.getText().toString().equals("") && content.getText().toString().equals("")) {
                    ToastUtils.showShort("动态内容不能为空");
                } else {

                    //加载加进度条，加班类别请求成功后消失。
                    progressDialog = new ProgressDialog(ProjectPublishActivity.this);
                    progressDialog.setMessage("创建中...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();  //将进度条显示出来

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //根据地址判断是不是本地视频，如果是本地视频，则进行压缩，如果是拍摄获取的视频，不进行压缩
                            List<String> tpPathList = new ArrayList<>();//临时变量
                            tpPathList.addAll(pathList);
                            pathList.clear();

                            int num = 0;//已装入list的地址数量

                            for (int i = 0; i < tpPathList.size(); i++) {
                                if (GetFileType.fileType(tpPathList.get(i)).equals("视频") && (!tpPathList.get(i).contains("mabeijianxi"))) {

                                    LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
                                    final LocalMediaConfig config = buidler
                                            .setVideoPath(tpPathList.get(i))
                                            .captureThumbnailsTime(1)
                                            .doH264Compress(new AutoVBRMode())
                                            .setFramerate(15)
                                            .setScale(1.0f)
                                            .build();
                                    OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();

                                    if (onlyCompressOverBean.isSucceed()) {
                                        num++;
                                        pathList.add(onlyCompressOverBean.getVideoPath());
                                    }
                                } else {
                                    num++;
                                    pathList.add(tpPathList.get(i));
                                }
                            }

                            int finalNum = num;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (finalNum == tpPathList.size()) {
                                        getArr(pathList);
                                    }
                                }
                            });
                        }
                    }).start();

                }

            }

        });

        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        chooseModel = findViewById(R.id.rl_choose_model);
        chooseModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectPublishActivity.this, GetModelNameActivity.class);
                startActivityForResult(intent, 555);
            }
        });

        showModelName = findViewById(R.id.tv_showmodel_name);
    }

    //文件下载方法
    private void downLoadFile(String fileName, String id) {

        //加载加进度条，加班类别请求成功后消失。
        progressDialog = new ProgressDialog(ProjectPublishActivity.this);
        progressDialog.setMessage("文件下载中...");
        progressDialog.setCancelable(true);
        progressDialog.show();  //将进度条显示出来

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/" + id)
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
                    File file = new File(AppConst.savePath + "/" + fileName);
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

                            //发送网址到播放界面进行播放
                            Intent intent=new Intent(ProjectPublishActivity.this, PlayVedioActivity.class);
                            intent.putExtra("vedioUrl",AppConst.savePath + "/" + fileName);
                            intent.putExtra("vedioname",fileName);
                            startActivity(intent);

                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("ProjectPublishActivity文件下载出错信息", e.getMessage());
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

    //GrideView中图片删除按钮点击事件
    private ShowPicGridViewAdapter.MyClickListener pListener = new ShowPicGridViewAdapter.MyClickListener() {
        @Override
        public void myOnClick(final int position, View v) {
            //获得组件
            //在GridView和ListView中，getChildAt ( int position ) 方法中position指的是当前可见区域的第几个元素。
            //如果你要获得GridView或ListView的第n个View，那么position就是n减去第一个可见View的位置
            View view = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
            //获得item中的对应的Imageview，用来拍照返回的时候显示照片略缩图
            delFile(position);
        }
    };

    //删除文件
    public void delFile(final int fileOrder) {

        //删除图片列表中被选中的需要删除的元素
        List<String> tempList = new ArrayList<>();
        tempList.addAll(tempPathList);
        tempPathList.clear();
        tempList.remove(fileOrder);
        tempPathList.addAll(tempList);
        adapter.notifyDataSetChanged();

        pathList.remove(fileOrder);//删除待上传中被选中要删除的元素

        //删除相册元素
        LocalMedia needToDelLocalMedia = null;
        if (fileOrder < selectList.size()) {
            needToDelLocalMedia = selectList.get(fileOrder);

            for (int i = selectList.size() - 1; i >= 0; i--) {
                LocalMedia needToDel = selectList.get(i);
                if (needToDel.equals(needToDelLocalMedia)) {
                    selectList.remove(needToDelLocalMedia);
                }
            }
        }

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
    }

    //从网络获取数据
    public void getDataFromInterNet(String picAnnexMd5CodeString, String vedioAnnexMd5CodeString, String audioAnnexMd5CodeString) {
        String contents = content.getText().toString();
        RequestBody requestBody = new FormBody.Builder()
                .add("Contens", contents)//内容
                .add("Picture", "" + picAnnexMd5CodeString)//图片
                .add("Video", "" + vedioAnnexMd5CodeString)//视频
                .add("ModelID", modelAnnexMd5CodeList.toString())//模型ID
                .add("Location", position.getText().toString())//位置
                .build();

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic")
                .post(requestBody)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("工程动态发布失败");
                progressDialog.dismiss();  //将进度条隐藏
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    ToastUtils.showShort("工程动态发布成功");
                    progressDialog.dismiss();  //将进度条隐藏
                    EventBus.getDefault().post(new EngineeringNewsRefreshEvenModel("刷新"));//刷新工程动态列表
                    finish();
                } else {
                    ToastUtils.showShort("工程动态发布失败");
                    progressDialog.dismiss();  //将进度条隐藏
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:

                    if (tempPathList != null) {
                        tempPathList.clear();
                    }

                    if (pathList != null) {
                        pathList.clear();
                    }

                    selectList = PictureSelector.obtainMultipleResult(data);

                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (LocalMedia media : selectList) {
                        if (media.isCompressed()) {
                            tempPathList.add(media.getCompressPath());//获取图片地址，用于上传。
                        } else {
                            //这边判断是否是本地视频，如果是，则进行压缩
                            tempPathList.add(media.getPath());//获取图片地址，用于上传。
                        }
                    }

                    if (vedioPathList != null) {
                        tempPathList.addAll(vedioPathList);
                    }

                    pathList.addAll(tempPathList);
                    adapter.notifyDataSetChanged();

                    break;

                case 555:

                    chooseModelList = (List<ChooseModelEntity>)data.getSerializableExtra("model_info");

                    StringBuffer sb=new StringBuffer();
                    for(int i=0;i<chooseModelList.size();i++){

                        if(chooseModelList.get(i).isIschoosed()){
                            sb.append(chooseModelList.get(i).getModelName());
                            if(i<chooseModelList.size()-1){
                                sb.append(",");
                            }

                            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                            map.put("Name", "" + chooseModelList.get(i).getModelName());
                            map.put("ID", "" + chooseModelList.get(i).getModelID());
                            modelAnnexMd5CodeList.add(JSON.toJSONString(map));
                        }

                    }

                    showModelName.setText(sb.toString());
                    break;

                case 214:
                    if (pathList != null) {
                        pathList.clear();
                    }

                    String outputDirectory = data.getStringExtra(MediaRecorderActivity.OUTPUT_DIRECTORY);//文件夹
                    String outputTempTranscodingVideoPath = data.getStringExtra(MediaRecorderActivity.VIDEO_URI);//视频
                    String outputVideoThumbPath = data.getStringExtra(MediaRecorderActivity.VIDEO_SCREENSHOT);//首图

                    vedioPathList.add(outputTempTranscodingVideoPath);

                    tempPathList.add(outputTempTranscodingVideoPath);//获取视频地址，用于上传。

                    pathList.addAll(tempPathList);
                    adapter.notifyDataSetChanged();

                    break;
            }
        }
    }

    //图片和视频选择
    private void picOrVedioDialog() {
        picOrVideoDialog = new Dialog(ProjectPublishActivity.this, R.style.ActionSheetDialogStyle);
        View inflate = LayoutInflater.from(ProjectPublishActivity.this).inflate(R.layout.pic_or_vedio_dialog, null);
        Button getPhoto = (Button) inflate.findViewById(R.id.bt_take_photo);
        Button getVedio = (Button) inflate.findViewById(R.id.bt_take_vedio);
        Button album = (Button) inflate.findViewById(R.id.bt_album);
        Button cancel = (Button) inflate.findViewById(R.id.btn_pic_or_vedio_cancel);
        getPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PictureSelector.create(ProjectPublishActivity.this)
                        .openCamera(PictureMimeType.ofAll())
                        .enableCrop(true)
                        .compress(true)
                        .showCropFrame(true)
                        .cropCompressQuality(60)
                        .selectionMedia(selectList)// 是否传入已选图片
                        .forResult(PictureConfig.CHOOSE_REQUEST);

                picOrVideoDialog.dismiss();
            }
        });

        getVedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DisplayMetrics dm = getResources().getDisplayMetrics();
                int heigth = dm.heightPixels;
                int width = dm.widthPixels;

                // 录制
                MediaRecorderConfig config = new MediaRecorderConfig.Buidler()

                        .fullScreen(true)
                        .smallVideoWidth(360)
                        .smallVideoHeight(480)
//                        .smallVideoWidth(width)
//                        .smallVideoHeight(heigth)
                        .recordTimeMax(60000)
                        .recordTimeMin(1500)
                        .maxFrameRate(30)
                        .videoBitrate(600000)
                        .captureThumbnailsTime(1)
                        .build();
                MediaRecorderActivity.goSmallVideoRecorder(ProjectPublishActivity.this, ProjectPublishActivity.class.getName(), config);
                picOrVideoDialog.dismiss();
            }
        });

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 进入相册 以下是例子：用不到的api可以不写
                PictureSelector.create(ProjectPublishActivity.this)
                        .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .maxSelectNum(9 - vedioPathList.size())// 最大图片选择数量 int
                        .minSelectNum(1)// 最小选择数量 int
                        .imageSpanCount(3)// 每行显示个数 int
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(true)// 是否可预览图片 true or false
                        .previewVideo(true)// 是否可预览视频 true or false
                        .enablePreviewAudio(false) // 是否可播放音频 true or false
                        .isCamera(false)// 是否显示拍照按钮 true or false
                        .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(1f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath(savePath + "VEDIO")// 自定义拍照保存路径,可不填
                        .enableCrop(false)// 是否裁剪 true or false
                        .compress(true)// 是否压缩 true or false
                        .cropCompressQuality(60)//压缩质量
                        .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .synOrAsy(true)//同步true或异步false 压缩 默认同步
                        .videoQuality(1)// 视频录制质量 0 or 1 int
                        .videoMaxSecond(60)// 显示多少秒以内的视频or音频也可适用 int
                        .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int
                        .selectionMedia(selectList)// 是否传入已选图片
//                        .recordVideoSecond(600)//视频秒数录制 默认60s int
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                picOrVideoDialog.dismiss();


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picOrVideoDialog.dismiss();
            }
        });

        picOrVideoDialog.setContentView(inflate);
        Window dialogWindow = picOrVideoDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.y = 20;
        dialogWindow.setAttributes(lp);
        picOrVideoDialog.show();
    }

}
