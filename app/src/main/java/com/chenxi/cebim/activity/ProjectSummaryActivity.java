package com.chenxi.cebim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.chenxi.cebim.R;

public class ProjectSummaryActivity extends BaseActivity {

    private RelativeLayout back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_summary);

        initView();
    }

    private void initView() {
        back=(RelativeLayout)findViewById(R.id.rl_project_summary_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}
