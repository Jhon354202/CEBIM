package com.chenxi.cebim.entity;

public class ConstructionDetails {
    private String id, name, addTime, updateTime, isInitial;
    private int projectID, operationUserID;
    private boolean isForbid;

    public ConstructionDetails(String id, int projectID, String name, int operationUserID, String addTime, String updateTime, String isInitial, boolean isForbid) {
        this.id = id;
        this.projectID = projectID;
        this.name = name;
        this.operationUserID = operationUserID;
        this.addTime = addTime;
        this.updateTime = updateTime;
        this.isInitial = isInitial;
        this.isForbid = isForbid;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsInitial() {
        return isInitial;
    }

    public void setIsInitial(String isInitial) {
        this.isInitial = isInitial;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getOperationUserID() {
        return operationUserID;
    }

    public void setOperationUserID(int operationUserID) {
        this.operationUserID = operationUserID;
    }

    public boolean isForbid() {
        return isForbid;
    }

    public void setForbid(boolean forbid) {
        isForbid = forbid;
    }
}
