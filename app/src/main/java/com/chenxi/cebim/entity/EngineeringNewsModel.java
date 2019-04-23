package com.chenxi.cebim.entity;

public class EngineeringNewsModel {

    private String MomentID,Contents,ModelID,Location,Picture,Video,Voice,Likes,CreatebyUserName,UpdataByUserName,ParentID,Son;
    private Object CreateAt,UpdataAt;
    private int ProjectID,CreateBy,UpdataBy,CreatebyUserID,UpdataByUserID;

    public EngineeringNewsModel(String MomentID,String Contents,String ModelID,String Location,String Picture,
                                String Video,String Voice,String Likes,String CreatebyUserName,String UpdataByUserName,
                                String Son,Object CreateAt,Object UpdataAt,int ProjectID,String ParentID,int CreateBy,
                                int UpdataBy,int CreatebyUserID,int UpdataByUserID){
        this.MomentID=MomentID;
        this.Contents=Contents;
        this.ModelID=ModelID;
        this.Location=Location;
        this.Picture=Picture;
        this.Video=Video;
        this.Voice=Voice;
        this.Likes=Likes;
        this.CreatebyUserName=CreatebyUserName;
        this.UpdataByUserName=UpdataByUserName;
        this.CreateAt=CreateAt;
        this.UpdataAt=UpdataAt;
        this.ProjectID=ProjectID;
        this.ParentID=ParentID;
        this.CreateBy=CreateBy;
        this.UpdataBy=UpdataBy;
        this.CreatebyUserID=CreatebyUserID;
        this.UpdataByUserID=UpdataByUserID;
        this.Son=Son;
    }

    public String getMomentID() {
        return MomentID;
    }

    public void setMomentID(String momentID) {
        MomentID = momentID;
    }

    public String getContents() {
        return Contents;
    }

    public void setContents(String contents) {
        Contents = contents;
    }

    public String getModelID() {
        return ModelID;
    }

    public void setModelID(String modelID) {
        ModelID = modelID;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public String getVideo() {
        return Video;
    }

    public void setVideo(String video) {
        Video = video;
    }

    public String getVoice() {
        return Voice;
    }

    public void setVoice(String voice) {
        Voice = voice;
    }

    public String getLikes() {
        return Likes;
    }

    public void setLikes(String likes) {
        Likes = likes;
    }

    public String getCreatebyUserName() {
        return CreatebyUserName;
    }

    public void setCreatebyUserName(String createbyUserName) {
        CreatebyUserName = createbyUserName;
    }

    public String getUpdataByUserName() {
        return UpdataByUserName;
    }

    public void setUpdataByUserName(String updataByUserName) {
        UpdataByUserName = updataByUserName;
    }

    public Object getCreateAt() {
        return CreateAt;
    }

    public void setCreateAt(Object createAt) {
        CreateAt = createAt;
    }

    public Object getUpdataAt() {
        return UpdataAt;
    }

    public void setUpdataAt(Object updataAt) {
        UpdataAt = updataAt;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setProjectID(int projectID) {
        ProjectID = projectID;
    }

    public int getCreateBy() {
        return CreateBy;
    }

    public void setCreateBy(int createBy) {
        CreateBy = createBy;
    }

    public int getUpdataBy() {
        return UpdataBy;
    }

    public void setUpdataBy(int updataBy) {
        UpdataBy = updataBy;
    }

    public int getCreatebyUserID() {
        return CreatebyUserID;
    }

    public void setCreatebyUserID(int createbyUserID) {
        CreatebyUserID = createbyUserID;
    }

    public int getUpdataByUserID() {
        return UpdataByUserID;
    }

    public void setUpdataByUserID(int updataByUserID) {
        UpdataByUserID = updataByUserID;
    }

    public String getParentID() {
        return ParentID;
    }

    public void setParentID(String parentID) {
        ParentID = parentID;
    }

    public String getSon() {
        return Son;
    }

    public void setSon(String son) {
        Son = son;
    }
}
