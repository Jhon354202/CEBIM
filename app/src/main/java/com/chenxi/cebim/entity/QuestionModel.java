package com.chenxi.cebim.entity;

/**
 * 协同问题模型
 */
public class QuestionModel {

    private Integer ProjectId,ClosedUserId,Priority,UserId,UpdatedBy;
    private String ID,Title,Comment,GroupId,Category,ViewportId,SystemType,At,Pictures,
            Uuids,SelectionSetIds,Video,Voice,Tags,ReadUsers,DocumentIds,UserName,CategoryName,SystemTypeName,firstFrame,ObservedUsers;

    private Boolean State,Observed,IsFinishedAndDelay;

    private Object CompletedAt,Deadline,Date,LastUpdate;


    public QuestionModel(Integer ProjectId,Integer ClosedUserId,Integer Priority,Integer UserId,Integer UpdatedBy,
                         String ID,String Title,String Comment,String GroupId,
                         String Category,String ViewportId,String SystemType,String At,String Pictures,
                         String Uuids,String SelectionSetIds,String Video,String Voice,String Tags,String ReadUsers,
                         String DocumentIds,String UserName,String CategoryName,String SystemTypeName,String firstFrame,
                         String ObservedUsers,Boolean State,Boolean Observed,Boolean IsFinishedAndDelay,
                         Object CompletedAt,Object Deadline,Object Date,Object LastUpdate){
        this.ProjectId=ProjectId;
        this.ClosedUserId=ClosedUserId;
        this.Priority=Priority;
        this.UserId=UserId;
        this.UpdatedBy=UpdatedBy;
        this.ID=ID;
        this.Title=Title;
        this.Comment=Comment;
        this.GroupId=GroupId;
        this.Category=Category;
        this.ViewportId=ViewportId;
        this.SystemType=SystemType;
        this.At=At;
        this.Pictures=Pictures;
        this.Uuids=Uuids;
        this.SelectionSetIds=SelectionSetIds;
        this.Video=Video;
        this.Voice=Voice;
        this.Tags=Tags;
        this.ReadUsers=ReadUsers;
        this.DocumentIds=DocumentIds;
        this.UserName=UserName;
        this.CategoryName=CategoryName;
        this.SystemTypeName=SystemTypeName;
        this.firstFrame=firstFrame;//图片或视频的第一帧图片地址
        this.ObservedUsers=ObservedUsers;
        this.State=State;
        this.Observed=Observed;
        this.IsFinishedAndDelay=IsFinishedAndDelay;
        this.CompletedAt=CompletedAt;
        this.Deadline=Deadline;
        this.Date=Date;
        this.LastUpdate=LastUpdate;

    }

    public Integer getProjectId() {
        return ProjectId;
    }

    public Integer getClosedUserId() {
        return ClosedUserId;
    }

    public Integer getPriority() {
        return Priority;
    }

    public Integer getUserId() {
        return UserId;
    }

    public Integer getUpdatedBy() {
        return UpdatedBy;
    }

    public String getFirstFrame() {
        return firstFrame;
    }

    public void setFirstFrame(String firstFrame) {
        this.firstFrame = firstFrame;
    }

    public String getID() {
        return ID;
    }

    public String getTitle() {
        return Title;
    }

    public String getComment() {
        return Comment;
    }

    public String getGroupId() {
        return GroupId;
    }

    public String getCategory() {
        return Category;
    }

    public String getViewportId() {
        return ViewportId;
    }

    public String getSystemType() {
        return SystemType;
    }

    public String getAt() {
        return At;
    }

    public String getPictures() {
        return Pictures;
    }

    public String getUuids() {
        return Uuids;
    }

    public String getSelectionSetIds() {
        return SelectionSetIds;
    }

    public String getVideo() {
        return Video;
    }

    public String getVoice() {
        return Voice;
    }

    public String getTags() {
        return Tags;
    }

    public String getReadUsers() {
        return ReadUsers;
    }

    public String getDocumentIds() {
        return DocumentIds;
    }

    public String getUserName() {
        return UserName;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public String getSystemTypeName() {
        return SystemTypeName;
    }

    public Boolean getState() {
        return State;
    }

    public Boolean getObserved() {
        return Observed;
    }

    public Boolean getFinishedAndDelay() {
        return IsFinishedAndDelay;
    }

    public Object getCompletedAt() {
        return CompletedAt;
    }

    public Object getDeadline() {
        return Deadline;
    }

    public Object getDate() {
        return Date;
    }

    public Object getLastUpdate() {
        return LastUpdate;
    }

    public void setProjectId(Integer projectId) {
        ProjectId = projectId;
    }

    public void setClosedUserId(Integer closedUserId) {
        ClosedUserId = closedUserId;
    }

    public void setPriority(Integer priority) {
        Priority = priority;
    }

    public void setUserId(Integer userId) {
        UserId = userId;
    }

    public void setUpdatedBy(Integer updatedBy) {
        UpdatedBy = updatedBy;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public void setViewportId(String viewportId) {
        ViewportId = viewportId;
    }

    public void setSystemType(String systemType) {
        SystemType = systemType;
    }

    public void setAt(String at) {
        At = at;
    }

    public void setPictures(String pictures) {
        Pictures = pictures;
    }

    public void setUuids(String uuids) {
        Uuids = uuids;
    }

    public void setSelectionSetIds(String selectionSetIds) {
        SelectionSetIds = selectionSetIds;
    }

    public void setVideo(String video) {
        Video = video;
    }

    public void setVoice(String voice) {
        Voice = voice;
    }

    public void setTags(String tags) {
        Tags = tags;
    }

    public void setReadUsers(String readUsers) {
        ReadUsers = readUsers;
    }

    public void setDocumentIds(String documentIds) {
        DocumentIds = documentIds;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public void setSystemTypeName(String systemTypeName) {
        SystemTypeName = systemTypeName;
    }

    public void setState(Boolean state) {
        State = state;
    }

    public void setObserved(Boolean observed) {
        Observed = observed;
    }

    public void setFinishedAndDelay(Boolean finishedAndDelay) {
        IsFinishedAndDelay = finishedAndDelay;
    }

    public void setCompletedAt(Object completedAt) {
        CompletedAt = completedAt;
    }

    public void setDeadline(Object deadline) {
        Deadline = deadline;
    }

    public void setDate(Object date) {
        Date = date;
    }

    public void setLastUpdate(Object lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public String getObservedUsers() {
        return ObservedUsers;
    }

    public void setObservedUsers(String observedUsers) {
        ObservedUsers = observedUsers;
    }
}
