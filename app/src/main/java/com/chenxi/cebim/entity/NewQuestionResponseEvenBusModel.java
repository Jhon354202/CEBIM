package com.chenxi.cebim.entity;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

public class NewQuestionResponseEvenBusModel {

    private String info;

    //用于传递图片对象列表
    public NewQuestionResponseEvenBusModel(String info){
        this.info=info;
    }

    public String getInfo() {
        return info;
    }
}
