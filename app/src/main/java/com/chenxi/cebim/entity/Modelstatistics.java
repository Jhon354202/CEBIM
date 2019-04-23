/**
 * Copyright 2019 bejson.com
 */
package com.chenxi.cebim.entity;

import java.util.Date;

/**
 * Auto-generated: 2019-03-28 11:14:32
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Modelstatistics {

    private int ModelID;
    private int ProjectID;
    private String ModelName;
    private long FileSize;
    private String DBName;
    private String OrderNo;
    private String OnlySign;
    private Date AddTime;
    private Date UpdateTime;
    private String ModelFile;
    private int OperationUserID;
    private String FileType;
    private String FileTypeInfo;
    private boolean isCompleted;
    private boolean isEdit;
    private boolean isChoose;

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public void setModelID(int ModelID) {
        this.ModelID = ModelID;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setProjectID(int ProjectID) {
        this.ProjectID = ProjectID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setModelName(String ModelName) {
        this.ModelName = ModelName;
    }

    public String getModelName() {
        return ModelName;
    }

    public void setFileSize(long FileSize) {
        this.FileSize = FileSize;
    }

    public long getFileSize() {
        return FileSize;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public String getDBName() {
        return DBName;
    }

    public void setOrderNo(String OrderNo) {
        this.OrderNo = OrderNo;
    }

    public String getOrderNo() {
        return OrderNo;
    }

    public void setOnlySign(String OnlySign) {
        this.OnlySign = OnlySign;
    }

    public String getOnlySign() {
        return OnlySign;
    }

    public void setAddTime(Date AddTime) {
        this.AddTime = AddTime;
    }

    public Date getAddTime() {
        return AddTime;
    }

    public void setUpdateTime(Date UpdateTime) {
        this.UpdateTime = UpdateTime;
    }

    public Date getUpdateTime() {
        return UpdateTime;
    }

    public void setModelFile(String ModelFile) {
        this.ModelFile = ModelFile;
    }

    public String getModelFile() {
        return ModelFile;
    }

    public void setOperationUserID(int OperationUserID) {
        this.OperationUserID = OperationUserID;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public void setFileType(String FileType) {
        this.FileType = FileType;
    }

    public String getFileType() {
        return FileType;
    }

    public void setFileTypeInfo(String FileTypeInfo) {
        this.FileTypeInfo = FileTypeInfo;
    }

    public String getFileTypeInfo() {
        return FileTypeInfo;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void toggle() {
        this.isChoose = !this.isChoose;
    }
}