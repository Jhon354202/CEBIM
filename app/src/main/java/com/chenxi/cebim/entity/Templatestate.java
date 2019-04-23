/**
 * Copyright 2019 bejson.com
 */
package com.chenxi.cebim.entity;

import java.util.Date;
import java.util.List;

/**
 * Auto-generated: 2019-03-11 10:20:3
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Templatestate {

    private String ID;
    private int ProjectID;
    private String Name;
    private int OperationUserID;
    private Date AddTime;
    private Date UpdateTime;
    private boolean IsInitial;
    private List<TbMaterialTraceTemplateStates> tbMaterialTraceTemplateStates;

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

    public void setUpdateTime(Date UpdateTime) {
        this.UpdateTime = UpdateTime;
    }

    public Date getUpdateTime() {
        return UpdateTime;
    }

    public void setIsInitial(boolean IsInitial) {
        this.IsInitial = IsInitial;
    }

    public boolean getIsInitial() {
        return IsInitial;
    }

    public void setTbMaterialTraceTemplateStates(List<TbMaterialTraceTemplateStates> tbMaterialTraceTemplateStates) {
        this.tbMaterialTraceTemplateStates = tbMaterialTraceTemplateStates;
    }

    public List<TbMaterialTraceTemplateStates> getTbMaterialTraceTemplateStates() {
        return tbMaterialTraceTemplateStates;
    }

}