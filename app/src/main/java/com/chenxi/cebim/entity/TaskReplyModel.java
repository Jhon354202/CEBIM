package com.chenxi.cebim.entity;

import java.io.Serializable;

public class TaskReplyModel implements Serializable {

    private String ID, TaskId, Remark, Pictures, ActualUnit, ModelUnit, CreatedAt, UpdatedAt,
    CreatedUserName,UpdatedUserName;

    private int PracticalLabor, CreatedBy, UpdatedBy,CreatedUserID,UpdatedUserID;

    private double Percentage;

    public TaskReplyModel(String ID, String TaskId, String Remark, String Pictures, String ActualUnit, String ModelUnit,
                          String CreatedAt, String UpdatedAt,String CreatedUserName,String UpdatedUserName, int PracticalLabor,
                          int CreatedBy, int UpdatedBy,int CreatedUserID,int UpdatedUserID,double Percentage) {

        this.ID = ID;
        this.TaskId = TaskId;
        this.Remark = Remark;
        this.Pictures = Pictures;
        this.ActualUnit = ActualUnit;
        this.ModelUnit = ModelUnit;
        this.CreatedAt = CreatedAt;
        this.UpdatedAt = UpdatedAt;
        this.CreatedUserName = CreatedUserName;
        this.UpdatedUserName = UpdatedUserName;
        this.PracticalLabor = PracticalLabor;
        this.CreatedBy = CreatedBy;
        this.UpdatedBy = UpdatedBy;
        this.CreatedUserID = CreatedUserID;
        this.UpdatedUserID = UpdatedUserID;
        this.Percentage = Percentage;

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTaskId() {
        return TaskId;
    }

    public void setTaskId(String taskId) {
        TaskId = taskId;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getPictures() {
        return Pictures;
    }

    public void setPictures(String pictures) {
        Pictures = pictures;
    }

    public double getPercentage() {
        return Percentage;
    }

    public void setPercentage(double percentage) {
        Percentage = percentage;
    }

    public String getActualUnit() {
        return ActualUnit;
    }

    public void setActualUnit(String actualUnit) {
        ActualUnit = actualUnit;
    }

    public String getModelUnit() {
        return ModelUnit;
    }

    public void setModelUnit(String modelUnit) {
        ModelUnit = modelUnit;
    }

    public String getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public int getPracticalLabor() {
        return PracticalLabor;
    }

    public void setPracticalLabor(int practicalLaborSum) {
        PracticalLabor = practicalLaborSum;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public int getUpdatedBy() {
        return UpdatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        UpdatedBy = updatedBy;
    }

    public String getCreatedUserName() {
        return CreatedUserName;
    }

    public void setCreatedUserName(String createdUserName) {
        CreatedUserName = createdUserName;
    }

    public String getUpdatedUserName() {
        return UpdatedUserName;
    }

    public void setUpdatedUserName(String updatedUserName) {
        UpdatedUserName = updatedUserName;
    }

    public int getCreatedUserID() {
        return CreatedUserID;
    }

    public void setCreatedUserID(int createdUserID) {
        CreatedUserID = createdUserID;
    }

    public int getUpdatedUserID() {
        return UpdatedUserID;
    }

    public void setUpdatedUserID(int updatedUserID) {
        UpdatedUserID = updatedUserID;
    }
}
