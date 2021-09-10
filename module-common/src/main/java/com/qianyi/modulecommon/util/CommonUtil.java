package com.qianyi.modulecommon.util;

import org.springframework.util.ObjectUtils;

import java.util.Locale;
import java.util.Random;

public class CommonUtil {

    //返回num个随机数
    public static String random(int num) {

        String randomStr = "";
        Random random = new Random();
        for (int i = 0; i < num; i++) {
            //使用nextInt(int count)获得count以内的整数，不含count
            int j = random.nextInt(10);
            randomStr = randomStr + j;
        }

        return randomStr;
    }

    public static Boolean isWindows() {
        String os = System.getProperty("os.name");
        System.out.println(os);
        if (os.toLowerCase().startsWith("win")) {
            return true;
        }
        return false;
    }

    public static boolean checkNull(String... args) {
        if (args == null || args.length == 0) {
            return true;
        }

        for (String arg : args) {
            if (ObjectUtils.isEmpty(arg)) {
                return true;
            }
        }

        return false;
    }

    public static String getLocalPicPath() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            return "D:/pic/";
        } else {
            return "/usr/path/";
        }
    }
}
