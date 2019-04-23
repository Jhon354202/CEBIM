package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.MaterialSettings;

import java.util.List;

public class MaterialtrackingSettingsAdapter extends RecyclerView.Adapter<MaterialtrackingSettingsAdapter.ViewHolder> {
    private List<MaterialSettings> materialSettingslist;
    private Activity activity;
    private OnRecyclerItemClickListener onRecyclerItemClickListener = null;


    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView setting_name;
        private ImageView tv_material_choose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setting_name = itemView.findViewById(R.id.setting_name);
            tv_material_choose = itemView.findViewById(R.id.tv_material_choose);
        }
    }

    /**
     * @param materialSettingslist 数据源
     */
    public MaterialtrackingSettingsAdapter(List<MaterialSettings> materialSettingslist, Activity activity) {
        this.materialSettingslist = materialSettingslist;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.materialset_item, viewGroup, false);
        View view = View.inflate(activity, R.layout.materialset_item, null);
        final MaterialtrackingSettingsAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.setting_name.setText(materialSettingslist.get(i).getName());
        viewHolder.itemView.setTag(materialSettingslist.get(i).getName());
        if (materialSettingslist.get(i).isChoose()) {
            viewHolder.tv_material_choose.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tv_material_choose.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener.onItemClick(v, v.getTag().toString(), i);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return materialSettingslist.size();
    }

    public interface OnRecyclerItemClickListener {
        void onItemClick(View view, String data, int position);
    }


    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        this.onRecyclerItemClickListener = listener;
    }
}
