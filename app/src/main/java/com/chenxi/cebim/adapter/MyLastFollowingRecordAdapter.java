package com.chenxi.cebim.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.material.ComponentTrackingDetailsActivity;
import com.chenxi.cebim.activity.material.ConstructionDetailsTrackingActivity;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialTraceModel;

import java.util.ArrayList;
import java.util.List;

public class MyLastFollowingRecordAdapter extends RecyclerView.Adapter<MyLastFollowingRecordAdapter.ViewHolder> {

    private List<MaterialTraceModel> materialTraceList = new ArrayList<MaterialTraceModel>();
    private int mPeojectID;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView materialName, materialId, materialState, userName, createTime;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            materialName = (TextView) view.findViewById(R.id.tv_material_name);
            materialId = (TextView) view.findViewById(R.id.tv_material_id);
            materialState = view.findViewById(R.id.tv_material_state);
            userName = view.findViewById(R.id.tv_myfollowing_trace_record_username);
            createTime = view.findViewById(R.id.tv_myfollowing_trace_record_time);
        }
    }

    public MyLastFollowingRecordAdapter(List<MaterialTraceModel> materialTraceStateList, int peojectID) {
        this.materialTraceList = materialTraceStateList;
        mPeojectID = peojectID;
    }

    public List<MaterialTraceModel> getList() {

        return materialTraceList;
    }


    @NonNull
    @Override
    public MyLastFollowingRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myfollowing_trace_record_item, viewGroup, false);
        final MyLastFollowingRecordAdapter.ViewHolder holder = new MyLastFollowingRecordAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(MyApplication.getContext(), ComponentTrackingDetailsActivity.class);
                intent.putExtra("ID", materialTraceList.get(position).getTempleteID());
                intent.putExtra("ComponentUID", materialTraceList.get(position).getComponentUID());
                view.getContext().startActivity(intent);
            }
        });

        return holder;
    }


    public void onBindViewHolder(MyLastFollowingRecordAdapter.ViewHolder holder, int position) {
        try {
            MaterialTraceModel materialTraceModel = materialTraceList.get(position);
            holder.materialName.setText(materialTraceModel.getComponentName());
            holder.materialId.setText(materialTraceModel.getComponentID());
            holder.materialState.setText(materialTraceModel.getStateInfo().getName());
            // if (materialTraceModel.getCrUserInfo() != null) {
            String str = materialTraceModel.getCrUserInfo().getUserName();
            //  if (str != null) {
            holder.userName.setText(str);
            //  }
            // }
            // if (materialTraceModel.getUpdateTime() != null && (!materialTraceModel.getUpdateTime().equals("null"))) {
            holder.createTime.setText(materialTraceModel.getUpdateTime());
            // }
        } catch (Exception e) {
            e.printStackTrace();
            holder.userName.setText("");
        }


    }

    @Override
    public int getItemCount() {
        if (materialTraceList == null) {
            return 0;
        } else {
            return materialTraceList.size();
        }
    }

    public void updateData(List<MaterialTraceModel> list) {
        materialTraceList.clear();
        materialTraceList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
