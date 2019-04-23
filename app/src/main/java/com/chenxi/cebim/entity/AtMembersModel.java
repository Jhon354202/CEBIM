package com.chenxi.cebim.entity;

import java.io.Serializable;

public class AtMembersModel implements Serializable{
    private int ProjectID, UserID, AddUserID, UpdateUserID, UserInfoID;
    private String RoleID, UserName, RoleName, RoleInfoID;
    private Object InTime, AddTime, UpdateTime;
    private boolean isChecked;

    public AtMembersModel(int ProjectID, int UserID, int AddUserID, int UpdateUserID, String RoleID,
                          Object InTime, Object AddTime, Object UpdateTime
            , int UserInfoID, String UserName, String RoleInfoID, String RoleName, boolean isChecked) {
        this.ProjectID = ProjectID;
        this.UserID = UserID;
        this.AddUserID = AddUserID;
        this.UpdateUserID = UpdateUserID;

        this.RoleID = RoleID;
        this.InTime = InTime;
        this.AddTime = AddTime;
        this.UpdateTime = UpdateTime;
        this.UserInfoID = UserInfoID;
        this.UserName = UserName;
        this.RoleInfoID = RoleInfoID;
        this.RoleName = RoleName;
        this.isChecked = isChecked;

    }

    public int getProjectID() {
        return ProjectID;
    }

    public void setProjectID(int projectID) {
        ProjectID = projectID;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public int getAddUserID() {
        return AddUserID;
    }

    public void setAddUserID(int addUserID) {
        AddUserID = addUserID;
    }

    public int getUpdateUserID() {
        return UpdateUserID;
    }

    public void setUpdateUserID(int updateUserID) {
        UpdateUserID = updateUserID;
    }

    public String getRoleID() {
        return RoleID;
    }

    public void setRoleID(String roleID) {
        RoleID = roleID;
    }

    public Object getInTime() {
        return InTime;
    }

    public void setInTime(Object inTime) {
        InTime = inTime;
    }

    public Object getAddTime() {
        return AddTime;
    }

    public void setAddTime(Object addTime) {
        AddTime = addTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(Object updateTime) {
        UpdateTime = updateTime;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getUserInfoID() {
        return UserInfoID;
    }

    public void setUserInfoID(int userInfoID) {
        UserInfoID = userInfoID;
    }

    public String getRoleInfoID() {
        return RoleInfoID;
    }

    public void setRoleInfoID(String roleInfoID) {
        RoleInfoID = roleInfoID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getRoleName() {
        return RoleName;
    }

    public void setRoleName(String roleName) {
        RoleName = roleName;
    }
}
