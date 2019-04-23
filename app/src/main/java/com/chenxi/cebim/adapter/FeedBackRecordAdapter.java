package com.chenxi.cebim.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.TaskFeedBackRecordActivity;
import com.chenxi.cebim.entity.TaskReplyModel;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.List;

/**
 * 反馈记录Adapter
 */
public class FeedBackRecordAdapter extends RecyclerView.Adapter<FeedBackRecordAdapter.ViewHolder> {

    private List<TaskReplyModel> mFeedBackRecordList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        RelativeLayout feedContent;
        ImageView headPic;
        TextView creatorName,creatTime,precentage,lastOne;
        CircularProgressBar circularProgressBar;

        public ViewHolder(View view) {
            super(view);
            dataFileView = view;
            feedContent=view.findViewById(R.id.rl_feed_back_record_content);
            headPic = (ImageView) view.findViewById(R.id.iv_feed_back_record_pic);
            creatorName = (TextView) view.findViewById(R.id.tv_feed_back_creator_name);
            creatTime = (TextView) view.findViewById(R.id.tv_feed_back_create_time);
            circularProgressBar=view.findViewById(R.id.cb_task_feed_back_record_progress);
            precentage=view.findViewById(R.id.tv_task_feed_back_record_progress);
            lastOne=view.findViewById(R.id.feedback_record_lastone);
        }
    }

    /**
     * @param feedBackRecordList 数据源
     */
    public FeedBackRecordAdapter(Context context, List<TaskReplyModel> feedBackRecordList) {
        mFeedBackRecordList = feedBackRecordList;
        mContext=context;
    }


    @NonNull
    @Override
    public FeedBackRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_back_record_item, viewGroup, false);
        final FeedBackRecordAdapter.ViewHolder holder = new FeedBackRecordAdapter.ViewHolder(view);

        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Intent intent=new Intent(mContext, TaskFeedBackRecordActivity.class);
                intent.putExtra("TaskId",mFeedBackRecordList.get(position).getTaskId());
                intent.putExtra("CreatedBy",mFeedBackRecordList.get(position).getCreatedBy());
                mContext.startActivity(intent);
            }
        });

        return holder;
    }

    public void onBindViewHolder(FeedBackRecordAdapter.ViewHolder holder, int position) {

        int pp=position;
        int ppp=holder.getAdapterPosition();
        if(position<mFeedBackRecordList.size()){
            holder.feedContent.setVisibility(View.VISIBLE);
            holder.lastOne.setVisibility(View.GONE);
            TaskReplyModel taskReplyModel = mFeedBackRecordList.get(holder.getAdapterPosition());
            holder.creatorName.setText(""+taskReplyModel.getCreatedUserName());
            holder.creatTime.setText(taskReplyModel.getCreatedAt().split(" ")[0]);
            holder.circularProgressBar.setProgress((float) taskReplyModel.getPercentage());
            holder.precentage.setText((int)taskReplyModel.getPercentage()+"%");
        }else{
            holder.feedContent.setVisibility(View.GONE);
            holder.lastOne.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mFeedBackRecordList.size()+1;
    }

}
