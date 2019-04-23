package com.chenxi.cebim.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chenxi.cebim.R;
import com.chenxi.cebim.utils.HttpUtil;
import com.lzy.ninegrid.NineGridView;
import com.mabeijianxi.smallvideorecord2.DeviceUtils;
import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.litepal.LitePal;
import org.xutils.x;

import java.io.File;

import okhttp3.OkHttpClient;

public class MyApplication extends Application {

    private static Context context;
    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        //LitePal初始化
        LitePal.initialize(this);
        //xUtil初始化
        x.Ext.init(this);
        ZXingLibrary.initDisplayOpinion(this);
        //获取全局context
        context = getApplicationContext();


        //获取全局okHttpClient
        okHttpClient= HttpUtil.getInstance();
        initTbs();

        NineGridView.setImageLoader(new GlideImageLoader());

        initSmallVideo();

    }

    /** Picasso 加载 */
    private class GlideImageLoader implements NineGridView.ImageLoader {

        @Override
        public void onDisplayImage(Context context, ImageView imageView, String url) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_default_image)//图片加载出来前，显示的图片
                    .error(R.drawable.ic_default_image);//图片加载失败后，显示的图片

            Glide.with(context).load(url)//
                    .apply(options)
                    .into(imageView);
        }

        @Override
        public Bitmap getCacheImage(String url) {
            return null;
        }
    }

    public static Context getContext() {
        return context;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private void initTbs() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                Log.d("MyApplication", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {

            }

            @Override
            public void onInstallFinish(int i) {

            }

            @Override
            public void onDownloadProgress(int i) {

            }
        });

        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    public static void initSmallVideo() {
        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/mabeijianxi/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/mabeijianxi/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/mabeijianxi/");
        }
        // 初始化拍摄，遇到问题可选择开启此标记，以方便生成日志
        JianXiCamera.initialize(false,null);
    }
}
