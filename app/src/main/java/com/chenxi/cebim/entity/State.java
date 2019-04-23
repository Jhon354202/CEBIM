package com.chenxi.cebim.entity;

import java.util.Date;

public class State {
    private String templeteID, name, stateID, stateName, templeteName, templeteStateName, color;
    private int projectID, sort, operationUserID;
    private Date addTime, updateTime, stateUpdateTime;
    boolean isInitial, stateIsInitial, stateIsForbid;

    public State(String templeteID, String name, String stateID,
                 String stateName, String templeteName, String templeteStateName,
                 String color, int projectID, int sort, int operationUserID,
                 Date addTime, Date updateTime, Date stateUpdateTime, boolean isInitial,
                 boolean stateIsInitial, boolean stateIsForbid) {
        this.templeteID = templeteID;
        this.name = name;
        this.stateID = stateID;
        this.stateName = stateName;
        this.templeteName = templeteName;
        this.templeteStateName = templeteStateName;
        this.color = color;
        this.projectID = projectID;
        this.sort = sort;
        this.operationUserID = operationUserID;
        this.addTime = addTime;
        this.updateTime = updateTime;
        this.stateUpdateTime = stateUpdateTime;
        this.isInitial = isInitial;
        this.stateIsInitial = stateIsInitial;
        this.stateIsForbid = stateIsForbid;

    }

    public String getTempleteID() {
        return templeteID;
    }

    public void setTempleteID(String templeteID) {
        this.templeteID = templeteID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStateID() {
        return stateID;
    }

    public void setStateID(String stateID) {
        this.stateID = stateID;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getTempleteName() {
        return templeteName;
    }

    public void setTempleteName(String templeteName) {
        this.templeteName = templeteName;
    }

    public String getTempleteStateName() {
        return templeteStateName;
    }

    public void setTempleteStateName(String templeteStateName) {
        this.templeteStateName = templeteStateName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getOperationUserID() {
        return operationUserID;
    }

    public void setOperationUserID(int operationUserID) {
        this.operationUserID = operationUserID;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getStateUpdateTime() {
        return stateUpdateTime;
    }

    public void setStateUpdateTime(Date stateUpdateTime) {
        this.stateUpdateTime = stateUpdateTime;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public void setInitial(boolean initial) {
        isInitial = initial;
    }

    public boolean isStateIsInitial() {
        return stateIsInitial;
    }

    public void setStateIsInitial(boolean stateIsInitial) {
        this.stateIsInitial = stateIsInitial;
    }

    public boolean isStateIsForbid() {
        return stateIsForbid;
    }

    public void setStateIsForbid(boolean stateIsForbid) {
        this.stateIsForbid = stateIsForbid;
    }
}
