package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.QuestionCommentModel;

import java.util.ArrayList;
import java.util.List;

public class QuestionResponseAdapter extends RecyclerView.Adapter<QuestionResponseAdapter.ViewHolder> {

    private List<QuestionCommentModel> mQuestionCommentList = new ArrayList<>();
    Context mContext;

    private int mProjectID;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        ImageView userPic, iv_response_audio_play;
        TextView userName, createTime, content, tv_response_audio_play, tv_response_at;
        LinearLayout playAudio, ll_response_at;
        RecyclerView showPicRecyclerView, showDocumentRecyclerView;

        public ViewHolder(View view) {
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

    /**
     * @param questionCommentList 数据源
     */
    public QuestionResponseAdapter(Context context, List<QuestionCommentModel> questionCommentList) {
        mContext = context;
        mQuestionCommentList = questionCommentList;
    }


    @NonNull
    @Override
    public QuestionResponseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_response_item, viewGroup, false);
        final QuestionResponseAdapter.ViewHolder holder = new QuestionResponseAdapter.ViewHolder(view);

        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
//                Intent intent = new Intent(view.getContext(), ProblemDetail.class);
//                view.getContext().startActivity(intent);
            }
        });


        return holder;
    }

    public void onBindViewHolder(QuestionResponseAdapter.ViewHolder holder, int position) {

        QuestionCommentModel questionCommentModel = mQuestionCommentList.get(holder.getAdapterPosition());

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.headpic);//把drawable中的图转为bitmap
        holder.userPic.setImageBitmap(ImageUtils.toRound(bmp));

        //设置评论者名字
        if (questionCommentModel.getUserName() != null) {
            holder.userName.setText(questionCommentModel.getUserName());
        }

        if (questionCommentModel.getDate() != null) {
            holder.createTime.setText(questionCommentModel.getDate().toString().replace("T", " "));
        }

        if (questionCommentModel.getComment() != null && (!questionCommentModel.getComment().toString().equals("null"))) {
            holder.content.setText(questionCommentModel.getComment());
        }

        //如果返回的音频数据为null或"[]"
        if (questionCommentModel.getVoice() != null && questionCommentModel.getVoice() != "[]") {
            holder.playAudio.setVisibility(View.VISIBLE);
            //还需获取音频时间并设置

        } else {
            holder.playAudio.setVisibility(View.GONE);
        }

        //如果返回的图片和视频数据为null或"[]"
        if ((questionCommentModel.getPictures()!= null && questionCommentModel.getPictures() != "[]")
                ||(questionCommentModel.getVideo() != null && questionCommentModel.getVideo() != "[]")) {
            holder.showPicRecyclerView.setVisibility(View.VISIBLE);

//            Glide.with(MyApplication.getContext())
//                    .load(questionCommentModel.getFirstFrame())
//                    .into(holder.picOrVideo);
        } else {
            holder.showPicRecyclerView.setVisibility(View.GONE);
        }

        if(questionCommentModel.getAt()!=null){
            holder.showDocumentRecyclerView.setVisibility(View.VISIBLE);
        }else{
            holder.showDocumentRecyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mQuestionCommentList.size();
    }

}
