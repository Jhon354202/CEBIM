package com.chenxi.cebim.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.AtMembersModel;

import java.util.ArrayList;
import java.util.List;

public class QuestionAtAdapter extends RecyclerView.Adapter<QuestionAtAdapter.ViewHolder> {

    private List<AtMembersModel> mDataFileList;

    private int mProjectID, mClassID;
    Boolean isShowCheckBox = false;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        ImageView headPic;
        TextView memberName;
        cn.refactor.library.SmoothCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            dataFileView = view;
            checkBox = (cn.refactor.library.SmoothCheckBox) view.findViewById(R.id.sb_at);
            checkBox.setClickable(false);//禁用checkBox点击事件
            headPic = (ImageView) view.findViewById(R.id.iv_at_logo);
            memberName = (TextView) view.findViewById(R.id.tv_at_name);
        }
    }

    /**
     * @param dataFileList 数据源
     */
    public QuestionAtAdapter(ArrayList<AtMembersModel> dataFileList) {
        mDataFileList = dataFileList;
    }

    public List<AtMembersModel> getList() {
        if (mDataFileList == null) {
            mDataFileList = new ArrayList<>();
        }
        return mDataFileList;
    }

    @NonNull
    @Override
    public QuestionAtAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_at_item, viewGroup, false);

        final QuestionAtAdapter.ViewHolder holder = new QuestionAtAdapter.ViewHolder(view);
        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    mDataFileList.get(position).setChecked(false);
                } else {
                    holder.checkBox.setChecked(true);
                    mDataFileList.get(position).setChecked(true);
                }
            }
        });

        return holder;
    }


    public void onBindViewHolder(QuestionAtAdapter.ViewHolder holder, int position) {

        AtMembersModel atMembersModel = mDataFileList.get(holder.getAdapterPosition());

        holder.memberName.setText(atMembersModel.getUserName());

        if(atMembersModel.isChecked()){
            holder.checkBox.setChecked(true);
        }else{
            holder.checkBox.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return mDataFileList.size();
    }

}

