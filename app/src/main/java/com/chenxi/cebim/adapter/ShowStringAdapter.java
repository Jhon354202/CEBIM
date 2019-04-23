package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.entity.QuestionCategoriesModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ShowStringAdapter extends RecyclerView.Adapter<ShowStringAdapter.ViewHolder> {

    private List<QuestionCategoriesModel> mItemList = new ArrayList<>();
    private String mActivityName;

    class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView itemName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            itemName = (TextView) view.findViewById(R.id.string_item_name);
        }
    }

    /**
     * @param list 条目名称
     */
    public ShowStringAdapter(List<QuestionCategoriesModel> list, String activityName) {

        mItemList = list;
        mActivityName=activityName;
    }

    @NonNull
    @Override
    public ShowStringAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_string_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mposition = holder.getAdapterPosition();
                String objectStr=mItemList.get(mposition).getID()+","+mItemList.get(mposition).getName()+","+
                        mItemList.get(mposition).getCreatedAt().toString()+","+mItemList.get(mposition).getCreatedBy()+","+
                        mItemList.get(mposition).getUpdatedAt().toString()+","+mItemList.get(mposition).getUpdatedBy();

                if(mActivityName.equals("CategoryActivity")){
                    EventBus.getDefault().post(new NewQuestionDelEven("CategoryActivity类型对象字符串@#@#@#"+objectStr));//返回itemName给NewQuestion
                    EventBus.getDefault().post(new NewQuestionDelEven("CategoryActivityfinish"));//提示CategoryActivity finish
                }else if(mActivityName.equals("SystemTypeActivity")){
                    EventBus.getDefault().post(new NewQuestionDelEven("SystemTypeActivity类型对象字符串@#@#@#"+objectStr));//返回itemName给NewQuestion
                    EventBus.getDefault().post(new NewQuestionDelEven("SystemTypeActivityfinish"));//提示CategoryActivity finish
                }

            }
        });

        return holder;
    }


    public void onBindViewHolder(ShowStringAdapter.ViewHolder holder, int position) {

        String str = mItemList.get(position).getName();
        holder.itemName.setText(str);

    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

}
