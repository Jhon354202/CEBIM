package com.chenxi.cebim.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.CacheDoubleUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.data.DirActivity;
import com.chenxi.cebim.activity.data.FileMoveActivity;
import com.chenxi.cebim.activity.data.SingleFileActivity;
import com.chenxi.cebim.entity.IsShowBottomSettingButton;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.GetFileType;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class DataFileAdapter extends RecyclerView.Adapter<DataFileAdapter.ViewHolder> {

    private ArrayList<TbFileShowmodel> mDataFileList;
    private ArrayList<TbFileShowmodel> cacheDataFileList = new ArrayList<>();

    private int mProjectID, mClassID;
    Boolean isShowCheckBox = false;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        ImageView filePic;
        TextView fileName, createTime, fileSize;
        cn.refactor.library.SmoothCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            dataFileView = view;
            checkBox = (cn.refactor.library.SmoothCheckBox) view.findViewById(R.id.item_cb);
            checkBox.setClickable(false);//禁用checkBox点击事件
            filePic = (ImageView) view.findViewById(R.id.iv_data_file);
            fileName = (TextView) view.findViewById(R.id.data_file_name);
            createTime = (TextView) view.findViewById(R.id.data_file_create_time);
            fileSize = (TextView) view.findViewById(R.id.data_file_size);
        }
    }

    /**
     * @param dataFileList 数据源
     * @param projectID    工程id
     */
    public DataFileAdapter(Context context, ArrayList<TbFileShowmodel> dataFileList, int projectID) {

        mDataFileList = dataFileList;
        mProjectID = projectID;
    }

    //DataFileFragment和DirActivity用于获取mDataFileList并设置mDataFileList中的checkbox是否选中
    public ArrayList<TbFileShowmodel> getList() {
        if (mDataFileList == null) {
            mDataFileList = new ArrayList<>();
        }
        return mDataFileList;
    }

    //是否显示CheckBox
    public void isShowCheckBox(Boolean isShow) {
        isShowCheckBox = isShow;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.data_file_item, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                if (isShowCheckBox) {//在checkBox显示的情况下，点击recyclerView的Item会选中checkBox
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                        mDataFileList.get(position).setChecked(false);
                    } else {
                        holder.checkBox.setChecked(true);
                        mDataFileList.get(position).setChecked(true);
                    }

                    int hasOrNoChecked = 0;//是否有被选中的checkBox
                    for (int i = 0; i < mDataFileList.size(); i++) {
                        if (mDataFileList.get(i).getChecked()) {
                            hasOrNoChecked = 1;
                        }
                    }

                    if (hasOrNoChecked == 1) {
                        EventBus.getDefault().post(new IsShowBottomSettingButton("打开底部导航栏"));//用于打开底部导航栏
                    } else {
                        EventBus.getDefault().post(new IsShowBottomSettingButton("关闭底部导航栏"));//用于关闭底部导航栏
                    }

                } else {//触发item的点击事件
                    //如果文件类型是文件夹，则打开下一层,否则，如果是文件的话，直接打开
                    if (mDataFileList.get(position).getFileType().equals("dir")) {

                        if (mDataFileList.get(position).getMove()) {
                            //FileMove界面调用
                            //若是移动，FileMoveActivity
                            Intent intent = new Intent(view.getContext(), FileMoveActivity.class);
                            intent.putExtra("classID", mDataFileList.get(position).getClassID());
                            intent.putExtra("parentClassId", mDataFileList.get(position).getParentClassID());
                            intent.putExtra("dirName", mDataFileList.get(position).getFileName());

                            view.getContext().startActivity(intent);
                        } else {
                            //若不是移动，则调回DirActivity
                            Intent intent = new Intent(view.getContext(), DirActivity.class);
                            intent.putExtra("projectID", mProjectID);
                            intent.putExtra("classID", mDataFileList.get(position).getClassID());
                            intent.putExtra("dirName", mDataFileList.get(position).getFileName());
                            view.getContext().startActivity(intent);
                        }

                    } else {
                        Intent intent = new Intent(view.getContext(), SingleFileActivity.class);
                        intent.putExtra("fileName", mDataFileList.get(position).getFileName());
                        intent.putExtra("FID", mDataFileList.get(position).getFID());
                        intent.putExtra("fileID", mDataFileList.get(position).getFileID());
                        intent.putExtra("projectID", mDataFileList.get(position).getProjectID());
                        intent.putExtra("classID", mDataFileList.get(position).getClassID());
                        intent.putExtra("tbFileShowmodelString", JSON.toJSONString(mDataFileList.get(position)));
                        view.getContext().startActivity(intent);
                    }
                }
            }
        });

        ArrayList<Integer> ischecked = new ArrayList<>();//用于装被选中的

        return holder;
    }

    //用于判断所点击的是否有和缓存中相同的，如有，不添加进cacheList，没有则添加进去
    private void isTheSameElement(int position) {
        //用于判断所点击的mModelList是否有和缓存中相同的，如有，则isTheSame赋值为1
        int isTheSame = 0;
        for (int i = 0; i < cacheDataFileList.size(); i++) {
            if (mDataFileList.get(position).equals(cacheDataFileList.get(i))) {
                isTheSame = 1;
            }
        }

        if (isTheSame == 0) {
            cacheDataFileList.add(mDataFileList.get(position));//添加点击的list
            CacheDoubleUtils.getInstance().put("file" + mProjectID, cacheDataFileList, ACache.TIME_DAY * 2);//缓存容器
        }

    }

    public void onBindViewHolder(ViewHolder holder, int position) {
//        final TbFileShowmodel tbFileShowmodel = mDataFileList.get(position);
        TbFileShowmodel tbFileShowmodel = mDataFileList.get(holder.getAdapterPosition());

        //设置是否开启checkBox
        if (!isShowCheckBox) {
            holder.checkBox.setVisibility(View.GONE);
        } else if (isShowCheckBox) {
            holder.checkBox.setVisibility(View.VISIBLE);
            //选中的添加到checkList
            if (tbFileShowmodel.getChecked()) {
                holder.checkBox.setChecked(true);

            } else {
                holder.checkBox.setChecked(false);
            }
        }

        if (tbFileShowmodel.getFileType().equals("dir")) {
            holder.filePic.setImageResource(R.drawable.folder);
            holder.createTime.setVisibility(View.GONE);
        } else {
            holder.createTime.setVisibility(View.VISIBLE);
            if (GetFileType.fileType(tbFileShowmodel.getFileName()).equals("图片")) {
                holder.filePic.setImageResource(R.drawable.picture);
            } else if (GetFileType.fileType(tbFileShowmodel.getFileName()).equals("文档")) {
                holder.filePic.setImageResource(R.drawable.text);
            } else if (GetFileType.fileType(tbFileShowmodel.getFileName()).equals("视频")) {
                holder.filePic.setImageResource(R.drawable.video);
            } else if (GetFileType.fileType(tbFileShowmodel.getFileName()).equals("音乐")) {
                holder.filePic.setImageResource(R.drawable.audio);
            } else {
                holder.filePic.setImageResource(R.drawable.unknow_format);
            }
            holder.createTime.setText((String) tbFileShowmodel.getAddTime().toString().substring(0, 10));
        }

        holder.fileName.setText(tbFileShowmodel.getFileName());

    }

    @Override
    public int getItemCount() {
        return mDataFileList.size();
    }

}
