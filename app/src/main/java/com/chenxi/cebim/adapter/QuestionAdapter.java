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
import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.coordination.ProblemDetail;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.entity.ReadUsersModel;
import com.chenxi.cebim.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<QuestionModel> mDataQuestionList = new ArrayList<>();
    Context mContext;

    private int mProjectID;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        ImageView userPic, picOrVideo;
        TextView userName, createTime, questionTitle, questionPriority, questiontype, questionsubject;

        public ViewHolder(View view) {
            super(view);
            dataFileView = view;
            userPic = view.findViewById(R.id.iv_question_user_pic);
            userName = view.findViewById(R.id.tv_question_username);
            createTime = view.findViewById(R.id.tv_question_time);
            questionTitle = view.findViewById(R.id.tv_question_title);
            questionPriority = view.findViewById(R.id.tv_question_priority);
            questiontype = view.findViewById(R.id.tv_question_type);
            questionsubject = view.findViewById(R.id.tv_question_subject);
            picOrVideo = view.findViewById(R.id.iv_pic_video);
        }
    }

    /**
     * @param dataQuestionList 数据源
     */
    public QuestionAdapter(Context context, List<QuestionModel> dataQuestionList) {
        mContext = context;
        mDataQuestionList = dataQuestionList;
        System.out.println();
    }


    @NonNull
    @Override
    public QuestionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.question_item, viewGroup, false);

        final QuestionAdapter.ViewHolder holder = new QuestionAdapter.ViewHolder(view);

        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断是否已读，如之前为未读，则标记
                List<String> readUsersList = new ArrayList<>();
                List<ReadUsersModel> readUsersModelList = new ArrayList<>();
                if (mDataQuestionList.get(position).getReadUsers() == null ||
                        mDataQuestionList.get(position).getReadUsers().equals("[]") ||
                        mDataQuestionList.get(position).getReadUsers().equals("") ||
                        mDataQuestionList.get(position).getReadUsers().equals("null")) {

                    //标记ReadUsers字段
                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                    map.put("Name", SPUtils.getInstance().getString("UserName"));
                    map.put("ID", SPUtils.getInstance().getInt("UserID"));
                    readUsersList.add(JSON.toJSONString(map));

                    changeQuestion(readUsersList.toString(),mDataQuestionList.get(position).getID());//修改已读列表

                } else if (!(mDataQuestionList.get(position).getReadUsers().contains(""+SPUtils.getInstance().getInt("UserID")))) {
                    //解析Json
                    try {
                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(mDataQuestionList.get(position).getReadUsers());
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String Name = jsonObject.getString("Name");
                            int ID = jsonObject.getInt("ID");

                            //标记ReadUsers字段
                            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                            map.put("Name", Name);
                            map.put("ID", ID);
                            readUsersList.add(JSON.toJSONString(map));
                        }
                        //标记ReadUsers字段
                        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                        map.put("Name", SPUtils.getInstance().getString("UserName"));
                        map.put("ID", SPUtils.getInstance().getInt("UserID"));
                        readUsersList.add(JSON.toJSONString(map));
                        changeQuestion(readUsersList.toString(),mDataQuestionList.get(position).getID());//修改已读列表

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                int position = holder.getAdapterPosition();
                Intent intent = new Intent(view.getContext(), ProblemDetail.class);
                intent.putExtra("problemDetailID", mDataQuestionList.get(position).getID());
                view.getContext().startActivity(intent);
            }
        });

        return holder;
    }

    /**
     * @param readUsersString   待修改的字段
     */
    private void changeQuestion(String readUsersString,String ID) {

        FormBody formBody = new FormBody.Builder()
                .add("ReadUsers", readUsersString)
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion/" + ID)
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.i("标记已阅读",e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if(response.code()==200){
                    LogUtil.i("标记已阅读",response.message());
                }else{
                    LogUtil.i("标记已阅读",response.message());
                }
            }
        });
    }


    public void onBindViewHolder(QuestionAdapter.ViewHolder holder, int position) {

        QuestionModel questionModel = mDataQuestionList.get(holder.getAdapterPosition());

//        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.headpic);//把drawable中的图转为bitmap
//        holder.userPic.setImageBitmap(ImageUtils.toRound(bmp));

        if (questionModel.getUserName() != null) {
            holder.userName.setText(questionModel.getUserName());
        }

        if (questionModel.getDate() != null) {
            String s = questionModel.getDate().toString();
            holder.createTime.setText(questionModel.getDate().toString().split(" ")[0]);
        }

        if (questionModel.getTitle() != null && (!questionModel.getTitle().toString().equals("null"))) {
            holder.questionTitle.setText(questionModel.getTitle());
        }

        //如果有图片则显示图片，如果没有图片有视频，着显示视频。
        if (questionModel.getPictures() != null && (!questionModel.getPictures().equals("null"))
                && (!questionModel.getPictures().equals("")) && (!questionModel.getPictures().equals("[]"))) {
            holder.picOrVideo.setVisibility(View.VISIBLE);

            //解析
            try {
                String data = questionModel.getPictures().toString();
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data);

                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String ID = jsonObject.getString("ID");
                Glide.with(MyApplication.getContext())
                        .load(AppConst.innerIp + "/api/AnnexFile/" + ID)
                        .into(holder.picOrVideo);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if ((questionModel.getPictures() == null || questionModel.getPictures().equals("null")
                || questionModel.getPictures().equals("") || questionModel.getPictures().equals("[]"))
                && questionModel.getVideo() != null
                && (!questionModel.getVideo().equals("[]")) && (!questionModel.getVideo().equals("null"))) {
            holder.picOrVideo.setVisibility(View.VISIBLE);
            Glide.with(MyApplication.getContext())
                    .load(R.drawable.vedio_bitmap)
                    .into(holder.picOrVideo);
        } else {
            holder.picOrVideo.setVisibility(View.GONE);
        }

//        questionPriority = view.findViewById(R.id.tv_question_priority);
//        questiontype = view.findViewById(R.id.tv_question_type);
//        questionsubject = view.findViewById(R.id.tv_question_building);

        //优先级设置
        if (questionModel.getPriority() == 0) {
            holder.questionPriority.setText("暂缓");
            holder.questionPriority.setBackgroundResource(R.drawable.shape_question_state);//设置背景
            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.gray_text, null));//设置字体颜色
        } else if (questionModel.getPriority() == 1) {
            holder.questionPriority.setText("中等");
            holder.questionPriority.setBackgroundResource(R.drawable.shape_question_state);//设置背景
            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.gray_text, null));//设
        } else if (questionModel.getPriority() == 2) {
            holder.questionPriority.setText("紧急");
            holder.questionPriority.setBackgroundResource(R.drawable.shape_red_question_state);//设置背景
            holder.questionPriority.setTextColor(mContext.getResources().getColor(R.color.tab_color_true, null));//设
        }

        //类型设置
        if (questionModel.getCategoryName() == null || questionModel.getCategoryName().equals("null") ||
                questionModel.getCategoryName().equals("")) {
            holder.questiontype.setVisibility(View.GONE);
        } else {
            holder.questiontype.setVisibility(View.VISIBLE);
            holder.questiontype.setText(questionModel.getCategoryName());
        }

        //专业设置
        if (questionModel.getSystemTypeName() == null || questionModel.getSystemTypeName().equals("null") ||
                questionModel.getSystemTypeName().equals("")) {
            holder.questionsubject.setVisibility(View.GONE);
        } else {
            holder.questionsubject.setVisibility(View.VISIBLE);
            holder.questionsubject.setText(questionModel.getSystemTypeName());
        }

    }

    @Override
    public int getItemCount() {
        return mDataQuestionList.size();
    }

}
