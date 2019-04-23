package com.chenxi.cebim.entity;

//EvenBus实体，用于NewQuestion类中点击删除图片时刷新右侧图片数量
public class NewQuestionDelEven {


    private String info;

    public NewQuestionDelEven(String info){
        this.info=info;
    }

    public String getInfo() {
        return info;
    }

}
