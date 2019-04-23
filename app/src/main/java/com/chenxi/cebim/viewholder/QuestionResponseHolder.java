package com.chenxi.cebim.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chenxi.cebim.R;

public class QuestionResponseHolder extends RecyclerView.ViewHolder {
    public View dataFileView;
    public ImageView userPic, iv_response_audio_play;
    public TextView userName, createTime, content, tv_response_audio_play, tv_response_at;
    public LinearLayout playAudio, ll_response_at;
    public RecyclerView showPicRecyclerView, showDocumentRecyclerView;

    public QuestionResponseHolder(View view) {
        super(view);
        dataFileView = view;
        userPic = view.findViewById(R.id.iv_response_user_pic);
        iv_response_audio_play = view.findViewById(R.id.iv_response_audio_play);
        userName = view.findViewById(R.id.tv_response_use_name);
        createTime = view.findViewById(R.id.tv_response_create_time);
        content = view.findViewById(R.id.problem_response_content);
        tv_response_audio_play = view.findViewById(R.id.tv_response_audio_play);
        tv_response_at = view.findViewById(R.id.tv_response_at);
        playAudio = view.findViewById(R.id.ll_response_audio_play);
        ll_response_at = view.findViewById(R.id.ll_response_at);
        showPicRecyclerView = view.findViewById(R.id.response_pic_recyclerView);
        showDocumentRecyclerView = view.findViewById(R.id.response_document_recyclerview);
    }
}
