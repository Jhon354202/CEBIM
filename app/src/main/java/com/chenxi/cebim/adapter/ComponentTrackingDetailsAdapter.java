package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialTraceRecord;
import com.chenxi.cebim.entity.TbMaterialTraceTemplateStates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ComponentTrackingDetailsAdapter extends RecyclerView.Adapter<ComponentTrackingDetailsAdapter.ViewHolder> {
    private List<TbMaterialTraceTemplateStates> list;
    private String ComponentUID;
    private List<MaterialTraceRecord> materialTraceRecords = new ArrayList<>();
    private Activity activity;

    public ComponentTrackingDetailsAdapter(Activity activity, List<TbMaterialTraceTemplateStates> list, String componentUID, List<MaterialTraceRecord> materialTraceRecords) {
        this.list = list;
        this.ComponentUID = componentUID;
        this.activity = activity;
        this.materialTraceRecords = materialTraceRecords;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, name, location_text, time;
        RelativeLayout location_view;
        ImageView image;
        RelativeLayout retry_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            name = itemView.findViewById(R.id.name);
            location_text = itemView.findViewById(R.id.location_text);
            time = itemView.findViewById(R.id.time);
            location_view = itemView.findViewById(R.id.location_view);
            image = itemView.findViewById(R.id.image);
            retry_layout = itemView.findViewById(R.id.retry_layout);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        TbMaterialTraceTemplateStates model = list.get(position);
        viewHolder.textView.setText(list.get(position).getIdInfo().getName());
        for (int i = 0; i < materialTraceRecords.size(); i++) {
            if (ComponentUID.equals(materialTraceRecords.get(i).getComponentUID())) {
                if (materialTraceRecords.get(i).getStateInfo().getName().equals(list.get(position).getStateName())) {
                    viewHolder.location_text.setText(materialTraceRecords.get(i).getLocation());
                    viewHolder.time.setText(String.valueOf(materialTraceRecords.get(i).getAddTime()));
                    viewHolder.name.setText(materialTraceRecords.get(i).getOperationUserInfo().getUserName());
                    if (viewHolder.location_text.getText() == "" || viewHolder.location_text.getText().equals(null)) {
                        viewHolder.location_view.setVisibility(View.GONE);
                    } else {
                        viewHolder.location_view.setVisibility(View.VISIBLE);
                    }
                    viewHolder.retry_layout.setVisibility(View.VISIBLE);
                    String color = materialTraceRecords.get(i).getStateInfo().getColor();
                    String[] colors = color.split(",");
                    viewHolder.textView.setBackgroundColor(Color.rgb(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2])));
                    viewHolder.textView.setTextColor(Color.WHITE);
                } else {
                    viewHolder.retry_layout.setVisibility(View.GONE);
                    viewHolder.textView.setBackgroundColor(Color.parseColor("#c4c4c4"));
                    viewHolder.textView.setTextColor(Color.WHITE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
