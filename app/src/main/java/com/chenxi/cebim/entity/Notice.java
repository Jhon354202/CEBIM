package com.chenxi.cebim.entity;

import java.io.Serializable;

public class Notice implements Serializable {

    private int image;
    private String noticeName;

    public Notice(int image, String noticeName){
        this.image=image;
        this.noticeName=noticeName;
    }

    public int getImage() {
        return image;
    }

    public String getNoticeName() {
        return noticeName;
    }
}
