package com.chenxi.cebim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.material.MaterialFollowActivity;
import com.chenxi.cebim.activity.material.MaterialTrackingSubmodeStatisticsActivity;
import com.chenxi.cebim.activity.material.MyLastFollowingRecordActivity;
import com.chenxi.cebim.activity.material.ProjectOverallDataStatisticsActivity;

public class ComponentActivity extends BaseActivity {

    private ImageView back;
    private RelativeLayout lastFollow, materialFollow, modelStatistics, globalStatistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component);

        //控件初始化
        initView();
    }

    private void initView() {
        back = findViewById(R.id.toolbar_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        lastFollow = findViewById(R.id.rl_last_follow);
        lastFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComponentActivity.this, MyLastFollowingRecordActivity.class);
                startActivity(intent);
            }
        });

        materialFollow = findViewById(R.id.rl_material_follow);
        materialFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComponentActivity.this, MaterialFollowActivity.class);
                startActivity(intent);
            }
        });

        modelStatistics = findViewById(R.id.rl_model_statistics);
        modelStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComponentActivity.this, MaterialTrackingSubmodeStatisticsActivity.class);
                startActivity(intent);
            }
        });

        globalStatistic = findViewById(R.id.rl_global_statistic);
        globalStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ComponentActivity.this, ProjectOverallDataStatisticsActivity.class);
                startActivity(intent);
            }
        });

    }


}
