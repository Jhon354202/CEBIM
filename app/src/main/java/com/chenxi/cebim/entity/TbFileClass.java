package com.chenxi.cebim.entity;

public class TbFileClass {
    private int ClassID,ProjectID,ParentClassID,OrderNo,OperationUserID;
    private String ClassName;
    private Boolean IsUse;
    private Object AddTime,UpdateTime;

    public TbFileClass(int ClassID,int ProjectID,int ParentClassID,int OrderNo,int OperationUserID,
                       String ClassName,Boolean IsUse,Object AddTime,Object UpdateTime) {

        this.ClassID = ClassID;
        this.ProjectID = ProjectID;
        this.ParentClassID = ParentClassID;
        this.OrderNo = OrderNo;
        this.OperationUserID = OperationUserID;
        this.ClassName = ClassName;
        this.IsUse = IsUse;
        this.AddTime = AddTime;
        this.UpdateTime = UpdateTime;
    }

    public int getClassID() {
        return ClassID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public int getParentClassID() {
        return ParentClassID;
    }

    public int getOrderNo() {
        return OrderNo;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public String getClassName() {
        return ClassName;
    }

    public Boolean getUse() {
        return IsUse;
    }

    public Object getAddTime() {
        return AddTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }
}
