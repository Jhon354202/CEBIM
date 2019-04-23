/**
  * Copyright 2019 bejson.com 
  */
package com.chenxi.cebim.entity;
import java.util.Date;

/**
 * Auto-generated: 2019-03-20 15:17:18
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class MaterialFollow {

    private String PrintGUID;
    private int PrintID;
    private int ModelID;
    private int ProjectID;
    private String MeterailID;
    private String User_Id;
    private String Entity_name;
    private String Entity_no;
    private String Entity_Uuid;
    private String Qr_code;
    private String Property;
    private Date CreatedAt;
    private int CreatedBy;
    private Date UpdatedAt;
    private String UpdatedBy;
    private String Entity_floor;
    private String Entity_domain;
    private String Entity_category;
    private String PrintHistory;
    private CreatedAtInfo CreatedAtInfo;
    private String UserInfo;
    private ProjectInfo ProjectInfo;
    private boolean isChoosed;
    private String nextstate;

    public String getNextstate() {
        return nextstate;
    }

    public void setNextstate(String nextstate) {
        this.nextstate = nextstate;
    }

    public boolean isChoosed() {
        return isChoosed;
    }

    public void setChoosed(boolean choosed) {
        isChoosed = choosed;
    }

    public void setPrintGUID(String PrintGUID) {
         this.PrintGUID = PrintGUID;
     }
     public String getPrintGUID() {
         return PrintGUID;
     }

    public void setPrintID(int PrintID) {
         this.PrintID = PrintID;
     }
     public int getPrintID() {
         return PrintID;
     }

    public void setModelID(int ModelID) {
         this.ModelID = ModelID;
     }
     public int getModelID() {
         return ModelID;
     }

    public void setProjectID(int ProjectID) {
         this.ProjectID = ProjectID;
     }
     public int getProjectID() {
         return ProjectID;
     }

    public void setMeterailID(String MeterailID) {
         this.MeterailID = MeterailID;
     }
     public String getMeterailID() {
         return MeterailID;
     }

    public void setUser_Id(String User_Id) {
         this.User_Id = User_Id;
     }
     public String getUser_Id() {
         return User_Id;
     }

    public void setEntity_name(String Entity_name) {
         this.Entity_name = Entity_name;
     }
     public String getEntity_name() {
         return Entity_name;
     }

    public void setEntity_no(String Entity_no) {
         this.Entity_no = Entity_no;
     }
     public String getEntity_no() {
         return Entity_no;
     }

    public void setEntity_Uuid(String Entity_Uuid) {
         this.Entity_Uuid = Entity_Uuid;
     }
     public String getEntity_Uuid() {
         return Entity_Uuid;
     }

    public void setQr_code(String Qr_code) {
         this.Qr_code = Qr_code;
     }
     public String getQr_code() {
         return Qr_code;
     }

    public void setProperty(String Property) {
         this.Property = Property;
     }
     public String getProperty() {
         return Property;
     }

    public void setCreatedAt(Date CreatedAt) {
         this.CreatedAt = CreatedAt;
     }
     public Date getCreatedAt() {
         return CreatedAt;
     }

    public void setCreatedBy(int CreatedBy) {
         this.CreatedBy = CreatedBy;
     }
     public int getCreatedBy() {
         return CreatedBy;
     }

    public void setUpdatedAt(Date UpdatedAt) {
         this.UpdatedAt = UpdatedAt;
     }
     public Date getUpdatedAt() {
         return UpdatedAt;
     }

    public void setUpdatedBy(String UpdatedBy) {
         this.UpdatedBy = UpdatedBy;
     }
     public String getUpdatedBy() {
         return UpdatedBy;
     }

    public void setEntity_floor(String Entity_floor) {
         this.Entity_floor = Entity_floor;
     }
     public String getEntity_floor() {
         return Entity_floor;
     }

    public void setEntity_domain(String Entity_domain) {
         this.Entity_domain = Entity_domain;
     }
     public String getEntity_domain() {
         return Entity_domain;
     }

    public void setEntity_category(String Entity_category) {
         this.Entity_category = Entity_category;
     }
     public String getEntity_category() {
         return Entity_category;
     }

    public void setPrintHistory(String PrintHistory) {
         this.PrintHistory = PrintHistory;
     }
     public String getPrintHistory() {
         return PrintHistory;
     }

    public void setCreatedAtInfo(CreatedAtInfo CreatedAtInfo) {
         this.CreatedAtInfo = CreatedAtInfo;
     }
     public CreatedAtInfo getCreatedAtInfo() {
         return CreatedAtInfo;
     }

    public void setUserInfo(String UserInfo) {
         this.UserInfo = UserInfo;
     }
     public String getUserInfo() {
         return UserInfo;
     }

    public void setProjectInfo(ProjectInfo ProjectInfo) {
         this.ProjectInfo = ProjectInfo;
     }
     public ProjectInfo getProjectInfo() {
         return ProjectInfo;
     }

}