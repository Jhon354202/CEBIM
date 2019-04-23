package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.ProjectInfoItem;

import java.util.List;

//用于ProjectActivity
public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<ProjectInfoItem> list;
    private LayoutInflater layoutInflater;
    private TextView itemName;
    private ImageView mImageView;

    private MyClickListener pListener;


    public GridViewAdapter(Context context, List<ProjectInfoItem> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size() ;//注意此处
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

        convertView = layoutInflater.inflate(R.layout.grid_item, null);
        mImageView = (ImageView) convertView.findViewById(R.id.item);
        itemName = (TextView) convertView.findViewById(R.id.item_name);

        GridView mGridView=(GridView)parent;
        ViewGroup.LayoutParams layoutParams = mGridView.getLayoutParams();


//  设置gradeView的高度为item的3倍
//        convertView.measure(0,0);
//        int height=convertView.getMeasuredHeight();//获取GrideView的item高度
//        if(getCount()<=12){
//            layoutParams.height = mGridView.getLayoutParams().WRAP_CONTENT;//设置GridView的高度为WRAP_CONTENT
//        }else{
//            layoutParams.height = height*3;//设置GridView的高度为item的3倍
//        }


        mGridView.setLayoutParams(layoutParams);
        itemName.setText(list.get(position).getItemName());

        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),list.get(position).getImage());//把drawable中的图转为bitmap
//        Glide.with(context).load(ImageUtils.toRound(bmp)).into(mImageView);
//        mImageView.setImageResource(list.get(position).getImage());
        mImageView.setImageBitmap(ImageUtils.toRound(bmp));
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
            myOnClick((Integer) v.getTag(), v);
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