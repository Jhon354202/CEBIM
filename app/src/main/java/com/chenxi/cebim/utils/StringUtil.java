package com.chenxi.cebim.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Zhang JianLong on 2018/9/20.
 * End Time:
 * Description:字符串工具，用于去掉字符串中的空格、换行等
 */

public class StringUtil {

    public static String replaceBlank(String str) {

        String dest = "";

        if (str != null) {

            Pattern p = Pattern.compile("\\s*|\t|\r|\n");

            Matcher m = p.matcher(str);

            dest = m.replaceAll("");

        }

        return dest;

    }

    /**
     *去掉首位指定符号
     * @param source 源字符串
     * @param element 需要去除的元素
     * @return
     */
    public static String trimFirstAndLastChar(String source,char element){
        boolean beginIndexFlag = true;
        boolean endIndexFlag = true;
        do{
            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();
            LogUtil.i("source","..................:"+source+".....................");
            source = source.substring(beginIndex, endIndex);
            beginIndexFlag = (source.indexOf(element) == 0);
            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
        } while (beginIndexFlag || endIndexFlag);
        return source;
    }

}