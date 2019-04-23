package com.chenxi.cebim.adapter;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.model.WebModelActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.EngineeringNewsModel;
import com.chenxi.cebim.entity.EngineeringNewsRefreshEvenModel;
import com.chenxi.cebim.entity.ModelEntity;
import com.chenxi.cebim.entity.RecentlyModelListClearEvent;
import com.chenxi.cebim.entity.RoleInfo;
import com.chenxi.cebim.utils.ACache;
import com.chenxi.cebim.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectNewsAdapter extends RecyclerView.Adapter<ProjectNewsAdapter.ViewHolder> {

    private List<EngineeringNewsModel> mEngineeringNewsModelList;
    private ArrayList<ModelEntity> cacheList = new ArrayList<ModelEntity>();
    private ArrayList<ModelEntity> allModelList = new ArrayList<>();
    private Activity mActivity;
    private int projectID;
    private ACache mCache;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View projectNewsView;
        ImageView userPic, like, discuss, likePic;
        TextView userName, projectDescription, time, likeUser, del, publishLocation, modelName;
        LinearLayout ll_like, ll_location, ll_modelName;
        RecyclerView picRecyclerView, replayRecyclerView;

        public ViewHolder(View view) {
            super(view);
            projectNewsView = view;
            userPic = (ImageView) view.findViewById(R.id.iv_user_head);

            picRecyclerView = view.findViewById(R.id.rv_engineering_news_pic);
            replayRecyclerView = view.findViewById(R.id.engineering_discuss_recyclerView);
            del = view.findViewById(R.id.project_del);
            like = (ImageView) view.findViewById(R.id.iv_like);
            discuss = (ImageView) view.findViewById(R.id.iv_discus);
            likePic = (ImageView) view.findViewById(R.id.iv_like_pic);

            userName = (TextView) view.findViewById(R.id.project_news_username);
            projectDescription = (TextView) view.findViewById(R.id.tv_project_news);
            time = (TextView) view.findViewById(R.id.tv_tiem);
            likeUser = (TextView) view.findViewById(R.id.like_user);
            publishLocation = view.findViewById(R.id.tv_publish_location);

            ll_like = view.findViewById(R.id.ll_like);
            ll_location = view.findViewById(R.id.ll_publish_location);
            ll_modelName = view.findViewById(R.id.ll_model_name);
            modelName = view.findViewById(R.id.tv_model_name);
        }
    }

    public ProjectNewsAdapter(Activity activity, List<EngineeringNewsModel> engineeringNewsModelList) {
        mEngineeringNewsModelList = engineeringNewsModelList;
        mActivity = activity;
        projectID = SPUtils.getInstance().getInt("projectID", -1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.project_news_item, viewGroup, false);
        final ProjectNewsAdapter.ViewHolder holder = new ProjectNewsAdapter.ViewHolder(view);

        //点赞与取消赞
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                //获取所有点赞名单，看是否包含该用户ID，如果有则剔除，没有则添加,最后刷新列表

                //解析
                try {
                    String likeStr = mEngineeringNewsModelList.get(position).getLikes().toString();
                    List<String> likeDataList = new ArrayList<>();//点赞信息列表

                    if (likeStr != null && (!likeStr.equals("[]")) && (!likeStr.equals("null")) && (!likeStr.equals(""))) {

                        JSONArray jsonArray = null;
                        jsonArray = new JSONArray(mEngineeringNewsModelList.get(position).getLikes().toString());

                        if (jsonArray.toString().contains("" + SPUtils.getInstance().getInt("UserID"))) {//如果已点赞，取消该用户的赞

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String likeName = jsonObject.getString("Name");
                                String likeID = jsonObject.getString("ID");

                                if (!("" + SPUtils.getInstance().getInt("UserID"))
                                        .equals(likeID)) {
                                    //组装
                                    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                                    map.put("ID", Integer.parseInt(likeID));
                                    map.put("Name", likeName);
                                    likeDataList.add(JSON.toJSONString(map));
                                }
                            }
                            if (likeDataList.size() < 1) {
                                changeLike("", mEngineeringNewsModelList.get(position).getMomentID(), position);
                            } else {
                                changeLike(likeDataList.toString(), mEngineeringNewsModelList.get(position).getMomentID(), position);
                            }

                        } else {//如果未点赞，则添加进点赞列表
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String likeName = jsonObject.getString("Name");
                                String likeID = jsonObject.getString("ID");

                                //组装
                                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                                map.put("ID", Integer.parseInt(likeID));
                                map.put("Name", likeName);
                                likeDataList.add(JSON.toJSONString(map));
                            }

                            //把当前用户的UserID，UserName添加进点赞列表
                            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                            map.put("ID", SPUtils.getInstance().getInt("UserID"));
                            map.put("Name", SPUtils.getInstance().getString("UserName"));
                            likeDataList.add(JSON.toJSONString(map));

                            if (likeDataList.size() < 1) {
                                changeLike("", mEngineeringNewsModelList.get(position).getMomentID(), position);
                            } else {
                                changeLike(likeDataList.toString(), mEngineeringNewsModelList.get(position).getMomentID(), position);
                            }
                        }

                    } else {
                        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                        map.put("ID", SPUtils.getInstance().getInt("UserID"));
                        map.put("Name", SPUtils.getInstance().getString("UserName"));
                        likeDataList.add(JSON.toJSONString(map));
                        if (likeDataList.size() < 1) {
                            changeLike("", mEngineeringNewsModelList.get(position).getMomentID(), position);
                        } else {
                            changeLike(likeDataList.toString(), mEngineeringNewsModelList.get(position).getMomentID(), position);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //删除某个item
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage("确定删除");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delItem(position);
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();

            }
        });

        holder.discuss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                EventBus.getDefault().post(new EngineeringNewsRefreshEvenModel("评论界面:"
                        + mEngineeringNewsModelList.get(position).getMomentID()));//通知ProjectNewsAdapter刷新列表
            }
        });

        holder.projectNewsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.ll_modelName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                //跳转去打开模型
                jumpToOpenModel(position);
            }

        });

        return holder;
    }

    //跳转打开模型
    private void jumpToOpenModel(int position) {
        EngineeringNewsModel engineeringNewsModel = mEngineeringNewsModelList.get(position);

        //下载该项目下的模型列表
        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/" + projectID + "/Model")
                .build();
        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                mCache = ACache.get(mActivity);
                try {
                    String responseData = response.body().string();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(responseData);
                    StringBuffer completedSb = new StringBuffer();
                    StringBuffer completedNameSb = new StringBuffer();
                    StringBuffer unCompletedSb = new StringBuffer();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int ModelID = jsonObject.getInt("ModelID");
                        int ProjectID = jsonObject.getInt("ProjectID");
                        int FileSize = jsonObject.getInt("FileSize");

                        int OrderNo = -1;
                        if (!jsonObject.get("OrderNo").toString().equals("null")) {
                            OrderNo = jsonObject.getInt("OrderNo");
                        }

                        String ModelName = jsonObject.getString("ModelName");
                        String DBName = jsonObject.getString("DBName");
                        String OnlySign = jsonObject.getString("OnlySign");
                        String AddTime = jsonObject.get("AddTime").toString();
                        String UpdateTime = jsonObject.get("UpdateTime").toString();
                        String ModelFile = jsonObject.getString("ModelFile");
                        String OperationUserID = jsonObject.getString("OperationUserID");
                        String FileType = jsonObject.getString("FileType");
                        String FileTypeInfo = jsonObject.getString("FileTypeInfo");

                        boolean IsChecked = false;
                        boolean IsCompleted = jsonObject.getBoolean("IsCompleted");

                        Byte[] FileContent = null;

                        ModelEntity modelEntity = new ModelEntity(ModelID, ProjectID, FileSize, OrderNo, ModelName, DBName,
                                OnlySign, AddTime, UpdateTime, ModelFile, OperationUserID
                                , FileType, FileTypeInfo, IsCompleted, IsChecked, FileContent);

                        if (engineeringNewsModel.getModelID().toString().contains("" + ModelID) && IsCompleted) {//字符串中包含该模型ID，且该模型已轻量化
                            completedSb.append("" + ModelID);
                            completedSb.append(",");

                            completedNameSb.append("" + ModelName);
                            completedNameSb.append(",");

                            allModelList.add(modelEntity);

                        } else if (engineeringNewsModel.getModelID().toString().contains("" + ModelID) && !IsCompleted) {//字符串中包含该模型ID，且该模型未轻量化
                            unCompletedSb.append("" + ModelName);
                            unCompletedSb.append(",");
                        }

                    }

                    //只打开已完成轻量化的模型
                    if(completedSb!=null&&completedSb.length()>0){
                        String modelIdString = StringUtil.trimFirstAndLastChar(completedSb.toString(), ',');
                        String completedNameString = StringUtil.trimFirstAndLastChar(completedNameSb.toString(), ',');

                        Intent intent = new Intent(mActivity, WebModelActivity.class);
                        intent.putExtra("ModelID", modelIdString);
                        intent.putExtra("ProjectID", projectID);
                        intent.putExtra("ModelName", completedNameString);
                        mActivity.startActivity(intent);
                    }


//                    isTheSameElement(allModelList, true);

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //未完成的不打开，给个tost提示
                            if (unCompletedSb != null && unCompletedSb.toString().length() > 0) {
                                String unCompletedModelIdString = StringUtil.trimFirstAndLastChar(unCompletedSb.toString(), ',');
                                ToastUtils.showLong(unCompletedModelIdString + "未完成轻量化，暂时无法打开");
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //用于判断所点击的mModelList是否有和缓存中相同的，如有，不添加进cacheList，没有则添加进去
    private void isTheSameElement(ArrayList<ModelEntity> allModelList, boolean ispostEven) {
        //用于判断所点击的mModelList是否有和缓存中相同的，如有，则needToDelPostion记录下标，先删除这个元素，然后在0位置插入这个元素

        for (int i = 0; i < allModelList.size(); i++) {
            cacheList = (ArrayList<ModelEntity>) mCache.getAsObject("最近打开模型" + projectID);//缓存List

            if (cacheList != null&&cacheList.size()>0) {
                for (int j = 0; j < cacheList.size(); j++) {
                    int needToDelPostion = -1;
                    ModelEntity needToDel = null;
                    int modelId = -1;

                    int iii=allModelList.get(i).getModelID();
                    int jjj=cacheList.get(j).getModelID();
                    if (allModelList.get(i).getModelID() == cacheList.get(j).getModelID()) {
                        needToDelPostion = i;//记录需要删除的位置
                        needToDel = cacheList.get(i);//记录需要调整到最前面的对象
                    }

                    if (needToDelPostion != -1) {
                        cacheList.add(0, allModelList.get(needToDelPostion));//添加点击的list,且放在最前面
                        mCache.put("最近打开模型" + projectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
                    } else {
                        //把这个元素放在列表最前面(先删除这个元素，再在最前面插入这个元素)
                        Iterator<ModelEntity> it = cacheList.iterator();
                        while (it.hasNext()) {
                            ModelEntity modelEntity = it.next();
                            if (modelEntity.getModelID() == modelId) {
                                it.remove();
                            }
                        }

                        cacheList.add(0, needToDel);
                        mCache.put("最近打开模型" + projectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
                    }
                }

            } else {
                ModelEntity ttt=allModelList.get(i);
                cacheList=new ArrayList<>();
                cacheList.add(0,ttt);
                mCache.put("最近打开模型" + projectID, cacheList, ACache.TIME_DAY * 2);//直接缓存对象
            }

        }

        if (ispostEven) {
            EventBus.getDefault().post(new RecentlyModelListClearEvent("刷新最近列表"));
        }

    }

    //修改点赞
    private void changeLike(String likeString, String MomentID, int position) {

        FormBody formBody = new FormBody.Builder()
                .add("Likes", likeString)
                .build();

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic/" + MomentID)
                .put(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                ToastUtils.showShort("点赞或取消点赞成功");
                                //设置likeString，然后局部刷新
                                mEngineeringNewsModelList.get(position).setLikes(likeString);
                                notifyItemChanged(position);//如果likeString包含当前UserID，则点赞图片设为彩色，反之为灰色

                            } else {
                                ToastUtils.showShort("点赞或取消点赞失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("点赞或取消点赞失败");
                        }
                    }
                });
            }
        });
    }

    /**
     * 删除item方法
     *
     * @param position
     */
    private void delItem(int position) {
        String momentID = mEngineeringNewsModelList.get(position).getMomentID();
        FormBody formBody = new FormBody.Builder().build();
        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/ProjectDynamic/" + momentID)
                .delete(formBody);
        Request request = builder.build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
            }

            @Override
            public void onResponse(Call call, final Response response) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.code() == 200) {
                                ToastUtils.showShort("item删除成功");

                                //删除
                                mEngineeringNewsModelList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            } else {
                                if (response.body().string().contains("不能删除，该动态发布超过一小时，要删除请联系管理员！")) {
                                    ToastUtils.showShort("不能删除，该动态发布超过一小时，要删除请联系管理员！");
                                } else {
                                    ToastUtils.showShort("item删除失败");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtils.showShort("item删除失败");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        EngineeringNewsModel engineeringNewsModel = mEngineeringNewsModelList.get(position);
        if (engineeringNewsModel.getCreatebyUserName() != null) {
            viewHolder.userName.setText(engineeringNewsModel.getCreatebyUserName());
        }

        if (engineeringNewsModel.getContents() == null || engineeringNewsModel.getContents().equals("null")
                || engineeringNewsModel.getContents().equals("")) {
            viewHolder.projectDescription.setVisibility(View.GONE);
        } else {
            viewHolder.projectDescription.setVisibility(View.VISIBLE);
            viewHolder.projectDescription.setText(engineeringNewsModel.getContents());
        }

        //解析图片信息、显示图片列表
        List<RoleInfo> picList = new ArrayList<>();
        if (engineeringNewsModel.getPicture() != null && (!engineeringNewsModel.getPicture().equals("[]")) &&
                (!engineeringNewsModel.getPicture().equals("null"))) {
            viewHolder.picRecyclerView.setVisibility(View.VISIBLE);

            //解析图片数据
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(engineeringNewsModel.getPicture().toString());
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String picName = jsonObject.getString("Name");
                    String picID = jsonObject.getString("ID");

                    RoleInfo roleInfo = new RoleInfo();
                    roleInfo.setName(picName);
                    roleInfo.setID(picID);
                    picList.add(roleInfo);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            viewHolder.picRecyclerView.setVisibility(View.GONE);
        }

        if ((engineeringNewsModel.getVideo() != null && (!engineeringNewsModel.getVideo().equals("null"))
                && (!engineeringNewsModel.getVideo().equals("[]")))) {
            viewHolder.picRecyclerView.setVisibility(View.VISIBLE);

            //解析图片地址，并显示解析
            try {
                String data = engineeringNewsModel.getVideo().toString();
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String Name = jsonObject.getString("Name");
                    String ID = jsonObject.getString("ID");
                    RoleInfo roleInfo = new RoleInfo();
                    roleInfo.setID(ID);
                    roleInfo.setName(Name);
                    picList.add(roleInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ToastUtils.showShort("数据解析出错");
            }
        }

        if (picList != null && (!picList.equals("null")) && picList.size() > 0) {
            //初始化Adapter等，显示列表
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            viewHolder.picRecyclerView.setLayoutManager(layoutManager);
            PicShowHorizontalAdapter adapter = new PicShowHorizontalAdapter(mActivity, picList);
            viewHolder.picRecyclerView.setAdapter(adapter);
        }

        //判断是否有模型名，有则显示，无则隐藏控件
        if (engineeringNewsModel.getModelID() != null && (!engineeringNewsModel.getModelID().equals("null"))
                && (!engineeringNewsModel.getModelID().equals(""))) {
            viewHolder.ll_modelName.setVisibility(View.VISIBLE);
            //解析并显示模型名称
            //解析
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(engineeringNewsModel.getModelID().toString());
                List<RoleInfo> modelList = new ArrayList<>();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String modelName = jsonObject.getString("Name");
                    String modelID = jsonObject.getString("ID");

                    RoleInfo roleInfo = new RoleInfo();
                    roleInfo.setName(modelName);
                    roleInfo.setID(modelID);
                    modelList.add(roleInfo);

                    sb.append(modelName);
                    if (i < jsonArray.length() - 1) {
                        sb.append("、");
                    }
                }
                viewHolder.modelName.setText(sb.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            viewHolder.ll_modelName.setVisibility(View.GONE);
        }

        //判断是否有定位，有这显示，无则隐藏控件
        if (engineeringNewsModel.getLocation() != null && (!engineeringNewsModel.getLocation().equals("null"))) {
            viewHolder.ll_location.setVisibility(View.VISIBLE);
            viewHolder.publishLocation.setText(engineeringNewsModel.getLocation());
        } else {
            viewHolder.ll_location.setVisibility(View.GONE);
        }

        //设置时间
        if (engineeringNewsModel.getCreateAt() != null) {
            viewHolder.time.setVisibility(View.VISIBLE);
            viewHolder.time.setText(engineeringNewsModel.getCreateAt().toString());
        } else {
            viewHolder.time.setVisibility(View.GONE);
        }

        //设置点赞图片，如果被点赞，则显示橙色，否则为灰色
        //是否有点赞的人，有的话显示点赞人名单，否则隐藏控件
        if (engineeringNewsModel.getLikes() != null && (!engineeringNewsModel.getLikes().equals("null"))
                && (!engineeringNewsModel.getLikes().equals("[]")) && (!engineeringNewsModel.getLikes().equals(""))
                ) {
            //解析
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(engineeringNewsModel.getLikes().toString());
                List<RoleInfo> likeList = new ArrayList<>();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String likeName = jsonObject.getString("Name");
                    String likeID = jsonObject.getString("ID");

                    RoleInfo roleInfo = new RoleInfo();
                    roleInfo.setName(likeName);
                    roleInfo.setID(likeID);
                    likeList.add(roleInfo);

                    sb.append(likeName);
                    if (i < jsonArray.length() - 1) {
                        sb.append("、");
                    }
                }

                if (engineeringNewsModel.getLikes().contains("" + SPUtils.getInstance().getInt("UserID"))) {
                    viewHolder.like.setImageResource(R.drawable.like_light);
                } else {
                    viewHolder.like.setImageResource(R.drawable.like_dark);
                }
                viewHolder.ll_like.setVisibility(View.VISIBLE);
                viewHolder.likeUser.setText(sb.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            viewHolder.ll_like.setVisibility(View.GONE);
            viewHolder.like.setImageResource(R.drawable.like_dark);
        }

        //是否有回复列表，有则显示列表，无则隐藏控件
        if (engineeringNewsModel.getSon() != null && (!engineeringNewsModel.getSon().equals("null"))
                && (!engineeringNewsModel.getSon().equals("[]")) && (!engineeringNewsModel.getSon().equals(""))) {

            viewHolder.replayRecyclerView.setVisibility(View.VISIBLE);
            List<EngineeringNewsModel> engineeringNewsSonList = new ArrayList<>();
            //解析并获取son对象列表
            try {
                JSONArray jsonArray = null;
                jsonArray = new JSONArray(engineeringNewsModel.getSon().toString());
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String MomentID = jsonObject.getString("MomentID");
                    String Contens = jsonObject.getString("Contens");
                    String ModelID = jsonObject.getString("ModelID");
                    String Location = jsonObject.getString("Location");
                    String Picture = jsonObject.getString("Picture");
                    String Video = jsonObject.getString("Video");
                    String Voice = jsonObject.getString("Voice");
                    String Likes = jsonObject.getString("Likes");

                    Object CreateAt = jsonObject.get("CreateAt");
                    Object UpdataAt = jsonObject.get("UpdataAt");

                    int ProjectID = jsonObject.getInt("ProjectID");
                    String ParentID = jsonObject.getString("ParentID");

                    String CreateByUserName;
                    int CreateByUserID;
                    com.alibaba.fastjson.JSONObject createbyInfo = com.alibaba.fastjson.JSONObject.
                            parseObject(jsonObject.get("CreatebyInfo").toString());
                    if (createbyInfo == null || createbyInfo.equals("null")) {
                        CreateByUserName = "";
                        CreateByUserID = -1;
                    } else {
                        CreateByUserName = createbyInfo.get("UserName").toString();
                        CreateByUserID = Integer.parseInt(createbyInfo.get("UserID").toString());
                    }

                    String UpdataByUserName = null;
                    int UpdataByUserID = -1;
                    if (!jsonObject.get("UpdataByInfo").toString().equals("null")) {
                        com.alibaba.fastjson.JSONObject updataByInfo = com.alibaba.fastjson.JSONObject.
                                parseObject(jsonObject.get("UpdataByInfo").toString());
                        UpdataByUserName = updataByInfo.get("UserName").toString();
                        UpdataByUserID = Integer.parseInt(updataByInfo.get("UserID").toString());
                    }

                    int CreateBy;
                    if (jsonObject.get("CreateBy").toString().equals("null")) {
                        CreateBy = -1;
                    } else {
                        CreateBy = jsonObject.getInt("CreateBy");
                    }

                    int UpdataBy;
                    if (jsonObject.get("UpdataBy").toString().equals("null")) {
                        UpdataBy = -1;
                    } else {
                        UpdataBy = jsonObject.getInt("UpdataBy");
                    }

                    String Son = null;

                    EngineeringNewsModel sonModel = new EngineeringNewsModel(MomentID, Contens, ModelID,
                            Location, Picture, Video, Voice, Likes, CreateByUserName, UpdataByUserName, Son, CreateAt, UpdataAt,
                            ProjectID, ParentID, CreateBy, UpdataBy, CreateByUserID, UpdataByUserID);
                    engineeringNewsSonList.add(sonModel);
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
                        viewHolder.replayRecyclerView.setLayoutManager(layoutManager);
                        EngineeringNewsReplyAdapter adapter = new EngineeringNewsReplyAdapter(mActivity, engineeringNewsSonList, position);
                        viewHolder.replayRecyclerView.setAdapter(adapter);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                ToastUtils.showShort("数据解析出错");
            }

        } else {
            viewHolder.replayRecyclerView.setVisibility(View.GONE);
        }

        viewHolder.discuss.setImageResource(R.drawable.discuss);
        viewHolder.likePic.setImageResource(R.drawable.like_dark);
    }

    @Override
    public int getItemCount() {
        return mEngineeringNewsModelList.size();
    }

    /**
     * 删除回复时的刷新
     *
     * @param parentPosition 回复的父position
     * @param tempList       删除的MomentID
     */
    public void refresh(int parentPosition, List<EngineeringNewsModel> tempList) {
        notifyItemChanged(parentPosition);
    }

}
