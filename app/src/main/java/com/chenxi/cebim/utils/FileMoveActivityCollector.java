package com.chenxi.cebim.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于移动文件功能的一次性退回到指定Activity
 */
public class FileMoveActivityCollector {
    public static List<Activity> activities = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
