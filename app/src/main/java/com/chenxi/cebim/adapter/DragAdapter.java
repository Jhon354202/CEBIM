package com.chenxi.cebim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.Interface.DragGridBaseAdapter;
import com.chenxi.cebim.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter {
    private List<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;
    private MyClickListener pListener;
    private ImageView mImageView,del;
    private TextView mTextView;

    public DragAdapter(Context context, List<HashMap<String, Object>> list){
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 由于复用convertView导致某些item消失了，所以这里不复用item，
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.selected_grid_item, null);
        mImageView = (ImageView) convertView.findViewById(R.id.item_image);
        mTextView = (TextView) convertView.findViewById(R.id.item_text);
        del=(ImageView)convertView.findViewById(R.id.item_del);
        del.setTag(position);
        del.setOnClickListener(pListener);

        mImageView.setImageResource((Integer) list.get(position).get("item_image"));
        mTextView.setText((CharSequence) list.get(position).get("item_text"));

        if(position == mHidePosition){
            convertView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }


    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        HashMap<String, Object> temp = list.get(oldPosition);
        if(oldPosition < newPosition){
            for(int i=oldPosition; i<newPosition; i++){
                Collections.swap(list, i, i+1);
            }
        }else if(oldPosition > newPosition){
            for(int i=oldPosition; i>newPosition; i--){
                Collections.swap(list, i, i-1);
            }
        }

        list.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    /**
     * 用于回调的抽象类
     */
    public static abstract class MyClickListener implements View.OnClickListener {
        /**
         * 基类的onClick方法
         */
        @Override
        public void onClick(View v) {
            myOnClick((Integer) v.getTag(), v);
        }
        public abstract void myOnClick(int position, View v);
    }


}
