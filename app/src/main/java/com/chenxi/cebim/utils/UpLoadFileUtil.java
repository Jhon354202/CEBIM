package com.chenxi.cebim.utils;

/**
 * Created by qfdn on 2018/4/2.
 */

public class UpLoadFileUtil {

    /*
     * 获取FullName
     */
    public static String getFileName(String filePath) {

        int lastSlashIndex = filePath.lastIndexOf("/");//获取最后一个斜杠
        String picName = filePath.substring(lastSlashIndex + 1);//获取图片名
        return picName;
    }

}
