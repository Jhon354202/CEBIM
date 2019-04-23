package com.chenxi.cebim.entity;

import android.graphics.Bitmap;

/**
 * 工程动态界面的对象
 */
public class ProjectNews {

    private Bitmap userPic,projecPic1,projectPic2,projectPic3;
    private String userName,projectDescription,tiem,likeUser;
    private int del,like,discuss,likePic;

    public ProjectNews(Bitmap userPic,String userName,int del,String projectDescription,
                       Bitmap projecPic1,Bitmap projectPic2,Bitmap projectPic3,String tiem,
                       int like,int discuss,int likePic,String likeUser){
        this.userPic=userPic;//用户头像
        this.userName=userName;//用户名
        this.del=del;//删除按钮
        this.projectDescription=projectDescription;//工程描述
        this.projecPic1=projecPic1;//工程图片1
        this.projectPic2=projectPic2;//工程图片2
        this.projectPic3=projectPic3;//工程图片3
        this.tiem=tiem;//时间
        this.like=like;//点赞按钮
        this.discuss=discuss;//讨论按钮
        this.likePic=likePic;//
        this.likeUser=likeUser;//
    }

    public Bitmap getUserPic() {
        return userPic;
    }

    public Bitmap getProjecPic1() {
        return projecPic1;
    }

    public Bitmap getProjectPic2() {
        return projectPic2;
    }

    public Bitmap getProjectPic3() {
        return projectPic3;
    }

    public String getUserName() {
        return userName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public String getTiem() {
        return tiem;
    }

    public String getLikeUser() {
        return likeUser;
    }

    public int getDel() {
        return del;
    }

    public int getLike() {
        return like;
    }

    public int getDiscuss() {
        return discuss;
    }

    public int getLikePic() {
        return likePic;
    }
}
