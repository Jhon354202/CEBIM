package com.chenxi.cebim.entity;

public class Part {
    String partName;
    boolean isCheck;

    public Part(String partName, boolean isCheck) {
        this.partName = partName;
        this.isCheck = isCheck;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
