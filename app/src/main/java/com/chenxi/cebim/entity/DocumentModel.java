package com.chenxi.cebim.entity;

/**
 * 附件实体
 */
public class DocumentModel {
    private String fileString, fileNama;

    public DocumentModel(String _id, String name) {
        this.fileString = _id;
        this.fileNama = name;
    }

    public String getFileString() {
        return fileString;
    }

    public void setFileString(String fileString) {
        this.fileString = fileString;
    }

    public String getFileNama() {
        return fileNama;
    }

    public void setFileNama(String fileNama) {
        this.fileNama = fileNama;
    }
}
