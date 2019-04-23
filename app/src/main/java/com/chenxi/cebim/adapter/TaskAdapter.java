package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.TaskDetailActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.SynergyTaskEntity;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<SynergyTaskEntity> mAllTaskList = new ArrayList<>();
//    Context mContext;
    Activity mActivity;

    private int mProjectID;
    private String mFrom;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View taskView;
        CircularProgressBar progressBar;
        TextView taskName, plannedOwnership, assign, completeorno, principal, associatedPerson, createTime, taskProgress;

        public ViewHolder(View view) {
            super(view);
            taskView = view;
            taskName = view.findViewById(R.id.tv_task_name);
            plannedOwnership = view.findViewById(R.id.tv_task_planned_ownership);
            assign= view.findViewById(R.id.tv_task_assign);
            completeorno= view.findViewById(R.id.tv_task_completeorno);
            principal= view.findViewById(R.id.tv_principal);
            associatedPerson= view.findViewById(R.id.tv_associated_person);
            taskProgress= view.findViewById(R.id.tv_task_progress);
            progressBar= view.findViewById(R.id.cb_task_progress);
            createTime = view.findViewById(R.id.tv_task_time);
        }
    }

    /**
     * @param allTaskList 数据源
     */
    public TaskAdapter(Activity activity, List<SynergyTaskEntity> allTaskList,String from) {
        mActivity = activity;
        mAllTaskList = allTaskList;
        mFrom=from;
    }


    @NonNull
    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.task_item, viewGroup, false);

        final TaskAdapter.ViewHolder holder = new TaskAdapter.ViewHolder(view);

        holder.taskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int position = holder.getAdapterPosition();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("SynergyTaskEntity", mAllTaskList.get(position));
                intent.setClass(mActivity, TaskDetailActivity.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);

            }
        });

        return holder;
    }

    public void onBindViewHolder(TaskAdapter.ViewHolder holder, int position) {

        SynergyTaskEntity SynergyTaskEntity = mAllTaskList.get(holder.getAdapterPosition());

        if (SynergyTaskEntity.getName()!= null) {
            holder.taskName.setText(SynergyTaskEntity.getName());
        }

        if (SynergyTaskEntity.getFinishText()!= null && (!SynergyTaskEntity.getFinishText().toString().equals("null"))) {
            holder.completeorno.setText(SynergyTaskEntity.getFinishText());
        }

        //责任人
        if (SynergyTaskEntity.getUserIds()!= null && (!SynergyTaskEntity.getUserIds().equals("null"))
                && (!SynergyTaskEntity.getUserIds().equals("")) && (!SynergyTaskEntity.getUserIds().equals("[]"))) {
            //解析获取责任人名字
            try {
                String data = SynergyTaskEntity.getUserIds().toString();
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data);

                StringBuffer sb=new StringBuffer();
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sb.append(jsonObject.get("Name"));
                    if(i<jsonArray.length()-1){
                        sb.append(",");
                    }
                }
                //显示命名
                holder.principal.setText(sb.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            holder.principal.setText("暂无");
        }

        //相关人
        if (SynergyTaskEntity.getRelativeUserIds()!= null && (!SynergyTaskEntity.getRelativeUserIds().equals("null"))
                && (!SynergyTaskEntity.getRelativeUserIds().equals("")) && (!SynergyTaskEntity.getRelativeUserIds().equals("[]"))) {
            //解析获取相关人名字
            try {
                String data = SynergyTaskEntity.getRelativeUserIds().toString();
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data);

                StringBuffer sb=new StringBuffer();
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    sb.append(jsonObject.get("Name"));
                    if(i<jsonArray.length()-1){
                        sb.append(",");
                    }
                }
                //显示命名
                holder.associatedPerson.setText(sb.toString());
                holder.assign.setText("已分配");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            holder.associatedPerson.setText("暂无");
            holder.assign.setText("未分配");
        }

        //创建时间
        if (SynergyTaskEntity.getCreatedAt()!= null && (!SynergyTaskEntity.getCreatedAt().toString().equals("null"))) {
            holder.createTime.setText(SynergyTaskEntity.getCreatedAt().toString().split(" ")[0]);
        }

        if(mFrom.equals("TaskReceivedFragment")){

            holder.taskProgress.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);

            //下载获取已接收数据，并显示
            Request request = new Request.Builder()
                    .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + SynergyTaskEntity.getID() + "/Reply")
                    .build();
            MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ToastUtils.showShort("数据请求出错");
                }

                @Override
                public void onResponse(Call call, Response response) {

                    if (response.code() == 200) {
                        try {
                            String responseData = response.body().string();
                            JSONArray jsonArray = null;
                            jsonArray = new JSONArray(responseData);

                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            double Percentage = 0.00;
                            if (!jsonObject.get("Percentage").toString().equals("null")) {
                                Percentage = jsonObject.getInt("Percentage");
                            }

                            double finalPercentage = Percentage;
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.taskProgress.setText((int)finalPercentage+"%");
                                    holder.progressBar.setProgress((float)finalPercentage);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                            ToastUtils.showShort("数据请求出错");
                        }
                    } else {
                        ToastUtils.showShort("数据请求出错");
                    }
                }
            });


        }else{
            holder.taskProgress.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }

//        //优先级设置
//        if (questionModel.getPriority() == 0) {
//            holder.questionPriority.setText("暂缓");
//            holder.questionPriority.setBackgroundResource(R.drawable.shape_question_state);//设置背景
//            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.gray_text, null));//设置字体颜色
//        } else if (questionModel.getPriority() == 1) {
//            holder.questionPriority.setText("中等");
//            holder.questionPriority.setBackgroundResource(R.drawable.shape_question_state);//设置背景
//            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.gray_text, null));//设
//        } else if (questionModel.getPriority() == 2) {
//            holder.questionPriority.setText("紧急");
//            holder.questionPriority.setBackgroundResource(R.drawable.shape_red_question_state);//设置背景
//            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.tab_color_true, null));//设
//        }



    }

    //获取反馈记录
    private void getFeedBack(String ID) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyTask/" + ID + "/Reply")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("数据请求出错");
            }

            @Override
            public void onResponse(Call call, Response response) {

                if (response.code() == 200) {
                    try {
                        String responseData = response.body().string();
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(responseData);

                        JSONObject jsonObject = jsonArray.getJSONObject(0);



                        double Percentage = 0.00;
                        if (!jsonObject.get("Percentage").toString().equals("null")) {
                            Percentage = jsonObject.getInt("Percentage");
                        }

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("数据请求出错");
                    }
                } else {
                    ToastUtils.showShort("数据请求出错");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAllTaskList.size();
    }

}
