package com.chenxi.cebim.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.model.WebModelActivity;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.ModelEntity;
import com.chenxi.cebim.entity.ModelList;
import com.chenxi.cebim.entity.RecentlyModelListClearEvent;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.Division;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;

public class AllModelAdapter extends RecyclerView.Adapter<AllModelAdapter.ViewHolder> {

    private ArrayList<ModelEntity> mModelList, cacheList = new ArrayList<ModelEntity>();
    private int mPeojectID;
    Boolean isShowCheckBox = false;
    private ACache mCache;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View projectView;
        TextView modelName, modelSize, isComplete;
        cn.refactor.library.SmoothCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            projectView = view;
            modelName = (TextView) view.findViewById(R.id.all_model_name);
            modelSize = (TextView) view.findViewById(R.id.model_size);
            isComplete = view.findViewById(R.id.is_completed);
            checkBox = view.findViewById(R.id.model_item_cb);
        }
    }

    public AllModelAdapter(ArrayList<ModelEntity> modelList, int peojectID) {
        mModelList = modelList;
        mPeojectID = peojectID;
        mCache = ACache.get(MyApplication.getContext());
    }

    //用于获取列表数据，并对其进行设置
    public ArrayList<ModelEntity> getList() {
        if (mModelList == null) {
            mModelList = new ArrayList<>();
        }
        return mModelList;
    }

    //是否显示CheckBox
    public void isShowCheckBox(Boolean isShow) {
        isShowCheckBox = isShow;
    }

    @NonNull
    @Override
    public AllModelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_model_item, viewGroup, false);
        final AllModelAdapter.ViewHolder holder = new AllModelAdapter.ViewHolder(view);
        holder.projectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                if (isShowCheckBox) {//在checkBox显示的情况下，点击recyclerView的Item会选中checkBox
                    if (holder.checkBox.isChecked()) {
                        holder.checkBox.setChecked(false);
                        mModelList.get(position).setChecked(false);
                    } else {
                        holder.checkBox.setChecked(true);
                        mModelList.get(position).setChecked(true);
                    }

                    EventBus.getDefault().post(new CommonEven("选中或取消选中模型"));//用于改变AllModelFragment底部导航栏数据

                } else {//触发item的点击事件

                    if (mModelList.get(position).isCompleted()) {
                        Intent intent = new Intent(view.getContext(), WebModelActivity.class);
                        intent.putExtra("ModelID", "" + mModelList.get(position).getModelID());
                        intent.putExtra("ProjectID", mModelList.get(position).getProjectID());
                        intent.putExtra("ModelName", mModelList.get(position).getModelName());
                        view.getContext().startActivity(intent);

                        if ((ArrayList<ModelList>) mCache.getAsObject("最近打开模型" + mPeojectID) != null) {
                            cacheList = (ArrayList<ModelEntity>) mCache.getAsObject("最近打开模型" + mPeojectID);//缓存List
                            isTheSameElement(position);
                        } else {
                            cacheList.add(mModelList.get(position));//添加点击的list
                            mCache.put("最近打开模型" + mPeojectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
                            EventBus.getDefault().post(new RecentlyModelListClearEvent("刷新最近列表"));
                        }

                    } else {
                        ToastUtils.showShort("模型轻量化处理未完成");
                    }

                }
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    mModelList.get(position).setChecked(false);
                } else {
                    holder.checkBox.setChecked(true);
                    mModelList.get(position).setChecked(true);
                }

                EventBus.getDefault().post(new CommonEven("选中或取消选中模型"));//用于改变AllModelFragment底部导航栏数据

            }
        });
        return holder;
    }

    //用于判断所点击的mModelList是否有和缓存中相同的，如有，不添加进cacheList，没有则添加进去
    private void isTheSameElement(int position) {
        //用于判断所点击的mModelList是否有和缓存中相同的，如有，则needToDelPostion记录下标，先删除这个元素，然后在0位置插入这个元素
        int needToDelPostion = -1;
        int modelId = -1;
        ModelEntity needToDel=null;
        for (int i = 0; i < cacheList.size(); i++) {
            if (mModelList.get(position).getModelID()==cacheList.get(i).getModelID()) {
                needToDelPostion = position;//记录需要删除的位置
                needToDel=mModelList.get(position);//记录需要调整到最前面的对象
                modelId=mModelList.get(position).getModelID();
            }
        }

        if (needToDelPostion == -1) {
            cacheList.add(0,mModelList.get(position));//添加点击的list
            mCache.put("最近打开模型" + mPeojectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
        }else{
            //把这个元素放在列表最前面(先删除这个元素，再在最前面插入这个元素)
            Iterator<ModelEntity> it = cacheList.iterator();
            while (it.hasNext())
            {
                ModelEntity modelEntity = it.next();
                if (modelEntity.getModelID() == modelId)
                {
                    it.remove();
                }
            }

            cacheList.add(0,needToDel);
            mCache.put("最近打开模型" + mPeojectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
        }

        EventBus.getDefault().post(new RecentlyModelListClearEvent("刷新最近列表"));
    }

    public void onBindViewHolder(AllModelAdapter.ViewHolder holder, int position) {
        ModelEntity modelEntity = mModelList.get(position);
        holder.modelName.setText(modelEntity.getModelName());
        holder.modelSize.setText(Division.division(modelEntity.getFileSize(), 1024 * 1024) + "M");

        if (modelEntity.isCompleted()) {
            holder.isComplete.setText("已完成轻量化");
            holder.modelName.setTextColor(Color.parseColor("#000000"));
            holder.modelSize.setTextColor(Color.parseColor("#000000"));
            holder.isComplete.setTextColor(Color.parseColor("#000000"));
        } else {
            holder.isComplete.setText("未完成轻量化");
            holder.modelName.setTextColor(Color.parseColor("#FF0000"));
            holder.modelSize.setTextColor(Color.parseColor("#FF0000"));
            holder.isComplete.setTextColor(Color.parseColor("#FF0000"));
        }

        //设置是否开启checkBox
        if (isShowCheckBox) {

            holder.checkBox.setVisibility(View.VISIBLE);
            //选中的添加到checkList
            if (modelEntity.isChecked()) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (mModelList == null) {//排除mModelList为null的情况，mModelList为null时mModelList.size()出错
            return 0;
        } else {
            return mModelList.size();
        }
    }
}
