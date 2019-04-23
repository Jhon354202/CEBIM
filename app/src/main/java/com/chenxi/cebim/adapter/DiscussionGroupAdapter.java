package com.chenxi.cebim.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.DiscussionGroupModel;
import com.chenxi.cebim.entity.NewQuestionDelEven;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DiscussionGroupAdapter extends RecyclerView.Adapter<DiscussionGroupAdapter.ViewHolder> {

    private List<DiscussionGroupModel> mItemList = new ArrayList<>();
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
    public DiscussionGroupAdapter(List<DiscussionGroupModel> list, String activityName) {

        mItemList = list;
        mActivityName=activityName;
    }

    @NonNull
    @Override
    public DiscussionGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_string_item, viewGroup, false);
        final DiscussionGroupAdapter.ViewHolder holder = new DiscussionGroupAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mposition = holder.getAdapterPosition();
                String objectStr=mItemList.get(mposition).getID()+","+mItemList.get(mposition).getName()+","
                        +mItemList.get(mposition).getUsers()+","+
                        mItemList.get(mposition).getCreatedAt().toString()+","+mItemList.get(mposition).getCreatedBy()+","+
                        mItemList.get(mposition).getUpdatedAt().toString()+","+mItemList.get(mposition).getUpdatedBy();

                if(mActivityName.equals("DiscussionGroupActivity")){
                    EventBus.getDefault().post(new NewQuestionDelEven("DiscussionGroupActivity类型对象字符串@#@#@#"+objectStr));//返回itemName给NewQuestion
                    EventBus.getDefault().post(new NewQuestionDelEven("DiscussionGroupActivityfinish"));//提示CategoryActivity finish
                }

            }
        });

        return holder;
    }


    public void onBindViewHolder(DiscussionGroupAdapter.ViewHolder holder, int position) {

        String str = mItemList.get(position).getName();
        holder.itemName.setText(str);

    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

}
