package com.chenxi.cebim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chenxi.cebim.R;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.utils.GetFileType;

import java.util.List;

public class ShowPicGridViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;
    private LayoutInflater layoutInflater;
    private ImageView mImageView;
    private RelativeLayout del;
    private MyClickListener pListener;

    public ShowPicGridViewAdapter(Context context, List<String> list, MyClickListener pListener) {
        this.context = context;
        this.list = list;
        this.pListener = pListener;
        layoutInflater = LayoutInflater.from(context);
    }

//    public void setIsShowDelete(boolean isShowDelete) {
//        this.isShowDelete = isShowDelete;
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return list.size() + 1;//注意此处
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.showpic_grid_item, null);
        mImageView = (ImageView) convertView.findViewById(R.id.item);
        del = convertView.findViewById(R.id.del);
        del.setVisibility(position == list.size() ? View.GONE : View.VISIBLE);// 设置删除按钮是否显示
        del.setTag(position);
        del.setOnClickListener(pListener);

        GridView mGridView = (GridView) parent;
        ViewGroup.LayoutParams layoutParams = mGridView.getLayoutParams();
        convertView.measure(0, 0);
        int height = convertView.getMeasuredHeight();//获取GrideView的item高度
        if (getCount() <= 12) {
            layoutParams.height = mGridView.getLayoutParams().WRAP_CONTENT;//设置GridView的高度为WRAP_CONTENT
        } else {
            layoutParams.height = height * 3;//设置GridView的高度为item的3倍
        }
        mGridView.setLayoutParams(layoutParams);

        if (position < list.size()) {

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.default_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (GetFileType.fileType(list.get(position)).equals("图片")) {
                Glide.with(MyApplication.getContext())
                        .load(list.get(position))
                        .apply(options)
                        .into(mImageView);

            } else if (GetFileType.fileType(list.get(position)).equals("视频")) {

                Glide.with(MyApplication.getContext())
                        .load(R.drawable.vedio_bitmap)
                        .apply(options)
                        .into(mImageView);
            }

        } else if (position == list.size() && position < 9) {
            Glide.with(MyApplication.getContext())
                    .load(R.drawable.add_pic)
                    .into(mImageView);
        }
        return convertView;
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
            if (v.getTag() != null) {
                myOnClick((Integer) v.getTag(), v);
            }
        }

        public abstract void myOnClick(int position, View v);
    }

//    public void refreshOneItem(GridView gridView, int position) {
//        /**第一个可见的位置**/
//        int firstVisiblePosition = gridView.getFirstVisiblePosition();
//        /**最后一个可见的位置**/
//        int lastVisiblePosition = gridView.getLastVisiblePosition();
//
//        /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
//        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
//            /**获取指定位置view对象**/
//            View view = gridView.getChildAt(position - firstVisiblePosition);
//            getView(position, view, gridView);
//        }
//
//    }


}