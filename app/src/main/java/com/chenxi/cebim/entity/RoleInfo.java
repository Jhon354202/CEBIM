package com.chenxi.cebim.entity;

import java.io.Serializable;

public class RoleInfo implements Serializable {

    private String Name,ID;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}

