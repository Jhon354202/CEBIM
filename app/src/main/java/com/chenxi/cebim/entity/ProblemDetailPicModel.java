package com.chenxi.cebim.entity;

import java.io.Serializable;

public class ProblemDetailPicModel implements Serializable{

    private String ID;
    private String Name;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
