package com.chenxi.cebim.entity;

import java.util.List;

public class ModelstatisticsFather {
    private String title;
    private List<Modelstatistics> modelstatisticsList;//二级数据列表
    private int allNum;
    private int downloadNum;
    private boolean isEdit;
    private boolean isChoose;

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public int getAllNum() {
        return allNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public int getDownloadNum() {
        return downloadNum;
    }

    public void setDownloadNum(int downloadNum) {
        this.downloadNum = downloadNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Modelstatistics> getModelstatisticsList() {
        return modelstatisticsList;
    }

    public void setModelstatisticsList(List<Modelstatistics> modelstatisticsList) {
        this.modelstatisticsList = modelstatisticsList;
    }

    public void toggle() {
        this.isChoose = !this.isChoose;
    }

    public int getChildCount() {
        return modelstatisticsList.size();
    }

    public Modelstatistics getChildItem(int index) {
        return modelstatisticsList.get(index);
    }

}
