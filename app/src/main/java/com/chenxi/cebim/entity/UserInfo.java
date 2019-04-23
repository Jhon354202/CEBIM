package com.chenxi.cebim.entity;

import java.io.Serializable;

/**
 * QuestionModel中的子对象
 */
public class UserInfo implements Serializable {

    private int UserID;
    private String UserName;

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int UserID) {
        this.UserID = UserID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String UserName) {
        this.UserName = UserName;
    }

}
