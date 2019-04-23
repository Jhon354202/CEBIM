package com.chenxi.cebim.viewholder;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.view.FullyGridLayoutManager;

public class DetailViewHolder extends RecyclerView.ViewHolder{
    public RecyclerView picRecyclerView, documentRecyclerView;
    public LinearLayout ll_audioPlay, ll_detailAt, ll_audio_record;
    public RelativeLayout rl_deadline;
    public TextView userName, createTime, isFollow, title, content, tv_audioPlay, priority, category,
            systemType, atString, isComplete, isCompleteBtn, deadline;
    public ImageView iv_audioPlay,iv_response_audio_play;
    public DetailViewHolder (View itemView) {
        super(itemView);

        //图片选择器
        FullyGridLayoutManager manager = new FullyGridLayoutManager(MyApplication.getContext(), 4, GridLayoutManager.VERTICAL, false);
        picRecyclerView = itemView.findViewById(R.id.detail_pic_recyclerView);
        picRecyclerView.setLayoutManager(manager);

        //附件列表
        documentRecyclerView = itemView.findViewById(R.id.document_recyclerview);
        LinearLayoutManager documentLayoutManager = new LinearLayoutManager(MyApplication.getContext());
        documentRecyclerView.setLayoutManager(documentLayoutManager);

        ll_audioPlay = itemView.findViewById(R.id.ll_detail_audio_play);//点击播放录音
        ll_detailAt = itemView.findViewById(R.id.ll_detail_at);
        ll_audio_record = itemView.findViewById(R.id.ll_detail_audio_record);//录音界面

        rl_deadline = itemView.findViewById(R.id.rl_detail_deadline);

        userName = itemView.findViewById(R.id.tv_use_name);//用户名
        createTime = itemView.findViewById(R.id.tv_create_time);//创建时间
        isFollow = itemView.findViewById(R.id.is_follow);//是否关注
        title = itemView.findViewById(R.id.problem_detail_title);//标题
        content = itemView.findViewById(R.id.problem_detail_content);//内容
        tv_audioPlay = itemView.findViewById(R.id.tv_audio_play);//录音倒计时
        priority = itemView.findViewById(R.id.tv_priority);//优先级
        category = itemView.findViewById(R.id.tv_category);//类型
        systemType = itemView.findViewById(R.id.tv_systemType);//专业
        atString = itemView.findViewById(R.id.tv_detail_at);//at人员列表
        isComplete =itemView. findViewById(R.id.tv_detail_iscomplete);//是否完成
        isCompleteBtn = itemView.findViewById(R.id.bt_detail_iscomplete);//是否完成按钮
        deadline = itemView.findViewById(R.id.tv_deadline);//截止日期
        iv_audioPlay = itemView.findViewById(R.id.iv_audio_play);//播音动画
    }
}
