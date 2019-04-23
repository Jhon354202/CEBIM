package com.chenxi.cebim.activity;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.chenxi.cebim.R;

import net.ezbim.layer.BIMData;
import net.ezbim.layer.BIMView;

public class ShowModelActivity extends BaseActivity {

    BIMData bimData;
    BIMView bIMView;
    net.ezbim.layer.BIMGLSurfaceView bIMGLSurfaceView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_model);

        bIMGLSurfaceView=(net.ezbim.layer.BIMGLSurfaceView)findViewById(R.id.model_view);
        bimData = new BIMData(this);
        bIMView = new BIMView(this, bIMGLSurfaceView);
        bIMView.bindData(bimData);

        //加载加进度条，加班类别请求成功后消失。
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("模型加载中...");
        progressDialog.setCancelable(true);
        progressDialog.show();  //将进度条显示出来

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final boolean isOpen=bimData.dataLoad().openModel("/storage/emulated/0/DCIM/5a66a0ad32af405ad7509374.yz", true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isOpen){
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).start();


//        bimData.dataLoad().openModel("file:///android_asset/5a66a0ad32af405ad7509374.yz", true);
//        bimData.dataLoad().openModel("/storage/emulated/0/DCIM/5a66a0ad32af405ad7509374.yz", true);
    }
}
