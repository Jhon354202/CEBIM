package com.chenxi.cebim.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chenxi.cebim.R;
import com.chenxi.cebim.activity.ProjectActivity;
import com.chenxi.cebim.entity.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> mProjectList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View projectView;
        ImageView projectPic;
        TextView projectName;
        TextView projectId;

        public ViewHolder(View view){
            super(view);
            projectView=view;
            projectPic=(ImageView) view.findViewById(R.id.iv_project);
            projectName=(TextView) view.findViewById(R.id.project_name);
            projectId=(TextView) view.findViewById(R.id.project_id);
        }
    }

    public ProjectAdapter(List<Project> projectList){
        mProjectList=projectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.project_item,viewGroup,false);
        final ViewHolder holder=new ViewHolder(view);
        holder.projectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                Project project=mProjectList.get(position);

                SPUtils.getInstance().put("projectID",project.getProjectId());
                SPUtils.getInstance().put("projectName",project.getProjectName());

                SPUtils.getInstance().remove(project.getProjectId() + "新建问题");//移除持久化附件数据（AddDocumentActivity等类中有）

                Intent intent=new Intent(view.getContext(),ProjectActivity.class);
                intent.putExtra("projectName",project.getProjectName());
                intent.putExtra("projectId",project.getProjectId());
                view.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    public void onBindViewHolder(ViewHolder holder,int position){
        Project project=mProjectList.get(position);
        holder.projectPic.setImageResource(R.drawable.building);
        holder.projectName.setText(project.getProjectName());
//        holder.projectId.setText(""+project.getProjectId());
    }

    @Override
    public int getItemCount() {
        return mProjectList.size();
    }
}
