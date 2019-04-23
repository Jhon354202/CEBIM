package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.Data;

import java.util.List;

public class StatisticsDataAdapter extends RecyclerView.Adapter<StatisticsDataAdapter.ViewHolder> {
    private List<Data> dataList;
    private Context context;
    private int checked = -1;
    private OnItemClickListener onItemClickListener;

    public StatisticsDataAdapter(List<Data> dataList, Context context) {
        this.context = context;
        this.dataList = dataList;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_statistics_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Data data = dataList.get(i);
        viewHolder.textView.setText(data.getDataName());
        if (dataList.get(i).isCheck()) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.textView.setTextColor(Color.BLUE);
        } else {
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.textView.setTextColor(Color.BLACK);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.choose);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
