package com.chenxi.cebim.entity;

public class DiscussionGroupModel {
    private String ID, Name,Users;
    private int ProjectId, CreatedBy, UpdatedBy;
    private Object CreatedAt, UpdatedAt;

    public DiscussionGroupModel(String ID, String Name, String Users, int ProjectId,
                                   int CreatedBy, int UpdatedBy, Object CreatedAt,
                                   Object UpdatedAt) {
        this.ID = ID;//类型ID
        this.Name = Name;//名称
        this.Users = Users;//用户
        this.ProjectId = ProjectId;//项目id
        this.CreatedBy = CreatedBy;//创建时间
        this.UpdatedBy = UpdatedBy;//创建人
        this.CreatedAt = CreatedAt;//修改时间
        this.UpdatedAt = UpdatedAt;//修改人
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUsers() {
        return Users;
    }

    public void setUsers(String users) {
        Users = users;
    }

    public int getProjectId() {
        return ProjectId;
    }

    public void setProjectId(int projectId) {
        ProjectId = projectId;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public int getUpdatedBy() {
        return UpdatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        UpdatedBy = updatedBy;
    }

    public Object getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(Object createdAt) {
        CreatedAt = createdAt;
    }

    public Object getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        UpdatedAt = updatedAt;
    }
}