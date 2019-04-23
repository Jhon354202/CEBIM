package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PlayVedioActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_vedio);

        Intent intent=getIntent();
        String vedioUrl=intent.getStringExtra("vedioUrl");
        String vedioname=intent.getStringExtra("vedioname");
        JzvdStd jzvdStd = (JzvdStd) findViewById(R.id.videoplayer);
        jzvdStd.setUp(vedioUrl, "", Jzvd.SCREEN_WINDOW_NORMAL);

        ImageView back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Jzvd.backPress()) {
                    return;
                }
                finish();
            }
        });

        TextView videoName=findViewById(R.id.toolbar_title_tv);
        videoName.setText(vedioname);

    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}
