/**
 * Copyright 2019 bejson.com
 */
package com.chenxi.cebim.entity;
import java.util.Date;

/**
 * Auto-generated: 2019-03-21 16:59:12
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class MaterialSettings {

    private String ID;
    private int ProjectID;
    private String Name;
    private String Color;
    private int OperationUserID;
    private Date AddTime;
    private String UpdateTime;
    private int chooseId;
    private boolean isChoose;
    public void setID(String ID) {
        this.ID = ID;
    }
    public String getID() {
        return ID;
    }

    public void setProjectID(int ProjectID) {
        this.ProjectID = ProjectID;
    }
    public int getProjectID() {
        return ProjectID;
    }

    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return Name;
    }

    public void setColor(String Color) {
        this.Color = Color;
    }
    public String getColor() {
        return Color;
    }

    public void setOperationUserID(int OperationUserID) {
        this.OperationUserID = OperationUserID;
    }
    public int getOperationUserID() {
        return OperationUserID;
    }

    public void setAddTime(Date AddTime) {
        this.AddTime = AddTime;
    }
    public Date getAddTime() {
        return AddTime;
    }

    public void setUpdateTime(String UpdateTime) {
        this.UpdateTime = UpdateTime;
    }
    public String getUpdateTime() {
        return UpdateTime;
    }
    public int getChooseId() {
        return chooseId;
    }

    public void setChooseId(int chooseId) {
        this.chooseId = chooseId;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }
}