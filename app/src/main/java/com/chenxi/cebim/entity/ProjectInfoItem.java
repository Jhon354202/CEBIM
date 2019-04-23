package com.chenxi.cebim.entity;

import java.io.Serializable;

/**
 * 自己创建的对象，用于写界面，后期有接口后替换
 */
public class ProjectInfoItem implements Serializable {
    private String itemName;
    private int image;
    private Boolean isSelected;

    public ProjectInfoItem(String itemName,int image,Boolean isSelected){
        this.itemName=itemName;
        this.image=image;
        this.isSelected=isSelected;
    }

    public ProjectInfoItem(){

    }

    public String getItemName() {
        return itemName;
    }

    public int getImage() {
        return image;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}
