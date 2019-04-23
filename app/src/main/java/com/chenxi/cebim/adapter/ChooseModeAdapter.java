package com.chenxi.cebim.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.ChooseModelEntity;

import java.util.List;

public class ChooseModeAdapter extends RecyclerView.Adapter<ChooseModeAdapter.ViewHolder> {

    private List<ChooseModelEntity> mChooseModelList;
    private Context mContext;
//    private String chooseName;//被选中的模型名

    private int mProjectID;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View chooseModelView;
        TextView modelName;
        ImageView chooseModelOrNo;


        public ViewHolder(View view) {
            super(view);
            chooseModelView = view;
            modelName = view.findViewById(R.id.tv_model_name);
            chooseModelOrNo = view.findViewById(R.id.tv_model_choose);
        }
    }

    /**
     * @param chooseModelList 数据源
     */
    public ChooseModeAdapter(Context context, List<ChooseModelEntity> chooseModelList) {
        mContext = context;
        this.mChooseModelList = chooseModelList;
    }

    @NonNull
    @Override
    public ChooseModeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.choose_model_item, viewGroup, false);

        final ChooseModeAdapter.ViewHolder holder = new ChooseModeAdapter.ViewHolder(view);

        holder.chooseModelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                for (int i = 0; i < mChooseModelList.size(); i++) {

                    if (i == position) {

                        if(mChooseModelList.get(position).isIschoosed()){
                            mChooseModelList.get(i).setIschoosed(false);
                        }else{
                            mChooseModelList.get(i).setIschoosed(true);
                        }

                    }
                }
                notifyDataSetChanged();
            }
        });

        return holder;
    }

    //返回模型名
    public List<ChooseModelEntity> getChooseModelList() {
        return mChooseModelList;
    }

    public void onBindViewHolder(ChooseModeAdapter.ViewHolder holder, int position) {

        holder.modelName.setText(mChooseModelList.get(position).getModelName());
        if (mChooseModelList.get(position).isIschoosed()) {
            holder.chooseModelOrNo.setVisibility(View.VISIBLE);
        } else {
            holder.chooseModelOrNo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChooseModelList.size();
    }

}
