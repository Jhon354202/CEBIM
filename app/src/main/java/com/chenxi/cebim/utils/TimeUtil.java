package com.chenxi.cebim.utils;

import java.util.Calendar;

public class TimeUtil {

    static Calendar now = Calendar.getInstance();

    // 返回年
    public static int getyear() {
        int year = now.get(Calendar.YEAR);

        return year;
    }

    // 返回月
    public static int getmonth() {
        int month = now.get(Calendar.MONTH) + 1;

        return month;
    }

    // 返回日
    public static int getday() {
        int day = now.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    // 返回时
    public static int gethour() {
        int hour = now.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    // 返回分
    public static int getminute() {
        int minute = now.get(Calendar.MINUTE);
        return minute;
    }

    // 返回秒
    public static int getsecond() {
        int second = now.get(Calendar.SECOND);
        return second;
    }

}

