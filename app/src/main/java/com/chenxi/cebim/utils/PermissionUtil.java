package com.chenxi.cebim.utils;

import android.app.Activity;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtil {

    public static void addPermission(Activity activity, String permission, String whichPermission) {

        if (!EasyPermissions.hasPermissions(activity, permission)) {//检查是否获取该权限,没有则进行获取

            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(activity, whichPermission, 0, permission);
        }
    }
}
