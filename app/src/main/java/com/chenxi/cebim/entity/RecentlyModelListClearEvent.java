package com.chenxi.cebim.entity;

//模型列表最近模块清除列表evenBus的实体类
public class RecentlyModelListClearEvent {
    private String message;
    public RecentlyModelListClearEvent(String message){
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
