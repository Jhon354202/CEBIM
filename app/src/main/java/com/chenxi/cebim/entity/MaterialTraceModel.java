/**
 * Copyright 2019 bejson.com
 */
package com.chenxi.cebim.entity;

import java.util.Date;

/**
 * Auto-generated: 2019-03-27 8:58:41
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class MaterialTraceModel {

    private String ID;
    private int ProjectID;
    private String TempleteID;
    private String ComponentUID;
    private String ComponentName;
    private String ComponentID;
    private String StateID;
    private String Specialty;
    private String Category;
    private String Storey;
    private int OperationUserID;
    private String AddTime;
    private String UpdateTime;
    private int ModelID;
    private String TemplateName;
    private CrUserInfo CrUserInfo;
    private StateInfo StateInfo;
    private TemplateInfo TemplateInfo;
    private boolean Cz;

    public boolean isCz() {
        return Cz;
    }

    public void setCz(boolean cz) {
        Cz = cz;
    }

    public void setTemplateInfo(TemplateInfo TemplateInfo) {
        this.TemplateInfo = TemplateInfo;
    }

    public TemplateInfo getTemplateInfo() {
        return TemplateInfo;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    boolean isClick;

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    public void setProjectID(int ProjectID) {
        this.ProjectID = ProjectID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setTempleteID(String TempleteID) {
        this.TempleteID = TempleteID;
    }

    public String getTempleteID() {
        return TempleteID;
    }

    public void setComponentUID(String ComponentUID) {
        this.ComponentUID = ComponentUID;
    }

    public String getComponentUID() {
        return ComponentUID;
    }

    public void setComponentName(String ComponentName) {
        this.ComponentName = ComponentName;
    }

    public String getComponentName() {
        return ComponentName;
    }

    public void setComponentID(String ComponentID) {
        this.ComponentID = ComponentID;
    }

    public String getComponentID() {
        return ComponentID;
    }

    public void setStateID(String StateID) {
        this.StateID = StateID;
    }

    public String getStateID() {
        return StateID;
    }

    public void setSpecialty(String Specialty) {
        this.Specialty = Specialty;
    }

    public String getSpecialty() {
        return Specialty;
    }

    public void setCategory(String Category) {
        this.Category = Category;
    }

    public String getCategory() {
        return Category;
    }

    public void setStorey(String Storey) {
        this.Storey = Storey;
    }

    public String getStorey() {
        return Storey;
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

    public void setModelID(int ModelID) {
        this.ModelID = ModelID;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setTemplateName(String TemplateName) {
        this.TemplateName = TemplateName;
    }

    public String getTemplateName() {
        return TemplateName;
    }

    public void setCrUserInfo(CrUserInfo CrUserInfo) {
        this.CrUserInfo = CrUserInfo;
    }

    public CrUserInfo getCrUserInfo() {
        return CrUserInfo;
    }

    public void setStateInfo(StateInfo StateInfo) {
        this.StateInfo = StateInfo;
    }

    public StateInfo getStateInfo() {
        return StateInfo;
    }

}