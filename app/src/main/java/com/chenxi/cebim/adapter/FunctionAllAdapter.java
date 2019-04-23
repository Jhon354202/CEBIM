package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.ProjectInfoItem;
import com.chenxi.cebim.utils.LogUtil;

import java.util.List;

public class FunctionAllAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;
    private List<ProjectInfoItem> mData;
    private LayoutInflater mInflater;//布局装载器对象
    private AllCallback mCallback;

    /**
     * 自定义接口，用于回调按钮点击事件到FunctionalEditActivity
     */
    public interface AllCallback {
        public void allClick(View v);
    }

    public FunctionAllAdapter(Context context, List<ProjectInfoItem> mDatas, AllCallback callback) {
        this.mContext = context;
        this.mData = mDatas;
        mInflater = LayoutInflater.from(context);
        mCallback = callback;
    }

    @Override
    public int getCount() {
        return mData.size() - 1;//-1是为了去除最后的更多数据
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final FunctionAllAdapter.ViewHolder viewHolder;
        //如果view未被实例化过，缓存池中没有对应的缓存
        if (convertView == null) {
            viewHolder = new FunctionAllAdapter.ViewHolder();
            // 由于我们只需要将XML转化为View，并不涉及到具体的布局，所以第二个参数通常设置为null
            convertView = mInflater.inflate(R.layout.function_edit_all_grid_item, null);

            //对viewHolder的属性进行赋值
            viewHolder.pic = (ImageView) convertView.findViewById(R.id.item_all_image);
            viewHolder.addOrNo = (ImageView) convertView.findViewById(R.id.item_all_add_or_no);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.item_all_text);

            //通过setTag将convertView与viewHolder关联
            convertView.setTag(viewHolder);
        } else {//如果缓存池中有对应的view缓存，则直接通过getTag取出viewHolder
            viewHolder = (FunctionAllAdapter.ViewHolder) convertView.getTag();
        }
        //这两行把图片设置成圆形再显示
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),(int)mData.get(position).getImage());//把drawable中的图转为bitmap
        viewHolder.pic.setImageBitmap(ImageUtils.toRound(bmp));

//        viewHolder.pic.setImageResource((int) mData.get(position).getImage());
        viewHolder.tv.setText((String) mData.get(position).getItemName());

        if (mData.get(position).getSelected()) {
            viewHolder.addOrNo.setImageResource(R.drawable.function_seleted);
            LogUtil.i("FunctionAllAdapter的刷新调试:",position+"");
        } else {
            viewHolder.addOrNo.setImageResource(R.drawable.function_add);
            LogUtil.i("FunctionAllAdapter的刷新调试:",position+"");
        }

        viewHolder.addOrNo.setOnClickListener(this);
        viewHolder.addOrNo.setTag(position);
        return convertView;
    }

    // ViewHolder用于缓存控件，三个属性分别对应item布局文件的三个控件
    class ViewHolder {
        public ImageView pic;
        public ImageView addOrNo;
        public TextView tv;
    }

    @Override
    public void onClick(View v) {
        mCallback.allClick(v);
    }
}