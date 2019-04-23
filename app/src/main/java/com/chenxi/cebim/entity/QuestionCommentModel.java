package com.chenxi.cebim.entity;

import java.io.Serializable;

public class QuestionCommentModel implements Serializable {

    private String ID,TopicId,UserId,Comment,Pictures,DocumentIds,Voice,Video,UserName;
    private String At;
    private Object Date;

    public QuestionCommentModel(String ID,String TopicId,String UserId,String At,Object Date,String Comment,
                                String Pictures,String DocumentIds,String Voice,String Video,String UserName){
        this.ID=ID;
        this.TopicId=TopicId;
        this.UserId=UserId;
        this.At=At;
        this.Date=Date;
        this.Comment=Comment;
        this.Pictures=Pictures;
        this.DocumentIds=DocumentIds;
        this.Voice=Voice;
        this.Video=Video;
        this.UserName=UserName;
    }

    public String getTopicId() {
        return TopicId;
    }

    public void setTopicId(String topicId) {
        TopicId = topicId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getPictures() {
        return Pictures;
    }

    public void setPictures(String pictures) {
        Pictures = pictures;
    }

    public String getDocumentIds() {
        return DocumentIds;
    }

    public void setDocumentIds(String documentIds) {
        DocumentIds = documentIds;
    }

    public String getVoice() {
        return Voice;
    }

    public void setVoice(String voice) {
        Voice = voice;
    }

    public String getVideo() {
        return Video;
    }

    public void setVideo(String video) {
        Video = video;
    }

    public String getAt() {
        return At;
    }

    public void setAt(String at) {
        At = at;
    }

    public Object getDate() {
        return Date;
    }

    public void setDate(Object date) {
        Date = date;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }
}
