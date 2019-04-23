package com.chenxi.cebim.entity;

import java.io.Serializable;

public class ReadUsersModel implements Serializable {

    private String Name;
    private int ID;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}