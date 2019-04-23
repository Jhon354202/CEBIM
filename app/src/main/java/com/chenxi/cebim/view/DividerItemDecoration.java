package com.chenxi.cebim.view;

import android.content.Context;

import com.yanyusong.y_divideritemdecoration.Y_Divider;
import com.yanyusong.y_divideritemdecoration.Y_DividerBuilder;
import com.yanyusong.y_divideritemdecoration.Y_DividerItemDecoration;

public class DividerItemDecoration extends Y_DividerItemDecoration {

    private DividerItemDecoration(Context context) {
        super(context);
    }

    @Override
    public Y_Divider getDivider(int itemPosition) {
        Y_Divider divider = null;

        //每一行第一个显示rignt和bottom
        divider = new Y_DividerBuilder()
                .setBottomSideLine(true, 0xff666666, 6, 0, 0)
                .create();

        return divider;
    }
}