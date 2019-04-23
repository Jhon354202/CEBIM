package com.chenxi.cebim.utils;

import java.text.DecimalFormat;

/**
 * Created by qfdn on 2018/3/21.
 */

public class NumberUtil {

    public static String numLengthControl(int i){

        String str = (i+"").toString();
        StringBuffer sb = new StringBuffer();
        for(int j = 0; j < 2 - str.length(); j++) {
            sb.append("0");
        }
        sb.append(str);
        str = sb.toString();

        return str;
    }

    /**
     * double转String,保留小数点后两位
     * @param num
     * @return
     */
    public static double doubleToString(double num){
        //使用0.00不足位补0，#.##仅保留有效位
        return Double.parseDouble(new DecimalFormat("0.00").format(num));
    }
}
