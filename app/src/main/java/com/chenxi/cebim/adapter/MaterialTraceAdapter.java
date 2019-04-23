package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.material.StructuralDetailsActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.MaterialFollow;
import com.chenxi.cebim.entity.MaterialSettings;
import com.chenxi.cebim.entity.MaterialTrace;
import com.chenxi.cebim.entity.MaterialTraceModel;
import com.chenxi.cebim.utils.ACache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MaterialTraceAdapter extends RecyclerView.Adapter<MaterialTraceAdapter.ViewHolder> {

    private List<MaterialFollow> materialTraceList = new ArrayList<MaterialFollow>();
    private int mPeojectID;
    Boolean isShowCheckBox = false;
    private ACache mCache;
    private Activity activity;
    private int mProjectID;
    private List<MaterialTrace> materialTraces = new ArrayList<>();
    private List<MaterialSettings> materialSettings = new ArrayList<>();

    static class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView materialName, materialId, materialState, previousstate;
        cn.refactor.library.SmoothCheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            materialName = (TextView) view.findViewById(R.id.tv_material_name);
            materialId = (TextView) view.findViewById(R.id.tv_material_id);
            materialState = view.findViewById(R.id.tv_material_state);
            checkBox = view.findViewById(R.id.material_item_cb);
            previousstate = view.findViewById(R.id.previousstate);
        }
    }

    public MaterialTraceAdapter(List<MaterialFollow> materialTraceStateList, int peojectID, Activity activity, List<MaterialSettings> materialSettings) {
        this.materialTraceList = materialTraceStateList;
        mPeojectID = peojectID;
        this.activity = activity;
        this.materialSettings = materialSettings;
    }

    public List<MaterialFollow> getList() {

        return materialTraceList;
    }


    @NonNull
    @Override
    public MaterialTraceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.materialtrace_item, viewGroup, false);
        final MaterialTraceAdapter.ViewHolder holder = new MaterialTraceAdapter.ViewHolder(view);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                // ToastUtils.showShort("你点击了" + position);
                Intent intent = new Intent(activity, StructuralDetailsActivity.class);
                intent.putExtra("materialName", holder.materialName.getText());
                intent.putExtra("materialId", materialTraceList.get(position).getEntity_no());
                intent.putExtra("previousstate", holder.previousstate.getText());
                intent.putExtra("position", position);
                intent.putExtra("nextstate", holder.materialState.getText());
                activity.startActivityForResult(intent, 1);
            }
        });

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (holder.checkBox.isChecked()) {
                    holder.checkBox.setChecked(false);
                    materialTraceList.get(position).setChoosed(false);
                } else {
                    holder.checkBox.setChecked(true);
                    materialTraceList.get(position).setChoosed(true);
                }
            }
        });
        return holder;
    }


    public void onBindViewHolder(MaterialTraceAdapter.ViewHolder holder, int position) {
        MaterialFollow materialTraceModel = materialTraceList.get(position);
        holder.materialName.setText(materialTraceModel.getEntity_name());
        holder.materialId.setText(materialTraceModel.getEntity_no());
        mProjectID = SPUtils.getInstance().getInt("projectID", -1);
        getData(holder, mProjectID, position);
        String nextstete = materialTraceModel.getNextstate();
        if (nextstete != "" && nextstete != null) {
            holder.materialState.setText(materialTraceModel.getNextstate());
        } else {
            holder.materialState.setText(materialSettings.get(0).getName());
        }
        //holder.materialState.setText(materialTraceModel.getStateInfoName());
        if (materialTraceModel.isChoosed()) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        if (materialTraceList == null) {
            return 0;
        } else {
            return materialTraceList.size();
        }
    }

    private void getData(MaterialTraceAdapter.ViewHolder holder, int mProjectID, int position) {
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + mProjectID + "/MaterialTrace?where=ComponentUID="
                        + "\"" + materialTraceList.get(position).getPrintGUID() + "\"")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        materialTraces = JSON.parseArray(responseData, MaterialTrace.class);
                        if (materialTraces.size() == 0) {
                            holder.previousstate.setText("无跟踪记录");
                        } else {
                            //for (int i = 0; i < materialTraces.size(); i++) {
                            holder.previousstate.setText(materialTraces.get(0).getStateInfo().getName());
                            // }
                        }
                    }
                });

            }
        });
    }

    public void updateData(List<MaterialFollow> list) {
        materialTraceList.clear();
        materialTraceList.addAll(list);
        notifyDataSetChanged();
    }
}
