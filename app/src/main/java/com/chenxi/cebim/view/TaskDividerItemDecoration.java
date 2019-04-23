package com.chenxi.cebim.view;

import android.content.Context;

import com.chenxi.cebim.R;
import com.chenxi.cebim.application.MyApplication;
import com.yanyusong.y_divideritemdecoration.Y_Divider;
import com.yanyusong.y_divideritemdecoration.Y_DividerBuilder;
import com.yanyusong.y_divideritemdecoration.Y_DividerItemDecoration;

public class TaskDividerItemDecoration extends Y_DividerItemDecoration {

    public TaskDividerItemDecoration(Context context) {
        super(context);
    }

    @Override
    public Y_Divider getDivider(int itemPosition) {

        Y_Divider divider = null;
        divider = new Y_DividerBuilder()
                .setBottomSideLine(true, MyApplication.getContext().getResources().getColor(R.color.divice_line, null), 6, 0, 0)
                .create();
        return divider;

    }
}
