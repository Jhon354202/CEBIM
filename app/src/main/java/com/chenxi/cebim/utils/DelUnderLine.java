package com.chenxi.cebim.utils;

import android.graphics.Color;
import android.support.v7.widget.SearchView;
import android.view.View;

import java.lang.reflect.Field;

public class DelUnderLine {

    //去掉搜索框的下划线
    public static void delUnderLine(SearchView sv) {
        if (sv != null) {
            try {        //--拿到字节码
                Class<?> argClass = sv.getClass();
                //--指定某个私有属性,mSearchPlate是搜索框父布局的名字
                Field ownField = argClass.getDeclaredField("mSearchPlate");
                //--暴力反射,只有暴力反射才能拿到私有属性
                ownField.setAccessible(true);
                View mView = (View) ownField.get(sv);
                //--设置背景
                mView.setBackgroundColor(Color.TRANSPARENT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
