package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.PlayVedioActivity;
import com.chenxi.cebim.activity.coordination.SlidePreviewPicActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.RoleInfo;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class PicShowHorizontalAdapter extends RecyclerView.Adapter<PicShowHorizontalAdapter.ViewHolder> {

    private List<RoleInfo> mPicList;
    private Activity mActivity;
    ProgressDialog progressDialog;


    static class ViewHolder extends RecyclerView.ViewHolder {
        View horizontalPicView;
        ImageView pic;

        public ViewHolder(View view) {
            super(view);
            horizontalPicView = view;
            pic = (ImageView) view.findViewById(R.id.iv_show_pic);
        }
    }

    public PicShowHorizontalAdapter(Activity activity, List<RoleInfo> picList) {
        mPicList = picList;
        mActivity = activity;
    }

    @NonNull
    @Override
    public PicShowHorizontalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontal_pic_item, viewGroup, false);
        final PicShowHorizontalAdapter.ViewHolder holder = new PicShowHorizontalAdapter.ViewHolder(view);
        holder.horizontalPicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (GetFileType.fileType(mPicList.get(position).getName()).equals("视频")) {
                    //如果本地已有视频，则直接打开，否则下载到本地再打开
                    File file = new File(AppConst.savePath + "/" + mPicList.get(position).getName());
                    if (file.exists()) {
                        Intent intent=new Intent(mActivity, PlayVedioActivity.class);
                        intent.putExtra("vedioUrl",AppConst.savePath + "/" + mPicList.get(position).getName());
                        intent.putExtra("vedioname",mPicList.get(position).getName());
                        mActivity.startActivity(intent);
                    } else {
                        downLoadFile(mPicList.get(position).getName(), mPicList.get(position).getID());
                    }

                } else if (GetFileType.fileType(mPicList.get(position).getName()).equals("图片")) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < mPicList.size(); i++) {
                        sb.append(mPicList.get(i).getName());
                        sb.append("@@@@@@");
                        sb.append(mPicList.get(i).getID());
                        if (i < mPicList.size() - 1) {
                            sb.append("@#@#@#");
                        }
                    }

                    Intent intent = new Intent(mActivity, SlidePreviewPicActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("preViewPic", sb.toString());
                    mActivity.startActivity(intent);
                }
            }
        });

        return holder;
    }

    //文件下载方法
    private void downLoadFile(String fileName, String id) {

        //加载加进度条，加班类别请求成功后消失。
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage("文件下载中...");
        progressDialog.setCancelable(true);
        progressDialog.show();  //将进度条显示出来

        Request request = new Request.Builder()
//                .url(AppConst.innerIp + "/api/" + SPUtils.getInstance().getInt("projectID") + "/EngineeringData/" + fid + "/DownLoad")
                .url(AppConst.innerIp + "/api/AnnexFile/" + id)
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("打开文件失败");
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
                    File file = new File(AppConst.savePath + "/" + fileName);
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
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }

                            //发送网址到播放界面进行播放
                            Intent intent=new Intent(mActivity, PlayVedioActivity.class);
                            intent.putExtra("vedioUrl",AppConst.savePath + "/" + fileName);
                            intent.putExtra("vedioname",fileName);
                            mActivity.startActivity(intent);

                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("SingleFileActivity文件下载出错信息", e.getMessage());
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

    @Override
    public void onBindViewHolder(@NonNull PicShowHorizontalAdapter.ViewHolder viewHolder, int position) {
        RoleInfo roleInfo = mPicList.get(position);

        String path = AppConst.innerIp + "/api/AnnexFile/" + roleInfo.getID();

        if (GetFileType.fileType(roleInfo.getName()).equals("图片")) {
            //显示图片
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.load_fail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(mActivity)
                    .load(path)
                    .apply(options)
                    .into(viewHolder.pic);
        } else if (GetFileType.fileType(roleInfo.getName()).equals("视频")) {
            Glide.with(mActivity)
                    .load(R.drawable.vedio_bitmap)
                    .into(viewHolder.pic);
        }
    }

    @Override
    public int getItemCount() {
        return mPicList.size();
    }
}
