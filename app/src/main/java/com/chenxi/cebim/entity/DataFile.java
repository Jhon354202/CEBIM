package com.chenxi.cebim.entity;

import android.support.annotation.NonNull;

import org.json.JSONArray;

import java.io.Serializable;

public class DataFile implements Serializable,Comparable<DataFile> {
    private String _id,name,parentId,type,fileId,fileType,
            fileSize,suffix,fileName,unoconv,uploadedBy,createdBy,updatedBy;
    private int order;
    private Boolean frequent,generated;
    private JSONArray readUserIds,entity_uuids,entities;
    private Object projectId,thumbnail,pdfView,tag,uploadedAt,createdAt,updatedAt;

    public DataFile(String _id, String name, String parentId, String type, String fileId, String fileType,
                    String fileSize, String suffix, String fileName, String unoconv, String uploadedBy, String createdBy,
                    String updatedBy,int order,Boolean frequent,Boolean generated,JSONArray readUserIds,
                    Object projectId,Object thumbnail,Object pdfView,Object tag,Object uploadedAt,Object createdAt,Object updatedAt) {
        this._id = _id;
        this.name = name;
        this.parentId = parentId;
        this.type = type;

        this.fileId = fileId;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.suffix = suffix;

        this.fileName = fileName;
        this.unoconv = unoconv;
        this.uploadedBy = uploadedBy;
        this.createdBy = createdBy;

        this.updatedBy = updatedBy;
        this.order = order;
        this.frequent = frequent;
        this.generated = generated;

        this.readUserIds = readUserIds;
        this.entity_uuids = entity_uuids;
        this.entities = entities;
        this.projectId = projectId;

        this.thumbnail = thumbnail;
        this.pdfView = pdfView;
        this.tag = tag;
        this.uploadedAt = uploadedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getType() {
        return type;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUnoconv() {
        return unoconv;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public int getOrder() {
        return order;
    }

    public Boolean getFrequent() {
        return frequent;
    }

    public Boolean getGenerated() {
        return generated;
    }

    public JSONArray getReadUserIds() {
        return readUserIds;
    }

    public JSONArray getEntity_uuids() {
        return entity_uuids;
    }

    public JSONArray getEntities() {
        return entities;
    }

    public Object getProjectId() {
        return projectId;
    }

    public Object getThumbnail() {
        return thumbnail;
    }

    public Object getPdfView() {
        return pdfView;
    }

    public Object getTag() {
        return tag;
    }

    public Object getUploadedAt() {
        return uploadedAt;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public int compareTo(@NonNull DataFile dataFile) {
        int i = this.getOrder() - dataFile.getOrder();
        return i;
    }

}
