package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.entity.MaterialTraceModel;
import com.chenxi.cebim.entity.StateNum;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.refactor.library.SmoothCheckBox;

public class StatisticRecyclerAdapter extends RecyclerView.Adapter<StatisticRecyclerAdapter.ViewHolder> implements View.OnClickListener {

    private List<StateNum> stateNums;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public StatisticRecyclerAdapter(Context context, List<StateNum> stateNums) {
        this.context = context;
        this.stateNums = stateNums;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView statisticName;

        private TextView statisticNum;


        private SmoothCheckBox checkbox;
        private Button view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            statisticName = itemView.findViewById(R.id.statistic_name);
            statisticNum = itemView.findViewById(R.id.statistic_num);
            checkbox = itemView.findViewById(R.id.checkbox);
            view = itemView.findViewById(R.id.view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.statistic_item, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.itemView.setOnClickListener(this::onClick);
        StateNum stateNum = stateNums.get(position);
        viewHolder.statisticNum.setText(String.valueOf(stateNum.getNum()));
        viewHolder.statisticName.setText(stateNum.getState());
        viewHolder.itemView.setTag(position);
        viewHolder.checkbox.setTag(position);
        viewHolder.checkbox.setOnClickListener(this::onClick);
        String color = stateNum.getColor();
        if (color != null) {
            String[] colors = color.split(",");
            viewHolder.view.setBackgroundColor(Color.rgb(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2])));
        }
        if (stateNum.isClick()) {
            viewHolder.checkbox.setChecked(true);
        } else {
            viewHolder.checkbox.setChecked(false);
        }
    }

    public enum ViewName {
        ITEM,
        PRACTISE
    }

    @Override
    public int getItemCount() {
        return stateNums.size();
    }

    public List<StateNum> getList() {
        return stateNums;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ViewName viewName, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void updateData(List<StateNum> list) {
        stateNums.clear();
        stateNums.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        if (mOnItemClickListener != null) {
            switch (v.getId()) {
                case R.id.statistic_recycler:
                    mOnItemClickListener.onItemClick(v, ViewName.PRACTISE, position);
                    break;
                default:
                    mOnItemClickListener.onItemClick(v, ViewName.ITEM, position);
                    break;
            }
        }
    }
}
