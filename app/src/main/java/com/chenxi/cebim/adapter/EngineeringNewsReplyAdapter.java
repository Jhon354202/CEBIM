package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.EngineeringNewsModel;
import com.chenxi.cebim.entity.EngineeringNewsRefreshEvenModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class EngineeringNewsReplyAdapter extends RecyclerView.Adapter<EngineeringNewsReplyAdapter.ViewHolder> {

    private List<EngineeringNewsModel> mEngineeringNewsSonList;
    private Activity mActivity;
    private List<String> likeDataList = new ArrayList<>();//点赞信息列表
    int mParentPosition;//父position

    static class ViewHolder extends RecyclerView.ViewHolder {
        View projectNewsView;

        TextView name,content;

        public ViewHolder(View view) {
            super(view);
            projectNewsView = view;
            name = (TextView) view.findViewById(R.id.tv_reply_name);
            content= (TextView) view.findViewById(R.id.tv_reply_content);

        }
    }

    public EngineeringNewsReplyAdapter(Activity activity, List<EngineeringNewsModel> engineeringNewsSonList,int parentPosition) {
        mEngineeringNewsSonList = engineeringNewsSonList;
        mActivity = activity;
        mParentPosition=parentPosition;
    }

    @NonNull
    @Override
    public EngineeringNewsReplyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.engineering_news_reply_item, null, false);
        final EngineeringNewsReplyAdapter.ViewHolder holder = new EngineeringNewsReplyAdapter.ViewHolder(view);
        holder.projectNewsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                EventBus.getDefault().post(new EngineeringNewsRefreshEvenModel("删除评论:"
                        + position+":"+mParentPosition+":"+mEngineeringNewsSonList.get(position).getMomentID()));//通知ProjectNewsAdapter刷新列表
            }
        });


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EngineeringNewsReplyAdapter.ViewHolder viewHolder, int position) {
        EngineeringNewsModel engineeringNewsModel = mEngineeringNewsSonList.get(position);
        viewHolder.name.setText(engineeringNewsModel.getCreatebyUserName());
        viewHolder.content.setText(":"+engineeringNewsModel.getContents());
    }

    @Override
    public int getItemCount() {
        return mEngineeringNewsSonList.size();
    }

}
