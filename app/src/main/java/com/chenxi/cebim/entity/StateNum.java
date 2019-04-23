package com.chenxi.cebim.entity;

import java.io.Serializable;
import java.util.List;

public class StateNum implements Serializable {
    String state;
    int num;
    boolean isClick;
    String color;
    List<String> list;
    List<Integer> modelIds;

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    public StateNum(String state, int num, String color, List<String> list) {
        this.list = list;
        this.state = state;
        this.num = num;
        this.color = color;
    }

    public StateNum(String state, int num, String color, List<String> list, List<Integer> modelIds) {
        this.list = list;
        this.state = state;
        this.num = num;
        this.color = color;
        this.modelIds = modelIds;
    }

    public List<Integer> getModelIds() {
        return modelIds;
    }

    public void setModelIds(List<Integer> modelIds) {
        this.modelIds = modelIds;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
