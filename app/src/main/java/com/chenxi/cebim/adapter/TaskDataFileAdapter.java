package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.PlayVedioActivity;
import com.chenxi.cebim.activity.PreviewFileActivity;
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

public class TaskDataFileAdapter extends RecyclerView.Adapter<TaskDataFileAdapter.ViewHolder> {

    private List<RoleInfo> mTaskDataFileList;
    private ProgressDialog progressDialog;
    private Activity mActivity;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View taskDataFileView;
        ImageView filePic;
        TextView fileName, createTime;

        public ViewHolder(View view) {
            super(view);
            taskDataFileView = view;
            filePic = view.findViewById(R.id.iv_task_data_file);
            fileName = view.findViewById(R.id.task_data_file_name);
            createTime = view.findViewById(R.id.task_data_file_create_time);
        }
    }

    /**
     * @param taskDataFileList 数据源
     */
    public TaskDataFileAdapter(Activity activity, List<RoleInfo> taskDataFileList) {

        mTaskDataFileList = taskDataFileList;
        mActivity=activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.task_data_item, viewGroup, false);
        final TaskDataFileAdapter.ViewHolder holder = new TaskDataFileAdapter.ViewHolder(view);

        holder.taskDataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();

//                if(GetFileType.fileType(mTaskDataFileList.get(position).getName()).equals("视频")||
//                        GetFileType.fileType(mTaskDataFileList.get(position).getName()).equals("音乐")){
                    progressDialog = new ProgressDialog(mActivity);
                    progressDialog.setMessage("数据加载中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();  //将进度条显示出来
                    downLoadFile(mTaskDataFileList.get(position).getID(), mTaskDataFileList.get(position).getName());
//                }else {



                    //打开和预览该文件
//                    Intent intent = new Intent(mActivity, PreviewFileActivity.class);
//                    intent.putExtra("fileName", mTaskDataFileList.get(position).getName());
//                    intent.putExtra("fileID", mTaskDataFileList.get(position).getID());
//                    intent.putExtra("projectID", SPUtils.getInstance().getInt("projectID"));
//                    mActivity.startActivity(intent);
//                }
            }
        });

        return holder;
    }

    //文件下载方法
    private void downLoadFile(final String fileIDStr, final String fileName) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/"+fileIDStr+"?isArt=true")
                .build();

        MyApplication.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showShort("打开文件失败");
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
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
                    File file = new File(AppConst.savePath, fileName);
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

                            //视频和音频直接打开第三方播放工具，文档和图片跳转到预览界面
                            if (GetFileType.fileType(fileName).equals("视频")) {
                                Intent intent=new Intent(mActivity, PlayVedioActivity.class);
                                intent.putExtra("vedioUrl",AppConst.savePath + "/" + fileName);
                                intent.putExtra("vedioname",fileName);
                                mActivity.startActivity(intent);
                            } else if (GetFileType.fileType(fileName).equals("音乐")) {

                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(AppConst.savePath + "/" + fileName);
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(mActivity, mActivity.getApplicationContext().getPackageName() + ".FileProvider", file);
                                    intent.setDataAndType(contentUri, "audio/*");
                                } else {
                                    uri = Uri.fromFile(file);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(uri, "audio/*");
                                }
                                mActivity.startActivity(intent);
                            }else{
                                //打开和预览图片和文档
                                Intent intent = new Intent(mActivity, PreviewFileActivity.class);
                                intent.putExtra("fileName", fileName);
                                intent.putExtra("fileID", fileIDStr);
                                intent.putExtra("projectID", SPUtils.getInstance().getInt("projectID"));
                                mActivity.startActivity(intent);
                            }
                        }
                    });

                } catch (Exception e) {
                    //下载出错
                    LogUtil.e("TaskDataActivity文件下载出错信息", e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                }
            }
        });
    }

    public void onBindViewHolder(TaskDataFileAdapter.ViewHolder holder, int position) {

        RoleInfo roleInfo = mTaskDataFileList.get(holder.getAdapterPosition());

        if (GetFileType.fileType(roleInfo.getName()).equals("图片")) {
            holder.filePic.setImageResource(R.drawable.picture);
        } else if (GetFileType.fileType(roleInfo.getName()).equals("文档")) {
            holder.filePic.setImageResource(R.drawable.text);
        } else if (GetFileType.fileType(roleInfo.getName()).equals("视频")) {
            holder.filePic.setImageResource(R.drawable.video);
        } else if (GetFileType.fileType(roleInfo.getName()).equals("音乐")) {
            holder.filePic.setImageResource(R.drawable.audio);
        } else {
            holder.filePic.setImageResource(R.drawable.unknow_format);
        }

//        holder.createTime.setText((String) tbFileShowmodel.getAddTime().toString().substring(0, 10));

        holder.fileName.setText(roleInfo.getName());

    }

    @Override
    public int getItemCount() {
        return mTaskDataFileList.size();
    }
}
