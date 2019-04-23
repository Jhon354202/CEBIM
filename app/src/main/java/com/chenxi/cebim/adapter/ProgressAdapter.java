package com.chenxi.cebim.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.CacheDoubleUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.SingleProgressActivity;
import com.chenxi.cebim.entity.ModelList;
import com.chenxi.cebim.entity.ProgressModel;
import com.chenxi.cebim.utils.ACache;

import java.util.ArrayList;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {

    private ArrayList<ProgressModel> mProgressModelList = new ArrayList<ProgressModel>();
    private ArrayList<ProgressModel> cacheProgressModelList = new ArrayList<ProgressModel>();
    private ArrayList<ProgressModel> allList = new ArrayList<ProgressModel>();
    private int mProjectID, mIsChoose;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View progressView;
        ImageView progressPic;
        TextView progressName, startTime, endTime;
        cn.refactor.library.SmoothCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            progressView = view;
            checkBox = (cn.refactor.library.SmoothCheckBox) view.findViewById(R.id.progress_item_cb);
            progressPic = (ImageView) view.findViewById(R.id.iv_progress);
            progressName = (TextView) view.findViewById(R.id.progress_name);
            startTime = (TextView) view.findViewById(R.id.progress_start_time);
            endTime = (TextView) view.findViewById(R.id.progress_end_time);
        }
    }

    public ProgressAdapter(ArrayList<ProgressModel> progressList, int projectID, int isChoose, int ParentEPPID) {//更列表ParentEPPID传-1，其他按实际值传递

        if (progressList != null) {

            if (ParentEPPID == -1) {//ParentEPPID == -1表明是根列表
                for (int i = 0; i < progressList.size(); i++) {

                    if (progressList.get(i).getParentEPPID() == -1) {//ParentEPPID是int？类型，故此处做此处理
                        mProgressModelList.add(progressList.get(i));
                    }
                }
            } else {//非根列表
                for (int i = 0; i < progressList.size(); i++) {

                    if (progressList.get(i).getParentEPPID() == ParentEPPID) {
                        mProgressModelList.add(progressList.get(i));
                    }
                }
            }
        }
        allList = progressList;
        mProjectID = projectID;
        mIsChoose = isChoose;
    }

    @NonNull
    @Override
    public ProgressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progress_item, viewGroup, false);

        final ProgressAdapter.ViewHolder holder = new ProgressAdapter.ViewHolder(view);

        holder.progressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                ToastUtils.showShort("你点击了第" + position + "个项目");

                if ((ArrayList<ModelList>) CacheDoubleUtils.getInstance().getSerializable("progress" + mProjectID) != null) {
                    cacheProgressModelList = (ArrayList<ProgressModel>) CacheDoubleUtils.getInstance().getSerializable("progress" + mProjectID);//缓存List
                    isTheSameElement(position);
                } else {
                    cacheProgressModelList.add(mProgressModelList.get(position));//添加点击的list
                    CacheDoubleUtils.getInstance().put("progress" + mProjectID, cacheProgressModelList, ACache.TIME_DAY * 2);//缓存容器
                }

                Intent intent = new Intent(view.getContext(), SingleProgressActivity.class);
                intent.putExtra("ProcessName", mProgressModelList.get(position).getProcessName());
                intent.putExtra("EPPID", mProgressModelList.get(position).getEPPID());
                intent.putExtra("projectId", mProgressModelList.get(position).getProjectID());
                view.getContext().startActivity(intent);

            }
        });

        return holder;
    }

    //用于判断所点击的是否有和缓存中相同的，如有，不添加进cacheList，没有则添加进去
    private void isTheSameElement(int position) {
        //用于判断所点击的mModelList是否有和缓存中相同的，如有，则isTheSame赋值为1
        int isTheSame = 0;
        for (int i = 0; i < cacheProgressModelList.size(); i++) {
            if (mProgressModelList.get(position).equals(cacheProgressModelList.get(i))) {
                isTheSame = 1;
            }
        }

        if (isTheSame == 0) {
            cacheProgressModelList.add(mProgressModelList.get(position));//添加点击的list
            CacheDoubleUtils.getInstance().put("progress" + mProjectID, cacheProgressModelList, ACache.TIME_DAY * 2);//缓存容器
        }
    }


    public void onBindViewHolder(ProgressAdapter.ViewHolder holder, int position) {
        ProgressModel progressModel = mProgressModelList.get(position);

        //设置是否开启checkBox
        if (mIsChoose == 0) {
            holder.checkBox.setVisibility(View.GONE);
        } else if (mIsChoose == 1) {
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        holder.progressPic.setImageResource(R.drawable.picture);
        holder.progressName.setText(progressModel.getProcessName());
        holder.startTime.setText(progressModel.getPlanBeginTime().toString().substring(0, 10));
        holder.endTime.setText(progressModel.getPlanEndTime().toString().substring(0, 10));

    }

    @Override
    public int getItemCount() {
        return mProgressModelList.size();
    }
}
