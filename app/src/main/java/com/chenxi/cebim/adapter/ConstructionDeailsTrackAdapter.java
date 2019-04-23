package com.chenxi.cebim.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.TbMaterialTraceTemplateStates;
import com.chenxi.cebim.entity.Templatestate;

import java.util.List;

public class ConstructionDeailsTrackAdapter extends RecyclerView.Adapter<ConstructionDeailsTrackAdapter.ViewHolder> {
    private List<TbMaterialTraceTemplateStates> list;

    public ConstructionDeailsTrackAdapter(List<TbMaterialTraceTemplateStates> list) {
        this.list = list;
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
        for (int i = 0; i < list.size(); i++) {
            viewHolder.textView.setText(list.get(position).getIdInfo().getName());
            String color=list.get(position).getIdInfo().getColor();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }
}