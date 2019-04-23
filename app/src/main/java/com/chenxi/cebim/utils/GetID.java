package com.chenxi.cebim.utils;

/**
 * 获取各种id
 */
public class GetID {
    public static int projectID;//当前项目id
    public static int userID;//当前用户id
    public static int userName;//当前用户名


    public static int getProjectID() {
        return projectID;
    }

    public static void setProjectID(int projectID) {
        GetID.projectID = projectID;
    }

    public static int getUserID() {
        return userID;
    }

    public static void setUserID(int userID) {
        GetID.userID = userID;
    }

    public static int getUserName() {
        return userName;
    }

    public static void setUserName(int userName) {
        GetID.userName = userName;
    }
}
