package com.chenxi.cebim.entity;

import java.io.Serializable;

public class ModelEntity implements Serializable{

    private int ModelID,ProjectID,FileSize,OrderNo;
    private String ModelName,DBName,OnlySign,AddTime,UpdateTime,ModelFile,OperationUserID,FileType,
            FileTypeInfo;
    private boolean IsCompleted,IsChecked;
    private Byte[] FileContent;

    public ModelEntity(int ModelID,int ProjectID,int FileSize,int OrderNo,String ModelName,String DBName,
                       String OnlySign,String AddTime,String UpdateTime,String ModelFile,String OperationUserID
                       ,String FileType,String FileTypeInfo,boolean IsCompleted,boolean IsChecked,Byte[] FileContent) {
        this.ModelID=ModelID;
        this.ProjectID = ProjectID;
        this.FileSize = FileSize;
        this.OrderNo = OrderNo;

        this.ModelName=ModelName;
        this.DBName=DBName;
        this.OnlySign = OnlySign;
        this.AddTime = AddTime;
        this.UpdateTime = UpdateTime;
        this.ModelFile = ModelFile;
        this.OperationUserID=OperationUserID;
        this.FileType = FileType;
        this.FileTypeInfo = FileTypeInfo;
        this.IsCompleted = IsCompleted;
        this.IsChecked = IsChecked;
        this.FileContent=FileContent;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setModelID(int modelID) {
        ModelID = modelID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setProjectID(int projectID) {
        ProjectID = projectID;
    }

    public int getFileSize() {
        return FileSize;
    }

    public void setFileSize(int fileSize) {
        FileSize = fileSize;
    }

    public int getOrderNo() {
        return OrderNo;
    }

    public void setOrderNo(int orderNo) {
        OrderNo = orderNo;
    }

    public String getModelName() {
        return ModelName;
    }

    public void setModelName(String modelName) {
        ModelName = modelName;
    }

    public String getDBName() {
        return DBName;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public String getOnlySign() {
        return OnlySign;
    }

    public void setOnlySign(String onlySign) {
        OnlySign = onlySign;
    }

    public String getAddTime() {
        return AddTime;
    }

    public void setAddTime(String addTime) {
        AddTime = addTime;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

    public String getModelFile() {
        return ModelFile;
    }

    public void setModelFile(String modelFile) {
        ModelFile = modelFile;
    }

    public String getOperationUserID() {
        return OperationUserID;
    }

    public void setOperationUserID(String operationUserID) {
        OperationUserID = operationUserID;
    }

    public String getFileType() {
        return FileType;
    }

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    public String getFileTypeInfo() {
        return FileTypeInfo;
    }

    public void setFileTypeInfo(String fileTypeInfo) {
        FileTypeInfo = fileTypeInfo;
    }

    public boolean isCompleted() {
        return IsCompleted;
    }

    public void setCompleted(boolean completed) {
        IsCompleted = completed;
    }

    public boolean isChecked() {
        return IsChecked;
    }

    public void setChecked(boolean checked) {
        IsChecked = checked;
    }

    public Byte[] getFileContent() {
        return FileContent;
    }

    public void setFileContent(Byte[] fileContent) {
        FileContent = fileContent;
    }
}
