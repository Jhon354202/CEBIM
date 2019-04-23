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

import com.blankj.utilcode.util.SPUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.AddDocumentActivity;
import com.chenxi.cebim.entity.IsShowDocumentSureBtn;
import com.chenxi.cebim.entity.TbFileShowmodel;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class AddDocumentAdapter extends RecyclerView.Adapter<AddDocumentAdapter.ViewHolder> {

    private ArrayList<TbFileShowmodel> mDataFileList;
    private ArrayList<TbFileShowmodel> cacheDataFileList = new ArrayList<>();

    private int mProjectID, mClassID;
    Boolean isShowCheckBox = false;
    String mDocumentFileString = "";
    String mFrom;

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
    public AddDocumentAdapter(Context context, ArrayList<TbFileShowmodel> dataFileList, int projectID, String documentFileString,String from) {

        mDataFileList = dataFileList;
        mProjectID = projectID;
        mDocumentFileString = documentFileString;
        mFrom=from;
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
                String fileIDCorrector = SPUtils.getInstance().getString(mProjectID + "新建问题", "");

                if (isShowCheckBox && mDataFileList.get(position).getFileType().equals("file")) {//在checkBox显示的情况下，点击recyclerView的Item会选中checkBox
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                        mDataFileList.get(position).setChecked(false);

                        //此处需要在持久化的fidCorrector中去掉点的这部分,并去掉，，号以及开头和结尾处的，号
                        String str=fileIDCorrector.replace(mDataFileList.get(position).getFileID() +
                                ":" + mDataFileList.get(position).getFileName(), "").replace(",,", ",");
                        if(str!=null&&(!str.equals(""))){
                            fileIDCorrector = StringUtil.trimFirstAndLastChar(str,',');
                        }else{
                            fileIDCorrector="";
                        }

                        SPUtils.getInstance().put(mProjectID + "新建问题", fileIDCorrector);

                        //判断是否有选中的item
                        boolean hasChecked=false;
                        for(int i=0;i<mDataFileList.size();i++){
                            if(mDataFileList.get(i).getChecked()){
                                hasChecked=true;
                            }
                        }

                        if(!hasChecked){
                            EventBus.getDefault().post(new IsShowDocumentSureBtn("无选中Item"));//通知AddDocumentActivity弹出取消和确定按钮
                        }

                    } else {
                        holder.checkBox.setChecked(true);
                        mDataFileList.get(position).setChecked(true);
                        EventBus.getDefault().post(new IsShowDocumentSureBtn("有选中Item"));//通知AddDocumentActivity弹出取消和确定按钮
                        if (fileIDCorrector == null || fileIDCorrector.equals("")) {
                            SPUtils.getInstance().put(mProjectID + "新建问题", "" + mDataFileList.get(position).getFileID() +
                                    ":" + mDataFileList.get(position).getFileName());
                        } else {
                            SPUtils.getInstance().put(mProjectID + "新建问题", fileIDCorrector + "," + mDataFileList.get(position).getFileID() +
                                    ":" + mDataFileList.get(position).getFileName());
                        }
                    }

                    int hasOrNoChecked = 0;//是否有被选中的checkBox
                    for (int i = 0; i < mDataFileList.size(); i++) {
                        if (mDataFileList.get(i).getChecked()) {
                            hasOrNoChecked = 1;
                        }
                    }

                } else {//点击文件夹

                    Intent intent = new Intent(view.getContext(), AddDocumentActivity.class);
                    intent.putExtra("from", mFrom);
                    intent.putExtra("classID", mDataFileList.get(position).getClassID());
                    intent.putExtra("parentClassId", mDataFileList.get(position).getParentClassID());
                    intent.putExtra("dirName", mDataFileList.get(position).getFileName());
                    intent.putExtra("documentFileString", mDocumentFileString);
                    view.getContext().startActivity(intent);
                }
            }
        });

        return holder;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {

        TbFileShowmodel tbFileShowmodel = mDataFileList.get(holder.getAdapterPosition());

        //设置是否开启checkBox
        if (isShowCheckBox && mDataFileList.get(position).getFileType().equals("dir")) {
            holder.checkBox.setVisibility(View.GONE);
        } else if (isShowCheckBox && mDataFileList.get(position).getFileType().equals("file")) {
            holder.checkBox.setVisibility(View.VISIBLE);

            //选中的添加到checkList
            String fileIDCorrector = SPUtils.getInstance().getString(mProjectID + "新建问题", "");

            if (fileIDCorrector == null || fileIDCorrector.equals("")) {
                tbFileShowmodel.setChecked(false);
            } else if (fileIDCorrector.contains("" + tbFileShowmodel.getFileID())) {
                tbFileShowmodel.setChecked(true);
            }

            if (mDocumentFileString.contains("" + tbFileShowmodel.getFileID())) {
                holder.checkBox.setChecked(true);
                mDataFileList.get(position).setChecked(true);
                EventBus.getDefault().post(new IsShowDocumentSureBtn("有选中Item"));//通知AddDocumentActivity弹出取消和确定按钮
            } else {
                holder.checkBox.setChecked(false);
                mDataFileList.get(position).setChecked(false);
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
