package com.chenxi.cebim.entity;

import org.litepal.crud.LitePalSupport;

/**
 * 常用对象
 */
public class CommonUseFileModel extends LitePalSupport {

    private int FID,ProjectID,ClassID,ParentClassID,OperationUserID,UseID;
    private String FileName,FileType,AddTime,UpdateTime,FileID;
    private Boolean IsChecked=false,IsMove=false;

    public int getUseID() {
        return UseID;
    }

    public void setUseID(int useID) {
        UseID = useID;
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

    public void setAddTime(String addTime) {
        AddTime = addTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
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
