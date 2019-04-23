package com.chenxi.cebim.entity;

import java.util.List;

public class RecentDownLoadEvent {
    private List<TbFileShowmodel> tbFileShowmodelList;

    public RecentDownLoadEvent(List<TbFileShowmodel> tbFileShowmodelList){
        this.tbFileShowmodelList=tbFileShowmodelList;
    }

    public List<TbFileShowmodel> getTbFileShowmodelList() {
        return tbFileShowmodelList;
    }

    public void setTbFileShowmodelList(List<TbFileShowmodel> tbFileShowmodelList) {
        this.tbFileShowmodelList = tbFileShowmodelList;
    }

}
