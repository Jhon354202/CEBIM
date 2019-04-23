package com.chenxi.cebim.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.PlayVedioActivity;
import com.chenxi.cebim.activity.PreviewFileActivity;
import com.chenxi.cebim.appConst.AppConst;
import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.entity.CommonEven;
import com.chenxi.cebim.entity.DocumentModel;
import com.chenxi.cebim.entity.NewQuestionDelEven;
import com.chenxi.cebim.utils.GetFileType;
import com.chenxi.cebim.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<DocumentModel> mDataFileList;

    private boolean mIsShowDel;
    private String mFrom;
    Activity mActivity;

    String savePath;//图片、视频、录音存储路径

    private ProgressDialog progressDialog;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View dataFileView;
        ImageView filePic, del;
        TextView fileName;

        public ViewHolder(View view) {
            super(view);
            dataFileView = view;
            filePic = (ImageView) view.findViewById(R.id.iv_data_file);
            fileName = (TextView) view.findViewById(R.id.document_file_name);
            del = (ImageView) view.findViewById(R.id.document_file_del);

        }
    }

    /**
     * @param dataFileList 数据源
     * @param projectID    工程id
     */
    public DocumentAdapter(Context context, List<DocumentModel> dataFileList, int projectID, boolean isShowDel, String from, Activity activity) {

        mDataFileList = dataFileList;
        mIsShowDel = isShowDel;//是否显示删除符号
        mFrom = from;//哪个界面在用这个DocumentAdapter
        mActivity = activity;
    }


    @NonNull
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.document_file_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.dataFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (mFrom.equals("NewQuestion")) {
                    //发送EvenBus回NewQuestion下载文件之后跳转到预览界面
                    EventBus.getDefault().post(new NewQuestionDelEven("预览:" + mDataFileList.get(position).getFileString() +
                            "@@@" + mDataFileList.get(position).getFileNama()));
                } else if (mFrom.equals("QuestionDetailAdapter") || mFrom.equals("QuestionResponseActivity")
                        || mFrom.equals("PublishTaskActivity") || mFrom.equals("TaskFeedBackActivity")) {

                    progressDialog = new ProgressDialog(mActivity);
                    progressDialog.setMessage("数据加载中...");
                    progressDialog.setCancelable(true);
                    progressDialog.show();  //将进度条显示出来
                    downLoadFile(mDataFileList.get(position).getFileString(), mDataFileList.get(position).getFileNama());

                }

            }
        });

        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String str = mFrom;
                int position = holder.getAdapterPosition();
                if (mFrom.equals("NewQuestion")) {
                    //发送EvenBus回NewQuestion来删除和刷新列表
                    EventBus.getDefault().post(new NewQuestionDelEven("删除:" + mDataFileList.get(position).getFileString()));
                } else if (mFrom.equals("QuestionResponseActivity")) {
                    //发送EvenBus回QuestionResponseActivity来删除和刷新列表
                    EventBus.getDefault().post(new NewQuestionDelEven("QuestionResponseActivity删除:" + mDataFileList.get(position).getFileString()));
                } else if (mFrom.equals("PublishTaskActivity")) {
                    //发送EvenBus回PublishTaskActivity来删除和刷新列表
                    EventBus.getDefault().post(new CommonEven("PublishTaskActivity删除:" + mDataFileList.get(position).getFileString()));
                } else if (mFrom.equals("TaskFeedBackActivity")) {
                    //发送EvenBus回TaskFeedBackActivity来删除和刷新列表
                    EventBus.getDefault().post(new CommonEven("TaskFeedBackActivity删除:" + mDataFileList.get(position).getFileString()));
                }
            }
        });

        return holder;
    }

    public void onBindViewHolder(DocumentAdapter.ViewHolder holder, int position) {

        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        DocumentModel documentModel = mDataFileList.get(holder.getAdapterPosition());

        if (GetFileType.fileType(documentModel.getFileNama()).equals("图片")) {
            holder.filePic.setImageResource(R.drawable.picture);
        } else if (GetFileType.fileType(documentModel.getFileNama()).equals("文档")) {
            holder.filePic.setImageResource(R.drawable.text);
        } else if (GetFileType.fileType(documentModel.getFileNama()).equals("视频")) {
            holder.filePic.setImageResource(R.drawable.video);
        } else if (GetFileType.fileType(documentModel.getFileNama()).equals("音乐")) {
            holder.filePic.setImageResource(R.drawable.audio);
        } else {
            holder.filePic.setImageResource(R.drawable.unknow_format);
        }

        if (mIsShowDel) {
            holder.del.setVisibility(View.VISIBLE);
        } else {
            holder.del.setVisibility(View.GONE);
        }

        holder.fileName.setText(documentModel.getFileNama());

    }

    @Override
    public int getItemCount() {
        return mDataFileList.size();
    }

    //文件下载方法
    private void downLoadFile(final String fileIDStr, final String fileName) {

        Request request = new Request.Builder()
                .url(AppConst.innerIp + "/api/AnnexFile/" + fileIDStr + "?isArt=true")
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
                                Intent intent = new Intent(mActivity, PlayVedioActivity.class);
                                intent.putExtra("vedioUrl", AppConst.savePath + "/" + fileName);
                                intent.putExtra("vedioname", fileName);
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
                            } else {
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
                    LogUtil.e("文件下载出错信息", e.getMessage());
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

}
