package com.chenxi.cebim.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.chenxi.cebim.R;

import java.util.ArrayList;

public class LocationSearchAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PoiItem> arrayList;
    private int selectPosition = -1;//用于记录用户选择的变量

    public LocationSearchAdapter(Context context, ArrayList<PoiItem> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {

        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {

        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_listview, null);
            holder.item_name = (TextView) convertView
                    .findViewById(R.id.item_name);
            holder.item_location = (TextView) convertView
                    .findViewById(R.id.item_location);
            holder.item_isSelect = (ImageView) convertView
                    .findViewById(R.id.is_select);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.item_name.setText(String.valueOf(arrayList.get(position)));
        holder.item_location.setText(String.valueOf(arrayList.get(position).getSnippet()));
        holder.item_isSelect.setVisibility(View.INVISIBLE);

        return convertView;
    }

    static class ViewHolder {
        TextView item_name;         //名称
        TextView item_location;     //位置
        ImageView item_isSelect;     //是否选中

    }

}

