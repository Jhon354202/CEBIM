package com.chenxi.cebim.entity;

import java.util.List;

/**
 * Auto-generated: 2019-04-01 14:38:57
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Template {

    private String ID;
    private int ProjectID;
    private String Name;
    private int OperationUserID;
    private String AddTime;
    private String UpdateTime;
    private List<String> tbMaterialTraceTemplateStates;
    boolean isChoose;

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

    public void setAddTime(String AddTime) {
        this.AddTime = AddTime;
    }

    public String getAddTime() {
        return AddTime;
    }

    public void setUpdateTime(String UpdateTime) {
        this.UpdateTime = UpdateTime;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setTbMaterialTraceTemplateStates(List<String> tbMaterialTraceTemplateStates) {
        this.tbMaterialTraceTemplateStates = tbMaterialTraceTemplateStates;
    }

    public List<String> getTbMaterialTraceTemplateStates() {
        return tbMaterialTraceTemplateStates;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }
}
