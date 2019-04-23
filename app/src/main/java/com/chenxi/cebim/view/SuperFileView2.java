package com.chenxi.cebim.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.LogUtils;
import com.chenxi.cebim.R;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

public class SuperFileView2 extends FrameLayout implements TbsReaderView.ReaderCallback {

    private static String TAG = "SuperFileView";
    private TbsReaderView mTbsReaderView;
    private int saveTime = -1;
    private Context context;

    public SuperFileView2(Context context) {
        this(context, null, 0);
    }

    public SuperFileView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuperFileView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTbsReaderView = new TbsReaderView(context, this);
        this.addView(mTbsReaderView, new LinearLayout.LayoutParams(-1, -1));
        this.context = context;
    }


    private OnGetFilePathListener mOnGetFilePathListener;


    public void setOnGetFilePathListener(OnGetFilePathListener mOnGetFilePathListener) {
        this.mOnGetFilePathListener = mOnGetFilePathListener;
    }


    private TbsReaderView getTbsReaderView(Context context) {
        return new TbsReaderView(context, this);
    }

    public void displayFile(File mFile) {

        if (mFile != null && !TextUtils.isEmpty(mFile.toString())) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            String bsReaderTemp = "/storage/emulated/0/TbsReaderTemp";
            File bsReaderTempFile = new File(bsReaderTemp);

            if (!bsReaderTempFile.exists()) {
                LogUtils.d(TAG, "准备创建/storage/emulated/0/TbsReaderTemp！！");
                boolean mkdir = bsReaderTempFile.mkdir();
                if (!mkdir) {
                    LogUtils.e(TAG, "创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
                }
            }

            //加载文件
            Bundle localBundle = new Bundle();
            LogUtils.d(TAG, mFile.toString());
            localBundle.putString("filePath", mFile.toString());
            String str = mFile.toString();
            localBundle.putString("tempPath", Environment.getExternalStorageDirectory() + "/" + "TbsReaderTemp");

            if (this.mTbsReaderView == null)
                this.mTbsReaderView = getTbsReaderView(context);
            boolean bool = this.mTbsReaderView.preOpen(getFileType(mFile.toString()), false);
            if (bool) {
                this.mTbsReaderView.openFile(localBundle);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.activity_preview_file, null);
                com.tencent.smtt.sdk.WebView forumConte = (com.tencent.smtt.sdk.WebView) view.findViewById(R.id.webview);
                com.chenxi.cebim.view.SuperFileView2 mSuperFileView = (com.chenxi.cebim.view.SuperFileView2) view.findViewById(R.id.mSuperFileView);
                RelativeLayout reviewFaild = (RelativeLayout) view.findViewById(R.id.rl_preview_faild);

                forumConte.setVisibility(GONE);
                mSuperFileView.setVisibility(GONE);
                reviewFaild.setVisibility(VISIBLE);
            }
        } else {
            LogUtils.e(TAG, "文件路径无效！");
        }

    }

    /***
     * 获取文件类型
     *
     * @param paramString
     * @return
     */
    private String getFileType(String paramString) {
        String str = "";

        if (TextUtils.isEmpty(paramString)) {
            LogUtils.d(TAG, "paramString---->null");
            return str;
        }
        LogUtils.d(TAG, "paramString:" + paramString);
        int i = paramString.lastIndexOf('.');
        if (i <= -1) {
            LogUtils.d(TAG, "i <= -1");
            return str;
        }


        str = paramString.substring(i + 1);
        LogUtils.d(TAG, "paramString.substring(i + 1)------>" + str);
        return str;
    }

    public void show() {
        if (mOnGetFilePathListener != null) {
            mOnGetFilePathListener.onGetFilePath(this);
        }
    }

    /***
     * 将获取File路径的工作，“外包”出去
     */
    public interface OnGetFilePathListener {
        void onGetFilePath(SuperFileView2 mSuperFileView2);
    }


    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {
        LogUtils.e(TAG, "****************************************************" + integer);
    }

    public void onStopDisplay() {
        if (mTbsReaderView != null) {
            mTbsReaderView.onStop();
        }
    }

}
