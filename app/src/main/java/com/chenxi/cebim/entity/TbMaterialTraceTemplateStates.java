/**
  * Copyright 2019 bejson.com 
  */
package com.chenxi.cebim.entity;
import java.util.Date;

/**
 * Auto-generated: 2019-03-11 10:20:3
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class TbMaterialTraceTemplateStates {

    private String TempleteID;
    private String StateID;
    private String StateName;
    private String TempleteName;
    private int Sort;
    private int OperationUserID;
    private Date AddTime;
    private Date UpdateTime;
    private boolean IsInitial;
    private boolean IsForbid;
    private IdInfo idInfo;
    public void setTempleteID(String TempleteID) {
         this.TempleteID = TempleteID;
     }
     public String getTempleteID() {
         return TempleteID;
     }

    public void setStateID(String StateID) {
         this.StateID = StateID;
     }
     public String getStateID() {
         return StateID;
     }

    public void setStateName(String StateName) {
         this.StateName = StateName;
     }
     public String getStateName() {
         return StateName;
     }

    public void setTempleteName(String TempleteName) {
         this.TempleteName = TempleteName;
     }
     public String getTempleteName() {
         return TempleteName;
     }

    public void setSort(int Sort) {
         this.Sort = Sort;
     }
     public int getSort() {
         return Sort;
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

    public void setIsForbid(boolean IsForbid) {
         this.IsForbid = IsForbid;
     }
     public boolean getIsForbid() {
         return IsForbid;
     }

    public void setIdInfo(IdInfo idInfo) {
         this.idInfo = idInfo;
     }
     public IdInfo getIdInfo() {
         return idInfo;
     }

}