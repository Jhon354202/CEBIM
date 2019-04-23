package com.chenxi.cebim.entity;

import java.io.Serializable;

//需要展示在列表的TbFile数据对象
public class TbFileShowmodel implements Serializable {
   private int FID,ProjectID,ClassID,ParentClassID,OperationUserID;
   private String FileName,FileType,FileID;
   private Object AddTime,UpdateTime;
   private Boolean IsChecked=false,IsMove=false;

   public TbFileShowmodel(int FID,int ProjectID,int ClassID,int ParentClassID,int OperationUserID,
                          String FileName,String FileType,String FileID,Object AddTime,Object UpdateTime,Boolean isChecked,Boolean IsMove){
       this.FID=FID;
       this.ProjectID=ProjectID;
       this.ClassID=ClassID;
       this.ParentClassID=ParentClassID;
       this.OperationUserID=OperationUserID;
       this.FileName=FileName;
       this.FileType=FileType;
       this.FileID=FileID;
       this.AddTime=AddTime;
       this.UpdateTime=UpdateTime;
       this.IsChecked=isChecked;
       this.IsMove=IsMove;//是否需要移动
   }

    public int getFID() {
        return FID;
    }

    public void setFID(int FID) {
        this.FID = FID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setProjectID(int projectID) {
        ProjectID = projectID;
    }

    public int getClassID() {
        return ClassID;
    }

    public void setClassID(int classID) {
        ClassID = classID;
    }

    public int getParentClassID() {
        return ParentClassID;
    }

    public void setParentClassID(int parentClassID) {
        ParentClassID = parentClassID;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public void setOperationUserID(int operationUserID) {
        OperationUserID = operationUserID;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getFileType() {
        return FileType;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    public Object getAddTime() {
        return AddTime;
    }

    public void setAddTime(Object addTime) {
        AddTime = addTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(Object updateTime) {
        UpdateTime = updateTime;
    }

    public Boolean getChecked() {
        return IsChecked;
    }

    public void setChecked(Boolean checked) {
        IsChecked = checked;
    }

    public Boolean getMove() {
        return IsMove;
    }

    public void setMove(Boolean move) {
        IsMove = move;
    }

    public String getFileID() {
        return FileID;
    }

    public void setFileID(String fileID) {
        FileID = fileID;
    }
}
