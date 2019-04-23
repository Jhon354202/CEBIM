package com.chenxi.cebim.entity;

public class RecentlyModelListFreshenEvent {
    private String message;
    public RecentlyModelListFreshenEvent(String message){
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
