package com.chenxi.cebim.entity;

public class Project {

    private int image;
    private String projectName;
    private int projectId;

    public Project(String projectName,int projectId){
        this.image=image;
        this.projectName=projectName;
        this.projectId=projectId;

    }

    public int getImage() {
        return image;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getProjectId() {
        return projectId;
    }

}
