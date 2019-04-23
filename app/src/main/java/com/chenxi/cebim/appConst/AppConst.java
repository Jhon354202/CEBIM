package com.chenxi.cebim.appConst;

import com.chenxi.cebim.application.MyApplication;
import com.chenxi.cebim.utils.DiskCacheDirUtil;

public class AppConst {
    /**
     * IP
     */
    public static final String innerIp ="http://114.115.160.197:8002";//最新外网
//    public static final String innerIp ="http://192.168.0.233:1801";//最新外网

    public static final String savePath ="" + DiskCacheDirUtil.getDiskCacheDir(MyApplication.getContext());// 已下载文件的本地存储地址
}