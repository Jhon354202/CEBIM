package com.chenxi.cebim.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.entity.Notice;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<Notice> mNoticeList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View projectView;
        ImageView noticePic;
        TextView noticeName;

        public ViewHolder(View view){
            super(view);
            projectView=view;
            noticePic=(ImageView) view.findViewById(R.id.notice_pic);
            noticeName=(TextView) view.findViewById(R.id.notice_name);
        }
    }

    public NoticeAdapter(Context context,List<Notice> noticeList){
        this.mContext=context;
        this.mNoticeList=noticeList;
    }

    @NonNull
    @Override
    public NoticeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notice_item,viewGroup,false);
        final NoticeAdapter.ViewHolder holder=new NoticeAdapter.ViewHolder(view);
        holder.projectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                ToastUtils.showShort("你点击了"+mNoticeList.get(position).getNoticeName());

//                Intent intent=new Intent(view.getContext(),ProjectActivity.class);
//                intent.putExtra("projectName",project.getProjectName());
//                intent.putExtra("projectId",project.getProjectId());
//                intent.putExtra("yzProjectID",project.getYzProjectID());
//                view.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    public void onBindViewHolder(NoticeAdapter.ViewHolder holder, int position){
        Notice notice=mNoticeList.get(position);

        //这两行把图片设置成圆形再显示
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),(int)notice.getImage());//把drawable中的图转为bitmap
        holder.noticePic.setImageBitmap(ImageUtils.toRound(bmp));

//        holder.noticePic.setImageResource(notice.getImage());
        holder.noticeName.setText(notice.getNoticeName());
    }

    @Override
    public int getItemCount() {
        return mNoticeList.size();
    }
}
