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


//已选GridView的Adapter
public class FunctionSelectedAdapter extends BaseAdapter implements View.OnClickListener {

    private String Tag="FunctionSelectedAdapter";
    private Context mContext;
    private List<ProjectInfoItem> mData;//FunctionEditActivity中传过来的数据源
    private List<ProjectInfoItem> mShowData;//显示在列表中的数据源
    private LayoutInflater mInflater;//布局装载器对象
    private SelectedCallback mCallback;

    /**
     * 自定义接口，用于回调按钮点击事件到FunctionalEditActivity
     */
    public interface SelectedCallback {
        public void functionSelectedClick(View v);
    }

    public FunctionSelectedAdapter(Context context, List<ProjectInfoItem> mDatas, SelectedCallback callback) {
        this.mContext = context;
        this.mData = mDatas;
        mInflater = LayoutInflater.from(context);
        mCallback = callback;

    }

    @Override
    public int getCount() {
        LogUtil.i(Tag+"--getCount()--",mData.size() - 1+"");
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

        ViewHolder viewHolder;
        //如果view未被实例化过，缓存池中没有对应的缓存
        if (convertView == null) {
            viewHolder = new ViewHolder();
            // 由于我们只需要将XML转化为View，并不涉及到具体的布局，所以第二个参数通常设置为null
            convertView = mInflater.inflate(R.layout.selected_grid_item, null);

            //对viewHolder的属性进行赋值
            viewHolder.pic = (ImageView) convertView.findViewById(R.id.item_image);
            viewHolder.del = (ImageView) convertView.findViewById(R.id.item_del);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.item_text);

            //通过setTag将convertView与viewHolder关联
            convertView.setTag(viewHolder);
        } else {//如果缓存池中有对应的view缓存，则直接通过getTag取出viewHolder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        LogUtil.i(Tag+"--已选的list长度--",mData.size()+"");
        //已选的显示，未选的不显示
        if (mData.get(position).getSelected()) {
            //这两行把图片设置成圆形再显示
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),(int)mData.get(position).getImage());//把drawable中的图转为bitmap
            viewHolder.pic.setImageBitmap(ImageUtils.toRound(bmp));

            viewHolder.del.setImageResource(R.drawable.del);
            viewHolder.tv.setText((String) mData.get(position).getItemName());
        }

        viewHolder.del.setOnClickListener(this);
        viewHolder.del.setTag(position);

        return convertView;
    }

    // ViewHolder用于缓存控件，三个属性分别对应item布局文件的三个控件
    class ViewHolder {
        public ImageView pic;
        public ImageView del;
        public TextView tv;
    }

    @Override
    public void onClick(View v) {
        mCallback.functionSelectedClick(v);
    }

}
