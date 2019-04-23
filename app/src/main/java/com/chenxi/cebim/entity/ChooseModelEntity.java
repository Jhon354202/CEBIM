package com.chenxi.cebim.entity;

import java.io.Serializable;

/**
 * 选择模型实体
 */
public class ChooseModelEntity implements Serializable{
    private String modelName;
    private int modelID;
    private boolean ischoosed;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getModelID() {
        return modelID;
    }

    public void setModelID(int modelID) {
        this.modelID = modelID;
    }

    public boolean isIschoosed() {
        return ischoosed;
    }

    public void setIschoosed(boolean ischoosed) {
        this.ischoosed = ischoosed;
    }
}
