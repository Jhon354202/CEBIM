package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.RoleInfo;
import com.chenxi.cebim.entity.TaskReplyModel;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FeedBackDetailAdapter extends RecyclerView.Adapter<FeedBackDetailAdapter.ViewHolder> {

    private List<TaskReplyModel> mFeedBackRecordList;
//    private Context mContext;
    private Activity mActivity;
    private List<RoleInfo> picList = new ArrayList<>();//装图片

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView createTime, progress, remark, practicalLaborSum, actualUnit;
        CircularProgressBar circularProgressBar;
        View divLineTop, divLineCenter, divLineBottom;
        RecyclerView picRecyclerView, documentRecyclerView;


        public ViewHolder(View view) {
            super(view);
            this.view = view;
            createTime = view.findViewById(R.id.tv_task_feed_back_record_detail_time);
            progress = view.findViewById(R.id.tv_task_feed_back_record_detail_progress);
            remark = view.findViewById(R.id.tv_feed_back_record_detail_remark);
            practicalLaborSum = view.findViewById(R.id.tv_feed_back_record_detail_actual_labour);
            actualUnit = view.findViewById(R.id.tv_feed_back_record_detail_actualUnit);

            circularProgressBar = view.findViewById(R.id.cb_task_feed_back_record_detail_progress);

            divLineTop = view.findViewById(R.id.view_div_line_top);
            divLineCenter = view.findViewById(R.id.view_div_line_center);
            divLineBottom = view.findViewById(R.id.view_div_line_bottom);

            picRecyclerView = view.findViewById(R.id.rv_feed_back_record_detail_pic);
            documentRecyclerView = view.findViewById(R.id.rv_feed_back_record_detail_document);
        }
    }

    /**
     * @param feedBackRecordList 数据源
     */
    public FeedBackDetailAdapter(Activity activity, List<TaskReplyModel> feedBackRecordList) {
        mFeedBackRecordList = feedBackRecordList;
        mActivity = activity;
    }


    @NonNull
    @Override
    public FeedBackDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_back_record_detail_item,
                viewGroup, false);
        final FeedBackDetailAdapter.ViewHolder holder = new FeedBackDetailAdapter.ViewHolder(view);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return holder;
    }

    public void onBindViewHolder(FeedBackDetailAdapter.ViewHolder holder, int position) {

        if (position ==0) {
            holder.divLineTop.setVisibility(View.INVISIBLE);
            holder.divLineCenter.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.feed_back_detail_circle_light_shape));

        } else {
            holder.divLineCenter.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.feed_back_detail_circle_gray_shape));

        }

        

        TaskReplyModel taskReplyModel = mFeedBackRecordList.get(holder.getAdapterPosition());
        holder.createTime.setText(taskReplyModel.getCreatedAt().split(" ")[0]);
        holder.progress.setText((int)taskReplyModel.getPercentage()+"%");

        if(taskReplyModel.getRemark()!=null&&(!taskReplyModel.getRemark().equals("null"))){
            holder.remark.setText(taskReplyModel.getRemark());
        }

        if(taskReplyModel.getPracticalLabor()!=-1){
            holder.practicalLaborSum.setText(""+taskReplyModel.getPracticalLabor());
        }else{
            holder.practicalLaborSum.setText("0");
        }

        if(taskReplyModel.getActualUnit()!=null&&(!taskReplyModel.getActualUnit().equals("null"))){
            holder.actualUnit.setText(taskReplyModel.getActualUnit());
        }

        holder.circularProgressBar.setProgress((float) taskReplyModel.getPercentage());

        //防止刷新时显示
        if (picList != null) {
            picList.clear();
        }

        if(taskReplyModel.getPictures()!=null&&(!taskReplyModel.getPictures().equals(""))
                &&(!taskReplyModel.getPictures().equals("null"))&&(!taskReplyModel.getPictures().equals("[]"))){
            holder.picRecyclerView.setVisibility(View.VISIBLE);

            //解析Pictures数据并显示图片
            //解析图片地址，并显示解析
            try {
                String data = taskReplyModel.getPictures().toString();
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String Name = jsonObject.getString("Name");
                    String ID = jsonObject.getString("ID");
                    RoleInfo roleInfo = new RoleInfo();
                    roleInfo.setID(ID);
                    roleInfo.setName(Name);
                    picList.add(roleInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            holder.picRecyclerView.setVisibility(View.GONE);
        }

        if (picList != null && (!picList.equals("null")) && picList.size() > 0) {
            //初始化Adapter等，显示列表
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.picRecyclerView.setLayoutManager(layoutManager);
            PicShowHorizontalAdapter adapter = new PicShowHorizontalAdapter(mActivity, picList);
            holder.picRecyclerView.setAdapter(adapter);
        }

    }

    @Override
    public int getItemCount() {
        return mFeedBackRecordList.size();
    }

}
