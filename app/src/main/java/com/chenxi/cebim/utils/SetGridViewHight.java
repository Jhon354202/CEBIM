package com.chenxi.cebim.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.chenxi.cebim.view.DragGridView;

/*
*用于设置DragGridView的高度，处理DragGridView被scrollView包裹时只显示一行的问题，但是，用了这个方法
* 会影响DragGridView的拖动，暂时放这边
 */
public class SetGridViewHight {
    public static void setGrideViewHeightBasedOnChildren(DragGridView gridView) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int col = 4;
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i += col) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;//这个70是写死的，有缺陷，看看如何完善
        ((ViewGroup.MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        gridView.setLayoutParams(params);
    }
}
