package com.chenxi.cebim.entity;

public class Data {
    private String dataName;
    private boolean isCheck;

    public Data(String dataName, boolean isCheck) {
        this.dataName = dataName;
        this.isCheck = isCheck;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
