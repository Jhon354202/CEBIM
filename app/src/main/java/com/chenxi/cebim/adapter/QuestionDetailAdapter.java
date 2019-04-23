package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.ProblemDetailIntegrationBean;
import com.chenxi.cebim.entity.QuestionCommentModel;
import com.chenxi.cebim.entity.QuestionDivideModel;
import com.chenxi.cebim.entity.QuestionModel;
import com.chenxi.cebim.entity.RoleInfo;
import com.chenxi.cebim.utils.CountDownTimerUtils;
import com.chenxi.cebim.utils.DiskCacheDirUtil;
import com.chenxi.cebim.utils.LogUtil;
import com.chenxi.cebim.utils.StringUtil;
import com.chenxi.cebim.viewholder.DetailViewHolder;
import com.chenxi.cebim.viewholder.ItemDavidingViewHolder;
import com.chenxi.cebim.viewholder.QuestionResponseHolder;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //recycleview的数据集合
    List<ProblemDetailIntegrationBean> mData;
    String savePath = DiskCacheDirUtil.getDiskCacheDir(MyApplication.getContext());//图片、视频、录音存储路径;//图片、视频、录音存储路径
    Activity mActivity;
    String ID;
    boolean playOrStop = true;//是否播音

    CountDownTimerUtils mCountDown;
    CountDownTimerUtils spangled;

    String detailAudioPath;

    int detailAudioTime;//显示的录音时长

    private List<RoleInfo> problemDetailPicList = new ArrayList<>();//装用于详情显示的图片
    private List<RoleInfo> problemResponsePicList = new ArrayList<>();//装用于回复显示的图片

    private List<DocumentModel> detailDocumentShowList = new ArrayList<>();//装用于详情显示的附件
    private List<DocumentModel> responseDocumentShowList = new ArrayList<>();//装用于回复显示的附件

    private List<RoleInfo> problemObservedUsersList = new ArrayList<>();//装关注着名单

    private DetailShowPicAdapter detailShowPicAdapter;
    private DocumentAdapter documentAdapter;

    private int detailRecLen = 0;

    //开放数据接口,让使用到adapter的地方可以操作mData
    public List<ProblemDetailIntegrationBean> getData() {
        return mData;
    }

    /**
     * @param activity
     * @param ID       ProblemDetail获取的ID
     */
    public QuestionDetailAdapter(Activity activity, String ID) {
        mData = new ArrayList<>();
        mActivity = activity;
        this.ID = ID;
    }

    //创建ViewHolder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //如果viewType是ITEM_TYPE.ITEM_TYPE_HEAD类型,则创建HeadViewHolder型viewholder
        if (viewType == ITEM_TYPE.ITEM_TYPE_DETAIL.ordinal()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_detail_item, parent, false);
            DetailViewHolder viewHolder = new DetailViewHolder(view);
            return viewHolder;
        }
        //如果viewType是ITEM_TYPE.ITEM_TYPE_TEXT类型,则创建ItemTextViewHolder型viewholder
        if (viewType == ITEM_TYPE.ITEM_TYPE_DIVIDE_LINE.ordinal()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_devide_item, parent, false);
            ItemDavidingViewHolder viewHolder = new ItemDavidingViewHolder(view);
            return viewHolder;
        }
        //如果viewType是ITEM_TYPE_DAVIDING,则创建ItemDavidingViewHolder型viewholder
        if (viewType == ITEM_TYPE.ITEM_TYPE_RESPONSE.ordinal()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_response_item, parent, false);
            QuestionResponseHolder viewHolder = new QuestionResponseHolder(view);
            return viewHolder;
        }
        return null;
    }

    //绑定数据
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //如果holder是DetailViewHolder的实例
        if (holder instanceof DetailViewHolder) {
            QuestionModel questionModel = (QuestionModel) mData.get(position).getT();

            //设置用户名
            if (questionModel.getUserName() != null && questionModel.getUserName().toString() != "") {
                ((DetailViewHolder) holder).userName.setText(questionModel.getUserName());
            }

            //设置创建日期
            if (questionModel.getDate() != null && questionModel.getDate().toString() != "") {
                ((DetailViewHolder) holder).createTime.setText(questionModel.getDate().toString().split("T")[0]);
            }

            //设置是否关注
            if (questionModel.getObservedUsers() != null && questionModel.getObservedUsers().contains("" + SPUtils.getInstance().getInt("UserID"))) {
                ((DetailViewHolder) holder).isFollow.setText("已关注");
                ((DetailViewHolder) holder).isFollow.setBackgroundResource(R.drawable.shape_follow);//设置背景
                ((DetailViewHolder) holder).isFollow.setTextColor(MyApplication.getContext().getResources().getColor(R.color.gray_text, null));//设置字体颜色
            } else {
                ((DetailViewHolder) holder).isFollow.setText("关注问题");
                ((DetailViewHolder) holder).isFollow.setBackgroundResource(R.drawable.question_detail_shape);//设置背景
                ((DetailViewHolder) holder).isFollow.setTextColor(MyApplication.getContext().getResources().getColor(R.color.white, null));//设置字体颜色
            }

            ((DetailViewHolder) holder).isFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (questionModel.getObservedUsers() == null
                            || questionModel.getObservedUsers().equals("")
                            || questionModel.getObservedUsers().equals("null")
                            || questionModel.getObservedUsers().equals("[]")) {
                        changeQuestion(questionModel, (DetailViewHolder) holder, "ObservedUsers", "true");
                    } else if (questionModel.getObservedUsers().contains(SPUtils.getInstance().getString("UserName")) &&
                            questionModel.getObservedUsers().contains(SPUtils.getInstance().getString("UserName"))) {
                        changeQuestion(questionModel, (DetailViewHolder) holder, "ObservedUsers", "false");
                    } else {
                        changeQuestion(questionModel, (DetailViewHolder) holder, "ObservedUsers", "true");
                    }

                }
            });

            //设置标题
            if (questionModel.getTitle() != null) {
                ((DetailViewHolder) holder).title.setText(questionModel.getTitle().toString());
            }

            //设置内容
            if (questionModel.getComment() != null && (!questionModel.getComment().toString().equals("null"))
                    && (!questionModel.getComment().toString().equals(""))) {
                ((DetailViewHolder) holder).content.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).content.setText(questionModel.getComment().toString());
            } else {
                ((DetailViewHolder) holder).content.setVisibility(View.GONE);
            }

            //播放录音设置
            String voiceStr = StringUtil.replaceBlank(questionModel.getVoice())
                    .replace("[", "").replace("]", "")
                    .replace("{", "").replace("}", "")
                    .replace("\"", "").replace("\"", "");

            if (voiceStr != null && (!voiceStr.equals("[]")) && (!voiceStr.equals("null")) && (!voiceStr.equals(""))) {
                String voiceName = voiceStr.split(",")[0].split(":")[1];
                String voiceId = voiceStr.split(",")[1].split(":")[1];
                //下载录音
                downVoice((DetailViewHolder) holder, voiceId, voiceName);
            } else {
                ((DetailViewHolder) holder).ll_audioPlay.setVisibility(View.GONE);
            }

            //播放和停止播放录音
            ((DetailViewHolder) holder).ll_audioPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playDetailAudio((DetailViewHolder) holder);
                }
            });

            //显示优先级
            if (questionModel.getPriority() == 0) {
                ((DetailViewHolder) holder).priority.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).priority.setText("暂缓");
            } else if (questionModel.getPriority() == 1) {
                ((DetailViewHolder) holder).priority.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).priority.setText("中等");
            } else if (questionModel.getPriority() == 2) {
                ((DetailViewHolder) holder).priority.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).priority.setText("紧急");
                ((DetailViewHolder) holder).priority.setTextColor(mActivity.getResources().getColor(R.color.tab_color_true, null));
                ((DetailViewHolder) holder).priority.setBackgroundResource(R.drawable.shape_red_question_state);
            } else {
                ((DetailViewHolder) holder).priority.setVisibility(View.GONE);
            }

            //显示类型
            if (questionModel.getCategoryName() == null ||
                    questionModel.getCategoryName().equals("") ||
                    (questionModel.getCategoryName().toString().equals("null"))) {
                ((DetailViewHolder) holder).category.setVisibility(View.GONE);
            } else {
                ((DetailViewHolder) holder).category.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).category.setText(questionModel.getCategoryName().toString());
            }

            //显示专业
            if (questionModel.getSystemTypeName() == null ||
                    questionModel.getSystemTypeName().equals("") ||
                    (questionModel.getSystemTypeName().toString().equals("null"))) {
                ((DetailViewHolder) holder).systemType.setVisibility(View.GONE);
            } else {
                ((DetailViewHolder) holder).systemType.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).systemType.setText(questionModel.getSystemTypeName().toString());
            }

            //显示状态
            if (questionModel.getState()) {
                ((DetailViewHolder) holder).isComplete.setText("完成");
                ((DetailViewHolder) holder).isCompleteBtn.setVisibility(View.GONE);
            } else {
                ((DetailViewHolder) holder).isComplete.setText("未完成");
                ((DetailViewHolder) holder).isCompleteBtn.setVisibility(View.VISIBLE);
            }

            //标记为已完成
            ((DetailViewHolder) holder).isCompleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!questionModel.getState()) {
                        changeQuestion(questionModel, (DetailViewHolder) holder, "State", "true");//是不是弄错了
                    }
                }
            });

            //显示@人员名单
            if (questionModel.getAt() == null || questionModel.getAt().equals("[]") || questionModel.getAt().equals("null")) {
                ((DetailViewHolder) holder).ll_detailAt.setVisibility(View.GONE);
            } else {
                ((DetailViewHolder) holder).ll_detailAt.setVisibility(View.VISIBLE);
                String tempstr1 = questionModel.getAt().toString().replace("\\", "");//替换掉\符号
                String tempstr2 = tempstr1.substring(1, tempstr1.length() - 1);//去掉首尾的[]
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tempstr2.split(", ").length; i++) {
                    try {
                        JSONObject jb = new JSONObject(tempstr2.split(", ")[i].toString());
                        sb.append(jb.getString("Name"));
                        sb.append("、");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ((DetailViewHolder) holder).atString.setText(sb.toString().substring(0, sb.length() - 1).toString());//会报数字越界异常，待排查
            }

            //显示截止日期
            if (questionModel.getDeadline().toString().equals("null")) {
                ((DetailViewHolder) holder).rl_deadline.setVisibility(View.GONE);
            } else {
                ((DetailViewHolder) holder).rl_deadline.setVisibility(View.VISIBLE);
                ((DetailViewHolder) holder).deadline.setText(questionModel.getDeadline().toString().split(" ")[0]);
            }

//            //显示图片
//            if (questionModel.getPictures() != null && (!questionModel.getPictures().equals("[]")) &&
//                    (!questionModel.getPictures().equals("null"))) {
//                ((DetailViewHolder) holder).picRecyclerView.setVisibility(View.VISIBLE);
//
//                //解析
//                try {
//                    String data = questionModel.getPictures().toString();
//                    JSONArray jsonArray = null;
//                    jsonArray = new JSONArray(data);
//
//                    if (problemDetailPicList != null) {
//                        problemDetailPicList.clear();
//                    }
//
//                    for (int i = 0; i < jsonArray.length(); i++) {
//
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        String Name = jsonObject.getString("Name");
//                        String ID = jsonObject.getString("ID");
//                        RoleInfo roleInfo = new RoleInfo();
//                        roleInfo.setID(ID);
//                        roleInfo.setName(Name);
//                        problemDetailPicList.add(roleInfo);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    ToastUtils.showShort("数据解析出错");
//                }
//            }
//
//            //显示视频第一屏
//            if (questionModel.getVideo() != null && (!questionModel.getVideo().equals("[]")) &&
//                    (!questionModel.getVideo().equals("null"))) {
//                ((DetailViewHolder) holder).picRecyclerView.setVisibility(View.VISIBLE);
//            }

            //            //初始化Adapter等，显示列表
//            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
//            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//            ((DetailViewHolder) holder).picRecyclerView.setLayoutManager(layoutManager);
//            PicShowHorizontalAdapter adapter = new PicShowHorizontalAdapter(mActivity, problemDetailPicList);
//            ((DetailViewHolder) holder).picRecyclerView.setAdapter(adapter);

            //防止刷新时显示
            if (problemDetailPicList != null) {
                problemDetailPicList.clear();
            }

            //如果返回的图片和视频数据为null或"[]"
            if ((questionModel.getPictures() != null && (!questionModel.getPictures().equals("null"))
                    && (!questionModel.getPictures().equals("[]")))) {
                ((DetailViewHolder) holder).picRecyclerView.setVisibility(View.VISIBLE);

                //解析图片地址，并显示解析
                try {
                    String data = questionModel.getPictures().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        RoleInfo roleInfo = new RoleInfo();
                        roleInfo.setID(ID);
                        roleInfo.setName(Name);
                        problemDetailPicList.add(roleInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if ((questionModel.getVideo() != null && (!questionModel.getVideo().equals("null"))
                    && (!questionModel.getVideo().equals("[]")))) {
                ((DetailViewHolder) holder).picRecyclerView.setVisibility(View.VISIBLE);

                //解析图片地址，并显示解析
                try {
                    String data = questionModel.getVideo().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        RoleInfo roleInfo = new RoleInfo();
                        roleInfo.setID(ID);
                        roleInfo.setName(Name);
                        problemDetailPicList.add(roleInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ((DetailViewHolder) holder).picRecyclerView.setVisibility(View.GONE);
            }

            if (problemDetailPicList != null && (!problemDetailPicList.equals("null")) && problemDetailPicList.size() > 0) {
                //初始化Adapter等，显示列表
                LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                ((DetailViewHolder) holder).picRecyclerView.setLayoutManager(layoutManager);
                PicShowHorizontalAdapter adapter = new PicShowHorizontalAdapter(mActivity, problemDetailPicList);
                ((DetailViewHolder) holder).picRecyclerView.setAdapter(adapter);
            }

            //附件列表
            LinearLayoutManager documentLayoutManager = new LinearLayoutManager(MyApplication.getContext());
            ((DetailViewHolder) holder).documentRecyclerView.setLayoutManager(documentLayoutManager);

            //显示附件列表
            if (questionModel.getDocumentIds() != null && (!questionModel.getDocumentIds().equals("[]")) &&
                    (!questionModel.getDocumentIds().equals("null"))) {

                //解析
                try {
                    String data = questionModel.getDocumentIds().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    if (detailDocumentShowList != null) {
                        detailDocumentShowList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        DocumentModel documentModel = new DocumentModel(ID, Name);
                        detailDocumentShowList.add(documentModel);
                    }
                    //显示附件列表
                    documentAdapter = new DocumentAdapter(MyApplication.getContext(), detailDocumentShowList,
                            SPUtils.getInstance().getInt("projectID"), false, "QuestionDetailAdapter", mActivity);
                    ((DetailViewHolder) holder).documentRecyclerView.setAdapter(documentAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        //如果holder是ItemDavidingViewHolder的实例
        if (holder instanceof ItemDavidingViewHolder) {
            //从数据集合中取出该项
            QuestionDivideModel questionDivideModel = (QuestionDivideModel) mData.get(position).getT();

        }

        //如果holder是QuestionResponseHolder的实例
        if (holder instanceof QuestionResponseHolder) {
            QuestionCommentModel questionCommentModel = (QuestionCommentModel) mData.get(position).getT();

//            Bitmap bmp = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.headpic);//把drawable中的图转为bitmap
//            ((QuestionResponseHolder) holder).userPic.setImageBitmap(ImageUtils.toRound(bmp));

            //设置评论者名字
            if (questionCommentModel.getUserName() != null) {
                ((QuestionResponseHolder) holder).userName.setText(questionCommentModel.getUserName());
            }

            if (questionCommentModel.getDate() != null) {
                ((QuestionResponseHolder) holder).createTime.setText(questionCommentModel.getDate().toString().replace("T", " "));
            }

            if (questionCommentModel.getComment() != null && (!questionCommentModel.getComment().toString().equals("null"))) {
                ((QuestionResponseHolder) holder).content.setText(questionCommentModel.getComment());
            }

            //如果返回的音频数据为null或"[]"
            if (questionCommentModel.getVoice() != null && (!questionCommentModel.getVoice().equals("[]"))
                    && (!questionCommentModel.getVoice().equals("null"))) {
                ((QuestionResponseHolder) holder).playAudio.setVisibility(View.VISIBLE);

                //播放录音设置
                String voiceStr = StringUtil.replaceBlank(questionCommentModel.getVoice())
                        .replace("[", "").replace("]", "")
                        .replace("{", "").replace("}", "")
                        .replace("\"", "").replace("\"", "");

                if (voiceStr != null && (!voiceStr.equals("[]")) && (!voiceStr.equals("null")) && (!voiceStr.equals(""))) {
                    String voiceName = voiceStr.split(",")[0].split(":")[1];
                    String voiceId = voiceStr.split(",")[1].split(":")[1];
                    //下载录音
                    downResponseVoice((QuestionResponseHolder) holder, voiceId, voiceName);
                } else {
                    ((QuestionResponseHolder) holder).playAudio.setVisibility(View.GONE);
                }

//                //播放和停止播放录音
//                ((QuestionResponseHolder) holder).playAudio.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        playResponseAudio((QuestionResponseHolder) holder);
//                    }
//                });

            } else {
                ((QuestionResponseHolder) holder).playAudio.setVisibility(View.GONE);
            }


            //显示图片和视频前，先确保不含数据，防止重复显示图片的问题
            if (problemResponsePicList != null) {
                problemResponsePicList.clear();
            }

            //如果返回的图片和视频数据为null或"[]"
            if ((questionCommentModel.getPictures() != null && (!questionCommentModel.getPictures().equals("null"))
                    && (!questionCommentModel.getPictures().equals("[]")))) {
                ((QuestionResponseHolder) holder).showPicRecyclerView.setVisibility(View.VISIBLE);

                //解析图片地址，并显示解析
                try {
                    String data = questionCommentModel.getPictures().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        RoleInfo roleInfo = new RoleInfo();
                        roleInfo.setID(ID);
                        roleInfo.setName(Name);
                        problemResponsePicList.add(roleInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if ((questionCommentModel.getVideo() != null && (!questionCommentModel.getVideo().equals("null"))
                    && (!questionCommentModel.getVideo().equals("[]")))) {
                ((QuestionResponseHolder) holder).showPicRecyclerView.setVisibility(View.VISIBLE);

                //解析图片地址，并显示解析
                try {
                    String data = questionCommentModel.getVideo().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        RoleInfo roleInfo = new RoleInfo();
                        roleInfo.setID(ID);
                        roleInfo.setName(Name);
                        problemResponsePicList.add(roleInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ((QuestionResponseHolder) holder).showPicRecyclerView.setVisibility(View.GONE);
            }

            if (problemResponsePicList != null && (!problemResponsePicList.equals("")) && (!problemResponsePicList.equals("null")) && problemResponsePicList.size() > 0) {
                //初始化Adapter等，显示列表
                LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                ((QuestionResponseHolder) holder).showPicRecyclerView.setLayoutManager(layoutManager);
                PicShowHorizontalAdapter adapter = new PicShowHorizontalAdapter(mActivity, problemResponsePicList);
                ((QuestionResponseHolder) holder).showPicRecyclerView.setAdapter(adapter);
            }


            //显示at
            if (questionCommentModel.getAt() != null && (!questionCommentModel.getAt().equals("[]"))
                    && (!questionCommentModel.getAt().equals("null"))&& (!questionCommentModel.getAt().equals(""))) {

                ((QuestionResponseHolder) holder).ll_response_at.setVisibility(View.VISIBLE);

                String tempstr2 = questionCommentModel.getAt().substring(1, questionCommentModel.getAt().length() - 1);//去掉首尾的[]
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tempstr2.split(", ").length; i++) {
                    try {
                        JSONObject jb = new JSONObject(tempstr2.split(", ")[i].toString());
                        sb.append(jb.getString("Name"));
                        sb.append("、");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ((QuestionResponseHolder) holder).tv_response_at.setText(sb.substring(0, sb.length() - 1).toString());//会报数字越界异常，待排查
            } else {
                ((QuestionResponseHolder) holder).ll_response_at.setVisibility(View.GONE);
            }

            //附件列表
            LinearLayoutManager documentLayoutManager = new LinearLayoutManager(MyApplication.getContext());
            ((QuestionResponseHolder) holder).showDocumentRecyclerView.setLayoutManager(documentLayoutManager);

            //显示附件列表
            if (questionCommentModel.getDocumentIds() != null && (!questionCommentModel.getDocumentIds().equals("[]")) &&
                    (!questionCommentModel.getDocumentIds().equals("null"))) {

                //解析
                try {
                    String data = questionCommentModel.getDocumentIds().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    if (responseDocumentShowList != null) {
                        responseDocumentShowList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        DocumentModel documentModel = new DocumentModel(ID, Name);
                        responseDocumentShowList.add(documentModel);
                    }
                    //显示附件列表
                    documentAdapter = new DocumentAdapter(MyApplication.getContext(), responseDocumentShowList,
                            SPUtils.getInstance().getInt("projectID"), false, "QuestionDetailAdapter", mActivity);
                    ((QuestionResponseHolder) holder).showDocumentRecyclerView.setAdapter(documentAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private DetailShowPicAdapter.onAddPicClickListener onAddPicClickListener = new DetailShowPicAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {

        }

    };

    //下载录音
    private void downVoice(DetailViewHolder holder, String voiceId, String fileName) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/" + voiceId)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("下载录音失败");
            }

            @Override
            public void onResponse(Call call, Response response) {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, fileName);
                    detailAudioPath = savePath + "/" + fileName;
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                    }
                    fos.flush();
                    // 下载完成

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //获取录音时长,报错Prepare failed.: status=0x1，待排查
                            try {
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                String str = savePath + "/" + fileName;
//                                mediaPlayer.setDataSource(savePath + "/" + fileName);
//                                mediaPlayer.prepare();

                                File file = new File(savePath + "/" + fileName);
                                FileInputStream fis = new FileInputStream(file);
                                mediaPlayer.setDataSource(fis.getFD());
                                mediaPlayer.prepare();

                                detailAudioTime = mediaPlayer.getDuration() / 1000;

                                if (detailAudioTime == 0) {
                                    holder.ll_audioPlay.setVisibility(View.GONE);
                                } else if (detailAudioTime == 60) {
                                    holder.ll_audioPlay.setVisibility(View.VISIBLE);
                                    holder.tv_audioPlay.setText("01:00");
                                } else {
                                    holder.ll_audioPlay.setVisibility(View.VISIBLE);
                                    holder.tv_audioPlay.setText("00:" + detailAudioTime / 10 + detailAudioTime % 10);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("ProblemDetail文件下载出错信息", e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

    }

    //下载回复录音
    private void downResponseVoice(QuestionResponseHolder holder, String voiceId, String fileName) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/" + voiceId)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("下载录音失败");
            }

            @Override
            public void onResponse(Call call, Response response) {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, fileName);
                    detailAudioPath = savePath + "/" + fileName;
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                    }
                    fos.flush();
                    // 下载完成

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //获取录音时长,报错Prepare failed.: status=0x1，待排查
                            try {
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                File file = new File(savePath + "/" + fileName);
                                FileInputStream fis = new FileInputStream(file);
                                mediaPlayer.setDataSource(fis.getFD());
                                mediaPlayer.prepare();

                                int responseAudioTime = mediaPlayer.getDuration() / 1000;

                                if (responseAudioTime == 0) {
                                    holder.playAudio.setVisibility(View.GONE);
                                } else if (responseAudioTime == 60) {
                                    holder.playAudio.setVisibility(View.VISIBLE);
                                    holder.tv_response_audio_play.setText("01:00");
                                } else {
                                    holder.playAudio.setVisibility(View.VISIBLE);
                                    holder.tv_response_audio_play.setText("00:" + responseAudioTime / 10 + responseAudioTime % 10);
                                }

                                //播放和停止播放录音
                                ((QuestionResponseHolder) holder).playAudio.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        playResponseAudio((QuestionResponseHolder) holder, savePath + "/" + fileName, responseAudioTime);
                                    }
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("ProblemDetail文件下载出错信息", e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

    }

    /**
     * @param field   待修改的字段
     * @param changed 改后的字段
     */
    private void changeQuestion(QuestionModel questionModel, DetailViewHolder holder, String field, String changed) {

        FormBody formBody = null;
        List<String> ObservedList = new ArrayList<>();//关注与否

        if (field.equals("State")) {
            formBody = new FormBody.Builder()
                    .add(field, changed)
                    .build();
        } else if (field.equals("ObservedUsers")) {

            if (questionModel.getObservedUsers() == null
                    || questionModel.getObservedUsers().equals("")
                    || questionModel.getObservedUsers().equals("null")
                    || questionModel.getObservedUsers().equals("[]")) {//ObservedUsers字段无数据的情况，直接添加当前用户名和用户ID
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("Name", SPUtils.getInstance().getString("UserName"));
                map.put("ID", SPUtils.getInstance().getInt("UserID"));
                ObservedList.add(JSON.toJSONString(map));
            } else if (questionModel.getObservedUsers().contains(SPUtils.getInstance().getString("UserName")) &&
                    questionModel.getObservedUsers().contains(SPUtils.getInstance().getString("UserName"))) {//ObservedUsers字段包含当前用户名和用户ID的情况，直接删除当前用户名和用户ID
                //解析ObservedUsers字符串
                try {
                    String data = questionModel.getObservedUsers().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        int ID = Integer.parseInt(jsonObject.getString("ID"));
                        if (ID != SPUtils.getInstance().getInt("UserID")) {
                            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                            map.put("Name", Name);
                            map.put("ID", "" + ID);
                            ObservedList.add(JSON.toJSONString(map));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {//ObservedUsers字段不包含当前用户名和用户ID的情况，直接在最后面添加当前用户名和用户ID

                //解析ObservedUsers字符串
                try {
                    String data = questionModel.getObservedUsers().toString();
                    JSONArray jsonArray = null;
                    jsonArray = new JSONArray(data);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String Name = jsonObject.getString("Name");
                        String ID = jsonObject.getString("ID");
                        RoleInfo roleInfo = new RoleInfo();
                        roleInfo.setID(ID);
                        roleInfo.setName(Name);

                        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                        map.put("Name", Name);
                        map.put("ID", "" + ID);
                        ObservedList.add(JSON.toJSONString(map));

                    }
                    //添加本地当前用户的Name和ID
                    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                    map.put("Name", SPUtils.getInstance().getString("UserName"));
                    map.put("ID", "" + SPUtils.getInstance().getInt("UserID"));
                    ObservedList.add(JSON.toJSONString(map));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            formBody = new FormBody.Builder()
                    .add(field, ObservedList.toString())
                    .build();
        }

        Request.Builder builder = new Request.Builder().
                url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/SynergyQuestion/" + ID)
                .put(formBody);
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
                                String responseData = response.body().string();
                                if (field.equals("ObservedUsers") && changed.equals("true")) {
                                    ToastUtils.showShort("关注成功");
                                    holder.isFollow.setText("已关注");
                                    holder.isFollow.setBackgroundResource(R.drawable.shape_follow);//设置背景
                                    holder.isFollow.setTextColor(mActivity.getResources().getColor(R.color.gray_text, null));//设置字体颜色
                                    questionModel.setObserved(true);
                                    questionModel.setObservedUsers(ObservedList.toString());
                                } else if (field.equals("ObservedUsers") && changed.equals("false")) {
                                    ToastUtils.showShort("取消关注成功");
                                    holder.isFollow.setText("关注问题");
                                    holder.isFollow.setBackgroundResource(R.drawable.question_detail_shape);//设置背景
                                    holder.isFollow.setTextColor(mActivity.getResources().getColor(R.color.white, null));//设置字体颜色
                                    questionModel.setObserved(false);
                                    questionModel.setObservedUsers(ObservedList.toString());
                                } else if (field.equals("State") && changed.equals("true")) {
                                    holder.isCompleteBtn.setVisibility(View.GONE);
                                    holder.isComplete.setText("已完成");
                                    questionModel.setState(true);
                                }

                            } else {
                                if (field.equals("Observed")) {
                                    ToastUtils.showShort("关注问题失败");
                                } else if (field.equals("State")) {
                                    ToastUtils.showShort("标记完成失败");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (field.equals("Observed")) {
                                ToastUtils.showShort("关注问题失败");
                            } else if (field.equals("State")) {
                                ToastUtils.showShort("标记完成失败");
                            }
                        }
                    }
                });
            }
        });
    }

    //播放详情中的录音
    private void playDetailAudio(DetailViewHolder holder) {
        if (playOrStop) {
            playOrStop = false;
            AudioPlayManager.getInstance().startPlay(MyApplication.getContext(), Uri.parse(detailAudioPath), new IAudioPlayListener() {
                @Override
                public void onStart(Uri var1) {
                    //开播（一般是开始语音消息动画）
                    mCountDown = new CountDownTimerUtils(holder.tv_audioPlay,
                            detailAudioTime * 1000, 1000, 0);
                    mCountDown.start();

                    spangled = new CountDownTimerUtils(holder.iv_audioPlay,
                            detailAudioTime * 1000, 500, 1);
                    spangled.start();
                }

                @Override
                public void onStop(Uri var1) {
                    //停播（一般是停止语音消息动画）
                    if (detailAudioTime == 60) {
                        holder.tv_audioPlay.setText("01:00");
                    } else {
                        holder.tv_audioPlay.setText("00:" + detailAudioTime / 10 + detailAudioTime % 10);
                    }
                    playOrStop = true;
                    if (mCountDown != null) {
                        mCountDown.onFinish();
                    }

                    if (spangled != null) {
                        spangled.onFinish();
                    }
                }

                @Override
                public void onComplete(Uri var1) {
                    //播完（一般是停止语音消息动画）
                    if (detailAudioTime == 60) {
                        holder.tv_audioPlay.setText("01:00");
                    } else {
                        holder.tv_audioPlay.setText("00:" + detailAudioTime / 10 + detailAudioTime % 10);
                    }
                    playOrStop = true;
                }
            });
        } else {
            playOrStop = true;
            AudioPlayManager.getInstance().stopPlay();
        }
    }

    //播放回复中的录音
    private void playResponseAudio(QuestionResponseHolder holder, String responseAudioPath, int responseAudioTime) {
        if (playOrStop) {
            playOrStop = false;
            AudioPlayManager.getInstance().startPlay(MyApplication.getContext(), Uri.parse(responseAudioPath), new IAudioPlayListener() {
                @Override
                public void onStart(Uri var1) {
                    //开播（一般是开始语音消息动画）
                    mCountDown = new CountDownTimerUtils(holder.tv_response_audio_play,
                            responseAudioTime * 1000, 1000, 0);
                    mCountDown.start();

                    spangled = new CountDownTimerUtils(holder.iv_response_audio_play,
                            responseAudioTime * 1000, 500, 1);
                    spangled.start();
                }

                @Override
                public void onStop(Uri var1) {
                    //停播（一般是停止语音消息动画）
                    if (responseAudioTime == 60) {
                        holder.tv_response_audio_play.setText("01:00");
                    } else {
                        holder.tv_response_audio_play.setText("00:" + responseAudioTime / 10 + responseAudioTime % 10);
                    }
                    playOrStop = true;
                    if (mCountDown != null) {
                        mCountDown.onFinish();
                    }

                    if (spangled != null) {
                        spangled.onFinish();
                    }
                }

                @Override
                public void onComplete(Uri var1) {
                    //播完（一般是停止语音消息动画）
                    if (responseAudioTime == 60) {
                        holder.tv_response_audio_play.setText("01:00");
                    } else {
                        holder.tv_response_audio_play.setText("00:" + responseAudioTime / 10 + responseAudioTime % 10);
                    }
                    playOrStop = true;
                }
            });
        } else {
            playOrStop = true;
            AudioPlayManager.getInstance().stopPlay();
        }
    }

    //返回当前位置的数据是哪种数据类型(头部,普通数据,还是分割条)
    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getDataType();
    }

    //返回数据集合的大小
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * 枚举类型,用来标识是哪一种类型的数据bean
     */
    public enum ITEM_TYPE {
        ITEM_TYPE_DETAIL,
        ITEM_TYPE_DIVIDE_LINE,
        ITEM_TYPE_RESPONSE,

    }
}
