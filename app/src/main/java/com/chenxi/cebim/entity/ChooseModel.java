package com.chenxi.cebim.entity;

import java.io.Serializable;

public class ChooseModel implements Serializable {
    private int modelId;
    private String Id;

    public ChooseModel(int modelId, String Id) {
        this.modelId = modelId;
        this.Id = Id;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
