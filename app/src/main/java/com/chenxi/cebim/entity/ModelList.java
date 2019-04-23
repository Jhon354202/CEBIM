package com.chenxi.cebim.entity;

import java.io.Serializable;

public class ModelList implements Serializable {
    private int modelId;
    private String modelName;
    private String addTime;
    private String updateTime;

    public ModelList(int modelId, String modelName, String addTime, String updateTime) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.addTime = addTime;
        this.updateTime = updateTime;
    }

    public int getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getAddTime() {
        return addTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelList other = (ModelList) obj;
        if (modelId != other.modelId)
                return false;

        if(modelName==null){
            if(other.modelName!=null)
                return false;
        }else if(!modelName.equals(other.modelName))
            return false;

        if(addTime==null){
            if(other.addTime!=null)
                return false;
        }else if(!addTime.equals(other.addTime))
            return false;

        if(updateTime==null){
            if(other.updateTime!=null)
                return false;
        }else if(!updateTime.equals(other.updateTime))
            return false;

            return true;
    }
}
