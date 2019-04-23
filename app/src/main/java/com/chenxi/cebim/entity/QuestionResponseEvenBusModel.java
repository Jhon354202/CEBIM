package com.chenxi.cebim.entity;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

public class QuestionResponseEvenBusModel {

    private List<LocalMedia> localMedialList;

    //用于传递图片对象列表
    public QuestionResponseEvenBusModel(List<LocalMedia> localMedialList){
        this.localMedialList=localMedialList;
    }

    public List<LocalMedia> getLocalMedialList() {
        return localMedialList;
    }

}
