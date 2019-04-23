package com.chenxi.cebim.entity;

//资料下的文件对象
public class TbFile {
    private int FID,ProjectID,ClassID,BIMModelID,OperationUserID;
    private String FileName,OnlySign,Remark,Tags,FileID;
    private Boolean InRecycle;
    Object AddTime,UpdateTime;

    public TbFile(int FID, int ProjectID, int ClassID, int BIMModelID, int OperationUserID,
                  String FileName, String OnlySign, String Remark, String Tags, String FileID,
                  Boolean InRecycle, Object AddTime, Object UpdateTime) {

        this.FID = FID;
        this.ProjectID = ProjectID;
        this.ClassID = ClassID;
        this.BIMModelID = BIMModelID;
        this.OperationUserID = OperationUserID;
        this.FileName = FileName;
        this.OnlySign = OnlySign;
        this.Remark = Remark;
        this.Tags = Tags;
        this.FileID = FileID;
        this.InRecycle = InRecycle;
        this.AddTime = AddTime;
        this.UpdateTime = UpdateTime;

    }

    public int getFID() {
        return FID;
    }

    public int getProjectID() {
        return ProjectID;
    }

    public int getClassID() {
        return ClassID;
    }

    public int getBIMModelID() {
        return BIMModelID;
    }

    public int getOperationUserID() {
        return OperationUserID;
    }

    public String getFileName() {
        return FileName;
    }

    public String getOnlySign() {
        return OnlySign;
    }

    public String getRemark() {
        return Remark;
    }

    public String getTags() {
        return Tags;
    }

    public String getFileID() {
        return FileID;
    }

    public Boolean getInRecycle() {
        return InRecycle;
    }

    public Object getAddTime() {
        return AddTime;
    }

    public Object getUpdateTime() {
        return UpdateTime;
    }

}
