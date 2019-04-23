package com.chenxi.cebim.entity;

import java.io.Serializable;

public class SynergyTaskEntity implements Serializable {

    private String ID,UserIds,RelativeUserIds,ActualStartDate,ActualFinishDate,Remark,MaterialStartAt,
            MaterialFinishAt,ActualUnit,ModelUnit,DocumentIds,Name,StartDate,FinishDate,NewDuration,
            DurationText,StartText,FinishText,Notes;

    private int PlanId,ProjectId,ActualDuration,PlanLabor,PracticalLaborSum,Priority,Serial,Duration,
            CreatedBy,UpdatedBy;

    private Object AssignedAt,DistributedAt,CreatedAt,UpdatedAt;

    public SynergyTaskEntity(String ID,String UserIds,String RelativeUserIds,String ActualStartDate,
                             String ActualFinishDate,String Remark,String MaterialStartAt,
                             String MaterialFinishAt,String ActualUnit,String ModelUnit,
                             String DocumentIds,String Name,String StartDate,String FinishDate,
                             String NewDuration,String DurationText,String StartText,String FinishText,
                             String Notes,int PlanId,int ProjectId,int ActualDuration,int PlanLabor,
                             int PracticalLaborSum,int Priority,int Serial,int Duration,int CreatedBy,
                             int UpdatedBy,Object AssignedAt,Object DistributedAt,Object CreatedAt,Object UpdatedAt){
        this.ID=ID;
        this.UserIds=UserIds;
        this.RelativeUserIds=RelativeUserIds;
        this.ActualStartDate=ActualStartDate;
        this.ActualFinishDate=ActualFinishDate;
        this.Remark=Remark;
        this.MaterialStartAt=MaterialStartAt;
        this.MaterialFinishAt=MaterialFinishAt;
        this.ActualUnit=ActualUnit;
        this.ModelUnit=ModelUnit;
        this.DocumentIds=DocumentIds;
        this.Name=Name;
        this.StartDate=StartDate;
        this.FinishDate=FinishDate;
        this.NewDuration=NewDuration;
        this.DurationText=DurationText;
        this.StartText=StartText;
        this.FinishText=FinishText;
        this.Notes=Notes;
        this.PlanId=PlanId;
        this.ProjectId=ProjectId;
        this.ActualDuration=ActualDuration;
        this.PlanLabor=PlanLabor;
        this.PracticalLaborSum=PracticalLaborSum;
        this.Priority=Priority;
        this.Serial=Serial;
        this.Duration=Duration;
        this.CreatedBy=CreatedBy;
        this.UpdatedBy=UpdatedBy;
        this.AssignedAt=AssignedAt;
        this.DistributedAt=DistributedAt;
        this.CreatedAt=CreatedAt;
        this.UpdatedAt=UpdatedAt;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserIds() {
        return UserIds;
    }

    public void setUserIds(String userIds) {
        UserIds = userIds;
    }

    public String getRelativeUserIds() {
        return RelativeUserIds;
    }

    public void setRelativeUserIds(String relativeUserIds) {
        RelativeUserIds = relativeUserIds;
    }

    public String getActualStartDate() {
        return ActualStartDate;
    }

    public void setActualStartDate(String actualStartDate) {
        ActualStartDate = actualStartDate;
    }

    public String getActualFinishDate() {
        return ActualFinishDate;
    }

    public void setActualFinishDate(String actualFinishDate) {
        ActualFinishDate = actualFinishDate;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getMaterialStartAt() {
        return MaterialStartAt;
    }

    public void setMaterialStartAt(String materialStartAt) {
        MaterialStartAt = materialStartAt;
    }

    public String getMaterialFinishAt() {
        return MaterialFinishAt;
    }

    public void setMaterialFinishAt(String materialFinishAt) {
        MaterialFinishAt = materialFinishAt;
    }

    public String getActualUnit() {
        return ActualUnit;
    }

    public void setActualUnit(String actualUnit) {
        ActualUnit = actualUnit;
    }

    public String getModelUnit() {
        return ModelUnit;
    }

    public void setModelUnit(String modelUnit) {
        ModelUnit = modelUnit;
    }

    public String getDocumentIds() {
        return DocumentIds;
    }

    public void setDocumentIds(String documentIds) {
        DocumentIds = documentIds;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStartDate() {
        return StartDate;
    }

    public void setStartDate(String startDate) {
        StartDate = startDate;
    }

    public String getFinishDate() {
        return FinishDate;
    }

    public void setFinishDate(String finishDate) {
        FinishDate = finishDate;
    }

    public String getNewDuration() {
        return NewDuration;
    }

    public void setNewDuration(String newDuration) {
        NewDuration = newDuration;
    }

    public String getDurationText() {
        return DurationText;
    }

    public void setDurationText(String durationText) {
        DurationText = durationText;
    }

    public String getStartText() {
        return StartText;
    }

    public void setStartText(String startText) {
        StartText = startText;
    }

    public String getFinishText() {
        return FinishText;
    }

    public void setFinishText(String finishText) {
        FinishText = finishText;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public int getPlanId() {
        return PlanId;
    }

    public void setPlanId(int planId) {
        PlanId = planId;
    }

    public int getProjectId() {
        return ProjectId;
    }

    public void setProjectId(int projectId) {
        ProjectId = projectId;
    }

    public int getActualDuration() {
        return ActualDuration;
    }

    public void setActualDuration(int actualDuration) {
        ActualDuration = actualDuration;
    }

    public int getPlanLabor() {
        return PlanLabor;
    }

    public void setPlanLabor(int planLabor) {
        PlanLabor = planLabor;
    }

    public int getPracticalLaborSum() {
        return PracticalLaborSum;
    }

    public void setPracticalLaborSum(int practicalLaborSum) {
        PracticalLaborSum = practicalLaborSum;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public int getSerial() {
        return Serial;
    }

    public void setSerial(int serial) {
        Serial = serial;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
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

    public Object getAssignedAt() {
        return AssignedAt;
    }

    public void setAssignedAt(Object assignedAt) {
        AssignedAt = assignedAt;
    }

    public Object getDistributedAt() {
        return DistributedAt;
    }

    public void setDistributedAt(Object distributedAt) {
        DistributedAt = distributedAt;
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
