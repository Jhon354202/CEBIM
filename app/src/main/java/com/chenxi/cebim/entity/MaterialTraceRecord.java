/**
 * Copyright 2019 bejson.com
 */
package com.chenxi.cebim.entity;

import java.util.Date;

/**
 * Auto-generated: 2019-04-10 9:59:31
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class MaterialTraceRecord {

    private String ID;
    private String TraceID;
    private String BeforeStateID;
    private String CurrentStateID;
    private int ProjectID;
    private String Location;
    private String Picture;
    private String ComponentUID;
    private String ComponentID;
    private String ComponentName;
    private int ModelID;
    private String Storey;
    private String Specialty;
    private String Category;
    private int OperationUserID;
    private String AddTime;
    private OperationUserInfo OperationUserInfo;
    private StateInfo StateInfo;
    private TemplateInfo TemplateInfo;
    private boolean Cz;

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setTraceID(String TraceID) {
        this.TraceID = TraceID;
    }

    public String getTraceID() {
        return TraceID;
    }

    public void setBeforeStateID(String BeforeStateID) {
        this.BeforeStateID = BeforeStateID;
    }

    public String getBeforeStateID() {
        return BeforeStateID;
    }

    public void setCurrentStateID(String CurrentStateID) {
        this.CurrentStateID = CurrentStateID;
    }

    public String getCurrentStateID() {
        return CurrentStateID;
    }

    public void setProjectID(int ProjectID) {
        this.ProjectID = ProjectID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getLocation() {
        return Location;
    }

    public void setPicture(String Picture) {
        this.Picture = Picture;
    }

    public String getPicture() {
        return Picture;
    }

    public void setComponentUID(String ComponentUID) {
        this.ComponentUID = ComponentUID;
    }

    public String getComponentUID() {
        return ComponentUID;
    }

    public void setComponentID(String ComponentID) {
        this.ComponentID = ComponentID;
    }

    public String getComponentID() {
        return ComponentID;
    }

    public void setComponentName(String ComponentName) {
        this.ComponentName = ComponentName;
    }

    public String getComponentName() {
        return ComponentName;
    }

    public void setModelID(int ModelID) {
        this.ModelID = ModelID;
    }

    public int getModelID() {
        return ModelID;
    }

    public void setStorey(String Storey) {
        this.Storey = Storey;
    }

    public String getStorey() {
        return Storey;
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

    public void setOperationUserInfo(OperationUserInfo OperationUserInfo) {
        this.OperationUserInfo = OperationUserInfo;
    }

    public OperationUserInfo getOperationUserInfo() {
        return OperationUserInfo;
    }

    public void setStateInfo(StateInfo StateInfo) {
        this.StateInfo = StateInfo;
    }

    public StateInfo getStateInfo() {
        return StateInfo;
    }

    public void setTemplateInfo(TemplateInfo TemplateInfo) {
        this.TemplateInfo = TemplateInfo;
    }

    public TemplateInfo getTemplateInfo() {
        return TemplateInfo;
    }

    public boolean isCz() {
        return Cz;
    }

    public void setCz(boolean cz) {
        Cz = cz;
    }
}