package com.chenxi.cebim.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.material.MaterialTrackingStatisticsActivity;
import com.chenxi.cebim.entity.Model;
import com.chenxi.cebim.entity.Modelstatistics;

import java.util.List;

public class StatisticsSearchAdapter extends RecyclerView.Adapter<StatisticsSearchAdapter.ViewHolder> {
    private List<Modelstatistics> modelList;
    private Context context;

    public StatisticsSearchAdapter(Context context, List<Modelstatistics> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Modelstatistics modelstatistics = modelList.get(position);
        viewHolder.textView.setText(modelstatistics.getModelName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MaterialTrackingStatisticsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("type", 1);
                bundle.putInt("model", modelstatistics.getModelID());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }


}
