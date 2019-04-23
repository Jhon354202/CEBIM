package com.chenxi.cebim.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.ActivityCollector;
import com.chenxi.cebim.utils.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 闪屏页面
 */
public class SplashActivity extends BaseActivity {
    protected static final int CODE_UPDATE_DIALOG = 0;//非强制更新
    protected static final int CODE_FORCE_UPDATE_DIALOG = 1;//强制更新
    protected static final int CODE_URL_ERROR = 2;
    protected static final int CODE_NET_ERROR = 3;
    protected static final int CODE_JSON_ERROR = 4;
    protected static final int CODE_ENTER_HOME = 5;// 进入主页面

    TextView tvProgress;

    // 服务器返回的信息
    private String mVersionName;// 版本名
    private int mVersionCode;// 版本号
    private String mIsForceUpdating;//是否强制更新
    private String mDesc;// 版本描述
    private String mDownloadUrl;// 下载地址
    long startTime, endTime;
    ACache mCache;//缓存
    ProgressDialog progressDialog;

    private boolean granted = false;
    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    AlertDialog dialog = null;

    int isTheFirstTimeShow = 0;


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_UPDATE_DIALOG:
                    showUpdateDailog();
                    break;
                case CODE_FORCE_UPDATE_DIALOG:
                    showForceUpdateDailog();
                    break;
                case CODE_URL_ERROR:
//                    Toast.makeText(SplashActivity.this, "url错误", Toast.LENGTH_SHORT)
//                            .show();
                    enterHome();
                    break;
                case CODE_NET_ERROR:
                    enterHome();
                    break;
                case CODE_JSON_ERROR:
//                    Toast.makeText(SplashActivity.this, "数据解析错误",
//                            Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_ENTER_HOME:
                    enterHome();
                    break;

                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

//        tvProgress = (TextView) findViewById(R.id.tv_progress);

        mCache = ACache.get(this);
        checkVerson();//检查是否有最新版本
    }

    /**
     * 获取权限
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                //具有权限
                granted = true;
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, GET_PERMISSION_REQUEST);
                granted = false;
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //电话权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }

                //录音权限
                int audioPermissionResult = grantResults[3];
                boolean audioPermissionGranted = audioPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }

                if (size == 0) {
                    granted = true;
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //判断是否点了不再提示，点了则跳出弹框
                    Boolean isCheckNoPromptAgain = false;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                            isCheckNoPromptAgain = true;
                            break;
                        }
                    }

                    if (isCheckNoPromptAgain) {
                        manualSetting();
                    } else {
                        showAlerDialog();
                    }

                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void manualSetting() {
        if (dialog == null || (dialog != null && !dialog.isShowing())) {
            new AlertDialog.Builder(this)
                    .setTitle("权限设置")
                    .setMessage("您已选择了不再提示权限设置按钮，或者系统默认不再提示权限设置," +
                            "请到【设置—权限】手动授权")
                    .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            //引导用户至设置页手动授权
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setCancelable(false).show();

        }
    }

    private void showAlerDialog() {

        dialog = new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage("CEBIM需要获取以下权限才能正常使用\n" + "A:存储空间(读取或下载文件和图片等)\n" +
                        "B:电话(仅用于获取设备信息，提供更个性化的服务)")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        enterHome();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .create();
        dialog.show();

        //以下用于修改弹窗的文字样式
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(ContextCompat.getColor(SplashActivity.this, R.color.gray_text));
            mMessageView.setTextSize(13);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void checkVerson() {

        final long startTime = System.currentTimeMillis();
        new Thread() {

            @Override
            public void run() {
                Message msg = Message.obtain();
                HttpURLConnection conn = null;
                try {
                    // 本机地址用localhost, 但是如果用模拟器加载本机的地址时,可以用ip(10.0.2.2)来替换
//                    URL url = new URL("http://192.168.0.108:1613/Content/android/cailiaoxunjia.json");
                    URL url = new URL("");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");// 设置请求方法
                    conn.setConnectTimeout(5000);// 设置连接超时
                    conn.setReadTimeout(5000);// 设置响应超时, 连接上了,但服务器迟迟不给响应
                    conn.connect();// 连接服务器

                    int responseCode = conn.getResponseCode();// 获取响应码
                    if (responseCode == 200) {
                        InputStream inputStream = conn.getInputStream();
                        String result = StreamUtils.readFromStream(inputStream);

                        // 解析json
                        JSONObject jo = new JSONObject(result);
                        mVersionName = jo.getString("versionName");
                        mVersionCode = jo.getInt("versionCode");
                        mIsForceUpdating = jo.getString("isForceUpdating");
                        mDesc = jo.getString("description");
                        mDownloadUrl = jo.getString("downloadUrl");

                        // 判断是否有更新,当1、待下载的版本号大于当前版本号；2、在WiFi环境下；3、没有忽略升级该版本的情况下会跳出弹框
                        if ((mVersionCode > getVersionCode()) && (NetworkUtils.getWifiEnabled()) && (!("" + mCache.getAsString("versionCode")).equals("" + mVersionCode))) {
                            // 服务器的VersionCode大于本地的VersionCode
                            // 说明有更新, 弹出升级对话框
                            if (mIsForceUpdating.equals("是")) {//强制更新的情况
                                msg.what = CODE_FORCE_UPDATE_DIALOG;
                            } else {//非强制更新的情况
                                msg.what = CODE_UPDATE_DIALOG;
                            }

                        } else {
                            // 没有版本更新
                            msg.what = CODE_ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    // url错误的异常
                    msg.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    // 网络错误异常
                    msg.what = CODE_NET_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    // json解析失败
                    e.printStackTrace();
                } finally {

                    long endTime = System.currentTimeMillis();
                    long timeUsed = endTime - startTime;// 访问网络花费的时间

                    if (timeUsed < 2000) {
                        // 强制休眠一段时间,保证闪屏页展示2秒钟
                        try {
                            Thread.sleep(2000 - timeUsed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mHandler.sendMessage(msg);
                    if (conn != null) {
                        conn.disconnect();// 关闭网络连接
                    }
                }
            }
        }.start();

    }

    /**
     * 获取本地app的版本号
     *
     * @return
     */
    private int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);// 获取包的信息

            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // 没有找到包名的时候会走此异常
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 非强制升级对话框
     */
    protected void showUpdateDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本:" + mVersionName);
        builder.setMessage(mDesc);
        builder.setCancelable(false);//不让用户取消对话框

        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                download();
            }
        });

        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });

        builder.setNeutralButton("忽略该版本", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCache.put("versionCode", "" + mVersionCode);
                enterHome();
            }
        });

        // 设置取消的监听, 用户点击返回键时会触发
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();
            }

        });

        builder.show();
    }

    /**
     * 强制升级对话框
     */
    protected void showForceUpdateDailog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本:" + mVersionName);
        builder.setMessage(mDesc);
        builder.setCancelable(false);//不让用户取消对话框
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                download();
            }

        });

        builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //不更新就退出app
                ActivityCollector.finishAll();

            }
        });

        // 设置取消的监听, 用户点击返回键时会触发
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();
            }

        });
        builder.show();
    }


    /*
     * 进入主界面
     */
    private void enterHome() {
        getPermissions();
    }

    /**
     * 下载apk文件
     */
    protected void download() {

        progressDialog = new ProgressDialog(SplashActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("安装包下载");

        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            String url = "http://127.0.0.1/server/abc.apk";
            RequestParams params = new RequestParams(url);
            //自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
            params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/myapp/");
            //自动为文件命名
            params.setAutoRename(true);
            x.http().post(params, new Callback.ProgressCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //判断是否是AndroidN以及更高的版本
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(SplashActivity.this, "com.chenxi.materialinquirysystem.fileProvider", result.getAbsoluteFile());
                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    } else {
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(Uri.fromFile(result.getAbsoluteFile()),
                                "application/vnd.android.package-archive");
                    }

                    startActivityForResult(intent, 0);// 如果用户取消安装的话,会返回结果,回调方法onActivityResult
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                }

                @Override
                public void onCancelled(CancelledException cex) {
                }

                @Override
                public void onFinished() {
                }

                //网络请求之前回调
                @Override
                public void onWaiting() {
                }

                //网络请求开始的时候回调
                @Override
                public void onStarted() {
                }

                //下载的时候不断回调的方法
                @Override
                public void onLoading(long total, long current, boolean isDownloading) {
                    //当前进度和文件总大小
                    Log.i("JAVA", "current：" + current + "，total：" + total);
                }
            });

