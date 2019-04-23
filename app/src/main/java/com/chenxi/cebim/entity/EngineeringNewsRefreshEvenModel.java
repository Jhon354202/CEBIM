package com.chenxi.cebim.entity;

public class EngineeringNewsRefreshEvenModel {
    private String info;

    //用于刷新工程动态
    public EngineeringNewsRefreshEvenModel(String info){
        this.info=info;
    }

    public String getInfo() {
        return info;
    }
}
