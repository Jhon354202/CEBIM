package com.chenxi.cebim.entity;

//功能编辑界面已选列表对象
public class FunctionEditSelected {

    private int picID;
    private String itemName;
    private Boolean isChecke;

    public FunctionEditSelected(int picID, String itemName, Boolean isChecke) {
        this.picID = picID;
        this.itemName = itemName;
        this.isChecke = isChecke;
    }

    public int getPicID() {
        return picID;
    }

    public String getItemName() {
        return itemName;
    }

    public Boolean getChecke() {
        return isChecke;
    }
}
