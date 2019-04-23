package com.chenxi.cebim.entity;

import java.io.Serializable;

public class MaterialTraceStateModel implements Serializable{


    private String ID,Name,Color,AddTime,UpdateTime;
    private int ProjectID,OperationUserID;


    public MaterialTraceStateModel(String ID,String Name,String Color,String AddTime,String UpdateTime,
                                   int ProjectID,int OperationUserID) {
        this.ID=ID;
        this.Name = Name;
        this.Color = Color;
        this.AddTime = AddTime;
        this.UpdateTime=UpdateTime;

        this.ProjectID=ProjectID;
        this.OperationUserID = OperationUserID;

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
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

    public int getProjectID() {
        return ProjectID;
    }

    public void setProjectID(int projectID) {
        ProjectID = projectID;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public void setOperationUserID(int operationUserID) {
        OperationUserID = operationUserID;
    }
}
