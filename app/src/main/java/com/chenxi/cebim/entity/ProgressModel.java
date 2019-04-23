package com.chenxi.cebim.entity;

public class ProgressModel {
    private int EPPID, ParentEPPID, ProjectID, PlanTimeLimit, Sort, GrowthType, OperationUserID;
    private String SerialNumber, ProcessName, OnlySign;
    private Boolean IsTaskGroup, IsShow;
    private Object PlanBeginTime, PlanEndTime, ActualBeginTime, ActualEndTime, AddTime, UpdateTime,Progress;

    public ProgressModel(int EPPID, int ParentEPPID, int ProjectID, int PlanTimeLimit,
                         int Sort, int GrowthType, int OperationUserID, String SerialNumber, String ProcessName,
                         String OnlySign, Boolean IsTaskGroup, Boolean IsShow, Object PlanBeginTime,Object PlanEndTime,
                         Object ActualBeginTime, Object ActualEndTime, Object AddTime, Object UpdateTime,Object Progress) {
        this.EPPID = EPPID;
        this.ParentEPPID = ParentEPPID;
        this.ProjectID = ProjectID;

        this.PlanTimeLimit = PlanTimeLimit;
        this.Sort = Sort;
        this.GrowthType = GrowthType;
        this.OperationUserID = OperationUserID;

        this.SerialNumber = SerialNumber;
        this.ProcessName = ProcessName;
        this.OnlySign = OnlySign;
        this.IsTaskGroup = IsTaskGroup;

        this.IsShow = IsShow;
        this.PlanBeginTime = PlanBeginTime;
        this.PlanEndTime = PlanEndTime;
        this.ActualBeginTime = ActualBeginTime;

        this.ActualEndTime = ActualEndTime;
        this.AddTime = AddTime;
        this.UpdateTime = UpdateTime;
        this.Progress=Progress;
    }

    public int getEPPID() {
        return EPPID;
    }

    public int getParentEPPID() {
        return ParentEPPID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public int getPlanTimeLimit() {
        return PlanTimeLimit;
    }

    public int getSort() {
        return Sort;
    }

    public int getGrowthType() {
        return GrowthType;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public String getProcessName() {
        return ProcessName;
    }

    public String getOnlySign() {
        return OnlySign;
    }

    public Boolean getTaskGroup() {
        return IsTaskGroup;
    }

    public Boolean getShow() {
        return IsShow;
    }

    public Object getPlanBeginTime() {
        return PlanBeginTime;
    }

    public Object getPlanEndTime() {
        return PlanEndTime;
    }

    public Object getActualBeginTime() {
        return ActualBeginTime;
    }

    public Object getActualEndTime() {
        return ActualEndTime;
    }

    public Object getAddTime() {
        return AddTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }

    public Object getProgress() {
        return Progress;
    }
}
