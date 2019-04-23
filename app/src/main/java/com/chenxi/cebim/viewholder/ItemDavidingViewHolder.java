package com.chenxi.cebim.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chenxi.cebim.R;

/**
 * 问题详情，详情和回复的分割线Holder
 */
public class ItemDavidingViewHolder extends RecyclerView.ViewHolder {
    public View view;

    public ItemDavidingViewHolder (View itemView) {
        super(itemView);
        view = (View) itemView.findViewById(R.id.view_deivide);
    }
}