//            OkGo.<File>get(mDownloadUrl).tag(this).execute(new FileCallback("" + Environment.getExternalStorageDirectory(), "app-debug.apk") {
//                @Override
//                public void onSuccess(Response<File> response) {
//
//                    progressDialog.dismiss();
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    //判断是否是AndroidN以及更高的版本
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        Uri contentUri = FileProvider.getUriForFile(SplashActivity.this, "com.chenxi.materialinquirysystem.fileProvider", response.body().getAbsoluteFile());
//                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
//                    } else {
//                        intent.addCategory(Intent.CATEGORY_DEFAULT);
//                        intent.setDataAndType(Uri.fromFile(response.body().getAbsoluteFile()),
//                                "application/vnd.android.package-archive");
//                    }
//
//                    startActivityForResult(intent, 0);// 如果用户取消安装的话,会返回结果,回调方法onActivityResult
//
//                }
//
//                @Override
//                public void downloadProgress(Progress progress) {
//
//                    super.downloadProgress(progress);
//
//                    //这行代码用于double类型数据保留小数位两位数
//                    DecimalFormat df = new DecimalFormat("######0.00");
//
//                    progressDialog.setTitle("安装包下载" + "(" + df.format(progress.totalSize / (1024.0 * 1024.0)) + "M" + ")");
//                    progressDialog.setProgress((int) (progress.fraction * 100));
//
//                }
//
//                @Override
//                public void onError(Response<File> response) {
//                    super.onError(response);
//                }
//            });

        } else {
            Toast.makeText(SplashActivity.this, "没有找到sdcard!",
                    Toast.LENGTH_SHORT).show();
            enterHome();
        }
    }

    // 如果用户取消安装的话,回调此方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //强制更新时在同时满足下面三个条件的情况下，会直接退出程序。
        if (mIsForceUpdating.equals("是") && resultCode == 0 && (mVersionCode != getVersionCode())) {
            ToastUtils.showShort("当前版本有漏洞，请更新到最新版本");
            ActivityCollector.finishAll();
        } else {
            enterHome();
        }

    }

    //屏蔽返回键，使app不在该界面退出。
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

