package com.chenxi.cebim.entity;

//EvenBus实体，用于DataFileFragment选择某个item时，弹出底部的导航栏（分享、打开、常用、更多）
public class IsShowBottomSettingButton {
    private String info;

    public IsShowBottomSettingButton(String info){
        this.info=info;
    }

    public String getInfo() {
        return info;
    }
}
