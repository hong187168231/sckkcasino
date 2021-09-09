package com.qianyi.modulecommon.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private static String patten = "yyyy-MM-dd HH:mm:ss";

    public static SimpleDateFormat getSimpleDateFormat(String patten) {
        return new SimpleDateFormat(patten);
    }
    
     public static SimpleDateFormat getSimpleDateFormat() {
         return getSimpleDateFormat(patten);
     }

    public static String today(String patten) {
        return getSimpleDateFormat(patten).format(new Date());
    }

    public static String today() {
        return today(patten);
    }
}
