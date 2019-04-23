package com.chenxi.cebim.utils;

import java.text.DecimalFormat;

public class Division {

    //证书相除，获取一位小数点
    public static String division(long a ,int b){
        String result = "";
        float num =(float)a/b;

        DecimalFormat df = new DecimalFormat("0.0");

        result = df.format(num);

        return result;

    }

}
