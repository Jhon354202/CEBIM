/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chenxi.cebim.activity.zxing.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.zxing.camera.CameraManager;
import com.chenxi.cebim.activity.zxing.decode.DecodeThread;
import com.chenxi.cebim.activity.zxing.utils.BeepManager;
import com.chenxi.cebim.activity.zxing.utils.CaptureActivityHandler;
import com.chenxi.cebim.activity.zxing.utils.Constant;
import com.chenxi.cebim.activity.zxing.utils.InactivityTimer;
import com.google.zxing.Result;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 此活动打开相机并对背景进行实际扫描
 * 线程。它会绘制一个取景器来帮助用户正确放置条形码，
 * 在图像处理过程中显示反馈，然后覆盖扫描成功时的结果。
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    @BindView(R.id.once)
    TextView once;
    @BindView(R.id.more)
    TextView more;
    @BindView(R.id.empty)
    ImageView empty;
    @BindView(R.id.backspace)
    ImageView backspace;
    @BindView(R.id.confirm)
    TextView confirm;
    @BindView(R.id.scanmore)
    RelativeLayout scanmore;
    @BindView(R.id.scan_num)
    TextView scanNum;
    @BindView(R.id.btn_album)
    ImageView btnAlbum;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private SurfaceView scanPreview = null;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private boolean isHasSurface = false;
    ObjectAnimator objectAnimator, objectAnimator2;//属性动画实现左移右移
    boolean scantype = true;
    List<String> scans = new ArrayList<>();
    Handler mHandler;
    Runnable runnable;
    public final int RESULT_CODE = 10002;
    public final int RESULT_CODE2 = 10003;
    ImageView btn_back;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_capture);
        ButterKnife.bind(this);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
        scanmore.setVisibility(View.GONE);
        scanNum.setVisibility(View.GONE);
        confirm.setEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(scanPreview.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            scanPreview.getHolder().addCallback(this);
        }

        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   The extras
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        String resultString = rawResult.getText();
        if (TextUtils.isEmpty(resultString)) {
        } else {
            if (scantype) {
                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.INTENT_EXTRA_KEY_QR_SCAN, resultString);
                System.out.println("sssssssssssssssss scan 0 = " + resultString);
                // 不能使用Intent传递大于40kb的bitmap，可以使用一个单例对象存储这个bitmap
//            bundle.putParcelable("bitmap", barcode);
//            Logger.d("saomiao",resultString);
                resultIntent.putExtras(bundle);
                CaptureActivity.this.setResult(RESULT_CODE, resultIntent);
                CaptureActivity.this.finish();
            } else {
                scans.add(rawResult.getText());
                confirm.setEnabled(true);
                scanNum.setText("已扫描：" + scans.size());
                mHandler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        SurfaceView surfaceView = findViewById(R.id.capture_preview);
                        SurfaceHolder surfaceHolder = surfaceView.getHolder();
                        initCamera(surfaceHolder);
                        if (handler != null) {
                            handler.restartPreviewAndDecode();
                        }
                    }
                };
                mHandler.postDelayed(runnable, 3000);
            }
        }
    }


    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }

            initCrop();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("相机打开出错，请稍后重试");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @OnClick({R.id.once, R.id.more, R.id.empty, R.id.backspace, R.id.confirm, R.id.btn_album})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.once:
                objectAnimator = ObjectAnimator.ofFloat(once, "translationX", -(once.getWidth() / 2 + more.getWidth()), 0f);
                objectAnimator2 = ObjectAnimator.ofFloat(more, "translationX", -(once.getWidth() / 2 + more.getWidth()), 0f);
                objectAnimator.setDuration(100);
                objectAnimator2.setDuration(100);
                objectAnimator2.start();
                objectAnimator.start();
                once.setEnabled(false);
                more.setEnabled(true);
                once.setTextColor(Color.parseColor("#3498db"));
                more.setTextColor(Color.WHITE);
                scanmore.setVisibility(View.GONE);
                scanNum.setVisibility(View.GONE);
                scantype = true;
                break;
            case R.id.more:
                objectAnimator = ObjectAnimator.ofFloat(once, "translationX", 0f, -(once.getWidth() / 2 + more.getWidth()));
                objectAnimator2 = ObjectAnimator.ofFloat(more, "translationX", 0f, -(once.getWidth() / 2 + more.getWidth()));
                objectAnimator.setDuration(100);
                objectAnimator2.setDuration(100);
                objectAnimator2.start();
                objectAnimator.start();
                more.setEnabled(false);
                once.setEnabled(true);
                more.setTextColor(Color.parseColor("#3498db"));
                once.setTextColor(Color.WHITE);
                scanmore.setVisibility(View.VISIBLE);
                scanNum.setVisibility(View.VISIBLE);
                scantype = false;
                break;
            case R.id.empty:
                scans.clear();
                confirm.setEnabled(false);
                scanNum.setText("已扫描：0");
                break;
            case R.id.backspace:
                scans.remove(scans.size() - 1);
                scanNum.setText("已扫描：" + scans.size());
                if (scans.size() == 0) {
                    backspace.setEnabled(false);
                    confirm.setEnabled(false);
                }
                break;
            case R.id.confirm:
                Intent intent = new Intent();
                intent.putExtra("scanresult", (Serializable) scans);
                intent.putExtra("scantype", scantype);
                CaptureActivity.this.setResult(RESULT_CODE2, intent);
                mHandler.removeCallbacks(runnable);
                CaptureActivity.this.finish();
                break;
            case R.id.btn_album:
                if (cameraManager.flashContralHander()) {
                    btnAlbum.setImageResource(R.drawable.light_on);
                } else {
                    btnAlbum.setImageResource(R.drawable.light_off);
                }
                break;
        }
    }

}